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

import java.util.List;

import org.apache.ws.policy.util.PolicyRegistry;

/**
 * Assertion is an interface which all constructs of policy must implement. It
 * defines three policy operations that all policy constructs must support.
 * 
 * Sanka Samaranayake (sanka@apache.org)
 */

public interface Assertion {
	/** Defines the short value for Primitive assertions */
	public static final short PRIMITIVE = 0x1;

	/** Defines the short value for All assertion */
	public static final short ALL = 0x2;

	/** Defines the short value for ExactlyOne assertion */
	public static final short EXACTLY_ONE = 0x3;

	/** Defines the short value for Policy Assertion */
	public static final short POLICY = 0x4;

	/** Defines the short value for PolicyReferece Assertion */
	public static final short POLICY_REF = 0x5;

	/**
	 * Returns a new assertion which is the normalized form of this assertion.
	 * 
	 * @return an assertion which is normalized form of this.
	 */
	public Assertion normalize() throws UnsupportedOperationException;

	/**
	 * Returns a new assertion which is the normalized form of this assertion.
	 * 
	 * @param reg
	 *            the registry which is used to resolve any policy references in
	 *            the process of normalization.
	 * @return
	 * @throws UnsupportedOperationException
	 *             if an assertion does not support this operation.
	 */
	public Assertion normalize(PolicyRegistry reg)
			throws UnsupportedOperationException;

	/**
	 * Returns an assertion which is the equivalent of intersect of self and
	 * argument. The rules to construct the equivalent assertion are specified
	 * in WS Policy 1.0 specification.
	 * 
	 * @param assertion
	 *            the assertion to intersect with
	 * @return the equivalent of intersect of self and the argument
	 */
	public Assertion intersect(Assertion assertion)
			throws UnsupportedOperationException;

	/**
	 * Returns an assertion which is equivalent of intersect of self and
	 * argument. Here the external policy are resolved via a policy registry
	 * that is supplied as an argument.
	 * 
	 * @param assertion
	 *            the assertion to intersect with
	 * @param cache
	 *            the policy registry which is used to resolve external policy
	 *            references
	 * @return the equivalent of intersection of self and argument
	 * @throws UnsupportedOperationException
	 *             if the operation is not meaningful
	 */
	public Assertion intersect(Assertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException;

	/**
	 * Returns the equivalent of merge of self and argument. The rules to
	 * construct the equivalent of merge are defined in WS Policy specification
	 * 1.0
	 * 
	 * @param assertion
	 *            the argument to merge with
	 * @return the equivalent of the merge of self and argument
	 */
	public Assertion merge(Assertion assertion)
			throws UnsupportedOperationException;

	/**
	 * Returns the equivalent of merge of self and argument. The rules to
	 * construct argument are specified in WS Policy specification 1.0 Here the
	 * external policy references are resolved via a policy registry that is
	 * supplied as an argument
	 * 
	 * @param assertion
	 *            the assertion to merge with
	 * @param reg
	 *            the policy registry that should be used to resolve external
	 *            policy references
	 * @return the equivalent of merge of self and argument
	 * @throws UnsupportedOperationException
	 *             if the merge is not meaningful
	 */
	public Assertion merge(Assertion assertion, PolicyRegistry reg)
			throws UnsupportedOperationException;

	/**
	 * Returns true if the assertion is in normalized form.
	 * 
	 * @return true if the assertion is in normalized form.
	 */
	public boolean isNormalized();

	/**
	 * Marks this assertion as in normalized form.
	 * 
	 * @param flag
	 */
	public void setNormalized(boolean flag);

	/**
	 * Returns ture if the assertion has a parent
	 * 
	 * @return true if a parent exists , false otherwise
	 */
	public boolean hasParent();

	/**
	 * Returns the parent of self or null if a parent non-exists
	 * 
	 * @return the parent of self
	 */
	public Assertion getParent();

	/**
	 * Sets the parent to argument
	 * 
	 * @param parent
	 *            the parent that should be parent of self
	 */
	public void setParent(Assertion parent);

	/**
	 * Adds an assertion as a term of this assertion.
	 * 
	 * @param assertion
	 *            the assertion to add as a term
	 */
	public void addTerm(Assertion assertion);

	/**
	 * Adds a list of assertions as terms of this assertion.
	 * 
	 * @param assertions
	 *            the list of assertions to add as terms
	 */
	public void addTerms(List assertions);

	/**
	 * Returns the list of terms of this assertion.
	 * 
	 * @return list of terms
	 */
	public List getTerms();

	/**
	 * Returns true if there are no terms in this assertion.
	 * 
	 * @return true if there are no terms.
	 */
	public boolean isEmpty();

	/**
	 * Removes the specified assertion from the terms list.
	 * 
	 * @param assertion
	 *            the assertion to remove from the terms list.
	 * @return true if it is removed from the child list.
	 */
	public boolean remove(Assertion assertion);

	/**
	 * Returns the number of terms of this assertion.
	 * 
	 * @return the no of terms of this assertion.
	 */
	public int size();

	/**
	 * Returns a short type which describes the type of the assertion.
	 * 
	 * @return a short value of one of following values:
	 * 
	 * PRIMITIVE_TYPE : if the assertion is a PrimitiveAssertion
	 * COMPOSITE_AND_TYPE : if the assertion is a AndCompositeAssertion
	 * COMPOSITE_XOR_TYPE : if the assertion is a XorCompositeAssertion
	 * COMPOSITE_POLICY_TYPE : if the assertion is a Policy
	 * POLICY_REFERENCE_TYPE : if the assertion is a PolicyReferece
	 */
	public short getType();

	/**
	 * Returns a string which describes the source of the assertion. For
	 * instance name of the policy file that contains the assertion. Returns
	 * null if it is not set.
	 * 
	 * @return a string that describe the origin of the assertion.
	 */
	public String getSource();

	/**
	 * Sets specified string as the source of the assertion.
	 * 
	 * @param source
	 *            the string which describe the source of the origin of the
	 *            assertion
	 */
	public void setSource(String source);

	/**
	 * Returns the line no of the assertion which is line no of the assertion in
	 * the file where the assertion is first read. came from. Returns -1 if it
	 * is not set.
	 * 
	 * @return the line no of the assertion in file where the assertion is first
	 *         read.
	 */
	public int getLineNo();

	/**
	 * Sets the line no of the assertion which should be the line no of the
	 * assertion in the file where the assertion is first read.
	 * 
	 * @param lineNo
	 */
	public void setLineNo(int lineNo);

}