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

import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.namespace.QName;

/**
 * Contains a registry of Converters.
 * 
 * By default, there are converters to convert back and forth
 * between DOM Elements and XMLStreamReaders.  If Axiom
 * is available, converters are also registered to convert
 * from those format to/from OMElements.
 */
public class ConverterRegistry {
    private static class ConverterKey {
        Class<?> src;
        Class<?> target;
        Converter<?, ?> converter;
    }

    private List<ConverterKey> registeredConverters = new CopyOnWriteArrayList<ConverterKey>();
    
    public ConverterRegistry() {
        // Register the identity converters first to avoid unnecessary conversions
        // if the supplied element implements both the DOM and Axiom APIs.
        registerConverter(new DOMToDOMConverter());
        registerConverter(new StaxToStaxConverter());
        registerConverter("org.apache.neethi.builders.converters.OMToOMConverter");
        
        //built into JDK stuff, should have no problem
        registerConverter(new StaxToDOMConverter());
        registerConverter(new DOMToStaxConverter());

        //requires Axiom, may have an issue
        registerConverter("org.apache.neethi.builders.converters.DOMToOMConverter");
        registerConverter("org.apache.neethi.builders.converters.OMToDOMConverter");
        registerConverter("org.apache.neethi.builders.converters.StaxToOMConverter");
        registerConverter("org.apache.neethi.builders.converters.OMToStaxConverter");
    }

    private void registerConverter(String name) {
        try {
            Converter<?, ?> c = (Converter<?, ?>)Class.forName(name, true,
                                                   Converter.class.getClassLoader()).newInstance();
            registerConverter(c);
        } catch (Throwable e) {
            //likely due to Axiom not available, we're OK with that.  There
            //won't be any builders registered that require Axiom if
            //axiom isn't avilable anyway
        }
    }
    
    private static ConverterKey createConverterKey(Converter<?, ?> converter, Class<?> c) {
        Class<?> interfaces[] = c.getInterfaces();
        for (int x = 0; x < interfaces.length; x++) {
            if (interfaces[x] == Converter.class) {
                ParameterizedType pt = (ParameterizedType)c.getGenericInterfaces()[x];
                ConverterKey key = new ConverterKey();
                key.src = (Class<?>)pt.getActualTypeArguments()[0];
                key.target = (Class<?>)pt.getActualTypeArguments()[1];
                key.converter = converter;
                return key;
            }
        }
        if (c.getSuperclass() != null) {
            return createConverterKey(converter, c.getSuperclass());
        }
        return null;
    }

    public final void registerConverter(Converter<?, ?> converter) {
        ConverterKey key = createConverterKey(converter, converter.getClass());
        registeredConverters.add(key);
    }
    
    
    public QName findQName(Object element) {
        ConverterKey key = findCompatibleConverter(element);

        try {
            return (QName)key.converter.getClass().getMethod("getQName", key.src)
                .invoke(key.converter, element);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("unchecked")
    public Map<QName, String> getAttributes(Object element) {
        ConverterKey key = findCompatibleConverter(element);

        try {
            return (Map<QName, String>)key.converter.getClass().getMethod("getAttributes", key.src)
                .invoke(key.converter, element);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public Iterator<Object> getChildElements(Object element) {
        ConverterKey key = findCompatibleConverter(element);

        try {
            return (Iterator<Object>)key.converter.getClass().getMethod("getChildren", key.src)
                .invoke(key.converter, element);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    } 
    
    public <S, T> T convert(S src, Class<T> target) {
        for (ConverterKey ent : registeredConverters) {
            if (ent.src.isInstance(src) && ent.target.isAssignableFrom(target)) {
                @SuppressWarnings("unchecked")
                Converter<S, T> cv = (Converter<S, T>)ent.converter;
                return cv.convert(src);
            }
        }
        throw new RuntimeException("Could not find a converter to convert from " 
                                   + src.getClass() + " to " + target);
    }

    private ConverterKey findCompatibleConverter(Object element) {
        for (ConverterKey ent : registeredConverters) {
            if (ent.src.isInstance(element)) {
                return ent;
            }
        }
        throw new RuntimeException("Could not find a converter to handle " 
                                   + element.getClass());
    }

}
