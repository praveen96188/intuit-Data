package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Mar 25, 2008
 * Time: 10:35:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionTypeBETests {
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

    public void getTransactionTypeCollection(){
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionType> txnTypeList = TransactionType.findTransactionTypeByTxnCategory(
                TransactionCategory.Employee);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Types for Employee :", 3, txnTypeList.size());

        PayrollServices.beginUnitOfWork();
        txnTypeList = TransactionType.findTransactionTypeByTxnCategory(
                TransactionCategory.Employer);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Types for Employer :", 39, txnTypeList.size());

        PayrollServices.beginUnitOfWork();
        txnTypeList = TransactionType.findTransactionTypeByTxnCategory(
                TransactionCategory.Intuit);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Types for Intuit :", 91, txnTypeList.size());

        PayrollServices.beginUnitOfWork();
        txnTypeList = TransactionType.findTransactionTypeByTxnCategory(
                TransactionCategory.Agency);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Transaction Types for Agency :", 17, txnTypeList.size());

        PayrollServices.beginUnitOfWork();
        txnTypeList = TransactionType.findTransactionTypeByAssociationType(
                TransactionAssociationType.Refund);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Refund Transaction Types :", 23, txnTypeList.size());

        PayrollServices.beginUnitOfWork();
        txnTypeList = TransactionType.findTransactionTypeByFeeIndicator(true);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Fee Transaction Types :", 2, txnTypeList.size());
    }
}
