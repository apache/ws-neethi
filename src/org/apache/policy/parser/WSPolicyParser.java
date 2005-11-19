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

package org.apache.policy.parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.policy.model.AndCompositeAssertion;
import org.apache.policy.model.Assertion;
import org.apache.policy.model.CompositeAssertion;
import org.apache.policy.model.Policy;
import org.apache.policy.model.PolicyReference;
import org.apache.policy.model.PrimitiveAssertion;
import org.apache.policy.model.XorCompositeAssertion;
import org.apache.policy.util.StringUtils;

/**
 * WSPolicyParser provides methods to build a Policy Model form an InputStream
 * and to write a Policy Model to an OutputStream.
 * 
 */
public class WSPolicyParser {
    public static WSPolicyParser self = null;
    
    private WSPolicyParser() {
    }
    
    public static WSPolicyParser getInstance() {
        if (self == null) {
            self = new WSPolicyParser();
        }
        return self;
    }
    
    public Policy buildPolicyModel(InputStream in) {
                
        Policy model = null;

        try {
            XMLStreamReader xmlr = 
                    XMLInputFactory.newInstance().createXMLStreamReader(in);
            OMXMLParserWrapper builder = 
                    OMXMLBuilderFactory.createStAXOMBuilder(OMAbstractFactory.getOMFactory(), xmlr);
            
            OMElement root = builder.getDocumentElement();
                    
            model = getPolicy(root);
            
        } catch (XMLStreamException ex) {
            throw new RuntimeException("error : " + ex.getMessage());   
        }
        
        return model;
    }
    
