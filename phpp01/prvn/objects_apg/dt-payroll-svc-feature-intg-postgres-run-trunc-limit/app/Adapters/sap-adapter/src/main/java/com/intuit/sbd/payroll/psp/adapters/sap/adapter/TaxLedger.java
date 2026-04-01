package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawItem;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawTransactions;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.TaxPeriod;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.RefundType;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * User: dweinberg
 * Date: 12/17/12
 * Time: 10:46 AM
 * Extracted from TaxAdapter
 */
public class TaxLedger {

    private static final String LS_PAYROLL = "Payroll";
    private static final String LS_PAYMENT = "Payment";
    private static final String LS_TOR = "Take on Return";
    private static final String LS_ADJUSTMENT = "Adjustment";
    private static final String LS_REFUND = "Refund";
    private static final String RefundPaymentStatus = "Completed";
    private static final String RefundPaymentMethod = "ACHCredit";

    static void findTaxTransactions(String paymentTemplateCd, String paymentMethod, boolean includeNotPostedPayments, boolean includePayrolls, boolean includePayments, boolean includeRefund, boolean includeAdjustments, boolean includeTORs, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, Company company, Map<String, SAPLawTransactions> sapLawTransactionsMap, SAPLawTransactions summaryTransactions, SpcfCalendar startDate) {
        if (includePayrolls || includeAdjustments || includeTORs) {
            // find all of the payroll runs for the company.  CLAs and ELAs, and Reconciling Adjustments are also stored as PayrollRuns
            DomainEntitySet<PayrollRun> payrollRuns = getPayrollRuns(company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);
            if (payrollRuns.size() > 0) {
                if (includePayrolls || includeAdjustments) {
                    Map<SpcfUniqueId, Map<String, LiabilitiesWages>> payrollLiabilitiesWages = rollupLiabilitiesAndWages(company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments, sapLawTransactionsMap.keySet());
                    Map<SpcfUniqueId, List<CompanyAdjustmentSubmission>> voids = getVoids(company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);
                    Map<SpcfUniqueId, Map<String, LiabilitiesWages>> voidLiabilitiesWages = rollupVoidLiabilitiesAndWages(company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments, sapLawTransactionsMap.keySet());
                    Map<SpcfUniqueId, PayrollAdditionalInfo> additionalPayrollInfo = getAdditionalPayrollInfo(company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);

                    addTaxTransactionsFromPayrolls(sapLawTransactionsMap,
                                                   summaryTransactions,
                                                   payrollRuns,
                                                   paymentTemplateCd,
                                                   payrollLiabilitiesWages,
                                                   voids,
                                                   voidLiabilitiesWages,
                                                   additionalPayrollInfo,
                                                   includePayrolls,
                                                   includeAdjustments);
                }
                if (includeTORs) {
                    //performance short-cut... TORs are only on adjustments
                    addTaxTransactionsFromTORPayrolls(sapLawTransactionsMap,
                                                      summaryTransactions,
                                                      payrollRuns.find(PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)),
                                                      paymentTemplateCd);
                }
            }
        }

        if (includePayments || includeRefund) {
            createPaymentSAPTransactions(sapLawTransactionsMap, summaryTransactions, company, paymentMethod, paymentTemplateCd, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments, includeRefund);
            }

