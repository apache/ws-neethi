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

package secParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.policy.Policy;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyWriter;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Werner Dittmann (werner@apache.org)
 */

public class WSSPolicyPrinter {

    FileInputStream fis = null;

    PolicyReader prdr = null;

    PolicyWriter prwrt = null;

    Policy merged = null;

    public static void main(String[] args) throws Exception {

        WSSPolicyPrinter processor = new WSSPolicyPrinter();
        if (!processor.setup()) {
            return;
        }
        if (args.length == 0) {
            String[] files = new String[2];
            files[0] = "policy/examples/wsse/resources/SecurityPolicyBindingsAsymmTest.xml";
            files[1] = "policy/examples/wsse/resources/SecurityPolicyMsg.xml";
            processor.go(files);            
        }
        else {
            processor.go(args);
        }
    }

    boolean setup() throws NoSuchMethodException {
        prdr = PolicyFactory.getPolicyReader(PolicyFactory.OM_POLICY_READER);
        prwrt = PolicyFactory.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);

        return true;
    }

    void go(String[] args) {

        merged = null;
        for (int i = 0; i < args.length; i++) {
            try {
                System.out.println(new File("").getAbsolutePath());
                fis = new FileInputStream(args[i]);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Policy newPolicy = prdr.readPolicy(fis);
            newPolicy = (Policy) newPolicy.normalize();

            if (merged == null) {
                merged = newPolicy;
            } else {
                merged = (Policy) merged.merge(newPolicy);
            }
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        prwrt.writePolicy(merged, baos);
        prettyPrint(baos);
    }

    public void prettyPrint(ByteArrayOutputStream baos) {

        ByteArrayInputStream stream = new ByteArrayInputStream(baos
                .toByteArray());

        try {

            // Find the implementation
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Create the document
            Document doc = builder.parse(stream);

            // Serialize the document
            OutputFormat format = new OutputFormat(doc);
            format.setLineWidth(80);
            format.setIndenting(true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(System.out, format);
            serializer.serialize(doc);

        } catch (FactoryConfigurationError e) {
            System.out.println("Could not locate a JAXP factory class");
        } catch (ParserConfigurationException e) {
            System.out.println("Could not locate a JAXP DocumentBuilder class");
        } catch (DOMException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (SAXException e) {
            System.err.println(e);
        }

    }

}
