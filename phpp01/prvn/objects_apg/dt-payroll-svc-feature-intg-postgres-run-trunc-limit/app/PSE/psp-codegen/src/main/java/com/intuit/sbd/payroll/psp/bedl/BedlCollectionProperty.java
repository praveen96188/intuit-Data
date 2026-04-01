package com.intuit.sbd.payroll.psp.bedl;

import java.util.ArrayList;
import java.util.List;

/**
 * User: achaves
 * Date: Nov 3, 2008
 * Time: 7:41:50 PM
 */
public class BedlCollectionProperty extends BedlProperty {
    private BedlDataEntity owner;
    String inversePropertyName;
    Boolean isOwner;
    private String joinTableName;
    private String joinColumnName;
    private String inverseJoinColumnName;
    Boolean isComposition;
    private Boolean isManyToMany;
    private Boolean isSource;
    private String accessorOverride;
    private boolean isAccessorOverride;
    private String memoryChangeTrackedProperty;
    private boolean hasChangeTrackedProperty;
    private int batchSizeForOneToMany = 100;
    private List<BedlFilterProperty> filters = null;

    public BedlCollectionProperty(BedlDataEntity owner, String pPropertyName, String pPropertyType, Boolean isOwner, String inversePropertyName, String joinTableName, String joinColumnName, String inverseJoinColumnName, Boolean isNullable, Boolean isComposition, Boolean isManyToMany, Boolean isSource) {
        super(pPropertyName, pPropertyType);
        this.owner = owner;
        this.isNullable = isNullable;
        this.isOwner = isOwner;
        this.inversePropertyName = inversePropertyName;
        this.joinTableName = joinTableName;
        this.joinColumnName = joinColumnName;
        this.inverseJoinColumnName = inverseJoinColumnName;
        this.isComposition = isComposition;
        this.isManyToMany = isManyToMany;
        this.isSource = isSource;
    }


    @Override
     public String getPropertyDefaultValue() {
        //return "SpcfDataSetFactory.<" + getPropertyType() + ">createInstance(s" + getPropertyName() + "SetTypeParams)";
        return "new DomainEntitySet<" + getPropertyType() + ">()";
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

    public Boolean getIsOwner() {
        return isOwner;
    }

    public String getInversePropertyName() {
        return inversePropertyName;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public String getJoinColumnName() {
        return joinColumnName;
    }

    public String getInverseJoinColumnName() {
        return inverseJoinColumnName;
    }

    public Boolean getIsComposition() {
        return isComposition;
    }

    @Override
    public ArrayList<String> getPspColumnNames() {
        ArrayList<String> result = new ArrayList<String>();
        result.add(joinColumnName);

        return result;
    }

    public Boolean getIsManyToMany() {
        return isManyToMany;
    }

    public Boolean getIsSource() {
        return isSource;
    }

    public String getAccessorOverride() {
        return accessorOverride;
    }

    public void setAccessorOverride(String accessorOverride) {
        this.accessorOverride = accessorOverride;
        isAccessorOverride = accessorOverride != null;
    }

    public boolean getIsAccessorOverride() {
        return isAccessorOverride;
    }

    public BedlDataEntity getOwner() {
        return owner;
    }

    public String getMemoryChangeTrackedProperty() {
        return memoryChangeTrackedProperty;
    }

    public void setMemoryChangeTrackedProperty(String memoryChangeTrackedProperty) {
        this.memoryChangeTrackedProperty = memoryChangeTrackedProperty;
        hasChangeTrackedProperty = memoryChangeTrackedProperty != null;
    }

    public boolean getHasChangeTrackedProperty() {
        return hasChangeTrackedProperty;
    }
    public int getBatchSizeForOneToMany() {
        return batchSizeForOneToMany;
    }

    public void setBatchSizeForOneToMany(int batchSizeForOneToMany) {
        this.batchSizeForOneToMany = batchSizeForOneToMany;
    }
}
