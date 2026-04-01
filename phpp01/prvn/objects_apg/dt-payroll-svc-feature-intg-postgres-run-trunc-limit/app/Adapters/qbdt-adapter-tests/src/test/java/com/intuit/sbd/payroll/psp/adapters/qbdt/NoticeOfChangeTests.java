package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Aug 2, 2008
 * Time: 10:41:15 AM
 */
public class NoticeOfChangeTests {

    public static String empName;
    public static String origAccountNumber;
    public static String origRoutingNumber;
    public static String origAccountType;
    public static String origBankName;
    public static String newAccountNumber;
    public static String newRoutingNumber;
    public static String newAccountTypeCode;
    public static String newAccountType;
    public static String newBankName;

    static {
        empName = "Donovan McNabb";
        origAccountNumber = "0011992288";
        origRoutingNumber = "113003842";
        origAccountType = "Savings";
        origBankName = null;
        newAccountNumber = "11111111111222222";
        newRoutingNumber = "123123123";
        newAccountTypeCode = "22";
        newAccountType = "Checking";
        newBankName = null;
    }

    @Before
    public void runBeforeEachTest() {
        empName = "Donovan McNabb";
        origAccountNumber = "0011992288";
        origRoutingNumber = "113003842";
        origAccountType = "Savings";
        origBankName = null;
        newAccountNumber = "11111111111222222";
        newRoutingNumber = "123123123";
        newAccountTypeCode = "22";
        newAccountType = "Checking";
        newBankName = null;

        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testPayrollRejectNOCMultipleEEBankAccounts() {
        setupOffloadAndNOCPayroll(true,"C01");
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
            String requestOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String ofxResponse = QBDTTestHelper.processRequestPayrollError(happyPathOfxObj,ErrorMessages.PayrollRejectNOCMultipleEEBankAccounts());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumber() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumber(empName,origAccountNumber,newAccountNumber),"C01");
    }

    @Test
    public void testNOCCorrected() {
        try {
            setupOffloadAndNOCPayroll(false,"C01");
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNull(ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);
            DomainEntitySet<CompanyEvent> payrollSubmittedWithPendingNOCList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(payrollSubmittedWithPendingNOCList.size(), 0);
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testNOCCorrectedInactiveBA() {
        try {
            setupOffloadAndNOCPayroll(false,"C01");
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();

            IPAYCHK iPaycheck = null;
            for (IPAYCHK ipaychk :  happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK()) {
                if (ipaychk.getIEMPNAME().equals("Donovan McNabb")) {
                    iPaycheck = ipaychk;
                    break;
                }
            }

            iPaycheck.getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111333333");
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNull(ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            //Verify Events
            DomainEntitySet<CompanyEvent> payrollSubmittedWithPendingNOCList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(payrollSubmittedWithPendingNOCList.size(), 0);

            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Inactive, true);
            assertEquals(companyEvents.size(), 1);

            //Verify Transaction Returns
            DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(company).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Open));
            assertEquals(0, transactionReturns.size());
            transactionReturns = TransactionReturn.findTransactionReturns(company).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Resolved)).sort(TransactionReturn.ReturnStatusEffectiveDate().Descending());
            assertEquals(1, transactionReturns.size());

