package com.intuit.sbd.payroll.psp;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.List;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import merlin.bedl.DomainModel;
import merlin.bedl.EntityInterfaceType;

/**
 * User: rnorian
 * Date: Jan 30, 2008
 * Time: 8:32:59 PM
 */
public class SAPActionScriptGen {
    public static void main(String args[]) {
        try {
            System.out.println("===[ SAP ActionScript Generator ]===");

            String bedlPath = System.getProperty("bedlFileName");
            File bedl = new File(bedlPath);
            if (!bedl.exists() || !bedl.canRead() || !bedl.isFile()) {
                System.err.println("BEDL file specified (" + bedlPath +  ") does not exist or cannot be opened.");
                System.exit(-1);
            }

            String templatePath = System.getProperty("templatePath");
            File templateDir = new File(templatePath);
            if (!templateDir.exists() || !templateDir.canRead() || !templateDir.isDirectory()) {
                System.err.println("templatePath specified (" + templatePath +  ") does not exist, is not a directory or cannot be opened.");
                System.exit(-1);
            }

            String outputPath = System.getProperty("outputPath", ".");
            File outputDir = new File(outputPath);
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                System.err.println("outputPath specified (" + outputPath + ") did not exist and could not be created.");
            }

            if (!outputDir.exists() || !outputDir.canWrite() || !outputDir.isDirectory()) {
                System.err.println("outputPath specified (" + outputPath + ") does not exist, is not a directory or cannot be written to.");
                System.exit(-1);
            }

            System.out.println("... initializing JAXB");
            JAXBContext jaxb = JAXBContext.newInstance("merlin.bedl");
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();

            System.out.println("... parsing bedl: " + bedl.getAbsolutePath());
            DomainModel domainModel = (DomainModel) unmarshaller.unmarshal(bedl);

            List<EntityInterfaceType> entityInterfaces = domainModel.getEntityInterfaces().getEntityInterface();
            if (entityInterfaces.size() == 0) {
                System.out.println("No EntityInterface types found in bedl: " + bedlPath);
                System.out.println("Exiting.");
                System.exit(0);
            }

            StringTemplateGroup templates = new StringTemplateGroup("ActionScriptGroup", templateDir.getAbsolutePath());

            System.out.println("... preparing to generate " + entityInterfaces.size() + " ActionScript files.");
            for (EntityInterfaceType entityInterface : entityInterfaces) {

                StringTemplate domainObjectTemplate = templates.getInstanceOf("DomainObject");
                domainObjectTemplate.setAttribute("package", domainModel.getNamespace());

                String actionScriptClassName = convertToActionScriptClassName(entityInterface.getName());
                String fileName = actionScriptClassName + ".as";

                System.out.println("... generating " + fileName);
                domainObjectTemplate.setAttribute("entityInterface", entityInterface);
                domainObjectTemplate.setAttribute("actionScriptClassName", actionScriptClassName);
                String programText = domainObjectTemplate.toString();

                File outputFile = new File(outputDir + File.separator + fileName);
                BufferedWriter bufferedFileWriter = new BufferedWriter(new FileWriter(outputFile));
                bufferedFileWriter.write(programText);
                bufferedFileWriter.close();
            }

            System.out.println("generation complete -- all files generated into directory: " + outputDir.getAbsolutePath());

            
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    public static String convertToActionScriptClassName(String modelDTOName) {
        String className = modelDTOName;
        if (className.endsWith("DTO"))
            className = className.substring(0, className.lastIndexOf("DTO"));

        String prefix = "SAP";
        if (className.startsWith(prefix))
            className = className.substring(prefix.length());

        return className;

    }
}
