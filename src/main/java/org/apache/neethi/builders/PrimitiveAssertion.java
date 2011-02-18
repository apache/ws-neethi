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

package org.apache.neethi.builders;


import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;

/**
 * 
 */
public class PrimitiveAssertion implements Assertion {
    protected QName name;
    protected boolean optional;
    protected boolean ignorable;
    protected Map<QName, String> attributes;
    
    public PrimitiveAssertion() {
        this((QName)null);
    }
    
    public PrimitiveAssertion(QName n) {
        this(n, false);
    }
    
    public PrimitiveAssertion(QName n, boolean o) {
        this(n, o, false);
    }
    public PrimitiveAssertion(QName n, boolean o, boolean i) {
        name = n;
        optional = o;
        ignorable = i;
    }
    public PrimitiveAssertion(QName n, boolean o, boolean i, Map<QName, String> atts) {
        name = n;
        optional = o;
        ignorable = i;
        if (atts != null) {
            attributes = new HashMap<QName, String>(atts);
        }
    }
    public String getAttribute(QName n) {
        if (attributes != null) {
            return attributes.get(n);
        }
        return null;
    }
    public synchronized void addAttribute(QName n, String value) {
        if (attributes == null) {
            attributes = new HashMap<QName, String>();
        }
        attributes.put(n, value);
    }
    public synchronized void addAttributes(Map<QName, String> atts) {
        if (attributes == null) {
            attributes = new HashMap<QName, String>(atts);
        } else {
            attributes.putAll(atts);
        }
    }
    public String toString() {
        return name.toString();
    }
    public boolean equal(PolicyComponent policyComponent) {
        if (this == policyComponent) {
            return true;
        }
        if (policyComponent.getType() != Constants.TYPE_ASSERTION) {
            return false;
        }
        return getName().equals(((Assertion)policyComponent).getName());
    }

    public short getType() {
        return Constants.TYPE_ASSERTION;
    }

    public QName getName() {
        return name;
    }
    
    public void setName(QName n) {
        name = n;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean o) {
        optional = o;        
    }
    public boolean isIgnorable() {
        return ignorable;
    }

    public void setIgnorable(boolean i) {
        ignorable = i;
    }
    
    public PolicyComponent normalize() {
        if (isOptional()) {
            Policy policy = new Policy();
            ExactlyOne exactlyOne = new ExactlyOne();

            All all = new All();
            all.addPolicyComponent(clone(false));
            exactlyOne.addPolicyComponent(all);
            exactlyOne.addPolicyComponent(new All());
            policy.addPolicyComponent(exactlyOne);

            return policy;
        }

        return clone(false);
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String namespace = Constants.findPolicyNamespace(writer);
        String pfx = writer.getPrefix(name.getNamespaceURI());
        boolean writeNS = false;
        if ("".equals(pfx) || pfx == null) {
            pfx = "";
            writer.setDefaultNamespace(name.getNamespaceURI());
            writeNS = true;
        } else {
            pfx = pfx + ":";
        }
        writer.writeStartElement(name.getNamespaceURI(), pfx + name.getLocalPart());
        if (writeNS) {
            writer.writeDefaultNamespace(name.getNamespaceURI());
        }
        if (optional) {
            writer.writeAttribute(namespace, Constants.ATTR_OPTIONAL, "true");
        }
        if (ignorable) {
            writer.writeAttribute(namespace, Constants.ATTR_IGNORABLE, "true");
        }
        writeAttributes(writer);
        writer.writeEndElement();
    }
    protected void writeAttributes(XMLStreamWriter writer) throws XMLStreamException {
        if (attributes != null) {
            for (Map.Entry<QName, String> att : attributes.entrySet()) {
                if (Constants.isIgnorableAttribute(att.getKey())) {
                    //already handled
                    continue;
                }
                if (Constants.isOptionalAttribute(att.getKey())) {
                    //already handled
                    continue;
                }
                String prefix = getOrCreatePrefix(att.getKey().getNamespaceURI(), writer);
                writer.writeAttribute(prefix, att.getKey().getNamespaceURI(),
                                      att.getKey().getLocalPart(),
                                      att.getValue());
            }
        }
    }
    protected String getOrCreatePrefix(String ns, XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(ns);
        int count = 1;
        while (prefix == null || "".equals(prefix)) {
            prefix = "ns" + count++;
            String ns2 =  writer.getNamespaceContext().getNamespaceURI(prefix);
            if (ns2 == null || "".equals(ns2)) {
                //found one that will work
                writer.writeNamespace(prefix, ns);
            } else {
                prefix = null;
            }
        }
        return prefix;
    }
    
    protected Assertion clone(boolean isoptional) {
        return new PrimitiveAssertion(name, isoptional, ignorable, attributes);
    }

}