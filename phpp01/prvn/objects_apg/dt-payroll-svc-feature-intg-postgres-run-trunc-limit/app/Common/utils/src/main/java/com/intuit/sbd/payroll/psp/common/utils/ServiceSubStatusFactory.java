package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.OnHoldReason;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
    @author Jeff Jones
 */
public class ServiceSubStatusFactory {

    public static ServiceSubStatusCode getServiceSubStatusCodeBySeverity(Company pCompany, ServiceCode pServiceCode) {
        return getServiceSubStatusCodeBySeverity(pCompany, pCompany.getCompanyService(pServiceCode));
    }

    public static ServiceSubStatusCode getServiceSubStatusCodeBySeverity(Company pCompany) {                
        if(pCompany.isCompanyOnService(ServiceCode.Tax)) {
            return getServiceSubStatusCodeBySeverity(pCompany, ServiceCode.Tax);
        } else {
            return getServiceSubStatusCodeBySeverity(pCompany, ServiceCode.DirectDeposit);
        }
    }

    private static ServiceSubStatusCode getServiceSubStatusCodeBySeverity(Company pCompany, CompanyService pCompanyService) {
        if(pCompanyService == null) {
            return null;
        }

        ServiceSubStatusCode serviceSubStatusCd = pCompanyService.getStatusCd();

        if ((serviceSubStatusCd.equals(ServiceSubStatusCode.Cancelled)) ||
                (serviceSubStatusCd.equals(ServiceSubStatusCode.Terminated))) {
            return serviceSubStatusCd;
        }

        Collection<OnHoldReason> onHoldReasons = pCompany.getCurrentOnHoldReasons();
        if ((onHoldReasons == null) || (onHoldReasons.size() == 0)){
            return serviceSubStatusCd;
        } else {
            List<String> onHoldReasonStringList = new ArrayList<String>();
            for (OnHoldReason onHoldReason: onHoldReasons) {
                onHoldReasonStringList.add(onHoldReason.getOnHoldReasonCd().toString());
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.AchRejectR1R9.toString())) {
                return ServiceSubStatusCode.AchRejectR1R9;
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.Fraud.toString())) {
                return ServiceSubStatusCode.Fraud;
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.AMLHold.toString())) {
                return ServiceSubStatusCode.AMLHold;
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.MissingPaperwork.toString())) {
                return ServiceSubStatusCode.MissingPaperwork;
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.PendingTermination.toString())) {
                return ServiceSubStatusCode.PendingTermination;
            }
            if (onHoldReasonStringList.contains(ServiceSubStatusCode.AuditCorrections.toString())) {
                return ServiceSubStatusCode.AuditCorrections;
            }
            return onHoldReasons.iterator().next().getOnHoldReasonCd();
        }
    }
}
