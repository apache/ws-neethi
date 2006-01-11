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

import org.apache.ws.policy.PrimitiveAssertion;

/**
 * @author Werner Dittmann (werner@apache.org)
 * 
 */
public class AlgorithmSuiteProcessor {
	private boolean initializedAlgorithmSuite = false;

	/**
	 * Intialize the AlgorithmSuite complex token.
	 * 
	 * This method creates a copy of the AlgorithmSuite token and sets the
	 * handler object to the copy. Then it creates copies of the child tokens
	 * that are allowed for AlgorithmSuite. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of AlgorithmSuite.
	 * 
	 * <p/> The handler object that must contain the methods
	 * <code>doAlgorithmSuite</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */

	private void initializeAlgorithmSuite(SecurityPolicyToken spt)
			throws NoSuchMethodException {

		SecurityPolicyToken tmpSpt;

		tmpSpt = SecurityPolicy.basic256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDes.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesRsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Sha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesSha256.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic256Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic192Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.basic128Sha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.tripleDesSha256Rsa15.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.inclusiveC14N.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.soapNormalization10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.strTransform10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.xPath10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = SecurityPolicy.xPathFilter20.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

	public Object doAlgorithmSuite(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);

		SecurityPolicyToken spt = spc.readCurrentSecurityToken();
		switch (spc.getAction()) {

		case SecurityProcessorContext.START:
			if (!initializedAlgorithmSuite) {
				try {
					initializeAlgorithmSuite(spt);
					initializedAlgorithmSuite = true;
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

	public Object doBasic256(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic192(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic128(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doTripleDes(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic256Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic192Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic128Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doTripleDesRsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic256Sha256(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic192Sha256(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic128Sha256(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doTripleDesSha256(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic256Sha256Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic192Sha256Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doBasic128Sha256Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doTripleDesSha256Rsa15(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doInclusiveC14N(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doSoapNormalization10(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doStrTransform10(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doXPath10(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

	public Object doXPathFilter20(SecurityProcessorContext spc) {
		System.out.println("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
		return new Boolean(true);
	}

}
