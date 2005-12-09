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

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyFactory {
    public static final int OM_POLICY_READER = 1;
    public static final int StAX_POLICY_WRITER = 2;
    
    public static final int DOM_POLICY_READER = 3;
    
    
    public static PolicyReader getPolicyReader(int type) {
    	String name = null;
        switch (type) {
        case DOM_POLICY_READER:
        	name = "org.apache.ws.policy.util.DOMPolicyReader";
        	break;
        case OM_POLICY_READER:
        	name = "org.apache.ws.policy.util.OMPolicyReader";
        	break;
        default: 
            throw new IllegalArgumentException("Unknown PolicyReader type ..");
        }
        try {
			return (PolicyReader)Loader.loadClass(name).newInstance();
		} catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot load PolicyReader type ..");
		} catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot load PolicyReader type ..");
		} catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load PolicyReader type ..");
		}
    }
    
    public static PolicyWriter getPolicyWriter(int type) {
    	String name = null;
        switch (type) {
        case StAX_POLICY_WRITER:
        	name = "org.apache.ws.policy.util.StAXPolicyWriter";
        	break;
        default: 
            throw new IllegalArgumentException("Unknown PolicyWriter type ..");
        }
        try {
			return (PolicyWriter)Loader.loadClass(name).newInstance();
		} catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot load PolicyWriter type ..");
		} catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot load PolicyWriter type ..");
		} catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load PolicyWriter type ..");
		}
    }
    
}
