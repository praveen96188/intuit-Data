package com.intuit.sbd.payroll.psp.bedl;

/**
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:42:30 PM
 */
public class BedlEnumerationProperty extends BedlScalarProperty {
    public BedlEnumerationProperty(BedlDataEntity pBedlDataEntity, String pPropertyName, String pPropertyType, int pPropertyLength, String pDefaultValue, String pEnumClassName, boolean pOptimisticLockingDisabled) {
        super(pBedlDataEntity, pPropertyName, pPropertyType, pPropertyLength, pDefaultValue, false, pOptimisticLockingDisabled);

        enumClassName = pEnumClassName;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    private String enumClassName;
}