package org.apache.ws.policy.attachment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyRegistry;
import org.w3c.dom.Document;

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

public class WSDLPolicyProcessor {

	private static final QName POLICY = new QName(
			PolicyConstants.WS_POLICY_NAMESPACE_URI, PolicyConstants.WS_POLICY);

	private static final QName POLICY_REF = new QName(
			PolicyConstants.WS_POLICY_NAMESPACE_URI,
			PolicyConstants.WS_POLICY_REFERENCE);

	Definition wsdl4jDefinition = null;

	PolicyRegistry registry = null;

	DOMPolicyReader prdr = null;

	public WSDLPolicyProcessor(InputStream in) throws WSDLException {
		this(in, null);
	}

	public WSDLPolicyProcessor(InputStream in, PolicyRegistry registry)
			throws WSDLException {
		if (registry != null) {
			this.registry = registry;
		}

		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setNamespaceAware(true);
			Document doc = builderFactory.newDocumentBuilder().parse(in);

			WSDLFactory wsdlFactory = WSDLFactory.newInstance();
			WSDLReader reader = wsdlFactory.newWSDLReader();

			wsdl4jDefinition = reader.readWSDL(null, doc);

			registry = new PolicyRegistry();

			prdr = (DOMPolicyReader) PolicyFactory
					.getPolicyReader(PolicyFactory.DOM_POLICY_READER);

			processDefinition(wsdl4jDefinition);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Policy getEffectiveServicePolicy(QName service) {
		Service wsdl4jService = wsdl4jDefinition.getService(service);

		List policyList = getPoliciesAsExtElements(wsdl4jService
				.getExtensibilityElements());

		return getEffectivePolicy(policyList);
	}

	public Policy getEffectiveEndpointPolicy(QName service, String port) {
		Service wsdl4jService = wsdl4jDefinition.getService(service);
		if (wsdl4jService == null) {
			throw new IllegalArgumentException("invalid service name");
		}

		Port wsdl4jPort = wsdl4jService.getPort(port);
		if (wsdl4jPort == null) {
			throw new RuntimeException("invalid port name");
		}

		ArrayList policyList = new ArrayList();

		policyList.addAll(getPoliciesAsExtElements(wsdl4jPort
				.getExtensibilityElements()));

		Binding wsdl4jBinding = wsdl4jPort.getBinding();
		policyList.addAll(getPoliciesAsExtElements(wsdl4jBinding
				.getExtensibilityElements()));

		PortType wsdl4jPortType = wsdl4jBinding.getPortType();
		policyList.addAll(getPoliciesAsExtAttributes(wsdl4jPortType
				.getExtensionAttributes()));

		return getEffectivePolicy(policyList);
	}

	public Policy getEffectiveOperationPolicy(QName service, String portName,
			String opName) {
		Service wsdl4jService = wsdl4jDefinition.getService(service);
		if (wsdl4jService == null) {
			throw new IllegalArgumentException("invalid service name");
		}

		Port wsdl4jPort = wsdl4jService.getPort(portName);
		if (wsdl4jPort == null) {
			throw new IllegalArgumentException("invalid port name");
		}

		Binding wsdl4jBinding = wsdl4jPort.getBinding();
		BindingOperation wsdl4jBindingOperation = wsdl4jBinding
				.getBindingOperation(opName, null, null);
		if (wsdl4jBindingOperation == null) {
			throw new IllegalArgumentException("invalid binding name");
		}

		ArrayList policyList = new ArrayList();
		policyList.addAll(getPoliciesAsExtElements(wsdl4jBindingOperation
				.getExtensibilityElements()));

		Operation wsdl4jOperation = wsdl4jBindingOperation.getOperation();
		policyList.addAll(getPoliciesAsExtElements(wsdl4jOperation
				.getExtensibilityElements()));

		return getEffectivePolicy(policyList);
	}
    
    public Policy getEffectiveInputPolicy(QName service, String portName, String opName) {
        Service wsdl4jService = wsdl4jDefinition.getService(service);
        if (wsdl4jService == null) {
            throw new IllegalArgumentException("invalid service name");
        }

        Port wsdl4jPort = wsdl4jService.getPort(portName);
        if (wsdl4jPort == null) {
            throw new IllegalArgumentException("invalid port name");
        }

        Binding wsdl4jBinding = wsdl4jPort.getBinding();
        BindingOperation wsdl4jBindingOperation = wsdl4jBinding
                .getBindingOperation(opName, null, null);
        if (wsdl4jBindingOperation == null) {
            throw new IllegalArgumentException("invalid binding name");
        }
        
        ArrayList policyList = new ArrayList();        
        BindingInput wsdl4jBindingInput = wsdl4jBindingOperation.getBindingInput();
        policyList.addAll(getPoliciesAsExtElements(wsdl4jBindingInput.getExtensibilityElements()));
        
        Operation wsdl4jOperation = wsdl4jBindingOperation.getOperation();
        Input wsdl4jInput = wsdl4jOperation.getInput();
        policyList.addAll(getPoliciesAsExtAttributes(wsdl4jInput.getExtensionAttributes()));
        
        Message wsdl4jMessage = wsdl4jInput.getMessage();
        policyList.addAll(getPoliciesAsExtElements(wsdl4jMessage.getExtensibilityElements()));
        
        
        return getEffectivePolicy(policyList);
    }
    
    public Policy getEffectiveOutputPolicy(QName service, String portName, String opName) {
        
        Service wsdl4jService = wsdl4jDefinition.getService(service);
        if (wsdl4jService == null) {
            throw new IllegalArgumentException("invalid service name");
        }

        Port wsdl4jPort = wsdl4jService.getPort(portName);
        if (wsdl4jPort == null) {
            throw new IllegalArgumentException("invalid port name");
        }

        Binding wsdl4jBinding = wsdl4jPort.getBinding();
        BindingOperation wsdl4jBindingOperation = wsdl4jBinding
                .getBindingOperation(opName, null, null);
        if (wsdl4jBindingOperation == null) {
            throw new IllegalArgumentException("invalid binding name");
        }
        
        ArrayList policyList = new ArrayList();
        BindingOutput wsdl4jBindingOutput = wsdl4jBindingOperation.getBindingOutput();
        policyList.addAll(getPoliciesAsExtElements(wsdl4jBindingOutput.getExtensibilityElements()));
        
        Operation wsdl4jOperation = wsdl4jBindingOperation.getOperation();
        Output wsdl4jOutput = wsdl4jOperation.getOutput();
        policyList.addAll(getPoliciesAsExtAttributes(wsdl4jOutput.getExtensionAttributes()));
        
        Message wsdl4jMessage = wsdl4jOutput.getMessage();
        policyList.addAll(getPoliciesAsExtElements(wsdl4jMessage.getExtensibilityElements()));
        
        return getEffectivePolicy(policyList);
    }
    
    public Policy getEffectiveFaultPolicy(QName service, String portName, String opName) {
        
         Service wsdl4jService = wsdl4jDefinition.getService(service);
         if (wsdl4jService == null) {
             throw new IllegalArgumentException("invalid service name");
         }

         Port wsdl4jPort = wsdl4jService.getPort(portName);
         if (wsdl4jPort == null) {
             throw new IllegalArgumentException("invalid port name");
         }

         Binding wsdl4jBinding = wsdl4jPort.getBinding();
         BindingOperation wsdl4jBindingOperation = wsdl4jBinding
                 .getBindingOperation(opName, null, null);
         if (wsdl4jBindingOperation == null) {
             throw new IllegalArgumentException("invalid binding name");
         }
         
         ArrayList policyList = new ArrayList();
         Map wsdl4jBindingFaults = wsdl4jBindingOperation.getBindingFaults();
         Iterator iterator = wsdl4jBindingFaults.values().iterator();
         
         if (! iterator.hasNext()) {
         	throw new RuntimeException("can't find any faults");
         }
         
         BindingFault wsdl4jBindingFault = (BindingFault) iterator.next();
         policyList.addAll(getPoliciesAsExtElements(wsdl4jBindingFault.getExtensibilityElements()));
         
         Operation wsdl4jOperation = wsdl4jBindingOperation.getOperation();
         Fault wsdl4jFault = wsdl4jOperation.getFault(wsdl4jBindingFault.getName());
         policyList.addAll(getPoliciesAsExtAttributes(wsdl4jFault.getExtensionAttributes()));
         
         Message wsdl4jMessage = wsdl4jFault.getMessage();
         policyList.addAll(getPoliciesAsExtElements(wsdl4jMessage.getExtensibilityElements()));
         
         return getEffectivePolicy(policyList);
    }

	private Policy getEffectivePolicy(List policyList) {
		Policy policy = null;
		Object policyElement;
		;

		for (Iterator iterator = policyList.iterator(); iterator.hasNext();) {
			policyElement = iterator.next();
			if (policyElement instanceof Policy) {
				policy = (policy == null) ? (Policy) policyElement
						: (Policy) policy.merge((Policy) policyElement,
								registry);

			} else if (policyElement instanceof PolicyReference) {
				policy = (policy == null) ? (Policy) ((PolicyReference) policyElement)
						.normalize(registry)
						: (Policy) policy.merge(
								(PolicyReference) policyElement, registry);

			}
		}

		if (!policy.isNormalized()) {
			policy = (Policy) policy.normalize(registry);
		}

		return policy;

	}

	private void processDefinition(Definition wsdl4jDefinition) {
		registerPoliciesAsExtElements(wsdl4jDefinition
				.getExtensibilityElements());
	}

	private void registerPoliciesAsExtElements(List extElements) {
		Object extElement;
		UnknownExtensibilityElement unknown;

		for (Iterator iterator = extElements.iterator(); iterator.hasNext();) {
			extElement = iterator.next();

			if (extElement instanceof UnknownExtensibilityElement) {
				unknown = (UnknownExtensibilityElement) extElement;

				if (POLICY.equals(unknown.getElementType())) {
					Policy p = prdr.readPolicy(unknown.getElement());

					if (p.getPolicyURI() != null) {
						registry.register(p.getPolicyURI(), p);
					}
				}
			}
		}
	}

	private List getPoliciesAsExtElements(List extElements) {
		ArrayList policyList = new ArrayList();

		Object extElement;
		UnknownExtensibilityElement unknown;

		for (Iterator iterator = extElements.iterator(); iterator.hasNext();) {
			extElement = iterator.next();

			if (extElement instanceof UnknownExtensibilityElement) {
				unknown = (UnknownExtensibilityElement) extElement;

				if (POLICY.equals(unknown.getElementType())) {

					Policy p = prdr.readPolicy(unknown.getElement());
					policyList.add(p);

				} else if (POLICY_REF.equals(unknown.getElementType())) {
					PolicyReference ref = prdr.readPolicyReference(unknown
							.getElement());
					policyList.add(ref);
				}
			}
		}

		return policyList;
	}

	private List getPoliciesAsExtAttributes(Map extAttributes) {
		ArrayList policyList = new ArrayList();
		QName PolicyURIs = (QName) extAttributes.get(new QName(
				PolicyConstants.WS_POLICY_NAMESPACE_URI, "PolicyURIs"));

		if (PolicyURIs != null) {
			String[] URIs = PolicyURIs.getLocalPart().split(" ");
			Policy policy;

			for (int i = 0; i < URIs.length; i++) {
				policy = registry.lookup(URIs[i]);

				if (policy == null) {
					throw new RuntimeException("cannot resolve : " + URIs[i]);
				}

				policyList.add(policy);
			}
		}

		return policyList;
	}
}