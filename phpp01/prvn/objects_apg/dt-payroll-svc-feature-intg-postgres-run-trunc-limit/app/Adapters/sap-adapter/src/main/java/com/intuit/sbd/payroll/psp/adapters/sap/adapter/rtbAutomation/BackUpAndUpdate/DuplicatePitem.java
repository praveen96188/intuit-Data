package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate;

import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.RTBBackUpEventType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
//import jdk.management.resource.internal.AbstractPlainDatagramSocketImplRMHooks;
import org.hibernate.Hibernate;

/**
 * <h1>RTB Automation</h1>
 * This class is introduced as part of RTB Automation
 * It takes the backUp of the PItem data for Duplicate Pitem Workflow and updates the corresponding Tables
 * @author  agupta43
 * @version 1.0
 * @since   2019-13-11
 */
public class DuplicatePitem {

    private static final SpcfLogger logger = PayrollServices.getLogger(DuplicatePitem.class);

    static CompanyLaw oldCompanyLaw = null;
    static CompanyLaw newCompanyLaw=null;
    static CompanyPayrollItem oldCompanyPayrollItem = null;
    static CompanyPayrollItem newCompanyPayrollItem=null;
    static String uniqueIDForIdentification;
    static QbdtPayrollItemInfo qbdtPayrollItemInfoOld = null;
    static QbdtPayrollItemInfo qbdtPayrollItemInfoNew = null;
    public static void backUpDataDuplicatePitem(String oldPitemId, String newPitemId, String psid, String creatorId) {

        logger.info("ServiceRequest: Starting to take backup Duplicate Pitem workflow for company:" + psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        boolean SourceID=true;
        boolean LawID=true;
        oldCompanyLaw = CompanyLaw.findCompanyLawBySourceId(company, oldPitemId);
        newCompanyLaw = CompanyLaw.findCompanyLawBySourceId(company, newPitemId);
        oldCompanyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, oldPitemId);
        newCompanyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, newPitemId);
        JsonObject jsonlevel1=new JsonObject();
        JsonObject json = new JsonObject();
        if(oldCompanyLaw!=null)
        {
            json=DuplicatePitem.createJSONDuplicateTaxitem(oldCompanyLaw,newCompanyLaw);

        }else
        {
            json=DuplicatePitem.createJSONDuplicatePayrollitem(oldCompanyPayrollItem,newCompanyPayrollItem);
        }

        jsonlevel1.add("backupdata",json);

        //Inserting the record to backUp table

        RTBAUTOMATIONBACKUP rtbautomationbackup=new RTBAUTOMATIONBACKUP();
        String jSonString=null;
        if(!jsonlevel1.isJsonNull())
            jSonString =jsonlevel1.toString();
        rtbautomationbackup.setRtbBackup(jSonString);
        rtbautomationbackup.setCompanyId(psid);
        rtbautomationbackup.setEventType((RTBBackUpEventType.DUPLICATEPITEM));
        Application.save(rtbautomationbackup);
        String uniqueID=rtbautomationbackup.getId().toString();
        setUniqueIDForIdentification(uniqueID);

