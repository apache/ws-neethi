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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import org.apache.neethi.builders.PrimitiveAssertion;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;

/**
 * 
 */
public class BasicTestCases extends TestCase {
    PolicyBuilder pb = new PolicyBuilder();
    
    public void testPrimitiveBuilder() throws Exception {
        String text = "<ns1:MaximumRetransmissionCount FooAtt=\"blah\" xmlns:ns1=\"http://foo\">10"
            + "</ns1:MaximumRetransmissionCount>";
        
        
        Assertion as = new XMLPrimitiveAssertionBuilder().build(getElementFromString(text),
                                                                pb.getAssertionBuilderFactory());
        assertNotNull(as);
        PrimitiveAssertion pas = (PrimitiveAssertion)as;
        assertEquals("10", pas.getTextValue());
        assertEquals("blah", pas.getAttribute(new QName("FooAtt")));
        
        StringWriter writer = new StringWriter();
        XMLStreamWriter xwriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        pas.serialize(xwriter);
        xwriter.flush();
        
        Element el = getElementFromString(writer.getBuffer().toString());
        assertEquals("blah", el.getAttribute("FooAtt"));
        assertEquals("10", el.getTextContent());
    }
    public void testPolicyChildren() throws Exception {
        String text = "<sp:Wss11 xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">"
                      + "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">"
                      + "<sp:MustSupportRefKeyIdentifier/>"
                      + "<sp:MustSupportRefIssuerSerial/>"
                      + "<sp:MustSupportRefThumbprint/>"
                      + "<sp:MustSupportRefEncryptedKey/>"
                      + "</wsp:Policy></sp:Wss11>";
                                                                                        

        Assertion as = new XMLPrimitiveAssertionBuilder().build(getElementFromString(text),
                                                                pb.getAssertionBuilderFactory());
        assertNotNull(as);
        
        
        StringWriter writer = new StringWriter();
        XMLStreamWriter xwriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        as.serialize(xwriter);
        xwriter.flush();
        xwriter.close();
        
        Element el = getElementFromString(writer.getBuffer().toString());
        assertEquals("Wss11", el.getLocalName());
        el = (Element)el.getFirstChild();
        assertEquals("Policy", el.getLocalName());
        el = (Element)el.getFirstChild();
        assertEquals("MustSupportRefKeyIdentifier", el.getLocalName());
    }

    
    public void testMultiChildren() throws Exception {
        String text = "<sp:SignedParts xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">"
            + "<sp:Body />"
            + "<sp:Header Name=\"To\" Namespace=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"/>"
            + "<sp:Header Name=\"From\" Namespace=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" />"
            + "</sp:SignedParts>";

        Assertion as = new XMLPrimitiveAssertionBuilder().build(getElementFromString(text),
                                                                pb.getAssertionBuilderFactory());
        assertNotNull(as);
        
        
        StringWriter writer = new StringWriter();
        XMLStreamWriter xwriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        as.serialize(xwriter);
        xwriter.flush();
        xwriter.close();
        
        Element el = getElementFromString(writer.getBuffer().toString());
        assertEquals("SignedParts", el.getLocalName());
        el = (Element)el.getFirstChild();
        assertEquals("Body", el.getLocalName());
        el = (Element)el.getNextSibling();
        assertEquals("Header", el.getLocalName());
    }

    
    private Element getElementFromString(String s) 
        throws ParserConfigurationException, SAXException, IOException {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();

        
        return db.parse(new InputSource(new StringReader(s))).getDocumentElement();
    }
}
