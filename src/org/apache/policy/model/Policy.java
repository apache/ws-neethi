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

import org.apache.policy.util.PolicyRegistry;

/**
 * Policy is the access point for policy framework. It the object model that 
 * represents a policy at runtime.
 *
 */
public class Policy extends AndCompositeAssertion implements Assertion {
    private String policyURI = null;
    private String xmlBase = null;
    private String id = null;
    
    public Policy() {
        setNormalized(false);
    }
    
    public Policy(String id) {
        this(null, id);
        setNormalized(false);
    }
    
    public Policy(String xmlBase, String id) {
        this.xmlBase = xmlBase;
        this.id = id;       
        setNormalized(false);
    }
    
    public void setBase(String xmlBase) {
        this.xmlBase = xmlBase;
    }
    
    public String getBase() {
        return xmlBase;
    }
    
    public void setId(String id) {
        this.id = id;   
    }
    
    public String getId() {
        return id;
    }
    
    public String getPolicyURI() {
        return (xmlBase != null) ? xmlBase + "#" + id : "#" + id;
    }
    
    public Assertion normalize() {
        return normalize(null);
    }
    
    public Assertion normalize(PolicyRegistry reg) {
        if (getParent() == null) {
            
            String xmlBase = getBase();
            String id      = getId();
            Policy result = new Policy(xmlBase, id);
            
            AndCompositeAssertion resultantAndTerm = new AndCompositeAssertion();
            XorCompositeAssertion resultantXorTerm = new XorCompositeAssertion();
            
            ArrayList childAndTermList = new ArrayList();
            ArrayList childXorTermList = new ArrayList();
            
            Iterator myTerms = getTerms().iterator();
            
            while (myTerms.hasNext()) {
                Object term = myTerms.next();
            
                if (term instanceof PrimitiveAssertion) {
                    resultantAndTerm.addTerm((Assertion) term);
                
                } else if (term instanceof PolicyReference) {
                    
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
            
            // processing child-AndCompositeAssertion
            if (! childAndTermList.isEmpty()) {
                Iterator andTerms = childAndTermList.iterator();
                
                while (andTerms.hasNext()) {
                    CompositeAssertion andTerm = (CompositeAssertion) andTerms.next();
                    resultantAndTerm.addTerms(andTerm.getTerms());
                }           
            }       
                    
            // processing child-XORCompositeAssertions
            if (childXorTermList.size() > 1) {
                
                outer : for (int i = 0; i < childXorTermList.size(); i++) {
                    inner : for (int j = i; j < childXorTermList.size(); j++) {
                        if (i != j) {
                            XorCompositeAssertion xorTermA = (XorCompositeAssertion) childXorTermList.get(i);
                            XorCompositeAssertion xorTermB = (XorCompositeAssertion) childXorTermList.get(j);
                            
                            // what if XORtermA or XORtermB is empty?
                            if (xorTermA.isEmpty() || xorTermB.isEmpty()) {
                                resultantXorTerm = new XorCompositeAssertion();
                                break outer;
                            }
                            Iterator iterA = xorTermA.getTerms().iterator();
                            
                            while (iterA.hasNext()) {
                                // must be an ANDterm
                                CompositeAssertion andTermA = (CompositeAssertion) iterA.next();
                                Iterator iterB = xorTermB.getTerms().iterator();
                                while (iterB.hasNext()) {
                                    // must be an ANDterm
                                    CompositeAssertion andTermB = (CompositeAssertion) iterB.next();
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
                CompositeAssertion xorTerm = (CompositeAssertion) childXorTermList.get(0);
                resultantXorTerm.addTerms(xorTerm.getTerms());
            }
                    
            if (childXorTermList.isEmpty()) {
                XorCompositeAssertion alters = new XorCompositeAssertion();
                alters.addTerm(resultantAndTerm);
                result.addTerm(alters);
                result.setNormalized(true);
                return result;
            } 
            
            if (resultantXorTerm.isEmpty()) {
                result.addTerm(resultantXorTerm);
                result.setNormalized(true);
                return result;
            }
            
            //  get list of primitive assertions form result (AndCompositeAssertion)
            List primTerms = resultantAndTerm.getTerms();
            
            // these terms should be AndCompositeAssertions
            Iterator andTerms = resultantXorTerm.getTerms().iterator();
            
            while (andTerms.hasNext()) {
                CompositeAssertion andTerm = (CompositeAssertion) andTerms.next();
                andTerm.addTerms(primTerms);
            }
            result.addTerm(resultantXorTerm);
            result.setNormalized(true);
            return result;
            
        } else {
            return super.normalize();
        }
    }
    
    public Assertion intersect(Assertion assertion , PolicyRegistry reg) {
        
        Policy result = new Policy(getBase(), getId());
        Policy normalizedMe = (Policy) ((isNormalized()) ? this : normalize(reg));
                
        XorCompositeAssertion alters = (XorCompositeAssertion) normalizedMe.getTerms().get(0);
        
        if (assertion instanceof PrimitiveAssertion) {
            result.addTerm(alters.intersect(assertion, reg));
            return result;
            
        } else {
            CompositeAssertion target = (CompositeAssertion) assertion;
            target = (CompositeAssertion) ((target.isNormalized()) ? target : target.normalize(reg));
            
            if (target instanceof Policy) {
                XorCompositeAssertion alters2 = (XorCompositeAssertion) target.getTerms().get(0);
                result.addTerm(alters.intersect(alters2));
                return result;
            } else {
                result.addTerm(alters.intersect(target));
                return result;
            }
        }
    }
    
    public Assertion merge(Assertion assertion, PolicyRegistry reg) {
        Policy result = new Policy(getBase(), getId());
        Policy normalizedMe = (Policy) ((isNormalized()) ? this : normalize(reg));
        XorCompositeAssertion alters = (XorCompositeAssertion) normalizedMe.getTerms().get(0);
        Assertion test = alters.merge(assertion, reg);
        
        result.addTerm(test);
        result.setNormalized(true);
        return result;
    }
}
