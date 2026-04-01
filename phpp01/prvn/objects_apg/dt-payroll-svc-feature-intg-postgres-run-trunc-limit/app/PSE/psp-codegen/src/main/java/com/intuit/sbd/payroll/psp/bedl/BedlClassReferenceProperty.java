package com.intuit.sbd.payroll.psp.bedl;

import com.intuit.sbd.payroll.psp.NameOverrides;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:42:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class BedlClassReferenceProperty extends BedlProperty {
    private List<BedlFilterProperty> filters = null;

    public BedlClassReferenceProperty(String pPropertyName, String pPropertyType, Boolean isNullable, Boolean isOwner, String inversePropertyName, Boolean hasFkColumn) {
        super(pPropertyName, pPropertyType, isNullable);
        this.isOwner = isOwner;
        this.inversePropertyName = inversePropertyName;
        this.hasFkColumn = hasFkColumn;
    }

    @Override
    public ArrayList<String> getPspColumnNames() {
        ArrayList<String> result = new ArrayList<String>();

        addColumnName(result, this.getPropertyName() + "Fk");

        return result;
    }

    public List<BedlFilterProperty> getFilters() {
        if (filters == null) {
            populateFilters();
        }
        return filters;
    }

    protected void populateFilters() {
        filters = new ArrayList<>();
        if(BedlDataEntity.companyFilterClasses.contains(getPropertyType())) {
            filters.add(new BedlFilterProperty("COMPANY_FILTER", "COMPANY_FK=:companySequence"));
        }
        if(BedlDataEntity.dateFilterClasses.contains(getPropertyType())) {
            filters.add(new BedlFilterProperty("DATE_FILTER", "CREATED_DATE>=:createdDate"));
        }
    }

    public String getInverseColumnName() {
        String overridenName = NameOverrides.getOverride(BedlProcessor.getCamelCaseName(inversePropertyName + "Fk"));
        if (overridenName != null) {
            return overridenName;
        } else {
            return BedlProcessor.getCamelCaseName(inversePropertyName + "Fk");
        }      
    }

    public Boolean getIsNotOwner() {
        return !isOwner;
    }

    public Boolean getIsOwner() {
        return isOwner;
    }

    public String getInversePropertyName() {
        return inversePropertyName;
    }

    private Boolean isOwner;
    private String inversePropertyName;
    private Boolean hasFkColumn;

    public Boolean getHasFkColumn() {
        return hasFkColumn;
    }

    public void setHasFkColumn(Boolean hasFkColumn) {
        this.hasFkColumn = hasFkColumn;
    }
}