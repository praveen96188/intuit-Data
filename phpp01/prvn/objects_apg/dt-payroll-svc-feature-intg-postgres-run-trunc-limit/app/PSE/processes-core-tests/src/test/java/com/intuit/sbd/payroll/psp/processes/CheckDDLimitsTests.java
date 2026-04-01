/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/CheckDDLimitsTests.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHOffloadRunner;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

public class CheckDDLimitsTests {
    private DataLoader dataloader = new DataLoader();



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
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */
    @Test
    public void testNullCompany() {
        PayrollServices.beginUnitOfWork();
        CheckDDLimits psd = new CheckDDLimits(null, SourceSystemCode.QBOE, null);
        ProcessResult submitDDPayroll = psd.validate();
        PayrollServices.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullSourceSystem() {
        PayrollServices.beginUnitOfWork();
        CheckDDLimits psd = new CheckDDLimits(null, null, "1234567");
        ProcessResult submitDDPayroll = psd.validate();
        PayrollServices.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }


    @Test
    public void testCompanyDNE() {
        PayrollServices.beginUnitOfWork();
        CheckDDLimits psd = new CheckDDLimits(null, SourceSystemCode.QBOE, "1234567");
        ProcessResult submitDDPayroll = psd.validate();
        PayrollServices.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    /***************************************Limit increase tests************************************/

    /**
     * Company exceeds limits by going over the employee maximum but still qualifies for an auto-increase
     */
    @Test
    public void testCompanyQualifiesForAutoIncrease_OverEEMax() {
        /*********************Begin setup******************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        //set up a company with 6 payrolls, one at least 3 months ago
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 4, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-05-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070502", 5);

        /*********************Begin test******************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /*********************Begin verification******************************/

        assertSuccess(checkLimitsResult);

        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        //Ensure correct limit violation count
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        assertEquals("Limit violations", 0L, limitViolationCount);

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("16501.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", ddCompanyServiceInfo.getOverrideCompanyLimitAmount());
        assertEquals("Employee override amount", expectedNewAmount, ddCompanyServiceInfo.getOverrideEmployeeLimitAmount());


        CompanyEvent limitIncreaseEvent = null;

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd().equals(EventTypeCode.DDIncreasePayrollLimit)) {
                limitIncreaseEvent = companyEvent;
            }
        }
        assertEquals("Company", limitExceedingCompany, limitIncreaseEvent.getCompany());
        assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit, limitIncreaseEvent.getEventTypeCd());
        assertEquals("Status code", CompanyEventStatus.Active, limitIncreaseEvent.getStatusCd());

