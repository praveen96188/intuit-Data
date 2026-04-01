// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AgencyRulesProcessor.java

package com.intuit.sbd.payroll.psp.agencyrules;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgencyRulesProcessor {

    public static final String AGENCY_ID = "AgencyID";
    public static final String AGENCY_NAME = "AgencyName";
    public static final String AGENCY_ABBREV = "AgencyAbbrev";

    public AgencyRulesProcessor(String pAgencyRulesFileName)
            throws ParserConfigurationException, IOException, SAXException {
        File agencyRulesFile = new File(pAgencyRulesFileName);
        if (!agencyRulesFile.exists()) {
            throw new RuntimeException((new StringBuilder()).append("Could not find agency rules file: ").append(pAgencyRulesFileName).toString());
        } else {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            agencyRulesDoc = docBuilder.parse(agencyRulesFile);
            agencyRulesFolderName = (new StringBuilder()).append(agencyRulesFile.getParent()).append(File.separator).toString();

            if (agencyRulesDoc.getElementsByTagName("EffectiveDates").getLength() == 1) {
                Element effDate = (Element) agencyRulesDoc.getElementsByTagName("EffectiveDates").item(0);
                mWageLimitsEffectiveQuarter = effDate.getElementsByTagName("Quarter").item(0).getFirstChild().getNodeValue();
                mWageLimitsEffectiveYear = effDate.getElementsByTagName("Year").item(0).getFirstChild().getNodeValue();
            }

            return;
        }
    }

    public Document getAgencyRulesDocument() {
        return agencyRulesDoc;
    }

    public String getAgencyRulesFolderName() {
        return agencyRulesFolderName;
    }

    public List getTables() {
        if (tables == null)
            tables = new ArrayList();
        return tables;
    }

    public List getTableColumns() {
        if (tableColumns == null)
            tableColumns = new ArrayList();
        return tableColumns;
    }


    private void getPaymentTemplateList(NodeList pPaymentTemplateList, String pAgencyId) {
        Map<String, String> paymentTemplateIds = new HashMap<String,String>();
        for (int i = 0; i < pPaymentTemplateList.getLength(); i++) {
            Element paymentTemplateElement = (Element) pPaymentTemplateList.item(i);
            String paymentTemplateId = paymentTemplateElement.getElementsByTagName("PaymentTemplateID").item(0).getFirstChild().getNodeValue();
            String paymentTemplateAbbrev = paymentTemplateId;
            Node abbrev = paymentTemplateElement.getElementsByTagName("PaymentTemplateAbbrev").item(0);
            if (abbrev != null) {
                paymentTemplateAbbrev = abbrev.getFirstChild().getNodeValue();
            }
            paymentTemplateIds.put(paymentTemplateId, paymentTemplateAbbrev);
            Node isObsolete = (paymentTemplateElement).getElementsByTagName("IsObsolete").item(0);
            if(isObsolete.getParentNode().getNodeName().equals("PaymentTemplate") ){
                if (!isObsolete.getFirstChild().getNodeValue().equalsIgnoreCase("true")) {
                    populateLawsAndFrequencies(paymentTemplateElement);
                }
            }else{
                populateLawsAndFrequencies(paymentTemplateElement);
            }
        }

        mPaymentTemplates.put(pAgencyId, paymentTemplateIds);
    }

    private void getFormTemplateList(NodeList pFormTemplateList, String pAgencyId) {
        ArrayList<String> formTemplateIds = new ArrayList<String>();
        for (int i = 0; i < pFormTemplateList.getLength(); i++) {
            Element formTemplateElement = (Element) pFormTemplateList.item(i);
            Node formTemplateId = formTemplateElement.getElementsByTagName("FormTemplateID").item(0);
            Node descriptionNode = formTemplateElement.getElementsByTagName("UIDescription").item(0);
            String description = descriptionNode.getFirstChild().getNodeValue();
            description = description.replace("\'","''");
            formTemplateIds.add(formTemplateId.getFirstChild().getNodeValue() + "%" + description);
        }

        mFormTemplates.put(pAgencyId, formTemplateIds);
    }

    private void populateLawsAndFrequencies(Element paymentTemplateElement){
        Node paymentTemplateId = paymentTemplateElement.getElementsByTagName("PaymentTemplateID").item(0);

        NodeList lawsList = paymentTemplateElement.getElementsByTagName("Law");

        for (int j = 0; j < lawsList.getLength(); j++) {
            Element a = (Element) lawsList.item(j);
            List<String> columnValues = new ArrayList<String>();
            columnValues.add(a.getElementsByTagName("LawID").item(0).getFirstChild().getNodeValue());
            Node abbrev = a.getElementsByTagName("LawAbbrev").item(0);
            if (abbrev != null) {
                columnValues.add(abbrev.getFirstChild().getNodeValue());
            }
            else{
                columnValues.add(a.getElementsByTagName("Description").item(0).getFirstChild().getNodeValue().replace("'", "''"));
            }
            columnValues.add(a.getElementsByTagName("Description").item(0).getFirstChild().getNodeValue().replace("'", "''"));
            columnValues.add(paymentTemplateId.getFirstChild().getNodeValue());
            mLaws.add(columnValues);

            // If this law has a wage limit, create a wage limit record.
            Node wageLimit = a.getElementsByTagName("TaxableWagebase").item(0);
            if (wageLimit != null) {
                List<String> wageLimitColumnValues = new ArrayList<String>();
                wageLimitColumnValues.add(a.getElementsByTagName("LawID").item(0).getFirstChild().getNodeValue());
                wageLimitColumnValues.add(wageLimit.getFirstChild().getNodeValue());
                mWageLimits.add(wageLimitColumnValues);
            }
        }

        NodeList frequencyList = paymentTemplateElement.getElementsByTagName("PaymentFrequency");
        ArrayList<PaymentTemplateFrequency> paymentFrequencyIds = new ArrayList<PaymentTemplateFrequency>();

        for (int j = 0; j < frequencyList.getLength(); j++) {
            Element paymentFrequencyElement = (Element) frequencyList.item(j);
            String paymentFrequencyID = paymentFrequencyElement.getElementsByTagName("PaymentFrequencyID").item(0).getFirstChild().getNodeValue();
            boolean obsolete = false;
            NodeList isObsoleteNodeList = paymentFrequencyElement.getElementsByTagName("IsObsolete");
            if (isObsoleteNodeList != null && isObsoleteNodeList.getLength() > 0) {
                obsolete = isObsoleteNodeList.item(0).getFirstChild().getNodeValue().equalsIgnoreCase("true");
            }

            paymentFrequencyIds.add(new PaymentTemplateFrequency(paymentFrequencyID, obsolete));
        }

        mPaymentTemplateFrequencies.put(paymentTemplateId.getFirstChild().getNodeValue(), paymentFrequencyIds);
    }


    public Map<String, Map<String, String>> getAgencies() {
        Map<String, Map<String, String>> agencies = new HashMap<String, Map<String, String>>();
        mPaymentTemplates = new HashMap<String, Map<String, String>>();
        mFormTemplates = new HashMap<String, ArrayList<String>>();
        mLaws = new ArrayList<List<String>>();
        mWageLimits = new ArrayList<List<String>>();
        mPaymentTemplateFrequencies = new HashMap<String, ArrayList<PaymentTemplateFrequency>>();
        
        NodeList agencyList = agencyRulesDoc.getElementsByTagName("AgencyID");
        for (int i = 0; i < agencyList.getLength(); i++) {
            Element agency = (Element) agencyList.item(i).getParentNode();
            String agencyName = null;
            String agencyAbbrev = null;
            String agencyId = agency.getElementsByTagName("AgencyID").item(0).getFirstChild().getNodeValue();
            Node name = agency.getElementsByTagName("Name").item(0);
            if (name != null) {
                agencyName = name.getFirstChild().getNodeValue();
            } else {
                agencyName = agencyId;
            }
            Node abbrev = agency.getElementsByTagName(AGENCY_ABBREV).item(0);
            if (abbrev != null) {
                agencyAbbrev = abbrev.getFirstChild().getNodeValue();
            } else {
                agencyAbbrev = agencyId;
            }
            Map<String, String> columns = new HashMap<String, String>();
            columns.put(AGENCY_ID, agencyId);
            columns.put(AGENCY_NAME, agencyName);
            columns.put(AGENCY_ABBREV, agencyAbbrev);
            agencies.put(agencyId, columns);

            //Populate get the payment template ids for the agency Id and create a map for the agency id with
            //payment template ids
            NodeList paymentTemplateList  = agency.getElementsByTagName("PaymentTemplate");
            
            getPaymentTemplateList(paymentTemplateList, agencyId);

            //Populate get the form template ids for the agency Id and create a map for the agency id with
            //form template ids
            NodeList formTemplateList  = agency.getElementsByTagName("FormTemplate");

            getFormTemplateList(formTemplateList, agencyId);
        }
        //
        return agencies;
    }

    private Document agencyRulesDoc;
    private String agencyRulesFolderName;
    private ArrayList tables;
    private ArrayList tableColumns;
    public NodeList properties;

    private HashMap<String, Map<String, String>> mPaymentTemplates=null;
    private List<List<String>> mLaws;
    private List<List<String>> mWageLimits;
    private String mWageLimitsEffectiveQuarter = null;
    private String mWageLimitsEffectiveYear = null;
    private HashMap<String, ArrayList<PaymentTemplateFrequency>> mPaymentTemplateFrequencies=null;
    private HashMap<String, ArrayList<String>> mFormTemplates=null;


    public HashMap<String, Map<String, String>> getPaymentTemplates() {
        this.getAgencies();
        return mPaymentTemplates;
    }

    public List<List<String>> getLaws() {
        this.getAgencies();
        return mLaws;
    }

    public List<List<String>> getWageLimits() {
        return mWageLimits;
    }

    public String getWageLimitsEffectiveQuarter() {
        return mWageLimitsEffectiveQuarter;
    }

    public String getWageLimitsEffectiveYear() {
        return mWageLimitsEffectiveYear;
    }
    public HashMap<String, ArrayList<PaymentTemplateFrequency>> getPaymentTemplateFrequencies() {
        this.getAgencies();
        return mPaymentTemplateFrequencies;
    }

    public HashMap<String, ArrayList<String>> getFormTemplates() {
        this.getAgencies();
        return mFormTemplates;
    }

    public class PaymentTemplateFrequency {
        public String frequencyId;
        public boolean obsolete;

        private PaymentTemplateFrequency(String frequencyId, boolean obsolete) {
            this.frequencyId = frequencyId;
            this.obsolete = obsolete;
        }
    }
}
