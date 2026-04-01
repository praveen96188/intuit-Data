package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 *
 * User: rkrishna
 * Date: Dec 21, 2007
 * Time: 10:36:53 AM

 */
public class CancelStrikeDDProcess extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private SpcfUniqueId mCompanyEventId;

    private CompanyEvent mCompanyEvent;

    public CompanyEvent getCompanyEvent() {
        return mCompanyEvent;
    }

    public CancelStrikeDDProcess(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                 SpcfUniqueId pCompanyEventId) {
        this.mSourceSystemCode = pSourceSystemCode;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mCompanyEventId = pCompanyEventId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        Company mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        mCompanyEvent = (CompanyEvent) Application.findById(CompanyEvent.class, mCompanyEventId);

        if (mCompanyEvent != null) {
            if (EventTypeCode.Strike == mCompanyEvent.getEventTypeCd()) {
                mCompanyEvent.setStatusCd(CompanyEventStatus.Inactive);
                mCompanyEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                mCompanyEvent = Application.save(mCompanyEvent);
            } else {
                processResult.getMessages()
                        .IllegalEventCancel(EntityName.Company, mCompanyEventId.toString(), mSourceCompanyId);
            }
        }

        return processResult;
    }
}
