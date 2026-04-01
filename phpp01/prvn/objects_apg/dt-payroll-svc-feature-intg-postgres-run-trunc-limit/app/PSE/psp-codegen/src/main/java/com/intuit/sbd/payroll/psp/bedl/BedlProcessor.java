package com.intuit.sbd.payroll.psp.bedl;

import com.intuit.sbd.payroll.psp.NameOverrides;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: achaves
 * Date: Jan 5, 2008
 * Time: 11:13:07 PM
  */
public class BedlProcessor {

    private Document bedlDoc;
    private String bedlFolderName;
    private ArrayList<BedlDataEntity> dataEntities;
    private ArrayList<BedlDataEntity> dataObjects;
    private ArrayList<BedlDataType> dataTypes;
    private ArrayList<BedlRelation> relations;
    private ArrayList<BedlEnumeration> enumerations;
    public NodeList properties;
    private HashMap<String, String> classNameToPspTableName = new HashMap<String, String>();
    private Map<String, CollectionAccessorOverrideInfo> collectionAccessorOverrides = new HashMap<String, CollectionAccessorOverrideInfo>();
    private List<String> memoryChangeTrackedProperties = new ArrayList<String>();

    public BedlProcessor(String bedlFileName) {
        try {
            File bedlFile = new File(bedlFileName);
            if (!bedlFile.exists())
                throw new RuntimeException("Could not find bedl file: " + bedlFileName);

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            bedlDoc = docBuilder.parse(bedlFile);
            bedlFolderName = bedlFile.getParent() + File.separator;

            collectionAccessorOverrides.put("MoneyMovementTransaction.FinancialTransaction", new CollectionAccessorOverrideInfo("FinancialTransaction.SettlementDate().greaterOrEqualThan(getInitiationDate())", "InitiationDate"));
            collectionAccessorOverrides.put("MoneyMovementTransaction.EntryDetailRecord", new CollectionAccessorOverrideInfo("EntryDetailRecord.InitiationDate().equalTo(getInitiationDate())", "InitiationDate"));

            memoryChangeTrackedProperties.add("MoneyMovementTransaction.InitiationDate");
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Document getBedlDocument() {
        return bedlDoc;
    }

    public String getBedlFolderName() {
        return bedlFolderName;
    }

    public List<BedlDataEntity> getDataEntities() {
        if (dataEntities == null) {
            dataEntities = new ArrayList<BedlDataEntity>();

            NodeList dataEntityList = bedlDoc.getElementsByTagName("DataEntity");
            for (int i = 0; i < dataEntityList.getLength(); i++) {
                dataEntities.add(new BedlDataEntity(this, (Element) dataEntityList.item(i)));
            }
        }
        return dataEntities;
    }

    public List<BedlDataEntity> getDataObjects() {
        if (dataObjects == null) {
            dataObjects = new ArrayList<BedlDataEntity>();

            NodeList dataObjectList = bedlDoc.getElementsByTagName("DataObject");
            for (int i = 0; i < dataObjectList.getLength(); i++) {
                dataObjects.add(new BedlDataEntity(this, (Element) dataObjectList.item(i)));
            }
        }
        return dataObjects;
    }

    public List<BedlDataType> getDataTypes() {
        if (dataTypes == null) {
            dataTypes = new ArrayList<BedlDataType>();

            NodeList dataObjectList = bedlDoc.getElementsByTagName("DataType");
            for (int i = 0; i < dataObjectList.getLength(); i++) {
                dataTypes.add(new BedlDataType(this, (Element) dataObjectList.item(i)));
            }
        }
        return dataTypes;
    }

    public ArrayList<BedlRelation> getRelations() {
        if (relations == null) {
            relations = new ArrayList<BedlRelation>();

            NodeList relationList = bedlDoc.getElementsByTagName("Relation");
            for (int i = 0; i < relationList.getLength(); i++) {
                relations.add(new BedlRelation((Element) relationList.item(i)));
            }
        }
        return relations;
    }

    public NodeList getProperties() {
        if (properties == null) {
            properties = bedlDoc.getElementsByTagName("Property");
        }
        return properties;
    }

    public ArrayList<BedlEnumeration> getEnumerations() {
        if (enumerations == null) {
            enumerations = new ArrayList<BedlEnumeration>();

            NodeList enumerationList = bedlDoc.getElementsByTagName("Enumeration");
            for (int i = 0; i < enumerationList.getLength(); i++) {
                enumerations.add(new BedlEnumeration((Element) enumerationList.item(i)));
            }
        }
        return enumerations;
    }

    public static String getCamelCaseName(String propertyName) {
        char[] ClassNameCharArray = propertyName.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean needsUnderscore = false;
        for (char c : ClassNameCharArray) {
            if (Character.isUpperCase(c)) {
                if (needsUnderscore) {
                    sb.append('_');
                }
                sb.append(c);
                needsUnderscore = true;
            } else {
                sb.append(Character.toUpperCase(c));
            }
        }

        return sb.toString();
    }

    public String getPspTableName(String className) {
        if (!classNameToPspTableName.containsKey(className)) {
            String overridenName = NameOverrides.getOverride(className.toUpperCase());
            if (overridenName != null) {
                classNameToPspTableName.put(className, overridenName);
            } else {
                classNameToPspTableName.put(className, getPSPTableNameWithoutOverride(className));
            }
        }
        return classNameToPspTableName.get(className);
    }

    public String getCamelOverriddenName(String pClassName) {
        pClassName = BedlProcessor.getCamelCaseName(pClassName);
        String overridenName = NameOverrides.getOverride(pClassName);

        if (overridenName != null) {
            return overridenName;
        } else {
            return pClassName;
        }
    }

    public String getPspTriggerName(String triggerName) {
        String overridenName = NameOverrides.getOverride(triggerName);
        if (overridenName != null) {
            return overridenName;
        } else {
            return triggerName;
        }

    }

    public BedlDataEntity findDataEntityOrDataObjectOrDataType(String name) {
        for (BedlDataEntity dataEntity : getDataEntities()) {
            if (dataEntity.getClassName().equals(name)) {
                return dataEntity;
            }
        }
        for (BedlDataEntity dataObject : getDataObjects()) {
            if (dataObject.getClassName().equals(name)) {
                return dataObject;
            }
        }
        for (BedlDataType dataType : getDataTypes()) {
            if (dataType.getClassName().equals(name)) {
                return dataType;
            }
        }
        return null;
    }

    public BedlDataType findDataType(String name) {
        for (BedlDataType dataType : getDataTypes()) {
            if (dataType.getClassName().equals(name)) {
                return dataType;
            }
        }
        return null;
    }

    public BedlEnumeration findEnumeration(String name) {
        for (BedlEnumeration enumeration : getEnumerations()) {
            if (enumeration.getEnumerationName().equals(name)) {
                return enumeration;
            }
        }
        return null;
    }

    public String getSchemaVersion() {
        Element schemaVersionElement = (Element) bedlDoc.getElementsByTagName("DomainModel").item(0);
        return schemaVersionElement.getAttribute("SchemaVersion");
    }

    public String getPSPTableNameWithoutOverride(String pClassName) {
        char[] ClassNameCharArray = pClassName.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean previousCharacterIsUpperCase = true;
        for (char c : ClassNameCharArray) {
            if (Character.isUpperCase(c)) {
                if (!previousCharacterIsUpperCase) {
                    sb.append('_');
                }
                sb.append(c);
                previousCharacterIsUpperCase = true;
            } else {
                sb.append(Character.toUpperCase(c));
                previousCharacterIsUpperCase = false;
            }
        }

        return sb.toString();
    }

    public Map<String, CollectionAccessorOverrideInfo> getCollectionAccessorOverrides() {
        return collectionAccessorOverrides;
    }

    public List<String> getMemoryChangeTrackedProperties() {
        return memoryChangeTrackedProperties;
    }

    public class CollectionAccessorOverrideInfo {
        public CollectionAccessorOverrideInfo(String query) {
            this.query = query;
        }

        public CollectionAccessorOverrideInfo(String query, String memoryTrackProperty) {
            this.query = query;
            this.memoryTrackProperty = memoryTrackProperty;
        }

        private String query;
        private String memoryTrackProperty;

        public String getQuery() {
            return query;
        }

        public String getMemoryTrackProperty() {
            return memoryTrackProperty;
        }
    }
}

