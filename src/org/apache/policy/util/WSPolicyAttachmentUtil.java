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

package org.apache.policy.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.policy.model.Assertion;
import org.apache.policy.model.Policy;
import org.apache.policy.parser.WSPConstants;
import org.apache.policy.parser.WSPolicyParser;
import org.apache.wsdl.Component;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.DefaultExtensibilityElement;
import org.w3c.dom.Element;

import com.ibm.wsdl.util.xml.DOM2Writer;

/**
 * This util class which implements WSPolicyAttachment sepcification (September
 * 2004).
 * 
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class WSPolicyAttachmentUtil {
	
	private WSDLDescription wsdlDescription = null;
	//private HashMap loadedPolicies = new HashMap();
	private PolicyRegistry reg = new PolicyRegistry();
	private WSPolicyParser parser = WSPolicyParser.getInstance();

	
	public WSPolicyAttachmentUtil() {
	}

	public WSPolicyAttachmentUtil(WSDLDescription wsdlDescription) {
		this.wsdlDescription = wsdlDescription;
		populatePolicyRegistry();
	}
	
	public WSPolicyAttachmentUtil(InputStream wsdlInputStream) {
		try {
			WSDLVersionWrapper build = WOMBuilderFactory.
                getBuilder(WSDLConstants.WSDL_1_1).build(wsdlInputStream);
			wsdlDescription = build.getDescription();
			populatePolicyRegistry();
			
		} catch (WSDLException e) {
			throw new IllegalArgumentException("error : "+ e.getMessage());
		}
	}
	
	public void setWSDLDescription(WSDLDescription wsdlDescription) {
		this.wsdlDescription = wsdlDescription;
		reg = new PolicyRegistry();
		populatePolicyRegistry();
	}
	
	public WSDLDescription getWSDLDescription() {
		if (wsdlDescription != null) {
			return wsdlDescription;
		}
		throw new IllegalStateException("ERROR: A WSDLDescription is not set");
		
	}
	
	public Policy getPolicyForService(QName serviceName) {
		return getServicePolicy(serviceName);
	}

	public Policy getPolicyForEndPoint(QName epName) {
		Policy servicePolicy = null;
		Policy endPointPolicy = null;
		
		Iterator iterator = wsdlDescription.getServices().values().iterator();
		
		while (iterator.hasNext()) {
			WSDLService service = (WSDLService) iterator.next();
			if (service.getEndpoints().containsKey(epName)) {
				servicePolicy = getPolicyForService(service.getName());
				break;
			}
		}
		
		endPointPolicy = getEndPointPolicy(epName);
		
		return (servicePolicy != null) 
				? (Policy) servicePolicy.merge(endPointPolicy)
				: endPointPolicy; 		
	}
	
	public Policy getPolicyForOperation(QName endPoint, QName operation) {
		Policy endPointPolicy = getPolicyForEndPoint(endPoint),
			   operationPolicy = getOperationPolicy(endPoint, operation);
		return (endPointPolicy != null)
				? (Policy) endPointPolicy.merge(operationPolicy)
				: operationPolicy;
	}
	
	public Policy getPolicyForInputMessage(QName endPoint, QName operation) {
		Policy operationPolicy = getPolicyForOperation(endPoint, operation),
			   inputMsgPolicy  = getInputMeassagePolicy(endPoint, operation);
		return (operationPolicy != null) 
				? (Policy) operationPolicy.merge(inputMsgPolicy)
				: inputMsgPolicy;
	}

	public Policy getPolicyForOutputMessage(QName endPoint, QName operation) {
		Policy operationPolicy = getPolicyForOperation(endPoint, operation),
		   outputMsgPolicy  = getOutputMeassagePolicy(endPoint, operation);
		return (operationPolicy != null) 
				? (Policy) operationPolicy.merge(outputMsgPolicy)
				: outputMsgPolicy;
		
	}

	public Policy getServicePolicy(QName serName) {
		WSDLService service = getWSDLDescription().getService(serName);
		return (service == null) ? null :(Policy) getComponentPolicy(service).normalize(reg);
	}
	
	public Policy getEndPointPolicy(QName epName) {
		WSDLEndpoint endpoint = getEndpoint(epName);
		if (endpoint == null) {
			return null;
		}
		
		ArrayList policies = new ArrayList();
		
		// wsdl:port
		Assertion epPolicy = getComponentPolicy(endpoint);
		if (epPolicy != null) {
			policies.add(getComponentPolicy(endpoint));			
		}		 

		//wsdl:binding
		WSDLBinding wsdlBinding = endpoint.getBinding();
		Assertion wsdlBindingPolicy = getComponentPolicy(wsdlBinding);
		if (wsdlBindingPolicy != null) {
			policies.add(getComponentPolicy(wsdlBinding));			
		}
		
		//wsdl:portType
		WSDLInterface wsdlInterface = wsdlBinding.getBoundInterface();
		Assertion portTypePolicy = getComponentPolicy(wsdlInterface);
		if (portTypePolicy != null) {
			policies.add(getComponentPolicy(wsdlInterface));
		}
		
		return getEffectivePolicy(policies);		
	}

	public Policy getOperationPolicy(QName endPointName, QName opName) {
		WSDLEndpoint endPoint = getEndpoint(endPointName);
						
		ArrayList list = new ArrayList();
		
		//wsdl:binding/wsdl:operation
		WSDLBinding binding = endPoint.getBinding();
		WSDLBindingOperation bindingOperation = (WSDLBindingOperation) binding.getBindingOperations().get(opName);
		
		Assertion bindingPolicy = getComponentPolicy(bindingOperation);
		if (bindingPolicy != null) {
			list.add(bindingPolicy);
		}
		
		// wsdl:portType/wsdl:operation
		WSDLOperation wsdlOperation = bindingOperation.getOperation();
		Assertion interfacePolicy = getComponentPolicy(wsdlOperation);
		
		if (interfacePolicy != null) {
			list.add(interfacePolicy);
			
		}
		
		return getEffectivePolicy(list);
	}
	
	public Policy getInputMeassagePolicy(QName endPointName, QName opName) {
		List policies = new ArrayList();
		WSDLEndpoint endPoint = getEndpoint(endPointName);
		
		// wsdl:binding/wsdl:operation/wsdl:input		
		WSDLBindingOperation wsdlBindingOperation = endPoint.getBinding().getBindingOperation(opName);
		WSDLBindingMessageReference bindingInput = wsdlBindingOperation.getInput();
		
		//List extensibilityAttributes = bindingInput.getExtensibilityAttributes();
		Policy bindingInputPolicy = getEffectivePolicy(getPoliciesAsExtensibleElements(bindingInput));
		if (bindingInputPolicy != null) {
			policies.add(bindingInputPolicy);		
		}
		
		// wsdl:portType/wsdl:operation/wsdl:input				
		WSDLOperation wsdlOperation = wsdlBindingOperation.getOperation();
		MessageReference operationInput = wsdlOperation.getInputMessage();
		Policy operationInputPolicy = getEffectivePolicy(getPoliciesAsExtensibilityAttribute(operationInput));
		if (operationInputPolicy != null) {
			policies.add(operationInputPolicy);
		}
		
		// wsdl:Message
		// TODO
		Policy messageInputPolicy = getEffectivePolicy(getPoliciesAsExtensibleElements(operationInput));
		if (messageInputPolicy != null) {
			policies.add(messageInputPolicy);
		}
		
		return getEffectivePolicy(policies);
	}

	public Policy getOutputMeassagePolicy(QName endPointName, QName opName) {
		List policies = new ArrayList();
		WSDLEndpoint endPoint = getEndpoint(endPointName);
		
		
		// wsdl:binding/wsdl:operation/wsdl:output
		WSDLBindingOperation wsdlBindingOperation = endPoint.getBinding().getBindingOperation(opName);
		WSDLBindingMessageReference bindingOutput = wsdlBindingOperation.getOutput();
		
		Policy bindingOutputPolicy = getEffectivePolicy(getPoliciesAsExtensibleElements(bindingOutput));
		if (bindingOutputPolicy != null) {
			policies.add(getComponentPolicy(bindingOutput));		
		}
		
		// wsdl:portType/wsdl:operation/wsdl:output
		WSDLOperation wsdlOperation = wsdlBindingOperation.getOperation();
		MessageReference operationOutput = wsdlOperation.getOutputMessage();
		Policy operationOutputPolicy = getEffectivePolicy(getPoliciesAsExtensibilityAttribute(operationOutput));
		if (operationOutputPolicy != null) {
			policies.add(operationOutputPolicy);
		}
		
		// wsdl:Message
		// TODO
		Policy messageOutputPolicy = getEffectivePolicy(getPoliciesAsExtensibleElements(operationOutput));
		if (messageOutputPolicy != null) {
			policies.add(messageOutputPolicy);
		}
		
		return getEffectivePolicy(policies);
	}

	public Policy getFaultMeassagePolicy(QName endPointName, QName opName, QName fault) {
		throw new UnsupportedOperationException();		
	}

	public List getServiceElementPolicy(WSDLService wsdlService) {
		return getPoliciesAsExtensibleElements(wsdlService);					
	}

	private Policy getEffectivePolicy(List policies) {
		Policy result = null;
		
		if (!policies.isEmpty()) {
			Iterator iter = policies.iterator();
			result = (Policy) iter.next();
			while (iter.hasNext()) {
				Policy next = (Policy) iter.next();
				result = (Policy) result.merge(next, reg);
			}
		}
		return result;
	}
	
	private WSDLEndpoint getEndpoint(QName epName) {
		Iterator iterator = wsdlDescription.getServices().values().iterator();
		while (iterator.hasNext()) {
			WSDLService service = (WSDLService) iterator.next();
			if (service.getEndpoints().containsKey(epName)) {
				return service.getEndpoint(epName);
			}
		}
		return null;
	}

	private Policy getComponentPolicy(Component component) {
		List myPolicyList   = new ArrayList();
		List attrPolicyList = getPoliciesAsExtensibilityAttribute(component),
			 elePolicyList  = getPoliciesAsExtensibleElements(component);
		
		myPolicyList.addAll(attrPolicyList);
		myPolicyList.addAll(elePolicyList);
		
		return getEffectivePolicy(myPolicyList);	
	}

	private List getPoliciesAsExtensibilityAttribute(Component component) {
		Iterator iterator;
		List policyURIStrings = new ArrayList();
		List policies   = new ArrayList();
		iterator = component.getExtensibilityAttributes().iterator();
	
		while (iterator.hasNext()) {
			WSDLExtensibilityAttribute exAttribute = (WSDLExtensibilityAttribute) iterator.next();
			QName qname = exAttribute.getKey();
			
			if (qname.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) &&
					qname.getLocalPart().equals("PolicyURIs")) {
				String value = exAttribute.getValue().toString();
				String[] uriStrings = value.split(" ");
			
				for (int i = 0; i < uriStrings.length; i++) {
					policyURIStrings.add(uriStrings[i].trim());
				}				
			}
		}
		if (!policyURIStrings.isEmpty()) {
			iterator = policyURIStrings.iterator();
			
			do {
				String policyURIString = (String) iterator.next();
				Policy policy = getPolicyFromURI(policyURIString);
				policies.add(policy);				
			} while (iterator.hasNext());
		}		
		return policies;
	}
	
	private List getPoliciesAsExtensibleElements(Component component) {
		
		ArrayList policies = new ArrayList();
		Iterator iterator = component.getExtensibilityElements().iterator();
		
		while (iterator.hasNext()) {
			Object extensibilityElement = iterator.next();
			if (extensibilityElement instanceof DefaultExtensibilityElement) {
				DefaultExtensibilityElement defaultExtensibilityElement = (DefaultExtensibilityElement) extensibilityElement;
				Element element = defaultExtensibilityElement.getElement();
				
				if (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) && element.getLocalName().equals("PolicyReference")) {
					policies.add(getPolicyAsPolicyRef(element));
					
				} else if (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) && element.getLocalName().equals("Policy")) {
					policies.add(getPolicyAsElement(element));
				}
			}
//			WSDLExtensibilityElement exElement = (WSDLExtensibilityElement) iterator.next();
//			Element element = (Element) exElement.getElement();
//			if (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) && element.getLocalName().equals("PolicyReference")) {
//				policyList.add(getPolicyAsPolicyRef(element));
//				
//			} else if (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) && element.getLocalName().equals("Policy")) {
//				policyList.add(getPolicyAsElement(element));
//			}
			
		}
		return policies;
	}
	
	private Policy getPolicyAsPolicyRef(Element element) {
		String policyURIString = element.getAttribute("URI");
		if (policyURIString != null && policyURIString.length() != 0) {
			return getPolicyFromURI(policyURIString);
		}
		return null;
	}
	
	private Policy getPolicyAsElement(Element element) {
		InputStream policyInputStream = getInputStream(element);
		return parser.buildPolicyModel(policyInputStream);
	}
	
//	private OMElement getElementAsOM(Element element, OMElement parent) {
//		OMFactory factory = OMFactory.newInstance();
//		
//		String namespaceURI = element.getNamespaceURI();
//		String localName = element.getLocalName();
//		QName qname = new QName(namespaceURI, localName);
//					
//		OMElement omElement = factory.createOMElement(qname, parent);
//		element.getChildNodes();
//		return null;
//	}
	
	private InputStream getInputStream(Element element) {
         
		StringWriter sw = new StringWriter();
        DOM2Writer.serializeAsXML(element, sw);
        
		return new ByteArrayInputStream(sw.toString().getBytes());
	}
	
	private Policy getPolicyFromURI(String policyURIString) {
		return reg.lookup(policyURIString);
	}
	
	public String getTargetURI() {
		return getWSDLDescription().getTargetNameSpace();
	}

	private void populatePolicyRegistry() {
		Iterator iterator;
		WSDLDescription des = getWSDLDescription();
		List extElements = des.getExtensibilityElements();
		registerPoliciesAsElements(extElements);
		
		iterator = des.getWsdlInterfaces().values().iterator();
		while (iterator.hasNext()) {
			WSDLInterface interfaze = (WSDLInterface) iterator.next();
			registerPoliciesInWSDLInterface(interfaze);
		}
		
		iterator = des.getBindings().values().iterator();
		while (iterator.hasNext()) {
			WSDLBinding wsdlBinding = (WSDLBinding) iterator.next();
			registerPoliciesInWSDLBinding(wsdlBinding);
		}
		
		iterator = des.getServices().values().iterator();
		while (iterator.hasNext()) {
			WSDLService service = (WSDLService) iterator.next();	
            registerPoliciesInService(service);
		}
		
		iterator = reg.keys();
		while (iterator.hasNext()) {
			String uriString = (String) iterator.next();
			Policy policy = reg.lookup(uriString);
			if (policy == null) {
				try {
					URI policyURI = new URI(uriString);
					URL policyURL = policyURI.toURL();
					Policy newPolicy = parser.buildPolicyModel(policyURL.openStream());
					reg.register(uriString, newPolicy);
					
				} catch (Exception e) {
					e.printStackTrace();
					reg.unregister(uriString);
                    iterator = reg.keys();
				}
			}
		}
	}

	private void registerPoliciesInService(WSDLService service) {
		List extensibilityElements = service.getExtensibilityElements();
		registerPoliciesAsElements(extensibilityElements);
		
		Iterator iterator = service.getEndpoints().values().iterator();
		while (iterator.hasNext()) {
			WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) iterator.next();
			extensibilityElements = wsdlEndpoint.getExtensibilityElements();
			registerPoliciesAsElements(extensibilityElements);			
		}		
	}
	
	private void registerPoliciesInWSDLBinding(WSDLBinding wsdlBinding) {
		List extensibilityElements = wsdlBinding.getExtensibilityElements();
		registerPoliciesAsElements(extensibilityElements);
		
		Iterator iterator = wsdlBinding.getBindingOperations().values().iterator();
		while (iterator.hasNext()) {
			WSDLBindingOperation wsdlBindingOperation = (WSDLBindingOperation) iterator.next();
			registerPoliciesInBindOperation(wsdlBindingOperation);
		}		
	}
	
	private void registerPoliciesInBindOperation(WSDLBindingOperation wsdlBindingOperation) {
		List extensibilityElements = wsdlBindingOperation.getExtensibilityElements();
		registerPoliciesAsElements(extensibilityElements);
		
        if (wsdlBindingOperation.getInput() != null) {
            extensibilityElements = wsdlBindingOperation.getInput().getExtensibilityElements();
            registerPoliciesAsElements(extensibilityElements);
        }
        if (wsdlBindingOperation.getOutput() != null) {
            extensibilityElements = wsdlBindingOperation.getOutput().getExtensibilityElements();
            registerPoliciesAsElements(extensibilityElements);
        }
	}
	
	private void registerPoliciesInWSDLInterface(WSDLInterface wsdlInterface) {
		registerPoliciesInElement(wsdlInterface);
		Iterator iterator = wsdlInterface.getOperations().values().iterator();
		while (iterator.hasNext()) {
			WSDLOperation wsdlOperation = (WSDLOperation) iterator.next();
			registerPoliciesInWSDLOperation(wsdlOperation);
		}
	}
	
	private void registerPoliciesInWSDLOperation(WSDLOperation wsdlOperation){
		List extensibilityElements = wsdlOperation.getExtensibilityElements();
		registerPoliciesAsElements(extensibilityElements);
        
        if (wsdlOperation.getInputMessage() != null) {
            registerPoliciesInElement(wsdlOperation.getInputMessage());
        }
        if (wsdlOperation.getOutputMessage() != null) {
            registerPoliciesInElement(wsdlOperation.getOutputMessage());        
        }
	}
	
	private void registerPoliciesInElement(Component component) {
		registerPoliciesAsAttribute(component.getExtensibilityAttributes());
		registerPoliciesAsElements(component.getExtensibilityElements());
	}
	
	private void registerPoliciesAsElements(List elements) {
		Iterator iterator = elements.iterator();
		while (iterator.hasNext()) {
			Object extensibilityElement = iterator.next();
			
			if (extensibilityElement instanceof DefaultExtensibilityElement) {
				DefaultExtensibilityElement defaultExtensibilityElement = (DefaultExtensibilityElement) extensibilityElement;
				Element element = defaultExtensibilityElement.getElement();
				
				if (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) && element.getLocalName().equals("PolicyReference")) {
					String uriString = element.getAttribute("URI");
					
					if (reg.lookup(uriString) == null) {
						reg.register(uriString, null);						
					}
				} 
				
				String policyID = element.getAttributeNS(WSPConstants.WSU_NAMESPACE_URI, "Id");
	
				if (policyID.length() != 0) {
					registerPolicyElement(element);
				}
			}
		}		
	}
	
	private void registerPoliciesAsAttribute(List elements) {
		Iterator iterator = elements.iterator();
		
		while (iterator.hasNext()) {
			WSDLExtensibilityAttribute wsdlExtensibilityAttribute = (WSDLExtensibilityAttribute) iterator.next();
			QName qname = wsdlExtensibilityAttribute.getKey();
			
			if (qname.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI) &&
					qname.getLocalPart().equals("PolicyURIs")) {
				String value = wsdlExtensibilityAttribute.getValue().toString();
				String[] policyURIs = value.split(" ");
				for (int i = 0; i < policyURIs.length; i++) {
					String policyURI = policyURIs[i].trim();

					if (reg.lookup(policyURI) == null) {
						reg.register(policyURI, null);
					}
				}				
			}
		}
	}
	
	private void registerPolicyElement(Element element) {
		InputStream elementInputStream = getInputStream(element);
		Policy policy = parser.buildPolicyModel(elementInputStream);
        
		reg.register(policy.getPolicyURI(), policy);
	}
}
