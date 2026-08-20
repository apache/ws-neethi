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
 * The maxElements/maxAttributes budgets used to fire only on the wsp:*
 * operator walk: every node inside an assertion subtree was materialized into
 * DOM by the converter layer with no counting at all, so millions of elements
 * could be allocated while the counters reported single digits. These tests
 * pin the fix: assertion subtrees are charged against the same parse budget.
 */
public class PolicyBuilderAssertionSubtreeBudgetTest extends PolicyTestCase {

    @Test
    public void testAssertionSubtreeElementsAreCountedAgainstBudget() {
        System.setProperty(PolicyBuilder.MAX_ELEMENTS_PROPERTY, "500");
        try {
            PolicyBuilder builder = new PolicyBuilder();
            try {
                builder.getPolicy(xmlStream(buildWideAssertionPolicyXml(600, 0)));
                fail("Expected RuntimeException due to element budget");
            } catch (RuntimeException ex) {
                assertTrue(ex.getMessage().contains("maximum number of elements"));
            }
        } finally {
            System.clearProperty(PolicyBuilder.MAX_ELEMENTS_PROPERTY);
        }
    }

    @Test
    public void testAssertionSubtreeAttributesAreCountedAgainstBudget() {
        System.setProperty(PolicyBuilder.MAX_ATTRIBUTES_PROPERTY, "500");
        try {
            PolicyBuilder builder = new PolicyBuilder();
            try {
                builder.getPolicy(xmlStream(buildWideAssertionPolicyXml(200, 5)));
                fail("Expected RuntimeException due to attribute budget");
            } catch (RuntimeException ex) {
                assertTrue(ex.getMessage().contains("maximum number of attributes"));
            }
        } finally {
            System.clearProperty(PolicyBuilder.MAX_ATTRIBUTES_PROPERTY);
        }
    }

    @Test
    public void testSmallAssertionSubtreeParses() {
        PolicyBuilder builder = new PolicyBuilder();
        Policy policy = builder.getPolicy(xmlStream(buildWideAssertionPolicyXml(10, 2)));

        assertNotNull(policy);
    }

    private static InputStream xmlStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildWideAssertionPolicyXml(int elements, int attributesPerElement) {
        StringBuilder xml = new StringBuilder(256 + elements * (24 + attributesPerElement * 12));
        xml.append("<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" xmlns:x=\"urn:x\">")
           .append("<x:r>");

        for (int i = 0; i < elements; i++) {
            xml.append("<x:e");
            for (int a = 0; a < attributesPerElement; a++) {
                xml.append(" a").append(a).append("=\"v\"");
            }
            xml.append("/>");
        }

        xml.append("</x:r>")
           .append("</wsp:Policy>");
        return xml.toString();
    }
}
