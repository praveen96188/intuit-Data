/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	1.0
 *
 * --------------------------------------------------------------------------
 */
 package com.intuit.sbd.payroll.psp.domainsecondary;

import com.intuit.sbd.payroll.psp.domainsecondary.util.constants.Guideline401kConstants;
import edu.emory.mathcs.backport.java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

 public enum DeductionItemPolicy {
    TppoCus401K, 

    TppoCusRoth401K, 

    TppoCus401KCatchup, 

    TdepCusLoanRepayment;

     private static Logger log = LoggerFactory.getLogger(DeductionItemPolicy.class);

     private static Map<String, String> deductionItemPolicyToStatutoryPolicyMap;
     private static Map<String, DeductionItemPolicy> statutoryPolicyToDeductionItemPolicyMap;

     static{
         Map<String, String> tempMap1 =  new HashMap<>();
         tempMap1.put(TppoCus401K.name(), Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
         tempMap1.put(TppoCusRoth401K.name(), Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_ROTH401K);
         tempMap1.put(TdepCusLoanRepayment.name(), Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN);
         deductionItemPolicyToStatutoryPolicyMap = Collections.unmodifiableMap(tempMap1);

         Map<String, DeductionItemPolicy> tempMap2 =  new HashMap<>();
         tempMap2.put(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K, TppoCus401K);
         tempMap2.put(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_ROTH401K, TppoCusRoth401K);
         tempMap2.put(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN, TdepCusLoanRepayment);
         statutoryPolicyToDeductionItemPolicyMap = Collections.unmodifiableMap(tempMap2);
     }

     public boolean in(DeductionItemPolicy... pDeductionItemPolicy) {
         for (DeductionItemPolicy curDeductionItemPolicy : pDeductionItemPolicy) {
             if (this == curDeductionItemPolicy) {
                 return true;
             }
         }
         return false;
     }

     public boolean notIn(DeductionItemPolicy... pDeductionItemPolicy) {
         return !in(pDeductionItemPolicy);
     }

     public boolean isDeduction(){
         return this.in(DeductionItemPolicy.TdepCusLoanRepayment);
     }

     public boolean isPension(){
         return this.in(DeductionItemPolicy.TppoCus401KCatchup,
                 DeductionItemPolicy.TppoCus401K, DeductionItemPolicy.TppoCusRoth401K);
     }

     public static DeductionItemPolicy getDeductionItemPolicyByName(String statutoryPolicyName) {
         try{
             return statutoryPolicyToDeductionItemPolicyMap.get(statutoryPolicyName);
         } catch(Exception e){
             log.error(String.format("Error getting deductionItemPolicyName=%s", statutoryPolicyName));
             return null;
         }
     }

     public static String getStatutoryPolicyName(String deductionItemPolicyName){
         try {
             return deductionItemPolicyToStatutoryPolicyMap.get(deductionItemPolicyName);
         } catch (Exception e) {
             log.error(String.format("Error getting StatutoryItemPolicyName=%s", deductionItemPolicyName));
             return "";
         }
     }

 }