package com.intuit.sbd.payroll.psp.bedl;

import com.intuit.sbd.payroll.psp.ClobToStringConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * A wrapper for a DataEntity DOM node from the generated bedl file
 */
public class BedlDataEntity {

    public static List<String> companyFilterClasses = Arrays.asList("CompanyEvent", "CompanyEventDetail", "CompanyEventEmailParam",
            "Compensation", "Deduction", "DisburseAdviceTaxLiab",
            "EntryDetailRecord", "FinancialTransactionState", "FinancialTransaction",
            "LedgerBalance", "MoneyMovementTransaction",
            "Paycheck", "PaycheckSplit", "Paystub", "PropertyAudit", "PstubEmployeeInfo",
            "PstubPaidTimeoffItem", "PstubPayItem", "QbdtPaycheckInfo",
            "QbdtPaylineInfo", "QbdtTransactionInfo", "Tax",

            "ATFPaymentsToProcess", "CompanyEventEmail", "CompanyNote",
            "EdiPaymentDetail", "EftpsPaymentDetail", "EmployerContribution",
            "EventAs400Sync", "FraudEvent", "FsetFilingDetail",
            "PaymentBatchAssoc", "PstubDDItem", "PstubMsg",
            "TaxPaymentOnHoldReason", "ThirdParty401kBatchPaycheck", "ThirdParty401kPaycheck",
            "TransactionReturn", "VoidedCheck", "WorkersCompPaycheck");

    public static List<String> partitionedParentClasses = Arrays.asList("CompanyAdjustmentSubmission", "LiabilityAdjustment", "LiabilityCheck",
            "PayrollRun", "PriorPaymentSubmission", "QbdtPayrollTransaction");
    public static List<String> dateFilterClasses = Arrays.asList("EntityUpdate");

    public BedlDataEntity(BedlProcessor pBedlProcessor, Element pDataEntity) {
        this.bedlProcessor = pBedlProcessor;
        this.dataEntity = pDataEntity;
        className = pDataEntity.getAttribute("Name");

        NodeList nodes = pDataEntity.getElementsByTagName("InternalNotes");
        if (nodes.getLength() > 0) {
            internalNotesText = nodes.item(0).getTextContent();
            if (internalNotesText != null) {
                noteSpecMap = new HashMap<String, BedlInternalNoteSpec>(3);
                String[] noteSpecs = internalNotesText.split(";");
                for (String noteSpec : noteSpecs) {
                    BedlInternalNoteSpec spec = new BedlInternalNoteSpec(noteSpec);
                    noteSpecMap.put(spec.getSpec(), spec);
                }
            }
        }
    }

    public String getModelVersion() {
        return "1.0"; //todo
    }

    public BedlDataEntity getBaseClass() {
        Element specializes = (Element) dataEntity.getElementsByTagName("Specializes").item(0);
        if (specializes != null) {
            String baseClassName = specializes.getAttribute("Type");
            return bedlProcessor.findDataEntityOrDataObjectOrDataType(baseClassName);
        }
        return null;
    }

    public Collection<BedlDataEntity> getDerivedClasses() {
        if (derivedClasses == null) {
            populateDerivedClasses();
        }

        return derivedClasses;

    }

    public String getBaseClassName() {
        return getBaseClass() == null ? null : getBaseClass().getClassName();
    }

    public boolean getIsDataObject() {
        return getKeyProperty() != null;
    }

    public boolean getIsComplexObject() {
        return false;
    }

    public boolean getIsEntity() {
        return !getIsDataObject() && !getIsComplexObject();
    }

    public String getBaseClassNameIncludingDomainEntity() {
        String baseClass = "DomainEntity";
        if (getIsDataObject()) {
            baseClass = "DataObject";
        }
        if (getBaseClass() != null) {
            baseClass = "domain." + getBaseClass().getClassName();
        }

        return baseClass;
    }

    public String getClassName() {
        return className;
    }

    public String getPspTableName() {
        return bedlProcessor.getPspTableName(className);
    }