        assertEquals("Limit type: ", EventLimitCode.Employee.toString(), limitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));
        assertEquals("Source payroll id", "BatchTest06", limitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
        assertEquals("Employee", c1DL.employee1.getId().toString(), limitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        assertEquals("Old limit amount", expectedOldAmount.toString(), limitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
        assertEquals("Increased limit amount", expectedNewAmount.toString(), limitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

        PayrollServices.commitUnitOfWork();
    }

    /**
     * The company has exceeded the company-level limits but still qualifies for an auto-increase
     */
    @Test
    public void testQualifiesForIncrease_OverCompanyMax() {
        /************************Begin setup************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 6, 1, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-07-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }
            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070702", 5);

        //Update the company limit to be low enough for the company to exceed it with the test payroll
        // and the employee limit so high that the employees will not exceed it
        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);

        SpcfMoney compOverrideAmount = new SpcfMoney("15000.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("900000.00");

        ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(companyForFindingService.getSourceSystemCd(),
                companyForFindingService.getSourceCompanyId(), compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();
        assertTrue("Update DD Limits", processResult.isSuccess());
        /************************End setup************************/

        Application.beginUnitOfWork();
        /************************Begin Test************************/
        //Reset the test time to be over three months from when we inserted the original payrolls
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();
        /************************End Test************************/

        /************************Persistence verification************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedCompanyOverrideAmount = new SpcfMoney("16666.10");

        assertEquals("Company override amount", expectedCompanyOverrideAmount, companyOverrideAmount);
        assertEquals("Employee override amount", empOverrideAmount, employeeOverrideAmount);

        //Ensure correct limit increase event
        CompanyEvent currEvent = null;


        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd().equals(EventTypeCode.DDIncreasePayrollLimit)) {
                currEvent = companyEvent;
            }
        }

        assertTrue("Limit Increase Company Event", currEvent != null);
        assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
        assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit, currEvent.getEventTypeCd());
        assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

        assertEquals("Limit type: ", EventLimitCode.Company.toString(), currEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));
        assertEquals("Source payroll id", "BatchTest06", currEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
        assertEquals("Old limit amount", compOverrideAmount.toString(), currEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
        assertEquals("Increased limit amount", expectedCompanyOverrideAmount.toString(), currEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));
        Application.commitUnitOfWork();
    }

    /**
     * The company does not qualify for a limit increase because they have less than the minimum number
     * of payrolls required (5 vs 6)
     */
    @Test
    public void testQualifiesForIncrease_TooFewPayrolls() {
        /************************Begin setup************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with only 11 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 5, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 5; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-06-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070602", 5);
        /************************End setup************************/

        /************************Begin test************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /************************End test************************/

        /************************Persistence verification************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("15001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        Application.commitUnitOfWork();
    }

    /**
     * The company does not qualify for a limit increase because they have less than the minimum number
     * of payrolls required (5 vs 6)
     * Rollback CheckDDlimits process, but the violation events should be added to the database
     */
    @Test
    public void testQualifiesForIncrease_TooFewPayrolls_withRollback() {
        /************************Begin setup************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with only 11 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 5, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 5; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-06-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070602", 5);
        /************************End setup************************/

        /************************Begin test************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        PayrollServices.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.rollbackUnitOfWork();

        /************************End test************************/

        /************************Persistence verification************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("15001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        Application.commitUnitOfWork();
    }

    /**
     * The company does NOT qualify for an increase because they have an existing strike, dated less than a year before test time
     */
    @Test
    public void testQualifiesForIncrease_ExistingStrike() {
        /**************************Begin setup***********************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 5, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-06-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070602", 5);

        PayrollServices.beginUnitOfWork();
        c1DL.persistStrike_LessThan1YearAgo();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        /**************************End setup***********************************/

        /**************************Begin test***********************************/
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /**************************Persistence verification***********************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 1L, limitViolationCount);

        int numberOfLimitViolationEvents = 0;
        int numberOfStrikeEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                numberOfLimitViolationEvents++;

                assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
                assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

                SpcfMoney limitAmount = new SpcfMoney("15000.00");
                SpcfMoney violationAmount = new SpcfMoney("15001.00");

                validateLimitViolationEvent(currEvent, limitAmount, violationAmount, "BatchTest06", null,
                        new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
            } else if (currEvent.getEventTypeCd() == EventTypeCode.Strike) {
                numberOfStrikeEvents++;
            }
        }

        assertEquals("Limit violation event count", 1, numberOfLimitViolationEvents);
        assertEquals("Strike event count", 1, numberOfStrikeEvents);
        Application.commitUnitOfWork();
    }

    /**
     * The company does NOT qualify for an increase because they have an existing strike, dated less than a year before test time
     */
    @Test
    public void testQualifiesForIncrease_OtherCompanyHasExistingStrike() {
        /**************************Begin setup***********************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070525000000");
        CompanyQB1DataLoader qb1DataLoader = new CompanyQB1DataLoader();
        qb1DataLoader.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult strikeProcess = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBDT, "8574536", StrikeReason.Manual.toString(), PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
        assertSuccess("Strike proc result", strikeProcess);

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 5, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-06-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070602", 5);
//        PayrollServices.beginUnitOfWork();
//        c1DL.persistStrike_LessThan1YearAgo();
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();
//        PayrollServices.commitUnitOfWork();

        /**************************End setup***********************************/

        /**************************Begin test***********************************/
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /**************************Persistence verification***********************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertSuccess(checkLimitsResult);

        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        int numberOfLimitViolationEvents = 0;
        int numberOfLimitIncreaseEvents = 0;
        int numberOfStrikeEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                numberOfLimitViolationEvents++;

                assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
                assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

                SpcfMoney limitAmount = new SpcfMoney("15000.00");
                SpcfMoney violationAmount = new SpcfMoney("15001.00");

                validateLimitViolationEvent(currEvent, limitAmount, violationAmount, "BatchTest06", null,
                        new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
            } else if (currEvent.getEventTypeCd() == EventTypeCode.Strike) {
                numberOfStrikeEvents++;
            } else if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                numberOfLimitIncreaseEvents++;

                assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
//                assertEquals("Employee", c3DL.employee1.getId().toString(),
//                        CompanyEventBE.getCompanyEventDetailValue(currEvent, EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", "15000.00", currEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", "16501.10", currEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));
                assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
                assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());
            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        assertEquals("Limit violation event count", 0, numberOfLimitViolationEvents);
        assertEquals("Strike event count", 0, numberOfStrikeEvents);
        Application.commitUnitOfWork();
    }

    /**
     * The company qualifies for a limit increase even though they have a strike because that strike is over a year old
     */
    @Test
    public void testQualifiesForIncrease_ExistingStrikeYearAgo() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 3 months from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 5, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-06-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070602", 5);

        PayrollServices.beginUnitOfWork();
        c1DL.persistStrike_MoreThan1YearAgo();
        PayrollServices.commitUnitOfWork();

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("16501.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewAmount, employeeOverrideAmount);
        int numberOfLimitViolationEvents = 0;
        int numberOfStrikeEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;
                numberOfLimitViolationEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c1DL.employee1.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            } else if (currEvent.getEventTypeCd() == EventTypeCode.Strike) {
                numberOfStrikeEvents++;
            }
        }

        assertEquals("Limit violation event count", 1, numberOfLimitViolationEvents);
        assertEquals("Strike event count", 1, numberOfStrikeEvents);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The company qualifies for a limit increase even though they have a strike because that strike is over a year old
     */
    @Test
    public void testQBDTQualifiesForIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 1 month from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070822", 5);
        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("17601.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;
                numberOfLimitIncreaseEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c3DL.employee1.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The company exceeds limits but does not qualify for an increase because its first payroll was less than three months from test time
     */
    @Test
    public void testQualifiesForIncrease_PayrollNotLongEnoughAgo() {
        /****************Begin setup****************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one LESS than 90 days from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 6, 24, 23, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-07-02"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c1DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        ACHOffloadRunner.runAchOffload("20070702", 5);

        /****************Begin test****************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, 22, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /****************Begin persistence verification*********************************/
        //Ensure correct events were input into DB
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 1L, limitViolationCount);

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("15001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }
        Application.commitUnitOfWork();

    }

    /***************************************Company exceeds limits tests************************************/

    /**
     * Company does not exceed limits and thus passes the validation
     */
    @Test
    public void testCompanyDoesNotExceedLimits() {
        /*********************Begin setup******************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        /*********************Begin test******************************/

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /*********************Begin verification******************************/
        assertTrue(checkLimitsResult.isSuccess());
        Application.beginUnitOfWork();

        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);
        Application.commitUnitOfWork();

        //Ensure that limit count for company is zero and that the company has no company events
        assertEquals("Limit violation count", 0L, limitViolationCount);
    }

    /**
     * The company's current payroll exceeds limits
     */
    @Test
    public void testCompanyExceedsLimitsCurrentPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        /****************Begin persistence verification*********************************/
        //Ensure correct events were input into DB
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);
        assertEquals("Limit violation count", 1L, limitViolationCount);

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("300000.00");

            SpcfMoney companyLimitAmount = new SpcfMoney("40000.00");
            SpcfMoney companyViolationAmount = new SpcfMoney("300150.00");

            switch (getEventLimitType(companyEvent)) {
                case Employee:
                case BankAccount:
                    validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest01", null,
                            new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
                    break;

                case Company:
                    validateLimitViolationEvent(companyEvent, companyLimitAmount, companyViolationAmount, "BatchTest01", null,
                            new EventLimitCode[]{EventLimitCode.Company});
                    break;

                default:
                    TestCase.fail("Unexpected limit type");
            }
        }
        Application.commitUnitOfWork();
    }

    /**
     * There is an already-existing payroll that, when combined with the given payroll, causes the company to exceed limits
     */
    @Test
    public void testCompanyExceedsLimitsExistingPayrolls() {
        //Set the system date well before the requirements for cutoff (7 days before)
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistPayrollRun(c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02")));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingExceedsLimits(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest03 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        /****************Begin persistence verification*********************************/
        //Ensure correct events were input into DB
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertEquals("Limit violation count", 1L, limitViolationCount);

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest03", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testCompanyExceedsLimitsWithBackdatedPayroll() {
        //Set the system date well before the requirements for cutoff (7 days before)
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistPayrollRun(c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02")));
        PayrollServices.commitUnitOfWork();

        assertTrue(PSPDate.getPSPTime().after(DateDTO.convertToSpcfCalendar(new DateDTO("2007-09-15"))));
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingExceedsLimits(new DateDTO("2007-09-15"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest03 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        /****************Begin persistence verification*********************************/
        //Ensure correct events were input into DB
        Application.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertEquals("Limit violation count", 1L, limitViolationCount);

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest03", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }
        Application.commitUnitOfWork();
    }

    /**
     * A single employee has multiple paychecks in the same payroll, the sum of which exceed the employee limits for the company
     */
    @Test
    public void testOneEmployeeExceedsLimitsMultiplePaychecks() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_MultiplePaychecksSameEE(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();
        /****************************Begin test***************************************/
        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        //Ensure correct events were input into DB
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());

        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 1L, limitViolationCount);

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * A single employee has multiple paychecks in the same payroll, the sum of which exceed the employee limits for the company
     */
    @Test
    public void testOneEmployeeExceedsLimitsMultiplePaycheckSplits() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistEmployee2BankAccount2();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_MultiplePaycheckSplitsSameEE(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();
        /****************************Begin test***************************************/
        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        //Ensure correct events were input into DB
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 1L, limitViolationCount);

        CompanyEvent currEvent = null;
        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                currEvent = companyEvent;
            }
        }
        assertTrue("Limit Violation Company Event", currEvent != null);
        assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
        assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

        SpcfMoney limitAmount = new SpcfMoney("15000.00");
        SpcfMoney violationAmount = new SpcfMoney("16001.00");

        validateLimitViolationEvent(currEvent, limitAmount, violationAmount, "BatchTest06", c1DL.employee2.getSourceEmployeeId(),
                new EventLimitCode[]{EventLimitCode.Employee});
        PayrollServices.commitUnitOfWork();
    }

    //
    /**
     * The company has exceeded limits so many times (6) that their status is set to suspended
     */
    @Test
    public void testCompanyExceedsLimits_Suspended() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        Application.beginUnitOfWork();
        CheckDDLimits cddL2 = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult2 = cddL2.execute();
        Application.commitUnitOfWork();

        assertEquals("Message size", 1, checkLimitsResult2.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult2.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult2.getMessages().get(0).getMessage());

        Application.beginUnitOfWork();
        CheckDDLimits cddL3 = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult3 = cddL3.execute();
        Application.commitUnitOfWork();

        assertEquals("Message size", 1, checkLimitsResult3.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult3.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult3.getMessages().get(0).getMessage());

        Application.beginUnitOfWork();
        CheckDDLimits cddL4 = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult4 = cddL4.execute();
        Application.commitUnitOfWork();

        assertEquals("Message size", 1, checkLimitsResult4.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult4.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult4.getMessages().get(0).getMessage());

        Application.beginUnitOfWork();
        CheckDDLimits cddL5 = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult5 = cddL5.execute();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult5.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        assertEquals("Service Status", ServiceSubStatusCode.PendingFirstPayroll, ddCompanyService.getStatusCd());
        int limitEventCount = 0;
        for (CompanyEvent companyEvent : CompanyEvent.findCompanyEvents(companyForFindingService)) {
            if (companyEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                limitEventCount++;
            }
        }
        assertEquals("Number of limit violation events", 10, limitEventCount);

        Application.beginUnitOfWork();
        CheckDDLimits cddL6 = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult6 = cddL6.execute();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);


        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        // This assert will fail because we need to check the On Hold Reason codes
        assertTrue(companyForFindingService.getOnHoldReasonCollection().size() == 1);
        Iterator<OnHoldReason> onHoldIterator = companyForFindingService.getOnHoldReasonCollection().iterator();
        OnHoldReason holdReason = onHoldIterator.next();
        assertEquals("Service Status",
                ServiceSubStatusCode.DirectDepositLimit,
                holdReason.getOnHoldReasonCd());
        //3 limit events for each payroll run (company, ee, ee bank account), and 6 payroll runs = 18 
        limitEventCount = 0;
        for (CompanyEvent companyEvent : CompanyEvent.findCompanyEvents(companyForFindingService)) {
            if (companyEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                limitEventCount++;
            }
        }
        assertEquals("Number of limit violation events", 12, limitEventCount);
        Application.commitUnitOfWork();
    }

    /**
     * There are existing payrolls for employees that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_EEs() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.SEPTEMBER, 8);

        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));
        existingPayrollRun.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingExceedsLimits(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertEquals("Number of consecutive limit violations", 0L, ddCompanyService.getConsecutiveLimitViolationCount());
    }

    /**
     * There are existing payrolls for bank accounts that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_BAs() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.OCTOBER, 16);

        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistEmployee2BankAccount2();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        existingPayrollRun.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        SpcfMoney compOverrideAmount = new SpcfMoney("30000.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("17025.00");

	    ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(companyForFindingService.getSourceSystemCd(),
                companyForFindingService.getSourceCompanyId(), compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();
        assertTrue("Update DD Limits", processResult.isSuccess());

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_OneBAExceedsLimits(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);
        Application.beginUnitOfWork();
        companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertEquals("Number of consecutive limit violations", 0L, ddCompanyService.getConsecutiveLimitViolationCount());
    }

    /**
     * There are existing payrolls for bank accounts that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window in the future, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_BAsFuture() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.OCTOBER, 16);

        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistEmployee2BankAccount2();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        existingPayrollRun.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        SpcfMoney compOverrideAmount = new SpcfMoney("30000.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("17025.00");

        ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(companyForFindingService.getSourceSystemCd(),
                companyForFindingService.getSourceCompanyId(), compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();
        assertTrue("Update DD Limits", processResult.isSuccess());

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_OneBAExceedsLimits(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);

        Application.beginUnitOfWork();
        companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertEquals("Number of consecutive limit violations", 0L, ddCompanyService.getConsecutiveLimitViolationCount());
    }

    /**
     * There are existing payrolls for employees that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window in the future, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_EEsFuture() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.OCTOBER, 16);

        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO existingPayrollRun = c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));
        existingPayrollRun.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingExceedsLimits(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertEquals("Number of consecutive limit violations", 0L, ddCompanyService.getConsecutiveLimitViolationCount());
    }


    /**
     * There are existing payrolls for the company that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window in the past, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_Company() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.SEPTEMBER, 26);

        PayrollRunDTO existingPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        existingPayrollRunDTO.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);

        SpcfMoney compOverrideAmount = new SpcfMoney("16152.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("900000.00");

        ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(companyForFindingService.getSourceSystemCd(),
                companyForFindingService.getSourceCompanyId(), compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();

        assertTrue("Update DD Limits", processResult.isSuccess());

        Application.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);
        assertEquals("Number of consecutive limit violations", 0L, ddCompanyService.getConsecutiveLimitViolationCount());

    }

    /**
     * There are existing payrolls for the company that would cause the company to exceed limits IF they were within the window.
     * However, they are outside of the window in the future, so the company does not exceed limits
     */
    @Test
    public void testExistingPayrollsOutsideOfRange_CompanyFuture() {
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        DateDTO payrollDateOutsideofRange = new DateDTO();

        payrollDateOutsideofRange.set(2007, Calendar.OCTOBER, 9);

        PayrollRunDTO existingPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        existingPayrollRunDTO.setTargetPayrollTXDate(payrollDateOutsideofRange);
        c1DL.persistPayrollRun(existingPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        SpcfMoney compOverrideAmount = new SpcfMoney("16152.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("900000.00");

        ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(companyForFindingService.getSourceSystemCd(),
                companyForFindingService.getSourceCompanyId(), compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();

        assertTrue("Update DD Limits", processResult.isSuccess());

        Application.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(companyForFindingService);
        Application.commitUnitOfWork();

        assertSuccess("checkDDLimits", checkLimitsResult);
    }

    /**
     * A payroll already exists with a future check date still within the window that will cause the next payroll to exceed limits
     */
    @Test
    public void testCompanyExceedsLimitsFuturePayrolls() {
        /*****************Begin setup**********************************************/
        //Set the system date well before the requirements for cutoff (7 days before)
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistPayrollRun(c1DL.getCompany1PR_FutureExceedsLimits(new DateDTO("2007-10-12")));
        PayrollServices.commitUnitOfWork();

        /*****************Begin test**********************************************/
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02"));

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        /*****************Persistence verification**********************************/
        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest02 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        Application.beginUnitOfWork();
        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest02", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }
        Application.commitUnitOfWork();

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
    }

    /**
     * There is an existing payroll for two of the company's employees that does not exceed limits.  Adding the next payroll
     * would cause both of the employees to exceed limits.  We need to make sure that the event recorded is for the employee
     * who MOST (maximally) exceeded limits
     */
    @Test
    public void testTwoEmployeesExceedLimitsExistingPayrolls() {
        //Set the system date well before the requirements for cutoff (7 days before)
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistPayrollRun(c1DL.getCompany1PR_ExistingPR(new DateDTO("2007-10-02")));

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_ExistingExceedsLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest03 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());
        Application.beginUnitOfWork();
        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest03", null,
                    new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);

        Application.commitUnitOfWork();
    }

    /**
     * There is an existing payroll for the same check date that, when coupled with the current payroll, will cause the company to exceed
     * the bank account limits ONLY (e.g. there are two employees being paid to the same bank account number/routing number
     */
    @Test
    public void testBankAccountExceedsLimitsExistingPayrolls() {
        PayrollServices.beginUnitOfWork();
        //Set the system date well before the requirements for cutoff (7 days before)
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        c1DL.persistEmployee2BankAccount2();
        c1DL.persistEmployee3BankAccount1();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_OneBAExceedsLimitsThreeEEs(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c1DL.getCompany1().getSourceSystemCd().toString()),
                c1DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        Application.commitUnitOfWork();

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTestExceedsBALimits for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        Application.beginUnitOfWork();
        //Ensure correct events were input into DB
        Company limitExceedingCompany = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService.findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

//        // Ensure correct limit violation event
        CompanyEvent currEvent = null;
        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() == EventTypeCode.LimitViolation) {
                currEvent = companyEvent;
            }
        }
        assertTrue("Limit Increase Company Event", currEvent != null);
        assertEquals("Company", limitExceedingCompany, currEvent.getCompany());
        assertEquals("Status code", CompanyEventStatus.Active, currEvent.getStatusCd());

        SpcfMoney limitAmount = new SpcfMoney("15000.00");
        SpcfMoney violationAmount = new SpcfMoney("26000.00");

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);

        validateLimitViolationEvent(currEvent, limitAmount, violationAmount, "BatchTestExceedsBALimits", null, "12345", "111000025",
                new EventLimitCode[]{EventLimitCode.BankAccount});
        Application.commitUnitOfWork();
    }

    /**
     * Tests the CheckDDLimits method to retrieve the company limit when the company still has the default limit
     */
    @Test
    public void testGetCompanyLimit_default() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        ddCompanyService = (DDCompanyServiceInfo)company1.getCompanyService(ServiceCode.DirectDeposit);
        CheckDDLimits cdl = new CheckDDLimits(null, company1.getSourceSystemCd(),
                company1.getSourceCompanyId());

        //Get limit from method, which should be the default since we haven't overriden for the company
        SpcfMoney defaultLimit_method = cdl.getDDCompanyLimitAmount(ddCompanyService);

        //Get the default limit from the DB
        SpcfMoney defaultLimit_DB = new SpcfMoney(LimitRule.findLimitRule(ddCompanyService.getCompany(), ServiceCode.DirectDeposit)
                                                           .findLimitValueByName(LimitValueType.DefaultCompanyLimit).getValue());
        Application.commitUnitOfWork();

        assertEquals("Company DD limit", defaultLimit_DB, defaultLimit_method);
    }

    /**
     * Tests the CheckDDLimits method to retrieve the company limit when the company's default limit has been overriden
     */
    @Test
    public void testGetCompanyLimit_override() {
        Application.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) dataloader.persistTestCompanyService(company1);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        SpcfMoney overrideAmount = new SpcfMoney("70000.00");

        ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(company1.getSourceSystemCd(),
                company1.getSourceCompanyId(), overrideAmount, null);
        Application.commitUnitOfWork();
        assertTrue("Update DD Limits", processResult.isSuccess());

        Application.beginUnitOfWork();
        CheckDDLimits cdl = new CheckDDLimits(null, company1.getSourceSystemCd(),
                company1.getSourceCompanyId());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        company1 = Company.findCompany("123456", SourceSystemCode.QBOE);

        ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company1, ServiceCode.DirectDeposit);        
        //Get limit from method, which should be the amount we overrode above
        SpcfMoney limit_method = cdl.getDDCompanyLimitAmount(ddCompanyService);
        Application.commitUnitOfWork();

        assertEquals("Company DD limit", overrideAmount, limit_method);
    }

    public static void validateLimitViolationEvent(
            CompanyEvent pCompanyEvent,
            SpcfMoney pLimitAmount,
            SpcfMoney pViolationAmount,
            String pSourcePayrollRunId,
            String pSourceEmployeeId,
            EventLimitCode[] pValidLimitCodes) {
        validateLimitViolationEvent(pCompanyEvent, pLimitAmount, pViolationAmount, pSourcePayrollRunId, pSourceEmployeeId, null, null, pValidLimitCodes);
    }

    public static void validateLimitViolationEvent(
            CompanyEvent pCompanyEvent,
            SpcfMoney pLimitAmount,
            SpcfMoney pViolationAmount,
            String pSourcePayrollRunId,
            String pSourceEmployeeId,
            String pBankAccountNumber,
            String pBankAccountRoutingNumber,
            EventLimitCode[] pValidLimitCodes) {

        assertEquals(EventTypeCode.LimitViolation, pCompanyEvent.getEventTypeCd());

        String limitCode = null;
        String bankAccountNumber = null;
        String bankAccountRoutingNumber = null;
        SpcfMoney limitAmount = null;
        SpcfMoney violationAmount = null;
        String employeeName = null;
        SpcfUniqueId employeeId = null;
        String sourcePayrollRunId = null;

        for (CompanyEventDetail eventDetail : pCompanyEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case LimitType:
                    limitCode = eventDetail.getValue();
                    break;

                case BankAccountNumber:
                    bankAccountNumber = eventDetail.getValue();
                    break;

                case BankAccountRoutingNumber:
                    bankAccountRoutingNumber = eventDetail.getValue();
                    break;

                case LimitAmount:
                    limitAmount = new SpcfMoney(SpcfMoney.createInstance(eventDetail.getValue()));
                    break;

                case ViolationAmount:
                    violationAmount = new SpcfMoney(SpcfMoney.createInstance(eventDetail.getValue()));
                    break;

                case EmployeeName:
                    employeeName = eventDetail.getValue();
                    break;

                case EmployeeId:
                    employeeId = SpcfUniqueId.createInstance(eventDetail.getValue());
                    break;

                case SourcePayrollRunId:
                    sourcePayrollRunId = eventDetail.getValue();
                    break;

                default:
                    TestCase.fail("Unexpected limit violation detail type: " + eventDetail.getEventDetailTypeCd());
            }
        }
        if (EnumUtils.getReadableName(EventLimitCode.Employee).equals(limitCode) || EnumUtils.getReadableName(EventLimitCode.BankAccount).equals(limitCode)) {
            assertNotNull(employeeId);
            Employee employee = PayrollServices.entityFinder.findById(Employee.class, employeeId);
            assertNotNull(employee);
            assertNotNull(employeeName);
            assertEquals(employeeName, employee.getFirstName() + " " + employee.getLastName());
            if (pSourceEmployeeId != null) {
                assertEquals(pSourceEmployeeId, employee.getSourceEmployeeId());
            }
            if (pBankAccountNumber != null) {
                assertEquals(pBankAccountNumber, bankAccountNumber);
            }
            if (pBankAccountRoutingNumber != null) {
                assertEquals(pBankAccountRoutingNumber, bankAccountRoutingNumber);
            }
        }

        assertEquals("Limit Amount", pLimitAmount, limitAmount);
        assertEquals("Source payroll id", pSourcePayrollRunId, sourcePayrollRunId);
        assertEquals("Violation Amount", pViolationAmount, violationAmount);
        List<String> eventLimitCdNames = new ArrayList<String>();
        for (EventLimitCode evenLimitCd : pValidLimitCodes) {
            eventLimitCdNames.add(EnumUtils.getReadableName(evenLimitCd));
        }
        assertTrue(eventLimitCdNames.contains(limitCode));
    }

    private EventLimitCode getEventLimitType(CompanyEvent pCompanyEvent) {
        assertEquals(EventTypeCode.LimitViolation, pCompanyEvent.getEventTypeCd());
        for (CompanyEventDetail eventDetail : pCompanyEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case LimitType:
                    return EnumUtils.getEnumForReadableName(EventLimitCode.class, eventDetail.getValue());
            }
        }
        return null;
    }

    /**
     * The QBDT company qualifies for a limit increase with no migrated payrolls
     */
    @Test
    public void testQBDTQualifiesForIncrease_NoMigratedPayrolls() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 1 month from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070822", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("17601.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;
                numberOfLimitIncreaseEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c3DL.employee1.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with 6 migrated payrolls, qualifies for a limit increase
     */
    @Test
    public void testQBDTQualifiesForIncrease_With6MigratedPayrolls() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 6
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(6);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        //Create FirstPayrollReceived CompanyEent with has a date at least 30days in the past        
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        CompanyEvent.createPayrollRunEvent(company, null, null, EventTypeCode.FirstPayrollReceived);
        PayrollServices.commitUnitOfWork();

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("17601.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;
                numberOfLimitIncreaseEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c3DL.employee1.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with 5 migrated payrolls(Not enough payrolls), does not qualify for limit increase
     */
    @Test
    public void testQBDTWith5MigratedPayrolls_DoesNotQualifyForLimitIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the Migrate Payroll Count as 5
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(5);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        //Create FirstPayrollReceived CompanyEent with has a date at least 30days in the past
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        CompanyEvent.createPayrollRunEvent(company, null, null, EventTypeCode.FirstPayrollReceived);
        PayrollServices.commitUnitOfWork();
        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with 3 migrated payrolls and 2 existing payrolls(not enough payrolls), does not qualify for limit increase
     */
    @Test
    public void testQBDTWith3MigratedPayrollsAnd2ExistingPayrolls_DoesNotQualifyForLimitIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the Migrate Payroll Count as 3
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(3);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        //Create FirstPayrollReceived CompanyEent with has a date at least 30days in the past
        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        CompanyEvent.createPayrollRunEvent(company, null, null, EventTypeCode.FirstPayrollReceived);
        PayrollServices.commitUnitOfWork();

        //Submit 2 more payrolls for the company
        for (int i = 0; i < 2; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070822", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with 2 migrated payrolls and 4 existing payrolls, qualify for limit increase
     */
    @Test
    public void testQBDTWith2MigratedPayrollsAnd4ExistingPayrolls_QualifyForLimitIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the Migrate Payroll Count as 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(6);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //set up a company with 6 payrolls, the earliest one MORE than 1 month from test time
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        CompanyEvent.createPayrollRunEvent(company, null, null, EventTypeCode.FirstPayrollReceived);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 4; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070822", 5);

/****************************End setup***************************************/

/****************************Begin test***************************************/

        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();


/****************************Begin persistence verification***************************************/

        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewAmount = new SpcfMoney("17601.10");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = currEvent;
                numberOfLimitIncreaseEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchTest06", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c3DL.employee1.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with 2 migrated payrolls and 4 existing payrolls, first payroll not far enough in the past does not qualify
     */
    @Test
    public void testQBDTWith2MigratedPayrollsAnd4ExistingPayrolls_PayrollNotLongEnough_DoesNotQualifyForIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        //Set the Migrate Payroll Count as 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(2);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        CompanyEvent.createPayrollRunEvent(company, null, null, EventTypeCode.FirstPayrollReceived);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 4; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 8, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * The QBDT company with No migrated payrolls and 6 existing payrolls, first payroll not far enough in the past does not qualify
     */
    @Test
    public void testQBDTWithNoMigratedPayrollsAnd6ExistingPayrolls_PayrollNotLongEnough_DoesNotQualifyForIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 8, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        PayrollServices.commitUnitOfWork();
    }


    /**
     * The QBDT company with No migrated payrolls and 6 existing payrolls, have an existing strike dated less than a year before test time
     * Does not qualify for limit increase
     */
    @Test
    public void testQBDTWithNoMigratedPayrollsAnd6ExistingPayrolls_StrikeExist_DoesNotQualifyForIncrease() {
        /****************************Begin setup***************************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000");
        Company3Dataloader c3DL = new Company3Dataloader();
        c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 7, 26, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        PayrollServices.commitUnitOfWork();

        for (int i = 0; i < 6; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-08-22"));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                }
            }

            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070822", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/
        //Add Strike for company with in a year.
        PayrollServices.beginUnitOfWork();
        testTime = SpcfCalendar.createInstance(2007, 9, 21, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyEvent.createStrikeEvent(company, StrikeReason.Manual, "Manual Strike", PSPDate.getPSPTime(), new DomainEntitySet<FinancialTransaction>());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        PayrollRunDTO payrollRunDTO = c3DL.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());
        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();

        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company.findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertFalse(checkLimitsResult.isSuccess());
        assertEquals("Message size", 1, checkLimitsResult.getMessages().size());
        assertEquals("Message code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest06 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                checkLimitsResult.getMessages().get(0).getMessage());

        for (CompanyEvent companyEvent : companyEvents) {
            if (companyEvent.getEventTypeCd() != EventTypeCode.LimitViolation) {
                continue;
            }
            assertEquals("Company", limitExceedingCompany, companyEvent.getCompany());
            assertEquals("Event type", EventTypeCode.LimitViolation, companyEvent.getEventTypeCd());
            assertEquals("Status code", CompanyEventStatus.Active, companyEvent.getStatusCd());

            SpcfMoney limitAmount = new SpcfMoney("15000.00");
            //The given payroll amount plus 30*5 for the first payrolls
            SpcfMoney violationAmount = new SpcfMoney("16001.00");

            validateLimitViolationEvent(companyEvent, limitAmount, violationAmount, "BatchTest06", null, new EventLimitCode[]{EventLimitCode.BankAccount, EventLimitCode.Employee});
        }

        assertEquals("Number of limit violations: ", 1L, limitViolationCount);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testTier2_EmployeeAutoIncrease() {
        // default dd limits  company: 40,000 w/in 6 payrolls    employee: 15,000  w/in 13 payrolls
        // tier 2 auto-limit increase mins.: 12 payrolls run, 180 days since first payroll recorded
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000"); // 2007-03-22

        // creates company w/DD service, a bank account, 2 employees and their bank accounts
        Company3Dataloader c3DL = new Company3Dataloader();
        Company company = c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        // meet tier 2 duration of time/payrolls run requirements
        for (int i = 2; i < 15; i++) {
            PayrollServices.beginUnitOfWork();
            // ensure first payroll will be 180 days back
            String monthStr = (i == 2 ? "04" : "11");

            String dayStr = Integer.toString(100 + i).substring(1);
            PSPDate.setPSPTime("2007" + monthStr + dayStr + "000000");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-" + monthStr + "-" + dayStr));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(new BigDecimal(1000));
                }
            }
            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070408", 5);
        ACHOffloadRunner.runAchOffload("20070415", 5);
        ACHOffloadRunner.runAchOffload("20071108", 5);
        ACHOffloadRunner.runAchOffload("20071115", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/

        // hit auto-limit increase
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-11-16"));
        payrollRunDTO.setPayrollTXBatchId("BatchIDX" + 99);
        Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(3500));    // violate employee max
            }
        }

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());

        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();


        /****************************Begin persistence verification***************************************/
        assertTrue(checkLimitsResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        SpcfMoney expectedOldAmount = new SpcfMoney("15000.00");

        // $1108 per first 12 payrolls, $39000 for last payroll
        // DD Limit Duration: 5 days for QBDT
        // (39000 + 1108) * 2 = $80,216
        SpcfMoney expectedCompanyNewAmount = new SpcfMoney("80216.00");

        // DD Limit Duration: 13 days for QBDT
        // (12000 + 3500) * 2 = $31,000
        SpcfMoney expectedEmployeeNewAmount = new SpcfMoney("31000.00");

        //Ensure correct overriden limit amounts
        assertNull("Company override amount", companyOverrideAmount);
        assertEquals("Employee override amount", expectedEmployeeNewAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;
                numberOfLimitIncreaseEvents++;
                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit,
                        currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());

                assertEquals("Limit type: ", EventLimitCode.Employee.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType));

                assertEquals("Source payroll id", "BatchIDX99", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));
                assertEquals("Employee", c3DL.employee2.getId().toString(),
                        currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                assertEquals("Old limit amount", expectedOldAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                assertEquals("Increased limit amount", expectedEmployeeNewAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));

            }
        }

        assertEquals("Limit increase event count", 1, numberOfLimitIncreaseEvents);
        PayrollServices.commitUnitOfWork();

    }


    @Test
      public void testTier2_PayrollNotOffloaded_EmployeeAutoIncrease() {
          // default dd limits  company: 40,000 w/in 6 payrolls    employee: 15,000  w/in 13 payrolls
          // tier 2 auto-limit increase mins.: 12 payrolls run, 180 days since first payroll recorded
          PayrollServices.beginUnitOfWork();
          PSPDate.setPSPTime("20070322000000"); // 2007-03-22

        // creates company w/DD service, a bank account, 2 employees and their bank accounts
        Company3Dataloader c3DL = new Company3Dataloader();
        Company company = c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        // meet tier 2 duration of time/payrolls run requirements
        for (int i = 2; i < 15; i++) {
            PayrollServices.beginUnitOfWork();
            // ensure first payroll will be 180 days back
            String monthStr = (i == 2 ? "04" : "11");

            String dayStr = Integer.toString(100 + i).substring(1);
            PSPDate.setPSPTime("2007" + monthStr + dayStr + "000000");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-" + monthStr + "-" + dayStr));
            currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(new BigDecimal(1000));
                }
            }
            c3DL.persistPayrollRun(currentPayrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        /****************************End setup***************************************/

        /****************************Begin test***************************************/

        // hit auto-limit increase
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-11-16"));
        payrollRunDTO.setPayrollTXBatchId("BatchIDX" + 99);
        Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(3500));    // violate employee max
            }
        }

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());

        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();


        /****************************Begin persistence verification***************************************/
        assertFalse("limit increase", checkLimitsResult.isSuccess());
        assertEquals("error code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());

    }

    @Test
    public void testTier2_PayrollCancelled_CompanyAutoIncrease() {
        // default dd limits  company: 40,000 w/in 6 payrolls    employee: 15,000  w/in 13 payrolls
        // tier 1 auto-limit increase mins.: 6 payrolls run,  30 days since first payroll recorded (QBDT)
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000"); // 2007-03-22

        // creates company w/DD service, a bank account, 2 employees and their bank accounts
        Company3Dataloader c3DL = new Company3Dataloader();
        Company company = c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        // make first payroll receive date 2007-04-01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070401000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-04-01"));
        currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX01");
        Collection<PaycheckDTO> origPayrollPaychecks = currentPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : origPayrollPaychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(2900));
            }
        }
        c3DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        ACHOffloadRunner.runAchOffload("20070401", 5);

        // meet tier 1 duration of time/payrolls run requirements - advance calendar 90 days 2007-07
        for (int i = 4; i < 9; i++) {
            PayrollServices.beginUnitOfWork();
            String dayStr = Integer.toString(i + 100).substring(1);
            PSPDate.setPSPTime("200707" + dayStr + "000000");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-07-" + dayStr));
            payrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(new BigDecimal(2900));
                }
            }
            c3DL.persistPayrollRun(payrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PayrollRun lastPayrollRun = PayrollRun.findPayrollRun(company, "BatchIDX8");
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        txnCancelDTO.setSourcePayrollRunId(lastPayrollRun.getSourcePayRunId());

        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findPaycheckSplitFinancialTransactions(
                        lastPayrollRun,
                        TransactionStateCode.Created);

        ArrayList<String> txnIdList = new ArrayList<String>();
        for (FinancialTransaction txn : txnList) {
            if(!txnIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId())){
                txnIdList.add(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
            }
        }
        txnCancelDTO.setSourcePaycheckIdList(txnIdList);

        PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnCancelDTO);
        PayrollServices.commitUnitOfWork();

        ACHOffloadRunner.runAchOffload("20070709", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/

        // hit auto-limit increase
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070709000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-07-09"));
        payrollRunDTO.setPayrollTXBatchId("BatchIDX" + 99);
        Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(6000));    // violate company & employee max
            }
        }

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());

        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();


        /****************************Begin persistence verification***************************************/
        assertFalse("limit increase", checkLimitsResult.isSuccess());
        assertEquals("error code", "1043", checkLimitsResult.getMessages().get(0).getMessageCode());
    }

    @Test
    public void testTier1_CompanyEmployeeIncrease() {
        // default dd limits  company: 40,000 w/in 6 payrolls    employee: 15,000  w/in 13 payrolls
        // tier 1 auto-limit increase mins.: 6 payrolls run,  30 days since first payroll recorded (QBDT)
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070322000000"); // 2007-03-22

        // creates company w/DD service, a bank account, 2 employees and their bank accounts
        Company3Dataloader c3DL = new Company3Dataloader();
        Company company = c3DL.persistCompany3();
        PayrollServices.commitUnitOfWork();

        // make first payroll receive date 2007-04-01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070401000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO currentPayrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-04-01"));
        currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX01");
        Collection<PaycheckDTO> origPayrollPaychecks = currentPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : origPayrollPaychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            currPaycheck.setPaycheckNetAmount(new SpcfMoney("2900.00"));
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(2900));
            }
        }
        c3DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();
        ACHOffloadRunner.runAchOffload("20070401", 5);

        // meet tier 1 duration of time/payrolls run requirements - advance calendar 90 days 2007-07
        for (int i = 4; i < 9; i++) {
            PayrollServices.beginUnitOfWork();
            String dayStr = Integer.toString(i + 100).substring(1);
            PSPDate.setPSPTime("200707" + dayStr + "000000");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-07-" + dayStr));
            payrollRunDTO.setPayrollTXBatchId("BatchIDX" + i);
            Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
            for (PaycheckDTO currPaycheck : paychecks) {
                currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
                currPaycheck.setPaycheckNetAmount(new SpcfMoney("2900.00"));
                Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
                for (DDTransactionDTO currDDTxn : ddTxns) {
                    currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                    currDDTxn.setDDTransactionAmount(new BigDecimal(2900));
                }
            }
            c3DL.persistPayrollRun(payrollRunDTO);
            PayrollServices.commitUnitOfWork();
        }
        ACHOffloadRunner.runAchOffload("20070709", 5);

        /****************************End setup***************************************/

        /****************************Begin test***************************************/

        // hit auto-limit increase
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070709000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = c3DL.getCompanyPR_DoesNotExceedLimits(new DateDTO("2007-07-09"));
        payrollRunDTO.setPayrollTXBatchId("BatchIDX" + 99);
        Collection<PaycheckDTO> paychecks = payrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            currPaycheck.setPaycheckNetAmount(new SpcfMoney("6000.00"));
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
                currDDTxn.setDDTransactionAmount(new BigDecimal(6000));    // violate company & employee max
            }
        }

        CheckDDLimits cddL = new CheckDDLimits(payrollRunDTO,
                SourceSystemCode.valueOf(c3DL.getCompany1().getSourceSystemCd().toString()),
                c3DL.getCompany1().getCompanyId());

        ProcessResult checkLimitsResult = cddL.execute();
        PayrollServices.commitUnitOfWork();


        /****************************Begin persistence verification***************************************/
        PayrollServices.beginUnitOfWork();
        Company limitExceedingCompany = Company
                .findCompany("8574536", SourceSystemCode.QBDT);

        assertNotNull("Company is not found", limitExceedingCompany);
        //Ensure correct limit violation count
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(limitExceedingCompany, ServiceCode.DirectDeposit);
        long limitViolationCount = ddCompanyServiceInfo.getConsecutiveLimitViolationCount();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(limitExceedingCompany);

        assertTrue(checkLimitsResult.isSuccess());
        //Ensure that limit count for company is one
        assertEquals("Limit violation count", 0L, limitViolationCount);

        //Override amounts
        SpcfMoney expectedOldCompanyLimitAmount = new SpcfMoney("40000.00");
        SpcfMoney expectedNewCompanyLimitAmount = new SpcfMoney("45100.00");
        SpcfMoney companyOverrideAmount = ddCompanyServiceInfo.getOverrideCompanyLimitAmount();

        SpcfMoney expectedOldEmployeeLimitAmount = new SpcfMoney("15000.00");
        SpcfMoney expectedNewEmployeeLimitAmount = new SpcfMoney("22550.00");
        SpcfMoney employeeOverrideAmount = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount();

        //Ensure correct overriden limit amounts
        assertEquals("Company override amount", expectedNewCompanyLimitAmount, companyOverrideAmount);
        assertEquals("Employee override amount", expectedNewEmployeeLimitAmount, employeeOverrideAmount);
        int numberOfLimitIncreaseEvents = 0;

        boolean companyEventFound = false;
        boolean employeeEventFound = false;
        boolean bankEventFound = false;
        Iterator itrCompanyEvents = companyEvents.iterator();
        while (itrCompanyEvents.hasNext()) {
            CompanyEvent currEvent = (CompanyEvent) itrCompanyEvents.next();
            if (currEvent.getEventTypeCd() == EventTypeCode.DDIncreasePayrollLimit) {
                numberOfLimitIncreaseEvents++;
                CompanyEvent currLimitIncreaseEvent = (CompanyEvent) currEvent;

                assertEquals("Company", limitExceedingCompany, currLimitIncreaseEvent.getCompany());
                assertEquals("Event type", EventTypeCode.DDIncreasePayrollLimit, currEvent.getEventTypeCd());
                assertEquals("Status code", CompanyEventStatus.Active, currLimitIncreaseEvent.getStatusCd());
                assertEquals("Source payroll id", "BatchIDX99", currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourcePayrollRunId));

                String limitType = currEvent.getCompanyEventDetailValue(EventDetailTypeCode.LimitType);
                if (EventLimitCode.Company == EventLimitCode.valueOf(limitType)) {
                    companyEventFound = true;
                    assertEquals("Old limit amount", expectedOldCompanyLimitAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                    assertEquals("Increased limit amount", expectedNewCompanyLimitAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));
                } else if (EventLimitCode.Employee == EventLimitCode.valueOf(limitType)) {
                    employeeEventFound = true;
                    assertEquals("Employee", c3DL.employee2.getId().toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
                    assertEquals("Old limit amount", expectedOldEmployeeLimitAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldLimitAmount));
                    assertEquals("Increased limit amount", expectedNewEmployeeLimitAmount.toString(), currLimitIncreaseEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewLimitAmount));
                } else if (EventLimitCode.BankAccount == EventLimitCode.valueOf(limitType)) {
                    bankEventFound = true;
                }
            }
        }

        assertEquals("Limit increase event count", 2, numberOfLimitIncreaseEvents);
        assertTrue("Company Event Found", companyEventFound);
        assertTrue("Employee Event Found", employeeEventFound);
        assertFalse("Bank Event Found", bankEventFound);

        PayrollServices.commitUnitOfWork();

    }
}
