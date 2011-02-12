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

public class Normalize2Test extends PolicyTestCase {

    public Normalize2Test() {
        super("Normalize2Test");
    }
    public void testOM() throws Exception {
        doTest(3);
    }
    public void testDOM() throws Exception {
        doTest(1);
    }
    public void testStax() throws Exception {
        doTest(2);
    }
    public void testStream() throws Exception {
        doTest(3);
    }

    public void doTest(int type) throws Exception {

        for (int i = 1; i < 21; i++) {

            String r1 = "samples2" + File.separator + "Policy" + i + ".xml";
            String r2 = "normalized2" + File.separator + "Policy" + i + ".xml";

            Policy p1 = getPolicy(r1, type);
            p1 = (Policy)p1.normalize(true);

            Policy p2 = getPolicy(r2, type);

            if (!PolicyComparator.compare(p1, p2)) {
                fail("samples2" + File.separator + "Policy" + i + ".normalize() FAILED");
            }
        }
    }
}
