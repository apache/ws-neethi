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
package org.apache.neethi;

import javax.xml.namespace.QName;

/**
 * Constants contains the set of Constants that are used throughout the Neethi2
 * framework.
 * 
 */
public class Constants {

    public static final String ATTR_NAME = "Name";

    public static final String ATTR_ID = "Id";

    public static final String ATTR_WSP = "wsp";

    public static final String ATTR_WSU = "wsu";

    public static final String URI_POLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String URI_WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String ELEM_POLICY = "Policy";

    public static final String ELEM_EXACTLYONE = "ExactlyOne";

    public static final String ELEM_ALL = "All";

    public static final String ELEM_POLICYREF = "PolicyReference";

    public static final short TYPE_POLICY = 0x1;

    public static final short TYPE_EXACTLYONE = 0x2;

    public static final short TYPE_ALL = 0x3;

    public static final short TYPE_POLICYREF = 0x4;

    public static final short TYPE_ASSERTION = 0x5;

    public static final QName Q_ELEM_POLICY = new QName(
            Constants.URI_POLICY_NS, Constants.ELEM_POLICY, Constants.ATTR_WSP);

    public static final QName Q_ELEM_OPTIONAL_ATTR = new QName(
            Constants.URI_POLICY_NS, "Optional", Constants.ATTR_WSP);
}
