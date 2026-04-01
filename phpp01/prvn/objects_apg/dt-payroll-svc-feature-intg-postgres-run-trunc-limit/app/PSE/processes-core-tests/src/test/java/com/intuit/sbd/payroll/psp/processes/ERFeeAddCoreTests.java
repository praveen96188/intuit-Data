package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.ERFeeAddCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 *
 * User: rsakhamuri
 * Date: Dec 14, 2007
 * Time: 3:18:35 PM

 */
public class ERFeeAddCoreTests {


    @Before
    public void runBeforeEachTest() {
        ERFeeAddCoreDataLoader.beforeEachTestForERFeeAdd();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /*@Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */


    public void companyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader payrollSubmitDataLoader = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = payrollSubmitDataLoader.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "InvalidCompanyId", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        // Commit
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

    }


    /**
     *  Test error message 177 - Company is not active

    @Test
    public void companyNotActive() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 "123123", FeeTypeCodeDTO.ReversalFee);
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        Assert.assertEquals(
                "The operation SubmitPayroll is not allowed for company QBOE:123272727 in its current state.",
                message.getMessage());
        /*assertEquals("Error Code:", "177", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272727 is not active.", message.getMessage());


    } */

    @Test
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(null, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, null, "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test message 186 - Company Bank Account is not Active for ACH settlement
    */
    @Test
    public void testCompanyBankAccountIsNotActive() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        // I don't like this, but I don't want to refactor the entire test
        for (FinancialTransaction finTx: payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))) {
            finTx.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", false, false));
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1062", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:123272727 does not have an active bank account.", message.getMessage());

    }

    /** Test for null payroll
     *
    */
    @Test
    public void testNullFeeType() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 null, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "FeeType has invalid value";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /** Test for invalid payroll
     *
    */
    @Test
    public void testInvalidPayroll() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId00",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "194", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Payroll Run with DDTxBatchID BatchId00 does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText, message.getMessage());


    }

    /** Test for null payroll
     *
    */
    @Test
    public void testNullPayroll() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", null,
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "130", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test invalid payroll status
    */
    @Test
    public void testInvalidERFeeAdd() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1048", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Action ERFeeAdd is not valid for payroll run with DDTxBatchID BatchId01, which has status of "
                + payrollRun.getPayrollRunStatus().toString()+".";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test invalid amount for NON-ACH
    */
    @Test
    public void testInvalidAmount() {

        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("00.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "267", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "For non-ACH transactions, the amount must be a non-zero, positive number.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test null amount for NON-ACH
    */
    @Test
    public void testNullAmount() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, null,
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "267", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "For non-ACH transactions, the amount must be a non-zero, positive number.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test invalid settlement date for NON-ACH
    */
    @Test
    public void testInvalidDatesNonACH() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        calendar.add(Calendar.DAY_OF_YEAR, -46);
        Date txDate = calendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "271", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Cannot record a transaction of Settlement Type Wire and date 2007/07/17 00:00:00.0, which is more than 45 days in the past.";
        assertEquals("Error Message", messageText, message.getMessage());

        calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        txDate = calendar.getTime();

        feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "266", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Cannot record a transaction of Settlement Type Wire with the future date 2007/09/02 00:00:00.0.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    /**
     * Test null settlement date for NON-ACH
    */
    @Test
    public void testNullDatesNonACH() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, null, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "269", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Settlement Date not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }



    /**
     * test method to test add fee process for NON_ACH settlement
    */
    @Test
    public void testAddFeeFinancialTxProcess() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        calendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = calendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate the result
        assertSuccess(processResult);
        assertNotNull(processResult.getResult());
        assertEquals("Fee Financial Transaction",
                TransactionTypeCode.EmployerFeeDebit, processResult.getResult().getFirst().getTransactionType().getTransactionTypeCd());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[] {TransactionStateCode.Completed});
        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(new SpcfMoney("50.00")));

        // verify financial transaction settlement type
        assertTrue("Settlement Code: ", financialTxs.get(0).getSettlementTypeCd().equals(SettlementType.Wire));

        // verify the SKU type
        assertEquals("SKU:", "408177", financialTxs.get(0).getSku());

        // verify the SKU quantity
        assertTrue("SKU:", 1 == financialTxs.get(0).getSkuQuantity());

        // non-ACH settlement means it should settle on the date we input
        SpcfCalendar expectedSettlementDate = CalendarUtils.convertToSpcfCalendar(txDate);
        CalendarUtils.clearTime(expectedSettlementDate);
        assertEquals("Settlement date", expectedSettlementDate.toLocal(), financialTxs.get(0).getSettlementDate().toLocal());

        // verify financial transaction bank accounts
