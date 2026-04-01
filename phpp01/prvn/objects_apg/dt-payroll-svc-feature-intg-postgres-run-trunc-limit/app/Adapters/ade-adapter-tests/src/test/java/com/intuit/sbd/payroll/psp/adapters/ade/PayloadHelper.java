package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.api.messages.Message;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.AgencyIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.DateUtil;
import com.intuit.schema.payroll.v3.company.TaxItem;
import com.intuit.schema.payroll.v3.company.TaxRate;
import com.intuit.schema.payroll.v3.company.TaxSetup;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 2/4/14
 * Time: 1:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class PayloadHelper {
    public static TaxSetup getTaxSetupAR(String rate) {
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_AR_SUI_ER_UI");
        taxItem.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", "AR"));
        taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("ARESD"));
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupCA(String rate, String ettRate) {
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_CA_SUI_ER_UI");
        taxItem.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", "CA"));
        taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("CAEDD"));
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_CA_SC_ER_ETT");
        taxItem.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", "CA"));
        taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("CAEDD"));
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(ettRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupAZ(String rate, String jttRate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("AZDES");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "AZ");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_AZ_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_AZ_SC_ER_JTT");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(jttRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupCO(String rate, String jttRate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("CODLE");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "CO");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_CO_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_CO_SC_COMBINED");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(jttRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupCT(String rate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("CTDOL");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "CT");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_CT_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);


        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupGA(String rate, String supRate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("GADOL");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "GA");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_GA_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        if (supRate != null) {
            taxItem = new TaxItem();
            taxItem.setId("US_GA_SC_ER_AA");
            taxItem.setJurisdictionId(state);
            taxItem.setAgencyId(agencyID);
            taxItem.setName("");
            taxRateList = new ArrayList<TaxRate>();
            taxRateList.add(getTaxRateList(supRate));
            taxItem.setTaxRates(taxRateList);
            taxLists.add(taxItem);
        }

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupHI(String rate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("HIDLIR");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "HI");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_HI_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);


        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupID(String rate, String supRate) {
        String agencyID = AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId("IDCL");
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "ID");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_ID_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        if(supRate != null){
        taxItem = new TaxItem();
        taxItem.setId("US_ID_SC_ER_WDF");
        taxItem.setJurisdictionId(state);
        taxItem.setAgencyId(agencyID);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(supRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        }

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupKY(String rate,String surChargeRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "KY");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_KY_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_KY_SC_ER_TF");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
         taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(surChargeRate));
        taxItem.setTaxRates(taxRateList);
        //taxItem.setTaxPaymentGroupId("US_RI_TX17_PAYMENT");
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMA(String rate, String emacRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MA");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MA_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_EMAC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMA_AdditionalFiling(String rate, String emacRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MA");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MA_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        SpcfCalendar effectiveDate = PSPDate.getPSPTime().copy();
        int month = effectiveDate.getMonth();
        if ((month - 3) < 0) {
            effectiveDate.addYears(-1);
            effectiveDate.setValues(effectiveDate.getYear(),1,1);
        } else {
            effectiveDate.addMonths(-3);
        }
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)-0.01d).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_EMAC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        effectiveDate = PSPDate.getPSPTime().copy();
        effectiveDate= nextQuarterFirstDate(effectiveDate);
        effectiveDate= nextQuarterFirstDate(effectiveDate);
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)+(0.02d)).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_EMAC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        effectiveDate= nextQuarterFirstDate(effectiveDate);
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)+0.01d).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_EMAC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        ///ER Mac End
        effectiveDate = PSPDate.getPSPTime().copy();
         month = effectiveDate.getMonth();
        if ((month - 3) < 0) {
            effectiveDate.addYears(-1);
            effectiveDate.setValues(effectiveDate.getYear(),1,1);
        } else {
            effectiveDate.addMonths(-3);
        }
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)-0.01d).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_UHI");
        taxItem.setAgencyId("US_MA_WUA");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        effectiveDate = PSPDate.getPSPTime().copy();
        effectiveDate= nextQuarterFirstDate(effectiveDate);
        effectiveDate= nextQuarterFirstDate(effectiveDate);
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)+(0.02d)).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_UHI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxItem.setAgencyId("US_MA_WUA");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        effectiveDate= nextQuarterFirstDate(effectiveDate);
        emacRate  =  Double.valueOf(Double.parseDouble(emacRate)+0.01d).toString();
        taxItem = new TaxItem();
        taxItem.setId("US_MA_SC_ER_UHI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxItem.setAgencyId("US_MA_WUA");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(emacRate,effectiveDate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        //UHI end

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMN(String rate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MN");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MN_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupNY(String rate,String rerRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "NY");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_NY_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        if(rerRate !=null  && rerRate.length() >0) {
            taxItem = new TaxItem();
            taxItem.setId("US_NY_SC_ER_RSF");
            taxItem.setJurisdictionId(state);
            taxItem.setName("");
            taxRateList = new ArrayList<TaxRate>();
            taxRateList.add(getTaxRateList(rerRate));
            taxItem.setTaxRates(taxRateList);
            taxLists.add(taxItem);
        }
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupRI(String rate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "RI");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_RI_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupME(String rate, String cssfRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "ME");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_ME_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        taxItem = new TaxItem();
        taxItem.setId("US_ME_SC_ER_CSSF");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(cssfRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMI(String rate, String oaRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MI");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MI_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
        taxItem = new TaxItem();
        taxItem.setId("US_MI_SC_ER_OA");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(oaRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMS(String rate, String totalRate, String wetRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MS");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MS_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_MS_SC_ER_TCT");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(wetRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupMT(String rate, String totalRate, String wetRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "MT");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_MT_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_MT_SC_ER_AFT");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(wetRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupNH(String rate, String attRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "NH");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_NH_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NH_SC_ER_AC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(attRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupNJ(String rate, String disabiltyRate, String healthcCareRate, String workforceRate, String fliRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "NJ");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_NJ_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NJ_SDI_ER_DI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(disabiltyRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NJ_SC_HEALTHCARE");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(healthcCareRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NJ_SC_WORKFORCE");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(workforceRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NJ_SC_EE_FLI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(fliRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupNV(String rate, String cepRate,String bondRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "NV");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_NV_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NV_SC_ER_CEP");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(cepRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_NV_SC_ER_SBC");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(bondRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupSC(String rate, String iscaRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "SC");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_SC_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_SC_SUI_ER_COMBINED");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(iscaRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }
    public static TaxSetup getTaxSetupSD(String rate, String sdSurcharge,String investmentFee) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "SD");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_SD_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_SD_SC_ER_IF");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(investmentFee));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_SD_SC_ER_SS");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(sdSurcharge));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupTN(String rate, String jsfRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "TN");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_TN_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

         /*     Inactive currenlty
        taxItem= new TaxItem();
        taxItem.setId("US_TN_SC_ER_JSF");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList=new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(jsfRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);
                              */
        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxSetup getTaxSetupWA(String rate, String eafRate) {
        String state = JurisdictionIdMapper.getComplianceJurisdictionId("US", "WA");
        TaxSetup taxsetup = new TaxSetup();
        List<TaxItem> taxLists = new ArrayList<TaxItem>();
        taxsetup.setTaxItems(taxLists);
        TaxItem taxItem = new TaxItem();
        taxItem.setId("US_WA_SUI_ER_UI");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        List<TaxRate> taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(rate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxItem = new TaxItem();
        taxItem.setId("US_WA_SC_ER_EAF");
        taxItem.setJurisdictionId(state);
        taxItem.setName("");
        taxRateList = new ArrayList<TaxRate>();
        taxRateList.add(getTaxRateList(eafRate));
        taxItem.setTaxRates(taxRateList);
        taxLists.add(taxItem);

        taxsetup.setTaxItems(taxLists);
        taxsetup.setLegalName("TEST_COMPANY_1");
        taxsetup.setCountry("USA");
        return taxsetup;
    }

    public static TaxRate getTaxRateList(String rate) {
        TaxRate taxRate = new TaxRate();
        taxRate.setRate(new BigDecimal(rate));
        Date startDate = DateUtil.getQuarterStartDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
        taxRate.setStartDate(startDate);
        return taxRate;

    }

    public static TaxRate getTaxRateList(String rate, SpcfCalendar effectiveDate) {
        if (effectiveDate == null) {
            effectiveDate = PSPDate.getPSPTime();
        }
        TaxRate taxRate = new TaxRate();
        taxRate.setRate(new BigDecimal(rate));
        Date startDate = DateUtil.getQuarterStartDate(new Date(effectiveDate.getTimeInMilliseconds()));
        taxRate.setStartDate(startDate);
        return taxRate;

    }

    private static SpcfCalendar nextQuarterFirstDate(SpcfCalendar date) {
        if (date == null) {
            return date;
        }
        int month = date.getMonth();
        if ((month + 3) > 12) {
            date.addYears(1);
            date.setValues(date.getYear(),1,1);

        } else {
            date.addMonths(3);
        }
        return date;
    }
}
