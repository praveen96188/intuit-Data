package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class LiabilityCheckLine extends BaseLiabilityCheckLine implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public LiabilityCheckLine()
	{
		super();
	}
    
    // ----- QBDT Token overrides -----

    @Override
    public void setAmount(SpcfMoney pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);    
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if(!ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);    
    }

    @Override
    public void setCompanyPayrollItem(CompanyPayrollItem pCompanyPayrollItem) {
        if(!ObjectUtils.equals(getCompanyPayrollItem(), pCompanyPayrollItem)) {
            onUpdate();
        }
        super.setCompanyPayrollItem(pCompanyPayrollItem);    
    }

    @Override
    public void setLiabilityCheck(LiabilityCheck pLiabilityCheck) {
        if(!ObjectUtils.equals(getLiabilityCheck(), pLiabilityCheck)) {
            onUpdate();
        }
        super.setLiabilityCheck(pLiabilityCheck);    
    }

    public void onUpdate() {
        if(getLiabilityCheck() != null) {
            getLiabilityCheck().onUpdate();
        }
    }
}