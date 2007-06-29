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

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;

import sun.misc.Service;

/**
 * AssertionFactory is used to create an Assertion from an OMElement. It uses an
 * appropriate AssertionBuilder instace to create an Assertion based on the
 * QName of the given OMElement. Domain Policy authors could right custom
 * AssertionBuilders to build Assertions for domain specific assertions.
 * 
 */
public class AssertionBuilderFactory {

    public static final String POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String POLICY = "Policy";

    public static final String EXACTLY_ONE = "ExactlyOne";

    public static final String ALL = "All";

    private static final QName XML_ASSERTION_BUILDER = new QName(
            "http://test.org/test", "test");

    private static HashMap registeredBuilders = new HashMap();

    static {
        AssertionBuilder builder;

        for (Iterator providers = Service.providers(AssertionBuilder.class); providers
                .hasNext();) {
            builder = (AssertionBuilder) providers.next();

            QName[] knownElements = builder.getKnownElements();
            for (int i = 0; i < knownElements.length; i++) {
                registerBuilder(knownElements[i], builder);
            }
        }
        
        registerBuilder(XML_ASSERTION_BUILDER, new XMLPrimitiveAssertionBuilder());
    }

    /**
     * Registers an AssertionBuilder with a specified QName. 
     *  
     * @param key the QName that the AssertionBuilder understand
     * @param builder the AssertionBuilder that can build an Assertion from
     *      OMElement of specified type
     */
    public static void registerBuilder(QName key, AssertionBuilder builder) {
        registeredBuilders.put(key, builder);
    }

    public AssertionBuilderFactory() {
    }

    /**
     * Returns an assertion that is built using the specified OMElement.
     * 
     * @param element the element that the AssertionBuilder can use to build
     *      an Assertion.
     * @return an Assertion that is built using the specified element.
     */
    public Assertion build(OMElement element) {

        AssertionBuilder builder;

        QName qname = element.getQName();
        builder = (AssertionBuilder) registeredBuilders.get(qname);

        if (builder != null) {
            return builder.build(element, this);
        }

        /*
         *  if we can't locate an appropriate AssertionBuilder, we always
         *  use the XMLPrimitiveAssertionBuilder 
         */ 
        builder = (AssertionBuilder) registeredBuilders
                .get(XML_ASSERTION_BUILDER);
        return builder.build(element, this);
    }
    
    /**
     * Returns an AssertionBuilder that build an Assertion from an OMElement
     * of qname type.
     * 
     * @param qname the type that the AssertionBuilder understands and builds an Assertion from
     * @return an AssertionBuilder that understands qname type
     */
    public AssertionBuilder getBuilder(QName qname) {
        return (AssertionBuilder) registeredBuilders.get(qname);
    }
}
