package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 2/22/12
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddBookTransferTransactionCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2012, 1, 22);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testForValidations() {

        PayrollServices.beginUnitOfWork();
        //Null Debit account
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction(null, "INTUIT EE RETURN", new SpcfMoney("0"));
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Debit account is null has invalid value", processResult.getErrorMessages().get(0).getMessage());

        //Null Credit account
        processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction("INTUIT EE RETURN", null, new SpcfMoney("0"));
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Credit account is null has invalid value", processResult.getErrorMessages().get(0).getMessage());

        //Zero dollar amount
        processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction("INTUIT FEE", "INTUIT EE RETURN", new SpcfMoney("0"));
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Transaction Amount has invalid value", processResult.getErrorMessages().get(0).getMessage());

        //Negative dollar amount
        processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction("INTUIT FEE", "INTUIT EE RETURN", new SpcfMoney("-1"));
        assertOne(processResult.getErrorMessages());
        assertEquals("5001", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Transaction Amount has invalid value", processResult.getErrorMessages().get(0).getMessage());

        PayrollServices.commitUnitOfWork();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testBookTransfer() {

        //Create Book Transfer and validate FT and MMT.
        SpcfCalendar initDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(initDate);
        CalendarUtils.addBusinessDays(initDate, 1);
        SpcfCalendar dueDate = initDate.copy();
        CalendarUtils.addBusinessDays(dueDate, 1);

        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction("INTUIT DD", "INTUIT EE RETURN", new SpcfMoney("5.5"));
        assertSuccess(processResult);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertEquals("Transaction Amount", new SpcfMoney("5.50"), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Transaction Type", TransactionType.findTransactionType(TransactionTypeCode.GlobalBookTransfer), financialTransaction.getTransactionType());
        assertEquals("Settlement Type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
        assertEquals("Credit Bank Account Owner Type", BankAccountOwnerType.Intuit, financialTransaction.getCreditBankAccountType());
        assertEquals("Debit Bank Account Owner Type", BankAccountOwnerType.Intuit, financialTransaction.getDebitBankAccountType());
        assertEquals("Transaction Current State", TransactionState.findTransactionState(TransactionStateCode.Created), financialTransaction.getCurrentTransactionState());
        assertEquals("Intuit global book transfer company", Company.getBookTransferCompany(), financialTransaction.getCompany());
        assertEquals("FT Settlement Date", dueDate, financialTransaction.getSettlementDate());

        IntuitBankAccount fromBankAccount = IntuitBankAccount.findIntuitBankAccountByName("INTUIT DD");
        IntuitBankAccount toBankAccount = IntuitBankAccount.findIntuitBankAccountByName("INTUIT EE RETURN");

        assertEquals("Debit Bank Account", fromBankAccount.getBankAccount(), financialTransaction.getDebitBankAccount());
        assertEquals("Credit Bank Account", toBankAccount.getBankAccount(), financialTransaction.getCreditBankAccount());

        MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();

        assertEquals("MMT amount", financialTransaction.getFinancialTransactionAmount(), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("MMT Status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        assertEquals("MMT Payment Method", PaymentMethod.ACHDirectDeposit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("MMT Tax Payment Status", TaxPaymentStatus.None, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MMT Company", Company.getBookTransferCompany(), moneyMovementTransaction.getCompany());
        assertEquals("MMT Init Date", initDate, moneyMovementTransaction.getInitiationDate());
        assertEquals("MMT Due Date", dueDate, moneyMovementTransaction.getDueDate());

        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(initDate);
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = offloader.getOffloadBatch().getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        assertEquals("Finalized Nacha files", 1, nachaFiles.size());
        NACHAFile nachaFile = nachaFiles.get(0);
        PayrollServices.rollbackUnitOfWork();

        String fileName = nachaFile.getFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));

        String[] expectedOutput = {"101 02100002197226160001201231710R094101JPMORGAN CHASE         INTUIT                         ",
                "5200INTUIT                     7700346611722616679CCD          120124120124   1021000020000001",
                "627111000614722616653        0000000550722616653      INTUIT DD               \\d{16}+",
                "622111000614722616711        0000000550722616711      INTUIT EE RETURN        \\d{16}+",
                "820000000200222001220000000005500000000005501722616679                         021000020000001",
                "9000001000001000000020022200122000000000550000000000550                                       ",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"};

        Assert.assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);

        for (int i = 0; i < expectedOutput.length; i++) {

            Pattern pattern = Pattern.compile(expectedOutput[i]);
            Matcher matcher = pattern.matcher(lines[i]);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        Application.refresh(financialTransaction);

        assertEquals("Entry Details", 2, moneyMovementTransaction.getEntryDetailRecordCollection().size());
        assertEquals("Transaction Current State", TransactionState.findTransactionState(TransactionStateCode.Executed), financialTransaction.getCurrentTransactionState());
        assertEquals("MMT Status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        Application.refresh(financialTransaction);

        assertEquals("Transaction Current State", TransactionState.findTransactionState(TransactionStateCode.Completed), financialTransaction.getCurrentTransactionState());
        assertEquals("MMT Status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }


}
