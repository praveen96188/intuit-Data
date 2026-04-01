package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.File;
import java.util.Calendar;


/**
 * Hand-written business logic
 */
public class TransactionReturnBatch extends BaseTransactionReturnBatch {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TransactionReturnBatch()
	{
		super();
	}

    public static boolean fileAlreadyDownloaded(SpcfCalendar pDate) {
        SpcfCalendar returnDateStart = pDate.copy();
        CalendarUtils.clearTime(returnDateStart);
        SpcfCalendar returnDateEnd = pDate.copy();
        CalendarUtils.endOfDay(returnDateEnd);

        Expression<TransactionReturnBatch> query =
                new Query<TransactionReturnBatch>()
                        .Select(TransactionReturnBatch.Id().Count())
                        .Where(TransactionReturnBatch.ReturnDate().between(returnDateStart, returnDateEnd));
        return Application.executeScalarAggQuery(TransactionReturnBatch.class, query) > 0;
    }

    public static TransactionReturnBatch createBatch(File pAchReturnsFile) {

        DomainEntitySet<TransactionReturnBatch> batchList = getTransactionReturnBatchByFile(pAchReturnsFile.getName());
        if (batchList.size() > 0) {
            StringBuffer err = new StringBuffer();

            err.append("Warning: ACH Return file may already have been processed. One or more ")
               .append("Transaction Return Batch records already exist in the database for the given file name (")
               .append(pAchReturnsFile.getName()).append("):").append("\n");

            for (TransactionReturnBatch returnBatch : batchList) {
                err.append(returnBatch).append("\n");
            }

            throw new RuntimeException(err.toString().trim());
        }

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(pAchReturnsFile.getAbsolutePath());
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        Application.save(transactionReturnBatch);
        return transactionReturnBatch;
    }

    private static DomainEntitySet<TransactionReturnBatch> getTransactionReturnBatchByFile(String pFileName,
                                                                                          TransactionReturnBatchStatusCode... pStatus) {
        Expression<TransactionReturnBatch> query;
        File file = new File(pFileName);

        if ((pStatus == null) || (pStatus.length == 0)) {
            query = new Query<TransactionReturnBatch>()
                    .Where(TransactionReturnBatch.ACHReturnFileName().like("%" + file.getName()))
                    .OrderBy(TransactionReturnBatch.ReturnDate().Descending());
        } else {
            query = new Query<TransactionReturnBatch>()
                    .Where(TransactionReturnBatch.ACHReturnFileName().like("%" + file.getName())
                                                 .And(TransactionReturnBatch.StatusCd().in(pStatus)))
                    .OrderBy(TransactionReturnBatch.ReturnDate().Descending());
        }

        return Application.find(TransactionReturnBatch.class, query);
    }

    public static DomainEntitySet<TransactionReturnBatch> getReceivedBatches() {
        return getTransactionReturnBatchByStatus(TransactionReturnBatchStatusCode.Received);
    }

    public static DomainEntitySet<TransactionReturnBatch> getReadyToProcessBatches() {
        return getTransactionReturnBatchByStatus(TransactionReturnBatchStatusCode.Persisted);
    }

    public static DomainEntitySet<TransactionReturnBatch> getProcessedBatches() {
        return getTransactionReturnBatchByStatus(TransactionReturnBatchStatusCode.Processed);
    }

    public static DomainEntitySet<TransactionReturnBatch> getTransactionReturnBatchByStatus(TransactionReturnBatchStatusCode... pStatus) {
        Expression<TransactionReturnBatch> query = new Query<TransactionReturnBatch>()
                .Where(TransactionReturnBatch.StatusCd().in(pStatus))
                .OrderBy(TransactionReturnBatch.ReturnDate());

        return Application.find(TransactionReturnBatch.class, query);
    }

    public static DomainEntitySet<TransactionReturn> getUnprocessedTransactionReturns(SpcfUniqueId pReturnBatchId) {
        Expression<TransactionReturn> query =
                new Query<TransactionReturn>()
                        .Where( TransactionReturn.ReturnBatch().Id().equalTo(pReturnBatchId)
                                .And(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Created)));

        return Application.find(TransactionReturn.class, query);
    }

    public static DomainEntitySet<TransactionReturn> getTransactionReturns(SpcfUniqueId pReturnBatchId) {
        Expression<TransactionReturn> query =
                new Query<TransactionReturn>().Where(TransactionReturn.ReturnBatch().Id().equalTo(pReturnBatchId));
        return Application.find(TransactionReturn.class, query);
    }


    @Override
    public String toString() {
        return "TransactionReturnBatch   Id: " + getId() + "   File: " + getACHReturnFileName() + "   State: " + getStatusCd().name();
    }
}