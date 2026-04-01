package com.intuit.sbd.payroll.psp.bedl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:41:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class BedlComplexProperty extends BedlProperty {
    public BedlComplexProperty(String pPropertyName, BedlDataType pComplexDataType) {
        super(pPropertyName, pComplexDataType.getClassName());

        complexDataType = pComplexDataType;
    }

    @Override
    public ArrayList<String> getPspColumnNames() {
        ArrayList<String> result = new ArrayList<String>();


        for (BedlProperty property : complexDataType.getScalarProperties()) {
            String complexDataTypePropertyName = property.getPropertyName();
            String expandedPropertyName = complexDataType.getClassName() + complexDataTypePropertyName;
            addColumnName(result, expandedPropertyName);
        }

        return result;
    }

    @Override
     public String getPropertyDefaultValue() {
        return "new " + getPropertyType() + "()";
    }

    public List<BedlScalarProperty> getScalarProperties() {
        return complexDataType.getScalarProperties();
    }

    private BedlDataType complexDataType;
}
