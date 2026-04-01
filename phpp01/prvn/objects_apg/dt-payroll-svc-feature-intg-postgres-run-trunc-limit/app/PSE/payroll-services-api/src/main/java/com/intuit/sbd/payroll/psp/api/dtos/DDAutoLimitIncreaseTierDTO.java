package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;


/**
 * Created by IntelliJ IDEA.
 * User: rnorian
 * Date: Sep 14, 2009
 * Time: 12:17:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DDAutoLimitIncreaseTierDTO {
    private String level;
    private String sourceSystemCd;
    private String payrollsRun;
    private String daysSinceFirstPayroll;
    private String increaseMultiplier;
    private String companyCap;
    private String employeeCap;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getPayrollsRun() {
        return payrollsRun;
    }

    public void setPayrollsRun(String payrollsRun) {
        this.payrollsRun = payrollsRun;
    }

    public String getDaysSinceFirstPayroll() {
        return daysSinceFirstPayroll;
    }

    public void setDaysSinceFirstPayroll(String daysSinceFirstPayroll) {
        this.daysSinceFirstPayroll = daysSinceFirstPayroll;
    }

    public String getIncreaseMultiplier() {
        return increaseMultiplier;
    }

    public void setIncreaseMultiplier(String increaseMultiplier) {
        this.increaseMultiplier = increaseMultiplier;
    }

    public String getCompanyCap() {
        return companyCap;
    }

    public void setCompanyCap(String companyCap) {
        this.companyCap = companyCap;
    }

    public String getEmployeeCap() {
        return employeeCap;
    }

    public void setEmployeeCap(String employeeCap) {
        this.employeeCap = employeeCap;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (level == null || !Validator.isNonNegativeInteger(level)) {
            validationResult.getMessages().InvalidArgumentType(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "level", "non-negative integer");
        }

        if (daysSinceFirstPayroll == null || !Validator.isNonNegativeInteger(daysSinceFirstPayroll)) {
            validationResult.getMessages().InvalidArgumentType(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "daysSinceFirstPayroll", "non-negative integer");
        }

        if (increaseMultiplier == null || !Validator.isDouble(increaseMultiplier)) {
            validationResult.getMessages().InvalidArgumentType(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "increaseMultiplier", "double");
        }

        if (companyCap == null || !Validator.isNonNegativeInteger(companyCap)) {
            validationResult.getMessages().InvalidArgumentType(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "companyCap", "non-negative integer");
        }

        if (employeeCap == null || !Validator.isNonNegativeInteger(employeeCap)) {
            validationResult.getMessages().InvalidArgumentType(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "employeeCap", "non-negative integer");
        }

        if (Integer.parseInt(employeeCap) > Integer.parseInt(companyCap)) {
            validationResult.getMessages().InvalidArgument(EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "employeeCap must be less than companCap");
        }

        return validationResult;
    }
}
