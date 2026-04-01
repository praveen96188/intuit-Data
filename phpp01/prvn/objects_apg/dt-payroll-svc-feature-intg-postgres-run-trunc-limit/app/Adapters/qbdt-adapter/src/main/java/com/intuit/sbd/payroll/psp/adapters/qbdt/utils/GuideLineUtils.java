package com.intuit.sbd.payroll.psp.adapters.qbdt.utils;

import com.intuit.sbd.payroll.psp.adapters.qbdt.processors.PayrollProcessor;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Objects;

public class GuideLineUtils {
    private static SpcfLogger logger = PayrollServices.getLogger(GuideLineUtils.class);

    public static boolean isUpdatePayrollAllowed(Company pCompany) {
        CompanyService companyService = pCompany.getCompanyService(com.intuit.sbd.payroll.psp.domain.ServiceCode.Guideline401k);

        boolean isAssistedCompany = pCompany.isCompanyOnService(ServiceCode.Tax);
        boolean isUpdatePayrollAllowed = Objects.nonNull(companyService) && !isAssistedCompany && companyService.isActive();
        if(isUpdatePayrollAllowed && isAssistedCompany)
        {
            logger.info("Update payroll enable for enhanced customers with psid="+pCompany.getSourceCompanyId());
        }
        return isUpdatePayrollAllowed;

    }
}
