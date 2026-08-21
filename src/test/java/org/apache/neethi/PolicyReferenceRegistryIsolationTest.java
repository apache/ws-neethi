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
 * PolicyReference.normalize used to register the fetched, remote-origin
 * policy into the caller's registry before the caller could vet it, so a
 * single attacker-controlled response was durably promoted into a store the
 * threat model marks trusted by construction. The fetched policy must stay
 * out of the caller's registry while remaining resolvable for the recursive
 * normalize (cycle termination for self-references).
 */
public class PolicyReferenceRegistryIsolationTest extends PolicyTestCase {

    private static final String REMOTE_URI = "http://policy.example.invalid/remote.xml";

    @Test
    public void testFetchedPolicyIsNotRegisteredIntoCallerRegistry() {
        final Policy fetched = new Policy();
        PolicyReference ref = new PolicyReference(policyEngine) {
            @Override
            public Policy getRemoteReferencedPolicy(String u) {
                return fetched;
            }
        };
        ref.setURI(REMOTE_URI);
        PolicyRegistry callerRegistry = new PolicyRegistryImpl();

        PolicyComponent normalized = ref.normalize(callerRegistry, true);

        assertNotNull(normalized);
        assertNull("fetched policy must not be silently registered",
                   callerRegistry.lookup(REMOTE_URI));
    }

    @Test
    public void testSelfReferencingFetchedPolicyStillTerminates() {
        final Policy fetched = new Policy();
        PolicyReference selfReference = new PolicyReference(policyEngine);
        selfReference.setURI(REMOTE_URI);
        fetched.addPolicyComponent(selfReference);

        PolicyReference ref = new PolicyReference(policyEngine) {
            @Override
            public Policy getRemoteReferencedPolicy(String u) {
                return fetched;
            }
        };
        ref.setURI(REMOTE_URI);

        try {
            ref.normalize(new PolicyRegistryImpl(), true);
            fail("Expected cycle detection for the self-referencing fetched policy");
        } catch (RuntimeException ex) {
            // the scoped lookup resolves the self-reference without another
            // fetch, and the normalizer's cycle detection then fires
            assertTrue(ex.getMessage().contains("Circular PolicyReference"));
        }
    }

    @Test
    public void testRegistryHitStillNormalizesAgainstCallerRegistry() {
        Policy registered = new Policy();
        PolicyRegistry callerRegistry = new PolicyRegistryImpl();
        callerRegistry.register(REMOTE_URI, registered);

        PolicyReference ref = new PolicyReference(policyEngine);
        ref.setURI(REMOTE_URI);

        assertNotNull(ref.normalize(callerRegistry, true));
    }
}
