package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.ActionEventCode;
import com.intuit.sbd.payroll.psp.domain.ActionType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 7, 2008
 * Time: 10:52:03 AM
 */
public class SAPActionEvent {

    private ActionEventCode actionEventCd;
	private String description;

    public ActionEventCode getActionEventCd() {
        return actionEventCd;
    }

    public void setActionEventCd(ActionEventCode actionEventCd) {
        this.actionEventCd = actionEventCd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }   
}
