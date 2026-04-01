package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyEventDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyEventDetailDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;


/**
 * User: mvillani
 * Date: Jul 31, 2007
 * Time: 2:09:15 PM
 */
public final class AddCompanyEventCore extends Process implements IProcess {

    /**
     * Core process for adding a new company event.
     *
     * @author Marcela Villani
     */

    private CompanyEventDTO mCompanyEventDTO;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;


    public AddCompanyEventCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                               CompanyEventDTO pCompanyEventDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyEventDTO = pCompanyEventDTO;

    }

    public ProcessResult<CompanyEvent> process() {
        ProcessResult processResult = new ProcessResult();

        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        // Add Company Event
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(company, mCompanyEventDTO.getEventTypeCode());

        // Add Company Event Details
        for (CompanyEventDetailDTO companyEventDetailDTO : mCompanyEventDTO.getEventDetails()) {
            companyEvent.addCompanyEventDetail(companyEventDetailDTO.getEventDetailTympeCode(), companyEventDetailDTO.getEventDetailValue());
        }

        // Save Company Event
        companyEvent = Application.save(companyEvent);
        processResult.setResult(companyEvent);

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate if CompanyEventDTO DTO is null
        if (mCompanyEventDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, mSourceCompanyId, mSourceCompanyId);

        }

        // Check if Company parameters are valid
        String sourceSystemCode = (null == mSourceSystemCd) ? null : mSourceSystemCd.toString();
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        Company foundCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (foundCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }


        return validationResult;
    }


}