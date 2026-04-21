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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Test two policies that mutually reference each other cause unbounded recursion in
     * AbstractPolicyOperator.normalizeOperator() when normalize(true) is called.
     *
     * Policy A contains a PolicyReference to "B".
     * Policy B contains a PolicyReference to "A".
     * Both are registered in the PolicyRegistry before normalization begins.
     *
     * normalizeOperator() resolves each PolicyReference via reg.lookup() and
     * recurses into the resolved policy's components with no cycle detection,
     * ultimately throwing StackOverflowError.
     */
    @Test
    public void testCircularPolicyReferenceThrowsStackOverflow() {
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
            fail("Expected StackOverflowError or RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }
}
