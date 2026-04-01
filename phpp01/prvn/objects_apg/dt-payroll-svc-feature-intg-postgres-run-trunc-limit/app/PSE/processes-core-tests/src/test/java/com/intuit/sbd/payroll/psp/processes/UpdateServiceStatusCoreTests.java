package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 17, 2008
 * Time: 4:57:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateServiceStatusCoreTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updServiceStatusCore_NullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                    null,
                                                                                    ServiceCode.DirectDeposit,
                                                                                    ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updServiceStatusCore_NullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(null,
                                                                                "1234567",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.ActiveCurrent);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateCompanyCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                    "upd_id_dne",
                                                                                    ServiceCode.DirectDeposit,
                                                                                    ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("169", errorMessage.getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", errorMessage.getMessage());
    }

    @Test
    public void updateServiceStatusCore_CompanyNotOnService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceCode.Tax,
                                                                                    ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1010", errorMessage.getMessageCode());
        assertEquals("Company QBOE:123456 is not associated with the Tax service.", errorMessage.getMessage());

    }

    @Test
    public void updateServiceStatusCore_StatusInvalidForService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceCode.DirectDeposit,
                                                                                    ServiceSubStatusCode.ActiveSeasonal);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1105", errorMessage.getMessageCode());
        assertEquals("Service sub-status 'ActiveSeasonal' is not applicable to the DirectDeposit service.",
                     errorMessage.getMessage());

    }

    @Test
    public void updateServiceStatusCore_ServiceStatusInvalidForSourceSystem() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.PendingPinCreation);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1106", errorMessage.getMessageCode());
        assertEquals("Service sub-status 'PendingPinCreation' is not applicable to the QBOE source system.",
                     errorMessage.getMessage());

    }

    /**
     * Test method to verify the validation of service status change from the current to new.
     * example: if the current state is ActiveCurrent we can only update the status
     * to ActiveSeasonal or PendingCancellation manually.
     */
    @Test
    public void updateServiceStatusCore_InvalidStatusChange() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1112", errorMessage.getMessageCode());
        assertEquals("Service sub-status 'PendingBankVerification' cannot be changed manually.", errorMessage.getMessage());
    }

    @Test
    public void updateServiceStatusCore_CompanyHasPendingTxs() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        assertSuccess(result);
    }

    @Test
    public void updateServiceStatusCore_Success() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);


        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071010");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 0, result.getMessages().size());
        // Ensure processing was succsessful
        assertSuccess("submitPayroll", result);

        // Verify the status is updated
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Updated status code", ServiceSubStatusCode.Cancelled,
                                    CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateServiceStatusCore_CancelServiceSuccess() {

        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", result);

        // Verify the service is cancelled
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123456", SourceSystemCode.QBOE);
        assertEquals("Updated status code", ServiceSubStatusCode.Cancelled,
                                    CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateServiceStatusCore_TerminateCompanySuccess() {

        // Create Company and CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Terminated);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", result);

        // Verify the status is updated
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123456", SourceSystemCode.QBOE);
        assertEquals("Updated status code", ServiceSubStatusCode.Terminated,
                                    CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateServiceStatusCore_TermToCancelRMRep() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMRep");

        // Reactivate and Verify
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled, true);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled);
    }

    @Test
    public void updateServiceStatusCore_TermToCancelRMManager() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMManager");

        // Reactivate and Verify
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled, true);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled);
    }

    @Test
    public void updateServiceStatusCore_TermToCancelRMSupervisor() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMSupervisor");

        // Reactivate and Verify
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled, true);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled);
    }

    @Test
    public void updateServiceStatusCore_TermToActiveCurrentRMRep() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMRep");

        // Reactivate and Verify
        ProcessResult<CompanyService> result = updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.ActiveCurrent, false);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "1204", errorMessage.getMessageCode());
        assertEquals("Error message", "Role RMRep cannot change a service to service sub-status 'ActiveCurrent'.",
                errorMessage.getMessage());
    }

    @Test
    public void updateServiceStatusCore_TermToActiveCurrentRMManager() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMManager");

        // Reactivate and Verify
        ProcessResult<CompanyService> result = updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.ActiveCurrent, false);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "1204", errorMessage.getMessageCode());
        assertEquals("Error message", "Role RMManager cannot change a service to service sub-status 'ActiveCurrent'.",
                errorMessage.getMessage());
    }

    @Test
    public void updateServiceStatusCore_TermToActiveCurrentRMSupervisor() {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();

        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep("RMSupervisor");

        // Reactivate and Verify
        ProcessResult<CompanyService> result = updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.ActiveCurrent, false);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "1204", errorMessage.getMessageCode());
        assertEquals("Error message", "Role RMSupervisor cannot change a service to service sub-status 'ActiveCurrent'.",
                errorMessage.getMessage());

    }

    @Test
    public void updateServiceStatusCore_TermToCancelDataFRGSupervisor() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("FRGSupervisor");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelQBOEIOPTeamLead() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("QBOE-IOPTeamLead");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelDataDirectDepositTierIII() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("DirectDepositTierIII");
    }     

    @Test
    public void updateServiceStatusCore_TermToCancelDataFRGManager() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("FRGManager");
    }


    @Test
    public void updateServiceStatusCore_TermToCancelQBOEIOPRep() {
        updateServiceStatusCore_TermToCancelByFailRole("QBOE-IOPRep");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelQBOEIOPSeniorRep() {
        updateServiceStatusCore_TermToCancelByFailRole("QBOE-IOPSeniorRep");
    }     

    @Test
    public void updateServiceStatusCore_TermToCancelQBOEIOPProductSpecialist() {
        updateServiceStatusCore_TermToCancelByFailRole("QBOE-IOPProductSpecialist");
    }          

    @Test
    public void updateServiceStatusCore_TermToCancelDataFRGRep() {
        updateServiceStatusCore_TermToCancelByFailRole("FRGRep");
    }        

    @Test
    public void updateServiceStatusCore_TermToCancelDataAccounting() {
        updateServiceStatusCore_TermToCancelByFailRole("Accounting");
    }    

    @Test
    public void updateServiceStatusCore_TermToCancelDataAccountingManager() {
        updateServiceStatusCore_TermToCancelByFailRole("AccountingManager");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelDataDesktopCareAgent() {
        updateServiceStatusCore_TermToCancelByFailRole("DesktopCareAgent");
    }       

    @Test
    public void updateServiceStatusCore_TermToCancelDataDesktopCareManager() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("DesktopCareManager");
    }    

    @Test
    public void updateServiceStatusCore_TermToCancelDataDirectDepositSME() {
        updateServiceStatusCore_TermToCancelByFailRoleToOkay("DirectDepositSME");
    }    

    @Test
    public void updateServiceStatusCore_TermToCancelDataHelpDesk() {
        updateServiceStatusCore_TermToCancelByFailRole("HelpDesk");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelDataCustodian() {
        updateServiceStatusCore_TermToCancelByFailRole("DataCustodian");
    }    

    @Test
    public void updateServiceStatusCore_TermToCancelReadOnly() {
        updateServiceStatusCore_TermToCancelByFailRole("ReadOnly");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelPSOAgent() {
        updateServiceStatusCore_TermToCancelByFailRole("PSOAgent");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelPOA() {
        updateServiceStatusCore_TermToCancelByFailRole("POA");
    }

    @Test
    public void updateServiceStatusCore_TermToCancelOperator() {
        updateServiceStatusCore_TermToCancelByFailRole("Operator");
    }        

    public void updateServiceStatusCore_TermToCancelByFailRole(String role) {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep(role);

        // Reactivate and Verify
        ProcessResult<CompanyService> result = updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled, false);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);
        assertEquals("Messages size", 2, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "1205", errorMessage.getMessageCode());
        assertEquals("Error message", "Role " + role + " cannot change a service from service sub-status 'Terminated'.",
                errorMessage.getMessage());
        errorMessage = result.getMessages().get(1);
        assertEquals("Error message code", "1204", errorMessage.getMessageCode());
        assertEquals("Error message", "Role " + role + " cannot change a service to service sub-status 'Cancelled'.",
                errorMessage.getMessage());
    }

    public void updateServiceStatusCore_TermToCancelByFailRoleToOkay(String role) {
        PayrollServices.beginUnitOfWork();
        Company company = dataloader.persistTestIntuitCompany();

        dataloader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult =
                PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(),
                                                        company.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();


        // Terminate the company
        updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated, true);

        // Verify the status is updated
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);

        loadRep(role);

        // Reactivate and Verify
        ProcessResult<CompanyService> result = updateStatus("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Cancelled, false);
        verifyStatusSuccess("123456", SourceSystemCode.QBOE, ServiceSubStatusCode.Terminated);
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "1205", errorMessage.getMessageCode());
        assertEquals("Error message", "Role " + role + " cannot change a service from service sub-status 'Terminated'.",
                errorMessage.getMessage());
    }
    
    
    private void loadRep(String repRole) {
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole(repRole);
        ProcessResult<AuthUser> processResult = PayrollServices.userManager.addUser("seanbarenz", Arrays.asList(foundRole.getRoleId()),"Sean","Barenz");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthUser user = processResult.getResult();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        AuthUser user2 = AuthUser.findUser("seanbarenz");
        PayrollServices.commitUnitOfWork();
    }

    private ProcessResult<CompanyService> updateStatus(String sourceCompanyId, SourceSystemCode sourceSystemCd, ServiceSubStatusCode targetStatus, boolean success) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> result = PayrollServices.companyManager.updateServiceStatus(sourceSystemCd,
                                                                                sourceCompanyId,
                                                                                ServiceCode.DirectDeposit,
                                                                                targetStatus);
        PayrollServices.commitUnitOfWork();
        assertEquals("Status Updated", success, result.isSuccess());
        return result;
    }


    private void verifyStatusSuccess(String sourceCompanyId, SourceSystemCode sourceSystemCd, ServiceSubStatusCode status) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        assertEquals("Updated status code", status,
                                        CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to test whether the user role has the previliges to update the status or not.
     * This validation is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore
     */
    @Test
    public void updateServiceStatusCore_CancelService_ServiceStatusNotAllowedForRole() {

        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Add user with the role 'QBOE-IOPRep', which doesn't have previliges to update the status from
        //'PendingBankVerification' to 'Cancelled'
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(foundRole.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", processResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 2, result.getMessages().size());
        assertEquals("1205", result.getMessages().get(0).getMessageCode());
        assertEquals("Role QBOE-IOPRep cannot change a service from service sub-status 'PendingBankVerification'.", result.getMessages().get(0).getMessage());
    }

    /**
     * Test case to test whether the user role has the previliges to update the status or not.
     * This validation is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore.
     */
    @Test
    public void updateServiceStatusCore_CancelService_ServiceStatusAllowedForRole() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20071010");
        PayrollServices.commitUnitOfWork();        

        //Add user with the role 'DirectDepositSupervisor', which has the previliges to update the status from
        //'PendingBankVerification' to 'Cancelled'
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("DesktopCareManager");
        ProcessResult processResult1 = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(foundRole.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", processResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) processResult1.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateServiceStatus(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceCode.DirectDeposit,
                                                                                ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("ProcessResult ", result);

        // Verify the service is cancelled
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Updated status code", ServiceSubStatusCode.Cancelled,
                                    CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }
}
