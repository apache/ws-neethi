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
public class UsernameTokenProcessor {

	private boolean initializedUsernameToken = false;

	private SecurityPolicy secPol = new SecurityPolicy();

	/**
	 * Intialize the UsernameToken complex token.
	 * 
	 * This method creates copies of the child tokens that are allowed for
	 * UsernameToken. These tokens are WssUsernameToken10 and
	 * WssUsernameToken11. These copies are also initialized with the handler
	 * object and then set as child tokens of UsernameToken.
	 * 
	 * <p/> The handler object must define the methods
	 * <code>doWssUsernameToken10, doWssUsernameToken11</code>.
	 * 
	 * @param spt
	 *            The token that will hold the child tokens.
	 * @throws NoSuchMethodException
	 */
	public void initializeUsernameToken(SecurityPolicyToken spt)
			throws NoSuchMethodException {
		// SecurityPolicyToken spt = secPol.usernameToken.copy();
		// spt.setProcessTokenMethod(handler);

		SecurityPolicyToken tmpSpt = secPol.wssUsernameToken10.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);

		tmpSpt = secPol.wssUsernameToken11.copy();
		tmpSpt.setProcessTokenMethod(this);
		spt.setChildToken(tmpSpt);
	}

}
