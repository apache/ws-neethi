/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.neethi;

import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.neethi.PolicyReference.MAX_REMOTE_POLICY_BYTES_PROPERTY;

import org.junit.Test;

/**
 * Tests for PolicyReference URI scheme validation (CWE-918 / SSRF hardening).
 *
 * getRemoteReferencedPolicy() must only allow http and https schemes and must
 * reject all others (file://, ftp://, jar://, etc.) before opening any connection.
 */
public class PolicyReferenceTest extends PolicyTestCase {

    /**
     * Loads a policy from test-policy-reference.xml and verifies the policy name
     * is parsed correctly. Normalization is not called, so no outbound call is made.
     */
    @Test
    public void testGetPolicyFromPolicyReferenceFile() throws Exception {
        Policy policy = getPolicy("samples/test-policy-reference.xml");
        assertNotNull(policy);
        assertEquals("OuterPolicy", policy.getName());
    }

    // -----------------------------------------------------------------------
    // Scheme allow-list: blocked schemes
    // -----------------------------------------------------------------------

    @Test(expected = RuntimeException.class)
    public void testFileSchemeIsRejected() throws Exception {
        File policyFile = new File(baseDir, testResourceDir + File.separator
                + "samples" + File.separator + "test-policy-reference.xml");
        PolicyReference ref = new PolicyReference(policyEngine);
        ref.getRemoteReferencedPolicy(policyFile.toURI().toString()); // file:///...
    }

    @Test(expected = RuntimeException.class)
    public void testFtpSchemeIsRejected() throws Exception {
        PolicyReference ref = new PolicyReference(policyEngine);
        ref.getRemoteReferencedPolicy("ftp://example.com/policy.xml");
    }

    @Test(expected = RuntimeException.class)
    public void testJarSchemeIsRejected() throws Exception {
        PolicyReference ref = new PolicyReference(policyEngine);
        ref.getRemoteReferencedPolicy("jar:file:///some.jar!/policy.xml");
    }

    @Test(expected = RuntimeException.class)
    public void testMalformedUriIsRejected() throws Exception {
        PolicyReference ref = new PolicyReference(policyEngine);
        ref.getRemoteReferencedPolicy("not a url at all");
    }

    // -----------------------------------------------------------------------
    // Scheme allow-list: ftp:// must not trigger an outbound connection
    // -----------------------------------------------------------------------

    /**
     * Verifies that a ftp:// URI is rejected BEFORE any TCP connection is made.
     * Opens a local ServerSocket and confirms no connection arrives within the
     * timeout after the call is rejected.
     */
    @Test
    public void testFtpSchemeDoesNotTriggerOutboundConnection() throws Exception {
        AtomicBoolean connectionReceived = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(500);
            final int port = server.getLocalPort();

            Thread acceptThread = new Thread(() -> {
                try {
                    Socket accepted = server.accept();
                    connectionReceived.set(true);
                    accepted.close();
                } catch (IOException ignored) {
                    // timeout — expected: no connection should arrive
                } finally {
                    latch.countDown();
                }
            });
            acceptThread.setDaemon(true);
            acceptThread.start();

            PolicyReference ref = new PolicyReference(policyEngine);
            try {
                ref.getRemoteReferencedPolicy("ftp://127.0.0.1:" + port + "/policy.xml");
                fail("Expected RuntimeException for ftp:// scheme");
            } catch (RuntimeException expected) {
                // correct — rejected before connecting
            }

            latch.await(1, TimeUnit.SECONDS);
        }

