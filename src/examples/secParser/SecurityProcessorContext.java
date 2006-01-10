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

import java.util.ArrayList;

import org.apache.ws.policy.PrimitiveAssertion;

public class SecurityProcessorContext {
	
	public static final int NONE = 0;
	public static final int START = 1;
	public static final int COMMIT = 2;
	public static final int ABORT = 3;

	public static final String[] ACTION_NAMES = new String[]{"NONE", "START", "COMMIT", "ABORT"};
	
	private ArrayList tokenStack = new ArrayList();

	private int tokenStackPointer = 0;
	
	private PrimitiveAssertion assertion = null;
	
	private int action = NONE;

	public SecurityProcessorContext() {
	}

	/**
	 * Gets the action to perform in the processing method.
	 * 
	 * @return The action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Sets to action to perform in the processing method.
	 * 
	 * @param action The actio to set. Either NONE, START, COMMIT, or ABORT
	 */
	public void setAction(int action) {
		this.action = action;
	}
	/**
	 * Get the current assertion that is being processed.
	 * 
	 * This is always a PrimitiveAssertion.
	 * 
	 * @return The current assertion.
	 */
	public PrimitiveAssertion getAssertion() {
		return assertion;
	}

	/**
	 * Set the current assertion that is being processed.
	 * 
	 * This is always a primitive assertion.
	 * 
	 * @param assertion The assertion to set
	 */
	public void setAssertion(PrimitiveAssertion assertion) {
		this.assertion = assertion;
	}

	/**
	 * Push a SecurityPolicyToken onto the token stack.
	 * 
	 * The pushed token becomes the current token. The current token is the
	 * starting point for further parsing.
	 * 
	 * @param spt
	 *            The SecurityPolicyToken to push on the stack
	 */
	public void pushSecurityToken(SecurityPolicyToken spt) {
		tokenStack.add(tokenStackPointer, spt);
		tokenStackPointer++;
	}

	/**
	 * Pop a SecurityPolicyToken from the token stack.
	 * 
	 * If the stack contains at least one token the method pops the topmost
	 * token from the stack and returns it. If the stack is empty the method
	 * returns a <code>null</code>.
	 * 
	 * @return The topmost SecurityPolicyToken or null if the stack is empty.
	 */
	public SecurityPolicyToken popSecurityToken() {
		if (tokenStackPointer > 0) {
			tokenStackPointer--;
			return (SecurityPolicyToken) tokenStack.get(tokenStackPointer);
		} else {
			return null;
		}
	}

	/**
	 * Reads and returns the current SecurityPolicyToken.
	 * 
	 * If the stack contains at least one token the method reads the topmost
	 * token from the stack and returns it. If the stack is empty the method
	 * returns a <code>null</code>. The metho does not remove the token from
	 * the stack.
	 * 
	 * @return The topmost SecurityPolicyToken or null if the stack is empty.
	 */
	public SecurityPolicyToken readCurrentSecurityToken() {
		if (tokenStackPointer > 0) {
			return (SecurityPolicyToken) tokenStack.get(tokenStackPointer - 1);
		} else {
			return null;
		}
	}

}
