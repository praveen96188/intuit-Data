package com.intuit.sbd.payroll.psp.gateways.wc.dto;

import com.intuit.bp.wc.common.schema.Payroll;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckPendingState;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Sriram Nutakki
 * Date created: 11/20/12
 */
public class PayrollDTO {

    private Payroll payroll;
    private Map<String, List<WorkersCompPaycheck>> includedPaychecksByCompany;

    public PayrollDTO() {
        includedPaychecksByCompany = new HashMap<String, List<WorkersCompPaycheck>>();
    }

    public PayrollDTO(Payroll pPayroll) {
        this();
        payroll = pPayroll;
    }

    public Payroll getPayroll() {
        return payroll;
    }

    public List<WorkersCompPaycheck> getIncludedPaychecks(String companyId) {
        return includedPaychecksByCompany.get(companyId);
    }

    public Map<String, List<WorkersCompPaycheck>> getIncludedPaychecksByCompany() {
        return includedPaychecksByCompany;
    }

    public void addIncludedPaycheck(String companyId, WorkersCompPaycheck paycheck) {
        List<WorkersCompPaycheck> paychecks = includedPaychecksByCompany.get(companyId);
        if (paychecks == null) {
            paychecks = new ArrayList<WorkersCompPaycheck>();
            includedPaychecksByCompany.put(companyId, paychecks);
        }
        paychecks.add(paycheck);
    }
}
