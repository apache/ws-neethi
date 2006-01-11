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
package examples.secParser.processors;

import examples.secParser.SecurityPolicy;
import examples.secParser.SecurityPolicyToken;
import examples.secParser.SecurityProcessorContext;

/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class EndorsingSupportingTokensProcessor {
	private boolean initializedEndorsingSupportingTokens = false;

	/**
	 * Intialize the EndorsingSupportingTokens complex token.
	 * 
	 * This method creates a copy of the EndorsingSupportingTokens token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for EndorsingSupportingTokens. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of EndorsingSupportingTokens.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeEndorsingSupportingTokens(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.x509Token.copy();
		tmpSpt.setProcessTokenMethod(new X509TokenProcessor());
		spt.setChildToken(tmpSpt);
		
		tmpSpt = SecurityPolicy.usernameToken.copy();
		tmpSpt.setProcessTokenMethod(new UsernameTokenProcessor());
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.algorithmSuite.copy();
		tmpSpt.setProcessTokenMethod(new AlgorithmSuiteProcessor());
		spt.setChildToken(tmpSpt);

		SignedPartsElementsProcessor spep = new SignedPartsElementsProcessor();
		tmpSpt = SecurityPolicy.signedParts.copy();
		tmpSpt.setProcessTokenMethod(spep);
		spt.setChildToken(tmpSpt);
		
		tmpSpt = SecurityPolicy.signedElements.copy();
		tmpSpt.setProcessTokenMethod(spep);		
		spt.setChildToken(tmpSpt);

		EncryptedPartsElementsProcessor epep = new EncryptedPartsElementsProcessor();
		tmpSpt = SecurityPolicy.encryptedParts.copy();
		tmpSpt.setProcessTokenMethod(epep);
		spt.setChildToken(tmpSpt);
		
		tmpSpt = SecurityPolicy.encryptedElements.copy();
		tmpSpt.setProcessTokenMethod(epep);
		spt.setChildToken(tmpSpt);

	}

	public Object doEndorsingSupportingTokens(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedEndorsingSupportingTokens) {
				try {
					initializeEndorsingSupportingTokens(spt);
					initializedEndorsingSupportingTokens = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			break;
		case SecurityProcessorContext.COMMIT:
			break;
		case SecurityProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}

}
