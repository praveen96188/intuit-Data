package com.intuit.sbd.payroll.psp.bedl;

import com.intuit.sbd.payroll.psp.ClobToStringConfig;
import com.intuit.sbd.payroll.psp.NumericBooleanTypeConfig;

import java.util.ArrayList;

/**
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:42:30 PM
 */
public class BedlScalarProperty extends BedlProperty {
    private int maxLength;
    private String defaultValue;
    private BedlDataEntity bedlDataEntity;
    private boolean inMemoryChangeTracked;
    private boolean optimisticLockingDisabled;

    public BedlScalarProperty(BedlDataEntity pBedlDataEntity, String pPropertyName, String pPropertyType, int pPropertyLength, String pDefaultValue, boolean pInMemoryChangeTracked, boolean pOptimisticLockingDisabled) {
        super(pPropertyName, pPropertyType);
        maxLength = pPropertyLength;
        defaultValue = pDefaultValue;
        bedlDataEntity = pBedlDataEntity;
        inMemoryChangeTracked = pInMemoryChangeTracked;
        optimisticLockingDisabled = pOptimisticLockingDisabled;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getHasMaxLength() {
        return this.maxLength > -1;
    }

    public Boolean getIsString() {
        return getPropertyType().equals("String");
    }

    public Boolean getIsBoolean() {
        return getPropertyType().equals("boolean");
    }

    public Boolean getIsLong() {
        return getPropertyType().equals("long");
    }

    public Boolean getIsInteger() {
        return getPropertyType().equals("int");
    }

    public Boolean getIsDouble() {
        return getPropertyType().equals("double");
    }

    public Boolean getIsClob() {
        return getPropertyType().equals("java.sql.Clob");
    }

    public String getPropertyHbmType() {
        if (getPropertyType().startsWith("Spcf")) return "com.intuit.sbd.payroll.psp.hibernate." + getPropertyType() + "UserType";

        if(getPropertyType().equalsIgnoreCase("boolean")){
            return "org.hibernate.type.NumericBooleanType";
        }

        if(getIsClob()){
            return "org.hibernate.type.TextType";
        }

        //if (getPropertyType().startsWith("Spcf")) return "com.intuit.spc.foundations.subsystemSpecific.dataAccess.objectServices.hibernate." /*"com.intuit.sbd.payroll.psp.hibernate."*/ + getPropertyType() + "UserType";
        return getPropertyType().substring(getPropertyType().lastIndexOf(".") + 1).toLowerCase();
    }


    public Boolean getIsPrimitive(){
        return getPropertyType().equals("boolean") || getPropertyType().equals("long") ||
                getPropertyType().equals("int") || getPropertyType().equals("double");
    }

    public Boolean getIsVersion() {
        return getFirstPspColumnName().equals("VERSION");
    }

    public Boolean getIsKeyProperty() {
        return (bedlDataEntity.getIsDataObject() && bedlDataEntity.getKeyProperty().getPropertyName().equals(this.getPropertyName()))
                || (!bedlDataEntity.getIsDataObject() && this.getPropertyName().equals("Id"));
    }

    public Boolean getIsEnumeration() {
        return bedlDataEntity.getBedlProcessor().findEnumeration(this.getPropertyType()) != null;
    }

    public boolean getInMemoryChangeTracked() {
        return inMemoryChangeTracked;
    }

    @Override
    public ArrayList<String> getPspColumnNames() {
        String suffix = "";
        if (bedlDataEntity instanceof BedlDataType) suffix = bedlDataEntity.getClassName();

        ArrayList<String> result = new ArrayList<String>();

        addColumnName(result, suffix + this.getPropertyName());

        return result;
    }

    public String getPropertyDefaultValue() {
        if (defaultValue != null) {
            return defaultValue;
        }

        if (getPropertyType().equals("long")) {
            return "0L";
        }
        if (getPropertyType().equals("int")) {
            return "0";
        }
        if (getPropertyType().equals("double")) {
            return "0.0d";
        }

        if (getPropertyType().equals("boolean")) {
            return "false";
        }

        if (getPropertyType().equals("SpcfMoney")) {
            return "new SpcfMoney(\"0\")";
        }

        if (getPropertyType().equals("SpcfDecimal")) {
            return "SpcfFactory.getInstance().createDecimal(\"0.00\")";
        }

        return super.getPropertyDefaultValue();
    }

    public boolean getOptimisticLockingDisabled() {
        return optimisticLockingDisabled;
    }

    public void setOptimisticLockingDisabled(boolean optimisticLockingDisabled) {
        this.optimisticLockingDisabled = optimisticLockingDisabled;
    }
}
