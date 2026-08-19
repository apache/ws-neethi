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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * A reference DAG in which every level holds two sibling references to the
 * next level normalizes to a single alternative at every step (so the
 * 10000-alternatives cap never trips) while the expansion work doubles per
 * level: 2^d normalizeOperator invocations from O(d) parsed elements, because
 * the cycle token is removed once each expansion completes and sibling
 * references then re-expand the same policy in full. The expansion budget
 * converts that into a fast, predictable RuntimeException.
 */
public class PolicyReferenceExpansionDoSTest extends PolicyTestCase {

    private static final int DAG_DEPTH = 30;

    private static final String WSU_NS =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    @Test
    public void testDoublingReferenceDagIsRejectedByExpansionBudget() {
        Policy policy = policyEngine.getPolicy(xmlStream(buildDoublingReferenceDagXml(DAG_DEPTH)));

        try {
            policy.normalize(registry, true);
            fail("Expected RuntimeException due to reference-expansion budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("PolicyReference expansions"));
        }
    }

    @Test
    public void testSiblingReferencesToSamePolicyStillNormalize() {
        Policy policy = policyEngine.getPolicy(xmlStream(buildSiblingReferencePolicyXml()));

        assertNotNull(policy.normalize(registry, true));
    }

    private static InputStream xmlStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildDoublingReferenceDagXml(int depth) {
        StringBuilder xml = new StringBuilder(1024 + depth * 160);
        xml.append("<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\"")
           .append(" xmlns:wsu=\"").append(WSU_NS).append("\"")
           .append(" xmlns:x=\"urn:x\">");

        xml.append("<wsp:PolicyReference URI=\"#B1\"/>")
           .append("<wsp:PolicyReference URI=\"#B1\"/>");

        for (int i = 1; i < depth; i++) {
            xml.append("<wsp:Policy wsu:Id=\"B").append(i).append("\">")
               .append("<wsp:PolicyReference URI=\"#B").append(i + 1).append("\"/>")
               .append("<wsp:PolicyReference URI=\"#B").append(i + 1).append("\"/>")
               .append("</wsp:Policy>");
        }

        xml.append("<wsp:Policy wsu:Id=\"B").append(depth).append("\">")
           .append("<x:Leaf/>")
           .append("</wsp:Policy>");

        xml.append("</wsp:Policy>");
        return xml.toString();
    }

    private static String buildSiblingReferencePolicyXml() {
        StringBuilder xml = new StringBuilder(512);
        xml.append("<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\"")
           .append(" xmlns:wsu=\"").append(WSU_NS).append("\"")
           .append(" xmlns:x=\"urn:x\">")
           .append("<wsp:PolicyReference URI=\"#P\"/>")
           .append("<wsp:PolicyReference URI=\"#P\"/>")
           .append("<wsp:Policy wsu:Id=\"P\"><x:Leaf/></wsp:Policy>")
           .append("</wsp:Policy>");
        return xml.toString();
    }
}