            //Verify Emp BankAccount
            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturns.getFirst());
            EmployeeBankAccount employeeBankAccount = finTxnList.getFirst().getCreditBankAccount().getEmployeeBankAccount();
            assertEquals(BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());
            String accountNumber = employeeBankAccount.getBankAccount().getAccountNumber();

            Paycheck paycheck = finTxnList.getFirst().getPaycheckSplit().getPaycheck();
            DomainEntitySet<FinancialTransaction> financialTransactions = paycheck.getPaycheckSplits().get(0).getFinancialTransactions();
            PayrollServices.commitUnitOfWork();

            DataLoadServices.returnTxns(financialTransactions, "C01", "11111111111222222222222222233");

            happyPathOfxObj = ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            iPaycheck = null;
            for (IPAYCHK ipaychk :  happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK()) {
                if (ipaychk.getIEMPNAME().equals("Donovan McNabb")) {
                    iPaycheck = ipaychk;
                    break;
                }
            }

            iPaycheck.getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID(accountNumber);
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumber("Donovan McNabb", "0011992288", "11111111111222222"));
            ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNull(ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());

            PayrollServices.beginUnitOfWork();

            //Verify Events
            payrollSubmittedWithPendingNOCList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(payrollSubmittedWithPendingNOCList.size(), 1);

            companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Inactive, true);
            assertEquals(companyEvents.size(), 1);
            companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, true);
            assertEquals(companyEvents.size(), 1);

            //Verify Transaction Returns
            transactionReturns = TransactionReturn.findTransactionReturns(company).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Open));
            assertEquals(1, transactionReturns.size());
            transactionReturns = TransactionReturn.findTransactionReturns(company).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Resolved)).sort(TransactionReturn.ReturnStatusEffectiveDate().Descending());
            assertEquals(1, transactionReturns.size());

            //Verify Emp BankAccount
            finTxnList = TransactionReturn.findFinancialTransaction(transactionReturns.getFirst());
            employeeBankAccount = finTxnList.getFirst().getCreditBankAccount().getEmployeeBankAccount();
            assertEquals(BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testNOCBankNameWarning() {
        try {
            setupOffloadAndNOCPayroll(false,"C04");
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            assertNotNull(ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE());
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);
            DomainEntitySet<CompanyEvent> payrollSubmittedWithPendingNOCList = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(payrollSubmittedWithPendingNOCList.size(), 1);
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testNOCEventsResolved() {
        try {
            setupOffloadAndNOCPayroll(false,"C01");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumber("Donovan McNabb", "0011992288", "11111111111222222"));

            happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testLoanC06NOCEventsResolved() {
        try {
            newAccountTypeCode = "52";
            setupOffloadAndNOCPayroll(false,"C06");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumber("Donovan McNabb", "0011992288", "11111111111222222"));

            happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Employee employee = Employee.findEmployee(company, "Donovan McNabb");
            EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, "11111111111222222113003842Savings0");

            assertEquals(BankAccountType.Savings, employeeBankAccount.getBankAccount().getAccountTypeCd());
            assertEquals(ACHBankAccountType.Loan, employeeBankAccount.getBankAccount().getACHAccountTypeCd());

            DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(company);
            assertEquals(1, transactionReturns.size());

            TransactionReturn transactionReturn = transactionReturns.getFirst();
            assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testLedgerC06NOCEventsResolved() {
        try {
            newAccountTypeCode = "42";
            setupOffloadAndNOCPayroll(false,"C06");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumber("Donovan McNabb", "0011992288", "11111111111222222"));

            happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Employee employee = Employee.findEmployee(company, "Donovan McNabb");
            EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, "11111111111222222113003842Savings0");

            assertEquals(BankAccountType.Savings, employeeBankAccount.getBankAccount().getAccountTypeCd());
            assertEquals(ACHBankAccountType.Ledger, employeeBankAccount.getBankAccount().getACHAccountTypeCd());

            DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(company);
            assertEquals(1, transactionReturns.size());

            TransactionReturn transactionReturn = transactionReturns.getFirst();
            assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testLoanC07NOCEventsResolved() {
        try {
            newAccountTypeCode = "52";
            setupOffloadAndNOCPayroll(false,"C07");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumber("Donovan McNabb", "0011992288", "113003842", "11111111111222222", "123123123"));

            happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID("123123123");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Employee employee = Employee.findEmployee(company, "Donovan McNabb");
            EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, "11111111111222222123123123Savings0");

            assertEquals(BankAccountType.Savings, employeeBankAccount.getBankAccount().getAccountTypeCd());
            assertEquals(ACHBankAccountType.Loan, employeeBankAccount.getBankAccount().getACHAccountTypeCd());

            DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(company);
            assertEquals(1, transactionReturns.size());

            TransactionReturn transactionReturn = transactionReturns.getFirst();
            assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testLedgerC07NOCEventsResolved() {
        try {
            newAccountTypeCode = "42";
            setupOffloadAndNOCPayroll(false,"C07");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processRequestPayrollError(happyPathOfxObj, ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumber("Donovan McNabb", "0011992288", "113003842", "11111111111222222", "123123123"));

            happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID("11111111111222222");
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID("123123123");
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            events = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Employee employee = Employee.findEmployee(company, "Donovan McNabb");
            EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, "11111111111222222123123123Savings0");

            assertEquals(BankAccountType.Savings, employeeBankAccount.getBankAccount().getAccountTypeCd());
            assertEquals(ACHBankAccountType.Ledger, employeeBankAccount.getBankAccount().getACHAccountTypeCd());

            DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(company);
            assertEquals(1, transactionReturns.size());

            TransactionReturn transactionReturn = transactionReturns.getFirst();
            assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testNOCLedgerAccount() {
        try {
            newAccountTypeCode = "42";
            setupOffloadAndNOCPayroll(false,"C05");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Paycheck paycheck = Paycheck.findPaycheck(company, "3");
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = paycheck.getPaycheckSplits().get(0).getFinancialTransactions()
                .get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit));
            assertEquals("42", entryDetailRecords.get(0).getRecordData().substring(1,3));
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testNOCLoanAccount() {
        try {
            newAccountTypeCode = "52";
            setupOffloadAndNOCPayroll(false,"C05");
            OFXDataloader ofxDataloader = new OFXDataloader();

            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
            assertEquals(company.getCompanyServiceCollection().iterator().next().getStatusCd(),ServiceSubStatusCode.ActiveCurrent);

            DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.NOC, CompanyEventStatus.Active, null, null);
            assertEquals(events.size(), 0);

            Paycheck paycheck = Paycheck.findPaycheck(company, "3");
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = paycheck.getPaycheckSplits().get(0).getFinancialTransactions()
                .get(0).getMoneyMovementTransaction().getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit));
            assertEquals("52", entryDetailRecords.get(0).getRecordData().substring(1,3));
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testPayrollRejectNOCMultipleEEBankAccounts1() throws Exception {
        testPayrollRejectNOCMultipleEEBankAccounts(new SpcfMoney("40.00"), new SpcfMoney("153.11"));
    }

    @Test
    public void testPayrollRejectNOCMultipleEEBankAccounts2() throws Exception {
        testPayrollRejectNOCMultipleEEBankAccounts(new SpcfMoney("927.69"), new SpcfMoney("40.00"));
    }

    @Test
    public void testPayrollRejectNOCMultipleEEBankAccounts3() throws Exception {
        testPayrollRejectNOCMultipleEEBankAccounts(new SpcfMoney("927.69"), new SpcfMoney("153.11"));
    }

    private void testPayrollRejectNOCMultipleEEBankAccounts(SpcfMoney mmtAmount1, SpcfMoney mmtAmount2) throws Exception {
        setupOffloadAndNOCPayroll(true,"C01");

        OFXDataloader ofxDataloader = new OFXDataloader();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        DomainEntitySet<TransactionReturn> txReturns = TransactionReturn.findTransactionReturns(assertOne(payrollRuns).getSourcePayRunId(), company);

        String txId = assertOne(assertOne(txReturns.find(TransactionReturn.MoneyMovementTransaction().MoneyMovementTransactionAmount().equalTo(new SpcfMoney(mmtAmount1)))).getMoneyMovementTransaction().getFinancialTransactionCollection()).getId().toString();
        PayrollServices.financialTransactionManager.updateBankReturnStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), txId, TransactionReturnStatusCode.Resolved, "");
        txId = assertOne(assertOne(txReturns.find(TransactionReturn.MoneyMovementTransaction().MoneyMovementTransactionAmount().equalTo(new SpcfMoney(mmtAmount2)))).getMoneyMovementTransaction().getFinancialTransactionCollection()).getId().toString();
        PayrollServices.financialTransactionManager.updateBankReturnStatus
                (SourceSystemCode.QBDT, company.getSourceCompanyId(), txId, TransactionReturnStatusCode.Resolved, "");

        DomainEntitySet<CompanyEvent> NOCList = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.NOC, CompanyEventStatus.Active, true);
        CompanyEvent nocEvent = assertOne(NOCList);
        String empName = nocEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeName);
        String newAccount = nocEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewAccountNumber);
        String oldAccount = nocEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber);
        PayrollServices.commitUnitOfWork();

        OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
        QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
        OFXManager.javaRequestToOFX(happyPathOfxObj, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        QBDTTestHelper.processRequestPayrollError(happyPathOfxObj,ErrorMessages.PayrollRejectNOCEEBankAccountNumber(empName, oldAccount, newAccount));
    }

    @Test
    public void testPayrollRejectNOCEEBankRoutingNumber() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankRoutingNumber(empName,origRoutingNumber,newRoutingNumber),"C02");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountType() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountType(empName,origAccountType,newAccountType),"C05");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberRoutingNumber() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumber(empName,origAccountNumber,origRoutingNumber,newAccountNumber,newRoutingNumber),"C03");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberAccountType() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumberAccountType(empName,origAccountNumber,origAccountType,newAccountNumber,newAccountType),"C06");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType(empName,origAccountNumber,origRoutingNumber,origAccountType,newAccountNumber,newRoutingNumber,newAccountType),"C07");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberBankName() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumber(empName,origAccountNumber,newAccountNumber),"C04","C01");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberBankName2() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCMultipleEEBankAccounts(),"C01","C04");
    }

    @Test
    public void testPayrollRejectNOCEEBankRoutingNumberBankName() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankRoutingNumber(empName,origRoutingNumber,newRoutingNumber),"C04","C02");
    }

    @Test
    public void testPayrollRejectNOCEEBankRoutingNumberBankName2() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCMultipleEEBankAccounts(),"C02","C04");
    }

    @Test
    public void testPayrollRejectNOCEEBankRoutingNumberAccountType() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankRoutingNumberAccountType(empName,origRoutingNumber,origAccountType,newRoutingNumber,newAccountType),"C02","C05");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberRoutingNumberBankName() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumber(empName,origAccountNumber,origRoutingNumber,newAccountNumber,newRoutingNumber),"C04","C03");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberRoutingNumberBankName2() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCMultipleEEBankAccounts(),"C03","C04");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberRoutingNumberAccountTypeBankName() {
        runPayrollReject(ErrorMessages.PayrollRejectNOCEEBankAccountNumberRoutingNumberAccountType(empName,origAccountNumber,origRoutingNumber,origAccountType,newAccountNumber,newRoutingNumber,newAccountType),"C01","C02","C04","C05");
    }

    @Test
    public void testPayrollRejectNOCEEBankAccountNumberNoChange() {
        newAccountNumber = origAccountNumber;
        setupOffloadAndNOCPayrollNoChange("C01");

        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);

        DomainEntitySet<CompanyEvent> NOCList = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.NOC, CompanyEventStatus.Active, true);
        assertEquals(NOCList.size(), 0);

        NOCList = CompanyEvent.findCompanyEvents
                (company, EventTypeCode.NOCWithOutChanges, CompanyEventStatus.Active, true);
        assertEquals(NOCList.size(), 1);
    }

    @Test
    public void testNOCRejectOnFirstPayroll() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        DataLoadServices.setPSPDate(2012, 8, 1);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // date dto zero based month
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 8, 8));
        PayrollRun payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(company, 2012, 8, 6);

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> employeeFinancialTransactions = payrollRun.getFinancialTransactionCollection()
                  .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeDdCredit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed)));
        assertEquals(1, employeeFinancialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

        String newAccountNumber = "12345";
        DataLoadServices.returnTxns(employeeFinancialTransactions, "C01", newAccountNumber);

        company = DataLoadServices.refreshCompany(company);
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(company, payrollRunDTO, false, false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofx = QBDTTestHelper.submitQBDTRequest(payrollOfx, false);

        assertTrue("Noc error message 2301 not found", ofx.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE().contains("Message Code 2301"));

        payrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setACCTID(newAccountNumber);
        QBDTTestHelper.submitQBDTRequest(payrollOfx);
    }



    private void runPayrollReject(ErrorMessage errMsg,String... nocCodesEachOffload) {
        setupOffloadAndNOCPayroll(false,nocCodesEachOffload);
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
            String requestOfxStr = OFXManager.javaRequestToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String ofxResponse = QBDTTestHelper.processRequestPayrollError(happyPathOfxObj,errMsg);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public static void setupOffloadAndNOCPayroll(boolean offloadMoreThanOne,String... nocCodes) {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
            String requestOfxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String dmPaycheckId=null;
            String alPaycheckId=null;

            for (IPAYROLLRUN payrollRun : happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
                for (IPAYCHK paycheck : payrollRun.getIPAYCHK()) {
                    if (paycheck.getIEMPNAME().compareTo(OFXDataloader.dmEmpName)==0) {
                        dmPaycheckId=paycheck.getIPAYCHKID();
                    }
                    if (offloadMoreThanOne) {
                        if (paycheck.getIEMPNAME().compareTo(OFXDataloader.alEmpName)==0) {
                           alPaycheckId=paycheck.getIPAYCHKID();
                        }
                    }

                }
            }
            assertNotNull(dmPaycheckId);
            if (offloadMoreThanOne) {
                assertNotNull(alPaycheckId);
            }

            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
//            QBDTRequestProcessor qbdtRequestProcessor= new QBDTRequestProcessor();
//            String ofxResponseStr = qbdtRequestProcessor.processRequest(requestOfxStr);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);

            QBDTTestHelper.runOffload(happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());

            for (String nocCode : nocCodes) {
                PayrollServices.beginUnitOfWork();
                ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
                DomainEntitySet<FinancialTransaction> coFinTxns = FinancialTransaction
                        .findFinancialTransactions(SourceSystemCode.QBDT, OFXDataloader.companyPSID,
                                TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);


                DomainEntitySet<FinancialTransaction> fnTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
                for (FinancialTransaction fnTx : coFinTxns) {
                    if (fnTx.getCreditBankAccount().getAccountNumber().compareTo(OFXDataloader.DM_ACCT_ID) == 0) {
                        if (fnTx.getPaycheckSplit().getPaycheck().getSourcePaycheckId().compareTo(dmPaycheckId) == 0) {
                            fnTxnsToReturn.add(fnTx);
                        }
                    }
                    else {
                        if (offloadMoreThanOne) {
                            if (fnTx.getPaycheckSplit().getPaycheck().getSourcePaycheckId().compareTo(alPaycheckId) == 0) {
                                fnTxnsToReturn.add(fnTx);
                            }
                        }
                    }
                }
                if (offloadMoreThanOne) {
                    assertEquals(3, fnTxnsToReturn.size());
                }
                else {
                    assertEquals(1, fnTxnsToReturn.size());
                }

//                offsetMap.put("C01", new int[][]{{0, 17}, null, null, null});
//                offsetMap.put("C02", new int[][]{null, {0, 9}, null, null});
//                offsetMap.put("C03", new int[][]{{12, 29}, {0, 9}, null, null});
//                offsetMap.put("C04", new int[][]{null, null, null, {0, 22}});
//                offsetMap.put("C05", new int[][]{null, null, {0, 2}, null});
//                offsetMap.put("C06", new int[][]{{0, 17}, null, {20, 22}, null});
//                offsetMap.put("C07", new int[][]{{9, 26}, {0, 9}, {26, 28}, null});

                String errDesc = null;
                if (nocCode.compareTo("C01") == 0) {
                    errDesc = newAccountNumber + "222222222233";
                }
                if (nocCode.compareTo("C02") == 0) {
                    errDesc = newRoutingNumber;
                }
                if (nocCode.compareTo("C03") == 0) {
                    errDesc = newRoutingNumber + "   " + newAccountNumber + "222222222233";
                }
                if (nocCode.compareTo("C04") == 0) {
                    errDesc = newAccountNumber + "89012";
                }
                if (nocCode.compareTo("C05") == 0) {
                    errDesc = newAccountTypeCode;
                }
                if (nocCode.compareTo("C06") == 0) {
                    errDesc = newAccountNumber + "   " + newAccountTypeCode;
                }
                if (nocCode.compareTo("C07") == 0) {
                    errDesc = newRoutingNumber + newAccountNumber + newAccountTypeCode;
                }
                if (nocCode.compareTo("C02") == 0) {
                    errDesc = newRoutingNumber;
                }
                DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(fnTxnsToReturn, nocCode, errDesc);

                PayrollServices.commitUnitOfWork();

                for (TransactionReturn transactionReturn : returnList) {
                    Application.beginUnitOfWork();
                    transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
                    TransactionReturnHandler returnHandler = TransactionReturnHandler.
                            getTransactionReturnHandler(transactionReturn);
                    DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);
                    FinancialTransaction finTxn = finTxnList.get(0);
                    BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
                    String oldAccountNumber = bankAccount.getAccountNumber();
                    returnHandler.execute(transactionReturn);
                    Application.commitUnitOfWork();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public static void setupOffloadAndNOCPayrollNoChange(String... nocCodes) {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1UseNextCompanyPaycheckIds();
            QBDTTestHelper.updateOFXWithNextCompanyToken(happyPathOfxObj);
            String dmPaycheckId=null;

            for (IPAYROLLRUN payrollRun : happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
                for (IPAYCHK paycheck : payrollRun.getIPAYCHK()) {
                    if (paycheck.getIEMPNAME().compareTo(OFXDataloader.dmEmpName)==0) {
                        dmPaycheckId=paycheck.getIPAYCHKID();
                    }
                }
            }
            assertNotNull(dmPaycheckId);

            String ofxResponseStr = QBDTTestHelper.processOFXRequestSuccess(happyPathOfxObj);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponseStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String signOnResponseCode = ofxResponseObj.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY();
            TestCase.assertEquals(QBOFX.MESSAGE_SEVERITY.INFO,signOnResponseCode);

            QBDTTestHelper.runOffload(happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIDTPAYCHKS());

            for (String nocCode : nocCodes) {
                PayrollServices.beginUnitOfWork();
                DomainEntitySet<FinancialTransaction> coFinTxns = FinancialTransaction
                        .findFinancialTransactions(SourceSystemCode.QBDT, OFXDataloader.companyPSID,
                                TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

                DomainEntitySet<FinancialTransaction> fnTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
                for (FinancialTransaction fnTx : coFinTxns) {
                    if (fnTx.getCreditBankAccount().getAccountNumber().compareTo(OFXDataloader.DM_ACCT_ID) == 0) {
                        if (fnTx.getPaycheckSplit().getPaycheck().getSourcePaycheckId().compareTo(dmPaycheckId) == 0) {
                            fnTxnsToReturn.add(fnTx);
                        }
                    }
                }

                assertEquals(1, fnTxnsToReturn.size());

                String errDesc = null;
                if (nocCode.compareTo("C01") == 0) {
                    errDesc = origAccountNumber + "       ";
                }
                if (nocCode.compareTo("C02") == 0) {
                    errDesc = origRoutingNumber;
                }
                if (nocCode.compareTo("C03") == 0) {
                    errDesc = origRoutingNumber + "   " + origAccountNumber + "       ";
                }
                if (nocCode.compareTo("C04") == 0) {
                    errDesc = origAccountNumber + "89012";
                }
                if (nocCode.compareTo("C05") == 0) {
                    errDesc = newAccountTypeCode;
                }
                if (nocCode.compareTo("C06") == 0) {
                    errDesc = origAccountNumber + "82222";
                }
                if (nocCode.compareTo("C07") == 0) {
                    errDesc = origRoutingNumber + origAccountNumber + newAccountTypeCode;
                }
                if (nocCode.compareTo("C02") == 0) {
                    errDesc = origRoutingNumber;
                }
                DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(fnTxnsToReturn, nocCode, errDesc);

                PayrollServices.commitUnitOfWork();

                for (TransactionReturn transactionReturn : returnList) {
                    Application.beginUnitOfWork();
                    transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
                    TransactionReturnHandler returnHandler = TransactionReturnHandler.
                            getTransactionReturnHandler(transactionReturn);
                    returnHandler.execute(transactionReturn);
                    Application.commitUnitOfWork();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}