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
 * AndCompositeAssertion requires that all of its terms are met.
 * 
 * Sanka Samaranayake (sanka@apache.org)
 */
public class AndCompositeAssertion extends AbstractAssertion implements
		CompositeAssertion {

	private Log log = LogFactory.getLog(this.getClass().getName());

	public AndCompositeAssertion() {
	}

	/**
	 * Adds an Assertion to its terms list
	 * 
	 * @param assertion
	 *            Assertion to be added
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
	 *            the assertion to intersect with self
	 * @param reg
	 *            a sepcified policy registry
	 * @return assertion the assertion which is equivalent to intersection
	 *         between self and the argument
	 */
	public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: AndCompositeAssertion::intersect");

		Assertion normalizedMe = ((isNormalized()) ? this : normalize(reg));

		if (!(normalizedMe instanceof AndCompositeAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);
		short type = target.getType();

		switch (type) {

		case Assertion.COMPOSITE_POLICY_TYPE: {
			Policy nPOLICY = new Policy();
			nPOLICY
					.addTerm(normalizedMe
							.intersect((XorCompositeAssertion) target
									.getTerms().get(0)));
			return nPOLICY;
		}

		case Assertion.COMPOSITE_XOR_TYPE: {
			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			for (Iterator iterator = target.getTerms().iterator(); iterator
					.hasNext();) {
				Assertion asser = normalizedMe
						.intersect((AndCompositeAssertion) iterator.next());

				//					Assertion asser = ((AndCompositeAssertion)
				// iterator.next()).intersect(normalizedMe);

				if (Assertion.COMPOSITE_AND_TYPE == asser.getType()) {
					nXOR.addTerm(asser);
				}
			}
			return nXOR;
		}

		case Assertion.COMPOSITE_AND_TYPE: {
			List PRIMITIVES_A = ((normalizedMe.size() > target.size()) ? normalizedMe
					.getTerms()
					: target.getTerms());
			List PRIMTIVES_B = ((normalizedMe.size() > target.size()) ? target
					.getTerms() : normalizedMe.getTerms());

			boolean isMatch = true;
			PrimitiveAssertion PRIMITIVE_A, PRIMTIVE_B = null;
			//				QName name_A, name_B;

			for (int i = 0; i < PRIMITIVES_A.size(); i++) {
				PRIMITIVE_A = (PrimitiveAssertion) PRIMITIVES_A.get(i);
				//					name_A = PRIMITIVE_A.getName();

				boolean flag = false;

				for (int j = 0; j < PRIMTIVES_B.size(); j++) {
					PRIMTIVE_B = (PrimitiveAssertion) PRIMTIVES_B.get(j);
					//						name_B = PRIMTIVE_B.getName();

					if (PRIMITIVE_A.getName().equals(PRIMTIVE_B.getName())) {
						flag = true;
						break;
					}

					//						if (name_A.getNamespaceURI().equals(
					//								name_B.getNamespaceURI())) {
					//							flag = true;
					//							break;
					//						}
				}

				if (!flag) {
					return new XorCompositeAssertion();
				}

				Assertion a = PRIMITIVE_A.intersect(PRIMTIVE_B);

				if (a instanceof XorCompositeAssertion) {
					return new XorCompositeAssertion();
				}
			}
			AndCompositeAssertion result = new AndCompositeAssertion();
			result.addTerms(PRIMITIVES_A);
			result.addTerms(PRIMTIVES_B);
			return result;
		}

		case Assertion.PRIMITIVE_TYPE: {
			QName name = ((PrimitiveAssertion) target).getName();
			boolean isMatch = false;

			QName targetName;
			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				targetName = ((PrimitiveAssertion) iterator.next()).getName();

				if (name.getNamespaceURI().equals(targetName.getNamespaceURI())) {
					isMatch = true;
					break;
				}
			}

			if (isMatch) {
				AndCompositeAssertion nAND = new AndCompositeAssertion();
				nAND.addTerms(normalizedMe.getTerms());
				nAND.addTerm(target);
				return nAND;
			}

			return new XorCompositeAssertion();
		}

		default: {
			throw new IllegalArgumentException("intersect is not defined for "
					+ target.getClass().getName() + "type assertions");
		}

		}

		//
		//		if (assertion instanceof PrimitiveAssertion) {
		//			QName qname = ((PrimitiveAssertion) assertion).getName();
		//			Iterator iterator = getTerms().iterator();
		//			boolean isMatch = false;
		//
		//			while (iterator.hasNext()) {
		//				PrimitiveAssertion primTerm = (PrimitiveAssertion) iterator
		//						.next();
		//				if (primTerm.getName().equals(qname)) {
		//					isMatch = true;
		//					break;
		//				}
		//			}
		//			return (isMatch) ? normalizedMe : new XorCompositeAssertion();
		//		}
		//
		//		Assertion target = (CompositeAssertion) assertion;
		//		target = (CompositeAssertion) ((target.isNormalized()) ? target
		//				: target.normalize(reg));

		//		if (target instanceof Policy) {
		//			XorCompositeAssertion alters = (XorCompositeAssertion) target
		//					.getTerms().get(0);
		//			return normalizedMe.intersect(alters);
		//
		//		} else if (target instanceof XorCompositeAssertion) {
		//			XorCompositeAssertion result = new XorCompositeAssertion();
		//			Iterator iterator = target.getTerms().iterator();
		//
		//			while (iterator.hasNext()) {
		//				AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator
		//						.next();
		//				Assertion value = normalizedMe.intersect(andTerm);
		//				if (value instanceof AndCompositeAssertion) {
		//					result.addTerm(value);
		//				}
		//			}
		//			return result;
		//		}
		//
		//		if (normalizedMe.isEmpty()) {
		//			return target;
		//		}
		//		if (target.isEmpty()) {
		//			return normalizedMe;
		//		}
		//
		//		List primTermsA = ((size() > target.size()) ? normalizedMe.getTerms()
		//				: target.getTerms());
		//		List primTermsB = ((size() > target.size()) ? target.getTerms()
		//				: normalizedMe.getTerms());
		//
		//		boolean isMatch = true;
		//		PrimitiveAssertion primTermA, primTermB;
		//		QName qnameA, qnameB;
		//
		//		for (int i = 0; i < primTermsA.size(); i++) {
		//			primTermA = (PrimitiveAssertion) primTermsA.get(i);
		//			qnameA = primTermA.getName();
		//			boolean flag = false;
		//
		//			for (int j = 0; j < primTermsB.size(); j++) {
		//				primTermB = (PrimitiveAssertion) primTermsB.get(j);
		//				qnameB = primTermB.getName();
		//				if (qnameA.equals(qnameB)) {
		//					flag = true;
		//					break;
		//				}
		//			}
		//			if (!flag) {
		//				isMatch = false;
		//				break;
		//			}
		//		}
		//
		//		if (isMatch) { // vocabulary matches
		//			AndCompositeAssertion result = new AndCompositeAssertion();
		//			result.addTerms(primTermsA);
		//			result.addTerms(primTermsB);
		//			return result;
		//		}
		//
		//		return new XorCompositeAssertion(); // no behaviour is admisible
	}

	/**
	 * Returns an assertion which is equivalent to merge of self and the
	 * argument.
	 * 
	 * @param assertion
	 *            the assertion to be merged with
	 * @param reg
	 *            the policy registry which the is used resolve external policy
	 *            references
	 * @return assertion the resultant assertion which is equivalent to merge of
	 *         self and argument
	 */
	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: AndCompositeAssertion::merge");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof AndCompositeAssertion)) {
			return normalizedMe.merge(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		switch (target.getType()) {

		case Assertion.COMPOSITE_POLICY_TYPE: {
			Policy nPOLICY = new Policy();
			nPOLICY.addTerm(normalizedMe.merge((XorCompositeAssertion) target
					.getTerms().get(0)));
			return nPOLICY;
		}

		case Assertion.COMPOSITE_XOR_TYPE: {

			XorCompositeAssertion nXOR = new XorCompositeAssertion();

			for (Iterator iterator = target.getTerms().iterator(); iterator
					.hasNext();) {
				AndCompositeAssertion AND = (AndCompositeAssertion) iterator
						.next();
				nXOR.addTerm(normalizedMe.merge(AND));
			}

			return nXOR;
		}

		case Assertion.COMPOSITE_AND_TYPE: {
			AndCompositeAssertion nAND = new AndCompositeAssertion();

			nAND.addTerms(normalizedMe.getTerms());
			nAND.addTerms(target.getTerms());

			return nAND;
		}

		case Assertion.PRIMITIVE_TYPE: {
			AndCompositeAssertion nAND = new AndCompositeAssertion();

			nAND.addTerms(normalizedMe.getTerms());
			nAND.addTerm(target);

			return nAND;
		}

		default: {
			throw new IllegalArgumentException("merge is not defined for");
		}
		}

		//        
		//        if (assertion instanceof PrimitiveAssertion) {
		//            AndCompositeAssertion andTerm = new AndCompositeAssertion();
		//            andTerm.addTerm(assertion);
		//            andTerm.addTerms(normalizedMe.getTerms());
		//            andTerm.setNormalized(true);
		//            return andTerm;
		//        }
		////
		//// Assertion target = ((assertion.isNormalized()) ? assertion :
		// assertion.normalize(reg));
		//
		//        if (target instanceof Policy) {
		//            XorCompositeAssertion xorTerm = (XorCompositeAssertion) target
		//                    .getTerms().get(0);
		//            return normalizedMe.merge(xorTerm);
		//
		//        } else if (target instanceof XorCompositeAssertion) {
		//            XorCompositeAssertion xorTerm = new XorCompositeAssertion();
		//
		//            Iterator hisAndTerms = target.getTerms().iterator();
		//            while (hisAndTerms.hasNext()) {
		//                AndCompositeAssertion hisAndTerm = (AndCompositeAssertion)
		// hisAndTerms
		//                        .next();
		//                xorTerm.addTerm(normalizedMe.merge(hisAndTerm));
		//            }
		//            xorTerm.setNormalized(true);
		//            return xorTerm;
		//
		//        } else if (target instanceof AndCompositeAssertion) {
		//            AndCompositeAssertion andTerm = new AndCompositeAssertion();
		//            andTerm.addTerms(normalizedMe.getTerms());
		//            andTerm.addTerms(target.getTerms());
		//            andTerm.setNormalized(true);
		//            return andTerm;
		//        }
		//
		//        throw new IllegalArgumentException("error : merge is not defined for"
		//                + assertion.getClass().getName());
	}

	/**
	 * Returns an Assertion which is normalized using a specified policy
	 * registry.
	 * 
	 * @param reg
	 *            the policy registry used to resolve policy references
	 * @return an Assertion which is the normalized form of self
	 */
	public Assertion normalize(PolicyRegistry reg) {
		log.debug("Enter: AndCompositeAssertion::normalize");

		if (isNormalized()) {
			return this;
		}

		AndCompositeAssertion AND = new AndCompositeAssertion();
		XorCompositeAssertion XOR = new XorCompositeAssertion();

		ArrayList XORs = new ArrayList();

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
					XorCompositeAssertion anXOR = new XorCompositeAssertion();
					anXOR.setNormalized(true);
					return anXOR;
				}
				XORs.add(term);
				continue;

			}

			if (term instanceof AndCompositeAssertion) {
				AND.addTerms(((AndCompositeAssertion) term).getTerms());
				continue;
			}

			AND.addTerm(term);
		}

		// processing child-XORCompositeAssertions
		if (XORs.size() > 1) {
            XOR.addTerms(Policy.crossProduct(XORs, 0));
            
		} else if (XORs.size() == 1) {
			Assertion XORterm = (Assertion) XORs.get(0);
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
			Assertion rAND = (Assertion) interator.next();
			rAND.addTerms(primTerms);
		}

		XOR.setNormalized(true);
		return XOR;
	}

	public final short getType() {
		return Assertion.COMPOSITE_AND_TYPE;
	}

}