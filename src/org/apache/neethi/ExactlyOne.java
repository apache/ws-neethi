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

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ExactlyOne extends AbstractPolicyOperator {

    public PolicyComponent normalize(boolean deep) {
        ExactlyOne exactlyOne = new ExactlyOne();

        if (isEmpty()) {
            return exactlyOne;
        }
        
        for (Iterator iterator = getPolicyComponents().iterator(); iterator.hasNext();) {
            PolicyComponent component = (PolicyComponent) iterator.next();
            short type = component.getType();
            
            if (type == PolicyComponent.ASSERTION && deep) {
                component = ((Assertion) component).normalize();
                type = component.getType();       
            } 
            
            if (type == PolicyComponent.POLICY) {
                All wrapper = new All();
                wrapper.addPolicyComponents(((Policy) component).getPolicyComponents());
                
                component = wrapper.normalize(deep);
                type = component.getType();
                
            } else if (type != PolicyComponent.ASSERTION) {
                component = ((PolicyOperator) component).normalize(deep);
                type = component.getType();
            }
            
            if (type == PolicyComponent.EXACTLYONE) {
                exactlyOne.addPolicyComponents(((ExactlyOne) component).getPolicyComponents());
                
            } else if (type == PolicyComponent.ALL) {
                exactlyOne.addPolicyComponent(component);
                
            } else {
                All wrapper = new All();
                wrapper.addPolicyComponent(component);
                exactlyOne.addPolicyComponent(wrapper);                
            }
        }
        
        return exactlyOne;
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NAMESPACE);

        if (prefix == null) {
            writer.writeStartElement(PREFIX, EXACTLYONE, NAMESPACE);
            writer.writeNamespace(PREFIX, NAMESPACE);
            writer.setPrefix(PREFIX, NAMESPACE);
        } else {
            writer.writeStartElement(NAMESPACE, EXACTLYONE);
        }
        
        PolicyComponent policyComponent;
        
        for (Iterator iterator = getPolicyComponents().iterator(); iterator.hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            policyComponent.serialize(writer);
        }
        
        writer.writeEndElement();

    }
    
    public final short getType() {
        return PolicyComponent.EXACTLYONE;
    }
    
}
