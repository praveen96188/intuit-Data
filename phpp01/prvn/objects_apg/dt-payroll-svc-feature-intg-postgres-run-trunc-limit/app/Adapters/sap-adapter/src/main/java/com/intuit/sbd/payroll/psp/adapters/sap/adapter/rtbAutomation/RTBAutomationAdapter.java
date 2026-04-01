package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate.DuplicateEmployee;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.utils.RTBAutomationUtils;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyUnprocessedRequest;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBAutomationJob;
import com.intuit.sbd.payroll.psp.adapters.sap.rtbAutomation.RTBAutomationJobEnum;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.BackUpAndUpdate.DuplicatePitem;
import com.intuit.sbd.payroll.psp.adapters.sap.rtbAutomation.SelectionEnum;
import com.intuit.sbd.payroll.psp.domain.QbdtRequestStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by smodgil on 2/08/19.
 * This class fetches data from Backend and pass it on to UI
 */

public class RTBAutomationAdapter {


    private static final SpcfLogger logger = PayrollServices.getLogger(RTBAutomationAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private static final String message = "Marked processed via RTB tool";




    /*
    The flow from SAP comes here and it triggers the Backup and update for Duplicate Employee
     */
    @FlexMethod
    public void duplicateEmployee(String oldEmployeeId, String newEmployeeId, String psid) throws Throwable {

        logger.info("Begin: duplicateEmployee");
        try{
            String creatorId=null;
            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
            if(principal !=null){
                creatorId=principal.getName();
            }
            AuthUser user = AuthUser.findUser(principal.getId());
            Application.beginUnitOfWork();
            logger.info("ServiceRequest: Calling backUpDataDuplicateEmployee with old employee id: "+oldEmployeeId+" and new employee id: "+newEmployeeId +" and psid: "+psid);

            //Calling Method to take to take backUp of Duplicate Employee Data
            DuplicateEmployee.backUpDataDuplicateEmployee(oldEmployeeId,newEmployeeId,psid,creatorId);
            String uniqueId=DuplicateEmployee.getUniqueIDForIdentification();
            String eventType=(RTBBackUpEventType.DUPLICATEEMPLOYEE).toString();
            String fName=user.getFirstName();
            String lName=user.getLastName();

            DuplicateEmployee.updateDuplicateEmployeeData(oldEmployeeId,newEmployeeId,psid,creatorId);

            Application.commitUnitOfWork();

            logger.info("duplicateEmployee: All edited data saved successfully in database");
  

            BatchUtils.sendRTBUniqueIdentificationEmail(eventType,psid,creatorId,uniqueId,fName,lName);
            logger.info("End: duplicateEmployee");
        }catch (Throwable t) {
            logger.error("Failed to update the duplicate employee with exception:"+t.getCause());
            String exception =RTBAutomationUtils.handleRTBAutomationException(t);
            aeFactory.throwGenericException(exception);
        }finally {
            Application.rollbackUnitOfWork();
        }


    }

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

    @FlexMethod
    @Operation(operationIds = OperationId.ExecuteRTBAutomationJob)
    public List<SAPRTBAutomationJob> getAutomationJobEnum() throws Throwable {
        List<SAPRTBAutomationJob> saprtbJobs = null;
        try {
            List<String> roles = getRoles();
            saprtbJobs = SelectionEnum.getSAPRTBJobListForRole(roles);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding RTBJobs jobs.", t);
        }
        return saprtbJobs;
    }

    /*
        The method fetches the employee name based on companyId and employeeId
    */
    @FlexMethod
    public String findEmployeeName(String companyId,String employeeId) throws Throwable {

        String employeeName = null;
        DomainEntitySet<Company> companyList = null;

        logger.info("RTBAutomationAdapter: findEmployeeName starts");

        try {
            companyList=getCompanyList(companyId);
            if(companyList !=null && !companyList.isEmpty()){
                DomainEntitySet<Employee> employees = Application.find(Employee.class, Employee.Company().equalTo(companyList.get(0)).And(Employee.SourceEmployeeId().equalTo(employeeId)));
                if(employees !=null && employees.size()!=0){
                    employeeName = employees.get(0).getFirstName().concat(" ").concat(employees.get(0).getLastName());
                }else{
                    employeeName="Employee doesn't exist";
                }
            }
        } catch (Throwable t) {

            aeFactory.throwGenericException("Error finding Employee", t.getCause());

        }
        logger.info("RTBAutomationAdapter: findEmployeeName ends");

        return employeeName;

    }

    /*
        The method fetches the company name based on companyId
    */
    @FlexMethod
    public String findCompanyName(String companyId) throws Throwable {

        String companyName = null;
        DomainEntitySet<Company> companyList = null;

        logger.info("RTBAutomationAdapter: findCompanyName starts");

        try {
            companyList=getCompanyList(companyId);
            if(companyList!=null && companyList.size()>0){
                for(Company company:companyList){
                    companyName = company.getLegalName();
                }
            }else{
                companyName="Company doesn't exist";
            }

        } catch (Throwable t) {

            aeFactory.throwGenericException("Error finding Company name", t.getCause());

        }
        logger.info("RTBAutomationAdapter: findCompanyName ends");

        return companyName;

    }
    /*
        The method fetches the company list based on companyId
    */
    private DomainEntitySet<Company> getCompanyList(String companyId){
        DomainEntitySet<Company> companyList = null;
        PayrollServices.beginUnitOfWork();
        companyList =Application.find(Company.class,new Query<Company>().Where(Company.SourceCompanyId().equalTo(companyId)));
        PayrollServices.commitUnitOfWork();
        return companyList;
    }
    /**
     * get the list of roles for logged in User
     *
     * @return
     */

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
    //Added for Duplicate Pitem Automation

    /*
        The method fetches the PItem name based on companyId and PItemID
    */
    @FlexMethod
    public String findPitemName(String companyId,String pitemId) throws Throwable {

        String pitemName = null;
        DomainEntitySet<Company> companyList = null;

        logger.info("RTBAutomationAdapter: findPitemName starts");

        try {
            companyList=getCompanyList(companyId);
            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
            CompanyLaw companyLaw=CompanyLaw.findCompanyLawBySourceId(company,pitemId);
            if(companyLaw!=null)
            {
                pitemName=companyLaw.getSourceDescription();
                pitemName=pitemName + ": " + "Its an TaxItem";
            }else if(companyLaw==null)
            {
                CompanyPayrollItem companyPayrollItem=CompanyPayrollItem.findItemForSourcePayrollItemId(company,pitemId);
                if(companyPayrollItem!=null)
                {
                    pitemName=companyPayrollItem.getSourceDescription();
                    pitemName=pitemName + ": " + "Its an PayrollItem";
                }
            }


            if(pitemName==null || pitemName=="") {
                pitemName = "PItem doesn't exist";
            }

        } catch (Throwable t) {

            aeFactory.throwGenericException("Error finding PItem", t.getCause());

        }
        logger.info("RTBAutomationAdapter: findPitemName ends");

        return pitemName;

    }


    /*
    The flow from SAP comes here and it triggers the Backup and update for Duplicate Pitem
     */
    @FlexMethod
    public void duplicatePitem(String oldPitemId, String newPitemId, String psid) throws Throwable {

        logger.info("Begin: duplicatePitem");
        try{
            String creatorId=null;
            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
            if(principal !=null){
                creatorId=principal.getName();
            }
            AuthUser user = AuthUser.findUser(principal.getId());
            Application.beginUnitOfWork();
            logger.info("ServiceRequest: Calling backUpDataDuplicatePitem with old Pitem id: "+oldPitemId+" and new PItem id: "+newPitemId +" and psid: "+psid);

            //Calling Method to take to take backUp of Duplicate Pitem Data
            DuplicatePitem.backUpDataDuplicatePitem(oldPitemId,newPitemId,psid,creatorId);


            String uniqueId=DuplicatePitem.getUniqueIDForIdentification();
            String eventType=(RTBBackUpEventType.DUPLICATEPITEM).toString();
            String fName=user.getFirstName();
            String lName=user.getLastName();

            DuplicatePitem.updateDuplicatemData(oldPitemId,newPitemId,psid,creatorId);

            Application.commitUnitOfWork();
            logger.info("duplicatePitem: All edited data saved successfully in database");


            BatchUtils.sendRTBUniqueIdentificationEmail(eventType,psid,creatorId,uniqueId,fName,lName);
            logger.info("End: duplicatePitem");
        }catch (Throwable t) {
            logger.error("Failed to update the duplicate pitems with exception:"+t.getCause());
            String exception =RTBAutomationUtils.handleRTBAutomationException(t);
            aeFactory.throwGenericException(exception);
        }finally {
            Application.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    public void processUnprocessedRequests(String psid) throws Throwable {
        try {

            Application.beginUnitOfWork();
            Company mCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (mCompany == null) {
                aeFactory.throwGenericException(String.format("Company %s does not exist", psid));
            }
            QbdtRequestStatus[] requestStatuses = new QbdtRequestStatus[]{QbdtRequestStatus.Error, QbdtRequestStatus.Processing};
            QbdtUnprocessedRequest.clearUnprocessedRequest(mCompany, message, requestStatuses);
            Application.commitUnitOfWork();

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error clearing unprocessed requests", "QBDT", psid, t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPCompanyUnprocessedRequest findCompanyNameAndUnprocessedRequest(String psid) throws Throwable {
        logger.info("RTBAutomationAdapter: findcompanyAndUnprocessedRequest start: " + psid);
        SAPCompanyUnprocessedRequest companyUnprocessedRequest = new SAPCompanyUnprocessedRequest();
        try {
            Company mCompany = Company.findCompany(psid, SourceSystemCode.QBDT);

            if (mCompany != null) {
                QbdtRequestStatus[] requestStatuses = new QbdtRequestStatus[]{QbdtRequestStatus.Error, QbdtRequestStatus.Processing};
                DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests = QbdtUnprocessedRequest.findUnprocessedRequests(mCompany, false, requestStatuses);
                companyUnprocessedRequest.setCompanyLegalName(mCompany.getLegalName());
                companyUnprocessedRequest.setRequestCount(qbdtUnprocessedRequests.size());
            } else {
                companyUnprocessedRequest = null;
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Company detail", t);
        }
        logger.info("RTBAutomationAdapter: companyAndUnprocessedRequest ends");

        return companyUnprocessedRequest;
    }
}



