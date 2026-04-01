package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.*;
import com.intuit.sbd.payroll.psp.common.ConstraintParameters;
import com.intuit.sbd.payroll.psp.common.TableColumn;
import com.intuit.sbd.payroll.psp.common.TableParameters;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 14, 2009
 * Time: 4:45:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTableGenerator {
    private BedlProcessor bedlProcessor;
    private HashMap<String, String> dataTypeMap = new HashMap<String, String>();
    private int MAX_STRING_LENGTH = 4000;
    private int MAX_UNIQUE_ID_LENGTH = 255;
    ArrayList<ConstraintParameters> mAlterTemplateParameters = new ArrayList<ConstraintParameters>();
    ArrayList<ConstraintParameters> mCreateIndexParameters = new ArrayList<ConstraintParameters>();
    private final String NOT_NULL = "NOT NULL";

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        dataTypeMap.put("int", "NUMBER(10,0)");
        dataTypeMap.put("long", "NUMBER(19,0)");
        dataTypeMap.put("boolean", "NUMBER(1,0)");
        dataTypeMap.put("String", "VARCHAR2($ CHAR)");
        dataTypeMap.put("java.sql.Clob", "CLOB");                                
        dataTypeMap.put("double", "NUMBER(19,7)");
        dataTypeMap.put("SpcfCalendar", "TIMESTAMP");
        dataTypeMap.put("SpcfMoney", "NUMBER(19,4)");
        dataTypeMap.put("SpcfDecimal", "NUMBER(19,7)");

        bedlProcessor = pBedlProcessor;
        StringTemplateGroup templates = new StringTemplateGroup("CreateTable", Generator.getTemplateLocation() + "createTables");
        StringTemplate createTableTemplate = templates.getInstanceOf("CreateTables");

        ArrayList<TableParameters> templateParameters = new ArrayList<TableParameters>();

        //Generate Tables for Data Entities
        generateTableParameters(pBedlProcessor.getDataEntities(), templateParameters);

        //Generate Tables for DataObjects
        generateTableParameters(pBedlProcessor.getDataObjects(), templateParameters);

        //Generate Tables for Many to Many Associations
        generateAssocationTables(templateParameters);

        //Add Create Table scripts to template
        createTableTemplate.setAttribute("tables", templateParameters);
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(bedlProcessor.getBedlFolderName() + "CreateTables.sql"));

        //Add Alter Table scripts to the template
        createTableTemplate.setAttribute("alterTables", mAlterTemplateParameters);

        //Add Create Index scripts to template
        createTableTemplate.setAttribute("createIndexes",mCreateIndexParameters);

        outputFile.write(createTableTemplate.toString());

        outputFile.close();
    }

    private void generateTableParameters(List<BedlDataEntity> pDataEntities, ArrayList<TableParameters> pTemplateParameters) {

        for (BedlDataEntity dataEntity : pDataEntities) {
            String className = dataEntity.getClassName();

            TableParameters classTemplateParameters =
                    new TableParameters("PSP_" + bedlProcessor.getPspTableName(className));

            String keyPropertyName = null;
            if (!dataEntity.getIsDataObject()) {
                String columnName = bedlProcessor.getPspTableName(className) + "_SEQ";
                classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(columnName, "VARCHAR2(255 CHAR)", NOT_NULL));
                classTemplateParameters.setPrimaryKey(columnName);
            }

            if (dataEntity.getBaseClass() == null) {
                if (!dataEntity.getIsDataObject()) {
                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("VERSION", "NUMBER(19,0)", NOT_NULL));
                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("CREATOR_ID", "VARCHAR2(30 CHAR)", ""));
                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("CREATED_DATE", "TIMESTAMP", NOT_NULL));

                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("MODIFIER_ID", "VARCHAR2(30 CHAR)", ""));
                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("MODIFIED_DATE", "TIMESTAMP", NOT_NULL));
                    classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("REALM_ID", "NUMBER(19,0) DEFAULT -1", NOT_NULL));
                } else {
                    keyPropertyName = dataEntity.getKeyProperty().getPropertyName();
                }
            }else{
                classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("REALM_ID", "NUMBER(19,0) DEFAULT -1", NOT_NULL));
            }

            //Scalar Properties
            List<BedlScalarProperty> scalarProperties = dataEntity.getScalarProperties();

            for (BedlScalarProperty property : scalarProperties) {
                ArrayList<String> columnNames = property.getPspColumnNames();
                generateTableProperties(property, classTemplateParameters, columnNames.get(0), keyPropertyName);
            }

            //Complex Properties
            List<BedlComplexProperty> complexProperties = dataEntity.getComplexProperties();

            for (BedlComplexProperty property : complexProperties) {
                BedlDataType dataType = bedlProcessor.findDataType(property.getPropertyType());
                ArrayList<String> columnNames = property.getPspColumnNames();

                for (int k = 0; k < columnNames.size(); k++) {
                    BedlScalarProperty scalarProperty = dataType.getScalarProperties().get(k);
                    generateTableProperties(scalarProperty, classTemplateParameters, columnNames.get(k), keyPropertyName);
                }
            }

            for (BedlRelation relation : bedlProcessor.getRelations()) {
                String sourceColumnType = "";
                String targetColumnType = "";
                BedlDataEntity targetDataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(relation.getTargetType());

                //If target is not DataObject then source & taget column type should be VARCHAR2 with Max Unique Id Length
                if (!targetDataEntity.getIsDataObject()) {
                    sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    targetColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                } else {
                    BedlScalarProperty property = targetDataEntity.getKeyProperty();
                    BedlEnumeration enumeration = bedlProcessor.findEnumeration(property.getPropertyType());

                    if (property.getIsString() || enumeration != null) {
                        targetColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    } else {
                        targetColumnType = dataTypeMap.get(property.getPropertyType());
                    }

                    BedlDataEntity sourceDataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(relation.getSourceType());

                    if (!sourceDataEntity.getIsDataObject()) {
                        sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    } else {
                        property = sourceDataEntity.getKeyProperty();
                        enumeration = bedlProcessor.findEnumeration(property.getPropertyType());

                        if (property.getIsString() || enumeration != null) {
                            sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                        } else {
                            sourceColumnType = dataTypeMap.get(property.getPropertyType());
                        }
                    }
                }

                String sourceType = relation.getSourceType();
                String targetType = relation.getTargetType();

                if (className.equals(sourceType)) {
                    //For many-many we use generateAssocationTables method.
                    if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many) && relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many)){
                        continue;
                    }

                    if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many)) {                        
                        String notNullStr = "";
                        if(!relation.getTargetLowerBoundMultiplicity().equals(BedlRelation.Multiplicity.Zero)){
                            notNullStr = NOT_NULL;
                        }

                        String columnName = getOverriddenNameForCamelClassName(relation.getTargetClassName()+ "Fk");
                        classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(columnName, targetColumnType, notNullStr));

                        String constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(relation.getSourceType()),BedlProcessor.getCamelCaseName(relation.getTargetClassName()));

                        ConstraintParameters fk = getForeignKey(classTemplateParameters.getTableName(), constraintName,  columnName, bedlProcessor.getPspTableName(relation.getTargetType()));
                        mAlterTemplateParameters.add(fk);
                        mCreateIndexParameters.add(fk);
                    }

                    if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.One) &&
                            relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.One)) {
                        String notNullStr = "";
                        if(!relation.getTargetLowerBoundMultiplicity().equals(BedlRelation.Multiplicity.Zero)){
                            notNullStr = NOT_NULL;
                        }
                        String columnName = getOverriddenNameForCamelClassName(relation.getTargetClassName() + "Fk");
                        classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(columnName, targetColumnType, notNullStr));

                        //Add FK Constraint
                        String constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(relation.getSourceType()),BedlProcessor.getCamelCaseName(relation.getTargetClassName()));
                        ConstraintParameters fk = getForeignKey(classTemplateParameters.getTableName(), constraintName, columnName, bedlProcessor.getPspTableName(relation.getTargetType()));
                        mAlterTemplateParameters.add(fk);
                        mCreateIndexParameters.add(fk);
                    }
                }

                if (className.equals(targetType)) {
                    if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many) && relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many)){
                        continue;
                    }

                    if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.One) &&
                            relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many)) {
                        String notNullStr = "";
                        if(!relation.getSourceLowerBoundMultiplicity().equals(BedlRelation.Multiplicity.Zero)){
                            notNullStr = NOT_NULL;
                        }

                        String columnName = getOverriddenNameForCamelClassName(relation.getSourceClassName() + "Fk");
                        classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(columnName, targetColumnType, notNullStr));

                        String constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(relation.getTargetType()),BedlProcessor.getCamelCaseName(relation.getSourceClassName()));
                        ConstraintParameters fk = getForeignKey(classTemplateParameters.getTableName(), constraintName, columnName, bedlProcessor.getPspTableName(relation.getSourceType()));
                        

                        mAlterTemplateParameters.add(fk);
                        mCreateIndexParameters.add(fk);
                    }
                }
            }
            
            if (dataEntity.getBaseClass() != null) {
                String columnName = "";
                if (!dataEntity.getIsDataObject()) {
                    columnName = bedlProcessor.getPspTableName(className) + "_SEQ";
                }else{
                    columnName = dataEntity.getKeyProperty().getPropertyName();
                    columnName = getOverriddenNameForCamelClassName(columnName);
                }
                String constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(dataEntity.getClassName()),BedlProcessor.getCamelCaseName(dataEntity.getBaseClass().getClassName()));
                ConstraintParameters fk = getForeignKey(classTemplateParameters.getTableName(), constraintName, columnName, bedlProcessor.getPspTableName(dataEntity.getBaseClass().getClassName()));

                mAlterTemplateParameters.add(fk);
            }

            pTemplateParameters.add(classTemplateParameters);
        }
    }

    private void generateAssocationTables(ArrayList<TableParameters> pTemplateParameters) {
        for (BedlRelation relation : bedlProcessor.getRelations()) {

            if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many) &&
                    relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many)) {

                String tableName = relation.getSourceClassName() + relation.getTargetClassName() + "_ASSOC";
                String sourceColumnType = "";
                String targetColumnType = "";

                TableParameters classTemplateParameters =
                        new TableParameters("PSP_" + bedlProcessor.getPspTableName(tableName));

                String sourceColumnName = getOverriddenNameForCamelClassName(relation.getSourceClassName() + "Fk");
                String targetColumnName = getOverriddenNameForCamelClassName(relation.getTargetClassName() + "Fk");

                BedlDataEntity targetDataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(relation.getTargetType());

                //If target is not DataObject then source & taget column type should be VARCHAR2 with Max Unique Id Length
                if (!targetDataEntity.getIsDataObject()) {
                    sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    targetColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                } else {
                    BedlScalarProperty property = targetDataEntity.getKeyProperty();
                    BedlEnumeration enumeration = bedlProcessor.findEnumeration(property.getPropertyType());

                    if (property.getIsString() || enumeration != null) {
                        targetColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    } else {
                        targetColumnType = dataTypeMap.get(property.getPropertyType());
                    }

                    BedlDataEntity sourceDataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(relation.getSourceType());

                    if (!sourceDataEntity.getIsDataObject()) {
                        sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    } else {
                        property = sourceDataEntity.getKeyProperty();
                        enumeration = bedlProcessor.findEnumeration(property.getPropertyType());

                        if (property.getIsString() || enumeration != null) {
                            sourceColumnType = dataTypeMap.get("String").replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                        } else {
                            sourceColumnType = dataTypeMap.get(property.getPropertyType());
                        }
                    }
                }

                classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(sourceColumnName, sourceColumnType, NOT_NULL));
                classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(targetColumnName, targetColumnType, NOT_NULL));
                classTemplateParameters.getPropertyTemplateParameters().add(new TableColumn("REALM_ID", "NUMBER(19,0) DEFAULT -1", NOT_NULL));
                classTemplateParameters.setPrimaryKey(sourceColumnName + "," + targetColumnName);
                pTemplateParameters.add(classTemplateParameters);

                String constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(relation.getSourceClassName())+BedlProcessor.getCamelCaseName(relation.getTargetClassName()) , BedlProcessor.getCamelCaseName(relation.getSourceClassName()));
                ConstraintParameters fk1 = getForeignKey(classTemplateParameters.getTableName(), constraintName, sourceColumnName, bedlProcessor.getPspTableName(relation.getSourceType()));
                mAlterTemplateParameters.add(fk1);

                constraintName = getFKConstraintName(BedlProcessor.getCamelCaseName(relation.getSourceClassName())+ BedlProcessor.getCamelCaseName(relation.getTargetClassName()), BedlProcessor.getCamelCaseName(relation.getTargetClassName()));
                ConstraintParameters fk2 = getForeignKey(classTemplateParameters.getTableName(), constraintName, targetColumnName, bedlProcessor.getPspTableName(relation.getTargetType()));
                mAlterTemplateParameters.add(fk2);

                mCreateIndexParameters.add(fk1);
                mCreateIndexParameters.add(fk2);
            }
        }

    }

    private void generateTableProperties(BedlScalarProperty pScalarProperty, TableParameters pClassTemplateParameters, String pColumnName, String pKeyPropertyName) {

        String propertyType = pScalarProperty.getPropertyType();
        BedlEnumeration enumeration = bedlProcessor.findEnumeration(propertyType);
        String databasePropertyType = "";

        if (enumeration != null) {
            databasePropertyType = "VARCHAR2(" + MAX_UNIQUE_ID_LENGTH + " CHAR)";
        } else {
            databasePropertyType = dataTypeMap.get(propertyType);
            if (pScalarProperty.getIsString()) {
                if (pScalarProperty.getHasMaxLength()) {
                    databasePropertyType = databasePropertyType.replace("$", String.valueOf(pScalarProperty.getMaxLength()));
                } else {
                    if (pKeyPropertyName != null && pKeyPropertyName.equals(pScalarProperty.getPropertyName())) {
                        databasePropertyType = databasePropertyType.replace("$", String.valueOf(MAX_UNIQUE_ID_LENGTH));
                    } else {
                        databasePropertyType = databasePropertyType.replace("$", String.valueOf(MAX_STRING_LENGTH));
                    }
                }
            }
        }

        if (pKeyPropertyName != null && pKeyPropertyName.equals(pScalarProperty.getPropertyName())) {
            pClassTemplateParameters.setPrimaryKey(pColumnName);
            pClassTemplateParameters.getPropertyTemplateParameters().add(0,new TableColumn(pColumnName, databasePropertyType, NOT_NULL));
            pClassTemplateParameters.getPropertyTemplateParameters().add(1,new TableColumn("VERSION", "NUMBER(19,0)", NOT_NULL));
            pClassTemplateParameters.getPropertyTemplateParameters().add(2,new TableColumn("REALM_ID", "NUMBER(19,0) DEFAULT -1",NOT_NULL));

        } else {
            pClassTemplateParameters.getPropertyTemplateParameters().add(new TableColumn(pColumnName, databasePropertyType, ""));
        }
    }

    private String getOverriddenNameForCamelClassName(String pClassName){
        pClassName = BedlProcessor.getCamelCaseName(pClassName);
        String overridenName = NameOverrides.getOverride(pClassName);

        if(overridenName != null){
            return overridenName;
        }else{
            return pClassName;
        }
    }

    private ConstraintParameters getForeignKey(String pTableName, String pConstraintName, String pConstraintColumnName, String pReferenceClassName){
        ConstraintParameters constraintTemplateParameters = new ConstraintParameters(pTableName);
        constraintTemplateParameters.setForeignKeyConstraintName("PSP_" + pConstraintName);
        constraintTemplateParameters.setForeignKeyColumnName(pConstraintColumnName);
        constraintTemplateParameters.setReferenceTableName("PSP_" + pReferenceClassName);

        return constraintTemplateParameters;
    }

    private String getFKConstraintName(String pFromEntityName, String pToEntityName) {
        String constraintName = (pFromEntityName + "_FK_" + pToEntityName).toUpperCase();
        String overridenName = NameOverrides.getOverride(constraintName);

        if(overridenName != null){
            return overridenName;
        }else{
            return constraintName;
        }        
    }
}
