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

package org.apache.ws.policy.model;

import org.apache.ws.policy.util.PolicyRegistry;

/**
 * PolicyReference class has implicit reference to a external policy. It acts as
 * wrapper to external policies in the standard policy framework.
 *  
 */
public class PolicyReference implements Assertion {

    private String PolicyURIString = null;

    private Assertion parent = null;

    public PolicyReference(String policyURIString) {
        this.PolicyURIString = policyURIString;
    }

    public String getPolicyURIString() {
        return PolicyURIString;
    }

    public Assertion normalize() {
        throw new UnsupportedOperationException();
    }

    public Assertion normalize(PolicyRegistry reg) {
        if (reg == null) {
            throw new RuntimeException("Cannot resolve : "
                    + getPolicyURIString() + " .. PolicyRegistry is null");
        }
        Policy targetPolicy = reg.lookup(getPolicyURIString());
        if (targetPolicy == null) {
            throw new RuntimeException("error : " + getPolicyURIString()
                    + " doesn't resolve to any known policy");
        }

        return targetPolicy.normalize(reg);
    }

    public Assertion intersect(Assertion assertion) {
        throw new UnsupportedOperationException();
    }

    public Assertion intersect(Assertion assertion, PolicyRegistry reg)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Assertion merge(Assertion assertion, PolicyRegistry reg)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Assertion merge(Assertion assertion) {
        throw new UnsupportedOperationException();
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Assertion getParent() {
        return parent;
    }

    public void setParent(Assertion parent) {
        this.parent = parent;
    }

    public boolean isNormalized() {
        throw new UnsupportedOperationException();
    }

    public void setNormalized(boolean flag) {
        throw new UnsupportedOperationException();
    }
}
