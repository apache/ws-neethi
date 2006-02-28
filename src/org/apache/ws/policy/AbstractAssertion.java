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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public abstract class AbstractAssertion implements Assertion {
	
	/** */
	protected boolean flag = false;
	
	/** */
	protected ArrayList childTerms = new ArrayList();
	
	/** */
	protected Assertion parent = null;
	
	/**
	 * 
	 */
	public Assertion normalize() throws UnsupportedOperationException {
		return normalize(null);
	}
	
	/**
	 * 
	 */
	public Assertion intersect(Assertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}
	
	/**
	 * 
	 */
	public Assertion merge(Assertion assertion)
			throws UnsupportedOperationException {
		return merge(assertion, null);
	}
	
	/**
	 * 
	 */	
	public boolean isNormalized() {
		return flag;
	}

	/**
	 * 
	 */
	public void setNormalized(boolean flag) {
		this.flag = flag;
		
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();) {
			((Assertion) iterator.next()).setNormalized(flag);			
		}
	}
	
	/**
	 * 
	 */
	public boolean hasParent() {
		return (parent != null);
	}

	/**
	 * 
	 */
	public Assertion getParent() {
		return parent;
	}

	public void setParent(Assertion parent) {
		this.parent = parent;
	}

	public void addTerm(Assertion assertion) {
		childTerms.add(assertion);
	}
	
	public void addTerms(List assertions) {
		childTerms.addAll(assertions);
	}

	public List getTerms() {
		return childTerms;
	}

	public boolean isEmpty() {
		return childTerms.isEmpty();
	}
	
	public boolean remove(Assertion assertion) {
		return childTerms.remove(assertion);
	}

	public int size() {
		return childTerms.size();
	}
}
