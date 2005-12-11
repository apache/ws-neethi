/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.ws.policy.util;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;

/**
 * WSPolicyUtil contains several utility methods for policy manipulations.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyUtil {
    
    public static boolean matchByQName(PrimitiveAssertion primTermA, 
            PrimitiveAssertion primTermB) {
        return primTermA.getName().equals(primTermB.getName());
    }
    
    public static boolean matchByQName(List primTermsA,List primTermsB) {
        List larger = (primTermsA.size() > primTermsB.size()) 
                ? primTermsA :primTermsB;
        List smaller = (primTermsA.size() < primTermsB.size()) 
                ? primTermsA : primTermsB;
    
        Iterator iterator = larger.iterator();
        PrimitiveAssertion primTerm;
        QName qname;
        Iterator iterator2;
        while (iterator.hasNext()) {
            primTerm = (PrimitiveAssertion) iterator.next();
            qname = primTerm.getName();
            iterator2 = smaller.iterator();
            
            boolean match = false;
            PrimitiveAssertion primTerm2;
            while (iterator2.hasNext()) {
                primTerm2 = (PrimitiveAssertion) iterator2.next();
                if (primTerm2.getName().equals(qname)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }
    
    
    public static List getPrimTermsList(Policy policy) {
        if (!policy.isNormalized()) {
            policy = (Policy) policy.normalize();
        }
        
        XorCompositeAssertion xorTerm 
            = (XorCompositeAssertion) policy.getTerms().get(0);
        AndCompositeAssertion andTerm 
            = (AndCompositeAssertion) xorTerm.getTerms().get(0);
        
        return andTerm.getTerms();
    }
    
    public static Policy getSinglePolicy(List policyList, PolicyRegistry reg) {
        Policy policyTerm = null;
        Iterator iterator = policyList.iterator();
        
        Policy policyTerm2;
        while (iterator.hasNext()) {
            policyTerm2 = (Policy) iterator.next();
            policyTerm = (policyTerm == null) 
                    ? policyTerm2 : (Policy) policyTerm.merge(policyTerm2, reg);
        }
        
        return policyTerm;
    }
    
    public static Policy getPolicy(List terms) {
        Policy policyTerm = new Policy();
        XorCompositeAssertion xorTerm = new XorCompositeAssertion();
        AndCompositeAssertion andTerm = new AndCompositeAssertion();
        
        andTerm.addTerms(terms);
        xorTerm.addTerm(andTerm);
        policyTerm.addTerm(xorTerm);
        
        return policyTerm;
    }
}
