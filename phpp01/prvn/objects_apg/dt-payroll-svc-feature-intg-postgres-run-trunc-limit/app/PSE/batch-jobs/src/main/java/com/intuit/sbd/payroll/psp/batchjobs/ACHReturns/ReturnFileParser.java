package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 20, 2008
 * Time: 10:22:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReturnFileParser {

    private static final char FILE_HEADER_REC_TYPE = '1';
    private static final char BATCH_HEADER_REC_TYPE = '5';
    private static final char ENTRY_DETAILS_REC_TYPE = '6';
    private static final char ADDENDA_REC_TYPE = '7';
    //private static final char BATCH_FOOTER_REC_TYPE = '8';
    private static final char FILE_FOOTER_REC_TYPE = '9';

    private String mLastReadLine = null;
    private Collection<Batch> mBatchList = null;
    private String mHeader = null;
    private Map<Company, Long> companyToken = new HashMap<Company, Long>();
    private static final SpcfLogger logger = Application.getLogger(ReturnFileParser.class);
    private PSPRequestContextManager pspRequestContextManager;

    public ReturnFileParser() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * Function to parse the ACH Return file and presist the data into TransactionReturn table and process the
     * transaction returns.
     *
     * @param pFilePath String
     * @param pFileName String
     */
    public void processFile(String pFilePath, String pFileName) {
        processFile(new File(pFilePath, pFileName));
    }

    /**
     * Function to parse the ACH Return file and presist the data into TransactionReturn table and process the
     * transaction returns.
     *
     * THIS DUPLICATES THE LOGIC OF THE Ach Returns File processing batch job
     *
     * @param pFileName Java File object
     */
    public SpcfUniqueId processFile(File pFileName) {
        if (!pFileName.exists()) {
            throw new RuntimeException("Unable to process returns file. File does not exist: " + pFileName.toString());
        }

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AchReturnsBatchJob));

            PayrollServices.beginUnitOfWork();
            TransactionReturnBatch transactionReturnBatch = TransactionReturnBatch.createBatch(pFileName);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            parseAndPersist(transactionReturnBatch.getId());
            PayrollServices.commitUnitOfWork();

            processReturnBatch(transactionReturnBatch.getId());
            PayrollServices.beginUnitOfWork();
            if (TransactionReturnBatch.getUnprocessedTransactionReturns(transactionReturnBatch.getId()).size() == 0) {
                transactionReturnBatch = Application.refresh(transactionReturnBatch);
                transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Processed);
            }
            PayrollServices.commitUnitOfWork();

            if (transactionReturnBatch.getStatusCd() == TransactionReturnBatchStatusCode.Processed) {
                PayrollServices.beginUnitOfWork();
                transactionReturnBatch = Application.refresh(transactionReturnBatch);
                try{
                    BatchUtils.createAchReturnAccountingFileAndEmail(transactionReturnBatch .getId());
                }catch (Throwable t){
                    logger.error("Error while uploading ACH return accounting file",t);
                }
                transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Completed);
                PayrollServices.commitUnitOfWork();
            }

            return transactionReturnBatch.getId();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public TransactionReturnBatch parseAndPersist(SpcfUniqueId pBatchId) {
        TransactionReturnBatch returnBatch = Application.findById(TransactionReturnBatch.class, pBatchId);
        if (returnBatch == null) {
            throw new RuntimeException("could not find TransactionReturnBatch with ID: " + pBatchId);
        }

        if (returnBatch.getStatusCd() != TransactionReturnBatchStatusCode.Received) {
            throw new RuntimeException("cannot parse a file that is not in the " + TransactionReturnBatchStatusCode.Received + " state\n\t" + returnBatch);
        }

        Expression<TransactionReturn> returnsCountQuery =
                new Query<TransactionReturn>()
                        .Select(TransactionReturn.Id().Count())
                        .Where(TransactionReturn.ReturnBatch().Id().equalTo(pBatchId));
        if (Application.executeScalarAggQuery(TransactionReturn.class, returnsCountQuery) > 0) {
            throw new RuntimeException("file has already been parsed and persisted but has Received status: " + returnBatch);
        }

        logger.info("parse and create transaction returns for return batch: " + returnBatch.getACHReturnFileName());

        File file = new File(returnBatch.getACHReturnFileName());
        if (!file.exists()) {
            throw new RuntimeException("Unable to process returns file. File does not exist: " + file.toString());
        }

        try {
            load(file);
        } catch (Exception e) {
            throw new RuntimeException("failed to parse file " + file.getName(), e);
        }

        int returnCount = build_TransactionReturnList(returnBatch).size();
        returnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        logger.info(returnCount + " transaction returns created for return batch: " + returnBatch.getACHReturnFileName());

        return returnBatch;
    }

    /**
     * Function to build the Transaction Return collection by getting the data from BatchList which
     * was created by parsing the data from ACH Return file
     *
     * @param pTransactionReturnBatch TransactionReturnBatch
     * @return transactionReturnList Collection
     */
    public Collection<TransactionReturn> build_TransactionReturnList(TransactionReturnBatch pTransactionReturnBatch) {
        Collection<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();
        Collection<Batch> batchList = getBatchList();

        if ((batchList != null) && !batchList.isEmpty()) {
            boolean returnAlreadyProcessed;
            MoneyMovementTransaction mmTxn;
            String reasonCode;

            for (Batch batch : batchList) {
                Collection<Entry> entriesList = batch.getEntriesList();

                for (Entry entry : entriesList) {
                    Collection<Addenda> addendasList = entry.getAddendasList();

                    for (Addenda addenda : addendasList) {
                        EntryDetailRecord entryDetailRecord = null;

                        try {
                            Long traceNumber = Long.parseLong(addenda.getOriginalTraceNumber());

                            entryDetailRecord = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(traceNumber);

                            if (entryDetailRecord == null) {
                                logger.error("Could not find EntryDetailRecord for trace number: " + traceNumber);
                            } else {
                                try {
                                    pspRequestContextManager.setRequestContextCompany(entryDetailRecord.getCompany());
                                    mmTxn = entryDetailRecord.getMoneyMovementTransaction();
                                    reasonCode = addenda.getReturnReasonCode().substring(0, 1);
                                    returnAlreadyProcessed = false;

                                    // only one "R" return is allowed for any given txn (multiple "C" returns are allowed)
                                    if ("R".equals(reasonCode)) {
                                        DomainEntitySet<TransactionReturn> txnReturnList =
                                                TransactionReturn.findTransactionReturnsByReturnCodeAndMMT(
                                                        mmTxn, reasonCode);

                                        returnAlreadyProcessed = !txnReturnList.isEmpty();
                                    }

                                    if (returnAlreadyProcessed) {
                                        DomainEntitySet<FinancialTransaction> ftList = mmTxn.getFinancialTransactionCollection();

                                        logger.error("Financial Transaction " + ftList.get(0).getId() +
                                                " is Returned Twice with " + reasonCode + " Code");

                                        // try to keep hibernate cache clean
                                        for (FinancialTransaction ft : ftList) {
                                            Application.evict(ft);
                                        }
                                    } else {
                                        TransactionReturn transactionReturn = new TransactionReturn();

                                        transactionReturn.setBankReturnCd(addenda.getReturnReasonCode());
                                        transactionReturn.setBankReturnDescription(addenda.getAddendaInformation());
                                        transactionReturn.setBankReturnTraceNumber(Long.parseLong(addenda.getBankTraceNumber()));
                                        transactionReturn.setReturnBatch(pTransactionReturnBatch);
                                        transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                                        transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
                                        transactionReturn.setMoneyMovementTransaction(mmTxn);
                                        transactionReturn.setCompany(mmTxn.getCompany());

                                        transactionReturnList.add(Application.save(transactionReturn));
                                    }
                                } finally {
                                    pspRequestContextManager.clearRequestContextCompany();
                                }
                            }
                        } catch (Throwable t) {
                            logger.error("Error while getting EntryDetail Record", t);
                        }
                    }
                }
            }
        }

        return transactionReturnList;
    }

    /**
     * Function to parse an ACH Return file and presist the data into TransactionReturn table and process the
     * transaction returns.
     *
     * @param pBatchId The TransactionReturnBatchId representing the batch to process.
     */
    public TransactionReturnBatch processReturnBatch(SpcfUniqueId pBatchId) {
        try {
            // process each transaction return in its own unit of work
            PayrollServices.beginUnitOfWork();
            TransactionReturnBatch returnBatch = Application.findById(TransactionReturnBatch.class, pBatchId);
            logger.info("processing " + returnBatch);
            if (returnBatch.getStatusCd() != TransactionReturnBatchStatusCode.Persisted) {
                logger.info("cannot process transaction returns for returns batch in state " + returnBatch.getStatusCd().name());
                return returnBatch;
            }
            PayrollServices.rollbackUnitOfWork();

            // process all transactions in a Created state (all others in batch are skipped)
            // transaction management is on a per TransactionReturn basis - as many as possible are processed & committed
            processTransactionReturns(pBatchId);

            return returnBatch;

        } finally {
            PayrollServices.rollbackUnitOfWork();
            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

     /**
     * Function to process the returned Transaction return by calling the corresponding return handlers execute() method
     * which meets the criteria with the return code.
     *
     * @param pReturnsBatchId TransactionReturnBatchId
     * @throws Exception exception
     */
    public void processTransactionReturns(SpcfUniqueId pReturnsBatchId) {
        int exceptionCount = 0;
        List<TransactionReturn> failedNOCReturnsList = new ArrayList<TransactionReturn>();
        List<TransactionReturn> failedRejectReturnsList = new ArrayList<TransactionReturn>();
        try {
            // first, process all of the NOC returns
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<TransactionReturn> nocReturns =
                    TransactionReturn.findTxnRetsForReturnType(
                            pReturnsBatchId, TransactionReturn.ReturnTypeCodes.NOTICE_OF_CHANGE, TransactionReturnStatusCode.Created);
            PayrollServices.rollbackUnitOfWork();
            logger.info("processing NOC returns (" + nocReturns.size() + ")");
            for (TransactionReturn txnReturn : nocReturns) {
                try {
                    pspRequestContextManager.setRequestContextCompany(txnReturn.getCompany());
                    boolean success = executeReturn(txnReturn.getId());
                    if (!success) {
                        exceptionCount++;
                        failedNOCReturnsList.add(txnReturn);
                    }
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }

            }

            // last, process all of the Reject returns
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<TransactionReturn> rejectReturns =
                    TransactionReturn.findTxnRetsForReturnType(
                            pReturnsBatchId, TransactionReturn.ReturnTypeCodes.RETURN, TransactionReturnStatusCode.Created);
            PayrollServices.rollbackUnitOfWork();
            logger.info("processing Reject returns (" + rejectReturns.size() + ")");
            for (TransactionReturn txnReturn : rejectReturns) {
                try {
                    pspRequestContextManager.setRequestContextCompany(txnReturn.getCompany());
                    boolean success = executeReturn(txnReturn.getId());
                    if (!success) {
                        exceptionCount++;
                        failedRejectReturnsList.add(txnReturn);
                    }
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if (exceptionCount > 0) {
            logger.error("Transaction Return processing encountered " + exceptionCount + " exceptions during processing.");
            try{
                BatchUtils.createAchReturnFailedTransactionReportEmail(pReturnsBatchId, failedNOCReturnsList, failedRejectReturnsList);
            }catch (RuntimeException e){
                logger.error("Transaction Return processing encountered a RunTimeException while sending the details of the failed records.",e);
                throw new RuntimeException("Transaction Return processing encountered a RunTimeException while sending the details of the failed records..  Please correct and rerun the returns job.");
            }
        }
    }

    private boolean executeReturn(SpcfUniqueId pTxnReturnId) {
        boolean success = true;

        TransactionReturn txnReturn = null;
        try {
            PayrollServices.beginUnitOfWork();
            txnReturn = Application.findById(TransactionReturn.class, pTxnReturnId);
            executeReturn(txnReturn);
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            success = false;
            StringBuilder errMessage = new StringBuilder("failed to process transaction return - *** the returns job will need to be run again after any errors are corrected ***");
            if (txnReturn != null) {
                errMessage.append("\n").append(txnReturn.toExtendedString());
            } else {
                errMessage.append(": ").append(pTxnReturnId);
            }
            errMessage.append("\n\n");
            StringWriter stackTrace = new StringWriter(1024);
            t.printStackTrace(new PrintWriter(stackTrace));
            errMessage.append(stackTrace.toString());
            logger.error(errMessage.toString());
            //Mark transaction return as error
            PayrollServices.rollbackUnitOfWork();
            PayrollServices.beginUnitOfWork();
            txnReturn = Application.findById(TransactionReturn.class, pTxnReturnId);
            txnReturn.setReturnStatusCd(TransactionReturnStatusCode.Error);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return success;
    }

    private void executeReturn(TransactionReturn pTxnReturn) {
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(pTxnReturn);

        if (handler != null) {
            try {
                logger.info(handler + " processing " + pTxnReturn);
                handler.execute(pTxnReturn);
            } catch (Throwable t) {
                String traceNumber = null;
                MoneyMovementTransaction mmt = pTxnReturn.getMoneyMovementTransaction();
                DomainEntitySet<EntryDetailRecord> edrList = mmt.getEntryDetailRecordCollection();

                for (EntryDetailRecord edr : edrList) {
                    // only set trace number once
                    if (traceNumber == null) {
                        traceNumber = edr.getTraceNumber();
                    }
                }

                throw new RuntimeException("Error while executing the Transaction Return " +
                                                   "Record with Trace Number: " + traceNumber, t);
            }
        }
    }

    /**
     * Function to load the data from ACH Return file
     *
     * @param pFile File
     * @throws Exception exception
     */
    public void load(File pFile) throws Exception {
        PgpReader reader = PgpReaderFactory.createInstance();
        reader.open(pFile);

        try {
            char entryType;

            mBatchList = new ArrayList<Batch>();
            mLastReadLine = reader.readLine();

            while (mLastReadLine != null) {
                if (mLastReadLine.length() != 0) {
                    entryType = mLastReadLine.charAt(0);

                    if (entryType == FILE_HEADER_REC_TYPE) {
                        mHeader = mLastReadLine;
                    } else if (entryType == BATCH_HEADER_REC_TYPE) {
                        mBatchList.add(new Batch(reader));
                    } else if (entryType == FILE_FOOTER_REC_TYPE) {

                    }
                }

                mLastReadLine = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public String getImmediateDestination() {
        return mHeader.substring(3, 13);
    }

    public String getImmediateOrigin() {
        return mHeader.substring(13, 23);
    }

    private class Batch {
        private Collection<Entry> mEntriesList;

        public Batch(PgpReader pReader) throws IOException {
            mLastReadLine = pReader.readLine();
            mEntriesList = new ArrayList<Entry>();

            if (mLastReadLine != null && mLastReadLine.length() != 0) {
                char entryType = mLastReadLine.charAt(0);

                while (entryType == ENTRY_DETAILS_REC_TYPE) {
                    mEntriesList.add(new Entry(pReader));
                    entryType = mLastReadLine.charAt(0);
                }
            }
        }

        public Collection<Entry> getEntriesList() {
            return mEntriesList;
        }
    }

    private class Entry {
        private String mData = null;
        private Collection<Addenda> mAddendasList = null;

        public Entry(PgpReader pReader) throws IOException {
            mData = mLastReadLine;
            mAddendasList = new ArrayList<Addenda>();

            mLastReadLine = pReader.readLine();
            if (mLastReadLine != null && mLastReadLine.length() > 0) {
                char entryType = mLastReadLine.charAt(0);

                while (entryType == ADDENDA_REC_TYPE) {
                    mAddendasList.add(new Addenda());
                    mLastReadLine = pReader.readLine();
                    entryType = mLastReadLine.charAt(0);
                }
            }
        }

        public String getIndividualId() {
            return mData.substring(39, 54).trim();
        }

        public int getAddendaFlag() {
            return Integer.parseInt(mData.substring(78, 79));
        }

        public Collection<Addenda> getAddendasList() {
            return mAddendasList;
        }
    }

    private class Addenda {
        private String mData = null;

        public Addenda() {
            mData = mLastReadLine;
        }

        public String getReturnReasonCode() {
            return mData.substring(3, 6);
        }

        public String getOriginalTraceNumber() {
            return mData.substring(6, 21);
        }

        public String getOriginalReceivingId() {
            return mData.substring(27, 35);
        }

        public String getAddendaInformation() {
            return mData.substring(35, 79);
        }

        public String getBankTraceNumber() {
            return mData.substring(79);
        }
    }

    public Collection<Batch> getBatchList() {
        return mBatchList;
    }
}
