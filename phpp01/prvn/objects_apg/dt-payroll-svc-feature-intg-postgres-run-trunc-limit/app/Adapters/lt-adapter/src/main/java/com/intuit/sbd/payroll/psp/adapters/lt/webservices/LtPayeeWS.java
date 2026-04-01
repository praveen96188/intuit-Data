package com.intuit.sbd.payroll.psp.adapters.lt.webservices;

import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtPayeeListWSDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.LtPayee;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Mar 8, 2010
 * Time: 10:29:33 AM
 * To change this template use File | Settings | File Templates.
 */

@WebService
public class LtPayeeWS {

    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtPayeeListWSDTO getPayeeList(@WebParam(name = "company") String companyId) throws Exception {
        LtPayee payee = new LtPayee();
        LtPayeeListWSDTO wsdto = new LtPayeeListWSDTO();

        try{
            wsdto.payee.addAll(payee.getPayees(companyId));
            wsdto.numberOfPayees = wsdto.payee.size();

        }catch(Exception e){
            throw e;
        }

        wsdto.status = 0;
        return wsdto;
    }
}
