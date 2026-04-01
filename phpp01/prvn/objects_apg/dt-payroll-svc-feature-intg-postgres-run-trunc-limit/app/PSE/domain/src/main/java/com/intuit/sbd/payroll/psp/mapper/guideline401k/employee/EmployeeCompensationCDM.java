package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import lombok.Data;

@Data
public class EmployeeCompensationCDM {

    private SpcfUniqueId employeePItemSeq;
    private SpcfMoney amount;
    private SpcfUniqueId companyPItemSeq;
    private PayrollItemStatus pItemStatus;
    private PayrollItemCode pItemCode;
    private double overtime;
}
