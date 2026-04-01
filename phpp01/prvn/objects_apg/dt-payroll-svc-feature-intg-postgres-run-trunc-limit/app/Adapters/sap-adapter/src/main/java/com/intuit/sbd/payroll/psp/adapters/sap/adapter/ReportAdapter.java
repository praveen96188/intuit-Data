package com.intuit.sbd.payroll.psp.adapters.sap.adapter;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPReportJob;
import com.intuit.sbd.payroll.psp.adapters.sap.rtb.ReportEnum;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by :smodgil on 01/22/20.
 * Description: This class works as a adapter containing business logic for the Download Report tool created in SAP UI
 */

public class ReportAdapter {


    private static final SpcfLogger logger = PayrollServices.getLogger(ReportAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    public static final String ENCRYPTED_FILE_EXT = ".pgp";
    public static final String FILE_EXT = ".txt";

    @FlexMethod

    @Operation(operationIds = OperationId.ReportFileDownload)

    public List<SAPReportJob> getReportList() throws Throwable {

        List<SAPReportJob> saprtbJobs = null;

        try {

            List<String> roles = getRoles();

            saprtbJobs = ReportEnum.getSAPRTBJobListForRole(roles);

        } catch (Throwable t) {

            aeFactory.throwGenericException("Error finding Report names.", t);

        }

        return saprtbJobs;

    }

    /*
    @FlexMethod

    @Operation(operationIds = OperationId.ReportFileDownload)

    public String downloadReport(String reportType,String date) throws Throwable {

        String reportName="";
        try {

            logger.info("Begin downloading AML file for the given date");

            BatchJobType type = Enum.valueOf(BatchJobType.class, reportType);

            switch (type) {
                case AMLReportProcessor:
                    AMLReportDownload amlReport = new AMLReportDownload();
                    reportName = amlReport.downloadReport(date);
            }

        } catch (S3ConnectionException e) {
            aeFactory.throwGenericException("Connection Failure occured!", e);
        } catch (S3DownloadException e) {
            aeFactory.throwGenericException("Download failure occured!", e);
        } catch (FileNotFoundException e) {
            aeFactory.throwGenericException("Decrypted file not found", e);
        } catch (Exception e) {
            aeFactory.throwGenericException("Exception occured while downloading report from s3 bucket", e);
        }
        logger.info("The report name to be downloaded is "+ reportName);
        return  reportName;

    }
    */

    /**
     * get the list of roles for logged in User
     *
     * @return
     */

    public List<String> getRoles() {

        List<String> roles = new ArrayList<String>();


        try {

            PayrollServices.beginUnitOfWork();

            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

            AuthUser foundUser = AuthUser.findUser(principal.getId());

            for (AuthRole authRole : foundUser.getAuthRoleCollection()) {

                roles.add(authRole.getRoleId());

            }

            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {

            PayrollServices.rollbackUnitOfWork();

            logger.error("Error while retrieving the user role information.");

        }


        return roles;

    }

    @FlexMethod
    public void deleteGeneratedReport(String decFile) {
        File sourceEncFile=null;
        File sourceDecryptedFile=null;
        try {
            logger.info("Inside deleteGenerateReport call");
            String encFile = getFilePath() + File.separator + decFile;
            String decryptedFile = encFile.replace(FILE_EXT, ENCRYPTED_FILE_EXT);

            sourceEncFile = new File(encFile);
            sourceDecryptedFile = new File(decryptedFile);

            if (sourceEncFile.exists()) {
                sourceEncFile.delete();
            }

            if (sourceDecryptedFile.exists()) {
                sourceDecryptedFile.delete();
            }

        } catch (Exception e) {
            logger.error("Exception occured while deleting the Generated AML report {}", e);
        } finally {
            try{
                if (sourceEncFile.exists()) {
                    sourceEncFile.delete();
                }

                if (sourceDecryptedFile.exists()) {
                    sourceDecryptedFile.delete();
                }
            }catch(Exception e){
                logger.error("ReportAdapter.deleteGeneratedReport Unable to delete the files:" +sourceEncFile +" and "+sourceDecryptedFile);
            }

        }

    }

    private static String getFilePath(){

        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_arcv_dir");
    }

}



