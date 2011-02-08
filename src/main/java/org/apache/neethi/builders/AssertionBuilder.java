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

package org.apache.neethi.builders;

import javax.xml.namespace.QName;

import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;

/**
 * AssertionBuilder is the interface which must implement by any
 * CustomAssertionBuilder. It defines a single method which takes an element
 * definitionand an AssertionFactory instance and creates an Assertion.  The 
 * AssertionBuilder must use one of the types for which there is a
 * Converter registered.  By default, that would be either an Element,
 * and XMLStreamReader, or OMElement (if Axiom is available).
 * Custom AssertionBuilder authors can use the AssertionFactory
 * specified to build Assertions for any unknown elements inside the given
 * element. They are given the opportunity to control the behavior of
 * Assertion operations based on the corresponding domain policy assertion of
 * the given element and the level of its processing.
 * 
 */
public interface AssertionBuilder<T> {

    /**
     * Constructs an assertion from a known element. If that element contains
     * other child elements that the Builder doesn't understand, it uses the
     * AssertionBuilderFactory to construct assertions from them.
     * 
     * @param element
     *            the known element from which an assertion can be built
     * @param factory
     *            the factory from which AssertionBuilders are taken to build
     *            assertion from unknown child elements
     * @return an Assertion built from the given element
     * @throws IllegalArgumentException
     *             if the given element is malformed
     */
    Assertion build(T element, AssertionBuilderFactory factory)
            throws IllegalArgumentException;

    /**
     * Returns an array of QNames of elements from which assertion can be
     * built by this AssertionFactory.
     * 
     * @return an array of QNames of known elements
     */
    QName[] getKnownElements();
    
}
