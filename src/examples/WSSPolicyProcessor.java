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

package examples;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ws.policy.model.AndCompositeAssertion;
import org.apache.ws.policy.model.Policy;
import org.apache.ws.policy.model.PrimitiveAssertion;
import org.apache.ws.policy.model.XorCompositeAssertion;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyReaderDOM;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class WSSPolicyProcessor {
    private static String policy = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:wsrm=\"http://schemas.xmlsoap.org/ws/2005/02/rm/policy\" "
            + "xmlns:sec=\"http://schemas.xmlsoap.org/ws/2002/12/secext\">"
            + "<wsp:ExactlyOne>"
            + "<wsp:All>"
            + "<sec:SecurityToken>"
            + "<sec:TokenType>sec:X509v3</sec:TokenType>"
            + "</sec:SecurityToken>"
            + "<sec:Integrity>"
            + "<sec:MessageParts Dialect=\"http://schemas.xmlsoap.org/ws/2002/12/wsse#soap\">"
            + "wsp:Body()"
            + "</sec:MessageParts>"
            + "</sec:Integrity>"
            + "<sec:SecurityHeader MustPrepend=\"true\" MustManifestEncryption=\"true\"/>"
            + "<wsrm:RMAssertion>"
            + "<wsrm:InactivityTimeout Milliseconds=\"600000\"/>"
            + "<wsrm:AcknowledgementInterval Milliseconds=\"200\"/>"
            + "</wsrm:RMAssertion>"
            + "</wsp:All>"
            + "</wsp:ExactlyOne>"
            + "</wsp:Policy>";

    public static void main(String[] args) throws Exception {
        WSSPolicyProcessor process = new WSSPolicyProcessor();
        
        /*
         * Use the Stream based parser, Axis2 OM  
         */
        PolicyReader reader = PolicyFactory.getInstance().getPolicyReader();
        Policy p = reader
                .readPolicy(new ByteArrayInputStream(policy.getBytes()));
        process.processPolicy((Policy) p.normalize());
        
        /*
         * Use standard Parser, w3c DOM
         */
        PolicyReaderDOM readerDom = PolicyFactory.getInstance().getPolicyReaderDOM();
        Policy pDom = readerDom
                .readPolicy(new ByteArrayInputStream(policy.getBytes()));
        process.processPolicy((Policy) pDom.normalize());
    }

    /*
     * This method takes a policy object which contains only *ONE* policy
     * alternative. WSS4J framework should configure it self in accordance with
     * WSSecurityPolicy policy assertions if there is any in that policy
     * alternative. If that alternative contains any WSSecurityPolicy policy
     * assertion which WSS4J cannot support, it should throw an exception and
     * notify ..
     *  
     */
    public void processPolicy(Policy policy) {

        if (!policy.isNormalized()) {
            throw new RuntimeException("Policy is not in normalized format");
        }

        XorCompositeAssertion xor = (XorCompositeAssertion) policy.getTerms()
                .get(0);
        List listOfPolicyAlternatives = xor.getTerms();

        if (listOfPolicyAlternatives.size() != 1) {
            throw new RuntimeException(
                    "Policy contians either zero or more than one policy alterntives");
        }

        AndCompositeAssertion aPolicyAlternative = (AndCompositeAssertion) listOfPolicyAlternatives
                .get(0);

        List listOfPrimitiveAssertions = aPolicyAlternative.getTerms();

        ArrayList listOfWSSPrimitiveAssertions = new ArrayList();

        Iterator iterator = listOfPrimitiveAssertions.iterator();
        while (iterator.hasNext()) {
            PrimitiveAssertion primitiveAssertion = (PrimitiveAssertion) iterator
                    .next();

            /*
             * We need to pick only the primitive assertions which conatain a WSSecurityPolicy policy assertion.
             * For that we'll check the namespace of the primitive assertion
             */

            if (primitiveAssertion.getName().getNamespaceURI().equals(
                    "http://schemas.xmlsoap.org/ws/2002/12/secext")) {
                listOfWSSPrimitiveAssertions.add(primitiveAssertion);
            }
        }

        loadConfigurations(listOfWSSPrimitiveAssertions);
    }

    /*
     * This method takes a list of primitive assertions which contains
     * WSSecurity policy assertions and configures WSS4j framework according to
     * those policy information.
     * 
     * For the time being I just printed those WSSecurity policy assertion to
     * System.out But what you should really do is something like setting the
     * options of WSDoAllReceiver/Sender according to these policy assertions.
     *  
     */
    public void loadConfigurations(List assertions) {
        Iterator iterator = assertions.iterator();
        while (iterator.hasNext()) {
            PrimitiveAssertion prim = (PrimitiveAssertion) iterator.next();

            /*
             * May be I should be setting the configuration options in
             * WSDoAll*Handler according to this security assertion.
             */

            System.out.println("WSSPolicyAssertion : "
                    + prim.getName().getLocalPart());
        }

    }
}

