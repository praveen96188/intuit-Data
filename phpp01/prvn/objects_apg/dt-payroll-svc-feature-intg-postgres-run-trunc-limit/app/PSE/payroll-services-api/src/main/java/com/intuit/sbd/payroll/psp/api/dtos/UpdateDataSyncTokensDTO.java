package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.Collection;
import java.util.HashSet;

/**
 * User: dweinberg
 * Date: 11/1/11
 * Time: 10:15 AM
 */
public class UpdateDataSyncTokensDTO {
    public static enum Action {
        Push, Stop
    }

    private Action action;
    private boolean undelete;
    private String comment;

    private Collection<SpcfUniqueId> employees;   //employee GUID
    private Collection<SpcfUniqueId> payrollItems; //QBDTPayrollItemInfo GUID
    private Collection<SpcfUniqueId> paychecks; //paycheck GUID
    private Collection<SpcfUniqueId> priorPaymentsAndRefunds; //PriorPaymentSubmission GUID
    private Collection<SpcfUniqueId> liabilityAdjustments; //CompanyAdjustmentSubmission GUID
    private Collection<SpcfUniqueId> liabilityChecks; //LiabilityCheck GUID
    private Collection<SpcfUniqueId> qbdtOnlyPayrollTransactions; //QbdtPayrollTransaction GUID

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (action == null) {
            validationResult.getMessages().InvalidValue(EntityName.DTO, null, "action");
        }

        if (action != Action.Push && undelete) {
            validationResult.getMessages().InvalidValue(EntityName.DTO, null, "undelete");
        }

        if (comment == null ) {
            validationResult.getMessages().InvalidValue(EntityName.DTO, null, "comment");
        }

        if (employees == null ) {
            employees = new HashSet<SpcfUniqueId>();
        }
        if (payrollItems == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }
        if (paychecks == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }
        if (priorPaymentsAndRefunds == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }
        if (liabilityAdjustments == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }
        if (liabilityChecks == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }
        if (qbdtOnlyPayrollTransactions == null) {
            payrollItems = new HashSet<SpcfUniqueId>();
        }

        return validationResult;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean getUndelete() {
        return undelete;
    }

    public void setUndelete(boolean pUndelete) {
        undelete = pUndelete;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Collection<SpcfUniqueId> getEmployees() {
        return employees;
    }

    public void setEmployees(Collection<SpcfUniqueId> employees) {
        this.employees = employees;
    }

    public Collection<SpcfUniqueId> getPayrollItems() {
        return payrollItems;
    }

    public void setPayrollItems(Collection<SpcfUniqueId> payrollItems) {
        this.payrollItems = payrollItems;
    }

    public Collection<SpcfUniqueId> getPaychecks() {
        return paychecks;
    }

    public void setPaychecks(Collection<SpcfUniqueId> paychecks) {
        this.paychecks = paychecks;
    }

    public Collection<SpcfUniqueId> getPriorPaymentsAndRefunds() {
        return priorPaymentsAndRefunds;
    }

    public void setPriorPaymentsAndRefunds(Collection<SpcfUniqueId> priorPaymentsAndRefunds) {
        this.priorPaymentsAndRefunds = priorPaymentsAndRefunds;
    }

    public Collection<SpcfUniqueId> getLiabilityAdjustments() {
        return liabilityAdjustments;
    }

    public void setLiabilityAdjustments(Collection<SpcfUniqueId> liabilityAdjustments) {
        this.liabilityAdjustments = liabilityAdjustments;
    }

    public Collection<SpcfUniqueId> getLiabilityChecks() {
        return liabilityChecks;
    }

    public void setLiabilityChecks(Collection<SpcfUniqueId> liabilityChecks) {
        this.liabilityChecks = liabilityChecks;
    }

    public Collection<SpcfUniqueId> getQbdtOnlyPayrollTransactions() {
        return qbdtOnlyPayrollTransactions;
    }

    public void setQbdtOnlyPayrollTransactions(Collection<SpcfUniqueId> qbdtOnlyPayrollTransactions) {
        this.qbdtOnlyPayrollTransactions = qbdtOnlyPayrollTransactions;
    }
}
