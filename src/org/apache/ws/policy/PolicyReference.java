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

package org.apache.ws.policy;

import java.util.List;

import org.apache.ws.policy.util.PolicyRegistry;

/**
 * PolicyReference class has implicit reference to a external policy. It is used
 * as a way to include an extenal policy object within another policy object.
 * These objects are replaced with the actual policies when a Policy object is
 * being normalized.
 * 
 * Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyReference implements Assertion {

	/** A string which uniquely identify the referring Policy object */
	private String PolicyURIString = null;

	private Assertion parent = null;
    
    /** To store the description of the origin of the assertion */
    protected String source = null;
    
    /** To store the line no of the assertion in the file */
    protected int lineNo = -1;

	/**
	 * Constructs a PolicyReferece object which refers to the Policy which is
	 * uniquely identified by policyURIString.
	 * 
	 * @param policyURIString
	 */
	public PolicyReference(String policyURIString) {
		this.PolicyURIString = policyURIString;
	}

	/**
	 * Returns a string which uniquely identified the referring Policy. For
	 * instance the referring policy object p, p.getPolicyURI() should return
	 * the same string
	 * 
	 * @return a String which uniquely identifies the referring Policy object.
	 */
	public String getPolicyURIString() {
		return PolicyURIString;
	}

	public Assertion normalize() {
		throw new UnsupportedOperationException();
	}

	public Assertion normalize(PolicyRegistry reg) {
		if (reg == null) {
			throw new RuntimeException("Cannot resolve : "
					+ getPolicyURIString() + " .. PolicyRegistry is null");
		}
        
        String key = getPolicyURIString();
        
        if (key.startsWith("#")) {
            key = key.substring(key.indexOf('#') + 1);            
        }
        
		Policy targetPolicy = reg.lookup(key);
        
		if (targetPolicy == null) {
			throw new RuntimeException("error : " + getPolicyURIString()
					+ " doesn't resolve to any known policy");
		}

		return targetPolicy.normalize(reg);
	}

	public Assertion intersect(Assertion assertion) {
		throw new UnsupportedOperationException();
	}

	public Assertion intersect(Assertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Assertion merge(Assertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Assertion merge(Assertion assertion) {
		throw new UnsupportedOperationException();
	}

	public boolean hasParent() {
		return parent != null;
	}

	public Assertion getParent() {
		return parent;
	}

	public void setParent(Assertion parent) {
		this.parent = parent;
	}

	public boolean isNormalized() {
		return false;
	}

	public void setNormalized(boolean flag) {
		throw new UnsupportedOperationException();
	}

	public void addTerm(Assertion assertion) {
		throw new UnsupportedOperationException();
	}

	public void addTerms(List assertions) {
		throw new UnsupportedOperationException();
	}

	public List getTerms() {
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Assertion assertion) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a short value which indicate this is a PolicyReference.
	 * 
	 * @return a short value to indicate that this is a PolicyReference.
	 */
	public final short getType() {
		return Assertion.POLICY_REF;
	}    
    
    public void setSource(String source) {
        this.source = source;        
    }
    
    public String getSource() {
        return source;
    }
    
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }
    
    public int getLineNo() {
        return lineNo;
    }
}