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

import javax.xml.namespace.QName;

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

    public PolicyComponent getFirstPolicyComponent() {
        return (PolicyComponent) policyComponents.get(0);
    }

    public boolean isEmpty() {
        return policyComponents.isEmpty();
    }

    protected List crossProduct(ArrayList allTerms, int index,
            boolean matchVacabulary) {

        ArrayList result = new ArrayList();
        ExactlyOne firstTerm = (ExactlyOne) allTerms.get(index);

        List restTerms;

        if (allTerms.size() == ++index) {
            restTerms = new ArrayList();
            All newTerm = new All();
            restTerms.add(newTerm);
        } else {
            restTerms = crossProduct(allTerms, index, matchVacabulary);
        }

        Iterator firstTermIter = firstTerm.getPolicyComponents().iterator();

        while (firstTermIter.hasNext()) {
            
            All assertion = (All) firstTermIter.next();
            Iterator restTermsItr = restTerms.iterator();

            while (restTermsItr.hasNext()) {
                All restTerm = (All) restTermsItr.next();
                All newTerm = new All();

                if (matchVacabulary) {
                    if (matchVocabulary(
                            ((All) assertion).getPolicyComponents(),
                            ((All) restTerm).getPolicyComponents())) {
                        newTerm.addPolicyComponents(((All) assertion)
                                .getPolicyComponents());
                        newTerm.addPolicyComponents(((All) restTerm)
                                .getPolicyComponents());
                        result.add(newTerm);
                    }

                } else {
                    newTerm.addPolicyComponents(((All) assertion)
                            .getPolicyComponents());
                    newTerm.addPolicyComponents(((All) restTerm)
                            .getPolicyComponents());
                    result.add(newTerm);
                }
            }
        }

        return result;
    }

    private boolean matchVocabulary(List assertions1, List assertions2) {

        Iterator S, L;

        if (assertions1.size() < assertions2.size()) {
            S = assertions1.iterator();
            L = assertions2.iterator();
        } else {
            S = assertions2.iterator();
            L = assertions1.iterator();
        }

        QName Sq, Lq;
        boolean found;

        for (; L.hasNext();) {
            Lq = ((PrimitiveAssertion) L.next()).getName();

            found = false;

            for (; S.hasNext();) {
                Sq = ((PrimitiveAssertion) S.next()).getName();

                if (Lq.equals(Sq)) {
                    found = true;
                    break;
                }

            }

            if (!found) {
                return false;
            }
        }
        return true;
    }
}
