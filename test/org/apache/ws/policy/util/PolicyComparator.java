/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.ws.policy.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Assertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyComparator {
	public static boolean compare(Policy arg1, Policy arg2) {
		if (arg1.getId() == null && arg2.getId() != null
				|| arg1.getId() != null && arg1.getId() == null) {
			return false;
		}

		if (arg1.getId() != null) {
			if (!arg1.getId().equals(arg2.getId())) {
				return false;
			}
		}

		if (arg1.getBase() == null && arg2.getBase() != null
				|| arg1.getBase() != null && arg1.getBase() == null) {
			return false;
		}

		if (arg1.getBase() != null) {
			if (!arg1.getBase().equals(arg2.getBase())) {
				return false;
			}
		}

		return compare(arg1.getTerms(), arg2.getTerms());
	}
	
	public static boolean compare(Assertion arg1, Assertion arg2) {
		if (! arg1.getClass().equals(arg2.getClass())) {
			return false;
		}

		if (arg1 instanceof Policy) {
			return compare((Policy) arg1, (Policy) arg2);

		} else if (arg1 instanceof PolicyReference) {

			return compare((PolicyReference) arg1, (PolicyReference) arg2);

		} else if (arg1 instanceof AndCompositeAssertion) {

			return compare((AndCompositeAssertion) arg1,
					(AndCompositeAssertion) arg2);

		} else if (arg1 instanceof XorCompositeAssertion) {
			return compare((XorCompositeAssertion) arg1,
					(XorCompositeAssertion) arg2);

		} else if (arg1 instanceof PrimitiveAssertion) {
			return compare((PrimitiveAssertion) arg1, (PrimitiveAssertion) arg2);

		} else {
			// TODO should I throw an exception ..
		}

		return false;
	}

	

	public static boolean compare(PolicyReference arg1, PolicyReference arg2) {
		return arg1.getPolicyURIString().equals(arg2.getPolicyURIString());
	}

	public static boolean compare(AndCompositeAssertion arg1,
			AndCompositeAssertion arg2) {
		return compare(arg1.getTerms(), arg2.getTerms());
	}

	public static boolean compare(XorCompositeAssertion arg1,
			XorCompositeAssertion arg2) {
		return compare(arg1.getTerms(), arg2.getTerms());
	}

	public static boolean compare(PrimitiveAssertion arg1,
			PrimitiveAssertion arg2) {
		if (!(arg1.getName().equals(arg2.getName()))) {
			return false;
		}
		if (arg1.getStrValue() != null) {
			String arg1Str = arg1.getStrValue().trim();
			if (arg2.getStrValue() == null) {
				return false;
			} else {
				String arg2Str = arg2.getStrValue().trim();
				if (! arg1Str.equals(arg2Str)) {
					return false;}
			}
		}
//		if ((arg1.getStrValue() == null || arg1.getStrValue().trim() == "") && (arg2.getStrValue() != null && arg2.getStrValue().trim() != "")
//				|| (arg1.getStrValue() != null && arg1.getStrValue().trim() == "") && (arg1.getStrValue() == null || arg1.getStrValue().trim() == "")) {
//			return false;
//		}
		return compare(arg1.getTerms(), arg2.getTerms());
	}

	private static boolean compare(List arg1, List arg2) {
		if (arg1.size() != arg2.size()) {
			return false;
		}
		
		Iterator iterator = arg1.iterator();
		Assertion assertion1;

		while (iterator.hasNext()) {
			assertion1 = (Assertion) iterator.next();

			Iterator iterator2 = arg2.iterator();
			boolean match = false;
			Assertion assertion2;

			while (iterator2.hasNext()) {
				assertion2 = (Assertion) iterator2.next();
				if (compare(assertion1, assertion2)) {
					match = true;
					break;
				}
			}

			if (!match) {
				return false;
			}
		}
		return true;
	}

	private static boolean compare(Hashtable arg1, Hashtable arg2) {
		if (arg1.size() != arg2.size()) {
			return false;
		}
		Iterator iterator1 = arg1.keySet().iterator();
		while (iterator1.hasNext()) {
			QName qname = (QName) iterator1.next();
			if (arg2.get(qname) == null
					|| !arg1.get(qname).equals(arg2.get(qname))) {
				return false;
			}
		}
		return true;
	}
}