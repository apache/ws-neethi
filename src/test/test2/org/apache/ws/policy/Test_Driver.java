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

import junit.framework.TestCase;

public class Test_Driver extends TestCase {

  public Test_Driver(String name) {
    super(name);
  }

  public void testStartDriver() {
  }

  public static void main(String[] args) {
    WSPTestSuite suite = new WSPTestSuite(Test_Driver.class);

    suite.addTestSuite(Test_JIRA14.class);
    suite.addTestSuite(Test_Policy.class);
    suite.addTestSuite(Test_EffectivePolicy.class);
    suite.addTestSuite(Test_Policy2.class);

    suite.run();
  }

}