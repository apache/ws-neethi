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
package org.apache.ws.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyTestCase extends TestCase {
	protected String baseDir = System.getProperty("basedir");
	protected String testDir = "test" + File.separator;
	protected String testResourceDir = "test-resources";
	
	public PolicyTestCase(String name) {
		super(name);
		if (baseDir == null) {
			baseDir = (String) (new File(".")).getAbsolutePath();
		}
		testDir = (String) (new File(baseDir, testDir).getAbsolutePath());	
	}	
	
	public InputStream getResource(String name) {
		String filePath = (new File(testResourceDir, name)).getAbsolutePath(); 
		try {
			FileInputStream fis = new FileInputStream(filePath);
			return fis;
		} catch (FileNotFoundException e) {
			fail("Cannot get resource: " + e.getMessage());
			throw new RuntimeException();
		}
	}
}

