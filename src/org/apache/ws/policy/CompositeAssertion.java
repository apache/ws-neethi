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
 * CompositeAssertion abstract class implements few method which are common to
 * composite assertions. A composite assertion as some terms (if any) and 
 * implicit logic that whether all (or any) of its terms should be statisfied.
 */
public abstract class CompositeAssertion implements Assertion {
    
    /** */
    protected boolean flag = true;
    
    /** */
    private List list = new ArrayList();
    
    /** */
    private Assertion parent = null;
    
    /**
     * Adds an assertion as one of its terms
     * 
     * @param assertion the assertion that should be added as its term
     */
    public void addTerm(Assertion assertion){
        assertion.setParent(this);
        list.add(assertion);
    }
    
    /**
     * Adds set of assertions as its terms
     * 
     * @param assertions the set of assertions that should be added as its 
     *        terms
     */
    public void addTerms(List assertions) {
        if (assertions.isEmpty()) {
            return;
        }
        
        Iterator items = assertions.iterator();
        while (items.hasNext()) {
            Object value = items.next();
        
            if (!(value instanceof Assertion)) {
                throw new IllegalArgumentException("argument contains a " +
                        "non-assertion");
            }
            addTerm((Assertion) value);
        }
    }
    
    public List getTerms() {
        return list;
    }
    
    /**
     * Returns true if no terms exist or false otherwise
     * @return true if no terms exist or false otherwise
     */
    public boolean isEmpty() {
        return list.size() == 0;
    }
    
    public boolean remove(Assertion assertion) {
        return list.remove(assertion);
    }
    
    public int size() {
        return list.size();
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

    public Assertion normalize() {
        return normalize(null);
    }
        
    public Assertion intersect(Assertion assertion)
            throws UnsupportedOperationException {
        return intersect(assertion, null);
    }
    
    public Assertion merge(Assertion assertion)
            throws UnsupportedOperationException {
        return merge(assertion, null);
    }
    
    public boolean isNormalized() {
        return flag;
    }
    
    public void setNormalized(boolean value) {
        Iterator children = getTerms().iterator();
        
        while (children.hasNext()) {
            Object child = children.next();
            if (child instanceof CompositeAssertion) {
                ((CompositeAssertion) child).setNormalized(true);
            }
        }
        flag = value;
    }
}
