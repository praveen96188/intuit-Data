/*
 * $Id: //psp/dev/PSE/BatchJobs/test/com/intuit/sbd/payroll/psp/batchjobs/offload/TestFeeOffloadEventCreated.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the class that creates fee offloaded events
 *
 * @author Dawn Martens
 */
public class TestFeeOffloadEventCreated {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPayrollAndNonPayrollFees() {
        ACHReturnsDataLoader.loadAndOffloadTwoQBDTCompanies();

        /**************************Payroll-related fees******************************************/
        //Ensure we haven't created any fee events yet
 /*       PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatches = PayrollServices.entityFinder.find(OffloadBatch.class);
        for (OffloadBatch currBatch : allBatches) {
            assertFalse("Fee events haven't yet been created for offload batch ", currBatch.getIsFeeEventCreationComplete());
        }
        Application.commitUnitOfWork();
*/
        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Company c2 = Company.findCompany("8774536", SourceSystemCode.QBDT);
        //We offloaded PAYROLL fees for these companies, so make sure there aren't any fee offloaded events created, which are only for non-payroll fees
        assertEquals("No fee offloaded events for c1", 0, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        assertEquals("No fee offloaded events for c2", 0, CompanyEvent.getEventCountByType(c2, EventTypeCode.FeeOffloaded));
        PayrollServices.commitUnitOfWork();

        /**************************Non Payroll-related fees******************************************/
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("75.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT1=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT2=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "8774536", "BatchTest09",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("75.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);

        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c2FT1=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c2FT2=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null,ACHFileType.DD);

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        c2 = Company.findCompany("8774536", SourceSystemCode.QBDT);
        assertEquals("2 fee offloaded events for c1", 2, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        assertEquals("2 fee offloaded events for c2", 2, CompanyEvent.getEventCountByType(c2, EventTypeCode.FeeOffloaded));
        DomainEntitySet<CompanyEventDetail> ft1CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT1);
        DomainEntitySet<CompanyEventDetail> ft2CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT2);
        assertEquals("One detail for c1ft1", 1, ft1CompEventDetails.size());
        assertEquals("One detail for c1ft2", 1, ft2CompEventDetails.size());
        DomainEntitySet<CompanyEventDetail> c2ft1CompEventDetails = CompanyEvent.findCompanyEventDetails(c2, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c2FT1);
        DomainEntitySet<CompanyEventDetail> c2ft2CompEventDetails = CompanyEvent.findCompanyEventDetails(c2, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c2FT2);
        assertEquals("One detail for c2ft1", 1, c2ft1CompEventDetails.size());
        assertEquals("One detail for c2ft2", 1, c2ft2CompEventDetails.size());
        PayrollServices.commitUnitOfWork();

        //Create fee offload events again to ensure we don't double-create events
        eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        PayrollServices.beginUnitOfWork();
        c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        c2 = Company.findCompany("8774536", SourceSystemCode.QBDT);
        assertEquals("2 fee offloaded events for c1", 2, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        assertEquals("2 fee offloaded events for c2", 2, CompanyEvent.getEventCountByType(c2, EventTypeCode.FeeOffloaded));
        ft1CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT1);
        ft2CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT2);
        assertEquals("One detail for c1ft1", 1, ft1CompEventDetails.size());
        assertEquals("One detail for c1ft2", 1, ft2CompEventDetails.size());
        c2ft1CompEventDetails = CompanyEvent.findCompanyEventDetails(c2, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c2FT1);
        c2ft2CompEventDetails = CompanyEvent.findCompanyEventDetails(c2, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c2FT2);
        assertEquals("One detail for c2ft1", 1, c2ft1CompEventDetails.size());
        assertEquals("One detail for c2ft2", 1, c2ft2CompEventDetails.size());
        PayrollServices.commitUnitOfWork();        

    }

    @Test
    public void testNoEventsToCreate() {
        ACHReturnsDataLoader.loadAndOffloadQBOE();

        //Ensure we haven't created any fee events yet
/*        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatches = PayrollServices.entityFinder.find(OffloadBatch.class);
        for (OffloadBatch currBatch : allBatches) {
            assertFalse("Fee events haven't yet been created for offload batch ", currBatch.getIsFeeEventCreationComplete());
        }
        Application.commitUnitOfWork();*/

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        // Verification
        //Ensure all offload batches have been processed
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany("1234567", SourceSystemCode.QBOE);
        assertNotNull("Found company1",c1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("No fee offloaded events for c1", 0, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testEventsForOnlyOneCompany() {
        ACHReturnsDataLoader.loadAndOffloadTwoQBDTCompanies();

        /**************************Payroll-related fees******************************************/
        //Ensure we haven't created any fee events yet
/*        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatches = PayrollServices.entityFinder.find(OffloadBatch.class);
        for (OffloadBatch currBatch : allBatches) {
            assertFalse("Fee events haven't yet been created for offload batch ", currBatch.getIsFeeEventCreationComplete());
        }
        Application.commitUnitOfWork();*/

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Company c2 = Company.findCompany("8774536", SourceSystemCode.QBDT);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        assertEquals("No fee offloaded events for c1", 0, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        assertEquals("No fee offloaded events for c2", 0, CompanyEvent.getEventCountByType(c2, EventTypeCode.FeeOffloaded));
        PayrollServices.commitUnitOfWork();

        /**************************Non Payroll-related fees******************************************/
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                                                 SettlementTypeDTO.ACH, txDate, new SpcfMoney("75.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT1=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT2=processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null, ACHFileType.DD);

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        c2 = Company.findCompany("8774536", SourceSystemCode.QBDT);
        assertEquals("2 fee offloaded events for c1", 2, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        assertEquals("0 fee offloaded events for c2", 0, CompanyEvent.getEventCountByType(c2, EventTypeCode.FeeOffloaded));
        DomainEntitySet<CompanyEventDetail> ft1CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT1);
        DomainEntitySet<CompanyEventDetail> ft2CompEventDetails = CompanyEvent.findCompanyEventDetails(c1, EventTypeCode.FeeOffloaded, EventDetailTypeCode.FinancialTransactionId, c1FT2);
        assertEquals("One detail for ft1", 1, ft1CompEventDetails.size());
        assertEquals("One detail for ft2", 1, ft2CompEventDetails.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFeeOffloadForZeroDollarFee_PSRV004188() {
        ACHReturnsDataLoader.loadAndOffloadTwoQBDTCompanies();

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        assertEquals("No fee offloaded events for c1", 0, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        PayrollServices.commitUnitOfWork();
        /**************************Non Payroll-related fees******************************************/
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = pastCalendar.getTime();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                                                SettlementTypeDTO.ACH, txDate, new SpcfMoney("0"),
                                                OfferingServiceChargeType.ReversalFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT1 = processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess(processResult);
        String c1FT2 = processResult.getResult().getFirst().getId().toString();
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null, ACHFileType.DD);

        // Verification
        //Ensure all batches have been processed
        PayrollServices.beginUnitOfWork();
        allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1 = Company.findCompany("8574536", SourceSystemCode.QBDT);
        assertEquals("2 fee offloaded events for c1", 0, CompanyEvent.getEventCountByType(c1, EventTypeCode.FeeOffloaded));
        PayrollServices.rollbackUnitOfWork();
    }


}
