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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;

import org.junit.Assert;

public abstract class PolicyTestCase extends Assert {
    
    protected String baseDir = System.getProperty("basedir");
    protected String testResourceDir = "src" + File.separator + "test" + File.separator + "test-resources";
    protected PolicyBuilder policyEngine = new PolicyBuilder();
    protected PolicyRegistry registry = new PolicyRegistryImpl();
    
    public PolicyTestCase() {
        if (baseDir == null) {
            baseDir = (String) new File(".").getAbsolutePath();
        }
        policyEngine.setPolicyRegistry(registry);
    }

    public Policy getPolicy(String name) throws Exception {
        return getPolicy(name, 0);
    }
    public Policy getPolicy(String name, int type) throws Exception {
        switch (type) {
        case 0:
            return policyEngine.getPolicy(getResource(name));
        case 1:
            return policyEngine.getPolicy(getResourceAsDOM(name));
        case 2:
            return policyEngine.getPolicy(getResourceAsStax(name));
        default:
            return policyEngine.getPolicy(getResourceAsElement(name));
        }
    }
    
    public InputStream getResource(String name) throws FileNotFoundException {
        String filePath = new File(testResourceDir, name).getAbsolutePath(); 
        return new FileInputStream(filePath);
    }
    public Element getResourceAsDOM(String name) 
        throws ParserConfigurationException, SAXException, IOException {
        InputStream in = getResource(name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);
        // dbf.setCoalescing(true);
        // dbf.setExpandEntityReferences(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();

        // db.setErrorHandler( new MyErrorHandler());

        return db.parse(in).getDocumentElement();
    }
    
    public XMLStreamReader getResourceAsStax(String name) 
        throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
        InputStream in = getResource(name);
        return XMLInputFactory.newInstance().createXMLStreamReader(in);
    }    
    public OMElement getResourceAsElement(String name) 
        throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
        InputStream in = getResource(name);
        OMElement element = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(),
                XMLInputFactory.newInstance().createXMLStreamReader(in)).getDocumentElement();
        return element;
    }
}

