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
package org.apache.neethi;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;

public class XmlPrimtiveAssertion implements Assertion {

    OMElement element;
    
    public XmlPrimtiveAssertion(OMElement element) {
        setValue(element);
    }

    public void setValue(OMElement element) {
        this.element = element;
    }

    public OMElement getValue() {
        return element;
    }

    public Assertion normalize() throws IllegalArgumentException {
        return normalize(null);
    }

    public Assertion normalize(PolicyRegistry registry) {
        return this;
    }

    public void serialize(XMLStreamWriter writer) {
        if (element != null) {
            try {
                element.serialize(writer);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            // TODO throw an exception??
        }
    }
    
    public final short getType() {
        return PolicyComponent.ASSERTION;
    }
}
