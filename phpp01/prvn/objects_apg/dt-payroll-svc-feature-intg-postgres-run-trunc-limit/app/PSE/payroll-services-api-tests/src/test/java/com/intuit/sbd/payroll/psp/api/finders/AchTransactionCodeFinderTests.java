package com.intuit.sbd.payroll.psp.api.finders;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AchTransactionCode;
import com.intuit.sbd.payroll.psp.domain.ACHBankAccountType;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: May 28, 2009
 * Time: 2:49:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AchTransactionCodeFinderTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void findAchTransactionCode21Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Checking, CreditDebitCode.Credit, true);
        PayrollServices.commitUnitOfWork();
        
        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "21");
    }

    @Test
    public void findTransactionCode22Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Checking, CreditDebitCode.Credit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "22");
    }

    @Test
    public void findTransactionCode26Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Checking, CreditDebitCode.Debit, true);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "26");
    }

    @Test
    public void findTransactionCode27Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Checking, CreditDebitCode.Debit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "27");
    }

    @Test
    public void findTransactionCode31Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Savings, CreditDebitCode.Credit, true);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "31");
    }

    @Test
    public void findTransactionCode32Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Savings, CreditDebitCode.Credit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "32");
    }

    @Test
    public void findTransactionCode36Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Savings, CreditDebitCode.Debit, true);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "36");
    }

    @Test
    public void findTransactionCode37Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Savings, CreditDebitCode.Debit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "37");
    }

    @Test
    public void findTransactionCode41Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Ledger, CreditDebitCode.Credit, true);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "41");
    }

    @Test
    public void findTransactionCode42Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Ledger, CreditDebitCode.Credit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "42");
    }

    @Test
    public void findTransactionCode46Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Ledger, CreditDebitCode.Debit, true);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "46");
    }

    @Test
    public void findTransactionCode47Test() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                ACHBankAccountType.Ledger, CreditDebitCode.Debit, false);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(found.size(), 1);
        Assert.assertEquals(found.get(0).getTransactionCode(), "47");
    }

    @Test
        public void findTransactionCode51Test() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                    ACHBankAccountType.Loan, CreditDebitCode.Credit, true);
            PayrollServices.commitUnitOfWork();

            Assert.assertEquals(found.size(), 1);
            Assert.assertEquals(found.get(0).getTransactionCode(), "51");
        }

        @Test
        public void findTransactionCode52Test() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                    ACHBankAccountType.Loan, CreditDebitCode.Credit, false);
            PayrollServices.commitUnitOfWork();

            Assert.assertEquals(found.size(), 1);
            Assert.assertEquals(found.get(0).getTransactionCode(), "52");
        }

        @Test
        public void findTransactionCode56Test() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                    ACHBankAccountType.Loan, CreditDebitCode.Debit, true);
            PayrollServices.commitUnitOfWork();

            Assert.assertEquals(found.size(), 1);
            Assert.assertEquals(found.get(0).getTransactionCode(), "56");
        }

        @Test
        public void findTransactionCode55Test() {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<AchTransactionCode> found = AchTransactionCode.findAchTransactionCode(
                    ACHBankAccountType.Loan, CreditDebitCode.Debit, false);
            PayrollServices.commitUnitOfWork();

            Assert.assertEquals(found.size(), 1);
            Assert.assertEquals(found.get(0).getTransactionCode(), "55");
        }

    @Test
    public void findTransactionCode01Test() {
        PayrollServices.beginUnitOfWork();
        AchTransactionCode found = AchTransactionCode.findAchTransactionCode("01");
        PayrollServices.commitUnitOfWork();

        Assert.assertNull(found);
    }

    @Test
    public void findTransactionCode37_2Test() {
        PayrollServices.beginUnitOfWork();
        AchTransactionCode found = AchTransactionCode.findAchTransactionCode("37");
        PayrollServices.commitUnitOfWork();

        Assert.assertNotNull(found);
        Assert.assertEquals(found.getAchAccountTypeCd(), ACHBankAccountType.Savings);
        Assert.assertEquals(found.getCreditDebitIndicator(), CreditDebitCode.Debit);
        Assert.assertEquals(found.getIsReturnCode(), false);
    }
}
