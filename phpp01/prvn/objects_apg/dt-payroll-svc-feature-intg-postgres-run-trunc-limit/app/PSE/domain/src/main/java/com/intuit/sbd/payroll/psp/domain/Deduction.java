package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class Deduction extends BaseDeduction implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Deduction()
	{
		super();
	}

    public static DomainEntitySet<Deduction> findDeductionByPaycheckAndType(Paycheck pPaycheck, CompanyPayrollItem pCompanyPayrollItem){
        Expression<Deduction> query =
                new Query<Deduction>()
                        .Where(Deduction.Paycheck().equalTo(pPaycheck)
                                .And(Deduction.CompanyPayrollItem().equalTo(pCompanyPayrollItem)));

        return Application.find(Deduction.class, query);
    }
    
    // ----- QBDT Token overrides -----

    @Override
    public void setDeductionAmount(SpcfMoney pDeductionAmount) {
        if(!ObjectUtils.equals(getDeductionAmount(), pDeductionAmount)) {
            onUpdate();
        }
        super.setDeductionAmount(pDeductionAmount);    
    }

    @Override
    public void setDeductionYTDAmount(SpcfMoney pDeductionYTDAmount) {
        if(!ObjectUtils.equals(getDeductionYTDAmount(), pDeductionYTDAmount)) {
            onUpdate();
        }
        super.setDeductionYTDAmount(pDeductionYTDAmount);    
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