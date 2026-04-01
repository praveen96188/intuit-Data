package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model;

import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PayrollDtoCompanyFileInfo {

    private PayrollDTO payrollDTO;
    private String fileName;

}
