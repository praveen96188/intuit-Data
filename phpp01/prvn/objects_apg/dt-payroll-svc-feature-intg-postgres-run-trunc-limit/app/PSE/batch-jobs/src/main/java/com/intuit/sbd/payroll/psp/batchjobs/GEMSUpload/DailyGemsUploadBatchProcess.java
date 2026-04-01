package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatch;
import com.intuit.sbd.payroll.psp.domain.GemsUploadBatchStatus;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Daily GEMS Upload Process.  This calculates teh amounts that will ultimately get written to a file that we
 * upload each night.
 */
public class DailyGemsUploadBatchProcess {
    private static final SpcfLogger logger;
    private static final String OUTPUT_DIRECTORY;
    private static final String FILE_PREFIX = "PSPAR";
    private static final String ACTIVITY_TYPE = "PSP";
    private static final String BANK_INITIAL = "PSP";
    private static final String DELIMITOR = ",";
    private static final String FILE_EOL = "\n";
    private int mNewBatchId;

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(DailyGemsUploadBatchProcess.class);

        OUTPUT_DIRECTORY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");
    }

    public static void main(String args[]) {
        try {
            if (args.length > 2) {
                logger.error("Wrong number of parameters. Usage: DailyGemsUploadBatchProcess [BatchId(optional)] [OffloadDate(optional)]");
                throw new RuntimeException("Wrong number of parameters. Usage: DailyGemsUploadBatchProcess [BatchId(optional)] [OffloadDate(optional)]");
            }
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.GemsAccountsReceivableBatchJob));
            try {
                DailyGemsUploadBatchProcess proc = new DailyGemsUploadBatchProcess();
                String batchId = "0";
                SpcfCalendar offloadDate = PSPDate.getPSPTime();
                PayrollServices.beginUnitOfWork();
                if (args.length > 0) {
                    for (String arg : args) {
                        // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                        if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                            offloadDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);
                        } else {
                            batchId = arg;
                        }
                    }
                }
                proc.createFile(batchId, offloadDate);
                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            logger.fatal("Exception in Daily GEMS Upload Batch Process ", t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    public void createFile() throws Exception {
        createFile(0, PSPDate.getPSPTime());
    }

    public void createFile(String pSupercededBatchId, SpcfCalendar pOffloadDate) throws Exception {
        int supercededBatchId = 0;

        if ((pSupercededBatchId != null) && (pSupercededBatchId.length() > 0)) {
            try {
                supercededBatchId = Integer.parseInt(pSupercededBatchId);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid superceded batch id specified: " + pSupercededBatchId, e);
            }
        }

        createFile(supercededBatchId, pOffloadDate);
    }

    public void createFile(int pSupercededBatchId, SpcfCalendar pOffloadDate) throws Exception {
        if ((pSupercededBatchId != 0) && (getUploadBatch(pSupercededBatchId) == null)) {
            throw new RuntimeException("Invalid Batch Id: " + String.valueOf(pSupercededBatchId));
        }

        logger.info("GEMS daily upload started.");

        mNewBatchId = 0;

        logger.info("Calling storedProcedure="+StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN.getStoredProcedureName() +
                " pSupercededBatchId="+pSupercededBatchId+" currentPrincipal="+Application.getCurrentPrincipal().getId());
        ResultSet resultSet = Application.executeSqlProcedure(StoredProcedures.GEMS_ACCOUNTS_RECEIVABLE_MAIN,
                                                              ResultSet.TYPE_FORWARD_ONLY,
                                                              ResultSet.CONCUR_READ_ONLY,
                                                              Pair.of(Integer.class, pSupercededBatchId),
                                                              Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                              Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())));


        if (resultSet == null) {
            throw new RuntimeException("prc_gems_accounts_receivable stored procedure returned null result set.");
        } else {
            try {
                processResults(resultSet, getOffloadDate(pOffloadDate));
            } finally {
                resultSet.close();
            }
        }

        logger.info("GEMS daily upload completed.");
    }

    private SpcfCalendar getOffloadDate(SpcfCalendar pOffloadDate) {
        //if the day the transactions are supposed to be for  the last day of the month and the current date is the first day of the month
        // use the current date for the transaction dates
        // Else
        // The internal date should use the transaction date for the transactions that are going to be used
        return (CalendarUtils.isFirstDayOfMonth(PSPDate.getPSPTime()) && CalendarUtils.isLastDayOfMonth(pOffloadDate)) ? PSPDate.getPSPTime() : pOffloadDate;
    }

    private void processResults(ResultSet pResultSet, SpcfCalendar pOffloadDate) throws Exception {
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        SpcfCalendar systemTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        String fileName = OUTPUT_DIRECTORY + File.separator + FILE_PREFIX +
                StringFormatter.formatDate(pspDate, "yyMMdd") + "-" +
                StringFormatter.formatDate(systemTime, "HHmmss") + ".txt";
        Key key  = IDPSFileStreamManager.newKeyHandleLatest();
        OutputStreamWriter fileWriter = new IDPSFileWriter(fileName,key);
        int recordCount = 0;

        try {
            while (pResultSet.next()) {
                ++recordCount;

                // the batch id is the same for all records in the result set
                if (mNewBatchId == 0) {
                    mNewBatchId = pResultSet.getBigDecimal("batch_id").intValue();
                }

                fileWriter.write(String.valueOf(mNewBatchId));
                fileWriter.write(DELIMITOR);
                fileWriter.write(ACTIVITY_TYPE);
                fileWriter.write(DELIMITOR);
                fileWriter.write(BANK_INITIAL);
                fileWriter.write(DELIMITOR);
                fileWriter.write(pResultSet.getString("sku"));
                fileWriter.write(DELIMITOR);
                fileWriter.write(String.valueOf(pResultSet.getBigDecimal("sku_quantity").intValue()));
                fileWriter.write(DELIMITOR);
                fileWriter.write(String.format("%.2f", pResultSet.getBigDecimal("income_amt")));
                fileWriter.write(DELIMITOR);
                fileWriter.write(String.format("%.2f", pResultSet.getBigDecimal("tax_amt")));
                fileWriter.write(DELIMITOR);
                fileWriter.write(StringFormatter.formatDate(pOffloadDate, "yyMMdd"));
                fileWriter.write(FILE_EOL);
            }

            fileWriter.flush();
        } finally {
            fileWriter.close();
        }
        logger.info("GEMS daily upload recordCount="+recordCount);
        finalizeUploadBatch(fileName, recordCount > 0);
    }

    private void finalizeUploadBatch(String pFileName, boolean pIsValidBatch) {
        GemsUploadBatch batch = getUploadBatch();

        if (batch == null) {
            throw new RuntimeException("GEMS daily upload batch could not be determined from result set.");
        } else {
            if (pIsValidBatch) {
                batch.setFileName(pFileName);
                batch.setUploadStatus(GemsUploadBatchStatus.Finalized);
            } else {
                batch.setUploadStatus(GemsUploadBatchStatus.Empty);
                new File(pFileName).delete();
            }

            batch.setStatusEffectiveDate(PSPDate.getPSPTime());

            Application.save(batch);
        }
    }

    private GemsUploadBatch getUploadBatch(int pBatchId) {
        Expression<GemsUploadBatch> query =
                new Query<GemsUploadBatch>()
                        .Where(GemsUploadBatch.BatchId().equalTo(pBatchId));

        DomainEntitySet<GemsUploadBatch> batchSet = Application.find(GemsUploadBatch.class, query);

        return batchSet.isEmpty() ? null : batchSet.get(0);
    }

    public GemsUploadBatch getUploadBatch() {
        return getUploadBatch(mNewBatchId);
    }
}
