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
package org.apache.neethi.builders;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;

/**
 * AssertionBuilder is the interface which must implement by any
 * CustomAssertionBuilder. It defines a single method which takes an OMElement
 * and an AssertionFactory instace and creates an Assertion from the given
 * OMElement. Custom AssertionBuilder authors can use the AssertionFactory
 * specified to build Assertions for any unknown OMElements inside the given
 * OMElement. They are given the opportunity to control the behaviour of
 * Assertion operations based on the corresponding domain policy assertion of
 * the given OMElement and the level of its processing.
 * 
 */
public interface AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException;
}
