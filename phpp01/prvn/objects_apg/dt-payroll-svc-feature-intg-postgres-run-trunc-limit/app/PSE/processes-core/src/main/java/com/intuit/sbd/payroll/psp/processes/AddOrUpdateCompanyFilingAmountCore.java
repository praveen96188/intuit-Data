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
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Core process for adding or updating a company filing amount.
 *
 * @author Tim Dry
 */
public class AddOrUpdateCompanyFilingAmountCore extends Process implements IProcess {
    // Set during construction.
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private CompanyFilingAmountDTO mCompanyFilingAmountDTO;

    // Derived during validation.
    private Company mCompany;
    private String mFilingAmountName;
    private SpcfCalendar mEffectiveDate;
    private boolean isRate = false;
    private Double mAmount;     // Could be rate or amount.  Caller is responsible for providing correct value.
    private CompanyAgencyPaymentTemplate mCompanyAgencyPaymentTemplate;

    public AddOrUpdateCompanyFilingAmountCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyFilingAmountDTO pCompanyFilingAmountDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyFilingAmountDTO = pCompanyFilingAmountDTO;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        CompanyFilingAmount companyFilingAmount;
        Double previousAmount = null;

        // See if there are any records for this CAPT/Effective Date
        DomainEntitySet<CompanyFilingAmount> cfas = CompanyFilingAmount.findCompanyFilingAmounts(
                mCompanyAgencyPaymentTemplate, mFilingAmountName, mEffectiveDate);

        // Should only be one, but mark them all as invalid.
        for (CompanyFilingAmount cfa : cfas) {
            cfa.setInvalidDate(PSPDate.getPSPTime());
            previousAmount = cfa.getAmount();
        }

        // Create a new one.
        companyFilingAmount = new CompanyFilingAmount(mCompanyAgencyPaymentTemplate, mFilingAmountName, mEffectiveDate, mAmount);
        Application.save(companyFilingAmount);

        CompanyEvent.createAdditionalFilingAmountEvent(mCompany, mCompanyAgencyPaymentTemplate.getPaymentTemplate(),
                                                       mFilingAmountName, previousAmount, mAmount, mEffectiveDate);

        // If this is an amount (not a rate) and there is not an amount for the following quarter, we need to create a $0 one.
        if (!isRate) {
            SpcfCalendar followingQuarter = mEffectiveDate.copy();
            followingQuarter.addMonths(3);

            cfas = CompanyFilingAmount.findCompanyFilingAmounts(mCompanyAgencyPaymentTemplate, mFilingAmountName, followingQuarter);
            if (cfas.size() == 0) {
                CompanyFilingAmount zeroFilingAmount = new CompanyFilingAmount(mCompanyAgencyPaymentTemplate, mFilingAmountName, followingQuarter, 0.00);
                Application.save(zeroFilingAmount);
            }
        }

        processResult.setResult(companyFilingAmount);

        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify we were given a DTO
        if (mCompanyFilingAmountDTO == null) {
            validationResult.getMessages().InvalidArgument(EntityName.AdditionalFilingAmount, "null", "DTO");
            return validationResult;
        }

        // Validate name
        mFilingAmountName = mCompanyFilingAmountDTO.getName();
        if (mFilingAmountName == null) {
            validationResult.getMessages().InvalidArgument(EntityName.AdditionalFilingAmount, "null", "Name");
            return validationResult;
        }

        // Name should allow us to find an Additional Filing Amount
        AdditionalFilingAmount afa = AdditionalFilingAmount.findByName(mFilingAmountName);
        if (afa == null) {
            validationResult.getMessages().InvalidArgument(EntityName.AdditionalFilingAmount, mFilingAmountName, "Name");
            return validationResult;
        }
        isRate = afa.getRate();

        // Make sure we have an effective date.
        DateDTO dateDTO = mCompanyFilingAmountDTO.getEffectiveDate();
        if (dateDTO == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Date, "null", "EffectiveDate");
            return validationResult;
        }

        // Should be first day of a quarter.
        int month = dateDTO.getMonth() + 1; // DTO Month is zero-based.
        int day = dateDTO.getDay();
        if (day != 1 || (month != SpcfCalendar.January && month != SpcfCalendar.April &&
                         month != SpcfCalendar.July && month != SpcfCalendar.October)) {
            validationResult.getMessages().InvalidArgument(EntityName.Date, dateDTO.toString(), "EffectiveDate");
            return validationResult;
        }
        mEffectiveDate = dateDTO.toSpcfCalendar();

        // Find the Company Agency Payment Template
        mCompanyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(mCompany, afa.getPaymentTemplate());
        if (mCompanyAgencyPaymentTemplate == null) {
            validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, afa.getPaymentTemplate().getPaymentTemplateCd(), "Payment Template");
            return validationResult;
        }

        mAmount = mCompanyFilingAmountDTO.getAmount();

        return validationResult;
    }

}
