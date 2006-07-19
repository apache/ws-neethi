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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

public class XmlPrimtiveAssertion implements PrimitiveAssertion {

    OMElement element;
    boolean isOptional;
    QName optionalAttri = new QName(PolicyOperator.NAMESPACE, "Optional", PolicyOperator.PREFIX);
    
    
    public XmlPrimtiveAssertion(OMElement element) {
        setValue(element);
        setOptionality(element);        
    }
    
    public QName getName() {
        return (element != null) ? element.getQName() : null;
    }

    public void setValue(OMElement element) {
        this.element = element;
    }

    public OMElement getValue() {
        return element;
    }
    
    public boolean isOptional() {
        return isOptional;
    }
    
    public PolicyComponent normalize() throws IllegalArgumentException {
        return normalize(null);
    }

    public PolicyComponent normalize(PolicyRegistry registry) {
        
        if (isOptional) {
            Policy policy = new Policy();
            ExactlyOne alternatives = new ExactlyOne();
            
            All alternative1 = new All();
            OMElement element1 = element.cloneOMElement();
            element1.removeAttribute(element1.getAttribute(optionalAttri));
            alternative1.addPolicyComponent(new XmlPrimtiveAssertion(element1));
            alternatives.addPolicyComponent(alternative1);
            
            All alternative2 = new All();
            alternatives.addPolicyComponent(alternative2);
            
            policy.addPolicyComponent(alternatives);
            return policy;
        }
        return this;
    }

    public void serialize(XMLStreamWriter writer) {
        if (element != null) {
            try {
                element.serialize(writer);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            // TODO throw an exception??
        }
    }
    
    public final short getType() {
        return PolicyComponent.ASSERTION;
    }
    
    private void setOptionality(OMElement element) {
        OMAttribute attribute = element.getAttribute(optionalAttri);
        if (attribute != null) {
            this.isOptional = (new Boolean(attribute.getAttributeValue()).booleanValue());
            
        } else {
            this.isOptional = false;            
        }        
    }
}
