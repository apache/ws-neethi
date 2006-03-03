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

import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * SchemaRegistry holds schemas associated with URIs.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class SchemaRegistry {

	/** Stores {URI, schema element} pairs */
	private HashMap reg = new HashMap();

	/**
	 * Returns the schema element associated with uri String
	 * 
	 * @param uri
	 *            the URI which uniquely identify the schema element
	 * @return the schema element which is associated with the uri.
	 */
	public Element lookup(String uri) {
		return (Element) reg.get(uri);
	}

	/**
	 * Registers a schema element with a uri.
	 * 
	 * @param uri
	 *            the key for the element
	 * @param schemaElement
	 *            the schema element to associate with the uri
	 */
	public void register(String uri, Element schemaElement) {
		reg.put(uri, schemaElement);
	}

	/**
	 * Unregisters the schema element associated with the specified uri.
	 * 
	 * @param uri
	 *            the uri of the schema element
	 */
	public void unregister(String uri) {
		reg.remove(uri);
	}
}