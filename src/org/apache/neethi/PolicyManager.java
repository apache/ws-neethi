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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.builders.AssertionBuilder;

public class PolicyManager {
    
    public static final String POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    
    public static final String POLICY = "Policy";
    
    public static final String EXACTLY_ONE = "ExactlyOne";
    
    public static final String ALL = "All";
    
    private static AssertionBuilderFactory factory = new AssertionBuilderFactory();
    
    
    public static void registerBuilder(QName qname, AssertionBuilder builder) {
        factory.registerBuilder(qname, builder);
    }
    public static Policy getPolicy(OMElement element) {
        return getPolicyOperator(element);        
    }
    
    public static Policy getPolicyOperator(OMElement element) {
        return (Policy) processOperationElement(element, new Policy());        
    }
    
    public static ExactlyOne getExactlyOneOperator(OMElement element) {
        return (ExactlyOne) processOperationElement(element, new ExactlyOne());
    }
    
    public static All getAllOperator(OMElement element) {
        return (All) processOperationElement(element, new All());
    }
    
    private static PolicyOperator processOperationElement(OMElement operationElement, PolicyOperator operator) {
        
        OMElement childElement;
        
        for (Iterator iterator = operationElement.getChildElements(); iterator.hasNext(); ) {
            childElement = (OMElement) iterator.next();
            
            if (PolicyOperator.NAMESPACE.equals(childElement.getNamespace().getName())) {
                
                if (PolicyOperator.POLICY.equals(childElement.getLocalName())) {
                    operator.addPolicyComponent(getPolicyOperator(childElement));
                    
                } else if (PolicyOperator.EXACTLYONE.equals(childElement.getLocalName())) {
                    operator.addPolicyComponent(getExactlyOneOperator(childElement));
                    
                } else if (PolicyOperator.ALL.equals(childElement.getLocalName())) {
                    operator.addPolicyComponent(getAllOperator(childElement));
                }
            } else {
                
                AssertionBuilder builder = factory.getBuilder(childElement.getNamespace().getName());
                
                if (builder == null) {    
                    XmlPrimtiveAssertion xmlPrimtiveAssertion = new XmlPrimtiveAssertion(childElement);
                    operator.addPolicyComponent(xmlPrimtiveAssertion);
                    
                } else {
                    operator.addPolicyComponent(builder.build(childElement, factory));
                }                
            }
        }
        return operator;
    } 
}
