package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.amazonaws.regions.Regions;
import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchListener;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.common.utils.mockutil.MockS3FileStore;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.psp.common.gateway.JSSGateway;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.sbg.shared.filestore.FileStoreFactory;
import com.intuit.sbg.shared.filestore.FileStoreType;
import com.intuit.sbg.shared.filestore.enums.EncryptionType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.jscape.inet.ftp.FtpListener;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.jss.processors.WorkersCompProcessor.UploadFilesToSplitLimit.*;


/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 15, 2009
 * Time: 8:07:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchUtils {
    private static final SpcfLogger logger = Application.getLogger(BatchUtils.class);

    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String FILESEP = System.getProperty("file.separator");
    public static String PSP_BATCHJOBS_S3_BUCKET_REGION = "psp_batch_s3_bucket_region";
    public static final String CC = "20";
    public static final String YY = "[0-9]{2}";
    public static final String MM = "((0[1-9])|(1[0-2]))";
    public static final String DD = "((0[1-9])|([12][0-9])|(3[01]))";
    public static final String VALIDYYYYMM = CC+YY+MM;
    public static final String VALIDYYYYMMDD = CC+YY+MM+DD;
    public static final String VALIDGUID = "^\\{?[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}\\}?$";
    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATE_TIME_FORMAT = "yyyyMMdd HH:mm:ss";
    public static final String NM_PMT_TEMPLATE = "NM-CRS1-PAYMENT";
    public static final String NM_FILENAME = "NM-41414-PAYMENT";
    public static final String MO_PMT_TEMPLATE = "MO-941-PAYMENT";
    public static final String MO_PMT_FREQUENCY = "QUARTERMONTHLY";
    public static final String MOQM_FILENAME = "MO-941M_Assisted_QM";
    public static String SCHEDULE_DATE_FORMAT="yyyy-MM-dd'T'HH:MM:ss";
    public static String pMessage="RTB Backup data Cleanup Communication " +
            "\n" +
            "This message is in reference to the following company:  CompanyId " +
            "\n" +
            "\n" +
            "Dear PayrollAdminFirstName PayrollAdminLastName, " +
            "\n" + "\n" +
            "The data modified by you on ModifiedDate is going to be deleted on DeletionDate. " +
            "\n" +
            "Unique identifier for ur record is: GetUniqueId \n" +
            "\n" +
            "If you want this data to persist please log a RTB Service Ticket immediately. " +
            "\n" +"\n" +
            "Sincerely, " +
            "\n" +
            "RTB Service Group " +"\n" +
            "Intuit Payroll Services";

    public static String deletionMsg="Dear PayrollAdminFirstName PayrollAdminLastName , " +"\n" +"\n" +
            "The data for EventType for PSID:CompanyId modified by you on ModifiedDate is deleted on CurrentDate "
            +"\n" +"\n" +
            "Regards, " +"\n" +
            "Team Intuit";

    public static String getConfigString(String pKey) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, pKey);
    }

    public static String getConfigString(String pKey, String pDefaultValue) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, pKey, pDefaultValue);
    }

    /**
     * Method to retrieve properties from the TaxAgency config file
     *
     * @param pKey
     * @return
     */
    public static String getTaxAgencyConfigString(String pKey) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, pKey);
    }

    public static String getTaxAgencyConfigString(String pKey, String pDefaultValue) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, pKey, pDefaultValue);
    }

    public static void delay(String pDelayProperty, String pDefaultDelay) {
        delay(Long.parseLong(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, pDelayProperty, pDefaultDelay)) * 60000);
    }

    public static void eftpsDelay(String pDelayProperty, String pDefaultDelay) {
        delay(Long.parseLong(ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, pDelayProperty, pDefaultDelay)) * 60000);
    }

    public static void delay(long pDelayInMillis) {
        try {
            Thread.sleep(pDelayInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error in thread delay ", e);
        }
    }

    public static boolean isValidGuid(String pGuid) {
        return (pGuid != null) && pGuid.matches(VALIDGUID);
    }

    public static String formatGuid(String pGuid) {
        String guid = null;

        if (isValidGuid(pGuid)) {
            guid = pGuid.replaceAll("[-{}]", "").toLowerCase();
            guid = String.format("%s-%s-%s-%s-%s",
                                 guid.substring(0, 8),
                                 guid.substring(8, 12),
                                 guid.substring(12, 16),
                                 guid.substring(16, 20),
                                 guid.substring(20));
        }

        return guid;
    }

    public static void beginUnitOfWorkManualFlushMode() {
        Application.beginUnitOfWork();
        setManualFlushMode();
    }

    public static void commitUnitOfWorkManualFlushMode() {
        setAutoFlushMode();
        Application.commitUnitOfWork();
    }

    public static void rollbackUnitOfWorkManualFlushMode() {
        Application.setDefaultHibernateFlushMode(null);
        Application.rollbackUnitOfWork();
    }

    public static void setManualFlushMode() {
        Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);
        Application.setDefaultHibernateFlushMode(FlushMode.MANUAL);
    }

    public static void setAutoFlushMode() {
        Application.setDefaultHibernateFlushMode(null);
        Application.getHibernateSession().setFlushMode(FlushMode.AUTO);
    }

    public static File moveFile(String pSourceFile, String pDestDir) {
        File destFile = copyFile(pSourceFile, pDestDir);
        new File(pSourceFile).delete(); // delete the source file
        return destFile;
    }

    public static File copyFile(String pSourceFile, String pDestDir) {
        try {
            File sourceFile = new File(pSourceFile);
            File destFile = new File(pDestDir, sourceFile.getName());
            FileInputStream iStreamFile = new FileInputStream(sourceFile);
            InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
            BufferedReader reader = new BufferedReader(iStreamReader);
            FileOutputStream oStreamFile = new FileOutputStream(destFile);
            OutputStreamWriter writer = new OutputStreamWriter(oStreamFile);

            try {
                while (reader.ready()) {
                    writer.write(reader.readLine() + NEWLINE);
                }
            } finally {
                writer.flush();
                writer.close();
                reader.close();
            }

            return destFile;
        } catch (Exception e) {
            throw new RuntimeException("Error copying file (source: " + pSourceFile + ", dest: " + pDestDir + ")", e);
        }
    }

    public static boolean isWeekendOrHoliday() {
        return isWeekendOrHoliday(null); // check if current PSPDate is a holiday
    }

    public static boolean isWeekendOrHoliday(SpcfCalendar pDate) {
        boolean holiday = false;
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (manageTransaction) {
                PayrollServices.beginUnitOfWork();
            }

            holiday = CalendarUtils.isWeekendOrHoliday((pDate != null) ? pDate : PSPDate.getPSPTime());
        } finally {
            if (manageTransaction) {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        return holiday;
    }

    public static DomainEntitySet<NACHAFile> getNachaFilesByStatus(NACHAFileStatus... pStatus) {
        Expression<NACHAFile> query = new Query<NACHAFile>().Where(NACHAFile.Status().in(pStatus)).OrderBy(NACHAFile.CreatedDate());

        return Application.find(NACHAFile.class, query);
    }

    public static DomainEntitySet<DICRFile> getDicrFilesByStatus(DICRFileStatus... pStatus) {
        Expression<DICRFile> query = new Query<DICRFile>().Where(DICRFile.Status().in(pStatus)).OrderBy(DICRFile.CreatedDate());

        return Application.find(DICRFile.class, query);
    }

    public static DICRFile getDicrFileForNachaFile(NACHAFile pNachaFile) {
        Expression<DICRFile> query = new Query<DICRFile>().Where(DICRFile.NACHAFile().equalTo(pNachaFile));
        DomainEntitySet<DICRFile> dicrFileSet = Application.find(DICRFile.class, query);
        return dicrFileSet.isEmpty() ? null : dicrFileSet.get(0);
    }

    public static DomainEntitySet<ThirdParty401kSignUpBatch> getThirdParty401kSignUpBatchByStatus(ThirdParty401kBatchStatusCode... pStatus) {
        Expression<ThirdParty401kSignUpBatch> query = new Query<ThirdParty401kSignUpBatch>()
                        .Where(ThirdParty401kSignUpBatch.DownloadStatusCd().in(pStatus))
                        .OrderBy(ThirdParty401kSignUpBatch.FileName());

        return Application.find(ThirdParty401kSignUpBatch.class, query);
    }

    public static DomainEntitySet<ThirdParty401kBatch> getTP401kUploadFilesByStatus(ThirdParty401kBatchStatusCode... pStatus) {
        Expression<ThirdParty401kBatch> query = new Query<ThirdParty401kBatch>()
                       .Where(ThirdParty401kBatch.UploadStatusCd().in(pStatus))
                       .OrderBy(ThirdParty401kBatch.CreatedDate());

        return Application.find(ThirdParty401kBatch.class, query);
    }

    public static DomainEntitySet<GemsUploadBatch> getGemsUploadFilesByStatus(ReportingFrequency pReportingFrequency, GemsUploadBatchStatus... pStatus) {
        Expression<GemsUploadBatch> query = new Query<GemsUploadBatch>()
                .Where(GemsUploadBatch.UploadStatus().in(pStatus)
                        .And(GemsUploadBatch.BatchType().equalTo(pReportingFrequency)))
                .OrderBy(GemsUploadBatch.CreatedDate());

        return Application.find(GemsUploadBatch.class, query);
    }
    public static File encryptFileUsingIDPS(File file){
        String fileWithoutExt = StreamUtil.FileWithoutExt(file.getAbsolutePath());
        String fileName = fileWithoutExt + ".idps";
        String fileExt =  FilenameUtils.getExtension(file.getName());
        File encFile = new File(fileName);
        try{
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            InputStream inpStream = new FileInputStream(file);
            int ret = StreamUtil.copyEncryptedBytes(inpStream,encFile,key);
            if(ret < 0){
                logger.warn("No file was written in encryption");
            }
            else{
                if(file.delete())//delete unencrypted file
                {
                    encFile = BatchUtils.renameFileExt(encFile,fileExt);
                }
                else{
                    logger.error("Error in deleting file" + file.getName());
                    try {
                        org.apache.commons.io.FileUtils.forceDelete(file);
                        encFile = BatchUtils.renameFileExt(encFile,fileExt);
                    }catch(Exception e){
                        logger.error("Failed to delete file" + file.getName());
                    }
                }

            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return encFile;
    }
    public static File encryptFileInStreamsUsingIDPS(File file){
        String fileWithoutExt = StreamUtil.FileWithoutExt(file.getAbsolutePath());
        String fileName = fileWithoutExt + ".idps";
        String fileExt =  FilenameUtils.getExtension(file.getName());
        File encFile = new File(fileName);
        try{
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();

            int totalBytes = StreamUtil.streamEncryptFileSingleThread(key,file,encFile);
            if(totalBytes < 0){
                logger.warn("No file was written in encryption");
            }
            else{
                if(file.delete())//delete unencrypted file
                {
                    encFile = BatchUtils.renameFileExt(encFile,fileExt);
                }
                else{
                    logger.error("Error in deleting file" + file.getName());
                    try {
                        org.apache.commons.io.FileUtils.forceDelete(file);
                        encFile = BatchUtils.renameFileExt(encFile,fileExt);
                    }catch(Exception e){
                        logger.error("Failed to delete file" + file.getName());
                    }

                }

            }

        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return encFile;
    }
    public static File renameFileExt(File src, String ext){
        String fileWithoutExt = StreamUtil.FileWithoutExt(src.getAbsolutePath());
        String fileName = fileWithoutExt + "." + ext;
        File target = new File(fileName);
        src.renameTo(target);
        return target;

    }
    public static void createAchReturnAccountingFileAndEmail(SpcfUniqueId pReturnBatchId) throws S3UploadException,S3ConnectionException{
        logger.info(String.format("Begin createAchReturnAccountingFileAndEmail for batch id: %s", pReturnBatchId.toString()));

        String recipient = getConfigString("psp_batch_nsf_email", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for NSF Returns file, skipping task.");
        } else {
            AchReturnAccountingFile file = new AchReturnAccountingFile(pReturnBatchId);

            if (file.createFiles()) {
                String subject = String.format("PSP ACH Returns file(s) for date: %s", file.getReturnDate().format("MM/dd/yyyy"));
                StringBuffer message = new StringBuffer();

                ArrayList<String> filesToBeAttached = new ArrayList<String>();
                ArrayList<String> fileNamesToBeAttached = new ArrayList<String>();
                if (file.getFile() != null && file.getFile().exists()) {
                    filesToBeAttached.add(file.getFile().getAbsolutePath());
                    fileNamesToBeAttached.add(file.getFile().getName());
                }
                message.append("The attached ACH Reject file for Assisted (Tax) companies is ready for processing:");
                message.append("\r\n\r\n");
                for (String fileToAttach : fileNamesToBeAttached) {
                    message.append(fileToAttach).append("\r\n");
                }
                message.append("\r\n");
                message.append("<EOM>");

                logger.info(message.toString());

                MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                     recipient,                 // to
                                     recipient,                 // from
                                     subject,                   // subject
                                     message.toString(),        // message body
                                     filesToBeAttached);        // attachments

                // s3 upload part. did not put the EFS LD flag here since the file itself is getting created directly on archive dir.
                String archiveDir = getConfigString("psp_batch_ftp_arcv_dir");
                String batchJobName = BatchJobType.NightlyBatchJobs.name();

                File unEncFile = new File(file.getFile().getAbsolutePath());
                File ach_file = encryptFileInStreamsUsingIDPS(unEncFile);

                S3UploadUtils.archive(batchJobName,archiveDir,ach_file.getAbsolutePath());
            }
        }

        logger.info(String.format("End createAchReturnAccountingFileAndEmail for batch id: %s", pReturnBatchId.toString()));
    }

    public static void createAchReturnFailedTransactionReportEmail(SpcfUniqueId pReturnBatchId, List<TransactionReturn> failedNOCReturns, List<TransactionReturn> failedRejectReturns) {
        logger.info(String.format("Begin createAchReturnFailedTransactionReportEmail for batch id: %s", pReturnBatchId.toString()));
        String psp_ach_action_alert = getConfigString("psp_ach_action_alert", "");
        String psp_offload_notify_list = getConfigString("psp_offload_notify_list", "");
        String from = StringUtils.isEmpty(psp_ach_action_alert)?psp_offload_notify_list:psp_ach_action_alert;
        String recipient = psp_offload_notify_list + "," + psp_ach_action_alert;
        // ToDo - remove the statement below and add correct mailing list

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for failed transaction report, skipping task.");
        } else {
            boolean manageTransaction = !Application.hasActiveTransaction();
            try {

                if (manageTransaction) {
                    PayrollServices.beginUnitOfWork();
                }
                String subject = String.format("PSP ACH Return Transaction Failure Report For Batch Id - %s", pReturnBatchId.toString());
                StringBuffer message = new StringBuffer();

                message.append("PSP ACH Return Transaction Failure Report:\r\n\r\n");
                message.append("Total return transaction failures - " + ((failedNOCReturns != null ? failedNOCReturns.size() : 0) + (failedRejectReturns != null ? failedRejectReturns.size() : 0)));
                message.append("\r\n");
                message.append("Number of NOC return failures - " + (failedNOCReturns != null ? failedNOCReturns.size() : 0) + "\r\n");
                message.append("Number of reject return failures - " + (failedRejectReturns != null ? failedRejectReturns.size() : 0) + "\r\n\r\n");

                message.append("Details:\r\n");
                logger.info(message.toString());
                StringBuffer logMessage = new StringBuffer();
                //Limiting the number of records sending in the mail to 500. But all records will be logged.
                int recordCount = 0;
                if(failedNOCReturns.size() > 0){
                    message.append("NOC Returns:\r\n");
                    for (TransactionReturn failedNOCReturn : failedNOCReturns) {
                        failedNOCReturn = Application.refresh(failedNOCReturn);
                        if(recordCount < 500)
                            message.append("Id: " + failedNOCReturn.getId() + "   " + "Company: " + failedNOCReturn.getCompany() + "   " + failedNOCReturn.toString() + "\r\n");
                        logMessage.append("Id: " + failedNOCReturn.getId() + "   " + "Company: " + failedNOCReturn.getCompany() + "   " + failedNOCReturn.toString() + "\r\n");
                        recordCount++;
                    }
                }
                if(failedRejectReturns.size() > 0){
                    recordCount = 0;
                    message.append("Reject Returns:\r\n");
                    for (TransactionReturn failedRejectReturn : failedRejectReturns) {
                        failedRejectReturn = Application.refresh(failedRejectReturn);
                        if(recordCount < 500)
                            message.append("Id: " + failedRejectReturn.getId() + "   " + "Company: " + failedRejectReturn.getCompany() + "   " + failedRejectReturn.toString() + "\r\n");
                        logMessage.append("Id: " + failedRejectReturn.getId() + "   " + "Company: " + failedRejectReturn.getCompany() + "   " + failedRejectReturn.toString() + "\r\n");
                        recordCount++;
                    }
                }
                message.append("\r\n");
                message.append("<EOM>");
                logger.info(logMessage.toString());



                MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                     recipient,                 // to
                                     from,                 // from
                                     subject,                   // subject
                                     message.toString());        // message body
            } finally {
                if (manageTransaction) {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }

        logger.info(String.format("End createAchReturnFailedTransactionReportEmail for batch id: %s", pReturnBatchId.toString()));
    }

    /**
     * Creates an E-mail with the state report as an attachment
     * @param builder The builder object containing the report
     * @param totalProcessed The total number of entries for companies in the report
     * @param startDate The start date for the report
     * @param endDate The end date for the report
     * @param stateReportType The type of report that was run
     * @param paymentTemplateFrequency The list of PaymentTemplateFrequencies that are in the report
     */
    public static void createStateReportEmail(StringBuilder builder, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, PaymentTemplateFrequency... paymentTemplateFrequency) {
        createStateReportEmail(builder, ".txt", totalProcessed, startDate, endDate, stateReportType, null, null, null, paymentTemplateFrequency);
    }

    public static void createStateReportEmail(StringBuilder builder, String filenameExtension, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, PaymentTemplateFrequency... paymentTemplateFrequency) {
        createStateReportEmail(builder, filenameExtension, totalProcessed, startDate, endDate, stateReportType, null, null, null, paymentTemplateFrequency);
    }

    public static void createStateReportEmail(StringBuilder builder, String filenameExtension, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, String filenameOverride, PaymentTemplateFrequency... paymentTemplateFrequency) {
        createStateReportEmail(builder, filenameExtension, totalProcessed, startDate, endDate, stateReportType, null, null, filenameOverride, paymentTemplateFrequency);
    }

    public static void createStateReportEmail(StringBuilder builder, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, Map<String, DepositFrequencyCode> pPaymentTemplatesWithDepositFreq, PaymentTemplateFrequency... paymentTemplateFrequency) {
        createStateReportEmail(builder, ".txt", totalProcessed, startDate, endDate, stateReportType, null, pPaymentTemplatesWithDepositFreq, null, paymentTemplateFrequency);
    }

    public static void createStateReportEmail(StringBuilder builder, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, String[] pPaymentTemplates, PaymentTemplateFrequency... paymentTemplateFrequency) {
        createStateReportEmail(builder, ".txt", totalProcessed, startDate, endDate, stateReportType, pPaymentTemplates, null, null, paymentTemplateFrequency);
    }

    public static void createStateReportEmail(StringBuilder builder, String filenameExtension, int totalProcessed, SpcfCalendar startDate, SpcfCalendar endDate,
                                              StateReportType stateReportType, String[] pPaymentTemplates, Map<String, DepositFrequencyCode> pPaymentTemplatesWithDepositFreq, String filenameOverride, PaymentTemplateFrequency... paymentTemplateFrequency) {
        logger.info(String.format("Begin createStateReportEmail: %s", stateReportType.toString()));

        String recipient = getConfigString("psp_batch_state_report_email", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for State Reports file, skipping task.");
        } else {
            String paymentName = null;
            String paymentFrequencyName = null;
            if(paymentTemplateFrequency.length > 0){
                paymentName = paymentTemplateFrequency[0].getPaymentTemplate().getPaymentTemplateAbbrev();
                paymentFrequencyName = paymentTemplateFrequency[0].getPaymentFrequencyId().toString();
            }
            List<String> paymentNames = new ArrayList<String>();
            List<String> paymentFrequencyNames = new ArrayList<String>();
            if(pPaymentTemplatesWithDepositFreq != null) {
                for (String paymentTemplate : pPaymentTemplatesWithDepositFreq.keySet()) {
                    paymentNames.add(paymentTemplate);
                    paymentFrequencyNames.add(pPaymentTemplatesWithDepositFreq.get(paymentTemplate).toString());
                }
            }

            String subject;

            if (stateReportType.equals(StateReportType.Recon)) {
                // ToDo : Add a method here to set the subect line
                if((NM_PMT_TEMPLATE).equals(paymentName)){
                    subject = String.format("State WH Recon For %s %s", NM_FILENAME, paymentFrequencyName);
                }else {
                    subject = String.format("State WH Recon For %s %s", paymentName, paymentFrequencyName);
                }
            } else if (stateReportType.equals(StateReportType.Coupon) || stateReportType.equals(StateReportType.ZeroCoupon)) {
                if(pPaymentTemplates != null) {
                    subject = String.format("State %s Report For: %s", stateReportType.toString(), pPaymentTemplates[0]);
                } else if(pPaymentTemplatesWithDepositFreq != null) {
                    subject = String.format("State %s Report For: ", stateReportType.toString());
                    for (int i = 0; i < paymentNames.size(); i++) {
                        subject += paymentNames.get(i) + "-" +paymentFrequencyNames.get(i) +", ";
                    }
                    subject = subject.substring(0, subject.length()-2);
                } else if((MO_PMT_TEMPLATE).equals(paymentName) && (MO_PMT_FREQUENCY).equals(paymentFrequencyName)){
                    subject = String.format("State Coupon Report For %s %s", MO_PMT_TEMPLATE, paymentFrequencyName);
                } else {
                    subject = String.format("State %s Report", stateReportType.toString());
                }
            } else {
                logger.error("StateReportType not found.  StateReportType is " + stateReportType.toString());
                subject = "Unknown";
            }

            StringBuilder message = new StringBuilder();

            ArrayList<String> filesToBeAttached = new ArrayList<String>();
            ArrayList<File> fileObjectsAttached = new ArrayList<File>();

            // Only attach file if there are entries
            if (totalProcessed != 0) {
                try {
                    String tempDir = getConfigString("psp_batch_temp", "");

                    File tempStateReportFile;

                    if (stateReportType.equals(StateReportType.Recon)) {

                        String filename = "";
                        // ToDo : Add a method here to set the filename
                        if(stateReportType.equals(StateReportType.Recon) && (NM_PMT_TEMPLATE).equals(paymentName)){
                            filename = NM_FILENAME + "_" + paymentFrequencyName + "_" +
                                    endDate.format("yyyyMMdd") + filenameExtension;
                        }else{
                            filename = paymentName + "_" + paymentFrequencyName + "_" +
                                    endDate.format("yyyyMMdd") + filenameExtension;
                        }

                        if(StringUtils.isNotEmpty(filenameOverride)){
                            filename = filenameOverride;
                        }
                        tempStateReportFile = new File(tempDir, filename);
                    } else if (stateReportType.equals(StateReportType.Coupon) || stateReportType.equals(StateReportType.ZeroCoupon)) {
                        String fileName = "";
                        if(pPaymentTemplates != null) {
                            fileName += stateReportType.toString() + "_" + pPaymentTemplates[0] + "_";
                        } else if(pPaymentTemplatesWithDepositFreq != null) {
                            fileName += stateReportType.toString() + "_";
                            for (PaymentTemplateFrequency aPaymentTemplateFrequency : paymentTemplateFrequency) {
                                fileName += aPaymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev() + "-" + aPaymentTemplateFrequency.getPaymentFrequencyId().toString() + "_";
                            }
                        } else if((MO_PMT_TEMPLATE).equals(paymentName)  && (MO_PMT_FREQUENCY).equals(paymentFrequencyName)){
                            fileName = MOQM_FILENAME + "_";
                        } else {
                            fileName += "All_" + stateReportType.toString() + "_";
                        }
                        tempStateReportFile = new File(tempDir, fileName  + endDate.format("yyyyMMdd") + filenameExtension);
                    } else {
                        tempStateReportFile = new File(tempDir, "unknown" + endDate.format("yyyyMMdd") + filenameExtension);
                    }

                    if (!tempStateReportFile.getParentFile().exists()) {
                        boolean created = tempStateReportFile.getParentFile().mkdirs();

                        if (!created) {
                            logger.error("Unable to create directory for temp State Report files.");
                            return;
                        }
                    }

                    // Write out report to file so it can be attached
                    FileWriter writer = new FileWriter(tempStateReportFile);
                    BufferedWriter bufferedWriter = new BufferedWriter(writer);
                    bufferedWriter.append(builder);
                    bufferedWriter.close();
                    writer.close();

                    filesToBeAttached.add(tempStateReportFile.getAbsolutePath());
                    fileObjectsAttached.add(tempStateReportFile);
                } catch (Exception e) {
                    logger.error("Unable to create state report file for " + paymentName + " for frequency " + paymentTemplateFrequency[0]);
                }
            }

            // If no entries found, add that to E-mail
            if (totalProcessed == 0) {
                message.append("NO MATCHING DATA FOUND TO REPORT!\r\n\r\n");
            }

            if (stateReportType.equals(StateReportType.Recon)) {
                message.append("Created State Report for ").append(paymentName).append(" for start date ")
                        .append(startDate.format("yyyy/MM/dd")).append(" and end date ").append(endDate.format("yyyy/MM/dd"));
            } else if (stateReportType.equals(StateReportType.Coupon) || stateReportType.equals(StateReportType.ZeroCoupon)) {
                message.append("Created State ").append(stateReportType).append(" Report for the date ")
                        .append(startDate.format("yyyy/MM/dd"));
            }

            message.append(" for the frequencies: ");

            if (paymentTemplateFrequency.length == 0) {
                message.append("NONE");
            }

            for (int i = 0; i < paymentTemplateFrequency.length; i++) {
                message.append(paymentTemplateFrequency[i].getPaymentTemplate().getPaymentTemplateAbbrev())
                        .append(" ").append(paymentTemplateFrequency[i].getPaymentFrequencyId().toString());

                if (i + 1 != paymentTemplateFrequency.length) {
                    message.append(", ");
                }
            }

            message.append(".");
            message.append("\r\n\r\n");
            for (File fileToAttach : fileObjectsAttached) {
                message.append(fileToAttach.getName()).append("\r\n");
            }
            message.append("\r\n");
            message.append("<EOM>");

            logger.info(message.toString());

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                 recipient,                 // to
                                 recipient,                 // from
                                 subject,                   // subject
                                 message.toString(),        // message body
                                 filesToBeAttached);        // attachments

            if (totalProcessed != 0) {
                // Delete temp files now that E-mail is created
                for (File fileToAttach : fileObjectsAttached) {
                    if (!fileToAttach.delete()) {
                        logger.warn("Could not delete temp report file " + fileToAttach + ".  This file needs to be manually deleted.");
                    }
                }
            }
        }

        logger.info(String.format("End createStateReportEmail: %s", stateReportType.toString()));
    }

    /**
     * Creates an E-mail with the Tax payment returns report as an attachment
     * @param message The list of financialTransactions that needs to be included in report
     */
    public static void sendStateTaxPaymentReturnNotification(StringBuilder message) {
        logger.info("Begin sendStateTaxPaymentReturnNotification.");
        logger.info(message);
        String recipient = getConfigString("psp_batch_state_report_email", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for state tax payment returns notifications, skipping task.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy h:mm a, z");
            String timestamp = sdf.format(CalendarUtils.convertToDate(PSPDate.getPSPTime()));
            String subject = String.format("Tax payment return Notification (%s)", timestamp);

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                 recipient,                 // to
                                 recipient,                 // from
                                 subject,                   // subject
                                 message.toString());        // message body

        }
        logger.info("End sendStateTaxPaymentReturnNotification.");
    }

    public static void sendTaxPaymentNotificationEmail(String pSubLine, String pMessage) {
        logger.info("Begin sendTaxPaymentNotificationEmail.");
        logger.info(pMessage);

        String recipient = getTaxAgencyConfigString("psp_agency_tax_payment_notify_list", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for tax payment notifications, skipping task.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy h:mm a, z");
            String timestamp = sdf.format(CalendarUtils.convertToDate(PSPDate.getPSPTime()));
            String subject = String.format("%s (%s)", pSubLine, timestamp);

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                 recipient, // to
                                 recipient, // from
                                 subject,   // subject
                                 pMessage); // message body
        }

        logger.info("End sendTaxPaymentNotificationEmail.");
    }

    public static void sendFsetRejectionsNotificationEmail(String pSubLine, String pMessage) {
        logger.info("Begin sendFsetRejectionsNotificationEmail.");
        logger.info(pMessage);

        String recipient = getTaxAgencyConfigString("psp_fset_notify_list", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for fset rejections notification, skipping task.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy h:mm a, z");
            String timestamp = sdf.format(CalendarUtils.convertToDate(PSPDate.getPSPTime()));
            String subject = String.format("%s (%s)", pSubLine, timestamp);

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                 recipient, // to
                                 recipient, // from
                                 subject,   // subject
                                 pMessage); // message body
        }

        logger.info("End sendFsetRejectionsNotificationEmail.");
    }

    public static SimpleFtpFile getFtpConnection(String pHost, String pUsername, String pPassword, FtpListener pListener) {
        return getFtpConnection(pHost,
                                pUsername,
                                pPassword,
                                pListener,
                                Boolean.parseBoolean(BatchUtils.getConfigString("psp_batch_ftp_debug")));

    }

    public static SimpleFtpFile getFtpConnection(String pHost, String pUsername, String pPassword,
                                                 FtpListener pListener, boolean pDebugMode) {
        SimpleFtpFile ftp = new SimpleFtpFile(pHost, pUsername, pPassword);

        initialize(ftp, pListener, pDebugMode);

        return ftp;
    }

    public static SimpleFtpsFile getFtpsConnection(String pHost, String pUsername,
                                                   String pPassword, FtpListener pListener) {
        return getFtpsConnection(pHost,
                                 pUsername,
                                 pPassword,
                                 pListener,
                                 Boolean.parseBoolean(BatchUtils.getConfigString("psp_batch_ftp_debug")));

    }

    public static SimpleFtpsFile getFtpsConnection(String pHost, String pUsername, String pPassword,
                                                   FtpListener pListener, boolean pDebugMode) {
        SimpleFtpsFile ftps = new SimpleFtpsFile(pHost, pUsername, pPassword);

        initialize(ftps, pListener, pDebugMode);

        return ftps;
    }

    /**
     * @return JSCH obj for SFTP
     */
    public static Transporter getJschConnection(String pHost, String pUsername, String password,
                                                JSchListener pListener, boolean pUseProxy) {
        return getJschConnection(pHost, pUsername, password, false, pListener, pUseProxy);
    }

    public static Transporter getJschConnection(String pHost, String pUsername, File pPrivateKeyFile,
                                             JSchListener pListener, boolean pUseProxy) {
        return getJschConnection(pHost, pUsername, pPrivateKeyFile.getAbsolutePath(), true, pListener, pUseProxy);
    }

    public static Transporter getJschConnection(String pHost, String pUsername, String passKey, boolean useAuthKey,
                                                JSchListener pListener, boolean pUseProxy) {
        return getJschConnection(pHost, pUsername, passKey, useAuthKey, pListener, pUseProxy, Transporter.TIME_OUT);

    }

    public static Transporter getJschConnection(String pHost, String pUsername, String passKey, boolean useAuthKey,
                                                JSchListener pListener, boolean pUseProxy, int timeOut) {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Allow SFTP Connection only to BankSim pHost="+pHost+" Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
            if(!pHost.equals("psp-bank-intuit-com")) {
                logger.error("Non BankSim Host in DS2 and STG Env pHost="+pHost+" expected psp-bank-intuit-com");
                throw new NonBankSimSFTPException("Non BankSim Host in DS2 and STG Env pHost="+pHost+" expected psp-bank-intuit-com");
            }
            useAuthKey=false;
        }

        Transporter sftp = SftpFactory.createInstance(pHost, pUsername, passKey, useAuthKey);

        sftp.setTimeOut(timeOut);
        sftp.addListener(pListener);
        sftp.setKnownHostsPath(getConfigString("psp_sftp_knownHosts", null));
        sftp.setDebug(Boolean.parseBoolean(BatchUtils.getConfigString("psp_batch_ftp_debug")));

        setJSchProxy(sftp, pUseProxy);

        return sftp;
    }

    private static void initialize(SimpleFtpFile pFtp, FtpListener pListener, boolean pDebugMode) {
        if (pListener != null) {
            pFtp.addListener(pListener);
        }

        if (pDebugMode) {
            pFtp.setDebugMode();
        } else {
            pFtp.setLogEvents(SimpleFtpFile.FtpLogEvent.CONNECT,
                              SimpleFtpFile.FtpLogEvent.DISCONNECT,
                              SimpleFtpFile.FtpLogEvent.UPLOAD,
                              SimpleFtpFile.FtpLogEvent.DOWNLOAD,
                              SimpleFtpFile.FtpLogEvent.CHANGEDIR,
                              SimpleFtpFile.FtpLogEvent.DELETEFILE);
        }
    }

    private static void initialize(SimpleFtpsFile pFtps, FtpListener pListener, boolean pDebugMode) {
        if (pListener != null) {
            pFtps.addListener(pListener);
        }

        if (pDebugMode) {
            pFtps.setDebugMode();
        } else {
            pFtps.setLogEvents(SimpleFtpsFile.FtpLogEvent.CONNECT,
                               SimpleFtpsFile.FtpLogEvent.DISCONNECT,
                               SimpleFtpsFile.FtpLogEvent.UPLOAD,
                               SimpleFtpsFile.FtpLogEvent.DOWNLOAD,
                               SimpleFtpsFile.FtpLogEvent.CHANGEDIR,
                               SimpleFtpsFile.FtpLogEvent.DELETEFILE);
        }
    }

    public static Transporter getBankSftpConnection(JSchListener pListener) {
        Transporter sftp;

        boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        if (enableEncryption) {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ACH_HOST, getConfigString("psp_batch_bank_encrypted_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_batch_bank_encrypted_server");
            }

            sftp = getJschConnection(bankHost,
                    getConfigString("psp_batch_bank_encrypted_username"),
                    getConfigString("psp_batch_bank_encrypted_password"),
                    false, pListener, true,
                    Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));
        } else {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ACH_HOST, getConfigString("psp_batch_bank_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_batch_bank_server");
            }


            sftp = getJschConnection(bankHost,
                    getConfigString("psp_batch_bank_username"),
                    getConfigString("psp_batch_bank_password"),
                    false, pListener, true,
                    Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));
        }

        return sftp;
    }

    public static Transporter get401kSftpConnection(JSchListener pListener) {
        return getJschConnection(getConfigString("psp_batch_tp401k_server"),
                            getConfigString("psp_batch_tp401k_username"),
                            getConfigString("psp_batch_tp401k_password"),
                            false, pListener, true,
                            Integer.parseInt(getConfigString("psp_batch_401K_connection_timeout", "10000")));
    }

    public static Transporter get401kSignupSftpConnection(JSchListener pListener) {
        return getJschConnection(getConfigString("psp_batch_tp401k_server"),
                                getConfigString("psp_batch_tp401k_signup_username"),
                                getConfigString("psp_batch_tp401k_signup_password"),
                                false, pListener, true,
                                Integer.parseInt(getConfigString("psp_batch_401K_connection_timeout", "10000")));

    }

    public static Transporter getGemsSftpConnection(JSchListener pListener) {
        Transporter sftp = getJschConnection(getConfigString("psp_gems_ftp_server"),
                getConfigString("psp_gems_ftp_username"),
                getConfigString("psp_gems_ftp_password"),
                pListener, false);

        return sftp;
    }

    public static Transporter getGemsScpConnection(JSchAdapter pListener) {

        // create new sftp instance
        Transporter sftp = getJschConnection(getConfigString("psp_gems_scp_server"),
                getConfigString("psp_gems_scp_username"), getConfigString("psp_gems_scp_private_key"),
                true, pListener, false);

        // set connection timeout (default to 10 seconds)
        sftp.setDebug(Boolean.parseBoolean(getConfigString("psp_batch_ftp_debug")));

        return sftp;
    }

    public static Transporter getRAFSftpConnection(JSchListener pListener) {
        String hostname = getConfigString("psp_raf_ftp_server");
        String username = getConfigString("psp_raf_ftp_username");
        Transporter sftp;
        String filename = getConfigString("psp_raf_ftp_key");
        File privateKeyFile = new File(filename);

        if (privateKeyFile.exists() && privateKeyFile.canRead()) {
            sftp = getJschConnection(hostname, username, filename, true, pListener, false,
                    Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));
        } else {
            throw new RuntimeException("Unable to access private key file: " + filename);
        }

        return sftp;
    }

    public static Transporter getATFExtractSftpConnection(JSchListener pListener, String server, String user, String password, int timeout) {
        return getJschConnection(server, user, password, false, pListener, false, timeout);

    }

    public static Transporter getTFSExtractSftpConnection(JSchListener pListener, String server, String user, String privateKey, int timeout) {
        Transporter sftp;

        if (StringUtils.isNotEmpty(privateKey)) {
            sftp = getJschConnection(server, user, privateKey, true, pListener, false, timeout);
        } else {
            throw new RuntimeException("Unable to access private key secret.");
        }
        return sftp;
    }

    public static Transporter getEftpsAs400SftpConnection(JSchListener pListener) {
        //
        // Use RAF connection since we're connecting to the same place
        //
        return getRAFSftpConnection(pListener);
    }

    public static Transporter getPositivePaySftpConnection(JSchListener pListener) {
        Transporter sftp;

        boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        if (enableEncryption) {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ARP_HOST, getConfigString("psp_batch_bank_encrypted_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_batch_bank_encrypted_server");
            }
            sftp = getJschConnection(bankHost,
                    getConfigString("psp_batch_bank_encrypted_username"),
                    getConfigString("psp_batch_bank_encrypted_password"),
                    pListener, true);
        } else {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ARP_HOST, getConfigString("psp_batch_bank_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_batch_bank_server");
            }
            sftp = getJschConnection(bankHost,
                    getConfigString("psp_batch_bank_username"),
                    getConfigString("psp_batch_bank_password"),
                    false, pListener, true,
                    Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));
        }

        return sftp;
    }

    public static Transporter getReconPlusSftpConnection(JSchListener pListener) {
        return getJschConnection(getConfigString("psp_batch_reconplus_host"),
                getConfigString("psp_batch_reconplus_username"),
                getConfigString("psp_batch_reconplus_password"),
                false, pListener, false,
                Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));

    }

    public static Transporter getEdiVanScpConnection(JSchListener pListener) {

        // create Transporter instance using public key authentication
        Transporter sftp = getJschConnection(getTaxAgencyConfigString("psp_edi_scp_server"), getTaxAgencyConfigString("psp_edi_scp_username"),
                getTaxAgencyConfigString("psp_edi_scp_private_key"), true, pListener, false);

        sftp.setDebug(Boolean.parseBoolean(BatchUtils.getConfigString("psp_batch_ftp_debug")));

        return sftp;
    }

    public static Transporter getFsetSftpConnection(JSchListener pListener) {
        Transporter sftp = getJschConnection(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_server"),
                BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_username"),
                BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_password"), false, pListener, true, Integer.parseInt(getConfigString("psp_fset_sftp_connection_timeout", "10000")));

        sftp.setDebug(true);

        return sftp;
    }

    public static void setJSchProxy(Transporter pTransporter, boolean pUseProxy){
    	try{
	        boolean clearProxySettings = true;
	        boolean sysProxy = SystemParameter.findBooleanValue(SystemParameter.Code.SFTP_PROXY_ENABLED, false);
	        logger.info("SFTP proxy enabled : " + pUseProxy + ", SysParam proxy enabled : " + sysProxy);

	        if (pUseProxy && sysProxy) {
	            String proxyHost = getConfigString("psp_sftp_proxy_host", "");
	            Pattern pattern = Pattern.compile("^(http|socks5)://(.+):([0-9]+)$", Pattern.CASE_INSENSITIVE);
	            Matcher matcher = pattern.matcher(proxyHost);

	            if (matcher.matches()) {
	            	//pTransporter.setProxyType(matcher.group(1).toUpperCase()); /* HTTP or SOCKS5 */
	            	pTransporter.setProxyHost(matcher.group(2), Integer.parseInt(matcher.group(3))); /* host, port */
	                clearProxySettings = false;
	                logger.info("Successfully setted SFTP Proxy to " + proxyHost);
	            }
	        }

	        if (clearProxySettings) {
	        	pTransporter.clearProxySettings();
	        }
        } catch(Exception exception){
        	throw new RuntimeException("Error setting JSch proxy", exception);
        }
    }

    public static void emailCheckPaymentDetails(StringBuilder pFileContent, SpcfCalendar pProcessDate, PaymentTemplate pPaymentTemplate) {
        logger.info(String.format("Begin emailCheckPaymentDetails: %s, %s", pPaymentTemplate.getPaymentTemplateCd(), pProcessDate));

        String recipient = getConfigString("psp_batch_sui_checkpayments_email", "");
        StringBuilder message = new StringBuilder();

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for sending check Payment details, skipping task.");
        } else {

            try {
                String tempDir = getConfigString("psp_batch_temp", "");

                File tempCheckPaymentsFile = new File(tempDir, pPaymentTemplate.getPaymentTemplateCd() + "_CheckPayments_" + pProcessDate.format("yyyyMMdd") + ".txt");

                if (!tempCheckPaymentsFile.getParentFile().exists()) {
                    boolean created = tempCheckPaymentsFile.getParentFile().mkdirs();

                    if (!created) {
                        logger.error("Unable to create directory for temp State Report files.");
                        return;
                    }
                }

                // Write out check payment details to file so it can be attached
                FileWriter writer = new FileWriter(tempCheckPaymentsFile);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.append(pFileContent);
                bufferedWriter.close();
                writer.close();

                String subject = pPaymentTemplate.getPaymentTemplateCd() + " Check Payments for " + pProcessDate.format("MM/dd/yyyy");
                message.append("Check payments list for " + pPaymentTemplate.getPaymentTemplateCd() + ", Initiation date " + pProcessDate.toString() + " is attached here. \r\n\r\n");

                message.append(tempCheckPaymentsFile.getName()).append("\r\n");

                message.append("\r\n");
                message.append("<EOM>");

                logger.info(message.toString());

                List<String> attachments = new ArrayList<String>();
                attachments.add(tempCheckPaymentsFile.getAbsolutePath());

                MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                        recipient,                 // to
                        recipient,                 // from
                        subject,                   // subject
                        message.toString(),        // message body
                        attachments);        // attachments

                if (!tempCheckPaymentsFile.delete()) {
                    logger.warn("Could not delete temp report file " + tempCheckPaymentsFile.getAbsolutePath() + ".  This file needs to be manually deleted.");
                }

            } catch (Exception e) {
                logger.error("Unable to create check payments file for " + pPaymentTemplate.getPaymentTemplateCd() + " for " + pProcessDate.toString());
            }
        }

        logger.info(String.format("End emailCheckPaymentDetails: %s, %s", pPaymentTemplate.getPaymentTemplateCd(), pProcessDate));
    }

    //NOTE: This method can be removed when we move to the same url for ACH returns and NOC file.
    // Currently we use different urls in non-prod environments
    public static Transporter getBankSftpConnectionForNocDownload(JSchListener pListener) {
        Transporter sftp;
        boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        if (enableEncryption) {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ACH_HOST, getConfigString("psp_noc_bank_encrypted_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_noc_bank_encrypted_server");
            }

            sftp = getJschConnection(bankHost,
                                     getConfigString("psp_noc_bank_encrypted_username"),
                                     getConfigString("psp_noc_bank_encrypted_password"),
                                     pListener, true);
        } else {
            String bankHost = SystemParameter.findStringValue(SystemParameter.Code.BANK_SFTP_ACH_HOST, getConfigString("psp_batch_bank_server"));
            if(bankHost == null) {
                bankHost = getConfigString("psp_noc_bank_server");
            }


            sftp = getJschConnection(bankHost,
                                     getConfigString("psp_noc_bank_username"),
                                     getConfigString("psp_noc_bank_password"),
                                     pListener, true);
        }

        // set connection timeout (default to 10 seconds)
        sftp.setTimeOut(Integer.parseInt(getConfigString("psp_batch_bank_connection_timeout", "10000")));

        return sftp;
    }


    public static void downloadNocReturnAccountingFileAndEmail() throws S3ConnectionException,S3UploadException{
        logger.info(String.format("Begin downloadNocReturnAccountingFileAndEmail"));

        String recipient = getConfigString("psp_batch_nsf_email", "");

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for NOC file, skipping task.");
        } else {
            // This will download the NOC file from the ftp server to the receive directory
            new SftpNocReturnsFileDownload().download();

            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            String fileName = String.format("psp-noc-%s.csv", spcfCalendar.format("yyyyMMdd"));

            // Create file from the above saved location
            File file = new File(BatchUtils.getConfigString("psp_batch_ftp_recv_dir"), fileName);

            String subject = String.format("PSP NOC Returns file(s) for date: %s", spcfCalendar.format("MM/dd/yyyy"));
            StringBuffer message = new StringBuffer();

            ArrayList<String> filesToBeAttached = new ArrayList<String>();
            ArrayList<String> fileNamesToBeAttached = new ArrayList<String>();

            if (file != null && file.exists()) {

                filesToBeAttached.add(file.getAbsolutePath());
                fileNamesToBeAttached.add(file.getName());

                message.append("The attached NOC file for Assisted (Tax) companies is ready for processing:");
                message.append("\r\n\r\n");
                for (String fileToAttach : fileNamesToBeAttached) {
                    message.append(fileToAttach).append("\r\n");
                }
                message.append("\r\n");
                message.append("<EOM>");

                logger.info(message.toString());

                MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                     recipient,                 // to
                                     recipient,                 // from
                                     subject,                   // subject
                                     message.toString(),        // message body
                                     filesToBeAttached);        // attachments

                //encrypt the file as it is sent now
                file = encryptFileInStreamsUsingIDPS(file);

                // s3 part
                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
                String batchJobName = BatchJobType.NightlyBatchJobs.name();

                S3UploadUtils.archive(batchJobName,archiveDir,file.getAbsolutePath());

            } else {
                message.append("No NOC files were downloaded.");
                message.append("\r\n\r\n");

                message.append("\r\n");
                message.append("<EOM>");

                logger.info(message.toString());

                MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                     recipient,                 // to
                                     recipient,                 // from
                                     subject,                   // subject
                                     message.toString(),        // message body
                                     filesToBeAttached);        // attachments
            }
        }

        logger.info(String.format("End downloadNocReturnAccountingFileAndEmail"));
    }

    public static Transporter getJpmcCsiSftpConnection(JSchListener pListener, String host) {
        Transporter sftp = getJschConnection(getConfigString(host),
                                                getConfigString("psp_jpmc_csi_ftp_username"),
                                                getConfigString("psp_jpmc_csi_ftp_password"),
                                                pListener, true);
        return sftp;
    }

    public static Transporter getJpmcG2SftpConnection(JSchListener pListener, String host) {
        String hostname = getConfigString(host);
        String username = getConfigString("psp_jpmc_g2_ftp_username");
        Transporter sftp;
        String publicKey = getConfigString("psp_jpmc_g2_private_key");
        if (StringUtils.isNotEmpty(publicKey)) {
            sftp = getJschConnection(hostname, username, publicKey, true, pListener, Boolean.TRUE);
        } else {
            throw new RuntimeException("Unable to access private key secret.");
        }
        return sftp;
    }

    public static Transporter getJmpcAmlConnection(JSchListener pListener,String pHost) {
        String hostname = getConfigString(pHost);
        String username = getConfigString("psp_jpmc_aml_ftp_username");
        Transporter sftp;
        String privateKey = getConfigString("psp_jpmc_aml_private_key");
        if (StringUtils.isNotEmpty(privateKey)) {
            sftp = getJschConnection(hostname, username, privateKey, true, pListener, false);
        } else {
            throw new RuntimeException("Unable to access private key secret.");
        }
        return sftp;
    }

    public static Transporter getGemsConnection(JSchListener pListener) {
        String hostname = getConfigString("psp_gems_new_scp_server");
        String username = getConfigString("psp_gems_scp_username");
        Transporter sftp;
        String privateKey = getConfigString("psp_gems_scp_private_key");
        if (StringUtils.isNotEmpty(privateKey)) {
            sftp = getJschConnection(hostname, username, privateKey, true, pListener, false);
        } else {
            throw new RuntimeException("Unable to access private key secret.");
        }
        return sftp;
    }

    public static Transporter getWCConnection(JSchListener pListener,String hostname,String username,String password) {
        Transporter sftp;
        if (StringUtils.isNotEmpty(password)) {
            sftp = BatchUtils.getJschConnection(hostname, username, password,false,  pListener, true);
        } else {
            throw new RuntimeException("Unable to access private key secret.");
        }
        return sftp;
    }


    public static void sendNotificationEmail(String pSubLine, String pMessage, String pRecipient) {
        logger.info("Begin sendNotificationEmail.");
        logger.info(pMessage);

        if ((pRecipient == null) || (pRecipient.length() == 0)) {
            logger.warn("No recipient email address specified to send email notifications, skipping task.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy h:mm a, z");
            String timestamp = sdf.format(CalendarUtils.convertToDate(PSPDate.getPSPTime()));
            String subject = String.format("%s (%s)", pSubLine, timestamp);

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                                 pRecipient, // to
                                 pRecipient, // from
                                 subject,   // subject
                                 pMessage); // message body
        }
        logger.info("End sendNotificationEmail.");
    }

    public static String scheduleJob(String jobName, SpcfCalendar timerExpression, String... jobInstanceParameters) throws Exception {
        Boolean USE_FLUX = SystemParameter.findBooleanValue(SystemParameter.Code.SCHEDULE_SAP_BATCHJOBS_USING_FLUX, Boolean.FALSE);
        logger.info("USE_FLUX : "+USE_FLUX);
        logger.info("TimerExpression : "+timerExpression);
        String jobId = null;

        //Adding the rollback flag, calling the FLUX BatchJobManager if flag is ON
        if(USE_FLUX) {
            String timerExpressionFromSpcfCalendar = null;
            if(timerExpression != null)
                timerExpressionFromSpcfCalendar = FluxUtils.getTimerExpressionFromSpcfCalendar(timerExpression);
            BatchJobManager batchJobManager = new BatchJobManager();
            jobId = batchJobManager.scheduleJob(BatchJobType.valueOf(jobName), jobInstanceParameters.toString(), timerExpressionFromSpcfCalendar);
        } else {
            String when = null;
            if(timerExpression != null)
                when = timerExpression.format(SCHEDULE_DATE_FORMAT);
            jobId = JSSGateway.scheduleJob(jobName, when, jobInstanceParameters);
        }
        return jobId;
    }

    public static List<String> getTfaPgpKeys() {
        List<String> keys = new ArrayList<String>();
        keys.add(BatchUtils.getConfigString("psp_tfa_public_key"));
        keys.add(BatchUtils.getConfigString("psp_tfa_intuit_public_key"));
        return keys;
    }

    public static List<String> getTfsPgpKeys() {
        List<String> keys = getTfaPgpKeys();
        keys.add(BatchUtils.getConfigString("psp_tfs_public_key"));
        return keys;
    }

    public static List<String> getAMLPgpKeys() {
        List<String> result = new ArrayList<String>();
        result.add(BatchUtils.getConfigString("psp_jpmc_aml_public_key"));
        result.add(BatchUtils.getConfigString("psp_tfa_intuit_public_key"));
        return result;
    }

    public static List<String> getWCPgpKeys(){
        List<String> result = new ArrayList<String>();
        result.add(BatchUtils.getConfigString("wc_server_file_encrypt_publickey"));
        result.add(BatchUtils.getConfigString("psp_tfa_intuit_public_key"));
        return result;
    }

    public static List<String> getWCSplitLimitPgpKeys() {
        List<String> result = new ArrayList<String>();
        if(isWCSFTPSPLITLIMITPPDEnabled()) {
            result.add(BatchUtils.getConfigString("wc_server_insurepay_file_encrypt_ppd_publickey"));
        }
        if(isWCSFTPSPLITLIMITPRDEnabled()) {
            result.add(BatchUtils.getConfigString("wc_server_insurepay_file_encrypt_prd_publickey"));
        }
        result.add(BatchUtils.getConfigString("psp_tfa_intuit_public_key"));
        return result;
    }

    public static FileStore getFileStore() throws Exception {
        if(Application.isParallelEnv()) {
            logger.info("Parallel Env Mock S3 FileStore Object BatchUtils Env="+Application.getEnvironmentName()+" SpringProfile="+Application.getSpringProfile());
            return new MockS3FileStore();
        }
        logger.info("Setting up S3 object BatchUtils");

        FileStore.FileStoreBuilder fileStore = FileStore.builder();
        String region = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,PSP_BATCHJOBS_S3_BUCKET_REGION);
        if(!Application.isAWSEnvironment()){
            fileStore.withS3AccessKey(BatchUtils.getConfigString("psp_s3_access_key"))
                    .withS3SecretKey(BatchUtils.getConfigString("psp_s3_secret"));
            String proxyHost = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyHost");
            if(!StringUtils.isEmpty(proxyHost)) {
                fileStore.withProxyHost(proxyHost)
                        .withProxyPort(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyPort"));
            }
        }
        fileStore.withRegions(Regions.fromName(region)).withEncryptionType(EncryptionType.NO_ENCRYPTION)
                .withFileStoreType(FileStoreType.S3);

        String s3BucketAssumeRole = BatchUtils.getConfigString("psp_iks_batch_s3_iam_assume_role");
        if(!StringUtils.isEmpty(s3BucketAssumeRole)){
            logger.info("s3 assume role is being set to "+s3BucketAssumeRole);
            fileStore.withS3RoleName(s3BucketAssumeRole);
        }
        FileStore result = FileStoreFactory.buildFileStore(fileStore);
        return result;
    }


    /*
    This mail is sent after the success of RTB Backup and update
     */
    public static void sendRTBUniqueIdentificationEmail(String eventType, String psid, String creatorId, String uniqueID,String fName,String lName) {
        logger.info("Begin sendRTBUniqueIdentificationEmail.");
        String pMessage=null;
        String recipient=fName+"_"+lName+"@intuit.com";
        pMessage="RTB Automation Communication \n" +
                "\n" +
                "This message is in reference to the following company: \n " +
                "CompanyID \n" +
                "\n" +
                "\n" +
                "Dear FirstName LastName ,\n" +
                "\n" +
                "You just modified the data for EventType \n" +
                "Unique ID: UniqueIdentifier \n" +
                "Note: This unique Id can be used if you want to raise RTB ticket in future to revert or update the dataset. \n" +
                "\n" +
                "Sincerely, \n" +
                "\n" +
                "RTB Service Group\n" +
                "Intuit Payroll Services";
        pMessage=pMessage.replace("CompanyID",psid);
        pMessage=pMessage.replace("FirstName",fName);
        pMessage=pMessage.replace("LastName",lName);
        pMessage=pMessage.replace("EventType",eventType);
        pMessage=pMessage.replace("UniqueIdentifier",uniqueID);
        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for RTB Automation change, skipping task.");
        } else {

            String subject = String.format("%s", "Notification: RTB Automation");

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                    recipient, // to
                    "no_reply@intuit.com", // from
                    subject,   // subject
                    pMessage); // message body
        }

        logger.info("End sendRTBUniqueIdentificationEmail.");
    }
     /*
     This method send the mail after deletion of RTB backup data from PSP_RTBAUTOMATIONBACKUP table
      */
    public static void sendRTBBackupDeletionNotificationEmail(String pMessage,String recipient) {
        logger.info("Begin sendRTBBackupDeletionNotificationEmail.");
        logger.debug(pMessage);

        if ((recipient == null) || (recipient.length() == 0)) {
            logger.warn("No recipient email address specified for RTB deletion notifications, skipping task.");
        } else {

            String subject = String.format("%s", "Notification: RTB backup data deletion");

            MailSender.sendEmail(getConfigString("psp_batch_mail_server"), // server
                    recipient, // to
                    "no_reply@intuit.com", // from
                    subject,   // subject
                    pMessage); // message body
        }

        logger.info("End sendRTBBackupDeletionNotificationEmail.");
    }

    public static void uploadFileViaSftp(String pHost, String pUser, String pPassword, String pPath, String pFileName, InputStream pFileContents, int pRetries, int pTimeout, SpcfLogger logger) {
        int retryCount = 0;
        boolean retry = true;
        while(retry) {
            Transporter sftp = BatchUtils.getJschConnection(pHost, pUser, pPassword, false, new JSchAdapter(), false, pTimeout);
            try {
                sftp.setLogger(logger);
                sftp.connect();

                if(StringUtils.isNotEmpty(pPath)) {
                    sftp.changeRemoteDir(pPath);
                }

                sftp.uploadFile(pFileName, pFileContents);
                retry = false;
            } catch (Exception e) {
                if (retryCount < pRetries) {
                    logger.error("SFTP failed on try number " + retryCount + ".  Sleeping for 10 seconds.", e);
                    ++retryCount;
                    retry = true;
                    try {
                        pFileContents.reset();
                    } catch (IOException ioException) {
                        logger.error("Error resetting file input stream.", e);
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    try {
                        //Sleep for 10 seconds
                        Thread.sleep(SystemParameter.findIntValue(SystemParameter.Code.SFTP_RETRY_SLEEP_MILLIS, 10000));
                    } catch (InterruptedException ie) {
                        //Do nothing
                    }
                    logger.info("Retrying SFTP...");
                } else {
                    logger.error("SFTP failed after maximum number of " + pRetries + " retries.");
                    throw new RuntimeException(e.getMessage(), e);
                }
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    retry = false;
                    logger.error("Error in disconnecting (aborting process) ", e);
                }
            }
        }
    }
}
