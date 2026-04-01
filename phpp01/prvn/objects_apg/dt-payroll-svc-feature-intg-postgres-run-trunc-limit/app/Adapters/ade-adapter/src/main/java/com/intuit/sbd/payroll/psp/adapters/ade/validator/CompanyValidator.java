package com.intuit.sbd.payroll.psp.adapters.ade.validator;

import com.intuit.ems.cep.api.ServiceResult;
import  com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 10/2/13
 * Time: 2:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyValidator {

    public static ServiceResult validateFullServiceCompany(Company pCompany) {
        ServiceResult serviceResult = new ServiceResult();
        if (!pCompany.hasService(ServiceCode.Tax)) {
            serviceResult.getMessages().GenericValidationMessage(Company.class, String.valueOf(pCompany.getSourceCompanyId()), "Only Assisted companies supported.");
        }
        return serviceResult;
    }
}