        logger.info("ServiceRequest: Completed taking backup of Employee and its related data for company:" + psid );


    }

    public static JsonObject createJSONDuplicateTaxitem(CompanyLaw oldCompanyLaw, CompanyLaw newCompanyLaw ){
        logger.info("Begin: createJSONDuplicateTaxitem");

        //Creating JSOn objects and array to store elements in readable format
        JsonObject json = new JsonObject();
        JsonObject oldCompanyLawJSON = new JsonObject();
        JsonObject newCompanyLawJSON = new JsonObject();
        if(oldCompanyLaw.getAdditionalCompanyLaw()!=null) {
            oldCompanyLawJSON.addProperty("ADDITIONAL_COMPANY_LAW_FK", oldCompanyLaw.getAdditionalCompanyLaw().getId().toString());
        }
        oldCompanyLawJSON.addProperty("MODIFIER_ID",oldCompanyLaw.getModifierId());
        oldCompanyLawJSON.addProperty("SOURCE_DESCRIPTION",oldCompanyLaw.getSourceDescription());
        oldCompanyLawJSON.addProperty("MODIFIED_DATE",oldCompanyLaw.getModifiedDate().toString());
        json.add("PSP_COMPANY_LAW",oldCompanyLawJSON);

        if(newCompanyLaw.getAdditionalCompanyLaw()!=null) {
            newCompanyLawJSON.addProperty("ADDITIONAL_COMPANY_LAW_FK", newCompanyLaw.getAdditionalCompanyLaw().getId().toString());
            json.add("PSP_COMPANY_LAW_NEW",newCompanyLawJSON);
        }

        JsonObject oldQBDTPayrollItemInfo = new JsonObject();
        oldQBDTPayrollItemInfo.addProperty("TOKEN",oldCompanyLaw.getQbdtPayrollItemInfo().getToken());
        oldQBDTPayrollItemInfo.addProperty("MODIFIER_ID",oldCompanyLaw.getQbdtPayrollItemInfo().getModifierId());
        oldQBDTPayrollItemInfo.addProperty("MODIFIED_DATE",oldCompanyLaw.getQbdtPayrollItemInfo().getModifiedDate().toString());
        oldQBDTPayrollItemInfo.addProperty("ISDeleted",oldCompanyLaw.getQbdtPayrollItemInfo().getIsDeleted());

        json.add("PSP_QBDT_PAYROLL_ITEM_INFO",oldQBDTPayrollItemInfo);

        logger.info("End: createJSONDuplicateTaxitem");
        return json;

    }
    public static JsonObject createJSONDuplicatePayrollitem(CompanyPayrollItem oldCompanyPayrollItem, CompanyPayrollItem newCompanyPayrollItem ){
        logger.info("Begin: createJSONDuplicatePitem");

        //Creating JSOn objects and array to store elements in readable format
        JsonObject json = new JsonObject();
        JsonObject oldCompanyPayrollItemJSON = new JsonObject();
        JsonObject newCompanyPayrollItemJSON = new JsonObject();
        if(oldCompanyPayrollItem.getAdditionalPayrollItem()!=null) {
            oldCompanyPayrollItemJSON.addProperty("ADDITIONAL_PAYROLL_ITEM_FK", oldCompanyPayrollItem.getAdditionalPayrollItem().getId().toString());
        }
        oldCompanyPayrollItemJSON.addProperty("MODIFIER_ID",oldCompanyPayrollItem.getModifierId());
        oldCompanyPayrollItemJSON.addProperty("MODIFIED_DATE",oldCompanyPayrollItem.getModifiedDate().toString());
        oldCompanyPayrollItemJSON.addProperty("SOURCE_DESCRIPTION",oldCompanyPayrollItem.getSourceDescription());

        json.add("PSP_PAYROLL_ITEM_INFO",oldCompanyPayrollItemJSON);

        if(newCompanyPayrollItem.getAdditionalPayrollItem()!=null) {
            newCompanyPayrollItemJSON.addProperty("ADDITIONAL_PAYROLL_ITEM_FK", newCompanyPayrollItem.getAdditionalPayrollItem().getId().toString());
            json.add("PSP_COMPANY_PAYROLL_ITEM_NEW",newCompanyPayrollItemJSON);
        }

        JsonObject oldQBDTPayrollItemInfo = new JsonObject();
        oldQBDTPayrollItemInfo.addProperty("TOKEN",oldCompanyPayrollItem.getQbdtPayrollItemInfo().getToken());
        oldQBDTPayrollItemInfo.addProperty("MODIFIER_ID",oldCompanyPayrollItem.getQbdtPayrollItemInfo().getModifierId());
        oldQBDTPayrollItemInfo.addProperty("MODIFIED_DATE",oldCompanyPayrollItem.getQbdtPayrollItemInfo().getModifiedDate().toString());
        oldQBDTPayrollItemInfo.addProperty("ISDeleted",oldCompanyPayrollItem.getQbdtPayrollItemInfo().getIsDeleted());

        json.add("PSP_QBDT_PAYROLL_ITEM_INFO",oldQBDTPayrollItemInfo);

        logger.info("End: createJSONDuplicatePitem");
        return json;

    }

    /*This method updates the data for Duplicate Pitem
    It marks the Duplicated Pitem to deleted state
     */

    public static void updateDuplicatemData(String oldPitemId, String newPitemId, String psid, String creatorId) {
        if(oldCompanyLaw!=null)
        {
            updateDuplicateTaxitemData(oldPitemId,newPitemId,psid,creatorId);

        }else
        {
            updateDuplicatePitemData(oldPitemId,newPitemId,psid,creatorId);

        }
    }


    public static void updateDuplicateTaxitemData(String oldPitemId, String newPitemId, String psid, String creatorId) {
        logger.info("Begin: updateDuplicateTaxitemData");

        oldCompanyLaw.setAdditionalCompanyLaw(newCompanyLaw);
        oldCompanyLaw.setStatus(PayrollItemStatus.Inactive);
        if(!(oldCompanyLaw.getSourceDescription().equals(newCompanyLaw.getSourceDescription())))
        {
            oldCompanyLaw.setSourceDescription(newCompanyLaw.getSourceDescription());

        }
        qbdtPayrollItemInfoOld=oldCompanyLaw.getQbdtPayrollItemInfo();
        qbdtPayrollItemInfoOld.setIsDeleted(true);
        qbdtPayrollItemInfoOld.setModifiedDate(PSPDate.getPSPTime());
        qbdtPayrollItemInfoOld.setModifierId(creatorId);
        qbdtPayrollItemInfoOld.setToken(-1);

        newCompanyLaw.setAdditionalCompanyLaw(null);

        Application.save(qbdtPayrollItemInfoOld);
        Application.save(oldCompanyLaw);
        Application.save(newCompanyLaw);

        logger.debug("updateDuplicateTaxitemData: PSP_COMPANY_LAW edited data saved successfully");
        logger.info("End: updateDuplicateTaxitemData");
    }
    public static void updateDuplicatePitemData(String oldPitemId, String newPitemId, String psid, String creatorId) {
        logger.info("Begin: updateDuplicatePitemData");

        oldCompanyPayrollItem.setAdditionalPayrollItem(newCompanyPayrollItem);
        oldCompanyPayrollItem.setStatus(PayrollItemStatus.Inactive);
        if(!(oldCompanyPayrollItem.getSourceDescription().equals(newCompanyPayrollItem.getSourceDescription())))
        {
            oldCompanyPayrollItem.setSourceDescription(newCompanyPayrollItem.getSourceDescription());

        }
        qbdtPayrollItemInfoOld=oldCompanyPayrollItem.getQbdtPayrollItemInfo();
        qbdtPayrollItemInfoOld.setIsDeleted(true);
        qbdtPayrollItemInfoOld.setModifiedDate(PSPDate.getPSPTime());
        qbdtPayrollItemInfoOld.setModifierId(creatorId);
        qbdtPayrollItemInfoOld.setToken(-2);

        newCompanyPayrollItem.setAdditionalPayrollItem(null);

        Application.save(qbdtPayrollItemInfoOld);
        Application.save(oldCompanyPayrollItem);
        Application.save(newCompanyPayrollItem);
        logger.debug("updateDuplicatePitemData: PSP_COMPANY_LAW edited data saved successfully");
        logger.info("End: updateDuplicatePitemData");
    }

    //getter and setter for unique Id to be used for record identification:sequence number
    public static void setUniqueIDForIdentification(String uniqueID){
        uniqueIDForIdentification=uniqueID;
    }

    public static String getUniqueIDForIdentification(){
        return uniqueIDForIdentification;
    }




}
