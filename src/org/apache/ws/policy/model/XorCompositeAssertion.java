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

package org.apache.ws.policy.model;

import java.util.Iterator;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.ws.policy.util.PolicyRegistry;

/**
 * XORCompositeAssertion represents a bunch of policy alternatives. It requires
 * that exactly one of its terms (policy alternative) is statisfied.
 *  
 */
public class XorCompositeAssertion extends CompositeAssertion implements
        Assertion {
    
    private Log log =
        LogFactory.getLog(this.getClass().getName());

    public XorCompositeAssertion() {
    }

    public void addTerm(Assertion assertion) {
        if (!(isNormalized() && (assertion instanceof AndCompositeAssertion) && ((AndCompositeAssertion) assertion)
                .isNormalized())) {
            setNormalized(false);
        }
        super.addTerm(assertion);
    }

    public Assertion normalize(PolicyRegistry reg) {
        log.debug("Enter: XorCompositeAssertion::normalize");
        
        XorCompositeAssertion XOR = new XorCompositeAssertion();
        
        if (isEmpty()) {
            XOR.setNormalized(true);
            return XOR;
        }
        
        Iterator terms = getTerms().iterator();
       
        while (terms.hasNext()) {
            Assertion term = (Assertion) terms.next();
            term = (term instanceof Policy) ? term : term.normalize(reg);

            if (term instanceof Policy) {
                Assertion wrapper = new AndCompositeAssertion();
                ((AndCompositeAssertion) wrapper).addTerms(((Policy) term)
                        .getTerms());
                wrapper = wrapper.normalize(reg);

                if (wrapper instanceof AndCompositeAssertion) {
                    XOR.addTerm(wrapper);

                } else {
                    XOR.addTerms(((XorCompositeAssertion) wrapper).getTerms());
                }
                continue;
            }

            if (term instanceof PrimitiveAssertion) {
                AndCompositeAssertion wrapper = new AndCompositeAssertion();
                wrapper.addTerm(term);
                XOR.addTerm(wrapper);
                continue;
            }

            if (term instanceof XorCompositeAssertion) {
                XOR.addTerms(((XorCompositeAssertion) term).getTerms());
                continue;
            }

            if (term instanceof AndCompositeAssertion) {
                XOR.addTerm(term);
            }
        }

        XOR.setNormalized(true);
        return XOR;
    }

    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: XorCompositeAssertion::intersect");
        
        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this
                : normalize(reg));

        if (!(normalizedMe instanceof XorCompositeAssertion)) {
            return normalizedMe.intersect(assertion, reg);
        }

        XorCompositeAssertion result = new XorCompositeAssertion();

        if (assertion instanceof PrimitiveAssertion) {

            Iterator iterator = normalizedMe.getTerms().iterator();

            while (iterator.hasNext()) {
                AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
                        .next();
                Assertion value = andTerm.intersect(assertion);
                if (value instanceof AndCompositeAssertion) {
                    result.addTerm(value);
                }
            }

        } else {
            CompositeAssertion target = (CompositeAssertion) assertion;
            target = (CompositeAssertion) ((target.isNormalized()) ? target
                    : target.normalize(reg));

            Iterator iterator = normalizedMe.getTerms().iterator();
            while (iterator.hasNext()) {
                AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
                        .next();

                if (target instanceof AndCompositeAssertion) {
                    Assertion value = andTerm.intersect(target);

                    if (value instanceof AndCompositeAssertion) {
                        result.addTerm(value);
                    }

                } else if (target instanceof XorCompositeAssertion) {

                    Iterator andTerms = target.getTerms().iterator();

                    while (andTerms.hasNext()) {
                        AndCompositeAssertion tAndTerm = (AndCompositeAssertion) andTerms
                                .next();
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
        log.debug("Enter: XorCompositeAssertion::merge");
        
        CompositeAssertion normalizedMe = (CompositeAssertion) ((isNormalized()) ? this
                : normalize(reg));

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
                    AndCompositeAssertion anAndTerm = (AndCompositeAssertion) iterator
                            .next();
                    andTerm.addTerms(anAndTerm.getTerms());
                    xorTerm.addTerm(andTerm);
                } while (iterator.hasNext());
            } else {
                AndCompositeAssertion andTerm = new AndCompositeAssertion();
                andTerm.addTerm(assertion);
                xorTerm.addTerm(andTerm);
            }
            xorTerm.setNormalized(true);
            return xorTerm;
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
            Iterator myAndTerms = normalizedMe.getTerms().iterator();

            while (myAndTerms.hasNext()) {
                AndCompositeAssertion myAndTerm = (AndCompositeAssertion) myAndTerms
                        .next();
                while (hisAndTerms.hasNext()) {
                    AndCompositeAssertion hisAndTerm = (AndCompositeAssertion) hisAndTerms
                            .next();
                    xorTerm.addTerm(myAndTerm.merge(hisAndTerm));
                }
            }

            xorTerm.setNormalized(true);
            return xorTerm;

        } else if (target instanceof AndCompositeAssertion) {
            XorCompositeAssertion xorTerm = new XorCompositeAssertion();
            Iterator myAndTerms = normalizedMe.getTerms().iterator();

            while (myAndTerms.hasNext()) {
                AndCompositeAssertion andTerm = new AndCompositeAssertion();
                andTerm.addTerms(target.getTerms());
                AndCompositeAssertion myAndTerm = (AndCompositeAssertion) myAndTerms
                        .next();
                andTerm.addTerms(myAndTerm.getTerms());
                xorTerm.addTerm(andTerm);
            }

            xorTerm.setNormalized(true);
            return xorTerm;
        }

        throw new IllegalArgumentException("error : merge is not defined for"
                + target.getClass().getName());
    }
}