    public Policy getPolicy(OMElement value) {
        if (value.getNamespace().getName().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) ||
                value.getLocalName().equals(WSPConstants.WS_POLICY)) {
            
            Policy policy = new Policy();
            OMAttribute xmlBase = getAttribute(value, new QName("", "base"));
            
            if (xmlBase != null) {
                policy.setBase(xmlBase.getAttributeValue());
            }
            
            OMAttribute id = getAttribute(value, new QName(WSPConstants.WSU_NAMESPACE_URI, "Id"));
                        
            if (id != null) {
                policy.setId(id.getAttributeValue());
            }
            
            Iterator children = value.getChildren();
            
            while (children.hasNext()){
                
                OMNode node = (OMNode) children.next();
                
                if (node instanceof OMElement){
                    OMElement ome = (OMElement) node;
                    
                    if (isCompositeAssertion(ome)) {
                        policy.addTerm(getCompositeAssertion(ome));
                    } else if (isPolicyReference(ome)){
                        policy.addTerm(getPolicyReference(ome));
                    } else {
                        policy.addTerm(getPrimitiveAssertion(ome));
                    }
                    
                }
            }
            return policy;
            
            
        }
        throw new IllegalArgumentException("Error : input is not a vaild policy element");  
    }
    
    public Assertion getCompositeAssertion(OMElement value) {
        CompositeAssertion compositeAssertion = null;
        
        if (value.getLocalName().equals(WSPConstants.WS_POLICY)) {
            String policyURI = "{" + value.getNamespace().getName() + "}" + value.getLocalName();
            compositeAssertion = new Policy(policyURI);
            
        } else if  (value.getLocalName().equals(WSPConstants.AND_COMPOSITE_ASSERTION)) {
            compositeAssertion = new AndCompositeAssertion();
            
        } else  if (value.getLocalName().equals(WSPConstants.XOR_COMPOSITE_ASSERTION)) {
            compositeAssertion = new XorCompositeAssertion();
            
        } else if (value.getLocalName().equals(WSPConstants.WS_POLICY_REFERENCE)) {
 
            OMAttribute uriAttr = value.getAttribute(new QName("URI"));
            
            return new PolicyReference(uriAttr.getAttributeValue());
            
//          try {
//              URI policyURI = new URI(uriAttr.getValue());
//              URL policyURL = policyURI.toURL();
//              return buildPolicyModel(policyURL.openStream());
//              
//              
//          } catch (Exception ex) {
//              throw new RuntimeException("error : " + ex.getMessage());
//          }
            
        } else {
            throw new IllegalArgumentException("cannot resolve the argument to" +
                    "a composite assertion");
        }
        
        Iterator children = value.getChildren();
        
        while (children.hasNext()){
            OMNode node = (OMNode) children.next();
            
            if (node instanceof OMElement){
                OMElement ome = (OMElement) node;
                
                if (isCompositeAssertion(ome)) {
                    compositeAssertion.addTerm(getCompositeAssertion(ome));
                } else if (isPolicyReference(ome)){
                    compositeAssertion.addTerm(getPolicyReference(ome));
                } else {
                    compositeAssertion.addTerm(getPrimitiveAssertion(ome));
                }
                
            }
        }
        return compositeAssertion;
    }
    
    public PrimitiveAssertion getPrimitiveAssertion(OMElement value) {
//      QName qname = new QName(value.getNamespace().getName(), value.getLocalName());
//      return new PrimitiveAssertion(qname, value);
    	return WSParserUtil.getPrimitiveAssertion(value);
          
    }
        
    public boolean isCompositeAssertion(OMElement value) {
        
        return (value.getNamespace().getName().equals(WSPConstants.WS_POLICY_NAMESPACE_URI)) 
                &&  (value.getLocalName().equals(WSPConstants.WS_POLICY)
                ||  value.getLocalName().equals(WSPConstants.AND_COMPOSITE_ASSERTION)
                ||  value.getLocalName().equals(WSPConstants.XOR_COMPOSITE_ASSERTION));
    }
    
    public boolean isPolicyReference(OMElement value) {
        return ((value.getNamespace().getName().equals(WSPConstants.WS_POLICY_NAMESPACE_URI)))
                && (value.getLocalName().equals(WSPConstants.WS_POLICY_REFERENCE));
    }
    
    public Assertion getPolicyReference(OMElement ome) {
        OMAttribute attri = getAttribute(ome, new QName("", "URI"));
        String uriString = attri.getAttributeValue();
        return new PolicyReference(uriString);
    }
    
    public void printModel(Policy model, OutputStream out) {
        PrintWriter pw = new PrintWriter(out, true);
        printAssertion(0, model, pw);
    }
    
    public void printAssertion(int tab, Assertion assertion, PrintWriter pw) {
        if (assertion instanceof PrimitiveAssertion) {
            printPrimitiveAssertion(tab, (PrimitiveAssertion) assertion, pw); 
        } else if (assertion instanceof CompositeAssertion) {
            printCompositeAssertion(tab, (CompositeAssertion) assertion, pw);
        }
    }
    
    public void printPrimitiveAssertion(int tab, PrimitiveAssertion primitive, PrintWriter pw) {
        //printOMElement(tab, (OMElement) primitive.getValue(), pw);
                
        pw.print(StringUtils.getChars(tab, ' '));
        pw.print("<" + primitive.getName().getPrefix() + ":" + primitive.getName().getLocalPart() + " xmlns:"
                + primitive.getName().getPrefix() + "=\""+ primitive.getName().getNamespaceURI() + "\"");
        
        Iterator attributeNames = primitive.getAttributes().keySet().iterator();
        
        while (attributeNames.hasNext()) {
            QName qname = (QName) attributeNames.next();
            String value = primitive.getAttribute(qname);
            
            pw.print(" "+ qname.getLocalPart() + "=\"" 
                    +  value+ "\"");
        }
        
        Iterator children = primitive.getTerms().iterator();
        
        if (children.hasNext()) {
            pw.println(">");

            // text
            if (primitive.getStrValue() != null) {
                printText(tab + 4, primitive.getStrValue(), pw);
            }
            
            do {
                Object child = children.next();
                
                if (child instanceof PrimitiveAssertion) { 
                    printPrimitiveAssertion(tab + 4, (PrimitiveAssertion) child, pw);
                    
                } else if (child instanceof CompositeAssertion) {
                    printCompositeAssertion(tab + 4, (CompositeAssertion) child, pw);
                    
                } else {
                    // TODO exception ?
                }
                
            } while (children.hasNext());
            
            pw.print(StringUtils.getChars(tab, ' '));
            pw.println("</" + primitive.getName().getPrefix() + ":" + primitive.getName().getLocalPart() + ">");
            
        } else {
            pw.println("/>");
        }       
        
        
    }
    
    private void printCompositeAssertion(int tab, CompositeAssertion composite, PrintWriter pw) {
        if (composite instanceof Policy) {
            pw.print(StringUtils.getChars(tab, ' ') + "<wsp:Policy xmlns:wsp=\"" 
                    + WSPConstants.WS_POLICY_NAMESPACE_URI + "\"");
            
            Iterator iterator = composite.getTerms().iterator();
            if (iterator.hasNext()) {
                pw.println(">");
                do {
                    Assertion child = (Assertion) iterator.next();
                    printAssertion(tab + 4, child, pw);
                    
                } while (iterator.hasNext());
                
                pw.println(StringUtils.getChars(tab, ' ') + "</wsp:Policy>");
                
            } else {
                pw.println("/>");
            }
            
        } else if (composite instanceof AndCompositeAssertion) {
            pw.print(StringUtils.getChars(tab, ' ') + "<wsp:All");
            
            Iterator iterator = composite.getTerms().iterator();
            if (iterator.hasNext()) {
                pw.println(">");
                do {
                    Assertion child = (Assertion) iterator.next();
                    printAssertion(tab + 4, child, pw);
                    
                } while (iterator.hasNext());
                
                pw.println(StringUtils.getChars(tab, ' ') + "</wsp:All>");
                
            } else {
                pw.println("/>");
            }
            
        } else if (composite instanceof XorCompositeAssertion) {
            pw.print(StringUtils.getChars(tab, ' ') + "<wsp:ExactlyOne");
            
            Iterator iterator = composite.getTerms().iterator();
            if (iterator.hasNext()) {
                pw.println(">");
                do {
                    Assertion child = (Assertion) iterator.next();
                    printAssertion(tab + 4, child, pw);
                    
                } while (iterator.hasNext());
                
                pw.println(StringUtils.getChars(tab, ' ') + "</wsp:ExactlyOne>");
                
            } else {
                pw.println("/>");
            }
            
        }
    }
    
