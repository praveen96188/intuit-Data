package com.intuit.sbd.payroll.psp.mapper.guideline401k.payslips;

import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import lombok.Data;

@Data
public class PspPaycheckToV4EmployeePayslipMapperObject {
    private Paycheck paycheck;
    private SpcfMoney grossAmount;
    private SpcfDecimal grossYtdAmount;
}
