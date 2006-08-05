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
package org.apache.ws.security.policy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.security.policy.Constants;
import org.apache.ws.security.policy.model.AlgorithmSuite;
import org.apache.ws.security.policy.model.Layout;
import org.apache.ws.security.policy.model.SupportingToken;
import org.apache.ws.security.policy.model.TransportBinding;
import org.apache.ws.security.policy.model.TransportToken;

public class TransportBindingBuilder implements AssertionBuilder {
 
    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        TransportBinding transportBinding = new TransportBinding();
        OMElement policyElement = element.getFirstElement();
        
        Policy policy = PolicyEngine.getPolicy(policyElement);
        policy = (Policy) policy.normalize(false);
        
        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
            processAlternative((List) iterator.next(), transportBinding, factory);
        }
        
        
        return transportBinding;
    }
    
    private void processAlternative(List assertionList, TransportBinding parent, AssertionBuilderFactory factory) {
        TransportBinding transportBinding = new TransportBinding();
        
        for (Iterator iterator = assertionList.iterator(); iterator.hasNext(); ) {
            
            Assertion primitive = (Assertion) iterator.next();
            QName name = primitive.getName();
            
            if (name.equals(Constants.ALGORITHM_SUITE)) {
                transportBinding.setAlgorithmSuite((AlgorithmSuite) primitive);
                
            } else if (name.equals(Constants.TRANSPORT_TOKEN)) {
                transportBinding.setTransportToken(((TransportToken) primitive));
                
            } else if (name.equals(Constants.INCLUDE_TIMESTAMP)) {
                transportBinding.setIncludeTimestamp(true);
                
            } else if (name.equals(Constants.LAYOUT)) {
                transportBinding.setLayout((Layout) primitive);
                 
            } else if (name.equals(Constants.SIGNED_SUPPORTING_TOKENS)) {
                transportBinding.setSignedSupportingToken((SupportingToken) primitive);
                
            } else if (name.equals(Constants.SIGNED_ENDORSING_SUPPORTING_TOKENS)) {
                transportBinding.setSignedEndorsingSupportingTokens((SupportingToken) primitive);
            }
        }
        parent.addOption(transportBinding);
    }
}
