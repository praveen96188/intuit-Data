package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ACHTraceIdFileParser {

    private static final char FILE_HEADER_REC_TYPE = 'H';
    private static final char FILE_TRAILER_REC_TYPE = 'T';
    private int RECORDS_PER_THREAD = 100;
    private int RECORDS_PER_BATCH = 10000;
    private int INTERVAL = 60;
    private int MAX_WAIT = 300;
    private static final SpcfLogger logger = Application.getLogger(ACHTraceIdFileParser.class);
    HeaderRecord headerRecord = null;
    TrailerRecord trailerRecord = null;
    List<DetailRecord> detailRecordList;

    private PSPRequestContextManager pspRequestContextManager;

    public ACHTraceIdFileParser() {
        detailRecordList = new ArrayList<DetailRecord>();
        INTERVAL = SystemParameter.findIntValue(SystemParameter.Code.ACHTRACEID_CONTROLS_THREAD_POOL_INTERVAL, 60);
        MAX_WAIT = SystemParameter.findIntValue(SystemParameter.Code.ACHTRACEID_CONTROLS_THREAD_POOL_MAX_WAIT, 300);
        RECORDS_PER_THREAD = SystemParameter.findIntValue(SystemParameter.Code.ACHTRACEID_CONTROLS_THREAD_POOL_RECORDS_PER_THREAD, 100);
        RECORDS_PER_BATCH = SystemParameter.findIntValue(SystemParameter.Code.ACHTRACEID_CONTROLS_THREAD_POOL_RECORDS_PER_BATCH, 10000);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    static class HeaderRecord {
        private String mData = null;

        public HeaderRecord(String mData) {
            this.mData = mData;
        }

        //Origin - The origin number for the trace file
        public String getOrigin() {
            return mData.substring(1, 10);
        }

        //CreationDate - File creation date
        public String getCreationDate() {
            return mData.substring(12, 20);
        }

    }

    static class DetailRecord {
        private String mData = null;

        public DetailRecord(String mData) {
            this.mData = mData;
        }

        //JPM ABA - JPM routing number
        public String getJPMRoutingNumber() {
            return mData.substring(0, 8);
        }

        //JPM Trace - This is the JPM trace number for the transaction
        public String getJPMTraceNumber() {
            return mData.substring(8, 15);
        }

        //Individual ID - Individual ID value from incoming detail record
        public String getIndividualId() {
            return mData.substring(16, 31);
        }

        //Tran ABA CHK - Routing number and check digit from incoming detail record
        public String getTranABACHK() {
            return mData.substring(32, 41);
        }

        //Account - DFI account value from incoming detail record (positions 13-29)
        public String getAccount() {
            return mData.substring(43, 60);
        }

        //Amount - Amount from incoming detail record (positions 30–39)
        public String getAmount() {
            return mData.substring(62, 72);
        }

        //DB CR IND - D for debit or C for credit
        public String getCreditDebitIndicator() {
            return mData.substring(72, 73);
        }

        //Effective Date - Effective Date of the incoming transaction
        public String getEffectiveDate() {
            return mData.substring(74, 80);
        }

        //Originating ABA - Originators ABA of the transaction
        public String getOriginatingABA() {
            return mData.substring(82, 90);
        }

        //Originating Trace - Originators Trace number of the transaction
        public String getOriginatingTrace() {
            return mData.substring(90, 97);
        }

        //Originating COID - Orig comp ID value from batch header record (positions 41-50)
        public String getCOID() {
            return mData.substring(98, 108);
        }
    }

    static class TrailerRecord {
        private String mData = null;

        public TrailerRecord(String mData) {
            this.mData = mData;
        }

        //Item Count - Count of detail records in the trace file
        public String getItemCount() {
            return mData.substring(1, 11);
        }
    }

    public void parseAndUpdateTraceId(String mFileName) throws Exception {
        logger.info("Parsing the ACHTrace File : " + mFileName);
        File traceFile = new File(mFileName);
        if (!traceFile.exists()) {
            throw new RuntimeException("Unable to process ACHTrace File. File does not exist: " + traceFile.toString());
        }
        int totalProcessedRecordsCount = 0;
        PgpReader reader = PgpReaderFactory.createInstance();
        try {
            reader.open(traceFile);
            String mLastReadLine = reader.readLine();
            char entryType;

            while (mLastReadLine != null) {
                if (StringUtils.isNotBlank(mLastReadLine)) {
                    entryType = mLastReadLine.charAt(0);
                    if (entryType == FILE_HEADER_REC_TYPE) {
                        headerRecord = new HeaderRecord(mLastReadLine);
                        logger.info("ACH Trace File Origin : " + headerRecord.getOrigin() + " CreationDate : " + headerRecord.getCreationDate());
                    } else if (entryType == FILE_TRAILER_REC_TYPE) {
                        trailerRecord = new TrailerRecord(mLastReadLine);
                        logger.info("ACH Trace File Item count : " + Integer.parseInt(trailerRecord.getItemCount()));
                    } else {
                        DetailRecord detailRecord = new DetailRecord(mLastReadLine);
                        detailRecordList.add(detailRecord);
                    }
                }

                if (detailRecordList.size() == RECORDS_PER_BATCH) {
                    totalProcessedRecordsCount += multithreadedProcessing(detailRecordList);
                    logger.info("BatchLevel : Completed processing " + totalProcessedRecordsCount + " records");
                    detailRecordList = new ArrayList<>();
                }
                mLastReadLine = reader.readLine();
            }
            if (detailRecordList.size() > 0) {
                totalProcessedRecordsCount += multithreadedProcessing(detailRecordList);
            }
        } finally {
            reader.close();
        }

        logger.info("Completed processing " + totalProcessedRecordsCount + " records of " + Integer.parseInt(trailerRecord.getItemCount()));

    }

    public int multithreadedProcessing(List<DetailRecord> recordList) {
        ExecutorService executorService = null;
        // Create threadPool with given parameters
        int cores = Runtime.getRuntime().availableProcessors();
        logger.info("No of cores: " + cores);
        int numberOfThreads = cores * 2;
        if (numberOfThreads <= 0) {

            numberOfThreads = 10;
        }

        executorService = new ThreadPoolExecutor(cores, // core size
                numberOfThreads, // max size
                60 * 5, // idle timeout
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executorService);

        List<List<DetailRecord>> partitionedList = ListUtils.partition(recordList, RECORDS_PER_THREAD);
        for (List<DetailRecord> iList : partitionedList) {
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return updateEDRWithJPMCTraceId(iList);
                }
            });
        }

        // Get the results of each thread execution
        int recordsProcessedCount = 0;

        try {
            for (int t = 0; t < partitionedList.size(); t++) {

                Future<Integer> future = completionService.take();
                recordsProcessedCount += future.get();

                if (recordsProcessedCount % 1000 == 0) {
                    logger.info("working -- completed processing " + recordsProcessedCount + " records");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Exception : ", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("ExecutionException : ", e);
            throw ThreadingUtils.launderThrowable(e.getCause());
        } finally {
            Application.rollbackUnitOfWork();
            if (executorService != null) {
                ThreadingUtils.shutdownAndAwaitTermination(executorService, INTERVAL, MAX_WAIT);
            }
        }
        return recordsProcessedCount;
    }

    public Integer updateEDRWithJPMCTraceId(List<DetailRecord> recordList) {
        int count = 0;
        try {
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, "ACHTraceIdProcessor");
            Application.beginUnitOfWork();
            for (DetailRecord detailRecord : recordList) {
                //TraceNumber is formed by ABA and trace, so we consider them together to lookup EDR
                EntryDetailRecord entryDetailRecord = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong(getIntuitTraceNumber(detailRecord.getOriginatingABA() + detailRecord.getOriginatingTrace())));
                if (entryDetailRecord != null) {
                    entryDetailRecord.setJPMCTraceNumber(detailRecord.getJPMRoutingNumber() + detailRecord.getJPMTraceNumber());
                    count++;
                } else {
                    logger.error("Could not find EntryDetailRecord for trace number: " + detailRecord.getOriginatingTrace());
                }
            }
            Application.commitUnitOfWork();
        } catch (Exception ex){
            // Catching the exception, so that the processing of other records is not impacted
            logger.error("Unable to parse: ", ex);
        } finally {
            Application.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
        return count;
    }

    private String getIntuitTraceNumber(String ptracenum) {
        return Long.toString(Long.parseLong(ptracenum));
    }

}
