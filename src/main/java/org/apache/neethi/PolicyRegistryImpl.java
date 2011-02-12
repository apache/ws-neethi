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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a default implementation of PolicyRegistry interface.
 */
public class PolicyRegistryImpl implements PolicyRegistry {
    
    private PolicyRegistry parent;
    
    private Map<String, Policy> reg = new ConcurrentHashMap<String, Policy>();
    
    public PolicyRegistryImpl() {
    }
    
    /**
     * Constructs a PolicyRegistryImpl with the specified PolicyRegistry
     * as it's parent. If it can't lookup a Policy in it's own registry
     * then it lookup in the parent and returns the results.  
     * 
     * @param parent the Parent of this PolicyRegistry
     */
    public PolicyRegistryImpl(PolicyRegistry parent) {
        this.parent = parent;
    }
    
    public Policy lookup(String key) {
        Policy policy = reg.get(key);
        
        if (policy == null && parent != null) {
            return parent.lookup(key);
        }
        
        return policy;
    }

    public void register(String key, Policy policy) {
        reg.put(key, policy);
    }
    
    public void remove(String key) {
        reg.remove(key);
    }
    
    public void setParent(PolicyRegistry parent) {
        this.parent = parent;
    }
    
    public PolicyRegistry getParent() {
        return parent;
    }
}