    public String getInternalNotesText() {
        return internalNotesText;
    }

    public Collection<BedlInternalNoteSpec> getInternalNoteSpecs() {
        if (noteSpecMap == null)
            return new ArrayList<BedlInternalNoteSpec>();
        return noteSpecMap.values();
    }

    public boolean hasInternalNotes() {
        return internalNotesText != null;
    }

    public boolean hasInternalNoteSpec(String spec) {
        if (noteSpecMap == null)
            return false;
        return noteSpecMap.containsKey(spec);
    }

    public BedlInternalNoteSpec getInternalNoteSpec(String spec) {
        if (noteSpecMap == null)
            return null;
        return noteSpecMap.get(spec);
    }

    public boolean supportsVersioning() {
        BedlInternalNoteSpec versioningSpec = getInternalNoteSpec(BedlInternalNoteSpec.VERSIONING);
        if (versioningSpec == null)
            return true;

        String value = versioningSpec.getValue();
        return (Boolean.TRUE.equals(Boolean.valueOf(value)) || value.equalsIgnoreCase("on"));
    }

    public boolean getIsDynamicUpdate() {
        return getHasClob() ||
                (hasInternalNoteSpec(BedlInternalNoteSpec.DYNAMIC_UPDATE) && getInternalNoteSpec(BedlInternalNoteSpec.DYNAMIC_UPDATE).getValue().equalsIgnoreCase("true"));
    }

    public boolean getHasClob() {
        for (BedlScalarProperty bedlScalarProperty : getScalarProperties()) {
            if (bedlScalarProperty.getIsClob()) {
                return true;
            }
        }
        return false;
    }


    public BedlScalarProperty getKeyProperty() {
        if (keyProperty == null) {
            NodeList properties = dataEntity.getElementsByTagName("KeyProperty");
            if (properties.getLength() > 0) {
                String keyPropertyName = properties.item(0).getTextContent();
                keyProperty = (BedlScalarProperty) findProperty(keyPropertyName);
            }
        }

        return keyProperty;
    }

    public String getKeyPropertyName() {
        if (getKeyProperty() != null) return getKeyProperty().getPropertyName();
        return bedlProcessor.getPspTableName(this.getClassName()) + "_SEQ";
    }

    public BedlProperty findProperty(String propertyName) {
        for (BedlScalarProperty property : getScalarProperties()) {
            if (property.getPropertyName().equals(propertyName)) {
                return property;
            }
        }

        for (BedlComplexProperty property : getComplexProperties()) {
            if (property.getPropertyName().equals(propertyName)) {
                return property;
            }
        }

        return null;
    }

    public List<BedlScalarProperty> getScalarProperties() {
        if (scalarProperties == null) {
            populateScalarAndComplexProperties();
        }

        return scalarProperties;
    }

    public List<BedlFilterDefintionProperty> getFilterDefinitions() {
        if (filterDefinitions == null) {
            populateFilterDefinitions();
        }

        return filterDefinitions;
    }

    public List<BedlFilterProperty> getFilters() {
        if (filters == null) {
            populateFilters();
        }
        return filters;
    }

    public List<BedlComplexProperty> getComplexProperties() {
        if (complexProperties == null) {
            populateScalarAndComplexProperties();
        }

        return complexProperties;
    }

