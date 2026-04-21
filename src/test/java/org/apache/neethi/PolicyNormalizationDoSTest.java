/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.neethi;

import javax.xml.namespace.QName;

import org.apache.neethi.builders.PrimitiveAssertion;

import org.junit.Test;

/**
 * <p>Policy normalization computes a Cartesian cross-product of the alternatives inside
 * nested {@code All} operators.  Each {@code All} that contains {@code k}
 * {@code ExactlyOne} children, where each child carries 2 alternatives, multiplies the
 * output set by 2.  A policy with {@code N} such levels therefore produces
 * {@code 2^N} normalised alternatives — uncapped.
 *
 * <p>Structure of the crafted policy (one level shown, repeated {@code NESTING_DEPTH} times):
 * <pre>
 * Policy (= All)
 *   ExactlyOne_1       ← 2 branches
 *     All → PrimitiveAssertion(a1)
 *     All → PrimitiveAssertion(b1)
 *   ExactlyOne_2       ← 2 branches
 *     All → PrimitiveAssertion(a2)
 *     All → PrimitiveAssertion(b2)
 *   …
 *   ExactlyOne_N       ← 2 branches
 *     All → PrimitiveAssertion(aN)
 *     All → PrimitiveAssertion(bN)
 * </pre>
 *
 * <p>Normalising this produces {@code 2^N} alternatives. Test that this is handled without
 * throwing {@link OutOfMemoryError} or hanging indefinitely, and that a clear exception is thrown
 * indicating an alternative-count limit was enforced.
 */
public class PolicyNormalizationDoSTest extends PolicyTestCase {

    /**
     * Number of {@code ExactlyOne} nodes placed directly inside the top-level policy.
     * Each adds one level of binary branching, so the cross-product of the normalised
     * policy has {@code 2^NESTING_DEPTH} alternatives.
     *
     * <p>25 levels → 33,554,432 alternatives.  This is deliberately chosen to be far
     * beyond any reasonable cap so that the vulnerability is unambiguous.
     */
    private static final int NESTING_DEPTH = 75;

    /**
     * Builds a {@link Policy} whose normalization produces {@code 2^NESTING_DEPTH}
     * alternatives, then asserts that normalization throws a {@link RuntimeException}
     * whose message indicates an alternative-count limit was enforced.
     *
     */
    @Test(expected = RuntimeException.class)
    public void testExponentialCrossProductIsRejectedWithAlternativeCountLimit()
            throws InterruptedException {
        Policy policy = buildExponentialPolicy(NESTING_DEPTH);

        policy.normalize(registry, true);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Builds a {@link Policy} containing {@code depth} {@link ExactlyOne} nodes.
     * Each {@link ExactlyOne} has exactly two branches ({@link All} nodes), making
     * the normalised policy produce {@code 2^depth} alternatives.
     *
     * <pre>
     * Policy
     *   ExactlyOne_i   (i = 1 … depth)
     *     All → PrimitiveAssertion("a{i}", ns="urn:test")
     *     All → PrimitiveAssertion("b{i}", ns="urn:test")
     * </pre>
     */
    private static Policy buildExponentialPolicy(int depth) {
        Policy policy = new Policy();
        for (int i = 0; i < depth; i++) {
            ExactlyOne eo = new ExactlyOne();

            All branchA = new All();
            branchA.addPolicyComponent(
                new PrimitiveAssertion(new QName("urn:test", "a" + i)));

            All branchB = new All();
            branchB.addPolicyComponent(
                new PrimitiveAssertion(new QName("urn:test", "b" + i)));

            eo.addPolicyComponent(branchA);
            eo.addPolicyComponent(branchB);
            policy.addPolicyComponent(eo);
        }
        return policy;
    }

}
