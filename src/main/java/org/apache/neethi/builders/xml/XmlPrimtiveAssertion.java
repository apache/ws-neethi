/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyRegistry;

import java.util.Iterator;

public class XmlPrimtiveAssertion implements Assertion {

    OMElement element;

    boolean isOptional;

    
    // Assertions can contain policies inside it.
    Policy policy;

    public XmlPrimtiveAssertion(OMElement element) {
        setValue(element);
        setOptionality(element);
    }

    public QName getName() {
        return (element != null) ? element.getQName() : null;
    }

    public void setValue(OMElement element) {
        this.element = element;
        // get all the policy namespace children
        // actually there can only be one nested policy
        Iterator iter = element.getChildrenWithName(new QName(Constants.URI_POLICY_NS, Constants.ELEM_POLICY));
        if (iter.hasNext()) {
            OMElement policyOMElement = (OMElement) iter.next();
            this.policy = PolicyEngine.getPolicy(policyOMElement);
            // detach element from the om tree
            policyOMElement.detach();
        }

    }

    public OMElement getValue() {
        return element;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public PolicyComponent normalize() {
        if (isOptional) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();

            All all = new All();
            OMElement omElement = element.cloneOMElement();

            omElement.removeAttribute(omElement.getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR));
            all.addPolicyComponent(new XmlPrimtiveAssertion(omElement));
            exactlyOne.addPolicyComponent(all);

            exactlyOne.addPolicyComponent(new All());
            policy.addPolicyComponent(exactlyOne);

            return policy;
        }

        return this;
    }

    public PolicyComponent normalize(boolean isDeep) {
        throw new UnsupportedOperationException();
    }

    public PolicyComponent normalize(PolicyRegistry registry) {

        if (isOptional) {
            Policy policy = new Policy();
            ExactlyOne alternatives = new ExactlyOne();

            All alternative1 = new All();
            OMElement element1 = element.cloneOMElement();
            element1.removeAttribute(element1.getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR));
            alternative1.addPolicyComponent(new XmlPrimtiveAssertion(element1));
            alternatives.addPolicyComponent(alternative1);

            All alternative2 = new All();
            alternatives.addPolicyComponent(alternative2);

            policy.addPolicyComponent(alternatives);
            return policy;
        }
        return this;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        if (element != null) {

            if (policy != null) {
                // write the start part of the element
                String prefix = writer.getPrefix(element.getNamespace()
                        .getNamespaceURI());
                if (prefix == null) {
                    writer.writeStartElement(element.getQName().getPrefix(),
                            element.getQName().getLocalPart(), element
                                    .getNamespace().getNamespaceURI());
                    writer.writeNamespace(element.getQName().getPrefix(),
                            element.getNamespace().getNamespaceURI());
                    writer.setPrefix(element.getQName().getPrefix(), element
                            .getNamespace().getNamespaceURI());

                } else {
                    writer.writeStartElement(element.getNamespace()
                            .getNamespaceURI(), element.getQName()
                            .getLocalPart());
                }
                // TODO : write attributes and inner elements
                policy.serialize(writer);
                writer.writeEndElement();
            } else {
                // we can not serialize as follows since OMElement seriali
                element.serialize(writer);
            }

        } else {
            // TODO throw an exception??
        }
    }

    public final short getType() {
        return Constants.TYPE_ASSERTION;
    }

    private void setOptionality(OMElement element) {
        OMAttribute attribute = element.getAttribute(Constants.Q_ELEM_OPTIONAL_ATTR);
        if (attribute != null) {
            this.isOptional = (new Boolean(attribute.getAttributeValue())
                    .booleanValue());

        } else {
            this.isOptional = false;
        }
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (policyComponent.getType() != Constants.TYPE_ASSERTION) {
            return false;
        }

        return getName().equals(((Assertion) policyComponent).getName());
    }

}
