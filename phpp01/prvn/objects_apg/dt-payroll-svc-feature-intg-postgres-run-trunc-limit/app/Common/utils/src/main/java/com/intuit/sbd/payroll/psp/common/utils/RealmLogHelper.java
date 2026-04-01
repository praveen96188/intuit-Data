package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementCode;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;

import java.util.Objects;

public class RealmLogHelper {

    public static final String QB_REALM_ADD = "QBRealmAdd";
    public static final String QB_REALM_UPDATE = "QBRealmUpdate";
    public static final String COMPANY_REALM_ADD = "CompanyRealmAdd";
    public static final String COMPANY_REALM_UPDATE = "CompanyRealmUpdate";
    public static final String COMPANY_DIFFERENT_REALM_UPDATE = "CompanyDifferentRealmUpdate";
    public static final String COMPANY_REALM_DELETE = "CompanyRealmDelete";

    public static String getRealmEventMessage(String realmEventType, String newRealmId, String oldRealmId, Company company, String customMessage) {
        StringBuffer realmEventMessageBuilder = new StringBuffer();
        realmEventMessageBuilder.append("RealmManagement=").append(realmEventType).append(", ");

        if(Objects.nonNull(customMessage)) {
            realmEventMessageBuilder.append(customMessage).append(", ");
        }

        realmEventMessageBuilder.append("PSID=").append(company.getSourceSystemCompanyId()).append(", ");
        realmEventMessageBuilder.append("NewRealmID=").append(newRealmId).append(", ");
        if(oldRealmId!=null) {
            realmEventMessageBuilder.append("OldRealmID=").append(oldRealmId).append(", ");
        }
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        if(entitlementUnit == null) {
            return realmEventMessageBuilder.toString();
        }

        Entitlement entitlement = entitlementUnit.getEntitlement();

        if(entitlement == null) {
            return realmEventMessageBuilder.toString();
        }

        realmEventMessageBuilder.append("SubscriptionNumber=").append(entitlement.getSubscriptionNumber()).append(", ");

        EntitlementCode entitlementCode = entitlement.getEntitlementCode();

        if(entitlementCode == null) {
            return realmEventMessageBuilder.toString();
        }

        realmEventMessageBuilder.append("ComapnyType=").append(entitlementCode.getEditionType()).append(", ");

        return realmEventMessageBuilder.toString();
    }
}
