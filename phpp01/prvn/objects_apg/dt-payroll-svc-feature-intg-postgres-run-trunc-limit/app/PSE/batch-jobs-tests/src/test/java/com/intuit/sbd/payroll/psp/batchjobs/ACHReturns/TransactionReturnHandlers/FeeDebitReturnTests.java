package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessMonthlyFeesTask;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 2:00:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeeDebitReturnTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();

        // set the PSP date for the test data
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

    }

    @After
    public void runAfterEachTest() {
        PayrollServices.rollbackUnitOfWork();
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFeeDebitReturnNSF() {
        ACHReturnsDataLoader.loadData2DayCompanyRequests1TxnReversedFeeReturned("R01", "This is an NSF description");
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

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent feeReturnEvent = null;
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the NSF.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        // Was the company put on hold for the NSF.
        DomainEntitySet<OnHoldReason> reasons = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertTrue("On Hold Reason", onHoldReasonsContainsSubStatusCode(reasons, ServiceSubStatusCode.AchRejectR1R9));

        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.SalesTaxReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 0, events.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testQBDTFeeDebitReturnNonNSF() {
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversedFeeReturned("R02", "This is a non-NSF description");
        FinancialTransaction returnedFeeFT = null;
        FinancialTransaction returnedSalesTaxFT = null;
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

        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Returned);
        for (FinancialTransaction currTxn : c2FinTxns) {
            if (TransactionTypeCode.ServiceSalesAndUseTax == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    returnedSalesTaxFT = currTxn;
                }
            }
        }

        assertNotNull(returnedSalesTaxFT);

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
            assertEquals("Financial transaction", returnedFeeFT.getId().toString(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        }

        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.SalesTaxReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.SalesTaxReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
            assertEquals("Financial transaction", returnedSalesTaxFT.getId().toString(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        }

        // Verify the fee was not charged for the return.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        // Was the company put on hold for the non-NSF return.
        DomainEntitySet<OnHoldReason> reasons = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertTrue("On Hold Reason", onHoldReasonsContainsSubStatusCode(reasons, ServiceSubStatusCode.AchRejectOther));

        Application.commitUnitOfWork();
    }

    @Test
    public void testQBDTMonthlyFeeReturnNSF() throws Exception {

        // Set the PSPDate to 6/1/2007 in preparation for company creation
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        // Create the company (internally uses a date of 20070601)
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        // Add an employee
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);
        
        PayrollServices.beginUnitOfWork();
        
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        // Set the PSPDate to 11/1/2012 in preparation for billing
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        // Call the ProcessMonthlyFeesTask
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();

        // Return the monthly fee.
        Application.beginUnitOfWork();
        FinancialTransaction monthlyFee = null;
        DomainEntitySet<FinancialTransaction> finTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.MonthlyFee == osc) {
                    monthlyFee = currTxn;
                }
            }
        }

        assertNotNull("Found Monthly Fee", monthlyFee);
        finTxnsToReturn.add(monthlyFee);

        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(finTxnsToReturn, "R01", "NSF return");

        // Verify the ReDebit transactions.
        for(FinancialTransaction trans : finTxnsToReturn) {
            assertNotNull(FinancialTransaction.getPendingRedebitTransaction(trans.getId().toString()));
        }

        // Verify the correct fee was charged for the NSF.
        verifyFeeOnlyNsfFee(company, TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, true);

        // Was the company put on hold for the NSF.
        DomainEntitySet<OnHoldReason> reasons = getNonExpiredOnHoldReasonList(monthlyFee);
        assertTrue("On Hold Reason Exists", onHoldReasonsContainsSubStatusCode(reasons, ServiceSubStatusCode.AchRejectR1R9));

    }

    @Test
    public void testQBDTMonthlyFeeReturnNonNSF() throws Exception {

        // Set the PSPDate to 6/1/2007 in preparation for company creation
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 1));
        PayrollServices.commitUnitOfWork();

        // Create the company (internally uses a date of 20070601)
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "19670404", "987654321", true, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        // Add an employee
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();

        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        // Submit a payroll to move the DD service to ActiveCurrent (from PendingFirstPayroll)
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("Submit Payroll", processResult);

        PayrollServices.commitUnitOfWork();

        // Set the PSPDate to 11/1/2012 in preparation for billing
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 1));
        PayrollServices.commitUnitOfWork();

        // The billing period must be after 9/29 (PSP R9 release) to allow PSP to handle MonthlyFee billing
        SpcfCalendar billingPeriod = CalendarUtils.getLastDayOfMonth(SpcfCalendar.createInstance(2012, 10, 1));

        // Call the ProcessMonthlyFeesTask
        ProcessMonthlyFeesTask task = new ProcessMonthlyFeesTask(company.getId().toString(), billingPeriod).call();

        // Return the monthly fee.
        Application.beginUnitOfWork();
        FinancialTransaction monthlyFee = null;
        DomainEntitySet<FinancialTransaction> finTxnsToReturn = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                           TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.MonthlyFee == osc) {
                    monthlyFee = currTxn;
                }
            }
        }

        assertNotNull("Found Monthly Fee", monthlyFee);
        finTxnsToReturn.add(monthlyFee);

        PSPDate.setPSPTime("20071010000000");
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(finTxnsToReturn, "R02", "This is a non-NSF description");

        // Verify that there are no ReDebit transactions.
        for(FinancialTransaction trans : finTxnsToReturn) {
            assertNull(FinancialTransaction.getPendingRedebitTransaction(trans.getId().toString()));
        }

        // Verify the fee was not charged for the return.
        verifyFeeOnlyNsfFee(company, TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        // Was the company put on hold for the return.
        DomainEntitySet<OnHoldReason> reasons = getNonExpiredOnHoldReasonList(monthlyFee);
        assertTrue("On Hold Reason Exists", onHoldReasonsContainsSubStatusCode(reasons, ServiceSubStatusCode.AchRejectOther));

    }

    @Test
    public void testQBDTFeeDebitReturnNSF() {
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversedFeeReturned("R01", "This is an NSF description");
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

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent feeReturnEvent = null;
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the NSF.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        //verify OnHold WAS created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertTrue("On Hold Reason", onHoldReasonsContainsSubStatusCode(onHoldReasonList, ServiceSubStatusCode.AchRejectR1R9));

        Application.commitUnitOfWork();
    }

    private DomainEntitySet<OnHoldReason> getNonExpiredOnHoldReasonList(FinancialTransaction pFT) {
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                       .Where(OnHoldReason.Company().equalTo(pFT.getCompany())
                              .And(OnHoldReason.ExpirationDate().isNull()))
                       .OrderBy(OnHoldReason.OnHoldReasonCd());

        return Application.find(OnHoldReason.class, query);
    }

    @Test
    public void testQBDTFeeRedebitReturnNSF() {
        ACHReturnsDataLoader.loadQBDTCompRevFeeRetFeeRedRet("R01", "This is an NSF description");

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Returned);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        assertEquals("Number of fee redebits returned", 1, allFinTxns.size());
        FinancialTransaction returnedFeeFT = Application.findById(FinancialTransaction.class, allFinTxns.get(0).getId());

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 2, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the NSF.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        //verify onhold is created with code AchRejectR01R090
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

    }

    @Test
    public void testFeeRedebitReturnNSF() {
        ACHReturnsDataLoader.loadDataCompRevFeeRetFeeRedRet("R01", "This is an NSF description");

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Returned);

        assertEquals("Number of fee redebits returned", 1, allFinTxns.size());
        FinancialTransaction returnedFeeFT = allFinTxns.get(0);

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 2, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the NSF.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        //verify OnHold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertTrue("On Hold Status", onHoldReasonsContainsSubStatusCode(onHoldReasonList, ServiceSubStatusCode.AchRejectR1R9));

        //todo should we do this?
        // assertTrue(onHoldReasonsContainsSubStatusCode(onHoldReasonList, ServiceSubStatusCode.RiskAssessment));

        Application.commitUnitOfWork();
    }

    @Test
    public void testFeeRedebitReturnNonNSF() {
        ACHReturnsDataLoader.loadDataCompRevFeeRetFeeRedRet("R02", "This is a non-NSF description");

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allFinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Returned);

        assertEquals("Number of fee redebits returned", 1, allFinTxns.size());
        FinancialTransaction returnedFeeFT = allFinTxns.get(0);

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 2, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the return.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        //verify OnHold is created with code AchRejectOther
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedFeeFT);
        assertTrue("On Hold Status", onHoldReasonsContainsSubStatusCode(onHoldReasonList, ServiceSubStatusCode.AchRejectOther));
        Application.commitUnitOfWork();
    }

    @Test
    public void testFeeDebitReturnNonNSF() {
        ACHReturnsDataLoader.loadData2DayCompanyRequests1TxnReversedFeeReturned("R02", "This is a non-NSF description");
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

        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedFeeFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent feeReturnEvent = null;
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        // Verify the fee was not charged for the return.
        verifyFeeOnlyNsfFee(returnedFeeFT.getCompany(), TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);

        Application.commitUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyFeeDebitReturnNSF() {
        String sourceCompanyId = "8574536";

        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversedFeeReturned(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R01", "This is an NSF description");

        //Validation
        Application.beginUnitOfWork();

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);

        // Verify Reversal Fee created
        transactionReturnVerifier.verifyFeeCreated(returnedFinancialTransactions, OfferingServiceChargeType.ReversalFee);

        // Verify No Debit Return Fee is created
        transactionReturnVerifier.verifyNoDebitReturnFeeForFeeOnlyNSF(company);

        transactionReturnVerifier.verifyFeeReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifyCompanyOnHold(company, ServiceSubStatusCode.AchRejectR1R9);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyFeeRedebitReturnNSF() {
        String sourceCompanyId = "8574536";

        ACHReturnsDataLoader.loadQBDTCompRevFeeRetFeeRedRet(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R01", "This is an NSF description");

        Application.beginUnitOfWork();

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, sourceCompanyId,
                        TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Returned);

        transactionReturnVerifier.verifyDebitReturnedFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyFeeReturnCompanyEvent(company, 2);

        transactionReturnVerifier.verifyNoDebitReturnFeeForFeeOnlyNSF(company);

        transactionReturnVerifier.verifyCompanyOnHold(company, ServiceSubStatusCode.AchRejectR1R9);

        Application.rollbackUnitOfWork();

    }

    @Test
    public void testQBDTSymphonyFeeDebitReturnNonNSF() {
        String sourceCompanyId = "8574536";

        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversedFeeReturned(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R02", "This is a non-NSF description");
        // Validation
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyFeeAndSalesTaxReturnForReversal(company);

        transactionReturnVerifier.verifyFeeReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifySalexTaxReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifyNoDebitReturnFeeForFeeOnlyNSF(company);

        transactionReturnVerifier.verifyCompanyOnHold(company, ServiceSubStatusCode.AchRejectOther);

        Application.rollbackUnitOfWork();
    }

    private FinancialTransaction findFeeFT(DomainEntitySet<FinancialTransaction> pFTs) {
        for (FinancialTransaction ft : pFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit) {
                return ft;
            }
        }
        return null;
    }
    
    private boolean onHoldReasonsContainsSubStatusCode(DomainEntitySet<OnHoldReason> reasons, ServiceSubStatusCode subStatusCode) {

        for(OnHoldReason reason : reasons) {
            if(reason.getOnHoldReasonCd().equals(subStatusCode)) {
                return true;
            }
        }

        return false;
    }
    
    private void verifyFeeOnlyNsfFee( Company company, TransactionTypeCode txType, TransactionStateCode txState, boolean charged ) {

        // Query to find the qualifying transactions.
        DomainEntitySet<FinancialTransaction> nsfFeeTx = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(), txType, txState);
        // Get the fee so we can compare amounts.
        Fee fee = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.FeeOnlyNSFFee)).getFirst();

        boolean foundFee = false;
        for (FinancialTransaction ft : nsfFeeTx) {
            if(ft.getFinancialTransactionAmount().equals(fee.getAmount())) {
                foundFee = true;
                break;
            }
        }
        assertTrue("Fee Only NSF Fee", foundFee == charged);
    }
}
