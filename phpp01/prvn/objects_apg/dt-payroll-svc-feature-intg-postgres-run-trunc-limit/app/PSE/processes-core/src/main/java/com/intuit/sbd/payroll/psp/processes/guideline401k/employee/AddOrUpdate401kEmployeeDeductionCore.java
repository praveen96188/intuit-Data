package com.intuit.sbd.payroll.psp.processes.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Decimal;
import com.intuit.v4.common.Rate;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employer.EmployerDeduction;

import java.util.Objects;

public class AddOrUpdate401kEmployeeDeductionCore extends Process {

    private final EmployeeDeduction employeeDeduction;

    private Hcm401kEmployeeDeduction hcm401kEmployeeDeduction;

    public AddOrUpdate401kEmployeeDeductionCore(EmployeeDeduction employeeDeduction) {
        this.employeeDeduction = employeeDeduction;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if(Objects.isNull(employeeDeduction)){
            validationResult.getMessages()
                    .BadProcessArgument("EmployeeDeduction");
            return validationResult;
        }

        if(employeeDeduction.isEmployeeSet()){
            if(!employeeDeduction.getEmployee().isIdSet()){
                validationResult.getMessages()
                        .BadProcessArgument("EmployeeId");
                return validationResult;
            }
        }

        if(employeeDeduction.isEmployerDeductionSet()) {
            if(!employeeDeduction.getEmployerDeduction().isIdSet()){
                validationResult.getMessages()
                        .BadProcessArgument("EmployerId");
                return validationResult;
            }
        }
        if(employeeDeduction.isIdSet()) {             //update call
            hcm401kEmployeeDeduction = ApplicationSecondary.findById(Hcm401kEmployeeDeduction.class,
                    SpcfUniqueId.createInstance(employeeDeduction.getId().getLocalId()));
        } else {                                      //create call
            hcm401kEmployeeDeduction = new Hcm401kEmployeeDeduction();
        }
        if(Objects.isNull(hcm401kEmployeeDeduction)) {
            validationResult.getMessages().
                    BadProcessArgument("hcm401kEmployeeDeduction");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process(){

        setDeductionEntity();
        ApplicationSecondary.save(hcm401kEmployeeDeduction);

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(getEmployeeDeductionFromDb());
        return processResult;
    }

    private void setDeductionEntity() {
        if(employeeDeduction.isEmployeeSet()) {
                hcm401kEmployeeDeduction.setEmployeeId(employeeDeduction.getEmployee().getId().getLocalId());
        }

        if(employeeDeduction.isActiveSet()) {
            hcm401kEmployeeDeduction.setActive(employeeDeduction.isActive());
        }

        if(employeeDeduction.isAmountSet()){
            if(employeeDeduction.getAmount().isPercent()) {
                hcm401kEmployeeDeduction.setAmount(employeeDeduction.getAmount().getPercentValue().doubleValue());
                hcm401kEmployeeDeduction.setHcm401kAmountType(Hcm401kAmountType.Percentage);
            } else {
                hcm401kEmployeeDeduction.setAmount(employeeDeduction.getAmount().getMoneyValue().doubleValue());
                hcm401kEmployeeDeduction.setHcm401kAmountType(Hcm401kAmountType.Dollar);
            }
        }

        hcm401kEmployeeDeduction.setHcm401kDeductionContributor(Hcm401kDeductionContributor.Employee);
        if(employeeDeduction.isEmployerDeductionSet()) {
            Hcm401kCompanyPolicy hcm401kCompanyPolicy = ApplicationSecondary.findById(Hcm401kCompanyPolicy.class,
                            SpcfUniqueId.createInstance(employeeDeduction.getEmployerDeduction().getId().getLocalId()));
            hcm401kEmployeeDeduction.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        }

        if(employeeDeduction.isAnnualMaximumSet()){
            hcm401kEmployeeDeduction.setMaxAmount(employeeDeduction.getAnnualMaximum().doubleValue());
        }
    }

    private EmployeeDeduction getEmployeeDeductionFromDb() {
        EmployeeDeduction employeeDeductionFromDB = new EmployeeDeduction();
        employeeDeductionFromDB.setId(GlobalId.create("",hcm401kEmployeeDeduction.getId().toString()));
        employeeDeductionFromDB.setActive(hcm401kEmployeeDeduction.getActive());

        Rate rate = new Rate();
        rate.setPercent(hcm401kEmployeeDeduction.getHcm401kAmountType().equals(Hcm401kAmountType.Percentage));
        if(rate.isPercent()){
            rate.setPercentValue(Decimal.valueOf(hcm401kEmployeeDeduction.getAmount()));
        }else {
            rate.setMoneyValue(Amount.valueOf(hcm401kEmployeeDeduction.getAmount()));
        }

        employeeDeductionFromDB.setAmount(rate);
        employeeDeductionFromDB.setAnnualMaximum(Amount.valueOf(hcm401kEmployeeDeduction.getMaxAmount()));

        EmployerDeduction employerDeduction = new EmployerDeduction();
        Hcm401kCompanyPolicy hcm401kCompanyPolicy = hcm401kEmployeeDeduction.getHcm401kCompanyPolicy();
        employerDeduction.setId(GlobalId.create("", hcm401kCompanyPolicy.getId().toString()));
        employerDeduction.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy(), DeductionItemProvider.Guideline).getDescription());
        employerDeduction.setStatutoryDeductionPolicy(hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy().toString());

        employeeDeductionFromDB.setEmployerDeduction(employerDeduction);
        return employeeDeductionFromDB;
    }
}