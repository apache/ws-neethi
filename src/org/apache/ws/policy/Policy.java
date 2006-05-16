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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.util.PolicyRegistry;

/**
 * Policy class is the runtime representation of a policy. It provides a
 * convenient model to store process any policy. Policy object requires that all
 * its terms are met.
 */
public class Policy extends AbstractAssertion implements CompositeAssertion {
    private Log log = LogFactory.getLog(this.getClass().getName());

    private Hashtable attributes = new Hashtable();

    /**
     * Creates a policy object
     */
    public Policy() {
    }

    /**
     * Creates a policy object with the specified Id
     * 
     * @param id
     *            a string as the id
     */
    public Policy(String id) {
        this(null, id);
        setNormalized(false);
    }

    /**
     * Creates a policy object with the specified xml-base and id.
     * 
     * @param xmlBase
     *            the xml-base
     * @param id
     *            a string as the id
     */
    public Policy(String xmlBase, String id) {
        setBase(xmlBase);
        setId(id);
        setNormalized(false);
    }

    /**
     * Set the xml-base of the policy object
     * 
     * @param xmlBase
     *            the xml base of the policy object
     */
    public void setBase(String xmlBase) {
        addAttribute(new QName(PolicyConstants.XML_NAMESPACE_URI,
                PolicyConstants.POLICY_BASE), xmlBase);
    }

    /**
     * Returns the xml-base of the policy object. Returns null if no xml-base is
     * set.
     * 
     * @return xml base of the policy object
     */
    public String getBase() {
        return (String) getAttribute(new QName(
                PolicyConstants.XML_NAMESPACE_URI,
                PolicyConstants.POLICY_BASE));
    }

    /**
     * Sets the id of the Policy object
     * 
     * @param id
     */
    public void setId(String id) {
        addAttribute(new QName(PolicyConstants.WSU_NAMESPACE_URI,
                PolicyConstants.POLICY_ID), id);
    }

    /**
     * Returns the Id of the Policy object. Returns null if no Id is set.
     * 
     * @return the Id of the policy object.
     */
    public String getId() {
        return (String) getAttribute(new QName(
                PolicyConstants.WSU_NAMESPACE_URI,
                PolicyConstants.POLICY_ID));
    }

    /**
     * Sets the Name of the Policy object
     * 
     * @param name
     */
    public void setName(String name) {
        addAttribute(new QName("", PolicyConstants.POLICY_NAME), name);
    }

    /**
     * Returns the Name of the Policy object. Returns null if no Name is set.
     * 
     * @return the Name of the policy object.
     */
    public String getName() {
        return (String) getAttribute(new QName("",
                PolicyConstants.POLICY_NAME));
    }

    /**
     * 
     * /** Returns a String which uniquely identify the policy object. It has
     * the format of {$xmlBase}#{$id}. If the xmlBase is null it will return
     * #{$id} as the URI String. If the Id is null, this will return.
     * 
     * @return a String which uniquely identify the policy object.
     */
    public String getPolicyURI() {
        if (getId() != null) {
            if (getBase() != null) {
                return getBase() + "#" + getId();
            }
            return "#" + getId();
        }
        return null;
    }

    public Assertion normalize() {
        return normalize(null);
    }

