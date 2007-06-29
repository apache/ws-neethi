/*
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

/**
 * PolicyRegistry contains (URI,Policy) pairs and it is used to resolve explicit
 * Policy references.
 * 
 */
public interface PolicyRegistry {

    /**
     * Associates a key with a Policy
     * 
     * @param key
     *            the key that the specified Policy to be associated
     * @param policy
     *            the policy to be associated with the key
     */
    public void register(String key, Policy policy);

    /**
     * Returns the Policy that the specified key is mapped. Retruns null if no
     * Policy is associated with that key.
     * 
     * @param key
     *            the key whose associated Policy is to be returned.
     * @return the policy associated with the specified key.
     */
    public Policy lookup(String key);

    /**
     * Removes the mapping for this key if present.
     * 
     * @param key
     *            the key whose mapping is to be removed
     */
    public void remove(String key);

}