//        assertNull("Credit Bank A/c: ", financialTxs.get(0).getCreditBankAccount());
        assertNull("Debit Bank A/c: ", financialTxs.get(0).getDebitBankAccount());

        // verify the consolidation of ER debit and Fee, SKUs into a single Money movement transaction
        FinancialTransaction feeTransaction = financialTxs.get(0);
        assertNull(feeTransaction.getMoneyMovementTransaction());
        financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] { TransactionStateCode.Created });
        FinancialTransaction erDebitTransaction = financialTxs.get(0);
        assertNotNull(erDebitTransaction.getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * test method to test add Return fee process for NON_ACH settlement
    */
    @Test
    public void testAddReturnFeeFinancialTxProcess() {
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                                                                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company,
                                                                                payrollRunDTO.getPayrollTXBatchId());

        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        Application.save(payrollRun);
        PayrollServices.commitUnitOfWork();

        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        calendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = calendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.DebitReturnFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate the result
        assertSuccess(processResult);
        assertNotNull(processResult.getResult());
        assertEquals("Fee Financial Transaction",
                TransactionTypeCode.EmployerFeeDebit, processResult.getResult().getFirst().getTransactionType().getTransactionTypeCd());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());        
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[] {TransactionStateCode.Completed});
        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(company));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(payrollRun));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(new SpcfMoney("50.00")));

        // verify financial transaction settlement type
        assertTrue("Settlement Code: ", financialTxs.get(0).getSettlementTypeCd().equals(SettlementType.Wire));

        // verify the SKU type
        assertEquals("SKU:", "408176", financialTxs.get(0).getSku());

        // verify the SKU quantity
        assertTrue("SKU:", 1 == financialTxs.get(0).getSkuQuantity());

        // non-ACH settlement means it should settle on the date we input
        SpcfCalendar expectedSettlementDate = CalendarUtils.convertToSpcfCalendar(txDate);
        CalendarUtils.clearTime(expectedSettlementDate);
        assertEquals("Settlement date: ", expectedSettlementDate.toLocal(), financialTxs.get(0).getSettlementDate().toLocal());

        // verify financial transaction bank accounts
