package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.*;

/**
 * @author Allen Chaves
 */
public class QueryExpressionGenerator {

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedDataEntityFolder = Generator.getGeneratedDataEntityFolderLocation()+ "query/";
        File folder = new File(generatedDataEntityFolder);
        folder.mkdirs();        

        System.out.println("Generating query expressions in " + generatedDataEntityFolder);

        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, dataEntity);
        }

        for (BedlDataEntity dataObject : pBedlProcessor.getDataObjects()) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, dataObject);
        }

        for (BedlDataEntity dataType : pBedlProcessor.getDataTypes()) {
            generateCode(pBedlProcessor, generatedDataEntityFolder, dataType);
        }

    }

    private void generateCode(BedlProcessor pBedlProcessor, String generatedDataEntityFolder, BedlDataEntity domainEntity) throws IOException {
        //
        // Expression
        //
        String baseDataEntityFullFileName = generatedDataEntityFolder + domainEntity.getClassName() + "Expression.java";

        // Set up and execute template engine
        StringTemplateGroup templates = new StringTemplateGroup("QueryExpression", Generator.getTemplateLocation() + "domainEntity");
        StringTemplate t = templates.getInstanceOf("QueryExpression");
        t.setAttribute("bedlDataEntity", domainEntity);
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseDataEntityFullFileName));
        outputFile.write(t.toString());
        outputFile.close();
    }
}