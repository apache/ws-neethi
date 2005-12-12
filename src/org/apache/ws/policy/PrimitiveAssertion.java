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
package org.apache.ws.policy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.util.PolicyRegistry;
import org.apache.ws.policy.util.PolicyUtil;

/**
 * PrimitiveAssertion wraps an assertion which is indivisible. Such assertion 
 * require domain specific knowledge for further processing. Hence this class
 * seperates that domain specific knowledge from generic framework.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PrimitiveAssertion implements Assertion {
    private Log log =
        LogFactory.getLog(this.getClass().getName());    
    
    private Assertion owner = null;
    private QName qname;
    private List terms = new ArrayList();
    private Hashtable attributes = new Hashtable();
    private boolean flag = false;
    private boolean isOptional = false;
    private String strValue = null;
    
    private Object value;
    
    public PrimitiveAssertion(QName qname) {
        this.qname = qname;
    }
    public PrimitiveAssertion(QName qname, Object value) {
        this.qname = qname;
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
        
    public QName getName() {
        return qname;
    }
       
    public Assertion intersect(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: PrimitveAssertion:intersect");
        
        Assertion normalizedMe = normalize(reg);
        Assertion target = assertion.normalize(reg);
        
        // Am not a primitive assertion anymore ..
        if (!(assertion instanceof PrimitiveAssertion)) {
            return normalizedMe.intersect(assertion, reg);
        } 
        
        // argument is not primitive type .. 
        if (!(target instanceof PrimitiveAssertion)) {
            return target.intersect(normalizedMe, reg);
        }
              
        PrimitiveAssertion arg = (PrimitiveAssertion) target;
        PrimitiveAssertion self = (PrimitiveAssertion) normalizedMe;
        
        if (!self.getName().equals(arg.getName())) {
            return new XorCompositeAssertion(); // no bahaviour is admisible            
        }
        
        if (self.getTerms().isEmpty() && arg.getTerms().isEmpty()) {
           AndCompositeAssertion assertion2 = new AndCompositeAssertion();
           assertion2.addTerm(self);
           assertion2.addTerm(arg);
           return assertion2;
        }
        
        if (self.getTerms().isEmpty() || arg.getTerms().isEmpty()) {
            return new XorCompositeAssertion(); // no 
        }
        
        List argChildTerms;
        if (arg.getTerms().get(0) instanceof Policy) {
            argChildTerms 
                = PolicyUtil.getPrimTermsList((Policy) arg.getTerms().get(0));
        } else {
            argChildTerms = arg.getTerms();
        }
        
        List selfChildTerms;
        if (self.getTerms().get(0) instanceof Policy) {
            selfChildTerms 
                = PolicyUtil.getPrimTermsList((Policy) self.getTerms().get(0));
        } else {
            selfChildTerms = self.getTerms();
        }
        
        if (PolicyUtil.matchByQName(argChildTerms, selfChildTerms)) {
                 
            AndCompositeAssertion andCompositeAssertion 
                = new AndCompositeAssertion();
            andCompositeAssertion.addTerm(arg);
            andCompositeAssertion.addTerm(self);
            return andCompositeAssertion;
        }
        
        return new XorCompositeAssertion();       
    }
    
    public Assertion intersect(Assertion assertion)
            throws UnsupportedOperationException {
        return intersect(assertion, null);
    }
    
    public Assertion merge(Assertion assertion, PolicyRegistry reg) {
        log.debug("Enter: PrimitveAssertion:merge");
        
        Assertion normalizedMe = normalize(reg);
        Assertion target = assertion.normalize(reg);
        
        if (!(normalizedMe instanceof PrimitiveAssertion)) {
            return normalizedMe.merge(assertion, reg);
        }
        
        if (!(target instanceof PrimitiveAssertion)) {
            return target.intersect(normalizedMe, reg);
        }
                
        AndCompositeAssertion andCompositeAssertion 
            = new AndCompositeAssertion();
        andCompositeAssertion.addTerm(target);
        andCompositeAssertion.addTerm(normalizedMe);
        return andCompositeAssertion;
    }
    
    public Assertion merge(Assertion assertion) {
        return merge(assertion, null);
    }
    
    public Assertion normalize() {
        return normalize(null);
    }
        
    public Assertion normalize(PolicyRegistry reg) {
        log.debug("Enter: PrimitveAssertion:normalize");
        
        if (isNormalized()) { return this; }
        
        if (isOptional()) {
            XorCompositeAssertion XOR = new XorCompositeAssertion();
            AndCompositeAssertion AND = new AndCompositeAssertion();
                        
            PrimitiveAssertion PRIM = getSelfWithoutTerms();
            PRIM.removeAttribute(new QName(PolicyConstants.WS_POLICY_NAMESPACE_URI, "Optional"));
            PRIM.setOptional(false);
            PRIM.setTerms(getTerms());
            
            AND.addTerm(PRIM);
            XOR.addTerm(AND);
            XOR.addTerm(new AndCompositeAssertion());
            
            return XOR.normalize(reg);
        }
        
        if (getTerms().isEmpty()) {
            PrimitiveAssertion PRIM = getSelfWithoutTerms();
            PRIM.setNormalized(true);
            return PRIM;
        }
        
        ArrayList policyTerms = new ArrayList();
        ArrayList nonPolicyTerms = new ArrayList();
        
        Iterator iterator = getTerms().iterator();
        
        while (iterator.hasNext()) {
            Assertion term = (Assertion) iterator.next();
            
            if (term instanceof Policy) {
                policyTerms.add(term);
                
            } else if (term instanceof PrimitiveAssertion) {
                nonPolicyTerms.add(term);
                
            } else {
                throw new RuntimeException();
                //TODO should I throw an exception ..
            }
        }
               
        if (policyTerms.isEmpty()) {
            PrimitiveAssertion PRIM = getSelfWithoutTerms();
            PRIM.setTerms(getTerms());
            PRIM.setNormalized(true);
            return PRIM;           
        }
        
        Policy policyTerm = PolicyUtil.getSinglePolicy(policyTerms, reg);
        CompositeAssertion xorTerm = (XorCompositeAssertion) 
                policyTerm.getTerms().get(0);
        
        List ANDs =  xorTerm.getTerms();
        
        if (ANDs.size() == 0) {
            return new XorCompositeAssertion();
        }
        
        if (ANDs.size() == 1) {
            ((AndCompositeAssertion) ANDs.get(0)).addTerms(nonPolicyTerms);
            PrimitiveAssertion PRIM = getSelfWithoutTerms();
            PRIM.addTerm(policyTerm);
            return PRIM;
        }
        
        
        Policy nPOLICY = new Policy();
        XorCompositeAssertion nXOR = new XorCompositeAssertion();
        nPOLICY.addTerm(nXOR);
        
        PrimitiveAssertion nPRIM;
        Iterator iterator2 = ANDs.iterator();
        
        ArrayList list;
        
        while (iterator2.hasNext()) {
            nPRIM = getSelfWithoutTerms();
            
            list = new ArrayList();
            list.addAll(((AndCompositeAssertion) iterator2.next()).getTerms());
            
            if (!nonPolicyTerms.isEmpty()) {
                list.addAll(nonPolicyTerms);                
            }
            nPRIM.addTerm(getSinglePolicy(list));
            AndCompositeAssertion AND = new AndCompositeAssertion();
            AND.addTerm(nPRIM);
            nXOR.addTerm(AND);            
        }
        nPOLICY.setNormalized(true);
        return nPOLICY;
    }
    
    private PrimitiveAssertion getSelfWithoutTerms() {
        PrimitiveAssertion self = new PrimitiveAssertion(getName());
        self.setAttributes(getAttributes());
        self.setStrValue(getStrValue());
        return self;
    }
        
    public boolean hasParent() {
        return owner != null;
    }
    
    public Assertion getParent() {
        return owner;
    }
    
    public void setAttributes(Hashtable attributes) {
        this.attributes = attributes;
    }
    
    public Hashtable getAttributes() {
        return attributes;
    }
    
    public void addAttribute(QName qname, Object value) {
        attributes.put(qname, value);
    }
    
    public String getAttribute(QName qname) {
        return (String) attributes.get(qname);
    }
    
    public void removeAttribute(QName qname) {
        attributes.remove(qname);
    }
    
    public void setParent(Assertion parent) {
        this.owner = parent;
    }
    
    public List getTerms() {
        return terms;
    }
    
    public void setTerms(List terms) {
        this.terms = terms;
    }
    
    public void addTerm(Object term) {
        terms.add(term);
    }
    
    public boolean isNormalized() {
        return flag;
    }
    
    public void setNormalized(boolean flag) {
        Iterator iterator = getTerms().iterator();
        while (iterator.hasNext()) {
            Assertion assertion = (Assertion) iterator.next();
            assertion.setNormalized(flag);
        }
        this.flag = flag;        
    }
    
    public String getStrValue() {
        return strValue;
    }
    
    public void setStrValue(String strValue) {
        this.strValue = strValue;        
    }
    
    public boolean isOptional() {
        return isOptional;
    }
    
    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }
    
    private Policy getSinglePolicy(List policyList, PolicyRegistry reg) {
        Policy result = null;
        Iterator iterator = policyList.iterator();
        while (iterator.hasNext()) {
            Policy policy = (Policy) iterator.next();
            result = (result == null) 
                ? policy 
                : (Policy) result.merge(policy, reg);
        }
        return result;
    }
    
    private Policy getSinglePolicy(List childTerms) {
        Policy policy = new Policy();
        XorCompositeAssertion xor = new XorCompositeAssertion();
        AndCompositeAssertion and = new AndCompositeAssertion();
        and.addTerms(childTerms);
        xor.addTerm(and);
        policy.addTerm(xor);
        return policy;        
    }
    
    private boolean isEmptyPolicy(Policy policy) {
        XorCompositeAssertion xor 
            = (XorCompositeAssertion) policy.getTerms().get(0);
        return xor.isEmpty();        
    }
    
    private List getTerms(Policy policy) {
        return ((AndCompositeAssertion) ((XorCompositeAssertion) policy.getTerms().get(0)).getTerms().get(0)).getTerms();
    }    
    
}