    public Assertion normalize(PolicyRegistry reg) {
        log.debug("Enter: Policy::normalize");

        if (isNormalized()) {
            return this;
        }

        String xmlBase = getBase();
        String id = getId();
        Policy policy = new Policy(xmlBase, id);

        All all = new All();
        ExactlyOne exactlyOne = new ExactlyOne();

        ArrayList childAndTermList = new ArrayList();
        ArrayList childXorTermList = new ArrayList();

        Iterator terms = getTerms().iterator();
        Assertion term;

        while (terms.hasNext()) {
            term = (Assertion) terms.next();
            term = term.normalize(reg);

            if (term instanceof Policy) {
                ExactlyOne anExactlyOne = (ExactlyOne) ((Policy) term)
                        .getTerms().get(0);

                if (anExactlyOne.size() != 1) {
                    term = anExactlyOne;

                } else {
                    all
                            .addTerms(((All) anExactlyOne.getTerms()
                                    .get(0)).getTerms());
                    continue;
                }
            }

            if (term instanceof ExactlyOne) {

                if (((ExactlyOne) term).isEmpty()) {
                    ExactlyOne tmpExactlyOne = new ExactlyOne();
                    tmpExactlyOne.setNormalized(true);

                    policy.addTerm(tmpExactlyOne);
                    policy.setNormalized(true);

                    return policy;
                }

                childXorTermList.add(term);
                continue;
            }

            if (term instanceof All) {

                if (((All) term).isEmpty()) {
                    All emptyAnd = new All();
                    exactlyOne.addTerm(emptyAnd);

                } else {
                    all.addTerms(((All) term).getTerms());
                }
                continue;
            }
            all.addTerm((Assertion) term);
        }

        // processing child-XORCompositeAssertions
        if (childXorTermList.size() > 1) {

            exactlyOne.addTerms(Policy.crossProduct(childXorTermList, 0));

        } else if (childXorTermList.size() == 1) {
            Assertion tmpExactlyOne = (Assertion) childXorTermList.get(0);
            exactlyOne.addTerms(tmpExactlyOne.getTerms());
        }

        if (childXorTermList.isEmpty()) {
            ExactlyOne tmpExactlyOne = new ExactlyOne();

            tmpExactlyOne.addTerm(all);
            policy.addTerm(tmpExactlyOne);
            policy.setNormalized(true);
            return policy;
        }

        List primTerms = all.getTerms();
        Iterator alls = exactlyOne.getTerms().iterator();

        while (alls.hasNext()) {
            Assertion anAndTerm = (Assertion) alls.next();
            anAndTerm.addTerms(primTerms);
        }

        policy.addTerm(exactlyOne);
        policy.setNormalized(true);
        return policy;
    }

    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: Policy::intersect");

        Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);
        if (!(normalizedMe instanceof Policy)) {
            return normalizedMe.intersect(assertion, reg);
        }

        Assertion target = (assertion.isNormalized()) ? assertion : assertion
                .normalize(reg);
        short type = target.getType();

        switch (type) {
        case Assertion.POLICY: {
            Policy newPolicy = new Policy();
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).intersect((ExactlyOne) target
                    .getTerms().get(0)));
            return newPolicy;
        }
        case Assertion.EXACTLY_ONE: {
            Policy newPolicy = new Policy();
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return newPolicy;
        }
        case Assertion.ALL: {
            Policy newPolicy = new Policy();
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return newPolicy;
        }
        case Assertion.PRIMITIVE: {
            Policy newPolicy = new Policy();
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return newPolicy;
        }

        default: {
            throw new IllegalArgumentException("intersect is not defined for "
                    + target.getClass().getName() + " type");
        }

        }
    }

    public Assertion merge(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: Policy::merge");

        Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

        if (!(normalizedMe instanceof Policy)) {
            return normalizedMe.merge(assertion, reg);
        }

        Policy newPolicy = new Policy();

        Assertion target = (assertion.isNormalized()) ? assertion : assertion
                .normalize(reg);
        short type = target.getType();

        switch (type) {

        case Assertion.POLICY: {

            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).merge((ExactlyOne) target.getTerms()
                    .get(0)));
            return newPolicy;
        }
        case Assertion.EXACTLY_ONE: {
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return newPolicy;
        }

        case Assertion.ALL: {
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return newPolicy;
        }

        case Assertion.PRIMITIVE: {
            newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return newPolicy;
        }

        default: {
            throw new IllegalArgumentException(" merge for "
                    + target.getClass().getName() + " not defined");
        }

        }
    }

    /**
     * Returns a short value which indicates this is a Policy.
     */
    public final short getType() {
        return Assertion.POLICY;
    }

    /**
     * Replaces all the attributes for this Policy from a single Hashtable.
     * 
     * @param attributes
     *            A Hashtable containing the attributes for this Policy as
     *            name/value pairs.
     */
    public void setAttributes(Hashtable attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns all of the attributes for this Policy as a Hashtable.
     * 
     * @return Hashtable containing the attributes for this Policy as name/value
     *         pairs.
     */
    public Hashtable getAttributes() {
        return attributes;
    }

    /**
     * Adds an attribute to the Policy.
     * 
     * @param qname
     *            The QName of the attribute.
     * @param value
     *            The value of attribute expressed as a String.
     */
    public void addAttribute(QName qname, String value) {
        if (value != null) {
            attributes.put(qname, value);
        }
    }

    /**
     * Returns a specified attribute value.
     * 
     * @param qname
     *            The QName of the attribute.
     * @return String The value of the attribute.
     */
    public String getAttribute(QName qname) {
        return (String) attributes.get(qname);
    }

    /**
     * Removes a specified attribute from the Policy.
     * 
     * @param qname
     *            The QName of the attribute.
     */
    public void removeAttribute(QName qname) {
        attributes.remove(qname);
    }

    /**
     * Clears all attributes from the Policy.
     */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * @param allTerms
     *            ExactlyOne to be corssproducted
     * @param index
     *            starting point of cross product
     * @return
     */
    protected static ArrayList crossProduct(ArrayList allTerms, int index) {

        ArrayList result = new ArrayList();
        ExactlyOne firstTerm = (ExactlyOne) allTerms
                .get(index);
        ArrayList restTerms;

        if (allTerms.size() == ++index) {
            restTerms = new ArrayList();
            All newTerm = new All();
            restTerms.add(newTerm);
        } else
            restTerms = crossProduct(allTerms, index);

        Iterator firstTermIter = firstTerm.getTerms().iterator();
        while (firstTermIter.hasNext()) {
            Assertion assertion = (Assertion) firstTermIter.next();
            Iterator restTermsItr = restTerms.iterator();
            while (restTermsItr.hasNext()) {
                Assertion restTerm = (Assertion) restTermsItr.next();
                All newTerm = new All();
                newTerm.addTerms(assertion.getTerms());
                newTerm.addTerms(restTerm.getTerms());
                result.add(newTerm);
            }
        }

        return result;
    }

    /**
     * Returns an Iterator to track the Alternatives within this Policy. This
     * iterator will again return an iterator which points to the set of
     * primitives in an alternative.
     * 
     * @return
     */
    public Iterator iterator() {
        return new PolicyIterator(this);
    }

    private class PolicyIterator implements java.util.Iterator {

        private ExactlyOne exactlyOne = null;

        private int currentIndex = 0;

        private PolicyIterator(Policy policy) {
            if (!policy.isNormalized()) {
                policy = (Policy) policy.normalize();
            }

            exactlyOne = (ExactlyOne) policy.getTerms().get(0);
        }

        public boolean hasNext() {
            return exactlyOne.size() > currentIndex;
        }

        public Object next() {
            All all = (All) exactlyOne.getTerms()
                    .get(currentIndex);
            currentIndex++;
            return all.getTerms();
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "currently not supported .. ");

        }
    }

}