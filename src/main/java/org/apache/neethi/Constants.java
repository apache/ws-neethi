/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.neethi;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Constants contains the set of Constants that are used throughout the Neethi2
 * framework.
 * 
 */
public final class Constants {

    public static final String ATTR_NAME = "Name";

    public static final String ATTR_ID = "Id";

    public static final String ATTR_WSP = "wsp";

    public static final String ATTR_WSU = "wsu";
    
    public static final String ATTR_URI = "URI";
    public static final String URI_POLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public static final String URI_POLICY_15_DEPRECATED_NS = "http://www.w3.org/2006/07/ws-policy";
    public static final String URI_POLICY_15_NS = "http://www.w3.org/ns/ws-policy";

    public static final String URI_WSU_NS 
        = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String ELEM_POLICY = "Policy";

    public static final String ELEM_EXACTLYONE = "ExactlyOne";

    public static final String ELEM_ALL = "All";

    public static final String ELEM_POLICY_REF = "PolicyReference";

    public static final short TYPE_POLICY = 0x1;

    public static final short TYPE_EXACTLYONE = 0x2;

    public static final short TYPE_ALL = 0x3;

    public static final short TYPE_POLICY_REF = 0x4;

    public static final short TYPE_ASSERTION = 0x5;

    
    public static final String ATTR_OPTIONAL = "Optional";
    public static final String ATTR_IGNORABLE = "Ignorable";

    public static final QName Q_ELEM_POLICY 
        = new QName(Constants.URI_POLICY_NS, Constants.ELEM_POLICY, Constants.ATTR_WSP);
    public static final QName Q_ELEM_POLICY_15 
        = new QName(Constants.URI_POLICY_15_NS, Constants.ELEM_POLICY, Constants.ATTR_WSP);

    public static final QName Q_ELEM_OPTIONAL_ATTR 
        = new QName(Constants.URI_POLICY_NS, "Optional", Constants.ATTR_WSP);
    public static final QName Q_ELEM_OPTIONAL_15_ATTR
        = new QName(Constants.URI_POLICY_15_NS, "Optional", Constants.ATTR_WSP);

    public static final QName Q_ELEM_IGNORABLE_15_ATTR
        = new QName(Constants.URI_POLICY_15_NS, "Ignorable", Constants.ATTR_WSP);
    
    private Constants() {
        //utility class, never constructed
    }
    
    public static boolean isInPolicyNS(QName q) {
        String ns = q.getNamespaceURI();
        return URI_POLICY_NS.equals(ns)
            || URI_POLICY_15_DEPRECATED_NS.equals(ns)
            || URI_POLICY_15_NS.equals(ns);
    }
    public static boolean isPolicyElement(String ns, String local) {
        return (URI_POLICY_NS.equals(ns) 
            || URI_POLICY_15_DEPRECATED_NS.equals(ns)
            || URI_POLICY_15_NS.equals(ns)) && ELEM_POLICY.equals(local);
    }
    public static boolean isPolicyElement(QName q) {
        return isInPolicyNS(q) && ELEM_POLICY.equals(q.getLocalPart());
    }
    public static boolean isPolicyRef(QName q) {
        return isInPolicyNS(q) && ELEM_POLICY_REF.equals(q.getLocalPart());
    }
    
    //Try and figure out if we are outputting 1.5 or 1.2 policy
    //kind of a hack.  Would be better to add a "version" to the serialize method,
    //but that would be incompatible
    public static String findPolicyNamespace(XMLStreamWriter writer) throws XMLStreamException {
        String prefix = writer.getPrefix(Constants.URI_POLICY_15_NS);
        if (prefix == null || "".equals(prefix)) {
            prefix = writer.getPrefix(Constants.URI_POLICY_15_DEPRECATED_NS);
        }
        if (prefix == null || "".equals(prefix)) {
            return Constants.URI_POLICY_NS;
        }
        return Constants.URI_POLICY_15_NS;
    }
}
