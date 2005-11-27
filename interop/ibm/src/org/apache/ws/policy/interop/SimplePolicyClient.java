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

import java.io.FileInputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.soap.SOAPEnvelope;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class SimplePolicyClient {
    public static void main(String[] args) throws Exception {
        Call call = new Call();
        call.setWsaAction("http://schemas.xmlsoap.org/ws/2004/09/mex/GetMetadata/Request");
        call.setTo(new EndpointReference("http://localhost:8080/axis2/services/PolicyService")); 
        
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP, false);

        FileInputStream fis = new FileInputStream(
                "/home/sanka/policy-docs/primitive.xml");
        XMLStreamReader xmlr = XMLInputFactory.newInstance()
                .createXMLStreamReader(fis);
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(), xmlr);

        OMElement arg = builder.getDocumentElement();

        SOAPEnvelope env = OMAbstractFactory.getSOAP11Factory()
                .getDefaultEnvelope();

        // adding arg1
        env.getBody().addChild(arg);
        
        //env.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));

//        fis = new FileInputStream("/home/sanka/policy-docs/policy2.xml");
//        xmlr = XMLInputFactory.newInstance().createXMLStreamReader(fis);
//        builder = OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory
//                .getOMFactory(), xmlr);
//        arg = builder.getDocumentElement();
//
//         adding arg2
//        env.getBody().addChild(arg);

        OMElement resutl = call.invokeBlocking("foo", env);
        StringWriter sw = new StringWriter();
        resutl.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                System.out));
        sw.flush();
        System.out.println(sw.toString());
        
    }

}
