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

import org.apache.neethi.util.PolicyComparator;

public class IntersectTest extends PolicyTestCase {

    public IntersectTest() {
        super("IntersectTest");
    }

    public void test() throws Exception {
        String r1, r2, r3;
        Policy p1, p2, p3, p4;

        File intersected = new File(testResourceDir, "intersected");
        File[] files = intersected.listFiles();

        String f, f1, f2;

        for (int i = 0; i < files.length; i++) {
            f = files[i].getName();

            if (files[i].isHidden()) { // to ignor .svn files
                continue;
            }

            f1 = f.substring(f.indexOf('y') + 1, f.indexOf('-'));
            f2 = f.substring(f.indexOf('-') + 1, f.indexOf('.'));

            r1 = "samples2" + File.separator + "Policy" + f1 + ".xml";
            r2 = "samples2" + File.separator + "Policy" + f2 + ".xml";
            r3 = "intersected" + File.separator + f;

            p1 = getPolicy(r1);
            p2 = getPolicy(r2);
            p3 = getPolicy(r3);

            try {
                // result
                p4 = (Policy)p1.intersect(p2);
    
                if (!PolicyComparator.compare(p4, p3)) {
    
                    fail("samples2" + File.separator + " Policy" + f1 + ".intersect(Policy" + f2 + ") FAILED");
                }
            } catch (UnsupportedOperationException e) {
                //expected for now... :-(
            }
        }
    }
}