//        assertNull("Credit Bank A/c: ", financialTxs.get(0).getCreditBankAccount());
        assertNull("Debit Bank A/c: ", financialTxs.get(0).getDebitBankAccount());

        // verify the consolidation of ER debit and Fee, SKUs into a single Money movement transaction
        FinancialTransaction feeTransaction = financialTxs.get(0);
        assertNull(feeTransaction.getMoneyMovementTransaction());
        financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] { TransactionStateCode.Created });
        FinancialTransaction erDebitTransaction = financialTxs.get(0);
        assertNotNull(erDebitTransaction.getMoneyMovementTransaction());

        PayrollServices.commitUnitOfWork();
    }
    /**
     * process test method for ACH settlement
    */
    @Test
    public void testAddFeeForACHSettlement() {
        ERFeeAddCoreDataLoader.loadDataForAddFeeForACHSettlement();
        
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate the result
        assertSuccess(processResult);
        assertNotNull(processResult.getResult());
        assertEquals("Fee Financial Transaction",
                TransactionTypeCode.EmployerFeeDebit, processResult.getResult().getFirst().getTransactionType().getTransactionTypeCd());


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTxs =
                ERFeeAddCoreDataLoader.getPayrollRun().getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[] { TransactionStateCode.Created }).sort(FinancialTransaction.FinancialTransactionAmount().Descending());
        // verify company
        assertTrue("Company: ", financialTxs.get(0).getCompany().equals(ERFeeAddCoreDataLoader.getCompany()));

        // verify payroll run
        assertTrue("Payroll: ", financialTxs.get(0).getPayrollRun().equals(ERFeeAddCoreDataLoader.getPayrollRun()));

        // verify financial transaction amount
        assertTrue("Transaction Amount: ", financialTxs.get(0).getFinancialTransactionAmount().equals(new SpcfMoney("50.00")));

        // verify financial transaction settlement type
        assertTrue("Settlement Code: ", financialTxs.get(0).getSettlementTypeCd().equals(SettlementType.ACH));

        // verify intuit and company bank a/cs
        CompanyBankAccount expectedCompanyBankAcc = CompanyBankAccount.findCompanyBankAccount(
                ERFeeAddCoreDataLoader.getCompany(), "123123");
        IntuitBankAccount expectedIntuitBankAcc = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(
                        TransactionTypeCode.EmployerFeeDebit),
                 CreditDebitCode.Credit);
        BankAccount expectedBankAcc = expectedIntuitBankAcc.getBankAccount();
        assertTrue("Company bank a/c: ", financialTxs.get(0).getDebitBankAccount().equals(expectedCompanyBankAcc.getBankAccount()));
        assertTrue("Intuit bank a/c id : ", financialTxs.get(0).getCreditBankAccount().getAccountNumber().equals(expectedBankAcc.getAccountNumber()));

        // verify financial transaction settlement date... for ACH, this should be based on the company's DD offload group
        SpcfCalendar settlementDate = null;
        settlementDate = FinancialTransaction.getSettlementDate(ERFeeAddCoreDataLoader.getCompany().getOffloadGroup());
        assertNotNull(settlementDate);
        assertEquals("Settlement Date: ", settlementDate.toLocal(), financialTxs.get(0).getSettlementDate().toLocal());
            
         // verify transaction response
        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 2;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(ERFeeAddCoreDataLoader.getCompany(), new Long(intToken));
        MoneyMovementTransaction feeMMTransaction = financialTxs.get(0).getMoneyMovementTransaction();
        TransactionResponse expTxRes = financialTxs.get(0).getCurrentFinancialTransactionState().getTransactionResponse();
        PayrollServices.commitUnitOfWork();

        assertNotNull(txResponses);
        assertTrue("Number of Transaction Responses: ", txResponses.size() == 1);
        assertTrue("Company: ", txResponses.get(0).getCompany().equals(ERFeeAddCoreDataLoader.getCompany()));
        assertTrue("Transaction Response: ", txResponses.get(0).equals(expTxRes));

        // verify the SKU type
        assertEquals("SKU:", "408177", financialTxs.get(0).getSku()); // reversal fee sku for "QBOE DD" offering"

        // verify the SKU quantity
        assertTrue("SKU:", 1 == financialTxs.get(0).getSkuQuantity());

        // make sure that the ER DD Debit and the manually-billed Fee Debit were not combined into the same MMT
        PayrollServices.beginUnitOfWork();
        feeMMTransaction = Application.findById(MoneyMovementTransaction.class, feeMMTransaction.getId());
        financialTxs =
                ERFeeAddCoreDataLoader.getPayrollRun().getFinancialTransactions(
                        new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] { TransactionStateCode.Created });
        FinancialTransaction erDebitTransaction = financialTxs.get(0);
        assertTrue("Fee Debit not combined with DD Debit in MMT:", ! feeMMTransaction.equals(erDebitTransaction.getMoneyMovementTransaction()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testExcludeFeeFromTransactionResponse() {
        ERFeeAddCoreDataLoader.loadDataForAddFeeForACHSettlement();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        // add a reversal fee
        ERFeeAddDTO dtoReversalFee = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> prReversalFee = PayrollServices.financialTransactionManager.addFeeTransaction(dtoReversalFee);
        PayrollServices.commitUnitOfWork();
        assertSuccess(prReversalFee);

        // add a payment arrangement fee (these should be excluded for QBOE companies)
        ERFeeAddDTO dtoPaymentArrangementFee = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                OfferingServiceChargeType.PaymentArrangementFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> prPaymentArrangementFee = PayrollServices.financialTransactionManager.addFeeTransaction(dtoPaymentArrangementFee);
        PayrollServices.commitUnitOfWork();
        assertSuccess(prPaymentArrangementFee);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeFTs =
                ERFeeAddCoreDataLoader.getPayrollRun().getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("fees created", 2, feeFTs.size());

        // verify transaction response
        Long token = TransactionResponse.getNextTxnResponseToken();

        int intToken = token.intValue();
        intToken = intToken - 2;
        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(ERFeeAddCoreDataLoader.getCompany(), new Long(intToken));
        DomainEntitySet<FinancialTransaction> responseFTs = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransactionState> ftStates = txResponses.get(0).getFinancialTransactionStates();
        for (FinancialTransactionState s : ftStates) {
            responseFTs.add(s.getFinancialTransaction());
        }
        assertNotNull(txResponses);
        assertTrue("Number of Transaction Responses: ", txResponses.size() == 1);
        assertTrue("One FT in response", responseFTs.size() == 1);
        assertTrue("response FT is not the excludable one", OfferingServiceChargeType.PaymentArrangementFee != responseFTs.get(0).getBillingDetail().getOfferingServiceChargeType());

        PayrollServices.commitUnitOfWork();

    }

    /**
     * make sure usage-based fee types result in validation failures
     */
    @Test
    public void testFeeTypeNotAllowed() {
        ERFeeAddCoreDataLoader.loadDataForAddFeeForACHSettlement();

        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", "BatchId01",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.PerPaycheck, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("error messages", 1, result.getMessages().size());
        assertEquals("error code", "5001", result.getMessages().get(0).getMessageCode());
        assertEquals("error message", "FeeType has invalid value", result.getMessages().get(0).getMessage());
    }
}
