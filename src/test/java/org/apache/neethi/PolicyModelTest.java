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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.neethi.builders.PrimitiveAssertion;

import org.junit.Test;

/**
 * Unit tests for {@link All}, {@link ExactlyOne}, {@link Policy},
 * and the shared behaviour in {@link AbstractPolicyOperator}.
 */
public class PolicyModelTest extends PolicyTestCase {

    private static final QName FOO = new QName("http://example.com", "Foo");
    private static final QName BAR = new QName("http://example.com", "Bar");

    // =========================================================
    // All
    // =========================================================

    @Test
    public void testAll_getType() {
        assertEquals(Constants.TYPE_ALL, new All().getType());
    }

    @Test
    public void testAll_newIsEmpty() {
        assertTrue(new All().isEmpty());
    }

    @Test
    public void testAll_addComponent_notEmpty() {
        All all = new All();
        all.addPolicyComponent(new PrimitiveAssertion(FOO));
        assertFalse(all.isEmpty());
    }

    @Test
    public void testAll_addAssertion_appearsInAssertions() {
        All all = new All();
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        all.addAssertion(a);

        List<PolicyComponent> assertions = all.getAssertions();
        assertEquals(1, assertions.size());
        assertSame(a, assertions.get(0));
    }

    @Test
    public void testAll_getPolicyComponents_returnsSameList() {
        All all = new All();
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        all.addPolicyComponent(a);

        assertEquals(1, all.getPolicyComponents().size());
        assertSame(a, all.getPolicyComponents().get(0));
    }

