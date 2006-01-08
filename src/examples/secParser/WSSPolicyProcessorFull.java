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

package examples.secParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.Assertion;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyFactory;


/**
 * @author Werner Dittmann (werner@apache.org)
 */

public class WSSPolicyProcessorFull {

    FileInputStream fis = null;

    PolicyReader prdr = null;

    Policy merged = null;

    int level = 0;

//    ArrayList securityTokens = new ArrayList();

    SecurityPolicyToken topLevel = new SecurityPolicyToken("_TopLevel_",
            SecurityPolicyToken.COMPLEX_TOKEN, true, null);
    
    SecurityPolicy secPolicy = null;

    public static void main(String[] args) throws Exception {

        WSSPolicyProcessorFull processor = new WSSPolicyProcessorFull();
        if (!processor.setup()) {
            return;
        }
        String[] files = new String[1];
        // files[0] = "policy/src/examples/policy2.xml";
        // files[0] = "policy/src/examples/SecurityPolicyMsg.xml";
        // processor.go(files);
        // System.out
        // .println("\n ----------------------------------------------------");
        files = new String[2];
        files[0] = "policy/src/examples/SecurityPolicyBindings.xml";
        files[1] = "policy/src/examples/SecurityPolicyMsg.xml";
        processor.go(files);
    }

    boolean setup() throws NoSuchMethodException {
        prdr = PolicyFactory.getPolicyReader(PolicyFactory.OM_POLICY_READER);
        secPolicy = new SecurityPolicy();
        
        SecurityPolicyToken spt = secPolicy.initializeSignedParts(this);
        topLevel.setChildToken(spt);
        
        return true;
    }

    void go(String[] args) {

        merged = null;
        for (int i = 0; i < args.length; i++) {
            try {
                fis = new FileInputStream(args[i]);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Policy newPolicy = prdr.readPolicy(fis);
            newPolicy = (Policy) newPolicy.normalize();
            // if (!newPolicy.isNormalized()) {
            // throw new RuntimeException("newPolicy is not in normalized
            // format");
            // }
            if (merged == null) {
                merged = newPolicy;
            } else {
                merged = (Policy) merged.merge(newPolicy);
            }
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        processPolicy(merged);
    }

    /**
     * This method takes a normalized policy object, processes it and returns
     * true if all assertion can be fulfilled.
     * 
     * Each policy must be nromalized accordig to the WS Policy framework
     * specification. Therefore a policy has one child (wsp:ExactlyOne) that is
     * a XorCompositeAssertion. This child may contain one or more other terms
     * (alternatives). To match the policy one of these terms (alternatives)
     * must match. If none of the contained terms match this policy cannot be
     * enforced.
     * 
     * @param policy
     *            The policy to process
     * @return True if this policy can be enforced by the policy enforcement
     *         implmentation
     */
    public boolean processPolicy(Policy policy) {

        if (!policy.isNormalized()) {
            throw new RuntimeException("Policy is not in normalized format");
        }

        XorCompositeAssertion xor = (XorCompositeAssertion) policy.getTerms()
                .get(0);
        List listOfPolicyAlternatives = xor.getTerms();

        boolean success = false;
        int numberOfAlternatives = listOfPolicyAlternatives.size();

        for (int i = 0; !success && i < numberOfAlternatives; i++) {
            AndCompositeAssertion aPolicyAlternative = (AndCompositeAssertion) listOfPolicyAlternatives
                    .get(i);

            List listOfAssertions = aPolicyAlternative.getTerms();

            Iterator iterator = listOfAssertions.iterator();
            /*
             * Loop over all assertions in this alternative. If all assertions
             * can be fulfilled then we choose this alternative and signal a
             * success.
             */
            boolean all = true;
            while (all && iterator.hasNext()) {
                Assertion assertion = (Assertion) iterator.next();
                if (assertion instanceof Policy) {
                    all = processPolicy((Policy) assertion);
                    continue;
                }
                if (!(assertion instanceof PrimitiveAssertion)) {
                    System.out.println("Got a unexpected assertion type: "
                            + assertion.getClass().getName());
                    continue;
                }
                all = processPrimitiveAssertion((PrimitiveAssertion) assertion);
            }
            /*
             * copy the status of assertion processing. If all is true the this
             * alternative is "success"ful
             */
            success = all;
        }
        return success;
    }

    boolean processPrimitiveAssertion(PrimitiveAssertion pa) {
        /*
         * We need to pick only the primitive assertions which conatain a
         * WSSecurityPolicy policy assertion. For that we'll check the namespace
         * of the primitive assertion
         */
        boolean commit = true;

        if (pa.getName().getNamespaceURI().equals(
                "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy")) {
            commit = startPolicyTransaction(pa);
        }

        List terms = pa.getTerms();
        if (terms.size() > 0) {
            for (int i = 0; commit && i < terms.size(); i++) {
                level++;
                Assertion assertion = (Assertion) pa.getTerms().get(i);
                if (assertion instanceof Policy) {
                    assertion = assertion.normalize();
                    commit = processPolicy((Policy) assertion);
                } else if (assertion instanceof PrimitiveAssertion) {
                    commit = processPrimitiveAssertion((PrimitiveAssertion) assertion);
                }
                level--;
            }
        }
        if (commit) {
            commitPolicyTransaction(pa);
        } else {
            abortPolicyTransaction(pa);
        }
        return commit;
    }

    public boolean startPolicyTransaction(PrimitiveAssertion prim) {

        /*
         * May be I should be setting the configuration options in
         * WSDoAll*Handler according to this security assertion.
         */
        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }
        String tokenName = prim.getName().getLocalPart();
        System.out.println(new String(indent) + tokenName);
        String text = prim.getStrValue();
        if (text != null) {
            text = text.trim();
            System.out
                    .println(new String(indent) + "Value: '" + text.toString() + "'");
        }
        SecurityPolicyToken spt = topLevel.getChildToken(tokenName);
        SecurityProcessorContext spc = new SecurityProcessorContext();
        if (spt != null) {
            try {
                System.out.println("SPT: " + spt);
                spt.invokeProcessTokenMethod(spc);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    public void abortPolicyTransaction(PrimitiveAssertion prim) {
        System.out.println("Aborting Policy transaction "
                + prim.getName().getLocalPart());
    }

    public void commitPolicyTransaction(PrimitiveAssertion prim) {
        System.out.println("Commit Policy transaction "
                + prim.getName().getLocalPart());
    }

    public Object doSignedParts(SecurityProcessorContext spc) {
        System.out.println("We found a SignedParts token");
        return new Boolean(true);
    }
    
    public Object doBody(SecurityProcessorContext spc) {
        System.out.println("We found a Body token");
        return new Boolean(true);
    }
    
    public Object doHeader(SecurityProcessorContext spc) {
        System.out.println("We found a Header token");
        return new Boolean(true);
    }
    
}
