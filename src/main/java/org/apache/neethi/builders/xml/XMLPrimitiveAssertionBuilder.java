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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.NestedPrimitiveAssertion;

public class XMLPrimitiveAssertionBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {
        Iterator it = element.getChildElements();
        OMElement el = it.hasNext() ? (OMElement)it.next() : null;
        if (!it.hasNext() && el != null && el.getQName().equals(Constants.Q_ELEM_POLICY)) {
            OMAttribute attribute = element
                .getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR);
            boolean isOptional = false;
            if (attribute != null) {
                isOptional = (new Boolean(attribute.getAttributeValue())
                    .booleanValue());
            }
            
            Policy policy = PolicyEngine.getPolicy(el);
            return new NestedPrimitiveAssertion(element.getQName(), isOptional, policy);
        }
        return new XmlPrimtiveAssertion(element);
    }

    public QName[] getKnownElements() {
        return new QName[] { new QName("UnknownElement") };
    }
}
