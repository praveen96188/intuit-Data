package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 21, 2008
 * Time: 2:43:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveOnHoldStatusCoreTests {
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
    public void removeOnHoldStatusCore_NullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                                                                                    null,
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void removeOnHoldStatusCore_NullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.removeOnHoldReason(null,
                                                                                "1234567",
                                                                                ServiceSubStatusCode.Fraud);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void removeOnHoldStatusCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "upd_id_dne",
                                                                                    ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("169", errorMessage.getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", errorMessage.getMessage());
    }

    @Test
    public void removeOnHoldStatusCore_CompanyNotOnHoldForSameServiceStatus() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceSubStatusCode.IntuitCollections);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1111", errorMessage.getMessageCode());
        assertEquals("On Hold sub-status 'Fraud' is not set for the company QBOE:123456.",
                errorMessage.getMessage());

    }

    @Test
    public void removeOnHoldStatusCore_Success() {

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

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<OnHoldReason> onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));

        assertEquals("Number of Active On Holds ", 1, onHoldReasons.size());

        assertEquals(1, company.getFraudCompanyCollection().size());
        assertEquals(2, company.getFraudAddressCollection().size());
        assertEquals(4, company.getFraudContactCollection().size());

        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold 
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 1, finTx.getOnHoldReasonCollection().size());
        }

        PayrollServices.commitUnitOfWork();

        // Remove the added on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Remove On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));

        assertEquals("Number of On Holds ", 0, onHoldReasons.size());

        assertEquals(0, company.getFraudCompanyCollection().size());
        assertEquals(0, company.getFraudAddressCollection().size());
        assertEquals(0, company.getFraudContactCollection().size());

        // Verify pending/created financial transactions are not on hold
        payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", false, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 0, getActiveOnHoldReasons(finTx));
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void removeOnHoldStatusCore_MultipleOnHolds() {

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

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.IntuitCollections);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());
        DomainEntitySet<OnHoldReason> onHoldReasons = Application.find(OnHoldReason.class, query);
        assertEquals("Number of Active On Holds ", 2, onHoldReasons.size());

        assertEquals(1, company.getFraudCompanyCollection().size());
        assertEquals(2, company.getFraudAddressCollection().size());
        assertEquals(4, company.getFraudContactCollection().size());

        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.IntuitCollections, onHoldReasons.get(1).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 2, finTx.getOnHoldReasonCollection().size());
        }

        PayrollServices.commitUnitOfWork();

        // Remove the added on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.IntuitCollections);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Remove On Hold", true, result.isSuccess());

        // verify on hold reasons was removed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        onHoldReasons = Application.find(OnHoldReason.class,
                                         OnHoldReason.Company().equalTo(company)
                                         .And(OnHoldReason.ExpirationDate().isNull()));
        assertEquals("Number of On Holds ", 1, onHoldReasons.size());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());

        assertEquals(1, company.getFraudCompanyCollection().size());
        assertEquals(2, company.getFraudAddressCollection().size());
        assertEquals(4, company.getFraudContactCollection().size());

        // Verify pending/created financial transactions are still on hold
        payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 1, getActiveOnHoldReasons(finTx));
        }
        PayrollServices.commitUnitOfWork();
    }

    private int getActiveOnHoldReasons(FinancialTransaction financialTx) {
        Iterator<OnHoldReason> onHoldReasonItr = financialTx.getOnHoldReasonCollection().iterator();
        int noOfOnHolds = 0;
        while (onHoldReasonItr.hasNext()) {
            if (null == onHoldReasonItr.next().getExpirationDate()) {
                noOfOnHolds++;
            }
        }
        return noOfOnHolds;
    }

    /**
     * Test case to test whether the user role has the previliges to add the onHold reason or not.
     * This validation is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore.
     */
    @Test
    public void removeOnHoldStatusCore_OnHoldStatusNotAllowedForUserRole() {

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

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<OnHoldReason> onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));
        assertEquals("Number of Active On Holds ", 1, onHoldReasons.size());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 1, finTx.getOnHoldReasonCollection().size());
        }

        PayrollServices.commitUnitOfWork();

        //Add user with the role 'QBOE-IOPRep', which doesn't have previliges to add 'Frauad' onHold reason
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        ProcessResult addUserResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(foundRole.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", addUserResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) addUserResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        // Remove the added on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        assertEquals("1205", result.getMessages().get(0).getMessageCode());
        assertEquals("Role QBOE-IOPRep cannot change a service from service sub-status 'Fraud'.", result.getMessages().get(0).getMessage());
    }

    /**
     * Test case to test whether the user role has the previliges to add the onHold reason or not.
     * This validation is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore.
     */
    @Test
    public void removeOnHoldStatusCore_OnHoldStatusAllowedForUserRole() {

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

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<OnHoldReason> onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));
        assertEquals("Number of Active On Holds ", 1, onHoldReasons.size());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 1, finTx.getOnHoldReasonCollection().size());
        }

        PayrollServices.commitUnitOfWork();

        //Add user with the role 'QBOE-IOPRep', which doesn't have previliges to add 'Frauad' onHold reason
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("RMRep");
        ProcessResult addUserResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(foundRole.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", addUserResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) addUserResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        // Remove the added on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                "123272727",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Remove On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));

        assertEquals("Number of On Holds ", 0, onHoldReasons.size());

        // Verify pending/created financial transactions are not on hold
        payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", false, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 0, getActiveOnHoldReasons(finTx));
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void removeOnHoldStatusForTaxService() {

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123456789", payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // First add an on hold reason
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                "123456789",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        DomainEntitySet<OnHoldReason> onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));

        assertEquals("Number of Active On Holds ", 1, onHoldReasons.size());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 1, finTx.getOnHoldReasonCollection().size());
        }

        PayrollServices.commitUnitOfWork();

        // Remove the added on hold reason
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                "123456789",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Remove On Hold", true, result.isSuccess());

        // verify on hold reasons were removed
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        onHoldReasons =
                Application.find(OnHoldReason.class,
                        OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()));

        assertEquals("Number of On Holds ", 0, onHoldReasons.size());

        // Verify pending/created financial transactions are not on hold
        payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction finTx : finanicalTxns) {
            assertEquals("Is On Hold", false, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 0, getActiveOnHoldReasons(finTx));
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInitiationDateIsUpdatedWhenHoldRemoved() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("UT-TC96-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OK-OW9A-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollRun pr = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        DataLoadServices.setPSPDate(2012, 1, 6);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction erTaxDebit = assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxDebit));
        assertEquals(TransactionStateCode.Executed, erTaxDebit.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 1, 12);
        DataLoadServices.returnTxns(pr, TransactionTypeCode.EmployerTaxDebit);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("UT-TC96-PAYMENT").find());
        assertEquals(TaxPaymentStatus.OnHold, moPayment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 5, 1);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayrollTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), pr.getId().toString()));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testRemoveHoldUpdatesInitDate() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("UT-TC96-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OK-OW9A-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        DataLoadServices.setPSPDate(2012, 1, 12);
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("UT-TC96-PAYMENT").find());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moPayment, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        moPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("UT-TC96-PAYMENT").find());
        assertEquals(1, moPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 5, 12);
        PayrollServices.beginUnitOfWork();
        moPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("UT-TC96-PAYMENT").find());
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(moPayment, PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();


    }
}
