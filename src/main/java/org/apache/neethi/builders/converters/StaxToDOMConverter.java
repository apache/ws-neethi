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

import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 */
public class StaxToDOMConverter extends AbstractStaxConverter 
    implements Converter<XMLStreamReader, Element> {

    public Element convert(XMLStreamReader reader) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            readDocElements(doc, doc, reader);
            return doc.getDocumentElement();
        } catch (ParserConfigurationException ex) {
            throw new ConverterException(ex);
        } catch (XMLStreamException ex) {
            throw new ConverterException(ex);
        }
    }

    public static void readDocElements(Document doc, Node parent,
                                       XMLStreamReader reader)
        throws XMLStreamException {
        
        Stack<Node> stack = new Stack<Node>();
        int event = reader.getEventType();
        while (reader.hasNext()) {
            switch (event) {
            case XMLStreamConstants.START_ELEMENT: {
                Element e = doc.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
                if (reader.getPrefix() != null) {
                    e.setPrefix(reader.getPrefix());
                }       
                e = (Element)parent.appendChild(e);

                for (int ns = 0; ns < reader.getNamespaceCount(); ns++) {
                    String uri = reader.getNamespaceURI(ns);
                    String prefix = reader.getNamespacePrefix(ns);

                    declare(e, uri, prefix);
                }

                for (int att = 0; att < reader.getAttributeCount(); att++) {
                    String name = reader.getAttributeLocalName(att);
                    String prefix = reader.getAttributePrefix(att);
                    if (prefix != null && prefix.length() > 0) {
                        name = prefix + ":" + name;
                    }

                    Attr attr = doc.createAttributeNS(reader.getAttributeNamespace(att), name);
                    attr.setValue(reader.getAttributeValue(att));
                    e.setAttributeNode(attr);
                }


                stack.push(parent);
                parent = e;
                break;
            }
            case XMLStreamConstants.END_ELEMENT:
                if (stack.isEmpty()) {
                    return;
                }
                parent = stack.pop();
                if (parent instanceof Document) {
                    return;
                }
                break;
            case XMLStreamConstants.NAMESPACE:
                break;
            case XMLStreamConstants.ATTRIBUTE:
                break;
            case XMLStreamConstants.CHARACTERS:
                if (parent != null) {
                    parent.appendChild(doc.createTextNode(reader.getText()));
                }
                break;
            case XMLStreamConstants.COMMENT:
                if (parent != null) {
                    parent.appendChild(doc.createComment(reader.getText()));
                }
                break;
            case XMLStreamConstants.CDATA:
                parent.appendChild(doc.createCDATASection(reader.getText()));
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                parent.appendChild(doc.createProcessingInstruction(reader.getPITarget(), reader.getPIData()));
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                parent.appendChild(doc.createProcessingInstruction(reader.getPITarget(), reader.getPIData()));
                break;
            default:
                break;
            }

            if (reader.hasNext()) {
                event = reader.next();
            }
        }
    }
    private static void declare(Element node, String uri, String prefix) {
        String qualname;
        if (prefix != null && prefix.length() > 0) {
            qualname = "xmlns:" + prefix;
        } else {
            qualname = "xmlns";
        }
        Attr attr = node.getOwnerDocument().createAttributeNS("http://www.w3.org/2000/xmlns/", qualname);
        attr.setValue(uri);
        node.setAttributeNodeNS(attr);
    }
}
