package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class EntitlementCode extends BaseEntitlementCode {

	/**
	 * Default constructor.
	 */
	public EntitlementCode()
	{
		super();
	}

    @Override
    public String toString() {
        if (getAssetItemCd().equals(AssetItemCode.DIYDiskDelivery)) {
            return "Disk Delivery " + getSubtypeDescription();
        } else {
            return getSubtypeDescription();
        }
    }

    public boolean isAssisted() {
        return getAssetItemCd().equals(AssetItemCode.Assisted)
                || getAssetItemCd().equals(AssetItemCode.AssistedAdvantage);
    }

    public boolean isDiamondAssisted() {
        return getSubtypeDescription().contains("Diamond");
    }

    public boolean isEOorER() {
        return getAssetItemCd().equals(AssetItemCode.EmployeeOrganizer)
                || getAssetItemCd().equals(AssetItemCode.EmploymentRegulation);
    }

    public static boolean assetItemNumberUsesUsageBilling( String assetItemNumber ) {
        DomainEntitySet<EntitlementCode> entitlementCodes = Application.find(EntitlementCode.class,
                                                                             AssetItemNumber().equalTo(assetItemNumber)
                                                                             .And(IsUsageBilling().equalTo(true))
                                                                             .And(AssetTypeCd().equalTo(AssetTypeCode.Payroll)));
        // If any Entitlement Codes with this Asset Item Number uses Usage Billing, assume they all do.
        return (entitlementCodes.size() > 0);
    }

    // Mapping for old payroll subtype codes to new quickbooks subtype numbers
    public static long getQuickBooksSubtypeFromPayrollSubtype(PayrollSubtypeCode pPayrollSubtypeCode) {
        if(pPayrollSubtypeCode != null) {
            switch(pPayrollSubtypeCode) {
                case Assisted:
                    return 4;
                case AssistedAdv:
                    return 5;
                case Basic0to3Emp:
                    return 8;
                case BasicLimited:
                    return 6;
                case BasicUnlimited:
                    return 7;
                case Enhanced:
                    return 2;
                case Enhanced0to3Emp:
                    return 12;
                case EnhancedAccountant:
                    return 3;
                case EnhancedUnlimited:
                    return 15;
                case FreeBasic1:
                    return 16;
                case NewBasicUnlimited:
                    return 11;
                case PAPEnhAcct:
                    return 3;
                case Standard:
                    return 1;
            }
        }

        // invalid
        return 0;
    }

    public static EntitlementCode findEntitlementCode(String pAssertItemNumber,
                                                      EditionType pEditionType,
                                                      NumberOfEmployeesType pNumberOfEmployeesType) {
        return findEntitlementCode(pAssertItemNumber, pEditionType, pNumberOfEmployeesType, AssetTypeCode.Payroll);
    }

    public static EntitlementCode findEntitlementCode(String pAssertItemNumber,
                                                      EditionType pEditionType,
                                                      NumberOfEmployeesType pNumberOfEmployeesType,
                                                      AssetTypeCode pAssetTypeCode) {

        Criterion<EntitlementCode> where = EntitlementCode.AssetItemNumber().equalTo(pAssertItemNumber)
                                                          .And(AssetTypeCd().equalTo(pAssetTypeCode));

        if(pEditionType != null) {
            where = where.And(EntitlementCode.EditionType().equalTo(pEditionType));
        } else {
            where = where.And(EntitlementCode.EditionType().isNull());
        }

        if(pNumberOfEmployeesType != null){
            where = where.And(EntitlementCode.NumberOfEmployeesType().equalTo(pNumberOfEmployeesType));
        } else {
            where = where.And(EntitlementCode.NumberOfEmployeesType().isNull());
        }

        Expression<EntitlementCode> query =
                new Query<EntitlementCode>()
                       .Where(where)
                       .OrderBy(Entitlement.CreatedDate());

        DomainEntitySet<EntitlementCode> entitlementCodes = Application.find(EntitlementCode.class, query);

        if(entitlementCodes.size() > 1) {
            throw new RuntimeException("More than one entitlement code exists for AssetItemNumber: " + pAssertItemNumber + " Edition: " + pEditionType + " and NOE: " + pNumberOfEmployeesType);
        } else if(entitlementCodes.size() == 0) {
            return null;
        } else {
            return entitlementCodes.get(0);
        }
    }

    public static EntitlementCode findEntitlementCode(String pAssertItemNumber) {
        Criterion<EntitlementCode> where = EntitlementCode.AssetItemNumber().equalTo(pAssertItemNumber);

        Expression<EntitlementCode> query =
                new Query<EntitlementCode>()
                       .Where(where);

        DomainEntitySet<EntitlementCode> entitlementCodes = Application.find(EntitlementCode.class, query);

        return  entitlementCodes.isEmpty() ? null : entitlementCodes.getFirst();
    }

    public static DomainEntitySet<EntitlementCode> findEntitlementCodes(AssetTypeCode pAssetTypeCode) {
        Criterion<EntitlementCode> where = EntitlementCode.AssetTypeCd().equalTo(pAssetTypeCode);

        Expression<EntitlementCode> query = new Query<EntitlementCode>().Where(where);

        return Application.find(EntitlementCode.class, query);
    }

}