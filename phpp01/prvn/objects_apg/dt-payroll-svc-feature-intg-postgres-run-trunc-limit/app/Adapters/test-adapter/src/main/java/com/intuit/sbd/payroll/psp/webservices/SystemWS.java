package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 24, 2011
 * Time: 11:03:23 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class SystemWS {
    @WebMethod
    public String updateSystemParameter(@WebParam(name = "paramName") String pParamName,
                                        @WebParam(name = "paramValue") String pParamValue) throws Exception {
        String oldValue = "";

        SystemParameter.Code paramCode = SystemParameter.Code.valueOf(pParamName);

        try {
            PayrollServices.beginUnitOfWork();

            oldValue = SystemParameter.findStringValue(paramCode);
            SystemParameter.update(paramCode, pParamValue);

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return oldValue;
    }
}
