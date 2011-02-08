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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyRegistry;

/**
 * XmlPrimitiveAssertion wraps an OMElement s.t. any unknown elements can be
 * treated an assertions if there is no AssertionBuilder that can build an
 * assertion from that OMElement.
 * 
 */
public class XmlPrimitiveAssertion implements Assertion {

    OMElement element;

    boolean optional;
    boolean ignorable;

    /**
     * Constructs a XmlPrimitiveAssertion from an OMElement.
     * 
     * @param element
     *            the OMElement from which the XmlAssertion is constructed
     */
    public XmlPrimitiveAssertion(OMElement element) {
        setValue(element);
        setOptionality(element);
        setIgnorability(element);
    }


    /**
     * Returns the QName of the wrapped OMElement.
     */
    public QName getName() {
        return (element != null) ? element.getQName() : null;
    }

    /**
     * Sets the wrapped OMElement.
     * 
     * @param element
     *            the OMElement to be set as wrapped
     */
    public void setValue(OMElement element) {
        this.element = element;
    }

    /**
     * Returns the wrapped OMElement.
     * 
     * @return the wrapped OMElement
     */
    public OMElement getValue() {
        return element;
    }

    /**
     * Returns <tt>true</tt> if the wrapped element that assumed to be an
     * assertion, is optional.
     */
    public boolean isOptional() {
        return optional;
    }
    /**
     * Returns <tt>true</tt> if the wrapped element that assumed to be an
     * assertion, is ignorable.
     */
    public boolean isIgnorable() {
        return ignorable;
    }

    /**
     * Returns the partial normalized version of the wrapped OMElement, that is
     * assumed to be an assertion.
     */
    public PolicyComponent normalize() {
        if (optional) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();

            All all = new All();
            OMElement omElement = element.cloneOMElement();

            OMAttribute att = omElement.getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR);
            if (att == null) {
                att = omElement.getAttribute(Constants.Q_ELEM_OPTIONAL_15_ATTR);
            }
            omElement.removeAttribute(att);
            all.addPolicyComponent(new XmlPrimitiveAssertion(omElement));
            exactlyOne.addPolicyComponent(all);

            exactlyOne.addPolicyComponent(new All());
            policy.addPolicyComponent(exactlyOne);

            return policy;
        }

        return this;
    }

    /**
     * Throws an UnsupportedOperationException since an assertion of an unknown
     * element can't be fully normalized due to it's unkonwn composite.
     */
    public PolicyComponent normalize(boolean isDeep) {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        if (element != null) {
            element.serialize(writer);

        } else {
            throw new RuntimeException("Wrapped Element is not set");
        }
    }

    /**
     * Returns Constants.TYPE_ASSERTION
     */
    public final short getType() {
        return Constants.TYPE_ASSERTION;
    }

    private void setOptionality(OMElement element) {
        OMAttribute attribute = element
                .getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR);
        if (attribute == null) {
            attribute = element
                .getAttribute(Constants.Q_ELEM_OPTIONAL_15_ATTR);
        }
        if (attribute != null) {
            this.optional = (new Boolean(attribute.getAttributeValue())
                    .booleanValue());

        } else {
            this.optional = false;
        }
    }
    private void setIgnorability(OMElement element2) {
        OMAttribute attribute = element
            .getAttribute(Constants.Q_ELEM_IGNORABLE_15_ATTR);
        if (attribute != null) {
            this.ignorable = (new Boolean(attribute.getAttributeValue())
                    .booleanValue());
        
        } else {
            this.ignorable = false;
        }
        
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (policyComponent.getType() != Constants.TYPE_ASSERTION) {
            return false;
        }

        return getName().equals(((Assertion) policyComponent).getName());
    }
}
