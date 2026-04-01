package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.LtEmployeeDTO;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Apr 3, 2008
 * Time: 2:13:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class LtEmployeeListWSDTO {
    public int numberOfEmployees;
    public ArrayList<LtEmployeeDTO> employee;
    public int status;

    public LtEmployeeListWSDTO() {
        this.employee = new ArrayList<LtEmployeeDTO>();
    }
}
