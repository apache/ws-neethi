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

import org.junit.Test;

/**
 * Unit tests for {@link PolicyRegistryImpl}.
 */
public class PolicyRegistryImplTest extends PolicyTestCase {

    // --- register / lookup ---

    @Test
    public void testLookupMissingKey_returnsNull() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        assertNull(reg.lookup("nonExistent"));
    }

    @Test
    public void testRegisterAndLookup() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        Policy policy = new Policy();
        policy.setId("p1");

        reg.register("p1", policy);

        assertSame(policy, reg.lookup("p1"));
    }

    @Test
    public void testRegisterOverwrite_replacesOldValue() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        Policy first = new Policy();
        first.setId("key");
        Policy second = new Policy();
        second.setId("key");

        reg.register("key", first);
        reg.register("key", second);

        assertSame(second, reg.lookup("key"));
    }

    @Test
    public void testRemove_makesLookupReturnNull() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        Policy policy = new Policy();
        reg.register("p", policy);

        reg.remove("p");

        assertNull(reg.lookup("p"));
    }

    @Test
    public void testRemove_nonExistentKey_doesNotThrow() {
        PolicyRegistryImpl reg = new PolicyRegistryImpl();
        // must not throw
        reg.remove("ghost");
    }

    // --- parent registry delegation ---

    @Test
    public void testParentDelegation_childMissLookesInParent() {
        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        Policy policy = new Policy();
        parent.register("fromParent", policy);

        PolicyRegistryImpl child = new PolicyRegistryImpl(parent);

        assertSame(policy, child.lookup("fromParent"));
    }

    @Test
    public void testChildShadowsParent() {
        Policy parentPolicy = new Policy();
        Policy childPolicy = new Policy();

        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        parent.register("key", parentPolicy);

        PolicyRegistryImpl child = new PolicyRegistryImpl(parent);
        child.register("key", childPolicy);

        assertSame(childPolicy, child.lookup("key"));
        assertSame(parentPolicy, parent.lookup("key"));
    }

    @Test
    public void testParentNotConsulted_whenChildHasKey() {
        Policy parentPolicy = new Policy();
        parentPolicy.setId("parent");

        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        parent.register("key", parentPolicy);

        Policy childPolicy = new Policy();
        childPolicy.setId("child");

        PolicyRegistryImpl child = new PolicyRegistryImpl(parent);
        child.register("key", childPolicy);

        assertEquals("child", child.lookup("key").getId());
    }

    @Test
    public void testNoParent_missingKeyReturnsNull() {
        PolicyRegistryImpl child = new PolicyRegistryImpl();
        assertNull(child.lookup("missing"));
    }

    // --- setParent / getParent ---

    @Test
    public void testSetAndGetParent() {
        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        PolicyRegistryImpl child = new PolicyRegistryImpl();

        assertNull(child.getParent());

        child.setParent(parent);

        assertSame(parent, child.getParent());
    }

    @Test
    public void testSetParent_enablesDelegation() {
        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        Policy policy = new Policy();
        parent.register("p", policy);

        PolicyRegistryImpl child = new PolicyRegistryImpl();
        child.setParent(parent);

        assertSame(policy, child.lookup("p"));
    }

    @Test
    public void testConstructorWithParent_setsParentCorrectly() {
        PolicyRegistryImpl parent = new PolicyRegistryImpl();
        PolicyRegistryImpl child = new PolicyRegistryImpl(parent);

        assertSame(parent, child.getParent());
    }
}
