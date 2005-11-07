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

import javax.xml.namespace.QName;

import org.apache.policy.util.PolicyRegistry;

/**
 * PrimitiveAssertion wraps an assertion which is indivisible. Such assertion 
 * require domain specific knowledge for further processing. Hence this class
 * seperates that domain specific knowledge from generic framework.
 * 
 */
public class PrimitiveAssertion implements Assertion {
	private Assertion owner = null;
	private QName qname;
    private Object value;
    
    public PrimitiveAssertion(QName qname, Object value) {
    	this.qname = qname;
        this.value = value;
    }
    
    public QName getName() {
        return qname;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
    	if (assertion instanceof CompositeAssertion) {
    		return assertion.intersect(this, reg);
    		
    	} else { // both are primitives
    		PrimitiveAssertion target = (PrimitiveAssertion) assertion;
    		
    		if(this.getName().equals(target.getName())) {
    			AndCompositeAssertion resultAnd = new AndCompositeAssertion();
        		resultAnd.addTerm(this);
        		resultAnd.addTerm(target);
        		return resultAnd;
    		} 
    		return new XorCompositeAssertion();
    	}
	}
    
    public Assertion intersect(Assertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}
    
	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		
		AndCompositeAssertion resultAnd = new AndCompositeAssertion();
		resultAnd.addTerm(this);
		resultAnd.addTerm(assertion);
		return resultAnd;
	}
	
	public Assertion merge(Assertion assertion) {
		return merge(assertion, null);
	}
	
//	public Assertion normalize() {
//		throw new UnsupportedOperationException("normalize is not supported " +
//				"in primitive assertions");
//	}
	
	public boolean hasParent() {
		return owner != null;
	}
	
	public Assertion getParent() {
		return owner;
	}
	
	public void setParent(Assertion parent) {
		this.owner = parent;
	}
}
