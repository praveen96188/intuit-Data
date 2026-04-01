package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 9:00:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ACHReturnsDataLoader {

    private static SpcfLogger logger = SpcfLogManager.getLogger(ACHReturnsDataLoader.class);

    public static final String PIN = "test1234!";

    public static Company1Dataloader c1dl;

    private static CompanyQB1DataLoader c4dl;
    private static CompanyQB2DataLoader cQBDTd1;

    public static void loadData5AgentCancels1CheckCancelsRefund() {
        /****Create er and payroll*****/
        Application.beginUnitOfWork();
        loadDataHappyPathNotBackdated();
        Application.commitUnitOfWork();

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /***Rep cancels one paycheck****/
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());

        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest05");
        txnCancelDTO.setAgentCancel(true);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult txnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);
        // Remove Agent from Principal
        Application.commitUnitOfWork();

        assertSuccess(txnProcResult);

        /*****Rep cancels ER refund****/
        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> refundTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdRefundCredit, null, null,
                TransactionStateCode.Created);
        Application.commitUnitOfWork();

        assertEquals("One refund txn", 1, refundTxns.size());
        FinancialTransaction refundTxn = refundTxns.get(0);

        Application.beginUnitOfWork();
        ProcessResult cancelTxnProcResult = PayrollServices.financialTransactionManager.cancelTransaction(SourceSystemCode.QBOE, "1234567", refundTxn.getId().toString());
        Application.commitUnitOfWork();

        assertSuccess(cancelTxnProcResult);

        /**********Offload remaining EE txn******/
        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }


    public static SpcfUniqueId loadQBDTPayrollOffload(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        return loadQBDTPayrollOffloaded(assetItemNumber, offloadGroup);

    }

    public static void loadData2PayRunsERNSFsAgentCancels2nd() {
        Application.beginUnitOfWork();

        //Insert one payroll for company1
        loadDataHappyPath();

        //Insert a second payroll for company1
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-09-12"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);

        // make sure the fee, added during payroll-submission, gets saved
        CompanyBankAccount cba = CompanyBankAccount
                .findActiveCompanyBankAccount(payrollRun.getCompany());
        Application.commitUnitOfWork();

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload er txn for second payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070905000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return the debit from the first payroll
        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.findFinancialTransactions(company, "BatchTest05", null, null, null, TransactionTypeCode.EmployerDdDebit, null, null, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        //Set Current Principal as agent
        PayrollServices.beginUnitOfWork();
        PspPrincipal principal = Application.getCurrentPrincipal();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        //Rep cancels all transactions in the 2nd payroll
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        txnCancelDTO.setSourcePaycheckIdList(null);
        txnCancelDTO.setSourcePayrollRunId("BatchTest002");
        Application.beginUnitOfWork();
        ProcessResult cancelTxnProcResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", txnCancelDTO);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        Application.commitUnitOfWork();

        assertSuccess(cancelTxnProcResult);
    }

    public static void loadData2DayERNSFsWireRepayment() {
        ACHReturnsDataLoader.loadData2DayERNSFs();

        Application.beginUnitOfWork();

        //Offload the redebit
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create a wire
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");

        DomainEntitySet<FinancialTransaction> returnedDdFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Returned);
        Assert.assertEquals("Number of Returned EmployerDdDebit FTs", 1, returnedDdFTs.size());

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("180.00"));
        redebitDTO.setOriginalFinancialTxId(returnedDdFTs.get(0).getId().toString());
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 8, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), collectionOfRedebitImpounds);

        Application.commitUnitOfWork();
        assertSuccess(processResult);
    }

    /**
     * Load a company that has a 2 day funding model, and add a payroll for that company
     * Offload the payroll (debits and credits go since the company is on 2 day)
     * Create an NSF for the ER DD DB
     * Create an R02 for one of the EE DD Credits
     *
     * @return The Transaction Returns for the ER DD DB and the EE DD CR
     */
    public static void loadDataForEEReturnTransferReturn() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee = Employee.findEmployee(company, "EE1");

        DomainEntitySet<FinancialTransaction> eeFinTxns = FinancialTransaction.findFinancialTransactions(
                company, null, employee, null, null, TransactionTypeCode.EmployeeDdCredit, null, null,
                TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R02",
                "This is an NSF description");
        returnList.addAll(persistTransactionReturns(eeFinTxns, "R02", "This is a bad ba desc"));
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of EEDDCR EX txns", 1, eeFinTxns.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();

            TransactionReturnHandler returnHandler = TransactionReturnHandler.
                    getTransactionReturnHandler(currRet);

            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }
    }

    /**
     * Load a company that has a 2 day funding model, and add a payroll for that company
     * Offload the payroll (debits and credits go since the company is on 2 day)
     * Create an NSF for the ER DD DB and handle it
     *
     * @return
     */
    public static void loadData2DayERNSFs() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(12);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");
    }

    public static void loadData2DayERNSFsOffloadRedebitAndReturnFee() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");

        DomainEntitySet<FinancialTransaction> returnedFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        Assert.assertEquals("Number of Returned EmployerFeeDebit FTs", 1, returnedFTs.size());

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(new SpcfMoney("75.00"));
        redebitDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 7, SpcfTimeZone.getLocalTimeZone())));
        redebitDTO.setOriginalFinancialTxId(returnedFTs.get(0).getId().toString());
        redebitDTO.setSettlementType(SettlementTypeDTO.Wire);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), collectionOfRedebitImpounds);

        Application.commitUnitOfWork();

        assertSuccess(processResult);

    }


    /**
     * Load a company that has a 2 day funding model, and add a payroll for that company
     * Offload the payroll (debits and credits go since the company is on 2 day)
     * Create a generic return for the ER DD DB and handle it
     *
     * @return
     */
    public static void loadData2DayERGenericReturn() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        Application.commitUnitOfWork();
        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");
    }

    public static void loadData2DayERGenericRetAgentWritesOff() {
        loadData2DayERGenericReturn();
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        ProcessResult procResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        Application.commitUnitOfWork();

        assertSuccess(procResult);

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    public static void loadData2DayCompanyRequests1TxnReversed() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        //Reverse single ee txn from payroll
        c1dl.reverseSingleTransactionInPayroll("BatchTest05", "EEBA2PS1");

        //Offload the reversal & fee
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    public static void loadData2DayCompanyRequests1TxnReversedFeeReturned(String pReturnCode, String pReturnDesc) {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        //Reverse single ee txn from payroll
        c1dl.reverseSingleTransactionInPayrollChargeFee("BatchTest05", "EEBA2PS1");

        //Offload the reversal & fee
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return the fee
        Application.beginUnitOfWork();
        FinancialTransaction reversalFee = null;
        DomainEntitySet<FinancialTransaction> finTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    reversalFee = currTxn;
                }
            }
        }

        finTxnsToReturn.add(reversalFee);

        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();
        assertNotNull("Found reversal fee", reversalFee);

        DataLoadServices.returnTxns(finTxnsToReturn, pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBDTPayrollReturnedRedebitReturned(String pReturnCode, String pReturnDesc) {
        return loadQBDTPayrollReturnedRedebitReturned(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBDTPayrollReturnedRedebitReturned(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturned(assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);

        //Offload the redebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        //Return the redebit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);
        Application.commitUnitOfWork();

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            currRet = Application.findById(TransactionReturn.class, currRet.getId());
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(Application.refresh(currRet));
            Application.commitUnitOfWork();
        }

        return returnList.get(0);

    }

    public static TransactionReturn loadQBDTPayrollReturnedWireRedebitReturned(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturned(pReturnCode, pReturnDesc);
        return wireAndReturnRedebitQBDT(SourceSystemCode.QBDT, "8574536",AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), "20071009000000", pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBOEPayrollReturnedWireFeeDebitReturned(String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturned(pReturnCode, pReturnDesc);
        return wireAndReturnFeeDebitQBOE(pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBOEPayrollReturnedWireRedebitReturned(String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturned(pReturnCode, pReturnDesc);
        return wireAndReturnRedebitQBOE(pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadPayrollRetAddRedebitAddWireRedebitRetNonNSF(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadPayrollReturnedAddManualRedebit(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);
        return wireAndReturnRedebitQBDTSymphony(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBOEPayrollRetAddRedebitAddWireRedebitRetNonNSF(String pReturnCode, String pReturnDesc) {
        return loadQBOEPayrollRetAddRedebitAddWireRedebitRetNonNSF(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBOEPayrollRetAddRedebitAddWireRedebitRetNonNSF(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturnedAddManualRedebit(assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);
        return wireAndReturnRedebitQBOE(pReturnCode, pReturnDesc);
    }

    public static TransactionReturn loadQBDTPayrollRetAddRedebitAddWireRedebitRetNonNSF() {
        loadQBDTPayrollReturnedAddManualRedebit("R02", "Non-NSF return");
        return wireAndReturnRedebitQBDT(SourceSystemCode.QBDT, "8574536",AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), "20071009000000", "R02", "Non-NSF");
    }

    private static TransactionReturn wireAndReturnRedebit(String pReturnCode, String pReturnDesc) {
        return wireAndReturnRedebit(SourceSystemCode.QBOE, c4dl.getCompany1().getCompanyId(), OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    private static TransactionReturn wireAndReturnRedebitQBDTSymphony(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        return wireAndReturnRedebitQBDT(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup,"20070905000000", pReturnCode, pReturnDesc);
    }

    private static TransactionReturn wireAndReturnRedebitQBDT(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String offloadTime, String pReturnCode, String pReturnDesc) {
        //Offload the redebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(offloadTime);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        //Wire the balance
        Application.beginUnitOfWork();

        SpcfCalendar wireDate = PSPDate.getPSPTime();

        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);
        assertEquals("Number of EmployerDdRedebit fin txns", 1, c1FinTxns.size());
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("777.77"), new DateDTO(wireDate), SettlementTypeDTO.Wire);
        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);

        if(assetItemNumber != AssetItemNumber.DIY_USAGE_BILLING_MONTHLY){
            DomainEntitySet<FinancialTransaction> c1FeeFinTxns = FinancialTransaction
                    .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                            TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

            DomainEntitySet<FinancialTransaction> c1TaxFinTxns = FinancialTransaction
                    .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                            TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Executed);

            assertEquals("Number of EmployerFeeRedebit fin txns", 1, c1FeeFinTxns.size());
            assertEquals("Number of ServiceSalesAndUseTaxRedebit fin txns", 1, c1TaxFinTxns.size());

            RedebitImpoundDTO feeRedebitImpoundDTO = new RedebitImpoundDTO(c1FeeFinTxns.get(0).getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2), new DateDTO(wireDate), SettlementTypeDTO.Wire);
            RedebitImpoundDTO taxRedebitImpoundDTO = new RedebitImpoundDTO(c1TaxFinTxns.get(0).getId().toString(), new SpcfMoney("0.09"), new DateDTO(wireDate), SettlementTypeDTO.Wire);
            redebitImpoundDTOs.add(feeRedebitImpoundDTO);
            redebitImpoundDTOs.add(taxRedebitImpoundDTO);
        }

        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(sourceSystemCode, sourceCompanyId, redebitImpoundDTOs);
        assertSuccess(procResult);

        //Return the redebit
        PSPDate.setPSPTime("20071010000000");

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
        }

        PayrollServices.commitUnitOfWork();

        return returnList.get(0);
    }

    private static TransactionReturn wireAndReturnRedebit(SourceSystemCode sourceSystemCode, String sourceCompanyId, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        //Offload the redebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        //Wire the balance
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> c1FeeFinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> c1TaxFinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Executed);

        assertEquals("Number of EmployerDdRedebit fin txns", 1, c1FinTxns.size());
        assertEquals("Number of EmployerFeeRedebit fin txns", 1, c1FeeFinTxns.size());
        assertEquals("Number of ServiceSalesAndUseTaxRedebit fin txns", 1, c1TaxFinTxns.size());

        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("777.77"), new DateDTO("2007-10-09"), SettlementTypeDTO.Wire);
        RedebitImpoundDTO feeRedebitImpoundDTO = new RedebitImpoundDTO(c1FeeFinTxns.get(0).getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2), new DateDTO("2007-10-09"), SettlementTypeDTO.Wire);
        RedebitImpoundDTO taxRedebitImpoundDTO = new RedebitImpoundDTO(c1TaxFinTxns.get(0).getId().toString(), new SpcfMoney("0.09"), new DateDTO("2007-10-09"), SettlementTypeDTO.Wire);
        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);
        redebitImpoundDTOs.add(feeRedebitImpoundDTO);
        redebitImpoundDTOs.add(taxRedebitImpoundDTO);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(sourceSystemCode, sourceCompanyId, redebitImpoundDTOs);
        assertSuccess(procResult);

        //Return the redebit
        PSPDate.setPSPTime("20071010000000");

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
        }

        PayrollServices.commitUnitOfWork();

        return returnList.get(0);
    }

    private static TransactionReturn wireAndReturnFeeDebitQBOE(String pReturnCode, String pReturnDesc) {
        //Offload the redebit and NSF fee
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070906171500");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Wire the fee
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());

        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("180.00"), new DateDTO("2007-09-06"), SettlementTypeDTO.Wire);

        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);


        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), redebitImpoundDTOs);
        assertSuccess(procResult);

        //Return the redebit
        PSPDate.setPSPTime("20071010000000");

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);


        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
        }

        PayrollServices.commitUnitOfWork();

        return returnList.get(0);
    }


    private static TransactionReturn wireAndReturnRedebitQBOE(String pReturnCode, String pReturnDesc) {
        //Offload the redebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070905171500");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Wire the balance
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());

        Collection<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        RedebitImpoundDTO payrollRedebitImpoundDTO = new RedebitImpoundDTO(c1FinTxns.get(0).getId().toString(), new SpcfMoney("180.00"), new DateDTO("2007-09-06"), SettlementTypeDTO.Wire);

        redebitImpoundDTOs.add(payrollRedebitImpoundDTO);

        //Return the redebit
        PSPDate.setPSPTime("20071010000000");

        ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), redebitImpoundDTOs);
        assertSuccess(procResult);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);


        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
        }

        PayrollServices.commitUnitOfWork();

        return returnList.get(0);
    }


    public static void loadQBDTPayrollReturnedAddPayrollRedebitR02NonNSF() {
        loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");
    }

    public static void loadQBDTPayrollReturnedAddPayrollRedebit(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturned(pReturnCode, pReturnDesc);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");

        //Just redebit the payroll txn
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
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
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);
    }

    public static void loadQBDTPayrollReturnedAddManualRedebit(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturned(pReturnCode, pReturnDesc);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");

        //Just redebit the payroll txn
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        DomainEntitySet<FinancialTransaction> feeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        DomainEntitySet<FinancialTransaction> taxFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Returned});

        assertEquals("Number of returned EmployerDdDebit txns", 1, financialTxs.size());
        assertEquals("Number of returned EmployerFeeDebit txns", 1, feeFinancialTxs.size());
        assertEquals("Number of returned ServiceSalesAndUseTax txns", 1, taxFinancialTxs.size());

        FinancialTransaction originalTxn = financialTxs.get(0);
        FinancialTransaction originalFeeTxn = feeFinancialTxs.get(0);
        FinancialTransaction originalTaxTxn = taxFinancialTxs.get(0);

        String originalTxnId = originalTxn.getId().toString();
        String originalFeeTxnId = originalFeeTxn.getId().toString();
        String originalTaxTxnId = originalTaxTxn.getId().toString();

        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxnId);

        RedebitImpoundDTO feeRedebitDTO = new RedebitImpoundDTO();
        feeRedebitDTO.setAmount(originalFeeTxn.getFinancialTransactionAmount());
        feeRedebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        feeRedebitDTO.setOriginalFinancialTxId(originalFeeTxnId);

        RedebitImpoundDTO taxRedebitDTO = new RedebitImpoundDTO();
        taxRedebitDTO.setAmount(originalTaxTxn.getFinancialTransactionAmount());
        taxRedebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        taxRedebitDTO.setOriginalFinancialTxId(originalTaxTxnId);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);
        collectionOfRedebitImpounds.add(feeRedebitDTO);
        collectionOfRedebitImpounds.add(taxRedebitDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);
    }

    public static void loadPayrollReturnedAddManualRedebit(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadPayrollReturned(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070905170000");

        //Just redebit the payroll txn
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
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
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(sourceSystemCode, sourceCompanyId, collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);
    }

    public static void loadQBOEPayrollReturnedAddManualRedebit(String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturnedAddManualRedebit(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static void loadQBOEPayrollReturnedAddManualRedebit(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturned(assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070905170000");

        //Just redebit the payroll txn
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
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
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);
    }

    public static String loadQBDTPayrollReturned(String pReturnCode, String pReturnDesc) {
        return loadQBDTPayrollReturned(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static String loadQBDTPayrollReturned(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollOffloaded(assetItemNumber, offloadGroup);
        
        //Return the debit
        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20071008000000");

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        // Execute the return handler
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);

        txnReturn = returnHandler.execute(txnReturn);
        
        String returnBatchId = txnReturn.getReturnBatch().getId().toString();

        Application.commitUnitOfWork();
        
        return returnBatchId;
    }

    public static void loadQBDTPayrollReturned_5Day(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollOffloadedDebit_5Day();

        //Return the debit
        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20071004000000");

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
        }

        Application.commitUnitOfWork();
    }

    public static void loadAndRunQBDTPayrollReturned_5Day(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturned_5Day(pReturnCode, pReturnDesc);

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    public static void loadPayrollReturned(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadPayrollOffloaded(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);

        //Return the debit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070905170000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnCode, pReturnDesc);
    }

    public static void loadQBOEPayrollReturned(String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollReturned(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static void loadQBOEPayrollReturned(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBOEPayrollOffloaded();

        //Return the debit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070905170000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnCode, pReturnDesc);
    }

    public static SpcfUniqueId loadQBDTPayrollOffloaded() {
        return loadQBDTPayrollOffloaded(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static SpcfUniqueId loadQBDTPayrollOffloaded(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        Application.beginUnitOfWork();
        loadDataHappyPathQBDT(assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);
        SpcfUniqueId batchId = offloader.getOffloadBatch().getId();

        return batchId;
    }

    public static SpcfUniqueId loadQBDTPayrollOffloadedDebit_5Day() {
        Application.beginUnitOfWork();
        loadDataHappyPathQBDT_5Day();
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        SpcfUniqueId batchId = offloader.getOffloadBatch().getId();

        return batchId;
    }

    public static SpcfUniqueId loadPayrollOffloaded(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907171500");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);
        SpcfUniqueId batchId = offloader.getOffloadBatch().getId();

        return batchId;
    }

    public static SpcfUniqueId loadQBOEPayrollOffloaded() {
        return loadPayrollOffloaded(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static SpcfUniqueId loadQBDT2PayrollOffloaded() {
        Application.beginUnitOfWork();
        loadDataHappyPathQBDT2ndCompany();
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        SpcfUniqueId batchId = offloader.getOffloadBatch().getId();

        return batchId;
    }

    public static void loadAndOffloadTwoQBDTCompanies() {
        loadQBDT2PayrollOffloaded();
        loadQBDTPayrollOffloaded();
    }

    public static void loadQBDTPayrollReturnedCBADeactivated(String pReturnCode, String pReturnDesc) {
        loadQBDTPayrollReturnedCBADeactivated(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static void loadQBDTPayrollReturnedCBADeactivated(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        Application.beginUnitOfWork();
        loadDataHappyPathQBDT(assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Application.commitUnitOfWork();

        //Change the CBA, but don't verify it
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO cbaDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        cbaDTO.setCompanyBankAccountID("C1BA1");
        ProcessResult procResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(), cbaDTO, true, true, false);
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        //Return the debit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071008000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnCode, pReturnDesc);
    }

    public static void loadQBDTCompanyRequests1TxnReversed() {
        loadQBDTCompanyRequests1TxnReversed(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static void loadQBDTCompanyRequests1TxnReversed(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        Application.beginUnitOfWork();
        loadDataHappyPathQBDT(assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071008000000");
        PayrollServices.commitUnitOfWork();
        //Reverse single ee txn from payroll
        c4dl.reverseSingleTransactionInPayrollChargeFee("BatchTest09", "EEBA2PS2");

        //Offload the reversal & fee
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);
    }

    public static void loadQBDTCompanyRequests1TxnReversedFeeReturned(String pReturnCode, String pReturnDesc) {
        loadQBDTCompanyRequests1TxnReversedFeeReturned(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static void loadQBDTCompanyRequests1TxnReversedFeeReturned(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {

        loadQBDTCompanyRequests1TxnReversed(assetItemNumber, offloadGroup);

        //Return the fee
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        FinancialTransaction reversalFee = null;
        DomainEntitySet<FinancialTransaction> finTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    reversalFee = currTxn;
                }
            }
        }

        assertNotNull("Found reversal fee", reversalFee);
        finTxnsToReturn.add(reversalFee);
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(finTxnsToReturn, pReturnCode, pReturnDesc);
    }

    public static void loadDataReversalFeeAndPayrollReturned(String pReturnCode, String pReturnDesc) {
        loadQBDTCompanyRequests1TxnReversedFeeReturned(pReturnCode, pReturnDesc);

        //Return the payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, pReturnCode, pReturnDesc);
    }

    public static void loadDataCompRevFeeRetFeeRedRet(String pReturnCode, String pReturnDesc) {
        loadData2DayCompanyRequests1TxnReversedFeeReturned(pReturnCode, pReturnDesc);

        FinancialTransaction returnedFeeFT = null;
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    returnedFeeFT = currTxn;
                }
            }
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(returnedFeeFT.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(returnedFeeFT.getId().toString());

        List<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        redebitImpoundDTOs.add(redebitDTO);
        ProcessResult redebitProcess = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                SourceSystemCode.QBOE,
                "1234567", redebitImpoundDTOs);
        Application.commitUnitOfWork();

        assertSuccess(redebitProcess);

        //Offload the fee redebit
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Return the fee redebit
        Application.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20071002000000");
        assertEquals("Found 1 reversal fee redebit", 1, allFinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(allFinTxns, pReturnCode, pReturnDesc);
    }

    public static void loadQBDTCompRevFeeRetFeeRedRet(String pReturnCode, String pReturnDesc) {
        loadQBDTCompRevFeeRetFeeRedRet(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public static void loadQBDTCompRevFeeRetFeeRedRet(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadQBDTCompanyRequests1TxnReversedFeeReturned(assetItemNumber, offloadGroup, pReturnCode, pReturnDesc);

        FinancialTransaction returnedFeeFT = null;
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    returnedFeeFT = currTxn;
                }
            }
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(returnedFeeFT.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(returnedFeeFT.getId().toString());

        List<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
        redebitImpoundDTOs.add(redebitDTO);
        ProcessResult redebitProcess = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                SourceSystemCode.QBDT,
                "8574536", redebitImpoundDTOs);
        Application.commitUnitOfWork();

        //Offload the fee redebit
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        //Return the fee redebit
        Application.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20071002000000");
        assertEquals("Found 1 reversal fee redebit", 1, allFinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(allFinTxns, pReturnCode, pReturnDesc);
    }

    public static void loadData2DayCompanyPutOnHold1EEReturnCompanyOffHold() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Put the company on hold
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");
        ProcessResult procResult = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE, "1234567", ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        //Return an EE txn
        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company, "EE1");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        PSPDate.setPSPTime("20070910000000");
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");

        Application.beginUnitOfWork();
        PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE, "1234567", ServiceSubStatusCode.Fraud);
        Application.commitUnitOfWork();

    }

    /**
     * Submit a payroll for Company1 and create a non-NSF return for that payroll.  Can only be used after c1dl has already
     * been initialized, such as by calling loadData2DayHappyPath
     */
    public static void loadPayrollOnlyDebitReturn() {
        if (c1dl == null) {
            throw new RuntimeException("METHOD loadPayrollOnlyDebitReturn CAN ONLY BE USED AFTER COMPANY1 HAS BEEN INITED");
        }

        //Put the company back on 5-day

        PayrollRunDTO payroll2 = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-09-21"));
        Application.beginUnitOfWork();
        c1dl.updateTo5DayFundingModel();
        c1dl.persistPayrollRun(payroll2);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> c1Payroll2FinTxn = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (currTxn.getPayrollRun().getSourcePayRunId().equals("BatchTest002")) {
                c1Payroll2FinTxn.add(currTxn);
            }
        }

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1Payroll2FinTxn.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1Payroll2FinTxn, "R02", "This is a non-NSF description");
    }

    public static void loadData2Day1EERet() {
        loadData2Day1EERet(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    /**
     * Load company with 2 day funding model, offload, return one of the ee txns
     */
    public static void loadData2Day1EERet(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        Employee employee1 = Employee.findEmployee(company, "EE1");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        PSPDate.setPSPTime("20070910000000");
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");
    }

    /**
     * Load company with 2 day funding model, offload, return one of the ee txns, but don't handle it
     */
    public static DomainEntitySet<TransactionReturn> loadData2Day1EERetDoNotHandle() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company, "EE1");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        return returnList;
    }

    public static DomainEntitySet<TransactionReturn> loadData2DayBothEERetDoNotHandle() {
        return loadData2DayBothEERetDoNotHandle(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    /**
     * Load company with 2 day funding model, offload, return one of the ee txns, but don't handle it
     */
    public static DomainEntitySet<TransactionReturn> loadData2DayBothEERetDoNotHandle(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", null, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 2, c1FinTxns.size());
        assertEquals("Number of returns", 2, returnList.size());

        return returnList;
    }

    /**
     * Load company with 5 day funding model, offload, return one of the er txns
     */
    public static void loadData5Day1ERRet() {
        Application.beginUnitOfWork();
        loadDataHappyPath();
        Application.commitUnitOfWork();

        //Offload er txn
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");
    }

    /**
     * Load company with 5 day funding model, offload, return one of the er txns
     */
    public static void loadData5Day1ERRet_WithStrikes() {
        Application.beginUnitOfWork();
        loadDataHappyPath();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        // Add three strikes
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        Application.commitUnitOfWork();

        //Offload er txn
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");
    }

    public static DomainEntitySet<TransactionReturn> loadData5Day1ERRet_With3StrikesAnd15PayrollCount() {
        Application.beginUnitOfWork();
        loadDataHappyPath();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        // Add three strikes
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        company.getQuickbooksInfo().setAS400PayrollCount(15);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        Application.commitUnitOfWork();

        //Offload er txn
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        return returnList;
    }


    /**
     * Load company with 2 day funding model, offload, return one of the ee txns
     */
//    public static void loadData2Day1EERetRepRefunds() {
//        loadData2Day1EERet();
//
//        RefundDTO refundDTO = new RefundDTO();
//        refundDTO.setSourcePayrollRunId("BatchTest05");
//        refundDTO.setCompanyBankAccountID("123123");
//        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
//        refundDTO.setFinancialTxAmt(new SpcfMoney("150.00"));
//        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 15, SpcfTimeZone.getLocalTimeZone())));
//
//        PayrollServices.beginUnitOfWork();
//        ProcessResult processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
//                SourceSystemCode.QBOE, "1234567", refundDTO);
//        PayrollServices.commitUnitOfWork();
//
//        assertSuccess("addEmployeeReturnRefundTransaction proc result", processResult);
//    }

    /**
     * Load a company that has a 2 day funding model, and add a payroll for that company
     * Offload the payroll (debits and credits go since the company is on 2 day)
     * Create an NSF for the ER DD DB and handle it
     *
     * @return
     */
    public static void loadData2DayERNSFsAgentReversesPayroll() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(7);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        //Load reversals (rep-inited)
        Application.beginUnitOfWork();
        c1dl.reverseEntirePayroll_IntuitInitiated("BatchTest05");
        Application.commitUnitOfWork();
    }

    /**
     * Function to load the data for Bank Account Verification ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForBankAccountVerificationReturnEvent() {
        loadDataHappyPath();

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerVerificationDebit CR txns", 2, financialTransactions.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadQBDTDataForBankAccountVerificationReturnEvent() {
        return loadQBDTDataForBankAccountVerificationReturnEvent(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public DomainEntitySet<TransactionReturn> loadQBDTDataForBankAccountVerificationReturnEvent(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadDataHappyPathQBDT(assetItemNumber, offloadGroup);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerVerificationDebit CR txns", 2, financialTransactions.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadQBDTDataForBankAccountVerificationReturnEvent_BA_not_active() {
        return loadQBDTDataForBankAccountVerificationReturnEvent_BA_not_active(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public DomainEntitySet<TransactionReturn> loadQBDTDataForBankAccountVerificationReturnEvent_BA_not_active(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        DataLoader dataloader = new DataLoader();
        CompanyQB1DataLoader c4dl = new CompanyQB1DataLoader();
        Company company = dataloader.persistCompany(c4dl.getCompany1(), assetItemNumber, offloadGroup);
        persistCompany_Dont_Activate_BA(company, c4dl.getCompany1Service(), offloadGroup);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R02",
                "This is a non-NSF description");

        assertEquals("Number of C1 EmployerVerificationDebit CR txns", 2, financialTransactions.size());

        return returnList;
    }

    /**
     * Function to load the data for Bank Account Verification ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForBankAccountVerificationReturnEvent_BA_not_active() {

        c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(c1dl.getCompany1());
        persistCompany_Dont_Activate_BA(company, c1dl.getCompany1Service());

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerVerificationDebit CR txns", 2, financialTransactions.size());

        return returnList;
    }

    /**
     * Function to load the data for Second NSF ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForSecondNSFReturn() {
        DomainEntitySet<TransactionReturn> returnList = loadDataForFirstNSFReturn();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        returnHandler.execute(transactionReturn);

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        returnList = persistTransactionReturns(c1FinTxns, "R01", "This is an NSF description");

        assertEquals("Number of C1 EmployerDdRedebit CR txns", 1, c1FinTxns.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForRefundReturnEvent(String pReturnCode, String pReturnDesc) {
        return loadDataForRefundReturnEvent(SourceSystemCode.QBOE, "123272727", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDesc);
    }

    public DomainEntitySet<TransactionReturn> loadDataForRefundReturnEvent(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc) {
        loadDataHappyPath(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        addCompany1Payroll3(sourceSystemCode, sourceCompanyId);
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        for (FinancialTransaction eeFinTxn : eeFinancialTxs) {
            eeFinTxn.updateFinancialTransactionState(TransactionStateCode.Cancelled);
        }


        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager.addRefundTransaction(
                sourceSystemCode,
                sourceCompanyId, refundDTO);

        assertSuccess("addRefundTransaction", processResult);

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerDdRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of C1 EmployerDdRefundCredit CR txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for Refund Return ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForRefundReturnEvent() {
        return loadDataForRefundReturnEvent("R02", "This is a non-NSF description");
    }

    public DomainEntitySet<TransactionReturn> loadDataForNSFRefundReturnEvent() {
        return loadDataForNSFRefundReturnEvent(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public DomainEntitySet<TransactionReturn> loadDataForNSFRefundReturnEvent(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        return loadDataForRefundReturnEvent(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup, "R01", "This is an NSF description");
    }

    /**
     * Function to load the data for Generic Debit ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public static DomainEntitySet<TransactionReturn> loadDataForGenericDebitReturn() {
        Application.beginUnitOfWork();
        loadDataHappyPath2Day();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        return returnList;
    }

    /**
     * Function to load the data for Generic Debit ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForGenericDebitReturnForOffloadedDebit() {
        loadDataHappyPath();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setSourcePayrollRunId("BatchTest05");
        refundDTO.setSettlementType(SettlementTypeDTO.ACH);
        refundDTO.setFinancialTxAmt(new SpcfMoney("1705.81"));
        refundDTO.setTxDate(new DateDTO(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone())));

        ProcessResult processResult = PayrollServices.financialTransactionManager
                .addEmployeeReturnRefundTransaction(SourceSystemCode.QBOE,
                        c1dl.getCompany().getSourceCompanyId(), refundDTO);

        assertSuccess("addEmployeeReturnRefundTransaction Process Result ", processResult);

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R02",
                "This is an NSF description");

        assertEquals("Number of EmployerDdDebit EX txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for Fee Debit ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForFeeDebitReturnEvent(String pBankReturnCd, String pDesc) {
        loadDataHappyPath();
        DomainEntitySet<TransactionReturn> returnList = loadDataForFirstNSFReturnOffloadedDebit(pBankReturnCd, pDesc);
        assertTrue("One TransactionReturn", returnList.size() == 1);

        FinancialTransaction feeFT = null;
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn
                .findFinancialTransaction(returnList.get(0));
        for (FinancialTransaction ft : returnedFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit) {
                feeFT = ft;
            }
        }
        assertTrue("TransactionReturn includes a Fee", feeFT != null);

        // make sure this is the one we just added
        assertTrue("Company: ", feeFT.getCompany().equals(c1dl.getCompany()));

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForDDReversalReturn() {
        return loadDataForDDReversalReturn(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }
    /**
     * Function to load the data for Direct Deposit Reversal ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForDDReversalReturn(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadDataForDDReversals(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        return loadDDReversalReturn(sourceSystemCode, sourceCompanyId, "R01", "NSF Return");
    }

    public DomainEntitySet<TransactionReturn> loadDataForDDReversalReturnNonNSF() {
        return loadDataForDDReversalReturnNonNSF(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public DomainEntitySet<TransactionReturn> loadDataForDDReversalReturnNonNSF(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadDataForDDReversals(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        return loadDDReversalReturn(sourceSystemCode, sourceCompanyId, "R02", "Non-NSF Return");
    }

    public DomainEntitySet<TransactionReturn> loadDDReversalReturn(String pReturnCode, String pReturnDesc) {
        return loadDDReversalReturn(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), pReturnCode, pReturnDesc);
    }

    public DomainEntitySet<TransactionReturn> loadDDReversalReturn(SourceSystemCode sourceSystemCode, String sourceCompanyId, String pReturnCode, String pReturnDesc) {

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);
        c1FinTxns.addAll(FinancialTransaction
                .findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Completed));

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode,
                pReturnDesc);

        return returnList;
    }

    public static DomainEntitySet<TransactionReturn> load1DDReversalReturn() {

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);
        c1FinTxns.remove(0);

        assertEquals("Number of reversals", 1, c1FinTxns.size());

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");
        assertEquals("Number of reversal returns", 1, returnList.size());

        return returnList;
    }

    public static DomainEntitySet<TransactionReturn> loadFirstDDReversalReturn() {
        return loadDDReversalReturn("EE1");
    }

    public static DomainEntitySet<TransactionReturn> loadDDReversalReturn(String pEmployeeId) {
        DomainEntitySet<FinancialTransaction> reversalTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);
        c1FinTxns.addAll(FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Completed));
        FinancialTransaction reversalTransaction = null;

        for (FinancialTransaction currReversal : c1FinTxns) {
            if (currReversal.getOriginalTransaction().getPaycheckSplit().getPaycheck().getDDEmployee().getSourceEmployeeId().equals(pEmployeeId)) {
                reversalTransaction = currReversal;
            }
        }
        assertNotNull(reversalTransaction);
        reversalTransactions.add(reversalTransaction);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(reversalTransactions, "R01",
                "This is an NSF description");
        assertEquals("Number of reversal returns", 1, returnList.size());

        return returnList;
    }

    public static DomainEntitySet<TransactionReturn> load2ndDDReversalReturn() {
        return loadDDReversalReturn("EE2");
    }

    public void loadDataForDDReversals() {
        loadDataForDDReversals(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }
    /**
     * Function to load the data for Direct Deposit Reversal ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public void loadDataForDDReversals(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadDataHappyPath(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        String pspTime = null;
        switch (company.getFundingModel().getFundingModelCd()){
            case FundingModel.Codes.TWO_DAY:
                pspTime = "20070904000000";
                break;
            case FundingModel.Codes.FIVE_DAY:
                pspTime = "20070907000000";
                break;
        }
        PSPDate.setPSPTime(pspTime);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        loadDataReversal(sourceSystemCode, sourceCompanyId);

        //Offload reversals
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
    }

    public void loadDataForDDReversals2Day() {
        loadDataForDDReversals2Day(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public void loadDataForDDReversals2Day(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadDataHappyPath2Day(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        loadDataReversal(sourceSystemCode, sourceCompanyId);

        //Offload reversals
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
    }

    public static void loadAndOffloadQBOE() {
        Application.beginUnitOfWork();
        loadDataHappyPath();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    /**
     * Function to load the data for First NSF ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForFirstNSFReturn() {
        loadDataHappyPath();

        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for First NSF ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForFirstNSFReturn_WithStrikes(boolean offloadDebitOnly) {
        loadDataHappyPath();

        // Add three strikes
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 23, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 27, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                company.getSourceCompanyId(), "Strike Reason",
                SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(processResult);


        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        if (!offloadDebitOnly) {
            PSPDate.setPSPTime("20070907000000");
            Application.commitUnitOfWork();

            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            Application.beginUnitOfWork();
        }


        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for First NSF ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> createFirstNSFReturn() {

        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for First NSF ACH Return Event testing for OffloadedDebit PayrollStatus
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForFirstNSFReturnOffloadedDebit(String pBankReturnCd, String pDesc) {
        // submit a payroll
        loadDataHappyPath();

        // offload those transactions
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        // make TransactionReturns for all Executed transactions in the payroll
        PayrollRun payrollRun = PayrollRun.findPayrollRun(c1dl.getCompany(), "BatchTest05");
        com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactionCollection();
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returns = createTransactionReturns(payrollMMTs, pBankReturnCd, pDesc);
        //todo:v2 this is a hack...we need to come up with a uniform way to create txn boundaries for tests
        Application.commitUnitOfWork();

        assertEquals("FTs in PayrollRun", 3, payrollFTs.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size()); // 1 ER DD Debit, 2 (Created) EE DD Credits
        assertEquals("MMTs in PayrollRun", 1, payrollMMTs.size()); // just the one with the Executed ER DD Debit
        assertEquals("TransactionReturns", 1, returns.size());
        Application.beginUnitOfWork();

        return returns;
    }

    /**
     * Function to load the data for Notice Of Change Events testing
     *
     * @param isInvalid               boolean
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @param pSourceSystemCode
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChange(boolean isInvalid, String pReturnCode,
                                                                        String pAchTransactionTypeCode, SourceSystemCode pSourceSystemCode) {
        if (pSourceSystemCode == SourceSystemCode.QBOE) {
            loadDataHappyPath();
            PSPDate.setPSPTime("20070904000000");
            Application.commitUnitOfWork();
        } else {
            loadDataHappyPathQBDT();
            PSPDate.setPSPTime("20071005000000");
            Application.commitUnitOfWork();
        }


        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns;
        if (pSourceSystemCode == SourceSystemCode.QBOE) {
            c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        } else {
            c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        }

        DomainEntitySet<TransactionReturn> returnList;

        if (!isInvalid) {
            if (pReturnCode.equals("C05")) {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
            } else {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
            }
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "                             ");
        }

        assertEquals("Number of C1 EmployerDdDebit CR txns", 1, c1FinTxns.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChange(String pReturnCode, String pReturnDesc, SourceSystemCode pSourceSystemCode) {
        if (pSourceSystemCode == SourceSystemCode.QBOE) {
            loadDataHappyPath();
            PSPDate.setPSPTime("20070904000000");
            Application.commitUnitOfWork();
        } else {
            loadDataHappyPathQBDT();
            PSPDate.setPSPTime("20071005000000");
            Application.commitUnitOfWork();
        }

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns;
        if (pSourceSystemCode == SourceSystemCode.QBOE) {
            c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        } else {
            c1FinTxns = FinancialTransaction
                    .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                            TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        }

        DomainEntitySet<TransactionReturn> returnList = returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);

        assertEquals("Number of C1 EmployerDdDebit CR txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for Notice Of Change Events testing
     *
     * @param isInvalid               boolean
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> createNoticeOfChange(boolean isInvalid, String pReturnCode,
                                                                   String pAchTransactionTypeCode) {
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList;

        if (!isInvalid) {
            if (pReturnCode.equals("C05")) {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
            } else {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
            }
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "                             ");
        }

        assertEquals("Number of C1 EmployerDdDebit CR txns", 1, c1FinTxns.size());

        return returnList;
    }


    /**
     * Function to load the data for Savings Account Type to test the Notice Of change events
     *
     * @param isInvalid               boolean
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChangeForSavingsAccounttype(boolean isInvalid,
                                                                                             String pReturnCode,
                                                                                             String pAchTransactionTypeCode) {
        loadDataHappyPath();


        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);

        DomainEntitySet<TransactionReturn> returnList;

        if (!isInvalid) {
            if (pReturnCode.equals("C05")) {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
            } else {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
            }
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "                             ");
        }

        BankAccount bankAccount = c1FinTxns.get(0).getNonIntuitBankAccount();
        bankAccount.updateBankAccountTypeCd(BankAccountType.Savings);
        bankAccount.updateACHBankAccountTypeCd(ACHBankAccountType.Savings);

        assertEquals("Number of C1 EmployerDdDebit CR txns", 1, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for Employee Bank Account Transaction Returns to test the Notice of Change events
     *
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChangeEmployeeBankAccount(String pReturnCode,
                                                                                           String pAchTransactionTypeCode) {
        loadDataHappyPath();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);

        DomainEntitySet<TransactionReturn> returnList;
        if (pReturnCode.equals("C05")) {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
        }

        assertEquals("Number of C1 EmployeeDdCredit CR txns", 2, c1FinTxns.size());

        return returnList;
    }

    /**
     * Function to load the data for Payee Bank Account Transaction Returns to test the Notice of Change events
     *
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChangePayeeBankAccount(String pReturnCode,
                                                                                        String pAchTransactionTypeCode) {
        return loadDataForNoticeOfChangePayeeBankAccount(pReturnCode, pAchTransactionTypeCode, null);
    }

    /**
     * Function to load the data for Payee Bank Account Transaction Returns to test the Notice of Change events
     *
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @param pReturnDesc String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForNoticeOfChangePayeeBankAccount(String pReturnCode,
                                                                                        String pAchTransactionTypeCode,
                                                                                        String pReturnDesc) {

        loadPayrollRunForBPACHReturnTest();
        PayrollServices.beginUnitOfWork();
        String sourceCompanyId = "123272727";
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList;
        if (pReturnCode.equals("C05")) {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
        } else {
            if (pReturnDesc == null) {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
            } else {
                returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);
            }

        }
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of C1 EmployeeDdCredit CR txns", 28, c1FinTxns.size());

        return returnList;
    }


     public DomainEntitySet<TransactionReturn> loadDataForSingleNoticeOfChangePayeeBankAccount(String pReturnCode,
                                                                                               String pAchTransactionTypeCode) {
        loadPayrollRunForBPACHReturnTest();
        PayrollServices.beginUnitOfWork();
        String sourceCompanyId = "123272727";
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

         DomainEntitySet<FinancialTransaction> singleDomainEntitySet = new DomainEntitySet<FinancialTransaction>();
         singleDomainEntitySet.add(c1FinTxns.get(0));

        DomainEntitySet<TransactionReturn> returnList;
        if (pReturnCode.equals("C05")) {
            returnList = persistTransactionReturns(singleDomainEntitySet, pReturnCode, pAchTransactionTypeCode);
        } else {
            returnList = persistTransactionReturns(singleDomainEntitySet, pReturnCode, "12100035825625625651325454321");
        }
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of C1 EmployeeDdCredit CR txns", 1, singleDomainEntitySet.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForSingleNoticeOfChangePayeeBankAccount(String pReturnCode,
                                                                                              String pAchTransactionTypeCode, String pReturnDesc) {
       loadPayrollRunForBPACHReturnTest();
       PayrollServices.beginUnitOfWork();
       String sourceCompanyId = "123272727";
       Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

       DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
               .findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                       TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> singleDomainEntitySet = new DomainEntitySet<FinancialTransaction>();
        singleDomainEntitySet.add(c1FinTxns.get(0));

       DomainEntitySet<TransactionReturn> returnList;
       if (pReturnCode.equals("C05")) {
           returnList = persistTransactionReturns(singleDomainEntitySet, pReturnCode, pAchTransactionTypeCode);
       } else {
           returnList = persistTransactionReturns(singleDomainEntitySet, pReturnCode, pReturnDesc);
       }
       PayrollServices.commitUnitOfWork();
       assertEquals("Number of C1 EmployeeDdCredit CR txns", 1, singleDomainEntitySet.size());

       return returnList;
   }

    /**
     * Function to load QBDT data for Employee Bank Account Transaction Returns to test the Notice of Change events
     *
     * @param pReturnCode             String
     * @param pAchTransactionTypeCode String
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadQBDTDataForNoticeOfChangeEmployeeBankAccount(String pReturnCode,
                                                                                               String pAchTransactionTypeCode) {
        loadDataHappyPathQBDT();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);

        DomainEntitySet<TransactionReturn> returnList;
        if (pReturnCode.equals("C05")) {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, "12100035825625625651325454321");
        }

        assertEquals("Number of C1 EmployeeDdCredit CR txns", 2, c1FinTxns.size());

        return returnList;
    }

    public TransactionReturn loadQBDTDataWithOneNOCReturn(String pReturnCode,
                                                          String pAchTransactionTypeCode,
                                                          String pSourceEmployeeId, String pReturnDesc) {
        loadDataHappyPathQBDT();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);

        FinancialTransaction ft = null;
        for (FinancialTransaction txn : c1FinTxns) {
            Employee ee = txn.getPaycheckSplit().getEmployeeBankAccount().getEmployee();
            if (pSourceEmployeeId.equals(ee.getSourceEmployeeId())) {
                ft = txn;
            }
        }

        if (ft == null) {
            return null;
        }

        c1FinTxns = new DomainEntitySet<FinancialTransaction>();
        c1FinTxns.add(ft);

        DomainEntitySet<TransactionReturn> returnList;
        if (pReturnCode.equals("C05")) {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pAchTransactionTypeCode);
        } else {
            returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDesc);
        }

        assertEquals("Number of C1 EmployeeDdCredit CR txns", 1, c1FinTxns.size());

        return returnList.get(0);
    }

    public DomainEntitySet<TransactionReturn> loadDataForDefaulltRejectReturnEvent(String pReturnCode, String pReturnDescription) {
        return loadDataForDefaulltRejectReturnEvent(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pReturnCode, pReturnDescription);
    }
    /**
     * Function to load the data for Bank Account Verification ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForDefaulltRejectReturnEvent(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, String pReturnCode, String pReturnDescription) {
        Application.beginUnitOfWork();
        loadDataHappyPath(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDDDebits Created txns", 1, financialTransactions.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions, pReturnCode, pReturnDescription);
        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions2 = FinancialTransaction.
                findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Executed);
        DomainEntitySet<TransactionReturn> returnList2 = persistTransactionReturns(financialTransactions2, pReturnCode,
                pReturnDescription);
        PayrollServices.commitUnitOfWork();

        return returnList2;
    }

    public String loadDataForEmployeeDdReturnQBDT(String pReturnCode, String pReturnDescription) {
        loadQBDTPayrollOffloaded();

        //Return the ee credit
        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20071008000000");

        DomainEntitySet<FinancialTransaction> c1FinTxns =
            FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                                                           TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        c1FinTxns = c1FinTxns.find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("333.33")));

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(c1FinTxns, pReturnCode, pReturnDescription);

        Application.commitUnitOfWork();

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        Application.beginUnitOfWork();

        //Execute the return handlers
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);

        txnReturn = returnHandler.execute(txnReturn);

        String returnBatchId = txnReturn.getReturnBatch().getId().toString();

        Application.commitUnitOfWork();

        return returnBatchId;
    }

    public DomainEntitySet<TransactionReturn> loadDataForDefaulltRejectReturnEventQBDT5Day(String pReturnCode, String pReturnDescription) {
        loadQBDTPayrollReturned_5Day(pReturnCode, pReturnDescription);

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions =
            FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, c4dl.getCompany1().getCompanyId(),
                                                           TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Executed);
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, pReturnCode,
                                                                                  pReturnDescription);
        PayrollServices.commitUnitOfWork();

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForBPDefaultRejectReturnEvent(String pReturnCode, String pReturnDescription) {
        loadPayrollRunForBPACHReturnTest2();
        PayrollServices.beginUnitOfWork();
        String sourceCompanyId = "123272727";
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, pReturnCode, pReturnDescription);
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDDDebits txns", 2, financialTransactions.size());

        return returnList;

    }

    public static void loadRefundRebillNetDebit() {
        loadRefundRebillNetDebit(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static void loadRefundRebillNetDebit(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadRefundRebill(assetItemNumber, offloadGroup, new SpcfMoney("800.00"), "R01", "Return desc");
    }

    public static void loadRefundRebillNetCredit() {
        loadRefundRebillNetCredit(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static void loadRefundRebillNetCredit(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        loadRefundRebill(assetItemNumber, offloadGroup, new SpcfMoney("9.99"), "R02", "Refund desc");
    }

    public static void loadRefundRebill(SpcfMoney pAmount, String pReturnCode, String pReturnDesc) {
        loadRefundRebill(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup(), pAmount, pReturnCode, pReturnDesc);
    }

    public static void loadRefundRebill(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup, SpcfMoney pAmount, String pReturnCode, String pReturnDesc) {
        loadQBDTCompanyRequests1TxnReversed(assetItemNumber, offloadGroup);

        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // now rebill that fee
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(),
                pAmount);
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

        PayrollServicesTest.assertSuccess("rebill operation", prRebill);
        assertEquals("Number of refund transactions", 1, refundedFeeFTs.size());
        assertEquals("Number of refund transactions", 1, refundedTaxFTs.size());

        FinancialTransaction refundTxn = refundedFeeFTs.get(0);
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(refundTxn);

        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions, pReturnCode, pReturnDesc);
    }

    public DomainEntitySet<TransactionReturn> createDefualtRejectReturnEvent() {
        return createDefualtRejectReturnEvent(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), OffloadGroup.findStandardOffloadGroup());
    }
    /**
     * Function to load the data for Bank Account Verification ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> createDefualtRejectReturnEvent(SourceSystemCode sourceSystemCode, String sourceCompanyId, OffloadGroup offloadGroup) {
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R02",
                "This is a non-NSF description");

        assertEquals("Number of C1 EmployeeDdCredit Created txns", 2, financialTransactions.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> createDDRejectReturnNSF() {
        return createDDRejectReturnNSF(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), OffloadGroup.findStandardOffloadGroup());
    }

    public DomainEntitySet<TransactionReturn> createDDRejectReturnNSF(SourceSystemCode sourceSystemCode, String sourceCompanyId, OffloadGroup offloadGroup) {
        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R01",
                "This is a non-NSF description");

        assertEquals("Number of C1 EmployeeDdCredit Created txns", 2, financialTransactions.size());

        return returnList;
    }

    /**
     * Function to add the TransactionReturns to the associated Financial Transactions
     *
     * @param pFinTxnList DomainEntitySet<FinancialTransaction>
     * @param pReturnCd   String
     * @param pReturnDesc String
     * @return DomainEntitySet<TransactionReturn>
     */
    public static DomainEntitySet<TransactionReturn> persistTransactionReturns(DomainEntitySet<FinancialTransaction> pFinTxnList,
                                                                               String pReturnCd, String pReturnDesc) {
        return createTransactionReturns(getMoneyMovementTransactions(pFinTxnList, false), pReturnCd, pReturnDesc);
    }

    /**
     * Builds TransactionReturns based on a set of MMTs.
     *
     * @param pSetOfMMTs
     * @param pReturnCd
     * @param pReturnDesc
     * @return
     */
    public static DomainEntitySet<TransactionReturn> createTransactionReturns(DomainEntitySet<MoneyMovementTransaction> pSetOfMMTs,
                                                                              String pReturnCd, String pReturnDesc) {
        TransactionReturnBatch batch = createBatch();

        // for each MMT, create a TransactionReturn and add it to the batch
        DomainEntitySet<TransactionReturn> returns = new DomainEntitySet<TransactionReturn>();
        for (MoneyMovementTransaction mmt : pSetOfMMTs) {
            returns.add(createTransactionReturn(batch, mmt, pReturnCd, pReturnDesc));
            logger.info("created TransactionReturn for MMT with " + mmt.getFinancialTransactionCollection().size() + " FTs");
        }
        batch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        return returns;
    }

    public static TransactionReturnBatch createBatch() {
        // create a batch
        TransactionReturnBatch batch = new TransactionReturnBatch();
        batch.setACHReturnFileName("");
        batch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        batch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        batch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        batch = Application.save(batch);
        return batch;
    }

    public static DomainEntitySet<MoneyMovementTransaction> getMoneyMovementTransactions(DomainEntitySet<FinancialTransaction> pFTs,
                                                                                         boolean pExecutedOnly) {
        // build the set of MMTs associated with FTs
        int nIncluded = 0;
        DomainEntitySet<MoneyMovementTransaction> set = new DomainEntitySet<MoneyMovementTransaction>();
        for (FinancialTransaction ft : pFTs) {
            // only those FTs in Executed can be included in a TransactionReturn
            TransactionStateCode ftStateCd = ft.getCurrentTransactionState().getTransactionStateCd();
            if (!pExecutedOnly || ftStateCd == TransactionStateCode.Executed) {
                ++nIncluded;
                MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();
                if (mmt != null) {
                    set.add(mmt);
                }
            }
            logger.info("FT: " + ft.getTransactionType().getTransactionTypeCd() + " is " + ftStateCd);
        }
        logger.info("Found " + set
                .size() + " MMTs for " + nIncluded + " FTs in " + (pExecutedOnly ? "Execute" : "ANY state"));
        return set;
    }

    private static TransactionReturn createTransactionReturn(TransactionReturnBatch pBatch,
                                                             MoneyMovementTransaction pMMT,
                                                             String pReturnCd, String pReturnDesc) {
        TransactionReturn txnReturn = new TransactionReturn();

        txnReturn.setBankReturnCd(pReturnCd);
        txnReturn.setBankReturnDescription(pReturnDesc);
        txnReturn.setBankReturnTraceNumber(112L);
        txnReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
        txnReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        txnReturn.setMoneyMovementTransaction(pMMT);
        txnReturn.setCompany(pMMT.getCompany());        
        txnReturn.setReturnBatch(pBatch);
        txnReturn = Application.save(txnReturn);

        return txnReturn;
    }

    /**
     * Function to load the data for main ACH Return file testing.
     *
     * @return c1FinTxns DomainEntitySet<FinancialTransaction>
     */
    public DomainEntitySet<FinancialTransaction> loadDataForACHReturnFileTesting() {

        loadDataForFirstNSFReturn();

        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerDdRedebit EX txns", 1, c1FinTxns.size());

        return c1FinTxns;
    }

    public DomainEntitySet<TransactionReturn> loadDataForDirectDepositReturnOnHoldCompany() {
        return loadDataForDirectDepositReturnOnHoldCompany(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId());
    }

    /**
     * Function to load the data for Bank Account Verification ACH Return Event testing
     *
     * @return returnList DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> loadDataForDirectDepositReturnOnHoldCompany(SourceSystemCode sourceSystemCode, String sourceCompanyId) {


        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(sourceSystemCode, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R02",
                "This is an NSF description");

        assertEquals("Number of C1 EmployeeDdCredit Created txns", 2, financialTransactions.size());

        return returnList;
    }

    public DomainEntitySet<TransactionReturn> loadDataForBPReturnOnHoldCompany() {

        String sourceCompanyId = "123272727";
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturns(financialTransactions, "R02",
                "This is an NSF description");

        assertEquals("Number of C1 EmployeeDdCredit  txns", 4, financialTransactions.size());

        return returnList;
    }

    public static void loadDataHappyPath() {
        loadDataHappyPath(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }
    /**
     * Methods to load data*
     */
    public static void loadDataHappyPath(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c1dl = new Company1Dataloader();

        PSPDate.setPSPTime("20070822000000");
        persistCompany1(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
    }

    public static void loadDataHappyPathNotBackdated() {
        c1dl = new Company1Dataloader();

        PSPDate.setPSPTime("20070822000000");
        persistCompany1_notBackDated();
    }

    public static void loadDataHappyPathQBDT() {
        loadDataHappyPathQBDT(AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    /**
     * Methods to load data*
     */
    public static void loadDataHappyPathQBDT(AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c4dl = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070823000000");
        c4dl.persistQBCompany1(assetItemNumber, offloadGroup);
        PayrollRunDTO payrollRunDTO = c4dl.get2ndCompany2PR_DoesNotExceedLimits();
        PayrollRun payrollRun = c4dl.persistPayrollRun(payrollRunDTO);
    }

    public static void loadDataHappyPathQBDT_5Day() {
        c4dl = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");

        c4dl.persistQBCompany1();
        c4dl.updateTo5DayFundingModel();

        PayrollRunDTO payrollRunDTO = c4dl.get2ndCompany2PR_DoesNotExceedLimits();
        PayrollRun payrollRun = c4dl.persistPayrollRun(payrollRunDTO);
    }

    /**
     * Methods to load data*
     */
    public static void loadDataHappyPathQBDT2ndCompany() {
        cQBDTd1 = new CompanyQB2DataLoader();

        PSPDate.setPSPTime("20070924000000");
        cQBDTd1.persistQBCompany1();
        PayrollRunDTO payrollRunDTO = cQBDTd1.get2ndCompany2PR_DoesNotExceedLimits();
        PayrollRun payrollRun = cQBDTd1.persistPayrollRun(payrollRunDTO);
    }

    public static void loadDataHappyPath2Day() {
        loadDataHappyPath2Day(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static void loadDataHappyPath2Day(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c1dl = new Company1Dataloader();

        PSPDate.setPSPTime("20070822000000");
        persistCompany1On2Day(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
    }

    public static void updateC1To2Day() {
        updateC1To2Day(SourceSystemCode.QBOE);
    }

    public static void updateC1To2Day(SourceSystemCode sourceSystemCode) {
        c1dl.updateTo2DayFundingModel(sourceSystemCode);
    }

    public static void loadDataReversal() {
        loadDataReversal(SourceSystemCode.QBOE, "1234567");
    }

    public static void loadDataReversal(SourceSystemCode sourceSystemCode, String sourceCompanyId) {
        PSPDate.setPSPTime("20071011000000");
        c1dl.reverseEntirePayroll(sourceSystemCode, sourceCompanyId, "BatchTest05");
    }

    public static PayrollRunDTO loadPayrollQBDT_NoBankAccountChange() {

        return c4dl.getPayrollRunNOC_NoBankAccountChange();

    }

    public static PayrollRunDTO loadPayrollQBDT_NoEEWithNOC() {

        return c4dl.getPayrollRunNOC_NoEEWithNOC();

    }

    public static PayrollRunDTO loadPayrollQBDT_EEBankAccountChanged() {

        return c4dl.getPayrollRunNOC_EEBankAccountChanged();

    }

    public static void persistCompany_Dont_Activate_BA(Company pCompany, ServiceInfoDTO pCompanyService) {
        persistCompany_Dont_Activate_BA(pCompany, pCompanyService, OffloadGroup.findStandardOffloadGroup());
    }

    public static void persistCompany_Dont_Activate_BA(Company pCompany, ServiceInfoDTO pCompanyService, OffloadGroup offloadGroup) {
        // Create Company and CompanyBankAccount
        DataLoader dataloader = new DataLoader();

        CompanyService ddCompanyService = dataloader.persistCompanyService(pCompany, pCompanyService);
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);

        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();

        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount
                .getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        assertFalse("PSPDate not on weekend or bank holiday", CalendarUtils.isWeekendOrHoliday(PSPDate.getPSPTime()));
        assertTrue("PSPDate should be set", PSPDate.getCurrentOffset() != 0L);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(offloadGroup.getOffloadGroupCd(), PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        // Set PSP Time to a date in the future
        PSPDate.addDaysToPSPTime(3);
    }

    private static void persistCompany1() {
        persistCompany1(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    private static void persistCompany1(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c1dl.persistCompany1(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-08-25"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(sourceSystemCode, sourceCompanyId, payrollRunDTO);
    }


    public static void loadPayrollRunForBPACHReturnTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();


        String sourceCompanyId = "123272727";

        // company setup

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();


    }

    public static void loadPayrollRunForBPACHReturnTest2() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();


        String sourceCompanyId = "123272727";

        // company setup

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 2; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-19"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070917000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();


    }

    private static void persistCompany1_notBackDated() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany1On2Day() {
        persistCompany1On2Day(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    private static void persistCompany1On2Day(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c1dl.persistCompany1(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        updateC1To2Day(sourceSystemCode);
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-11"));
        c1dl.persistPayrollRun(sourceSystemCode, sourceCompanyId, payrollRunDTO);
    }

    public static void persistCompany1On2Day_1ee2PaycheckSplits() {
        persistCompany1On2Day_1ee2PaycheckSplits(SourceSystemCode.QBOE, "1234567", AssetItemNumber.DIY_YEARLY, OffloadGroup.findStandardOffloadGroup());
    }

    public static void persistCompany1On2Day_1ee2PaycheckSplits(SourceSystemCode sourceSystemCode, String sourceCompanyId, AssetItemNumber assetItemNumber, OffloadGroup offloadGroup) {
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime("20070822000000");
        c1dl.persistCompany1(sourceSystemCode, sourceCompanyId, assetItemNumber, offloadGroup);
        updateC1To2Day(sourceSystemCode);
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits_1ee2Paychecksplits(new DateDTO("2007-09-11"));
        c1dl.persistPayrollRun(sourceSystemCode, sourceCompanyId,payrollRunDTO);
    }

    private void addCompany1Payroll3() {
        addCompany1Payroll3(SourceSystemCode.QBOE, "1234567");
    }

    private void addCompany1Payroll3(SourceSystemCode sourceSystemCode, String sourcecompanyId) {
        PayrollRunDTO payrollRunDTO = c1dl.get3rdCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-15"));
        c1dl.persistPayrollRun(sourceSystemCode, sourcecompanyId, payrollRunDTO);
    }

}

    
