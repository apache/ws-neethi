/*
 * Copyright 2004,2006 The Apache Software Foundation.
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
package org.apache.ws.policy;

/**
 * <p>Common PUBLIC constants used throughout the package.
 * 
 * @version 1.0.0
 */
public interface TestConstants {
 
  /**
    * Namespace for WS-Policy.
    */
  public static final String NAMESPACE_WSP = "http://schemas.xmlsoap.org/ws/2004/09/policy".intern();
  /**
    * Namespace for Web service utilities.
    */
  public static final String NAMESPACE_WSU =
    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd".intern();

  /**
  	* Namespace for Web service security.
  	*/
  public static final String NAMESPACE_WSSE =
    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".intern();

  /**
   * Namespace for WS-SecurityPolicy.
   */
  public static final String NAMESPACE_WSE = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy".intern();
  
  /**
  	* Namespace for Web service addressing.
  	*/
  public static final String NAMESPACE_WSA = "http://schemas.xmlsoap.org/ws/2004/08/addressing".intern();
  
  /**
  * Namespace for WSDL
  */
  public static final String NAMESPACE_WSDL = "http://schemas.xmlsoap.org/wsdl/".intern();

  /**
  * Namespace for XML addressing.
  */
  public static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace".intern();

  /**
   * Namespace of xmlns prefix when defining namespace aliases
   */
  public static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/".intern();

  /**
   * Namespace definition
   */
  public static final String NAMESPACE_DEF = "http://www.w3.org/XML/1998/namespace".intern();

  // ***********************************************************************
  //                      Operator Constants
  // ***********************************************************************
  /**
    * WS-Policy <i>All</i> Operator String value.
    */
  public static final String OPERATOR_ALL = "All".intern();
  /**
  	* WS-Policy <i>ExactlyOne</i> Operator String value.
  	*/
  public static final String OPERATOR_EXACTLYONE = "ExactlyOne".intern();
  /**
  	* WS-Policy <i>Policy</i> Operator String value.		 
  	*/
  public static final String OPERATOR_POLICY = "Policy".intern();
	/**
		* WS-Policy <i>Optional</i> attribute String value.		 
		*/
	 public static final String ATTRIBUTE_OPTIONAL = "Optional".intern();
  // ***********************************************************************
  // Element constants
  // ***********************************************************************
  /**
  	* WS-Policy <i>Policy</i> Element String value.		
  	*/
  public static final String ELEMENT_POLICY = "Policy".intern();
	/**
	 * WS-Policy <i>PolicyReference</i> Element String value.		
	 */
  public static final String ELEMENT_POLICYREF = "PolicyReference".intern();

  // ***********************************************************************
  //                      AppliesTo Constants
  //
  // These constants refer to constants used in the definition of
  // scope in the AppliesTo class.
  // ***********************************************************************
  /**
  	* AppliesTo attribute to identify <code>WS-Addressing</code> Object.
  	*/
  public static final String ATTACH_ENDPOINT = "EndpointReference".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL service at which Policy is attached.
  	*/
  public static final String ATTACH_SERVICE = "Service".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL portType definition at which Policy is attached.
  	*/
  public static final String ATTACH_PORTTYPE = "PortType".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL binding at which Policy is attached.
  	*/
  public static final String ATTACH_BINDING = "Binding".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL operation at which Policy is attached.
  	*/
  public static final String ATTACH_OPERATION = "Operation".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL input message at which Policy is attached.
  	*/
  public static final String ATTACH_INPUT = "Input".intern();
  /**
   * AppliesTo attribute to identify name of WSDL output message at which Policy is attached.
   */
  public static final String ATTACH_OUTPUT = "Output".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL fault message at which Policy is attached.
  	*/
  public static final String ATTACH_FAULT = "Fault".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL port at which Policy is attached.
  	*/
  public static final String ATTACH_PORT = "Port".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL message definition at which Policy is attached.
  	*/
  public static final String ATTACH_MESSAGE = "Message".intern();

