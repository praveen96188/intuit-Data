package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.common.utils.batchjobs3util.IDPSS3FileUtility;
import com.intuit.sbd.payroll.psp.tools.DisableEntitlementUnitList;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;

/**
 * Class: ProcessMassCancellation handle different
 * method of auto mass cancellation process
 *
 * @author  sbishi
 * @since   2020-05-12
 */
public class ProcessMassCancellation {

    private static final SpcfLogger logger = PayrollServices.getLogger(ProcessMassCancellation.class);

    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private final String mArchiveDir = BatchUtils.getConfigString("psp_rtb_mcn_arcv_dir");

    JobResult jobResult = new JobResult();

    /**
     * @param pToProperty = To address
     * @param pSubjectProperty = Email Subject
     * @param file = File to process
     */
    protected void sendEmail(String pToProperty,String pFromProperty, String pSubjectProperty, String file) {
        String pMessageBody = "";
        try {
            pMessageBody    =   "Please process the file :<b> "+file.substring(file.indexOf("MassCancel_"))+ "</b> for auto mass cancellation";
            pSubjectProperty = String.format("%s", "Notification: RTB Automation");
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString(pToProperty), // to
                    BatchUtils.getConfigString(pFromProperty), // from
                    pSubjectProperty,
                    pMessageBody);
        } catch (Exception e) {
            logger.error("Failed to send email message for mass cancellation failure case. Email message was: " + pMessageBody, e);
        }
    }

    /**
     *
     * @param fileName
     * @param mapRecord
     * @return
     * @throws IOException
     */
    protected String writeFailedCompanyToFile(String fileName,Map<String,String> mapRecord)throws IOException {

        File file = new File(fileName);
        OutputStreamWriter fileWriter = null;

        file.createNewFile();
        fileWriter = new FileWriter(fileName,true);

        for(String key: mapRecord.keySet()){

            fileWriter.write(mapRecord.get(key)+","+key);
            fileWriter.write(System.getProperty("line.separator"));
        }
        fileWriter.close();
        return fileName;
    }

    /**
     * @param selectedMassCnFile
     * @return process mc status
     */
    public JobResult executeMassCancellation(String selectedMassCnFile) {

        StopWatch stopWatch = StopWatch.startTimer();

        logger.info("Auto MassCancelTimer:started job " + stopWatch.getElapsedMillis());

        String outDir        = BatchUtils.getConfigString("psp_rtb_mcn_send_dir");
        String[] listOfFiles = new File(outDir).list();
        long fileCount       = Arrays.asList(listOfFiles).stream().filter(file -> file.equals(selectedMassCnFile)).count();
        String filePath      = outDir + File.separator + selectedMassCnFile;
        String outDirDecr    = BatchUtils.getConfigString("psp_rtb_mcn_decr_dir");
        List<String> recordStrList = new ArrayList<>();
        List<String> list;
        String[] recordList;
        String status    = "";
        File fileSend    = null;
        File fileTemp    = null;
        String filePathDecry = null;

        logger.info("File count : "+fileCount);

        try {
            if (fileCount > 0) {

               filePathDecry = outDirDecr + File.separator + selectedMassCnFile;
               IDPSS3FileUtility.decryptFile(filePath, filePathDecry);
               logger.info("File copy from: "+filePath + " to : "+filePathDecry);

               recordList = new String(Files.readAllBytes(Paths.get(filePathDecry))).split("\n");

               if(recordList.length > 0){

                  logger.info("Auto Mass cancelling for " +  recordList.length + " records. RecordSize=" +  recordList.length);
                  recordStrList = Arrays.asList(recordList);

                //Run manual query to disable company
               recordStrList.forEach(record->{
                   this.removeOnHold(record.split(",")[1]);
                   this.setLastQuarterToFile(record.split(",")[1]);
               });

                //Sending file to ERS for Disable Entitlement
                String[] args = {"-file=" + filePathDecry};
                String jobResultMainAuto = new DisableEntitlementUnitList().mainAuto(args);

                //Cancel company service
               recordStrList.forEach(record->{
                   this.cancelCompanyService(record.split(",")[1]);
               });

                fileSend = new File(filePath);
                fileTemp = new File(filePathDecry);

                //archiving file
                S3UploadUtils.archiveWithNoBatchJob(mArchiveDir,filePath);
                logger.info("File archive from : "+filePath + " to : "+mArchiveDir);

                logger.info("Auto MasCancelTimer:done processing .elapsed time: " + stopWatch.getElapsedMillis());
                stopWatch.stop();
                status = "Success";

                jobResult.addInfoMessage("=============REPORT (AUTO MASS CANCELLATION) ================");
                jobResult.addInfoMessage("Total number of companies to be cancelled " + recordList.length );
                jobResult.addInfoMessage("Number of companies successfully to cancelled " + jobResultMainAuto);
                if(null != jobResultMainAuto ){
                     jobResult.addInfoMessage("Number of companies failed to cancelled " + (recordList.length - Integer.valueOf(jobResultMainAuto)));
                }else{
                     jobResult.addInfoMessage("Number of companies failed to cancelled " + (recordList.length - 0));
                }
                jobResult.setSuccess(true);
                jobResult.addInfoMessage("=============================================================");

                logger.info("Auto Mass cancelling is completed. SuccessfulCancelled=" + jobResultMainAuto + " TotalRecords=" + recordList.length);
             }
            }
        } catch (Exception exception) {
            jobResult.addErrorMessage(exception.getMessage());
            logger.error("Disabling of Entitlement and cancelling of company services is failed for :"+exception.getMessage());

        } finally {
            try {
                if(status.equals("Success")){
                    if(fileSend.exists())
                        fileSend.delete();
                    if(fileTemp.exists())
                        fileTemp.delete();
                }
            } catch (Exception exception) {
                logger.error("Failed to delete file" + fileTemp.getName()+"  - "+fileSend.getName()+" for :"+exception);
            }
            return jobResult;
        }
    }
    /**
     * Cancel the company Services
     * @param companyId
     */
    private void cancelCompanyService(String companyId) {
        PayrollServices.beginUnitOfWork();
        StringBuilder builderComSer = new StringBuilder();
        builderComSer.append("update psp_company_service" +
                " set STATUS_CD = 'Cancelled', modified_date = " +
                Application.getUTCTimeExtractString("CURRENT_TIMESTAMP") +
                ", modifier_id = :modifierId"  +
                " where status_cd NOT IN ('Cancelled') and SERVICE_FK in ('Tax', 'DirectDeposit') and" +
                " company_fk in (select company_seq from psp_company where source_company_id in (:companyId))");
        org.hibernate.Query queryComSer = Application.getHibernateSession().createSQLQuery(builderComSer.toString());
        queryComSer.setParameter("modifierId", Application.getCurrentPrincipal().getId());
        queryComSer.setParameter("companyId", companyId);
        int noOfRowUpdatequeryComSer = queryComSer.executeUpdate();
        PayrollServices.commitUnitOfWork();
        logger.info("Cancel the company Services for company : "+companyId +" : "+noOfRowUpdatequeryComSer);
    }
    /**
     * Set the LAST_QUARTER_TO_FILE to file
     * @param companyId Company source IdRTBAdapter.java
     */
    private void setLastQuarterToFile(String companyId) {
        PayrollServices.beginUnitOfWork();
        StringBuilder builderSeInfo = new StringBuilder();
        builderSeInfo.append("update psp_tax_company_service_info" +
                " set LAST_QUARTER_TO_FILE = :lastQuarterToFile"  +
                " where TAX_COMPANY_SERVICE_INFO_SEQ in (select company_service_seq from psp_company_service" +
                " where service_fk = 'Tax' and company_fk in (select company_seq from psp_company" +
                " where source_company_id in(:companyId)))") ;
        org.hibernate.Query querySeInfo = Application.getHibernateSession().createSQLQuery(builderSeInfo.toString());
        int currentQuarter =Integer.valueOf(String.valueOf(Calendar.getInstance(Locale.US).get(Calendar.YEAR)) + String.valueOf(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR)));
        querySeInfo.setParameter("lastQuarterToFile", currentQuarter);
        querySeInfo.setParameter("companyId", companyId);
        int noOfRowUpdatequerySeInfo = querySeInfo.executeUpdate();
        PayrollServices.commitUnitOfWork();
        logger.info("Set the LAST_QUARTER_TO_FILE for company : "+companyId +" : "+noOfRowUpdatequerySeInfo);
    }
    /**
     * Remove On Holds
     * @param companyId
     */
    private void removeOnHold(String companyId) {
        PayrollServices.beginUnitOfWork();
        StringBuilder builderHoldReason = new StringBuilder();
        builderHoldReason.append("update PSP_ON_HOLD_REASON" +
                " set expiration_date = " +
                Application.getUTCTimeExtractString("CURRENT_TIMESTAMP") +
                ", modified_date = " +
                Application.getUTCTimeExtractString("CURRENT_TIMESTAMP") +
                ", modifier_id = :modifierId"  +
                " where on_hold_reason_seq in (" +
                " select on_hold_reason_seq from PSP_ON_HOLD_REASON" +
                " where PSP_ON_HOLD_REASON.COMPANY_FK IN" +
                " (select company_seq from psp_company" +
                " where source_company_id in(:companyId))" +
                " and expiration_date is null)");
        org.hibernate.Query queryHoldReason = Application.getHibernateSession().createSQLQuery(builderHoldReason.toString());
        queryHoldReason.setParameter("modifierId", Application.getCurrentPrincipal().getId());
        queryHoldReason.setParameter("companyId", companyId);
        int noOfRowUpdateHoldReason = queryHoldReason.executeUpdate();
        PayrollServices.commitUnitOfWork();
        logger.info("Remove On Holds for company : "+companyId +" : "+noOfRowUpdateHoldReason);
    }

    /**
     *
     * @return
     */
    public static List<String> getFailedMassCanFileList() {
        List<String> processMassCanFileList = new ArrayList<>();
        String outDir = BatchUtils.getConfigString("psp_rtb_mcn_send_dir");
        File folderProcessMassCanFile = new File(outDir);

        File[] listOfFiles = folderProcessMassCanFile.listFiles();
        if(null !=listOfFiles) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().contains("MassCancel_")) {
                    processMassCanFileList.add(file.getName());
                }
            }
        }
        processMassCanFileList.remove(".DS_Store");
        return processMassCanFileList;
    }
}
