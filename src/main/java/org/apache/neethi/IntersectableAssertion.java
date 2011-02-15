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

/**
 * Assertions that need special consideration to implement
 * the intersection algorithms should implement this interface.
 * 
 * If an assertion does not implement this interface, the 
 * intersection algorithm will just check the qname of the
 * assertion as well as the contained policy if the assertion
 * implements the PolicyContainingAssertion interface.  This 
 * is adequate for most use cases and per spec, but this
 * interface allows an assertion to possibly consider special
 * attributes or other details to determine the behavior
 * for the intersection.
 */
public interface IntersectableAssertion extends Assertion {
    
    boolean isCompatible(Assertion assertion, boolean strict);
    
    Assertion intersect(Assertion assertion, boolean strict);
}
