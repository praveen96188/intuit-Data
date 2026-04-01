package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class QbdtPaylineInfo extends BaseQbdtPaylineInfo implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public QbdtPaylineInfo()
	{
		super();
	}

    @Override
    public void setCompensation(Compensation pCompensation) {
        if(!ObjectUtils.equals(getCompensation(), pCompensation)) {
            onUpdate();
        }
        super.setCompensation(pCompensation);    
    }

    @Override
    public void setEmployerContribution(EmployerContribution pEmployerContribution) {
        if(!ObjectUtils.equals(getEmployerContribution(), pEmployerContribution)) {
            onUpdate();
        }
        super.setEmployerContribution(pEmployerContribution);    
    }

    @Override
    public void setQuantityType(QbdtNumericType pQuantityType) {
        if(!ObjectUtils.equals(getQuantityType(), pQuantityType)) {
            onUpdate();
        }
        super.setQuantityType(pQuantityType);    
    }

    @Override
    public void setRateType(QbdtNumericType pRateType) {
        if(!ObjectUtils.equals(getRateType(), pRateType)) {
            onUpdate();
        }
        super.setRateType(pRateType);    
    }

    @Override
    public void setDeduction(Deduction pDeduction) {
        if(!ObjectUtils.equals(getDeduction(), pDeduction)) {
            onUpdate();
        }
        super.setDeduction(pDeduction);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);    
    }

    @Override
    public void setExpenseByJob(boolean pExpenseByJob) {
        if(!ObjectUtils.equals(getExpenseByJob(), pExpenseByJob)) {
            onUpdate();
        }
        super.setExpenseByJob(pExpenseByJob);    
    }

    @Override
    public void setQuantity(double pQuantity) {
        if(!ObjectUtils.equals(getQuantity(), pQuantity)) {
            onUpdate();
        }
        super.setQuantity(pQuantity);    
    }

    @Override
    public void setWcCode(String pWcCode) {
        if(!ObjectUtils.equals(getWcCode(), pWcCode)) {
            onUpdate();
        }
        super.setWcCode(pWcCode);    
    }

    @Override
    public void setItem(String pItem) {
        if(!ObjectUtils.equals(getItem(), pItem)) {
            onUpdate();
        }
        super.setItem(pItem);    
    }

    @Override
    public void setJob(String pJob) {
        if(!ObjectUtils.equals(getJob(), pJob)) {
            onUpdate();
        }
        super.setJob(pJob);    
    }

    @Override
    public void setTrackingClass(String pTrackingClass) {
        if(!ObjectUtils.equals(getTrackingClass(), pTrackingClass)) {
            onUpdate();
        }
        super.setTrackingClass(pTrackingClass);    
    }

    @Override
    public void setRate(double pRate) {
        if(!ObjectUtils.equals(getRate(), pRate)) {
            onUpdate();
        }
        super.setRate(pRate);    
    }

    public void onUpdate() {
        if (getCompensation() != null) {
            getCompensation().onUpdate();
        } else if (getDeduction() != null) {
            getDeduction().onUpdate();
        } else if (getEmployerContribution() != null) {
            getEmployerContribution().onUpdate();
        }
    }
}