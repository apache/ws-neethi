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
import org.apache.ws.policy.util.PolicyUtil;

/**
 * PrimitiveAssertion wraps an assertion which contain domain specific
 * knowledge. This type of an assertion should only be evaluated by a component
 * which contains the required knowledge. For instance a PrimitiveAssertion
 * which contains a WSSecurity policy assertionshould be processed only by the
 * security module.
 */
public class PrimitiveAssertion extends AbstractAssertion implements Assertion {

	private Log log = LogFactory.getLog(this.getClass().getName());

	private Assertion owner = null;

	private QName qname;

	private List terms = new ArrayList();

	private Hashtable attributes = new Hashtable();

	private boolean flag = false;

	private boolean isOptional = false;

	private String strValue = null;

	private Object value;

	public PrimitiveAssertion(QName qname) {
		this.qname = qname;
	}

	public PrimitiveAssertion(QName qname, Object value) {
		this.qname = qname;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public QName getName() {
		return qname;
	}

	public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: PrimitveAssertion:intersect");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof PrimitiveAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		// Am not a primitive assertion anymore ..
		if (!(assertion instanceof PrimitiveAssertion)) {
			return normalizedMe.intersect(assertion, reg);
		}

		// argument is not primitive type ..
		if (!(target instanceof PrimitiveAssertion)) {
			return target.intersect(normalizedMe, reg);
		}

		PrimitiveAssertion arg = (PrimitiveAssertion) target;
		PrimitiveAssertion self = (PrimitiveAssertion) normalizedMe;

		if (!self.getName().equals(arg.getName())) {
			return new ExactlyOne(); // no bahaviour is admisible
		}

		if (self.getTerms().isEmpty() && arg.getTerms().isEmpty()) {
			All assertion2 = new All();
			assertion2.addTerm(self);
			assertion2.addTerm(arg);
			return assertion2;
		}

		if (self.getTerms().isEmpty() || arg.getTerms().isEmpty()) {
			return new ExactlyOne(); // no
		}

		List argChildTerms;
		if (arg.getTerms().get(0) instanceof Policy) {
			argChildTerms = PolicyUtil.getPrimTermsList((Policy) arg.getTerms()
					.get(0));
		} else {
			argChildTerms = arg.getTerms();
		}

		List selfChildTerms;
		if (self.getTerms().get(0) instanceof Policy) {
			selfChildTerms = PolicyUtil.getPrimTermsList((Policy) self
					.getTerms().get(0));
		} else {
			selfChildTerms = self.getTerms();
		}

		///////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////

		PrimitiveAssertion PRIMITIVE_A, PRIMITIVE_B = null;
		List primListA, primListB;

		if (selfChildTerms.size() > argChildTerms.size()) {
			primListA = selfChildTerms;
			primListB = argChildTerms;
		} else {
			primListA = argChildTerms;
			primListB = selfChildTerms;
		}

		boolean isIntersect = false;

		for (Iterator iterator = primListA.iterator(); iterator.hasNext();) {
			PRIMITIVE_A = (PrimitiveAssertion) iterator.next();

			boolean found = false;

			for (Iterator iterator2 = primListB.iterator(); iterator2.hasNext();) {
				PRIMITIVE_B = (PrimitiveAssertion) iterator2.next();
				if (PRIMITIVE_A.getName().equals(PRIMITIVE_B.getName())) {
					found = true;
					break;
				}
			}

			if (!found) {
				return new ExactlyOne();
			}

			if (PRIMITIVE_A.intersect(PRIMITIVE_B) instanceof ExactlyOne) {
				return new ExactlyOne();
			}
		}

		All all = new All();
		all.addTerm(arg);
		all.addTerm(self);
		return all;
	}

	public Assertion intersect(Assertion assertion)
			throws UnsupportedOperationException {
		return intersect(assertion, null);
	}

	public Assertion merge(Assertion assertion, PolicyRegistry reg) {
		log.debug("Enter: PrimitveAssertion:merge");

		Assertion normalizedMe = (isNormalized()) ? this : normalize(reg);

		if (!(normalizedMe instanceof PrimitiveAssertion)) {
			return normalizedMe.merge(assertion, reg);
		}

		Assertion target = (assertion.isNormalized()) ? assertion : assertion
				.normalize(reg);

		if (!(target instanceof PrimitiveAssertion)) {
			return target.merge(normalizedMe, reg);
		}

		/*
		 * both self and the argument are primitive assertions. Hence both
		 * should be wrapped in an AndCompositeType
		 */
		All all = new All();
		all.addTerm(target);
		all.addTerm(normalizedMe);
		return all;
	}

	public Assertion merge(Assertion assertion) {
		return merge(assertion, null);
	}

	public Assertion normalize() {
		return normalize(null);
	}

