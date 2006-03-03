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

package org.apache.ws.policy.util;

import java.io.OutputStream;

import org.apache.ws.policy.Policy;

/**
 * PolicyWriter is an interface which all PolicyWriters must implement. It
 * defines a single method which writes a policy to an OutputStream.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public interface PolicyWriter {

	/**
	 * Writes a Policy object to an OutputStream.
	 * 
	 * @param policy
	 *            the Policy object to write
	 * @param outputStream
	 *            the OutputStream to which the Policy obect should be written
	 */
	public void writePolicy(Policy policy, OutputStream outputStream);
}