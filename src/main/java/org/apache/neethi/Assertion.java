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
 * This is an interface that any Assertion must implement. Hence any domain 
 * specific type can be used with this framework if it implements this 
 * interface.
 */
public interface Assertion extends PolicyComponent {
    
    /**
     * Returns the QName of the Root Element of this Assertion.
     *  
     * @return QName the QName of the Root Element of this Assertion.
     */
    QName getName();
    
    /**
     * Returns true if this Assertion is optional. Returns false otherwise. 
     * 
     * @return true if the assertion is optional.
     */
    boolean isOptional();
    
    
    /**
     * Returns true if this Assertion is ignorable. Returns false otherwise. 
     * 
     * @return true if the assertion is ignorable.
     */
    boolean isIgnorable();
    
    /**
     * Serialize this Assertion into its XML infoset using XMLStreamWriter.
     */
    void serialize(XMLStreamWriter writer) throws XMLStreamException;
    
    /**
     * Returns a new PolicyComponent that is the normalized version of this. 
     */
    PolicyComponent normalize();
}
