/*
 * $Id: //psp/dev/PSE/BatchJobs/test/com/intuit/sbd/payroll/psp/batchjobs/offload/TestFinancialTransactionStateInserted.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.processors.OffloadedTransactionsEventsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the class that creates fee offloaded events
 *
 * @author Dawn Martens
 */
public class TestFinancialTransactionStateInserted {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFinancialTransactionStateInsert() {
        ACHReturnsDataLoader.loadAndOffloadQBOE();

        //Ensure we haven't created any fee events yet
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatches = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        Application.commitUnitOfWork();
        
        for (OffloadBatch currBatch : allBatches) {
            //Create fee offload events & insert financial transaction state
            OffloadedTransactionsEventsProcessor processor = new OffloadedTransactionsEventsProcessor(
                    BatchJobProcessor.RunMode.NotUsingFlux,
                    BatchJobType.OffloadedTransactionsEvents,
                    SpcfUniqueId.createInstance(true).toString(),
                    OffloadACHTransactions.getFeeEventsBatchJobInstanceParameter(currBatch.getId().toString(), currBatch.getOffloadDate()));
            processor.execute();
        }



        // Verification
        //Ensure all offload batches have been processed
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadBatch> allBatchesAfter = PayrollServices.entityFinder.find(OffloadBatch.class, OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed));
        for (OffloadBatch currBatch : allBatchesAfter) {
            assertTrue("Fee events have been created for all offload batches", currBatch.getIsOffloadedTransactionsEventCreationComplete());
        }

        //Ensure financialtransactionstate was inserted
        DomainEntitySet<FinancialTransactionState> fts = PayrollServices.entityFinder.find(FinancialTransactionState.class, FinancialTransactionState.TransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed));
        assertTrue("FinancialTransactionState records were created", fts.size() > 0);
        Application.commitUnitOfWork();
    }
}