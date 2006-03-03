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

package org.apache.ws.policy.util;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.ws.policy.Policy;

/**
 * PolicyRegistry is useful to resolve any PolicyReferences to actual policies.
 * It can optionally be passed as an argument to policy operations (i.e.
 * normalize(...), merge(...) and intersect(...)) and any PolicyReference is
 * resovled to actually Policy within that operations.
 */
public class PolicyRegistry {
	/** Stores {PolicyURI, Polcy} pairs */
	HashMap reg = new HashMap();

	/** To refer to the parent registry */
	PolicyRegistry parent = null;

	public PolicyRegistry() {
	}

	public PolicyRegistry(PolicyRegistry parent) {
		this.parent = parent;
	}

	public void setParent(PolicyRegistry parent) {
		this.parent = parent;
	}

	public PolicyRegistry getParent() {
		return parent;
	}

	/**
	 * Returns the Policy object which is uniquely identified by the policyURI
	 * String. Returns null if there is no Policy object with the specified
	 * policyURI. The policyURI should be the same as the String which returns
	 * when getPolicyURI() is invoked on returning Policy object.
	 * 
	 * @param policyURI
	 *            a String which uniquely indentifies the Policy
	 * @return a Policy object which has the same policyURI
	 * @throws IllegalArgumentException
	 */
	public Policy lookup(String policyURI) throws IllegalArgumentException {

		Policy policy = (Policy) reg.get(policyURI);

		if (policy == null && parent != null) {
			policy = parent.lookup(policyURI);
		}

		return policy;
	}

	/**
	 * Registers a Policy object with the specified policyURI.
	 * 
	 * @param policyURI
	 *            the key which should be the policyURI of the Policy object
	 * @param policy
	 *            the Policy object to register
	 */
	public void register(String policyURI, Policy policy) {
		reg.put(policyURI, policy);
	}

	/**
	 * Unregisters a Policy object specified by the policyURI.
	 * 
	 * @param policyURI
	 *            the policyURI of the Policy object to unregister
	 */
	public void unregister(String policyURI) {
		reg.remove(policyURI);
	}

	public Iterator keys() {
		return reg.keySet().iterator();
	}

	public Iterator values() {
		return reg.values().iterator();
	}
}