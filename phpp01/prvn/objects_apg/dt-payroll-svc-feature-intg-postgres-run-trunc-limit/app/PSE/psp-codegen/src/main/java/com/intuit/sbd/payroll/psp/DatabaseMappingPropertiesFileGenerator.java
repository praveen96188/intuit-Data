package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlInternalNoteSpec;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import com.intuit.sbd.payroll.psp.bedl.BedlRelation;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 28, 2007
 * Time: 8:19:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseMappingPropertiesFileGenerator {
    private BedlProcessor bedlProcessor;
    private HashMap<String, Integer> classNameToFkCount = new HashMap<String, Integer>();

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        bedlProcessor = pBedlProcessor;

          /**
         * <DataEntity Name="AssignedDepositFrequency" AllowCustomization="false" AllowDynamicSpecialization="false">
         <Specializes Type="EffectiveDepositFrequency" />
         <Properties>
         <Property Name="AgencyEffectiveDate" HandcodedAccessorType="None">
         <Type Type="Date" />
         </Property>
         <Property Name="FrequencySource" HandcodedAccessorType="None">
         <Type Type="DepositFrequencySource" />
         </Property>
         <Property Name="Notes" HandcodedAccessorType="None">
         <Type Type="String" />
         </Property>
         </Properties>
         <Methods />
         </DataEntity>
         */
        for (BedlDataEntity dataEntity : bedlProcessor.getDataEntities()) {
            GenerateMappingsForEntity(dataEntity);
        }

        for (BedlDataEntity dataObject : bedlProcessor.getDataObjects()) {
            GenerateMappingsForEntity(dataObject);
        }
        /**
         *     <Relation Type="Association" Direction="SourceToTarget">
         <Source>
         <Type Type="CompanyEvent" />
         <Multiplicity UpperBound="*" />
         </Source>
         <Target>
         <Type Type="Company" />
         <Multiplicity UpperBound="1" />
         </Target>
         </Relation>
         */
        for (BedlRelation relation : bedlProcessor.getRelations()) {
            if (relation.getSourceUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many) ||
                    relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.One)) {

                NameOverrides.putOverride(
                        GetSpcfFKName(bedlProcessor.getCamelCaseName(relation.getSourceClassNameForFk()), bedlProcessor.getCamelCaseName(relation.getTargetClassNameForFk())),
                        GetPspFKName(relation.getSourceClassNameForFk(), relation.getTargetClassNameForFk()));
            }

            if (relation.getTargetUpperBoundMultiplicity().equals(BedlRelation.Multiplicity.Many) ||
                    relation.getSourceClassName().equals(BedlRelation.Multiplicity.One)) {
                NameOverrides.putOverride(
                        GetSpcfFKName(bedlProcessor.getCamelCaseName(relation.getTargetClassNameForFk()), bedlProcessor.getCamelCaseName(relation.getSourceClassNameForFk())),
                        GetPspFKNameLegacy(relation.getTargetClassNameForFk(), relation.getSourceClassNameForFk()));
            }
        }//end of for loop with i var

        // Write property file
        NameOverrides.store();
    }

    private void GenerateMappingsForEntity(BedlDataEntity dataEntityOrDataObject) {
        String className = dataEntityOrDataObject.getClassName();

        NameOverrides.putOverride(GetSpcfTableName(className), bedlProcessor.getPspTableName(className));

        // Inheritance generates a fk
        if (dataEntityOrDataObject.getBaseClass() != null) {
            String baseClassName = dataEntityOrDataObject.getBaseClass().getClassName();
            NameOverrides.putOverride(
                    GetSpcfFKName(bedlProcessor.getCamelCaseName(className), bedlProcessor.getCamelCaseName(baseClassName)),
                    GetPspFKName(className, baseClassName)
            );
        }

        // Triggers
        if (className.length() > 23) {
            NameOverrides.putOverride(className.toUpperCase() + "_UT", className.substring(0, 23).toUpperCase() + "_UT");
            NameOverrides.putOverride(className.toUpperCase() + "_IT", className.substring(0, 23).toUpperCase() + "_IT");
        }
    }

    private String GetSpcfTableName(String className) {
        return className.toUpperCase();
    }

    private String GetPspFKName(String childClassName, String parentClassName) {
        String pspTableNameWithoutOverride = bedlProcessor.getPSPTableNameWithoutOverride(childClassName);
        BedlInternalNoteSpec predefinedFKNumbers = GetPredefinedFKOrder(childClassName, parentClassName);

        Integer fkNumber = null;
        int nextNonPredefinedFKNumber = 1;

        if (predefinedFKNumbers != null) {
            nextNonPredefinedFKNumber = predefinedFKNumbers.getValues().length + 1;
            fkNumber = predefinedFKNumbers.getValueOrdinal(parentClassName);
        }

        if (fkNumber == null) {
            fkNumber = GetNextFKNumber(pspTableNameWithoutOverride, nextNonPredefinedFKNumber);
        }

        if (pspTableNameWithoutOverride.length() > 22) {
            pspTableNameWithoutOverride = pspTableNameWithoutOverride.substring(0, 22);
        }
        String fkName = pspTableNameWithoutOverride + "_FK" + fkNumber.toString();
        return fkName.toUpperCase();
    }

    private String GetPspFKNameLegacy(String childClassName, String parentClassName) {
        String pspTableNameWithoutOverride = childClassName;  // Compare with GetPSPFKName
        //BedlInternalNoteSpec predefinedFKNumbers = GetPredefinedFKOrder(childClassName, parentClassName);

        //Integer fkNumber = null;
        int nextNonPredefinedFKNumber = 1;

        //if (predefinedFKNumbers != null) {
        //    nextNonPredefinedFKNumber = predefinedFKNumbers.getValues().length + 1;
        //    fkNumber = predefinedFKNumbers.getValueOrdinal(parentClassName);
        //}

        //if (fkNumber == null) {
         Integer fkNumber = GetNextFKNumber(pspTableNameWithoutOverride, nextNonPredefinedFKNumber);
        //}

        if (pspTableNameWithoutOverride.length() > 22) {
            pspTableNameWithoutOverride = pspTableNameWithoutOverride.substring(0, 22);
        }
        String fkName = pspTableNameWithoutOverride + "_FK" + fkNumber.toString();
        return fkName.toUpperCase();

    }


    private String GetSpcfFKName(String fromEntityName, String toEntityName) {
        return (fromEntityName + "_FK_" + toEntityName).toUpperCase();
    }

    private BedlInternalNoteSpec GetPredefinedFKOrder(String childClassName, String parentClassName) {
        BedlDataEntity dataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(childClassName);
        BedlInternalNoteSpec bedlInternalNoteSpec = dataEntity.getInternalNoteSpec(BedlInternalNoteSpec.FOREIGNKEYORDER);
        // temporary special logic for mmt because it has too many fks
        // if this has to be done for another entity move this into a static initializer
        if(dataEntity.getClassName().equals("MoneyMovementTransaction")) {            
            List<String> values = new ArrayList<String>(Arrays.<String>asList(bedlInternalNoteSpec.getValues()));
            values.add("EFEPaymentSubmissionBatch");
            values.add("PaymentTemplate");
            values.add("PaymentFrequency");
            values.add("AuthUser");
            values.add("QbdtTransactionInfo");
            String[] valueArray = new String[values.size()];
            valueArray = values.<String>toArray(valueArray);
            bedlInternalNoteSpec.setValues(valueArray);
        }
        return bedlInternalNoteSpec;
    }

    private Integer GetNextFKNumber(String childTableName, Integer nextNonPredefinedFKNumber) {
        Integer NextFKNumber = nextNonPredefinedFKNumber;
        if (classNameToFkCount.containsKey(childTableName)) {
            NextFKNumber = classNameToFkCount.get(childTableName);
        }
        classNameToFkCount.put(childTableName, NextFKNumber + 1);
        return NextFKNumber;
    }

    private String GetMultiplicity(Element parentElement, String attributeName) {
        Element MultiplicityElement = (Element) parentElement.getElementsByTagName("Multiplicity").item(0);
        if (MultiplicityElement == null) {
            return "1";
        } else {
            if (MultiplicityElement.hasAttribute(attributeName)) {
                return MultiplicityElement.getAttribute(attributeName);
            } else {
                return "1";
            }
        }
    }

}
