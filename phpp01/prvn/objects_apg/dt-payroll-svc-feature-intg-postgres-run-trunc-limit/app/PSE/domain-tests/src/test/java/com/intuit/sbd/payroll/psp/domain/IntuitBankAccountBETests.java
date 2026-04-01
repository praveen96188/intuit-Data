package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * User: mvillani
 * Date: Aug 16, 2007
 * Time: 7:12:32 AM

 */
public class IntuitBankAccountBETests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }
    

    @Test
    public void validateGetIntuitBankAccount()
    {
        PayrollServices.beginUnitOfWork();
        IntuitBankAccount intuitBankAccount = IntuitBankAccount.findIntuitBankAccount(TransactionType.findTransactionType(
                TransactionTypeCode.EmployerDdDebit), CreditDebitCode.Credit);
        assertEquals("3b76af25-eb67-4409-a559-225a85eee4b9", intuitBankAccount.getId().toString());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void validateGetNullIntuitBankAccount()
    {
        PayrollServices.beginUnitOfWork();
        IntuitBankAccount intuitBankAccount = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit), CreditDebitCode.Debit);
        assertNull(intuitBankAccount);
        PayrollServices.commitUnitOfWork();        
    }


}
