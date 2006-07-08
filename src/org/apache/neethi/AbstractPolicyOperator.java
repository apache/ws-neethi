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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractPolicyOperator implements PolicyOperator {
    protected ArrayList policyComponents = new ArrayList();
    
    public void addPolicyComponent(PolicyComponent component) {
        policyComponents.add(component);
    }
    
    public void addPolicyComponents(List components) {
        policyComponents.addAll(components);
    }
    
    public List getPolicyComponents() {
        return policyComponents;
    }
    
    public boolean isEmpty() {
        return policyComponents.isEmpty();
    }
    
    protected List crossProduct(ArrayList allTerms, int index) {

        ArrayList result = new ArrayList();
        ExactlyOne firstTerm = (ExactlyOne) allTerms
                .get(index);
        
        List restTerms;
        
        if (allTerms.size() == ++index) {
            restTerms = new ArrayList();
            All newTerm = new All();
            restTerms.add(newTerm);
        } else {
            restTerms = crossProduct(allTerms, index);
        }

        Iterator firstTermIter = firstTerm.getPolicyComponents().iterator();
        
        while (firstTermIter.hasNext()) {
            Assertion assertion = (Assertion) firstTermIter.next();
            Iterator restTermsItr = restTerms.iterator();
            
            while (restTermsItr.hasNext()) {
                Assertion restTerm = (Assertion) restTermsItr.next();
                All newTerm = new All();
                newTerm.addPolicyComponents(((All) assertion).getPolicyComponents());
                newTerm.addPolicyComponents(((All) restTerm).getPolicyComponents());
                result.add(newTerm);
            }
        }
        
        return result;
    }
    
}
