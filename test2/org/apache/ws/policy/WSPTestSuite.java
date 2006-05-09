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
import junit.framework.TestSuite;
import junit.framework.TestResult;

import java.util.Date;
import java.text.*;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * <p> 
 * @version 1.0.0 
 */
public class WSPTestSuite extends TestSuite {

  private Timer _timer = null;
  
  public WSPTestSuite(Class testcase) {
    StringTokenizer st = new StringTokenizer(testcase.toString(), ".");
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (st.hasMoreTokens() == false) {
        super.setName(token);
      }
    }
    super.addTestSuite(testcase);
  }

  private void logSuiteStart() {
    DateFormat now = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    System.out.println("======================================================================");
    System.out.println(">>> START(" + this.getName() + ")");
    System.out.println(">>> " + now.format(new Date()));
    System.out.println(">>> Number of tests = " + this.countTestCases());
    System.out.println("    -------------------------------------");
    _timer = new Timer(this.getName());
    _timer.start("elapsedTime");
  }

  private void logSuiteEnd(TestResult results) {
    DateFormat now = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    System.out.println("    -------------------------------------");
    System.out.println(">>> END(" + this.getName() + ")");

    Enumeration failures = results.failures();

    if (failures.hasMoreElements() == false) {
      System.out.println(">>> Successful run; [" + results.runCount() + "] tests completed");
    } else {
      System.out.println(">>> Failures detected; [" + results.failureCount() + "/" + results.runCount()+ "] tests failed");
      int count = 1;
      while (failures.hasMoreElements()) {
        System.out.println("      [" + count + "] " + failures.nextElement());
        count++;
      }
    }
    System.out.println("======================================================================");
    _timer.stop();
    System.out.println("======================================================================");
    
  }

  public void run() {
    TestResult results = new TestResult();
    logSuiteStart();

    Enumeration tests = this.tests();
    int i = 0;
    while (tests.hasMoreElements()) {
      TestSuite testS = (TestSuite) tests.nextElement();
      int before = 0;
      int after = 0;
      String outcome = null;
      for (int j = 0; j < testS.testCount(); j++) {
        before = results.failureCount();
        TestCase tc = (TestCase) testS.testAt(j);

        tc.run(results);
        after = results.failureCount();
        if (before == after) {
          outcome = "SUCCESS";

        } else {
          outcome = "*** FAIL ***";
        }
        System.out.println("    " + tc.getName() + ": " + outcome);

      }
    }
    logSuiteEnd(results);
  }

  public static void logInfo(String info) {
    System.out.println("      --> " + info);
  }

}
