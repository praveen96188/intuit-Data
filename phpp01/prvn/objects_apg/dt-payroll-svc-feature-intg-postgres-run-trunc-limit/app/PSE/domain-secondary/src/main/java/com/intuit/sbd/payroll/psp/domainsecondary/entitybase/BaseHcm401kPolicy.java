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
import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.DeductionItemProvider;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kPolicy;
import com.intuit.sbd.payroll.psp.query.Hcm401kCompanyPolicyDomainEntitySetProperty;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

import java.util.Set;

public class BaseHcm401kPolicy extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // DeductionItemPolicy
    //
    private DeductionItemPolicy mDeductionItemPolicy = DeductionItemPolicy.TppoCus401K;

    public void setDeductionItemPolicy(DeductionItemPolicy pDeductionItemPolicy)
    {
        mDeductionItemPolicy = pDeductionItemPolicy;
    }

    public DeductionItemPolicy getDeductionItemPolicy()
    {
    	return mDeductionItemPolicy;
    }

    //
    // Description
    //
    private String mDescription = null;

    public void setDescription(String pDescription)
    {
    	if (pDescription != null && pDescription.length() == 0)
    	{
    		pDescription = null;
    	}

        mDescription = pDescription;
    }

    public String getDescription()
    {
    	return mDescription;
    }

    //
    // DeductionItemProvider
    //
    private DeductionItemProvider mDeductionItemProvider = DeductionItemProvider.Guideline;

    public void setDeductionItemProvider(DeductionItemProvider pDeductionItemProvider)
    {
        mDeductionItemProvider = pDeductionItemProvider;
    }

    public DeductionItemProvider getDeductionItemProvider()
    {
    	return mDeductionItemProvider;
    }
    //
    // Hcm401kCompanyPolicy
    //
    protected DomainEntitySet<Hcm401kCompanyPolicy> mHcm401kCompanyPolicySet = new DomainEntitySet<Hcm401kCompanyPolicy>();

    @SuppressWarnings("unchecked")
    public DomainEntitySet<Hcm401kCompanyPolicy> getHcm401kCompanyPolicyCollection()
    {
    	return mHcm401kCompanyPolicySet;
    }

    @SuppressWarnings("unchecked")
    public void addHcm401kCompanyPolicy(Hcm401kCompanyPolicy pHcm401kCompanyPolicy)
    {
    	getHcm401kCompanyPolicyCollection().add(pHcm401kCompanyPolicy);
    }

    @SuppressWarnings("unchecked")
    public void removeHcm401kCompanyPolicy(Hcm401kCompanyPolicy pHcm401kCompanyPolicy)
    {
    	getHcm401kCompanyPolicyCollection().remove(pHcm401kCompanyPolicy);
    }


    /**
     * Package-protected setter for Hcm401kCompanyPolicy.
     * Only for O/R-mapper use.
     * @param pHcm401kCompanyPolicy Hcm401kCompanyPolicy.
    */
    @SuppressWarnings("unchecked")
    void setHcm401kCompanyPolicySet(Set<Hcm401kCompanyPolicy> pHcm401kCompanyPolicy)
    {
    	mHcm401kCompanyPolicySet =  new DomainEntitySet<Hcm401kCompanyPolicy>(pHcm401kCompanyPolicy);
    }

    /**
     * Package-Protected getter for Hcm401kCompanyPolicy.
     * Only for internal and O/R-mapper use.
     * @return Hcm401kCompanyPolicy.
    */
    @SuppressWarnings("unchecked")
    Set<Hcm401kCompanyPolicy> getHcm401kCompanyPolicySet()
    {
    	return mHcm401kCompanyPolicySet.toNative();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void reinitializeHcm401kCompanyPolicySet() {
        mHcm401kCompanyPolicySet = new DomainEntitySet<Hcm401kCompanyPolicy>();
    }



    // PSP query support
    public static final ScalarProperty<Hcm401kPolicy, DeductionItemPolicy> DeductionItemPolicy() {return new ScalarProperty<Hcm401kPolicy, DeductionItemPolicy>(null, "DeductionItemPolicy");};
    public static final ScalarProperty<Hcm401kPolicy, String> Description() {return new ScalarProperty<Hcm401kPolicy, String>(null, "Description");};
    public static final ScalarProperty<Hcm401kPolicy, DeductionItemProvider> DeductionItemProvider() {return new ScalarProperty<Hcm401kPolicy, DeductionItemProvider>(null, "DeductionItemProvider");};
    public static final Hcm401kCompanyPolicyDomainEntitySetProperty<Hcm401kPolicy, Hcm401kCompanyPolicy> Hcm401kCompanyPolicySet() {return new Hcm401kCompanyPolicyDomainEntitySetProperty<Hcm401kPolicy, Hcm401kCompanyPolicy>(null, "Hcm401kCompanyPolicySet", "Hcm401kPolicy");};
}