package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.agencyrules.AgencyRulesProcessor;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;

import java.io.File;

public class PspCodeGen {
    public static void main(String args[]) {
        try {
            String bedlFileName = System.getProperty("bedlFileName");
            String database = System.getProperty("database");
            System.out.println(String.format("Database=%s",database));
            DatabaseType databaseType = DatabaseType.fromValue(database);
            File bedlFile = new File(bedlFileName);
            if (!bedlFile.exists())
                throw new RuntimeException(bedlFileName + " : could not find bedl file");

            NameOverrides.load(bedlFile.getParent());

            BedlProcessor bedlProcessor = new BedlProcessor(bedlFileName);

            DatabaseMappingPropertiesFileGenerator DbMappingPropertiesGenerator = new DatabaseMappingPropertiesFileGenerator();
            DbMappingPropertiesGenerator.generateCode(bedlProcessor);

            // Agency rules XML file is under c:\dev\psp\main\pse\AgencyRules\src\resources\xml
            String agencyRulesXmlFileName = bedlFile.getParent() + "/../../../../agency-rules/src/main/resources/xml/AgencyRules.xml";
            AgencyRulesProcessor arProcessor = new AgencyRulesProcessor(agencyRulesXmlFileName);


            if (System.getProperty("isAfterCodeGen") != null && System.getProperty("isAfterCodeGen").equals("true")) {
                new QueryExpressionGenerator().generateCode(bedlProcessor);
                new QueryDomainEntitySetPropertyGenerator().generateCode(bedlProcessor);
                new DomainEntityGenerator().generateCode(bedlProcessor);
                new EnumerationGenerator().generateCode(bedlProcessor);
                new DBInstallScriptGenerator().generateCode(bedlProcessor);
                new HbmGenerator().generateCode(bedlProcessor);
                new HbmForHQLGenerator().generateCode(bedlProcessor);
                HbmProcessor.process(bedlProcessor);
            }
            else {
                new CreateTableGenerator().generateCode(bedlProcessor);
                new DeleteCompanyStoredProcedureGenerator().generateCode(bedlProcessor);
                new ColumnConstraintGenerator().generateCode(bedlProcessor);
                new AuditTriggerGenerator().generateCode(bedlProcessor,databaseType);
                new EnumGenerator().generateCode(bedlProcessor);
                new AgencyRulesGenerator().generateCode(arProcessor);
                new HbmGenerator().generateCode(bedlProcessor);
                HbmProcessor.process(bedlProcessor);
            }

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

}
