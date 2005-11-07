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

import java.util.Iterator;

import org.apache.policy.util.PolicyRegistry;

/**
 * XORCompositeAssertion represents a bunch of policy alternatives. It requires
 * that exactly one of its terms (policy alternative) is statisfied.
 * 
 */
public class XorCompositeAssertion extends CompositeAssertion implements Assertion  {
	
	public XorCompositeAssertion() {
	}
	
	public void addTerm(Assertion assertion) {
		if(!(isNormalized() && (assertion instanceof AndCompositeAssertion) 
				&& ((AndCompositeAssertion) assertion).isNormalized())) {
			setNormalize(false);
		}
		super.addTerm(assertion);
	}
	
	public Assertion normalize(PolicyRegistry reg) {
		XorCompositeAssertion xorLogic = new XorCompositeAssertion();
		Iterator terms = getTerms().iterator();
		
		while (terms.hasNext()) {
			Assertion term = (Assertion) terms.next();
			
			if (term instanceof PrimitiveAssertion) { // just wrap it in an AND
													  // logic and add 
				AndCompositeAssertion wrapper = new AndCompositeAssertion();
				wrapper.addTerm(term);
				xorLogic.addTerm(wrapper);
			} else if (term instanceof PolicyReference) {
				if (reg == null) {
					throw new RuntimeException("PolicyCache is not defined");
				}
				
				PolicyReference policyRef = (PolicyReference) term;
				Policy policy =  reg.lookup(policyRef.getPolicyURIString());
				if (policy == null) {
					throw new RuntimeException("PolicyReference<" + policyRef.getPolicyURIString() +"> cannot be resolved");
				} 
				AndCompositeAssertion andTerm = new AndCompositeAssertion();
				andTerm.addTerms(policy.getTerms());
				Assertion normalizedPolicy = andTerm.normalize(reg);
				if (normalizedPolicy instanceof XorCompositeAssertion) {
					xorLogic.addTerms(((XorCompositeAssertion) normalizedPolicy).getTerms());
				} else {
					xorLogic.addTerm(normalizedPolicy);
				}
				
			} else {
				// must be a composite assertion
				CompositeAssertion cterm = (CompositeAssertion) term;
				cterm =((cterm.isNormalized()) ? cterm  :(CompositeAssertion) cterm.normalize(reg));
				
				if (cterm instanceof XorCompositeAssertion) {
					// just adds the child-terms to super
					xorLogic.addTerms(cterm.getTerms());
				} else {
					// must be an AndCompositeAssertion with primitives
					xorLogic.addTerm(cterm);
				}
			}
		}
		xorLogic.setNormalize(true);
		return xorLogic;
	}
	
	public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
		CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this : normalize(reg));
		
		if (!(normalizedMe instanceof XorCompositeAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}
		
		XorCompositeAssertion result = new XorCompositeAssertion();
		
		if (assertion instanceof PrimitiveAssertion) {
		
			Iterator iterator = normalizedMe.getTerms().iterator();
			
			while (iterator.hasNext()) {
				AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator.next();
				Assertion value = andTerm.intersect(assertion);
				if (value instanceof AndCompositeAssertion) {
					result.addTerm(value);
				}
			}
							
		} else {
			CompositeAssertion target = (CompositeAssertion) assertion;
			target = (CompositeAssertion) ((target.isNormalized()) ? target : target.normalize(reg));
			
			Iterator iterator = normalizedMe.getTerms().iterator();
			while (iterator.hasNext()) {
				AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator.next();
		
				if (target instanceof AndCompositeAssertion) {
					Assertion value = andTerm.intersect(target);
					
					if (value instanceof AndCompositeAssertion) {
						result.addTerm(value);
					}				
					
				} else if (target instanceof XorCompositeAssertion) {
					
					Iterator andTerms = target.getTerms().iterator();
										
					while (andTerms.hasNext()) {
						AndCompositeAssertion tAndTerm = (AndCompositeAssertion) andTerms.next();
						Assertion value = andTerm.intersect(tAndTerm);
						
						if (value instanceof AndCompositeAssertion) {
							result.addTerm(value);
						}
					}
				}			
			}			
		}
		
		return result;
	}

	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this : normalize(reg));
	
		if (!(normalizedMe instanceof XorCompositeAssertion)) {
			return normalizedMe.merge(assertion, reg);
		}
				
		if (assertion instanceof PrimitiveAssertion) {
			XorCompositeAssertion xorTerm = new XorCompositeAssertion();
			
			Iterator iterator = normalizedMe.getTerms().iterator();
			if (iterator.hasNext()) {
				do {
					AndCompositeAssertion andTerm = new AndCompositeAssertion();
					andTerm.addTerm(assertion);
					AndCompositeAssertion anAndTerm = (AndCompositeAssertion) iterator.next();
					andTerm.addTerms(anAndTerm.getTerms());
					xorTerm.addTerm(andTerm);
				} while (iterator.hasNext());
			} else {
				AndCompositeAssertion andTerm = new AndCompositeAssertion();
				andTerm.addTerm(assertion);
				xorTerm.addTerm(andTerm);
			}
			xorTerm.setNormalize(true);
			return xorTerm;
		}
		
		CompositeAssertion target = (CompositeAssertion) assertion;
		target = (CompositeAssertion) ((target.isNormalized()) ? target : target.normalize(reg));
		
		if (target instanceof Policy) {
			XorCompositeAssertion xorTerm = (XorCompositeAssertion) target.getTerms().get(0);
			return normalizedMe.merge(xorTerm);
			
		} else if (target instanceof XorCompositeAssertion) {
			XorCompositeAssertion xorTerm = new XorCompositeAssertion();
			Iterator hisAndTerms = target.getTerms().iterator();
			Iterator myAndTerms = normalizedMe.getTerms().iterator();
			
			while (myAndTerms.hasNext()) {
				AndCompositeAssertion myAndTerm = (AndCompositeAssertion) myAndTerms.next();
				while (hisAndTerms.hasNext()) {
					AndCompositeAssertion hisAndTerm = (AndCompositeAssertion) hisAndTerms.next();
					xorTerm.addTerm(myAndTerm.merge(hisAndTerm));
				}
			}
			
			xorTerm.setNormalize(true);
			return xorTerm;
			
		} else if (target instanceof AndCompositeAssertion) {
			XorCompositeAssertion xorTerm = new XorCompositeAssertion();
			Iterator myAndTerms = normalizedMe.getTerms().iterator();
			
			while (myAndTerms.hasNext()) {
				AndCompositeAssertion andTerm = new AndCompositeAssertion();
				andTerm.addTerms(target.getTerms());
				AndCompositeAssertion myAndTerm = (AndCompositeAssertion) myAndTerms.next();
				andTerm.addTerms(myAndTerm.getTerms());
				xorTerm.addTerm(andTerm);				
			}
			
			xorTerm.setNormalize(true);
			return xorTerm;			
		}
		
		throw new IllegalArgumentException("error : merge is not defined for" + target.getClass().getName());
	}	
}
