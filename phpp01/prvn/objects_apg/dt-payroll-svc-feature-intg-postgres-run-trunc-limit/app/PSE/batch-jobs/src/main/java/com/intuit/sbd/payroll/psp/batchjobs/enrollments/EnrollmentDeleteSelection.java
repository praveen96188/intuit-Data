package com.intuit.sbd.payroll.psp.batchjobs.enrollments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: dweinberg
 * Date: 4/24/13
 * Time: 11:17 AM
 */
public class EnrollmentDeleteSelection {

    private final static SpcfLogger logger = Application.getLogger(EnrollmentDeleteSelection.class);
    private String DATE_FORMAT = "MM/dd/yyyy";
    /*
    Company status = terminated or canceled
    ACH Enrollment status = enrolled
    Last quarter to file is less than the current quarter

    Company status = active
    FL PItem (Filing Flag) = Inactive
    Not already in the delete file

    No Pending FL Tax Payment
     */
    public void selectACHEnrollmentsForDelete() {
        ArrayList<Object[]> achEnrollmentsForDelete = Application.executeNamedQuery(
                Application.getQueryName("achEnrollmentsForDelete"), new String[]{"excludeDeletedCompany"}, new Object[]{Company.isDGDeleteFeatureEnabled()});
        for (Object[] achEnrollmentForDelete : achEnrollmentsForDelete) {
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf((String) achEnrollmentForDelete[0]);
            String sourceCompanyId = (String) achEnrollmentForDelete[1];

            ProcessResult pr = PayrollServices.companyManager.deleteACHEnrollment(sourceSystemCode, sourceCompanyId, PaymentTemplate.FL_SUI);
            if (!pr.isSuccess()) {
                throw new RuntimeException(String.format("Error deleting ACH enrollment for %s:%s. %s", sourceSystemCode, sourceCompanyId, pr.toString()));
            }
        }
    }

    public void selectRAFEnrollmentsForDelete() {
        ArrayList<Object[]> rafEnrollmentsForDelete = Application.executeNamedQuery(
                Application.getQueryName("rafEnrollmentsForDelete"), new String[]{"excludeDeletedCompany"}, new Object[]{Company.isDGDeleteFeatureEnabled()});
        for (Object[] rafEnrollmentForDelete : rafEnrollmentsForDelete) {
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf((String) rafEnrollmentForDelete[0]);
            String sourceCompanyId = (String) rafEnrollmentForDelete[1];
            SpcfUniqueId rafEnrollmentId = SpcfUniqueId.createInstance((String)rafEnrollmentForDelete[2]);

            RAFEnrollment rafEnrollment = Application.findById(RAFEnrollment.class, rafEnrollmentId);

            ProcessResult pr = PayrollServices.companyManager.updateRAFEnrollmentStatus(sourceSystemCode, sourceCompanyId, rafEnrollment, RAFEnrollmentStatus.PendingDeleteTape);
            if (!pr.isSuccess()) {
                throw new RuntimeException(String.format("Error deleting RAF enrollment for %s:%s. %s", sourceSystemCode, sourceCompanyId, pr.toString()));
            }
        }
    }

