package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.IdpsRuntimeException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBJob;
import com.intuit.sbd.payroll.psp.adapters.sap.rtb.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.encryption.DataEncrypter;
import com.intuit.sbd.payroll.psp.common.utils.encryption.IDPSDecrypter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by anandp233 on 2/25/14.
 */

public class RTBAdapter {


    private static final SpcfLogger logger = PayrollServices.getLogger(RTBAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final Boolean IS_AUTO_MASSCAN_ENABLED = true;

    @FlexMethod

    @Operation(operationIds = OperationId.ExecuteRTBJob)

    public List<SAPRTBJob> getRTBJobList() throws Throwable {

        List<SAPRTBJob> saprtbJobs = null;

        try {

            List<String> roles = getRoles();

            saprtbJobs = RTBJobEnum.getSAPRTBJobListForRole(roles);

        } catch (Throwable t) {

            aeFactory.throwGenericException("Error finding RTBJobs jobs.", t);

        }

        return saprtbJobs;

    }

    @FlexMethod

    @Operation(operationIds = OperationId.ExecuteRTBJob)

    public String uploadAndExecuteRTBJob(String selectedRTBJob, byte[] fileBinary) throws Throwable {

        JobResult jobResult = new JobResult();

        RTBJobEnum rtbJob = null;

        try {

            logger.info("Executing the selected " + selectedRTBJob + " RTB Job.");

            rtbJob = RTBJobEnum.getRTBJob(selectedRTBJob);

            List<String> roles = getRoles();


            //verify the user role is authorized to execute the RTB operation

            if (!rtbJob.isSupportedRole(roles)) {

                jobResult.setSuccess(false);

                jobResult.addErrorMessage("Your are not authorized to execute " + rtbJob.getShortDescription() + " operation.");

                logger.error("Logged in user is not authorized to execute " + rtbJob.getShortDescription() + " RTB operation.");

                return jobResult.toString();

            }


            ExecuteRTBJob executeRTBJob = new ExecuteRTBJob();

            jobResult = executeRTBJob.runRTBJob(rtbJob, fileBinary);

            if (jobResult.isSuccess()) {

                logger.info("Execution of " + rtbJob.getShortDescription() + " RTB job is successful.");

                logger.info("\n" + rtbJob.getShortDescription() + " Job Result :\n" + jobResult);

            } else {

                logger.error("Execution of " + rtbJob.getShortDescription() + " RTB job is failed.");

            }

        } catch (Throwable t) {

            String jobName = (rtbJob == null ? "" : rtbJob.getShortDescription());

            aeFactory.throwGenericException("Error while executing the " + jobName + " job.", t);

        }

        return jobResult.toString();

    }

    @FlexMethod
    @Operation(operationIds = OperationId.DecryptText)
    public String decryptText(String encrypt) throws Throwable {

        String decryptedText = null;
        try {
            boolean useIdps = SystemParameter.findBooleanValue(SystemParameter.Code.ENABLE_IDPS_ENCRYPTION_FOR_PTC, false);
            if(useIdps){
                decryptedText = IDPSDecrypter.decryptQuickbaseData(encrypt.trim());
            } else {
                decryptedText = DataEncrypter.decryptQuickbaseData(encrypt.trim());
            }
        }
        catch (IdpsRuntimeException | IdpsException | IdpsCommunicationException e){
            logger.info("Encryption_type=IDPS Error decrypting with IDPS : "+ e.getMessage());
            logger.info("Attempting to decrypt using local key");
            try {
                decryptedText = DataEncrypter.decryptQuickbaseData(encrypt.trim());
            }
            catch (Throwable t){
                logger.error("Encryption_type=LEGACY Error while decrypting the encrypted text");
                aeFactory.throwGenericException("Error while decrypting the encrypted text.", t);
            }
        }
        catch (Throwable t){
            logger.error("Encryption_type=LEGACY Error while decrypting the encrypted text");
            aeFactory.throwGenericException("Error while decrypting the encrypted text.", t);
        }
        return decryptedText;
    }

    @FlexMethod
    public String getQueryConsoleIksUrl() throws Throwable {
        String queryConsoleUrl = "";
        boolean isQueryConsoleInIksEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_QUERY_CONSOLE_IN_IKS, true);
        if (isQueryConsoleInIksEnabled) {
            queryConsoleUrl = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "query_console_iks_url");
            logger.info("query console url is " + queryConsoleUrl);
        }
        return  queryConsoleUrl;
    }
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
    @Operation(operationIds = OperationId.ExecuteRTBJob)
    public List<String> getProcessMassCanFileList() throws Throwable {

        List<String> processMassCanFileList = null;
        try {
            processMassCanFileList =  new ProcessMassCancellation().getFailedMassCanFileList();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding getProcessMassCanFileList", t);
        }
        return processMassCanFileList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ExecuteRTBJob)
    public String executeMassCancellation(String selectedMassCnFile) throws Throwable {
        JobResult jobResult = new JobResult();
        ProcessMassCancellation processMassCancellation = new ProcessMassCancellation();
        List<String> processMassCanFileList = null;
        try {
            jobResult = processMassCancellation.executeMassCancellation(selectedMassCnFile);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding executeMassCancellation", t);
        }
        return jobResult.toString();
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ExecuteRTBJob)
    public Boolean getAutoMassCanFlag() throws Throwable {
        return IS_AUTO_MASSCAN_ENABLED;
    }



}



