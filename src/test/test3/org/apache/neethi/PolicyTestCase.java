/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.neethi;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;


public class PolicyTestCase extends org.apache.ws.policy.PolicyTestCase {
	public PolicyTestCase(String name) {
		super(name);
	}	
	
	public OMElement getResourceAsElement(String name) {
		try {
			InputStream in = getResource(name);
            OMElement element = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), XMLInputFactory.newInstance().createXMLStreamReader(in)).getDocumentElement();
            return element;
            
		} catch (Exception e) {
			fail("Cannot get resource: " + e.getMessage());
			throw new RuntimeException();
		}
	}
}

