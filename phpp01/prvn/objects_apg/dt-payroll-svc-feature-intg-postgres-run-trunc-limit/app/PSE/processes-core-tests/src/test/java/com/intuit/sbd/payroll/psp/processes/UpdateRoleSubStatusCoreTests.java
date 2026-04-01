package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 28, 2008
 * Time: 12:08:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRoleSubStatusCoreTests {
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
    public void updateRoleSubStatusCore_NullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                    null,
                                                                                    ServiceCode.DirectDeposit,
                                                                                    null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_NullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(null,
                                                                                    "1234567",
                                                                                    ServiceCode.DirectDeposit,
                                                                                    null);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                    "CompanyDNE",
                                                                                    ServiceCode.DirectDeposit,
                                                                                    null);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("169", errorMessage.getMessageCode());
        assertEquals("Company QBOE:CompanyDNE does not exist.", errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_CompanyNotOnService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceCode.Tax,
                                                                                    null);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1010", errorMessage.getMessageCode());
        assertEquals("Company QBOE:123456 is not associated with the Tax service.", errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_NullServiceSubStatusList() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceCode.DirectDeposit, null);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("5002", errorMessage.getMessageCode());
        assertEquals("Required 'ServiceSubStatusList' input is missing or blank", errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_ServiceSubStatusNotAllowedForRole() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //Add user
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(foundRole.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", processResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Cancelled);
        subStatusList.add(serviceSubStatus);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                "123456",
                ServiceCode.DirectDeposit,
                subStatusList);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 2, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1203", errorMessage.getMessageCode());
        assertEquals("Service sub-status 'PendingBankVerification' is not applicable to the QBOE-IOPRep role.", errorMessage.getMessage());

        Message errorMessage2 = result.getMessages().get(1);

        assertEquals("1203", errorMessage2.getMessageCode());
        assertEquals("Service sub-status 'Cancelled' is not applicable to the QBOE-IOPRep role.", errorMessage2.getMessage());
    }    

    @Test
    public void updateRoleSubStatusCore_CannotAddMultipleSubStatuses() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.PendingBankVerification);
        subStatusList.add(serviceSubStatus);
        serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.PendingFirstPayroll);
        subStatusList.add(serviceSubStatus);

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                "123456",
                ServiceCode.DirectDeposit,
                subStatusList);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1202", errorMessage.getMessageCode());
        assertEquals("Cannot Add Multiple ServiceSubStatuses for PendingActivation Service Status.", errorMessage.getMessage());
    }

    @Test
    public void updateRoleSubStatusCore_CancelServiceSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Cancelled);
        subStatusList.add(serviceSubStatus);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                "123456",
                ServiceCode.DirectDeposit,
                subStatusList);
        PayrollServices.commitUnitOfWork();
        assertSuccess("UpdateSubStatus", result);
        // Verify the status is updated
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123456", SourceSystemCode.QBOE);
        assertEquals("Updated status code", ServiceSubStatusCode.Cancelled,
                CompanyService.findCompanyService(company, ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to verify company is terminated even verification return transfers present
     */
    @Test
    public void updateRoleSubStatusCore_VerificationReturnTransfersExists() {
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        ProcessResult prUnhandled = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), "123123", true, true);
        PayrollServices.commitUnitOfWork();
        assertSuccess(prUnhandled);
		
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                        findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                                TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        Assert.assertEquals("Number of C1 EmployerVerificationDebit CR txns", 2, financialTransactions.size());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Terminated);
        subStatusList.add(serviceSubStatus);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                "1234567",
                                                                                ServiceCode.DirectDeposit, subStatusList);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        // verify company is terminated
        assertCompanyTerminated(SourceSystemCode.QBOE, "1234567");
    }

    @Test
    public void updateRoleSubStatusCore_VerificationTxsExists() {
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        PayrollServices.commitUnitOfWork();

        c1dl.persistCompanyWithPendingBAVerification();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Terminated);
        subStatusList.add(serviceSubStatus);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                                                                                "1234567",
                                                                                ServiceCode.DirectDeposit, subStatusList);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        // verify company is terminated
        assertCompanyTerminated(SourceSystemCode.QBOE, "1234567");
    }


    @Test
    public void updateRoleSubStatusCore_AddOnHoldSubStatus() {
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
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Add OnHold Reason", result);

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Number of Active On Holds ", 1, company.getOnHoldReasonCollection().size());
        Iterator<OnHoldReason> onHoldReasonIterator = company.getOnHoldReasonCollection().iterator();
        OnHoldReason onHoldReason = onHoldReasonIterator.next();
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateRoleSubStatusCore_AddMultipleOnHoldSubStatus() {
        //Add Multiple onHold Reasons
        addMultipleOnHoldsReasons();

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Number of Active On Holds ", 2, company.getOnHoldReasonCollection().size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to test the scenario to remove an OnHold Reason if the company is onHold and the existing company
     * onHoldReason does'nt exists in the input ServiceSubStatus list
     * and add new OnHold Reasons for the given ServiceSubStatuses in input list.
     */
    @Test
    public void updateRoleSubStatusCore_AddMultipleOnHoldsForOnHoldCompany() {
        //Add Multiple onHold Reasons
        addMultipleOnHoldsReasons();

        // verify two on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Number of Active On Holds ", 2, company.getOnHoldReasonCollection().size());
        PayrollServices.commitUnitOfWork();

        //Call UpdateSubStatues process to add single OnHoldReason
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Fraud);
        subStatusList.add(serviceSubStatus);

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                "123272727",
                ServiceCode.DirectDeposit,
                subStatusList);
        PayrollServices.commitUnitOfWork();
        System.out.println("Result " + result.getMessages());
    }

    private void addMultipleOnHoldsReasons() {
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
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.IntuitCollections);
        subStatusList.add(serviceSubStatus);
        serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Fraud);
        subStatusList.add(serviceSubStatus);

        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBOE,
                "123272727",
                ServiceCode.DirectDeposit,
                subStatusList);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());
    }

    private void assertCompanyTerminated(SourceSystemCode pSourceSystemCd, String pSourceCompanyId) {
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(pSourceCompanyId, pSourceSystemCd);
        Iterator<CompanyBankAccount> itCompanyBankAccounts = foundCompany.getCompanyBankAccountCollection().iterator();
        CompanyService companyService = CompanyService.findCompanyService(foundCompany, ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        foundCompany = PayrollServices.entityFinder.findById(Company.class,  foundCompany.getId());

        Assert.assertEquals("Service Status: ", ServiceSubStatusCode.Terminated, companyService.getStatusCd());

        DomainEntitySet<Employee> employees = foundCompany.getDirectDepositEmployees();
        if (employees != null) {
            for (Employee currEmployee : employees) {
                Assert.assertEquals("Employee Status", EmployeeStatus.Active, currEmployee.getStatusCd());
                DomainEntitySet<EmployeeBankAccount> eeBAs = currEmployee.getEmployeeBankAccountCollection();
                if (eeBAs != null) {
                    for (EmployeeBankAccount currEEBA : eeBAs) {
                        Assert.assertEquals("EEBA Status", BankAccountStatus.Active, currEEBA.getStatusCd());
                    }
                }
            }
        }

        PayrollServices.commitUnitOfWork();
    }
}
