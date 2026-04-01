package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401k;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
    @author Jeff Jones
 */
public class ThirdParty401kPayrollDTO extends ThirdParty401kCensusDTO {

    Paycheck mPaycheck;

    //Payroll File Data
    public ThirdParty401kPayrollDTO() {

    }

    private ThirdParty401kPaycheck.PayrollFilePaycheck mPayrollFilePaycheck;
    public ThirdParty401kPayrollDTO(ThirdParty401kCompanyServiceInfo pTP401kCompanyServiceInfo, Paycheck pPaycheck) {
        super(pTP401kCompanyServiceInfo, pPaycheck.getSourceEmployee());
        mPaycheck = pPaycheck;
        mPayrollFilePaycheck = mPaycheck.getThirdParty401kPaycheck().getPayrollFilePaycheck();
        validatePayrollData(pPaycheck);
    }

    public Paycheck getPaycheck() {
        return mPaycheck;
    }

    public String getSalary() {
        return mPayrollFilePaycheck.getSalary();
    }

    public String getDeferral() {
        return mPayrollFilePaycheck.getDeferral();
    }

    public String getRoth() {
        return mPayrollFilePaycheck.getRoth();
    }

    public String getLoan() {
        return mPayrollFilePaycheck.getLoan();
    }

    public String getMatching() {
        return mPayrollFilePaycheck.getMatching();
    }

    public String getProfitSharing() {
        return mPayrollFilePaycheck.getProfitSharing();
    }

    public String getSafeHarbor() {
        return mPayrollFilePaycheck.getSafeHarbor();
    }

    public String getHours() {
        return mPayrollFilePaycheck.getHours();
    }

    public String getBeginPayPeriod() {
        return mPayrollFilePaycheck.getBeginPayPeriod();
    }

    public String getEndPayPeriod() {
        return mPayrollFilePaycheck.getEndPayPeriod();
    }

    private void validatePayrollData(Paycheck pPaycheck) {
        ArrayList<String> paycheckValidationErrors = mPayrollFilePaycheck.isValidForPayrollFile();
        this.getValidationErrors().addAll(paycheckValidationErrors);
    }

    public boolean isSalaryValid() {
        return mPayrollFilePaycheck.isSalaryValid();
    }

    public boolean isDeferralValid() {
        return mPayrollFilePaycheck.isDeferralValid();
    }

    public boolean isRothValid() {
        return mPayrollFilePaycheck.isRothValid();
    }

    public boolean isLoanValid() {
        return mPayrollFilePaycheck.isLoanValid();
    }

    public boolean isMatchingValid() {
        return mPayrollFilePaycheck.isMatchingValid();
    }

    public boolean isProfitSharingValid() {
        return mPayrollFilePaycheck.isProfitSharingValid();
    }

    public boolean isSafeHarborValid() {
        return mPayrollFilePaycheck.isSafeHarborValid();
    }

    @Override
    protected String key() {
        String key = super.key();
        key += getBeginPayPeriod();
        key += getEndPayPeriod();
        key += getSalary();
        key += getDeferral();
        key += getRoth();
        key += getLoan();
        key += getMatching();
        key += getProfitSharing();
        key += getSafeHarbor();
        key += getHours();
        return key;
    }
}
