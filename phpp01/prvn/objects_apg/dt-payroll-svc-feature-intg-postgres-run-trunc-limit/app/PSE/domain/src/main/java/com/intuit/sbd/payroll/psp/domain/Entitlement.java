package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;

/**
 * Hand-written business logic
 */
public class Entitlement extends BaseEntitlement {

	/**
	 * Default constructor.
	 */
	public Entitlement()
    {
        super();
        SpcfCalendar today = PSPDate.getPSPTime();
        setBillingDayOfMonth(today.getDay()); // default BDOM to current day - JIRA: PSP-4117
        setSubscriptionStartDate(today);
    }

    public boolean hasDummyEntitlementCode() {
        return getEntitlementCode() != null &&
                (!getEntitlementCode().isEOorER()) &&
                getEntitlementCode().getQuickBooksSubtype() == 0;
    }
    public boolean isDisabled() {
        return EntitlementStateCode.Disabled.equals(getEntitlementState());
    }

    public static Entitlement findEntitlement(String pLicenseNumber, String pEOC) {
        return findEntitlement(pLicenseNumber, pEOC, false);
    }

    public static Entitlement findEntitlement(String pLicenseNumber, String pEOC, boolean pEagerLoadUnits) {
        Criterion<Entitlement> where =
                Entitlement.LicenseNumber().equalTo(pLicenseNumber);
        if (pEOC != null && pEOC.trim().length() > 0) {
            where = where.And(Entitlement.EntitlementOfferingCode().equalTo(pEOC));
        }

        DomainEntitySet<Entitlement> entitlements;
        if(pEagerLoadUnits) {
            entitlements = Application.find(Entitlement.class, new Query<Entitlement>().Where(where).EagerLoad(Entitlement.EntitlementUnitSet(), Entitlement.EntitlementUnitSet().Filter().Company()));
        } else {
            entitlements = Application.find(Entitlement.class, where);
        }

        if(entitlements.size() > 1) {
            throw new RuntimeException("More than one entitlement exists for License Number: " + pLicenseNumber + ((pEOC != null) ? " EOC: " + pEOC : ""));
        } else if(entitlements.size() > 0) {
            return entitlements.get(0);
        }

        return null;
    }

    public static DomainEntitySet<Entitlement> findEntitlementsByCustomerId(String pCustomerId) {
        Criterion<Entitlement> where =
                Entitlement.CustomerId().equalTo(pCustomerId);

        return Application.find(Entitlement.class, where);        
    }

    public static Entitlement findEntitlementBySubscriptionNumber(String pSubscriptionNumber) {

        Entitlement entitlement = null;

        Expression<Entitlement> query =
                new Query<Entitlement>()
                        .Where(Entitlement.SubscriptionNumber().equalTo(pSubscriptionNumber))
                        .EagerLoad(Entitlement.EntitlementUnitSet(), Entitlement.EntitlementUnitSet().Filter().Company(), Entitlement.EntitlementCode());

        DomainEntitySet<Entitlement> entitlements = Application.find(Entitlement.class, query);

        if (entitlements != null && entitlements.isNotEmpty()) {
            if (entitlements.size() > 1) {
                throw new RuntimeException("multiple entitlements found with subscription number: " + pSubscriptionNumber);
            }
            entitlement = entitlements.getFirst();
        }

        return entitlement;
    }


    public static ArrayList<String> findSymphonySubscriptionNumberByCompany(String companyId, String sourceSystemCd) {
        ArrayList<String> subscriptionNumbers = new ArrayList<String>();

        Expression<Entitlement> query =
                new Query<Entitlement>()
                        .Where(Entitlement.EntitlementUnitSet().Exists(EntitlementUnit.Company().SourceCompanyId().equalTo(companyId)
                                .And(EntitlementUnit.Company().SourceSystemCd().equalTo(SourceSystemCode.valueOf(sourceSystemCd))))
                                .And(Entitlement.EntitlementCode().IsUsageBilling().equalTo(Boolean.TRUE)))
                        .EagerLoad(Entitlement.EntitlementUnitSet(), Entitlement.EntitlementUnitSet().Filter().Company(), Entitlement.EntitlementCode());

        DomainEntitySet<Entitlement> entitlements = Application.find(Entitlement.class, query);

        for(Entitlement entitlement : entitlements){
            subscriptionNumbers.add(entitlement.getSubscriptionNumber());
        }

        return subscriptionNumbers;
    }

    public DomainEntitySet<EntitlementUnit> getActiveEntitlementUnitCollection() {
        return getEntitlementUnitCollection().find(EntitlementUnit.EntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES));
    }

    @Override
    public String toString() {
        return "License: " + getLicenseNumber() + " EOC: " + getEntitlementOfferingCode();
    }

    public boolean hasPendingOrRecentMessages() {
        SpcfCalendar offset = PSPDate.getPSPTime();
        offset.addMinutes(-1);

        DomainEntitySet<EntitlementMessage> entitlementMessages =
                Application.find(EntitlementMessage.class,
                                 EntitlementMessage.LicenseNumber().equalTo(getLicenseNumber())
                                                   .And(EntitlementMessage.EntitlementOfferingCode().equalTo(getEntitlementOfferingCode()))
                                                   .And(EntitlementMessage.Status().equalTo(EntitlementMessageStatusCode.New)
                                                                          .Or(EntitlementMessage.CreatedDate().greaterThan(offset))));

        return entitlementMessages.isNotEmpty();

    }

    public boolean hasSubscriptionEndDateChanged(SpcfCalendar pNewValue) {
        if (getSubscriptionEndDate() != null) {
            return !getSubscriptionEndDate().equals(pNewValue);
        }

        if (pNewValue != null) {
            return !pNewValue.equals(getSubscriptionEndDate());
        }

        //Both values are null
        return false;
    }
}
