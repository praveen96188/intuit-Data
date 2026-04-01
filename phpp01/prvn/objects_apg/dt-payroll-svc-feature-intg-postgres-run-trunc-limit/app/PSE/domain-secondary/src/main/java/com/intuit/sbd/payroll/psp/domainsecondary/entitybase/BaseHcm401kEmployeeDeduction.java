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

package com.intuit.sbd.payroll.psp.domainsecondary.entitybase;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kAmountType;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kDeductionContributor;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.sbd.payroll.psp.query.Hcm401kCompanyPolicyExpression;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

public class BaseHcm401kEmployeeDeduction extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // EmployeeId
    //
    private String mEmployeeId = null;

    public void setEmployeeId(String pEmployeeId)
    {
    	if (pEmployeeId != null && pEmployeeId.length() == 0)
    	{
    		pEmployeeId = null;
    	}

        mEmployeeId = pEmployeeId;
    }

    public String getEmployeeId()
    {
    	return mEmployeeId;
    }

    //
    // Amount
    //
    private double mAmount = 0.0d;

    public void setAmount(double pAmount)
    {
        mAmount = pAmount;
    }

    public double getAmount()
    {
    	return mAmount;
    }

    //
    // Hcm401kAmountType
    //
    private Hcm401kAmountType mHcm401kAmountType = Hcm401kAmountType.Dollar;

    public void setHcm401kAmountType(Hcm401kAmountType pHcm401kAmountType)
    {
        mHcm401kAmountType = pHcm401kAmountType;
    }

    public Hcm401kAmountType getHcm401kAmountType()
    {
    	return mHcm401kAmountType;
    }

    //
    // MaxAmount
    //
    private double mMaxAmount = 0.0d;

    public void setMaxAmount(double pMaxAmount)
    {
        mMaxAmount = pMaxAmount;
    }

    public double getMaxAmount()
    {
    	return mMaxAmount;
    }

    //
    // Hcm401kDeductionContributor
    //
    private Hcm401kDeductionContributor mHcm401kDeductionContributor = Hcm401kDeductionContributor.Employer;

    public void setHcm401kDeductionContributor(Hcm401kDeductionContributor pHcm401kDeductionContributor)
    {
        mHcm401kDeductionContributor = pHcm401kDeductionContributor;
    }

    public Hcm401kDeductionContributor getHcm401kDeductionContributor()
    {
    	return mHcm401kDeductionContributor;
    }

    //
    // Active
    //
    private boolean mActive = false;

    public void setActive(boolean pActive)
    {
        mActive = pActive;
    }

    public boolean getActive()
    {
    	return mActive;
    }
    //
    // Hcm401kCompanyPolicy
    //
    private Hcm401kCompanyPolicy mHcm401kCompanyPolicy = null;

    public void setHcm401kCompanyPolicy(Hcm401kCompanyPolicy pHcm401kCompanyPolicy)
    {
    	mHcm401kCompanyPolicy = pHcm401kCompanyPolicy;
    }

    public Hcm401kCompanyPolicy getHcm401kCompanyPolicy()
    {
    	return mHcm401kCompanyPolicy;
    }



    // PSP query support
    public static final ScalarProperty<Hcm401kEmployeeDeduction, String> EmployeeId() {return new ScalarProperty<Hcm401kEmployeeDeduction, String>(null, "EmployeeId");};
    public static final ScalarProperty<Hcm401kEmployeeDeduction, Double> Amount() {return new ScalarProperty<Hcm401kEmployeeDeduction, Double>(null, "Amount");};
    public static final ScalarProperty<Hcm401kEmployeeDeduction, Hcm401kAmountType> Hcm401kAmountType() {return new ScalarProperty<Hcm401kEmployeeDeduction, Hcm401kAmountType>(null, "Hcm401kAmountType");};
    public static final ScalarProperty<Hcm401kEmployeeDeduction, Double> MaxAmount() {return new ScalarProperty<Hcm401kEmployeeDeduction, Double>(null, "MaxAmount");};
    public static final ScalarProperty<Hcm401kEmployeeDeduction, Hcm401kDeductionContributor> Hcm401kDeductionContributor() {return new ScalarProperty<Hcm401kEmployeeDeduction, Hcm401kDeductionContributor>(null, "Hcm401kDeductionContributor");};
    public static final ScalarProperty<Hcm401kEmployeeDeduction, Boolean> Active() {return new ScalarProperty<Hcm401kEmployeeDeduction, Boolean>(null, "Active");};
    public static final Hcm401kCompanyPolicyExpression<Hcm401kEmployeeDeduction> Hcm401kCompanyPolicy() {return new Hcm401kCompanyPolicyExpression<Hcm401kEmployeeDeduction>(null, "Hcm401kCompanyPolicy");};
}