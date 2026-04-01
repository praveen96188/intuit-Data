package com.intuit.sbd.payroll.psp.adapters.ade.dg;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import java.util.Objects;

public class DGCompanyValidator {

    public static boolean validateDG(ServiceResult validationResult, String psid) {
        if(!AuthUser.hasSAPAdminAccess()) {
            Company company = Company.findCompanyNoEagerLoad(psid, SourceSystemCode.QBDT);
            if (Objects.isNull(company)) {
                validationResult.getMessages().EntityDoesNotExist(com.intuit.schema.payroll.v3.company.Company.class, psid);
                return true;
            }
        }
        return false;
    }
}
