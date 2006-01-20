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

import secParser.SecurityPolicy;
import secParser.SecurityPolicyToken;
import secParser.SecurityProcessorContext;

import org.apache.ws.policy.PrimitiveAssertion;

/**
 * @author Werner Dittmann (werner@apache.org)
 */

public class EncryptedPartsElementsProcessor {

	private boolean initializedEncryptedParts = false;

	private boolean initializedEncryptedElements = false;

	/**
	 * Intialize the EncryptedParts complex token.
	 * 
	 * This method creates copies of the child tokens that are allowed for
	 * SignedParts. These tokens are Body and Header. These copies are
	 * initialized with handler object and then set as child tokens of
	 * EncryptedParts. <p/> The handler object must define the methods
	 * <code>doSignedParts, doBody, doHeader</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeEncryptedParts(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.body.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.header.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

	}

	/**
	 * Intialize the EncryptedElements complex token.
	 * 
	 * This method creates a copy of the child token that is allowed for
	 * EncryptedElements. The token is XPath. This copy is initialized with a
	 * handler object and then set as child token of EncryptedElements. <p/> The
	 * handler object must define the method <code>doXPath</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeEncryptedElements(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.xPath.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doEncryptedParts(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedEncryptedParts) {
				try {
					initializeEncryptedParts(spt);
					initializedEncryptedParts = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			System.out.println(spt.getTokenName());
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

	public Object doEncryptedElements(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedEncryptedElements) {
				try {
					initializeEncryptedElements(spt);
					initializedEncryptedElements = true;
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return new Boolean(false);
				}
			}
			System.out.println(spt.getTokenName());
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

	public Object doBody(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doHeader(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doXPath(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

}
