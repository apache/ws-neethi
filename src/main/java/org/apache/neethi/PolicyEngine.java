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

import org.apache.axiom.om.OMElement;
import org.apache.neethi.builders.AssertionBuilder;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import java.io.InputStream;
import java.util.Iterator;

/**
 * PolicyEngine provides set of static methods to create a Policy object from an
 * InputStream, OMElement, .. etc.  It wrappers a static PolicyBuilder to actually
 * do the building.   This class is provided to ease transition from Neethi 2.x to 
 * Neethi 3.x  
 */
public class PolicyEngine {

    public static final String POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String POLICY = "Policy";

    public static final String EXACTLY_ONE = "ExactlyOne";

    public static final String ALL = "All";

    public static final String POLICY_REF = "PolicyReference";

    private static PolicyBuilder builder;
    
    private static synchronized PolicyBuilder getBuilder() {
        if (builder == null) {
            builder = new PolicyBuilder();
        }
        return builder;
    }

    /**
     * Registers an AssertionBuilder instances and associates it with a QName.
     * PolicyManager or other AssertionBuilders instances can use this
     * AssertionBuilder instance to process and build an Assertion from a
     * OMElement with the specified QName.
     * 
     * @param qname
     *            the QName of the Assertion that the Builder can build
     * @param builder
     *            the AssertionBuilder that can build assertions that of 'qname'
     *            type
     */
    public static void registerBuilder(QName qname, AssertionBuilder builder) {
        getBuilder().getAssertionBuilderFactory().registerBuilder(qname, builder);
    }

    /**
     * Creates a Policy object from an InputStream.
     * 
     * @param inputStream
     *            the InputStream of the Policy
     * @return a Policy object of the Policy that is fed as a InputStream
     */
    public static Policy getPolicy(InputStream inputStream) {
        return getBuilder().getPolicy(inputStream);
    }

    /**
     * Creates a PolicyReference object.
     * 
     * @param inputStream
     *            the InputStream of the PolicyReference
     * @return a PolicyReference object of the PolicyReference
     */
    public static PolicyReference getPolicyReferene(InputStream inputStream) {
        return getBuilder().getPolicyReference(inputStream);
    }

    /**
     * Creates a Policy object from an OMElement.
     * 
     * @param element
     *            the Policy element
     * @retun a Policy object of the Policy element
     */
    public static Policy getPolicy(OMElement element) {
        return getBuilder().getPolicy(element);
    }

    /**
     * Creates a Policy object from an Element.
     * 
     * @param element
     *            the Policy element
     * @retun a Policy object of the Policy element
     */
    public static Policy getPolicy(Object element) {
        return getBuilder().getPolicy(element);
    }
    
    /**
     * Creates a PolicyReference object from an OMElement.
     * 
     * @param element
     *            the PolicyReference element
     * @return a PolicyReference object of the PolicyReference element
     */
    public static PolicyReference getPolicyReference(OMElement element) {
        return getBuilder().getPolicyReference(element);
    }

    /**
     * Creates a PolicyReference object from an Element.
     * 
     * @param element
     *            the PolicyReference element
     * @return a PolicyReference object of the PolicyReference element
     */
    public static PolicyReference getPolicyReference(Object element) {
        return getBuilder().getPolicyReference(element);
    }
}
