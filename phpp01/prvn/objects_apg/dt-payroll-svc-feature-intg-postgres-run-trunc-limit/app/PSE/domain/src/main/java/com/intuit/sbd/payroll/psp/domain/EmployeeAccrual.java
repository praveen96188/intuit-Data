package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class EmployeeAccrual extends BaseEmployeeAccrual implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public EmployeeAccrual()
	{
		super();
	}
    
    // ----- QBDT Token overrides -----
    @Override
    public void setAccrualPeriod(AccrualPeriod pAccrualPeriod) {
        if(!ObjectUtils.equals(getAccrualPeriod(), pAccrualPeriod)) {
            onUpdate();
        }
        super.setAccrualPeriod(pAccrualPeriod);
    }

    @Override
    public void setHoursPerPeriod(double pHoursPerPeriod) {
        if(!ObjectUtils.equals(getHoursPerPeriod(), pHoursPerPeriod)) {
            onUpdate();
        }
        super.setHoursPerPeriod(pHoursPerPeriod);
    }

    @Override
    public void setHours(double pHours) {
        if(!ObjectUtils.equals(getHours(), pHours)) {
            onUpdate();
        }
        super.setHours(pHours);
    }

    @Override
    public void setMaxHours(double pMaxHours) {
        if(!ObjectUtils.equals(getMaxHours(), pMaxHours)) {
            onUpdate();
        }
        super.setMaxHours(pMaxHours);
    }

    @Override
    public void setNewYearReset(boolean pNewYearReset) {
        if(!ObjectUtils.equals(getNewYearReset(), pNewYearReset)) {
            onUpdate();
        }
        super.setNewYearReset(pNewYearReset);
    }

    @Override
    public void setAccrualType(AccrualType pAccrualType) {
        if(!ObjectUtils.equals(getAccrualType(), pAccrualType)) {
            onUpdate();
        }
        super.setAccrualType(pAccrualType);
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
