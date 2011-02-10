/*
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

package org.apache.neethi;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.util.PolicyComparator;

public class IntersectTest extends PolicyTestCase {
    int failCount = 0;
    
    public IntersectTest() {
        super("IntersectTest");
    }
    public void testOM() throws Exception {
        doTest("samples2", "intersected", 3);
    }
    public void testDOM() throws Exception {
        doTest("samples2", "intersected", 1);
    }
    public void testStax() throws Exception {
        doTest("samples2", "intersected", 2);
    }
    public void testStream() throws Exception {
        doTest("samples2", "intersected", 3);
    }
    
    public void testW3CDOM() throws Exception {
        doTest("w3tests", "w3tests" + File.separator + "Intersected", 1);
    }

    /*
    public void testMyTest() throws Exception {
        runTest("w3tests", "w3tests" + File.separator + "Intersected",
                "23", "26", "Policy23-26-lax.xml", false, 1);
    }
    */

    public void doTest(String base, String intersectedDir, int type) throws Exception {

        File intersected = new File(testResourceDir, intersectedDir);
        File[] files = intersected.listFiles();

        String f, f1, f2;

        for (int i = 0; i < files.length; i++) {
            f = files[i].getName();

            if (files[i].isHidden()) { // to ignore .svn files
                continue;
            }
            if (f.startsWith(".")) {
                continue;
            }

            
            boolean strict = !f.contains("lax");
            if (f.contains("-lax")) {
                f = f.substring(0, f.indexOf("-lax")) + f.substring(f.indexOf("-lax") + 4);
            }
            if (f.contains("-strict")) {
                f = f.substring(0, f.indexOf("-strict")) + f.substring(f.indexOf("-strict") + 7);
            }
            f1 = f.substring(f.indexOf('y') + 1, f.indexOf('-'));
            
            f2 = f.substring(f.indexOf('-') + 1, f.indexOf('.'));
            
            runTest(base, intersectedDir, f1, f2, files[i].getName(), strict, type);
        }
    }
    public void runTest(String base, String intersectedDir, 
                        String f1, String f2, int type) throws Exception {
        runTest(base, intersectedDir, f1, f2, "Policy" + f1 + "-" + f2 + ".xml", true, type);
    }
    public void runTest(String base, String intersectedDir, 
                        String f1, String f2, String f,
                        boolean strict,
                        int type) throws Exception {
        Policy p1, p2, p3, p4;
        String r1, r2, r3;

        r1 = base + File.separator + "Policy" + f1 + ".xml";
        r2 = base + File.separator + "Policy" + f2 + ".xml";
        r3 = intersectedDir + File.separator + f;

        p1 = getPolicy(r1, type);
        p2 = getPolicy(r2, type);
        p3 = getPolicy(r3, type);

        // result
        p4 = (Policy)p1.intersect(p2, strict);

        if (p4 == null || !PolicyComparator.compare(p4, p3)) {
            /*
             System.out.println(++failCount + " Fail: " + base + File.separator + "Policy" 
                               + f1 + ".intersect(Policy" + f2 + ", "
                               + strict +")");
            */
            fail(base + File.separator + " Policy" + f1 + ".intersect(Policy" 
                 + f2 + ", " + strict + ") FAILED");
            /*
        } else {
            System.out.println("Pass: " + base + File.separator + "Policy" + f1 + ".intersect(Policy" + f2 + ", "
                               + strict +")");
                               */
        }
    }
}
