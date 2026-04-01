package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.processes.AddEmployeeReturnTransferTransaction;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 *
 * User: rkrishna
 * Date: Dec 4, 2007
 * Time: 4:50:13 PM

 */
public class TransactionReturnBETests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test

    /**
     * Test case to test the Transaction return collection query based on the payroll run id & company id.
     * Multiple Companies are added with the same payroll run
     */
    public void getTransactionReturnCollectionByPayrollId(){
        //initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();

        //Save Payrolls for Company - 123272727
        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        }

        //Save Payrolls for Company - 123123123
        payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123123123();

        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            dataloader.savePayroll(SourceSystemCode.QBOE, "123123123", payrollRunDTO);
        }

        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Load Transaction Returns for Company1 - 123272727
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payRun = PayrollRun.findPayrollRun(company1, "BatchId01");
        TransactionReturnTestDataLoader returnsLoader = new TransactionReturnTestDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                                                                                          "R02",
                                                                                          "This is an EE Return");
        PayrollServices.commitUnitOfWork();
        
        //Load Transaction Returns for Company1 - 123123123
        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany("123123123", SourceSystemCode.QBOE);
        payRun = PayrollRun.findPayrollRun(company2, "BatchId01");
        returnsLoader = new TransactionReturnTestDataLoader();
        c1FinTxns = payRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                                                                                          "R02",
                                                                                          "This is an EE Return");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<TransactionReturn> company1TxnReturns = TransactionReturn.
                findTransactionReturns("BatchId01", company1);

        DomainEntitySet<TransactionReturn> company2TxnReturns = TransactionReturn.
                findTransactionReturns("BatchId01", company2);
        PayrollServices.commitUnitOfWork();

        assertEquals("Transaction Returns for Company : 123272727 & PayrollRun :BatchId01 ",2, company1TxnReturns.size());
        assertEquals("Transaction Returns for Company : 123123123 & PayrollRun :BatchId01 ",2, company2TxnReturns.size());
    }

    @Test

    /**
     * Test case to test the Transaction return collection query based on the payroll run id & company id.
     * Same company with multiple payroll runns
     */
    public void getTransactionReturnCollectionByPayrollId1(){
        //initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();

        //Save Payrolls for Company - 123272727
        for (PayrollRunDTO payrollRunDTO: payrollRunDTOs) {
            dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        }

        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Load Transaction Returns for Company1 - 123272727
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payRun = PayrollRun.findPayrollRun(company1, "BatchId01");
        TransactionReturnTestDataLoader returnsLoader = new TransactionReturnTestDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                                                                                          "R02",
                                                                                          "This is an EE Return");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<TransactionReturn> company1TxnReturns = TransactionReturn.
                findTransactionReturns("BatchId01", company1);

        assertEquals("Transaction Returns for Company : 123272727 & PayrollRun :BatchId01 ",2, company1TxnReturns.size());
    }

    @Test
    public void getTransactionReturnCollection() {
        //Submit the Payrolls for Company1
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 30, SpcfTimeZone.getLocalTimeZone()));
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Submit the Payrolls for Company2
        PayrollServices.beginUnitOfWork();
        Company2Dataloader c2dl = new Company2Dataloader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        c2dl.persistCompany2();
        payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Load Transaction Returns for Company1 - 1234567
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                "R01",
                "This is a non-NSF description");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, c2dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        TransactionReturnTestDataLoader.persistTransactionReturns(c2FinTxns, "R01", "This is a non-NSF description");
        PayrollServices.commitUnitOfWork();

        //Get the Transaction Returns for Company1
        PayrollServices.beginUnitOfWork();
        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Created);
        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        int totalRecordCount = TransactionReturn.
                getTransactionReturnCollection(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(), null, null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Returns:", 2, transactionReturnList.size());
        assertEquals("Total Record Count :", 2, totalRecordCount);

        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Return Status Cd ", TransactionReturnStatusCode.Created, transactionReturn.getReturnStatusCd());
            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            assertEquals("PayRoll Run Id ", "BatchTest05",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());

            junit.framework.Assert.assertEquals("Source Company Id", c1dl.getCompany1().getCompanyId(), financialTransaction.get(0).getCompany().getSourceCompanyId());
        }
        PayrollServices.commitUnitOfWork();

        System.out.println("Number of Transaction Returns:" + transactionReturnList.size());

        //Get the Transaction Returns for all the companies
        PayrollServices.beginUnitOfWork();
        retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Created);
        transactionReturnList = new DomainEntitySet<TransactionReturn>();
        totalRecordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Returns:", 4, transactionReturnList.size());
        assertEquals("Total Record Count :", 4, totalRecordCount);
    }

    @Test
    public void getTransactionReturnCollectionBySourceSystemCode() {
        //Submit the Payrolls for Company1
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Submit the Payrolls for Company3 (QBDT Comapny)
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        c3dl.persistCompany3();
        payrollRunDTO = c3dl.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c3dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Load Transaction Returns for Company1 - 1234567
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                "R01",
                "This is a non-NSF description");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c3FinTxns = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        TransactionReturnTestDataLoader.persistTransactionReturns(c3FinTxns, "R01", "This is a non-NSF description");
        PayrollServices.commitUnitOfWork();

        //Get the Transaction Returns for Company3
        PayrollServices.beginUnitOfWork();
        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Created);
        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        int totalRecordCount = TransactionReturn.
                getTransactionReturnCollection(SourceSystemCode.QBDT, c3dl.getCompany1().getCompanyId(), c3dl.getCompany1().getFein(), null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Returns:", 2, transactionReturnList.size());
        assertEquals("Total Record Count :", 2, totalRecordCount);

        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Return Status Cd ", transactionReturn.getReturnStatusCd(), TransactionReturnStatusCode.Created);
            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            assertEquals("PayRoll Run Id ", "BatchTest87",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());

            junit.framework.Assert.assertEquals("Source Company Id", c3dl.getCompany1().getCompanyId(), financialTransaction.get(0).getCompany().getSourceCompanyId());
        }
        PayrollServices.commitUnitOfWork();

        //Get the Transaction Returns for all the companies
        PayrollServices.beginUnitOfWork();
        retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Created);
        transactionReturnList = new DomainEntitySet<TransactionReturn>();
        totalRecordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Returns:", 4, transactionReturnList.size());
        assertEquals("Total Record Count :", 4, totalRecordCount);
    }


    @Test
    public void getTransactionReturnCollectionWithoutMaxResults() {
        TransactionReturnTestDataLoader.loadDataForMultipleTransactionReturns();

        Application.beginUnitOfWork();

        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Created);

        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        int recordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        Assert.assertEquals("Number of Transaction Returns:", 3, transactionReturnList.size());
        Assert.assertEquals("Total Record count :", 3, recordCount);

        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Return Status Cd ", transactionReturn.getReturnStatusCd(), TransactionReturnStatusCode.Created);

            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            assertEquals("PayRoll Run Id ", "BatchId01",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void getTransactionReturnCollectionByRSLVDStatusCd() {

        TransactionReturnTestDataLoader.loadDataForMultipleTransactionReturns();

        Application.beginUnitOfWork();

        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Resolved);

        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();

        int recordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, null, retunStatusCd, null, null, 0, 0, transactionReturnList);

        Assert.assertEquals("Number of Transaction Returns:", 0, transactionReturnList.size());
        Assert.assertEquals("Total Record count :", 0, recordCount);

        Application.commitUnitOfWork();
    }

    @Test
    public void getTransactionReturnCollectionByFinTxnIds() {
        //Submit payroll and create returns for one of the ees and for the er
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        ACHReturnsDataLoader.loadDataForEEReturnTransferReturn();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070908000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        AddEmployeeReturnTransferTransaction eeReturnTransfer = new AddEmployeeReturnTransferTransaction(
                SourceSystemCode.QBOE, "1234567", "BatchTest05");
        ProcessResult processResult = eeReturnTransfer.execute();

        Application.commitUnitOfWork();

        assertSuccess("Process Result", processResult);

        Application.beginUnitOfWork();

        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Open);
        retunStatusCd.add(TransactionReturnStatusCode.Resolved);

        Collection finTxnIds = new ArrayList();

        Company company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchTest05", null, null, null, null, null, null, null);

        for (FinancialTransaction financialTransaction : financialTransactions) {
            finTxnIds.add(financialTransaction.getId());
        }

        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        int recordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, finTxnIds, retunStatusCd, null, null, 0, 0, transactionReturnList);

        Assert.assertEquals("Number of Transaction Returns:", 2, transactionReturnList.size());
        Assert.assertEquals("Total Record count :", 2, recordCount);

        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Bank Return Cd ", transactionReturn.getBankReturnCd(), "R02");

            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            assertEquals("PayRoll Run Id ", "BatchTest05",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());

            if (financialTransaction.get(0).getMoneyMovementTransaction().equals(transactionReturn.getMoneyMovementTransaction()) &&
                    financialTransaction.get(0).getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerDdDebit)) {
                assertEquals("Return Status Cd ", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
            }
            else {
                assertEquals("Return Status Cd ", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
            }
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void getTransactionReturnCollectionByFinTxnIdsWithoutReturnStatusCd() {
        Application.beginUnitOfWork();
        TransactionReturnTestDataLoader loader = new TransactionReturnTestDataLoader();

        TransactionReturnTestDataLoader.loadDataForTransactionReturn();

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Collection finTxnIds = new ArrayList();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        Employee employee = Employee.findEmployee(company, "Emp1");

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", employee
                        , null, null, null, null);

        for (FinancialTransaction financialTransaction : financialTransactions) {
            finTxnIds.add(financialTransaction.getId());
        }

        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();

        int recordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, finTxnIds, null, null, null, 0, 0, transactionReturnList);

        Assert.assertEquals("Number of Transaction Returns:", 1, transactionReturnList.size());
        Assert.assertEquals("Total Record count :", 1, recordCount);

        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Bank Return Cd ", transactionReturn.getBankReturnCd(), "R01");

            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            assertEquals("PayRoll Run Id ", "BatchId01",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void getTransactionReturnCollectionByFinTxnIdsWithMaxResults() {
        //TransactionReturnTestDataLoader.loadDataForMultipleTransactionReturns();
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForSecondNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Collection retunStatusCd = new ArrayList();

        retunStatusCd.add(TransactionReturnStatusCode.Open);
        retunStatusCd.add(TransactionReturnStatusCode.Resolved);

        Collection finTxnIds = new ArrayList();

        Company company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchTest05", null, null, null, null, null, null, null);

        for (FinancialTransaction financialTransaction : financialTransactions) {
            finTxnIds.add(financialTransaction.getId());
        }

        DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        int recordCount = TransactionReturn.
                getTransactionReturnCollection(null, null, null, finTxnIds, retunStatusCd, null, null, 1, 2, transactionReturnList);

        Assert.assertEquals("Number of Transaction Returns:", 2, transactionReturnList.size());
        Assert.assertEquals("Total Record Count:", 2, recordCount);

        for (TransactionReturn transactionReturn : transactionReturnList) {
            assertEquals("Bank Return Cd ", transactionReturn.getBankReturnCd(), "R01");

            DomainEntitySet<FinancialTransaction> financialTransaction = TransactionReturn.
                    findFinancialTransaction(transactionReturn);

            if (financialTransaction.get(0).getMoneyMovementTransaction().equals(transactionReturn.getMoneyMovementTransaction()) &&
                    financialTransaction.get(0).getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerDdRedebit)) {
                assertEquals("Return Status Cd ", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
            }
            else {
                assertEquals("Return Status Cd ", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
            }

            assertEquals("PayRoll Run Id ", "BatchTest05",
                    financialTransaction.get(0).getPayrollRun().getSourcePayRunId());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testFindTransactionReturnsByServiceAndExcludedStatus() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForSecondNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        PayrollServices.commitUnitOfWork();

        //Ensure correct transactionreturned and returned for the given service & excluded status
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Service ddService = Application.findById(Service.class, ServiceCode.DirectDeposit);
        DomainEntitySet<TransactionReturn> transactionReturnCollection =
                TransactionReturn.findTransactionReturnsByServiceAndExcludedStatus(company, ddService, TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of UnResovled Transaction Returns for DD Service :", 1, transactionReturnCollection.size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Service taxService = Application.findById(Service.class, ServiceCode.Tax);
        transactionReturnCollection =
                TransactionReturn.findTransactionReturnsByServiceAndExcludedStatus(company, taxService, TransactionReturnStatusCode.Resolved);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of UnResovled Transaction Returns for Tax Service :", 1, transactionReturnCollection.size());
    }

}
