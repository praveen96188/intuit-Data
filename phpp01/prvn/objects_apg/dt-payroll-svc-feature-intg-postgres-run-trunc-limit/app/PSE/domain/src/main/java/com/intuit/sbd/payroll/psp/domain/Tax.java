package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Hand-written business logic
 */
public class Tax extends BaseTax implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Tax()
	{
		super();
	}

    public static ArrayList<SpcfUniqueId> getAllTaxCompanies() {

        String[] paramNames = new String[3];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "serviceCd";
        paramNames[2] = "excludeDeletedCompany";

        Object[] paramValues = new Object[3];
        paramValues[0] = SourceSystemCode.QBDT;
        paramValues[1] = ServiceCode.Tax;
        paramValues[2] = !AuthUser.hasSAPAdminAccess();

        ArrayList<SpcfUniqueId> retList =
            Application.executeNamedQuery("findCompanyIdsBySourceSystemAndService", paramNames, paramValues);


        return retList;
    }

    public static List<Object[]> getFileableCompanyLiabilities(SpcfCalendar pLastRunDate) {
        //todo: dawn put into sys param table
        SpcfCalendar beginningOfTimeTax = SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone());

        // note - get any companies that may have changes so that we can also capture any deletes/ voids i.e.; don't worry about
        //the status of the paycheck
        String[] paramNames = new String[4];
        paramNames[0] = "lastRunDate";
        paramNames[1] = "beginningOfTimeTax";
        paramNames[2] = "runDateTime";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = pLastRunDate;
        paramValues[1] = beginningOfTimeTax;
        paramValues[2] = PSPDate.getPSPTime();
        paramValues[3] = !AuthUser.hasSAPAdminAccess();

        List<Object[]> retList =
            Application.executeNamedQuery("findCompaniesWithUpdatedFileableLiabilities", paramNames, paramValues);

        return retList;
    }

    public static List<Object[]> getFileableCompanyAdjustments(SpcfCalendar pLastRunDate) {
        SpcfCalendar beginningOfTimeTax = SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone());

        // note - get any companies that may have changes so that we can also capture any deletes/ voids i.e.; don't worry about
        //the status of the paycheck
        String[] paramNames = new String[4];
        paramNames[0] = "lastRunDate";
        paramNames[1] = "beginningOfTimeTax";
        paramNames[2] = "runDateTime";
        paramNames[3] = "excludeDeletedCompany";

        Object[] paramValues = new Object[4];
        paramValues[0] = pLastRunDate;
        paramValues[1] = beginningOfTimeTax;
        paramValues[2] = PSPDate.getPSPTime();
        paramValues[3] = !AuthUser.hasSAPAdminAccess();
        
        List<Object[]> retList =
            Application.executeNamedQuery("findCompaniesWithUpdatedFileableAdjustments", paramNames, paramValues);

        return retList;
    }
    
    // ----- QBDT Token overrides -----

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
    public void setTaxLiabilityAmount(SpcfMoney pTaxLiabilityAmount) {
        if(!ObjectUtils.equals(getTaxLiabilityAmount(), pTaxLiabilityAmount)) {
            onUpdate();
        }
        super.setTaxLiabilityAmount(pTaxLiabilityAmount);    
    }

    @Override
    public void setTaxLiabilityYTDAmount(SpcfMoney pTaxLiabilityYTDAmount) {
        if(!ObjectUtils.equals(getTaxLiabilityYTDAmount(), pTaxLiabilityYTDAmount)) {
            onUpdate();
        }
        super.setTaxLiabilityYTDAmount(pTaxLiabilityYTDAmount);    
    }

    @Override
    public void setPayStubOrder(long pPayStubOrder) {
        if(!ObjectUtils.equals(getPayStubOrder(), pPayStubOrder)) {
            onUpdate();
        }
        super.setPayStubOrder(pPayStubOrder);    
    }

    @Override
    public void setTipsTaxableWageAmount(SpcfMoney pTipsTaxableWageAmount) {
        if(!ObjectUtils.equals(getTipsTaxableWageAmount(), pTipsTaxableWageAmount)) {
            onUpdate();
        }
        super.setTipsTaxableWageAmount(pTipsTaxableWageAmount);    
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if(!ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);    
    }

    @Override
    public void setLaw(Law pLaw) {
        if(!ObjectUtils.equals(getLaw(), pLaw)) {
            onUpdate();
        }
        super.setLaw(pLaw);    
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