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
 * AbstractAssertion provides the default implementation of some basic functions
 * definded in Assertion interface.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public abstract class AbstractAssertion implements Assertion {

	/** Flag to mark whether this assertion is in normalize format */
	protected boolean flag = false;

	/** To store terms of this assertion */
	protected ArrayList childTerms = new ArrayList();

	/** To refer to the parent of this assertion */
	protected Assertion parent = null;
    
    /** To store the description of the origin of the assertion */
    protected String source = null;
    
    /** To store the line no of the assertion in the file */
    protected int lineNo = -1;

	/**
	 * 
	 * Default implementation of normalize() operation.
	 * 
	 * @return an assertion which is the normalized format of this assertion.
	 */
	public Assertion normalize() throws UnsupportedOperationException {
		return normalize(null);
	}

	/**
	 * Default implementation of intersect(Assertion) operation.
	 * 
	 * @param assertion
	 *            the assertion to intersect with this object.
	 * @return an assertion which is equivalent to the intersect of this
	 *         assertion and the argument.
	 *  
	 */
	public Assertion intersect(Assertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}

	/**
	 * Default implementation of merge(Assertion) operation.
	 * 
	 * @param assertion
	 *            the assertion to merge with this object.
	 * @return an assertion which is equivalent to the merge of this assertion
	 *         and the argument.
	 */
	public Assertion merge(Assertion assertion)
			throws UnsupportedOperationException {
		return merge(assertion, null);
	}

	/**
	 * Returns true if this assertion is in normalzied format.
	 * 
	 * @return true if this assertion is in normalized format.
	 */
	public boolean isNormalized() {
		return flag;
	}

	/**
	 * Marks this assertion as it is in normalized format.
	 * 
	 * @param flag
	 *            which marks this object as in its normalized format or not.
	 */
	public void setNormalized(boolean flag) {
		this.flag = flag;

		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();) {
			((Assertion) iterator.next()).setNormalized(flag);
		}
	}

	/**
	 * Returns true if this assertion object has a parent.
	 * 
	 * @return returns true if this has a parent.
	 */
	public boolean hasParent() {
		return (parent != null);
	}

	/**
	 * Returns the parent of this assertion.
	 * 
	 * @return parent of this assertion.
	 */
	public Assertion getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this assertion.
	 * 
	 * @param parent
	 *            of this assertion.
	 */
	public void setParent(Assertion parent) {
		this.parent = parent;
	}

	/**
	 * Adds an assertion as a term of this assertion.
	 * 
	 * @param assertion
	 *            the term to add
	 */
	public void addTerm(Assertion assertion) {
		childTerms.add(assertion);
	}

	/**
	 * Adds a list of assertions as terms of this assertion.
	 * 
	 * @param assertions
	 *            list of terms to add
	 */
	public void addTerms(List assertions) {
		childTerms.addAll(assertions);
	}

	/**
	 * Returns the list of terms of this assertion.
	 * 
	 * @param a
	 *            list of terms of this assertion.
	 */
	public List getTerms() {
		return childTerms;
	}

	/**
	 * Returns true if this assertion has no terms.
	 * 
	 * @return if this assertion has no terms.
	 *  
	 */
	public boolean isEmpty() {
		return childTerms.isEmpty();
	}

	/**
	 * Remove the geven term from the set of terms of this assertion.
	 * 
	 * @param returns
	 *            true if the assertion is removed.
	 */
	public boolean remove(Assertion assertion) {
		return childTerms.remove(assertion);
	}

	/**
	 * Returns the number of terms this assertion has.
	 * 
	 * @return the number of terms
	 */
	public int size() {
		return childTerms.size();
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