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

 public enum Hcm401kAmountType {
    Dollar, 

    Percentage;

    public boolean in(Hcm401kAmountType... pHcm401kAmountType) {
        for (Hcm401kAmountType curHcm401kAmountType : pHcm401kAmountType) {
            if (this == curHcm401kAmountType) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn(Hcm401kAmountType... pHcm401kAmountType) {
        return !in(pHcm401kAmountType);
    }

 }