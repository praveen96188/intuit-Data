package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class ActionEvent extends BaseActionEvent {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ActionEvent()
	{
		super();
	}

    public LedgerAccountAction getLedgerAccountAction() {
        LedgerAccountAction foundAction = null;
        DomainEntitySet<LedgerAccountAction> ledgerAccountAction = Application.find(LedgerAccountAction.class, LedgerAccountAction.ActionEvent().equalTo(this));

        if (ledgerAccountAction.size() != 1) {
            throw new RuntimeException(
                    "Query for ledger account action by ledger action " + this + " did not return 0 or 1 results as expected");
        }

        if (ledgerAccountAction.size() != 0) {
            foundAction = ledgerAccountAction.get(0);
        }
        return foundAction;
    }

    public static DomainEntitySet<ActionEvent> getAllPayrollActionEvents() {
        return Application.find(ActionEvent.class, ActionEvent.Type().equalTo(ActionType.PayrollRun));
    }
}