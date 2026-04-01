/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/offload/OffloadACHTransactions.java#1 $
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
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.offload.nachaobjects.*;
import com.intuit.sbd.payroll.psp.batchjobs.processors.OffloadedTransactionsEventsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriterFactory;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ACHFileType;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.NACHABatchType;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.*;
import org.hibernate.Query;
import org.hibernate.exception.GenericJDBCException;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * This class generates the NACHA file(s) and updates the statuses of the transactions that ended up in the file
 *
 * @author Dawn Martens
 * @author Wiktor Kozlik
 */
public class OffloadACHTransactions {
    /** The NACHA file created */
    NACHAFile nachaFile;
    List<NACHAFile> nachaFileList= new ArrayList();

    private OffloadBatch mOffloadBatch;
    private List<OffloadBatch> mOffloadBatchList=new ArrayList();
    private SpcfCalendar mOffloadDate;
    private OffloadGroup mOffloadGroup;


    //Global vars used per file
    private int mRecordCount;
    private int mEntryCount;
    private int mAddendaCount;
    private int mBatchCount;

    //Global var used for offload
    private HashMap<String, String> mSourceSystemBatchEntryDescriptions;

    private static final SpcfMoney SPCF_MONEY_ZERO = new SpcfMoney("0");
    private static final String DATA_FILE_PREFIX = "d.";

    //JP MORGAN CHASE-SPECIFIC CONSTANTS
    private static boolean ENABLE_ENCRYPTION;
    private static String IMMEDIATE_DESTINATION;
    private static String IMMEDIATE_DESTINATION_NAME;
    private static String IMMEDIATE_ORIGIN;
    private static String IMMEDIATE_ORIGIN_NAME;

    //Environment-specific properties
    private static final String JPMC_ACH_FILE_EOL = "\n";
    private static final String WINDOWS_ACH_FILE_EOL = "\r\n";
    private static final String ACH_FILE_EOL;
    private static final String OUTPUT_DIRECTORY;

    //FILE HEADER RECORD CONSTANTS
    private static final String FILE_HEADER_REC_TYPE_CODE = "1";
    private static final String RECORD_SIZE = "094";
    private static final String BLOCKING_FACTOR = "10";
    private static final String FORMAT_CODE = "1";
    private static final String REFERENCE_CODE = "";
    private static final String PRIORITY_CODE = "01";

    //FILE HEADER RECORD CONSTANT LENGTHS
    private static final int IMMEDIATE_DEST_NAME_LENGTH = 23;
    private static final int IMMEDIATE_ORIGIN_NAME_LENGTH = 23;
    private static final int REFERENCE_CODE_LENGTH = 8;
    private static final String ACH_DATE_TIME_FORMAT = "yyMMddHHmm";

    //FILE FOOTER RECORD CONSTANTS
    private static final String FILE_FOOTER_REC_TYPE_CODE = "9";
    private static final SpcfLogger logger;

    private static String INTUIT_CCD_BATCH_COMPANY_NAME;
    private static String CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD;
    private OffloadHelper offloadHelper;

    private PSPRequestContextManager pspRequestContextManager;


    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(OffloadACHTransactions.class);

        OUTPUT_DIRECTORY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_send_dir");

        if ("true".equalsIgnoreCase(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_offload_use_bank_line_end_char"))) {
            ACH_FILE_EOL = JPMC_ACH_FILE_EOL;
        } else {
            ACH_FILE_EOL = WINDOWS_ACH_FILE_EOL;
        }

        INTUIT_CCD_BATCH_COMPANY_NAME = SystemParameter.findSystemParameter(
                SystemParameter.Code.JPMC_CCD_BATCH_COMPANY_NAME).getSystemParameterValue();
        CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD = SystemParameter.findSystemParameter(
                SystemParameter.Code.CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD).getSystemParameterValue();

    }

    public OffloadACHTransactions() {
        offloadHelper = new OffloadHelper();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    private static void showUsage() {
        System.out.println("Usage: OffloadBatchJob [offloadGroupCode] [offloadDate]");
        System.out.println("       Where offload date format is " + BatchUtils.DATE_FORMAT);
        System.out.println("       Example: OffloadBatchJob STD 20070925");
    }

    public static void verifyDeployment() {
        System.out.println("OffloadACHTransactions deployed successfully.");
        System.exit(0);
    }

    /**
     * An entry point into Offload
     *
     * @param args     String[]
     */
    public static void main(String args[]) {
        try {
            // Validate input params
            if ((args.length == 1) && args[0].equalsIgnoreCase("verify")) {
                verifyDeployment();
            }

            if (args.length != 3) {
                throw new RuntimeException("Wrong number of parameters.");
            } else if (!args[1].matches(BatchUtils.VALIDYYYYMMDD)) {
                // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                throw new RuntimeException("Invalid offload date specified: " + args[1]);
            }

            // args[0] == offload group
            // args[1] == processing date
            // args[2] == File Type

            ACHFileType fileType = ACHFileType.valueOf(args[2].toString());;

            SpcfCalendar processingDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfCalendar pDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, args[1]);

            processingDate.setValues(pDate.getYear(), pDate.getMonth(), pDate.getDay());

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AchOffloadBatchJob));

