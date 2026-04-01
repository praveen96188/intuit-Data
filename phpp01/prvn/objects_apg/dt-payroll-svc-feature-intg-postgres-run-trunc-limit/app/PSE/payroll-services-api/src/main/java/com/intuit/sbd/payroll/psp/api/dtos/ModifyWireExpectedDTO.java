package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.CollectionStage;
import com.intuit.sbd.payroll.psp.domain.PayrollRunAction;
import com.intuit.sbd.payroll.psp.domain.ActionEventCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 2, 2008
 * Time: 9:16:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyWireExpectedDTO {

    private String sourcePayrollRunId;
    private DateDTO wireExpectedDate;
    private CollectionStage collectionStage;
    private ActionEventCode actionEventCode;
    private Boolean lastChanceEmail = false;
    
    public ModifyWireExpectedDTO() {
        super();
    }

    public ModifyWireExpectedDTO(String pSourcePayrollRunId, DateDTO pWireExpectedDate,
                                 CollectionStage pCollectionStage, ActionEventCode pActionEvent,
                                 Boolean pLastChanceEmail) {
        sourcePayrollRunId = pSourcePayrollRunId;
        wireExpectedDate = pWireExpectedDate;
        collectionStage = pCollectionStage;
        actionEventCode = pActionEvent;
        lastChanceEmail = pLastChanceEmail;
    }

    public Boolean getLastChanceEmail() {
        return lastChanceEmail;
    }

    public void setLastChanceEmail(Boolean lastChanceEmail) {
        this.lastChanceEmail = lastChanceEmail;
    }

    public String getSourcePayrollRunId() {
        return sourcePayrollRunId;
    }

    public void setSourcePayrollRunId(String sourcePayrollRunId) {
        this.sourcePayrollRunId = sourcePayrollRunId;
    }

    public DateDTO getWireExpectedDate() {
        return wireExpectedDate;
    }

    public void setWireExpectedDate(DateDTO wireExpectedDate) {
        this.wireExpectedDate = wireExpectedDate;
    }

    public CollectionStage getCollectionStage() {
        return collectionStage;
    }

    public void setCollectionStage(CollectionStage collectionStage) {
        this.collectionStage = collectionStage;
    }

    public ActionEventCode getActionEventCode() {
        return actionEventCode;
    }

    public void setActionEventCode(ActionEventCode actionEventCode) {
        this.actionEventCode = actionEventCode;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollRunId == null || sourcePayrollRunId.length() <= 0) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, null, "SourcePayrollRunId");
        }

        if (collectionStage == null) {
            validationResult.getMessages().InvalidValue(EntityName.CollectionStage, null, "CollectionStage");
        }

        if (actionEventCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.ActionEvent, null, "ActionEventCode");
        }

        if (wireExpectedDate == null) {
            validationResult.getMessages().InvalidValue(EntityName.Date, null, "WireExpectedDate");    
        } else {
            validationResult.merge(wireExpectedDate.validate());
        }

        return validationResult;
    }
}
