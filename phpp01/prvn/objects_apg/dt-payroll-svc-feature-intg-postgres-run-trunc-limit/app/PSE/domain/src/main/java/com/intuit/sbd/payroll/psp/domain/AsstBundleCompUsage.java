package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Hand-written business logic
 */
public class AsstBundleCompUsage extends BaseAsstBundleCompUsage {

	/**
	 * Default constructor.
	 */
	public AsstBundleCompUsage()
	{
		super();
	}

	public static AsstBundleCompUsage findAssistedBundleCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pLicenseId, String pEntitlementId) {
		AsstBundleCompUsage foundAssistedBundleCompanyUsage = null;

		NaturalKey naturalKey = new NaturalKey(AsstBundleCompUsage.class, pSourceSystemCd, pSourceCompanyId, pLicenseId, pEntitlementId);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundAssistedBundleCompanyUsage = Application.findById(AsstBundleCompUsage.class, primaryKey);
		} else {
			DomainEntitySet<AsstBundleCompUsage> assistedBundleCompanyUsages = Application.find(AsstBundleCompUsage.class, AsstBundleCompUsage.SourceCompanyId().equalTo(pSourceCompanyId).And(AsstBundleCompUsage.SourceSystemCd().equalTo(pSourceSystemCd)).And(AsstBundleCompUsage.LicenseId().equalTo(pLicenseId)).And(AsstBundleCompUsage.EntitlementId().equalTo(pEntitlementId)));

			if (assistedBundleCompanyUsages.size() > 1) {
				throw new RuntimeException("Query for company usage by source system " + pSourceSystemCd + " and source company id " + pSourceCompanyId + " did not return 0 or 1 results as expected");
			}

			if (!assistedBundleCompanyUsages.isEmpty()) {
				foundAssistedBundleCompanyUsage = assistedBundleCompanyUsages.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundAssistedBundleCompanyUsage.getId());
			}
		}

		return foundAssistedBundleCompanyUsage;
	}

	private static AsstBundleCompUsage createAssistedBundleCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pEntitlementId, String pLicenseId) {
		AsstBundleCompUsage createdAssistedBundleCompanyUsage = new AsstBundleCompUsage();

		createdAssistedBundleCompanyUsage.setSourceCompanyId(pSourceCompanyId);
		createdAssistedBundleCompanyUsage.setSourceSystemCd(pSourceSystemCd);
		createdAssistedBundleCompanyUsage.setEntitlementId(pEntitlementId);
		createdAssistedBundleCompanyUsage.setLicenseId(pLicenseId);

		Application.save(createdAssistedBundleCompanyUsage);

		NaturalKey naturalKey = new NaturalKey(AsstBundleCompUsage.class, pSourceSystemCd, pSourceCompanyId, pLicenseId, pEntitlementId);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdAssistedBundleCompanyUsage.getId());

		return createdAssistedBundleCompanyUsage;
	}

	public static AsstBundleCompUsage findOrCreateAssistedBundleCompanyUsage(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, String pEntitlementId, String pLicenseId) {
		AsstBundleCompUsage newAssistedBundleCompanyUsage = findAssistedBundleCompanyUsage(pSourceCompanyId, pSourceSystemCd, pLicenseId, pEntitlementId);
		if (newAssistedBundleCompanyUsage == null) {
			newAssistedBundleCompanyUsage = createAssistedBundleCompanyUsage(pSourceCompanyId, pSourceSystemCd, pEntitlementId, pLicenseId);
		}
		return newAssistedBundleCompanyUsage;
	}

}