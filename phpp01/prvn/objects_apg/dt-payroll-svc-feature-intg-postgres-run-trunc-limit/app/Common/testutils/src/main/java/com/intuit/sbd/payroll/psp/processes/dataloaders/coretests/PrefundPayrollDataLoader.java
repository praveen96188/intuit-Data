package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 29, 2009
 * Time: 8:26:36 AM
 */
public class PrefundPayrollDataLoader {

    public static void before() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static Company3Dataloader loadPrefundWithTaxes() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        return companyDataloader;
    }

    public static Company3Dataloader createQBDTCompanyAndSubmitFirstPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company3Dataloader companyDataloader = new Company3Dataloader();
        companyDataloader.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(companyDataloader.getCompany());
        dtoUpdate.setTaxExemptExpirationDate(null);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), dtoUpdate);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company for tax-exempt-expiration and legal address", prUpdate);

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        return companyDataloader;
    }

    public static void load() {
        before();
        loadPrefundWithTaxes();
    }
}
