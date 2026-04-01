package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class EmployerPreference extends BaseEmployerPreference {

    //App names
    public static final String VMP = "VMP";
    //Preference names
    public static final String SELF_SERVICE_SIGN_IN = "SelfServiceSignIn";
    public static final String HIDE_PAYCHECKS_DATED_IN_THE_FUTURE = "HidePaychecksDatedInTheFuture";

    //Preference values
    public static final String ON = "On";
    public static final String OFF = "Off";
    /**
	 * Default constructor.
	 */
	public EmployerPreference()
	{
		super();
	}

    public static List<EmployerPreference> findEmployerPreferences(String companyRealmId, String appName) {
        Expression<EmployerPreference> query = new Query<EmployerPreference>()
                .Where(EmployerPreference.Company().IAMRealmId().equalTo(companyRealmId)
                .And(EmployerPreference.AppName().equalTo(appName)));
        DomainEntitySet<EmployerPreference> employerPreferences = Application.find(EmployerPreference.class, query);
        return new ArrayList<EmployerPreference>(employerPreferences);
    }

    public static EmployerPreference findEmployerPreference(String companyRealmId, String appName, String preferenceName) {
        EmployerPreference employerPreference = null;
        Expression<EmployerPreference> query = new Query<EmployerPreference>()
                .Where(EmployerPreference.Company().IAMRealmId().equalTo(companyRealmId)
                .And(EmployerPreference.AppName().equalTo(appName))
                .And(EmployerPreference.PreferenceName().equalTo(preferenceName)));
        DomainEntitySet<EmployerPreference> employerPreferences = Application.find(EmployerPreference.class, query);
        if(employerPreferences != null) {
            employerPreference = employerPreferences.getFirst();
        }
        return employerPreference;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findEmployerPreference
     * only handles company seq in companyRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param appName
     * @param preferenceName
     * @return
     */
    public static EmployerPreference findEmployerPreferenceByCompanyUniqueId(String companyUniqueId, String appName, String preferenceName) {
        EmployerPreference employerPreference = null;
        Expression<EmployerPreference> query = new Query<EmployerPreference>()
                .Where(EmployerPreference.Company().Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId))
                        .And(EmployerPreference.AppName().equalTo(appName))
                        .And(EmployerPreference.PreferenceName().equalTo(preferenceName)));
        DomainEntitySet<EmployerPreference> employerPreferences = Application.find(EmployerPreference.class, query);
        if(employerPreferences != null) {
            employerPreference = employerPreferences.getFirst();
        }
        return employerPreference;
    }
}