    protected void populateFilterDefinitions() {
        filterDefinitions = new ArrayList<>();
        if(getClassName().equals("Company") || getClassName().equals("Employee")) {
            BedlFilterDefintionProperty.FilterParam filterParam = new BedlFilterDefintionProperty.FilterParam("isDgAssociated", "int");
            List<BedlFilterDefintionProperty.FilterParam>  filterParams = Arrays.asList(filterParam);
            filterDefinitions.add(new BedlFilterDefintionProperty("defaultFilter", filterParams));
        }

        if(companyFilterClasses.contains(getClassName())) {
            BedlFilterDefintionProperty.FilterParam filterParam = new BedlFilterDefintionProperty.FilterParam("companySequence", "com.intuit.sbd.payroll.psp.hibernate.SpcfUniqueIdUserType");
            List<BedlFilterDefintionProperty.FilterParam>  filterParams = Arrays.asList(filterParam);
            filterDefinitions.add(new BedlFilterDefintionProperty("COMPANY_FILTER", filterParams));
        }

        if(dateFilterClasses.contains(getClassName())) {
            BedlFilterDefintionProperty.FilterParam filterParam = new BedlFilterDefintionProperty.FilterParam("createdDate", "com.intuit.sbd.payroll.psp.hibernate.SpcfCalendarUserType");
            List<BedlFilterDefintionProperty.FilterParam>  filterParams = Arrays.asList(filterParam);
            filterDefinitions.add(new BedlFilterDefintionProperty("DATE_FILTER", filterParams));
        }
    }

    protected void populateFilters() {
        filters = new ArrayList<>();
        if(getClassName().equals("Company") || getClassName().equals("Employee")) {
            filters.add(new BedlFilterProperty("defaultFilter", "IS_DG_DISASSOCIATED=:isDgAssociated"));
        }
        if(companyFilterClasses.contains(getClassName())) {
            filters.add(new BedlFilterProperty("COMPANY_FILTER", "COMPANY_FK=:companySequence"));
        }
        if(dateFilterClasses.contains(getClassName())) {
            filters.add(new BedlFilterProperty("DATE_FILTER", "CREATED_DATE>=:createdDate"));
        }
    }

    protected void populateScalarAndComplexProperties() {
        scalarProperties = new ArrayList<BedlScalarProperty>();
        complexProperties = new ArrayList<BedlComplexProperty>();

        NodeList properties = dataEntity.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);

            String propertyName = property.getAttributes().getNamedItem("Name").getNodeValue();
            String propertyDataTypeName = ((Element) property.getElementsByTagName("Type").item(0)).getAttribute("Type");

