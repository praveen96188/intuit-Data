package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlEnumeration;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 15, 2009
 * Time: 11:13:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnumerationGenerator {
    static String NEW_LINE = System.getProperty("line.separator");

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        String generatedDataEntityFolder = Generator.getGeneratedDomainFolderLocation();

        for (BedlEnumeration enumeration : pBedlProcessor.getEnumerations()) {
            String enumFileName = generatedDataEntityFolder + enumeration.getEnumerationName() + ".java";
            //String enumUserTypeFileName = generatedDataEntityFolder + enumeration.getEnumerationName() + "UserType.java";
            StringBuffer enumStr = new StringBuffer();
            Boolean first = true;
            for (String enumerationValue : enumeration.getEnumerationValues()) {
                if (!first) {
                    enumStr.append(", ");
                    enumStr.append(NEW_LINE);
                    enumStr.append(NEW_LINE);
                }
                enumStr.append(enumerationValue);

                first = false;
            }

            File baseDataEntity = new File(enumFileName);
            baseDataEntity.delete();

//a            baseDataEntity = new File(enumUserTypeFileName);
//a            baseDataEntity.delete();

            //generateCode(pBedlProcessor, generatedDataEntityFolder, domainEntityFolder, dataEntity);
            StringTemplateGroup templates = new StringTemplateGroup("BaseDomainEntity", Generator.getTemplateLocation() + "domainEntity");
            StringTemplate enumerationTemplate = templates.getInstanceOf("Enumeration");
            enumerationTemplate.setAttribute("enumerationName", enumeration.getEnumerationName());
            enumerationTemplate.setAttribute("enumValues", enumStr.toString());
            enumerationTemplate.setAttribute("modelVersion", "1.0");
            BufferedWriter outputFile = new BufferedWriter(new FileWriter(enumFileName));
            outputFile.write(enumerationTemplate.toString());
            outputFile.close();

//a            StringTemplate enumerationUserTemplate = templates.getInstanceOf("EnumerationUserType");
//a            enumerationUserTemplate.setAttribute("enumerationName", enumeration.getEnumerationName());
//a            enumerationUserTemplate.setAttribute("modelVersion", "1.0");
//a            outputFile = new BufferedWriter(new FileWriter(enumUserTypeFileName));
//a            outputFile.write(enumerationUserTemplate.toString());
//a           outputFile.close();

        }
    }
}
