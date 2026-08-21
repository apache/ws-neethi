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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * PolicyReference is a wrapper that holds explicit PolicyReferences.
 */
public class PolicyReference implements PolicyComponent {

    public static final String MAX_REMOTE_POLICY_BYTES_PROPERTY = "org.apache.neethi.remote.maxPolicyBytes";
    public static final String MAX_REMOTE_FETCH_MILLIS_PROPERTY = "org.apache.neethi.remote.maxFetchMillis";
    /**
     * Set to "false" to disable rewriting http URLs to the vetted literal
     * address (see getRemoteReferencedPolicy). Pinning is on by default; the
     * opt-out exists for deployments that rely on name-based virtual hosting
     * for http policy endpoints and accept the DNS-rebinding residual risk.
     */
    public static final String PIN_ADDRESS_PROPERTY = "org.apache.neethi.remote.pinAddress";
    private static final long DEFAULT_MAX_REMOTE_POLICY_BYTES = 64L * 1024L * 1024L;
    private static final long DEFAULT_MAX_REMOTE_FETCH_MILLIS = 30L * 1000L;

    private String uri;
    private PolicyBuilder engine;
    private final long maxRemotePolicyBytes;
    private final long maxRemoteFetchMillis;

    public PolicyReference() {
        maxRemotePolicyBytes = readConfiguredLimit(MAX_REMOTE_POLICY_BYTES_PROPERTY,
                                                   DEFAULT_MAX_REMOTE_POLICY_BYTES);
        maxRemoteFetchMillis = readConfiguredLimit(MAX_REMOTE_FETCH_MILLIS_PROPERTY,
                                                   DEFAULT_MAX_REMOTE_FETCH_MILLIS);
    }
    
    public PolicyReference(PolicyBuilder p) {
        engine = p;
        maxRemotePolicyBytes = readConfiguredLimit(MAX_REMOTE_POLICY_BYTES_PROPERTY,
                                                   DEFAULT_MAX_REMOTE_POLICY_BYTES);
        maxRemoteFetchMillis = readConfiguredLimit(MAX_REMOTE_FETCH_MILLIS_PROPERTY,
                               DEFAULT_MAX_REMOTE_FETCH_MILLIS);
    }
    
    /**
     * Sets the Policy URI
     * @param uri the Policy URI
     */
    public void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the Policy URI that is referred by self.
     * @return a String that is the Policy URI referred by self
     */
    public String getURI() {
        return uri;
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (Constants.TYPE_POLICY_REF != policyComponent.getType()) {
            return false;
        }
        
        String u = ((PolicyReference)policyComponent).getURI();
        return u != null && u.length() != 0 && u.equals(this.uri);

    }


    /**
     * Returns short value of Constants.TYPE_POLICY_REF 
     */
    public short getType() {
        return Constants.TYPE_POLICY_REF;
    }

    /**
     * Throws an UnsupportedOperationException since PolicyReference.normalize()
     * can't resolve the Policy that it refers to unless a PolicyRegistry is
     * provided.
     * @return The normalized PolicyComponent
     */
    public PolicyComponent normalize() {
        throw new UnsupportedOperationException("PolicyReference.normalize() is meaningless");
    }
    
    /**
     * Returns normalized version of the Policy that is referred by self. The specified 
     * PolicyRegistry is used to lookup for the Policy that is referred and {@code deep} 
     * indicates the level of normalization for the returning Policy.
     * 
     * @param reg the PolicyRegistry that is used to resolved the Policy referred by self
     * @param deep the flag to indicate whether returning Policy should be fully normalized
     * @return the normalized version for the Policy refered by self
     */
    public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
        String key = getURI();
        int pos = key.indexOf("#");
        if (pos == 0) {
            key = key.substring(1);
        } else if (pos > 0) {
            key = key.substring(0, pos);
        }
        
        Policy policy = reg.lookup(key);        
        
        if (policy == null) {
            policy = getRemoteReferencedPolicy(key);

            if (policy == null) {
                throw new RuntimeException(key + " can't be resolved");
            }
            reg.register(key, policy);
        }
        
        return policy.normalize(reg, deep);
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String namespace = Constants.findPolicyNamespace(writer);
        String wspPrefix = writer.getPrefix(namespace);
        
        if (wspPrefix == null) {
            wspPrefix = Constants.ATTR_WSP;
            writer.setPrefix(wspPrefix, namespace);
        }
        
        writer.writeStartElement(wspPrefix, Constants.ELEM_POLICY_REF, namespace);
        writer.writeNamespace(Constants.ATTR_WSP, namespace);
        writer.writeAttribute(Constants.ATTR_URI, getURI());
        
