package com.intuit.sbd.payroll.psp.processes.datamanager;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.processes.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DGAuthBasedDeleteEmployerProcessCore extends Process{

    private static final Logger logger = LoggerFactory.getLogger(DGAuthBasedDeleteEmployerProcessCore.class);

    private String psid;
    private SourceSystemCode sourceSystemCode;
    private Company mCompany;
    private String workOrderId;
    private String workOrderCreatedTime;

    public DGAuthBasedDeleteEmployerProcessCore(String psid, SourceSystemCode sourceSystemCode,
                                                String workOrderId, String workOrderCreatedTime){
        this.psid = psid;
        this.sourceSystemCode = sourceSystemCode;
        this.workOrderId = workOrderId;
        this.workOrderCreatedTime = workOrderCreatedTime;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Objects.isNull(psid)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, psid,
                    "psid");
            return validationResult;
        }

        mCompany = Company.findCompany(psid,sourceSystemCode);

        if(Objects.isNull(mCompany)){
            logger.info("No company exists in PSP with psid {}", psid);
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, psid,
                    String.valueOf(sourceSystemCode), psid);
            return validationResult;
        }

        if (Objects.isNull(workOrderId)) {
            logger.info("WorkOrderId found empty");
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        Map<EventDetailTypeCode, String> eventDetailsMap = new HashMap<>();
        String realmId;

        logger.info("Starting disassociation for PSP_COMPANY table for psid {} and work order id {}"
                ,psid,workOrderId);

        eventDetailsMap.put(EventDetailTypeCode.WorkOrderId, Objects.isNull(workOrderId) ? "" : workOrderId);
        eventDetailsMap.put(EventDetailTypeCode.WorkOrderCreatedTime, Objects.isNull(workOrderCreatedTime) ? ""
                : workOrderCreatedTime);

        realmId = mCompany.getIAMRealmId();
        mCompany.setIsDgDisassociated(Boolean.TRUE);
        mCompany.setIAMRealmId(null);
        Application.save(mCompany);

        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.DGDeleteRequest);
        eventDetailsMap.put(EventDetailTypeCode.Description, "Employer requested for deletion of " +
                "the company information.");
        eventDetailsMap.put(EventDetailTypeCode.CompanySequence, mCompany.getId().toString());
        eventDetailsMap.put(EventDetailTypeCode.CompanyName, mCompany.getLegalName());
        eventDetailsMap.put(EventDetailTypeCode.DataRealmId, realmId);

        eventDetailsMap.forEach((eventDetailTypeCode, value) ->
                companyEvent.addCompanyEventDetail(eventDetailTypeCode, value));

        processResult.setResult(mCompany.getSourceCompanyId());

        return processResult;
    }
}
