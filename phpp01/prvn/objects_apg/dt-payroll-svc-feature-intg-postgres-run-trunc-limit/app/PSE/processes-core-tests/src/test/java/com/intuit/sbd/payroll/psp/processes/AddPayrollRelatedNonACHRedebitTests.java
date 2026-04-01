package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Aug 21, 2008
 * Time: 2:33:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddPayrollRelatedNonACHRedebitTests {
    private static Company1Dataloader c1dl;


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        loadDataHappyPath();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNullRedebitImpoundDTO() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(null);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Redebit failed", processResult.isSuccess());

        assertEquals("Number of messages", 1, processResult.getMessages().size());
        Message returnedMessage = processResult.getMessages().get(0);
        assertEquals("Message text", "Invalid argument: RedebitImpoundDTO", returnedMessage.getMessage());
        assertEquals("Message id", "11", returnedMessage.getMessageCode());
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitImpoundDTO.setAmount(originalTxn.getFinancialTransactionAmount());

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                null, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "137", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitImpoundDTO.setAmount(originalTxn.getFinancialTransactionAmount());

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, null, collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "138", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testCompanyDNE() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
        redebitImpoundDTO.setAmount(originalTxn.getFinancialTransactionAmount());

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Redebit failed", processResult.isSuccess());

        assertEquals("Number of messages", 1, processResult.getMessages().size());
        Message returnedMessage = processResult.getMessages().get(0);
        assertEquals("Message text", "Company QBOE:8574536 does not exist.", returnedMessage.getMessage());
        assertEquals("Message id", "169", returnedMessage.getMessageCode());
    }

    @Test
    public void testFinTxnDNE() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        String fakeFinTxnId = "11111111-4848-4444-843A-52adf678ce75";
        redebitImpoundDTO.setOriginalFinancialTxId(fakeFinTxnId);
        redebitImpoundDTO.setAmount(originalTxn.getFinancialTransactionAmount());

        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Redebit failed", processResult.isSuccess());

        assertEquals("Number of messages", 1, processResult.getMessages().size());
        Message returnedMessage = processResult.getMessages().get(0);
        assertEquals("Message text", "Financial Transaction " + fakeFinTxnId + " does not exist for company QBOE:1234567.", returnedMessage.getMessage());
        assertEquals("Message id", "264", returnedMessage.getMessageCode());
    }

    @Test
    public void testAllTxnsNotInSamePayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-09-22")));
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        //Offload both payrolls
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        PayrollServices.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        //Return both debits for R02
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "1234567", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        Assert.assertEquals("Number of ERDDDBs executed txns", 2, finTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(finTxns, "R02", "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun1.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap2 = payrollRun2.getUncollectedDDAmount();
        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of payroll txns", 1, payrollHashMap2.size());
        FinancialTransaction payroll1txn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction payroll2txn = payrollHashMap2.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payroll1Redebit = new RedebitImpoundDTO(payroll1txn.getId().toString(), new SpcfMoney("180.00"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO payroll2Redebit = new RedebitImpoundDTO(payroll2txn.getId().toString(), new SpcfMoney("50.00"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        collectionOfRedebitImpounds.add(payroll1Redebit);
        collectionOfRedebitImpounds.add(payroll2Redebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Process result", procResult.isSuccess());
        assertEquals("Number of errors", 1, procResult.getMessages().size());
        Message errorMessage = procResult.getMessages().get(0);
        assertEquals("Error message text", "All redebits must be associated with the same payroll run for company QBOE:1234567.", errorMessage.getMessage());
        assertEquals("Error message code", "1302", errorMessage.getMessageCode());
        assertEquals("Error message level", MessageInfo.MessageLevel.ERROR, errorMessage.getLevel());
    }

    @Test
    public void testNonACHRedebitProcess() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerDdDebit txns", 1, financialTxns.size());

        FinancialTransaction originalTxn = financialTxns.get(0);

        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeTxns.size());

        FinancialTransaction feeTxn = feeTxns.get(0);

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxTxns.size());

        FinancialTransaction taxTxn = taxTxns.get(0);

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), feeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), taxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        financialTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Txns", 1, financialTxns.size());

        for (FinancialTransaction redebitFinTxn : financialTxns) {
            assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction Amount ", originalTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        //Persistence check for EmployerFeeRedebit Transaction
        assertEquals("Number of Financial Txns", 1, feeTxns.size());

        for (FinancialTransaction redebitFinTxn : feeTxns) {
            assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction Amount ", feeTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Original Transaction ", feeRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        //Persistence check for ServiceSalesAndUseTaxRedebit Transaction
        assertEquals("Number of Financial Txs", 1, taxTxns.size());

        for (FinancialTransaction redebitFinTxn : taxTxns) {
            assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction Amount ", taxTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Original Transaction ", taxRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify there is no active ACHReject onhold reason
        company = Application.findById(Company.class, company.getId());
        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(ServiceSubStatusCode.AchRejectR1R9);
        assertEquals("AchRejectR1R9 is expired", null, onHoldReason);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHRedebitProcess_PartialAmountWired() {

        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        DomainEntitySet<FinancialTransaction> feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeTxs.size());

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxTxns.size());

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("Payroll Run Status", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction redebitFinTxn : financialTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());

            Assert.assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        // check for EmployerFeeRedebit Transaction is still returned
        assertEquals("Number of Financial Txs", 0, feeTxs.size());

        //Persistence check for ServiceSalesAndUseTaxRedebit Transaction
        assertEquals("Number of Financial Txs", 0, taxTxns.size());

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify ACHReject onhold reason is still active
        company = Application.findById(Company.class, company.getId());
        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(ServiceSubStatusCode.AchRejectR1R9);
        assertEquals("AchRejectR1R9 is not expired", null, onHoldReason.getExpirationDate());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHRedebitProcessNonNSF() {

        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerDdDebit txns", 1, financialTxs.size());

        FinancialTransaction originalTxn = financialTxs.get(0);

        DomainEntitySet<FinancialTransaction> feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeTxs.size());

        FinancialTransaction originalFeeTxn = feeTxs.get(0);

        DomainEntitySet<FinancialTransaction> taxTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxTxs.size());

        FinancialTransaction originalTaxTxn = taxTxs.get(0);

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(originalFeeTxn.getId().toString(), originalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(originalTaxTxn.getId().toString(), originalTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        Assert.assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        taxTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction redebitFinTxn : financialTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        Assert.assertEquals("Number of Fee Financial Txs", 1, feeTxs.size());

        for (FinancialTransaction redebitFinTxn : feeTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalFeeTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Original Transaction ", feeRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        Assert.assertEquals("Number of Tax Financial Txs", 1, taxTxs.size());

        for (FinancialTransaction redebitFinTxn : taxTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalTaxTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Original Transaction ", taxRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        Assert.assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);
            assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify there is no active ACHReject onhold reason
        company = Application.findById(Company.class, company.getId());
        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(ServiceSubStatusCode.AchRejectOther);
        assertEquals("AchRejectOther is expired", null, onHoldReason);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHRedebitProcessNonNSF_PartialAmountWired() {

        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        DomainEntitySet<FinancialTransaction> feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(feeTxs.size() == 1);
        FinancialTransaction originalFeeTxn = feeTxs.get(0);

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
//        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(originalFeeTxn.getId().toString(), originalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
//        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        Assert.assertEquals("Payroll Run Status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction redebitFinTxn : financialTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());

            Assert.assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        Assert.assertEquals("Number of Fee Financial Txs", 0, feeTxs.size());

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        Assert.assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify there is no active ACHReject onhold reason
        company = Application.findById(Company.class, company.getId());
        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(ServiceSubStatusCode.AchRejectOther);
        assertEquals("AchRejectOther is not expired", null, onHoldReason.getExpirationDate());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Return ER debit, create reversals, and then create wire with less than the exact amount to cover debt
     */
    @Test
    public void testRepaymentPendingReversalsCovers() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();


        //Return both debits for R02
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "1234567", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(finTxns, "R02", "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();

        transactionReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        transactionReverseDTO.setDdTransactionIdList(null);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        transactionReverseDTO.setTxDate(null);
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult procResult = PayrollServices.payrollManager.reverseTransaction(c1dl.getCompany().getSourceSystemCd(),
                c1dl.getCompany().getSourceCompanyId(), transactionReverseDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Reversal process result", procResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        assertEquals("Payroll run status is PendingReversals", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 4,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(new SpcfMoney("180.00"));

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), c1dl.getCompany().getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Assert.assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        DomainEntitySet<FinancialTransaction> cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        //Ensure the wire was properly created
        Assert.assertEquals("Number of Completed Financial Txs", 1, financialTxs.size());
        FinancialTransaction erDDRedb = financialTxs.get(0);
        Assert.assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        Assert.assertEquals("Financial Transaction Amount ", new SpcfMoney("180.00"), erDDRedb.getFinancialTransactionAmount());
        Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        Assert.assertEquals("Settlement Type ", SettlementType.Wire, erDDRedb.getSettlementTypeCd());

        //Ensure the ACH reversals were cancelled
        Assert.assertEquals("Number of ACH ee dd reversal Financial Txs", 2, cancelledReversals.size());
        for (FinancialTransaction currEEDDReversal : cancelledReversals) {
            Assert.assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), currEEDDReversal.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            org.junit.Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test - Return ER debit, create redebit, and then create wire with the exact amount to cover debt
     */
    @Test
    public void testRepaymentRecordPendingRedebitPayStatus() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerDdDebit txns", 1, financialTxs.size());

        FinancialTransaction originalTxn = financialTxs.get(0);

        DomainEntitySet<FinancialTransaction> financialFeeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, financialFeeTxs.size());

        FinancialTransaction originalFeeTxn = financialFeeTxs.get(0);

        DomainEntitySet<FinancialTransaction> financialTaxTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, financialTaxTxs.size());

        FinancialTransaction originalTaxTxn = financialTaxTxs.get(0);

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(originalFeeTxn.getId().toString(), originalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(originalTaxTxn.getId().toString(), originalTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for Employer Redebit Transaction
        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        // verify payroll status and company status before adding repayment tx
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        Collection<OnHoldReason> onHolds = company.getCurrentOnHoldReasons();

        assertEquals("Number of OnHolds", 0, onHolds.size());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        DomainEntitySet<FinancialTransaction> canceledRedebits = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        PayrollServices.commitUnitOfWork();

        //Ensure the wire was properly created
        assertEquals("Number of Completed Financial Txs", 1, financialTxs.size());

        FinancialTransaction erDDRedb = financialTxs.get(0);

        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), erDDRedb.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("777.77"), erDDRedb.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Completed, erDDRedb.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.Wire, erDDRedb.getSettlementTypeCd());
        assertEquals("Number of Cancelled Financial Txs", 1, canceledRedebits.size()); //Ensure the ACH redebit was cancelled

        FinancialTransaction erDDRedbACH = canceledRedebits.get(0);

        assertEquals("Payroll Run Id ", payrollRun.getSourcePayRunId(), erDDRedbACH.getPayrollRun().getSourcePayRunId());
        assertEquals("Financial Transaction Amount ", new SpcfMoney("777.77"), erDDRedbACH.getFinancialTransactionAmount());
        assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, erDDRedbACH.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Settlement Type ", SettlementType.ACH, erDDRedbACH.getSettlementTypeCd());

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        Assert.assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);
            assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testZeroDollarTotal() {

        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerDdDebit txns", 1, financialTxns.size());

        FinancialTransaction originalTxn = financialTxns.get(0);

        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeTxns.size());

        FinancialTransaction feeTxn = feeTxns.get(0);

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxTxns.size());

        FinancialTransaction taxTxn = taxTxns.get(0);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(new RedebitImpoundDTO(originalTxn.getId().toString(), new SpcfMoney("0.00"),
                new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        collectionOfRedebitImpounds.add(new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0.00"),
                new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));
        collectionOfRedebitImpounds.add(new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.00"),
                new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", !processResult.isSuccess());
        assertEquals("Number of messages", 1, processResult.getMessages().size());
        assertEquals("Error code", "283", processResult.getMessages().get(0).getMessageCode()); // amount not positive

        // now try it with one of the amounts > 0
        collectionOfRedebitImpounds.get(0).setAmount(new SpcfMoney("0.01"));
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();
        assertSuccess("one amount non-zero and one amount zero", processResult);
        DomainEntitySet<FinancialTransaction> createdRedebits = (DomainEntitySet<FinancialTransaction>) processResult.getResult();
        assertEquals("number of redebits created", 1, createdRedebits.size());
        assertEquals("amount of redebit", new SpcfMoney("0.01"), createdRedebits.get(0).getFinancialTransactionAmount());
    }

    @Test
    public void testRepaymentRecordPendingRedebitPayStatus_TaxCompany() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2007, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2007, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2007, 9, 4);

        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2007-09-11"));

        DataLoadServices.setPSPDate(2007, 9, 7);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2007, 9, 8);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnTxns = Application.refresh(payrollRun).getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerFeeDebit);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.returnTxns(returnTxns, "R02", "Non-NSF Return");

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        PSPDate.setPSPTime("20071009000000");

        //Just redebit the payroll txn
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        String originalTxnId = originalTxn.getId().toString();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxnId);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(), company.getSourceCompanyId(), collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");

        redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("65.00"));
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxnId);

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);
        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(), company.getSourceCompanyId(), collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();

        SpcfDecimal ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                payrollRun.getSourcePayRunId(),
                company).negate();

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(),
                new SpcfMoney(ledgerBalance), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        Application.refresh(payrollRun);

        Assert.assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction redebitFinTxn : financialTxs) {
            Assert.assertEquals("Financial Transaction Amount ", ledgerBalance,
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());

            Assert.assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        Assert.assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            org.junit.Assert.assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify there is no active ACHReject onhold reason
        company = Application.findById(Company.class, company.getId());
        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(ServiceSubStatusCode.AchRejectOther);
        assertEquals("AchRejectOther is expired", null, onHoldReason);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonACHRedebitProcess_removeHold() {

        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        //Add Non-ACH Redebit for EmployerDDDebit Returned
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DataLoadServices.addTaxService(company);
        PayrollServices.beginUnitOfWork();
        
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);

        DomainEntitySet<FinancialTransaction> feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeTxs.size());

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxTxns.size());

        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(originalTxn.getId().toString(), originalTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO erFees = new RedebitImpoundDTO(feeTxs.get(0).getId().toString(), feeTxs.get(0).getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO serviceSales = new RedebitImpoundDTO(taxTxns.get(0).getId().toString(), taxTxns.get(0).getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(erFees);
        collectionOfRedebitImpounds.add(serviceSales);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBDT, company.getSourceCompanyId(), collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        //Persistence check for EmployerDDRedebit Transaction
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        feeTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Financial Txs", 1, financialTxs.size());

        for (FinancialTransaction redebitFinTxn : financialTxs) {
            Assert.assertEquals("Payroll Run Id ", "BatchTest09", redebitFinTxn.getPayrollRun().getSourcePayRunId());
            Assert.assertEquals("Financial Transaction Amount ", originalTxn.getFinancialTransactionAmount(),
                    redebitFinTxn.getFinancialTransactionAmount());
            Assert.assertEquals("Financial Transaction State ", TransactionStateCode.Completed,
                    redebitFinTxn.getCurrentTransactionState().getTransactionStateCd());

            Assert.assertEquals("Original Transaction ", payrollRedebit.getOriginalFinancialTxId(),
                    redebitFinTxn.getOriginalTransaction().getId().toString());
        }

        // check for EmployerFeeRedebit Transaction is complete
        assertEquals("Number of Financial Txs", 1, feeTxs.size());

        //Persistence check for ServiceSalesAndUseTaxRedebit Transaction
        assertEquals("Number of Financial Txs", 1, taxTxns.size());

        //Persistence check for Transaction Return Status
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of Returned Financial Txs", 1, finTxns.size());

        for (FinancialTransaction finalcialTransaciton : finTxns) {
            TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(
                    finalcialTransaciton).get(0);

            assertEquals("Transaction Return Status Cd ", TransactionReturnStatusCode.Resolved,
                    txnReturn.getReturnStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NonAchPaymentReceived, CompanyEventStatus.Active, null, null);

        assertEquals("Number of NonAchPaymentReceived events", 1, companyEvents.size());
        // verify ACHReject onhold reason is expired
        company = Application.findById(Company.class, company.getId());
        Collection<OnHoldReason> onHoldReasons = company.getExpiredOnHoldReasons();
        assertEquals("Expired on Hold reasons", 1, onHoldReasons.size());
        DomainEntitySet<CompanyEvent> companySyncEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PSPToAS400HoldRemoveSync, CompanyEventStatus.Active, null, null);
        assertEquals("Number of PSPToAS400HoldRemoveSync events", 1, companySyncEvents.size());
        PayrollServices.commitUnitOfWork();
        
    }

    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
    }
}
