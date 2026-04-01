package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlEnumeration;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wiktor Kozlik
 */
public class EnumGenerator {

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {

        HashMap<String, String> enumsToDataObjects = new HashMap<String, String>();

        StringTemplateGroup templates = new StringTemplateGroup("EnumGroup", Generator.getTemplateLocation() + "enum");
        StringTemplate t = templates.getInstanceOf("Enumerations");

        for (BedlDataEntity dataObject : pBedlProcessor.getDataObjects()) {
            if (dataObject.getKeyProperty() != null) {
                String keyPropertyDataTypeName = dataObject.getKeyProperty().getPropertyType();

                BedlEnumeration enumeration = pBedlProcessor.findEnumeration(keyPropertyDataTypeName);
                if (enumeration != null) {
                    enumsToDataObjects.put(keyPropertyDataTypeName, dataObject.getClassName());
                }
            }
        }

        t.setAttribute("enumsToDataObjects", enumsToDataObjects);

        File dirName = new File(Generator.getGeneratedDomainFolderLocation());
        dirName.mkdirs();
        
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(dirName + "/Enumerations.java"));
        outputFile.write(t.toString());
        outputFile.close();

    }

}
