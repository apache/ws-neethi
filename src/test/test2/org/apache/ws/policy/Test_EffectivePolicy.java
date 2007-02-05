package org.apache.ws.policy;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.ws.policy.attachment.WSDLPolicyProcessor;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyRegistry;

public class Test_EffectivePolicy extends PolicyTestCase {

    public Test_EffectivePolicy(String name) {
        super(name);
    }

    public void testService() {

        try {
        	InputStream in = getResource("base/EffectivePolicy_Service.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolService = wpp.getEffectiveServicePolicy(service);
            it = normPolService.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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
            ex.printStackTrace();
            fail();
        }
    }

    public void testEndpoint() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_Endpoint.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolEndpoint = wpp.getEffectiveEndpointPolicy(service,
                    "MyPort");
            it = normPolEndpoint.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public void testOperation() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_Operation.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolOperation = wpp.getEffectiveOperationPolicy(service,
                    "MyPort", "MyOperation");
            it = normPolOperation.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public void testOperation_EmptyBinding() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_Operation_EmptyBinding.wsdl");
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolOperation = wpp.getEffectiveOperationPolicy(service,                
                    "MyPort", "MyOperation");          
            it = normPolOperation.iterator();

            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
       
            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { hour };
            PrimitiveAssertion p2[] = { day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public void testInput() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_Input.wsdl");
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            
            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolInput = wpp.getEffectiveInputPolicy(service,
                    "MyPort", "MyOperation");
            it = normPolInput.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

            int count = 0;
            while (it.hasNext()) {
                count++;
                List alternative = (List) it.next();
                assertTrue("Alternative[" + count + "] value permutation", set
                        .contains(alternative) == true);
            }
            assertTrue("Not all permutations were present", set.isComplete());

        } catch (Exception ex) {
            ex.printStackTrace();
            WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
            fail();
        }
    }

