package com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories;

import com.intuit.ems.dataservice.v1.resource.EmployeePreferenceParams;
import com.intuit.ems.dataservice.v1.resource.EmployerPreferenceParams;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployerPreference;
import com.intuit.sbd.payroll.psp.domain.PstubEmployeePreference;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

// Class for creating domain objects from CDM objects
public class DomainFactory {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(DomainFactory.class);
    }

    public static PstubEmployeePreference createEmployeePreference(String appName, EmployeePreferenceParams employeePreference, Employee ee) {
        PstubEmployeePreference eePref = new PstubEmployeePreference();
        eePref.setAppName(appName);
        eePref.setEmployee(ee);
        eePref.setPreferenceName(employeePreference.getPreferenceName());
        eePref.setPreferenceValue(employeePreference.getPreferenceValue());
        return eePref;
    }

    public static EmployerPreference createEmployerPreference(String appName, EmployerPreferenceParams employerPreferenceParams, Company company) {
        EmployerPreference employerPreference = new EmployerPreference();
        employerPreference.setAppName(appName);
        employerPreference.setPreferenceName(employerPreferenceParams.getPreferenceName());
        employerPreference.setPreferenceValue(employerPreferenceParams.getPreferenceValue());
        employerPreference.setCompany(company);
        return employerPreference;
    }
}