    @Test
    public void testAll_addMultipleComponents() {
        All all = new All();
        all.addPolicyComponent(new PrimitiveAssertion(FOO));
        all.addPolicyComponent(new PrimitiveAssertion(BAR));

        assertEquals(2, all.getPolicyComponents().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAll_addNullComponent_throwsIllegalArgument() {
        new All().addPolicyComponent(null);
    }

    @Test
    public void testAll_getFirstPolicyComponent_emptyReturnsNull() {
        assertNull(new All().getFirstPolicyComponent());
    }

    @Test
    public void testAll_getFirstPolicyComponent_returnsFirst() {
        All all = new All();
        PrimitiveAssertion first = new PrimitiveAssertion(FOO);
        PrimitiveAssertion second = new PrimitiveAssertion(BAR);
        all.addPolicyComponent(first);
        all.addPolicyComponent(second);

        assertSame(first, all.getFirstPolicyComponent());
    }

    @Test
    public void testAll_addPolicyComponents_addsAll() {
        All source = new All();
        source.addPolicyComponent(new PrimitiveAssertion(FOO));
        source.addPolicyComponent(new PrimitiveAssertion(BAR));

        All dest = new All();
        dest.addPolicyComponents(source.getPolicyComponents());

        assertEquals(2, dest.getPolicyComponents().size());
    }

    @Test
    public void testAll_equal_sameContent() {
        All a1 = new All();
        a1.addPolicyComponent(new PrimitiveAssertion(FOO));
        All a2 = new All();
        a2.addPolicyComponent(new PrimitiveAssertion(FOO));

        assertTrue(a1.equal(a2));
    }

    @Test
    public void testAll_equal_differentContent() {
        All a1 = new All();
        a1.addPolicyComponent(new PrimitiveAssertion(FOO));
        All a2 = new All();
        a2.addPolicyComponent(new PrimitiveAssertion(BAR));

        assertFalse(a1.equal(a2));
    }

    @Test
    public void testAll_constructorWithParent_addsItselfToParent() {
        ExactlyOne parent = new ExactlyOne();
        All child = new All(parent);

        assertEquals(1, parent.getPolicyComponents().size());
        assertSame(child, parent.getPolicyComponents().get(0));
    }

    // =========================================================
    // ExactlyOne
    // =========================================================

    @Test
    public void testExactlyOne_getType() {
        assertEquals(Constants.TYPE_EXACTLYONE, new ExactlyOne().getType());
    }

    @Test
    public void testExactlyOne_newIsEmpty() {
        assertTrue(new ExactlyOne().isEmpty());
    }

    @Test
    public void testExactlyOne_addComponent_notEmpty() {
        ExactlyOne eo = new ExactlyOne();
        eo.addPolicyComponent(new All());
        assertFalse(eo.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExactlyOne_addNullComponent_throwsIllegalArgument() {
        new ExactlyOne().addPolicyComponent(null);
    }

    @Test
    public void testExactlyOne_constructorWithParent_addsItselfToParent() {
        Policy parent = new Policy();
        ExactlyOne child = new ExactlyOne(parent);

        assertEquals(1, parent.getPolicyComponents().size());
        assertSame(child, parent.getPolicyComponents().get(0));
    }

    // =========================================================
    // Policy
    // =========================================================

    @Test
    public void testPolicy_getType() {
        assertEquals(Constants.TYPE_POLICY, new Policy().getType());
    }

    @Test
    public void testPolicy_defaultConstructor_noNameNoId() {
        Policy p = new Policy();
        assertNull(p.getName());
        assertNull(p.getId());
        assertNull(p.getNamespace());
    }

    @Test
    public void testPolicy_setGetName() {
        Policy p = new Policy();
        p.setName("MyPolicy");
        assertEquals("MyPolicy", p.getName());
    }

    @Test
    public void testPolicy_setGetId() {
        Policy p = new Policy();
        p.setId("policy-id-1");
        assertEquals("policy-id-1", p.getId());
    }

    @Test
    public void testPolicy_getNamespace() {
        Policy p = new Policy(null, "http://my.ns");
        assertEquals("http://my.ns", p.getNamespace());
    }

    @Test
    public void testPolicy_addAndGetAttribute() {
        Policy p = new Policy();
        QName key = new QName("http://example.com", "customAttr");
        p.addAttribute(key, "attrValue");

        assertEquals("attrValue", p.getAttribute(key));
    }

    @Test
    public void testPolicy_getAttribute_absentKeyReturnsNull() {
        Policy p = new Policy();
        assertNull(p.getAttribute(new QName("http://example.com", "missing")));
    }

    @Test
    public void testPolicy_getAttributes_containsAll() {
        Policy p = new Policy();
        QName k1 = new QName("", "k1");
        QName k2 = new QName("", "k2");
        p.addAttribute(k1, "v1");
        p.addAttribute(k2, "v2");

        assertEquals("v1", p.getAttributes().get(k1));
        assertEquals("v2", p.getAttributes().get(k2));
    }

    @Test
    public void testPolicy_registryRoundtrip() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        Policy p = new Policy(reg);
        assertSame(reg, p.getPolicyRegistry());
    }

    @Test
    public void testPolicy_setPolicyRegistry() {
        Policy p = new Policy();
        assertNull(p.getPolicyRegistry());

        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        p.setPolicyRegistry(reg);
        assertSame(reg, p.getPolicyRegistry());
    }

    // =========================================================
    // Policy.merge
    // =========================================================

    @Test
    public void testPolicy_merge_componentCounts() {
        Policy p1 = new Policy();
        p1.addPolicyComponent(new PrimitiveAssertion(FOO));

        Policy p2 = new Policy();
        p2.addPolicyComponent(new PrimitiveAssertion(BAR));

        Policy merged = p1.merge(p2);

        // merged has components from both p1 and p2
        assertEquals(2, merged.getPolicyComponents().size());
    }

    @Test
    public void testPolicy_merge_doesNotMutateOriginals() {
        Policy p1 = new Policy();
        p1.addPolicyComponent(new PrimitiveAssertion(FOO));

        Policy p2 = new Policy();
        p2.addPolicyComponent(new PrimitiveAssertion(BAR));

        p1.merge(p2);

        assertEquals(1, p1.getPolicyComponents().size());
        assertEquals(1, p2.getPolicyComponents().size());
    }

    // =========================================================
    // Policy.normalize (programmatic, no XML files)
    // =========================================================

    @Test
    public void testPolicy_normalizeEmpty_resultsInEmptyAlternatives() {
        // An empty Policy normalizes to ExactlyOne containing one empty All
        Policy empty = new Policy();
        Policy normalized = empty.normalize(null, true);

        assertNotNull(normalized);
        // normalized form: ExactlyOne > [one empty All]
        List<PolicyComponent> top = normalized.getPolicyComponents();
        assertEquals(1, top.size());
        assertTrue(top.get(0) instanceof ExactlyOne);

        ExactlyOne eo = (ExactlyOne) top.get(0);
        assertEquals(1, eo.getPolicyComponents().size());
        assertTrue(eo.getPolicyComponents().get(0) instanceof All);
        assertTrue(((All) eo.getPolicyComponents().get(0)).isEmpty());
    }

    @Test
    public void testPolicy_normalizeSingleAssertion() {
        // Policy > ExactlyOne > All > Assertion(FOO)
        PrimitiveAssertion assertion = new PrimitiveAssertion(FOO);
        All all = new All();
        all.addPolicyComponent(assertion);
        ExactlyOne eo = new ExactlyOne();
        eo.addPolicyComponent(all);
        Policy p = new Policy();
        p.addPolicyComponent(eo);

        Policy normalized = p.normalize(null, true);

        // The top level should still be ExactlyOne
        assertEquals(1, normalized.getPolicyComponents().size());
        assertTrue(normalized.getPolicyComponents().get(0) instanceof ExactlyOne);
    }

    @Test
    public void testPolicy_normalize_resolvesRegistryRef() {
        // Policy A has PolicyReference("B"). Policy B has assertion(FOO).
        // Registry maps "B" → policyB.
        PolicyRegistryImpl reg = new PolicyRegistryImpl();

        PrimitiveAssertion foo = new PrimitiveAssertion(FOO);
        All allB = new All();
        allB.addPolicyComponent(foo);
        ExactlyOne eoB = new ExactlyOne();
        eoB.addPolicyComponent(allB);
        Policy policyB = new Policy(reg);
        policyB.setId("B");
        policyB.addPolicyComponent(eoB);
        reg.register("B", policyB);

        PolicyReference refToB = new PolicyReference();
        refToB.setURI("B");
        All allA = new All();
        allA.addPolicyComponent(refToB);
        ExactlyOne eoA = new ExactlyOne();
        eoA.addPolicyComponent(allA);
        Policy policyA = new Policy(reg);
        policyA.addPolicyComponent(eoA);

        Policy normalized = policyA.normalize(reg, true);

        assertNotNull(normalized);
        // Should have resolved without throwing
        assertFalse(normalized.getPolicyComponents().isEmpty());
    }

    @Test
    public void testPolicy_normalize_unresolvableRef_throws() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();

        PolicyReference ref = new PolicyReference();
        ref.setURI("NoSuchPolicy");
        All all = new All();
        all.addPolicyComponent(ref);
        ExactlyOne eo = new ExactlyOne();
        eo.addPolicyComponent(all);
        Policy p = new Policy(reg);
        p.addPolicyComponent(eo);

        try {
            p.normalize(reg, true);
            fail("Expected RuntimeException for unresolvable PolicyReference");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("can't be resolved"));
        }
    }

    @Test
    public void testPolicy_normalize_preservesNameAndId() {
        Policy p = new Policy();
        p.setName("MyPol");
        p.setId("my-id");

        Policy normalized = p.normalize(null, false);

        assertEquals("MyPol", normalized.getName());
        assertEquals("my-id", normalized.getId());
    }

    // =========================================================
    // PolicyReference
    // =========================================================

    @Test
    public void testPolicyReference_getType() {
        assertEquals(Constants.TYPE_POLICY_REF, new PolicyReference().getType());
    }

    @Test
    public void testPolicyReference_setGetURI() {
        PolicyReference ref = new PolicyReference();
        ref.setURI("#MyPolicy");
        assertEquals("#MyPolicy", ref.getURI());
    }

    @Test
    public void testPolicyReference_equal_sameURI() {
        PolicyReference r1 = new PolicyReference();
        r1.setURI("#SomePol");
        PolicyReference r2 = new PolicyReference();
        r2.setURI("#SomePol");

        assertTrue(r1.equal(r2));
    }

    @Test
    public void testPolicyReference_equal_differentURI() {
        PolicyReference r1 = new PolicyReference();
        r1.setURI("#Pol1");
        PolicyReference r2 = new PolicyReference();
        r2.setURI("#Pol2");

        assertFalse(r1.equal(r2));
    }

    @Test
    public void testPolicyReference_equal_wrongType() {
        PolicyReference ref = new PolicyReference();
        ref.setURI("#Pol");

        assertFalse(ref.equal(new PrimitiveAssertion(FOO)));
    }
}
