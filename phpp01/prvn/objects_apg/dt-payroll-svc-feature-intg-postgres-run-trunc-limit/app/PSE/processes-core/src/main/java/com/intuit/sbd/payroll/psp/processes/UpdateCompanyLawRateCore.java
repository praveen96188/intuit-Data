/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateCompanyCore.java#7 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Core process for updating a company law rate.
 *
 * @author Tim Dry
 */
public class UpdateCompanyLawRateCore extends Process implements IProcess {

    private static final int MAX_VALID_RATES = 4;

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Company mCompany;
    private Law mLaw;
    private SpcfCalendar mQuarterStart;
    private Double mRate;
    private QbdtNumericType mRateType = QbdtNumericType.Percentage;
    private boolean mPushToQuickbooks;

    private CompanyLaw mCompanyLaw;

    public UpdateCompanyLawRateCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Law pLaw, SpcfCalendar pQuarterStart,
                                    Double pRate, QbdtNumericType pRateType, boolean pPushToQuickbooks) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mLaw = pLaw;
        mQuarterStart = pQuarterStart;
        mRate = pRate;
        mRateType = pRateType==null?QbdtNumericType.Percentage:pRateType;
        mPushToQuickbooks = pPushToQuickbooks;
    }

    public UpdateCompanyLawRateCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Law pLaw, SpcfCalendar pQuarterStart,
                                    Double pRate, boolean pPushToQuickbooks) {
        this(pSourceSystemCd,pSourceCompanyId,pLaw,pQuarterStart,pRate,QbdtNumericType.Percentage,pPushToQuickbooks);
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        double currentRate;

        CompanyLawRate currentLawRate = CompanyLawRate.findEffectiveLawRate(mCompanyLaw, mQuarterStart);
        if (currentLawRate != null) {
            currentRate = currentLawRate.getRate();

            // If the rate we are updating is not from a prior quarter, mark it as invalid since we are replacing it
            if (!currentLawRate.getEffectiveDate().before(mQuarterStart)) {
                currentLawRate.setInvalidDate(PSPDate.getPSPTime());
            }
        } else {
            currentRate = 0.0d;
        }

        // Create a new rate record with the CompanyLaw using the provided rate and quarter start.
        CompanyLawRate newRate = new CompanyLawRate();
        newRate.setCompanyLaw(mCompanyLaw);
        newRate.setRate(mRate);
        newRate.setRateType(mRateType);
        newRate.setEffectiveDate(mQuarterStart);
        Application.save(newRate);

        if (mPushToQuickbooks) {
            // Add it to the Company Law collection which should also update the Company Law token.
            mCompanyLaw.addCompanyLawRate(newRate);

            QbdtPayrollItemInfo pItemInfo = mCompanyLaw.getQbdtPayrollItemInfo();
            pItemInfo.setRatePushToken(mCompany.getCurrentToken());
        }

        CompanyEvent.createTaxRateChangeEvent(newRate, currentRate, mRate);

        // Look for any rates falling after the specified quarter and mark as invalid.
        DomainEntitySet<CompanyLawRate> futureRates = Application.find(CompanyLawRate.class,
                                CompanyLawRate.CompanyLaw().equalTo(mCompanyLaw)
                                     .And(CompanyLawRate.EffectiveDate().greaterThan(mQuarterStart))
                                     .And(CompanyLawRate.InvalidDate().isNull()));
        for (CompanyLawRate futureRate : futureRates) {
            //If the quarter start date falls into next year, we should not invalidate the rate
            int curYear = mQuarterStart.getYear();
            if(futureRate.getEffectiveDate().getYear() > curYear) {
                continue;
            }

            futureRate.setInvalidDate(PSPDate.getPSPTime());
        }

        // We have to make sure we did not exceed the total allowable.
        DomainEntitySet<CompanyLawRate> validRates = Application.find(CompanyLawRate.class, new Query<CompanyLawRate>()
                .Where(CompanyLawRate.CompanyLaw().equalTo(mCompanyLaw)
                    .And(CompanyLawRate.EffectiveDate().lessThan(mQuarterStart))
                    .And(CompanyLawRate.InvalidDate().isNull()))
                .OrderBy(CompanyLawRate.EffectiveDate()));
        // We will retain MAX_VALID_RATES - 1 since we just created a new one that is not included in the query.
        for (int i = 0 ; i < (validRates.size() - (MAX_VALID_RATES - 1)) ; i++) {
            validRates.get(i).setInvalidDate(PSPDate.getPSPTime());
        }

        // Put the new rate in the returned result.
        processResult.setResult(newRate);

        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (mLaw == null) {
            validationResult.getMessages().LawNotSpecified(EntityName.Law, null);
            return validationResult;
        }

        if (mQuarterStart == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Date, "null", "quarterStartDate");
            return validationResult;
        }

        // Quarter start must be either Jan 1, Apr 1, July 1, or Oct 1.
        if (mQuarterStart.getDay() != 1 || (( mQuarterStart.getMonth() != SpcfCalendar.January )&&
                                            ( mQuarterStart.getMonth() != SpcfCalendar.April ) &&
                                            ( mQuarterStart.getMonth() != SpcfCalendar.July ) &&
                                            ( mQuarterStart.getMonth() != SpcfCalendar.October ))) {
            validationResult.getMessages().InvalidArgument(EntityName.Date, mQuarterStart.toString(), "quarterStartDate");
            return validationResult;
        }

        // Verify we have a rate.
        if (mRate == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Date, "null", "rate");
            return validationResult;
        }

        // Verify there is a valid Company Law Rate for the provided quarter.
        mCompanyLaw = CompanyLaw.findCompanyLaw(mCompany, mLaw.getLawId());
        if (mCompanyLaw == null) {
            validationResult.getMessages().CompanyLawDoesNotExist(EntityName.Company, mSourceSystemCd.toString(), mSourceCompanyId, mLaw.getLawId());
            return validationResult;
        }

        return validationResult;
    }

}
