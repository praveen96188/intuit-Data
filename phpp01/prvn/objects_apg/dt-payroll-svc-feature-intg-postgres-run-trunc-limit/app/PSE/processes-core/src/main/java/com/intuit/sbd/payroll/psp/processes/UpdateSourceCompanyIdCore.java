package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * @author Jeff Jones
 */
public class UpdateSourceCompanyIdCore extends Process implements IProcess {


    private Company domainCompany;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private String newSourceCompanyId;

    public Company getUpdatedCompany() {
        return domainCompany;
    }

    public UpdateSourceCompanyIdCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pNewSourceCompanyId) {
        this.sourceSystemCd = pSourceSystemCd;
        this.sourceCompanyId = pSourceCompanyId;
        this.newSourceCompanyId = pNewSourceCompanyId;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        CompanyEvent.createCompanyInfoChangeEvent(domainCompany, sourceCompanyId, newSourceCompanyId, EventTypeCode.SourceCompanyIdChanged);

        domainCompany.setSourceCompanyId(newSourceCompanyId);

        Application.save(domainCompany);

        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company exists
        domainCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (domainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        //Validate company does not already exist
        Company alreadyExistingCompany = Company
                .findCompany(newSourceCompanyId, sourceSystemCd);
        if (alreadyExistingCompany != null) {
            validationResult.getMessages().CompanyAlreadyExists(EntityName.Company, newSourceCompanyId,
                    sourceSystemCd.toString(), newSourceCompanyId);
            return validationResult;
        }

        return validationResult;
    }
}
