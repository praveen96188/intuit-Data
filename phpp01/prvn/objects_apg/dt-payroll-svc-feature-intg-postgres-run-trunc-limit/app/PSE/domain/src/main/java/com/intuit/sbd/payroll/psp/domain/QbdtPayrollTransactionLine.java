package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class QbdtPayrollTransactionLine extends BaseQbdtPayrollTransactionLine implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public QbdtPayrollTransactionLine()
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
    public void setWageBaseAmount(SpcfMoney pWageBaseAmount) {
        if(!ObjectUtils.equals(getWageBaseAmount(), pWageBaseAmount)) {
            onUpdate();
        }
        super.setWageBaseAmount(pWageBaseAmount);    
    }

    @Override
    public void setTaxableWageAmount(SpcfMoney pTaxableWageAmount) {
        if(!ObjectUtils.equals(getTaxableWageAmount(), pTaxableWageAmount)) {
            onUpdate();
        }
        super.setTaxableWageAmount(pTaxableWageAmount);    
    }

    @Override
    public void setCompanyPayrollItem(CompanyPayrollItem pCompanyPayrollItem) {
        if(!ObjectUtils.equals(getCompanyPayrollItem(), pCompanyPayrollItem)) {
            onUpdate();
        }
        super.setCompanyPayrollItem(pCompanyPayrollItem);    
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
    }

    @Override
    public void setQbdtPayrollTransaction(QbdtPayrollTransaction pQbdtPayrollTransaction) {
        if(!ObjectUtils.equals(getQbdtPayrollTransaction(), pQbdtPayrollTransaction)) {
            onUpdate();
        }
        super.setQbdtPayrollTransaction(pQbdtPayrollTransaction);    
    }

    public void onUpdate() {
        if(getQbdtPayrollTransaction() != null) {
            getQbdtPayrollTransaction().onUpdate();
        }
    }
}