	public Assertion normalize(PolicyRegistry reg) {
		log.debug("Enter: PrimitveAssertion:normalize");

		if (isNormalized()) {
			return this;
		}

		if (isOptional()) {
			ExactlyOne exactlyOne = new ExactlyOne();
			All all = new All();

			PrimitiveAssertion prim = getSelfWithoutTerms();
			prim.removeAttribute(new QName(
					PolicyConstants.POLICY_NAMESPACE_URI, "Optional"));
			prim.setOptional(false);
			prim.setTerms(getTerms());

			all.addTerm(prim);
			exactlyOne.addTerm(all);
			exactlyOne.addTerm(new All());

			return exactlyOne.normalize(reg);
		}

		if (getTerms().isEmpty()) {
			PrimitiveAssertion pirm = getSelfWithoutTerms();
			pirm.setNormalized(true);
			return pirm;
		}

		ArrayList policyTerms = new ArrayList();
		ArrayList nonPolicyTerms = new ArrayList();

		Iterator iterator = getTerms().iterator();

		while (iterator.hasNext()) {
			Assertion term = (Assertion) iterator.next();

			if (term instanceof Policy) {
				policyTerms.add(term);

			} else if (term instanceof PrimitiveAssertion) {
				nonPolicyTerms.add(term);

			} else {
				throw new RuntimeException();
				//TODO should I throw an exception ..
			}
		}

		if (policyTerms.isEmpty()) {
			PrimitiveAssertion prim = getSelfWithoutTerms();
			prim.setTerms(getTerms());
			prim.setNormalized(true);
			return prim;
		}

		Policy policy = PolicyUtil.getSinglePolicy(policyTerms, reg);
		Assertion exactlyOne = (ExactlyOne) policy.getTerms()
				.get(0);

		List alls = exactlyOne.getTerms();

		if (alls.size() == 0) {
			return new ExactlyOne();
		}

		if (alls.size() == 1) {
			((All) alls.get(0)).addTerms(nonPolicyTerms);
			PrimitiveAssertion prim = getSelfWithoutTerms();
			prim.addTerm(policy);
			return prim;
		}

		Policy newPolicy = new Policy();
		ExactlyOne newExactlyOne = new ExactlyOne();
		newPolicy.addTerm(newExactlyOne);

		PrimitiveAssertion newPrim;
		Iterator iterator2 = alls.iterator();

		ArrayList list;

		while (iterator2.hasNext()) {
			newPrim = getSelfWithoutTerms();

			list = new ArrayList();
			list.addAll(((All) iterator2.next()).getTerms());

			if (!nonPolicyTerms.isEmpty()) {
				list.addAll(nonPolicyTerms);
			}
			newPrim.addTerm(getSinglePolicy(list));
			All all = new All();
			all.addTerm(newPrim);
			newExactlyOne.addTerm(all);
		}
		newPolicy.setNormalized(true);
		return newPolicy;
	}

	private PrimitiveAssertion getSelfWithoutTerms() {
		PrimitiveAssertion self = new PrimitiveAssertion(getName());
		self.setAttributes(getAttributes());
		self.setStrValue(getStrValue());
		return self;
	}

	public boolean hasParent() {
		return owner != null;
	}

	public Assertion getParent() {
		return owner;
	}

	public void setAttributes(Hashtable attributes) {
		this.attributes = attributes;
	}

	public Hashtable getAttributes() {
		return attributes;
	}

	public void addAttribute(QName qname, Object value) {
		attributes.put(qname, value);
	}

	public String getAttribute(QName qname) {
		return (String) attributes.get(qname);
	}

	public void removeAttribute(QName qname) {
		attributes.remove(qname);
	}

	public void setParent(Assertion parent) {
		this.owner = parent;
	}

	public List getTerms() {
		return terms;
	}

	public void setTerms(List terms) {
		this.terms = terms;
	}

	public void addTerm(Assertion term) {
		terms.add(term);
	}

	public void addTerms(List terms) {
		terms.addAll(terms);
	}

	public boolean isNormalized() {
		return flag;
	}

	public void setNormalized(boolean flag) {
		Iterator iterator = getTerms().iterator();
		while (iterator.hasNext()) {
			Assertion assertion = (Assertion) iterator.next();
			assertion.setNormalized(flag);
		}
		this.flag = flag;
	}

	public String getStrValue() {
		return strValue;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	private Policy getSinglePolicy(List policyList, PolicyRegistry reg) {
		Policy result = null;
		Iterator iterator = policyList.iterator();
		while (iterator.hasNext()) {
			Policy policy = (Policy) iterator.next();
			result = (result == null) ? policy : (Policy) result.merge(policy,
					reg);
		}
		return result;
	}

	public boolean isEmpty() {
		return terms.isEmpty();
	}

	public boolean remove(Assertion assertion) {
		return terms.remove(assertion);
	}

	public int size() {
		return terms.size();
	}

	private Policy getSinglePolicy(List childTerms) {
		Policy policy = new Policy();
		ExactlyOne exactlyOne = new ExactlyOne();
		All all = new All();
		all.addTerms(childTerms);
		exactlyOne.addTerm(all);
		policy.addTerm(exactlyOne);
		return policy;
	}

	private boolean isEmptyPolicy(Policy policy) {
		ExactlyOne exactlyOne = (ExactlyOne) policy.getTerms()
				.get(0);
		return exactlyOne.isEmpty();
	}

	private List getTerms(Policy policy) {
		return ((All) ((ExactlyOne) policy
				.getTerms().get(0)).getTerms().get(0)).getTerms();
	}

	public final short getType() {
		return Assertion.PRIMITIVE;
	}

}