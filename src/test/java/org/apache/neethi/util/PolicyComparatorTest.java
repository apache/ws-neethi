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
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyTestCase;
import org.apache.neethi.builders.PrimitiveAssertion;

import org.junit.Test;

public class PolicyComparatorTest extends PolicyTestCase {

    // --- Policy-level comparisons ---

    @Test
    public void testPolicyComparison_differentNames() {
        Policy policy1 = new Policy();
        policy1.setName("Name_1");
        Policy policy2 = new Policy();
        policy2.setName("Name_2");

        assertFalse(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_sameNames() {
        Policy policy1 = new Policy();
        policy1.setName("SameName");
        Policy policy2 = new Policy();
        policy2.setName("SameName");

        assertTrue(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_bothNullName() {
        // Neither policy has a Name → names equal; no Id either → equal
        Policy policy1 = new Policy();
        Policy policy2 = new Policy();

        assertTrue(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_oneNullOneName() {
        Policy policy1 = new Policy();
        policy1.setName("SomeName");
        Policy policy2 = new Policy();

        assertFalse(PolicyComparator.compare(policy1, policy2));
        assertFalse(PolicyComparator.compare(policy2, policy1));
    }

    @Test
    public void testPolicyComparison_differentIds() {
        Policy policy1 = new Policy();
        policy1.setId("id-1");
        Policy policy2 = new Policy();
        policy2.setId("id-2");

        assertFalse(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_sameIds() {
        Policy policy1 = new Policy();
        policy1.setId("shared-id");
        Policy policy2 = new Policy();
        policy2.setId("shared-id");

        assertTrue(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_oneNullId() {
        Policy policy1 = new Policy();
        policy1.setId("some-id");
        Policy policy2 = new Policy();

        assertFalse(PolicyComparator.compare(policy1, policy2));
        assertFalse(PolicyComparator.compare(policy2, policy1));
    }

    @Test
    public void testPolicyComparison_differentNamespaces() {
        Policy policy1 = new Policy(null, "http://ns1.example.com");
        Policy policy2 = new Policy(null, "http://ns2.example.com");

        assertFalse(PolicyComparator.compare(policy1, policy2));
    }

    @Test
    public void testPolicyComparison_sameNamespaceAndName() {
        Policy policy1 = new Policy(null, "http://example.com/ns");
        policy1.setName("Pol");
        Policy policy2 = new Policy(null, "http://example.com/ns");
        policy2.setName("Pol");

        assertTrue(PolicyComparator.compare(policy1, policy2));
    }

    // --- All comparisons ---

    @Test
    public void testCompareAll_bothEmpty() {
        assertTrue(PolicyComparator.compare((PolicyComponent) new All(), new All()));
    }

    @Test
    public void testCompareAll_withMatchingComponents() {
        QName qn = new QName("http://example.com", "Foo");
        All a1 = new All();
        a1.addPolicyComponent(new PrimitiveAssertion(qn));
        All a2 = new All();
        a2.addPolicyComponent(new PrimitiveAssertion(qn));

        assertTrue(PolicyComparator.compare((PolicyComponent) a1, a2));
    }

    @Test
    public void testCompareAll_differentComponents() {
        All a1 = new All();
        a1.addPolicyComponent(new PrimitiveAssertion(new QName("http://ex.com", "A")));
        All a2 = new All();
        a2.addPolicyComponent(new PrimitiveAssertion(new QName("http://ex.com", "B")));

        assertFalse(PolicyComparator.compare((PolicyComponent) a1, a2));
    }

    @Test
    public void testCompareAll_differentSizes() {
        QName qn = new QName("http://ex.com", "X");
        All a1 = new All();
        a1.addPolicyComponent(new PrimitiveAssertion(qn));
        All a2 = new All();

        assertFalse(PolicyComparator.compare((PolicyComponent) a1, a2));
    }

    // --- ExactlyOne comparisons ---

    @Test
    public void testCompareExactlyOne_bothEmpty() {
        assertTrue(PolicyComparator.compare((PolicyComponent) new ExactlyOne(), new ExactlyOne()));
    }

    @Test
    public void testCompareExactlyOne_withMatchingAll() {
        ExactlyOne e1 = new ExactlyOne();
        e1.addPolicyComponent(new All());
        ExactlyOne e2 = new ExactlyOne();
        e2.addPolicyComponent(new All());

        assertTrue(PolicyComparator.compare((PolicyComponent) e1, e2));
    }

    // --- Assertion comparisons ---

    @Test
    public void testCompareAssertions_sameName() {
        QName qn = new QName("http://ex.com", "MyAssertion");
        PrimitiveAssertion a1 = new PrimitiveAssertion(qn);
        PrimitiveAssertion a2 = new PrimitiveAssertion(qn);

        assertTrue(PolicyComparator.compare((PolicyComponent) a1, a2));
    }

    @Test
    public void testCompareAssertions_differentName() {
        PrimitiveAssertion a1 = new PrimitiveAssertion(new QName("http://ex.com", "Foo"));
        PrimitiveAssertion a2 = new PrimitiveAssertion(new QName("http://ex.com", "Bar"));

        assertFalse(PolicyComparator.compare((PolicyComponent) a1, a2));
    }

    // --- Mixed type comparisons ---

    @Test
    public void testCompare_differentTypes() {
        // All vs ExactlyOne — getClass() differs → false
        assertFalse(PolicyComparator.compare((PolicyComponent) new All(), new ExactlyOne()));
    }
}