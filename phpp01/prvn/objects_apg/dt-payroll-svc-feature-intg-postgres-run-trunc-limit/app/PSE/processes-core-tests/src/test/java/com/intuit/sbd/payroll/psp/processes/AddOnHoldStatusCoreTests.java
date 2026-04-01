package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertCount;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 21, 2008
 * Time: 2:43:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddOnHoldStatusCoreTests {
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
    public void addOnHoldStatusCore_NullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    null,
                                                                                    ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addOnHoldStatusCore_NullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(null,
                                                                                "1234567",
                                                                                ServiceSubStatusCode.ActiveCurrent);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addOnHoldStatusCore_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "upd_id_dne",
                                                                                    ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("169", errorMessage.getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", errorMessage.getMessage());
    }

    @Test
    public void addOnHoldStatusCore_StatusInvalidForService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                    "123456",
                                                                                    ServiceSubStatusCode.AS400DirectDepositLimitHold);
        PayrollServices.commitUnitOfWork();
        assertCount(1, result);
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1105", errorMessage.getMessageCode());
        assertEquals("Service sub-status 'AS400DirectDepositLimitHold' is not applicable to the DirectDeposit service.",
                     errorMessage.getMessage());

    }


    @Test
    public void addOnHoldStatusCore_AlreadyOnHoldForSameServiceStatus() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123456",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("1109", errorMessage.getMessageCode());
        assertEquals("On Hold sub-status 'Fraud' is already set for the company QBOE:123456.",
                errorMessage.getMessage());

    }

    @Test
    public void addOnHoldStatusCore_AMLHoldServiceSubStatus() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "123456",
                ServiceSubStatusCode.AMLHold);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company1, EventTypeCode.ServiceStatusChange));
        assertEquals("AML Hold", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewOnHoldReason));

        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.DesktopAMLHoldApplied)));
        assertNotNull(companyEventEmail);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,
                "123456",
                ServiceSubStatusCode.AMLHold);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Remove On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEvent=assertOne(CompanyEvent.findCompanyEvents(company1, EventDetailTypeCode.OldOnHoldReason,"AML Hold"));
        assertEquals("AML Hold", companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldOnHoldReason));

        companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.DesktopAMLHoldRemoved)));
        assertNotNull(companyEventEmail);
        PayrollServices.rollbackUnitOfWork();



    }

    @Test
    public void addOnHoldStatusCore_AlreadyOnHoldForDifferentServiceStatus() {
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
                                                                                ServiceSubStatusCode.IntuitCollections);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        Expression query = new Query<OnHoldReason>()
                .Where(OnHoldReason.Company().equalTo(company)
                       .And(OnHoldReason.ExpirationDate().isNull()))
                .OrderBy(OnHoldReason.OnHoldReasonCd());

        DomainEntitySet<OnHoldReason> onHoldReasons = Application.find(OnHoldReason.class, query);
        assertEquals("Number of Active On Holds ", 2, onHoldReasons.size());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReasons.get(0).getOnHoldReasonCd());
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.IntuitCollections, onHoldReasons.get(1).getOnHoldReasonCd());
        // verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Created});
        for (FinancialTransaction finTx:finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            assertEquals("Number of On Holds for FinancialTransaction", 2, finTx.getOnHoldReasonCollection().size());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addOnHoldStatusCore_Success() {

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
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Number of Active On Holds ", 1, company.getOnHoldReasonCollection().size());
        Iterator<OnHoldReason> onHoldReasonIterator = company.getOnHoldReasonCollection().iterator();
        OnHoldReason onHoldReason = onHoldReasonIterator.next();
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());

        // Verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Created});
        for (FinancialTransaction finTx:finanicalTxns) {
            System.out.println("Txn type: "+finTx.getTransactionType().getTransactionTypeCd());
            assertEquals("Is On Hold", true, finTx.getOnHold());
            if (finTx.getOnHoldReasonCollection().iterator().hasNext()) {
                assertEquals("Financial Transaction On Hold Reason",
                        onHoldReason,
                        finTx.getOnHoldReasonCollection().iterator().next());
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addOnHoldStatusCore_PayrollAndNonPayrollFees() {

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        Company company = Application.refresh(c3dl.persistCompany3());

        // update its address so fees will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoUpdate);
        PayrollServicesTest.assertSuccess("Updating company legal address for taxability", prUpdate);

        PayrollRunDTO payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                                "8574536",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        assertEquals("Number of Active On Holds ", 1, company.getOnHoldReasonCollection().size());
        Iterator<OnHoldReason> onHoldReasonIterator = company.getOnHoldReasonCollection().iterator();
        OnHoldReason onHoldReason = onHoldReasonIterator.next();
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());

        // Verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchTest87");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[] {TransactionStateCode.Created});
        assertEquals("Number of financial transactions", 5, finanicalTxns.size()); // 1 ER debit, 2 EE credits, 1 fee, 1 tax
        for (FinancialTransaction finTx:finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            if (finTx.getOnHoldReasonCollection().iterator().hasNext()) {
                assertEquals("Financial Transaction On Hold Reason",
                        onHoldReason,
                        finTx.getOnHoldReasonCollection().iterator().next());
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result2 = PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBDT,
                                                                                "8574536",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result2);

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        //Application.beginUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        //Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(10);
        TransactionReverseDTO txnRevDTO = new TransactionReverseDTO();
        txnRevDTO.setChargeFee(true);
        txnRevDTO.setIntuitInitiatedReversals(false);
        txnRevDTO.setSourcePayrollRunId("BatchTest87");
        txnRevDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseResult = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, "8574536", txnRevDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(reverseResult);

        PayrollServices.beginUnitOfWork();
        ProcessResult result4 = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                                                                                "8574536",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result4.getMessages().size());
        assertEquals("Add On Hold", true, result4.isSuccess());

        // verify on hold reasons were NOT added to the additional fee txn and its associated sales tax
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        assertTrue("Company is on hold", company.isCompanyOnHold());
        Iterator<OnHoldReason> onHoldReasonItr = company.getOnHoldReasonCollection().iterator();
        int numActiveOnHolds=0;
        OnHoldReason onHoldReason1=null;
        while (onHoldReasonItr.hasNext()) {
            OnHoldReason tempReason = onHoldReasonItr.next();
            if (tempReason.getExpirationDate() == null) {
                numActiveOnHolds++;
                onHoldReason1 = tempReason;
            }
        }
        assertEquals("Number of Active On Holds ", 1, numActiveOnHolds);
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReason1.getOnHoldReasonCd());

        // Verify pending/created fee and sales tax financial transactions are on hold
        payroll = PayrollRun.findPayrollRun(company, "BatchTest87");
        finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[] {TransactionStateCode.Created});
        assertEquals("Number of financial transactions", 2, finanicalTxns.size());
        for (FinancialTransaction finTx:finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
        }
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test case to test whether the user role has the previliges to add the onHold reason or not. 
     * This test case is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore.
     */    
    @Test
    public void addOnHoldStatusCore_OnHoldStatusNotAllowedForRole() {

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

        //Add user with the role 'QBOE-IOPRep', which doesn't have previliges to add 'Frauad' onHold reason
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("QBOE-IOPRep");
        ProcessResult addUserResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(foundRole.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", addUserResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) addUserResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));


        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        assertEquals("1204", result.getMessages().get(0).getMessageCode());
        assertEquals("Role QBOE-IOPRep cannot change a service to service sub-status 'Fraud'.", result.getMessages().get(0).getMessage());
    }

    /**
     * Test case to test whether the user role has the previliges to add the onHold reason or not.
     * This validation is applicable if the user is Agent. If the user is not an agent validation for role previliges
     * will ignore.
     */
    @Test
    public void addOnHoldStatusCore_OnHoldStatusAllowedForRole() {

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

        //Add user with the role 'DirectDepositSupervisor', which has the previliges to add 'Fruad' onHold reason
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("RMRep");
        ProcessResult addUserResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(foundRole.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Add User ProcessResult ", addUserResult);

        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser) addUserResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));


        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                                                                                "123272727",
                                                                                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 0, result.getMessages().size());
        assertEquals("Add On Hold", true, result.isSuccess());

        // verify on hold reasons were added
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertEquals("Number of Active On Holds ", 1, company.getOnHoldReasonCollection().size());
        Iterator<OnHoldReason> onHoldReasonIterator = company.getOnHoldReasonCollection().iterator();
        OnHoldReason onHoldReason = onHoldReasonIterator.next();
        assertEquals("On Hold Reason Code", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());

        // Verify pending/created financial transactions are on hold
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> finanicalTxns = payroll.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Created});
        for (FinancialTransaction finTx:finanicalTxns) {
            assertEquals("Is On Hold", true, finTx.getOnHold());
            if (finTx.getOnHoldReasonCollection().iterator().hasNext()) {
                assertEquals("Financial Transaction On Hold Reason",
                        onHoldReason,
                        finTx.getOnHoldReasonCollection().iterator().next());
            }
        }
        PayrollServices.commitUnitOfWork();
        
    }

    @Test
    public void testImpoundOnHoldIfAgencyTaxCreditApplied() {
        //3606
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 23);
        PayrollRun voidPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-25"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 24);
        DataLoadServices.voidAPaycheck(voidPayroll);

        DataLoadServices.setPSPDate(2013, 1, 26);
        PayrollRun newPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-28"));

        DataLoadServices.setPSPDate(2013, 1, 28);
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.RiskAssessment);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        Application.refresh(newPayroll);
        FinancialTransaction taxDebit = assertOne(newPayroll.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(TransactionStateCode.Created, taxDebit.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());

    }
}
