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

package org.apache.neethi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Policy is a PolicyOperator that requires to statisfy all of its
 * PolicyComponents. It is always the outermost component of a Policy.
 * 
 */
public class Policy extends All {

    private Map<QName, String> attributes = new HashMap<QName, String>();

    /**
     * Returns a Normalized version of self. If <tt>deep</tt> is set
     * <tt>false</tt> then the assertions are not normalized and it returns a
     * partially normalized version of self.
     * 
     * @param deep
     *            a flag to indicate whether to normalize the assertions
     * @return a PolicyComponent that is normalized version of self
     */
    public PolicyComponent normalize(boolean deep) {
        return normalize(null, deep);
    }

    /**
     * Returns a normalized version of self.If <tt>deep</tt> is set
     * <tt>false</tt> then the assertions are not normalized and it returns a
     * partially normalized version of self.
     * 
     * @param reg
     *            a PolicyRegistry from which the PolicyReferences are resolved
     * @param deep
     *            a flag to indicate whether to normalize the assertions
     * @return a normalzied version of self
     */
    public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
        return normalize(this, reg, deep);
    }

    /**
     * Returns a Policy that is the merge of specified Policy and self.
     * 
     * @param policy
     *            the Policy to be merged with self
     * @return a Policy that is the merge of the specified Policy and self
     */
    public Policy merge(Policy policy) {
        Policy result = new Policy();
        result.addPolicyComponents(getPolicyComponents());
        result.addPolicyComponents(policy.getPolicyComponents());
        return result;
    }

    /**
     * Throws an UnSupportedOpertionException. TODO for a next version.
     */
    public Policy intersect(Policy policy) {
        throw new UnsupportedOperationException();
    }

    /**
     * Serializes the Policy to a XMLStreamWriter.
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String wspPrefix = writer.getPrefix(Constants.URI_POLICY_NS);

        if (wspPrefix == null) {
            wspPrefix = Constants.ATTR_WSP;
            writer.setPrefix(wspPrefix, Constants.URI_POLICY_NS);
        }

        String wsuPrefix = writer.getPrefix(Constants.URI_WSU_NS);
        if (wsuPrefix == null) {
            wsuPrefix = Constants.ATTR_WSU;
            writer.setPrefix(wsuPrefix, Constants.URI_WSU_NS);
        }

        writer.writeStartElement(wspPrefix, Constants.ELEM_POLICY,
                Constants.URI_POLICY_NS);

        QName key;
        
        String prefix = null;
        String namespaceURI = null;
        String localName = null;

        Map<String, String> prefix2ns = new HashMap<String, String>();

        for (Iterator iterator = getAttributes().keySet().iterator(); iterator
                .hasNext();) {
            
            key = (QName) iterator.next();
            localName = key.getLocalPart();
            
            namespaceURI = key.getNamespaceURI();
            namespaceURI = (namespaceURI == null || namespaceURI.length() == 0) ? null : namespaceURI;
                        
            if (namespaceURI != null) {
                
                String writerPrefix = writer.getPrefix(namespaceURI);
                writerPrefix = (writerPrefix == null || writerPrefix.length() == 0) ? null : writerPrefix;
                
                if (writerPrefix == null) {
                    prefix = key.getPrefix();
                    prefix = (prefix == null || prefix.length() == 0) ? null : prefix;
                    
                } else {
                    prefix = writerPrefix;
                }
                
                if (prefix != null) {
                    writer.writeAttribute(prefix, namespaceURI, localName, getAttribute(key));
                    prefix2ns.put(prefix, key.getNamespaceURI());

                } else {
                    writer.writeAttribute(namespaceURI, localName, getAttribute(key));
                }
                    
            } else {
                writer.writeAttribute(localName, getAttribute(key));
            }

            
        }

        // writes xmlns:wsp=".."
        writer.writeNamespace(wspPrefix, Constants.URI_POLICY_NS);

        String prefiX;

        for (Iterator iterator = prefix2ns.keySet().iterator(); iterator
                .hasNext();) {
            prefiX = (String) iterator.next();
            writer.writeNamespace(prefiX, (String) prefix2ns.get(prefiX));
        }

        PolicyComponent policyComponent;

        for (Iterator iterator = getPolicyComponents().iterator(); iterator
                .hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            policyComponent.serialize(writer);
        }

        writer.writeEndElement();

    }

    /**
     * Returns Constants.TYPE_POLICY
     */
    public short getType() {
        return Constants.TYPE_POLICY;
    }

    /**
     * Returns an Iterator that will return a list of assertions correspond to a
     * Policy alternative if any. The <tt>iterator.next()</tt> will return a
     * list of assertions correspond to a Policy alternative if any and
     * <tt>iterator.hasNext()</tt> will indicates whether there is another
     * Policy alternative.
     * 
     * @return
     */
    public Iterator getAlternatives() {
        return new PolicyIterator(this);
    }

    private class PolicyIterator implements Iterator {
        Iterator alternatives = null;

        public PolicyIterator(Policy policy) {
            policy = (Policy) policy.normalize(false);
            ExactlyOne exactlyOne = (ExactlyOne) policy
                    .getFirstPolicyComponent();
            alternatives = exactlyOne.getPolicyComponents().iterator();
        }

        public boolean hasNext() {
            return alternatives.hasNext();
        }

        public Object next() {
            return ((All) alternatives.next()).getPolicyComponents();
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "policyAlternative.remove() is not supported");
        }
    }

    /**
     * Adds an attribute to self.
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     */
    public void addAttribute(QName name, String value) {
        attributes.put(name, value);
    }

    /**
     * Returns the value of the attribute specified by the QName. Returns
     * <tt>null</tt> if not present.
     * 
     * @param name
     *            the QName of the attribute
     * @return the value of the attribute specified by the QName
     */
    public String getAttribute(QName name) {
        return attributes.get(name);
    }

    /**
     * Returns a <tt>Map</tt> of all attributes of self.
     * 
     * @return a Map of all attributes of self
     */
    public Map<QName, String> getAttributes() {
        return attributes;
    }

    /**
     * Sets the <tt>Name</tt> attribute of self.
     * 
     * @param name
     *            the Name attribute of self
     */
    public void setName(String name) {
        addAttribute(new QName("", Constants.ATTR_NAME), name);
    }

    /**
     * Returns the <tt>Name</tt> attribute of self.
     * 
     * @return the Name attribute of self
     */
    public String getName() {
        return getAttribute(new QName("", Constants.ATTR_NAME));
    }

    /**
     * Sets the wsu:Id attribute of self.
     * 
     * @param id
     *            the Id attribute of self
     */
    public void setId(String id) {
        addAttribute(new QName(Constants.URI_WSU_NS, Constants.ATTR_ID), id);
    }

    /**
     * Returns the Id attribute of self.
     * 
     * @return the Id attribute of self
     */
    public String getId() {
        return getAttribute(new QName(Constants.URI_WSU_NS, Constants.ATTR_ID));
    }
}
