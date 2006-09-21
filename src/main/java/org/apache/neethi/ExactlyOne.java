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

public class ExactlyOne extends AbstractPolicyOperator implements PolicyAlternatives {
    
    public void addAlternative(PolicyAlternative policyAlternative) {
        addPolicyComponent(policyAlternative);
    }
    

    public List getAlternatives() {
        // TODO Auto-generated method stub
        return null;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(Constants.URI_POLICY_NS);

        if (prefix == null) {
            writer.writeStartElement(Constants.POLICY_PREFIX, Constants.ELEM_EXACTLYONE, Constants.URI_POLICY_NS);
            writer.writeNamespace(Constants.POLICY_PREFIX, Constants.URI_POLICY_NS);
            writer.setPrefix(Constants.POLICY_PREFIX, Constants.URI_POLICY_NS);
        } else {
            writer.writeStartElement(Constants.URI_POLICY_NS, Constants.ELEM_EXACTLYONE);
        }

        PolicyComponent policyComponent;

        for (Iterator iterator = getPolicyComponents().iterator(); iterator.hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            policyComponent.serialize(writer);
        }

        writer.writeEndElement();
    }

    public final short getType() {
        return Constants.TYPE_EXACTLYONE;
    }
}