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

import java.util.Iterator;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.apache.ws.policy.util.PolicyRegistry;

/**
 * XORCompositeAssertion represents a bunch of policy alternatives. It requires
 * that exactly one of its terms (policy alternative) is statisfied.
 *  
 */
public class XorCompositeAssertion extends AbstractAssertion implements
		CompositeAssertion {

	private Log log = LogFactory.getLog(this.getClass().getName());

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

		if (isNormalized()) {
			return this;
		}

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

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof XorCompositeAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);
		short type = target.getType();

		switch (type) {
		
		case Assertion.COMPOSITE_POLICY_TYPE: {
			Policy nPOLICY = new Policy();
			nPOLICY.addTerm(((XorCompositeAssertion) normalizedMe.getTerms()
					.get(0)).intersect(target));
			return nPOLICY;
		}
		
		case Assertion.COMPOSITE_XOR_TYPE: {
			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			Assertion asser;
			AndCompositeAssertion AND;
			
			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator.hasNext(); ) {
				AND = (AndCompositeAssertion) iterator.next();
				
				for (Iterator iterator2 = target.getTerms().iterator(); iterator2.hasNext(); ) {
					asser = AND.intersect((AndCompositeAssertion) iterator2.next());
					
					if (asser instanceof AndCompositeAssertion) {
						nXOR.addTerm(asser);
					}
				}
			}
			
			return nXOR;
		}
		
		case Assertion.COMPOSITE_AND_TYPE : {
			XorCompositeAssertion nXOR = new XorCompositeAssertion();
			Assertion asser;
			
			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator.hasNext();) {
				asser = ((AndCompositeAssertion) iterator.next()).intersect(target);
				
				if (asser instanceof AndCompositeAssertion) {
					nXOR.addTerm(asser);
				}
			}
			return nXOR;
		}
		
		case Assertion.PRIMITIVE_TYPE: {
			XorCompositeAssertion nXOR = new XorCompositeAssertion();
			
			Assertion asser;
			
			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator.hasNext(); ) {
				asser = ((AndCompositeAssertion) iterator.next()).intersect(target);
				
				if (asser instanceof AndCompositeAssertion) {
					nXOR.addTerm(asser);
				}
			}
			return nXOR;
		}
		
		default: {
			throw new IllegalArgumentException("intersect for assertion type " + target.getClass().getName() + " not defined");
		}
		
		}
		
		
//
//		XorCompositeAssertion result = new XorCompositeAssertion();
//
//		if (assertion instanceof PrimitiveAssertion) {
//
//			Iterator iterator = normalizedMe.getTerms().iterator();
//
//			while (iterator.hasNext()) {
//				AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
//						.next();
//				Assertion value = andTerm.intersect(assertion);
//				if (value instanceof AndCompositeAssertion) {
//					result.addTerm(value);
//				}
//			}
//
//		} else {
//
//			Iterator iterator = normalizedMe.getTerms().iterator();
//			while (iterator.hasNext()) {
//				AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
//						.next();
//
//				if (target instanceof AndCompositeAssertion) {
//					Assertion value = andTerm.intersect(target);
//
//					if (value instanceof AndCompositeAssertion) {
//						result.addTerm(value);
//					}
//
//				} else if (target instanceof XorCompositeAssertion) {
//
//					Iterator andTerms = target.getTerms().iterator();
//
//					while (andTerms.hasNext()) {
//						AndCompositeAssertion tAndTerm = (AndCompositeAssertion) andTerms
//								.next();
//						Assertion value = andTerm.intersect(tAndTerm);
//
//						if (value instanceof AndCompositeAssertion) {
//							result.addTerm(value);
//						}
//					}
//				}
//			}
//		}
//
//		return result;
	}

	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: XorCompositeAssertion::merge");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof XorCompositeAssertion)) {
			return normalizedMe.merge(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		short type = target.getType();

		switch (type) {

		case Assertion.COMPOSITE_POLICY_TYPE: {

			Policy nPOLICY = new Policy();
			nPOLICY.addTerm(normalizedMe.merge((Assertion) target.getTerms()
					.get(0)));
			return nPOLICY;
		}

		case Assertion.COMPOSITE_XOR_TYPE: {

			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				AndCompositeAssertion AND = (AndCompositeAssertion) iterator
						.next();

				for (Iterator iterator2 = target.getTerms().iterator(); iterator2
						.hasNext();) {
					nXOR.addTerm(AND.merge((Assertion) iterator2.next()));
				}

				if (target.isEmpty() && AND.isEmpty()) { // FIXME is this a
														 // hack?
					/*
					 * " <wsp:ExactlyOne> <wsp:All/>
					 * </wsp:ExactlyOne>".intersect(" <wsp:ExactlyOne/>")
					 */
					nXOR.addTerm(AND);
				}
			}

			return nXOR;
		}

		case Assertion.COMPOSITE_AND_TYPE: {

			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				nXOR.addTerm(((AndCompositeAssertion) iterator.next())
						.merge(target));
			}
			return nXOR;

		}

		case Assertion.PRIMITIVE_TYPE: {
			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				nXOR.addTerm(((AndCompositeAssertion) iterator.next())
						.merge(target));
			}

			return nXOR;
		}

		default: {
			throw new IllegalArgumentException("merge is not defined for "
					+ target.getClass().getName() + " type assertions");
		}

		}
	}

	public final short getType() {
		return Assertion.COMPOSITE_XOR_TYPE;
	}
}