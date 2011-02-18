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

package org.apache.neethi.builders.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.PolicyContainingPrimitiveAssertion;
import org.apache.neethi.builders.PrimitiveAssertion;

public class XMLPrimitiveAssertionBuilder implements AssertionBuilder<Element> {

    public Assertion build(Element element, AssertionBuilderFactory factory)
        throws IllegalArgumentException {
        
        Node nd = element.getFirstChild();
        int count = 0;
        int policyCount = 0;
        Element policyEl = null;
        while (nd != null) {
            if (nd instanceof Element) {
                count++;
                Element el = (Element)nd;
                if (Constants.isPolicyElement(el.getNamespaceURI(), el.getLocalName())) {
                    policyEl = el;
                    policyCount++;
                }
            }
            nd = nd.getNextSibling();
        }
        Map<QName, String> atts = new HashMap<QName, String>();
        NamedNodeMap attrs = element.getAttributes();
        if (attrs != null) {
            for (int x = 0; x < attrs.getLength(); x++) {
                Attr attr = (Attr)attrs.item(x);
                atts.put(new QName(attr.getNamespaceURI(), attr.getLocalName()), attr.getValue());
            }
        }

        if (count == 0) {
            return newPrimitiveAssertion(element, atts.isEmpty() ? null : atts);
        } else if (policyCount == 1 && count == 1) {
            Policy policy = factory.getPolicyEngine().getPolicy(policyEl);
            return newPolicyContainingAssertion(element, atts.isEmpty() ? null : atts, policy);
        }
        return new XmlPrimitiveAssertion(element);
    }
    
    public Assertion newPrimitiveAssertion(Element element, Map<QName, String> atts) {
        return new PrimitiveAssertion(new QName(element.getNamespaceURI(), element.getLocalName()),
                                      isOptional(element), isIgnorable(element));        
    }
    public Assertion newPolicyContainingAssertion(Element element, Map<QName, String> atts, Policy policy) {
        return new PolicyContainingPrimitiveAssertion(new QName(element.getNamespaceURI(),
                                                                element.getLocalName()),
                                      isOptional(element), isIgnorable(element),
                                      policy);
    }
    
    public static boolean isOptional(Element el) {
        Attr optional = el.getAttributeNodeNS(Constants.URI_POLICY_13_NS, Constants.ATTR_OPTIONAL);
        if (optional == null) {
            optional = el.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_OPTIONAL);
        }
        if (optional == null) {
            optional = el.getAttributeNodeNS(Constants.URI_POLICY_15_DEPRECATED_NS, Constants.ATTR_OPTIONAL);
        }
        return optional == null ? false : Boolean.parseBoolean(optional.getValue());
    }

    public static boolean isIgnorable(Element el) {
        Attr ignorable = el.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_IGNORABLE);
        if (ignorable == null) {
            ignorable = el.getAttributeNodeNS(Constants.URI_POLICY_15_DEPRECATED_NS,
                                              Constants.ATTR_IGNORABLE);
        }
        return ignorable == null ? false : Boolean.parseBoolean(ignorable.getValue());
    }
    
    public QName[] getKnownElements() {
        return new QName[] {new QName("UnknownElement")};
    }

}
