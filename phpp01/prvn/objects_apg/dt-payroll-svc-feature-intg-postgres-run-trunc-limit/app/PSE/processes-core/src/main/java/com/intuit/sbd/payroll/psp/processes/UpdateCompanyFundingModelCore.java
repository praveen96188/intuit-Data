package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: mvillani
 * Date: Nov 14, 2007
 * Time: 4:09:00 PM
 */
public class UpdateCompanyFundingModelCore extends Process implements IProcess {

    /**
     * Core process for updating a company's funding model
     *
     * @author Marcela Villani
     */


    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private FundingModel mFundingModel;
    private Company mCompany;

    public UpdateCompanyFundingModelCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                         FundingModel pNewFundingModel) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mFundingModel = pNewFundingModel;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (!mCompany.getFundingModel().equals(mFundingModel)) {
            // Create Company ACH Funding Model Changed event
            CompanyEvent.createCompanyInfoChangeEvent(mCompany, mCompany.getFundingModel().getFundingModelCd(), mFundingModel.getFundingModelCd(), EventTypeCode.CompanyFundingModelChanged);
            mCompany.setFundingModel(mFundingModel);
            mCompany = Application.save(mCompany);
        }

        processResult.setResult(mCompany);
        return processResult;
    }

    public Company getCompany() {
        return mCompany;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Source System Allows Multiple Funding Models
        SourcePayrollParameter allowMultipleFundingModels = SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.AllowMultipleFundingModels);
        if (!allowMultipleFundingModels.getParameterValue().equals("1")) {

            validationResult.getMessages().FundingModelUpdateNotAllowed(
                    EntityName.SourcePayrollParameter, mSourceSystemCd.toString(),
                    mSourceSystemCd.toString());
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
        }

        return validationResult;
    }
}

