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
package secParser.processors;

import org.apache.ws.policy.PrimitiveAssertion;

import secParser.SecurityPolicy;
import secParser.SecurityPolicyToken;
import secParser.SecurityProcessorContext;

/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class Trust10Processor {

	private boolean initializedTrust10 = false;

	/**
	 * Intialize the Trust10 complex token.
	 * 
	 * This method creates a copy of the Trust10 token and sets the handler object
	 * to the copy. Then it creates copies of the child tokens that are allowed
	 * for Trust10. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of Trust10.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doTrust10</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeTrust10(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.mustSupportClientChallenge
				.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportServerChallenge.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.requireClientEntropy.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.requireServerEntropy.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.mustSupportIssuedTokens.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doTrust10(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedTrust10) {
				try {
					initializeTrust10(spt);
					initializedTrust10 = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			PrimitiveAssertion pa = spc.getAssertion();
			String text = pa.getStrValue();
			if (text != null) {
				text = text.trim();
				System.out.println("Value: '" + text.toString() + "'");
			}
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}
	
	public Object doMustSupportClientChallenge(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doMustSupportServerChallenge(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doRequireClientEntropy(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doRequireServerEntropy(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doMustSupportIssuedTokens(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}
}
