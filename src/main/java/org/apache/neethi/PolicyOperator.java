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

import java.util.List;

/**
 * PolicyOperator is an interface that all Policy operators must implement. It
 * can contain any number of Assertions or PolicyOperators and it has an
 * implecit logic of how those PolicyComponents should be met.
 * 
 */
public interface PolicyOperator extends PolicyComponent {

    /**
     * Add a PolicyComponent to the PolicyOperator.
     * 
     * @param component
     */
    public void addPolicyComponent(PolicyComponent component);

    /**
     * Returns a List of PolicyComponents which this PolicyOperator contains.
     * 
     * @return the List of PolicyComponents that this PolicyOperator contains.
     */
    public List getPolicyComponents();

    /**
     * Returns true if the PolicyOperator doesn't contain any PolicyComponents.
     * 
     * @return true if this PolicyOperator doesn't contain any PolicyComponenets
     */
    public boolean isEmpty();
}
