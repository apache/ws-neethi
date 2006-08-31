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

import org.apache.neethi.util.PolicyComparator;

/**
 * 
 *
 */
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

    public PolicyComponent normalize() {
        return normalize(true);
    }

    /**
     * normalized form of the the assertion, all and Exactly one define as
     * <ExactlyOne>
     * <All>
     * <Assertion/>
     * </All>
     * <ExactlyOne>
     *
     * @param deep - normalize the assertions or not - currently assertion normalization is not implemented
     * @return the normalize form of this policy commponent
     */

    public PolicyComponent normalize(boolean deep) {
        return AbstractPolicyOperator.normalize(this, deep);
    }
    
    /**
     * here it is assumed that the two arguments passed to the method are not empty arguments
     * and exactlyOne1 and exactlyOne2 in normal form
     *
     * @param exactlyOne1
     * @param exactlyOne2
     * @return cross product of the two exactlyones
     */
    private static ExactlyOne getCrossProduct(ExactlyOne exactlyOne1, ExactlyOne exactlyOne2) {
        ExactlyOne crossProduct = new ExactlyOne();
        All crossProductAll;

        All currentAll1;
        All currentAll2;

        for (Iterator iter1 = exactlyOne1.getPolicyComponents().iterator(); iter1.hasNext();) {
            currentAll1 = (All) iter1.next();

            for (Iterator iter2 = exactlyOne2.getPolicyComponents().iterator(); iter2.hasNext();) {
                currentAll2 = (All) iter2.next();
                crossProductAll = new All();
                crossProductAll.addPolicyComponents(currentAll1.getPolicyComponents());
                crossProductAll.addPolicyComponents(currentAll2.getPolicyComponents());
                crossProduct.addPolicyComponent(crossProductAll);
            }
        }

        return crossProduct;
    }
        
    private static PolicyComponent normalize(PolicyOperator operator, boolean deep) {
                
        short type = operator.getType();
                
        
        if (operator.isEmpty()) {
            ExactlyOne exactlyOne = new ExactlyOne();
            
            if (PolicyComponent.EXACTLYONE != type) {
                exactlyOne.addPolicyComponent(new All());
            }
            return exactlyOne;
        }
                
        ArrayList childComponentsList = new ArrayList();
        PolicyComponent policyComponent;
        
        for (Iterator iterator = operator.getPolicyComponents().iterator(); iterator.hasNext();) {
            policyComponent = (PolicyComponent) iterator.next();
            
            if (policyComponent.getType() == PolicyComponent.ASSERTION) {
                policyComponent = ((Assertion) policyComponent).normalize();
                
                if (policyComponent.getType() == PolicyComponent.POLICY) {
                    childComponentsList.add(((Policy) policyComponent).getFirstPolicyComponent());
                    
                } else  {
                    ExactlyOne exactlyOne = new ExactlyOne();
                    All all = new All();
                    
                    all.addPolicyComponent(policyComponent);
                    exactlyOne.addPolicyComponent(all);
                    childComponentsList.add(exactlyOne);
                }
                
            } else if (policyComponent.getType() == PolicyComponent.POLICY) {
                All all = new All();
                all.addPolicyComponents(((Policy) policyComponent).getPolicyComponents());
                childComponentsList.add(all.normalize(deep));
                
            } else {
                childComponentsList.add(((AbstractPolicyOperator) policyComponent).normalize(deep));
            }            
        }
        
        return computeResultantComponent(childComponentsList, type);
    }
    
    /**
     * 
     * @param normalizedInnerComponets
     * @param componentType
     * @return
     */
    private static PolicyComponent computeResultantComponent(List normalizedInnerComponets, short componentType) {
        
        ExactlyOne exactlyOne = new ExactlyOne();
        
        if (componentType == PolicyComponent.EXACTLYONE) {            
            ExactlyOne innerExactlyOne;
            
            for (Iterator iter = normalizedInnerComponets.iterator(); iter.hasNext();) {
                innerExactlyOne = (ExactlyOne) iter.next();
                exactlyOne.addPolicyComponents(innerExactlyOne.getPolicyComponents());
            }
            
        } else if ((componentType == PolicyComponent.POLICY) || (componentType == PolicyComponent.ALL)) {
            // if the parent type is All then we have to get the cross product
            if (normalizedInnerComponets.size() > 1) {
                // then we have to get the cross product with each other to process all elements
                Iterator iter = normalizedInnerComponets.iterator();
                // first get the first element
                exactlyOne = (ExactlyOne) iter.next();
                // if this is empty, this is an not admissible policy and total result is equalent to that
                if (!exactlyOne.isEmpty()) {
                    ExactlyOne currentExactlyOne;

                    for (; iter.hasNext();) {
                        currentExactlyOne = (ExactlyOne) iter.next();
                        if (currentExactlyOne.isEmpty()) {
                            // if this is empty, this is an not admissible policy and total result is equalent to that
                            exactlyOne = currentExactlyOne;
                            break;
                        } else {
                            exactlyOne = getCrossProduct(exactlyOne, currentExactlyOne);
                        }
                    }

                }

            } else {
                // i.e only one element exists in the list then we can safely
                // return that element this is ok even if it is an empty element
                exactlyOne = (ExactlyOne) normalizedInnerComponets.iterator().next();
            }
        }
        
        return exactlyOne;
    }

    public boolean equal(PolicyComponent policyComponent) {
        return PolicyComparator.compare(this, policyComponent);
    }
}
