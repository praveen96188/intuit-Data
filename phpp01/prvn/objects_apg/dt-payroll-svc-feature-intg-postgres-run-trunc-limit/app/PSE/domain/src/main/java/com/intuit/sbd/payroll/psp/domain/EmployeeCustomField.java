package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class EmployeeCustomField extends BaseEmployeeCustomField implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public EmployeeCustomField()
	{
		super();
	}

    // ----- QBDT Token overrides -----
    @Override
    public void setName(String pName) {
        if(!ObjectUtils.equals(getName(), pName)) {
            onUpdate();
        }
        super.setName(pName);
    }

    @Override
    public void setValue(String pValue) {
        if(!ObjectUtils.equals(getValue(), pValue)) {
            onUpdate();
        }
        super.setValue(pValue);
    }

    @Override
    public void setFieldOrder(int pFieldOrder) {
        if(!ObjectUtils.equals(getFieldOrder(), pFieldOrder)) {
            onUpdate();
        }
        super.setFieldOrder(pFieldOrder);
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