        sumTransactionsMapAndRemoveUnrequestedTransactions(sapLawTransactionsMap, summaryTransactions, startDate, yearQuarterEndCalendar);
    }

    //Payroll Run ID => [Law ID => Liability/Wages]
    private static Map<SpcfUniqueId, Map<String, LiabilitiesWages>> rollupLiabilitiesAndWages(Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments, Collection<String> lawIds) {
        HqlBuilder taxHql = new HqlBuilder(true, "select pr.Id, tax.Law.LawId, sum(tax.TaxLiabilityAmount), sum(tax.TaxableWagesAmount) " +
                                                   " from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                   " join pr.PaycheckSet pc " +
                                                   " join pc.TaxSet tax " +
                                                   "where pr.Company = :company " +
                                                   "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                   "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) " +
                                                   "and pc.SourcePaycheckId not like '-%'"+
                                                   "and (pc.Status <> 'Inactive' or pc.CompanyAdjustmentSubmission is not null) " +
                                                   "and tax.Law.LawId  in (:lawIds) " +
                                                   "and not ( tax.Law.PaymentTemplate.ProcessingStartDate is not null and pr.PaycheckDate < tax.Law.PaymentTemplate.ProcessingStartDate and tax.CreatedDate  < :backdateStart) " +
                                                   "group by pr.Id, tax.Law.LawId");

        HqlBuilder laHql = new HqlBuilder(true, "select pr.Id, la.Law.LawId, sum(la.Amount), sum(la.TaxableWages) " +
                                                  " from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                  " join pr.LiabilityAdjustmentSet la " +
                                                  "where pr.Company = :company " +
                                                  "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                  "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) " +
                                                  "and  not exists (select 'T' from com.intuit.sbd.payroll.psp.domain.Paycheck as pc where pc.PayrollRun.Id = pr.Id and pc.SourcePaycheckId like '-%')"+
                                                  "and la.Law.LawId  in (:lawIds) " +
                                                  "and not ( (la.Law.PaymentTemplate.ProcessingStartDate is null or pr.PaycheckDate < la.Law.PaymentTemplate.ProcessingStartDate) and la.CreatedDate  < :backdateStart) " +
                                                  "group by pr.Id, la.Law.LawId");

        setPayrollParameters(taxHql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);
        setPayrollParameters(laHql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);

        SpcfCalendar backdateProcessingBegan = SystemParameter.findCalendarValue(SystemParameter.Code.PSP_BACKDATE_PROCESSING_BEGIN);

        taxHql.setParameterList("lawIds", lawIds.toArray());
        laHql.setParameterList("lawIds", lawIds.toArray());

        taxHql.setParameter("backdateStart", backdateProcessingBegan);
        laHql.setParameter("backdateStart", backdateProcessingBegan);

        Map<SpcfUniqueId, Map<String, LiabilitiesWages>> liabilitiesWagesMap = new HashMap<SpcfUniqueId, Map<String, LiabilitiesWages>>();

        List<Object[]> taxInfoList = taxHql.list();
        addLiabilitiesAndWages(liabilitiesWagesMap, taxInfoList);
        List<Object[]> laInfoList = laHql.list();
        addLiabilitiesAndWages(liabilitiesWagesMap, laInfoList);

        return liabilitiesWagesMap;
    }

    private static void addLiabilitiesAndWages(Map<SpcfUniqueId, Map<String, LiabilitiesWages>> liabilitiesWagesMap, List<Object[]> infoList) {
        for (Object[] taxInfo : infoList){
            SpcfUniqueId payrollRunId = (SpcfUniqueId) taxInfo[0];
            String lawId = (String) taxInfo[1];
            SpcfMoney liabilitiesAmount = (SpcfMoney) taxInfo[2];
            SpcfMoney wagesAmount = (SpcfMoney) taxInfo[3];

            if (!liabilitiesWagesMap.containsKey(payrollRunId)) {
                liabilitiesWagesMap.put(payrollRunId, new HashMap<String, LiabilitiesWages>());
            }
            Map<String, LiabilitiesWages> payrollRunLiabilityWagesMap = liabilitiesWagesMap.get(payrollRunId);

            if (!payrollRunLiabilityWagesMap.containsKey(lawId)) {
                payrollRunLiabilityWagesMap.put(lawId, new LiabilitiesWages());
            }
            payrollRunLiabilityWagesMap.get(lawId).addLiabilities(liabilitiesAmount);
            payrollRunLiabilityWagesMap.get(lawId).addWages(wagesAmount);
        }
    }

    private static class LiabilitiesWages {
        public SpcfMoney liabilities = SpcfMoney.ZERO;
        public SpcfMoney wages = SpcfMoney.ZERO;
        public void addLiabilities(SpcfMoney pLiabilities) {
            if (pLiabilities != null) {
                liabilities = new SpcfMoney(liabilities.add(pLiabilities));
            }
        }
        public void addWages(SpcfMoney pWages) {
            if (pWages != null) {
                wages = new SpcfMoney(wages.add(pWages));
            }
        }
    }

    private static Map<SpcfUniqueId, Map<String, LiabilitiesWages>> rollupVoidLiabilitiesAndWages(Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments, Collection<String> lawIds) {
        HqlBuilder taxHql = new HqlBuilder(true, "select pc.CompanyAdjustmentSubmission.Id, tax.Law.LawId, sum(tax.TaxLiabilityAmount), sum(tax.TaxableWagesAmount) " +
                                                   " from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                   " join pr.PaycheckSet pc " +
                                                   " join pc.TaxSet tax " +
                                                   "where pr.Company = :company " +
                                                   "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                   "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) " +
                                                   "and pc.SourcePaycheckId not like '-%'"+
                                                   "and pc.CompanyAdjustmentSubmission is not null " +
                                                   "and tax.Law.LawId  in (:lawIds) " +
                                                   "and not ( (tax.Law.PaymentTemplate.ProcessingStartDate is null or pr.PaycheckDate < tax.Law.PaymentTemplate.ProcessingStartDate) and tax.CreatedDate  < :backdateStart) " +
                                                   "group by pc.CompanyAdjustmentSubmission.Id, tax.Law.LawId");
        setPayrollParameters(taxHql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);

        taxHql.setParameterList("lawIds", lawIds.toArray());

        SpcfCalendar backdateProcessingBegan = SystemParameter.findCalendarValue(SystemParameter.Code.PSP_BACKDATE_PROCESSING_BEGIN);
        taxHql.setParameter("backdateStart", backdateProcessingBegan);

        Map<SpcfUniqueId, Map<String, LiabilitiesWages>> liabilitiesWagesMap = new HashMap<SpcfUniqueId, Map<String, LiabilitiesWages>>();

        List<Object[]> taxInfoList = taxHql.list();
        addLiabilitiesAndWages(liabilitiesWagesMap, taxInfoList);

        return liabilitiesWagesMap;
    }

    private static Map<SpcfUniqueId, List<CompanyAdjustmentSubmission>> getVoids(Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments) {
        HqlBuilder voidHql = new HqlBuilder(true, "select pr.Id, pc.CompanyAdjustmentSubmission" +
                                                    " from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                    " join pr.PaycheckSet pc " +
                                                    "where pr.Company = :company " +
                                                    "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                    "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) " +
                                                    "and pc.SourcePaycheckId not like '-%'"+
                                                    "and pc.CompanyAdjustmentSubmission is not null");
        setPayrollParameters(voidHql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);

        Map<SpcfUniqueId, List<CompanyAdjustmentSubmission>> voidMap = new HashMap<SpcfUniqueId, List<CompanyAdjustmentSubmission>>();

        List<Object[]> voids = voidHql.list();
        for (Object[] aVoid : voids) {
            SpcfUniqueId payrollRunId = (SpcfUniqueId) aVoid[0];
            CompanyAdjustmentSubmission cas = (CompanyAdjustmentSubmission) aVoid[1];

            if (! voidMap.containsKey(payrollRunId)) {
                voidMap.put(payrollRunId, new ArrayList<CompanyAdjustmentSubmission>());
            }
            if (! voidMap.get(payrollRunId).contains(cas)) {
                voidMap.get(payrollRunId).add(cas);
            }
        }

        return voidMap;
    }

    private static Map<SpcfUniqueId, PayrollAdditionalInfo> getAdditionalPayrollInfo(Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments) {
        HqlBuilder hql = new HqlBuilder(true, "select pr.Id, " +
                                                "case when exists (select 'T' from pr.LiabilityAdjustmentSet la where la.IsReconcilingAdjustment = true) then 'Adjustment' else 'Regular' end, " +
                                                "case when exists (select 'T' from pr.FinancialTransactionSet ft) then 'Regular' else 'Recorded' end " +
                                                "from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                "where pr.Company = :company " +
                                                "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) " +
                                                "and  not exists (select 'T' from com.intuit.sbd.payroll.psp.domain.Paycheck as pc where pc.PayrollRun.Id = pr.Id and pc.SourcePaycheckId like '-%')");
        setPayrollParameters(hql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);

        Map<SpcfUniqueId, PayrollAdditionalInfo> payrollAdditionalInfoMap = new HashMap<SpcfUniqueId, PayrollAdditionalInfo>();
        List<Object[]> infoList = hql.list();

        for (Object[] info : infoList) {
            payrollAdditionalInfoMap.put((SpcfUniqueId) info[0], new PayrollAdditionalInfo(info[1].equals("Adjustment"), info[2].equals("Recorded")));
        }

        return payrollAdditionalInfoMap;
    }

    private static DomainEntitySet<PayrollRun> getPayrollRuns(Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments) {
        HqlBuilder hql = new HqlBuilder(true, "from com.intuit.sbd.payroll.psp.domain.PayrollRun pr " +
                                                "where pr.Company = :company " +
                                                "and pr.PaycheckDate between :checkDateStart and :checkDateEnd " +
                                                "and pr.PayrollRunStatus not in (:payrollRunExcludeStatusList) "+
                                                "and  not exists (select 'T' from com.intuit.sbd.payroll.psp.domain.Paycheck as pc where pc.PayrollRun.Id = pr.Id and pc.SourcePaycheckId like '-%')" );
        setPayrollParameters(hql, company, yearQuarterStartCalendar, yearQuarterEndCalendar, includeNotPostedPayments);
        List<PayrollRun> payrollRunList = hql.list();
        return new DomainEntitySet<PayrollRun>(new HashSet<PayrollRun>(payrollRunList));
    }

    private static class PayrollAdditionalInfo {
        public boolean reconcilingAdjustment;
        public boolean recorded;

        private PayrollAdditionalInfo(boolean pReconcilingAdjustment, boolean pRecorded) {
            reconcilingAdjustment = pReconcilingAdjustment;
            recorded = pRecorded;
        }
    }

    private static void setPayrollParameters(HqlBuilder builder, Company company, SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments) {
        builder.setParameter("company", company);
        builder.setParameter("checkDateStart", TaxPeriod.getQuarterStart(yearQuarterStartCalendar));
        builder.setParameter("checkDateEnd", yearQuarterEndCalendar);
        if (includeNotPostedPayments) {
            builder.setParameterList("payrollRunExcludeStatusList", PayrollStatus.Canceled, PayrollStatus.Superseded);
        } else {
            builder.setParameterList("payrollRunExcludeStatusList", PayrollStatus.Canceled, PayrollStatus.Pending, PayrollStatus.Superseded);
        }
    }

    static void initializeTransactionsMap(Map<String, SAPLawTransactions> sapLawTransactionsMap,
                                           DomainEntitySet<CompanyAgency> companyAgencies,
                                           CompanyAgency specifiedCompanyAgency,
                                           PaymentTemplate specifiedPaymentTemplate,
                                           String specifiedLawId,
                                           SpcfCalendar yearQuarterStartCalendar) {
        for (CompanyAgency ca : companyAgencies) {
            if (specifiedCompanyAgency != null && !specifiedCompanyAgency.equals(ca)) {
                continue;
            }
            for (CompanyAgencyPaymentTemplate capt : ca.getCompanyAgencyPaymentTemplateCollection()) {
                PaymentTemplate pt = capt.getPaymentTemplate();
                if (specifiedPaymentTemplate != null && !specifiedPaymentTemplate.equals(pt)) {
                    continue;
                }
                if (!pt.isSupportedAsOfDate(yearQuarterStartCalendar)) {
                    continue;
                }
                for (Law law : pt.getLawCollection()) {
                    if (law.shouldExcludeFromUI()) {
                        continue;
                    }
                    String currentLawId = law.getLawId();
                    // if a law id is supplied only find transactions for that id
                    if (StringUtils.isNotEmpty(specifiedLawId)) {
                        if (!currentLawId.equals(specifiedLawId)) {
                            continue;
                        }
                    }

                    SAPLawTransactions sapLawTransaction = new SAPLawTransactions();
                    sapLawTransaction.setAgency(TaxTranslator.getSAPAgencyFromDomainEntity(ca.getAgency(), null));
                    sapLawTransaction.setCurrentTaxesSum(0.00);
                    sapLawTransaction.setLaw(TaxTranslator.getLawItemsFromDomainEntity(law));
                    sapLawTransaction.setTaxTransactions(new ArrayList<SAPTaxTransaction>());
                    sapLawTransactionsMap.put(currentLawId, sapLawTransaction);
                }
            }
        }
    }

    static SAPLawTransactions initializeSummary(Map<String, SAPLawTransactions> sapLawTransactionsMap, CompanyAgency specifiedCompanyAgency, PaymentTemplate specifiedPaymentTemplate) {
        //init summary if there is more than 1 law and the transactions don't span agencies or templates
        if (sapLawTransactionsMap.size() > 1 && specifiedPaymentTemplate != null) {
            SAPLawTransactions summaryTransactions = new SAPLawTransactions();
            if (specifiedCompanyAgency != null) {
                summaryTransactions.setAgency(TaxTranslator.getSAPAgencyFromDomainEntity(specifiedCompanyAgency.getAgency(), null));
            }
            summaryTransactions.setCurrentTaxesSum(0.00);
            SAPLawItem summaryLaw = new SAPLawItem();
            // set the display name
            summaryLaw.setName(specifiedPaymentTemplate.getPaymentTemplateAbbrev() + " " + "Summary");
            summaryTransactions.setLaw(summaryLaw);
            summaryTransactions.setTaxTransactions(new ArrayList<SAPTaxTransaction>());
            return summaryTransactions;
        }
        else {
            return null;
        }
    }

    private static void addTaxTransactionsFromPayrolls(Map<String, SAPLawTransactions> sapLawTransactionsMap, SAPLawTransactions summaryTransactions, DomainEntitySet<PayrollRun> payrollRuns,
                                                String paymentTemplateCd, Map<SpcfUniqueId, Map<String, LiabilitiesWages>> payrollLiabilitiesWages, Map<SpcfUniqueId, List<CompanyAdjustmentSubmission>> voids, Map<SpcfUniqueId, Map<String, LiabilitiesWages>> voidLiabilitiesWages, Map<SpcfUniqueId, PayrollAdditionalInfo> additionalPayrollInfo,  boolean includePayrolls, boolean includeAdjustments) {


        for (PayrollRun payrollRun : payrollRuns) {
            PayrollAdditionalInfo payrollAdditionalInfo = additionalPayrollInfo.get(payrollRun.getId());
            Map<String, LiabilitiesWages> lawAmounts = payrollLiabilitiesWages.get(payrollRun.getId());

            //exclude payrolls that have no txns; include payrolls that have transactions that net 0
            if ( lawAmounts != null &&
                    ((includeAdjustments && payrollAdditionalInfo.reconcilingAdjustment) || (includePayrolls && ! payrollAdditionalInfo.reconcilingAdjustment))) {

                SpcfDecimal currentTaxesSumAcrossLaws = SpcfMoney.ZERO;
                for (Map.Entry<String, LiabilitiesWages> lawAmountsEntry : lawAmounts.entrySet()) {
                    currentTaxesSumAcrossLaws = SpcfUtils.add(currentTaxesSumAcrossLaws, lawAmountsEntry.getValue().liabilities);
                    SAPTaxTransaction sapTaxTransaction = createTaxTransaction(false,
                                                                               payrollRun,
                                                                               null,
                                                                               null,
                                                                               paymentTemplateCd,
                                                                               lawAmountsEntry.getKey(),
                                                                               payrollRun.getPaycheckDate(),
                                                                               payrollRun.getCreatedDate(),
                                                                               TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                               TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                               payrollAdditionalInfo.reconcilingAdjustment ? LS_ADJUSTMENT : LS_PAYROLL,
                                                                               null,
                                                                               payrollAdditionalInfo.recorded ? "Recorded" : payrollRun.getPayrollRunStatus().toString(),
                                                                               lawAmountsEntry.getValue().liabilities,
                                                                               lawAmountsEntry.getValue().wages,
                                                                               payrollAdditionalInfo.reconcilingAdjustment);
                    sapLawTransactionsMap.get(lawAmountsEntry.getKey()).getTaxTransactions().add(sapTaxTransaction);
                }

                // add payroll transaction to the summary
                if (summaryTransactions != null) {
                    SAPTaxTransaction sapTaxTransaction = createTaxTransaction(true,
                                                                               payrollRun,
                                                                               null,
                                                                               null,
                                                                               paymentTemplateCd,
                                                                               null,
                                                                               payrollRun.getPaycheckDate(),
                                                                               payrollRun.getCreatedDate(),
                                                                               TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                               TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                               payrollAdditionalInfo.reconcilingAdjustment ? LS_ADJUSTMENT : LS_PAYROLL,
                                                                               null,
                                                                               payrollAdditionalInfo.recorded ? "Recorded" : payrollRun.getPayrollRunStatus().toString(),
                                                                               currentTaxesSumAcrossLaws,
                                                                               SpcfMoney.ZERO,
                                                                               payrollAdditionalInfo.reconcilingAdjustment);

                    summaryTransactions.getTaxTransactions().add(sapTaxTransaction);

                }
            }

            List<CompanyAdjustmentSubmission> payrollVoids = voids.get(payrollRun.getId());

            if (payrollVoids != null && includePayrolls) {
                Collections.sort(payrollVoids, new Comparator<CompanyAdjustmentSubmission>() {
                    public int compare(CompanyAdjustmentSubmission o1, CompanyAdjustmentSubmission o2) {
                        return o1.getSubmissionDate().compareTo(o2.getSubmissionDate());
                    }
                });

                for (CompanyAdjustmentSubmission cas : payrollVoids) {
                    Map<String, LiabilitiesWages> voidLawAmounts = voidLiabilitiesWages.get(cas.getId());

                    if (voidLawAmounts == null) {
                        continue; //we can have voids that don't actually have any liabilities.  In that case, ignore them.
                    }

                    SpcfDecimal currentTaxesSumAcrossLaws = SpcfMoney.ZERO;

                    for (Map.Entry<String, LiabilitiesWages> lawAmountsEntry : voidLawAmounts.entrySet()) {
                        currentTaxesSumAcrossLaws = SpcfUtils.add(currentTaxesSumAcrossLaws, lawAmountsEntry.getValue().liabilities);
                        SAPTaxTransaction sapTaxTransaction = createTaxTransaction(false,
                                                                                   payrollRun,
                                                                                   cas.getId().toString(),
                                                                                   null,
                                                                                   paymentTemplateCd,
                                                                                   lawAmountsEntry.getKey(),
                                                                                   payrollRun.getPaycheckDate(),
                                                                                   cas.getSubmissionDate(),
                                                                                   TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                                   TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                                   LS_PAYROLL,
                                                                                   null,
                                                                                   null,
                                                                                   lawAmountsEntry.getValue().liabilities.negate(),
                                                                                   lawAmountsEntry.getValue().wages.negate(),
                                                                                   false);
                        sapLawTransactionsMap.get(lawAmountsEntry.getKey()).getTaxTransactions().add(sapTaxTransaction);
                    }

                    // add payroll transaction to the summary
                    if (summaryTransactions != null) {
                        SAPTaxTransaction sapTaxTransaction = createTaxTransaction(true,
                                                                                   payrollRun,
                                                                                   cas.getId().toString(),
                                                                                   null,
                                                                                   paymentTemplateCd,
                                                                                   null,
                                                                                   payrollRun.getPaycheckDate(),
                                                                                   cas.getSubmissionDate(),
                                                                                   TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                                   TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                                   LS_PAYROLL,
                                                                                   null,
                                                                                   null,
                                                                                   currentTaxesSumAcrossLaws.negate(),
                                                                                   SpcfMoney.ZERO,
                                                                                   false);

                        summaryTransactions.getTaxTransactions().add(sapTaxTransaction);

                    }
                }
            }
        }

    }

    private static void addTaxTransactionsFromTORPayrolls(Map<String, SAPLawTransactions> sapLawTransactionsMap, SAPLawTransactions summaryTransaction,  DomainEntitySet<PayrollRun> payrollRuns,
                                                   String paymentTemplateCd) {
        for (PayrollRun payrollRun : payrollRuns) {
            SpcfDecimal currentTorSumAcrossLaws = SpcfMoney.ZERO;
            for (FinancialTransaction torTransaction : payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyRefundTOR)) {
                if (sapLawTransactionsMap.containsKey(torTransaction.getLaw().getLawId())) {
                    currentTorSumAcrossLaws = SpcfUtils.add(currentTorSumAcrossLaws, torTransaction.getFinancialTransactionAmount());

                    SAPTaxTransaction sapTaxTransaction = createTaxTransaction(false,
                                                                               payrollRun,
                                                                               null,
                                                                               null,
                                                                               paymentTemplateCd,
                                                                               torTransaction.getLaw().getLawId(),
                                                                               payrollRun.getPaycheckDate(),
                                                                               payrollRun.getCreatedDate(),
                                                                               TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                               TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                               LS_TOR,
                                                                               null,
                                                                               payrollRun.getPayrollRunStatus().toString(),
                                                                               torTransaction.getFinancialTransactionAmount(),
                                                                               SpcfMoney.ZERO,
                                                                               false);

                    sapLawTransactionsMap.get(torTransaction.getLaw().getLawId()).getTaxTransactions().add(sapTaxTransaction);

                    if (torTransaction.isVoided()) {
                        SAPTaxTransaction sapVoidTransaction = createTaxTransaction(false,
                                                                                   payrollRun,
                                                                                   null,
                                                                                   null,
                                                                                   paymentTemplateCd,
                                                                                   torTransaction.getLaw().getLawId(),
                                                                                   payrollRun.getPaycheckDate(),
                                                                                   torTransaction.getCurrentFinancialTransactionState().getCreatedDate(),
                                                                                   TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                                   TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                                   LS_TOR,
                                                                                   null,
                                                                                   null,
                                                                                   torTransaction.getFinancialTransactionAmount().negate(),
                                                                                   SpcfMoney.ZERO,
                                                                                   false);

                        sapLawTransactionsMap.get(torTransaction.getLaw().getLawId()).getTaxTransactions().add(sapVoidTransaction);

                        if (summaryTransaction != null) {
                            SAPTaxTransaction sapVoidSummaryTransaction = createTaxTransaction(true,
                                                                                               payrollRun,
                                                                                               null,
                                                                                               null,
                                                                                               paymentTemplateCd,
                                                                                               null,
                                                                                               payrollRun.getPaycheckDate(),
                                                                                               torTransaction.getCurrentFinancialTransactionState().getCreatedDate(),
                                                                                               TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                                               TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                                               LS_TOR,
                                                                                               null,
                                                                                               null,
                                                                                               torTransaction.getFinancialTransactionAmount().negate(),
                                                                                               SpcfMoney.ZERO,
                                                                                               false);

                            summaryTransaction.getTaxTransactions().add(sapVoidSummaryTransaction);
                        }
                    }
                }
            }

            if (summaryTransaction != null && !currentTorSumAcrossLaws.isZero()) {
                SAPTaxTransaction sapTaxTransaction = createTaxTransaction(true,
                                                                           payrollRun,
                                                                           null,
                                                                           null,
                                                                           paymentTemplateCd,
                                                                           null,
                                                                           payrollRun.getPaycheckDate(),
                                                                           payrollRun.getCreatedDate(),
                                                                           TaxPeriod.getQuarterNumber(payrollRun.getPaycheckDate()),
                                                                           TaxPeriod.getYearNumber(payrollRun.getPaycheckDate()),
                                                                           LS_TOR,
                                                                           null,
                                                                           payrollRun.getPayrollRunStatus().toString(),
                                                                           currentTorSumAcrossLaws,
                                                                           SpcfMoney.ZERO,
                                                                           false);

                summaryTransaction.getTaxTransactions().add(sapTaxTransaction);
            }
        }
    }

    private static <T extends DomainEntity> boolean shouldExcludeBecauseNotProcessedOnPSP(T domainEntity, PaymentTemplate paymentTemplate, MoneyMovementTransaction mmt) {
        return shouldExcludeBecauseNotProcessedOnPSP(domainEntity, paymentTemplate, mmt.getPaymentPeriodEnd());
    }

    private static <T extends DomainEntity> boolean shouldExcludeBecauseNotProcessedOnPSP(T domainEntity, PaymentTemplate paymentTemplate, SpcfCalendar effectiveDate) {
        SpcfCalendar backdateProcessingBegan = SystemParameter.findCalendarValue(SystemParameter.Code.PSP_BACKDATE_PROCESSING_BEGIN);
        return paymentTemplate.getProcessingStartDate() != null && effectiveDate.before(paymentTemplate.getProcessingStartDate()) && domainEntity.getCreatedDate().before(backdateProcessingBegan);
    }

    private static SAPTaxTransaction createTaxTransaction(boolean isSummary, PayrollRun payrollRun, String voidId, MoneyMovementTransaction moneyMovementTxn, String paymentTemplateCd, String lawId, SpcfCalendar checkPaymentDate, SpcfCalendar submissionDate, int quarter, int year, String txnDescription,
                                                   String paymentMethod, String paymentStatus, SpcfDecimal taxes, SpcfDecimal wages, boolean isReconcilingAdjustment) {
        SAPTaxTransaction sapTaxTransaction = new SAPTaxTransaction();
        sapTaxTransaction.setIsSummary(isSummary);
        sapTaxTransaction.setVoidId(voidId);
        sapTaxTransaction.setTemplateCd(paymentTemplateCd);
        sapTaxTransaction.setLawId(lawId);
        sapTaxTransaction.setCheckPaymentDate(SAPTranslator.getDateFromSpcfCalendar(checkPaymentDate));
        sapTaxTransaction.setSubmissionDate(SAPTranslator.getDateFromSpcfCalendar(submissionDate));
        sapTaxTransaction.setQuarter(quarter);
        sapTaxTransaction.setYear(year);
        sapTaxTransaction.setTxnDescription(txnDescription);
        sapTaxTransaction.setPaymentMethod(paymentMethod);
        sapTaxTransaction.setPaymentStatus(paymentStatus);

        if (payrollRun != null) {
            sapTaxTransaction.setPayrollRunId(payrollRun.getId().toString());
        }
        if (moneyMovementTxn != null) {
            sapTaxTransaction.setMoneyMovementTransactionId(moneyMovementTxn.getId().toString());
            sapTaxTransaction.setPayment(TaxTranslator.getPayment(moneyMovementTxn, paymentStatus, null));
        }

        sapTaxTransaction.setCurrentTaxes(SAPTranslator.getDoubleFromSpcfMoneyNullZero(taxes));
        sapTaxTransaction.setCurrentWages(SAPTranslator.getDoubleFromSpcfMoneyNullZero(wages));
        sapTaxTransaction.setIsReconcilingAdjustment(isReconcilingAdjustment);

        if (payrollRun != null && payrollRun.getPayrollRunType().equals(PayrollType.Adjustment)) {
            CompanyEventDetail memoDetail = payrollRun.getManualAdjustmentNote();
            if (memoDetail != null) {
                sapTaxTransaction.setManualLedgerCreator(SAPTranslator.getUserNameFromUserID(memoDetail.getCompanyEvent().getCreatorId()));
                sapTaxTransaction.setManualLedgerMemo(memoDetail.getValue());
            }
        }
        else if (moneyMovementTxn != null && moneyMovementTxn.getMoneyMovementPaymentMethod() == PaymentMethod.HPDE) {
            CompanyEventDetail memoDetail;
            if(txnDescription.equals("Refund")){
                memoDetail = moneyMovementTxn.getFullRefundNote();
            }else{
                memoDetail = moneyMovementTxn.getManualAdjustmentNote();
            }

            if (memoDetail != null) {
                sapTaxTransaction.setManualLedgerCreator(SAPTranslator.getUserNameFromUserID(memoDetail.getCompanyEvent().getCreatorId()));
                sapTaxTransaction.setManualLedgerMemo(memoDetail.getValue());
            }
        }

        return sapTaxTransaction;
    }

    private static void createPaymentSAPTransactions(Map<String, SAPLawTransactions> sapLawTransactionsMap, SAPLawTransactions summaryTransactions, Company company, String paymentMethod, String specifiedPaymentTemplateCd,
                                              SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar, boolean includeNotPostedPayments, boolean includeRefund) {

        DomainEntitySet<MoneyMovementTransaction> payments = getPayments(company, specifiedPaymentTemplateCd, yearQuarterStartCalendar, yearQuarterEndCalendar, paymentMethod, includeNotPostedPayments);
        // filter out post balf payments
        payments = payments.find(MoneyMovementTransaction.MoneyMovementPaymentMethod().isNull().Or(MoneyMovementTransaction.MoneyMovementPaymentMethod().notIn(PaymentMethod.PostBalfHPDE, PaymentMethod.PostBalfHPDERefund)));

        for (MoneyMovementTransaction payment : payments) {
            if (shouldExcludeBecauseNotProcessedOnPSP(payment, payment.getPaymentTemplate(), payment)) {
                continue;
            }
            boolean refund = false;
            // init law -> payment amount map
            Map<String, SpcfDecimal> lawPaymentsMap = new HashMap<String, SpcfDecimal>();
            for (String lawId : sapLawTransactionsMap.keySet()) {
                lawPaymentsMap.put(lawId, SpcfMoney.ZERO);
            }
            for (FinancialTransaction financialTransaction : payment.getFinancialTransactionCollection()) {
                if(FinancialTransaction.isRefundType(financialTransaction) && includeRefund){
                    refund = true;
                }
                if (financialTransaction.contributesToPayment()) {
                    String currentLawId = financialTransaction.getLaw().getLawId();
                    if (lawPaymentsMap.containsKey(currentLawId)) {
                        TransactionTypeCode transactionType = financialTransaction.getTransactionType().getTransactionTypeCd();
                        if (TransactionType.addsToPayment(transactionType)) {
                            lawPaymentsMap.put(currentLawId, SpcfUtils.add(lawPaymentsMap.get(currentLawId), financialTransaction.getFinancialTransactionAmount()));
                        }
                        else if (TransactionType.subtractsFromPayment(transactionType)) {
                            lawPaymentsMap.put(currentLawId, SpcfUtils.subtract(lawPaymentsMap.get(currentLawId), financialTransaction.getFinancialTransactionAmount()));
                        }
                    }
                }
            }

            String paymentMethodString;
            if (payment.getMoneyMovementPaymentMethod() == PaymentMethod.HPDE || payment.getMoneyMovementPaymentMethod() == PaymentMethod.HPDERefund) {
                paymentMethodString = "Recorded";
            }
            else {
                paymentMethodString = payment.getMoneyMovementPaymentMethodString();
            }
            String paymentStatus;
            if (payment.getTaxPaymentStatus() != TaxPaymentStatus.None) {
                switch (payment.getTaxPaymentStatus()) {
                    case OnHold:
                        paymentStatus = "On Hold";
                        break;
                    case ReadyToSend:
                        paymentStatus = "Pending";
                        break;
                    case ATFFinalized:
                        paymentStatus = "Finalized";
                        break;
                    case SentToAgency:
                        paymentStatus = "Executed";
                        break;
                    case AcknowledgedByAgency:
                        //Will be Complete for normal credits and Executed for Direct credits until ATP
                        paymentStatus = payment.getFirstFinancialTransaction().getCurrentTransactionState().getTransactionStateCd().toString();
                        break;
                    default:
                        paymentStatus = ""; //no other statuses should ever be returned
                }
            }
            else if (payment.getMoneyMovementPaymentMethod() == PaymentMethod.HPDE || payment.getMoneyMovementPaymentMethod() == PaymentMethod.HPDERefund) {
                paymentStatus = "Recorded";
            }
            else {
                paymentStatus = payment.getManualPaymentStatus().toString();
            }


            for (String lawId : sapLawTransactionsMap.keySet()) {
                if (!lawPaymentsMap.get(lawId).isZero()) {
                    SAPTaxTransaction sapTaxTransaction = createTaxTransaction(false,
                                                                               null,
                                                                               null,
                                                                               payment,
                                                                               specifiedPaymentTemplateCd,
                                                                               lawId,
                                                                               payment.getSettlementDate(),
                                                                               null,
                                                                               TaxPeriod.getQuarterNumber(payment.getPaymentPeriodEnd()),
                                                                               TaxPeriod.getYearNumber(payment.getPaymentPeriodEnd()),
                                                                               refund ? LS_REFUND : LS_PAYMENT,
                                                                               refund ? RefundPaymentMethod : paymentMethodString,
                                                                               refund ? RefundPaymentStatus : paymentStatus,
                                                                               lawPaymentsMap.get(lawId).negate(),
                                                                               SpcfMoney.ZERO,
                                                                               false);

                    sapLawTransactionsMap.get(lawId).getTaxTransactions().add(sapTaxTransaction);
                }
            }
            // summary
            if (summaryTransactions != null && !payment.getMoneyMovementTransactionAmount().isZero()) {
                SAPTaxTransaction sapTaxTransaction = createTaxTransaction(true,
                                                                           null,
                                                                           null,
                                                                           payment,
                                                                           specifiedPaymentTemplateCd,
                                                                           null,
                                                                           payment.getSettlementDate(),
                                                                           null,
                                                                           TaxPeriod.getQuarterNumber(payment.getPaymentPeriodEnd()),
                                                                           TaxPeriod.getYearNumber(payment.getPaymentPeriodEnd()),
                                                                           refund ? LS_REFUND : LS_PAYMENT,
                                                                           refund ? RefundPaymentMethod : paymentMethodString,
                                                                           refund ? RefundPaymentStatus : paymentStatus,
                                                                           payment.getMoneyMovementPaymentMethod() == PaymentMethod.HPDERefund ? payment.getMoneyMovementTransactionAmount() : payment.getMoneyMovementTransactionAmount().negate(),
                                                                           SpcfMoney.ZERO,
                                                                           false);

                summaryTransactions.getTaxTransactions().add(sapTaxTransaction);
            }
        }
    }


    private static void sumTransactionsMapAndRemoveUnrequestedTransactions(Map<String, SAPLawTransactions> sapLawTransactionsMap, SAPLawTransactions summaryTransactions,
                                                                    SpcfCalendar startDate, SpcfCalendar yearQuarterEndCalendar) {
        for (SAPLawTransactions lawTransactions : sapLawTransactionsMap.values()) {
            sumTransactions(lawTransactions, startDate, yearQuarterEndCalendar);
        }
        if (summaryTransactions != null) {
            sumTransactions(summaryTransactions, startDate, yearQuarterEndCalendar);
        }
    }

    private static void sumTransactions(SAPLawTransactions lawTransactions, SpcfCalendar startDate, SpcfCalendar yearQuarterEndCalendar) {
        //sort by "posting" date
        Collections.sort(lawTransactions.getTaxTransactions(), new Comparator<SAPTaxTransaction>() {
            public int compare(SAPTaxTransaction o1, SAPTaxTransaction o2) {
                if (o1.getQuarter() != o2.getQuarter()) {
                    return o1.getQuarter() - o2.getQuarter();
                }

                if(o1.getCheckPaymentDate() != null && o2.getCheckPaymentDate() != null) {
                    return o1.getCheckPaymentDate().compareTo(o2.getCheckPaymentDate());
                } else {
                    return 0;
                }
            }
        });


        SpcfMoney qtdTaxes = null;
        SpcfMoney qtdWages = null;
        SpcfMoney ytdTaxes = null;
        SpcfMoney ytdWages = null;
        SpcfMoney totalBalance = SpcfMoney.ZERO; //differs from YTD taxes only in that it includes all lines that show on the screen (including payments but not including previous quarters)
        SAPTaxTransaction previousTaxTransaction = null;
        for (Iterator<SAPTaxTransaction> iterator = lawTransactions.getTaxTransactions().iterator(); iterator.hasNext();) {
            SAPTaxTransaction taxTransaction = iterator.next();

            if (previousTaxTransaction == null || previousTaxTransaction.getQuarter() != taxTransaction.getQuarter()) {
                qtdTaxes = SpcfMoney.ZERO;
                qtdWages = SpcfMoney.ZERO;
            }
            if (previousTaxTransaction == null || previousTaxTransaction.getYear() != taxTransaction.getYear()) {
                ytdTaxes = SpcfMoney.ZERO;
                ytdWages = SpcfMoney.ZERO;
            }

            //only set QTD/YTD for payrolls/adjustments
            if (!taxTransaction.getTxnDescription().equals(LS_PAYMENT) && !taxTransaction.getTxnDescription().equals(LS_TOR) && !taxTransaction.getIsReconcilingAdjustment() && !taxTransaction.getTxnDescription().equals(LS_REFUND))  {
                qtdTaxes = (SpcfMoney) SpcfUtils.add(qtdTaxes, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(taxTransaction.getCurrentTaxes()));
                qtdWages = (SpcfMoney) SpcfUtils.add(qtdWages, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(taxTransaction.getCurrentWages()));
                ytdTaxes = (SpcfMoney) SpcfUtils.add(ytdTaxes, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(taxTransaction.getCurrentTaxes()));
                ytdWages = (SpcfMoney) SpcfUtils.add(ytdWages, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(taxTransaction.getCurrentWages()));
            }

            taxTransaction.setQTDTaxes(SAPTranslator.getDoubleFromSpcfMoneyNullZero(qtdTaxes));
            taxTransaction.setQTDWages(SAPTranslator.getDoubleFromSpcfMoneyNullZero(qtdWages));
            taxTransaction.setYTDTaxes(SAPTranslator.getDoubleFromSpcfMoneyNullZero(ytdTaxes));
            taxTransaction.setYTDWages(SAPTranslator.getDoubleFromSpcfMoneyNullZero(ytdWages));

            previousTaxTransaction = taxTransaction;

            //remove transactions if not requested by user
            boolean includeLine = true;
            if (taxTransaction.getTxnDescription().equals(LS_PAYMENT) || taxTransaction.getTxnDescription().equals(LS_REFUND)) {
                if (startDate != null && yearQuarterEndCalendar != null) {
                    if (TaxPeriod.getQuarterNumber(startDate) > taxTransaction.getQuarter()) {
                        includeLine = false;
                    }
                }
            }
            else {
                if (startDate != null && yearQuarterEndCalendar != null) {
                    if (startDate.after(SAPTranslator.getSpcfCalendarFromDate(taxTransaction.getCheckPaymentDate()))) {
                        includeLine = false;
                    }
                }
            }

            if (includeLine) {
                totalBalance = (SpcfMoney) SpcfUtils.add(totalBalance, SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(taxTransaction.getCurrentTaxes()));
            }
            else {
                iterator.remove();
            }

        }


        lawTransactions.setCurrentTaxesSum(SAPTranslator.getDoubleFromSpcfMoneyNullZero(totalBalance));


        //set whether items are last in a quarter
        SAPTaxTransaction lastTransaction = null;
        for (int i = lawTransactions.getTaxTransactions().size() - 1; i >= 0; i--) {
            SAPTaxTransaction taxTxn = lawTransactions.getTaxTransactions().get(i);
            if (lastTransaction == null || lastTransaction.getQuarter() != taxTxn.getQuarter()) {
                taxTxn.setIsLastLineInQuarter(true);
            }
            lastTransaction = taxTxn;
        }

    }

    private static DomainEntitySet<MoneyMovementTransaction> getPayments(Company company, String specifiedPaymentTemplateCd,
                                                                         SpcfCalendar yearQuarterStartCalendar, SpcfCalendar yearQuarterEndCalendar,
                                                                        String paymentMethod, boolean includeNotPostedPayments) {
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.Company().equalTo(company);

        SpcfCalendar firstDayOfQuarterMinus45Days = yearQuarterStartCalendar.copy();
        firstDayOfQuarterMinus45Days.addDays(-45);

        paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.PaymentPeriodEnd().between(yearQuarterStartCalendar, yearQuarterEndCalendar))
                                               .And(MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(firstDayOfQuarterMinus45Days));

        if (specifiedPaymentTemplateCd != null) {
            //if this is not specified, will ultimately filter on laws--might get a few extras now, but ought not be a big deal
            paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(specifiedPaymentTemplateCd));
        } else {
            paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().isNotNull());
        }

        if (paymentMethod != null) {
            if (paymentMethod.equals("Recorded")) {
                paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.HPDE, PaymentMethod.HPDERefund));
            } else {
                paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.valueOf(paymentMethod)));
            }
        }

        List<TaxPaymentStatus> allowedTaxPaymentStatuses = new ArrayList<TaxPaymentStatus>();
        List<PaymentStatus> allowedPaymentStatuses = new ArrayList<PaymentStatus>();

        allowedTaxPaymentStatuses.add(TaxPaymentStatus.SentToAgency);
        allowedTaxPaymentStatuses.add(TaxPaymentStatus.AcknowledgedByAgency);
        allowedTaxPaymentStatuses.add(TaxPaymentStatus.ReturnedTaxPaid);
        allowedTaxPaymentStatuses.add(TaxPaymentStatus.None); //NONE = HPDE

        allowedPaymentStatuses.add(PaymentStatus.Executed);

        if (includeNotPostedPayments) {
            //include anything that isn't posted yet
            allowedTaxPaymentStatuses.add(TaxPaymentStatus.OnHold);
            allowedTaxPaymentStatuses.add(TaxPaymentStatus.ReadyToSend);
            allowedTaxPaymentStatuses.add(TaxPaymentStatus.ATFFinalized);

            allowedPaymentStatuses.add(PaymentStatus.OnHold);
            allowedPaymentStatuses.add(PaymentStatus.Created);
            allowedPaymentStatuses.add(PaymentStatus.InProcess);
        }

        paymentWhereClause = paymentWhereClause.And(MoneyMovementTransaction.TaxPaymentStatus().in(allowedTaxPaymentStatuses))
                                               .And(MoneyMovementTransaction.Status().in(allowedPaymentStatuses));

        return Application.find(MoneyMovementTransaction.class,
                                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                     .OrderBy(MoneyMovementTransaction.PaymentPeriodEnd())
                                                                     .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet()));
    }
}
