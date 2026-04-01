package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;

/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 9/8/12
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */

import com.intuit.ems.tfs.messages.v1.*;
import com.intuit.payroll.agency.api.IJurisdiction;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;


public class PayrollFormInfoBuilder {

    private PayrollFormInfo payrollFormInfo = new PayrollFormInfo();
    private Deque<PayrollFormInfo.EmployeeInfo> employeeInfos = new ArrayDeque<PayrollFormInfo.EmployeeInfo>();

    final String JAN_01 = "-01-01";
    final String DEC_31 = "-12-31";

    private static final Map<String, BigInteger> W2_CODES;

    private static final Map<DepositFrequencyCode, DepositFrequencyType> DEPOSIT_FREQUENCY_MAP;

    static {
        DEPOSIT_FREQUENCY_MAP = new HashMap<DepositFrequencyCode, DepositFrequencyType>();
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.ACCELERATED, DepositFrequencyType.THIRD_DAY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.ANNUAL, DepositFrequencyType.ANNUAL);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.EARLYFILER, DepositFrequencyType.EARLY_FILER);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.EIGHTHMONTHLY, DepositFrequencyType.EIGHTH_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.FIVEBANKINGDAY, DepositFrequencyType.FIVE_BANKING_DAY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.MONTHLY, DepositFrequencyType.MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.MONTHLYACCELERATED, DepositFrequencyType.MONTHLY_ACCELERATED);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.NEXTBANKINGDAY, DepositFrequencyType.NEXT_BANKING_DAY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.QUADMONTHLY, DepositFrequencyType.QUAD_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.QUARTERLY, DepositFrequencyType.QUARTERLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.QUARTERMONTHLY, DepositFrequencyType.QUARTER_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.SEMIANNUAL, DepositFrequencyType.SEMI_ANNUAL);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.SEMIMONTHLY, DepositFrequencyType.SEMI_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.SEMIWEEKLY, DepositFrequencyType.SEMI_WEEKLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.SPLITMONTHLY, DepositFrequencyType.SPLIT_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.THREEBANKINGDAY, DepositFrequencyType.THREE_BANKING_DAY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.TWICEMONTHLY, DepositFrequencyType.TWICE_MONTHLY);
        DEPOSIT_FREQUENCY_MAP.put(DepositFrequencyCode.WEEKLY, DepositFrequencyType.WEEKLY);
    }

    static {
        W2_CODES = new HashMap<String, BigInteger>();
        W2_CODES.put("ALLOCTIPS", new BigInteger("51"));
        W2_CODES.put("DPDNTCARE", new BigInteger("6"));
        W2_CODES.put("DPDNTCARECO", new BigInteger("48"));
        W2_CODES.put("NONQUALPLAN", new BigInteger("8"));
        W2_CODES.put("SEC457", new BigInteger("7"));
        W2_CODES.put("ADOPTION", new BigInteger("47"));
        W2_CODES.put("GROUPTERMLIFE", new BigInteger("50"));
        W2_CODES.put("MEDSAVING", new BigInteger("45"));
        W2_CODES.put("NONTAXSICK", new BigInteger("2"));
        W2_CODES.put("QUALMVEX", new BigInteger("29"));
        W2_CODES.put("ROTH401K", new BigInteger("57"));
        W2_CODES.put("ROTH403B", new BigInteger("58"));
        W2_CODES.put("SIMPLE", new BigInteger("46"));
        W2_CODES.put("Q125POP", new BigInteger("53"));
        W2_CODES.put("Q401K", new BigInteger("11"));
        W2_CODES.put("Q403B", new BigInteger("12"));
        W2_CODES.put("Q408K", new BigInteger("13"));
        W2_CODES.put("Q457B", new BigInteger("14"));
        W2_CODES.put("TTT14", new BigInteger("67"));
        W2_CODES.put("Q501C", new BigInteger("15"));
        W2_CODES.put("TTT3", new BigInteger("56"));
        W2_CODES.put("TTT7", new BigInteger("60"));
        W2_CODES.put("TTT8", new BigInteger("61"));
        W2_CODES.put("FRNGBNFTS", new BigInteger("9"));
        W2_CODES.put("OTHER", new BigInteger("3"));
        W2_CODES.put("OTHMVEXP", new BigInteger("10"));
        W2_CODES.put("TTT1", new BigInteger("54"));
        W2_CODES.put("TTT2", new BigInteger("55"));
        W2_CODES.put("TTT4", new BigInteger("57"));
        W2_CODES.put("TTT5", new BigInteger("58"));
        W2_CODES.put("LTAX1", new BigInteger("17"));
        W2_CODES.put("LTAX2", new BigInteger("19"));
        W2_CODES.put("SECLOCAL", new BigInteger("55"));
        W2_CODES.put("TTT11", new BigInteger("64"));
        W2_CODES.put("TTT6", new BigInteger("59"));
        W2_CODES.put("TTT9", new BigInteger("62"));
        W2_CODES.put("TIPS", new BigInteger("4"));
        W2_CODES.put("TTT10", new BigInteger("63"));
        W2_CODES.put("TTT17", new BigInteger("70"));
        W2_CODES.put("TTT19", new BigInteger("72"));

        W2_CODES.put("TTT22", new BigInteger("75"));
        W2_CODES.put("TTT23", new BigInteger("76"));
        W2_CODES.put("TTT24", new BigInteger("77"));
        W2_CODES.put("TTT25", new BigInteger("78"));
        W2_CODES.put("TTT26", new BigInteger("79"));
        W2_CODES.put("TTT27", new BigInteger("80"));
        W2_CODES.put("TTT28", new BigInteger("81"));
        W2_CODES.put("TTT29", new BigInteger("82"));
        W2_CODES.put("TTT30", new BigInteger("83"));
        W2_CODES.put("TTT31", new BigInteger("84"));
        W2_CODES.put("TTT32", new BigInteger("85"));
        W2_CODES.put("TTT33", new BigInteger("86"));
    }

    public PayrollFormInfoBuilder() {
        payrollFormInfo.setCompanyFilingSubmissionID(UUID.randomUUID().toString());
    }

    public PayrollFormInfo getPayrollFormInfo() {
        PayrollFormInfo result = payrollFormInfo;
        result.getEmployeeInfo().clear();
        result.getEmployeeInfo().addAll(employeeInfos);
        return result;
    }

    public List<PayrollFormInfo.EmployeeInfo> getEmployeeInfo() {
        PayrollFormInfo result = payrollFormInfo;
        return result.getEmployeeInfo();
    }

    public PayrollFormInfo.CompanyInfo getCompanyInfo() {
        PayrollFormInfo result = payrollFormInfo;
        return result.getCompanyInfo();
    }

    public void clear() {
        payrollFormInfo = new PayrollFormInfo();
        employeeInfos.clear();
    }

    public PayrollFormInfoBuilder buildCompany(Company pCompany) {
       return  buildCompany( pCompany,0);
    }
    // Company Info
    public PayrollFormInfoBuilder buildCompany(Company pCompany,int yearForDataRun) {
        PayrollFormInfo.CompanyInfo companyInfo = new PayrollFormInfo.CompanyInfo();

        companyInfo.setIntuitPayrollServiceID(pCompany.getSourceCompanyId());
        companyInfo.setLegalName(pCompany.getLegalName());
        companyInfo.setCompanyName(pCompany.getDbaName());
        companyInfo.setEmployerID(pCompany.getFedTaxId());

        companyInfo.setLegalAddressLine1(pCompany.getLegalAddress().getAddressLine1());
        companyInfo.setLegalAddressLine2(pCompany.getLegalAddress().getAddressLine2());
        companyInfo.setLegalCity(pCompany.getLegalAddress().getCity());
        companyInfo.setLegalState(pCompany.getLegalAddress().getState());
        companyInfo.setLegalZip(pCompany.getLegalAddress().getFullZipCode());
        companyInfo.setLegalCountry(pCompany.getLegalAddress().getCountry());

        // Additional Filing Info
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) pCompany.getCompanyService(ServiceCode.Tax);
        PayrollFormInfo.CompanyInfo.AdditionalFilingInfo additionalFilingInfo = new PayrollFormInfo.CompanyInfo.AdditionalFilingInfo();
        additionalFilingInfo.setGenerateAnnualForms(taxCompanyServiceInfo.getFileAnnualReturns());

        //additionalFilingInfo.setCompanyTaxRateQuarter(taxCompanyServiceInfo.get);
        //additionalFilingInfo.setCompanyTaxRateYear();
        //additionalFilingInfo.setGenerateW2Forms();
        //additionalFilingInfo.setOldFEIN(taxCompanyServiceInfo);

        // PSP-1683 - Fix old EIN logic  to PayrollFormInfoBuilder for populating Box H
        EntityChange entityChange = EntityChange.findMostRecentEntityChangeForCompanyWithoutError(pCompany);
        //set previousEmployerId if effectivedate id > 1/1/XXXX  and its the same year for which report is running. Else don't set.
        if(entityChange != null && entityChange.getEffectiveDate() != null && yearForDataRun >0){
            SpcfCalendar einEffetciveDate  = entityChange.getEffectiveDate();
            CalendarUtils.clearTime(einEffetciveDate);
            Calendar firstDayOfYear = Calendar.getInstance();
            firstDayOfYear.set(yearForDataRun,Calendar.JANUARY,1);
            SpcfCalendar  firstDayOfYearDate= SpcfCalendar.createInstance(firstDayOfYear.getTimeInMillis() , SpcfTimeZone.getLocalTimeZone());
            if(yearForDataRun > 0 && einEffetciveDate.compareTo(CalendarUtils.getFirstDayOfTheYearLocal(firstDayOfYearDate))> 0 && einEffetciveDate.getYear() == yearForDataRun){
                String previousEmployerId = entityChange.getOldEIN();
                companyInfo.setPreviousEmployerID(previousEmployerId);
            }
        }
        if (taxCompanyServiceInfo.getFinalAnnualReturns()) {
            additionalFilingInfo.setFinalFormIndicator("Y");
        } else {
            additionalFilingInfo.setFinalFormIndicator("N");
        }
        if (taxCompanyServiceInfo.getLastPayrollDate() != null) {
            additionalFilingInfo.setFinalPayrollDate(taxCompanyServiceInfo.getLastPayrollDate().format(("yyyy-MM-dd")));
        }
        additionalFilingInfo.setFirstFilingQuarter(CalendarUtils.getQuarterAsInt(taxCompanyServiceInfo.getServiceStartDate()));
        additionalFilingInfo.setLastFilingQuarter(taxCompanyServiceInfo.getLastQuarterToFile());
        additionalFilingInfo.setFirstFilingYear(taxCompanyServiceInfo.getServiceStartDate().getYear());
        additionalFilingInfo.setLastFilingYear(taxCompanyServiceInfo.getLastTaxYear());
        additionalFilingInfo.setIntuitPrint(taxCompanyServiceInfo.getW2DeliveryPreferenceCd().equals(DeliveryPreferenceCode.Mail));

        switch (taxCompanyServiceInfo.getStatusCd()) {
            case ActiveCurrent:
            case ActiveSeasonal:
                //Check if any OnHold Reasons exist
                if (!pCompany.isCompanyOnHold()) {
                    additionalFilingInfo.setTaxServiceStatus(TaxServiceStatusType.Active);
                } else {
                    additionalFilingInfo.setTaxServiceStatus(TaxServiceStatusType.OnHold);
                }
                break;
            case AS400Hold:
                additionalFilingInfo.setTaxServiceStatus(TaxServiceStatusType.OnHold);
                break;
            case Cancelled:
            case Terminated:
                additionalFilingInfo.setTaxServiceStatus(TaxServiceStatusType.Inactive);
                break;
            default:
                additionalFilingInfo.setTaxServiceStatus(TaxServiceStatusType.Inactive);
                break;
        }

        additionalFilingInfo.setInHouse(taxCompanyServiceInfo.getInHouseW2());
        additionalFilingInfo.setIncludeOnSSA(taxCompanyServiceInfo.getIncludeOnSSAFile());
        companyInfo.setAdditionalFilingInfo(additionalFilingInfo);

        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, "IRS");
        Criterion<CompanyAgencyFormTemplate> where = CompanyAgencyFormTemplate.CompanyAgency().equalTo(companyAgency)
                                                                              .And(CompanyAgencyFormTemplate.FormTemplate().FormTemplateCd().in("IRS-941-FILING", "IRS-944-FILING"));

        DomainEntitySet<CompanyAgencyFormTemplate> companyAgencyFormTemplates = Application.find(CompanyAgencyFormTemplate.class, where).sort(CompanyAgencyFormTemplate.EffectiveDate().Descending());
        if (companyAgencyFormTemplates.size() > 0) {
            CompanyAgencyFormTemplate companyAgencyFormTemplate = companyAgencyFormTemplates.get(0);
            if (companyAgencyFormTemplate != null) {
                companyInfo.setFederalReturnType(companyAgencyFormTemplate.getFormTemplate().getFormTemplateCd().substring(4, 7));
            }
        }
        payrollFormInfo.setCompanyInfo(companyInfo);
        return this;
    }

    // Company Tax Item Info
    public PayrollFormInfoBuilder addCompanyTaxItemInfo(PayrollFormInfo.CompanyInfo pCompanyInfo, EmployeeW2Totals pEmployeeW2Totals, SpcfCalendar pPayPeriodBeginDate, SpcfCalendar pPayPeriodEndDate) {
        if (getTaxItemInfo(pEmployeeW2Totals.getLaw()) == null) {
            PayrollFormInfo.CompanyInfo.TaxItemInfo taxItemInfo = new PayrollFormInfo.CompanyInfo.TaxItemInfo();
            taxItemInfo.setTaxTableID(new BigInteger(pEmployeeW2Totals.getLaw().getLawId()));
            // Jurisdiction (State) From Agency Rules
            Company foundCompany = null;
            if(pEmployeeW2Totals.getLaw().isIA())
            {
                DomainEntitySet<CompanyPaymentTemplateAgencyId> companyPaymentTemplateAgencyIds = Application.find(CompanyPaymentTemplateAgencyId.class,
                        CompanyPaymentTemplateAgencyId.Name().equalTo("BEN Number")
                                .And(CompanyPaymentTemplateAgencyId.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(pEmployeeW2Totals.getCompany())));
                CompanyPaymentTemplateAgencyId firstcompanyPaymentTemplateAgencyId = companyPaymentTemplateAgencyIds.getFirst();
                if(firstcompanyPaymentTemplateAgencyId!=null){
                    taxItemInfo.setAdditionalAccountNumberWithAgency(firstcompanyPaymentTemplateAgencyId.getAgencyTaxpayerId());
                }
            }

            if (!pEmployeeW2Totals.getLaw().isLaw177()) {
                PaymentTemplate paymentTemplate = pEmployeeW2Totals.getLaw().getPaymentTemplate();
                // If this is a NOCALC Payment Template we need to get the state from the Payment Template Code
                String state = null;

                if (paymentTemplate.getPaymentTemplateCd().contains("NOCALC")) {
                    state = paymentTemplate.getPaymentTemplateCd().substring(0, 2);

                } else {
                    IJurisdiction jurisdiction = paymentTemplate.getAgency().getJurisdiction();
                    if (jurisdiction != null) {
                        state = jurisdiction.getStateID();
                    }
                }
                if (state != null && !state.equals("US")) {
                    taxItemInfo.setState(State50Type.fromValue(state));
                }
            }

            // Account Number
            PaymentTemplate paymentTemplate = pEmployeeW2Totals.getLaw().getPaymentTemplate();
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pEmployeeW2Totals.getCompany(), paymentTemplate);
            if (companyAgencyPaymentTemplate != null) {
                taxItemInfo.setAccountNumberWithAgency(companyAgencyPaymentTemplate.getAgencyTaxpayerId());
            }


            ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate>   taxRates = getTaxRates(pEmployeeW2Totals.getCompanyLaw(), pPayPeriodBeginDate,pPayPeriodEndDate) ;
            for (PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate taxRate:taxRates) {
                taxItemInfo.getTaxRate().add(taxRate);
            }

            ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency> depositFrequencies = getDepositFrequencies(pEmployeeW2Totals.getCompanyLaw(), pPayPeriodBeginDate, pPayPeriodEndDate);
            for (PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency depositFrequency:depositFrequencies) {
                taxItemInfo.getDepositFrequency().add(depositFrequency);
            }
            
            pCompanyInfo.getTaxItemInfo().add(taxItemInfo);
        }
        return this;
    }

    // Company Federal Totals

    public PayrollFormInfoBuilder addCompanyFederalTotals(PayrollFormInfo.CompanyInfo pCompanyInfo, EmployeeW2Totals pEmployeeW2Totals, int pYear) {
        List<FederalTotals> federalTotalsList = pCompanyInfo.getFederalTotals();
        FederalTotals federalTotals = new FederalTotals();
        if (federalTotalsList.size() > 0) {
            federalTotals = federalTotalsList.get(0);
            federalTotalsList.remove(0);
        }

        federalTotals.setPeriodBeginDate(pYear + JAN_01);
        federalTotals.setPeriodEndDate(pYear + DEC_31);

        // Social Security - Law = FICA
        if (pEmployeeW2Totals.getLaw().isFICA_EE()) {
            federalTotals.setSocialSecurityWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            federalTotals.setSocialSecurityLiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setSocialSecurityTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount()));
        }

        // Federal - Law = FIT
        if (pEmployeeW2Totals.getLaw().isFIT()) {
            federalTotals.setFederalWithholding(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setFederalWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
        }

        // FUTA
        if (pEmployeeW2Totals.getLaw().isFUTA()) {
            federalTotals.setFUTALiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setTaxableFUTAWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            federalTotals.setTotalFUTAWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages()));
            LawStatus exemptStatus = pEmployeeW2Totals.getCompanyLaw().getExemptionStatus();
            if (exemptStatus != null && exemptStatus.equals(LawStatus.Exempt)) {
                pCompanyInfo.setIsFUTAExempt(true);
            } else {
                pCompanyInfo.setIsFUTAExempt(false);
            }
        }

        // MEDICARE
        if (pEmployeeW2Totals.getLaw().getLawId().equals(Law.EEMED)) {
            federalTotals.setMedicareLiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setMedicareWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
        }

        // EIC
        if (pEmployeeW2Totals.getLaw().isAEIC()) {
            federalTotals.setEIC(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
        }

        pCompanyInfo.getFederalTotals().add(federalTotals);
        return this;

    }

    // Company W2 Totals

    public PayrollFormInfoBuilder addCompanyW2Totals(PayrollFormInfo.CompanyInfo pCompanyInfo, CompanyPayrollItem pCompanyPayrollItem, SpcfDecimal pAmount) {
        W2Totals w2Totals = new W2Totals();
        w2Totals.setCode(W2_CODES.get(pCompanyPayrollItem.getTaxFormLine()));
        w2Totals.setAmount(SpcfUtils.convertToBigDecimal(pAmount));
        w2Totals.setPayrollItemID(new BigInteger(pCompanyPayrollItem.getSourcePayrollItemId()));
        pCompanyInfo.getW2Totals().add(w2Totals);
        return this;
    }

    // Company Tax Item Totals

    public PayrollFormInfoBuilder addCompanyTaxItemTotals(PayrollFormInfo.CompanyInfo pCompanyInfo, EmployeeW2Totals pEmployeeW2Totals, SpcfCalendar pPayPeriodBeginDate, SpcfCalendar pPayPeriodEndDate, boolean pIncludePayrollItem) {
        String lawId = pEmployeeW2Totals.getLaw().getLawId();
        TaxItemTotals taxItemTotals = getTaxItemTotals(pCompanyInfo.getTaxItemTotals(), lawId);

        if(taxItemTotals == null) {
            taxItemTotals = new TaxItemTotals();
            taxItemTotals.setPeriodBeginDate(pPayPeriodBeginDate.format("yyyy-MM-dd"));
            taxItemTotals.setPeriodEndDate(pPayPeriodEndDate.format("yyyy-MM-dd"));
            taxItemTotals.setTaxTableID(new BigInteger(pEmployeeW2Totals.getLaw().getLawId()));
            taxItemTotals.setTaxAmount(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            taxItemTotals.setTotalTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount()));
            taxItemTotals.setTaxableWagesAndTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            taxItemTotals.setTotalWagesAndTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages()));
        }else {
            pCompanyInfo.getTaxItemTotals().remove(taxItemTotals);
            taxItemTotals.setPeriodBeginDate(pPayPeriodBeginDate.format("yyyy-MM-dd"));
            taxItemTotals.setPeriodEndDate(pPayPeriodEndDate.format("yyyy-MM-dd"));
            taxItemTotals.setTaxTableID(new BigInteger(pEmployeeW2Totals.getLaw().getLawId()));
            taxItemTotals.setTaxAmount(taxItemTotals.getTaxAmount().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount())));
            taxItemTotals.setTotalTips(taxItemTotals.getTotalTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount())));
            taxItemTotals.setTaxableWagesAndTips(taxItemTotals.getTaxableWagesAndTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages())));
            taxItemTotals.setTotalWagesAndTips(taxItemTotals.getTotalWagesAndTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages())));
        }

        if (pIncludePayrollItem) {
            taxItemTotals.setPayrollItemID(new BigInteger(pEmployeeW2Totals.getCompanyLaw().getSourceId()));
        }
        pCompanyInfo.getTaxItemTotals().add(taxItemTotals);
        return this;
    }


    // Company Payroll Item Info

    public PayrollFormInfoBuilder addPayrollItemInfo(PayrollFormInfo.CompanyInfo pCompanyInfo, CompanyLaw pCompanyLaw) {
        PayrollFormInfo.CompanyInfo.PayrollItemInfo payrollItemInfo = new PayrollFormInfo.CompanyInfo.PayrollItemInfo();
        payrollItemInfo.setName(pCompanyLaw.getSourceDescription());
        payrollItemInfo.setPayrollItemID(new BigInteger(pCompanyLaw.getSourceId()));
        QbdtPayrollItemInfo pItemInfo = pCompanyLaw.getQbdtPayrollItemInfo();
        
        if (pItemInfo!=null) {
            payrollItemInfo.setIsEmployeePaid(pItemInfo.getIsEmployeePaid());
        }

        if (pItemInfo != null && pItemInfo.getAgencyId() != null) {
            payrollItemInfo.setAccountNumberWithAgency(pItemInfo.getAgencyId());
        } else {
            PaymentTemplate paymentTemplate = pCompanyLaw.getLaw().getPaymentTemplate();
            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompanyLaw.getCompanyAgency().getCompany(), paymentTemplate);
            if (companyAgencyPaymentTemplate != null) {
                payrollItemInfo.setAccountNumberWithAgency(companyAgencyPaymentTemplate.getAgencyTaxpayerId());
            }
        }
        payrollItemInfo.setTaxTableID(new BigInteger(pCompanyLaw.getLaw().getLawId()));
        pCompanyInfo.getPayrollItemInfo().add(payrollItemInfo);
        return this;
    }

    public PayrollFormInfoBuilder addPayrollItemInfo(PayrollFormInfo.CompanyInfo pCompanyInfo, CompanyPayrollItem pCompanyPayrollItem) {
        PayrollFormInfo.CompanyInfo.PayrollItemInfo payrollItemInfo = new PayrollFormInfo.CompanyInfo.PayrollItemInfo();
        payrollItemInfo.setName(pCompanyPayrollItem.getSourceDescription());
        payrollItemInfo.setPayrollItemID(new BigInteger(pCompanyPayrollItem.getSourcePayrollItemId()));
        pCompanyInfo.getPayrollItemInfo().add(payrollItemInfo);
        return this;
    }

    // Employee Info

    public PayrollFormInfoBuilder addEmployee(Employee pEmployee) {
        PayrollFormInfo.EmployeeInfo employeeInfo = new PayrollFormInfo.EmployeeInfo();
        employeeInfo.setEmployeeID(pEmployee.getSourceEmployeeId());
        employeeInfo.setFirstName(pEmployee.getFirstName());
        employeeInfo.setMiddleInitial(pEmployee.getMiddleName());
        employeeInfo.setLastName(pEmployee.getLastName());
        //PSP-7001, Replace the ssn with 000000000 if null
        String ssn = pEmployee.getTaxId() != null ? pEmployee.getTaxId() : "000000000";
        employeeInfo.setSSN(ssn);
        employeeInfo.setIsStatutory(pEmployee.getIsStatutory());
        employeeInfo.setHasPensionPlan(pEmployee.getHasRetirementPlan());
        if (pEmployee.getLiveState() != null) {
            employeeInfo.setStateLived(pEmployee.getLiveState());
        }
        if (pEmployee.getWorkState() != null) {
            employeeInfo.setStateWorked(pEmployee.getWorkState());
        }
        if (pEmployee.getMailingAddress() != null) {
            employeeInfo.setAddressLine1(pEmployee.getMailingAddress().getAddressLine1());
            employeeInfo.setAddressLine2(pEmployee.getMailingAddress().getAddressLine2());
            employeeInfo.setCity(pEmployee.getMailingAddress().getCity());
            employeeInfo.setState(pEmployee.getMailingAddress().getState());
            employeeInfo.setZipCode(pEmployee.getMailingAddress().getFullZipCode());
        }
        if (pEmployee.getHireDate() != null) {
            employeeInfo.setHireDate(pEmployee.getHireDate().format("yyyy-MM-dd"));
        }
        if (pEmployee.getTerminationDate() != null) {
            employeeInfo.setReleaseDate(pEmployee.getTerminationDate().format("yyyy-MM-dd"));
        }
        employeeInfos.push(employeeInfo);
        payrollFormInfo.getEmployeeInfo().add(employeeInfo);
        return this;
    }

    // Employee Federal Totals
    public PayrollFormInfoBuilder addEmployeeFederalTotals(PayrollFormInfo.EmployeeInfo pEmployeeInfo, EmployeeW2Totals pEmployeeW2Totals, int pYear) {
        List<FederalTotals> federalTotalsList = pEmployeeInfo.getFederalTotals();
        FederalTotals federalTotals = new FederalTotals();
        if (federalTotalsList.size() > 0) {
            federalTotals = federalTotalsList.get(0);
            federalTotalsList.remove(0);
        }

        federalTotals.setPeriodBeginDate(pYear + JAN_01);
        federalTotals.setPeriodEndDate(pYear + DEC_31);

        // Social Security - Law = FICA
        if (pEmployeeW2Totals.getLaw().isFICA_EE()) {
            federalTotals.setSocialSecurityWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            federalTotals.setSocialSecurityLiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setSocialSecurityTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount()));
        }

        // Federal - Law = FIT
        if (pEmployeeW2Totals.getLaw().isFIT()) {
            federalTotals.setFederalWithholding(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setFederalWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
        }

        // FUTA
        if (pEmployeeW2Totals.getLaw().isFUTA()) {
            federalTotals.setFUTALiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setTaxableFUTAWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            federalTotals.setTotalFUTAWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages()));
        }

        // MEDICARE
        if (pEmployeeW2Totals.getLaw().getLawId().equals(Law.EEMED)) {
            federalTotals.setMedicareLiability(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            federalTotals.setMedicareWages(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
        }

        // EIC
        if (pEmployeeW2Totals.getLaw().isAEIC()) {
            federalTotals.setEIC(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
        }

        pEmployeeInfo.getFederalTotals().add(federalTotals);
        return this;
    }

    // Employee W2 Totals
    public PayrollFormInfoBuilder addEmployeeW2Totals(PayrollFormInfo.EmployeeInfo pEmployeeInfo, CompanyPayrollItem pCompanyPayrollItem, SpcfDecimal pAmount) {
        W2Totals w2Totals = new W2Totals();
        w2Totals.setCode(W2_CODES.get(pCompanyPayrollItem.getTaxFormLine()));
        w2Totals.setPayrollItemID(new BigInteger(pCompanyPayrollItem.getSourcePayrollItemId()));
        w2Totals.setAmount(SpcfUtils.convertToBigDecimal(pAmount));
        pEmployeeInfo.getW2Totals().add(w2Totals);
        return this;
    }

    // Employee Tax Item Totals
    public PayrollFormInfoBuilder addEmployeeTaxItemTotals(PayrollFormInfo.EmployeeInfo pEmployeeInfo, EmployeeW2Totals pEmployeeW2Totals, SpcfCalendar pPayPeriodBeginDate, SpcfCalendar pPayPeriodEndDate, boolean pIncludePayrollItem) {

        String lawId =  pEmployeeW2Totals.getLaw().getLawId();
        TaxItemTotals taxItemTotals = getTaxItemTotals(pEmployeeInfo.getTaxItemTotals(), lawId);
        if(taxItemTotals == null) {
            taxItemTotals=new TaxItemTotals();
            taxItemTotals.setPeriodBeginDate(pPayPeriodBeginDate.format("yyyy-MM-dd"));
            taxItemTotals.setPeriodEndDate(pPayPeriodEndDate.format("yyyy-MM-dd"));
            taxItemTotals.setTaxTableID(new BigInteger(pEmployeeW2Totals.getLaw().getLawId()));
            taxItemTotals.setTaxAmount(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount()));
            taxItemTotals.setTotalTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount()));
            taxItemTotals.setTaxableWagesAndTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages()));
            taxItemTotals.setTotalWagesAndTips(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages()));
        }else {
            pEmployeeInfo.getTaxItemTotals().remove(taxItemTotals);
            taxItemTotals.setPeriodBeginDate(pPayPeriodBeginDate.format("yyyy-MM-dd"));
            taxItemTotals.setPeriodEndDate(pPayPeriodEndDate.format("yyyy-MM-dd"));
            taxItemTotals.setTaxTableID(new BigInteger(pEmployeeW2Totals.getLaw().getLawId()));
            taxItemTotals.setTaxAmount(taxItemTotals.getTaxAmount().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getAmount() == null ? SpcfMoney.ZERO : pEmployeeW2Totals.getAmount())));
            taxItemTotals.setTotalTips(taxItemTotals.getTotalTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTipsTaxableWagesAmount())));
            taxItemTotals.setTaxableWagesAndTips(taxItemTotals.getTaxableWagesAndTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTaxableWages())));
            taxItemTotals.setTotalWagesAndTips(taxItemTotals.getTotalWagesAndTips().add(SpcfUtils.convertToBigDecimal(pEmployeeW2Totals.getTotalWages())));
        }
        if (pIncludePayrollItem) {
               taxItemTotals.setPayrollItemID(new BigInteger(pEmployeeW2Totals.getCompanyLaw().getSourceId()));
        }

        pEmployeeInfo.getTaxItemTotals().add(taxItemTotals);
        return this;
    }


    // Helper Methods

    public PayrollFormInfoBuilder addEmployees(ArrayList<Employee> pEmployees) {
        for (Employee employee : pEmployees) {
            addEmployee(employee);
        }
        return this;
    }

    public boolean containsEmployee(Employee pEmployee) {
        List<PayrollFormInfo.EmployeeInfo> eeInfos = payrollFormInfo.getEmployeeInfo();
        if (eeInfos.size() > 0) {
            for (PayrollFormInfo.EmployeeInfo eeInfo : eeInfos) {
                if (eeInfo.getEmployeeID().equals(pEmployee.getSourceEmployeeId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public PayrollFormInfo.EmployeeInfo getEmployeeInfo(Employee pEmployee) {
        List<PayrollFormInfo.EmployeeInfo> eeInfos = payrollFormInfo.getEmployeeInfo();
        if (eeInfos.size() > 0) {
            for (PayrollFormInfo.EmployeeInfo eeInfo : eeInfos) {
                if (eeInfo.getEmployeeID().equals(pEmployee.getSourceEmployeeId())) {
                    return eeInfo;
                }
            }
        }
        return null;
    }

    public PayrollFormInfo.CompanyInfo.TaxItemInfo getTaxItemInfo(Law pLaw) {
        List<PayrollFormInfo.CompanyInfo.TaxItemInfo> taxItemInfoList = payrollFormInfo.getCompanyInfo().getTaxItemInfo();
        if (taxItemInfoList.size() > 0) {
            for (PayrollFormInfo.CompanyInfo.TaxItemInfo taxItemInfo : taxItemInfoList) {
                if (taxItemInfo.getTaxTableID().equals(pLaw.getLawId())) {
                    return taxItemInfo;
                }
            }
        }
        return null;
    }


    private ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency> getDepositFrequencies(CompanyLaw pCompanyLaw, SpcfCalendar pBeginDate, SpcfCalendar pEndDate) {
        ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency> depositFrequencies = new ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency>();

        EffectiveDepositFrequency  domainDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(
                pCompanyLaw.getCompanyAgency().getCompany(), pCompanyLaw.getLaw().getPaymentTemplate(), pEndDate);

        if (domainDepositFrequency != null) {
            DepositFrequencyType xmlDepositFrequencyCode =  DEPOSIT_FREQUENCY_MAP.get(domainDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());

            if (xmlDepositFrequencyCode!=null) {
                PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency xmlDepositFrequency = new PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency();

                xmlDepositFrequency.setDepositFrequency(xmlDepositFrequencyCode);

                if (domainDepositFrequency.getEffectiveDate()!=null) {
                    xmlDepositFrequency.setEffectiveDate(domainDepositFrequency.getEffectiveDate().format("yyyy-MM-dd"));
                }

                depositFrequencies.add(xmlDepositFrequency);
            }
        }

        return depositFrequencies;
    }

    private ArrayList< PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate> getTaxRates(CompanyLaw pCompanyLaw, SpcfCalendar pBeginDate, SpcfCalendar pEndDate) {
        HashMap<Integer,BigDecimal>   effectiveYearMonths = new HashMap<Integer, BigDecimal>() ;
        Set< PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate> taxRateSet = new TreeSet<PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate>(new Comparator<PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate>() {
            public int compare(PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate taxRate1, PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate taxRate2) {
                int resultYear = taxRate1.getPeriod().getYear().compareTo(taxRate2.getPeriod().getYear());
                if(resultYear == 0){
                    int resultMonth = taxRate1.getPeriod().getQuarter().compareTo(taxRate2.getPeriod().getQuarter());
                    return resultMonth == 0 ? (taxRate1.getPeriod().getMonth().compareTo(taxRate2.getPeriod().getMonth())) : resultMonth;
                } else {
                    return resultYear;
                }
            }
        });
        Criterion<CompanyLawRate> where = CompanyLawRate.CompanyLaw().equalTo(pCompanyLaw)
                                                        .And(CompanyLawRate.InvalidDate().isNull());

        DomainEntitySet<CompanyLawRate> companyLawRates = Application.find(CompanyLawRate.class, where);
        for (CompanyLawRate companyLawRate : companyLawRates) {
            if (companyLawRate.isExpiredAsOf(pBeginDate)) {
                continue;
            }

            SpcfCalendar rateEffectiveDate = PSPDate.getPSPTime();
            SpcfCalendar rateExpirationDate = PSPDate.getPSPTime();
            if (companyLawRate.getEffectiveDate() != null) {
                rateEffectiveDate = companyLawRate.getEffectiveDate();
            }
            SpcfCalendar calculatedExpirationDate = companyLawRate.calculateExpirationDate();
            if (calculatedExpirationDate != null) {
                rateExpirationDate = calculatedExpirationDate;
            }
            int startYearMonth = rateEffectiveDate.getYear() * 100 + rateEffectiveDate.getMonth();
            int endYearMonth = rateExpirationDate.getYear() * 100 + rateExpirationDate.getMonth();
            int processingPeriodBegin = pBeginDate.getYear() * 100 + pBeginDate.getMonth();
            int processingPeriodEnd = pEndDate.getYear() * 100 + pEndDate.getMonth();
            if (processingPeriodBegin > startYearMonth) {
                startYearMonth = processingPeriodBegin;
            }
            BigDecimal rateAmount = new BigDecimal(companyLawRate.getRate()).setScale(6, RoundingMode.HALF_EVEN);
            while (startYearMonth <= endYearMonth && startYearMonth <= processingPeriodEnd ) {
                effectiveYearMonths.put(startYearMonth,rateAmount);
                PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate taxRate = new PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate();
                SpcfCalendar effectivePeriodDate = SpcfCalendar.createInstance(startYearMonth / 100, startYearMonth % 100, 1);
                Period period = new Period();
                period.setYear(BigInteger.valueOf(effectivePeriodDate.getYear()));
                period.setMonth(BigInteger.valueOf(effectivePeriodDate.getMonth()));
                period.setQuarter(BigInteger.valueOf(CalendarUtils.getQuarterAsInt(effectivePeriodDate)));
                taxRate.setPeriod(period);
                taxRate.setRate(new BigDecimal(companyLawRate.getRate()).setScale(6, RoundingMode.HALF_EVEN));
                taxRateSet.add(taxRate);
                startYearMonth++;
                if (startYearMonth % 100 == 13) {
                    startYearMonth = (startYearMonth/100 + 1) * 100 + 1;
                }
            }
        }
        return new ArrayList<PayrollFormInfo.CompanyInfo.TaxItemInfo.TaxRate>(taxRateSet);
    }

    public PayrollFormInfoBuilder buildDataSpace(FilingTypeType pDataspace, BigInteger pYear) {
        PayrollFormInfo.DataSpace dataspace = new PayrollFormInfo.DataSpace();

        dataspace.setFilingType(pDataspace);
        dataspace.setYear(pYear);

        payrollFormInfo.setDataSpace(dataspace);

        return this;
    }

    private static final String LOCAL_TAXES_LAW_ID="177";

    public TaxItemTotals getTaxItemTotals(List<TaxItemTotals> taxItemTotals, String lawId) {
        if (lawId.equals(LOCAL_TAXES_LAW_ID)) {
            return null;
        } else {
            for (TaxItemTotals itemTotals : taxItemTotals) {
                if (StringUtils.equals(itemTotals.getTaxTableID().toString(), lawId)) {
                    return itemTotals;
                }
            }
        }
        return null;
    }

    public static String getW2Codes(String taxFormLine) {
        W2Totals w2Totals = new W2Totals();
        w2Totals.setCode(W2_CODES.get(taxFormLine));
        return String.valueOf(w2Totals.getCode());
    }
}
