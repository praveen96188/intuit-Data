package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.agencyrules.AgencyRulesProcessor;
import com.intuit.sbd.payroll.psp.agencyrules.AgencyRulesTable;
import com.intuit.sbd.payroll.psp.common.TableColumn;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Nov 12, 2008
 * Time: 7:50:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AgencyRulesGenerator {
    private AgencyRulesProcessor agencyRulesProcessor;

    public void generateCode(AgencyRulesProcessor pAgencyRulesProcessor) throws IOException {
        agencyRulesProcessor = pAgencyRulesProcessor;

        HashMap<String, Map<String, String>> paymentTemplateMap = agencyRulesProcessor.getPaymentTemplates();

        // PaymentTemplate
        AgencyRulesTable agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_PAYMENT_TEMPLATE", "PAYMENT_TEMPLATE_CD");
        TableColumn agencyRulesTableColumn = new TableColumn("PAYMENT_TEMPLATE_CD", "VARCHAR(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("AGENCY_FK", "VARCHAR(30)", "");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("PAYMENT_TEMPLATE_ABBREV", "VARCHAR(30)", "");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("NO_CALCULATION", "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);

        for(String agencyId : paymentTemplateMap.keySet()){
            Map<String, String> paymentTemplates =  paymentTemplateMap.get(agencyId);
            for (String paymentTemplateCode : paymentTemplates.keySet()) {
                String valuesList = "'" + paymentTemplateCode + "' , '" + agencyId + "', '" + paymentTemplates.get(paymentTemplateCode) + "', " + "0";
                agencyRulesTable.getInsertRowValues().add(valuesList);
            }
        }
        writeOutputFile(agencyRulesTable);

        List<List<String>> laws = agencyRulesProcessor.getLaws();

        // Law
        agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_LAW", "LAW_ID");
        agencyRulesTableColumn = new TableColumn("LAW_ID", "varchar(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("LAW_ABBREV", "varchar(50)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("DESCRIPTION", "varchar(50)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("PAYMENT_TEMPLATE_FK", "varchar(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("LAW_CATEGORY_CODE", "varchar(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        for (List<String> columnValues : laws) {
            String valuesList = "'" + columnValues.get(0) + "', '" + columnValues.get(1) + "', '" + columnValues.get(2) + "', '" + columnValues.get(3)
                    + "','Unused'";
            agencyRulesTable.getInsertRowValues().add(valuesList);
        }
        
        writeOutputFile(agencyRulesTable);

        // WageLimit
        agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_WAGE_LIMIT", "WAGE_LIMIT_ID");
        agencyRulesTableColumn = new TableColumn("WAGE_LIMIT_ID", "varchar(10)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("AMOUNT", "number(19,4)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("EFFECTIVE_YEAR_QUARTER", "varchar(5)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("LAW_FK", "varchar2(255 CHAR)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);

        int year = Integer.parseInt(agencyRulesProcessor.getWageLimitsEffectiveYear());
        int quarter =   Integer.parseInt(agencyRulesProcessor.getWageLimitsEffectiveQuarter());
        String effectiveYearQuarter = agencyRulesProcessor.getWageLimitsEffectiveYear() + agencyRulesProcessor.getWageLimitsEffectiveQuarter();
        if (effectiveYearQuarter == null) {
            // Use the current year/quarter.
            GregorianCalendar now = new GregorianCalendar();
            year = now.get(GregorianCalendar.YEAR);
            quarter = ((now.get(GregorianCalendar.MONTH)) / 3) + 1;
        }

        List<List<String>> wageLimits = agencyRulesProcessor.getWageLimits();
        for (List<String> columnValues : wageLimits) {

            // The primary key for the WageLimit table is just the Law Id (0 padded) + the effective year/quarter.
            // We could just make these columns a compound primary key, but the Agency Rules SQL template doesn't
            // support comparisons of this type when inserting/updating.
            for (int i = quarter; i <= 4; i++) {
                effectiveYearQuarter = String.format("%04d%01d", year, i);
                String wageLimitId = String.format("%04d%s", new Integer(columnValues.get(0)), effectiveYearQuarter);
                String valuesList = "'" + wageLimitId + "', '" + columnValues.get(1) + "', '" + effectiveYearQuarter + "', '" + columnValues.get(0) + "'";
                agencyRulesTable.getInsertRowValues().add(valuesList);
            }
        }

        writeOutputFile(agencyRulesTable);

        // Agency
        final String agencyId = "AGENCY_ID";
        final String name = "NAME";
        final String agencyAbbrev = "AGENCY_ABBREV";
        final String defaultRAAForm = "DEFAULT_R_A_A_FORM";
        final String achEnrollmentReq = "A_C_H_ENROLLMENT_REQUIRED";
        final String raaEnrollmentReq = "R_A_A_ENROLLMENT_REQUIRED";
        final String rafEnrollmentReq = "R_A_F_ENROLLMENT_REQUIRED";
        final String refIntuitForRefundPmt= "RFNDS_INTUIT_FOR_RETURNED_PMT";
        final String agencySupported = "AGENCY_SUPPORTED";
        final String noCalculation = "NO_CALCULATION";
        //
        Map<String,Map<String,String>> agencies = pAgencyRulesProcessor.getAgencies();
        Map<String,Map<String,String>> curDefinitions = new SQLParser().parse(System.getProperty("agencies.sql"));

        agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_AGENCY", agencyId);
        agencyRulesTableColumn = new TableColumn(agencyId, "varchar(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(name, "varchar(300)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(agencyAbbrev, "varchar(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(defaultRAAForm, "varchar(255)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(achEnrollmentReq, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(raaEnrollmentReq, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(rafEnrollmentReq, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(refIntuitForRefundPmt, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(agencySupported, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn(noCalculation, "NUMBER(1)", "NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);

        for (String id : agencies.keySet()) {
            Map<String, String> columns = agencies.get(id);
            Map<String, String> curValues = curDefinitions.get(id);                
            if (curValues == null) curValues = new HashMap<String, String>();
            StringBuffer buf = new StringBuffer();
            buf.append(" '").append(columns.get(AgencyRulesProcessor.AGENCY_ID)).append("', ");
            buf.append(" '").append(columns.get(AgencyRulesProcessor.AGENCY_NAME)).append("',");
            buf.append(" '").append(columns.get(AgencyRulesProcessor.AGENCY_ABBREV)).append("',");
            String defaultRaaForm = curValues.get(defaultRAAForm);
            String achEnrlReq = curValues.get(achEnrollmentReq);
            String raaEnrlReq = curValues.get(raaEnrollmentReq);
            String rafEnrlReq = curValues.get(rafEnrollmentReq);
            String refundPayment = curValues.get(refIntuitForRefundPmt);
            String agencySupportVal = curValues.get(agencySupported);
            String noCalculationVal  = curValues.get(noCalculation);

            buf.append(defaultRaaForm == null ? "null" : "'" + defaultRaaForm + "'");
            buf.append(", ");
            buf.append(achEnrlReq == null ? "0" : achEnrlReq);
            buf.append(", ");
            buf.append(raaEnrlReq == null ? "0" : raaEnrlReq);
            buf.append(", ");
            buf.append(rafEnrlReq == null ? "0" : rafEnrlReq);
            buf.append(", ");
            buf.append(refundPayment == null ? "0" : refundPayment);
            buf.append(", ");
            buf.append(agencySupportVal == null ? "0" : agencySupportVal);
            buf.append(", ");
            buf.append(noCalculationVal == null ? "0" : noCalculationVal);
               
            agencyRulesTable.getInsertRowValues().add(buf.toString());
        }
        
        writeOutputFile(agencyRulesTable);

        //Payment Template Frequencies 
        HashMap<String, ArrayList<AgencyRulesProcessor.PaymentTemplateFrequency>> paymentFrequenciesMap = agencyRulesProcessor.getPaymentTemplateFrequencies();

        agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_PMT_TEMPLATE_FREQUENCY", "PAYMENT_TEMPLATE_FREQUENCY_ID","PAYMENT_FREQUENCY_ID","PAYMENT_TEMPLATE_FK");
        TableColumn tableColumn = new TableColumn("PAYMENT_TEMPLATE_FREQUENCY_ID", "VARCHAR2(255 CHAR)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(tableColumn);
        tableColumn = new TableColumn("PAYMENT_FREQUENCY_ID", "VARCHAR2(50 CHAR)", "");
        agencyRulesTable.getTableColumns().add(tableColumn);
        tableColumn = new TableColumn("PAYMENT_TEMPLATE_FK", "VARCHAR2(255 CHAR)", "");
        agencyRulesTable.getTableColumns().add(tableColumn);
        tableColumn = new TableColumn("OBSOLETE", "NUMBER(1)", "");
        agencyRulesTable.getTableColumns().add(tableColumn);

        for(String paymentTemplateId : paymentFrequenciesMap.keySet()){
            ArrayList<AgencyRulesProcessor.PaymentTemplateFrequency> paymentFrequencies =  paymentFrequenciesMap.get(paymentTemplateId);
            for (AgencyRulesProcessor.PaymentTemplateFrequency paymentFrequency : paymentFrequencies) {
                String paymentFrequencyId = paymentFrequency.frequencyId;
                String obsolete = paymentFrequency.obsolete ? "1" : "0";
                String uid = UUID.randomUUID().toString();
                String valuesList = "'" + uid + "' , '" + paymentFrequencyId + "','" + paymentTemplateId + "'," +  obsolete;
                agencyRulesTable.getInsertRowValues().add(valuesList);
            }
        }

        writeOutputFile(agencyRulesTable);

        HashMap<String, ArrayList<String>> formTemplateMap = agencyRulesProcessor.getFormTemplates();  

        // FormTemplate
        agencyRulesTable = new AgencyRulesTable(pAgencyRulesProcessor, "PSP_FORM_TEMPLATE", "FORM_TEMPLATE_CD");
        agencyRulesTableColumn = new TableColumn("FORM_TEMPLATE_CD", "VARCHAR(30)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("DESCRIPTION", "VARCHAR(255)", "NOT NULL");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);
        agencyRulesTableColumn = new TableColumn("AGENCY_FK", "VARCHAR(30)", "");
        agencyRulesTable.getTableColumns().add(agencyRulesTableColumn);

        for(String agencyCode : formTemplateMap.keySet()){
            ArrayList<String> formTemplates =  formTemplateMap.get(agencyCode);
            for (String formTemplateCode : formTemplates) {
                String[] templates = formTemplateCode.split("%");
                String valuesList = "'" + templates[0] + "' , '" + templates[1] + "','" + agencyCode + "'";
                agencyRulesTable.getInsertRowValues().add(valuesList);
            }
        }
        writeOutputFile(agencyRulesTable);
    }


    private void writeOutputFile(AgencyRulesTable pAgencyRulesTable) throws IOException {
        StringTemplate t;
        // Set up template engine
        if(pAgencyRulesTable.getTableName().equals("PSP_PMT_TEMPLATE_FREQUENCY")){
            StringTemplateGroup templates = new StringTemplateGroup("PopulateTablePaymentFrequency", Generator.getTemplateLocation()+ "agencyRules");
            t = templates.getInstanceOf("PopulateTablePaymentFrequency");
        }else{
            StringTemplateGroup templates = new StringTemplateGroup("PopulateTable", Generator.getTemplateLocation() + "agencyRules");
            t = templates.getInstanceOf("PopulateTable");
        }


        // Bind instance to template engine and write out generated file
        t.setAttribute("AgencyRulesTable", pAgencyRulesTable);

        String targetFolder = agencyRulesProcessor.getAgencyRulesFolderName() + "../../../../../domain/target/src/sql/";

        // Make sure folders exist
        File folder = new File(targetFolder);
        folder.mkdirs();

        // Write file
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(targetFolder + "populate_"+ pAgencyRulesTable.getTableName().toLowerCase() + ".sql"));
                       // C:\dev\PSP\main\PSE\Domain\src\main\model
        outputFile.write(t.toString());
        outputFile.close();
    }
}
