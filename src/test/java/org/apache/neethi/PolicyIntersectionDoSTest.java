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

import org.apache.neethi.builders.PolicyContainingPrimitiveAssertion;
import org.apache.neethi.builders.PrimitiveAssertion;
import org.apache.neethi.util.PolicyIntersector;
import org.junit.Test;

public class PolicyIntersectionDoSTest extends PolicyTestCase {

    private static final int ALTERNATIVES_PER_POLICY = 101;
    private static final int SAFE_ALTERNATIVES_PER_POLICY = 100;
    private static final int LARGE_CANDIDATE_SET = 1500;

    @Test
    public void testIntersectionRejectsCartesianProductBeyondAlternativeBudget() {
        Policy left = buildPolicyWithEmptyAlternatives(ALTERNATIVES_PER_POLICY);
        Policy right = buildPolicyWithEmptyAlternatives(ALTERNATIVES_PER_POLICY);

        try {
            left.intersect(right, true);
            fail("Expected RuntimeException due to intersection alternative limit");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("intersection"));
            assertTrue(ex.getMessage().contains("maximum number of alternatives"));
        }
    }

    @Test
    public void testIntersectionAllowsCartesianProductAtAlternativeBudget() {
        Policy left = buildPolicyWithEmptyAlternatives(SAFE_ALTERNATIVES_PER_POLICY);
        Policy right = buildPolicyWithEmptyAlternatives(SAFE_ALTERNATIVES_PER_POLICY);

        Policy intersection = left.intersect(right, true);

        assertNotNull(intersection);
        assertEquals(
            SAFE_ALTERNATIVES_PER_POLICY * SAFE_ALTERNATIVES_PER_POLICY,
            ((ExactlyOne)intersection.getFirstPolicyComponent()).getPolicyComponents().size());
    }

    @Test
    public void testIntersectionWithNoCompatibleAlternativesReturnsEmptyPolicy() {
        Policy left = buildSingleAssertionPolicy("left");
        Policy right = buildSingleAssertionPolicy("right");

        Policy intersection = left.intersect(right, true);

        assertNotNull(intersection);
        assertTrue(intersection.getFirstPolicyComponent() instanceof ExactlyOne);
        assertTrue(((ExactlyOne)intersection.getFirstPolicyComponent()).getPolicyComponents().isEmpty());
    }

    @Test
    public void testRecursiveCandidateSearchRequiresIntersectionStepBudget() {
        Policy left = buildLateMatchNestedPolicy(LARGE_CANDIDATE_SET, false);
        Policy right = buildLateMatchNestedPolicy(LARGE_CANDIDATE_SET, true);

        try {
            new PolicyIntersector(true).intersect(left, right, true);
            fail("Expected RuntimeException due to intersection step budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("intersection steps"));
        }
    }

    private static Policy buildPolicyWithEmptyAlternatives(int alternatives) {
        Policy policy = new Policy();
        ExactlyOne exactlyOne = new ExactlyOne();

        for (int i = 0; i < alternatives; i++) {
            exactlyOne.addPolicyComponent(new All());
        }

        policy.addPolicyComponent(exactlyOne);
        return policy;
    }

    private static Policy buildSingleAssertionPolicy(String localName) {
        Policy policy = new Policy();
        ExactlyOne exactlyOne = new ExactlyOne();
        All all = new All();

        all.addPolicyComponent(new PrimitiveAssertion(new QName("urn:test", localName)));
        exactlyOne.addPolicyComponent(all);
        policy.addPolicyComponent(exactlyOne);

        return policy;
    }

    /**
     * Builds one policy alternative containing many same-QName
     * policy-containing assertions. The nested policy leaf names are unique;
     * reversing order on the second operand forces late unordered matches and
     * maximizes recursive candidate search work.
     */
    private static Policy buildLateMatchNestedPolicy(int size, boolean reversed) {
        Policy policy = new Policy();
        ExactlyOne exactlyOne = new ExactlyOne();
        All all = new All();

        for (int i = 0; i < size; i++) {
            int idx = reversed ? size - 1 - i : i;
            Policy nested = new Policy();
            nested.addPolicyComponent(new PrimitiveAssertion(new QName("urn:test", "leaf" + idx)));

            all.addPolicyComponent(new PolicyContainingPrimitiveAssertion(
                new QName("urn:test", "n"),
                false,
                false,
                nested));
        }

        exactlyOne.addPolicyComponent(all);
        policy.addPolicyComponent(exactlyOne);
        return policy;
    }
}