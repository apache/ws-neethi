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
import java.io.FileOutputStream;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyWriter;

public class Test_JIRA14 extends TestCase {

  QName q1 = new QName("http://www.apache.org/policyTest", "q1");
  QName q2 = new QName("http://www.apache.org/policyTest", "q2");
  QName q3 = new QName("http://www.apache.org/policyTest", "q3");
  QName q4 = new QName("http://www.apache.org/policyTest", "q4");

  /**
   * Constructor
   */
  public Test_JIRA14(String name) {
    super(name);
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
      assertTrue("Should be no attributes", attributes.size() == 1);

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
      String value = null;
      assertTrue("Attribute number check [" + attributes.size() + "]",
          attributes.size() == 5);

      pol.clearAttributes();

      attributes = pol.getAttributes();
      assertTrue("Should be no attributes", attributes.size() == 0);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testCompleteAttributes() {
    try {
      String myId = "myId";
      Policy pol = new Policy(myId);
      assertTrue("INCORRECT Id", pol.getId().equals(myId));

      Hashtable myHashtable = new Hashtable();

      myHashtable.put(q1, "First");
      myHashtable.put(q2, "Second");
      myHashtable.put(q3, "Third");
      myHashtable.put(q4, "Fourth");

      pol.setAttributes(myHashtable);

      Hashtable attributes = pol.getAttributes();
      String value = null;
      assertTrue("Attribute number check [" + attributes.size() + "]",
          attributes.size() == 4);

      value = (String) attributes.get((Object) q4);
      assertTrue("Attribute value check for [Fourth]", value.equals("Fourth"));
      value = (String) attributes.get((Object) q1);
      assertTrue("Attribute value check for [First]", value.equals("First"));
      value = (String) attributes.get((Object) q3);
      assertTrue("Attribute value check for [Third]", value.equals("Third"));
      value = (String) attributes.get((Object) q2);
      assertTrue("Attribute value check for [Second]", value.equals("Second"));

      pol.clearAttributes();

      attributes = pol.getAttributes();
      assertTrue("Should be no attributes", attributes.size() == 0);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public void testReadAttributes() {
    try {
      File f = new File("test-resources/base/Policy_Optional_MixedNested.xml");
      FileInputStream in = new FileInputStream(f);
      PolicyReader pReader = PolicyFactory
          .getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy pol = pReader.readPolicy(in);

      QName qNameA = new QName(TestConstants.NAMESPACE_CAL, "attributeA");
      QName qNameB = new QName(TestConstants.NAMESPACE_CAL, "attributeB");
      QName qNameC = new QName(TestConstants.NAMESPACE_CAL, "attributeC");
      
      assertTrue("Attribute B INCORRECT", pol.getAttribute(qNameB).equals("B"));
      assertTrue("Attribute A INCORRECT", pol.getAttribute(qNameA).equals("A"));
      assertTrue("Attribute C INCORRECT", pol.getAttribute(qNameC).equals("C"));

      // Now do the same for DOM ...
      f = new File("test-resources/base/Policy_Optional_MixedNested.xml");
      in = new FileInputStream(f);
      pReader = PolicyFactory.getPolicyReader(PolicyFactory.DOM_POLICY_READER);
      pol = pReader.readPolicy(in);

      assertTrue("Attribute B INCORRECT", pol.getAttribute(qNameB).equals("B"));
      assertTrue("Attribute A INCORRECT", pol.getAttribute(qNameA).equals("A"));
      assertTrue("Attribute C INCORRECT", pol.getAttribute(qNameC).equals("C"));
      
      // Write it out ...
      PolicyWriter pWriter = PolicyFactory
          .getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
      File tempFile = buildFile("tempFile");
      FileOutputStream fos = new FileOutputStream(tempFile);
      pWriter.writePolicy(pol, fos);

      // Read it back ... it should have the same attributes
      in = new FileInputStream(tempFile);
      pReader = PolicyFactory.getPolicyReader(PolicyFactory.OM_POLICY_READER);
      Policy polRead = pReader.readPolicy(in);      
      
      assertTrue("Attribute B INCORRECT", pol.getAttribute(qNameB).equals("B"));
      assertTrue("Attribute A INCORRECT", pol.getAttribute(qNameA).equals("A"));
      assertTrue("Attribute C INCORRECT", pol.getAttribute(qNameC).equals("C"));     

    } catch (Exception ex) {
      WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
      fail();
    }
  }

  public static void main(String[] args) {
    WSPTestSuite suite = new WSPTestSuite(Test_JIRA14.class);
    suite.run();
  }
  
  private File buildFile(String fileName) {
    File file = null;
    try {
      String sep = System.getProperty("file.separator");
      String fullFile = "." + sep + fileName + ".xml";

      file = new File(fullFile);

    } catch (Exception ex) {
      WSPTestSuite.logInfo("buildFile: Unexpected exception: " + ex.toString());
      fail();
    }
    return file;
  }

}