    public void testInput_NotNamed() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_InputNotNamed.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolInput = wpp.getEffectiveInputPolicy(service,
                    "MyPort", "MyOperation");
            it = normPolInput.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public void testOutput() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_Output.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolOutput = wpp.getEffectiveOutputPolicy(service,
                    "MyPort", "MyOperation");
            it = normPolOutput.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public void testOutput_NotNamed() {
        try {
        	InputStream in = getResource("base/EffectivePolicy_OutputNotNamed.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolOutput = wpp.getEffectiveOutputPolicy(service,
                    "MyPort", "MyOperation");
            it = normPolOutput.iterator();

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    /*
     * TODO Faults should be named ...
     * public void testFault() {
     *   try {
     *     InputStream in = getResource("base/EffectivePolicy_Fault.wsdl");
     *     TODO: You get a NPException when you do the following ...
     *     // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
     *     WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in,
     *       new PolicyRegistry());
     * 
     * String ns = "http://policy.tests.webservices.com"; QName service = new
     * QName(ns, "MyService"); Iterator it = null;
     * 
     * Policy normPolFault = wpp.getEffectiveFaultPolicy(service, "MyPort",
     * "MyOperation"); it = normPolFault.iterator();
     * 
     * PrimitiveAssertion yearDay = new PrimitiveAssertion(new
     * QName(TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_YEAR_DAY ));
     * yearDay.setStrValue(TestConstants.VALUE_NUMERIC); PrimitiveAssertion hour =
     * new PrimitiveAssertion(new QName(TestConstants.NAMESPACE_CAL,
     * TestConstants.ASSERTION_HOUR ));
     * hour.setStrValue(TestConstants.VALUE_NUMERIC); PrimitiveAssertion day =
     * new PrimitiveAssertion(new QName(TestConstants.NAMESPACE_CAL,
     * TestConstants.ASSERTION_DAY ));
     * day.setStrValue(TestConstants.VALUE_ALPHA); PrimitiveAssertion logTime =
     * new PrimitiveAssertion(new QName(TestConstants.NAMESPACE_UTIL,
     * TestConstants.ASSERTION_LOG ));
     * logTime.setStrValue(TestConstants.VALUE_TIME);
     * 
     * AssertionSet set = new AssertionSet();
     * 
     * PrimitiveAssertion p1[] = { yearDay, logTime, hour }; PrimitiveAssertion
     * p2[] = { yearDay, logTime, day };
     * 
     * set.clear(); set.add(p1); set.add(p2);
     * 
     * int count = 0; while (it.hasNext()) { count++; List alternative = (List)
     * it.next(); assertTrue("Alternative[" + count + "] value permutation", set
     * .contains(alternative) == true); } assertTrue("Not all permutations were
     * present", set.isComplete());
     *  } catch (Exception ex) { WSPTestSuite.logInfo("Unexpected exception: " +
     * ex.toString()); fail(); } }
     */

    public void testNoPolicies() {

        try {
        	InputStream in = getResource("base/EffectivePolicy_NoPolicies.wsdl");
            // TODO: You get a NPException when you do the following ...
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in);
            // But the following avoid it.
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS, new
            // PolicyRegistry());

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");

            Policy normPolService = wpp.getEffectiveServicePolicy(service);
            assertTrue("Service Policy should be null", normPolService == null);

            Policy normPolEndpoint = wpp.getEffectiveEndpointPolicy(service,
                    "MyPort");
            assertTrue("Endpoint Policy should be null",
                    normPolEndpoint == null);

            /*
             * TODO: Fault should be named ... Policy normPolFault =
             * wpp.getEffectiveFaultPolicy(service, "MyPort", "MyOperation",
             * "MyFault"); assertTrue("Fault Policy should be null",
             * normPolFault == null);
             */

            Policy normPolOutput = wpp.getEffectiveOutputPolicy(service,
                    "MyPort", "MyOperation");
            assertTrue("Output Policy should be null", normPolOutput == null);

            Policy normPolInput = wpp.getEffectiveInputPolicy(service,
                    "MyPort", "MyOperation");
            assertTrue("Input Policy should be null", normPolInput == null);

        } catch (Exception ex) {
            WSPTestSuite.logInfo("Unexpected exception: " + ex.toString());
            fail();
        }
    }

    public void testNames() {

        try {
            // First off we have to create a registry with the Policies that
            // are needed for the test ... just to be tricky, all of these
            // have globally referencable Names, except one embedded Id
            // referenced Policy.
            Vector assertions = null;

            PrimitiveAssertion yearDay = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL,
                    TestConstants.ASSERTION_YEAR_DAY));
            yearDay.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion hour = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_HOUR));
            hour.setStrValue(TestConstants.VALUE_NUMERIC);
            PrimitiveAssertion day = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_CAL, TestConstants.ASSERTION_DAY));
            day.setStrValue(TestConstants.VALUE_ALPHA);
            PolicyReference logTimeReference = new PolicyReference("#LogTime");

            Policy yearDayPolicy = new Policy(
                    "http://policy.tests.webservices.com/YearDay");
            assertions = new Vector();
            assertions.add(yearDay);
            assertions.add(logTimeReference);
            yearDayPolicy.addTerms(assertions);

            InputStream in = getResource("base/EffectivePolicy_ImportedPolicies.xml");
            PolicyReader pReader = PolicyFactory
                    .getPolicyReader(PolicyFactory.OM_POLICY_READER);
            Policy hourAndDay = pReader.readPolicy(in);

            PolicyRegistry myRegistry = new PolicyRegistry();
            myRegistry.register("http://policy.tests.webservices.com/YearDay",
                    yearDayPolicy);
            myRegistry.register(
                    "http://policy.tests.webservices.com/HourAndDay",
                    hourAndDay);

            in = getResource("base/EffectivePolicy_ServicePartial.wsdl");
            // TODO: You get a NPException when you do the following ...
            // WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(fIS);
            WSDLPolicyProcessor wpp = new WSDLPolicyProcessor(in, myRegistry);

            String ns = "http://policy.tests.webservices.com";
            QName service = new QName(ns, "MyService");
            Iterator it = null;

            Policy normPolService = wpp.getEffectiveServicePolicy(service);
            it = normPolService.iterator();

            PrimitiveAssertion logTime = new PrimitiveAssertion(new QName(
                    TestConstants.NAMESPACE_UTIL, TestConstants.ASSERTION_LOG));
            logTime.setStrValue(TestConstants.VALUE_TIME);

            AssertionSet set = new AssertionSet();

            PrimitiveAssertion p1[] = { yearDay, logTime, hour };
            PrimitiveAssertion p2[] = { yearDay, logTime, day };

            set.clear();
            set.add(p1);
            set.add(p2);

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

    public static void main(String[] args) {
        WSPTestSuite suite = new WSPTestSuite(Test_EffectivePolicy.class);
        suite.run();
    }

}