    public void writeMonthlyEnrollmentReport() throws IOException, S3UploadException, S3ConnectionException {

        String header = "Change Date,PSID,FEIN,Company Name,Payment Template,Agency,Agency Taxpayer ID,Type of Change\n";
        String dataForActivePaymentTemplates = getDataForCancelledCompaniesWithActivePaymentTemplates();
        String dataForPaymentTemplatesForActiveCompanies = getDataForPaymentTemplateForActiveCompanies();
        String dataForNewlyAddedCompanyLaws = getDataForNewlyAddedCompanyLaws();

        String filenameExtension=".csv";
        String tempDir = BatchUtils.getConfigString("psp_batch_temp", "");
        String filename = String.format("Monthly_Enrollment_Report for %s", PSPDate.getPSPTime().format("MMM yyyy"));
        File tempMonthlyEnrollmentReportFile = new File(tempDir, filename + filenameExtension);
        if (!tempMonthlyEnrollmentReportFile.getParentFile().exists()) {
            boolean created = tempMonthlyEnrollmentReportFile.getParentFile().mkdirs();
            if (!created) {
                logger.error("Unable to create directory for temp Monthly Enrollment Report files.");
                return;
            }
        }

        // Write out report to file so it can be attached
        FileWriter writer;
        try {
            writer = new FileWriter(tempMonthlyEnrollmentReportFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.append(header);
            bufferedWriter.append(dataForActivePaymentTemplates);
            bufferedWriter.append(dataForPaymentTemplatesForActiveCompanies);
            bufferedWriter.append(dataForNewlyAddedCompanyLaws);
            bufferedWriter.close();
            writer.close();
        } catch (IOException e) {
            logger.error(e);
            throw e;
        }

        StringBuilder message = new StringBuilder();

        message.append("Created the Monthly Enrollment Report for the Companies that have cancelled the service but have active payment templates and for the companies that are active but their payment templates are turned Inactive")
               .append(".")
               .append("\r\n\r\n")
               .append(tempMonthlyEnrollmentReportFile.getName())
               .append("\r\n")
               .append("</EOM>");

        //String recipient = "praveenkumar_hoolimath@intuit.com";
        String recipient = BatchUtils.getTaxAgencyConfigString("psp_fset_notify_list");
        String subject = "Monthly Enrollment Report";
        if(StringUtils.isNotEmpty(recipient)){
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"), recipient, recipient, subject, message.toString(), tempMonthlyEnrollmentReportFile.getAbsolutePath());
        } else {
            logger.error("Email id not found");
        }
        //encrypt this file as its not get deleted...
        BatchUtils.encryptFileInStreamsUsingIDPS(tempMonthlyEnrollmentReportFile);
        String batchJobName = BatchJobType.EnrollmentDeleteSelectionProcessor.name();
        S3UploadUtils.archive(batchJobName,tempDir,tempMonthlyEnrollmentReportFile.getAbsolutePath());

        //To be commented for unit tests to confirm the results
        /*// Delete temp file now that E-mail is created
        if (!tempMonthlyEnrollmentReportFile.delete()) {
            logger.error("Could not delete temp report file " + tempMonthlyEnrollmentReportFile + ".  This file needs to be manually deleted.");
        }*/

    }

    /**
     * This method gets the data(for the last month) about the Payment Templates that are active even after the cancellation of the
     * Service by the customer. Excluding the agencies IRS and FLDOR
     * @return data for the report with the fields, (Change date, PSID, FIEN, Company Name, Payment Template, AID, type of change)
     */
    private String getDataForCancelledCompaniesWithActivePaymentTemplates(){
        //Companies that have cancelled the service but still have the payment templates active

        SpcfCalendar firstDay = CalendarUtils.getFirstDayOfMonth(PSPDate.getPSPTime());
        SpcfCalendar lastDay = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime());

        logger.info("first day " + firstDay);
        logger.info("last day " + lastDay);

        String queryString = "select cs.StatusEffectiveDate, c.SourceCompanyId, c.FedTaxIdEnc, c.LegalName, capt.PaymentTemplate.PaymentTemplateCd, ca.Agency.AgencyId, capt.AgencyTaxpayerIdEnc " +
                    " from com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate capt " +
                    " join capt.CompanyAgency ca " +
                    " join ca.Company c " +
                    " join c.CompanyServiceSet cs" +
                    " where " +
                    " cs.StatusCd in ('" + ServiceSubStatusCode.Cancelled + "','" + ServiceSubStatusCode.Terminated + "')" +
                    " and cs.Service='" + ServiceCode.Tax + "' " +
                    " and ca.Agency not in ('NOCALC','IRS','FLDOR') " +
                    " and cs.StatusEffectiveDate between :firstDayOfMonth and :lastDayOfMonth " +
                    " and exists (select '1'" +
                    "             from com.intuit.sbd.payroll.psp.domain.CompanyLaw cl" +
                    "             join cl.Law l " +
                    "             where cl.CompanyAgency=ca and " +
                    "             l.PaymentTemplate=capt.PaymentTemplate and cl.FilingStatus='Active')";

        if (Company.isDGDeleteFeatureEnabled()) {
            queryString = queryString + " and c.IsDgDisassociated = 0 ";
        }

        queryString = queryString + " order by cs.StatusEffectiveDate desc, capt.PaymentTemplate.PaymentTemplateCd";

        String[] paramNames = {"firstDayOfMonth","lastDayOfMonth"};
        Object[] paramValues = {firstDay, lastDay};
        List<Object[]> results = Application.executeHQLQuery(queryString, paramNames, paramValues);

        StringBuilder builder = new StringBuilder();
        for (Object[] result : results) {
            SpcfCalendar changeDate = (SpcfCalendar) result[0];
            builder.append(changeDate.format(DATE_FORMAT));
            builder.append(",");
            builder.append(result[1]);
            builder.append(",");
            String decryptedFein = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName, (String) result[2]);
            builder.append(decryptedFein);

            builder.append(",");
            builder.append("\"" + result[3] + "\"");
            builder.append(",");
            builder.append(result[4]);
            builder.append(",");
            builder.append(result[5]);
            builder.append(",");                                           //AID
            String decryptedAid = EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, (String) result[6]);
            builder.append(decryptedAid);
            builder.append(",Payroll Service Cancellation");
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * This method gets the data(for the last month) for the payment templates, which payment templates have changed from Active to Inactive and viceversa,
     * status determined by the FilingStatus field of the CompanyLaw corresponding to it.
     * Excluding the agencies IRS and FLDOR
     * A Payment template is said to be Active even if there is a single law using it (in the CompanyLaw).
     * @return data for the report with the fields, (Change date, PSID, FIEN, Company Name, Payment Template, AID, type of change)
     */
    private String getDataForPaymentTemplateForActiveCompanies(){

        SpcfCalendar firstDay = CalendarUtils.getFirstDayOfMonth(PSPDate.getPSPTime());
        Timestamp firstDayTimestamp = new Timestamp(firstDay.getTimeInMilliseconds());
        SpcfCalendar lastDay = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime());
        Timestamp lastDayTimestamp = new Timestamp(lastDay.getTimeInMilliseconds());

