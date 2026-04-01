package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.dtos.PaystubDTO;
import com.intuit.sbd.payroll.psp.api.managers.IPaystubManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.Paystub;
import com.intuit.sbd.payroll.psp.processes.*;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaystubManager implements IPaystubManager {
    public ProcessResult<Paystub> addPaystub(Paycheck pPaycheck, Employee pEmployee, PaystubDTO pPaystubDTO) {
        IProcess processCore = new AddPaystubCore(pPaycheck, pEmployee, pPaystubDTO);
        ProcessResult<Paystub> processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<Paystub> updatePaystub(Paycheck pPaycheck, Employee pEmployee, Paystub pPaystub, PaystubDTO pPaystubDTO) {
        IProcess processCore = new UpdatePaystubCore(pPaycheck, pEmployee, pPaystub, pPaystubDTO);
        ProcessResult<Paystub> processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<Paystub> deletePaystub(Company pCompany) {
        IProcess processCore = new DeletePaystubCore(pCompany);
        ProcessResult<Paystub> processResult = processCore.execute();
        return processResult;
    }
}
