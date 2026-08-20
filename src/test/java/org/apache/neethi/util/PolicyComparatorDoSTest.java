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
import org.apache.neethi.builders.PrimitiveAssertion;

import org.junit.Test;

/**
 * PolicyComparator's list comparison is unordered nested matching with no
 * memoization: each component of one list scans the other list for a match,
 * so operand orderings engineered to match late cost O(n1 * n2) comparisons
 * — minutes of pinned CPU at the parse budgets. The comparison budget must
 * turn that into a fast, predictable RuntimeException.
 */
public class PolicyComparatorDoSTest extends PolicyTestCase {

    private static final int LARGE = 6000;
    private static final int SMALL = 200;

    @Test
    public void testQuadraticUnorderedMatchingIsRejectedByComparisonBudget() {
        Policy p1 = buildAlternativesPolicy(LARGE, false);
        Policy p2 = buildAlternativesPolicy(LARGE, true);

        try {
            PolicyComparator.compare(p1, p2);
            fail("Expected RuntimeException due to comparison budget");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("component comparisons"));
        }
    }

    @Test
    public void testModerateUnorderedComparisonStillWorks() {
        Policy p1 = buildAlternativesPolicy(SMALL, false);
        Policy p2 = buildAlternativesPolicy(SMALL, true);

        assertTrue(PolicyComparator.compare(p1, p2));
    }

    /**
     * Builds a Policy holding one ExactlyOne of {@code n} single-assertion
     * alternatives. With {@code reversed} set, the alternatives appear in
     * reverse order, so the unordered matcher finds each partner only at the
     * far end of its scan — the quadratic worst case for equal operands.
     */
    private static Policy buildAlternativesPolicy(int n, boolean reversed) {
        Policy policy = new Policy();
        ExactlyOne eo = new ExactlyOne();
        for (int i = 0; i < n; i++) {
            int idx = reversed ? n - 1 - i : i;
            All all = new All();
            all.addPolicyComponent(new PrimitiveAssertion(new QName("urn:test", "a" + idx)));
            eo.addPolicyComponent(all);
        }
        policy.addPolicyComponent(eo);
        return policy;
    }
}