        writer.writeEndElement();
    }
    
    public Policy getRemoteReferencedPolicy(String u) {
        URL url;
        try {
            url = new URL(u);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Malformed uri.");
        }

        String scheme = url.getProtocol();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new RuntimeException("Unsupported URI scheme: only http and https are permitted.");
        }

        // Resolve the host and reject addresses that can never serve a policy document:
        //   - link-local  (169.254.x.x / fe80::/10) — cloud IMDS, auto-configuration
        //   - multicast   (224.0.0.0/4 / ff00::/8)  — no HTTP server listens at a multicast address
        //   - any-local   (0.0.0.0 / ::)             — unspecified/wildcard, not a valid destination
        // Loopback (127.x.x.x / ::1) and site-local (RFC-1918) addresses are permitted
        // so that policies on localhost or an internal network can be resolved.
        // EVERY address the host resolves to is vetted, so a multi-record DNS
        // answer cannot smuggle a forbidden address past the filter.
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(url.getHost());
        } catch (UnknownHostException e) {
            throw new RuntimeException("PolicyReference URI host could not be resolved.");
        }
        for (InetAddress addr : addresses) {
            if (isForbiddenAddress(addr)) {
                throw new RuntimeException(
                    "PolicyReference URI resolves to a forbidden address (link-local, multicast, or wildcard).");
            }
        }

        // Pin the connection to an address that was actually vetted:
        // URLConnection re-resolves the hostname at connect time, which opens
        // a DNS-rebinding TOCTOU window between the check above and the
        // connect. For http the URL host is rewritten to the vetted literal
        // address (note: the JDK will send that literal in the Host header -
        // see PIN_ADDRESS_PROPERTY to opt out for name-based virtual
        // hosting). For https the hostname is kept: TLS certificate
        // verification against the original hostname binds the peer identity,
        // and a literal-address URL would break it.
        URL connectionUrl = url;
        if ("http".equalsIgnoreCase(scheme)
            && !"false".equalsIgnoreCase(System.getProperty(PIN_ADDRESS_PROPERTY))) {
            try {
                connectionUrl = new URL(url.getProtocol(), toUrlHost(addresses[0]),
                                        url.getPort(), url.getFile());
            } catch (MalformedURLException mue) {
                throw new RuntimeException("Malformed uri.");
            }
        }

        try {
            URLConnection connection = connectionUrl.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);

            long declaredLength = connection.getContentLengthLong();
            if (declaredLength > maxRemotePolicyBytes) {
                throw new RuntimeException(
                    "Remote policy response exceeded the maximum remote policy size ("
                    + maxRemotePolicyBytes + " bytes).");
            }

            // total wall-clock deadline for the whole fetch: the JDK read
            // timeout below applies to each blocking read individually, so a
            // server trickling one byte per interval would otherwise hold the
            // resolving thread forever
            long deadlineNanos = System.nanoTime() + maxRemoteFetchMillis * 1_000_000L;
            InputStream in = connection.getInputStream();
            try {
                byte[] payload = readBounded(in, maxRemotePolicyBytes, deadlineNanos, maxRemoteFetchMillis);
                PolicyBuilder pe = engine;
                if (pe == null) {
                    pe = new PolicyBuilder();
                }
                return pe.getPolicy(new ByteArrayInputStream(payload));
            } finally {
                in.close();
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot reach remote policy reference.");
        }
    }

    // package-private for tests
    static byte[] readBounded(InputStream input, long maxBytes,
                              long deadlineNanos, long maxMillis) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;

        while (true) {
            if (System.nanoTime() - deadlineNanos >= 0) {
                throw new RuntimeException(
                    "Remote policy fetch exceeded the maximum total fetch time ("
                    + maxMillis + " ms).");
            }
            int read = input.read(buffer);
            if (read == -1) {
                break;
            }

            total += read;
            if (total > maxBytes) {
                throw new RuntimeException(
                    "Remote policy response exceeded the maximum remote policy size ("
                    + maxBytes + " bytes).");
            }

            out.write(buffer, 0, read);
        }

        return out.toByteArray();
    }

    /**
     * Returns whether the resolved address belongs to a class the reference
     * fetcher must never connect to. Package-private for tests.
     */
    static boolean isForbiddenAddress(InetAddress addr) {
        return addr.isLinkLocalAddress() || addr.isMulticastAddress() || addr.isAnyLocalAddress();
    }

    private static String toUrlHost(InetAddress addr) {
        String literal = addr.getHostAddress();
        int scope = literal.indexOf('%');
        if (scope >= 0) {
            // an IPv6 scope id is not valid in a URL host
            literal = literal.substring(0, scope);
        }
        return literal.indexOf(':') >= 0 ? "[" + literal + "]" : literal;
    }

    private static long readConfiguredLimit(String key, long defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

}
