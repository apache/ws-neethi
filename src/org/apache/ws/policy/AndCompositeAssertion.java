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

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.util.PolicyRegistry;

/**
 * AndCompositeAssertion represents either policy or a single policy
 * alternative. It requires that all its terms are satisfied.
 */
public class AndCompositeAssertion extends CompositeAssertion implements
        Assertion {

    private Log log =
        LogFactory.getLog(this.getClass().getName());    

    public AndCompositeAssertion() {
    }

    /**
     * Adds an Assertion to its terms list
     * 
     * @param assertion
     *           Assertion to be added
     */
    public void addTerm(Assertion assertion) {
        if (!(isNormalized() && (assertion instanceof PrimitiveAssertion))) {
            setNormalized(false);
        }
        super.addTerm(assertion);
    }

    /**
     * Returns the intersection of self and argument against a specified Policy
     * Registry.
     * 
     * @param assertion
     *           the assertion to intersect with self
     * @param reg
     *           a sepcified policy registry
     * @return assertion the assertion which is equivalent to intersection
     *         between self and the argument
     */
    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: AndCompositeAssertion::intersect");

        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this
                : normalize(reg));

        if (!(normalizedMe instanceof AndCompositeAssertion)) {
            return normalizedMe.intersect(assertion, reg);
        }

        if (assertion instanceof PrimitiveAssertion) {
            QName qname = ((PrimitiveAssertion) assertion).getName();
            Iterator iterator = getTerms().iterator();
            boolean isMatch = false;

            while (iterator.hasNext()) {
                PrimitiveAssertion primTerm = (PrimitiveAssertion) iterator
                        .next();
                if (primTerm.getName().equals(qname)) {
                    isMatch = true;
                    break;
                }
            }
            return (isMatch) ? normalizedMe : new XorCompositeAssertion();
        }

        CompositeAssertion target = (CompositeAssertion) assertion;
        target = (CompositeAssertion) ((target.isNormalized()) ? target
                : target.normalize(reg));

        if (target instanceof Policy) {
            XorCompositeAssertion alters = (XorCompositeAssertion) target
                    .getTerms().get(0);
            return normalizedMe.intersect(alters);

        } else if (target instanceof XorCompositeAssertion) {
            XorCompositeAssertion result = new XorCompositeAssertion();
            Iterator iterator = target.getTerms().iterator();

            while (iterator.hasNext()) {
                AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
                        .next();
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

        List primTermsA = ((size() > target.size()) ? normalizedMe.getTerms()
                : target.getTerms());
        List primTermsB = ((size() > target.size()) ? target.getTerms()
                : normalizedMe.getTerms());

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
     * @param assertion
     *           the assertion to be merged with
     * @param reg
     *           the policy registry which the is used resolve external policy
     *           references
     * @return assertion the resultant assertion which is equivalent to merge of
     *         self and argument
     */
    public Assertion merge(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: AndCompositeAssertion::merge");
        
        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this
                : normalize(reg));

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
        target = (CompositeAssertion) ((target.isNormalized()) ? target
                : target.normalize(reg));

        if (target instanceof Policy) {
            XorCompositeAssertion xorTerm = (XorCompositeAssertion) target
                    .getTerms().get(0);
            return normalizedMe.merge(xorTerm);

        } else if (target instanceof XorCompositeAssertion) {
            XorCompositeAssertion xorTerm = new XorCompositeAssertion();

            Iterator hisAndTerms = target.getTerms().iterator();
            while (hisAndTerms.hasNext()) {
                AndCompositeAssertion hisAndTerm = (AndCompositeAssertion) hisAndTerms
                        .next();
                xorTerm.addTerm(normalizedMe.merge(hisAndTerm));
            }
            xorTerm.setNormalized(true);
            return xorTerm;

        } else if (target instanceof AndCompositeAssertion) {
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
     * Returns an Assertion which is normalized using a specified policy
     * registry.
     * 
     * @param reg
     *           the policy registry used to resolve policy references
     * @return an Assertion which is the normalized form of self
     */
    public Assertion normalize(PolicyRegistry reg) {
        log.debug("Enter: AndCompositeAssertion::normalize");
        
        AndCompositeAssertion AND = new AndCompositeAssertion();
        XorCompositeAssertion XOR = new XorCompositeAssertion();

        ArrayList andTerms = new ArrayList();
        ArrayList xorTerms = new ArrayList();

        if (isEmpty()) {
            AND.setNormalized(true);
            return AND;
        }

        Iterator terms = getTerms().iterator();

        while (terms.hasNext()) {
            Assertion term = (Assertion) terms.next();
            term = (term instanceof Policy) ? term : term.normalize(reg);

            if (term instanceof Policy) {
                Assertion wrapper = new AndCompositeAssertion();
                ((AndCompositeAssertion) wrapper).addTerms(((Policy) term)
                        .getTerms());
                term = wrapper.normalize(reg);
            }

            if (term instanceof XorCompositeAssertion) {
                
                if (((XorCompositeAssertion) term).isEmpty()) {

                    /*  */
                    XorCompositeAssertion anXorTerm = new XorCompositeAssertion();
                    anXorTerm.setNormalized(true);
                    return anXorTerm;
                }
                xorTerms.add(term);
                continue;

            }
            
            if (term instanceof AndCompositeAssertion) {
                AND.addTerms(((AndCompositeAssertion) term).getTerms());
                continue;
            }
            
            AND.addTerm(term);
        }

        // processing child-XORCompositeAssertions
        if (xorTerms.size() > 1) {

            XorCompositeAssertion xorTermA, xorTermB;

            for (int i = 0; i < xorTerms.size(); i++) {

                for (int j = i; j < xorTerms.size(); j++) {

                    if (i != j) {
                        xorTermA = (XorCompositeAssertion) xorTerms.get(i);
                        xorTermB = (XorCompositeAssertion) xorTerms.get(j);

                        Iterator interatorA = xorTermA.getTerms().iterator();

                        CompositeAssertion andTermA;
                        Iterator iteratorB;

                        while (interatorA.hasNext()) {
                            andTermA = (CompositeAssertion) interatorA.next();
                            iteratorB = xorTermB.getTerms().iterator();

                            CompositeAssertion andTermB;
                            AndCompositeAssertion anAndTerm;

                            while (iteratorB.hasNext()) {

                                andTermB = (CompositeAssertion) iteratorB
                                        .next();
                                anAndTerm = new AndCompositeAssertion();
                                anAndTerm.addTerms(andTermA.getTerms());
                                anAndTerm.addTerms(andTermB.getTerms());
                                XOR.addTerm(anAndTerm);
                            }
                        }

                    }
                }
            }

        } else if (xorTerms.size() == 1) {
            CompositeAssertion XORterm = (CompositeAssertion) xorTerms.get(0);
            XOR.addTerms(XORterm.getTerms());
        }
        
        if (XOR.isEmpty()) {
        	AND.setNormalized(true);
        	return AND;
        }

        if (AND.isEmpty()) {
            XOR.setNormalized(true);
            return XOR;
        }
        

        List primTerms = AND.getTerms();
        Iterator interator = XOR.getTerms().iterator();

        while (interator.hasNext()) {
            CompositeAssertion andTerm = (CompositeAssertion) interator.next();
            andTerm.addTerms(primTerms);
        }

        XOR.setNormalized(true);
        return XOR;
    }
}
