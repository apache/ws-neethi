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
 * All requires that all of its terms are met.
 * 
 * Sanka Samaranayake (sanka@apache.org)
 */
public class All extends AbstractAssertion implements
		CompositeAssertion {

	private Log log = LogFactory.getLog(this.getClass().getName());

	public All() {
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
		log.debug("Enter: All::intersect");

		Assertion normalizedMe = ((isNormalized()) ? this : normalize(reg));

		if (!(normalizedMe instanceof All)) {
			return normalizedMe.intersect(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);
		short type = target.getType();

		switch (type) {

		case Assertion.POLICY: {
			Policy newPolicy = new Policy();
			newPolicy
					.addTerm(normalizedMe
							.intersect((ExactlyOne) target
									.getTerms().get(0)));
			return newPolicy;
		}

		case Assertion.EXACTLY_ONE: {
			ExactlyOne newExactlyOne = new ExactlyOne();

			for (Iterator iterator = target.getTerms().iterator(); iterator
					.hasNext();) {
				Assertion asser = normalizedMe
						.intersect((All) iterator.next());
                
				if (Assertion.ALL == asser.getType()) {
					newExactlyOne.addTerm(asser);
				}
			}
			return newExactlyOne;
		}

		case Assertion.ALL: {
			List primitives_A = ((normalizedMe.size() > target.size()) ? normalizedMe
					.getTerms()
					: target.getTerms());
			List primtives_B = ((normalizedMe.size() > target.size()) ? target
					.getTerms() : normalizedMe.getTerms());

			boolean isMatch = true;
			PrimitiveAssertion primitive_A, primitive_B = null;
			//				QName name_A, name_B;

			for (int i = 0; i < primitives_A.size(); i++) {
				primitive_A = (PrimitiveAssertion) primitives_A.get(i);
				//					name_A = PRIMITIVE_A.getName();

				boolean flag = false;

				for (int j = 0; j < primtives_B.size(); j++) {
					primitive_B = (PrimitiveAssertion) primtives_B.get(j);
					//						name_B = PRIMTIVE_B.getName();

					if (primitive_A.getName().equals(primitive_B.getName())) {
						flag = true;
						break;
					}
				}

				if (!flag) {
					return new ExactlyOne();
				}

				Assertion a = primitive_A.intersect(primitive_B);

				if (a instanceof ExactlyOne) {
					return new ExactlyOne();
				}
			}
			All result = new All();
			result.addTerms(primitives_A);
			result.addTerms(primtives_B);
			return result;
		}

		case Assertion.PRIMITIVE: {
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
				All newAll = new All();
				newAll.addTerms(normalizedMe.getTerms());
				newAll.addTerm(target);
				return newAll;
			}

			return new ExactlyOne();
		}

		default: {
			throw new IllegalArgumentException("intersect is not defined for "
					+ target.getClass().getName() + "type assertions");
		}

		}
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
		log.debug("Enter: All::merge");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof All)) {
			return normalizedMe.merge(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		switch (target.getType()) {

		case Assertion.POLICY: {
			Policy newPolicy = new Policy();
			newPolicy.addTerm(normalizedMe.merge((ExactlyOne) target
					.getTerms().get(0)));
			return newPolicy;
		}

		case Assertion.EXACTLY_ONE: {

			ExactlyOne newExactlyOne = new ExactlyOne();

			for (Iterator iterator = target.getTerms().iterator(); iterator
					.hasNext();) {
				All all = (All) iterator
						.next();
				newExactlyOne.addTerm(normalizedMe.merge(all));
			}

			return newExactlyOne;
		}

		case Assertion.ALL: {
			All newAll = new All();

			newAll.addTerms(normalizedMe.getTerms());
			newAll.addTerms(target.getTerms());

			return newAll;
		}

		case Assertion.PRIMITIVE: {
			All newAll = new All();

			newAll.addTerms(normalizedMe.getTerms());
			newAll.addTerm(target);

			return newAll;
		}

		default: {
			throw new IllegalArgumentException("merge is not defined for");
		}
		}
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
		log.debug("Enter: All::normalize");

		if (isNormalized()) {
			return this;
		}

		All all = new All();
		ExactlyOne exactlyOne = new ExactlyOne();

		ArrayList exactlyOnes = new ArrayList();

		if (isEmpty()) {
			all.setNormalized(true);
			return all;
		}

		Iterator terms = getTerms().iterator();

		while (terms.hasNext()) {
			Assertion term = (Assertion) terms.next();
			term = (term instanceof Policy) ? term : term.normalize(reg);

			if (term instanceof Policy) {
				Assertion wrapper = new All();
				((All) wrapper).addTerms(((Policy) term)
						.getTerms());
				term = wrapper.normalize(reg);
			}

			if (term instanceof ExactlyOne) {

				if (((ExactlyOne) term).isEmpty()) {

					/*  */
					ExactlyOne anExactlyOne = new ExactlyOne();
					anExactlyOne.setNormalized(true);
					return anExactlyOne;
				}
				exactlyOnes.add(term);
				continue;

			}

			if (term instanceof All) {
				all.addTerms(((All) term).getTerms());
				continue;
			}

			all.addTerm(term);
		}

		// processing child-XORCompositeAssertions
		if (exactlyOnes.size() > 1) {
            exactlyOne.addTerms(Policy.crossProduct(exactlyOnes, 0));
            
		} else if (exactlyOnes.size() == 1) {
			Assertion XORterm = (Assertion) exactlyOnes.get(0);
			exactlyOne.addTerms(XORterm.getTerms());
		}

		if (exactlyOne.isEmpty()) {
			all.setNormalized(true);
			return all;
		}

		if (all.isEmpty()) {
			exactlyOne.setNormalized(true);
			return exactlyOne;
		}

		List primTerms = all.getTerms();
		Iterator interator = exactlyOne.getTerms().iterator();

		while (interator.hasNext()) {
			Assertion newAll = (Assertion) interator.next();
			newAll.addTerms(primTerms);
		}

		exactlyOne.setNormalized(true);
		return exactlyOne;
	}

	public final short getType() {
		return Assertion.ALL;
	}

}