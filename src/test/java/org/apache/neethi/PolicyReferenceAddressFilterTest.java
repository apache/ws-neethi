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

import java.net.Inet6Address;
import java.net.InetAddress;

import org.junit.Test;

/**
 * Address-class checks for the remote policy fetcher. All literals resolve
 * without DNS. The forbidden classes are link-local (cloud IMDS), multicast,
 * and any-local; loopback and RFC-1918 stay permitted by documented intent.
 */
public class PolicyReferenceAddressFilterTest extends PolicyTestCase {

    @Test
    public void testLinkLocalIsForbidden() throws Exception {
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("169.254.169.254")));
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("fe80::1")));
    }

    @Test
    public void testMulticastIsForbidden() throws Exception {
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("224.0.0.1")));
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("ff02::1")));
    }

    @Test
    public void testAnyLocalIsForbidden() throws Exception {
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("0.0.0.0")));
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("::")));
    }

    @Test
    public void testLoopbackAndPrivateRangesStayPermitted() throws Exception {
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("127.0.0.1")));
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("::1")));
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("10.0.0.5")));
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("192.168.1.10")));
    }

    @Test
    public void testPublicAddressStaysPermitted() throws Exception {
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("93.184.216.34")));
    }

    @Test
    public void testIpv4MappedEncodingOfLinkLocalIsForbidden() throws Exception {
        // getByName normalizes ::ffff:a.b.c.d to Inet4Address, so the mapped
        // encoding of the IMDS address classifies as link-local and is caught
        assertTrue(PolicyReference.isForbiddenAddress(
            InetAddress.getByName("::ffff:169.254.169.254")));
    }

    @Test
    public void testUniqueLocalIsForbidden() throws Exception {
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("fd00:ec2::254")));
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("fc00::1")));
        assertTrue(PolicyReference.isForbiddenAddress(InetAddress.getByName("fdff::1")));
    }

    @Test
    public void testNat64EmbeddedForbiddenIpv4IsForbidden() throws Exception {
        assertTrue(PolicyReference.isForbiddenAddress(
            InetAddress.getByName("64:ff9b::169.254.169.254")));
        assertTrue(PolicyReference.isForbiddenAddress(
            InetAddress.getByName("64:ff9b::0.0.0.0")));
    }

    @Test
    public void testNat64EmbeddedPermittedIpv4StaysPermitted() throws Exception {
        // NAT64 form of a public IPv4 address — permitted, like the address itself
        assertFalse(PolicyReference.isForbiddenAddress(
            InetAddress.getByName("64:ff9b::93.184.216.34")));
        // NAT64 form of RFC-1918 — permitted by documented intent (Q6)
        assertFalse(PolicyReference.isForbiddenAddress(
            InetAddress.getByName("64:ff9b::10.0.0.5")));
    }

    @Test
    public void testMappedFormKeptAsInet6IsClassifiedByEmbeddedAddress() throws Exception {
        // getByName normalizes mapped forms to Inet4Address; guard the raw
        // Inet6Address representation as well (e.g. from a custom resolver)
        byte[] mappedLinkLocal = new byte[16];
        mappedLinkLocal[10] = (byte) 0xff;
        mappedLinkLocal[11] = (byte) 0xff;
        mappedLinkLocal[12] = (byte) 169;
        mappedLinkLocal[13] = (byte) 254;
        mappedLinkLocal[14] = (byte) 169;
        mappedLinkLocal[15] = (byte) 254;

        assertTrue(PolicyReference.isForbiddenAddress(
            Inet6Address.getByAddress(null, mappedLinkLocal, 0)));
    }

    @Test
    public void testGlobalIpv6StaysPermitted() throws Exception {
        assertFalse(PolicyReference.isForbiddenAddress(InetAddress.getByName("2001:db8::1")));
    }
}