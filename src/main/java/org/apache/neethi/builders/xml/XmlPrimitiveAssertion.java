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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyRegistry;

/**
 * XmlPrimitiveAssertion wraps an Element s.t. any unknown elements can be
 * treated an assertions if there is no AssertionBuilder that can build an
 * assertion from that Element.
 * 
 */
public class XmlPrimitiveAssertion implements Assertion {

    protected Element element;
    protected boolean optional;
    protected boolean ignorable;

    /**
     * Constructs a XmlPrimitiveAssertion from an Element.
     * 
     * @param element
     *            the Element from which the XmlAssertion is constructed
     */
    public XmlPrimitiveAssertion(Element element) {
        setValue(element);
        setOptionality(element);
        setIgnorability(element);
    }


    /**
     * Returns the QName of the wrapped Element.
     */
    public QName getName() {
        return (element != null) ? new QName(element.getNamespaceURI(), element.getLocalName()) : null;
    }

    /**
     * Sets the wrapped Element.
     * 
     * @param element
     *            the Element to be set as wrapped
     */
    public void setValue(Element element) {
        this.element = element;
    }

    /**
     * Returns the wrapped Element.
     * 
     * @return the wrapped Element
     */
    public Element getValue() {
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
     * Returns the partial normalized version of the wrapped Element, that is
     * assumed to be an assertion.
     */
    public PolicyComponent normalize() {
        if (optional) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();

            All all = new All();
            Element element = (Element)this.element.cloneNode(true);
            Attr attr = element.getAttributeNodeNS(Constants.URI_POLICY_NS, Constants.ATTR_OPTIONAL);
            if (attr != null) {
                element.removeAttributeNode(attr);
            }
            attr = element.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_OPTIONAL);
            if (attr != null) {
                element.removeAttributeNode(attr);
            }
            all.addPolicyComponent(new XmlPrimitiveAssertion(element));
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
            copyEvents(XMLInputFactory.newInstance().createXMLEventReader(new DOMSource(element)), writer);
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

    private void setOptionality(Element element2) {
        Attr attribute = element2.getAttributeNodeNS(Constants.URI_POLICY_NS, Constants.ATTR_OPTIONAL);
        if (attribute == null) {
            attribute = element2.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_OPTIONAL);
        }
        if (attribute != null) {
            this.optional = (new Boolean(attribute.getValue())
                    .booleanValue());

        } else {
            this.optional = false;
        }
    }
    private void setIgnorability(Element element2) {
        Attr attribute = element2.getAttributeNodeNS(Constants.URI_POLICY_15_NS, Constants.ATTR_IGNORABLE);
        if (attribute != null) {
            this.ignorable = (new Boolean(attribute.getValue())
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
    
    private void copyEvents(XMLEventReader reader, XMLStreamWriter writer) 
        throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            
            switch (event.getEventType()) {
            case XMLEvent.ATTRIBUTE: {
                Attribute attr = (Attribute) event;
                QName name = attr.getName();
                writer.writeAttribute(name.getPrefix(), name.getNamespaceURI(),
                                       name.getLocalPart(), attr.getValue());
                break;
            }
            case XMLEvent.START_DOCUMENT:
            case XMLEvent.END_DOCUMENT:
                //not doing this as we're in a partial write mode
                return;

            case XMLEvent.END_ELEMENT:
                writer.writeEndElement();
                break;
            
            case XMLEvent.NAMESPACE: {
                Namespace ns = (Namespace) event;
                writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
                break;
            }
            
            case XMLEvent.START_ELEMENT: {
                StartElement se = event.asStartElement();
                QName n = se.getName();
                writer.writeStartElement(n.getPrefix(), n.getLocalPart(),
                                          n.getNamespaceURI());
                Iterator it = se.getNamespaces();
                while (it.hasNext()) {
                    Namespace ns = (Namespace) it.next();
                    writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
                }
                it = se.getAttributes();
                while (it.hasNext()) {
                    Attribute attr = (Attribute) it.next();
                    QName name = attr.getName();
                    writer.writeAttribute(name.getPrefix(), name.getNamespaceURI(),
                                           name.getLocalPart(), attr.getValue());
                }
            }
            break;
           
            case XMLEvent.CHARACTERS: {
                Characters ch = event.asCharacters();
                String text = ch.getData();
                if (ch.isCData()) {
                    writer.writeCData(text);
                } else {
                    writer.writeCharacters(text);
                }
            }
            break;

            case XMLEvent.CDATA:
                writer.writeCData(event.asCharacters().getData());
                break;
            
            case XMLEvent.COMMENT:
                writer.writeComment(((Comment) event).getText());
            break;
            }
        }
    }
}
