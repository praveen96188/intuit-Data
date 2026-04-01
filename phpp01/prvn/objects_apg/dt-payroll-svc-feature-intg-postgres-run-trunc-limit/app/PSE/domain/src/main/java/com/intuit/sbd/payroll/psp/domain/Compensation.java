package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class Compensation extends BaseCompensation implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Compensation()
	{
		super();
	}

    public static DomainEntitySet<Compensation> findCompensationByPaycheckAndType(Paycheck pPaycheck, CompanyPayrollItem pCompanyPayrollItem){
        Expression<Compensation> query =
                new Query<Compensation>()
                        .Where(Compensation.Paycheck().equalTo(pPaycheck)
                                .And(Compensation.CompanyPayrollItem().equalTo(pCompanyPayrollItem)));

        return Application.find(Compensation.class, query);
    }
    
    // ----- QBDT Token overrides -----

    @Override
    public void setCompensationAmount(SpcfMoney pCompensationAmount) {
        if(!ObjectUtils.equals(getCompensationAmount(), pCompensationAmount)) {
            onUpdate();
        }
        super.setCompensationAmount(pCompensationAmount);    
    }

    @Override
    public void setHoursWorked(double pHoursWorked) {
        if(!ObjectUtils.equals(getHoursWorked(), pHoursWorked)) {
            onUpdate();
        }
        super.setHoursWorked(pHoursWorked);    
    }

    @Override
    public void setCompensationYTDAmount(SpcfMoney pCompensationYTDAmount) {
        if(!ObjectUtils.equals(getCompensationYTDAmount(), pCompensationYTDAmount)) {
            onUpdate();
        }
        super.setCompensationYTDAmount(pCompensationYTDAmount);    
    }

    @Override
    public void setPayStubOrder(long pPayStubOrder) {
        if(!ObjectUtils.equals(getPayStubOrder(), pPayStubOrder)) {
            onUpdate();
        }
        super.setPayStubOrder(pPayStubOrder);    
    }

    @Override
    public void setQbdtPaylineInfo(QbdtPaylineInfo pQbdtPaylineInfo) {
        if(!ObjectUtils.equals(getQbdtPaylineInfo(), pQbdtPaylineInfo)) {
            onUpdate();
        }
        super.setQbdtPaylineInfo(pQbdtPaylineInfo);    
    }

    @Override
    public void setCompanyPayrollItem(CompanyPayrollItem pCompanyPayrollItem) {
        if(!ObjectUtils.equals(getCompanyPayrollItem(), pCompanyPayrollItem)) {
            onUpdate();
        }
        super.setCompanyPayrollItem(pCompanyPayrollItem);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);    
    }

    @Override
    public void setPaycheck(Paycheck pPaycheck) {
        if(!ObjectUtils.equals(getPaycheck(), pPaycheck)) {
            onUpdate();
        }
        super.setPaycheck(pPaycheck);    
    }

    public void onUpdate() {
        if(getPaycheck() != null) {
            getPaycheck().onUpdate();
        }
    }

}