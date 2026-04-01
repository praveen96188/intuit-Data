package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class CompanyOffering extends BaseCompanyOffering {

    /**
     * Default constructor.
     */
    public CompanyOffering() {
        super();
    }

    public static void updateCompanyOffering(Company pCompany, EntitlementUnit pEntitlementUnit) {
        EntitlementCode entitlementCode = pEntitlementUnit.getEntitlement().getEntitlementCode();
        if (pEntitlementUnit.getEntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES) && entitlementCode.getIsPrimary() && pCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
            CompanyOffering companyOffering = pCompany.getDirectDepositCompanyOffering();
            // When adding Assisted/AssistedAdvantage entitlement unit, Tax service may not be present on the company. If Tax is not present do not change company offering.
            // Upgrading from DIY-DD to Tax, CompanyOffering is updated to AssistedOffering after submitting balance file, that is taken care in CompanyService.updateCompanyServiceStatus
            CompanyService taxService = pCompany.getCompanyService(ServiceCode.Tax);
            if (entitlementCode.getAssetItemCd().in(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage) && (taxService == null || pCompany.isMigratingToAssisted())) {
                return;
            }

            if (companyOffering != null) {
                DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode)
                                                                                                                                                           .And(EntitlementCodeOffering.Offering().equalTo(companyOffering.getOffering())));
                if (entitlementCodeOfferings.isEmpty()) {
                    if (entitlementCode.getAssetItemCd().in(AssetItemCode.Assisted, AssetItemCode.AssistedAdvantage)) {
                        if (pCompany.getPriceType() != null) {
                            entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode)
                                                                                                                              .And(EntitlementCodeOffering.IsDefault().equalTo(true))
                                                                                                                              .And(EntitlementCodeOffering.PriceType().equalTo(pCompany.getPriceType())));
                        }
                    } else {
                        entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode)
                                                                                                                          .And(EntitlementCodeOffering.IsDefault().equalTo(true)));
                    }
                    if (entitlementCodeOfferings.isNotEmpty()) {
                        pCompany.removeCompanyOffering(companyOffering);
                        Application.delete(companyOffering);
                        CompanyOffering newCompOffering = new CompanyOffering();
                        newCompOffering.setCompany(pCompany);
                        newCompOffering.setOffering(entitlementCodeOfferings.getFirst().getOffering());
                        Application.save(newCompOffering);
                        pCompany.addCompanyOffering(newCompOffering);

                        CompanyEvent.createOfferingUpdatedEvent(pCompany, companyOffering.getOffering().getSKU(), newCompOffering.getOffering().getSKU());
                    }
                }
            }
        }
    }
}