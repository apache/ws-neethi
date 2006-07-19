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
package org.apache.neethi;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Policy extends AbstractPolicyOperator {

    public PolicyComponent normalize(boolean deep) {
        
        All all = new All();
        ExactlyOne exactlyOne = new ExactlyOne();

        ArrayList exactlyOnes = new ArrayList();

        if (isEmpty()) {
            Policy policy = new Policy();
            policy.addPolicyComponent(exactlyOne);
            exactlyOne.addPolicyComponent(all);
            return policy;
        }

        PolicyComponent component;

        for (Iterator iterator = getPolicyComponents().iterator(); iterator
                .hasNext();) {
            component = (PolicyComponent) iterator.next();
            short type = component.getType();
            
            if (type == PolicyComponent.ASSERTION && deep) {
                component = ((Assertion) component).normalize();
                type = component.getType();
            }
            
            if (type == PolicyComponent.POLICY) {
                All wrapper = new All();
                wrapper.addPolicyComponents(((Policy) component)
                        .getPolicyComponents());
                component = wrapper.normalize(deep);
                type = component.getType();
                
            } else if (type != PolicyComponent.ASSERTION) {
                component = ((PolicyOperator) component).normalize(deep);
                type = component.getType();
            }
            
            if (type == PolicyComponent.EXACTLYONE) {

                if (((ExactlyOne) component).isEmpty()) {
                    Policy policy = new Policy();
                    ExactlyOne anExactlyOne = new ExactlyOne();
                    
                    policy.addPolicyComponent(anExactlyOne);
                    return policy;

                } else {
                    exactlyOnes.add(component);
                }

            } else if (type == PolicyComponent.ALL) {
                all
                        .addPolicyComponents(((All) component)
                                .getPolicyComponents());

            } else {
                all.addPolicyComponent(component);
            }
        }

        // processing child ExactlyOne operators
        if (exactlyOnes.size() > 1) {
            exactlyOne.addPolicyComponents(crossProduct(exactlyOnes, 0, false));

        } else if (exactlyOnes.size() == 1) {
            ExactlyOne anExactlyOne = (ExactlyOne) exactlyOnes.get(0);
            exactlyOne.addPolicyComponents(anExactlyOne.getPolicyComponents());
        }

        if (exactlyOne.isEmpty()) {
            Policy policy = new Policy();
            ExactlyOne anExactlyOne = new ExactlyOne();
            
            policy.addPolicyComponent(anExactlyOne);
            anExactlyOne.addPolicyComponent(all);
            return policy;
            
        } else if (all.isEmpty()) {
            Policy policy = new Policy();
            policy.addPolicyComponent(exactlyOne);
            
            return policy;
            
        } else {
            Policy policy = new Policy();
            
            All anAll;
            
            for (Iterator iterator = exactlyOne.getPolicyComponents()
                    .iterator(); iterator.hasNext();) {
                anAll = (All) iterator.next();
                anAll.addPolicyComponents(all.getPolicyComponents());
            }
            
            policy.addPolicyComponent(exactlyOne);
            return policy;
        }
    }
    
    public Policy merge(Policy policy) {
        
        Policy result = new Policy();
        ExactlyOne alternative = new ExactlyOne();
        result.addPolicyComponent(alternative);
        
        ArrayList alternatives = new ArrayList();
        policy = (Policy) policy.normalize(false);
        
        alternatives.add(this.getFirstPolicyComponent());
        alternatives.add(policy.getFirstPolicyComponent());
        
        alternative.addPolicyComponents(crossProduct(alternatives, 0, false));
        
        return result;
    }
    
    public Policy intersect(Policy policy) {
        
        
        throw new UnsupportedOperationException("still not implemented");
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NAMESPACE);

        if (prefix == null) {
            writer.writeStartElement(PREFIX, POLICY, NAMESPACE);
            writer.writeNamespace(PREFIX, NAMESPACE);
            writer.setPrefix(PREFIX, NAMESPACE);
            
        } else {
            writer.writeStartElement(NAMESPACE, POLICY);
        }
        
        PolicyComponent policyComponent;
        
        for (Iterator iterator = getPolicyComponents().iterator(); iterator.hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            policyComponent.serialize(writer);
        }
        
        writer.writeEndElement();

    }
    
    public final short getType() {
        return PolicyComponent.POLICY;
    }
    
    public Iterator getAlternatives() {
        return new PolicyIterator(this);
    }
    
    private class PolicyIterator implements Iterator {
        Iterator alternatives = null;
        
        public PolicyIterator(Policy policy) {
            policy = (Policy) policy.normalize(false);
            ExactlyOne exactlyOne = (ExactlyOne) policy.getFirstPolicyComponent();
            alternatives = exactlyOne.getPolicyComponents().iterator();
        }

        public boolean hasNext() {
            return alternatives.hasNext();
        }

        public Object next() {
            return ((All) alternatives.next()).getPolicyComponents();
        }

        public void remove() {
            throw new UnsupportedOperationException("policyAlternative.remove() is not supported");
        }
        
    }
    
}
