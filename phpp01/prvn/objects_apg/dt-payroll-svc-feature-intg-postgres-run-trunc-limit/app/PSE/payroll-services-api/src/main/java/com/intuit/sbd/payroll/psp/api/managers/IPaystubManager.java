package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaystubDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IPaystubManager {
    ProcessResult<Paystub> addPaystub(Paycheck pPaycheck, Employee pEmployee, PaystubDTO pPaystubDTO);
    ProcessResult<Paystub> updatePaystub(Paycheck pPaycheck, Employee pEmployee, Paystub pPaystub, PaystubDTO pPaystubDTO);
    ProcessResult<Paystub> deletePaystub(Company pCompany);
}
