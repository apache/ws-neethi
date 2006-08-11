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
 * ExactlyOne requires that exactly one of its terms are met.
 * 
 * Sanka Samaranayake (sanka@apache.org)
 */
public class ExactlyOne extends AbstractAssertion implements
		CompositeAssertion {

	private Log log = LogFactory.getLog(this.getClass().getName());

	public ExactlyOne() {
	}

	public void addTerm(Assertion assertion) {
		if (!(isNormalized() && (assertion instanceof All) && ((All) assertion)
				.isNormalized())) {
			setNormalized(false);
		}
		super.addTerm(assertion);
	}

	public Assertion normalize(PolicyRegistry reg) {
		log.debug("Enter: ExactlyOne::normalize");

		if (isNormalized()) {
			return this;
		}

		ExactlyOne exactlyOne = new ExactlyOne();

		if (isEmpty()) {
			exactlyOne.setNormalized(true);
			return exactlyOne;
		}

		Iterator terms = getTerms().iterator();

		while (terms.hasNext()) {
			Assertion term = (Assertion) terms.next();
			term = (term instanceof Policy) ? term : term.normalize(reg);

			if (term instanceof Policy) {
				Assertion wrapper = new All();
				((All) wrapper).addTerms(((Policy) term)
						.getTerms());
				wrapper = wrapper.normalize(reg);

				if (wrapper instanceof All) {
					exactlyOne.addTerm(wrapper);

				} else {
					exactlyOne.addTerms(((ExactlyOne) wrapper).getTerms());
				}
				continue;
			}

			if (term instanceof PrimitiveAssertion) {
				All wrapper = new All();
				wrapper.addTerm(term);
				exactlyOne.addTerm(wrapper);
				continue;
			}

			if (term instanceof ExactlyOne) {
				exactlyOne.addTerms(((ExactlyOne) term).getTerms());
				continue;
			}

			if (term instanceof All) {
				exactlyOne.addTerm(term);
			}
		}

		exactlyOne.setNormalized(true);
		return exactlyOne;
	}

	public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: ExactlyOne::intersect");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof ExactlyOne)) {
			return normalizedMe.intersect(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);
		short type = target.getType();

		switch (type) {

		case Assertion.POLICY: {
			Policy newPolicy = new Policy();
			newPolicy.addTerm(((ExactlyOne) normalizedMe.getTerms()
					.get(0)).intersect(target));
			return newPolicy;
		}

		case Assertion.EXACTLY_ONE: {
			ExactlyOne newExactlyOne = new ExactlyOne();

			Assertion asser;
			All all;

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				all = (All) iterator.next();

				for (Iterator iterator2 = target.getTerms().iterator(); iterator2
						.hasNext();) {
					asser = all.intersect((All) iterator2
							.next());

					if (asser instanceof All) {
						newExactlyOne.addTerm(asser);
					}
				}
			}

			return newExactlyOne;
		}

		case Assertion.ALL: {
			ExactlyOne newExactlyOne = new ExactlyOne();
			Assertion asser;

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				asser = ((All) iterator.next())
						.intersect(target);

				if (asser instanceof All) {
					newExactlyOne.addTerm(asser);
				}
			}
			return newExactlyOne;
		}

		case Assertion.PRIMITIVE: {
			ExactlyOne newExactlyOne = new ExactlyOne();

			Assertion asser;

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				asser = ((All) iterator.next())
						.intersect(target);

				if (asser instanceof All) {
					newExactlyOne.addTerm(asser);
				}
			}
			return newExactlyOne;
		}

		default: {
			throw new IllegalArgumentException("intersect for assertion type "
					+ target.getClass().getName() + " not defined");
		}

		}
	}

	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: ExactlyOne::merge");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof ExactlyOne)) {
			return normalizedMe.merge(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		short type = target.getType();

		switch (type) {

		case Assertion.POLICY: {

			Policy newPolicy = new Policy();
			newPolicy.addTerm(normalizedMe.merge((Assertion) target.getTerms()
					.get(0)));
			return newPolicy;
		}

		case Assertion.EXACTLY_ONE: {

			ExactlyOne newExactlyOne = new ExactlyOne();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				All AND = (All) iterator
						.next();

				for (Iterator iterator2 = target.getTerms().iterator(); iterator2
						.hasNext();) {
					newExactlyOne.addTerm(AND.merge((Assertion) iterator2.next()));
				}
			}

			return newExactlyOne;
		}

		case Assertion.ALL: {

			ExactlyOne newExactlyOne = new ExactlyOne();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				newExactlyOne.addTerm(((All) iterator.next())
						.merge(target));
			}
			return newExactlyOne;

		}

		case Assertion.PRIMITIVE: {
			ExactlyOne newExactlyOne = new ExactlyOne();

			for (Iterator iterator = normalizedMe.getTerms().iterator(); iterator
					.hasNext();) {
				newExactlyOne.addTerm(((All) iterator.next())
						.merge(target));
			}

			return newExactlyOne;
		}

		default: {
			throw new IllegalArgumentException("merge is not defined for "
					+ target.getClass().getName() + " type assertions");
		}

		}
	}

	public final short getType() {
		return Assertion.EXACTLY_ONE;
	}
}