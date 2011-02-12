/**
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

import org.apache.neethi.util.PolicyComparator;

public class MergeTest extends PolicyTestCase {
    public MergeTest() {
        super("MergeTest");
    }
    public void testOM() throws Exception {
        doTest("samples2", "merged", 3);
    }
    public void testDOM() throws Exception {
        doTest("samples2", "merged", 1);
    }
    public void testStax() throws Exception {
        doTest("samples2", "merged", 2);
    }
    public void testStream() throws Exception {
        doTest("samples2", "merged", 3);
    }
    public void testW3CDOM() throws Exception {
        doTest("w3tests", "w3tests" + File.separator + "Merged", 1);
    }
    public void testW3COM() throws Exception {
        doTest("w3tests", "w3tests" + File.separator + "Merged", 3);
    }

    public void doTest(String base, String merged, int type) throws Exception {

        File samples2 = new File(testResourceDir, merged);
        File[] files = samples2.listFiles();

        for (int i = 0; i < files.length; i++) {

            if (files[i].isHidden()) {
                continue;
            }

            String f = files[i].getName();
            if (f.startsWith(".")) {
                continue;
            }

            
            String f1 = f.substring(f.indexOf('y') + 1, f.indexOf('-'));
            String f2 = f.substring(f.indexOf('-') + 1, f.indexOf('.'));

            String r1 = base + File.separator + "Policy" + f1 + ".xml";
            String r2 = base + File.separator + "Policy" + f2 + ".xml";
            String r3 = merged + File.separator + f;

            Policy p1 = getPolicy(r1, type);
            Policy p2 = getPolicy(r2, type);
            Policy p3 = getPolicy(r3, type);

            // result
            Policy p4 = (Policy)p1.merge(p2);
            
            p4 = (Policy)p4.normalize(true);

            if (!PolicyComparator.compare(p4, p3)) {
                fail("samples2" + File.separator + " Policy" + f1 + ".merge(Policy" + f2 + ") FAILED");
            }
        }
    }
}
