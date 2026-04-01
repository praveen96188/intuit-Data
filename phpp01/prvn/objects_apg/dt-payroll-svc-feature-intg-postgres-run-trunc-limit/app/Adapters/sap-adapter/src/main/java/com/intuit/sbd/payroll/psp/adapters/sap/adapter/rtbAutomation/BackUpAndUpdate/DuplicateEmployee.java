package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate;

import com.google.gson.JsonArray;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.google.gson.JsonObject;
import org.hibernate.Hibernate;

/**
 * <h1>RTB Automation</h1>
 * This class is introduced as part of RTB Automation
 * It takes the backUp of the Employee data for Duplicate Employee Workflow and updates the corresponding Tables
 * @author  agupta43
 * @version 1.0
 * @since   2019-09-09
 */
public class DuplicateEmployee {

    private static final SpcfLogger logger = PayrollServices.getLogger(DuplicateEmployee.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    static QbdtEmployeeInfo qbdtEmployeeInfoOld = null;
    static Employee oldEmployee = null;
    static DomainEntitySet<Paycheck> oldPaycheckData=null;
    static Employee newEmployee=null;
    static String uniqueIDForIdentification;
    static int PAYCHECK_COUNT=50;
    public static void backUpDataDuplicateEmployee(String oldEmployeeId, String newEmployeeId, String psid, String creatorId) {

        logger.info("ServiceRequest: Starting to take backup Duplicate employee workflow for company:" + psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        oldEmployee=Employee.findEmployee(company, oldEmployeeId);
        qbdtEmployeeInfoOld = oldEmployee.getQbdtEmployeeInfo();

        JsonObject jsonlevel1=new JsonObject();
        newEmployee = Employee.findEmployee(company, newEmployeeId);

        oldPaycheckData = Paycheck.findPaychecksBySourceEmployee(oldEmployee.getCompany(), oldEmployee);
        JsonObject json = new JsonObject();
        json=DuplicateEmployee.createJSONDuplicateEmployee(qbdtEmployeeInfoOld,oldEmployee,oldPaycheckData);
        jsonlevel1.add("backupdata",json);

        //Inserting the record to backUp table

        RTBAUTOMATIONBACKUP rtbautomationbackup=new RTBAUTOMATIONBACKUP();
        String jSonString=null;
        if(!jsonlevel1.isJsonNull())
            jSonString =jsonlevel1.toString();
            rtbautomationbackup.setRtbBackup(jSonString);
            rtbautomationbackup.setCompanyId(psid);
            rtbautomationbackup.setEventType((RTBBackUpEventType.DUPLICATEEMPLOYEE));
            Application.save(rtbautomationbackup);
        String uniqueID=rtbautomationbackup.getId().toString();
        setUniqueIDForIdentification(uniqueID);

        logger.info("ServiceRequest: Completed taking backup of Employee and its related data for company:" + psid );


    }

    /*This method updates the data for Duplicate Employee
    It marks the Duplicated Employee to deleted state and updates it paycheck info to new employee
     */

    public static void updateDuplicateEmployeeData(String oldEmployeeId, String newEmployeeId, String psid, String creatorId) {
        logger.info("Begin: updateDuplicateEmployeeData");

        qbdtEmployeeInfoOld.setIsDeleted(true);
        qbdtEmployeeInfoOld.setModifiedDate(PSPDate.getPSPTime());
        qbdtEmployeeInfoOld.setModifierId(creatorId);
        Application.save(qbdtEmployeeInfoOld);

        logger.debug("updateDuplicateEmployeeData: PSP_QBDT_EMPLOYEE_INFO edited data saved successfully");
        for (Paycheck pc : oldPaycheckData) {
            pc.setSourceEmployee(newEmployee);
            Application.save(pc);
        }
        logger.debug("updateDuplicateEmployeeData: PSP_PAYCHECK edited data saved successfully");
        oldEmployee.setStatusCd(EmployeeStatus.Inactive);
        oldEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        oldEmployee.setCompany(null);
        oldEmployee.setSourceEmployeeId("RTB || " + oldEmployee.getSourceEmployeeId() + "||" + PSPDate.getPSPTime());
        Application.save(oldEmployee);
        qbdtEmployeeInfoOld.setToken(-1);
        Application.save(qbdtEmployeeInfoOld);

        logger.debug("updateDuplicateEmployeeData: PSP_EMPLOYEE edited data saved successfully");
        logger.info("End: updateDuplicateEmployeeData");
    }

    //getter and setter for unique Id to be used for record identification:sequence number
    public static void setUniqueIDForIdentification(String uniqueID){
        uniqueIDForIdentification=uniqueID;
    }

    public static String getUniqueIDForIdentification(){
        return uniqueIDForIdentification;
    }

    public static JsonObject createJSONDuplicateEmployee(QbdtEmployeeInfo qbdtEmployeeInfoOld, Employee oldEmployee, DomainEntitySet<Paycheck> oldPaycheckData){
        logger.info("Begin: createJSONDuplicateEmployee");

        //Creating JSOn objects and array to store elements in readable format
        JsonObject json = new JsonObject();
        JsonObject oldQBDTEmployee = new JsonObject();
        oldQBDTEmployee.addProperty("QBDT_EMPLOYEE_INFO_SEQ",qbdtEmployeeInfoOld.getId().toString());
        oldQBDTEmployee.addProperty("IS_DELETED",qbdtEmployeeInfoOld.getIsDeleted());
        oldQBDTEmployee.addProperty("TOKEN",qbdtEmployeeInfoOld.getToken());
        oldQBDTEmployee.addProperty("MODIFIER_ID",qbdtEmployeeInfoOld.getModifierId());
        oldQBDTEmployee.addProperty("MODIFIED_DATE",qbdtEmployeeInfoOld.getModifiedDate().toString());
        json.add("PSP_QBDT_EMPLOYEE_INFO",oldQBDTEmployee);
        JsonObject oldPSPEmployee = new JsonObject();
        oldPSPEmployee.addProperty("EMPLOYEE_SEQ",oldEmployee.getId().toString());
        oldPSPEmployee.addProperty("STATUS_CD",oldEmployee.getStatusCd().toString());
        oldPSPEmployee.addProperty("COMPANY_FK",oldEmployee.getCompany().getId().toString());
        oldPSPEmployee.addProperty("SOURCE_EMPLOYEE_ID",oldEmployee.getSourceEmployeeId());
        oldPSPEmployee.addProperty("STATUS_EFFECTIVE_DATE",oldEmployee.getStatusEffectiveDate().toString());
        oldPSPEmployee.addProperty("MODIFIER_ID",oldEmployee.getModifierId());
        oldPSPEmployee.addProperty("MODIFIED_DATE",oldEmployee.getModifiedDate().toString());
        json.add("PSP_EMPLOYEE",oldPSPEmployee);
        json.addProperty("ORIGINAL_PAYCHECK_COUNT", oldPaycheckData.size());
        int payCheckCount   =   0;
        JsonObject oldPSPPaycheck=null;
        JsonArray arrayElementPSPPaycheckOldEmployee=new JsonArray();
        for (Paycheck pc : oldPaycheckData) {
            if(payCheckCount>PAYCHECK_COUNT ){
                break;
            }
            payCheckCount = payCheckCount+1;
               oldPSPPaycheck = new JsonObject();
                oldPSPPaycheck.addProperty("PAYCHECK_SEQ", pc.getId().toString());
                if (pc.getSourceEmployee() != null)
                        oldPSPPaycheck.addProperty("SOURCE_EMPLOYEE_FK", pc.getSourceEmployee().getId().toString());
                if (pc.getDDEmployee() != null) {
                        oldPSPPaycheck.addProperty("DD_EMPLOYEE_DETAILS", pc.getDDEmployee().getId().toString());
                    }
                arrayElementPSPPaycheckOldEmployee.add(oldPSPPaycheck);
        }
        json.addProperty("SAVED_PAYCHECK_COUNT",payCheckCount);
        json.add("PSP_PAYCHECK",arrayElementPSPPaycheckOldEmployee);
        logger.info("End: createJSONDuplicateEmployee");
        return json;

    }

}
