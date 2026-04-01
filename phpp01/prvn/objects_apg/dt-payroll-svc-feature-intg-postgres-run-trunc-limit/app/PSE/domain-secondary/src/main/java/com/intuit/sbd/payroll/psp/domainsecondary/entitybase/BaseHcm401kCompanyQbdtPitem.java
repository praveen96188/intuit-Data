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

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kDeductionContributor;
import com.intuit.sbd.payroll.psp.query.Hcm401kCompanyPolicyExpression;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

public class BaseHcm401kCompanyQbdtPitem extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // QbdtPitemId
    //
    private String mQbdtPitemId = null;

    public void setQbdtPitemId(String pQbdtPitemId)
    {
    	if (pQbdtPitemId != null && pQbdtPitemId.length() == 0)
    	{
    		pQbdtPitemId = null;
    	}

        mQbdtPitemId = pQbdtPitemId;
    }

    public String getQbdtPitemId()
    {
    	return mQbdtPitemId;
    }

    //
    // CompanyPayrollItemId
    //
    private String mCompanyPayrollItemId = null;

    public void setCompanyPayrollItemId(String pCompanyPayrollItemId)
    {
    	if (pCompanyPayrollItemId != null && pCompanyPayrollItemId.length() == 0)
    	{
    		pCompanyPayrollItemId = null;
    	}

        mCompanyPayrollItemId = pCompanyPayrollItemId;
    }

    public String getCompanyPayrollItemId()
    {
    	return mCompanyPayrollItemId;
    }

    //
    // Hcm401kContributor
    //
    private Hcm401kDeductionContributor mHcm401kContributor = Hcm401kDeductionContributor.Employer;

    public void setHcm401kContributor(Hcm401kDeductionContributor pHcm401kContributor)
    {
        mHcm401kContributor = pHcm401kContributor;
    }

    public Hcm401kDeductionContributor getHcm401kContributor()
    {
    	return mHcm401kContributor;
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
    public static final ScalarProperty<Hcm401kCompanyQbdtPitem, String> QbdtPitemId() {return new ScalarProperty<Hcm401kCompanyQbdtPitem, String>(null, "QbdtPitemId");};
    public static final ScalarProperty<Hcm401kCompanyQbdtPitem, String> CompanyPayrollItemId() {return new ScalarProperty<Hcm401kCompanyQbdtPitem, String>(null, "CompanyPayrollItemId");};
    public static final ScalarProperty<Hcm401kCompanyQbdtPitem, Hcm401kDeductionContributor> Hcm401kContributor() {return new ScalarProperty<Hcm401kCompanyQbdtPitem, Hcm401kDeductionContributor>(null, "Hcm401kContributor");};
    public static final Hcm401kCompanyPolicyExpression<Hcm401kCompanyQbdtPitem> Hcm401kCompanyPolicy() {return new Hcm401kCompanyPolicyExpression<Hcm401kCompanyQbdtPitem>(null, "Hcm401kCompanyPolicy");};
}