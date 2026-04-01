package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.bp.wc.common.schema.Payroll;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.v4.integration.App;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by cmehta1 on 8/14/18.
 */
public class SymphonyVoidTests {

    @Test
    public void testSymphonyVoidSinglePaycheckBeforeOffload() {

        try {
            SIGNONMSGSRQV1 signOnMsg = null;
            PayrollServicesTest.beforeEachTest();
            Application.truncateTables();
            ApplicationSecondary.truncateTables();
            PayrollServices.beginUnitOfWork();
            CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
            PSPDate.setPSPTime("20070822000000");
            Company company= companyQB1DataLoader.persistQBCompanySymphony();
            ObjectFactory objFact = new ObjectFactory();
            signOnMsg = objFact.createSIGNONMSGSRQV1();
            SONRQ signOnRequest = objFact.createSONRQ();
            signOnRequest.setUSERID(company.getSourceCompanyId());
            signOnMsg.setSONRQ(signOnRequest);

            PayrollServices.commitUnitOfWork();
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(payrollReqMsgOFXRoot);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
            assertEquals(0,erDDDebits.size());
            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransactions(company,TransactionTypeCode.EmployerDdDebit,TransactionTypeCode.EmployeeDdCredit,TransactionTypeCode.EmployerFeeDebit);

           LinkedHashSet<PayrollRun> payrollRunLinkedHashSet = new LinkedHashSet<>();
            for(FinancialTransaction financialTransaction : financialTransactions){
                PayrollRun payrollRun = financialTransaction.getPayrollRun();
                System.out.println(payrollRun);
                payrollRunLinkedHashSet.add(payrollRun);
                SpcfDecimal payrollAmt = payrollRun.getPayrollDirectDepositAmount();
                System.out.println(payrollAmt.toString());
                assertFalse(TransactionStateCode.Cancelled.equals(financialTransaction.getCurrentFinancialTransactionState()));
                Assert.assertNotNull(financialTransaction.getMoneyMovementTransaction());
            }
            ArrayList<PayrollRun> payrollRunArrayList = new ArrayList<>();

            for(PayrollRun payrollRun : payrollRunLinkedHashSet){

                payrollRunArrayList.add(payrollRun);
            }
            SpcfMoney payrollAmt = payrollRunArrayList.get(0).getPayrollDirectDepositAmount();
            assertEquals(10203.55,Double.parseDouble(payrollAmt.toString()));
            payrollAmt = payrollRunArrayList.get(1).getPayrollDirectDepositAmount();
            assertEquals(1120.80,Double.parseDouble(payrollAmt.toString()));

            PaychecklVoidTest paychecklVoidTest = new PaychecklVoidTest();
            Application.commitUnitOfWork();
            paychecklVoidTest.voidPaycheckFundsRecovered(payrollReqMsgOFXRoot,ofxResponseObj,1, true,true);
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebitCancelled = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
            assertEquals(1,erDDDebitCancelled.size());
            DomainEntitySet<FinancialTransaction> eECreditCancelled = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
            assertEquals(1,eECreditCancelled.size());
            assertEquals("PayrollRunStatus", PayrollStatus.Pending, erDDDebitCancelled.getFirst().getPayrollRun().getPayrollRunStatus());;
            PayrollRun voidedPayrollRun=erDDDebitCancelled.getFirst().getPayrollRun();
            assertEquals("Payroll Amount After Voiding Paycheck","193.11",voidedPayrollRun.getPayrollDirectDepositAmount().toString());
            Application.commitUnitOfWork();



        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }


    @Test
    public void qbdtNewPayrollRunFlow() {

        try {
            SIGNONMSGSRQV1 signOnMsg = null;
            PayrollServicesTest.beforeEachTest();
            Application.truncateTables();
            ApplicationSecondary.truncateTables();
            PayrollServices.beginUnitOfWork();
            CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
            PSPDate.setPSPTime("20070822000000");
            Company company= companyQB1DataLoader.persistQBCompanySymphony();
            ObjectFactory objFact = new ObjectFactory();
            signOnMsg = objFact.createSIGNONMSGSRQV1();
            SONRQ signOnRequest = objFact.createSONRQ();
            signOnRequest.setUSERID(company.getSourceCompanyId());
            signOnMsg.setSONRQ(signOnRequest);

            PayrollServices.commitUnitOfWork();
            // Process a sync request to handle the bank events.
            QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX payrollReqMsgOFXRoot = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();
            String payrollRespMsgOFXStr = QBDTTestHelper.processOFXRequestSuccess(payrollReqMsgOFXRoot);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(payrollRespMsgOFXStr, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
            assertEquals(0,erDDDebits.size());
            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransactions(company,TransactionTypeCode.EmployerDdDebit,TransactionTypeCode.EmployeeDdCredit,TransactionTypeCode.EmployerFeeDebit);
            for(FinancialTransaction financialTransaction : financialTransactions){

                assertFalse(TransactionStateCode.Cancelled.equals(financialTransaction.getCurrentFinancialTransactionState()));
                Assert.assertNotNull(financialTransaction.getMoneyMovementTransaction());
            }
            Application.commitUnitOfWork();
            //paychecklVoidTest.voidPaycheckFundsRecovered(payrollReqMsgOFXRoot,ofxResponseObj,1, true,true);
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebitCreated = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
            assertEquals(2,erDDDebitCreated.size());
            DomainEntitySet<FinancialTransaction> eECreditCreated = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
            assertEquals(6,eECreditCreated.size());
            assertEquals("PayrollRunStatus", PayrollStatus.Pending, erDDDebitCreated.getFirst().getPayrollRun().getPayrollRunStatus());
            Application.commitUnitOfWork();


        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.toString());
        }
    }

    @Test
    public void voidPaycheckAfterOffload() {

        try{
            SIGNONMSGSRQV1 signOnMsg = null;
            PayrollServicesTest.beforeEachTest();
            Application.truncateTables();
            ApplicationSecondary.truncateTables();
            PayrollServices.beginUnitOfWork();
            CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();
            PSPDate.setPSPTime("20070822000000");
            Company company= companyQB1DataLoader.persistQBCompanySymphony();
            ObjectFactory objFact = new ObjectFactory();
            signOnMsg = objFact.createSIGNONMSGSRQV1();
            SONRQ signOnRequest = objFact.createSONRQ();
            signOnRequest.setUSERID(company.getSourceCompanyId());
            signOnMsg.setSONRQ(signOnRequest);
            PayrollServices.commitUnitOfWork();
            // Process a sync request to handle the bank events.
                QBDTTestHelper.processOFXSyncRequestDataRecovery(QBOFX.EMPTY_STR);

            IPAYROLLMSGSRQV1 payrollReqMsgOFXObj = null;
            PayrollServices.beginUnitOfWork();
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX requestOFX = ofxDataloader.loadHappyPathOFX();
            PayrollServices.commitUnitOfWork();

            String responseOFXStr = QBDTTestHelper.offloadCompanyPayroll(requestOFX);
            com.intuit.sbd.payroll.psp.common.ofx.response.OFX responseOFXObj = OFXManager.ofxResponseToJava(responseOFXStr,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            String originalPayrollTxId = responseOFXObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX().get(0).getIPAYROLLTXID();
            //PaychecklVoidTest paychecklVoidTest = new PaychecklVoidTest();
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
            assertEquals(0,erDDDebits.size());
            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransactions(company,TransactionTypeCode.EmployerDdDebit,TransactionTypeCode.EmployeeDdCredit,TransactionTypeCode.EmployerFeeDebit);

            LinkedHashSet<PayrollRun> payrollRunLinkedHashSet = new LinkedHashSet<>();
            for(FinancialTransaction financialTransaction : financialTransactions){
                PayrollRun payrollRun = financialTransaction.getPayrollRun();
                System.out.println(payrollRun);
                payrollRunLinkedHashSet.add(payrollRun);
                SpcfDecimal payrollAmt = payrollRun.getPayrollDirectDepositAmount();
                System.out.println(payrollAmt.toString());
                assertFalse(TransactionStateCode.Cancelled.equals(financialTransaction.getCurrentFinancialTransactionState()));
                Assert.assertNotNull(financialTransaction.getMoneyMovementTransaction());
            }
            ArrayList<PayrollRun> payrollRunArrayList = new ArrayList<>();

            for(PayrollRun payrollRun : payrollRunLinkedHashSet){

                payrollRunArrayList.add(payrollRun);
            }
            SpcfMoney payrollAmt = payrollRunArrayList.get(0).getPayrollDirectDepositAmount();
            assertEquals(10203.55,Double.parseDouble(payrollAmt.toString()));
            payrollAmt = payrollRunArrayList.get(1).getPayrollDirectDepositAmount();
            assertEquals(1120.80,Double.parseDouble(payrollAmt.toString()));

            PaychecklVoidTest paychecklVoidTest = new PaychecklVoidTest();
            Application.commitUnitOfWork();
            payrollReqMsgOFXObj = paychecklVoidTest.runPostOffloadSinglePaycheckTestForSymohony(requestOFX,originalPayrollTxId,true);
            Application.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> erDDDebitCancelled = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
            assertEquals(0,erDDDebitCancelled.size());
            DomainEntitySet<FinancialTransaction> eECreditCancelled = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,"8574536", TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
            assertEquals(0,eECreditCancelled.size());
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
        }

        }

}
