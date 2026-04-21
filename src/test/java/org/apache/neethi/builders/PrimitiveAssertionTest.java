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

package org.apache.neethi.builders;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.neethi.Constants;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyTestCase;

import org.junit.Test;

/**
 * Unit tests for {@link PrimitiveAssertion}.
 */
public class PrimitiveAssertionTest extends PolicyTestCase {

    private static final QName FOO = new QName("http://example.com", "Foo");
    private static final QName BAR = new QName("http://example.com", "Bar");
    private static final QName ATTR = new QName("http://example.com", "myAttr");

    // =========================================================
    // Constructors
    // =========================================================

    @Test
    public void testDefaultConstructor_nullName() {
        PrimitiveAssertion pa = new PrimitiveAssertion();
        assertNull(pa.getName());
    }

    @Test
    public void testQNameConstructor_nameSet() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        assertEquals(FOO, pa.getName());
    }

    @Test
    public void testOptionalConstructor_optionalSet() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO, true);
        assertTrue(pa.isOptional());
    }

    @Test
    public void testIgnorableConstructor_ignorableSet() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO, false, true);
        assertTrue(pa.isIgnorable());
    }

    @Test
    public void testFullConstructor_attributesAndText() {
        Map<QName, String> atts = new HashMap<QName, String>();
        atts.put(ATTR, "value");

        PrimitiveAssertion pa = new PrimitiveAssertion(FOO, false, false, atts, "text");

        assertEquals("value", pa.getAttribute(ATTR));
        assertEquals("text", pa.getTextValue());
    }

    // =========================================================
    // Defaults
    // =========================================================

    @Test
    public void testOptional_falseByDefault() {
        assertFalse(new PrimitiveAssertion(FOO).isOptional());
    }

    @Test
    public void testIgnorable_falseByDefault() {
        assertFalse(new PrimitiveAssertion(FOO).isIgnorable());
    }

    @Test
    public void testTextValue_nullByDefault() {
        assertNull(new PrimitiveAssertion(FOO).getTextValue());
    }

    @Test
    public void testGetAttributes_emptyMapByDefault() {
        assertTrue(new PrimitiveAssertion(FOO).getAttributes().isEmpty());
    }

    @Test
    public void testGetAttribute_absentReturnsNull() {
        assertNull(new PrimitiveAssertion(FOO).getAttribute(ATTR));
    }

    // =========================================================
    // Setters
    // =========================================================

    @Test
    public void testSetOptional_roundtrip() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.setOptional(true);
        assertTrue(pa.isOptional());
        pa.setOptional(false);
        assertFalse(pa.isOptional());
    }

    @Test
    public void testSetIgnorable_roundtrip() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.setIgnorable(true);
        assertTrue(pa.isIgnorable());
    }

    @Test
    public void testSetTextValue_roundtrip() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.setTextValue("hello");
        assertEquals("hello", pa.getTextValue());
    }

    @Test
    public void testSetName_roundtrip() {
        PrimitiveAssertion pa = new PrimitiveAssertion();
        pa.setName(FOO);
        assertEquals(FOO, pa.getName());
    }

    // =========================================================
    // addAttribute / addAttributes / getAttribute
    // =========================================================

    @Test
    public void testAddAttribute_roundtrip() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.addAttribute(ATTR, "42");
        assertEquals("42", pa.getAttribute(ATTR));
    }

    @Test
    public void testAddAttributes_mergesAll() {
        Map<QName, String> atts = new HashMap<QName, String>();
        QName k1 = new QName("http://ex.com", "k1");
        QName k2 = new QName("http://ex.com", "k2");
        atts.put(k1, "v1");
        atts.put(k2, "v2");

        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.addAttributes(atts);

        assertEquals("v1", pa.getAttribute(k1));
        assertEquals("v2", pa.getAttribute(k2));
    }

    @Test
    public void testGetAttributes_returnsCopy() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        pa.addAttribute(ATTR, "original");

        Map<QName, String> copy = pa.getAttributes();
        copy.put(ATTR, "mutated");

        // The internal map must not be affected
        assertEquals("original", pa.getAttribute(ATTR));
    }

    // =========================================================
    // getType
    // =========================================================

    @Test
    public void testGetType() {
        assertEquals(Constants.TYPE_ASSERTION, new PrimitiveAssertion(FOO).getType());
    }

    // =========================================================
    // equals / hashCode
    // =========================================================

    @Test
    public void testEquals_reflexive() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        assertEquals(pa, pa);
    }

    @Test
    public void testEquals_symmetric() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        PrimitiveAssertion b = new PrimitiveAssertion(FOO);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    public void testEquals_differentName() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        PrimitiveAssertion b = new PrimitiveAssertion(BAR);
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentOptional() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO, true);
        PrimitiveAssertion b = new PrimitiveAssertion(FOO, false);
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentIgnorable() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO, false, true);
        PrimitiveAssertion b = new PrimitiveAssertion(FOO, false, false);
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentTextValue() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO, false, false, null, "text1");
        PrimitiveAssertion b = new PrimitiveAssertion(FOO, false, false, null, "text2");
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentAttributes() {
        Map<QName, String> atts1 = new HashMap<QName, String>();
        atts1.put(ATTR, "A");
        Map<QName, String> atts2 = new HashMap<QName, String>();
        atts2.put(ATTR, "B");

        PrimitiveAssertion a = new PrimitiveAssertion(FOO, false, false, atts1);
        PrimitiveAssertion b = new PrimitiveAssertion(FOO, false, false, atts2);
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_notPrimitiveAssertion() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        assertFalse(pa.equals("not an assertion"));
        assertFalse(pa.equals(null));
    }

    @Test
    public void testHashCode_equalObjectsSameHash() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO, false, false, null, "v");
        PrimitiveAssertion b = new PrimitiveAssertion(FOO, false, false, null, "v");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCode_consistent() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        int h1 = pa.hashCode();
        int h2 = pa.hashCode();
        assertEquals(h1, h2);
    }

    // =========================================================
    // equal(PolicyComponent) — the neethi-specific method
    // =========================================================

    @Test
    public void testEqualPolicyComponent_sameAssertion() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        PrimitiveAssertion b = new PrimitiveAssertion(FOO);
        assertTrue(a.equal(b));
    }

    @Test
    public void testEqualPolicyComponent_differentAssertion() {
        PrimitiveAssertion a = new PrimitiveAssertion(FOO);
        PrimitiveAssertion b = new PrimitiveAssertion(BAR);
        assertFalse(a.equal(b));
    }

    @Test
    public void testEqualPolicyComponent_nonAssertion() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        assertFalse(pa.equal(new ExactlyOne()));
    }

    @Test
    public void testEqualPolicyComponent_reflexive() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        assertTrue(pa.equal(pa));
    }

    // =========================================================
    // normalize()
    // =========================================================

    @Test
    public void testNormalize_nonOptional_returnsSelf() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO, false);
        PolicyComponent normalized = pa.normalize();

        // non-optional assertion normalizes to a clone of itself (TYPE_ASSERTION)
        assertEquals(Constants.TYPE_ASSERTION, normalized.getType());
    }

    @Test
    public void testNormalize_optional_returnsPolicy() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO, true);
        PolicyComponent normalized = pa.normalize();

        // optional assertion normalizes to: Policy > ExactlyOne > [All>Assertion, All]
        assertEquals(Constants.TYPE_POLICY, normalized.getType());
        Policy p = (Policy) normalized;
        assertEquals(1, p.getPolicyComponents().size());
        assertTrue(p.getPolicyComponents().get(0) instanceof ExactlyOne);

        ExactlyOne eo = (ExactlyOne) p.getPolicyComponents().get(0);
        assertEquals(2, eo.getPolicyComponents().size()); // two alternatives
    }

    // =========================================================
    // toString
    // =========================================================

    @Test
    public void testToString_containsName() {
        PrimitiveAssertion pa = new PrimitiveAssertion(FOO);
        String str = pa.toString();
        assertNotNull(str);
        assertTrue(str.contains("Foo"));
    }
}
