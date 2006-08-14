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

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.policy.util.PolicyComparator;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;


public class Test_Policy extends PolicyTestCase {

  Policy pol4, pol5, pol6, pol7;

  PrimitiveAssertion la1, la2, la3;

  QName q1 = new QName("http://www.apache.org/policyTest", "q1");

  QName q2 = new QName("http://www.apache.org/policyTest", "q2");

  QName q3 = new QName("http://www.apache.org/policyTest", "q3");

  QName q4 = new QName("http://www.apache.org/policyTest", "q4");

  /**
   * Constructor
   */
  public Test_Policy(String name) {
    super(name);
  }

  public void testAlternatives_AllEmpty() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_AllEmpty.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);
      Iterator it = pol.iterator();
      assertTrue("Should be an Alternative", it.hasNext() == true);
      List alternative = (List) it.next();
      assertTrue("Should be EMPTY Alternative", alternative.size() == 0);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_EOEmpty() {

    try {
      InputStream in = getResource("base/Policy_Alternatives_EOEmpty.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      Iterator it = pol.iterator();
      assertTrue("Should be NO Alternative", it.hasNext() == false);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_PolicyEmpty() {

    try {
      InputStream in = getResource("base/Policy_Alternatives_PolicyEmpty.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      Iterator it = pol.iterator();
      assertTrue("Should be an Alternative", it.hasNext() == true);
      List alternative = (List) it.next();
      assertTrue("Should be EMPTY Alternative", alternative.size() == 0);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }

  }

  public void testAlternatives_AllSingle() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_AllSingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };

      set.clear();
      set.add(p1);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_EOSingle() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_EOSingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };

      set.clear();
      set.add(p1);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_PolicySingle() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_PolicySingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };

      set.clear();
      set.add(p1);
      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_AllMany() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_AllMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log, month, monthDay, yearDay };

      set.clear();
      set.add(p1);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_EOMany() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_EOMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = { log };
      PrimitiveAssertion p3[] = { month };
      PrimitiveAssertion p4[] = { monthDay };
      PrimitiveAssertion p5[] = { yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);
      set.add(p5);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_PolicyMany() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_PolicyMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log, month, monthDay, yearDay };

      set.clear();
      set.add(p1);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_AllNested() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_AllNested.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log };
      PrimitiveAssertion p2[] = { month, monthDay };
      PrimitiveAssertion p3[] = { year, yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_EONested() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_EONested.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, month, year };
      PrimitiveAssertion p2[] = { hour, monthDay, year };
      PrimitiveAssertion p3[] = { hour, month, yearDay };
      PrimitiveAssertion p4[] = { hour, monthDay, yearDay };
      PrimitiveAssertion p5[] = { log, month, year };
      PrimitiveAssertion p6[] = { log, month, yearDay };
      PrimitiveAssertion p7[] = { log, monthDay, year };
      PrimitiveAssertion p8[] = { log, monthDay, yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);
      set.add(p5);
      set.add(p6);
      set.add(p7);
      set.add(p8);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_PolicyNested() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_PolicyNested.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log, year, yearDay };
      PrimitiveAssertion p2[] = { month, monthDay, year, yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_MixedNested() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_MixedNested.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameUnknownX = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_UNKNOWN);
      QName qNameUnknownY = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_UNKNOWN);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue("UNSUPPORTED_XXX");
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue("UNSUPPORTED_YYY");
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion unknownX = new PrimitiveAssertion(qNameUnknownX);
      unknownX.setStrValue("XXX");
      PrimitiveAssertion unknownY = new PrimitiveAssertion(qNameUnknownY);
      unknownY.setStrValue("YYY");

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, unknownX, yearDay, monthDay };
      PrimitiveAssertion p2[] = { hour, unknownX, unknownY, monthDay };
      PrimitiveAssertion p3[] = { month, yearDay, monthDay };
      PrimitiveAssertion p4[] = { month, unknownY, monthDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);
      
      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_AllSingle() {
    try {
      InputStream in = getResource("base/Policy_Optional_AllSingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = {};

      set.clear();
      set.add(p1);
      set.add(p2);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_EOSingle() {
    try {
      InputStream in = getResource("base/Policy_Optional_EOSingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = {};

      set.clear();
      set.add(p1);
      set.add(p2);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());
      
    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_PolicySingle() {
    try {
      InputStream in = getResource("base/Policy_Optional_PolicySingle.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = {};

      set.clear();
      set.add(p1);
      set.add(p2);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_AllMany() {
    try {
      InputStream in = getResource("base/Policy_Optional_AllMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log, month, monthDay, yearDay };
      PrimitiveAssertion p2[] = { hour, log, month, yearDay };
      PrimitiveAssertion p3[] = { hour, month, monthDay, yearDay };
      PrimitiveAssertion p4[] = { hour, month, yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);

      Policy polNormalized = (Policy) pol.normalize();

      Iterator it = polNormalized.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_EOMany() {
    try {
      InputStream in = getResource("base/Policy_Optional_EOMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);
 
      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = { log };
      PrimitiveAssertion p3[] = { month };
      PrimitiveAssertion p4[] = { monthDay };
      PrimitiveAssertion p5[] = { yearDay };
      PrimitiveAssertion p6[] = {};

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);
      set.add(p5);
      set.add(p6);

      Policy normalizedPolicy = (Policy) pol.normalize();

      Iterator it = normalizedPolicy.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_PolicyMany() {
    try {
      InputStream in = getResource("base/Policy_Optional_PolicyMany.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameYear = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue(TestConstants.VALUE_ALPHA);
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion year = new PrimitiveAssertion(qNameYear);
      year.setStrValue(TestConstants.VALUE_NUMERIC);

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, log, month, monthDay, yearDay };
      PrimitiveAssertion p2[] = { hour, log, month, yearDay };
      PrimitiveAssertion p3[] = { hour, month, monthDay, yearDay };
      PrimitiveAssertion p4[] = { hour, month, yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_MixedNested() {
    try {
      InputStream in = getResource("base/Policy_Optional_MixedNested.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameMonth = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH);
      QName qNameMonthDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_MONTH_DAY);
      QName qNameYearDay = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_YEAR_DAY);
      QName qNameUnknownX = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_UNKNOWN);
      QName qNameUnknownY = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_UNKNOWN);

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion month = new PrimitiveAssertion(qNameMonth);
      month.setStrValue("UNSUPPORTED_XXX");
      PrimitiveAssertion monthDay = new PrimitiveAssertion(qNameMonthDay);
      monthDay.setStrValue("UNSUPPORTED_YYY");
      PrimitiveAssertion yearDay = new PrimitiveAssertion(qNameYearDay);
      yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
      PrimitiveAssertion unknownX = new PrimitiveAssertion(qNameUnknownX);
      unknownX.setStrValue("XXX");
      PrimitiveAssertion unknownY = new PrimitiveAssertion(qNameUnknownY);
      unknownY.setStrValue("YYY");

      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour, unknownX, yearDay, monthDay };
      PrimitiveAssertion p2[] = { hour, unknownX, unknownY, monthDay };
      PrimitiveAssertion p3[] = { hour, unknownX, unknownY };
      PrimitiveAssertion p4[] = { hour, unknownX, monthDay };
      PrimitiveAssertion p5[] = { hour, unknownX };
      PrimitiveAssertion p6[] = { hour, unknownX, yearDay };

      PrimitiveAssertion p7[] = { month, unknownY, monthDay };
      PrimitiveAssertion p8[] = { month, yearDay, monthDay };
      PrimitiveAssertion p9[] = { month, unknownY };
      PrimitiveAssertion p10[] = { month, monthDay };
      PrimitiveAssertion p11[] = { month };
      PrimitiveAssertion p12[] = { month, yearDay };

      PrimitiveAssertion p13[] = { unknownX, unknownY, monthDay };
      PrimitiveAssertion p14[] = { unknownX, yearDay, monthDay };
      PrimitiveAssertion p15[] = { unknownX, unknownY };
      PrimitiveAssertion p16[] = { unknownX, monthDay };
      PrimitiveAssertion p17[] = { unknownX };
      PrimitiveAssertion p18[] = { unknownX, yearDay };

      PrimitiveAssertion p19[] = { unknownY, monthDay };
      PrimitiveAssertion p20[] = { yearDay, monthDay };
      PrimitiveAssertion p21[] = { unknownY };
      PrimitiveAssertion p22[] = { monthDay };
      PrimitiveAssertion p23[] = {};
      PrimitiveAssertion p24[] = { yearDay };

      set.clear();
      set.add(p1);
      set.add(p2);
      set.add(p3);
      set.add(p4);
      set.add(p5);
      set.add(p6);
      set.add(p7);
      set.add(p8);
      set.add(p9);
      set.add(p10);
      set.add(p11);
      set.add(p12);
      set.add(p13);
      set.add(p14);
      set.add(p15);
      set.add(p16);
      set.add(p17);
      set.add(p18);
      set.add(p19);
      set.add(p20);
      set.add(p21);
      set.add(p22);
      set.add(p23);
      set.add(p24);

      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOptional_Explosion() {
    try {
      InputStream in = getResource("base/Policy_Optional_Explosion.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);
         
      Iterator it = pol.iterator();
      
      long alternativesCount = 0;
      while (it.hasNext()) {
    	assertTrue(it.next() instanceof List);
        alternativesCount++;    
      }
      assertTrue("Number of Alternatives [" + alternativesCount + "] should be 65536", alternativesCount == 65536);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testAlternatives_Duplicates() {
    try {
      InputStream in = getResource("base/Policy_Alternatives_Duplicates.xml");
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameHour = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_HOUR);
      QName qNameLog = new QName(TestConstants.NAMESPACE_CAL,
          TestConstants.ASSERTION_LOG);   

      PrimitiveAssertion hour = new PrimitiveAssertion(qNameHour);
      hour.setStrValue(TestConstants.VALUE_24_HOUR);
      PrimitiveAssertion log = new PrimitiveAssertion(qNameLog);
      log.setStrValue(TestConstants.VALUE_ID);
     
      AssertionSet set = new AssertionSet();

      PrimitiveAssertion p1[] = { hour };
      PrimitiveAssertion p2[] = { log };
     
      set.clear();
      set.add(p1);
      set.add(p2);
 
      Iterator it = pol.iterator();
      int count = 0;
      while (it.hasNext()) {
        count++;     
        List alternative = (List) it.next();
        assertTrue("Alternative[" + count + "] value permutation", set
            .contains(alternative) == true);
      }
      assertTrue("Not all permutations were present", set.isComplete());

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }
  
  public void testAddAttribute() {
    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      pol.addAttribute(q1, "First");
      pol.addAttribute(q2, "Second");
      pol.addAttribute(q3, "Third");
      pol.addAttribute(q4, "Fourth");

      Hashtable attributes = pol.getAttributes();
      String value = null;
      assertTrue("Attribute number check [" + attributes.size() + "]",
          attributes.size() == 5);

      value = pol.getAttribute(q4);
      assertTrue("Attribute value check for [Fourth]", value.equals("Fourth"));
      value = pol.getAttribute(q1);
      assertTrue("Attribute value check for [First]", value.equals("First"));
      value = pol.getAttribute(q3);
      assertTrue("Attribute value check for [Third]", value.equals("Third"));
      value = pol.getAttribute(q2);
      assertTrue("Attribute value check for [Second]", value.equals("Second"));

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testRemoveAttribute() {
    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      pol.addAttribute(q1, "First");
      String value = pol.getAttribute(q1);
      assertTrue("Attribute value check", value.equals("First"));
      pol.removeAttribute(q1);
      Hashtable attributes = pol.getAttributes();
      assertTrue("Should only be ONE attribute", attributes.size() == 1);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testOverwriteAttribute() {
    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      String newId = "newId";
      pol.setId(newId);
      assertTrue("INCORRECT Id from overwrite", pol.getId().equals(newId));

      pol.addAttribute(q1, "First");
      String value = pol.getAttribute(q1);
      assertTrue("Attribute value check", value.equals("First"));

      pol.addAttribute(q1, "Overwrite");
      value = pol.getAttribute(q1);
      assertTrue("Attribute value check", value.equals("Overwrite"));

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testUnknownAttribute() {

    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      String value = pol.getAttribute(q1);
      assertTrue("Unexpected value [" + value + "] from q1", value == null);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testClearAllAttributes() {
    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      pol.addAttribute(q1, "First");
      pol.addAttribute(q2, "Second");
      pol.addAttribute(q3, "Third");
      pol.addAttribute(q4, "Fourth");

      Hashtable attributes = pol.getAttributes();
      assertTrue("Attribute number check [" + attributes.size() + "]",
          attributes.size() == 5);

      pol.clearAttributes();

      attributes = pol.getAttributes();
      assertTrue("Should be no attributes", attributes.size() == 0);
      
      assertTrue("Id should have been cleared", pol.getId() == null);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testEquality() {
    try {
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      InputStream in = getResource("base/Policy_Equality1.xml");
      Policy pol1 = pReader.readPolicy(in);

      in = getResource("base/Policy_Equality2.xml");
      Policy pol2 = pReader.readPolicy(in);

      assertTrue("Comparator: Policy1 should be EQUAL to Policy2",
          PolicyComparator.compare(pol1, pol2) == true);
      assertTrue("Comparator: Policy2 should be EQUAL to Policy1",
          PolicyComparator.compare(pol2, pol1) == true);

      assertTrue("Policy1 Should be EQUAL to Policy2", pol1.equals(pol2));
      assertTrue("Policy2 Should be EQUAL to Policy1", pol2.equals(pol1));

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public static void main(String[] args) {
    WSPTestSuite suite = new WSPTestSuite(Test_Policy.class);
    suite.run();
  }

}