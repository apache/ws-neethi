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

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/** 
 * All is a PolicyOperator that require all its PolicyComponents to be met.
 */
public class All extends AbstractPolicyOperator {
    
    public All() {
        super();
    }
    public All(PolicyOperator parent) {
        super(parent);
    }
    
    /**
     * Adds an assertion to its PolicyComponent list.
     * 
     * @param assertion the assertion to be added.
     */
    public void addAssertion(Assertion assertion) {
        addPolicyComponent(assertion);
    }

    /**
     * Returns a <tt>List</tt> of it's PolicyComponents.
     * 
     * @return a List of it's PolicyComponents
     */
    public List<PolicyComponent> getAssertions() {
        return policyComponents;
    }

    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String namespace = Constants.findPolicyNamespace(writer);
        String prefix = writer.getPrefix(namespace);

        if (prefix == null) {
            writer.writeStartElement(Constants.ATTR_WSP, Constants.ELEM_ALL, namespace);
            writer.writeNamespace(Constants.ATTR_WSP, namespace);
            writer.setPrefix(Constants.ATTR_WSP, namespace);
        } else {
            writer.writeStartElement(namespace, Constants.ELEM_ALL);
        }

        for (PolicyComponent policyComponent : getPolicyComponents()){
            policyComponent.serialize(writer);
        }

        writer.writeEndElement();
    }

    /**
     * Returns Constants.TYPE_ALL
     */
    public short getType() {
        return Constants.TYPE_ALL;
    }
}
