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


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyOperator;

/**
 * Implementation of an assertion that required exactly one (possibly empty) child element
 * of type Policy (as does for examples the wsam:Addressing assertion).
 * 
 */
public class NestedPrimitiveAssertion extends PrimitiveAssertion {
    private Policy nested;
    
    
    public NestedPrimitiveAssertion(QName name, boolean optional, Policy p) {
        super(name, optional);
        this.nested = p;
    }

    
    public PolicyComponent normalize() {
        Policy normalisedNested 
            = (Policy)nested.normalize(true);
        
        Policy p = new Policy();
        ExactlyOne ea = new ExactlyOne();
        p.addPolicyComponent(ea);
        if (isOptional()) {
            ea.addPolicyComponent(new All());
        }
        // for all alternatives in normalised nested policy
        Iterator<List<PolicyComponent>> alternatives = normalisedNested.getAlternatives();
        while (alternatives.hasNext()) {
            All all = new All();
            List<PolicyComponent> alternative = alternatives.next();
            Policy n = new Policy();
            NestedPrimitiveAssertion a = new NestedPrimitiveAssertion(getName(), false, nested);
            ExactlyOne nea = new ExactlyOne();
            n.addPolicyComponent(nea);
            All na = new All();
            nea.addPolicyComponent(na);
            na.addPolicyComponents(alternative);
            all.addPolicyComponent(a);
            ea.addPolicyComponent(all);            
        } 
        return p;      
    } 
    
    public boolean equal(PolicyComponent policyComponent) {
        
        if (!super.equal(policyComponent)) {
            return false;
        }
        NestedPrimitiveAssertion other = (NestedPrimitiveAssertion)policyComponent;
        return getPolicy().equal(other.getPolicy());
    }
    
    protected void setPolicy(Policy n) {
        nested = n;
    }
    public Policy getPolicy() {
        return nested;
    }
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(name.getNamespaceURI(), name.getLocalPart());
        if (optional) {
            writer.writeAttribute(Constants.Q_ELEM_OPTIONAL_ATTR.getNamespaceURI(),
                                  Constants.Q_ELEM_OPTIONAL_ATTR.getLocalPart(), "true");
        }
        nested.serialize(writer);
        writer.writeEndElement();
    }
    
    
}
