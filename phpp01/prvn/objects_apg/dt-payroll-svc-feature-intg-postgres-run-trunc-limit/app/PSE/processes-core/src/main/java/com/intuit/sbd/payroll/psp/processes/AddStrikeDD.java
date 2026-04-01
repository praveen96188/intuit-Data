/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddStrikeDD.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 *
 * User: rkrishna
 * Date: Dec 18, 2007
 * Time: 4:24:42 PM

 */
public class AddStrikeDD extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private String mStrikeReasonDescription;
    private SpcfCalendar mStrikeDate;

    private Company mCompany;
    private CompanyEvent mStrikeEvent;

    public CompanyEvent getStrikeEvent() {
        return mStrikeEvent;
    }   

    public AddStrikeDD(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pStrikeReasonDescription,
                              SpcfCalendar pStrikeDate) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mStrikeReasonDescription = pStrikeReasonDescription;
        mStrikeDate = pStrikeDate;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
        }

        if((mStrikeReasonDescription == null) || !(Validator.isValidLength(mStrikeReasonDescription, 1, 200))){
            validationResult.getMessages()
                    .InvalidValue(EntityName.Company, mStrikeReasonDescription, "Strike Reason");
        }

        validateDate(validationResult);

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        mStrikeEvent = CompanyEvent.createStrikeEvent(mCompany,
                                                     StrikeReason.Manual,
                                                     mStrikeReasonDescription,
                                                     mStrikeDate,new DomainEntitySet<FinancialTransaction>());

        return processResult;
    }

    /**
     * Private method that validates that a strike date should be
     * the past date.
     *
     * @param pValidationResult Validation Result
     */
    private void validateDate(ProcessResult pValidationResult) {

        if (mStrikeDate == null) {
            pValidationResult.getMessages()
                    .InvalidValue(EntityName.Date, mSourceSystemCode.toString(), "Strike Date");
        } else {
            SpcfCalendar currentDate = PSPDate.getPSPTime();

            if (mStrikeDate.after(currentDate)) {
                pValidationResult.getMessages().InvalidArgument(EntityName.Date,
                        mStrikeDate.toString(), "Strike Date can't be in the future");
            }
        }
    }
}