        assertFalse(
            "ftp:// scheme triggered an outbound TCP connection — scheme allow-list not enforced",
            connectionReceived.get());
    }

    // -----------------------------------------------------------------------
    // Scheme allow-list: http and https are permitted
    // -----------------------------------------------------------------------

    /**
     * Verifies that http:// URIs are accepted by the scheme check (the connection
     * will fail with a RuntimeException due to the server refusing/not existing,
     * but the exception must NOT be about an unsupported scheme).
     */
    @Test
    public void testHttpSchemeIsPermitted() {
        PolicyReference ref = new PolicyReference(policyEngine);
        try {
            // Use a port that will immediately refuse the connection so the test
            // does not block, but the scheme validation must pass.
            ref.getRemoteReferencedPolicy("http://127.0.0.1:1/policy.xml");
            fail("Expected RuntimeException due to connection failure");
        } catch (RuntimeException e) {
            assertFalse(
                "http:// was rejected by scheme allow-list — it should be permitted",
                e.getMessage().contains("Unsupported URI scheme"));
        }
    }

    @Test
    public void testHttpsSchemeIsPermitted() {
        PolicyReference ref = new PolicyReference(policyEngine);
        try {
            ref.getRemoteReferencedPolicy("https://127.0.0.1:1/policy.xml");
            fail("Expected RuntimeException due to connection failure");
        } catch (RuntimeException e) {
            assertFalse(
                "https:// was rejected by scheme allow-list — it should be permitted",
                e.getMessage().contains("Unsupported URI scheme"));
        }
    }

    /**
     * Ensures circular references are detected for policies with explicit Id values.
     */
    @Test
    public void testCircularPolicyReferenceWithIdsIsDetected() {
        // Build Policy A: ExactlyOne > All > PolicyReference("B")
        PolicyReference refToB = new PolicyReference();
        refToB.setURI("B");
        All allA = new All();
        allA.addPolicyComponent(refToB);
        ExactlyOne eoA = new ExactlyOne();
        eoA.addPolicyComponent(allA);
        Policy policyA = new Policy(registry);
        policyA.setId("A");
        policyA.addPolicyComponent(eoA);

        // Build Policy B: ExactlyOne > All > PolicyReference("A")
        PolicyReference refToA = new PolicyReference();
        refToA.setURI("A");
        All allB = new All();
        allB.addPolicyComponent(refToA);
        ExactlyOne eoB = new ExactlyOne();
        eoB.addPolicyComponent(allB);
        Policy policyB = new Policy(registry);
        policyB.setId("B");
        policyB.addPolicyComponent(eoB);

        registry.register("A", policyA);
        registry.register("B", policyB);

        try {
            policyA.normalize(registry, true);
            fail("Expected RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }

    /**
     * Ensures circular references are detected even when referenced policies have no Id.
     *
     * Cycle detection must use a stable non-null resolution token so that
     * no-Id references cannot bypass the resolving set.
     */
    @Test
    public void testCircularPolicyReferenceWithoutIdsIsDetected() {
        // Policy A references registry key "B" but has no policy Id.
        PolicyReference refToB = new PolicyReference();
        refToB.setURI("B");
        All allA = new All();
        allA.addPolicyComponent(refToB);
        ExactlyOne eoA = new ExactlyOne();
        eoA.addPolicyComponent(allA);
        Policy policyA = new Policy(registry);
        policyA.addPolicyComponent(eoA);

        // Policy B references registry key "A" and also has no policy Id.
        PolicyReference refToA = new PolicyReference();
        refToA.setURI("A");
        All allB = new All();
        allB.addPolicyComponent(refToA);
        ExactlyOne eoB = new ExactlyOne();
        eoB.addPolicyComponent(allB);
        Policy policyB = new Policy(registry);
        policyB.addPolicyComponent(eoB);

        registry.register("A", policyA);
        registry.register("B", policyB);

        try {
            policyA.normalize(registry, true);
            fail("Expected RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }

    @Test
    public void testRemotePolicyDeclaredContentLengthAboveLimitIsRejected() throws Exception {
        String previous = System.getProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY);
        System.setProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY, "1024");

        byte[] payload = buildLargePolicyPayload(2048);
        try (LocalHttpServer server = LocalHttpServer.fixedLength(payload)) {
            String url = "http://127.0.0.1:" + server.getPort() + "/policy.xml";
            PolicyReference ref = new PolicyReference(policyEngine);
            try {
                ref.getRemoteReferencedPolicy(url);
                fail("Expected RuntimeException due to remote response byte budget");
            } catch (RuntimeException ex) {
                assertTrue(ex.getMessage().contains("maximum remote policy size"));
            }
        } finally {
            restoreProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY, previous);
        }
    }

    @Test
    public void testRemotePolicyChunkedResponseAboveLimitIsRejected() throws Exception {
        String previous = System.getProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY);
        System.setProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY, "1024");

        byte[] payload = buildLargePolicyPayload(4096);
        try (LocalHttpServer server = LocalHttpServer.chunked(payload)) {
            String url = "http://127.0.0.1:" + server.getPort() + "/policy.xml";
            PolicyReference ref = new PolicyReference(policyEngine);
            try {
                ref.getRemoteReferencedPolicy(url);
                fail("Expected RuntimeException due to remote response byte budget");
            } catch (RuntimeException ex) {
                assertTrue(ex.getMessage().contains("maximum remote policy size"));
            }
        } finally {
            restoreProperty(MAX_REMOTE_POLICY_BYTES_PROPERTY, previous);
        }
    }

    private static void restoreProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }

    private static byte[] buildLargePolicyPayload(int totalBytes) {
        String prefix = "<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\">";
        String suffix = "</wsp:Policy>";
        int middleSize = Math.max(0, totalBytes - prefix.length() - suffix.length());
        char[] middle = new char[middleSize];
        Arrays.fill(middle, ' ');
        String xml = prefix + new String(middle) + suffix;
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    private static final class LocalHttpServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final Thread serverThread;
        private final byte[] payload;
        private final boolean sendContentLength;
        private final CountDownLatch ready = new CountDownLatch(1);
        private final AtomicBoolean closed = new AtomicBoolean(false);

        static LocalHttpServer fixedLength(byte[] payload) throws IOException {
            return new LocalHttpServer(payload, true);
        }

        static LocalHttpServer chunked(byte[] payload) throws IOException {
            return new LocalHttpServer(payload, false);
        }

        LocalHttpServer(byte[] payload, boolean sendContentLength) throws IOException {
            this.payload = payload;
            this.sendContentLength = sendContentLength;
            this.serverSocket = new ServerSocket(0);
            this.serverThread = new Thread(this::serveOnce, "policy-reference-test-server");
            this.serverThread.setDaemon(true);
            this.serverThread.start();
            awaitReady();
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                serverSocket.close();
                try {
                    serverThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void awaitReady() throws IOException {
            try {
                if (!ready.await(1, TimeUnit.SECONDS)) {
                    throw new IOException("Test HTTP server did not start in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for test HTTP server", e);
            }
        }

        private void serveOnce() {
            ready.countDown();
            try (Socket client = serverSocket.accept()) {
                consumeRequest(client.getInputStream());
                writeResponse(client.getOutputStream());
            } catch (IOException ignored) {
                // The tests close the server socket during cleanup.
            }
        }

        private void consumeRequest(InputStream input) throws IOException {
            byte[] buffer = new byte[1024];
            int matched = 0;
            while (matched < 4) {
                int read = input.read(buffer);
                if (read == -1) {
                    break;
                }
                for (int i = 0; i < read; i++) {
                    byte b = buffer[i];
                    if ((matched == 0 || matched == 2) && b == '\r') {
                        matched++;
                    } else if ((matched == 1 || matched == 3) && b == '\n') {
                        matched++;
                    } else {
                        matched = 0;
                    }
                    if (matched == 4) {
                        return;
                    }
                }
            }
        }

        private void writeResponse(OutputStream out) throws IOException {
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 200 OK\r\n");
            response.append("Content-Type: application/xml\r\n");
            response.append("Connection: close\r\n");
            if (sendContentLength) {
                response.append("Content-Length: ").append(payload.length).append("\r\n");
            }
            response.append("\r\n");
            out.write(response.toString().getBytes(StandardCharsets.US_ASCII));
            out.write(payload);
            out.flush();
        }
    }
}
