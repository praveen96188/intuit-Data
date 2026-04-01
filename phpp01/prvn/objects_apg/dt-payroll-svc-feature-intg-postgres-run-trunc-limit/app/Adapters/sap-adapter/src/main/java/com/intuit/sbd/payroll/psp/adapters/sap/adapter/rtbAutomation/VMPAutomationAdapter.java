package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate.VmpCheck;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBAutomationJob;
import com.intuit.sbd.payroll.psp.adapters.sap.rtbAutomation.RTBAutomationJobEnum;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class VMPAutomationAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(VMPAutomationAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    DomainEntitySet<Company> companyList = null;
    String errMsg=null;


    @FlexMethod
    @Operation(operationIds = OperationId.ExecuteRTBAutomationJob)
    public List<SAPRTBAutomationJob> getAutomationJobList() throws Throwable {
        List<SAPRTBAutomationJob> saprtbJobs = null;
        try {
            List<String> roles = getRoles();
            saprtbJobs = RTBAutomationJobEnum.getSAPRTBJobListForRole(roles);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding RTBJobs jobs.", t);
        }
        return saprtbJobs;
    }


    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
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
            logger.error("Error while retrieving the user role information."+e.getMessage());
        }
        return roles;

    }
    /*
       The flow from SAP comes here and it triggers the update  Realm id with null value
        */
    @FlexMethod
    public void updateRealmId(final String psId) throws Throwable {

        logger.info("Begin: VMP service");
        try{
            String creatorId=null;
            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
            if(principal !=null){
                creatorId=principal.getName();
                logger.info(" VMP service: User Creator Corp Id :"+creatorId);
            }
            AuthUser user = AuthUser.findUser(principal.getId());
            Application.beginUnitOfWork();
            updateRealmIdAndBackUpVmpService(psId, creatorId);
            String uniqueId=VmpCheck.getUniqueIDForIdentification();
            String eventType=RTBBackUpEventType.VMPSERVICEEVENT.toString();
            String fName=user.getFirstName();
            String lName=user.getLastName();
            logger.info(" VMP service: User First Name :"+fName+ "User Last Name :" +lName +"User Event Type :"
            +eventType+"Display Unique Id :"+uniqueId);
             Application.commitUnitOfWork();
            logger.info(" VMP service: All edited data saved successfully in database");
            BatchUtils.sendRTBUniqueIdentificationEmail(eventType, psId, creatorId, uniqueId, fName, lName);
            logger.info("End:  VMP service");
        }
        finally {
            Application.rollbackUnitOfWork();
        }
    }
    /**
     * @param psId
     * @param creatorId
     * Description = Update Realm id with null value and Back up PSP_company Data
     */
    private void updateRealmIdAndBackUpVmpService(final String psId, final String creatorId) throws Exception {
        if(null!=psId && psId.length()>0 ) {
            List<String> psidValue = getCommaSeparatedList(psId);
         if (psidValue.size() <= 2) {
                for (String strPsid : psidValue) {
                    companyList = getCompanyList(strPsid);
                    if (companyList != null && companyList.size() > 0) {
                        for (Company company : companyList) {
                            String strCompanyName = company.getLegalName();
                            String strRealmValue = company.getIAMRealmId();
                            logger.info(" Company Legal Name :"+strCompanyName);
                            logger.info(" Company Realm Value :"+strRealmValue);
                            //Calling Method to take backUp of VMPServiceData
                            VmpCheck.backUpVMPServiceData(strPsid,creatorId);
                            if (strRealmValue == null){
                                errMsg = "CompanyName :"+strCompanyName +"\nRealmId doesn't exist";
                                logger.error(" Display Company Legal Name And Realm Id with Exception :"+errMsg);
                                aeFactory.throwGenericException(errMsg);
                            }
                            VmpCheck.updateVMPCheckData(strPsid, creatorId);
                        }
                    } else {
                        errMsg = "Company doesn't exist";
                        logger.error(" Display Company Legal Name with Exception :"+errMsg);
                        aeFactory.throwGenericException(errMsg);
                    }
                }
            }else {
                errMsg = "Should be less than 2 Input PSID";
                logger.error(" Display Exceed more than 2 PSID with Exception :"+errMsg);
                aeFactory.throwGenericException(errMsg);
            }
        }
    }

    /**
     * @param psId
     * @return DomainEntitySet<Company>
     * Description = Get list of Company using psId param
     */

    private DomainEntitySet<Company> getCompanyList(final String psId){
        companyList =Application.find(Company.class,new Query<Company>().Where(Company.SourceCompanyId().equalTo(psId)));
        return companyList;
    }

    /**
     * @param parameter
     * @return List<String>
     * description = Check Comma Separated for variable
     */
    public static List<String> getCommaSeparatedList(final String parameter) {
        List<String> param = Arrays.asList(parameter.trim().split(","));
        for (int i = 0; i < param.size(); i++) {
            String s = param.get(i);
            s = s.trim();
            param.set(i, s);
        }
        return param;
    }

}

