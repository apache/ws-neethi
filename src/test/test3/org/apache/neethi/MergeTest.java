/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.neethi;

import java.io.File;

import org.apache.neethi.util.PolicyComparator;

public class MergeTest extends PolicyTestCase {
    
    public MergeTest() {
        super("MergeTest");
    }

    public void test() throws Exception {
        String r1, r2, r3;
        Policy p1, p2, p3, p4;

        File samples2 = new File(testResourceDir, "merged");
        File[] files = samples2.listFiles();

        String f, f1, f2;

        for (int i = 0; i < files.length; i++) {

            if (files[i].isHidden()) {
                continue;
            }

            f = files[i].getName();

            f1 = f.substring(f.indexOf('y') + 1, f.indexOf('-'));
            f2 = f.substring(f.indexOf('-') + 1, f.indexOf('.'));

            r1 = "samples2" + File.separator + "Policy" + f1 + ".xml";
            r2 = "samples2" + File.separator + "Policy" + f2 + ".xml";
            r3 = "merged" + File.separator + f;

            p1 = PolicyEngine.getPolicy(getResourceAsElement(r1));
            p2 = PolicyEngine.getPolicy(getResourceAsElement(r2));
            p3 = PolicyEngine.getPolicy(getResourceAsElement(r3));

            // result
            p4 = (Policy) p1.merge(p2);

            if (!PolicyComparator.compare(p4, p3)) {
//                System.out.println("samples2" + File.separator + " Policy" + f1
//                        + ".merge(Policy" + f2 + ") FAILED");
                fail("samples2" + File.separator + " Policy" + f1
                        + ".merge(Policy" + f2 + ") FAILED");
            }
        }
    }

}
