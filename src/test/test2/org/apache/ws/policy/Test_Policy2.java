/*
 * Copyright 2004,2006 The Apache Software Foundation.
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.util.PolicyComparator;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyUtil;

public class Test_Policy2 extends PolicyTestCase {
  PolicyReader reader = PolicyFactory
      .getPolicyReader(PolicyFactory.OM_POLICY_READER);

  public Test_Policy2(String name) {
    super(name);
  }

  public void testNormalizeNested() throws Exception {
    String fileName;
    Policy policy;

    fileName = "base" + File.separator + "Policy_Vocabulary.xml";
    policy = reader.readPolicy(getResource(fileName));
    
    List vocab = policy.getVocabulary();
    
    assertTrue("Normalize test", vocab.size() == 5 );
    
  }

  public static void main(String[] args) {
    WSPTestSuite suite = new WSPTestSuite(Test_Policy2.class);
    suite.run();
  }

}