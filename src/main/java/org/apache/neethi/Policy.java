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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Policy extends All {

    public PolicyComponent normalize(boolean deep) {
        Policy policy = new Policy();
        policy.addPolicyComponent(super.normalize(deep));
        return policy;
    }

    public Policy merge(Policy policy) {
        Policy result = new Policy();
        result.addPolicyComponents(getPolicyComponents());
        result.addPolicyComponents(policy.getPolicyComponents());
        return result;
    }

    public Policy intersect(Policy policy) {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(NAMESPACE);

        if (prefix == null) {
            writer.writeStartElement(PREFIX, LOCAL_NAME_POLICY, NAMESPACE);
            writer.writeNamespace(PREFIX, NAMESPACE);
            writer.setPrefix(PREFIX, NAMESPACE);

        } else {
            writer.writeStartElement(NAMESPACE, LOCAL_NAME_POLICY);
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
        return PolicyComponent.POLICY;
    }

    public Iterator getAlternatives() {
        return new PolicyIterator(this);
    }

    private class PolicyIterator implements Iterator {
        Iterator alternatives = null;

        public PolicyIterator(Policy policy) {
            policy = (Policy) policy.normalize(false);
            ExactlyOne exactlyOne = (ExactlyOne) policy
                    .getFirstPolicyComponent();
            alternatives = exactlyOne.getPolicyComponents().iterator();
        }

        public boolean hasNext() {
            return alternatives.hasNext();
        }

        public Object next() {
            return ((All) alternatives.next()).getPolicyComponents();
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "policyAlternative.remove() is not supported");
        }

    }
}
