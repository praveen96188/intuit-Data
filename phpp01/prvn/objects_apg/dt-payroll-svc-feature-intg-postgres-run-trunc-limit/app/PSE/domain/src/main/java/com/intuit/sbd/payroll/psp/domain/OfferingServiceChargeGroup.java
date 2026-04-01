package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class OfferingServiceChargeGroup extends BaseOfferingServiceChargeGroup {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Finds the OfferingServiceChargeGroups with the given charge-type for a given company.
     * @param pCompany
     * @param pChargeType
     * @return The matching OfferingServiceChargeGroup, or null if there is no group with this type on this offering.
     */
    public static DomainEntitySet<OfferingServiceChargeGroup> findOfferingServiceChargeGroup(Company pCompany, OfferingServiceChargeType pChargeType) {
        DomainEntitySet<OfferingServiceChargeGroup> found = null;
        for (CompanyOffering currOffering : pCompany.getCompanyOfferingCollection()) {
            if (found == null) {
                found=Application.find(OfferingServiceChargeGroup.class,
                                         Offering().equalTo(currOffering.getOffering())
                                         .And(AppliesTo().equalTo(pChargeType)));
            } else {
                DomainEntitySet<OfferingServiceChargeGroup> tempFound = Application.find(OfferingServiceChargeGroup.class,
                                         Offering().equalTo(currOffering.getOffering())
                                         .And(AppliesTo().equalTo(pChargeType)));
                found.addAll(tempFound);
            }
        }
        if (found==null || found.size()==0) {
            return null; // there is no such charge-type for this company's offerings, i.e. no charge
        } else {
            return found;
        }
    }

    public static OfferingServiceCharge findFirstOfferingServiceCharge(Company pCompany, OfferingServiceChargeType pChargeType) {
        DomainEntitySet<OfferingServiceCharge> found = null;
        for (CompanyOffering currOffering : pCompany.getCompanyOfferingCollection()) {
            if (found == null) {
                found=Application.find(OfferingServiceCharge.class,
                                         OfferingServiceCharge.OfferingServiceChargeGroup().Offering().equalTo(currOffering.getOffering())
                                         .And(OfferingServiceCharge.OfferingServiceChargeGroup().AppliesTo().equalTo(pChargeType)));
            } else {
                DomainEntitySet<OfferingServiceCharge> tempFound = Application.find(OfferingServiceCharge.class,
                                         OfferingServiceCharge.OfferingServiceChargeGroup().Offering().equalTo(currOffering.getOffering())
                                         .And(OfferingServiceCharge.OfferingServiceChargeGroup().AppliesTo().equalTo(pChargeType)));
                found.addAll(tempFound);
            }
        }
        if (found==null || found.size()==0) {
            return null; // there is no such charge-type for this company's offerings, i.e. no charge
        } else {
            return found.get(0);
        }
    }

     public static OfferingServiceChargeGroup findOfferingServiceChargeGroup(Offering pOffering, OfferingServiceChargeType pChargeType) {
        DomainEntitySet<OfferingServiceChargeGroup> found = Application.find(OfferingServiceChargeGroup.class,
                                 Offering().equalTo(pOffering)
                                 .And(AppliesTo().equalTo(pChargeType)));
        if (found==null || found.size()==0) {
            return null; // there is no such charge-type for this company's offerings, i.e. no charge
        } else {
            return found.get(0);
        }
    }

     public static OfferingServiceChargeGroup findFirstOfferingServiceChargeGroup(Company pCompany, OfferingServiceChargeType pChargeType) {
        DomainEntitySet<OfferingServiceChargeGroup> found = findOfferingServiceChargeGroup(pCompany, pChargeType);
        if (found==null || found.size()==0) {
            return null; // there is no such charge-type for this company's offerings, i.e. no charge
        } else {
            return found.get(0);
        }
    }

    public static OfferingServiceChargeGroup findOfferingServiceChargeGroup(Company pCompany, OfferingCode pOfferingCode, OfferingServiceChargeType pChargeType) {
        DomainEntitySet<OfferingServiceChargeGroup> found = null;
        Expression<CompanyOffering> query =
                new Query<CompanyOffering>()
                       .Where(CompanyOffering.Company().equalTo(pCompany)
                              .And(CompanyOffering.Offering().OfferingCode().equalTo(pOfferingCode)));

        DomainEntitySet<CompanyOffering> companyOfferings = Application.find(CompanyOffering.class, query);
        if (companyOfferings.size()>1) {
            Application.getLogger(OfferingServiceChargeGroup.class).error("Found two offerings for the same company: "+pCompany.getId()+" and offering code: "+pOfferingCode+".  Continuing processing but this signifies a bug that needs to be fixed");
       }

        //should be just one offering per company per offering code
        for (CompanyOffering currOffering : companyOfferings) {
            if (found == null) {
                found=Application.find(OfferingServiceChargeGroup.class,
                                         Offering().equalTo(currOffering.getOffering())
                                         .And(AppliesTo().equalTo(pChargeType)));
            } else {
                DomainEntitySet<OfferingServiceChargeGroup> tempFound = Application.find(OfferingServiceChargeGroup.class,
                                         Offering().equalTo(currOffering.getOffering())
                                         .And(AppliesTo().equalTo(pChargeType)));
                found.addAll(tempFound);
            }
        }
        if (found==null || found.size()==0) {
            return null; // there is no such charge-type for this company's offering, i.e. no charge
        } else {
            return found.get(0);
        }
    }

    public static boolean isPayrollChargeType(OfferingServiceChargeType pChargeType) {
        switch (pChargeType) {
            case PerTransmission:
            case PerPayroll:
            case PerPaycheck:
            case PerPayment:
            case MonthlyFee:
            case ExtraStateFee:
            case DirectDepositFee:
            case BackdatedPayroll:
            case W2Fee:
            case W2BaseFee:
            case EmployeesPaid:
                return true;

            default:
                return false;
        }
    }

    public static boolean isW2ChargeType(OfferingServiceChargeType pChargeType) {
        switch (pChargeType) {
            case W2Fee:
            case W2BaseFee:
                return true;

            default:
                return false;
        }
    }

    public static OfferingServiceChargeGroup getOfferingServiceChargeGroup(Company pCompany, OfferingCode pOfferingCode, OfferingServiceChargeType pChargeType) {
        OfferingServiceChargeGroup group;

        if (pOfferingCode != null) {
            group = findOfferingServiceChargeGroup(pCompany, pOfferingCode, pChargeType);
        } else {
            group = findFirstOfferingServiceChargeGroup(pCompany, pChargeType);
        }

        return group;
    }

    public static boolean isChargeTypeValidForOffering(Company pCompany, OfferingCode pOfferingCode, OfferingServiceChargeType pChargeType) {
        return getOfferingServiceChargeGroup(pCompany, pOfferingCode, pChargeType) != null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public OfferingServiceChargeGroup()
	{
		super();
	}

    public DomainEntitySet<OfferingServiceCharge> getTiers() {
        Expression<OfferingServiceCharge> query =
                new Query<OfferingServiceCharge>()
                       .Where(OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(this))
                       .OrderBy(OfferingServiceCharge.TierNumber().Descending());

        return Application.find(OfferingServiceCharge.class, query);
    }


    /**
     * For a group with flat pricing, this returns the (only) OfferingServiceCharge in the group.
     * For a group with tiered pricing, this returns the OfferingServiceCharge that is appropriate for the given quantity.
     * @param pQuantity
     * @return The matching OfferingServiceCharge, or null if there are no OfferingServiceCharges in the group, or if
     *         there is more than one tier and none is the default.
     */
    public OfferingServiceCharge selectTier(int pQuantity) {
        OfferingServiceCharge offeringSvcChg = null;
        
        // Get OfferingServiceCharges in this group, sorted by tier number
        Expression<OfferingServiceCharge> query =
                new Query<OfferingServiceCharge>()
                       .Where(OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(this))
                       .OrderBy(OfferingServiceCharge.TierNumber());

        DomainEntitySet<OfferingServiceCharge> charges = Application.find(OfferingServiceCharge.class, query);

        for (OfferingServiceCharge osc : charges) {
            if (pQuantity >= osc.getTierUnits()) {
                offeringSvcChg = osc;
            }
        }

        return offeringSvcChg;
    }

    public DomainEntitySet<OfferingServiceCharge> selectTiers() {
        OfferingServiceCharge offeringSvcChg = null;

        // Get OfferingServiceCharges in this group, sorted by tier number
        Expression<OfferingServiceCharge> query =
                new Query<OfferingServiceCharge>()
                       .Where(OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(this))
                       .OrderBy(OfferingServiceCharge.TierNumber().Descending());

        return Application.find(OfferingServiceCharge.class, query);
    }


    public boolean tierExists(int pTierNumber) {
        DomainEntitySet<OfferingServiceCharge> found =
                Application.find(OfferingServiceCharge.class,
                        OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(this)
                        .And(OfferingServiceCharge.TierNumber().equalTo(pTierNumber)));
        return !found.isEmpty();
    }

    public boolean hasDefaultTier() {
        DomainEntitySet<OfferingServiceCharge> found =
                Application.find(OfferingServiceCharge.class,
                        OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(this)
                        .And(OfferingServiceCharge.TierUnits().equalTo(0)));
        return !found.isEmpty();
    }

}
