package org.apache.ws.policy;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * PolicyConstants interfaces defines some CONST VALUES that are used in the
 * entier framework.
 */
public interface PolicyConstants {

	/** Tag name of AndComposteAssertion */
	public static final String AND_COMPOSITE_ASSERTION = "All";

	/** Tag name of XorCompositeAssertion */
	public static final String XOR_COMPOSITE_ASSERTION = "ExactlyOne";

	/** Tag name of Policy */
	public static final String WS_POLICY = "Policy";

	/** Prefix of WS-Policy namespace */
	public static final String WS_POLICY_PREFIX = "wsp";

	/** Prefix of WSU namespace */
	public static final String WSU_NAMESPACE_PREFIX = "wsu";

	/** Tag name of PolicyReference */
	public static final String WS_POLICY_REFERENCE = "PolicyReference";

	/** Namespace of WS-Policy */
	public static final String WS_POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy";

	/** Namespace of WSU */
	public static final String WSU_NAMESPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	/** XML namespace */
	public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
  
  /** Policy Id attribute **/
  public static final String WS_POLICY_ID = "Id";
  
  /** Policy Name attribute **/
  public static final String WS_POLICY_NAME = "Name";
  
  /** Policy base attribute **/
  public static final String WS_POLICY_BASE = "base";
  
  /** Namespace of xmlns prefix when defining namespace aliases  */
  public static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";

}