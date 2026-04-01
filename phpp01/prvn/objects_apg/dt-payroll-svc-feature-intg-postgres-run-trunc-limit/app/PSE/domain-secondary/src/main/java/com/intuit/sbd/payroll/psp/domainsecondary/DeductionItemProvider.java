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

 public enum DeductionItemProvider {
    Guideline;

    public boolean in(DeductionItemProvider... pDeductionItemProvider) {
        for (DeductionItemProvider curDeductionItemProvider : pDeductionItemProvider) {
            if (this == curDeductionItemProvider) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn(DeductionItemProvider... pDeductionItemProvider) {
        return !in(pDeductionItemProvider);
    }

    public static DeductionItemProvider getDeductionProvider(String deductionProvider) {
        DeductionItemProvider deductionProviderEnum;
        try{
         deductionProviderEnum = DeductionItemProvider.valueOf(deductionProvider);
        }catch (Exception e){
         throw new DeductionProviderNotFoundException("Invalid Deduction provider sent.", e);
        }
        return deductionProviderEnum;
    }

 }