            BedlDataType complexType = bedlProcessor.findDataType(propertyDataTypeName);
            if (complexType != null) {
                complexProperties.add(new BedlComplexProperty(propertyName, complexType));
            } else {
                scalarProperties.add(createScalarProperty(property, propertyName, propertyDataTypeName));
            }
        }
    }

    private BedlScalarProperty createScalarProperty(Element property, String propertyName, String propertyDataTypeName) {
        int propertyLength = -1;
        if (property.getAttributes().getNamedItem("Length") != null) {
            propertyLength = new Integer(property.getAttributes().getNamedItem("Length").getNodeValue());
        }

        String defaultValue = null;
        BedlEnumeration enumeration = bedlProcessor.findEnumeration(propertyDataTypeName);
        if (property.getElementsByTagName("InitialValue").getLength() > 0) {
            defaultValue = property.getElementsByTagName("InitialValue").item(0).getTextContent();
            if (enumeration != null) {
                defaultValue = "com.intuit.sbd.payroll.psp.domain." + enumeration.getEnumerationName() + "." + defaultValue;
            }
        } else {
            if(hasInternalNoteSpec(BedlInternalNoteSpec.NULL_ENUMS) && Arrays.asList(getInternalNoteSpec(BedlInternalNoteSpec.NULL_ENUMS).getValues()).contains(propertyName)) {
                defaultValue = null;
            } else if (enumeration != null) {
                defaultValue = "com.intuit.sbd.payroll.psp.domain." + enumeration.getEnumerationName() + "." + enumeration.getEnumerationValues().get(0);
            }
        }

        boolean optimisticLockingDisabled = false;
        if (hasInternalNoteSpec(BedlInternalNoteSpec.VERSIONING)) {
            optimisticLockingDisabled = Arrays.asList(getInternalNoteSpec(BedlInternalNoteSpec.VERSIONING).getValues()).contains("-" + propertyName);
        }

        if (enumeration != null) {
            return new BedlEnumerationProperty(this, propertyName, propertyDataTypeName, propertyLength, defaultValue, "com.intuit.sbd.payroll.psp.domain." + enumeration.getEnumerationName(), optimisticLockingDisabled);
        } else {
            boolean inMemoryChangeTracked = bedlProcessor.getMemoryChangeTrackedProperties().contains(this.getClassName() + "." + propertyName);
            return new BedlScalarProperty(this, propertyName, propertyDataTypeName, propertyLength, defaultValue, inMemoryChangeTracked, optimisticLockingDisabled);
        }
    }

    public Collection<BedlClassReferenceProperty> getOneToOneProperties() {
        if (oneToOneProperties == null) {
            populateRelationProperties();
        }
        return oneToOneProperties;
    }

    public Collection<BedlClassReferenceProperty> getManyToOneProperties() {
        if (manyToOneProperties == null) {
            populateRelationProperties();
        }
        return manyToOneProperties;
    }

    public Collection<BedlCollectionProperty> getOneToManyProperties() {
        if (oneToManyProperties == null) {
            populateRelationProperties();
        }
        return oneToManyProperties;
    }

    public Collection<BedlCollectionProperty> getManyToManyProperties() {
        if (manyToManyProperties == null) {
            populateRelationProperties();
        }
        return manyToManyProperties;
    }

    public Collection<BedlClassReferenceProperty> getOneToOneRelations() {
        if (oneToOneRelations == null) {
            populateRelationProperties();
        }
        return oneToOneRelations;
    }

    public Collection<BedlClassReferenceProperty> getManyToOneRelations() {
        if (manyToOneRelations == null) {
            populateRelationProperties();
        }
        return manyToOneRelations;
    }

    public Collection<BedlCollectionProperty> getOneToManyRelations() {
        if (oneToManyRelations == null) {
            populateRelationProperties();
        }
        return oneToManyRelations;
    }

    public Collection<BedlCollectionProperty> getManyToManyRelations() {
        if (manyToManyRelations == null) {
            populateRelationProperties();
        }
        return manyToManyRelations;
    }

    public Set<String> getFilteredCollections() {
        if (filteredCollections == null) {
            populateRelationProperties();
        }
        return filteredCollections;
    }

    public boolean getHasFilteredCollections() {
        return !getFilteredCollections().isEmpty();
    }

    public boolean getIsTenantInfoRequired() {
        return companyFilterClasses.contains(getClassName());
    }

    public boolean getIsPartitionedParentTenantInfoRequired() {
        return partitionedParentClasses.contains(getClassName());
    }

    private void populateRelationProperties() {
        oneToOneProperties = new ArrayList<BedlClassReferenceProperty>();
        manyToOneProperties = new ArrayList<BedlClassReferenceProperty>();
        oneToManyProperties = new ArrayList<BedlCollectionProperty>();
        manyToManyProperties = new ArrayList<BedlCollectionProperty>();

        oneToOneRelations = new ArrayList<BedlClassReferenceProperty>();
        manyToOneRelations = new ArrayList<BedlClassReferenceProperty>();
        oneToManyRelations = new ArrayList<BedlCollectionProperty>();
        manyToManyRelations = new ArrayList<BedlCollectionProperty>();

        filteredCollections = new HashSet<String>();

        /*
            <Relation Type="Association" Direction="SourceToTarget">
      <Source>
        <Type Type="Company" />
        <Multiplicity LowerBound="0" UpperBound="*" />
      </Source>
      <Target Role="FundingModel">
        <Type Type="FundingModel" />
        <Multiplicity UpperBound="1" />
      </Target>
    </Relation>

    <Relation Type="Association" Direction="Bidirectional">
      <Source Role="OriginalTransaction">
        <Type Type="FinancialTransaction" />
        <Multiplicity LowerBound="0" UpperBound="1" />
      </Source>
      <Target Role="AssociatedTransactions">
        <Type Type="FinancialTransaction" />
        <Multiplicity LowerBound="0" UpperBound="*" />
      </Target>
    </Relation>

            */
        if (getClassName().equals("FinancialTransaction") ||                
                getClassName().equals("Company1")) {
            int i = 0;
        }
        for (BedlRelation relation : bedlProcessor.getRelations()) {
            if (relation.getSourceType().equals(getClassName())) {
                switch (relation.getSourceUpperBoundMultiplicity()) {
                    case Many:
                        if (relation.getTargetUpperBoundMultiplicity() == BedlRelation.Multiplicity.Many) {
                            Boolean isOwner = determineRelationshipOwner(relation) == BedlRelation.RelationshipSide.Source;
                            String joinTableName = "PSP_" + bedlProcessor.getPspTableName(relation.getSourceClassName() + relation.getTargetClassName() + "_ASSOC");
                            String joinColumnName = bedlProcessor.getCamelOverriddenName(relation.getTargetClassName() + "Fk");
                            String inverseJoinColumnName = bedlProcessor.getCamelOverriddenName(relation.getSourceClassName() + "Fk");
                            Boolean isNullable = relation.getSourceLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;

                            BedlCollectionProperty collectionProperty = new BedlCollectionProperty(this, relation.getTargetClassName(), relation.getTargetType(), isOwner, relation.getSourceClassNameForFk(), joinTableName, joinColumnName, inverseJoinColumnName, isNullable, false, true, true);

                            manyToManyRelations.add(collectionProperty);
                            if (relation.getDirection() != BedlRelation.Direction.TargetToSource) {
                                manyToManyProperties.add(collectionProperty);
                            }
                        } else {
                            Boolean isNullable = relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;

                            BedlClassReferenceProperty bedlClassReferenceProperty = new BedlClassReferenceProperty(relation.getTargetClassName(), relation.getTargetType(), isNullable, true, "", false);

                            manyToOneRelations.add(bedlClassReferenceProperty);
                            if (relation.getDirection() != BedlRelation.Direction.TargetToSource) {
                                manyToOneProperties.add(bedlClassReferenceProperty);
                            }
                        }
                        break;
                    case One:
                        if (relation.getTargetUpperBoundMultiplicity() == BedlRelation.Multiplicity.Many) {
                            String joinColumnName = bedlProcessor.getCamelOverriddenName(relation.getSourceClassName() + "Fk");
                            Boolean isNullable = relation.getSourceLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;
                            Boolean isComposition = relation.getRelationType() == BedlRelation.RelationType.Composition;

                            BedlCollectionProperty collectionProperty = new BedlCollectionProperty(this, relation.getTargetClassName(), relation.getTargetType(), false, relation.getSourceClassNameForFk(), "", joinColumnName, "", isNullable, isComposition, false, true);

                            String overrideKey = relation.getSourceClassName() + "." + relation.getTargetClassName();
                            if (bedlProcessor.getCollectionAccessorOverrides().containsKey(overrideKey)) {
                                collectionProperty.setAccessorOverride(bedlProcessor.getCollectionAccessorOverrides().get(overrideKey).getQuery());
                                collectionProperty.setMemoryChangeTrackedProperty(bedlProcessor.getCollectionAccessorOverrides().get(overrideKey).getMemoryTrackProperty());
                                filteredCollections.add(collectionProperty.getPropertyName());
                            }

                            oneToManyRelations.add(collectionProperty);
                            if (relation.getDirection() != BedlRelation.Direction.TargetToSource) {
                                oneToManyProperties.add(collectionProperty);
                            }
                        } else {
                            Boolean isNullable = relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;
                            Boolean isOwner = determineRelationshipOwner(relation) == BedlRelation.RelationshipSide.Source;

                            BedlClassReferenceProperty bedlClassReferenceProperty = new BedlClassReferenceProperty(relation.getTargetClassName(), relation.getTargetType(), isNullable, isOwner, relation.getSourceClassNameForFk(), false);

                            oneToOneRelations.add(bedlClassReferenceProperty);
                            if (relation.getDirection() != BedlRelation.Direction.TargetToSource) {
                                if (!isOwner) {
                                    System.out.println("one-to-one implemented as collection: " + relation.getSourceClassName() + "->" + relation.getTargetClassName());
                                }

                                oneToOneProperties.add(bedlClassReferenceProperty);
                            }
                            //if (relation.getDirection() == BedlRelation.Direction.Bidirectional) System.out.println("Bidirectional one-to-one: " + relation.getSourceClassName() + " " + relation.getTargetClassName());
                        }
                        break;
                }
            }

            if (relation.getTargetType().equals(getClassName())) {
                switch (relation.getTargetUpperBoundMultiplicity()) {
                    case Many:
                        if (relation.getSourceUpperBoundMultiplicity() == BedlRelation.Multiplicity.Many) {
                            Boolean isOwner = determineRelationshipOwner(relation) == BedlRelation.RelationshipSide.Target;
                            String joinTableName = "PSP_" + bedlProcessor.getPspTableName(relation.getSourceClassName() + relation.getTargetClassName() + "_ASSOC");
                            String joinColumnName = bedlProcessor.getCamelOverriddenName(relation.getTargetClassName() + "Fk");
                            String inverseJoinColumnName = bedlProcessor.getCamelOverriddenName(relation.getSourceClassName() + "Fk");
                            Boolean isNullable = relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;

                            BedlCollectionProperty collectionProperty = new BedlCollectionProperty(this, relation.getSourceClassName(), relation.getSourceType(), isOwner, relation.getTargetClassNameForFk(), joinTableName, joinColumnName, inverseJoinColumnName, isNullable, false, true, false);

                            manyToManyRelations.add(collectionProperty);
                            if (relation.getDirection() != BedlRelation.Direction.SourceToTarget) {
                                manyToManyProperties.add(collectionProperty);
                            }
                            //if (relation.getDirection() == BedlRelation.Direction.Bidirectional) System.out.println("Bidirectional many-to-many: " + relation.getSourceClassName() + " " + relation.getTargetClassName());
                        } else {
                            Boolean isNullable = relation.getSourceLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;

                            BedlClassReferenceProperty bedlClassReferenceProperty = new BedlClassReferenceProperty(relation.getSourceClassName(), relation.getSourceType(), isNullable, true, "", false);

                            manyToOneRelations.add(bedlClassReferenceProperty);
                            if (relation.getDirection() != BedlRelation.Direction.SourceToTarget) {
                                manyToOneProperties.add(bedlClassReferenceProperty);
                            }
                        }
                        break;
                    case One:
                        if (relation.getSourceUpperBoundMultiplicity() == BedlRelation.Multiplicity.Many) {
                            String joinColumnName = bedlProcessor.getCamelOverriddenName(relation.getTargetClassName() + "Fk");
                            Boolean isNullable = relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;
                            Boolean isComposition = relation.getRelationType() == BedlRelation.RelationType.Composition;

                            BedlCollectionProperty collectionProperty = new BedlCollectionProperty(this, relation.getSourceClassName(), relation.getSourceType(), false, relation.getTargetClassNameForFk(), "", joinColumnName, "", isNullable, isComposition, false, false);
                            if("PstubPayItem".equals(relation.getSourceClassName())) {
                                collectionProperty.setBatchSizeForOneToMany(50);
                            }
                            String overrideKey = relation.getTargetClassName() + "." + relation.getSourceClassName();
                            if (bedlProcessor.getCollectionAccessorOverrides().containsKey(overrideKey)) {
                                collectionProperty.setAccessorOverride(bedlProcessor.getCollectionAccessorOverrides().get(overrideKey).getQuery());
                                collectionProperty.setMemoryChangeTrackedProperty(bedlProcessor.getCollectionAccessorOverrides().get(overrideKey).getMemoryTrackProperty());
                                filteredCollections.add(collectionProperty.getPropertyName());
                            }

                            oneToManyRelations.add(collectionProperty);
                            if (relation.getDirection() != BedlRelation.Direction.SourceToTarget) {
                                oneToManyProperties.add(collectionProperty);
                            }
                        } else {
                            Boolean isNullable = relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.Zero;
                            Boolean isOwner = determineRelationshipOwner(relation) == BedlRelation.RelationshipSide.Target;

                            BedlClassReferenceProperty bedlClassReferenceProperty = new BedlClassReferenceProperty(relation.getSourceClassName(), relation.getSourceType(), isNullable, isOwner, relation.getTargetClassNameForFk(), true);

                            oneToOneRelations.add(bedlClassReferenceProperty);
                            if (relation.getDirection() != BedlRelation.Direction.SourceToTarget) {
                                if (!isOwner) {
                                    System.out.println("one-to-one implemented as collection: " + relation.getTargetClassName() + "->" + relation.getSourceClassName());
                                }
                                oneToOneProperties.add(bedlClassReferenceProperty);
                            }
                            //if (relation.getDirection() == BedlRelation.Direction.Bidirectional) System.out.println("Bidirectional one-to-one: " + relation.getSourceClassName() + " " + relation.getTargetClassName());
                        }
                        break;
                }
            }

        }
    }

    private void populateDerivedClasses() {
        derivedClasses = new ArrayList<BedlDataEntity>();

        for (BedlDataEntity dataEntity : bedlProcessor.getDataEntities()) {
            if (this.equals(dataEntity.getBaseClass())) {
                derivedClasses.add(dataEntity);
            }
        }
    }

    private BedlRelation.RelationshipSide determineRelationshipOwner(BedlRelation relation) {
        switch (relation.getDirection()) {
            case SourceToTarget:
                return BedlRelation.RelationshipSide.Source;
            case TargetToSource:
                return BedlRelation.RelationshipSide.Target;
            case Bidirectional: {
                return BedlRelation.RelationshipSide.Source;
                /*
                if (relation.getSourceLowerBoundMultiplicity() == BedlRelation.Multiplicity.One) {
                   return BedlRelation.RelationshipSide.Source;
                }
                else {
                    if (relation.getTargetLowerBoundMultiplicity() == BedlRelation.Multiplicity.One) {
                        return BedlRelation.RelationshipSide.Target;
                    }
                    else {
                        return BedlRelation.RelationshipSide.Source;
                    }
                }
                */
            }
            default:
                throw new RuntimeException("Couldn't determine relationship owner");
        }
    }

    public BedlProcessor getBedlProcessor() {
        return bedlProcessor;
    }

    private static HashMap<String, String> classNameToPspTableName = new HashMap<String, String>();

    private BedlProcessor bedlProcessor;
    private Element dataEntity;
    private String className;
    private String modelVersion;
    private String internalNotesText;
    private HashMap<String, BedlInternalNoteSpec> noteSpecMap = null;
    private ArrayList<String> propertyNames = null;
    protected ArrayList<BedlScalarProperty> scalarProperties = null;
    protected ArrayList<BedlFilterDefintionProperty> filterDefinitions = null;
    protected ArrayList<BedlFilterProperty> filters = null;
    private ArrayList<BedlComplexProperty> complexProperties = null;
    private ArrayList<BedlClassReferenceProperty> oneToOneProperties = null;
    private ArrayList<BedlClassReferenceProperty> manyToOneProperties = null;
    private ArrayList<BedlCollectionProperty> oneToManyProperties = null;
    private ArrayList<BedlCollectionProperty> manyToManyProperties = null;
    private ArrayList<BedlClassReferenceProperty> oneToOneRelations = null;
    private ArrayList<BedlClassReferenceProperty> manyToOneRelations = null;
    private ArrayList<BedlCollectionProperty> oneToManyRelations = null;
    private ArrayList<BedlCollectionProperty> manyToManyRelations = null;
    private ArrayList<BedlDataEntity> derivedClasses = null;
    private BedlScalarProperty keyProperty = null;
    private Set<String> filteredCollections;
}

