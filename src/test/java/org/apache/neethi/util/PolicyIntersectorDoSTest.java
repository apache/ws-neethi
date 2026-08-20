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

package org.apache.neethi.util;

import javax.xml.namespace.QName;

import org.apache.neethi.All;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyTestCase;
import org.apache.neethi.builders.PolicyContainingPrimitiveAssertion;
import org.apache.neethi.builders.PrimitiveAssertion;

import org.junit.Test;

/**
 * Intersection output is capped, but the candidate search was not: for
 * PolicyContainingAssertion pairs the intersector recurses into the nested
 * policies with no memoization and no work budget, so same-QName fan-out at
 * every nesting level with incompatible leaves multiplies the failed
 * recursive subtree intersections (~fanout^(2*depth)) while the document only
 * grows linearly. The step budget must convert that into a fast, predictable
 * RuntimeException.
 */
public class PolicyIntersectorDoSTest extends PolicyTestCase {

    private static final int LARGE = 1500;

    @Test
    public void testRecursiveCandidateSearchIsRejectedByStepBudget() {
        Policy p1 = buildLateMatchPolicy(LARGE, false);
        Policy p2 = buildLateMatchPolicy(LARGE, true);

        try {
            new PolicyIntersector(true).intersect(p1, p2, true);
            fail("Expected RuntimeException due to intersection step budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("intersection steps"));
        }
    }

    @Test
    public void testCompatiblePoliciesIsAlsoBudgeted() {
        Policy p1 = buildLateMatchPolicy(LARGE, false);
        Policy p2 = buildLateMatchPolicy(LARGE, true);

        try {
            new PolicyIntersector(true).compatiblePolicies(p1, p2);
            fail("Expected RuntimeException due to intersection step budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("intersection steps"));
        }
    }

    @Test
    public void testSmallNestedIntersectionStillWorks() {
        Policy p1 = buildLateMatchPolicy(20, false);
        Policy p2 = buildLateMatchPolicy(20, true);

        assertTrue(new PolicyIntersector(true).compatiblePolicies(p1, p2));
        assertNotNull(new PolicyIntersector(true).intersect(p1, p2, true));
    }

    /**
     * Builds one alternative containing {@code size} same-QName
     * policy-containing assertions. The nested policy of each assertion has a
     * unique leaf name. Reversing order forces late matches in unordered scan,
     * so candidate search performs near-quadratic pair attempts and each
     * attempt recurses into nested-policy compatibility.
     */
    private static Policy buildLateMatchPolicy(int size, boolean reversed) {
        Policy policy = new Policy();
        ExactlyOne eo = new ExactlyOne();
        All all = new All();

        for (int i = 0; i < size; i++) {
            int idx = reversed ? size - 1 - i : i;
            Policy nested = new Policy();
            nested.addPolicyComponent(new PrimitiveAssertion(new QName("urn:test", "leaf" + idx)));
            all.addPolicyComponent(new PolicyContainingPrimitiveAssertion(
                new QName("urn:test", "n"),
                false, false,
                nested));
        }

        eo.addPolicyComponent(all);
        policy.addPolicyComponent(eo);
        return policy;
    }
}
