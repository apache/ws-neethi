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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import org.apache.ws.policy.model.Policy;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.OMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.StAXPolicyWriter;

/**
 * @author Werner Dittmann (werner@apache.org)
 */

public class SimplePolicyExample {

    public static void main(String[] args) throws Exception {

        FileInputStream fis = null;
        if (args.length > 0) {
            fis = new FileInputStream(args[0]);
        } else {
            fis = new FileInputStream("policy/src/examples/policy2.xml");
        }

        OMPolicyReader prdr = (OMPolicyReader) PolicyFactory
                .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
        StAXPolicyWriter pwrt = (StAXPolicyWriter) PolicyFactory
                .getPolicyWriter();

        Policy argOne = prdr.readPolicy(fis);
        Policy norm = (Policy) argOne.normalize();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pwrt.writePolicy(norm, baos);

        System.out.println(baos.toString());
        System.out.println("-----\n");

        fis.close();
        /*
         * Use standard Parser, w3c DOM
         */
        if (args.length > 0) {
            fis = new FileInputStream(args[0]);
        } else {
            fis = new FileInputStream("policy/src/examples/policy2.xml");
        }
        DOMPolicyReader readerDom = (DOMPolicyReader) PolicyFactory
                .getPolicyReader(PolicyFactory.DOM_POLICY_READER);

        Policy pDom = readerDom.readPolicy(fis);
        Policy normDom = (Policy) pDom.normalize();

        baos = new ByteArrayOutputStream();
        pwrt.writePolicy(normDom, baos);

        System.out.println(baos.toString());

        fis.close();

    }
}
