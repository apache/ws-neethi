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


import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.All;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyContainingAssertion;

/**
 * Implementation of an assertion that required exactly one (possibly empty) child element
 * of type Policy (as does for examples the wsam:Addressing assertion).
 * 
 */
public class PolicyContainingPrimitiveAssertion
    extends PrimitiveAssertion 
    implements PolicyContainingAssertion {
    
    protected Policy nested;
    
    public PolicyContainingPrimitiveAssertion(QName name, 
                                              boolean optional,
                                              boolean ignorable, 
                                              Policy p) {
        super(name, optional, ignorable);
        this.nested = p;
    }

    public PolicyComponent normalize() {
        Policy normalisedNested 
            = (Policy)nested.normalize(true);
        
        Policy p = new Policy(nested.getPolicyRegistry(), nested.getNamespace());
        ExactlyOne ea = new ExactlyOne();
        p.addPolicyComponent(ea);
        if (isOptional()) {
            ea.addPolicyComponent(new All());
        }
        // for all alternatives in normalized nested policy
        Iterator<List<Assertion>> alternatives = normalisedNested.getAlternatives();
        while (alternatives.hasNext()) {
            All all = new All();
            List<Assertion> alternative = alternatives.next();
            Policy n = new Policy(nested.getPolicyRegistry(), nested.getNamespace());
            Assertion a = clone(false, n);
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
    protected Assertion clone(boolean optional, Policy n) {
        return new PolicyContainingPrimitiveAssertion(name, optional, ignorable, n);
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (this == policyComponent) {
            return true;
        }
        if (!super.equal(policyComponent)) {
            return false;
        }
        PolicyContainingPrimitiveAssertion other = (PolicyContainingPrimitiveAssertion)policyComponent;
        return getPolicy().equal(other.getPolicy());
    }
    
    public void setPolicy(Policy n) {
        nested = n;
    }
    public Policy getPolicy() {
        return nested;
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
        nested.serialize(writer);
        writer.writeEndElement();
    }

}
