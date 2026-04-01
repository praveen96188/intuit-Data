package com.intuit.ems.psp.adapters.dataadapter.helper;

import com.intuit.ems.psp.adapters.dataadapter.dto.PayrollStatus;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 */
public class LicenseHelper {

    public PayrollStatus findEntitlementByQbLicense(String pQbLicense) throws Exception {

        String[] qbLicenseArray = new String[]{pQbLicense, pQbLicense.replaceAll("-", "")};
        List<Object[]> entitlementList = Application.executeNamedQuery(Application.getQueryName("findEntitlementInfoByQbLicense"), new String[]{"qbLicenseNums"},
                                                                       new Object[]{qbLicenseArray});


        PayrollStatus payrollStatus = new PayrollStatus();
        if (entitlementList == null) {
            return payrollStatus;
        }

        SpcfCalendar endDate = null;
        String entStatus = "Disabled";
        for (Object[] result : entitlementList) {
            //end_date, entitlement_state, psid, entitlement_unit_status;
            SpcfCalendar temp = (SpcfCalendar) result[0];
            if (endDate == null) {
                endDate = temp;
            } else if (temp != null && temp.after(endDate)) {
                endDate = temp;
            }
            entStatus = result[1] == null ? "Disabled" : (String) result[1];

            String psid = (String) result[2];

            if (result[3] != null && "Enabled".equals(result[1])) {
                entStatus = EntitlementUnitStatusCode.valueOf((String) result[3]).in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES) ? "Enabled" : "Disabled";
            }
            if ("Enabled".equals(entStatus)) {
                endDate = null;
                break;
            }

        }

        if (endDate != null) {
            payrollStatus.setEndDate(endDate.toLocal().format("yyyyMMdd"));
        }
        payrollStatus.setStatus(entStatus);

        return payrollStatus;

    }

    public static boolean isQBVersionSupported(String pQbVersion) {

        if (pQbVersion == null) {
            return false;
        }

        int intQBVersion;
        try {
            intQBVersion = new Integer(pQbVersion);
        } catch (NumberFormatException e) {
            return false;
        }
        String minAppQBVersionSupportedStr =
                SourcePayrollParameter.findSourcePayrollParameter(
                        SourceSystemCode.QBDT, SourcePayrollParameterCode.MinQBVersionSupported).getParameterValue();
        int minAppQBVersionSupported = new Integer(minAppQBVersionSupportedStr);


        return (intQBVersion >= minAppQBVersionSupported);

    }
}
