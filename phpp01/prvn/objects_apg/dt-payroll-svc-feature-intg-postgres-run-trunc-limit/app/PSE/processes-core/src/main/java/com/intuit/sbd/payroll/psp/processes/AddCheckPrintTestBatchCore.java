package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatchStatus;
import com.intuit.sbd.payroll.psp.domain.CheckPrintPaycheck;
import com.intuit.sbd.payroll.psp.domain.CheckPrintPaycheckStatus;
import com.intuit.sbd.payroll.psp.domain.CheckPrintSignature;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyPaycheckBatch;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 4:10:06 PM
 */
public class AddCheckPrintTestBatchCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystem;
    private String mSourceCompanyId;
    private Company mCompany;

    public AddCheckPrintTestBatchCore(SourceSystemCode pSourceSystemCd, String pCompanyId) {
        mSourceSystem = pSourceSystemCd;
        mSourceCompanyId = pCompanyId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystem, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystem);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystem.toString(), mSourceCompanyId);
            return validationResult;
        }

        if(mCompany.getService(ServiceCode.CheckDistribution) == null) {
            validationResult.getMessages().CompanyNotActiveOnService(EntityName.Company, mSourceCompanyId,
                    mSourceSystem.toString(), mSourceCompanyId, ServiceCode.CheckDistribution.toString());
            return validationResult;
        }

        // make sure the company has a signature
        if(CheckPrintSignature.findCheckPrintSignature(mCompany) == null) {
            validationResult.getMessages().CompanyDoesNotHaveSignature(EntityName.Company, mSourceCompanyId,
                    mSourceSystem.toString(), mSourceCompanyId);
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult<CompanyPaycheckBatch> process() {
        ProcessResult<CompanyPaycheckBatch> processResult = new ProcessResult<CompanyPaycheckBatch>();

        String test = "Test";
        CheckPrintPaycheck checkPrintPaycheck = new CheckPrintPaycheck();
        checkPrintPaycheck.setCheckNumber("1234");
        checkPrintPaycheck.setCheckPrintPaycheckStatusCode(CheckPrintPaycheckStatus.AddedToPrintBatch);
        checkPrintPaycheck.setCompany(mCompany);
        checkPrintPaycheck.setEmployeePrintName(test);
        checkPrintPaycheck.setSourcePaycheckId(test);
        Application.save(checkPrintPaycheck);

        CompanyPaycheckBatch checkPrintBatch = new CompanyPaycheckBatch();
        checkPrintBatch.setCompany(mCompany);
        checkPrintBatch.setPaycheckDate(PSPDate.getPSPTime());
        checkPrintBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.Pending);
        checkPrintBatch.setCheckPrintBatchMessage(null);
        checkPrintBatch.setNumberOfChecks(1);
        checkPrintPaycheck.setCompanyPaycheckBatch(checkPrintBatch);
        checkPrintBatch.addCheckPrintPaycheck(checkPrintPaycheck);

        checkPrintBatch = Application.save(checkPrintBatch);        
        processResult.setResult(checkPrintBatch);
        processResult.setSuccess(true);

        return processResult;
    }


}