  /**
  	* AppliesTo attribute to identify name of WSDL message part definition at which Policy is attached.
  	*/
  public static final String ATTACH_ADDRESS = "Address".intern();

  /**
  	* AppliesTo attribute to identify name of WSDL message part definition at which Policy is attached.
  	*/
  public static final String ATTACH_SERVICENAME = "ServiceName".intern();

  /**
  	* AppliesTo attribute to identify name of WSDL operation at which Policy is attached.
  	*/
  public static final String ATTACH_BINDING_OPERATION = "BindingOperation".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL input message at which Policy is attached.
  	*/
  public static final String ATTACH_BINDING_INPUT = "BindingInput".intern();
  /**
   * AppliesTo attribute to identify name of WSDL output message at which Policy is attached.
   */
  public static final String ATTACH_BINDING_OUTPUT = "BindingOutput".intern();
  /**
  	* AppliesTo attribute to identify name of WSDL fault message at which Policy is attached.
  	*/
  public static final String ATTACH_BINDING_FAULT = "BindingFault".intern();

  // **************************************************************************
  // *                Domain specific constants
  // **************************************************************************
  public static String NAMESPACE_CAL = "http://policy.test.webservices.com/calendar";
  public static String NAMESPACE_UTIL = "http://policy.test.webservices.com/util";
  public static String NAMESPACE_FORM = "http://policy.test.webservices.com/form";

  // These are some of the predefined values for the assertions.
  public static String VALUE_NUMERIC = "Numeric";
  public static String VALUE_ALPHA = "Alpha";
  public static String VALUE_24_HOUR = "24";
  public static String VALUE_12_HOUR = "12";
  public static String VALUE_ID = "ID";
  public static String VALUE_TIME = "Time";
  public static String VALUE_RED = "Red";
  public static String VALUE_GREEN = "Green";
  public static String VALUE_BLUE = "Blue";
  public static String VALUE_LARGE = "Large";
  public static String VALUE_MEDIUM = "Medium";
  public static String VALUE_SMALL = "Small";
  public static String VALUE_SQUARE = "Square";
  public static String VALUE_CIRCLE = "Circle";
  public static String VALUE_TRIANGLE = "Triangle";

  // These are the supported Assertions (local names)
  public static String ASSERTION_MINUTE = "Minute";
  public static String ASSERTION_SECOND = "Second";
  public static String ASSERTION_HOUR = "Hour";
  public static String ASSERTION_MONTH = "Month";
  public static String ASSERTION_YEAR = "Year";
  public static String ASSERTION_LOG = "Log";
  public static String ASSERTION_UNKNOWN = "Unknown";
  public static String ASSERTION_YEAR_DAY = "YearDay";
  public static String ASSERTION_WEEK_DAY = "WeekDay";
  public static String ASSERTION_MONTH_DAY = "MonthDay";
  public static String ASSERTION_DAY = "Day";
  public static String ASSERTION_ZONE = "Zone";
  
  public static String ASSERTION_WIDGET = "Widget";
  public static String ASSERTION_COLOR = "Color";
  public static String ASSERTION_SHAPE = "Shape";
  public static String ASSERTION_SIZE = "Size";
  public static String ASSERTION_QUANTITY = "Quantity";
  
  // These are more complex ones
  public static String ASSERTION_RANGE = "Range";
  public static String ASSERTION_SET = "Set";

  // Supported RANGE attributes
  public static String RANGE_ATTRIBUTE_TYPE = "type";
  public static String RANGE_ATTRIBUTE_LOWER = "lower";
  public static String RANGE_ATTRIBUTE_UPPER = "upper";
  //Supported RANGE TYPE values
  public static String RANGE_TYPE_YEAR = ASSERTION_YEAR;
  public static String RANGE_TYPE_HOUR = ASSERTION_HOUR;

  // Supported SET attributes
  public static String SET_ATTRIBUTE_TYPE = "type";
  public static String SET_ATTRIBUTE_VALUES = "values";
  //Supported SET TYPE values
  public static String SET_TYPE_MONTH = ASSERTION_MONTH;
 
}
