package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate;
import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.Hibernate;
/**
 * This class is used for fetching psp company table base on source Company id and update Realm id
 * ,Modifier date ,creator id
 * @author dkumar19
 */
public class VmpCheck {

    private static final SpcfLogger logger = PayrollServices.getLogger(VmpCheck.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    static String uniqueIDForIdentification;
    static Company company = null;

    /**
     * @param psid
     * @param creatorId
     * @return Void
     * Description = Update I_A_M_REALM_ID in PSP_COMPANY table base on  psid ,creatorId param
     */

    public static void updateVMPCheckData(final String psid, final String creatorId) {
        logger.info("Begin updateVMPCheckData: ");
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company.getIAMRealmId() != null) {
            company.setIAMRealmId(null);
            company.setModifiedDate(PSPDate.getPSPTime());
            company.setModifierId(creatorId);
            Application.save(company);
            logger.info(" Update VMP data successfully in DB : ");
        }
        logger.info("End updateVMPCheckData : ");
    }
    /**
     * @param psid
     * @param creatorId
     * @return Void
     * Description = Back Data for VMP service using psid ,creatorId param
     */
    public static void backUpVMPServiceData(final String psid, final String creatorId) {

        logger.info("ServiceRequest: Starting to take backup VMP service Data:" + psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        JsonObject json = new JsonObject();
        JsonObject jsonResponse = new JsonObject();
        json=VmpCheck.createJSONVmpServiceData(company);
        jsonResponse.add("backupdata",json);

        //Inserting the record to backUp table

        RTBAUTOMATIONBACKUP rtbautomationbackup=new RTBAUTOMATIONBACKUP();
        String jSonString=null;
        if(!json.isJsonNull())
            jSonString =json.toString();
        rtbautomationbackup.setRtbBackup(jSonString);
        rtbautomationbackup.setCompanyId(psid);
        rtbautomationbackup.setEventType((RTBBackUpEventType.VMPSERVICEEVENT));
        Application.save(rtbautomationbackup);
        String uniqueID=rtbautomationbackup.getId().toString();
        setUniqueIDForIdentification(uniqueID);
        String eventTypeConfig =  BatchUtils.getConfigString("psp_jss_rtbautomation_eventtypes");
        logger.info("End:  eventTypeConfig service"+eventTypeConfig);
        logger.info("ServiceRequest: Completed taking backup of VMP service Data:" + psid );
    }
    /**
     * @param company
     * @return JsonObject Object
     * Description = Create Json Object Response using Company param
     */

    public static JsonObject createJSONVmpServiceData(final Company company){
        logger.info("Begin: createJSONVmpServiceData");
        //Creating JSOn objects and array to store elements in readable format
        JsonObject json = new JsonObject();
        JsonObject jsonRealmBackData = new JsonObject();
        jsonRealmBackData.addProperty("COMPANY_SEQ",company.getId().toString());
        jsonRealmBackData.addProperty("MODIFIER_ID",company.getModifierId());
        jsonRealmBackData.addProperty("I_A_M_REALM_ID",company.getIAMRealmId());
        jsonRealmBackData.addProperty("SOURCE_COMPANY_ID",company.getSourceCompanyId());
        jsonRealmBackData.addProperty("CREATOR_ID",company.getCreatorId());
        jsonRealmBackData.addProperty("LEGAL_NAME",company.getLegalName());
        jsonRealmBackData.addProperty("MODIFIED_DATE",company.getModifiedDate().toString());
        json.add("PSP_COMPANY",jsonRealmBackData);
        logger.info("End: createJSONVmpServiceData");
        return jsonRealmBackData;
    }
    //getter and setter for unique Id to be used for record identification:sequence number
    public static void setUniqueIDForIdentification(String uniqueID) {
        uniqueIDForIdentification = uniqueID;
    }

    public static String getUniqueIDForIdentification() {
        return uniqueIDForIdentification;
    }

}