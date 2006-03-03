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

import java.io.InputStream;

import org.apache.ws.policy.Policy;

/**
 * PolicyReader is an interface which every PolicyReader must implement. It
 * contains a single method to create a Policy object from an InputStream.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public interface PolicyReader {

	/**
	 * Creates a Policy object from an InputStream.
	 * 
	 * @param inputStream
	 *            the inputStream of the source file
	 * @return a policy object constructed from the InputStream
	 * @throws RuntimeException
	 *             if an error occurs while constructing the Policy object
	 */
	public Policy readPolicy(InputStream inputStream) throws RuntimeException;
}