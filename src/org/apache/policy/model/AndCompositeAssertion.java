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

package org.apache.policy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.policy.util.PolicyRegistry;

/**
 * AndCompositeAssertion represents either policy or a single policy 
 * alternative. It requires that all its terms are satisfied.
 */
public class AndCompositeAssertion extends CompositeAssertion implements Assertion {
    
    public AndCompositeAssertion() {
    }
        
    /**
     * Adds an Assertion to its terms list
     * @param assertion Assertion to be added
     */
    public void addTerm(Assertion assertion) {
        if (!(isNormalized() && (assertion instanceof PrimitiveAssertion))) {
            setNormalized(false);
        }
        super.addTerm(assertion);
    }
    
    /**
     * Returns the intersection of self and argument against a 
     * specified Policy Registry.
     *  
     * @param assertion  the assertion to intersect with self
     * @param reg a sepcified policy registry
     * @return assertion the assertion which is equivalent to 
     *                   intersection between self and the argument
     */
    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
        
        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this : normalize(reg));
        
        if (!(normalizedMe instanceof AndCompositeAssertion)) {
            return normalizedMe.intersect(assertion, reg);
        }
                
        if (assertion instanceof PrimitiveAssertion) {
            QName qname = ((PrimitiveAssertion) assertion).getName();
            Iterator iterator = getTerms().iterator();
            boolean isMatch = false;
            
            while (iterator.hasNext()) {
                PrimitiveAssertion primTerm = (PrimitiveAssertion) iterator.next();
                if (primTerm.getName().equals(qname)) {
                    isMatch = true;
                    break;
                }
            }
            return (isMatch) ? normalizedMe : new XorCompositeAssertion();
        }
        
        CompositeAssertion target = (CompositeAssertion) assertion;
        target = (CompositeAssertion) ((target.isNormalized()) ? target : target.normalize(reg));
        
        if (target instanceof Policy) {
            XorCompositeAssertion alters = (XorCompositeAssertion) target.getTerms().get(0);
            return normalizedMe.intersect(alters);
            
        } else if (target instanceof XorCompositeAssertion) {
            XorCompositeAssertion result = new XorCompositeAssertion();
            Iterator iterator = target.getTerms().iterator();
            
            while (iterator.hasNext()) {
                AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator.next();
                Assertion value = normalizedMe.intersect(andTerm);
                if (value instanceof AndCompositeAssertion) {
                    result.addTerm(value);
                }
            }
            return result;
        }
        
        if (normalizedMe.isEmpty()) {
            return target;
        }
        if (target.isEmpty()) {
            return normalizedMe;
        }
                
        List primTermsA = ((size() > target.size()) ? normalizedMe.getTerms() : target.getTerms());
        List primTermsB = ((size() > target.size()) ? target.getTerms() : normalizedMe.getTerms());

        boolean isMatch = true;
        PrimitiveAssertion primTermA, primTermB;
        QName qnameA, qnameB;
        
        for (int i = 0; i < primTermsA.size(); i++) {
            primTermA = (PrimitiveAssertion) primTermsA.get(i);
            qnameA = primTermA.getName();
            boolean flag = false;
            
            for (int j = 0; j < primTermsB.size(); j++) {
                primTermB = (PrimitiveAssertion) primTermsB.get(j);
                qnameB = primTermB.getName();
                if (qnameA.equals(qnameB)) {
                    flag = true;
                    break;
                }                       
            }
            if (!flag) {
                isMatch = false;
                break;
            }                   
        }
        
        if (isMatch) { // vocabulary matches
            AndCompositeAssertion result = new AndCompositeAssertion();
            result.addTerms(primTermsA);
            result.addTerms(primTermsB);
            return result;
        }
        
        return new XorCompositeAssertion(); // no behaviour is admisible
    }
    
    
    
    /**
     * Returns an assertion which is equivalent to merge of self and the 
     * argument. 
     * 
     * @param assertion the assertion to be merged with
     * @param reg the policy registry which the is used resolve external policy
     *        references
     * @return assertion the resultant assertion which is equivalent to merge 
     *         of self and argument
     */
    public Assertion merge(Assertion assertion, PolicyRegistry reg) {
        
        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this : normalize(reg));
        
        if (!(normalizedMe instanceof AndCompositeAssertion)) {
            return normalizedMe.merge(assertion, reg);
        }
        
        if (assertion instanceof PrimitiveAssertion) {
            AndCompositeAssertion andTerm = new AndCompositeAssertion();
            andTerm.addTerm(assertion);
            andTerm.addTerms(normalizedMe.getTerms());
            andTerm.setNormalized(true);
            return andTerm;
        }

        CompositeAssertion target = (CompositeAssertion) assertion;
        target = (CompositeAssertion) ((target.isNormalized()) ? target : target.normalize(reg));
        
        if (target instanceof Policy) {
            XorCompositeAssertion xorTerm = (XorCompositeAssertion) target.getTerms().get(0);
            return normalizedMe.merge(xorTerm);
            
        } else if (target instanceof XorCompositeAssertion) {
            XorCompositeAssertion xorTerm = new XorCompositeAssertion();
            
            Iterator hisAndTerms = target.getTerms().iterator();
            while (hisAndTerms.hasNext()) {
                AndCompositeAssertion hisAndTerm = (AndCompositeAssertion) hisAndTerms.next();
                xorTerm.addTerm(normalizedMe.merge(hisAndTerm));                
            }
            xorTerm.setNormalized(true);
            return xorTerm;
            
        }  else if (target instanceof AndCompositeAssertion) {
            AndCompositeAssertion andTerm = new AndCompositeAssertion();
            andTerm.addTerms(normalizedMe.getTerms());
            andTerm.addTerms(target.getTerms());
            andTerm.setNormalized(true);
            return andTerm;         
        }
        
        throw new IllegalArgumentException("error : merge is not defined for" 
                + assertion.getClass().getName());
    }
    
    /**
     * Returns an Assertion which is normalized using a specified 
     * policy registry.
     * 
     * @param reg the policy registry used to resolve policy 
     *            references
     * @return an Assertion which is the normalized form of
     *         self 
     */
    public Assertion normalize(PolicyRegistry reg) {
        AndCompositeAssertion resultantAndTerm = new AndCompositeAssertion();
        XorCompositeAssertion resultantXorTerm = new XorCompositeAssertion();
        
        ArrayList childAndTermList = new ArrayList();
        ArrayList childXorTermList = new ArrayList();
        
        Iterator myTerms = getTerms().iterator();
        
        while (myTerms.hasNext()) {
            Object term = myTerms.next();
            
            if (term instanceof PolicyReference) {
                if (reg == null) {
                    throw new RuntimeException("PolicyCache is not defined");
                }
                PolicyReference policyRef = (PolicyReference) term;
                Policy policy = reg.lookup(policyRef.getPolicyURIString());
                if (policy == null) {
                    throw new RuntimeException("PolicyReference<" + policyRef.getPolicyURIString() + "can not be resolved");
                }
                
                AndCompositeAssertion andTerm = new AndCompositeAssertion();
                andTerm.addTerms(policy.getTerms());
                Assertion normalizedPolicyRef = andTerm.normalize(reg);
                
                if (normalizedPolicyRef instanceof AndCompositeAssertion) {
                    childAndTermList.add(normalizedPolicyRef);
                } else {
                    childXorTermList.add(normalizedPolicyRef);
                }
                
                 
            } else if (term instanceof PrimitiveAssertion) {
                resultantAndTerm.addTerm((Assertion) term);
            
            } else if (term instanceof CompositeAssertion) {
                CompositeAssertion cterm = (CompositeAssertion) term;
                
                cterm =((cterm.isNormalized()) ? cterm  :(CompositeAssertion) cterm.normalize(reg));
                
                if (cterm instanceof AndCompositeAssertion) {
                    childAndTermList.add(cterm);
                } else {
                    childXorTermList.add(cterm);
                }
            }
        }
        
        // processing child-AndCompositeAssertions
        
        if (! childAndTermList.isEmpty()) {
            Iterator ANDterms = childAndTermList.iterator();
            
            while (ANDterms.hasNext()) {
                CompositeAssertion ANDterm = (CompositeAssertion) ANDterms.next();
                resultantAndTerm.addTerms(ANDterm.getTerms());
            }           
        }       
                
        // processing child-XORCompositeAssertions
        if (childXorTermList.size() > 1) {
            
            outer : for (int i = 0; i < childXorTermList.size(); i++) {
                inner : for (int j = i; j < childXorTermList.size(); j++) {
                    if (i != j) {
                        XorCompositeAssertion xorTermA = (XorCompositeAssertion) childXorTermList.get(i);
                        XorCompositeAssertion xorTermB = (XorCompositeAssertion) childXorTermList.get(j);
                        
                        /*
                         * if xorTermA or xorTermB is empty then the result should be an 
                         * a policy with zero alternatives
                         */
                        
                        if (xorTermA.isEmpty() || xorTermB.isEmpty()) {
                            resultantXorTerm = new XorCompositeAssertion();
                            break outer;
                        }
                        Iterator interatorA = xorTermA.getTerms().iterator();
                        
                        while (interatorA.hasNext()) {
                            CompositeAssertion andTermA = (CompositeAssertion) interatorA.next();
                            Iterator iteratorB = xorTermB.getTerms().iterator();
                            
                            while (iteratorB.hasNext()) {
                            
                                CompositeAssertion andTermB = (CompositeAssertion) iteratorB.next();
                                AndCompositeAssertion andTerm = new AndCompositeAssertion();
                                andTerm.addTerms(andTermA.getTerms());
                                andTerm.addTerms(andTermB.getTerms());
                                resultantXorTerm.addTerm(andTerm);
                            }
                        }
                        
                    }
                }
            }
        
        } else if (childXorTermList.size() == 1) {
            CompositeAssertion XORterm = (CompositeAssertion) childXorTermList.get(0);
            resultantXorTerm.addTerms(XORterm.getTerms());
        }
        
        if (childXorTermList.isEmpty()) {
    
            resultantAndTerm.setNormalized(true);
            return resultantAndTerm;
        } 
        
        if (resultantXorTerm.isEmpty()) {
            if (resultantAndTerm.isEmpty()) {
                resultantAndTerm.setNormalized(true);
                return resultantAndTerm;
            }
            resultantXorTerm.setNormalized(true);
            return resultantXorTerm;
        }
        
        //  get list of primitive assertions form result (AndCompositeAssertion)
        List primTerms = resultantAndTerm.getTerms();
        
        // these terms should be AndCompositeAssertions
        Iterator andTerms = resultantXorTerm.getTerms().iterator();
        
        while (andTerms.hasNext()) {
            CompositeAssertion andTerm = (CompositeAssertion) andTerms.next();
            andTerm.addTerms(primTerms);
        }
        
        resultantXorTerm.setNormalized(true);
        return resultantXorTerm;
    }
}
