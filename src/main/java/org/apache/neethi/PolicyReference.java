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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * PolicyReference is a wrapper that holds explicit PolicyReferences.
 */
public class PolicyReference implements PolicyComponent {

    private String uri;
    private PolicyEngine engine;

    public PolicyReference() {
    }
    
    public PolicyReference(PolicyEngine p) {
        engine = p;
    }
    
    /**
     * Sets the Policy URI
     * @param uri the Policy URI
     */
    public void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the Policy URI that is referred by self.
     * @return a String that is the Policy URI referred by self
     */
    public String getURI() {
        return uri;
    }

    public boolean equal(PolicyComponent policyComponent) {
        if (Constants.TYPE_POLICY_REF != policyComponent.getType()) {
            return false;
        }
        
        String u = ((PolicyReference)policyComponent).getURI();
        return u != null && u.length() != 0 && u.equals(this.uri);

    }


    /**
     * Returns short value of Constants.TYPE_POLICY_REF 
     */
    public short getType() {
        return Constants.TYPE_POLICY_REF;
    }

    /**
     * Throws an UnsupportedOperationException since PolicyReference.normalize()
     * can't resolve the Policy that it refers to unless a PolicyRegistry is
     * provided.
     */
    public PolicyComponent normalize() {
        throw new UnsupportedOperationException("PolicyReference.normalize() is meaningless");
    }
    
    /**
     * Returns normalized version of the Policy that is referred by self. The specified 
     * PolicyRegistry is used to lookup for the Policy that is referred and <tt>deep</tt> 
     * indicates the level of normalization for the returning Policy.
     * 
     * @param reg the PolicyRegistry that is used to resolved the Policy referred by self
     * @param deep the flag to indicate whether returning Policy should be fully normalized
     * @return the normalized version for the Policy refered by self
     */
    public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
        String key = getURI();
        int pos = key.indexOf("#");
        if (pos == 0) {
            key = key.substring(1);
        } else if (pos > 0) {
            key = key.substring(0, pos);
        }
        
        Policy policy = reg.lookup(key);        
        
        if (policy == null) {
            policy = getRemoteReferencedPolicy(key);

            if (policy == null) {
                throw new RuntimeException(key + " can't be resolved");
            }
            reg.register(key, policy);
        }
        
        return policy.normalize(reg, deep);
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        String namespace = Constants.findPolicyNamespace(writer);
        String wspPrefix = writer.getPrefix(namespace);
        
        if (wspPrefix == null) {
            wspPrefix = Constants.ATTR_WSP;
            writer.setPrefix(wspPrefix, namespace);
        }
        
        writer.writeStartElement(wspPrefix, Constants.ELEM_POLICY_REF, namespace);
        writer.writeNamespace(Constants.ATTR_WSP, namespace);
        writer.writeAttribute(Constants.ATTR_URI, getURI());
        
        writer.writeEndElement();
    }
    
    public Policy getRemoteReferencedPolicy(String u) {
        try {
            //create java.net URL pointing to remote resource
            URL url = new URL(u);
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);

            InputStream in = connection.getInputStream();
            try {
                PolicyEngine pe = engine;
                if (pe == null) {
                    pe = new PolicyEngine();
                }
                return pe.getPolicy(connection.getInputStream());
            } finally {
                in.close();
            }
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Malformed uri: " + u);
        } catch (IOException ioe) {        
            throw new RuntimeException("Cannot reach remote resource: " + u);
        }
    }
}
