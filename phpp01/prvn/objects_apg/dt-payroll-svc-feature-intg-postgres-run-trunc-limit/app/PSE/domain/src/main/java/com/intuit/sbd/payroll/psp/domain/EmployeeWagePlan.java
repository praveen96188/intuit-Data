package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class EmployeeWagePlan extends BaseEmployeeWagePlan implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public EmployeeWagePlan()
	{
		super();
	}
    
    // ----- QBDT Token overrides -----
    @Override
    public void setName(WagePlanNameCode pName) {
        if(!ObjectUtils.equals(getName(), pName)) {
            onUpdate();
        }
        super.setName(pName);
    }

    @Override
    public void setState(String pState) {
        if(!ObjectUtils.equals(getState(), pState)) {
            onUpdate();
        }
        super.setState(pState);
    }

    @Override
    public void setWagePlanValue(String pWagePlanValue) {
        if(!ObjectUtils.equals(getWagePlanValue(), pWagePlanValue)) {
            onUpdate();
        }
        super.setWagePlanValue(pWagePlanValue);
    }

    @Override
    public void setWagePlanDomain(WagePlanDomainCode pWagePlanDomain) {
        if(!ObjectUtils.equals(getWagePlanDomain(), pWagePlanDomain)) {
            onUpdate();
        }
        super.setWagePlanDomain(pWagePlanDomain);
    }

    @Override
    public void setDescription(String pDescription) {
        if(!ObjectUtils.equals(getDescription(), pDescription)) {
            onUpdate();
        }
        super.setDescription(pDescription);
    }

    @Override
    public void setRulesVersion(String pRulesVersion) {
        if(!ObjectUtils.equals(getRulesVersion(), pRulesVersion)) {
            onUpdate();
        }
        super.setRulesVersion(pRulesVersion);
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