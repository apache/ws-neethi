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
                PolicyConstants.WS_POLICY_BASE), xmlBase);
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
                PolicyConstants.WS_POLICY_BASE));
    }

    /**
     * Sets the id of the Policy object
     * 
     * @param id
     */
    public void setId(String id) {
        addAttribute(new QName(PolicyConstants.WS_POLICY_NAMESPACE_URI,
                PolicyConstants.WS_POLICY_ID), id);
    }

    /**
     * Returns the Id of the Policy object. Returns null if no Id is set.
     * 
     * @return the Id of the policy object.
     */
    public String getId() {
        return (String) getAttribute(new QName(
                PolicyConstants.WS_POLICY_NAMESPACE_URI,
                PolicyConstants.WS_POLICY_ID));
    }

    /**
         * Sets the Name of the Policy object
         * 
         * @param name
         */
        public void setName(String name) {
            addAttribute(new QName("", PolicyConstants.WS_POLICY_NAME), name);
        }
    
        /**
         * Returns the Name of the Policy object. Returns null if no Name is set.
         * 
         * @return the Name of the policy object.
         */
        public String getName() {
            return (String) getAttribute(new QName(
                    "", PolicyConstants.WS_POLICY_NAME));
        }
        
        /**
    
    /**
     * Returns a String which uniquely identify the policy object. It has the
     * format of {$xmlBase}#{$id}. If the xmlBase is null it will return #{$id}
     * as the URI String. If the Id is null, this will return.
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

        AndCompositeAssertion AND = new AndCompositeAssertion();
        XorCompositeAssertion XOR = new XorCompositeAssertion();

        ArrayList childAndTermList = new ArrayList();
        ArrayList childXorTermList = new ArrayList();

        Iterator terms = getTerms().iterator();
        Assertion term;

        while (terms.hasNext()) {
            term = (Assertion) terms.next();
            term = term.normalize(reg);

            if (term instanceof Policy) {
                XorCompositeAssertion Xor = (XorCompositeAssertion) ((Policy) term)
                        .getTerms().get(0);

                if (Xor.size() != 1) {
                    term = Xor;

                } else {
                    AND
                            .addTerms(((AndCompositeAssertion) Xor.getTerms()
                                    .get(0)).getTerms());
                    continue;
                }
            }

            if (term instanceof XorCompositeAssertion) {

                if (((XorCompositeAssertion) term).isEmpty()) {
                    XorCompositeAssertion emptyXor = new XorCompositeAssertion();
                    emptyXor.setNormalized(true);

                    policy.addTerm(emptyXor);
                    policy.setNormalized(true);

                    return policy;
                }

                childXorTermList.add(term);
                continue;
            }

            if (term instanceof AndCompositeAssertion) {

                if (((AndCompositeAssertion) term).isEmpty()) {
                    AndCompositeAssertion emptyAnd = new AndCompositeAssertion();
                    XOR.addTerm(emptyAnd);

                } else {
                    AND.addTerms(((AndCompositeAssertion) term).getTerms());
                }
                continue;
            }
            AND.addTerm((Assertion) term);
        }

        // processing child-XORCompositeAssertions
        if (childXorTermList.size() > 1) {

            XOR.addTerms(Policy.crossProduct(childXorTermList, 0));

        } else if (childXorTermList.size() == 1) {
            Assertion xorTerm = (Assertion) childXorTermList.get(0);
            XOR.addTerms(xorTerm.getTerms());
        }

        if (childXorTermList.isEmpty()) {
            XorCompositeAssertion xor = new XorCompositeAssertion();

            xor.addTerm(AND);
            policy.addTerm(xor);
            policy.setNormalized(true);
            return policy;
        }

        List primTerms = AND.getTerms();
        Iterator andTerms = XOR.getTerms().iterator();

        while (andTerms.hasNext()) {
            Assertion anAndTerm = (Assertion) andTerms.next();
            anAndTerm.addTerms(primTerms);
        }

        policy.addTerm(XOR);
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
        case Assertion.COMPOSITE_POLICY_TYPE: {
            Policy nPOLICY = new Policy();
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).intersect((XorCompositeAssertion) target
                    .getTerms().get(0)));
            return nPOLICY;
        }
        case Assertion.COMPOSITE_XOR_TYPE: {
            Policy nPOLICY = new Policy();
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return nPOLICY;
        }
        case Assertion.COMPOSITE_AND_TYPE: {
            Policy nPOLICY = new Policy();
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return nPOLICY;
        }
        case Assertion.PRIMITIVE_TYPE: {
            Policy nPOLICY = new Policy();
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).intersect(target));
            return nPOLICY;
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

        Policy nPOLICY = new Policy();

        Assertion target = (assertion.isNormalized()) ? assertion : assertion
                .normalize(reg);
        short type = target.getType();

        switch (type) {

        case Assertion.COMPOSITE_POLICY_TYPE: {

            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).merge((XorCompositeAssertion) target.getTerms()
                    .get(0)));
            return nPOLICY;
        }
        case Assertion.COMPOSITE_XOR_TYPE: {
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return nPOLICY;
        }

        case Assertion.COMPOSITE_AND_TYPE: {
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return nPOLICY;
        }

        case Assertion.PRIMITIVE_TYPE: {
            nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
                    .get(0)).merge(target));
            return nPOLICY;
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
        return Assertion.COMPOSITE_POLICY_TYPE;
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
     *            XorCompositeAssertion to be corssproducted
     * @param index
     *            starting point of cross product
     * @return
     */
    protected static ArrayList crossProduct(ArrayList allTerms, int index) {

        ArrayList result = new ArrayList();
        XorCompositeAssertion firstTerm = (XorCompositeAssertion) allTerms
                .get(index);
        ArrayList restTerms;

        if (allTerms.size() == ++index) {
            restTerms = new ArrayList();
            AndCompositeAssertion newTerm = new AndCompositeAssertion();
            restTerms.add(newTerm);
        } else
            restTerms = crossProduct(allTerms, index);

        Iterator firstTermIter = firstTerm.getTerms().iterator();
        while (firstTermIter.hasNext()) {
            Assertion assertion = (Assertion) firstTermIter.next();
            Iterator restTermsItr = restTerms.iterator();
            while (restTermsItr.hasNext()) {
                Assertion restTerm = (Assertion) restTermsItr.next();
                AndCompositeAssertion newTerm = new AndCompositeAssertion();
                newTerm.addTerms(assertion.getTerms());
                newTerm.addTerms(restTerm.getTerms());
                result.add(newTerm);
            }
        }

        return result;
    }

}