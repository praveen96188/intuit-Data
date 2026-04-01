package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 23, 2010
 * Time: 11:18:47 AM
 */
public class UpdateCompanyTokensAndIdsCore extends Process {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private CompanyDTO mCompanyDTO;
    private boolean mOverride;

    private Company mCompany;

    public UpdateCompanyTokensAndIdsCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CompanyDTO pCompanyDTO, boolean pOverride) {
        mSourceCompanyId = pSourceCompanyId;
        mSourceSystemCode = pSourceSystemCd;
        mCompanyDTO = pCompanyDTO;
        mOverride = pOverride;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mCompanyDTO == null) {
            validationResult.getMessages().CompanyNotSpecified(EntityName.Company, null);
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult<Company> process() {
        if(mOverride || (mCompanyDTO.getCurrentToken() != null && mCompanyDTO.getCurrentToken() > mCompany.getCurrentToken())) {
            mCompany.setCurrentToken(mCompanyDTO.getCurrentToken());
        }

        if(mOverride || (mCompanyDTO.getNextEmployeeId() != null && Long.parseLong(mCompanyDTO.getNextEmployeeId()) > Long.parseLong(mCompany.getNextEmployeeId()))) {
            mCompany.setNextEmployeeId(mCompanyDTO.getNextEmployeeId());
        }

        if(mOverride || (mCompanyDTO.getNextPaycheckId() != null && Long.parseLong(mCompanyDTO.getNextPaycheckId()) > Long.parseLong(mCompany.getNextPaycheckId()))) {
            mCompany.setNextPaycheckId(mCompanyDTO.getNextPaycheckId());
        }

        if(mOverride || (mCompanyDTO.getNextPayrollTransactionId() != null && Long.parseLong(mCompanyDTO.getNextPayrollTransactionId()) > Long.parseLong(mCompany.getNextPayrollTransactionId()))) {
            mCompany.setNextPayrollTransactionId(mCompanyDTO.getNextPayrollTransactionId());
        }

        if(mOverride || (mCompanyDTO.getNextPayrollItemId() != null && Long.parseLong(mCompanyDTO.getNextPayrollItemId()) > Long.parseLong(mCompany.getNextPayrollItemId()))) {
            mCompany.setNextPayrollItemId(mCompanyDTO.getNextPayrollItemId());
        }

        mCompany = Application.save(mCompany);

        ProcessResult<Company> processResult = new ProcessResult<Company>();
        processResult.setResult(mCompany);
        return processResult;
    }
}
