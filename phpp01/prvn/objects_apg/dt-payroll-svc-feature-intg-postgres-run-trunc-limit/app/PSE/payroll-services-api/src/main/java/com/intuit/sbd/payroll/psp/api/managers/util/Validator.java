package com.intuit.sbd.payroll.psp.api.managers.util;

import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Nov 8, 2008
 * Time: 8:07:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Validator {
    public static ProcessResult validCompanyParameters(Enum pSourceSystemCd, String pSourceCompanyId) {
        ProcessResult validationResult = new ProcessResult();

        if (pSourceSystemCd == null) {
            validationResult.getMessages().SourceSystemCdNotSpecified(EntityName.Company, pSourceCompanyId);
        }

        if (pSourceCompanyId == null) {
            validationResult.getMessages().CompanyIdNotSpecified(EntityName.Company, pSourceCompanyId);
        }

        return validationResult;

    }
}
