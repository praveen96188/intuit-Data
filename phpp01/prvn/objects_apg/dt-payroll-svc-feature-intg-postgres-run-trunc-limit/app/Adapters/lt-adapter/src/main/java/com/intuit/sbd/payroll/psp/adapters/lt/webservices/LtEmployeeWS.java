package com.intuit.sbd.payroll.psp.adapters.lt.webservices;

import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtEmployeeListWSDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.LtEmployee;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Apr 3, 2008
 * Time: 2:18:33 PM
 * To change this template use File | Settings | File Templates.
 */

@WebService
public class LtEmployeeWS {

    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtEmployeeListWSDTO getEmployeeList(@WebParam(name = "company") String companyId,
                                               @WebParam(name = "sourceSystemId") String sourceSystemId) throws Exception {
        LtEmployee employee = new LtEmployee();
        LtEmployeeListWSDTO wsdto = new LtEmployeeListWSDTO();

        try{
            wsdto.employee.addAll(employee.getEmployeeList(companyId, SourceSystemCode.valueOf(sourceSystemId)));
            wsdto.numberOfEmployees = wsdto.employee.size();

        }catch(Exception e){
            throw e;
        }

        wsdto.status = 0;
        return wsdto;
    }

    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtEmployeeListWSDTO getCloudEmployeeList(@WebParam(name = "company") String companyId) throws Exception {
        LtEmployee employee = new LtEmployee();
        LtEmployeeListWSDTO wsdto = new LtEmployeeListWSDTO();

        try{
            wsdto.employee.addAll(employee.getCloudEmployeeList(companyId));
            wsdto.numberOfEmployees = wsdto.employee.size();

        }catch(Exception e){
            throw e;
        }

        wsdto.status = 0;
        return wsdto;
    }

}
