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

package org.apache.neethi.builders.converters;

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * A converter is used to convert from a particular source of
 * Policy information into a form usable by a registered builder.
 * 
 * It also contains methods for obtaining information about the
 * current element while being processed.
 */
public interface Converter<S, T> {

    QName getQName(S s);
    Map<QName, String> getAttributes(S s);
    Iterator<S> getChildren(S s);

    /**
     * 
     * @param s The source object
     * @return the result of the conversion; may not be <code>null</code>
     * @throws ConverterException
     *             if the conversion fails; note that to indicate a failure, the method may throw
     *             other unchecked exceptions specific to the APIs involved in the conversion
     */
    T convert(S s);
}
