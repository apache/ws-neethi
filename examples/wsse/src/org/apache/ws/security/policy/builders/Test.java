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
package org.apache.ws.security.policy.builders;

import java.io.FileInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.ws.security.policy.Constants;


public class Test {

    public static void main(String[] args) throws Exception {

        OMElement element =
                OMXMLBuilderFactory.
                createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                   XMLInputFactory.newInstance().createXMLStreamReader(
                                   new FileInputStream("/home/sanka/testpolicy.xml")))
                                   .getDocumentElement();

        AssertionBuilderFactory factory = new AssertionBuilderFactory();
        AssertionBuilder builder;

        builder = new TransportBindingBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "TransportBinding"), builder);

        builder = new AlgorithmSuiteBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "AlgorithmSuite"), builder);

        builder = new TransportTokenBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "TransportToken"), builder);

        builder = new LayoutBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "Layout"), builder);

        builder = new SignedElementsBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "SignedElements"), builder);

        builder = new SignedPartsBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "SignedParts"), builder);

        builder = new SupportingTokensBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "SupportingTokens"), builder);
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "SignedSupportingTokens"), builder);
        PolicyEngine
                .registerBuilder(new QName(Constants.SP_NS, "EndorsingSupportingTokens"), builder);
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "SignedEndorsingSupportingTokens"),
                                     builder);


        builder = new UsernameTokenBuilder();
        PolicyEngine.registerBuilder(new QName(Constants.SP_NS, "UsernameToken"), builder);

        Policy p = PolicyEngine.getPolicy(element);
        System.out.println(p + "done");

    }
}
