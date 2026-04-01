package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model;

import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//cloned code from workers comp gateway
//going forward same class we will cleanup from workers comp gateway
public class PayrollDTO<T> {

    //private Payroll payroll;
    private T payroll;
    private Map<String, List<WorkersCompPaycheck>> includedPaychecksByCompany;

    public PayrollDTO() {
        includedPaychecksByCompany = new HashMap<String, List<WorkersCompPaycheck>>();
    }

    public PayrollDTO(T pPayroll) {
        this();
        payroll = pPayroll;
    }

    public T getPayroll() {
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