        String[] paramNames = {"firstDayOfMonth","lastDayOfMonth","excludeDeletedCompany"};
        Object[] paramValues = {firstDayTimestamp, lastDayTimestamp, Company.isDGDeleteFeatureEnabled()};
        String namedQuery = Application.getQueryName("paymentTemplateStatusChangesForActiveCompaniesENC");

        List<Object[]> results = Application.executeNamedQuery(namedQuery, paramNames, paramValues);

        StringBuilder builder = new StringBuilder();
        for (Object[] result : results) {
            Timestamp changeDateTS = (Timestamp) result[0];                             //Change Date
            Date dateFromTS = CalendarUtils.convertLocalTimestamp(changeDateTS.getTime());
            SpcfCalendar changeDate = CalendarUtils.convertToSpcfCalendar(dateFromTS);
            builder.append(changeDate.format(DATE_FORMAT));
            builder.append(",");
            builder.append(result[1]);                                                //PSID
            builder.append(",");

            String decryptedFein = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName, (String) result[2]);//FEIN
            builder.append(decryptedFein);

            builder.append(",");
            builder.append("\"" +result[3] + "\"");                                   //Company Name
            builder.append(",");
            builder.append(result[4]);                                                //Payment Template
            builder.append(",");
            builder.append(result[5]);                                                //Agency
            builder.append(",");

