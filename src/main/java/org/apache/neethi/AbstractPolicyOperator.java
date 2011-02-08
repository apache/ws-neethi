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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.neethi.util.PolicyComparator;

/**
 * AbstractPolicyOperator provides an implementation of few functions of 
 * PolicyOperator interface that other PolicyOperators can use.
 */
public abstract class AbstractPolicyOperator implements PolicyOperator {
    protected List<PolicyComponent> policyComponents = new ArrayList<PolicyComponent>();

    public void addPolicyComponent(PolicyComponent component) {
        policyComponents.add(component);
    }

    public void addPolicyComponents(List<? extends PolicyComponent> components) {
        policyComponents.addAll(components);
    }

    public List<PolicyComponent> getPolicyComponents() {
        return policyComponents;
    }

    public PolicyComponent getFirstPolicyComponent() {
        return policyComponents.get(0);
    }

    public boolean isEmpty() {
        return policyComponents.isEmpty();
    }
    
    public boolean equal(PolicyComponent policyComponent) {
        return PolicyComparator.compare(this, policyComponent);
    }
    
    protected static Policy normalize(Policy policy, PolicyRegistry reg, boolean deep) {
        Policy result = new Policy();
        
        String policyName = policy.getName();
        if (policyName != null) {
            result.setName(policyName);
        }
        
        String id = policy.getId();
        if (id != null) {
            result.setId(id);
        }
        
        
        result.addPolicyComponent(normalizeOperator(policy,reg, deep));
        return result;
    }
    
    private static PolicyComponent normalizeOperator(PolicyOperator operator, PolicyRegistry reg, boolean deep) {
                        
        short type = operator.getType();
                
        
        if (operator.isEmpty()) {
            ExactlyOne exactlyOne = new ExactlyOne();
            
            if (Constants.TYPE_EXACTLYONE != type) {
                exactlyOne.addPolicyComponent(new All());
            }
            return exactlyOne;
        }
                
        List<PolicyComponent> childComponentsList = new ArrayList<PolicyComponent>();
        for (PolicyComponent policyComponent : operator.getPolicyComponents()) {
            if (policyComponent.getType() == Constants.TYPE_ASSERTION) {
                
                if (deep) {
                    policyComponent = ((Assertion) policyComponent).normalize();                    
                }
                
                if (policyComponent.getType() == Constants.TYPE_POLICY) {
                    childComponentsList.add(((Policy) policyComponent).getFirstPolicyComponent());
                    
                } else  {
                    ExactlyOne exactlyOne = new ExactlyOne();
                    All all = new All();
                    
                    all.addPolicyComponent(policyComponent);
                    exactlyOne.addPolicyComponent(all);
                    childComponentsList.add(exactlyOne);
                }
            } else if (policyComponent.getType() == Constants.TYPE_POLICY_REF) {
                String uri = ((PolicyReference) policyComponent).getURI();
                policyComponent = reg.lookup(uri);
                
                if (policyComponent == null) {
                    throw new RuntimeException(uri + " can't be resolved");
                }
                
                All all = new All();
                all.addPolicyComponents(((Policy) policyComponent).getPolicyComponents());
                childComponentsList.add(AbstractPolicyOperator.normalizeOperator(all, reg, deep));
         
            } else if (policyComponent.getType() == Constants.TYPE_POLICY) {
                All all = new All();
                all.addPolicyComponents(((Policy) policyComponent).getPolicyComponents());
                childComponentsList.add(AbstractPolicyOperator.normalizeOperator(all, reg, deep));
                
            } else {
                childComponentsList.add(AbstractPolicyOperator.normalizeOperator((PolicyOperator) policyComponent, reg, deep));
            }            
        }
        
        return computeResultantComponent(childComponentsList, type);
    }
    
    private static PolicyComponent computeResultantComponent(List<PolicyComponent> normalizedInnerComponets, 
                                                             short componentType) {
        
        ExactlyOne exactlyOne = new ExactlyOne();
        
        if (componentType == Constants.TYPE_EXACTLYONE) {            
            for (PolicyComponent comp : normalizedInnerComponets) {
                ExactlyOne innerExactlyOne = (ExactlyOne)comp;
                exactlyOne.addPolicyComponents(innerExactlyOne.getPolicyComponents());
            }
            
        } else if ((componentType == Constants.TYPE_POLICY) || (componentType == Constants.TYPE_ALL)) {
            // if the parent type is All then we have to get the cross product
            if (normalizedInnerComponets.size() > 1) {
                // then we have to get the cross product with each other to process all elements
                Iterator<PolicyComponent> iter = normalizedInnerComponets.iterator();
                // first get the first element
                exactlyOne = (ExactlyOne) iter.next();
                // if this is empty, this is an not admissible policy and total result is equivalent to that
                if (!exactlyOne.isEmpty()) {
                    ExactlyOne currentExactlyOne;

                    for (; iter.hasNext();) {
                        currentExactlyOne = (ExactlyOne) iter.next();
                        if (currentExactlyOne.isEmpty()) {
                            // if this is empty, this is an not admissible policy and total result is equivalent to that
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
                exactlyOne = (ExactlyOne) normalizedInnerComponets.get(0);
            }
        }
        
        return exactlyOne;
    }
    
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
}
