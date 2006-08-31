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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This is an interface which any component of the frame must implement.
 */
public interface PolicyComponent {

    public static final short POLICY = 0x1;

    public static final short EXACTLYONE = 0x2;

    public static final short ALL = 0x3;

    public static final short ASSERTION = 0x4;

    /**
     * Serializes the PolicyComponent using an XMLStreamWriter.
     *
     * @param writer the writer that the component should write itself
     * @throws XMLStreamException if an errors in the process of serialization of the
     *                            PolicyComponent.
     */
    public void serialize(XMLStreamWriter writer) throws XMLStreamException;

    /**
     * Returns a short value which uniquely identify the type of the
     * PolicyComponent.
     *
     * @return PolicyComponent.POLICY for Policy type PolicyComponent
     *         PolicyComponent.EXACTLYONE for ExactlyOne type PolicyComponent
     *         PolicyComponent.All for All type PolicyComponent
     *         PolicyComponent.ASSERTION for Assertion type PolicyComponent
     */
    public short getType();

    public PolicyComponent normalize();
    
    public boolean equal(PolicyComponent policyComponent);
}
