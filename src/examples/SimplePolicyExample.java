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
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyWriter;

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

        PolicyReader prdr = PolicyFactory
                .getPolicyReader(PolicyFactory.OM_POLICY_READER);
        PolicyWriter pwrt = PolicyFactory
                .getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);

        Policy argOne = prdr.readPolicy(fis);
        Policy norm = (Policy) argOne.normalize();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pwrt.writePolicy(norm, baos);

        System.out.println("Policy Id: " + norm.getId());
        System.out.println("Policy base: " + norm.getBase());
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
        prdr = PolicyFactory
                .getPolicyReader(PolicyFactory.DOM_POLICY_READER);

        argOne = prdr.readPolicy(fis);
        norm = (Policy) argOne.normalize();

        baos = new ByteArrayOutputStream();
        pwrt.writePolicy(norm, baos);

        System.out.println("Policy Id: " + norm.getId());
        System.out.println("Policy base: " + norm.getBase());
        System.out.println(baos.toString());

        fis.close();

    }
}
