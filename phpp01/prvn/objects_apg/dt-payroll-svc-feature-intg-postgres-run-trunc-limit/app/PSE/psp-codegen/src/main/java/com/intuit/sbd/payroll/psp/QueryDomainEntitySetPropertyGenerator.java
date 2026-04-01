package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlCollectionProperty;
import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dweinberg
 * Date: 1/4/13
 * Time: 1:02 PM
 */
public class QueryDomainEntitySetPropertyGenerator {

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedDataEntityFolder = Generator.getGeneratedDataEntityFolderLocation() + "query/";
        File folder = new File(generatedDataEntityFolder);
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();

        System.out.println("Generating query DomainEntitySet properties in " + generatedDataEntityFolder);

        Set<String> propertyTypes = new HashSet<String>();
        for (BedlDataEntity bedlDataEntity : pBedlProcessor.getDataEntities()) {
            getPropertyTypes(propertyTypes, bedlDataEntity);
        }

        for (BedlDataEntity dataObject : pBedlProcessor.getDataObjects()) {
            getPropertyTypes(propertyTypes, dataObject);
        }

        for (BedlDataEntity dataType : pBedlProcessor.getDataTypes()) {
            getPropertyTypes(propertyTypes, dataType);
        }

        for (String propertyType : propertyTypes) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, propertyType);
        }

    }

    private void getPropertyTypes(Set<String> propertyTypes, BedlDataEntity bedlDataEntity) {
        for (BedlCollectionProperty oneToManyProperty : bedlDataEntity.getOneToManyProperties()) {
            propertyTypes.add(oneToManyProperty.getPropertyType());
        }
    }

    private void generateCode(BedlProcessor pBedlProcessor, String generatedDataEntityFolder, String propertyType) throws IOException {
        //
        // Expression
        //
        String baseDataEntityFullFileName = generatedDataEntityFolder + propertyType + "DomainEntitySetProperty.java";

        // Set up and execute template engine
        StringTemplateGroup templates = new StringTemplateGroup("QueryExpression", Generator.getTemplateLocation() + "domainEntity");
        StringTemplate t = templates.getInstanceOf("QueryDomainEntitySetProperty");
        t.setAttribute("propertyType", propertyType);
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseDataEntityFullFileName));
        outputFile.write(t.toString());
        outputFile.close();
    }
}
