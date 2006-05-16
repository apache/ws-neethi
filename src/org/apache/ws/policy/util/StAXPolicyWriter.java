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

package org.apache.ws.policy.util;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.ws.policy.All;
import org.apache.ws.policy.Assertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.ExactlyOne;

/**
 * StAXPolicyWriter implements PolicyWriter and provides different methods to
 * create a Policy object. It uses StAX as its underlying mechanism to create
 * XML elements.
 */
public class StAXPolicyWriter implements PolicyWriter {

	private int num = 1;

	public StAXPolicyWriter() {
	}

	public void writePolicy(Policy policy, OutputStream output) {
		XMLStreamWriter writer = null;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
					output);
			writePolicy(policy, writer);

			writer.flush();

		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void writePolicy(Policy policy, XMLStreamWriter writer)
			throws XMLStreamException {
		String writerPrefix = writer
				.getPrefix(PolicyConstants.POLICY_NAMESPACE_URI);

		if (writerPrefix != null) {
			writer.writeStartElement(PolicyConstants.POLICY_NAMESPACE_URI,
					PolicyConstants.POLICY);

		} else {
			writer.writeStartElement(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.writeNamespace(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.setPrefix(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);

		}

    Hashtable attributes = policy.getAttributes();
    Enumeration attrNames = attributes.keys();
    while (attrNames.hasMoreElements()) {
      QName tAttrName = (QName) attrNames.nextElement();
      String tAttrNamespaceURI = tAttrName.getNamespaceURI();
      // If it's a namespace, make sure we note that it's been written ...

      if (tAttrNamespaceURI != null) {
        if (tAttrNamespaceURI.equals(PolicyConstants.NAMESPACE_XMLNS)) {
          String tPrefix = tAttrName.getLocalPart();
          // For XMLSNS attributes, the attribute value is the actual namespace.
          writerPrefix = writer.getPrefix((String) attributes.get(tAttrName));
          // We only need to act upon it if we haven't seen it before ...
          if (writerPrefix == null) {
            writer.setPrefix(tPrefix, (String) attributes.get(tAttrName));
            writer.writeNamespace(tPrefix, (String) attributes.get(tAttrName));
          }
        } else {
          // It must be a standard attribute ...
          String tPrefix = writer.getPrefix(tAttrNamespaceURI);

          // Firstly, deal with the ones that have prefixes in common usage ...
          if (tAttrNamespaceURI.equals(PolicyConstants.WSU_NAMESPACE_URI)) {
            if (tPrefix == null) {
              tPrefix = PolicyConstants.WSU_NAMESPACE_PREFIX;
              writer.setPrefix(tPrefix, tAttrNamespaceURI);
              writer.writeNamespace(tPrefix, tAttrNamespaceURI);
            }
          } else {
            if (tPrefix == null) {
              tPrefix = generateNamespace();
              writer.setPrefix(tPrefix, tAttrNamespaceURI);
              writer.writeNamespace(tPrefix, tAttrNamespaceURI);
            }
          }
          writer.writeAttribute(tPrefix, tAttrNamespaceURI, tAttrName
              .getLocalPart(), (String) attributes.get(tAttrName));
        }
      }
    }
    Iterator iterator = policy.getTerms().iterator();
    while (iterator.hasNext()) {
      Assertion term = (Assertion) iterator.next();
      writeAssertion(term, writer);
    }

    writer.writeEndElement();

	}

	private void writeAssertion(Assertion assertion, XMLStreamWriter writer)
			throws XMLStreamException {
		if (assertion instanceof PrimitiveAssertion) {
			writePrimitiveAssertion((PrimitiveAssertion) assertion, writer);

		} else if (assertion instanceof ExactlyOne) {
			writeXorCompositeAssertion((ExactlyOne) assertion,
					writer);

		} else if (assertion instanceof PolicyReference) {
			writePolicyReference((PolicyReference) assertion, writer);

		} else if (assertion instanceof Policy) {
			writePolicy((Policy) assertion, writer);
		} else if (assertion instanceof All) {
			writeAndCompositeAssertion((All) assertion,
					writer);

		} else {
			throw new RuntimeException("unknown element type");
		}
	}

	private void writeAndCompositeAssertion(All assertion,
			XMLStreamWriter writer) throws XMLStreamException {

		String writerPrefix = writer
				.getPrefix(PolicyConstants.POLICY_NAMESPACE_URI);

		if (writerPrefix == null) {
			writer.writeStartElement(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.ALL,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.writeNamespace(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.setPrefix(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);

		} else {
			writer.writeStartElement(PolicyConstants.POLICY_NAMESPACE_URI,
					PolicyConstants.ALL);
		}

		List terms = assertion.getTerms();
		writeTerms(terms, writer);

		writer.writeEndElement();
	}

	private void writeXorCompositeAssertion(ExactlyOne assertion,
			XMLStreamWriter writer) throws XMLStreamException {
		String writerPrefix = writer
				.getPrefix(PolicyConstants.POLICY_NAMESPACE_URI);

		if (writerPrefix == null) {
			writer.writeStartElement(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.EXACTLY_ONE,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.writeNamespace(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.setPrefix(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);

		} else {
			writer.writeStartElement(PolicyConstants.POLICY_NAMESPACE_URI,
					PolicyConstants.EXACTLY_ONE);
		}

		List terms = assertion.getTerms();
		writeTerms(terms, writer);

		writer.writeEndElement();
	}

	private void writePrimitiveAssertion(PrimitiveAssertion assertion,
			XMLStreamWriter writer) throws XMLStreamException {
		QName qname = assertion.getName();

		String writerPrefix = writer.getPrefix(qname.getNamespaceURI());
		if (writerPrefix != null) {
			writer.writeStartElement(qname.getNamespaceURI(), qname
					.getLocalPart());
		} else {
			String prefix = (qname.getPrefix() != null) ? qname.getPrefix()
					: generateNamespace();
			writer.writeStartElement(prefix, qname.getLocalPart(), qname
					.getNamespaceURI());
			writer.writeNamespace(prefix, qname.getNamespaceURI());
			writer.setPrefix(prefix, qname.getNamespaceURI());

		}

		Hashtable attributes = assertion.getAttributes();
		writeAttributes(attributes, writer);

		String text = assertion.getStrValue();
		if (text != null) {
			writer.writeCharacters(text);
		}

		List terms = assertion.getTerms();
		writeTerms(terms, writer);

		writer.writeEndElement();
	}

	public void writePolicyReference(PolicyReference assertion,
			XMLStreamWriter writer) throws XMLStreamException {

		String writerPrefix = writer
				.getPrefix(PolicyConstants.POLICY_NAMESPACE_URI);
		if (writerPrefix != null) {
			writer.writeStartElement(PolicyConstants.POLICY_NAMESPACE_URI,
					PolicyConstants.POLICY_REFERENCE);
		} else {

			writer.writeStartElement(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_REFERENCE,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.writeNamespace(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);
			writer.setPrefix(PolicyConstants.POLICY_PREFIX,
					PolicyConstants.POLICY_NAMESPACE_URI);

		}
		writer.writeAttribute("URI", assertion.getPolicyURIString());

		writer.writeEndElement();
	}

	private void writeTerms(List terms, XMLStreamWriter writer)
			throws XMLStreamException {

		Iterator iterator = terms.iterator();
		while (iterator.hasNext()) {
			Assertion assertion = (Assertion) iterator.next();
			writeAssertion(assertion, writer);
		}
	}

	private void writeAttributes(Hashtable attributes, XMLStreamWriter writer)
			throws XMLStreamException {

		Iterator iterator = attributes.keySet().iterator();
		while (iterator.hasNext()) {
			QName qname = (QName) iterator.next();
			String value = (String) attributes.get(qname);

			String prefix = qname.getPrefix();
			if (prefix != null) {
				writer.writeAttribute(prefix, qname.getNamespaceURI(), qname
						.getLocalPart(), value);
			} else {
				writer.writeAttribute(qname.getNamespaceURI(), qname
						.getLocalPart(), value);
			}
		}
	}

	private String generateNamespace() {
		return "ns" + num++;
	}
}