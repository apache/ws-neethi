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

/**
 * @author Werner Dittmann (werner@apache.org)
 */
public class X509TokenProcessor {
	private boolean initializedUsernameToken = false;

	private SecurityPolicy secPol = new SecurityPolicy();


	
	/**
	 * Intialize the X509 complex token.
	 * 
	 * This method creates a copy of the X509Token token and sets the handler
	 * object to the copy. Then it creates copies of the child tokens that are
	 * allowed for X509Token. These tokens are:
	 * 
	 * These copies are also initialized with the handler object and then set as
	 * child tokens of X509Token.
	 * 
	 * <p/> 
	 * The handler object that must contain the methods
	 * <code>doX509Token</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	private void initializeX509Token(SecurityPolicyToken spt)
			throws NoSuchMethodException {
//		SecurityPolicyToken spt = secPol.x509Token.copy();
//		spt.setProcessTokenMethod(handler);

		SecurityPolicyToken tmpSpt = secPol.requireKeyIdentifierReference.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.requireIssuerSerialReference.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.requireEmbeddedTokenReference.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.requireThumbprintReference.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509V1Token10.copy(); 
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509V3Token10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509Pkcs7Token10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509PkiPathV1Token10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509V1Token11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509V3Token11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssX509Pkcs7Token11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
		
		tmpSpt = secPol.wssX509PkiPathV1Token11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}
}
