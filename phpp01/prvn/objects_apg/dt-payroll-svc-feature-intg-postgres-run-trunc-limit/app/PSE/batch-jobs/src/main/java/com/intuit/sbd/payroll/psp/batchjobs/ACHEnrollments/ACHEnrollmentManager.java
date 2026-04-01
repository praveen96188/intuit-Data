package com.intuit.sbd.payroll.psp.batchjobs.ACHEnrollments;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.h2.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * User: ihannur
 * Date: 2/5/13
 * Time: 2:34 PM
 */
public class ACHEnrollmentManager {

    private static final String ACH_FILE_EOL = "\r\n";

    private static final String TAX_CATEGORY = "5425";
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(ACHEnrollmentManager.class);

    public static void transmitPendingACHEnrollmentFiles() {
        logger.info("Begin transmitPendingACHEnrollmentFiles.");

        try {
            File sendDir = new File(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_send_dir"));

            File errDir = new File(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_err_dir"));

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<ACHEnrollmentFile> achEnrollmentFiles = Application.find(ACHEnrollmentFile.class, ACHEnrollmentFile.Status().equalTo(ACHEnrollmentFileStatus.PendingTransmission));

            for (ACHEnrollmentFile achEnrollmentFile : achEnrollmentFiles) {
                File srcFile = new File(achEnrollmentFile.getFileName());
                try {
                    FileUtils.copyFileToDirectory(srcFile, sendDir);
                } catch (Throwable t) {
                    logger.error("Send File: " + achEnrollmentFile.getFileName() + " failed, updating File status to Error", t);
                    String batchJobName = BatchJobType.ACHEnrollmentBatchJob.name();
                    try{
                        achEnrollmentFile.setFileName(S3UploadUtils.archive(batchJobName,errDir.getAbsolutePath(),achEnrollmentFile.getFileName()));
                    }catch (Throwable t1){
                        logger.error("Unable to upload to S3",t1);
                    }
                    achEnrollmentFile.updateStatus(ACHEnrollmentFileStatus.Error);
                    return;
                }

                achEnrollmentFile.updateStatus(ACHEnrollmentFileStatus.SentToAgency);

                Application.save(achEnrollmentFile);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw new RuntimeException("Error in create ADD/DELETE ACHEnrollment file.", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info("End transmitPendingACHEnrollmentFiles.");
    }

    public static void archiveACHEnrollmentFiles() {
        logger.info("Begin archiveACHEnrollmentFiles.");

        try {
            File archiveDir = new File(BatchUtils.getTaxAgencyConfigString("psp_achenrollment_arcv_dir"));
            String batchJobName = BatchJobType.ACHEnrollmentBatchJob.name();

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<ACHEnrollmentFile> achEnrollmentFiles = Application.find(ACHEnrollmentFile.class, ACHEnrollmentFile.Status().in(ACHEnrollmentFileStatus.SentToAgency));

            for (ACHEnrollmentFile achEnrollmentFile : achEnrollmentFiles) {
                try{
                    achEnrollmentFile.setFileName(S3UploadUtils.archive(batchJobName,archiveDir.getAbsolutePath(),achEnrollmentFile.getFileName()));
                } catch (Throwable t) {
                    logger.error("Archive File: " + achEnrollmentFile.getFileName() + " failed, updating File status to Archived", t);
                }
                achEnrollmentFile.updateStatus(ACHEnrollmentFileStatus.Archived);
                Application.save(achEnrollmentFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info("End archiveACHEnrollmentFiles.");
    }

    public static void processResponseFiles() {
        logger.info("Begin processResponseFiles.");
        try {
            PayrollServices.beginUnitOfWork();
            int countsOfValidRecords = 0;
            DomainEntitySet <ACHEnrollmentDetail> pendingAchEnrollmentDetails = Application.find(ACHEnrollmentDetail.class, ACHEnrollmentDetail.ACHEnrollment().Status().equalTo(ACHEnrollmentStatus.PendingEnrollmentResponse));
            DomainEntitySet<ACHEnrollmentFile> achEnrollmentResponseFiles = Application.find(ACHEnrollmentFile.class, ACHEnrollmentFile.Status().in(ACHEnrollmentFileStatus.UploadedByAgent));
            for (ACHEnrollmentFile achEnrollmentResponseFile : achEnrollmentResponseFiles) {
                if (achEnrollmentResponseFile.getFileContent() != null && achEnrollmentResponseFile.getFileContent() != null) {

                    BufferedReader bufferedReader = new BufferedReader(IOUtils.getReader(achEnrollmentResponseFile.getFileContent()));
                    countsOfValidRecords += processAndRemoveResponseRecords(achEnrollmentResponseFile, bufferedReader, pendingAchEnrollmentDetails);
                    achEnrollmentResponseFile.updateStatus(ACHEnrollmentFileStatus.Processed);

                } else {
                    logger.error("ACHEnrollmentResponse File is uploaded by Agent without file contents, File Name:" + achEnrollmentResponseFile.getFileName());
                    achEnrollmentResponseFile.updateStatus(ACHEnrollmentFileStatus.Error);
                }
            }

            if(countsOfValidRecords > 0) {
                for (ACHEnrollmentDetail pendingAchEnrollmentDetail : pendingAchEnrollmentDetails) {
                    pendingAchEnrollmentDetail.getACHEnrollment().updateStatus(ACHEnrollmentStatus.EnrollmentRejected);
                    pendingAchEnrollmentDetail.getACHEnrollment().setStatusReason("Not found in FL Agent listing file");
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw new RuntimeException("Error in processing ACHEnrollment response file", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info("End processResponseFiles.");
    }

    private static int processAndRemoveResponseRecords(ACHEnrollmentFile pACHEnrollmentResponseFile, BufferedReader pBufferedReader, DomainEntitySet<ACHEnrollmentDetail> pPendingAchEnrollmentDetails) throws IOException {
        StringBuilder invalidLines = new StringBuilder();
        int counts = 0;
        String line;
        try {
            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
            while ((line = pBufferedReader.readLine()) != null) {
                // Lines starting with 7 digit account number, 9 digit FEIN and then status, account number and FEIN are separated by single space
                if (line.length() > 17 && line.substring(0, 17).matches("\\d{7}\\s\\d{9}")) {

                    counts++;
                    String accountNumber = line.substring(0, 7);
                    String ein = line.substring(8, 17);
                    List<String> agencyIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(ACHEnrollmentDetail.AgencyIdKeyName, accountNumber);
                    List<String> feinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(ACHEnrollmentDetail.FeinKeyName, ein);
                    ACHEnrollmentDetail achEnrollmentDetail = pPendingAchEnrollmentDetails.find(ACHEnrollmentDetail
                            .AgencyIdEnc().in(agencyIdEncList).And(ACHEnrollmentDetail.FeinEnc().in(feinEncList)))
                            .getFirst();

                    if (achEnrollmentDetail != null) {
                        achEnrollmentDetail.setResponseFile(pACHEnrollmentResponseFile);
                        achEnrollmentDetail.getACHEnrollment().setStatusReason("Found in FL Agent listing file");

                        //Update the status to Enrolled and create Company event for ACH Enrollment
                        achEnrollmentDetail.getACHEnrollment().updateStatus(ACHEnrollmentStatus.Enrolled);

                        //Updating Agent register flag to true
                        PayrollServices.paymentManager.updatePaymentAgentEnabledCore(achEnrollmentDetail.getACHEnrollment().getCompanyAgency().getCompany().getSourceSystemCd(),
                                                                                     achEnrollmentDetail.getACHEnrollment().getCompanyAgency().getCompany().getSourceCompanyId(),
                                                                                     paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true);
                        Application.save(achEnrollmentDetail);
                        pPendingAchEnrollmentDetails.remove(achEnrollmentDetail); // Removing this from pendingEnrollmentResponse list as this is marked as Enrolled
                    }

                } else {
                    String invalidLine = StringUtils.trim(line);
                    boolean isHeader = invalidLine != null  &&
                            (invalidLine.equals("\n") || invalidLine.equals("\f\n")  || invalidLine.equals("\f")|| invalidLine.equals("") ||
                                    invalidLine.startsWith("ACCOUNT") ||
                                    invalidLine.startsWith("Account") ||
                                    invalidLine.startsWith("Qtr")
                            );
                    if (StringUtils.isNotEmpty(invalidLine) && !isHeader) {
                        invalidLines.append(line).append("\n");
                    }
                }
            }
        } finally {
            pBufferedReader.close();
        }

        if (counts == 0) {
            logger.error("ACHEnrollment Response file doesn't have any ACHEnrollment responses.");
        } else {
            logger.info("Processed: " + counts + " ACHEnrollment response/s.");
        }

        // Alert agents if the file had contents that could not be processed or if the file had zero contents
        if (counts == 0 || invalidLines.length() > 0) {
            sendAlertForInvalidResponse(pACHEnrollmentResponseFile, invalidLines, (counts == 0));
        }

        return counts;
    }



    public static ACHEnrollmentFile createACHEnrollmentFile(SpcfCalendar pEffectiveDate, boolean pAdd) throws IOException {

        logger.info("Begin createACHEnrollmentFile EffectiveDate: "+ pEffectiveDate);
        ACHEnrollmentFile achEnrollmentFile = null;
        PayrollServices.beginUnitOfWork();
        try {
            DomainEntitySet<ACHEnrollment> achEnrollments;
            if (pAdd) {
                achEnrollments = Application.find(ACHEnrollment.class, ACHEnrollment.Status().equalTo(ACHEnrollmentStatus.PendingEnrollment)
                                                                                    .And(ACHEnrollment.EffectiveDate().equalTo(pEffectiveDate)));
            } else {
                achEnrollments = Application.find(ACHEnrollment.class, ACHEnrollment.Status().equalTo(ACHEnrollmentStatus.PendingDelete)
                                                                                    .And(ACHEnrollment.EffectiveDate().equalTo(pEffectiveDate)));
            }

            logger.info("Creating ACH Enrollment ADD/DELETE records, Selected records:" + achEnrollments.size() + ", ADD Flag:" + pAdd);

            if (achEnrollments.size() > 0) {
                achEnrollmentFile = new ACHEnrollmentFile();

                String outDir = BatchUtils.getTaxAgencyConfigString("psp_achenrollment_work_dir");
                String timestamp = PSPDate.getPSPTime().format("yyyyMMddHHmmssS");
                String filenamePrefix = "DORUTEA";
                String testFileName = BatchUtils.getTaxAgencyConfigString("psp_achenrollment_test_filename");
                if (StringUtils.isNotEmpty(testFileName)) {
                    filenamePrefix = testFileName;
                }
                achEnrollmentFile.setFileName(outDir + File.separator + filenamePrefix + timestamp);

                achEnrollmentFile.updateStatus(ACHEnrollmentFileStatus.PendingTransmission);

                achEnrollmentFile.setType(pAdd ? ACHEnrollmentFileType.Add : ACHEnrollmentFileType.Delete);

                Application.save(achEnrollmentFile);
                PrintWriter printWriter = null;
                try {
                    Key key  = IDPSFileStreamManager.newKeyHandleLatest();
                    IDPSFileWriter fileWriter = new IDPSFileWriter(new File(achEnrollmentFile.getFileName()),key,"US-ASCII");
                    printWriter = new PrintWriter(fileWriter);

                    writeFileHeaderRecord(printWriter);
                    for (ACHEnrollment achEnrollment : achEnrollments) {
                        ACHEnrollmentDetail achEnrollmentDetail = new ACHEnrollmentDetail();
                        achEnrollmentDetail.setACHEnrollment(achEnrollment);
                        achEnrollmentDetail.setRequestFile(achEnrollmentFile);
                       try{
                           writeEmployer1Record(printWriter, achEnrollment, achEnrollmentDetail, pAdd);
                       }catch(Exception ex){
                           String errorMessage   ="Error while processing request in ACHEnrollmentManager while "+(pAdd ?" Adding " :" Deleting ")+" ACHEnrollment.";
                           if(achEnrollment.getCompanyAgency() !=null){
                               errorMessage=  errorMessage+ " psid:"+achEnrollment.getCompanyAgency().getCompany().getSourceCompanyId() +" ,Agency :"+achEnrollment.getCompanyAgency().getAgency().getAgencyId()+". Reason is  :";
                           }   else{
                               errorMessage=errorMessage+" Reason is  :";
                           }
                           logger.error(errorMessage,ex);
                           continue;
                       }

                        if (pAdd) {
                            achEnrollment.updateStatus(ACHEnrollmentStatus.PendingEnrollmentResponse);
                        } else {
                            achEnrollment.updateStatus(ACHEnrollmentStatus.Deleted);
                        }
                        Application.save(achEnrollmentDetail);
                        achEnrollment.setACHEnrollmentDetail(achEnrollmentDetail);
                    }
                    writeFooterRecord(printWriter, achEnrollments.size() + 1);

                } finally {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                }
                Application.save(achEnrollmentFile);
                logger.info("Created ACH Enrollment ADD/DELETE File:" + achEnrollmentFile.getFileName() + ", Selected records:" + achEnrollments.size() + ", ADD Flag:" + pAdd);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info("End createACHEnrollmentFile.");
        return achEnrollmentFile;
    }

    private static void writeFileHeaderRecord(PrintWriter pWriter) throws IOException {
        pWriter.append("0"); // Header record starts with 0, Record Type  Always equals 0 for Agent record.
        pWriter.append(String.format("%s", "0000000A00" + BatchUtils.getTaxAgencyConfigString("psp_achenrollment_agent_id"))); // Agent/Employer number, Leading zeroes + (A00 + 5 digit Agent # or E + 7 digit UT Acct. #)
        pWriter.append(TAX_CATEGORY); // Tax Category
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_agent_name"))); // Business Name of Submitter
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_first_name"))); // Contact Name - First
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_middle_name"))); // Contact Name - Middle
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_last_name"))); // Contact Name - Last
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_attn_line"))); // Attn. Line
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_address_line"))); // Mailing Address
        pWriter.append(String.format("%-26s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_address_city"))); // Contact City
        pWriter.append(String.format("%-2s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_address_state"))); // Contact State
        pWriter.append(String.format("%5s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_address_zip"))); // Contact Zip1
        pWriter.append(String.format("%4s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_address_zip_extn"))); // Contact Zip2
        pWriter.append(String.format("%10s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_phone"))); // Contact Phone
        pWriter.append(String.format("%10s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_fax"))); // Contact Fax
        pWriter.append(String.format("%2s", "US")); // Contact Country Code // For To
        pWriter.append(String.format("%-10s", "")); // Contact Foreign Postal Code
        pWriter.append(String.format("%-45s", "")); // Contact Foreign City Name
        pWriter.append(String.format("%-45s", "")); // Contact Foreign Province Name
        pWriter.append(String.format("%015d", 0)); // Contact Foreign Phone
        pWriter.append(String.format("%015d", 0)); // Contact Foreign Fax
        pWriter.append(String.format("%1d", 2)); // Payment method
        pWriter.append(String.format("%1d", 6)); // Filing method
        pWriter.append(String.format("%-20s", "")); // Bank Account Number
        pWriter.append(String.format("%09d", 0)); // Bank Transit Key
        pWriter.append(String.format("%-40s", "")); // Bank Name
        pWriter.append(String.format("%1d", 0)); // Checking/Savings Indicator. Valid options 0 = Checking, 1 = Savings
        pWriter.append(String.format("%-255s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_credit_justification"))); // Credit Justification
        pWriter.append(String.format("%-40s", BatchUtils.getTaxAgencyConfigString("psp_achenrollment_contact_email"))); // Contact Email Address
        pWriter.append(ACH_FILE_EOL); // EOL
    }

    private static void writeEmployer1Record(PrintWriter pWriter, ACHEnrollment pACHEnrollment, ACHEnrollmentDetail pACHEnrollmentDetail, boolean pAdd) throws IOException {

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = pACHEnrollment.getCompanyAgency().getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate)).getFirst();
        if(companyAgencyPaymentTemplate ==null ) {
            logger.error("ACH enrollment request can be sent for empty agency Id, companyAgencyPaymentTemplate is null");
            return;
        }
        if(StringUtils.isEmpty(companyAgencyPaymentTemplate.getAgencyTaxpayerId())) {
            logger.error("ACH enrollment request can be sent for empty agency Id, CompanyAgencyId: "+ companyAgencyPaymentTemplate.getCompanyAgency().getId());
            return;
        }

        pWriter.append("1"); // Employer1 record starts with 1  , Record Type
        String agencyId = companyAgencyPaymentTemplate.getAgencyTaxpayerId().replace("-", "");
        pACHEnrollmentDetail.setAgencyId(agencyId);   // Response file will have agency Id, no zero padding in response file
        pWriter.append(String.format("%015d", Integer.parseInt(agencyId))); // Zero padded Agency Id, Account number, Request file is expecting 15 digit zero padded agency Id

        pWriter.append(pACHEnrollment.getCompanyAgency().getCompany().getFedTaxId()); //FEIN
        pACHEnrollmentDetail.setFEIN(pACHEnrollment.getCompanyAgency().getCompany().getFedTaxId());

        pWriter.append(pACHEnrollment.getEffectiveDate().format("yyyyMMdd")); // Effective Date, YYYYMMDD, For Add it is first day of current quarter, For Delete last day of previous quarter when request is created

        if (pAdd) {
            pWriter.append("A"); // Reason Code, For Add it is A
        } else {
            pWriter.append("D"); // Reason Code, For Delete it is D
        }
        pWriter.append(TAX_CATEGORY); // Tax Category
        pWriter.append("3"); // Agent Reporting Code
        String legalName = formatStingValue(pACHEnrollment.getCompanyAgency().getCompany().getLegalName());
        pWriter.append(String.format("%-40s", legalName)); // Business Name
        pACHEnrollmentDetail.setLegalName(legalName);
        pWriter.append(ACH_FILE_EOL); // EOL

    }

    private static void writeFooterRecord(PrintWriter pWriter, int pRecordsCount) throws IOException {
        //Footer record starts with 9, then followed by number of records including header
        pWriter.append("9"); // Record Type
        pWriter.append(String.format("%010d", pRecordsCount)); // Zero padded 10 digits long number of records
        pWriter.append(ACH_FILE_EOL); // EOL
    }

    private static String formatStingValue(String pLegalName) {
        if (pLegalName != null) {
            pLegalName = pLegalName.replaceAll("\\W", " ").replaceAll("_", " ");
            return pLegalName.toUpperCase();
        }

        return " ";
    }

    /**
     * Construct email messages for failed responses
     *
     * @param pACHEnrollmentResponseFile Failed response file
     * @param pZeroProcessedResponses    Is nothing in file processed?
     * @param pInvalidLines any other invalid lines in file
     */
    private static void sendAlertForInvalidResponse(ACHEnrollmentFile pACHEnrollmentResponseFile, StringBuilder pInvalidLines, boolean pZeroProcessedResponses) {
        logger.info("Sending email for errors in ACH Response file");
        StringBuffer message = new StringBuffer();

        message.append("One or more errors occured when processing the following ACH Enrollment response.");
        message.append(BatchUtils.NEWLINE);
        message.append(new File(pACHEnrollmentResponseFile.getFileName()).getName());
        message.append(BatchUtils.NEWLINE);

        if (pZeroProcessedResponses) {
            message.append("Zero record was processed. The file was either empty or had invalid contents.");
            message.append(BatchUtils.NEWLINE);
        }
        if (pInvalidLines.length() > 0) {
            message.append("The following lines were invalid \n").append(pInvalidLines.toString());
            message.append(BatchUtils.NEWLINE);
        }

        logger.info(message);
        sendEmail("psp_achenrollment_response_errors_notify_list", "psp_achenrollment_response_errors_notify_subject", message.toString());
    }

    private static void sendEmail(String pToFromProperty, String pSubjectProperty, String pMessageBody) {
        //
        // don't fail the overall batch job if there is an error sending email, just report the error in the log.
        //
        try {
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                                 BatchUtils.getConfigString(pToFromProperty), // to
                                 BatchUtils.getConfigString(pToFromProperty), // from
                                 BatchUtils.getConfigString(pSubjectProperty),
                                 pMessageBody);
        } catch (Exception e) {
            logger.error("Failed to send email message for daily batch job. Email message was: " + pMessageBody, e);
        }
    }

}
