/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.ws.policy.interop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.soap.SOAPBody;
import org.apache.ws.policy.model.Policy;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyWriter;


/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class SimplePolicyService {
    private MessageContext msgCtx = null;

    public void init(MessageContext msgCtx) {
        this.msgCtx = msgCtx;
    }

    public OMElement normalize() {
        Iterator iterator = getArgs().iterator();
        OMElement element = (OMElement) iterator.next();

        Policy argOne = getReader().readPolicy(getInputStream(element));
        Policy normalized = (Policy) argOne.normalize();
        return getOMElement(normalized);
    }

    public OMElement merge() {
        Iterator iterator = getArgs().iterator();
        System.out.println("inside merge");

        OMElement element = (OMElement) iterator.next();
        OMElement element2 = (OMElement) iterator.next();

        Policy argOne = getReader().readPolicy(getInputStream(element));
        Policy argTwo = getReader().readPolicy(getInputStream(element2));

        Policy merged = (Policy) argOne.merge(argTwo);
        return getOMElement(merged);

    }

    public OMElement intersect() {
        Iterator iterator = getArgs().iterator();
        OMElement element = (OMElement) iterator.next();
        OMElement element2 = (OMElement) iterator.next();

        Policy argOne = getReader().readPolicy(getInputStream(element));
        Policy argTwo = getReader().readPolicy(getInputStream(element2));

        Policy intersected = (Policy) argOne.intersect(argTwo);
        return getOMElement(intersected);
    }

    private ArrayList getArgs() {
        ArrayList argList = new ArrayList();

        SOAPBody body = msgCtx.getEnvelope().getBody();
        Iterator iterator = body.getChildElements();
        OMElement child;

        while (iterator.hasNext()) {
            child = getWholeElement((OMElement) iterator.next());
            argList.add(child);
        }

        return argList;
    }

    private PolicyReader getReader() {
        return PolicyFactory.getInstance().getPolicyReader();
    }

    private PolicyWriter getWriter() {
        return PolicyFactory.getInstance().getPolicyWriter();
    }

    private OMElement getWholeElement(OMElement element) {
        element.build();
        element.detach();
        return element;
    }

    private InputStream getInputStream(OMElement element) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            element.serialize(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(baos));
            return new ByteArrayInputStream(baos.toByteArray());

        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }

    }

    private OMElement getOMElement(Policy policy) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getWriter().writePolicy(policy, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos
                    .toByteArray());
            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(bais);
            OMXMLParserWrapper wrapper = OMXMLBuilderFactory
                    .createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                            reader);

            return wrapper.getDocumentElement();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
