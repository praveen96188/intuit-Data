package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jan 5, 2008
 * Time: 7:50:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditTriggerGenerator {
    private BedlProcessor bedlProcessor;
    private String newSyntax;
    private String rowNum;
    public void generateCode(BedlProcessor pBedlProcessor, DatabaseType databaseType) throws IOException {
        bedlProcessor = pBedlProcessor;
        String baseFolderPath = Generator.getDatabaseSpecificLocation(pBedlProcessor.getBedlFolderName(), databaseType);
        // Set up template engine
        StringTemplateGroup templates = new StringTemplateGroup("TriggerGroup", Generator.getDatabaseSpecificLocation(Generator.getTemplateLocation().concat("auditTrigger"), databaseType));
        StringTemplate t = templates.getInstanceOf("CreateTriggers");
        ArrayList<ClassTemplateParameters> templateParameters = new ArrayList<ClassTemplateParameters>();

        /**
         <DataEntity Name="Company" AllowCustomization="false" AllowDynamicSpecialization="false">
         <InternalNotes>AuditAssociations(FundingModel)</InternalNotes>

         Process the columns to be audited
         */
        /** :NEW - oracle , NEW - Postgres */
        newSyntax= (databaseType==DatabaseType.ORACLE)?":NEW":"NEW";
        /** ROWNUM - oracle , LIMIT - Postgres */
        rowNum = (databaseType==DatabaseType.ORACLE)?"and ROWNUM <=" : "LIMIT ";

        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            String className = dataEntity.getClassName();

            if (dataEntity.hasInternalNoteSpec(BedlInternalNoteSpec.AUDIT_ASSOCIATIONS) || dataEntity.hasInternalNoteSpec(BedlInternalNoteSpec.AUDIT_PROPERTIES)) {
                String tableName = bedlProcessor.getPspTableName(className);
                String triggerName = bedlProcessor.getPspTriggerName("PSP_" + tableName + "_AT");

                ClassTemplateParameters classTemplateParameters =
                        new ClassTemplateParameters(triggerName, tableName, getCreatorId(dataEntity), getModifierId(dataEntity));
                templateParameters.add(classTemplateParameters);

                if (dataEntity.hasInternalNoteSpec(BedlInternalNoteSpec.AUDIT_ASSOCIATIONS)) {
                    String[] associationNames = dataEntity.getInternalNoteSpec(BedlInternalNoteSpec.AUDIT_ASSOCIATIONS).getValues();
                    for (String associationName : associationNames) {
                        String columnName = bedlProcessor.getPspTableName(associationName) + "_FK";
                        String columnToCharFormat = "";
                        String companyIdColumnName = getCompanyIdColumnName(className);
                        String companyIdFromClause = getCompanyIdFromClause(className);
                        String pkColumnName = bedlProcessor.getPspTableName(className) + "_SEQ";

                        classTemplateParameters.getPropertyTemplateParameters().add(new PropertyTemplateParameters(columnName, columnToCharFormat, className, associationName, companyIdColumnName, companyIdFromClause, pkColumnName));
                    }
                }

                if (dataEntity.hasInternalNoteSpec(BedlInternalNoteSpec.AUDIT_PROPERTIES)) {
                    String[] propertyNames = dataEntity.getInternalNoteSpec(BedlInternalNoteSpec.AUDIT_PROPERTIES).getValues();
                    for (String propertyName : propertyNames) {
                        String columnToCharFormat = "";
                        String companyIdColumnName = getCompanyIdColumnName(className);
                        String companyIdFromClause = getCompanyIdFromClause(className);
                        String pkColumnName = bedlProcessor.getPspTableName(className) + "_SEQ";

                        BedlProperty property = dataEntity.findProperty(propertyName);
                        if (property == null) {
                            throw new RuntimeException("Property " + propertyName + " not found on " + className);
                        }
                        ArrayList<String> columnNames = property.getPspColumnNames();

                        if (property instanceof BedlComplexProperty) {
                            BedlDataType dataType = bedlProcessor.findDataType(property.getPropertyType());
                            for (int k = 0; k < columnNames.size(); k++) {
                                String complexDataTypePropertyName = dataType.getScalarProperties().get(k).getPropertyName();
                                classTemplateParameters.getPropertyTemplateParameters().add(new PropertyTemplateParameters(columnNames.get(k), columnToCharFormat, className, property.getPropertyType() + "." + complexDataTypePropertyName, companyIdColumnName, companyIdFromClause, pkColumnName));
                            }
                        } else {
                            classTemplateParameters.getPropertyTemplateParameters().add(new PropertyTemplateParameters(columnNames.get(0), columnToCharFormat, className, propertyName, companyIdColumnName, companyIdFromClause, pkColumnName));
                        }
                    }
                }
            }
        }

        // Pass the columns to be audited to template engine and write out the generated code
        t.setAttribute("triggers", templateParameters);
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseFolderPath.concat("generated_audit_triggers.sql")));
        outputFile.write(t.toString());
        outputFile.close();
    }

    private String getCompanyIdFromClause(String className) {
        // Instead of hard-coding, should walk up the inheritance chain and the relations to find an association with
        // company. Generate the joins accordingly then
        if (className.equals("DDCompanyServiceInfo")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyService") + " t1 " +
                    "WHERE t1.COMPANY_SERVICE_SEQ = "+newSyntax+".DDCOMPANY_SERVICE_INFO_SEQ";
        } else if (className.equals("BPCompanyServiceInfo")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyService") + " t1 " +
                    "WHERE t1.COMPANY_SERVICE_SEQ = "+newSyntax+".BPCOMPANY_SERVICE_INFO_SEQ";
        } else if (className.equals("TaxCompanyServiceInfo")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyService") + " t1 " +
                    "WHERE t1.COMPANY_SERVICE_SEQ = "+newSyntax+".TAX_COMPANY_SERVICE_INFO_SEQ";
        } else if (className.equals("Address")) {
            return "PSP_" + bedlProcessor.getPspTableName("Company") + " t1 JOIN PSP_EMPLOYEE ee ON t1.COMPANY_SEQ = ee.COMPANY_FK JOIN PSP_INDIVIDUAL individual on EE.EMPLOYEE_SEQ = INDIVIDUAL.INDIVIDUAL_SEQ " +
                    "WHERE (t1.MAILING_ADDRESS_FK = "+newSyntax+".ADDRESS_SEQ or t1.LEGAL_ADDRESS_FK = "+newSyntax+".ADDRESS_SEQ or INDIVIDUAL.MAILING_ADDRESS_FK = "+newSyntax+".ADDRESS_SEQ) "+rowNum+"1";
        } else if (className.equals("Individual")) {
            return "PSP_" + bedlProcessor.getPspTableName("Employee") + " t1 " +
                    "WHERE t1.EMPLOYEE_SEQ = "+newSyntax+".INDIVIDUAL_SEQ";
        } else if (className.equals("TaxPenaltyInterest")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyAgency") + " t1  " +
                    "WHERE t1.COMPANY_AGENCY_SEQ = "+newSyntax+".COMPANY_AGENCY_FK";
        } else if (className.equals("CompanyPaymentTemplatePaymentMethod")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyAgency") + " t1  " +
                    "JOIN PSP_COMPANYAGENCY_PMTTEMPLATE capt ON t1.COMPANY_AGENCY_SEQ = capt.COMPANY_AGENCY_FK " +
                    "WHERE capt.COMPANYAGENCY_PMTTEMPLATE_SEQ = "+newSyntax+".COMPANY_AGENCY_PMT_TEMPLATE_FK";
        } else if (className.equals("CompanyAgencyPaymentTemplate") || className.equals("CompanyLaw")) {
            return "PSP_" + bedlProcessor.getPspTableName("CompanyAgency") + " t1  " +
                    "WHERE t1.COMPANY_AGENCY_SEQ = "+newSyntax+".COMPANY_AGENCY_FK";
        } else {
            return "DUAL";
        }
    }

    private String getCompanyIdColumnName(String className) {
        if (className.equals("Company")) {
            return newSyntax+".COMPANY_SEQ";
        } else if (className.equals("Address")) {
            return "t1.COMPANY_SEQ";
        } else if (className.equals("DDCompanyServiceInfo")
                || className.equals("BPCompanyServiceInfo")
                || className.equals("Individual")
                || className.equals("TaxPenaltyInterest")
                || className.equals("CompanyPaymentTemplatePaymentMethod")
                || className.equals("CompanyAgencyPaymentTemplate")
                || className.equals("CompanyLaw")
                || className.equals("TaxCompanyServiceInfo")) {
            return "t1.COMPANY_FK";
        } else {
            return newSyntax+".COMPANY_FK";
        }
    }

    private String getCreatorId(BedlDataEntity dataEntity) {
        if (dataEntity.getBaseClass() != null) {
            return "SELECT CREATOR_ID INTO v_creator_id FROM PSP_" +
                    bedlProcessor.getPspTableName(dataEntity.getBaseClass().getClassName()) +
                    " WHERE " + bedlProcessor.getPspTableName(dataEntity.getBaseClass().getClassName()) + "_SEQ = "+newSyntax+"." + bedlProcessor.getPspTableName(dataEntity.getClassName()) + "_SEQ;";
        } else {
            return "SELECT "+newSyntax+".CREATOR_ID INTO v_creator_id FROM DUAL;";
        }
    }

    private String getModifierId(BedlDataEntity dataEntity) {
        if (dataEntity.getBaseClass() != null) {
            return "SELECT MODIFIER_ID INTO v_modifier_id FROM PSP_" +
                    bedlProcessor.getPspTableName(dataEntity.getBaseClass().getClassName()) +
                    " WHERE " + bedlProcessor.getPspTableName(dataEntity.getBaseClass().getClassName()) + "_SEQ = "+newSyntax+"." + bedlProcessor.getPspTableName(dataEntity.getClassName()) + "_SEQ;";
        } else {
            return "SELECT "+newSyntax+".MODIFIER_ID INTO v_modifier_id FROM DUAL;";
        }
    }


    public class ClassTemplateParameters {
        public ClassTemplateParameters(String pTriggerName, String pTableName, String pCreatorId, String pModifierId) {
            tableName = pTableName;
            creatorId = pCreatorId;
            modifierId = pModifierId;
            triggerName = pTriggerName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getCreatorId() {
            return creatorId;
        }

        public String getModifierId() {
            return modifierId;
        }

        public ArrayList<PropertyTemplateParameters> getPropertyTemplateParameters() {
            return propertyTemplateParameters;
        }

        private String triggerName;
        private String tableName;
        private String creatorId;
        private String modifierId;
        private ArrayList<PropertyTemplateParameters> propertyTemplateParameters = new ArrayList<PropertyTemplateParameters>();

        public String getTriggerName() {
            return triggerName;
        }

        public void setTriggerName(String triggerName) {
            this.triggerName = triggerName;
        }
    }

    public class PropertyTemplateParameters {
        public PropertyTemplateParameters(String pColumnName,
                                          String pColumnToCharFormat,
                                          String pClassName,
                                          String pPropertyName,
                                          String pCompanyIdColumnName,
                                          String pCompanyIdFromClause,
                                          String pPkColumnName) {
            columnName = pColumnName;
            columnToCharFormat = pColumnToCharFormat;
            className = pClassName;
            propertyName = pPropertyName;
            companyIdColumnName = pCompanyIdColumnName;
            companyIdFromClause = pCompanyIdFromClause;
            pkColumnName = pPkColumnName;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getClassName() {
            return className;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getColumnToCharFormat() {
            return columnToCharFormat;
        }

        public String getCompanyIdColumnName() {
            return companyIdColumnName;
        }

        public String getCompanyIdFromClause() {
            return companyIdFromClause;
        }

        public String getPkColumnName() {
            return pkColumnName;
        }


        private String columnName;
        private String className;
        private String propertyName;
        private String columnToCharFormat;
        private String companyIdColumnName;
        private String companyIdFromClause;
        private String pkColumnName;
    }
}