            // Do the offload
            try {
                PayrollServices.beginUnitOfWork();

                // Generate the file
                String offloadBatchId = new OffloadACHTransactions().offloadAndCreateFiles(args[0], processingDate, fileType);

                PayrollServices.commitUnitOfWork();

                // Schedule the fee offloaded event creation batch job and its monitor
                // Pass the offload batch id and the processingDate so we can also
                BatchJobManager batchJobManager = new BatchJobManager();
                String jobId = batchJobManager.scheduleJob(BatchJobType.OffloadedTransactionsEvents,
                        getFeeEventsBatchJobInstanceParameter(offloadBatchId,
                                getOffloadDate(processingDate)));
                batchJobManager.scheduleJob(BatchJobType.OffloadedTransactionsEventsMonitor, jobId);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            logger.fatal("Exception in Offload ACH Transactions ", t);
            t.printStackTrace();
            showUsage();
            System.exit(1);
        }
    }

    /**
     * Method to call the offload stored procedure and the fee event batch job
     *
     * @param pOffloadGroup String
     * @param pRunDate      SpcfCalendar
     * @return String
     */
    public String offloadAndPostOffload(String pOffloadGroup, SpcfCalendar pRunDate) {

        String offloadBatchId = offloadAndPostOffload(pOffloadGroup, pRunDate, ACHFileType.DD);
        return offloadBatchId;
    }

    /**
     * Method to call the offload stored procedure and the fee event batch job
     *
     * @param pOffloadGroup String
     * @param pRunDate      SpcfCalendar
     * @param pACHFileType  ACHFileType
     * @return String
     */
    public String offloadAndPostOffload(String pOffloadGroup, SpcfCalendar pRunDate, ACHFileType pACHFileType) {
        PayrollServices.beginUnitOfWork();
        // statement to Call the offload stored procedure
        String offloadBatchId = offloadAndCreateFiles(pOffloadGroup, pRunDate, pACHFileType);
        PayrollServices.commitUnitOfWork();

        if (offloadBatchId != null) {
            PayrollServices.beginUnitOfWork();
            OffloadBatch currBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(offloadBatchId));
            PayrollServices.commitUnitOfWork();

            //
            // execute the Fee event Batch job
            OffloadedTransactionsEventsProcessor processor = new OffloadedTransactionsEventsProcessor(
                    BatchJobProcessor.RunMode.NotUsingFlux,
                    BatchJobType.OffloadedTransactionsEvents,
                    SpcfUniqueId.createInstance(true).toString(),
                    OffloadACHTransactions.getFeeEventsBatchJobInstanceParameter(currBatch.getId().toString(),
                    currBatch.getOffloadDate()));

            processor.execute();
        }

        return offloadBatchId;
    }

    public String offloadAndCreateFiles(String pOffloadGroup, SpcfCalendar pRunDate, ACHFileType pFileType) {
        String batchId = offload(pOffloadGroup, pRunDate, pFileType);

        if (batchId != null) {
            generateFiles(batchId, pFileType);
        }

        return batchId;
    }

    /**
     * Validates the given parameters; if valid, generates ACH files as appropriate
     *
     * @param pOffloadGroup Offload group to run offload for, NOT nullable
     * @param pRunDate      Run date for offload, NULLABLE.  If not given, PSP date will be used
     * @param pFileType     ACHFileType
     * @return Returns the offload batch id.
     */
    public String offload(String pOffloadGroup, SpcfCalendar pRunDate, ACHFileType pFileType) {
        String offloadBatchId = null;

        try {
            StopWatch timer = StopWatch.startTimer();

            validate(pOffloadGroup, pRunDate);

            logger.info(String.format("Starting ach offload procedure [%s, %s, %s]",
                    pOffloadGroup,
                    mOffloadDate.format("yyyyMMdd"),
                    Application.getCurrentPrincipal().getId()));

            // Call offload PL/SQL stored procedure
            offloadBatchId = OffloadBatch.findPendingOffloadBatch(mOffloadDate, pOffloadGroup, true);


            if (offloadBatchId != null) {
                checkCreditDebitTotals(offloadBatchId);
                logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD.getStoredProcedureName()+
                        " offloadBatchId="+offloadBatchId+" mOffloadDate="+mOffloadDate+" pFileType="+pFileType+" currentPrincipal="+Application.getCurrentPrincipal().getId());
                Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD, true,
                                                Pair.of(String.class, offloadBatchId),
                                                Pair.of(Timestamp.class, new Timestamp(mOffloadDate.getTimeInMilliseconds())),
                                                Pair.of(String.class, pFileType.toString()),
                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())));

                // Offload batch is being updated in prc_offload - refresh it
                OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(offloadBatchId));
                Application.refresh(offloadBatch);
                for (NACHAFile nachaFile : offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.InProcess)) {
                    Application.refresh(nachaFile);
                }

                logger.info(String.format("Returned from ach offload stored procedure [%s, %s, %s] " +
                        "(returned offload batch id: %s). Elapsed time: %s",
                        pOffloadGroup,
                        mOffloadDate.format("yyyyMMdd"),
                        Application.getCurrentPrincipal().getId(),
                        offloadBatchId,
                        timer.stop().getElapsedTimeString()));
            }
            else {
                logger.info(String.format("No pending offload for [%s, %s, %s] " +
                        "Elapsed time: %s",
                        pOffloadGroup,
                        mOffloadDate.format("yyyyMMdd"),
                        Application.getCurrentPrincipal().getId(),
                        timer.stop().getElapsedTimeString()));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Error generating ACH offload. ", t);
        }

        return offloadBatchId;
    }

    public List<String> offload(List<String> pOffloadGroups, SpcfCalendar pRunDate, ACHFileType pFileType) {
        List<String> offloadBatchIds = null;

        try {
            StopWatch timer = StopWatch.startTimer();

            for(String offloadGroup: pOffloadGroups){
                validate(offloadGroup, pRunDate);
            }
            logger.info(String.format("Starting ach offload procedure [%s, %s, %s]",
                    String.join(",", pOffloadGroups),
                    mOffloadDate.format("yyyyMMdd"),
                    Application.getCurrentPrincipal().getId()));

                // Call offload PL/SQL stored procedure
            offloadBatchIds=OffloadBatch.findPendingOffloadBatchesForOffloadGroups(pOffloadGroups, mOffloadDate, true);


            if (offloadBatchIds==null || offloadBatchIds.isEmpty()) {
                logger.info(String.format("No pending offload for [%s, %s, %s] " +
                        "Elapsed time: %s",
                        pOffloadGroups.get(0),
                        mOffloadDate.format("yyyyMMdd"),
                        Application.getCurrentPrincipal().getId(),
                        timer.stop().getElapsedTimeString()));

            }else {
                checkCreditDebitTotals(offloadBatchIds);
                for(String offloadBatchId: offloadBatchIds){
                    logger.info("Calling storedProcedure="+StoredProcedures.PRC_OFFLOAD.getStoredProcedureName()+
                            " offloadBatchId="+offloadBatchId+" mOffloadDate="+mOffloadDate+" pFileType="+pFileType.toString()+" currentPrincipal="+Application.getCurrentPrincipal().getId());
                    Application.executeSqlProcedure(StoredProcedures.PRC_OFFLOAD, true,
                                                    Pair.of(String.class, offloadBatchId),
                                                    Pair.of(Timestamp.class, new Timestamp(mOffloadDate.getTimeInMilliseconds())),
                                                    Pair.of(String.class, pFileType.toString()),
                                                    Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                    Pair.of(Timestamp.class, new Timestamp(SpcfCalendar.getNow().getTimeInMilliseconds())));

                    // Offload batch is being updated in prc_offload - refresh it
                    OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(offloadBatchId));
                    Application.refresh(offloadBatch);
                    for (NACHAFile nachaFile : offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.InProcess)) {
                        Application.refresh(nachaFile);
                    }

                }
                logger.info(String.format("Returned from ach offload stored procedure [%s, %s, %s] " +
                        "(returned offload batch id: %s). Elapsed time: %s",
                        String.join(",",offloadBatchIds),
                        mOffloadDate.format("yyyyMMdd"),
                        Application.getCurrentPrincipal().getId(),
                        String.join(",",offloadBatchIds),
                        timer.stop().getElapsedTimeString()));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Error generating ACH offload. ", t);
        }

        return offloadBatchIds;
    }

    public void checkCreditDebitTotals(String offloadBatchId) {
        OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(offloadBatchId));
        Application.refresh(offloadBatch);
        for (NACHAFile nachaFile : offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.InProcess)) {
            Application.refresh(nachaFile);
            ScrollableResults creditDebitTotals = getCreditDebitTotals(nachaFile);
            HashMap<CreditDebitCode, SpcfMoney> amounts = new HashMap<CreditDebitCode, SpcfMoney>();
            amounts.put(CreditDebitCode.Credit, SpcfMoney.ZERO);
            amounts.put(CreditDebitCode.Debit, SpcfMoney.ZERO);
            while(creditDebitTotals.next()){
                amounts.put((CreditDebitCode)creditDebitTotals.get(0), (SpcfMoney) creditDebitTotals.get(1));
            }

            //There will be 2 records returned, one for credit and one for debit. So, we check for the number of records returned
            boolean performCreditDebitTotalsCheck = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
            if(performCreditDebitTotalsCheck && !amounts.get(CreditDebitCode.Credit).equals(amounts.get(CreditDebitCode.Debit))) {
                SpcfMoney difference = new SpcfMoney(amounts.get(CreditDebitCode.Credit).subtract(amounts.get(CreditDebitCode.Debit)).abs());
                SpcfMoney threshold = new SpcfMoney(CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD);
                String message = " NACHAFile Type : "+nachaFile.getFileType()+ "\n" +
                        " NACHAFile Id : " +nachaFile.getId()+"\n"+
                        " Offload Date : " +offloadBatch.getOffloadDate()+"\n"+
                        " Offload BatchId : "+offloadBatch.getId()+"\n";
                if(difference.isGreaterThanEqualTo(threshold)){
                    logger.fatal("Stopping the job at this point. " +
                            "The totalDebitAmount and totalCreditAmount do not match. The difference is greater than or equal to the threshold of $"+CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD+". \n" +
                            "TotalCreditAmount : "+amounts.get(CreditDebitCode.Credit)+" TotalDebitAmount : "+amounts.get(CreditDebitCode.Debit)+" Difference : $"+difference+
                            " Please contact the Business to resolve this before re-running the job. Please create a pager duty alert.\n" + message);
                    String messageBody = "The totalDebitAmount and totalCreditAmount do not match. The difference is greater than or equal to the threshold of $"+CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD+".\n " +
                            " TotalCreditAmount : $"+amounts.get(CreditDebitCode.Credit)+" TotalDebitAmount : $"+amounts.get(CreditDebitCode.Debit)+" Difference : $"+difference+"\n" +
                            "The offload batchjob is stopped for now.\n" +
                            "Following are the NACHAFile and OffloadBatch details : \n"+message;
                    sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", messageBody);
                    throw new RuntimeException("The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold $"+CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD);
                } else {
                    String messageBody = "The totalDebitAmount and totalCreditAmount do not match. " +
                            "The difference is less than the threshold $"+CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD+"\n" +
                            " TotalCreditAmount : $"+amounts.get(CreditDebitCode.Credit)+" TotalDebitAmount : $"+amounts.get(CreditDebitCode.Debit)+" Difference :  $"+difference+"\n" +
                            "Following are the NACHAFile and OffloadBatch details : \n"+message;
                    logger.fatal(messageBody);
                    sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", messageBody);
                }
            }
        }
    }

    public void checkCreditDebitTotals(List<String> offloadBatchIds) {
        NACHAFile pNachaFile;
        logger.info("Credit and Debit Totals would be calculated for the following OffloadBatch Ids: "+String.join(",", offloadBatchIds));
        List<OffloadBatch> offloadBatches = new ArrayList<>();
        DomainEntitySet<NACHAFile> nachaFiles = new DomainEntitySet<NACHAFile>();
        for (String offloadBatchId : offloadBatchIds) {
            OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(offloadBatchId));
            offloadBatches.add(offloadBatch);
            Application.refresh(offloadBatch);
            nachaFiles.addAll(offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.InProcess));
        }

        nachaFiles = nachaFiles.sort(NACHAFile.FileType());
        Iterator iterator = nachaFiles.iterator();
        List<NACHAFile> nachaFileBatch = new ArrayList<>();
        NACHAFile prev = null;
        while(iterator.hasNext()) {
            pNachaFile = (NACHAFile)iterator.next();

            Application.refresh(pNachaFile);
            if (prev == null || prev.getFileType()== pNachaFile.getFileType()) {
                nachaFileBatch.add(pNachaFile);
                prev=pNachaFile;
                if(iterator.hasNext())
                    continue;
            }
            ScrollableResults creditDebitTotals = getCreditDebitTotals(nachaFileBatch);
            HashMap<CreditDebitCode, SpcfMoney> amounts = new HashMap<CreditDebitCode, SpcfMoney>();
            amounts.put(CreditDebitCode.Credit, SpcfMoney.ZERO);
            amounts.put(CreditDebitCode.Debit, SpcfMoney.ZERO);
            while (creditDebitTotals.next()) {
                amounts.put((CreditDebitCode) creditDebitTotals.get(0), (SpcfMoney) creditDebitTotals.get(1));
            }

            NACHAFile stdNachaFile = getSTDNachaFile(offloadBatchIds, nachaFileBatch);
            OffloadBatch stdOffloadBatch = stdNachaFile.getOffloadBatch();

            //There will be 2 records returned, one for credit and one for debit. So, we check for the number of records returned
            boolean performCreditDebitTotalsCheck = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_CREDIT_DEBIT_TOTALS_CHECK);
            if (performCreditDebitTotalsCheck && !amounts.get(CreditDebitCode.Credit).equals(amounts.get(CreditDebitCode.Debit))) {
                SpcfMoney difference = new SpcfMoney(amounts.get(CreditDebitCode.Credit).subtract(amounts.get(CreditDebitCode.Debit)).abs());
                SpcfMoney threshold = new SpcfMoney(CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD);
                String message = " NACHAFile Type : " + stdNachaFile.getFileType() + "\n" +
                        " NACHAFile Id : " + stdNachaFile.getId() + "\n" +
                        " Offload Date : " + stdOffloadBatch.getOffloadDate() + "\n" +
                        " Offload BatchId : " + stdOffloadBatch.getId() + "\n";
                if (difference.isGreaterThanEqualTo(threshold)) {
                    logger.fatal("Stopping the job at this point. " +
                            "The totalDebitAmount and totalCreditAmount do not match. The difference is greater than or equal to the threshold of $" + CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD + ". \n" +
                            "TotalCreditAmount : " + amounts.get(CreditDebitCode.Credit) + " TotalDebitAmount : " + amounts.get(CreditDebitCode.Debit) + " Difference : $" + difference +
                            " Please contact the Business to resolve this before re-running the job. Please create a pager duty alert.\n" + message);
                    String messageBody = "The totalDebitAmount and totalCreditAmount do not match. The difference is greater than or equal to the threshold of $" + CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD + ".\n " +
                            " TotalCreditAmount : $" + amounts.get(CreditDebitCode.Credit) + " TotalDebitAmount : $" + amounts.get(CreditDebitCode.Debit) + " Difference : $" + difference + "\n" +
                            "The offload batchjob is stopped for now.\n" +
                            "Following are the NACHAFile and OffloadBatch details : \n" + message;
                    sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", messageBody);
                    throw new RuntimeException("The totalDebitAmount and totalCreditAmount do not match and the difference is greater than or equal to the threshold $" + CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD);
                } else {
                    String messageBody = "The totalDebitAmount and totalCreditAmount do not match. " +
                            "The difference is less than the threshold $" + CREDIT_DEBIT_DIFFERENCE_ALERT_THRESHOLD + "\n" +
                            " TotalCreditAmount : $" + amounts.get(CreditDebitCode.Credit) + " TotalDebitAmount : $" + amounts.get(CreditDebitCode.Debit) + " Difference :  $" + difference + "\n" +
                            "Following are the NACHAFile and OffloadBatch details : \n" + message;
                    logger.fatal(messageBody);
                    sendEmail("psp_offload_notify_list", "psp_offload_notify_subject", messageBody);
                }

            }
            prev = pNachaFile;
            nachaFileBatch= new ArrayList<>();
            nachaFileBatch.add(pNachaFile);
        }
    }

    private NACHAFile getSTDNachaFile(List<String> offloadBatchIds, List<NACHAFile> nachaFileBatch) {
        NACHAFile stdNachaFile=null;
        for(NACHAFile nf: nachaFileBatch){
            if (nf.getOffloadBatch().getOffloadGroup().getOffloadGroupCd().equals("STD")) {
                stdNachaFile = nf;
            }
        }
        if(stdNachaFile==null){
            throw new RuntimeException("There is no STD group NACHAFile for offloadBatchIds: "+String.join(",",offloadBatchIds));
        }
        return stdNachaFile;
    }


    protected void sendEmail(String pToFromProperty, String pSubjectProperty, String pMessageBody) {
        //
        // don't fail the overall batch job if there is an error sending email, just report the error in the log.
        //
        try {
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString(pToFromProperty), // to
                    BatchUtils.getConfigString(pToFromProperty), // from
                    BatchUtils.getConfigString(pSubjectProperty),
                    pMessageBody,
                    Boolean.TRUE,
                    null,
                    null,
                    null);
        } catch (Exception e) {
            logger.error("Failed to send email message for daily batch job. Email message was: " + pMessageBody, e);
        }
    }

    public ScrollableResults getCreditDebitTotals(NACHAFile pNACHAFile){
        String hqlSelect =
                "Select entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount)" +
                        "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                        "where entryDetailRecord.NACHAFile = :nachaFile \n" +
                        "      and entryDetailRecord.InitiationDate = :initiationDate \n" +
                        "group by entryDetailRecord.CreditDebitIndicator";

        String[] paramNames = new String[2];
        paramNames[0] = "nachaFile";
        paramNames[1] = "initiationDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = pNACHAFile;
        paramValues[1] = pNACHAFile.getOffloadBatch().getOffloadDate();

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        for (int i = 0; i < paramValues.length; i++) {
            queryObject.setParameter(paramNames[i], paramValues[i]);
        }
        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults getCreditDebitTotals(List<NACHAFile> pNACHAFiles){
        if(pNACHAFiles.isEmpty())
            return null;

        String hqlSelect =
                "Select entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount)" +
                        "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                        "where entryDetailRecord.NACHAFile in (:nachaFiles) \n" +
                        " and entryDetailRecord.InitiationDate = :initiationDate \n" +
                        "group by entryDetailRecord.CreditDebitIndicator";

        Query queryObject = Application.createHibernateQuery(hqlSelect);

        queryObject.setParameterList("nachaFiles", pNACHAFiles);
        queryObject.setParameter("initiationDate", pNACHAFiles.get(0).getOffloadBatch().getOffloadDate());

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    public void generateFiles(String pBatchId, ACHFileType pFileType) {
        try {
            StopWatch timer = StopWatch.startTimer();

            populateSystemParameterValues();
            populateSourcePayrollParameterMap();

            mOffloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(pBatchId));

            if (mOffloadBatch == null) {
                throw new RuntimeException(String.format("Specified offload batch id not found (%s)", pBatchId));
            }

            mOffloadGroup = mOffloadBatch.getOffloadGroup();
            mOffloadDate = getOffloadDate(mOffloadBatch.getOffloadDate().toLocal());

            logger.info(String.format("Starting ach file creation procedure [%s, %s]",
                    Application.getCurrentPrincipal().getId(),
                    "Offload batch id: " + mOffloadBatch.getId().toString()));

            switch (pFileType) {
                case DD:
                    offloadDDTransactions();
                    break;
                case Tax:
                    generateFile(NACHAFileType.CCDPlus, false);
                    break;
                default:
                    offloadDDTransactions();
                    break;
            }

            logger.info(String.format("Completed ach file creation procedure [%s, %s, %s]",
                    Application.getCurrentPrincipal().getId(),
                    "Offload batch id: " + mOffloadBatch.getId().toString(),
                    "Elapsed time: " + timer.stop().getElapsedTimeString()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Error creating ACH files. ", t);
        }
    }

    public void generateFiles(List<String> pBatchIds, ACHFileType pFileType) {
        try {
            StopWatch timer = StopWatch.startTimer();

            populateSystemParameterValues();
            populateSourcePayrollParameterMap();
            for(String batchId : pBatchIds){
                OffloadBatch offloadBatch = Application.findById(OffloadBatch.class, SpcfUniqueId.createInstance(batchId));
                mOffloadBatchList.add(offloadBatch);
            }

            if (mOffloadBatchList.isEmpty()) {
                throw new RuntimeException(String.format("Specified offload batch id not found (%s)", String.join(",",pBatchIds)));
            }

            String code = OffloadGroup.Codes.STANDARD;

            mOffloadGroup = OffloadGroup.findOffloadGroup(code);
            mOffloadDate = getOffloadDate(mOffloadBatchList.get(0).getOffloadDate().toLocal());

            logger.info(String.format("Starting ach file creation procedure [%s, %s]",
                    Application.getCurrentPrincipal().getId(),
                    "Offload batch id: " + String.join(",",pBatchIds)));
            offloadDDTransactions();

            logger.info(String.format("Completed ach file creation procedure [%s, %s, %s]",
                    Application.getCurrentPrincipal().getId(),
                    "Offload batch id: " + String.join(",", pBatchIds),
                    "Elapsed time: " + timer.stop().getElapsedTimeString()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Error creating ACH files. ", t);
        }
    }

    private void offloadDDTransactions() throws Exception {
        boolean isPayrollRunLevelFundingModel = offloadHelper.isPayrollRunLevelFundingModel(mOffloadBatch.getOffloadDate());
        generateFile(NACHAFileType.CCD, isPayrollRunLevelFundingModel);
        generateFile(NACHAFileType.PPD, isPayrollRunLevelFundingModel);
    }

    private void populateSourcePayrollParameterMap() {
        mSourceSystemBatchEntryDescriptions = new HashMap<String, String>();

        for (SourceSystemCode currSourceSystemCd : new SourceSystemCode[]{SourceSystemCode.QBDT, SourceSystemCode.QBOE}) {
            String reversalKey = NACHABatchType.Reversal + ":" + currSourceSystemCd;
            SourcePayrollParameter reversalEntryDesc = SourcePayrollParameter
                    .findSourcePayrollParameter(currSourceSystemCd, SourcePayrollParameterCode.ReversalEntryDescription);

            if (reversalEntryDesc != null) {
                mSourceSystemBatchEntryDescriptions.put(reversalKey, reversalEntryDesc.getParameterValue());
            }

            String payrollKey = NACHABatchType.Payroll + ":" + currSourceSystemCd;
            SourcePayrollParameter payrollEntryDesc = SourcePayrollParameter
                    .findSourcePayrollParameter(currSourceSystemCd, SourcePayrollParameterCode.PayrollEntryDescription);

            if (payrollEntryDesc != null) {
                mSourceSystemBatchEntryDescriptions.put(payrollKey, payrollEntryDesc.getParameterValue());
            }

            String bookTxfrKey = NACHABatchType.BookTransfer + ":" + currSourceSystemCd;
            SourcePayrollParameter bookTxfrEntryDesc = SourcePayrollParameter
                    .findSourcePayrollParameter(currSourceSystemCd,
                            SourcePayrollParameterCode.BookTransferEntryDescription);

            if (bookTxfrEntryDesc != null) {
                mSourceSystemBatchEntryDescriptions.put(bookTxfrKey, bookTxfrEntryDesc.getParameterValue());
            }

            String taxPaymentKey = NACHABatchType.TaxPayment + ":" + currSourceSystemCd;
            SourcePayrollParameter taxpaymentEntryDesc = SourcePayrollParameter
                    .findSourcePayrollParameter(currSourceSystemCd,
                            SourcePayrollParameterCode.TaxPaymentEntryDescription);

            if (taxpaymentEntryDesc != null) {
                mSourceSystemBatchEntryDescriptions.put(taxPaymentKey, taxpaymentEntryDesc.getParameterValue());
            }

            String retryPaymentKey = NACHABatchType.RetryPayment + ":" + currSourceSystemCd;
            SourcePayrollParameter retryPaymentEntryDesc = SourcePayrollParameter
                    .findSourcePayrollParameter(currSourceSystemCd,
                                                SourcePayrollParameterCode.RetryPaymentEntryDescription);

            if (retryPaymentEntryDesc != null) {
                mSourceSystemBatchEntryDescriptions.put(retryPaymentKey, retryPaymentEntryDesc.getParameterValue());
            }
        }
    }

    private void populateSystemParameterValues() {
        //populate system parameters
        ENABLE_ENCRYPTION = SystemParameter.findBooleanValue(
                SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);

        IMMEDIATE_DESTINATION = SystemParameter.findSystemParameter(
                SystemParameter.Code.JPMC_IMMEDIATE_DESTINATION).getSystemParameterValue();

        IMMEDIATE_DESTINATION_NAME = SystemParameter.findSystemParameter(
                SystemParameter.Code.JPMC_IMMEDIATE_DESTINATION_NAME).getSystemParameterValue();

        IMMEDIATE_ORIGIN = SystemParameter.findSystemParameter(
                SystemParameter.Code.JPMC_IMMEDIATE_ORIGIN).getSystemParameterValue();

        IMMEDIATE_ORIGIN_NAME = SystemParameter.findSystemParameter(
                SystemParameter.Code.JPMC_IMMEDIATE_ORIGIN_NAME).getSystemParameterValue();
    }

    /**
     * Validates that the passed offload group is a valid offload group code and that the passed run date is not a weekend or holiday
     *
     * @param pOffloadGroup Offload group to offload for
     * @param pRunDate      Run date to offload for
     */
    protected void validate(String pOffloadGroup, SpcfCalendar pRunDate) {
        if (pOffloadGroup == null) {
            throw new RuntimeException("NULL Offload Group");
        }

        //Set the offload group
        mOffloadGroup = OffloadGroup.findOffloadGroup(pOffloadGroup);
        if (mOffloadGroup == null) {
            throw new RuntimeException("Invalid offload group code: " + pOffloadGroup);
        }

        // This shouldn't really be here - method is doing too many things
        mOffloadDate = getOffloadDate(pRunDate);
    }

    private static SpcfCalendar getOffloadDate(SpcfCalendar pRunDate) {
        SpcfCalendar calculatedOffloadDate;

        // Set the run date for the offload
        if (pRunDate != null) {
            calculatedOffloadDate = pRunDate;
        } else {
            calculatedOffloadDate = PSPDate.getPSPTime();
        }

        //Set the time portion to zeroes for comparison purposes
        calculatedOffloadDate.setValues(calculatedOffloadDate.getYear(),
                calculatedOffloadDate.getMonth(),
                calculatedOffloadDate.getDay());

        return calculatedOffloadDate;
    }

    /**
     * If there are transactions applicable to the given file type for the offload date, generate the file and update the transaaction
     * and payroll statuses as appropriate
     *
     * @param pFileType The file type to generate
     * @throws IOException
     */
    private void generateFile(NACHAFileType pFileType, boolean isPayrollRunLevelFundingModel) throws Exception {
        // re-init global vars specific to a single NACHA file
        mRecordCount = 0;
        mEntryCount = 0;
        mAddendaCount = 0;
        mBatchCount = 0;

        nachaFile = null;

        if(pFileType != NACHAFileType.CCDPlus && SystemParameter.isSystemInTestState()){
             nachaFileList=new ArrayList<>();
             for(OffloadBatch offloadBatch: mOffloadBatchList){
                 addFinalizedNACHAFilesToList(pFileType, offloadBatch);
             }
            if(nachaFileList.isEmpty()){
                return;
            }else{
                //if nachaFileList is not empty then we need to check if STD Nacha file is finalized, if not then Mark it Finalized so that File path can be used of STD.
                for(OffloadBatch offloadBatch: mOffloadBatchList){
                    finalizeStandardNachaFile(pFileType, offloadBatch);
                }
            }

        }else{
            for (NACHAFile anyNachaFile : mOffloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized)) {
                if (anyNachaFile.getFileType() == pFileType) {
                    nachaFile = anyNachaFile;
                    break;
                }
            }

            if (nachaFile == null) {
                // Nothing to do
                return;
            }
        }
        // Update file name
        nachaFile.setFileName(getFileNameTemplate().replace("FTYPE", pFileType.toString()));

        logger.info("Creating ACH file: " + new File(nachaFile.getFileName()).getName());

        // Create the physical file and initialize the "totals" for the file
        NACHATotals fileTotals = new NACHATotals();

        PgpWriter writer = PgpWriterFactory.createInstance();
        writer.open(nachaFile.getFileName());

        try {
            writeFileHeaderRecord(writer, nachaFile.getFileIDModifier());

            if (isPayrollRunLevelFundingModel) {
                logger.info("ENABLE_PAYROLL_RUN_LEVEL_FUNDING_MODEL flag is on and generating NACHA file using SettlementDate");
                generateCompanyBatchesBySettlementDate(writer, fileTotals, nachaFile);
                generateIntuitBatchesBySettlementDate(writer, fileTotals, nachaFile);
            } else {
                logger.info("Generating NACHA file by Funding Model");
                // Create batches for company/employee (non-Intuit transactions) and write them to the file
                generateCompanyBatches(writer, fileTotals, nachaFile);

                // Create accumulated batches and write them to the file
                generateIntuitBatches(writer, fileTotals, nachaFile);
            }
            writeFileFooterRecord(writer, fileTotals);
            generateDummyBlock(writer);

            // Update totals
            //totals in test phase include PSPO and STD
            nachaFile.setCreditTxnTotalAmount(fileTotals.getTotalCreditAmount());
            nachaFile.setDebitTxnTotalAmount(fileTotals.getTotalDebitAmount());
        } finally {
            writer.close();
        }
    }

    private void finalizeStandardNachaFile(NACHAFileType pFileType, OffloadBatch offloadBatch) {
        //if offloadBatch is for STAndard, check for nachaFileType, and then if nachafile is not finalized and finalize it
        if(offloadBatch.getOffloadGroup().getOffloadGroupCd().equals("STD")){
            for (NACHAFile anyNachaFile : offloadBatch.getNACHAFileCollection()) {
                if (anyNachaFile.getFileType() == pFileType ) {
                    nachaFile=anyNachaFile;
                    if(!anyNachaFile.getStatus().equals(NACHAFileStatus.Finalized)){
                        nachaFile.setStatus(NACHAFileStatus.Finalized);
                        logger.error("Standard NACHA file is not in finalized state, so marking it finalized manually NACHA file id: "+nachaFile.getId());
                    }else{
                        break;
                    }
                }
            }
        }
    }

    private void addFinalizedNACHAFilesToList(NACHAFileType pFileType, OffloadBatch offloadBatch) {
        for (NACHAFile anyNachaFile : offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized)) {
            if (anyNachaFile.getFileType() == pFileType) {
                nachaFileList.add(anyNachaFile);
            }
        }
    }

    /**
     * Gets the last file name that the batch output
     * @return The last fully qualified file name
     */
    public String getOutputFileName() {
        return nachaFile.getFileName();
    }

    /**
     * Return the fully-qualified file name as OUTPUT_DIRECTORY/DATA_FILE_PREFIX/systemDateInMillies+FTYPE.txt
     * The string FTYPE is replaced in the stored procedure based on the file type being generated : CCD, PPD or CCDPlus
     * <p/>
     * System date in milliseconds is used to ensure uniqueness
     *
     * @return The name of the file
     */
    private String getFileNameTemplate() {
        String outputDir = OUTPUT_DIRECTORY;
        String filePrefix = outputDir + File.separator;

        String fileSuffix;
        if (ENABLE_ENCRYPTION) {
            fileSuffix = DATA_FILE_PREFIX + String.valueOf((new Date()).getTime()) + "FTYPE.pgp";
        } else {
            fileSuffix = DATA_FILE_PREFIX + String.valueOf((new Date()).getTime()) + "FTYPE.txt";
        }

        return filePrefix + fileSuffix;
    }

    /**
     * Evicts the objects in the given Object array from the Hibernate cache.
     *
     * @param pObjects   Object[]
     */
    private void evictObjectsFromCache(Object[] pObjects) {
        for (Object obj : pObjects) {
            if (obj != null) {
                Application.evict(obj);
            }
        }
    }

    private ScrollableResults findIntuitBatches(NACHAFile pNACHAFile) {

        logger.info("Getting intuit batch records. NachaFile Id=" + pNACHAFile.getId());
        String hqlSelect = null;

            hqlSelect = "  Select entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount) as TotalAmount, entryDetailRecord.Company.FundingModel.FundingModelCd\n" +
                    "    from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord\n" +
                    "   where entryDetailRecord.NACHAFile = :nachaFile\n" +
                    "     and entryDetailRecord.IntuitBankAccount is not null\n" +
                    "     and entryDetailRecord.InitiationDate = :initiationDate \n" +
                    "group by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, entryDetailRecord.Company.FundingModel.FundingModelCd\n" +
                    "order by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, entryDetailRecord.Company.FundingModel.FundingModelCd";

        String[] paramNames = new String[2];
        paramNames[0] = "nachaFile";
        paramNames[1] = "initiationDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = pNACHAFile;
        paramValues[1] = pNACHAFile.getOffloadBatch().getOffloadDate();

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        for (int i = 0; i < paramValues.length; i++) {
            queryObject.setParameter(paramNames[i], paramValues[i]);
        }

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private ScrollableResults findIntuitBatchesBySettlementDate(NACHAFile pNACHAFile) {

        logger.info("Getting intuit batch records. NachaFile Id=" + pNACHAFile.getId());
        String hqlSelect = "  Select entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount) as TotalAmount, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                "\n" +
                "    from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord\n" +
                "   where entryDetailRecord.NACHAFile = :nachaFile\n" +
                "     and entryDetailRecord.IntuitBankAccount is not null\n" +
                "     and entryDetailRecord.InitiationDate = :initiationDate \n" +
                "group by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                "\n" +
                "order by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate");

        String[] paramNames = new String[2];
        paramNames[0] = "nachaFile";
        paramNames[1] = "initiationDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = pNACHAFile;
        paramValues[1] = pNACHAFile.getOffloadBatch().getOffloadDate();

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        for (int i = 0; i < paramValues.length; i++) {
            queryObject.setParameter(paramNames[i], paramValues[i]);
        }

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }


    private ScrollableResults findIntuitBatches(List<NACHAFile> pNACHAFiles) {
        logger.info("Getting intuit batch records.");
        String hqlSelect = null;

             hqlSelect = "  Select entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount) as TotalAmount, entryDetailRecord.Company.FundingModel.FundingModelCd\n" +
                    "    from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord\n" +
                    "   where entryDetailRecord.NACHAFile in (:nachaFiles)\n" +
                    "     and entryDetailRecord.IntuitBankAccount is not null\n" +
                    "     and entryDetailRecord.InitiationDate = :initiationDate \n" +
                    "group by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, entryDetailRecord.Company.FundingModel.FundingModelCd\n" +
                    "order by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, entryDetailRecord.Company.FundingModel.FundingModelCd";

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        queryObject.setParameterList("nachaFiles", pNACHAFiles);
        queryObject.setParameter("initiationDate", pNACHAFiles.get(0).getOffloadBatch().getOffloadDate());

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private ScrollableResults findIntuitBatchesBySettlementDate(List<NACHAFile> pNACHAFiles) {
        logger.info("Getting intuit batch records.");

        String hqlSelect = "  Select entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, sum(entryDetailRecord.Amount) as TotalAmount, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                "\n" +
                "    from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord\n" +
                "   where entryDetailRecord.NACHAFile in (:nachaFiles)\n"+
                "     and entryDetailRecord.IntuitBankAccount is not null\n" +
                "     and entryDetailRecord.InitiationDate = :initiationDate \n" +
                "group by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                "\n" +
                "order by entryDetailRecord.NACHABatchType, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.IntuitBankAccount.Id, entryDetailRecord.CreditDebitIndicator, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate");

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        queryObject.setParameterList("nachaFiles", pNACHAFiles);
        queryObject.setParameter("initiationDate", pNACHAFiles.get(0).getOffloadBatch().getOffloadDate());

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * Create and write out Intuit accumulated batches and accumulated transactions for those batches iff the amounts of the accumulated
     * transactions is greater than zero
     *
     * @param pWriter    Initialized file writer
     * @param fileTotals Totals associated with the file
     * @param pNachaFile NACHA File created by the stored procedure
     * @throws IOException If there is a problem writing to the file
     */
    private void generateIntuitBatches(PgpWriter pWriter, NACHATotals fileTotals, NACHAFile pNachaFile)
            throws IOException {

        Collection<AccumulatedBatch> accumulatedBatches = new ArrayList<AccumulatedBatch>();

        AccumulatedBatch currAccumulatedBatch = null;
        NACHABatchType currNachaBatchType = null;
        SourceSystemCode currSourceSystemCd = null;
        IntuitBankAccount currIntuitBankAccount = null;
        AccumulatedIntuitAccount currAccumIntuitAccount = null;
        String currFundingModelCd = null;
        boolean isPPD = false;
        NACHAFileType fileType = null;

        ScrollableResults entryDetailResults=null;
        if(!nachaFileList.isEmpty() && DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFileList.get(0))){
            fileType = nachaFileList.get(0).getFileType();
            entryDetailResults = findIntuitBatches(nachaFileList);
        }else{
            fileType = pNachaFile.getFileType();
            entryDetailResults = findIntuitBatches(pNachaFile);
        }

        if(NACHAFileType.PPD.equals(fileType)) {
            isPPD = true;
        }
        
        try {  //todo understand
            while (entryDetailResults.next()) {
                NACHABatchType currRecBatchType = (NACHABatchType) entryDetailResults.get(0);
                SourceSystemCode currRecSourceSystemCd = (SourceSystemCode) entryDetailResults.get(1);
                SpcfUniqueId currRecIntuitBankAccountId = (SpcfUniqueId) entryDetailResults.get(2);
                CreditDebitCode currRecCreditDebitInd = (CreditDebitCode) entryDetailResults.get(3);
                SpcfMoney currRecTotalAmount = (SpcfMoney) entryDetailResults.get(4);
                String currRecFundingModelCd = null;
                currRecFundingModelCd = (String) entryDetailResults.get(5);

                IntuitBankAccount currRecIntuitBankAccount = Application.findById(IntuitBankAccount.class, currRecIntuitBankAccountId);
                ArrayList<AccumulatedIntuitTransaction> accumulatedIntuitTransactionsForAmount = getAccumulatedIntuitTransactionsForBAAndAmount(currRecIntuitBankAccount, currRecTotalAmount, currRecCreditDebitInd);

                if (currRecBatchType != currNachaBatchType || currRecSourceSystemCd != currSourceSystemCd || (isPPD && currFundingModelCd != currRecFundingModelCd)) {
                    String standardEntryDesc = mSourceSystemBatchEntryDescriptions.get(currRecBatchType + ":" + currRecSourceSystemCd);
                    NACHATotals batchTotals = new NACHATotals();
                    currAccumulatedBatch = new AccumulatedBatch(currRecBatchType, pNachaFile.getFileType(), mOffloadDate, batchTotals, standardEntryDesc);
                    currAccumIntuitAccount = new AccumulatedIntuitAccount(currRecIntuitBankAccount);
                    for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                        currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                    }
                    currAccumulatedBatch.addAccumulatedAccount(currAccumIntuitAccount, currRecIntuitBankAccount);
                    accumulatedBatches.add(currAccumulatedBatch);
                    
                    if(isPPD) {
                        DomainEntitySet<FundingModel> fundingModelSet = Application.find(FundingModel.class, FundingModel.FundingModelCd().equalTo(currRecFundingModelCd));
                        currAccumulatedBatch.setFundingModel(fundingModelSet.get(0));
                    }

                    currNachaBatchType = currRecBatchType;
                    currSourceSystemCd = currRecSourceSystemCd;
                    currIntuitBankAccount = currRecIntuitBankAccount;
                    currFundingModelCd = currRecFundingModelCd;
                } else {
                    if (!currRecIntuitBankAccount.equals(currIntuitBankAccount)) {
                        currAccumIntuitAccount = new AccumulatedIntuitAccount(currRecIntuitBankAccount);
                        for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                            currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                        }
                        currAccumulatedBatch.addAccumulatedAccount(currAccumIntuitAccount, currRecIntuitBankAccount);
                        currIntuitBankAccount = currRecIntuitBankAccount;
                    } else {
                        for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                            currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                        }
                    }
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(entryDetailResults.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            entryDetailResults.close();
        }

        //Last trace num is either 1) the maximum trace number of the file so far or, if there aren't anything other
        // than Intuit trace numbers in the file, the next possible trace number
        Long lastTraceNumForFile = EntryDetailRecord.findMaxTraceNumberForFile(pNachaFile);
        if (lastTraceNumForFile == null) {
            lastTraceNumForFile = EntryDetailRecord.getNextTraceNumber();
        }

        //Get all the accumulated batches for the file
        for (AccumulatedBatch currAccumBatch : accumulatedBatches) {
            //Get the accumulated Intuit bank accounts for the accumulated batch
            Collection<AccumulatedIntuitAccount> currAccumulatedTransactions = currAccumBatch.getAccumulatedAccounts();
            boolean bInitiatedBatchWrite = false;
            NACHATotals batchTotals = currAccumBatch.getBatchTotals();


            for (AccumulatedIntuitAccount intuitAccount : currAccumulatedTransactions) {
                //Get the accumulated transactions for the accumulated account
                Collection<AccumulatedIntuitTransaction> accumulatedTransaction = intuitAccount.getTransactions();

                //Only initiation writing of this batch to the file if there are transactions for the batch and we haven't
                //already initiatied it
                if (accumulatedTransaction.size() > 0 && !bInitiatedBatchWrite) {
                    bInitiatedBatchWrite = true;
                    currAccumBatch.setBatchNumber(++mBatchCount);
                }

                //For each of the accumulated transactions:
                //  Get the next trace number
                //  Get the entry detail record
                //  Add the entry detail record to the batch
                //  Update the individual non-Intuit (ee and er) transactions with the trace nubmer for this accumulated transaction
                for (AccumulatedIntuitTransaction currAccumTxn : accumulatedTransaction) {
                    //Only write the record if the transaction amount is greater than 0
                    if (currAccumTxn.getAmount().compareTo(SPCF_MONEY_ZERO) > 0) {
                        lastTraceNumForFile = lastTraceNumForFile + 1;
                        String currRecTraceNumber = lastTraceNumForFile.toString();
                        String currRecordData = currAccumTxn.getRecordData();
                        String strEntryDetail = processAndGetEntryDetailRecord(currRecordData, currAccumTxn.getAmount(),
                                currAccumTxn.getCreditOrDebit(), currRecTraceNumber, batchTotals, fileTotals);
                        currAccumBatch.addEntryDetailRecord(strEntryDetail, currAccumTxn.getCreditOrDebit(), currRecTraceNumber);
                    }
                }
            }

            //If we have initiated the writing of this batch, that means we could write it to the file
            if (bInitiatedBatchWrite) {
                currAccumBatch.writeToFile(pWriter);
                //Add the number of records in the batch to the total record count
                mRecordCount += currAccumBatch.getTotalNumberOfRecordsInBatch();
                mEntryCount += currAccumBatch.getNumberOfEntryDetailRecords();
                mAddendaCount += currAccumBatch.getNumberOfAddendaRecords();
            }
        }
    }

    private void generateIntuitBatchesBySettlementDate(PgpWriter pWriter,
                                                       NACHATotals fileTotals, NACHAFile pNachaFile) throws IOException {

        Collection<AccumulatedBatch> accumulatedBatches = new ArrayList<AccumulatedBatch>();

        AccumulatedBatch currAccumulatedBatch = null;
        NACHABatchType currNachaBatchType = null;
        SourceSystemCode currSourceSystemCd = null;
        IntuitBankAccount currIntuitBankAccount = null;
        AccumulatedIntuitAccount currAccumIntuitAccount = null;
        SpcfCalendar currSettlementDate = null;

        ScrollableResults entryDetailResults = null;
        if (!nachaFileList.isEmpty() && DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFileList.get(0))) {
            entryDetailResults = findIntuitBatchesBySettlementDate(nachaFileList);
        } else {
            entryDetailResults = findIntuitBatchesBySettlementDate(pNachaFile);
        }

        try {
            while (entryDetailResults.next()) {
                NACHABatchType currRecBatchType = (NACHABatchType) entryDetailResults.get(0);
                SourceSystemCode currRecSourceSystemCd = (SourceSystemCode) entryDetailResults.get(1);
                SpcfUniqueId currRecIntuitBankAccountId = (SpcfUniqueId) entryDetailResults.get(2);
                CreditDebitCode currRecCreditDebitInd = (CreditDebitCode) entryDetailResults.get(3);
                SpcfMoney currRecTotalAmount = (SpcfMoney) entryDetailResults.get(4);
                SpcfCalendar currRecSettlementDate = (SpcfCalendar) entryDetailResults.get(5);
                IntuitBankAccount currRecIntuitBankAccount = Application.findById(IntuitBankAccount.class, currRecIntuitBankAccountId);
                ArrayList<AccumulatedIntuitTransaction> accumulatedIntuitTransactionsForAmount = getAccumulatedIntuitTransactionsForBAAndAmount(currRecIntuitBankAccount, currRecTotalAmount, currRecCreditDebitInd);

                if (currRecBatchType != currNachaBatchType || currRecSourceSystemCd != currSourceSystemCd || !currRecSettlementDate.equals(currSettlementDate)) {
                    String standardEntryDesc = mSourceSystemBatchEntryDescriptions.get(currRecBatchType + ":" + currRecSourceSystemCd);
                    NACHATotals batchTotals = new NACHATotals();
                    currAccumulatedBatch = new AccumulatedBatch(currRecBatchType, pNachaFile.getFileType(), mOffloadDate, batchTotals, standardEntryDesc);
                    currAccumulatedBatch.setBatchSettlementDate(currRecSettlementDate);
                    currAccumIntuitAccount = new AccumulatedIntuitAccount(currRecIntuitBankAccount);
                    for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                        currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                    }
                    currAccumulatedBatch.addAccumulatedAccount(currAccumIntuitAccount, currRecIntuitBankAccount);
                    accumulatedBatches.add(currAccumulatedBatch);

                    currNachaBatchType = currRecBatchType;
                    currSourceSystemCd = currRecSourceSystemCd;
                    currIntuitBankAccount = currRecIntuitBankAccount;
                    currSettlementDate = currRecSettlementDate;
                } else {
                    if (!currRecIntuitBankAccount.equals(currIntuitBankAccount)) {
                        currAccumIntuitAccount = new AccumulatedIntuitAccount(currRecIntuitBankAccount);
                        for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                            currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                        }
                        currAccumulatedBatch.addAccumulatedAccount(currAccumIntuitAccount, currRecIntuitBankAccount);
                        currIntuitBankAccount = currRecIntuitBankAccount;
                    } else {
                        for (AccumulatedIntuitTransaction currTxn : accumulatedIntuitTransactionsForAmount) {
                            currAccumIntuitAccount.addAmount(currTxn.getCreditOrDebit(), currTxn.getAmount());
                        }
                    }
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(entryDetailResults.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            entryDetailResults.close();
        }

        //Last trace num is either 1) the maximum trace number of the file so far or, if there aren't anything other
        // than Intuit trace numbers in the file, the next possible trace number
        Long lastTraceNumForFile = EntryDetailRecord.findMaxTraceNumberForFile(pNachaFile);
        if (lastTraceNumForFile == null) {
            lastTraceNumForFile = EntryDetailRecord.getNextTraceNumber();
        }

        //Get all the accumulated batches for the file
        for (AccumulatedBatch currAccumBatch : accumulatedBatches) {
            //Get the accumulated Intuit bank accounts for the accumulated batch
            Collection<AccumulatedIntuitAccount> currAccumulatedTransactions = currAccumBatch.getAccumulatedAccounts();
            boolean bInitiatedBatchWrite = false;
            NACHATotals batchTotals = currAccumBatch.getBatchTotals();


            for (AccumulatedIntuitAccount intuitAccount : currAccumulatedTransactions) {
                //Get the accumulated transactions for the accumulated account
                Collection<AccumulatedIntuitTransaction> accumulatedTransaction = intuitAccount.getTransactions();

                //Only initiation writing of this batch to the file if there are transactions for the batch and we haven't
                //already initiatied it
                if (accumulatedTransaction.size() > 0 && !bInitiatedBatchWrite) {
                    bInitiatedBatchWrite = true;
                    currAccumBatch.setBatchNumber(++mBatchCount);
                }

                //For each of the accumulated transactions:
                //  Get the next trace number
                //  Get the entry detail record
                //  Add the entry detail record to the batch
                //  Update the individual non-Intuit (ee and er) transactions with the trace nubmer for this accumulated transaction
                for (AccumulatedIntuitTransaction currAccumTxn : accumulatedTransaction) {
                    //Only write the record if the transaction amount is greater than 0
                    if (currAccumTxn.getAmount().compareTo(SPCF_MONEY_ZERO) > 0) {
                        lastTraceNumForFile = lastTraceNumForFile + 1;
                        String currRecTraceNumber = lastTraceNumForFile.toString();
                        String currRecordData = currAccumTxn.getRecordData();
                        String strEntryDetail = processAndGetEntryDetailRecord(currRecordData, currAccumTxn.getAmount(),
                                currAccumTxn.getCreditOrDebit(), currRecTraceNumber, batchTotals, fileTotals);
                        currAccumBatch.addEntryDetailRecord(strEntryDetail, currAccumTxn.getCreditOrDebit(), currRecTraceNumber);
                    }
                }
            }

            //If we have initiated the writing of this batch, that means we could write it to the file
            if (bInitiatedBatchWrite) {
                currAccumBatch.writeToFile(pWriter);
                //Add the number of records in the batch to the total record count
                mRecordCount += currAccumBatch.getTotalNumberOfRecordsInBatch();
                mEntryCount += currAccumBatch.getNumberOfEntryDetailRecords();
                mAddendaCount += currAccumBatch.getNumberOfAddendaRecords();
            }
        }
    }

    private ArrayList<AccumulatedIntuitTransaction> getAccumulatedIntuitTransactionsForBAAndAmount(IntuitBankAccount pIntuitBA, SpcfMoney pTotalAmount, CreditDebitCode pCreditOrDebit) {
        ArrayList<AccumulatedIntuitTransaction> accumulatedTxns = new ArrayList<AccumulatedIntuitTransaction>();

        SpcfMoney remainingAmount = pTotalAmount;

        while (remainingAmount.compareTo(SPCF_MONEY_ZERO) > 0) {
            // the amount for this record is the maximum of the remaining amount and the NACHA-imposed limit
            SpcfMoney recordAmount = remainingAmount;

            if (recordAmount.compareTo(EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT) > 0) {
                recordAmount = EntryDetailRecord.NACHA_MAX_ENTRY_DETAIL_AMOUNT;
            }

            AccumulatedIntuitTransaction currRecAccumIntuitTxn =
                    new AccumulatedIntuitTransaction(pIntuitBA, recordAmount, pCreditOrDebit);

            accumulatedTxns.add(currRecAccumIntuitTxn);

            remainingAmount = new SpcfMoney(remainingAmount.subtract(recordAmount));
        }

        return accumulatedTxns;
    }

    public ScrollableResults findEntryDetailRecords(NACHAFile pNACHAFile) {
        String hqlSelect = "Select entryDetailRecord, entryDetailRecord.NACHABatchType, entryDetailRecord.LegalName, entryDetailRecord.StandardEntryDescription, \n" +
                        "       entryDetailRecord.Company.Id, entryDetailRecord.Company.FedTaxIdEnc, entryDetailRecord.Company.SourceSystemCd, " +
                        "       entryDetailRecord.Company.SourceCompanyId \n" +
                        "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                        "where entryDetailRecord.NACHAFile = :nachaFile \n" +
                        "      and entryDetailRecord.RecordDataEnc is not null \n" +
                        "      and entryDetailRecord.IntuitBankAccount is null \n" +
                        "      and entryDetailRecord.InitiationDate = :initiationDate \n" +
                        "order by abs(cast ((entryDetailRecord.TraceNumber) as java.lang.Long))";


        String[] paramNames = new String[2];
        paramNames[0] = "nachaFile";
        paramNames[1] = "initiationDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = pNACHAFile;
        paramValues[1] = pNACHAFile.getOffloadBatch().getOffloadDate();

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        for (int i = 0; i < paramValues.length; i++) {
            queryObject.setParameter(paramNames[i], paramValues[i]);
        }

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults findEDRsForSettlementDateGrouping(NACHAFile pNACHAFile) {
        String hqlSelect = "Select entryDetailRecord, entryDetailRecord.NACHABatchType, entryDetailRecord.LegalName, entryDetailRecord.StandardEntryDescription, \n" +
                "       entryDetailRecord.Company.Id, entryDetailRecord.Company.FedTaxIdEnc, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.Company.SourceCompanyId, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                " \n" +
                "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                "where entryDetailRecord.NACHAFile = :nachaFile \n" +
                "      and entryDetailRecord.RecordDataEnc is not null \n" +
                "      and entryDetailRecord.IntuitBankAccount is null \n" +
                "      and entryDetailRecord.InitiationDate = :initiationDate \n" +
                "order by abs(cast ((entryDetailRecord.TraceNumber) as java.lang.Long))";


        String[] paramNames = new String[2];
        paramNames[0] = "nachaFile";
        paramNames[1] = "initiationDate";

        Object[] paramValues = new Object[2];
        paramValues[0] = pNACHAFile;
        paramValues[1] = pNACHAFile.getOffloadBatch().getOffloadDate();

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        for (int i = 0; i < paramValues.length; i++) {
            queryObject.setParameter(paramNames[i], paramValues[i]);
        }

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults findEntryDetailRecords(List<NACHAFile> pNACHAFileList) {
        String hqlSelect = "Select entryDetailRecord, entryDetailRecord.NACHABatchType, entryDetailRecord.LegalName, entryDetailRecord.StandardEntryDescription, \n" +
                    "       entryDetailRecord.Company.Id, entryDetailRecord.Company.FedTaxIdEnc, entryDetailRecord.Company.SourceSystemCd, " +
                    "       entryDetailRecord.Company.SourceCompanyId \n" +
                    "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                    "where entryDetailRecord.NACHAFile in (:nachaFiles) \n" +
                    "      and entryDetailRecord.RecordDataEnc is not null \n" +
                    "      and entryDetailRecord.IntuitBankAccount is null \n" +
                    "      and entryDetailRecord.InitiationDate = :initiationDate \n" +
                    "order by abs(cast ((entryDetailRecord.TraceNumber) as java.lang.Long))";

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        queryObject.setParameterList("nachaFiles", pNACHAFileList);
        queryObject.setParameter("initiationDate", pNACHAFileList.get(0).getOffloadBatch().getOffloadDate());

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    public ScrollableResults findEDRsForSettlementDateGrouping(List<NACHAFile> pNACHAFileList) {
        String hqlSelect = "Select entryDetailRecord, entryDetailRecord.NACHABatchType, entryDetailRecord.LegalName, entryDetailRecord.StandardEntryDescription, \n" +
                "       entryDetailRecord.Company.Id, entryDetailRecord.Company.FedTaxIdEnc, entryDetailRecord.Company.SourceSystemCd, entryDetailRecord.Company.SourceCompanyId, " +
                Application.getTruncFunctionString("entryDetailRecord.SettlementDate") +
                " \n" +
                "from com.intuit.sbd.payroll.psp.domain.EntryDetailRecord as entryDetailRecord \n" +
                "where entryDetailRecord.NACHAFile in (:nachaFiles) \n" +
                "      and entryDetailRecord.RecordDataEnc is not null \n" +
                "      and entryDetailRecord.IntuitBankAccount is null \n" +
                "      and entryDetailRecord.InitiationDate = :initiationDate \n" +
                "order by abs(cast ((entryDetailRecord.TraceNumber) as java.lang.Long))";

        Query queryObject = Application.createHibernateQuery(hqlSelect);
        queryObject.setParameterList("nachaFiles", pNACHAFileList);
        queryObject.setParameter("initiationDate", pNACHAFileList.get(0).getOffloadBatch().getOffloadDate());

        return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }


    /**
     * @param pFileWriter Initialized file writer
     * @param fileTotals  File totals associated with the NACHA File
     * @param pNachaFile  NACHA File created by the stored procedure
     * @throws IOException If there is a problem writing to the file
     */
    private void generateCompanyBatches(PgpWriter pFileWriter,
                                        NACHATotals fileTotals,
                                        NACHAFile pNachaFile)
            throws IOException {
        SourceSystemCode currSourceSystemCd = null;
        NACHABatchType currBatchType = null;
        NACHATotals batchTotals = null;
        Batch currBatch = null;
        SpcfUniqueId currBatchCompanyId = null;
        NACHAFileType nachaFileType = pNachaFile.getFileType();
        ScrollableResults entryDetailResults=null;
        if(!nachaFileList.isEmpty() && DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFileList.get(0))){
           entryDetailResults = findEntryDetailRecords(nachaFileList);
        }else{
           entryDetailResults = findEntryDetailRecords(pNachaFile);
        }
        try {
            while (entryDetailResults.next()) {
                try {
                    EntryDetailRecord currRecord = (EntryDetailRecord) entryDetailResults.get(0);
                    pspRequestContextManager.setRequestContextCompany(currRecord.getCompany());
                    NACHABatchType currRecordBatchType = (NACHABatchType) entryDetailResults.get(1);
                    String currRecordCompanyLegalName = (String) entryDetailResults.get(2);
                    String currRecordStandardEntryDescription = (String) entryDetailResults.get(3);
                    SpcfUniqueId currRecordCompanyId = (SpcfUniqueId) entryDetailResults.get(4);
                    String currRecordCompanyFedTaxId = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName,(String) entryDetailResults.get(5));
                    SourceSystemCode currRecordSourceSystemCd = (SourceSystemCode) entryDetailResults.get(6);
                    String currCompanySourceCompanyId = (String) entryDetailResults.get(7);
                    //For each new company/batch type combo we see, create a new batch
                    if (!currRecordCompanyId.equals(currBatchCompanyId) || currRecordBatchType != currBatchType || nachaFileType.equals(NACHAFileType.CCDPlus) ) {
                        //We've found a new batch...if this isn't the very first batch, write the prior batch to the file
                        if (currBatch != null) {
                            currBatch.writeToFile(pFileWriter, nachaFileType);
                            //Add the number of records in the batch to the total record count
                            mRecordCount += currBatch.getTotalNumberOfRecordsInBatch();
                            mEntryCount += currBatch.getNumberOfEntryDetailRecords();
                            mAddendaCount += currBatch.getNumberOfAddendaRecords();
                        }

                        //Get the new current company and current batch type
                        currBatchCompanyId = currRecordCompanyId;
                        currBatchType = currRecordBatchType;
                        currSourceSystemCd = currRecordSourceSystemCd;

                        //If the database value is null, there is not a state-specific entry and we should get the standard
                        // entry description for this source system and batch type.
                        String standardEntryDesc = currRecordStandardEntryDescription;
                        if ( standardEntryDesc == null ) {
                            standardEntryDesc = mSourceSystemBatchEntryDescriptions.get(currBatchType + ":" + currSourceSystemCd);
                        }

                        //Create a new batch with a new batch totals
                        batchTotals = new NACHATotals();
                        String companyName = currRecordCompanyLegalName;
                        if (nachaFileType == NACHAFileType.CCD) {
                            companyName = INTUIT_CCD_BATCH_COMPANY_NAME;
                        }
                        currBatch = new Batch(currBatchType, companyName, currRecordCompanyFedTaxId, currCompanySourceCompanyId,
                                mOffloadDate, nachaFileType, batchTotals, standardEntryDesc);
                        currBatch.setBatchNumber(++mBatchCount);
                        currBatch.setFundingModel(currRecord.getCompany().getFundingModel());
                        //If this is CCDPlus file and credit entry detail record , add payment template code of entry detail record
                        //This will be used to state specific company header for nacha file. This logic added for MN-DEED1-PAYMENT template
                        //MN-DEED1-PAYMENT has different requirement for company header which will be generated during creating nacha file.
                        if (nachaFileType == NACHAFileType.CCDPlus && CreditDebitCode.Credit.equals(currRecord.getCreditDebitIndicator())) {
                            currBatch.setPaymentTemplateCd(currRecord.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
                        }
                    }

                    //Get the entry detail record data we'll write to the file, and add the totals for this record to the
                    // accumulated intuit amounts, file totals, and batch totals.  Add the record to the batch.
                    String strEntryDetailRecord = processAndGetEntryDetailRecord(currRecord.getRecordData(),
                            currRecord.getAmount(), currRecord.getCreditDebitIndicator(), currRecord.getTraceNumber(), batchTotals, fileTotals);

                    currBatch.addEntryDetailRecord(strEntryDetailRecord, currRecord.getCreditDebitIndicator(), currRecord.getTraceNumber());
                    // If this is a CCDPlus File add the addenda record
                    if (nachaFileType == NACHAFileType.CCDPlus) {
                        currBatch.addAddendaRecord(currRecord);
                    }

                    // To allow for unlimited size result sets, we need to keep the cache clean.
                    evictObjectsFromCache(entryDetailResults.get());
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            entryDetailResults.close();
        }

        //Write the last batch out to the file
        if (currBatch != null) {
            currBatch.writeToFile(pFileWriter, nachaFileType);
            //Add the number of records in the batch to the total record count
            mRecordCount += currBatch.getTotalNumberOfRecordsInBatch();
            mEntryCount += currBatch.getNumberOfEntryDetailRecords();
            mAddendaCount += currBatch.getNumberOfAddendaRecords();
        }
    }

    /**
     * @param pFileWriter Initialized file writer
     * @param fileTotals  File totals associated with the NACHA File
     * @param pNachaFile  NACHA File created by the stored procedure
     * @throws IOException If there is a problem writing to the file
     */
    private void generateCompanyBatchesBySettlementDate(PgpWriter pFileWriter,
                                        NACHATotals fileTotals,
                                        NACHAFile pNachaFile)
            throws IOException {
        SourceSystemCode currSourceSystemCd = null;
        NACHABatchType currBatchType = null;
        NACHATotals batchTotals = null;
        Batch currBatch = null;
        SpcfUniqueId currBatchCompanyId = null;
        SpcfCalendar currSettlementDate = null;
        NACHAFileType nachaFileType = pNachaFile.getFileType();
        ScrollableResults entryDetailResults=null;
        if(!nachaFileList.isEmpty() && DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFileList.get(0))){
            entryDetailResults = findEDRsForSettlementDateGrouping(nachaFileList);
        }else{
            entryDetailResults = findEDRsForSettlementDateGrouping(pNachaFile);
        }
        try {
            while (entryDetailResults.next()) {
                try {
                    EntryDetailRecord currRecord = (EntryDetailRecord) entryDetailResults.get(0);
                    pspRequestContextManager.setRequestContextCompany(currRecord.getCompany());
                    NACHABatchType currRecordBatchType = (NACHABatchType) entryDetailResults.get(1);
                    String currRecordCompanyLegalName = (String) entryDetailResults.get(2);
                    String currRecordStandardEntryDescription = (String) entryDetailResults.get(3);
                    SpcfUniqueId currRecordCompanyId = (SpcfUniqueId) entryDetailResults.get(4);
                    String currRecordCompanyFedTaxId = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName,(String) entryDetailResults.get(5));
                    SourceSystemCode currRecordSourceSystemCd = (SourceSystemCode) entryDetailResults.get(6);
                    String currCompanySourceCompanyId = (String) entryDetailResults.get(7);
                    SpcfCalendar currRecordSettlementDate = (SpcfCalendar) entryDetailResults.get(8);

                    //For each new company/batch type combo we see, create a new batch
                    //New batch for every CCDPlus record --> Required for Settlement Date based grouping for CCDPlus (Tax offload target state).
                    if (!currRecordCompanyId.equals(currBatchCompanyId) || currRecordBatchType != currBatchType || !currRecordSettlementDate.equals(currSettlementDate) || nachaFileType.equals(NACHAFileType.CCDPlus)) {
                        //We've found a new batch...if this isn't the very first batch, write the prior batch to the file
                        if (currBatch != null) {
                            currBatch.writeToFile(pFileWriter, nachaFileType);
                            //Add the number of records in the batch to the total record count
                            mRecordCount += currBatch.getTotalNumberOfRecordsInBatch();
                            mEntryCount += currBatch.getNumberOfEntryDetailRecords();
                            mAddendaCount += currBatch.getNumberOfAddendaRecords();
                        }

                        //Get the new current company and current batch type
                        currBatchCompanyId = currRecordCompanyId;
                        currBatchType = currRecordBatchType;
                        currSourceSystemCd = currRecordSourceSystemCd;
                        currSettlementDate = currRecordSettlementDate;

                        //If the database value is null, there is not a state-specific entry and we should get the standard
                        // entry description for this source system and batch type.
                        String standardEntryDesc = currRecordStandardEntryDescription;
                        if ( standardEntryDesc == null ) {
                            standardEntryDesc = mSourceSystemBatchEntryDescriptions.get(currBatchType + ":" + currSourceSystemCd);
                        }

                        //Create a new batch with a new batch totals
                        batchTotals = new NACHATotals();
                        String companyName = currRecordCompanyLegalName;
                        if (nachaFileType == NACHAFileType.CCD) {
                            companyName = INTUIT_CCD_BATCH_COMPANY_NAME;
                        }
                        currBatch = new Batch(currBatchType, companyName, currRecordCompanyFedTaxId, currCompanySourceCompanyId,
                                mOffloadDate, nachaFileType, batchTotals, standardEntryDesc);
                        currBatch.setBatchNumber(++mBatchCount);
                        currBatch.setBatchSettlementDate(currRecordSettlementDate);
                        //If this is CCDPlus file and credit entry detail record , add payment template code of entry detail record
                        //This will be used to state specific company header for nacha file. This logic added for MN-DEED1-PAYMENT template
                        //MN-DEED1-PAYMENT has different requirement for company header which will be generated during creating nacha file.
                        if (nachaFileType == NACHAFileType.CCDPlus && CreditDebitCode.Credit.equals(currRecord.getCreditDebitIndicator())) {
                            currBatch.setPaymentTemplateCd(currRecord.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd());
                        }
                    }

                    //Get the entry detail record data we'll write to the file, and add the totals for this record to the
                    // accumulated intuit amounts, file totals, and batch totals.  Add the record to the batch.
                    String strEntryDetailRecord = processAndGetEntryDetailRecord(currRecord.getRecordData(),
                            currRecord.getAmount(), currRecord.getCreditDebitIndicator(), currRecord.getTraceNumber(), batchTotals, fileTotals);

                    currBatch.addEntryDetailRecord(strEntryDetailRecord, currRecord.getCreditDebitIndicator(), currRecord.getTraceNumber());
                    // If this is a CCDPlus File add the addenda record
                    if (nachaFileType == NACHAFileType.CCDPlus) {
                        currBatch.addAddendaRecord(currRecord);
                    }

                    // To allow for unlimited size result sets, we need to keep the cache clean.
                    evictObjectsFromCache(entryDetailResults.get());
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            entryDetailResults.close();
        }

        //Write the last batch out to the file
        if (currBatch != null) {
            currBatch.writeToFile(pFileWriter, nachaFileType);
            //Add the number of records in the batch to the total record count
            mRecordCount += currBatch.getTotalNumberOfRecordsInBatch();
            mEntryCount += currBatch.getNumberOfEntryDetailRecords();
            mAddendaCount += currBatch.getNumberOfAddendaRecords();
        }
    }

    /**
     * Add to the batch totals, file totals, hash values, and add the trace number to the String representing the record
     *
     * @param pRecordData    Entry detail record minus the trace number
     * @param pAmount        Amount for entry detail record
     * @param pCreditDebitCd If this entry detail record is a credit or a debit
     * @param pTraceNumber   The trace number to add to this entry detail record
     * @param pBatchTotals   The batch totals for the batch this entry detail record is part of
     * @param pFileTotals    The file totals for the file this entry detail record is a part of
     * @return The String representing the entry detail record, including the trace number
     */
    private String processAndGetEntryDetailRecord(String pRecordData, SpcfMoney pAmount,
                                                  CreditDebitCode pCreditDebitCd, String pTraceNumber,
                                                  NACHATotals pBatchTotals, NACHATotals pFileTotals) {
        StringBuilder strEntryDetailRecord = new StringBuilder();

        // The hash calculations are done on the middle (first) eight digit of the record's routing number
        String hashString = getRoutingNumberFromRecordData(pRecordData).substring(0, 8);
        pBatchTotals.updateHash(hashString);
        pFileTotals.updateHash(hashString);

        // Create the String value of the entry detail record as the record data and the formatted trace number
        strEntryDetailRecord.append(pRecordData);
        strEntryDetailRecord.append(StringFormatter.formatString(pTraceNumber, 15, '0', true));
        strEntryDetailRecord.append(ACH_FILE_EOL);

        // Add to the appropriate file and batch totals
        if (pCreditDebitCd.equals(CreditDebitCode.Debit)) {
            pBatchTotals.addToTotalDebit(pAmount);
            pFileTotals.addToTotalDebit(pAmount);
        } else if (pCreditDebitCd.equals(CreditDebitCode.Credit)) {
            pBatchTotals.addToTotalCredit(pAmount);
            pFileTotals.addToTotalCredit(pAmount);
        }

        return strEntryDetailRecord.toString();
    }

    /**
     * Write the "1" record to the file
     *
     * @param pWriter       Initialized file writer
     * @param pUniqueFileId File ID modifier 0-9 or A-Z
     * @throws IOException If there is a problem writing to the file
     */
    private void writeFileHeaderRecord(PgpWriter pWriter, String pUniqueFileId) throws IOException {
        pWriter.write(FILE_HEADER_REC_TYPE_CODE);
        pWriter.write(PRIORITY_CODE);
        pWriter.write(IMMEDIATE_DESTINATION);
        pWriter.write(IMMEDIATE_ORIGIN);

        SpcfCalendar offloadDateTime = mOffloadGroup.getCalendarForCutoffTime(mOffloadDate);
        pWriter.write(StringFormatter.formatDate(offloadDateTime, ACH_DATE_TIME_FORMAT));
        pWriter.write(pUniqueFileId);
        pWriter.write(RECORD_SIZE);
        pWriter.write(BLOCKING_FACTOR);
        pWriter.write(FORMAT_CODE);
        pWriter.write(StringFormatter.formatString(IMMEDIATE_DESTINATION_NAME, IMMEDIATE_DEST_NAME_LENGTH));
        pWriter.write(StringFormatter.formatString(IMMEDIATE_ORIGIN_NAME, IMMEDIATE_ORIGIN_NAME_LENGTH));
        pWriter.write(StringFormatter.formatString(REFERENCE_CODE, REFERENCE_CODE_LENGTH));
        pWriter.write(ACH_FILE_EOL);

        mRecordCount++;
    }

    /**
     * Write the final "9" record to the file
     *
     * @param pWriter     pWriter Initialized file writer
     * @param pFileTotals Totals for the file
     * @throws IOException If there is a problem writing to the file
     */
    private void writeFileFooterRecord(PgpWriter pWriter, NACHATotals pFileTotals) throws IOException {
        BigDecimal bdTotalCreditAmount = SpcfUtils.convertToBigDecimal(pFileTotals.getTotalCreditAmount());
        BigDecimal bdTotalDebitAmount = SpcfUtils.convertToBigDecimal(pFileTotals.getTotalDebitAmount());

        pWriter.write(FILE_FOOTER_REC_TYPE_CODE);
        mRecordCount++;  // let's count self first since it is required for calculation

        // Calculate the block count
        int blockCount = ((mRecordCount % 10) == 0) ? mRecordCount / 10 : (mRecordCount / 10 + 1);
        pWriter.write(StringFormatter.formatLong(mBatchCount, 6));                        // batch count
        pWriter.write(StringFormatter.formatLong(blockCount, 6));                        // block count
        pWriter.write(StringFormatter.formatLong(mEntryCount + mAddendaCount, 8));      // entry/addenda count
        pWriter.write(StringFormatter.formatLong(pFileTotals.getEntryHash(), 10));       // entry hash
        pWriter.write(StringFormatter.formatCurrencyNoDecimalPoint(bdTotalDebitAmount, 12));   // total debit amount
        pWriter.write(StringFormatter.formatCurrencyNoDecimalPoint(bdTotalCreditAmount, 12)); // total credit amount
        pWriter.write(StringFormatter.formatString(null, 39));                           // reserved
        pWriter.write(ACH_FILE_EOL);
    }

    /**
     * Calculates and writes out the final block of "9"'s required by the NACHA file standards
     *
     * @param writer Initialized file writer
     * @throws IOException If there is a problem writing to the file
     */
    private void generateDummyBlock(PgpWriter writer) throws IOException {
        while ((mRecordCount % 10) != 0) {
            for (int i = 0; i < 94; i++) {
                writer.write('9');
            }
            writer.write(ACH_FILE_EOL);
            mRecordCount++;
        }
    }

    /**
     * gen
     * Returns the routing number from an entry detail record
     *
     * @param pRecordData Entry detail record
     * @return The routing number at positions specified by ACH in the entry detail record
     */
    private String getRoutingNumberFromRecordData(String pRecordData) {
        return pRecordData.substring(3, 12);
    }

    /**
     * The offload batch for this run of offload.
     *
     * @return NULL if the offload run was invalid; otherwise, the created OffloadBatch
     */
    public OffloadBatch getOffloadBatch() {
        return mOffloadBatch;
    }

    public static String getFeeEventsBatchJobInstanceParameter(String pOffloadBatchId, SpcfCalendar pProcessingDate) {
        return pOffloadBatchId + "%" +
                pProcessingDate.getYear() + "%" +
                pProcessingDate.getMonth() + "%" +
                pProcessingDate.getDay() + "%" +
                pProcessingDate.getHour() + "%" +
                pProcessingDate.getMinute() + "%" +
                pProcessingDate.getSecond() + "%" +
                pProcessingDate.getMillisecond();
    }
    
}
