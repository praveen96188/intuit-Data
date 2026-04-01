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

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kPolicy;
import com.intuit.sbd.payroll.psp.query.Hcm401kCompanyQbdtPitemDomainEntitySetProperty;
import com.intuit.sbd.payroll.psp.query.Hcm401kEmployeeDeductionDomainEntitySetProperty;
import com.intuit.sbd.payroll.psp.query.Hcm401kPolicyExpression;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

import java.util.Set;

public class BaseHcm401kCompanyPolicy extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // CompanyId
    //
    private String mCompanyId = null;

    public void setCompanyId(String pCompanyId)
    {
    	if (pCompanyId != null && pCompanyId.length() == 0)
    	{
    		pCompanyId = null;
    	}

        mCompanyId = pCompanyId;
    }

    public String getCompanyId()
    {
    	return mCompanyId;
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
    // Hcm401kPolicy
    //
    private Hcm401kPolicy mHcm401kPolicy = null;

    public void setHcm401kPolicy(Hcm401kPolicy pHcm401kPolicy)
    {
    	mHcm401kPolicy = pHcm401kPolicy;
    }

    public Hcm401kPolicy getHcm401kPolicy()
    {
    	return mHcm401kPolicy;
    }
    //
    // Hcm401kCompanyQbdtPitem
    //
    protected DomainEntitySet<Hcm401kCompanyQbdtPitem> mHcm401kCompanyQbdtPitemSet = new DomainEntitySet<Hcm401kCompanyQbdtPitem>();

    @SuppressWarnings("unchecked")
    public DomainEntitySet<Hcm401kCompanyQbdtPitem> getHcm401kCompanyQbdtPitemCollection()
    {
    	return mHcm401kCompanyQbdtPitemSet;
    }

    @SuppressWarnings("unchecked")
    public void addHcm401kCompanyQbdtPitem(Hcm401kCompanyQbdtPitem pHcm401kCompanyQbdtPitem)
    {
    	getHcm401kCompanyQbdtPitemCollection().add(pHcm401kCompanyQbdtPitem);
    }

    @SuppressWarnings("unchecked")
    public void removeHcm401kCompanyQbdtPitem(Hcm401kCompanyQbdtPitem pHcm401kCompanyQbdtPitem)
    {
    	getHcm401kCompanyQbdtPitemCollection().remove(pHcm401kCompanyQbdtPitem);
    }


    /**
     * Package-protected setter for Hcm401kCompanyQbdtPitem.
     * Only for O/R-mapper use.
     * @param pHcm401kCompanyQbdtPitem Hcm401kCompanyQbdtPitem.
    */
    @SuppressWarnings("unchecked")
    void setHcm401kCompanyQbdtPitemSet(Set<Hcm401kCompanyQbdtPitem> pHcm401kCompanyQbdtPitem)
    {
    	mHcm401kCompanyQbdtPitemSet =  new DomainEntitySet<Hcm401kCompanyQbdtPitem>(pHcm401kCompanyQbdtPitem);
    }

    /**
     * Package-Protected getter for Hcm401kCompanyQbdtPitem.
     * Only for internal and O/R-mapper use.
     * @return Hcm401kCompanyQbdtPitem.
    */
    @SuppressWarnings("unchecked")
    Set<Hcm401kCompanyQbdtPitem> getHcm401kCompanyQbdtPitemSet()
    {
    	return mHcm401kCompanyQbdtPitemSet.toNative();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void reinitializeHcm401kCompanyQbdtPitemSet() {
        mHcm401kCompanyQbdtPitemSet = new DomainEntitySet<Hcm401kCompanyQbdtPitem>();
    }

    //
    // Hcm401kEmployeeDeduction
    //
    protected DomainEntitySet<Hcm401kEmployeeDeduction> mHcm401kEmployeeDeductionSet = new DomainEntitySet<Hcm401kEmployeeDeduction>();

    @SuppressWarnings("unchecked")
    public DomainEntitySet<Hcm401kEmployeeDeduction> getHcm401kEmployeeDeductionCollection()
    {
    	return mHcm401kEmployeeDeductionSet;
    }

    @SuppressWarnings("unchecked")
    public void addHcm401kEmployeeDeduction(Hcm401kEmployeeDeduction pHcm401kEmployeeDeduction)
    {
    	getHcm401kEmployeeDeductionCollection().add(pHcm401kEmployeeDeduction);
    }

    @SuppressWarnings("unchecked")
    public void removeHcm401kEmployeeDeduction(Hcm401kEmployeeDeduction pHcm401kEmployeeDeduction)
    {
    	getHcm401kEmployeeDeductionCollection().remove(pHcm401kEmployeeDeduction);
    }


    /**
     * Package-protected setter for Hcm401kEmployeeDeduction.
     * Only for O/R-mapper use.
     * @param pHcm401kEmployeeDeduction Hcm401kEmployeeDeduction.
    */
    @SuppressWarnings("unchecked")
    void setHcm401kEmployeeDeductionSet(Set<Hcm401kEmployeeDeduction> pHcm401kEmployeeDeduction)
    {
    	mHcm401kEmployeeDeductionSet =  new DomainEntitySet<Hcm401kEmployeeDeduction>(pHcm401kEmployeeDeduction);
    }

    /**
     * Package-Protected getter for Hcm401kEmployeeDeduction.
     * Only for internal and O/R-mapper use.
     * @return Hcm401kEmployeeDeduction.
    */
    @SuppressWarnings("unchecked")
    Set<Hcm401kEmployeeDeduction> getHcm401kEmployeeDeductionSet()
    {
    	return mHcm401kEmployeeDeductionSet.toNative();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void reinitializeHcm401kEmployeeDeductionSet() {
        mHcm401kEmployeeDeductionSet = new DomainEntitySet<Hcm401kEmployeeDeduction>();
    }



    // PSP query support
    public static final ScalarProperty<Hcm401kCompanyPolicy, String> CompanyId() {return new ScalarProperty<Hcm401kCompanyPolicy, String>(null, "CompanyId");};
    public static final ScalarProperty<Hcm401kCompanyPolicy, Boolean> Active() {return new ScalarProperty<Hcm401kCompanyPolicy, Boolean>(null, "Active");};
    public static final Hcm401kPolicyExpression<Hcm401kCompanyPolicy> Hcm401kPolicy() {return new Hcm401kPolicyExpression<Hcm401kCompanyPolicy>(null, "Hcm401kPolicy");};
    public static final Hcm401kCompanyQbdtPitemDomainEntitySetProperty<Hcm401kCompanyPolicy, Hcm401kCompanyQbdtPitem> Hcm401kCompanyQbdtPitemSet() {return new Hcm401kCompanyQbdtPitemDomainEntitySetProperty<Hcm401kCompanyPolicy, Hcm401kCompanyQbdtPitem>(null, "Hcm401kCompanyQbdtPitemSet", "Hcm401kCompanyPolicy");};
    public static final Hcm401kEmployeeDeductionDomainEntitySetProperty<Hcm401kCompanyPolicy, Hcm401kEmployeeDeduction> Hcm401kEmployeeDeductionSet() {return new Hcm401kEmployeeDeductionDomainEntitySetProperty<Hcm401kCompanyPolicy, Hcm401kEmployeeDeduction>(null, "Hcm401kEmployeeDeductionSet", "Hcm401kCompanyPolicy");};
}