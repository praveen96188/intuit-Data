package com.intuit.sbd.payroll.psp.adapters.sap.rtbAutomation;



import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBAutomationJob;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBJob;



import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**

 * Created by smodgil on 02/08/19.
 Add the enum here for each RTB ticket which has to be automated
 */

public enum RTBAutomationJobEnum {

    
    //UPDATE_DUP_EMPLOYEE("updateDuplicateEmployee", "Update Duplicate Employee", "It will resolove duplicate employee with given PSIDs",getSupportedRoles("RTBAutomationAdmin")),
    CANCEL_VMP_REALM("cancelRealmID", "Cancel Realm ID", "It will resolove with given PSIDs",getSupportedRoles("RTBAutomationAdmin")),
    UPDATE_DUP_PITEM("updateDuplicatePitem", "Update Duplicate Pitem", "It will resolove duplicate pitems with given PSIDs",getSupportedRoles("RTBAutomationAdmin")),
    UNPROCESSED_REQUEST_INFO("clearUnprocessedRequests", "Clear Unprocessed Requests", "Update Unprocessed request as Processed",getSupportedRoles("RTBAutomationAdmin"));
    private String id;//must be unique

    private String shortDescription;//short description which will be visible to end users

    private String description;//details technical description for understanding the developer or analyst

    private Set supportedRoles;

    //Role Ids
    private static final String RTB_AUTOMATION_ROLE_ID = "RTBAutomationAdmin";

    RTBAutomationJobEnum(String pId, String pShortDescription, String pDescription,  Set pSupportedRoles) {

        id = pId;

        shortDescription = pShortDescription;

        description = pDescription;

        supportedRoles=pSupportedRoles;

    }

    public String getId() {

        return id;

    }


    public void setId(String pId) {

        id = pId;
    }

    public String getShortDescription() {

        return shortDescription;
    }


    public void setShortDescription(String pShortDescription) {

        shortDescription = pShortDescription;
    }


    public String getDescription() {

        return description;

    }


    public void setDescription(String pDescription) {

        description = pDescription;

    }

    public Set getSupportedRoles() {

        return supportedRoles;

    }

    public void setSupportedRoles(Set pSupportedRoles) {

        supportedRoles = pSupportedRoles;

    }

    @Override

    public String toString() {

        return shortDescription;

    }

    /**
     * This method can be used to add the roles supported by RTBAutomation jobs
     * @param jobId
     * @return supportedRoles
     */
    public static Set getSupportedRoles(String jobId){

        Set supportedRoles = new HashSet();

        supportedRoles.add(RTB_AUTOMATION_ROLE_ID);

        return supportedRoles;

    }

    /**
     * This method will allow or deny access to the automation job based on user role
     * @param roleIds
     * @return jobs which can be accessed by the given role
     */
    public static List<SAPRTBAutomationJob> getSAPRTBJobListForRole(List<String> roleIds) {

        List<SAPRTBAutomationJob> saprtbJobList = new ArrayList<SAPRTBAutomationJob>();

        for (RTBAutomationJobEnum rtbJob : values()) {

            for (String roleId : roleIds){

                if (rtbJob.getSupportedRoles().contains(roleId)){

                    saprtbJobList.add(new SAPRTBAutomationJob(rtbJob.getId(), rtbJob.getShortDescription(), rtbJob.getDescription()));

                    break;

                }

            }

        }

        return saprtbJobList;

    }
}
