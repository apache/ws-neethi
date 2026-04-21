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
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class StaxToDOMConverterTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static XMLStreamReader readerFor(String xml) throws Exception {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        return xif.createXMLStreamReader(new StringReader(xml));
    }

    private static Element convert(String xml) throws Exception {
        return new StaxToDOMConverter().convert(readerFor(xml));
    }

    // -----------------------------------------------------------------------
    // Element structure
    // -----------------------------------------------------------------------

    @Test
    public void testSimpleElement() throws Exception {
        Element el = convert("<root/>");
        assertNotNull(el);
        assertEquals("root", el.getLocalName());
        assertNull(el.getFirstChild());
    }

    @Test
    public void testNestedElements() throws Exception {
        Element root = convert("<root><child><grandchild/></child></root>");
        assertEquals("root", root.getLocalName());
        Element child = (Element) root.getFirstChild();
        assertEquals("child", child.getLocalName());
        Element grandchild = (Element) child.getFirstChild();
        assertEquals("grandchild", grandchild.getLocalName());
        assertNull(grandchild.getFirstChild());
    }

    @Test
    public void testMultipleSiblings() throws Exception {
        Element root = convert("<root><a/><b/><c/></root>");
        Node a = root.getFirstChild();
        assertEquals("a", a.getLocalName());
        Node b = a.getNextSibling();
        assertEquals("b", b.getLocalName());
        Node c = b.getNextSibling();
        assertEquals("c", c.getLocalName());
        assertNull(c.getNextSibling());
    }

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    @Test
    public void testUnprefixedAttribute() throws Exception {
        Element el = convert("<root foo=\"bar\"/>");
        assertEquals("bar", el.getAttribute("foo"));
    }

    @Test
    public void testMultipleAttributes() throws Exception {
        Element el = convert("<root a=\"1\" b=\"2\" c=\"3\"/>");
        assertEquals("1", el.getAttribute("a"));
        assertEquals("2", el.getAttribute("b"));
        assertEquals("3", el.getAttribute("c"));
    }

    // -----------------------------------------------------------------------
    // Namespaces
    // -----------------------------------------------------------------------

    @Test
    public void testNamespacedElement() throws Exception {
        Element el = convert("<ns:root xmlns:ns=\"http://example.com/\"/>");
        assertEquals("http://example.com/", el.getNamespaceURI());
        assertEquals("root", el.getLocalName());
        assertEquals("ns", el.getPrefix());
    }

    @Test
    public void testDefaultNamespace() throws Exception {
        Element el = convert("<root xmlns=\"http://example.com/\"/>");
        assertEquals("http://example.com/", el.getNamespaceURI());
        assertEquals("root", el.getLocalName());
    }

    @Test
    public void testNamespacedAttribute() throws Exception {
        Element el = convert(
            "<root xmlns:x=\"http://example.com/\" x:attr=\"val\"/>");
        assertEquals("val", el.getAttributeNS("http://example.com/", "attr"));
    }

    @Test
    public void testChildInheritedNamespace() throws Exception {
        Element root = convert(
            "<ns:root xmlns:ns=\"http://example.com/\"><ns:child/></ns:root>");
        Element child = (Element) root.getFirstChild();
        assertEquals("http://example.com/", child.getNamespaceURI());
        assertEquals("child", child.getLocalName());
    }

    // -----------------------------------------------------------------------
    // Text content
    // -----------------------------------------------------------------------

    @Test
    public void testTextContent() throws Exception {
        Element el = convert("<root>hello world</root>");
        Text text = (Text) el.getFirstChild();
        assertEquals("hello world", text.getNodeValue());
    }

    @Test
    public void testMixedContent() throws Exception {
        Element root = convert("<root>before<child/>after</root>");
        Node before = root.getFirstChild();
        assertEquals(Node.TEXT_NODE, before.getNodeType());
        assertEquals("before", before.getNodeValue());
        Node child = before.getNextSibling();
        assertEquals("child", child.getLocalName());
        Node after = child.getNextSibling();
        assertEquals(Node.TEXT_NODE, after.getNodeType());
        assertEquals("after", after.getNodeValue());
    }

    // -----------------------------------------------------------------------
    // Comment
    // -----------------------------------------------------------------------

    @Test
    public void testComment() throws Exception {
        Element root = convert("<root><!-- a comment --></root>");
        Comment comment = (Comment) root.getFirstChild();
        assertEquals(" a comment ", comment.getData());
    }

    @Test
    public void testCommentAmongSiblings() throws Exception {
        Element root = convert("<root><a/><!-- note --><b/></root>");
        Node a = root.getFirstChild();
        assertEquals("a", a.getLocalName());
        Node comment = a.getNextSibling();
        assertEquals(Node.COMMENT_NODE, comment.getNodeType());
        assertEquals(" note ", comment.getNodeValue());
        Node b = comment.getNextSibling();
        assertEquals("b", b.getLocalName());
    }

    // -----------------------------------------------------------------------
    // CDATA section
    // -----------------------------------------------------------------------

    @Test
    public void testCDataSection() throws Exception {
        Element root = convert("<root><![CDATA[some <raw> data]]></root>");
        CDATASection cdata = (CDATASection) root.getFirstChild();
        assertEquals("some <raw> data", cdata.getData());
    }

    // -----------------------------------------------------------------------
    // Processing instruction
    // -----------------------------------------------------------------------

    @Test
    public void testProcessingInstruction() throws Exception {
        Element root = convert("<root><?myapp key=\"value\"?></root>");
        ProcessingInstruction pi = (ProcessingInstruction) root.getFirstChild();
        assertEquals("myapp", pi.getTarget());
        assertEquals("key=\"value\"", pi.getData());
    }

    @Test
    public void testProcessingInstructionNoData() throws Exception {
        Element root = convert("<root><?myapp?></root>");
        ProcessingInstruction pi = (ProcessingInstruction) root.getFirstChild();
        assertEquals("myapp", pi.getTarget());
    }

    // -----------------------------------------------------------------------
    // Entity reference (Finding 6 — CWE-617)
    // -----------------------------------------------------------------------

    /**
     * Reproduces Finding 6 (SECURITY_REPORT.md): on the unfixed code the
     * ENTITY_REFERENCE case called createProcessingInstruction(getPITarget(),
     * getPIData()), where getPITarget() throws IllegalStateException because the
     * current event is not PROCESSING_INSTRUCTION.
     *
     * The fix changes it to createEntityReference(reader.getLocalName()).
     */
    @Test
    public void testEntityReferenceProducesEntityReferenceNode() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE root [<!ENTITY greet \"Hello\">]>"
            + "<root>&greet;</root>";

        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);

        XMLStreamReader reader = xif.createXMLStreamReader(new StringReader(xml));
        Element element = new StaxToDOMConverter().convert(reader);

        assertNotNull(element);
        Node child = element.getFirstChild();
        assertNotNull("Expected an entity reference child node under <root>", child);
        assertEquals(
            "Expected ENTITY_REFERENCE_NODE but got node type " + child.getNodeType(),
            Node.ENTITY_REFERENCE_NODE,
            child.getNodeType());
        assertEquals("greet", child.getNodeName());
    }
}
