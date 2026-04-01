package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.RefundERFraudOrEscalationDataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Sep 17, 2008
 * Time: 1:50:47 PM
 */
public class AddEmployerFraudOrEscalationRefundCoreTests {
    @Before
    public void runBeforeEachTest() {
        RefundERFraudOrEscalationDataLoader.before();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void ach_happy() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.ACH, 0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("process", result);
        checkTransactions(dtoList, payroll);
    }

    @Test
    public void wire_happy() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfMoney ledgerBefore = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), payroll.getCompany());
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        SpcfMoney ledgerAfter = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), payroll.getCompany());
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("process", result);
        assertEquals("ledger balance before refunds", new SpcfMoney("0"), ledgerBefore);
        assertTrue("ledger balance changed after refunds", !ledgerAfter.equals(ledgerBefore));
        checkTransactions(dtoList, payroll);
    }

    @Test
    public void wire_happyPartials() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        // reduce the amounts of all the DTOs to something less than their full amounts
        SpcfMoney unrefunded = new SpcfMoney("0.01"); // we'll reduce the refund amounts by this much
        for (ERRefundDTO dto : dtoList) {
            SpcfDecimal reduced = dto.getFinancialTxAmt().subtract(SpcfDecimal.createInstance(0.01));
            dto.setFinancialTxAmt(new SpcfMoney(reduced));
        }

        // create those refunds
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("process partial refunds", result);
        checkTransactions(dtoList, payroll);

        // now do it all again, refunding whatever is left (the amount of the reduction)

        PayrollServices.beginUnitOfWork();
        dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        // make sure the collected amounts have shrunk appropriately
        for (ERRefundDTO dto : dtoList) {
            assertEquals("debit amount after partial refund", unrefunded, dto.getFinancialTxAmt());
        }

        // create those refunds
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("process refunds of remaining amounts", result);
        checkTransactions(dtoList, payroll);
    }

    @Test
    public void wire_badInputFT() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        // make the second input FT bad
        dtoList.get(1).setFinancialTxId("deadbeef-dead-beef-dead-beefdeadbeef");

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        assertTrue("process failed", !result.isSuccess());
        assertEquals("Validation error code", "5003", result.getMessages().get(0).getMessageCode()); // 5003 no entity with given id
        assertTrue("Invalid value is the refund amount", result.getMessages().get(0).getMessage().indexOf("FinancialTransaction") >= 0);
    }

    @Test
    public void wire_badDatePast() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, -46);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        assertTrue("process failed", !result.isSuccess());
        assertEquals("Validation error code", "271", result.getMessages().get(0).getMessageCode()); // 271 = settlement date too far in the past
    }

    @Test
    public void wire_badDateFuture() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, +1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        assertTrue("process failed", !result.isSuccess());
        assertEquals("Validation error code", "266", result.getMessages().get(0).getMessageCode()); // 266 = settlement date too far in the future
    }

    @Test
    public void wire_badAmount() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        // refund is too much
        SpcfDecimal amt = dtoList.get(0).getFinancialTxAmt();
        amt = amt.add(SpcfDecimal.createInstance(1.00));
        dtoList.get(0).setFinancialTxAmt(new SpcfMoney(amt));

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        assertTrue("process failed", !result.isSuccess());
        assertEquals("Validation error code", "5001", result.getMessages().get(0).getMessageCode());
        assertTrue("Invalid value is the refund amount", result.getMessages().get(0).getMessage().indexOf("Refund Amount") >= 0);

        // refund is too little
        dtoList.get(0).setFinancialTxAmt(new SpcfMoney("0"));

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        System.out.println(result.toString());
        assertTrue("process failed", !result.isSuccess());
        assertEquals("Validation error code", "283", result.getMessages().get(0).getMessageCode()); // specific to amounts: The amount must be a non-zero, positive number.
    }

    @Test
    public void ach_pendingRefunds() {
        PayrollRun payroll = RefundERFraudOrEscalationDataLoader.loadData("20070831000000");

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.ACH, 0); // must be ACH to get "pending" refund
        PayrollServices.commitUnitOfWork();

        // use the first DTO by itself to create a refund (successfully)...
        // remove and save the second DTO for use in a second attempt, which should fail
        assertTrue("DTOs for at least 2 refundable transactions", dtoList.size()>=2);
        Iterator<ERRefundDTO> it = dtoList.iterator();;
        it.next(); // disregard first item
        ERRefundDTO secondDTO = it.next();
        dtoList.remove(secondDTO);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("creating refund as precondition", result);

        // now try it with the second DTO... it should fail because now there's a pending refund (from our first attempt)
        dtoList.clear();
        dtoList.add(secondDTO);
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(RefundERFraudOrEscalationDataLoader.SRC_SYS_CODE, RefundERFraudOrEscalationDataLoader.SRC_COMPANY_ID, dtoList);
        PayrollServices.commitUnitOfWork();

        assertTrue("process failed", ! result.isSuccess());
        assertEquals("Validation error code", "1055", result.getMessages().get(0).getMessageCode());
    }

    @Test
    public void testACHRefundTax() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 5, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addEEBankAccounts(company);

        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-20"));

        DataLoadServices.setPSPDate(2012, 5, 17);
        DataLoadServices.runOffload();

        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payrollRun, SettlementTypeDTO.ACH, 0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoList));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerFraudOrEscalationRefundCredit).find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("2.90"))));
        assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerFraudOrEscalationRefundCredit).find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("2.00"))));
        assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxFraudOrEscalationRefundCredit).find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("2476.00"))));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void wire_afterReturn() {
        // load all necessary data
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payroll = PayrollRun.findPayrollRun(company, "BatchTest09");
        Application.commitUnitOfWork();

        // offload the redebit(s)
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> createdRedebits = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);
        FinancialTransaction ddRedebit = createdRedebits.get(0);
        MoneyMovementTransaction mmt = ddRedebit.getMoneyMovementTransaction();
        PSPDate.setPSPTime(mmt.getInitiationDate());
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        // post-offload processing
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions post = new ProcessACHTransactions();
        post.process(PSPDate.getPSPTime());
        ddRedebit = Application.refresh(ddRedebit);
        TransactionStateCode ddRedebitState = ddRedebit.getCurrentTransactionState().getTransactionStateCd();
        PayrollServices.commitUnitOfWork();
        assertEquals(TransactionStateCode.Completed, ddRedebitState);

        PayrollServices.beginUnitOfWork();
        List<ERRefundDTO> dtoList = buildDTOs(payroll, SettlementTypeDTO.Wire, 0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfMoney ledgerBefore = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), payroll.getCompany());
        ProcessResult result = PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoList);
        SpcfMoney ledgerAfter = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable, payroll.getSourcePayRunId(), payroll.getCompany());
        PayrollServices.commitUnitOfWork();

        PayrollServicesTest.assertSuccess("process", result);
        assertTrue("ledger balance changed after refunds", !ledgerAfter.equals(ledgerBefore));
        checkTransactions(dtoList, payroll);
    }

    //
    // helpers
    //

    static private List<ERRefundDTO> buildDTOs(PayrollRun pPayroll, SettlementTypeDTO pSettlementType, int pNonAchSettlementOffset) {
        // set the transaction date to "today" plus the offset
        SpcfCalendar txDate = PSPDate.getPSPTime();
        txDate.addDays(pNonAchSettlementOffset);

        // find all debit transactions against the employer and summarize each
        pPayroll = Application.refresh(pPayroll);
        HashMap<FinancialTransaction, SpcfMoney> debits = new HashMap<FinancialTransaction, SpcfMoney>();
        debits.putAll(pPayroll.getCollectedDDAmount());
        debits.putAll(pPayroll.getCollectedTaxAmount());
        debits.putAll(pPayroll.getCollectedFeeAmounts());
        debits.putAll(pPayroll.getCollectedSalesTaxAmounts());

        // build DTOs to refund each
        List<ERRefundDTO> dtoList = new ArrayList<ERRefundDTO>();
        for (Map.Entry<FinancialTransaction, SpcfMoney> entry : debits.entrySet()) {
            FinancialTransaction ftDebit = entry.getKey();
            if(ftDebit.getFinancialTransactionAmount().isLessThanEqualTo(SpcfMoney.ZERO)) {
                continue;
            }
            SpcfMoney amtCollected = entry.getValue();

            // the amount is the collected amount, modified (+/-) by the input "excess" amount
            ERRefundDTO dto = new ERRefundDTO();
            dto.setFinancialTxId(ftDebit.getId().toString());
            dto.setFinancialTxAmt(new SpcfMoney(amtCollected));
            dto.setTxDate(new DateDTO(txDate));
            dto.setSettlementType(pSettlementType);

            dtoList.add(dto);
            System.out.println("Debit FT ..." + ftDebit.getId().toString().substring(32) + " max refund amount is $" + amtCollected);
        }

        return dtoList;
    }

    static private void checkTransactions(Collection<ERRefundDTO> pDTOs, PayrollRun pPayrollRun) {
        MoneyMovementTransaction mmt = null;
        for (ERRefundDTO dto : pDTOs) {
            // get FTs related to the one identified in the DTO...
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> related = new DomainEntitySet<FinancialTransaction>();
            FinancialTransaction debitFT = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(dto.getFinancialTxId()));
            debitFT.getRelatedTransactions(related);
            PayrollServices.commitUnitOfWork();

            // find the most-recently-created EmployerFraudOrEscalationRefundCredit among them, if any is there
            FinancialTransaction refundFT = null;
            for (FinancialTransaction relatedFT : related) {
                if (relatedFT.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFraudOrEscalationRefundCredit) {
                    if (refundFT == null || relatedFT.getCreatedDate().after(refundFT.getCreatedDate())) {
                        refundFT = relatedFT;
                    }
                }
            }

            // make sure it's right
            String prefix = debitFT.getTransactionType().getTransactionTypeCd().toString() +
                    "..." + debitFT.getId().toString().substring(32);
            assertTrue(prefix + " refund FT created", refundFT != null);
            assertEquals(prefix + " refund FT related to correct payroll", pPayrollRun.getId().toString(), refundFT.getPayrollRun().getId().toString());
            assertTrue(prefix + " refund FT has related FT", refundFT.getOriginalTransaction() != null);
            assertEquals(prefix + " refund FT related to correct debit", dto.getFinancialTxId(), refundFT.getOriginalTransaction().getId().toString());
            assertEquals(prefix + " refund FT type", TransactionTypeCode.EmployerFraudOrEscalationRefundCredit, refundFT.getTransactionType().getTransactionTypeCd());
            assertEquals(prefix + " refund FT amount", dto.getFinancialTxAmt(), refundFT.getFinancialTransactionAmount());
            assertEquals(prefix + " refund FT settlement type", ProcessesToDTO.getDomainSettlementType(dto.getSettlementType()), refundFT.getSettlementTypeCd());
            if (dto.getSettlementType() != SettlementTypeDTO.ACH) {
                assertEquals(prefix + " created FT settlement date", DateDTO.convertToSpcfCalendar(dto.getTxDate()), refundFT.getSettlementDate().toLocal());
            }
            else {
                // check the MMT... (first time through this loop, save the MMT)
                if (mmt == null) {
                    mmt = refundFT.getMoneyMovementTransaction();
                }
                assertEquals(prefix + " refund FT has correct MMT", mmt.getId(), refundFT.getMoneyMovementTransaction().getId());
            }

            System.out.println("Refund FT ..." + refundFT.getId().toString().substring(32) + " for $" + refundFT.getFinancialTransactionAmount() + " ok");
        }
    }
}