            String decryptedAid = EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, (String) result[6]);//AID
            builder.append(decryptedAid);

            builder.append(",");
            builder.append(result[7]);                                                //type of change
            builder.append(" to ");
            builder.append(result[8]);
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Gets the Data for the newly added laws in the last month.
     */
    private String getDataForNewlyAddedCompanyLaws(){
        SpcfCalendar firstDay = CalendarUtils.getFirstDayOfMonth(PSPDate.getPSPTime());
        Timestamp firstDayTimestamp = new Timestamp(firstDay.getTimeInMilliseconds());
        SpcfCalendar lastDay = CalendarUtils.getLastDayOfMonth(PSPDate.getPSPTime());
        Timestamp lastDayTimestamp = new Timestamp(lastDay.getTimeInMilliseconds());

        String[] paramNames = {"firstDayOfMonth","lastDayOfMonth"};
        Object[] paramValues = {firstDayTimestamp, lastDayTimestamp};

        String queryString = "select distinct " +
                Application.getTruncFunctionString("cl.CreatedDate") +
                ", c.SourceCompanyId, c.FedTaxIdEnc, c.LegalName, capt.PaymentTemplate.PaymentTemplateCd, ca.Agency.AgencyId, capt.AgencyTaxpayerIdEnc  " +
                    " from com.intuit.sbd.payroll.psp.domain.CompanyLaw cl " +
                    " join cl.Law l" +
                    " join cl.CompanyAgency ca " +
                    " join ca.Company c " +
                    " join c.CompanyServiceSet cs, " +
                    " com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate as capt " +
                    " where capt.CompanyAgency=ca " +
                    " and l.PaymentTemplate.PaymentTemplateCd=capt.PaymentTemplate.PaymentTemplateCd " +
                    " and cl.CreatedDate between :firstDayOfMonth and :lastDayOfMonth " +
                    " and ca.Agency not in ('NOCALC','IRS','FLDOR') " +
                    " and cs.Service='"+ServiceCode.Tax+"' " ;

        if (Company.isDGDeleteFeatureEnabled()) {
            queryString = queryString + " and c.IsDgDisassociated = 0 ";
        }
        //" group by cl.CreatedDate, c.SourceCompanyId, c.FedTaxId, c.LegalName, capt.PaymentTemplate.PaymentTemplateCd, ca.Agency.AgencyId, capt.AgencyTaxpayerId" +
        queryString = queryString + " order by " +
                Application.getTruncFunctionString("cl.CreatedDate") +
                ", capt.PaymentTemplate.PaymentTemplateCd";

        List<Object[]> results = Application.executeHQLQuery(queryString, paramNames, paramValues);

        StringBuilder builder = new StringBuilder();
        for (Object[] result : results) {
            SpcfCalendar changeDate;
            if(result[0] instanceof SpcfCalendar){
                changeDate = (SpcfCalendar) result[0];
            } else {
                Timestamp changeDateTS = (Timestamp) result[0];                             //Change Date
                Date dateFromTS = CalendarUtils.convertLocalTimestamp(changeDateTS.getTime());
                changeDate = CalendarUtils.convertToSpcfCalendar(dateFromTS);
            }
            builder.append(changeDate.format(DATE_FORMAT));
            builder.append(",");
            builder.append(result[1]);                                                //PSID
            builder.append(",");
            String decryptedFein = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName, (String) result[2]);//FEIN
            builder.append(decryptedFein);
            builder.append(",");
            builder.append("\"" +result[3] + "\"");                                   //Company Name
            builder.append(",");
            builder.append(result[4]);                                                //Payment Template
            builder.append(",");
            builder.append(result[5]);                                                //Agency
            builder.append(",");
            String decryptedAid = EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, (String) result[6]);//AID
            builder.append(decryptedAid);
            builder.append(",");
            builder.append(" New Law Added");                                         //type of change
            builder.append("\n");
        }
        return builder.toString();
    }

}
