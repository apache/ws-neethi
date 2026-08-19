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
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

import org.junit.Test;

/**
 * Reproduces unbounded recursion when a primitive assertion contains only a
 * nested policy and the assertion builder re-enters the public policy parser.
 */
public class PolicyBuilderNestedPolicyDoSTest extends PolicyTestCase {

    private static final int SANDWICH_LAYERS = 100000;

    @Test
    public void testNestedPolicyAssertionsAreRejectedByDepthBudget() {
        PolicyBuilder builder = new PolicyBuilder();

        try {
            builder.getPolicy(parseElement(buildPolicyAssertionSandwichXml(SANDWICH_LAYERS)));
            fail("Expected RuntimeException due to policy depth budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("maximum policy nesting depth"));
        }
    }

    private static Element parseElement(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
        } catch (Exception ex) {
            throw new RuntimeException("Could not parse test policy.", ex);
        }
    }

    private static String buildPolicyAssertionSandwichXml(int layers) {
        StringBuilder xml = new StringBuilder(128 + (layers * 40));
        xml.append("<wsp:Policy xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" ")
            .append("xmlns:p=\"urn:neethi-test\">");

        for (int i = 0; i < layers; i++) {
            xml.append("<p:Assertion><wsp:Policy>");
        }
        xml.append("<p:Assertion/>");
        for (int i = 0; i < layers; i++) {
            xml.append("</wsp:Policy></p:Assertion>");
        }

        xml.append("</wsp:Policy>");
        return xml.toString();
    }
}