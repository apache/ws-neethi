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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

	public static void main(String[] args) throws Exception {

		WSSPolicyProcessorFull processor = new WSSPolicyProcessorFull();
		processor.setup();
		String[] files = new String[1];
		files[0] = "policy/src/examples/policy2.xml";
		processor.go(files);
		System.out
				.println("\n ----------------------------------------------------");
		files = new String[2];
		files[0] = "policy/src/examples/SecurityPolicyBindings.xml";
		files[1] = "policy/src/examples/SecurityPolicyMsg.xml";
		processor.go(files);
	}

	void setup() {
		prdr = PolicyFactory.getPolicyReader(PolicyFactory.OM_POLICY_READER);

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

	/*
	 * This method takes a policy object which contains policy alternatives.
	 * WSS4J framework should configure it self in accordance with
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

		int numberOfAlternatives = listOfPolicyAlternatives.size();

		for (int i = 0; i < numberOfAlternatives; i++) {
			AndCompositeAssertion aPolicyAlternative = (AndCompositeAssertion) listOfPolicyAlternatives
					.get(i);

			List listOfAssertions = aPolicyAlternative.getTerms();

			Iterator iterator = listOfAssertions.iterator();
			while (iterator.hasNext()) {
				Assertion assertion = (Assertion) iterator.next();
				if (assertion instanceof Policy) {
					processPolicy((Policy) assertion);
					continue;
				}
				if (!(assertion instanceof PrimitiveAssertion)) {
					System.out.println("Got a unexpected assertion type: "
							+ assertion.getClass().getName());
					continue;
				}
				processPrimitiveAssertion((PrimitiveAssertion) assertion);
			}
		}
	}

	void processPrimitiveAssertion(PrimitiveAssertion pa) {
		/*
		 * We need to pick only the primitive assertions which conatain a
		 * WSSecurityPolicy policy assertion. For that we'll check the namespace
		 * of the primitive assertion
		 */
		if (pa.getName().getNamespaceURI().equals(
				"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy")) {
			loadConfigurations(pa);
		}

		List terms = pa.getTerms();
		if (terms.size() > 0) {
			for (int i = 0; i < terms.size(); i++) {
				level++;
				Assertion assertion = (Assertion) pa.getTerms().get(i);
				if (assertion instanceof Policy) {
					assertion = assertion.normalize();
					processPolicy((Policy) assertion);
				} else if (assertion instanceof PrimitiveAssertion) {
					processPrimitiveAssertion((PrimitiveAssertion) assertion);
				}
				level--;
			}
		}
	}

	public void loadConfigurations(PrimitiveAssertion prim) {

		/*
		 * May be I should be setting the configuration options in
		 * WSDoAll*Handler according to this security assertion.
		 */
		StringBuffer indent = new StringBuffer();
		for (int i = 0; i < level; i++) {
			indent.append("  ");
		}
		System.out.println(new String(indent) + prim.getName().getLocalPart());

	}

}
