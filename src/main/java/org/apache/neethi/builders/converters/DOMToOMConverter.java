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

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;

/**
 * 
 */
public class DOMToOMConverter extends AbstractDOMConverter 
    implements Converter<Element, OMElement> {

    public OMElement convert(Element s) {
        try {
            return OMXMLBuilderFactory
                .createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                     XMLInputFactory.newInstance().createXMLStreamReader(new DOMSource(s)))
                                     .getDocumentElement();
        } catch (XMLStreamException e) {
            //ignore - will deal with it in the builder
        } catch (FactoryConfigurationError e) {
            //ignore - will deal with it in the builder
        }
        return null;

    }
}
