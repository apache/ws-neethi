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

    /**
     * Constructs an assertion from a known OMElement. If that element contains
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
    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException;

    /**
     * Returns an array of QNames of OMElements from which assertion can be
     * built by this AssertionFactory.
     * 
     * @return an array of QNames of known OMElements
     */
    public QName[] getKnownElements();
}
