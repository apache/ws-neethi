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
package org.apache.neethi.util;

import java.util.Iterator;
import java.util.List;

import org.apache.neethi.All;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PrimitiveAssertion;

public class PolicyComparator {

    public static boolean compare(Policy arg1, Policy arg2) {
        return compare(arg1.getPolicyComponents(), arg2.getPolicyComponents());
    }
    
    public static boolean compare(PolicyComponent arg1, PolicyComponent arg2) {
        if (! arg1.getClass().equals(arg2.getClass())) {
            return false;
        }

        if (arg1 instanceof Policy) {
            return compare((Policy) arg1, (Policy) arg2);

        } else if (arg1 instanceof All) {
            return compare((All) arg1,(All) arg2);

        } else if (arg1 instanceof ExactlyOne) {
            return compare((ExactlyOne) arg1,
                    (ExactlyOne) arg2);

        } else if (arg1 instanceof PrimitiveAssertion) {
            return compare((PrimitiveAssertion) arg1, (PrimitiveAssertion) arg2);

        } else {
            // TODO should I throw an exception ..
        }

        return false;
    }

    public static boolean compare(All arg1,
            All arg2) {
        return compare(arg1.getPolicyComponents(), arg2.getPolicyComponents());
    }

    public static boolean compare(ExactlyOne arg1,
            ExactlyOne arg2) {
        return compare(arg1.getPolicyComponents(), arg2.getPolicyComponents());
    }

    public static boolean compare(PrimitiveAssertion arg1,
            PrimitiveAssertion arg2) {
        if (!(arg1.getName().equals(arg2.getName()))) {
            return false;
        }
        return true;
    }

    private static boolean compare(List arg1, List arg2) {
        if (arg1.size() != arg2.size()) {
            return false;
        }
        
        Iterator iterator = arg1.iterator();
        PolicyComponent assertion1;

        while (iterator.hasNext()) {
            assertion1 = (PolicyComponent) iterator.next();

            Iterator iterator2 = arg2.iterator();
            boolean match = false;
            PolicyComponent assertion2;

            while (iterator2.hasNext()) {
                assertion2 = (PolicyComponent) iterator2.next();
                if (compare(assertion1, assertion2)) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                return false;
            }
        }
        return true;
    }
}