//  private void printOMElement(int tab, OMElement element, PrintWriter pw) {
//      OMNamespace ns  = element.getNamespace();
//      
//      pw.print(StringUtils.getChars(tab, ' '));
//      pw.print("<" + ns.getPrefix() + ":" + element.getLocalName() + " xmlns:"
//              + ns.getPrefix() + "=\""+ ns.getName() + "\"");
//      
//      Iterator attributes = element.getAllAttributes();
//      if (attributes != null) {
//          
//          while (attributes.hasNext()) {
//              OMAttribute attribute = (OMAttribute) attributes.next();
//              pw.print(" "+ attribute.getLocalName() + "=\"" 
//                        +  attribute.getAttributeValue() + "\"");
//          }
//      }
//      
//      Iterator children = element.getChildren();
//      
//      if (children.hasNext()) {
//          pw.println(">");
//          
//          do {
//              Object child = children.next();
//              
//              if (child instanceof OMElement) {
//                  printOMElement(tab + 4, (OMElement) child, pw);
//                  
//              } else if (child instanceof OMText) {
//                  String strValue = ((OMText) child).getText().trim();
//                  
//                  if (strValue.length() != 0) {
//                      pw.println(StringUtils.getChars(tab + 4, ' ') 
//                              + strValue);                    
//                  }
//                  
//              }
//              
//          } while (children.hasNext());
//          
//          pw.print(StringUtils.getChars(tab, ' '));
//          pw.println("<" + ns.getPrefix() + ":" + element.getLocalName() + "/>");
//          
//      } else {
//          pw.println("/>");
//      }       
//  }
    
    private void printText(int tab, String value, PrintWriter pw) {
        pw.print(StringUtils.getChars(tab, ' '));
        pw.println(value);
    }
    
    private OMAttribute getAttribute(OMElement target, QName qname) {
        Iterator iterator = target.getAllAttributes();
        while (iterator.hasNext()) {
            OMAttribute attr = (OMAttribute) iterator.next();
            if (qname.equals(attr.getQName())) {
                return attr;
            }
        }
        return null;
    }
    
    private Hashtable getAttributes(OMElement element) {
        Hashtable attributes = new Hashtable();
        Iterator iterator = element.getAllAttributes();
        
        while (iterator.hasNext()) {
            OMAttribute attribute = (OMAttribute) iterator.next();
            attributes.put(attribute.getQName(), attribute.getAttributeValue());            
        }
        
        return attributes;        
    }
}
