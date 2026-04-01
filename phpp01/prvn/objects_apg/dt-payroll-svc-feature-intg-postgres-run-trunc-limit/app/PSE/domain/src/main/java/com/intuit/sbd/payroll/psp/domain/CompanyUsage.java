package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Hand-written business logic
 */
public class CompanyUsage extends BaseCompanyUsage {

	/**
	 * Default constructor.
	 */
	public CompanyUsage() {
		super();
	}

	public static CompanyUsage findCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pLicenseId, String pEntitlementId) {
		CompanyUsage foundCompanyUsage = null;

		NaturalKey naturalKey = new NaturalKey(CompanyUsage.class, pSourceSystemCd, pSourceCompanyId, pLicenseId, pEntitlementId);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundCompanyUsage = Application.findById(CompanyUsage.class, primaryKey);
		} else {
			DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(pSourceCompanyId).And(CompanyUsage.SourceSystemCd().equalTo(pSourceSystemCd)).And(CompanyUsage.LicenseId().equalTo(pLicenseId)).And(CompanyUsage.EntitlementId().equalTo(pEntitlementId)));

			if (companyUsages.size() > 1) {
				throw new RuntimeException("Query for company usage by source system " + pSourceSystemCd + " and source company id " + pSourceCompanyId + " did not return 0 or 1 results as expected");
			}

			if (!companyUsages.isEmpty()) {
				foundCompanyUsage = companyUsages.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundCompanyUsage.getId());
			}
		}

		return foundCompanyUsage;
	}

	private static CompanyUsage createCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pEntitlementId, String pLicenseId, int pBillingDayOfMonth, int pStartDayOfUsageMonth) {
		CompanyUsage createdCompanyUsage = new CompanyUsage();

		createdCompanyUsage.setSourceCompanyId(pSourceCompanyId);
		createdCompanyUsage.setSourceSystemCd(pSourceSystemCd);
		createdCompanyUsage.setEntitlementId(pEntitlementId);
		createdCompanyUsage.setLicenseId(pLicenseId);
		createdCompanyUsage.setBillingDayOfMonth(pBillingDayOfMonth);
		createdCompanyUsage.setStartDayOfUsageMonth(pStartDayOfUsageMonth);

		Application.save(createdCompanyUsage);

        NaturalKey naturalKey = new NaturalKey(CompanyUsage.class, pSourceSystemCd, pSourceCompanyId, pLicenseId, pEntitlementId);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdCompanyUsage.getId());

		return createdCompanyUsage;
	}

    private void migrateCompanyUsageToNewEntitlement(Bill pBill, String pEntitlementId, String pLicenseId, int pBillingDayOfMonth) {
        CompanyUsage newUsage = CompanyUsage.createCompanyUsage(getSourceCompanyId(), getSourceSystemCd(), pEntitlementId, pLicenseId, pBillingDayOfMonth, getStartDayOfUsageMonth());
        pBill.setCompanyUsage(newUsage);
        Application.save(pBill);
    }

    public void migrateCompanyUsageToNewEntitlement(Bill pBill, Entitlement pEntitlement) {
        migrateCompanyUsageToNewEntitlement(pBill, pEntitlement.getEntitlementOfferingCode(), pEntitlement.getLicenseNumber(), pEntitlement.getBillingDayOfMonth());
    }

	public static CompanyUsage findOrCreateCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pEntitlementId, String pLicenseId, int pBillingDayOfMonth, int pStartDayOfUsageMonth) {
		CompanyUsage newCompanyUsage = findCompanyUsage(pSourceCompanyId, pSourceSystemCd, pLicenseId, pEntitlementId);
		if (newCompanyUsage == null) {
			newCompanyUsage = createCompanyUsage(pSourceCompanyId, pSourceSystemCd, pEntitlementId, pLicenseId, pBillingDayOfMonth, pStartDayOfUsageMonth);
		}
		return newCompanyUsage;
	}
}
