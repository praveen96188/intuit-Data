package com.intuit.sbd.payroll.psp.processes.datamanager;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.VmpEmployeeInfo;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DGAuthBasedDeleteEmployeeProcessCore extends Process {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(DGAuthBasedDeleteEmployeeProcessCore.class);

    private String mId;
    private Employee mEmployee;
    private String workOrderId;
    private String workOrderCreatedTime;
    private VmpEmployeeInfo vmpEmployeeInfo;

    public DGAuthBasedDeleteEmployeeProcessCore(String mId, String workOrderId, String workOrderCreatedTime) {
        this.mId = mId;
        this.workOrderId = workOrderId;
        this.workOrderCreatedTime = workOrderCreatedTime;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Objects.isNull(mId)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Employee, mId, "mId");
            return validationResult;
        }

        mEmployee = Application.findById(Employee.class, SpcfUniqueId.createInstance(mId));

        if (Objects.isNull(mEmployee)) {
            vmpEmployeeInfo = Application.findById(VmpEmployeeInfo.class, SpcfUniqueId.createInstance(mId));
            if (Objects.isNull(vmpEmployeeInfo)) {
                validationResult.getMessages().NoEntityWithGivenId("Employee", mId);
                validationResult.getMessages().NoEntityWithGivenId("VmpEmployeeInfo", mId);
                return validationResult;
            }
        }

        if (Objects.isNull(workOrderId)) {
            logger.info("WorkOrderId found empty");
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        Map<EventDetailTypeCode, String> eventDetailsMap = new ConcurrentHashMap<>();
        eventDetailsMap.put(EventDetailTypeCode.WorkOrderId, Objects.isNull(workOrderId) ? "" : workOrderId);
        eventDetailsMap.put(EventDetailTypeCode.WorkOrderCreatedTime, Objects.isNull(workOrderCreatedTime) ? "" : workOrderCreatedTime);


        //detach CFR from employee and update IS_DG_DISASSOCIATED flag
        if (!Objects.isNull(mEmployee)) {
            mEmployee.setIsDgDisassociated(Boolean.TRUE);
            mEmployee.setConsumerRealmId(null);
            Application.save(mEmployee);

            // Create CompanyEvent with relevant details
            CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(mEmployee.getCompany(), EventTypeCode.DGDeleteRequest);
            eventDetailsMap.put(EventDetailTypeCode.Description, "Employee requested for deletion of their information");
            eventDetailsMap.put(EventDetailTypeCode.EmployeeSequence, mEmployee.getId().toString());
            eventDetailsMap.put(EventDetailTypeCode.EmployeeName, mEmployee.getFullName());

            eventDetailsMap.forEach((eventDetailTypeCode, value) -> companyEvent.addCompanyEventDetail(eventDetailTypeCode, value));

            processResult.setResult(mEmployee.getCompany().getIAMRealmId());
        } else {
            vmpEmployeeInfo.setConsumerRealmId(null);
            Application.save(vmpEmployeeInfo);

            // Create CompanyEvent with relevant details
            CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(vmpEmployeeInfo.getCompany(), EventTypeCode.DGDeleteRequest);
            eventDetailsMap.put(EventDetailTypeCode.EmployeeSequence, vmpEmployeeInfo.getId().toString());
            eventDetailsMap.put(EventDetailTypeCode.Description, "Employee with personaId=" + vmpEmployeeInfo.getPersonaId() + " and emailid=" + vmpEmployeeInfo.getEmail() + " requested for deletion of their information");
            eventDetailsMap.forEach((eventDetailTypeCode, value) -> companyEvent.addCompanyEventDetail(eventDetailTypeCode, value));

            processResult.setResult(vmpEmployeeInfo.getCompany().getIAMRealmId());
        }
        return processResult;
    }
}
