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
package org.apache.policy.model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.policy.parser.WSPolicyParser;
import org.apache.policy.util.PolicyRegistry;
import org.apache.policy.util.WSPolicyUtil;

/**
 * PrimitiveAssertion wraps an assertion which is indivisible. Such assertion 
 * require domain specific knowledge for further processing. Hence this class
 * seperates that domain specific knowledge from generic framework.
 * 
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PrimitiveAssertion implements Assertion {
    private Assertion owner = null;
    private QName qname;
    private List terms = new ArrayList();
    private Hashtable attributes = new Hashtable();
    private boolean flag = false;
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
        WSPolicyParser.getInstance().printAssertion(0, arg, new PrintWriter(System.out, true));
        WSPolicyParser.getInstance().printAssertion(0, self, new PrintWriter(System.out, true));
        
        
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
                = WSPolicyUtil.getPrimTermsList((Policy) arg.getTerms().get(0));
        } else {
            argChildTerms = arg.getTerms();
        }
        
        List selfChildTerms;
        if (self.getTerms().get(0) instanceof Policy) {
            selfChildTerms 
                = WSPolicyUtil.getPrimTermsList((Policy) self.getTerms().get(0));
        } else {
            selfChildTerms = self.getTerms();
        }
        
        if (WSPolicyUtil.matchByQName(argChildTerms, selfChildTerms)) {
                 
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
        if (getTerms().isEmpty()) {
            PrimitiveAssertion primitiveAssertion = getSelfWithoutTerms();
            primitiveAssertion.setNormalized(true);
            return primitiveAssertion;
        }
        
        ArrayList policyTerms = new ArrayList();
        ArrayList childNonPolicyTerms = new ArrayList();
        
        Iterator iterator = getTerms().iterator();
        
        while (iterator.hasNext()) {
            Assertion term = (Assertion) iterator.next();
            if (!(term.isNormalized())) {
                term = term.normalize();
            }
            
            if (term instanceof Policy) {
                policyTerms.add(term);
                
            } else if (term instanceof PrimitiveAssertion) {
                childNonPolicyTerms.add(term);
                
            } else {
                //TODO should I throw an exception ..
            }
        }
               
        if (policyTerms.isEmpty()) {
            PrimitiveAssertion primitiveAssertion = getSelfWithoutTerms();
            primitiveAssertion.setTerms(getTerms());
            primitiveAssertion.setNormalized(true);
            return primitiveAssertion;           
        }
        
        Policy policyTerm = WSPolicyUtil.getSinglePolicy(policyTerms, reg);
        CompositeAssertion xorTerm = (XorCompositeAssertion) 
                policyTerm.getTerms().get(0);
        
        Iterator iterator2 = xorTerm.getTerms().iterator();
 //       AndCompositeAssertion andTerm = (AndCompositeAssertion) iterator2.next();
                
        if (!(iterator2.hasNext())) { //policy with no alternatives
            // two scenarios ..
            
            /* no leaves */
            if (childNonPolicyTerms.isEmpty()) {
                PrimitiveAssertion primTerm = getSelfWithoutTerms();
                primTerm.addTerm(policyTerm);
                return primTerm;                
            }
                        
            /* (2) some leaves*/           
            ArrayList allTerms = new ArrayList();
            allTerms.addAll(childNonPolicyTerms);
            allTerms.addAll(((AndCompositeAssertion) iterator2.next()).getTerms());
            PrimitiveAssertion primTerm = getSelfWithoutTerms();
            primTerm.addTerm(getSinglePolicy(allTerms));
            return primTerm;        
        } 
        
        /* Policy with many terms */
        Policy endPolicyTerm = new Policy();
        XorCompositeAssertion endXorTerm = new XorCompositeAssertion();
        endPolicyTerm.addTerm(endXorTerm);        
        
        ArrayList endAndTerms = new ArrayList();
        AndCompositeAssertion anEndAndTerm = new AndCompositeAssertion();
        PrimitiveAssertion self;
        
        ArrayList termsForAnEndAnd = new ArrayList();
        self = getSelfWithoutTerms();
        
        termsForAnEndAnd.addAll(childNonPolicyTerms);
        termsForAnEndAnd.addAll(((AndCompositeAssertion) iterator2.next()).getTerms());
        self.addTerm(WSPolicyUtil.getPolicy(termsForAnEndAnd));
        anEndAndTerm.addTerm(self);
        endAndTerms.add(anEndAndTerm);
               
        while (iterator2.hasNext()) {
            anEndAndTerm = new AndCompositeAssertion();     
            self = getSelfWithoutTerms();
                        
            termsForAnEndAnd.clear();
            termsForAnEndAnd.addAll(childNonPolicyTerms);
            termsForAnEndAnd.addAll(((AndCompositeAssertion) iterator2.next()).getTerms());
            self.addTerm(WSPolicyUtil.getPolicy(termsForAnEndAnd));
            
            anEndAndTerm.addTerm(self);
            endAndTerms.add(anEndAndTerm);            
        }
        endXorTerm.addTerms(endAndTerms);
        return endPolicyTerm;
    }
    
    private PrimitiveAssertion getSelfWithoutTerms() {
        PrimitiveAssertion self = new PrimitiveAssertion(getName());
        self.setAttributes(getAttributes());
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
