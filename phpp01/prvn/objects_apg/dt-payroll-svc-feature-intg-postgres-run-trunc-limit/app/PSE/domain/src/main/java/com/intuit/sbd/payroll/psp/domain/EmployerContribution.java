package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class EmployerContribution extends BaseEmployerContribution implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public EmployerContribution()
	{
		super();
	}

    public static DomainEntitySet<EmployerContribution> findContributionByPaycheckAndType(Paycheck pPaycheck, CompanyPayrollItem pCompanyPayrollItem) {
        Expression<EmployerContribution> query =
                new Query<EmployerContribution>()
                        .Where(EmployerContribution.Paycheck().equalTo(pPaycheck)
                                .And(EmployerContribution.CompanyPayrollItem().equalTo(pCompanyPayrollItem)));

        return Application.find(EmployerContribution.class, query);
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setContributionAmount(SpcfMoney pContributionAmount) {
        if(!ObjectUtils.equals(getContributionAmount(), pContributionAmount)) {
            onUpdate();
        }
        super.setContributionAmount(pContributionAmount);    
    }

    @Override
    public void setContributionYTDAmount(SpcfMoney pContributionYTDAmount) {
        if(!ObjectUtils.equals(getContributionYTDAmount(), pContributionYTDAmount)) {
            onUpdate();
        }
        super.setContributionYTDAmount(pContributionYTDAmount);    
    }

    @Override
    public void setTaxableWagesAmount(SpcfMoney pTaxableWagesAmount) {
        if(!ObjectUtils.equals(getTaxableWagesAmount(), pTaxableWagesAmount)) {
            onUpdate();
        }
        super.setTaxableWagesAmount(pTaxableWagesAmount);    
    }

    @Override
    public void setTotalWagesAmount(SpcfMoney pTotalWagesAmount) {
        if(!ObjectUtils.equals(getTotalWagesAmount(), pTotalWagesAmount)) {
            onUpdate();
        }
        super.setTotalWagesAmount(pTotalWagesAmount);    
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
    public void setPaycheck(Paycheck pPaycheck) {
        if(!ObjectUtils.equals(getPaycheck(), pPaycheck)) {
            onUpdate();
        }
        super.setPaycheck(pPaycheck);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);    
    }

    public void onUpdate() {
         if(getPaycheck() != null) {
            getPaycheck().onUpdate();
        }
    }
}