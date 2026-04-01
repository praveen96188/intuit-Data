package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 15, 2008
 * Time: 1:51:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveFraudFlagCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Company mCompany;

    public RemoveFraudFlagCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
    }

    public ProcessResult validate() {

        // validate company parameters
        ProcessResult result = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        if (!result.isSuccess()) {
            return result;
        }

        // make sure Company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            result.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return result;
        }

        boolean fraudFlag = mCompany.getIsFlaggedForFraud();

        if (!fraudFlag) {
            result.getMessages().FraudFlagIsNotSet(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
        }

        return result;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        mCompany.setIsFlaggedForFraud(false);
        Application.save(mCompany);

        // deactivate fraud events
        FraudEvent.deactiveCompanyFraudEvents(mCompany, FraudEventCategory.Payroll);

        //Create FraudFlagRemovedEvent event
        CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.FraudFlagRemovedEvent);

        processResult.setResult(mCompany);

        return processResult;
    }
}
