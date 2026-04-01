package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.LtEmployeeDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.LtPayeeDTO;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Apr 3, 2008
 * Time: 2:13:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class LtPayeeListWSDTO {
    public int numberOfPayees;
    public ArrayList<LtPayeeDTO> payee;
    public int status;

    public LtPayeeListWSDTO() {
        this.payee = new ArrayList<LtPayeeDTO>();
    }
}