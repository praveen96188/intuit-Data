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

 public enum Hcm401kDeductionContributor {
    Employer, 

    Employee;

    public boolean in(Hcm401kDeductionContributor... pHcm401kDeductionContributor) {
        for (Hcm401kDeductionContributor curHcm401kDeductionContributor : pHcm401kDeductionContributor) {
            if (this == curHcm401kDeductionContributor) {
                return true;
            }
        }
        return false;
    }

    public boolean notIn(Hcm401kDeductionContributor... pHcm401kDeductionContributor) {
        return !in(pHcm401kDeductionContributor);
    }

 }