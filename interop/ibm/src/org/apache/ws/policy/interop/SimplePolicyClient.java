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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.soap.SOAPEnvelope;

/**
 * How to run ----------
 * 
 * (1) Normalize: user@localhost$java -cp <jar files> SimplePolicyClient -N <URL
 * of policy1>
 * 
 * (2) Merge: user@localhost$java -cp <jar files> SimplePolicyClient -M <URL of
 * policy1> <URL of policy2>
 * 
 * (3) Intersection user@localhost$java -cp <jar files> SimplePolicyClient -I
 * <URL of policy1> <URL of policy2>
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class SimplePolicyClient {

    private static String normalizeActionString = "http://example.com/ws/2004/09/policy/Normalize/Request";

    private static String mergeActionString = "http://example.com/ws/2004/09/policy/Merge/Request";

    private static String intersectActionString = "http://example.com/ws/2004/09/policy/Intersect/Request";

    private static String targetEPR = "http://wsi.alphaworks.ibm.com:8080/wspolicy/services/policyService";

    public static void main(String[] args) throws Exception {
        Call call = new Call();
        Options options = new Options();
        call.setClientOptions(options);

        options.setTo(new EndpointReference(targetEPR));
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);

        if (args[0].equals("-N")) {
            options.setAction(normalizeActionString);

        } else if (args[0].equals("-M")) {
            options.setAction(mergeActionString);

        } else if (args[0].equals("-I")) {
            options.setAction(intersectActionString);
        }

        SOAPEnvelope env = OMAbstractFactory.getSOAP11Factory()
                .getDefaultEnvelope();

        // adding args[1]
        env.getBody().addChild(getOMElementFromURL(args[1]));

        if (!args[0].equals("-N")) {
            env.getBody().addChild(getOMElementFromURL(args[2]));
        }

        SOAPEnvelope response = (SOAPEnvelope) call.invokeBlocking("foo", env);

        StringWriter sw = new StringWriter();
        OMElement result = response.getBody().getFirstElement();

        System.out.println("Output");
        result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                sw));
        sw.flush();
        System.out.println(sw.toString());
    }

    private static OMElement getOMElementFromURL(String urlString)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL targetURL = new URL(urlString);
        OMXMLParserWrapper wrapper = OMXMLBuilderFactory.createStAXOMBuilder(
                OMAbstractFactory.getOMFactory(), XMLInputFactory.newInstance()
                        .createXMLStreamReader(targetURL.openStream()));
        return wrapper.getDocumentElement();

    }

}
