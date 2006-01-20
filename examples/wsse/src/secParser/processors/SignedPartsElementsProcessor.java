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
 */

public class SignedPartsElementsProcessor {

	private boolean initializedSignedParts = false;

	private boolean initializedSignedElements = false;

	public SignedPartsElementsProcessor() {
	}

	/**
	 * Intialize the SignedParts complex token.
	 * 
	 * This method creates copies of the child tokens that are allowed for
	 * SignedParts. These tokens are Body and Header. These copies are
	 * initialized with handler object and then set as child tokens of
	 * SignedParts. <p/> The handler object must define the methods
	 * <code>doSignedParts, doBody, doHeader</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeSignedParts(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.body.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.header.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

	}

	/**
	 * Intialize the SignedElements complex token.
	 * 
	 * This method creates a copy of the child token that is allowed for
	 * SignedElements. The token is XPath. This copy is initialized with a
	 * handler object and then set as child token of SignedElements. <p/> The
	 * handler object must define the method <code>doXPath</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeSignedElements(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		SecurityPolicyToken tmpSpt = SecurityPolicy.xPath.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doSignedParts(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedSignedParts) {
				try {
					initializeSignedParts(spt);
					initializedSignedParts = true;
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

	public Object doSignedElements(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();

		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedSignedElements) {
				try {
					initializeSignedElements(spt);
					initializedSignedElements = true;
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
