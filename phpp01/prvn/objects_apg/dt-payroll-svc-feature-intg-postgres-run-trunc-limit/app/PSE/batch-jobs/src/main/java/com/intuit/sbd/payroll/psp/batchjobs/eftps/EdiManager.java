package com.intuit.sbd.payroll.psp.batchjobs.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.*;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.ops.eftpsBp.*;
import com.paycycle.ops.eftpsBp.EdiFile;
import com.paycycle.util.PgpUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.FlushMode;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Nov 8, 2010
 * Time: 12:27:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiManager {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(EdiManager.class);

    private static boolean checkFileIsStable(File pFile) {
        String fileStabilityDelayProp = EftpsUtil.getConfigString("psp_agency_file_stability_delay", "0");
        int fileStabilityDelay = Integer.parseInt(fileStabilityDelayProp);

        //
        // We need to ensure that we do not attempt to process a file that is currently being uploaded to PSP
        // from either the AS400 or TFA.  If the file is in the process of being transferred, we will defer
        // processing of the file in this cycle (in which case the we will try again the next time the job runs.)
        //

        long prevFileSize = pFile.length();

        if (fileStabilityDelay > 0) {
            BatchUtils.delay(fileStabilityDelay); // wait configured time to see if the file size changes.
        }

        long curFileSize = pFile.length();

        if (curFileSize == 0) {
            logger.warn(String.format("Pending EDI file %s has length zero.", pFile.getPath()));
        }

        return (curFileSize != 0) && (curFileSize == prevFileSize);
    }

    private static void processFilesInDir(String pSourceDirectory, boolean pTranslateFile) {
        processFilesInDir(pSourceDirectory, pTranslateFile, EftpsUtil.getWorkDir(), EftpsUtil.getErrDir());
    }

    private static void processFilesInDir(String pSourceDirectory, boolean pTranslateFile, String pWorkDirectory, String pErrDirectory) {
        logger.info(String.format("Begin processing files in directory %s", pSourceDirectory));

        List<File> fileList = EftpsUtil.getFilesFromDir(pSourceDirectory);

        if (fileList.isEmpty()) {
            logger.info(String.format("No files to process in specified directory %s", pSourceDirectory));
            return;
        }

        for (File file : fileList) {
            EdiEftpsFileValidator ediFile;
            File encryptedFile = file;
            try {
                //
                // Before attempting to process the file, check to ensure it is stable and ready for processing
                // (i.e. that a file transfer is not in progress)
                //
                if (!checkFileIsStable(file)) {
                    logger.warn(String.format("File %s is in transition (processing deferred)", file.getPath()));
                    continue;
                }

                if(encryptedFile.getName().endsWith(".pgp")){
                    file = PgpUtils.getUnencryptedFile(encryptedFile);
                }

                if (pTranslateFile) {
                    //
                    // If the file is from the AS400, it needs to be translated (GS02 and 80-byte wrapped)
                    //
                    file = EdiFileAs400Translator.translateFromAs400(file, EftpsUtil.getWorkDir());
                    if(encryptedFile.getName().endsWith(".pgp")){
                        encryptedFile = EftpsUtil.moveFile(encryptedFile, pWorkDirectory);
                    }
                } else {
                    //
                    // Attempt to move the file from the source directory to the working directory
                    //
                    file = EftpsUtil.moveFile(file, pWorkDirectory);
                    if(encryptedFile.getName().endsWith(".pgp")){
                        encryptedFile = EftpsUtil.moveFile(encryptedFile, pWorkDirectory);
                    }
                }

                //
                // Create a new eftps file validator to validate this file
                //
                ediFile = new EdiEftpsFileValidator(file);
            } catch (Throwable t) {
                logger.error(String.format("Error moving EDI file %s to working directory %s ",
                                           file.getPath(), EftpsUtil.getWorkDir()), t);
                continue;
            }

            try {
                if (!ediFile.isValid()) {
                    throw new RuntimeException("EDI file failed validation.");
                }

                switch (ediFile.getEftpsFileType()) {
                    case EftpsForecast: // support TBD
                    case EftpsForecastAck: // support TBD
                        break;
                    case EftpsEnrollment: // not applicable (originates at Intuit and no AS400 equivalent)
                    case EftpsEnrollmentResponseAck: // not applicable (originates at Intuit and no AS400 equivalent)
                        break;
                    case EftpsPaymentAck:
                    case EftpsEnrollmentAck:
                    case EftpsPaymentReturnAck:
                    case EftpsPaymentResponseAck:
                    case EftpsPaymentConfirmationAck: // handles PSP/AS400 processing accordingly
                        processAcknowledgementFile(ediFile.getEdiFile().getPath());
                        break;
                    case EftpsEnrollmentResponse: // PSP only
                        processEnrollmentResponseFile(ediFile.getEdiFile().getPath());
                        break;
                    case EftpsPayment: // parse/process AS400 payment file (record keeping only)
                        processAS400PaymentFile(ediFile.getEdiFile().getPath());
                        break;
                    case EftpsPaymentResponse: // handles PSP/AS400 processing accordingly
                        processPaymentResponseFile(ediFile.getEdiFile().getPath());
                        break;
                    case EftpsPaymentReturn: // PSP only
                        processPaymentReturnFile(ediFile.getEdiFile().getPath());
                        break;
                    case EftpsPaymentConfirmation: // PSP only
                        processPaymentConfirmationFile(ediFile.getEdiFile().getPath());
                        break;
                    case StateEdiPaymentResponse: // handles PSP/AS400 processing accordingly
                        processStateEDIPaymentResponseFile(ediFile.getEdiFile().getPath()); 
                        break;
                    case StateEdiPaymentAck:          // Ack file state EDI payment file handles PSP/AS400 processing
                        processStateEDIAcknowledgementFile(ediFile.getEdiFile().getPath());
                        break;                    
                    default:
                        throw new RuntimeException(String.format("Unsupported EdiFileType in processFilesInDir (%s)",
                                                                 ediFile.getEftpsFileType()));
                }
            } catch (Throwable t) {
                if(encryptedFile.getName().endsWith(".pgp")) {
                    logger.error(String.format("Error processing EDI file %s ", encryptedFile.getPath()), t);

                    EftpsUtil.updateEftpsFileErrorStatus(ediFile.getFileId(), encryptedFile, pErrDirectory);
                } else {
                    logger.error(String.format("Error processing EDI file %s ", file.getPath()), t);

                    EftpsUtil.updateEftpsFileErrorStatus(ediFile.getFileId(), file, pErrDirectory);
                }
            } finally {
                if(encryptedFile.getName().endsWith(".pgp"))
                    file.delete();
            }
        }

        logger.info(String.format("End processing files in directory %s", pSourceDirectory));
    }

    private static void sendEmail(String pMessageHeader, List<Integer> pFileIdList) {
        if (!pFileIdList.isEmpty()) {
            try {
                try {
                    Application.beginUnitOfWork();

                    Integer[] iArray = pFileIdList.toArray(new Integer[pFileIdList.size()]);
                    DomainEntitySet<EftpsFile> files = Application.find(EftpsFile.class, EftpsFile.FileId().in(iArray));
                    sendEmail(pMessageHeader, files);
                } finally {
                    Application.rollbackUnitOfWork();
                }
            } catch (Throwable t) {
                logger.error("Error creating PSP EFTPS Notification email. ", t);
            }
        }
    }

    private static void sendEmailForEDIPayments(String pMessageHeader, List<Integer> pFileIdList) {

        if (!pFileIdList.isEmpty()) {
            try {
                Application.beginUnitOfWork();

                Integer[] iArray = pFileIdList.toArray(new Integer[pFileIdList.size()]);
                DomainEntitySet<StateEdiTaxFile> files = Application.find(StateEdiTaxFile.class, StateEdiTaxFile.FileId().in(iArray));
                if (!files.isEmpty()) {
                    StringBuilder messageBody = new StringBuilder();
                    String crlf = "\r\n";

                    // Build the message body
                    messageBody.append(pMessageHeader).append(crlf);

                    for (int i = pMessageHeader.length(); i > 0; --i) {
                        messageBody.append("-");
                    }

                    messageBody.append(crlf);

                    for (StateEdiTaxFile stateEdiTaxFile : files) {
                        messageBody.append(crlf); // add empty line for separation
                        messageBody.append("File Name    : ").append(new File(stateEdiTaxFile.getFileName()).getName()).append(crlf);
                        messageBody.append("File Type    : ").append(stateEdiTaxFile.getFileType().name()).append(crlf);
                        messageBody.append("File Code    : ").append(stateEdiTaxFile.getFileCode()).append(crlf);
                        messageBody.append("File Id      : ").append(stateEdiTaxFile.getFileId()).append(crlf);
                        messageBody.append("File Status  : ").append(stateEdiTaxFile.getStatusCd().name()).append(crlf);
                        messageBody.append("System Owner : ").append(stateEdiTaxFile.getSystemOwner().name()).append(crlf);

                        switch (stateEdiTaxFile.getFileType()) {
                            case StateEdiPayment: {
                                Expression<EdiPaymentDetail> query = new Query<EdiPaymentDetail>()
                                        .Select(EdiPaymentDetail.Id().Count(), EdiPaymentDetail.PaymentAmount().Sum())
                                        .Where(EdiPaymentDetail.ParentFile().equalTo(stateEdiTaxFile));
                                List result = Application.executeQuery(EdiPaymentDetail.class, query);

                                // 'result' will be an ArrayList of Object[2] containing aggregates:
                                //    [0] = Count of detail records (Integer), [1] = Sum of all detail tax amounts (SpcfMoney)
                                // (and since query is aggregation, ArrayList will only contain one member)
                                if (!result.isEmpty() && (result.get(0) != null)) {
                                    Object[] values = (Object[]) result.get(0);

                                    if (values[0] != null) {
                                        messageBody.append("Txn Count   : ").append(values[0].toString()).append(crlf);
                                    }

                                    if (values[1] != null) {
                                        BigDecimal amt = SpcfUtils.convertToBigDecimal((SpcfMoney) values[1]);
                                        messageBody.append("Tax Amount  : ").append(String.format("$%(,.2f", amt)).append(crlf);
                                    }
                                }
                                break;
                            }
                        }
                    }

                    // Send the email
                    BatchUtils.sendTaxPaymentNotificationEmail("PSP EDI Notification", messageBody.toString());
                }
            } catch (Throwable t) {
                logger.error("Error creating PSP EDI Notification email. ", t);
            }
            finally {
                Application.rollbackUnitOfWork();
            }

        }
    }

    private static void sendEmail(String pMessageHeader, DomainEntitySet<EftpsFile> pEftpsFileList) {
        if (!pEftpsFileList.isEmpty()) {
            StringBuilder messageBody = new StringBuilder();
            String crlf = "\r\n";

            //
            // Build the message body
            //

            messageBody.append(pMessageHeader).append(crlf);

            for (int i = pMessageHeader.length(); i > 0; --i) {
                messageBody.append("-");
            }

            messageBody.append(crlf);

            for (EftpsFile eftpsFile : pEftpsFileList) {
                messageBody.append(crlf); // add empty line for separation
                messageBody.append("File Name    : ").append(new File(eftpsFile.getFileName()).getName()).append(crlf);
                messageBody.append("File Type    : ").append(eftpsFile.getFileType().name()).append(crlf);
                messageBody.append("File SubType : ").append(eftpsFile.getFileSubtype().name()).append(crlf);
                messageBody.append("File Code    : ").append(eftpsFile.getFileCode()).append(crlf);
                messageBody.append("File Id      : ").append(eftpsFile.getFileId()).append(crlf);
                messageBody.append("File Status  : ").append(eftpsFile.getStatusCd().name()).append(crlf);
                messageBody.append("System Owner : ").append(eftpsFile.getSystemOwner().name()).append(crlf);

                switch (eftpsFile.getFileType()) {
                    case EftpsPayment: {
                        Expression<EftpsPaymentDetail> query = new Query<EftpsPaymentDetail>()
                                .Select(EftpsPaymentDetail.Id().Count(), EftpsPaymentDetail.PaymentAmount().Sum())
                                .Where(EftpsPaymentDetail.ParentFile().equalTo(eftpsFile));
                        List result = Application.executeQuery(EftpsPaymentDetail.class, query);

                        //
                        // 'result' will be an ArrayList of Object[2] containing aggregates:
                        //    [0] = Count of detail records (Integer), [1] = Sum of all detail tax amounts (SpcfMoney)
                        // (and since query is aggregation, ArrayList will only contain one member)
                        //

                        if (!result.isEmpty() && (result.get(0) != null)) {
                            Object[] values = (Object[]) result.get(0);

                            if (values[0] != null) {
                                messageBody.append("Txn Count   : ").append(values[0].toString()).append(crlf);
                            }

                            if (values[1] != null) {
                                BigDecimal amt = SpcfUtils.convertToBigDecimal((SpcfMoney) values[1]);
                                messageBody.append("Tax Amount  : ").append(String.format("$%(,.2f", amt)).append(crlf);
                            }
                        }
                        break;
                    }

                    case EftpsEnrollment: {
                        Expression<EftpsEnrollmentDetail> query = new Query<EftpsEnrollmentDetail>()
                                .Select(EftpsEnrollmentDetail.Id().Count())
                                .Where(EftpsEnrollmentDetail.ParentFile().equalTo(eftpsFile));
                        List result = Application.executeQuery(EftpsEnrollmentDetail.class, query);

                        if (!result.isEmpty() && (result.get(0) != null)) {
                            messageBody.append("Txn Count   : ").append(result.get(0).toString()).append(crlf);
                        }
                        break;
                    }

                    case EftpsPaymentReturn: {
                        Expression<EftpsPaymentDetail> query = new Query<EftpsPaymentDetail>()
                                .Select(EftpsPaymentDetail.Id().Count(), EftpsPaymentDetail.PaymentAmount().Sum())
                                .Where(EftpsPaymentDetail.ReturnFile().equalTo(eftpsFile).And(EftpsPaymentDetail.BaseParentFile().SystemOwner().equalTo(SystemOwnerType.AS400)));
                        List result = Application.executeQuery(EftpsPaymentDetail.class, query);

                        if (!result.isEmpty() && (result.get(0) != null)) {
                            Object[] values = (Object[]) result.get(0);

                            if (values[0] != null) {
                                messageBody.append("As400 returned Txn Count   : ").append(values[0].toString()).append(crlf);
                            }

                            if (values[1] != null) {
                                BigDecimal amt = SpcfUtils.convertToBigDecimal((SpcfMoney) values[1]);
                                messageBody.append("As400 returned Tax Amount  : ").append(String.format("$%(,.2f", amt)).append(crlf);
                            }
                        }
                        break;
                    }
                }
            }

            //
            // Send the email
            //
            BatchUtils.sendTaxPaymentNotificationEmail("PSP EFTPS Notification", messageBody.toString());
        }
    }

    /**
     * Detect files waiting to be sent to TFA and send them.
     */
    public static void processPendingTransmissions() {
        logger.info("Begin processPendingTransmissions.");

        //
        // Upload PendingTransmission files
        //
        FtpsEftpsTfaFileUpload tfaFileUpload = new FtpsEftpsTfaFileUpload();
        tfaFileUpload.uploadOnS3();

        //
        // Send an email if any files were uploaded
        //
        sendEmail("EFTPS EDI file send summary", tfaFileUpload.getUploadedFileIdList());

        logger.info("End processPendingTransmissions.");
    }

    /**
     * Detect files waiting to be sent to AS400 and transmit them.
     */
    public static void transmitAS400Files() {
        logger.info("Begin transmitAS400Files.");

        new SftpEftpsAs400FileUpload().upload();

        logger.info("End transmitAS400Files.");
    }

    /**
     * Download files from TFA and to process them.
     */
    public static void downloadFileFromS3() throws Exception {
        logger.info("Begin downloadFileFromS3.");
        FileStore fs = BatchUtils.getFileStore();
        for (String fileName : fs.listFiles(EftpsUtil.getS3Bucket(),EftpsUtil.getS3InboundFolder(),".*")) {
            logger.info("Downloading started form S3 file="+fileName);
            InputStream inputStream = fs.readFileAsStream(EftpsUtil.getS3Bucket(), fileName);
            File file = new File(EftpsUtil.getTfaDir()+File.separator+FilenameUtils.getName(fileName)+".pgp");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
            logger.info("Downloading completed form S3 file="+fileName);
            logger.info("Archive started S3 file="+fileName);
            fs.moveFile(EftpsUtil.getS3Bucket(),fileName,EftpsUtil.getS3ArchiveFolder()+FilenameUtils.getName(fileName));
            logger.info("Archive completed S3 file="+fileName);
        }
        logger.info("End downloadFileFromS3.");
    }

    /**
     * Detect files arriving from TFA and process them.
     */
    public static void processWaitingResponseFiles() {
        logger.info("Begin processWaitingResponseFiles.");

        processFilesInDir(EftpsUtil.getTfaDir(), false);

        logger.info("End processWaitingResponseFiles.");
    }

    public static void processAS400Files() {
        logger.info("Begin processAS400Files.");

        processFilesInDir(EftpsUtil.getAS400Dir(), true);

        logger.info("End processAS400Files.");
    }

    public static void archiveFiles() throws S3ConnectionException,S3UploadException {
        logger.info("Begin archiveFiles.");

        try {
            String archiveDir = EftpsUtil.getArchiveDir();

            Application.beginUnitOfWork();

            DomainEntitySet<EftpsFile> eftpsFiles = EftpsFile.getCompletedEftpsFiles();

            for (EftpsFile eftpsFile : eftpsFiles) {
                String fileName = S3UploadUtils.archive(BatchJobType.EftpsResponse.name(),archiveDir,eftpsFile.getFileName());
                eftpsFile.setStatusCd(EdiFileStatus.Archived);
                eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                eftpsFile.setFileName(fileName);

                Application.save(eftpsFile);
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        logger.info("End archiveFiles.");
    }

    public static void processEnrollments() {
        logger.info("Begin processing EFTPS Enrollments.");

        StopWatch timer = StopWatch.startTimer();

        int maxRecords = EftpsUtil.getMaxAllowedEnrollmentsPerFile();

        List<Integer> fileIdList = new Vector<Integer>();
        DomainEntitySet<EftpsEnrollment> pendingEnrollments;

        do {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                pendingEnrollments = EftpsEnrollment.getPendingEftpsEnrollments(maxRecords);

                if (pendingEnrollments.isEmpty()) {
                    logger.info("There are no EFTPS Enrollments to be processed.");
                } else {
                    logger.info(String.format("Generating EFTPS Enrollments file for %d pending enrollments.",
                                              pendingEnrollments.size()));

                    try {
                        EnrollmentFile file = new EnrollmentFile();

                        try {
                            if (file.write(pendingEnrollments) > 0) {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Enrollment File processing stats (enrollment file created):").append(EftpsUtil.NEWLINE);
                                sb.append("> File Name                        : ").append(file.getDetailedFileName()).append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Enrollment Count        : ").append(pendingEnrollments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Enrollment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Enrollments (in file) : ").append(file.getSuccessfulEnrollmentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Invalid Enrollments (not in file): ").append(file.getInvalidEnrollmentCount()).append(EftpsUtil.NEWLINE);

                                logger.info(sb.toString());

                                //
                                // Save the file id for later notification email
                                //
                                fileIdList.add(file.getFileControlNumber());
                            } else {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Enrollment File processing stats (enrollment file not created):").append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Enrollment Count        : ").append(pendingEnrollments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Enrollment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Enrollments (in file) : ").append(file.getSuccessfulEnrollmentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Invalid Enrollments (not in file): ").append(file.getInvalidEnrollmentCount()).append(EftpsUtil.NEWLINE);

                                logger.warn(sb.toString());
                            }
                        } catch (Throwable t) {
                            cleanupEdiFile(file);
                            throw t;
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException("Error creating EFTPS Enrollment file. ", t);
                    }
                }

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } while (!(pendingEnrollments.size() < maxRecords));

        //
        // Send an email if any files were created
        //
        sendEmail("EFTPS Enrollment file creation summary", fileIdList);

        logger.info(String.format("End processing EFTPS Enrollments in %s.", timer.getElapsedTimeString()));
    }

    public static void processEnrollmentResponseFile(String pFileName) {
        logger.info(String.format("Begin processing EFTPS Enrollment Response file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            EnrollmentResponseFile responseFile = new EnrollmentResponseFile();

            try {
                responseFile.setFileName(pFileName);
                responseFile.read();
            } catch (Throwable t) {
                cleanupEdiFile(responseFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", responseFile.getDetailedFileName()));
            logger.info(String.format("Processed %d Responses with %d Accepted enrollments and %d Rejected enrollments.",
                                      responseFile.getRecordCount(), responseFile.getAcceptCount(), responseFile.getRejectCount()));
            logger.info(String.format("Created: %s", responseFile.getAckFile().getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EFTPS Enrollment Response file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing EFTPS Enrollment Response file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    public static void processAcknowledgementFile(String pFileName) {
        logger.info(String.format("Begin processing EFTPS Acknowledgement file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            AckFile ackFile = new AckFile();

            try {
                ackFile.setFileName(pFileName);
                ackFile.read();
            } catch (Throwable t) {
                cleanupEdiFile(ackFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", ackFile.getDetailedFileName()));
            logger.info(String.format("Processed %d Acknowledgements with %d Accepted and %d Rejected.",
                                      ackFile.getRecordCount(), ackFile.getAcceptCount(), ackFile.getRejectCount()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EFTPS Acknowledgement file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing EFTPS Acknowledgement file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    public static void ageOutEnrollments() {
        logger.info("Begin EFTPS Enrollment Age Out processing.");

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            int ageOutDays = Integer.parseInt(EftpsUtil.getConfigString("psp_eftps_enrollment_ageout_days"));

            int count = EftpsEnrollment.ageOutEligibleEftpsEnrollments(ageOutDays);

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Aged out %d EFTPS Enrollment(s).", count));
        } catch (Throwable t) {
            throw new RuntimeException("Error in EFTPS Enrollment Age Out processing. ", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End EFTPS Enrollment Age Out processing in %s.", timer.getElapsedTimeString()));
    }

    public static List<Integer> processNextDayPayments() {
        return processPayments(PaymentFile.PaymentFileMode.PFM_NEXT_DAY);
    }

    public static List<Integer> process100kPayments() {
        return processPayments(PaymentFile.PaymentFileMode.PFM_100K);
    }

    public static List<Integer> processSameDayPayments(String pPaymentReferenceNumber, SpcfCalendar pPaymentSettlementDate) {
        return processPayments(PaymentFile.PaymentFileMode.PFM_SAME_DAY, pPaymentReferenceNumber, pPaymentSettlementDate);
    }

    public static List<Integer> processPayments(PaymentFile.PaymentFileMode pPaymentMode) {
        return processPayments(pPaymentMode, null, null);
    }

    public static List<Integer> processPayments(PaymentFile.PaymentFileMode pPaymentMode,
                                       String pBepsReferenceNumber,
                                       SpcfCalendar pBepsSettlementDate) {
        logger.info("Begin processing EFTPS Payments.");

        StopWatch timer = StopWatch.startTimer();

        int maxRecords = EftpsUtil.getMaxAllowedPaymentsPerFile();

        List<Integer> fileIdList = new Vector<Integer>();
        DomainEntitySet<MoneyMovementTransaction> pendingPayments;
        boolean processAbandonedTransactions;

        do {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                pendingPayments = EftpsUtil.getPendingPayments(pPaymentMode, maxRecords);
                processAbandonedTransactions = false;

                if (pendingPayments.isEmpty()) {
                    logger.info("There are no EFTPS Payments to be processed.");
                } else {
                    logger.info(String.format("Generating EFTPS Payments file for %d pending payments.",
                                              pendingPayments.size()));

                    try {
                        PaymentFile file = new PaymentFile(pPaymentMode);

                        file.addWriteRecordListener(new RecordListener() {
                            private int cnt = 0;
                            public void recordCreated(RecordTemplate template) {
                                if ((++cnt % EftpsUtil.getEdi838MaxTransactionsPerSegment()) == 0) {
                                    logger.info("processed record count: " + cnt);
                                }
                            }
                        });

                        if (pBepsReferenceNumber != null) {
                            file.configureForBepsPayment(pBepsReferenceNumber, pBepsSettlementDate);
                        }

                        try {
                            if (file.write(pendingPayments) > 0) {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Payment File processing stats (payment file created):").append(EftpsUtil.NEWLINE);
                                sb.append("> File Name                     : ").append(file.getDetailedFileName()).append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                logger.info(sb.toString());

                                //
                                // Save the file id for later notification email
                                //
                                fileIdList.add(file.getFileControlNumber());
                            } else {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EFTPS Payment File processing stats (payment file not created):").append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                logger.warn(sb.toString());
                            }

                            //
                            // We need to check to make sure all the payments in this result set were processed
                            // (some could have been abandoned due to EFTPS file spec constraints - i.e. max segments reached)
                            // ('skipped' records count as processed records - they were just disqualified from the file.)
                            //
                            // If any payments were abandoned, we want to go around again to pick them up after this batch commits
                            //
                            processAbandonedTransactions = !file.allPaymentsProcessed();
                        } catch (Throwable t) {
                            cleanupEdiFile(file);
                            throw t;
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException("Error creating EFTPS Payment file. ", t);
                    }
                }

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } while (!(pendingPayments.size() < maxRecords) || processAbandonedTransactions);

        //
        // Send an email if any files were created
        //
        sendEmail("EFTPS Payment file creation summary", fileIdList);

        logger.info(String.format("End processing EFTPS Payments in %s.", timer.getElapsedTimeString()));

        return fileIdList;
    }

    /*
        p_user_id           			IN VARCHAR2,  -- For audit purposes
        p_app_server_date   			IN TIMESTAMP, -- UTC Date
        p_payment_file_id	  			IN NUMBER,    -- file control number
        p_offload_date      			IN TIMESTAMP, -- UTC Date
     */
    public static void markPaymentsAsSent(Integer pPaymentFileControlNumber) {
        logger.info("Calling storedProcedure=" + StoredProcedures.PRC_EFTPS_PAYMENTS_SENT.getStoredProcedureName() +
                " currentPrincipal=" + Application.getCurrentPrincipal().getId() + " pPaymentFileControlNumber=" + pPaymentFileControlNumber);
        Application.executeSqlProcedure(StoredProcedures.PRC_EFTPS_PAYMENTS_SENT, false,
                                        Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                        Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                        Pair.of(Integer.class, pPaymentFileControlNumber));
    }


    public static void insertPaymentSentStatusChangeEvent(Integer pPaymentFileControlNumber) {
        logger.info("Calling storedProcedure="+StoredProcedures.PRC_EFTPS_PAYMENTS_SENT_EVENTS.getStoredProcedureName() +
                " currentPrincipal="+Application.getCurrentPrincipal().getId()+" pPaymentFileControlNumber="+pPaymentFileControlNumber);
        Application.executeSqlProcedure(StoredProcedures.PRC_EFTPS_PAYMENTS_SENT_EVENTS, false,
                                        Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                        Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                        Pair.of(Integer.class, pPaymentFileControlNumber),
                                        Pair.of(String.class, "SentToAgency"));
    }

    public static void processPaymentResponseFile(String pFileName) {
        logger.info(String.format("Begin processing EFTPS Payment Response file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            PaymentResponseFile responseFile = new PaymentResponseFile();

            try {
                responseFile.setFileName(pFileName);
                responseFile.read();
                logger.info("Flushing Hibernate cache.");
                Application.getHibernateSession().flush();
                logger.info("Calling storedProcedure="+StoredProcedures.PRC_EFTPS_PAYMENTS_RESPONSE.getStoredProcedureName() +
                        " currentPrincipal=" + Application.getCurrentPrincipal().getId() + " responseFile.getFileControlNumber=" + responseFile.getFileControlNumber() + " responseFilePaymentSubtype=" + responseFile.getPaymentFile().getFileSubtype());
                Application.executeSqlProcedure(StoredProcedures.PRC_EFTPS_PAYMENTS_RESPONSE,
                                                false,
                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                                Pair.of(Integer.class, responseFile.getFileControlNumber()),
                                                Pair.of(Integer.class, responseFile.getPaymentFile().getFileSubtype() == EftpsFileSubtype.PaymentNextDay ? 1 : 0));
            } catch (Throwable t) {
                cleanupEdiFile(responseFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", responseFile.getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EFTPS Payments Response file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing EFTPS Payments Response file %s in %s.", pFileName,
                                  timer.getElapsedTimeString()));
    }

    public static void processPaymentReturnFile(String pFileName) {
        logger.info(String.format("Begin processing EFTPS Payment Return file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            PaymentReturnFile returnFile = new PaymentReturnFile();

            try {
                returnFile.setFileName(pFileName);
                returnFile.read();
                logger.info("Flushing Hibernate cache.");
                Application.getHibernateSession().flush();
                logger.info("Calling storedProcedure="+StoredProcedures.PRC_EFTPS_PAYMENTS_RETURN.getStoredProcedureName() +
                        " currentPrincipal=" + Application.getCurrentPrincipal().getId() + " returnFileControlNumber=" + returnFile.getFileControlNumber());
                Application.executeSqlProcedure(StoredProcedures.PRC_EFTPS_PAYMENTS_RETURN, false,
                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())),
                                                Pair.of(Integer.class, returnFile.getFileControlNumber()));
            } catch (Throwable t) {
                cleanupEdiFile(returnFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            if(returnFile.as400PaymentReturnsFound()) {
                List<Integer> fileIdList = new ArrayList<Integer>();
                fileIdList.add(returnFile.getFileControlNumber());
                sendEmail("EFTPS Payment returns file summary for AS400 payment returns", fileIdList);
            }

            logger.info(String.format("Finished processing %s", returnFile.getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EFTPS Payment Return file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing EFTPS Payment Return file %s in %s.", pFileName,
                                  timer.getElapsedTimeString()));
    }

    public static void processPaymentConfirmationFile(String pFileName) {
        logger.info(String.format("Begin processing EFTPS Payment Confirmation file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            PaymentConfirmationFile confirmationFile = new PaymentConfirmationFile();

            try {
                confirmationFile.setFileName(pFileName);
                confirmationFile.read();
            } catch (Throwable t) {
                cleanupEdiFile(confirmationFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", confirmationFile.getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EFTPS Payment Confirmation file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing EFTPS Payment Confirmation file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    /**
     * Method to process AS400 EFTPS Payments file (only recording payment data for reference)
     */
    public static void processAS400PaymentFile(String pFileName) {
        logger.info(String.format("Begin processing AS400 EFTPS Payments file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        List<Integer> fileIdList = new Vector<Integer>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            PaymentFileAs400Adapter as400PaymentFile = new PaymentFileAs400Adapter();

            try {
                as400PaymentFile.setFileName(pFileName);
                as400PaymentFile.read();

                //
                // Save the file id for later notification email
                //
                fileIdList.add(as400PaymentFile.getFileControlNumber());
            } catch (Throwable t) {
                cleanupEdiFile(as400PaymentFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", as400PaymentFile.getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing AS400 EFTPS Payments file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        //
        // Send an email if any files were created
        //
        sendEmail("EFTPS Payment file creation summary", fileIdList);

        logger.info(String.format("End processing AS400 EFTPS Payments file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    private static void cleanupEdiFile(EdiFile pFile) {
        try {
            if (pFile != null) {
                pFile.cleanup();
            }
        } catch (Throwable t) {
            logger.error("failure to clean-up file - " + pFile.getEdiFileType().name() + ":" + pFile.getFileName(), t);
        }
    }

    public static List<Integer> processEDIPayments(SpcfCalendar pInitiationDate) {
        logger.info("Begin processing EDI Payments.");

        StopWatch timer = StopWatch.startTimer();

        int maxRecords = EftpsUtil.getMaxAllowedPaymentsPerFile();

        List<Integer> fileIdList = new Vector<Integer>();
        DomainEntitySet<MoneyMovementTransaction> pendingPayments;
        boolean processAbandonedTransactions;

        do {
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                pendingPayments = MoneyMovementTransaction.getPendingTaxPaymentsForDate(PaymentMethod.EDI, pInitiationDate, maxRecords);
                processAbandonedTransactions = false;

                if (pendingPayments.isEmpty()) {
                    logger.info("There are no EDI Payments to be processed.");
                } else {
                    logger.info(String.format("Generating EDI Payments file for %d pending payments.",
                                              pendingPayments.size()));

                    try {
                        EdiPaymentFile file = new EdiPaymentFile();

                        file.addWriteRecordListener(new RecordListener() {
                            private int cnt = 0;
                            public void recordCreated(RecordTemplate template) {
                                if ((++cnt % EftpsUtil.getEdi838MaxTransactionsPerSegment()) == 0) {
                                    logger.info("processed record count: " + cnt);
                                }
                            }
                        });

                        try {
                            if (file.write(pendingPayments) > 0) {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EDI Payment File processing stats (payment file created):").append(EftpsUtil.NEWLINE);
                                sb.append("> File Name                     : ").append(file.getDetailedFileName()).append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                logger.info(sb.toString());

                                //
                                // Save the file id for later notification email
                                //
                                fileIdList.add(file.getFileControlNumber());
                            } else {
                                StringBuffer sb = new StringBuffer();

                                sb.append("EDI Payment File processing stats (payment file not created):").append(EftpsUtil.NEWLINE);
                                sb.append("> Selected Payment Count        : ").append(pendingPayments.size()).append(EftpsUtil.NEWLINE);
                                sb.append("> Processed Payment Count       : ").append(file.getRecordCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Successful Payments (in file) : ").append(file.getSuccessfulPaymentCount()).append(EftpsUtil.NEWLINE);
                                sb.append("> Skipped Payments (not in file): ").append(file.getSkippedPaymentCount()).append(EftpsUtil.NEWLINE);

                                logger.warn(sb.toString());
                            }

                            //
                            // We need to check to make sure all the payments in this result set were processed
                            // (some could have been abandoned due to EFTPS file spec constraints - i.e. max segments reached)
                            // ('skipped' records count as processed records - they were just disqualified from the file.)
                            //
                            // If any payments were abandoned, we want to go around again to pick them up after this batch commits
                            //
                            processAbandonedTransactions = !file.allPaymentsProcessed();
                        } catch (Throwable t) {
                            cleanupEdiFile(file);
                            throw t;
                        }
                    } catch (Throwable t) {
                        throw new RuntimeException("Error creating EDI Payment file. ", t);
                    }
                }

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } while (!(pendingPayments.size() < maxRecords) || processAbandonedTransactions);

        // Send an email if any files were created
        sendEmailForEDIPayments("EDI Payment file creation summary", fileIdList);

        logger.info(String.format("End processing EDI Payments in %s.", timer.getElapsedTimeString()));

        return fileIdList;
    }

    public static void markEDIPaymentsAsSent(Integer pPaymentFileControlNumber) {
        DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = Application.find(StateEdiTaxFile.class, StateEdiTaxFile.FileId().equalTo(pPaymentFileControlNumber));
        if(stateEdiTaxFiles.size() != 1) {
            throw new RuntimeException(String.format("EDI file is not found or more than one EDI files are found with File Id %s ", pPaymentFileControlNumber));
        }
        StateEdiTaxFile stateEdiTaxFile = stateEdiTaxFiles.get(0);
        DomainEntitySet<EdiPaymentDetail> ediPaymentDetails = Application.find(EdiPaymentDetail.class, EdiPaymentDetail.ParentFile().equalTo(stateEdiTaxFile)
                .And(EdiPaymentDetail.GroupId().equalTo(pPaymentFileControlNumber)).And(EdiPaymentDetail.MoneyMovementTransaction().TaxPaymentStatus().equalTo(TaxPaymentStatus.SentToAgency))
                .And(EdiPaymentDetail.MoneyMovementTransaction().Status().equalTo(PaymentStatus.Executed)));

        for (EdiPaymentDetail ediPaymentDetail : ediPaymentDetails) {
            for (FinancialTransaction financialTransaction : ediPaymentDetail.getMoneyMovementTransaction().getFinancialTransactionCollection()) {
                financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            }
        }

        stateEdiTaxFile.setStatusCd(EdiFileStatus.PendingTransmission);
        stateEdiTaxFile.setStatusEffectiveDate(PSPDate.getPSPTime());
        stateEdiTaxFile.setModifierId(Application.getCurrentPrincipal().getId());
        stateEdiTaxFile.setModifiedDate(PSPDate.getPSPTime());
        Application.save(stateEdiTaxFile);
    }

     /**
     * Detect files waiting to be sent to AS400 and transmit them.
     */
    public static void transmitAS400StateEDIFiles() {
        logger.info("Begin transmitAS400StateEDIFiles.");

        new SftpStateEdiAS400FileUpload().upload();

        logger.info("End transmitAS400StateEDIFiles.");
    }

    public static void processStateEDIAcknowledgementFile(String pFileName) {
        logger.info(String.format("Begin processing State EDI Acknowledgement file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            StateAckFile ackFile = new StateAckFile();

            try {
                ackFile.setFileName(pFileName);
                ackFile.read();
            } catch (Throwable t) {
                cleanupEdiFile(ackFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", ackFile.getDetailedFileName()));
            logger.info(String.format("Processed %d Acknowledgements with %d Accepted and %d Rejected.",
                                      ackFile.getRecordCount(), ackFile.getAcceptCount(), ackFile.getRejectCount()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing State EDI Acknowledgement file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info(String.format("End processing State EDI Acknowledgement file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    /**
     * Detect files arriving from VAN and translate the files by adding segment separators.
     */
    public static void preProcessWaitingEDIResponseFiles() {
        logger.info("Begin preProcessWaitingEDIResponseFiles.");

        String sourceDirectory = EftpsUtil.getEdiVanDir();
        String archiveDirectory = EftpsUtil.getEdiArchiveDir();
        String as400Directory = EftpsUtil.getEdiAS400Dir();
        
        List<File> fileList = EftpsUtil.getFilesFromDir(sourceDirectory);

        if (fileList.isEmpty()) {
            logger.info(String.format("No files to process in specified directory %s", sourceDirectory));
            return;
        }

        for (File file : fileList) {

            //Copy the original file to AS400 directory to send to AS400 if it is As400 file
            EftpsUtil.copyFile(file, as400Directory);

            String originalFileName = file.getName();
            File backupFile = new File(sourceDirectory, originalFileName+"_original");
            file.renameTo(backupFile);

            //Copy the original file to Archive directory to keep original file copy, with "_original" appended to file name          
            EftpsUtil.copyFile(backupFile, archiveDirectory);

            StateEDIFileReader stateEDIFileReader = new StateEDIFileReader(backupFile);
            File newFile = new File(sourceDirectory, originalFileName);
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(newFile);
                for (EDIRecordTemplate ediRecordTemplate : stateEDIFileReader.getRecordList()) {
                    fileWriter.write(ediRecordTemplate.toString()+"\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error pre-processing State EDI response file %s ", originalFileName), e);
            } finally {
                try {
                    if(fileWriter != null){
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    logger.error(String.format("Error pre-processing State EDI file %s", originalFileName), e);
                }
            }
            backupFile.delete(); // Delete this file as original version is already archived
        }


        logger.info("End preProcessWaitingEDIResponseFiles.");
    }    

    /**
     * Detect files arriving from VAN and process them.
     */
    public static void processWaitingEDIResponseFiles() {
        logger.info("Begin processWaitingResponseFiles.");

        processFilesInDir(EftpsUtil.getEdiVanDir(), false, EftpsUtil.getEdiWorkDir(), EftpsUtil.getEdiErrDir());

        logger.info("End processWaitingResponseFiles.");
    }

    public static void processStateEDIPaymentResponseFile(String pFileName) {
        logger.info(String.format("Begin processing EDI Payment Response file %s", pFileName));

        StopWatch timer = StopWatch.startTimer();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            EdiPaymentResponseFile responseFile = new EdiPaymentResponseFile();

            try {
                responseFile.setFileName(pFileName);
                responseFile.read();
            } catch (Throwable t) {
                cleanupEdiFile(responseFile);
                throw t;
            }

            PayrollServices.commitUnitOfWork();

            logger.info(String.format("Finished processing %s", responseFile.getDetailedFileName()));
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error processing EDI Payments Response file %s ", pFileName), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        logger.info(String.format("End processing EDI Payments Response file %s in %s.", pFileName, timer.getElapsedTimeString()));
    }

    public static void insertPaymentSentStatusChangeEventForEDIPayments(Integer pPaymentFileControlNumber) {
        DomainEntitySet<EdiPaymentDetail> paymentDetails = Application.find(EdiPaymentDetail.class, EdiPaymentDetail.BaseParentFile().FileId().equalTo(pPaymentFileControlNumber));

        for (EdiPaymentDetail paymentDetail : paymentDetails) {
            CompanyEvent.createTaxPaymentStatusChangeEvent(paymentDetail, TaxPaymentStatus.ReadyToSend); // Payments send process can only send ReadyToSend payments
        }
    }

    /**
     * Detect files waiting to be sent to VAN and send them.
     */
    public static void processPendingStateEdiTransmissions() {
        logger.info("Begin processPendingStateEdiTransmissions.");

        // Upload PendingTransmission files
        ScpEdiVanFileUpload scpFileUpload = new ScpEdiVanFileUpload();
        scpFileUpload.upload();

        // Send an email if any files were uploaded
        sendEmailForEDIPayments("EDI Payment file Send summary", scpFileUpload.getUploadedFileIdList());

        logger.info("End processPendingStateEdiTransmissions.");
    }

    public static void archiveEDIFiles() throws S3ConnectionException,S3UploadException{
        logger.info("Begin archiveEDIFiles.");

        try {
            String archiveDir = EftpsUtil.getEdiArchiveDir();

            Application.beginUnitOfWork();

            DomainEntitySet<StateEdiTaxFile> stateEdiTaxFiles = StateEdiTaxFile.getCompletedStateEdiFiles();

            for (StateEdiTaxFile stateEdiTaxFile : stateEdiTaxFiles) {
                String fileName = S3UploadUtils.archive(BatchJobType.EftpsSend.name(),archiveDir,stateEdiTaxFile.getFileName());
                        EftpsUtil.moveFile(stateEdiTaxFile.getFileName(), archiveDir);

                stateEdiTaxFile.setStatusCd(EdiFileStatus.Archived);
                stateEdiTaxFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                stateEdiTaxFile.setFileName(fileName);

                Application.save(stateEdiTaxFile);
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        logger.info("End archiveEDIFiles.");
    }

}
