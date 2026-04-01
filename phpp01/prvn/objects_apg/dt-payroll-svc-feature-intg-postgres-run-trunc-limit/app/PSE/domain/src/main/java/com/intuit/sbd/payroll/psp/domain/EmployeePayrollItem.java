package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class EmployeePayrollItem extends BaseEmployeePayrollItem implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public EmployeePayrollItem()
	{
		super();
	}

    // ----- QBDT Token overrides -----
    @Override
    public void setAmount(double pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);
    }

    @Override
    public void setItemLimit(double pLimit) {
        if(!ObjectUtils.equals(getItemLimit(), pLimit)) {
            onUpdate();
        }
        super.setItemLimit(pLimit);
    }

    @Override
    public void setType(PaylineType pType) {
        if(!ObjectUtils.equals(getType(), pType)) {
            onUpdate();
        }
        super.setType(pType);
    }

    @Override
    public void setAmountType(QbdtNumericType pAmountType) {
        if(!ObjectUtils.equals(getAmountType(), pAmountType)) {
            onUpdate();
        }
        super.setAmountType(pAmountType);
    }

    @Override
    public void setLimitType(QbdtNumericType pLimitType) {
        if(!ObjectUtils.equals(getLimitType(), pLimitType)) {
            onUpdate();
        }
        super.setLimitType(pLimitType);
    }

    @Override
    public void setItemOrder(int pItemOrder) {
        if(!ObjectUtils.equals(getItemOrder(), pItemOrder)) {
            onUpdate();
        }
        super.setItemOrder(pItemOrder);
    }

    @Override
    public void setCompanyPayrollItem(CompanyPayrollItem pCompanyPayrollItem) {
        if(!ObjectUtils.equals(getCompanyPayrollItem(), pCompanyPayrollItem)) {
            onUpdate();
        }
        super.setCompanyPayrollItem(pCompanyPayrollItem);
    }

    @Override
    public void setEmployee(Employee pEmployee) {
        if(!ObjectUtils.equals(getEmployee(), pEmployee)) {
            onUpdate();
        }
        super.setEmployee(pEmployee);
    }

    public void onUpdate() {
        if(getEmployee() != null) {
            getEmployee().onUpdate();
        }
    }
}
