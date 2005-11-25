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

package org.apache.ws.policy.util;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.ws.policy.model.Policy;

/**
 * PolicyRegistry is contains reference to external policy that are used by the
 * policy model to resolve policy reference objects to their actual policies.
 * 
 */
public class PolicyRegistry {
    HashMap     reg    = new HashMap ();
    PolicyRegistry parent = null;
    
    public PolicyRegistry () {
    }
    
    public PolicyRegistry (PolicyRegistry parent) {
        this.parent = parent;
    }
    
    public Policy lookup (String policyURI) throws IllegalArgumentException {
        
        Policy policy =  (Policy) reg.get (policyURI);

        if (policy == null && parent != null) {
          policy = parent.lookup (policyURI);
        }

//      if (policy == null) {
//        throw new IllegalArgumentException ("policy '" + policyURI + "' not in registry");
//      }

        return policy;
      }
      // register a policy
      public void register (String policyURI, Policy policy) {
        reg.put (policyURI, policy);
      }
      
      // unregister a policy
      public void unregister (String policyURI) {
        reg.remove (policyURI);
      }
      
      public Iterator keys() {
        return reg.keySet().iterator();
      }
      
      public Iterator values() {
        return reg.values().iterator();
      }       
}
