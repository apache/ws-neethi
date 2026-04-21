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

package org.apache.neethi.builders.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class StaxToDOMConverterTest {

    /**
     * Converts an XML document containing an entity reference with
     * IS_REPLACING_ENTITY_REFERENCES=false, which causes the StAX parser to emit
     * an ENTITY_REFERENCE event instead of expanding the entity inline.
     */
    @Test
    public void testEntityReferenceProducesEntityReferenceNode() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE root [<!ENTITY greet \"Hello\">]>"
            + "<root>&greet;</root>";

        XMLInputFactory xif = XMLInputFactory.newInstance();
        // Prevent the parser from expanding &greet; into text, so it emits
        // an ENTITY_REFERENCE event that exercises the buggy case.
        xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);

        XMLStreamReader reader = xif.createXMLStreamReader(new StringReader(xml));

        Element element = new StaxToDOMConverter().convert(reader);

        assertNotNull(element);
        Node child = element.getFirstChild();
        assertNotNull("Expected an entity reference child node under <root>", child);
        assertEquals(
            "Expected ENTITY_REFERENCE_NODE (" + Node.ENTITY_REFERENCE_NODE + ") "
                + "but got node type " + child.getNodeType()
                + " — ENTITY_REFERENCE case calls createProcessingInstruction instead of createEntityReference",
            Node.ENTITY_REFERENCE_NODE,
            child.getNodeType());
        assertEquals("greet", child.getNodeName());
    }
}
