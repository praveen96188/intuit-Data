package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import com.intuit.sbd.payroll.psp.bedl.BedlDataType;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Allen Chaves
 */
public class DomainEntityGenerator {

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedDataEntityFolder = Generator.getGeneratedDomainFolderLocation();
        String domainEntityFolder = pBedlProcessor.getBedlFolderName() + "../java/com/intuit/sbd/payroll/psp/domain/";
        File folder = new File(generatedDataEntityFolder);
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();


        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, domainEntityFolder, dataEntity);
        }

        for (BedlDataEntity dataObject : pBedlProcessor.getDataObjects()) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, domainEntityFolder, dataObject);
        }

        for (BedlDataType dataType : pBedlProcessor.getDataTypes()){
            generateDataTypeCode(pBedlProcessor, generatedDataEntityFolder, domainEntityFolder, dataType);
        }

    }

    private void generateCode(BedlProcessor pBedlProcessor, String generatedDataEntityFolder, String domainEntityFolder, BedlDataEntity domainEntity) throws IOException {
        //
        // BaseXXXXXX
        //
        String baseDataEntityFullFileName = generatedDataEntityFolder + "Base" + domainEntity.getClassName() + ".java";

        File baseDataEntity = new File(baseDataEntityFullFileName);
        baseDataEntity.delete();

        // Set up and execute template engine
        StringTemplateGroup templates = new StringTemplateGroup("BaseDomainEntity", Generator.getTemplateLocation() + "domainEntity");
        StringTemplate t = templates.getInstanceOf("BaseDomainEntity");
        t.setAttribute("bedlDataEntity", domainEntity);
        t.setAttribute("modelVersion", "1.0");
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseDataEntityFullFileName));
        outputFile.write(t.toString());
        outputFile.close();

        //
        // XXXXXX
        //
        String oldDataEntityFullFileName = generatedDataEntityFolder + domainEntity.getClassName() + ".java";
        String newDataEntityFullFileName = domainEntityFolder + domainEntity.getClassName() + ".java";

        File oldDataEntity = new File(oldDataEntityFullFileName);
        oldDataEntity.delete();

        File newDataEntity = new File(newDataEntityFullFileName);
        if (!newDataEntity.exists()) {
            // Set up and execute template engine
            templates = new StringTemplateGroup("DomainEntity", Generator.getTemplateLocation() + "domainEntity");
            t = templates.getInstanceOf("DomainEntity");
            t.setAttribute("bedlDataEntity", domainEntity);
            t.setAttribute("modelVersion", "1.0");
            outputFile = new BufferedWriter(new FileWriter(newDataEntityFullFileName));
            outputFile.write(t.toString());
            outputFile.close();
        }
    }

    private void generateDataTypeCode(BedlProcessor pBedlProcessor, String generatedDataEntityFolder, String domainEntityFolder, BedlDataType dataType) throws IOException {
        //String baseDataEntityFullFileName = "C:/dev/PSP/v1-maint/PSE/Domain/target/src/com/intuit/sbd/payroll/psp/domain/" + dataType.getClassName() + ".java";
        String baseDataEntityFullFileName = generatedDataEntityFolder + dataType.getClassName() + ".java";

        File baseDataEntity = new File(baseDataEntityFullFileName);
        baseDataEntity.delete();

        // Set up and execute template engine
        StringTemplateGroup templates = new StringTemplateGroup("DataEntity", Generator.getTemplateLocation() + "domainEntity");
        StringTemplate t = templates.getInstanceOf("DataEntity");
        t.setAttribute("bedlDataEntity", dataType);
        t.setAttribute("modelVersion", "1.0");
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseDataEntityFullFileName));
        outputFile.write(t.toString());
        outputFile.close();
    }
}
