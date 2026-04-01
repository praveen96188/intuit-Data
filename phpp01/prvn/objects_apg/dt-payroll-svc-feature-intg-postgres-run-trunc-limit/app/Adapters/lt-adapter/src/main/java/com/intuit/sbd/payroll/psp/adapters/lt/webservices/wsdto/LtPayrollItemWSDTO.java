package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.LtPayrollItemDTO;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: May 3, 2011
 * Time: 10:36:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class LtPayrollItemWSDTO {

    public int PayrollItemCount;
    public ArrayList<LtPayrollItemDTO> payrollItem;
    public int status;
    public String message;

    public LtPayrollItemWSDTO() {
        this.payrollItem = new ArrayList<LtPayrollItemDTO>();
    }
}
