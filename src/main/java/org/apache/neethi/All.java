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

import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * 
 *
 */
public class All extends AbstractPolicyOperator implements PolicyAlternative {
    
    public void addAssertion(Assertion assertion) {
        addPolicyComponent(assertion);
    }

    public List getAssertions() {
        return policyComponents;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NAMESPACE);

        if (prefix == null) {
            writer.writeStartElement(PREFIX, LOCAL_NAME_ALL, NAMESPACE);
            writer.writeNamespace(PREFIX, NAMESPACE);
            writer.setPrefix(PREFIX, NAMESPACE);
        } else {
            writer.writeStartElement(NAMESPACE, LOCAL_NAME_ALL);
        }

        PolicyComponent policyComponent;

        for (Iterator iterator = getPolicyComponents().iterator(); iterator
                .hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            policyComponent.serialize(writer);
        }

        writer.writeEndElement();
    }

    public short getType() {
        return PolicyComponent.ALL;
    }
}
