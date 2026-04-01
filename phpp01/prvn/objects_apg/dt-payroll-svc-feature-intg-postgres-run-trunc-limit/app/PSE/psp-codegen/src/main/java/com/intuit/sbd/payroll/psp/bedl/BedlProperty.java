package com.intuit.sbd.payroll.psp.bedl;

import com.intuit.sbd.payroll.psp.NameOverrides;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:41:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class BedlProperty {
    private String propertyName;
    private String propertyType;
    protected Boolean isNullable;


    public BedlProperty(String pPropertyName, String pPropertyType) {
        this(pPropertyName, pPropertyType, true);
    }

    public BedlProperty(String pPropertyName, String pPropertyType, Boolean pIsNullable) {
        propertyName = pPropertyName;
        propertyType = pPropertyType;
        isNullable = pIsNullable;

        if (propertyType.equals("Money")) {
            propertyType = "SpcfMoney";
        }

        if (propertyType.equals("Decimal")) {
            propertyType = "SpcfDecimal";
        }

        if (propertyType.equals("Date")) {
            propertyType = "SpcfCalendar";
        }

        if (propertyType.equals("LargeText")) {
            propertyType = "java.sql.Clob";
        }

        if (propertyType.equals("Long")) {
            propertyType = "long";
        }

        if (propertyType.equals("Boolean")) {
            propertyType = "boolean";
        }

        if (propertyType.equals("Integer")) {
            propertyType = "int";
        }

        if (propertyType.equals("Double")) {
            propertyType = "double";
        }

    }


    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public Boolean getIsClob() {
        return getPropertyType().equals("java.sql.Clob");
    }

    public String getPropertyReferenceType() {
        if (propertyType.equals("long")) {
            return "Long";
        }

        if (propertyType.equals("boolean")) {
            return "Boolean";
        }

        if (propertyType.equals("int")) {
            return "Integer";
        }

        if (propertyType.equals("double")) {
            return "Double";
        }

        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public ArrayList<String> getPspColumnNames() {
        return new ArrayList<String>();
    }

    public String getFirstPspColumnName() {
        return this.getPspColumnNames().get(0);
    }    

    protected void addColumnName(ArrayList<String> columnNames, String propertyName) {
        String overridenName = NameOverrides.getOverride(BedlProcessor.getCamelCaseName(propertyName));
        if (overridenName != null) {
            columnNames.add(overridenName);
        } else {
            columnNames.add(BedlProcessor.getCamelCaseName(propertyName));
        }
    }

    public String getPropertyDefaultValue() {
        return "null";
    }

    public Boolean getIsNotNullable() {
        return !isNullable;
    }
}
