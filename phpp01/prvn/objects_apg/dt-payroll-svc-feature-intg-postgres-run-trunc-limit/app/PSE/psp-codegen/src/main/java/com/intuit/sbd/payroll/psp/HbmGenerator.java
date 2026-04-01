package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Allen Chaves
 */
public class HbmGenerator {

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedHbmFolder = Generator.getGeneratedResourceFolderLocation();
        File hbmFolder = new File(generatedHbmFolder);
        hbmFolder.mkdirs();

        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            generateCode(pBedlProcessor, generatedHbmFolder, dataEntity);
        }

        for (BedlDataEntity dataObject : pBedlProcessor.getDataObjects()) {
            generateCode(pBedlProcessor, generatedHbmFolder, dataObject);
        }
    }


    private void generateCode(BedlProcessor pBedlProcessor, String generatedHbmFolder, BedlDataEntity domainEntity) throws IOException {
        String hbmFullFileName = generatedHbmFolder + domainEntity.getClassName() + ".hbm.xml";

        File hbmFile = new File(hbmFullFileName);
        hbmFile.delete();

        // Set up and execute template engine
        StringTemplate t = getStringTemplate(pBedlProcessor, domainEntity);
        t.setAttribute("bedlDataEntity", domainEntity);
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(hbmFullFileName));
        outputFile.write(t.toString());
        outputFile.close();
    }

    private StringTemplate getStringTemplate(BedlProcessor pBedlProcessor, BedlDataEntity domainEntity) {
        if (domainEntity.getBaseClass() != null) {
            StringTemplateGroup templates = new StringTemplateGroup("HbmSubclass", Generator.getTemplateLocation() + "domainEntity");
            return templates.getInstanceOf("HbmSubclass");
        }
        else {
            StringTemplateGroup templates = new StringTemplateGroup("Hbm", Generator.getTemplateLocation() + "domainEntity");
            return templates.getInstanceOf("Hbm");
        }
    }

}