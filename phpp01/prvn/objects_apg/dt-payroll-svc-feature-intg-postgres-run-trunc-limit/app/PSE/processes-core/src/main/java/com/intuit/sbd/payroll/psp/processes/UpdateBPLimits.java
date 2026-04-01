package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 *
 * User: mvillani
 * Date: Nov 14, 2007
 * Time: 4:25:09 PM

 */
public class UpdateBPLimits extends Process implements IProcess {

    private static final String SERVICE_IDENTIFIER = "Bill Payment";

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private SpcfMoney mCompanyBPLimit;
    private SpcfMoney mPayeeBPLimit;
    private Company mCompany;
    private BPCompanyServiceInfo mBPCompanyServiceInfo;

    public UpdateBPLimits(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                          SpcfMoney pNewCompanyBPLimit, SpcfMoney pNewEmployeeBPLimit) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyBPLimit = pNewCompanyBPLimit;
        mPayeeBPLimit = pNewEmployeeBPLimit;
    }

    public BPCompanyServiceInfo getBPCompanyServiceInfo() {
        return mBPCompanyServiceInfo;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        SpcfMoney oldCompanyBPLimit = mBPCompanyServiceInfo.getOverrideCompanyLimitAmount();

        // Set Company Bill Payment Limit
        if (mCompanyBPLimit != null) {
            if (!mCompanyBPLimit.equals(oldCompanyBPLimit)) {
                mBPCompanyServiceInfo.setOverrideCompanyLimitAmount(mCompanyBPLimit);
            }
        } else if (oldCompanyBPLimit != null) {
            mBPCompanyServiceInfo.setOverrideCompanyLimitAmount(null);
        }

        // Set Payee Bill Payment Limit

        SpcfMoney oldPayeeBPLimit = mBPCompanyServiceInfo.getOverridePayeeLimitAmount();
        if (mPayeeBPLimit != null) {
            if (!mPayeeBPLimit.equals(oldPayeeBPLimit)) {
                mBPCompanyServiceInfo.setOverridePayeeLimitAmount(mPayeeBPLimit);
            }
        } else if (oldPayeeBPLimit != null) {
            mBPCompanyServiceInfo.setOverridePayeeLimitAmount(null);
        }

        mBPCompanyServiceInfo = Application.save(mBPCompanyServiceInfo);

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

        // Check if Company is associated with BillPayment Service
        mBPCompanyServiceInfo = (BPCompanyServiceInfo) CompanyService
                .findCompanyService(mCompany, ServiceCode.BillPayment);
        if (mBPCompanyServiceInfo == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(),
                    mSourceCompanyId, SERVICE_IDENTIFIER);
        }

        return validationResult;

    }


}