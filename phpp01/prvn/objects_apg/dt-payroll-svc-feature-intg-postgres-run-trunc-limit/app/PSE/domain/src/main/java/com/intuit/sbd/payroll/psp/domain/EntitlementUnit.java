package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Hand-written business logic
 */
public class EntitlementUnit extends BaseEntitlementUnit implements IUpdatable {
    public static String FedTaxIdKeyName="EntUnit_FedTaxId";

    public static final EntitlementUnitStatusCode[] ACTIVE_ENTITLEMENT_UNIT_STATUSES ={
            EntitlementUnitStatusCode.Activated,
            EntitlementUnitStatusCode.PendingActivation,
            EntitlementUnitStatusCode.PendingReactivation,
            EntitlementUnitStatusCode.ErrorActivating,
            EntitlementUnitStatusCode.ActivationHold
    };

	/**
	 * Default constructor.
	 */
	public EntitlementUnit()
	{
		super();
	}

    public String getFullServiceKey() {
        if (StringUtils.isEmpty(getExtensionKey())) {
            return getServiceKey();
        } else {
            return getServiceKey() + " " + getExtensionKey();
        }
    }

    public static EntitlementUnit findEntitlementUnit(SpcfUniqueId pId) {
        EntitlementUnit entitlementUnit = null;

        Expression<EntitlementUnit> query =
                new Query<EntitlementUnit>()
                        .Where(EntitlementUnit.Id().equalTo(pId))
                        .EagerLoad(EntitlementUnit.Company(), EntitlementUnit.Entitlement(), EntitlementUnit.Entitlement().EntitlementCode());

        DomainEntitySet<EntitlementUnit> entitlementUnits = Application.find(EntitlementUnit.class, query);

        if (entitlementUnits != null && entitlementUnits.isNotEmpty()) {
            entitlementUnit = entitlementUnits.getFirst();
        }

        return entitlementUnit;
    }

    public static DomainEntitySet<EntitlementUnit> findEntitlementUnits(String pFedTaxId, String pLicenseNumber, String pEOC) {
        Criterion<EntitlementUnit> where = null;
        if(pFedTaxId == null) {
            where = EntitlementUnit.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, pFedTaxId);
            where = EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList);
        }

        if (pLicenseNumber != null) {
            where = where.And(EntitlementUnit.Entitlement().LicenseNumber().equalTo(pLicenseNumber));
        }
        if (pEOC != null) {
            where = where.And(EntitlementUnit.Entitlement().EntitlementOfferingCode().equalTo(pEOC));
        }

        return Application.find(EntitlementUnit.class, new Query<EntitlementUnit>().
                Where(where)
                .OrderBy(EntitlementUnit.Entitlement().LicenseNumber(), EntitlementUnit.Entitlement().EntitlementOfferingCode())
                .EagerLoad(EntitlementUnit.Entitlement(), EntitlementUnit.Company()));
    }

    public static DomainEntitySet<EntitlementUnit> findActiveEntitlementUnits(String pFedTaxId, String pLicenseNumber, String pEOC) {
        Criterion<EntitlementUnit> where = null;
        if(pFedTaxId == null) {
            where = EntitlementUnit.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, pFedTaxId);
            where = EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList);
        }

        where = where.And(EntitlementUnit.EntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES));

        if (pLicenseNumber != null) {
            where = where.And(EntitlementUnit.Entitlement().LicenseNumber().equalTo(pLicenseNumber));
        }
        if (pEOC != null) {
            where = where.And(EntitlementUnit.Entitlement().EntitlementOfferingCode().equalTo(pEOC));
        }

        return Application.find(EntitlementUnit.class, new Query<EntitlementUnit>().
                Where(where)
                .OrderBy(EntitlementUnit.Entitlement().LicenseNumber(), EntitlementUnit.Entitlement().EntitlementOfferingCode())
                .EagerLoad(EntitlementUnit.Entitlement(), EntitlementUnit.Company()));
    }

    public static DomainEntitySet<EntitlementUnit> getActiveEntitlementUnits(String pFedTaxId, String pLicenseNumber, String pEOC) {

        return findEntitlementUnits(pFedTaxId, pLicenseNumber, pEOC).find(EntitlementUnit.EntitlementUnitStatus().in(ACTIVE_ENTITLEMENT_UNIT_STATUSES));
    }
    
    public static EntitlementUnit getActiveEntitlementUnitByFedTaxId(String pFedTaxId, Entitlement entitlement) {
        DomainEntitySet<EntitlementUnit> entitlementUnits = getActiveEntitlementUnits(pFedTaxId, entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        if(entitlementUnits.size() > 1) {
            throw new RuntimeException("More than one Activated Entitlement Units found with FedTaxId:"+pFedTaxId);
        }
        return entitlementUnits.getFirst();
    }

    public static List<EntitlementUnit> findEntitlementUnitsOnUsageBillingBySubscriptionStartDate(SpcfCalendar pSubscriptionStartDate) {
        if( pSubscriptionStartDate == null) {
            return null;
        }

        String[] paramName = {"subscriptionStartDate"};
        Object[] paramValue = {SpcfDateFormat.format(pSubscriptionStartDate, "MMddyyyy")};

        return Application.executeNamedQuery(
                Application.getQueryName("findEntitlementUnitsOnUsageBillingBySubscriptionStartDate"), paramName, paramValue);
    }

    public static DomainEntitySet<EntitlementUnit> findEntitlementUnits(String pFedTaxId) {
        Criterion<EntitlementUnit> where = null;
        if(pFedTaxId == null) {
            where = EntitlementUnit.FedTaxIdEnc().isNull();
        } else {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName, pFedTaxId);
            where = EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList);
        }
        return Application.find(EntitlementUnit.class, new Query<EntitlementUnit>().Where(where)
                                                                                   .EagerLoad(EntitlementUnit.Entitlement(), EntitlementUnit.Company()));
    }

    public boolean isDeactivated() {        
        return EntitlementUnitStatusCode.Deactivated.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.ErrorDeactivating.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.PendingDeactivation.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.DeactivationHold.equals(getEntitlementUnitStatus());
    }
    
    public boolean isActivated() {
        return EntitlementUnitStatusCode.Activated.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.ActivationHold.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.ErrorActivating.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.PendingReactivation.equals(getEntitlementUnitStatus()) ||
                EntitlementUnitStatusCode.PendingActivation.equals(getEntitlementUnitStatus());
    }

    public boolean isHistoric() {
        return EntitlementUnitStatusCode.Historic.equals(getEntitlementUnitStatus());
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setServiceKey(String pServiceKey) {
        // don't update the token when the service key is set initially
        if(getServiceKey() != null && !ObjectUtils.equals(getServiceKey(), pServiceKey)) {
            onUpdate();
        }
        super.setServiceKey(pServiceKey);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    public void onUpdate() {
        if(getCompany() != null) {
            getCompany().onUpdate();
        }
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}