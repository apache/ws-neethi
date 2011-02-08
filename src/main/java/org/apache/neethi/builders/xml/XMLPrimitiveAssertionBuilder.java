/*
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

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.PolicyContainingAssertion;

public class XMLPrimitiveAssertionBuilder implements AssertionBuilder<Element> {

    public Assertion build(Element element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        Node nd = element.getFirstChild();
        while (nd != null) {
            if (!(nd instanceof Element)) {
                nd = nd.getNextSibling();
                continue;
            }
            Element el = (Element)nd; 
            if (Constants.isPolicyElement(el.getNamespaceURI(), el.getLocalName())) {
                Attr optional = el.getAttributeNodeNS(Constants.URI_POLICY_NS, Constants.ATTR_OPTIONAL);
                if (optional == null) {
                    optional = el.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_OPTIONAL);
                }
                Attr ignorable = el.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_IGNORABLE);
                

            
                Policy policy = PolicyEngine.getPolicy(el);
                return new PolicyContainingAssertion(new QName(element.getNamespaceURI(), element.getLocalName()),
                                                 optional == null ? false : Boolean.parseBoolean(optional.getValue()),
                                                 ignorable == null ? false : Boolean.parseBoolean(ignorable.getValue()),
                                                 policy);
            }
            nd = nd.getNextSibling();
        }
        return new XmlPrimitiveAssertion(element);
    }

    public QName[] getKnownElements() {
        return new QName[] { new QName("UnknownElement") };
    }

}
