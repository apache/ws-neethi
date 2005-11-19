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

package org.apache.policy.parser;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMElement;
import org.apache.policy.model.Policy;
import org.apache.policy.model.PrimitiveAssertion;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class WSParserUtil {
    
    public static PrimitiveAssertion getPrimitiveAssertion(OMElement element) {
        QName qname = element.getQName();
        
        PrimitiveAssertion result = new PrimitiveAssertion(qname);
        
        // setting the text value ..
        String strValue = element.getText();
        if (strValue != null && strValue.length() != 0) {
            result.setStrValue(strValue);            
        }
        
//        Iterator childElements = element.getChildElements();
//        if (!(childElements.hasNext())) {
//            result.addTerm(element);
//            return result;
//        }
                
        //CHECK ME
        Iterator childElements = element.getChildElements();
        while (childElements.hasNext()) {
            OMElement childElement = (OMElement) childElements.next();
            
            if (childElement.getNamespace().getName().equals(WSPConstants.WS_POLICY_NAMESPACE_URI)
                    && childElement.getLocalName().equals(WSPConstants.WS_POLICY)) {
                Policy policy = WSPolicyParser.getInstance().getPolicy(childElement);
                result.addTerm(policy);
            } else {
                PrimitiveAssertion pa = getPrimitiveAssertion(childElement);
                result.addTerm(pa);                
            }
        }
                
        return result;       
    }

}
