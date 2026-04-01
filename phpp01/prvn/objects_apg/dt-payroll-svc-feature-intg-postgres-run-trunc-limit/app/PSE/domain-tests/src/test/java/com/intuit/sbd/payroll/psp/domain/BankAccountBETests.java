package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Aug 15, 2007
 * Time: 2:20:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BankAccountBETests {
    private BankAccount bankAccount;
    private ProcessResult processResult;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        bankAccount = new BankAccount();        
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateEmptyBankAccount()
    {
        processResult = bankAccount.validateBankAccount();
        assertEquals(2, processResult.getMessages().size());
    }

    @Test
    public void validateValidBankAccount()
    {
        processResult = getTestBankAccount().validateBankAccount();
        assertEquals(0, processResult.getMessages().size());
    }

    @Test
    public void validateEqualsIgnoreBankName()
    {
        // Bank Accounts are null
        BankAccount bankAccount1 = null;
        BankAccount bankAccount2 = null;

        // Bank Accounts are the same object
        bankAccount1 = getTestBankAccount();
        bankAccount2 = bankAccount1;
        assertTrue(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));
        // Bank Accounts have the same properties
        bankAccount1 = getTestBankAccount();
        bankAccount2 = getTestBankAccount();
        assertTrue(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));
        // Change Bank Name
        bankAccount2.setBankName("abc");
        assertTrue(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));
        // Change Account Number
        bankAccount2.setAccountNumber("123");
        assertFalse(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));
        bankAccount2.setAccountNumber(bankAccount1.getAccountNumber());
        // Change Routing Number
        bankAccount2.setRoutingNumber("678");
        assertFalse(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));
        bankAccount2.setRoutingNumber(bankAccount1.getRoutingNumber());
        // Change Account Type
        bankAccount2.setAccountTypeCd(BankAccountType.Savings);
        assertFalse(bankAccount1.equalsIgnoreBankNameSourceBankName(bankAccount2));

    }

    private BankAccount getTestBankAccount()
    {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber("12345");
        bankAccount.setAccountTypeCd(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");
        return bankAccount;
    }



 }
