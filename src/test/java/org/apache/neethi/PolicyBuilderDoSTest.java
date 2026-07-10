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
 * Reproduces parser recursion DoS where deeply nested policy operators trigger
 * StackOverflowError during PolicyBuilder.getPolicy(InputStream).
 */
public class PolicyBuilderDoSTest extends PolicyTestCase {

    private static final int DEPTH_WITHIN_DEFAULT_BUDGET = 64;
    private static final int DEPTH_ABOVE_DEFAULT_BUDGET = 4096;

    @Test
    public void testDeeplyNestedPolicyIsRejectedByDepthBudget() {
        PolicyBuilder builder = new PolicyBuilder();
        InputStream stream = xmlStream(buildNestedAllPolicyXml(DEPTH_ABOVE_DEFAULT_BUDGET));

        try {
            builder.getPolicy(stream);
            fail("Expected RuntimeException due to policy depth budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("maximum policy nesting depth"));
        }
    }

    @Test
    public void testNestedPolicyWithinDepthBudgetParses() {
        PolicyBuilder builder = new PolicyBuilder();
        InputStream stream = xmlStream(buildNestedAllPolicyXml(DEPTH_WITHIN_DEFAULT_BUDGET));
        Policy policy = builder.getPolicy(stream);

        assertNotNull(policy);
    }

    private static InputStream xmlStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildNestedAllPolicyXml(int depth) {
        StringBuilder xml = new StringBuilder(128 + (depth * 20));
        xml.append("<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\">");

        for (int i = 0; i < depth; i++) {
            xml.append("<wsp:All>");
        }

        xml.append("<wsp:All/>");

        for (int i = 0; i < depth; i++) {
            xml.append("</wsp:All>");
        }

        xml.append("</wsp:Policy>");
        return xml.toString();
    }
}
