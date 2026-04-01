package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class Offering extends BaseOffering {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<Offering> findAvailableOfferings() {
        // TODO: when Offering.EffectiveDate is added, this should only return offerings currently effective
        Expression<Offering> query =
                new Query<Offering>()
                      .OrderBy(Offering.SKU());
        return Application.find(Offering.class, query);
    }

    public static DomainEntitySet<Offering> findAvailableOfferings(ServiceCode pServiceCode, EntitlementCode entitlementCode) {
        Expression<EntitlementCodeOffering> query =
                new Query<EntitlementCodeOffering>()
                        .Where(EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode).And(EntitlementCodeOffering.Offering().ServiceCode().equalTo(pServiceCode)))
                        .OrderBy(EntitlementCodeOffering.Offering().SKU());

        DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, query);
        DomainEntitySet<Offering> offerings = new DomainEntitySet<Offering>();
        for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
            offerings.add(entitlementCodeOffering.getOffering());
        }
        return offerings;
    }

    /**
     * Returns the Offering described by pOfferingInfo.
     * @return true if the SKU is in use (by any but the excluded entity), otherwise false
     */
    public static Offering findOffering(OfferingCode offeringCode, String sku, long pQuickBooksSubtype, ServiceCode pServiceCode) {
        if (offeringCode!=null) {
            return findByOfferingCode(offeringCode);
        } else if (sku != null) {
            return findBySKU(sku);
        } else if (pQuickBooksSubtype != 0) {
            return findByQuickBooksSubtypeAndService(pQuickBooksSubtype, pServiceCode);
        } else { //handle other ways of specifying the offering (when determined) before this else-clause
            return null;
        }
    }

    public static Offering findByOfferingCode(OfferingCode pOfferingCode) {
        DomainEntitySet<Offering> found = Application.find(Offering.class, OfferingCode().equalTo(pOfferingCode));
        return found.isEmpty() ? null : found.get(0);
    }

    public static Offering findOffering(Company pCompany, ServiceCode pServiceCd) {
        if (pCompany == null || pServiceCd == null) {
            return null;
        }
        Expression<CompanyOffering> query =
                new Query<CompanyOffering>()
                       .Where(CompanyOffering.Company().equalTo(pCompany)
                              .And(CompanyOffering.Offering().ServiceCode().equalTo(pServiceCd)))
                       .OrderBy(CompanyOffering.Offering().ServiceCode().Descending());

        DomainEntitySet<CompanyOffering> companyOfferings = Application.find(CompanyOffering.class, query);
        if (companyOfferings.size()>1) {
            throw new RuntimeException("Did not find 0 or one company offerings for service as expected: "+pCompany.getId()+" "+pServiceCd);
        } else if (companyOfferings.size() == 1){
            return companyOfferings.get(0).getOffering();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the given SKU is already in use by any Offering or OfferingServiceCharge entities.
     * If non-null, pExcluded (an Offering) is excluded from the test.
     * @param pSKU The SKU to be tested
     * @param pExcluded This entity is excluded from the in-use test
     * @return true if the SKU is in use (by any but the excluded entity), otherwise false
     */
    public static boolean skuIsInUse(String pSKU, Offering pExcluded) {
        if (OfferingServiceCharge.findBySKU(pSKU) != null) { // if a service charge is using this sku
            return true;
        } else { // no service charge is using it
            Offering existing = findBySKU(pSKU);
            if (existing == null) { // no offering is using it
                return false;
            } else if (pExcluded!=null && pExcluded.equals(existing)) { // in use by the excluded offering
                return false;
            } else { // in use by one that is NOT excluded
                return true;
            }
        }
    }

    public static Offering findBySKU(String pSKU) {
        DomainEntitySet<Offering> found = Application.find(Offering.class, SKU().equalTo(pSKU));
        if (found==null || found.size()==0) {
            return null;
        } else {
            return found.get(0);
        }
    }

    public static Offering findByQuickBooksSubtypeAndService(long QuickBooksSubtype, ServiceCode pServiceCode) {
        // exclude those with future effective dates
        SpcfCalendar tomorrow = PSPDate.getPSPTime();
        CalendarUtils.clearTime(tomorrow);
        tomorrow.addDays(1);
        // order the results by effective-date, descending, so the first match is the newest effective date
        Expression<EntitlementCodeOffering> query =
                new Query<EntitlementCodeOffering>()
                       .Where(EntitlementCodeOffering.EntitlementCode().QuickBooksSubtype().equalTo(QuickBooksSubtype)
                               .And(EntitlementCodeOffering.ServiceCd().equalTo(pServiceCode))
                               .And(EntitlementCodeOffering.EffectiveDate().lessThan(tomorrow)))
                       .OrderBy(EntitlementCodeOffering.EffectiveDate().Descending());

        DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings =
                Application.find(EntitlementCodeOffering.class, query);
        if (entitlementCodeOfferings == null || entitlementCodeOfferings.size() == 0) {
            return null;
        } else {
            return entitlementCodeOfferings.get(0).getOffering();
        }
    }

    public static Offering findDefaultOffering(Company pCompany, ServiceCode pServiceCode) {

        switch(pServiceCode) {
            case BillPayment:
                return Application.find(Offering.class, Offering.OfferingCode().equalTo(OfferingCode.BillPaymentSTDFY16)).getFirst();
            case RiskAssessment:
            case Cloud:
            case CloudV2:
            case ThirdParty401k:
            case ViewMyPaycheck:
            case CheckDistribution:
            case WorkersComp:
                return Application.find(Offering.class, Offering.ServiceCode().equalTo(pServiceCode)).getFirst();
            case DirectDeposit:
                if(pCompany.getSourceSystemCd() == SourceSystemCode.QBOE) {
                    return Application.find(Offering.class, Offering.OfferingCode().equalTo(OfferingCode.QBOEDD)).getFirst();
                }
        }

        EntitlementUnit entitlementUnit = pCompany.getActivePrimaryEntitlementUnit();

        if(entitlementUnit == null) {
            DomainEntitySet<EntitlementUnit> entitlementUnits = pCompany.getPrimaryEntitlementUnits();
            if(entitlementUnits.size() == 1) {
                entitlementUnit = entitlementUnits.getFirst();
            } else {
                CompanyOffering companyOffering = pCompany.getDirectDepositCompanyOffering();
                if(companyOffering != null) {
                    return companyOffering.getOffering();
                } else {
                    Application.getLogger(Offering.class).error("No active primary Entitlement Unit and more than one inactive primary entitlement units found. Also company is not assigned DirectDeposit Offering, Company Id:"+ pCompany.getId().toString());
                    //throw new RuntimeException("No active primary Entitlement Unit and more than one inactive primary entitlement units found. Also company is not assigned DirectDeposit Offering.");
                    return null;
                }
            }
        }

        EntitlementCode entitlementCode = entitlementUnit.getEntitlement().getEntitlementCode();

        DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings;

        if(pServiceCode == ServiceCode.Tax) {
            String priceType = pCompany.getPriceType();
            if(priceType != null && priceType.toUpperCase().equals("STANDARD")) {
                priceType = "Standard";
            }
            entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode)
                    .And(EntitlementCodeOffering.IsDefault().equalTo(true)).And(EntitlementCodeOffering.PriceType().equalTo(priceType)));
        } else {
            entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.EntitlementCode().equalTo(entitlementCode)
                    .And(EntitlementCodeOffering.IsDefault().equalTo(true)));
        }

        if(entitlementCodeOfferings.size() == 0) {
            return null;
        }

        return entitlementCodeOfferings.getFirst().getOffering();
    }
    
    public String getPriceType() {
        DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.Offering().equalTo(this));
        if(entitlementCodeOfferings.getFirst() != null) {
            return entitlementCodeOfferings.getFirst().getPriceType();
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Offering()
	{
		super();
	}

    public SpcfMoney getListPrice(OfferingServiceChargeType pChargeType, int pQuantity) {
        OfferingServiceCharge charge = getCharge(pChargeType, pQuantity);
        if (charge != null) {
            OfferingServiceChargePrice price = charge.getCurrentPrice();
            if (price != null) {
                return (SpcfMoney) price.getBasePrice().add(price.getUnitPrice().multiply(SpcfDecimal.createInstance((long)pQuantity)));
            }
        }
        return null;
    }

//    public String getSku(OfferingServiceChargeType pChargeType, int pQuantity) {
//        OfferingServiceCharge charge = getCharge(pChargeType, pQuantity);
//        if (charge != null) {
//            return charge.getSKU();
//        }
//        return null;
//    }

    public OfferingServiceCharge getCharge(OfferingServiceChargeType pChargeType, int pQuantity) {
        if (this != null) {
            OfferingServiceChargeGroup group = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(this, pChargeType);
            if (group != null) {
                return group.selectTier(pQuantity);
            }
        }
        return null;
    }

    public List<OfferingServiceCharge> getPayrollCharges() {
        // get all OfferingServiceChargeGroups for the offering
        DomainEntitySet<OfferingServiceChargeGroup> allGroups = Application.find(OfferingServiceChargeGroup.class, OfferingServiceChargeGroup.Offering().equalTo(this));

        // walk through those, choosing only the ones that represent "payroll" charges
        List<OfferingServiceCharge> payrollCharges = new ArrayList<OfferingServiceCharge>();
        for (OfferingServiceChargeGroup group : allGroups) {
            if (OfferingServiceChargeGroup.isPayrollChargeType(group.getAppliesTo())) {
                OfferingServiceCharge charge = group.selectTier(1);
                payrollCharges.add(charge);
            }
        }

        return payrollCharges;
    }

    /**
     * Finds all Offers applicable to at least one of the OfferingServiceCharges relate to the given Offering,
     * provided those Offers are approved and effective as of now.
     * @return
     */
    public DomainEntitySet<Offer> getApplicableOffers() {
        // Offers' effective-dates must be before tomorrow at 00:00:00
        SpcfCalendar tomorrow = PSPDate.getPSPTime();
        tomorrow.addDays(1);
        CalendarUtils.clearTime(tomorrow);

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "offering";
        paramValues[0] = this;

        paramNames[1] = "tomorrow";
        paramValues[1] = tomorrow;

        return Application.findByNamedQuery("findApplicableDiscountOffers", paramNames, paramValues);
    }
}
