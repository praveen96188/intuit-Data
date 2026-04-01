package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 *
 * User: mvillani
 * Date: Nov 14, 2007
 * Time: 4:25:09 PM

 */
public class UpdateDDLimits extends Process implements IProcess {

    private static final String SERVICE_IDENTIFIER = "DD";

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private SpcfMoney mCompanyDDLimit;
    private SpcfMoney mEmployeeDDLimit;
    private Company mCompany;
    private DDCompanyServiceInfo mDDCompanyServiceInfo;

    public UpdateDDLimits(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                          SpcfMoney pNewCompanyDDLimit, SpcfMoney pNewEmployeeDDLimit) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyDDLimit = pNewCompanyDDLimit;
        mEmployeeDDLimit = pNewEmployeeDDLimit;
    }

    public DDCompanyServiceInfo getDDCompanyServiceInfo() {
        return mDDCompanyServiceInfo;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        SpcfMoney oldCompanyDDLimit = mDDCompanyServiceInfo.getOverrideCompanyLimitAmount();

        // Set Company DD Limit
        if (mCompanyDDLimit != null) {
            if (!mCompanyDDLimit.equals(oldCompanyDDLimit)) {
                mDDCompanyServiceInfo.setOverrideCompanyLimitAmount(mCompanyDDLimit);
            }
        } else if (oldCompanyDDLimit != null) {
            mDDCompanyServiceInfo.setOverrideCompanyLimitAmount(null);
        }

        // Set Employee DD Limit

        SpcfMoney oldEmployeeDDLimit = mDDCompanyServiceInfo.getOverrideEmployeeLimitAmount();
        if (mEmployeeDDLimit != null) {
            if (!mEmployeeDDLimit.equals(oldEmployeeDDLimit)) {
                mDDCompanyServiceInfo.setOverrideEmployeeLimitAmount(mEmployeeDDLimit);
            }
        } else if (oldEmployeeDDLimit != null) {
            mDDCompanyServiceInfo.setOverrideEmployeeLimitAmount(null);
        }

        mDDCompanyServiceInfo = Application.save(mDDCompanyServiceInfo);

        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if Company is associated with DD Service
        mDDCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(mCompany, ServiceCode.DirectDeposit);
        if (mDDCompanyServiceInfo == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(),
                    mSourceCompanyId, SERVICE_IDENTIFIER);
        }

        return validationResult;

    }


}
