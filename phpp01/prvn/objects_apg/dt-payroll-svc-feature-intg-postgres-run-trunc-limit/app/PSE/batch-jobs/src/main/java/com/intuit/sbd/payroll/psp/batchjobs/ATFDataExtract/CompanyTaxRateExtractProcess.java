package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:58:03 PM
 */
public class CompanyTaxRateExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "CO_TAX_RATE";

    public CompanyTaxRateExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode = COMPARE_PROCESS_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyTaxRateInfo;
    }

    public void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        super.execute();
        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    /**
     * This method writes the actual company info details.
     *
     * @param pPW PrintWriter
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {

        SpcfCalendar lastRunDate = null;
        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(true).toLocal();
        }

        //Get Company law tax rate information
        List<CompanyTaxRateInfo> companyTaxRateInfoResults = findCompanyTaxRateInfoResults(lastRunDate);

        for (int i = 0; i < companyTaxRateInfoResults.size(); i++) {
            CompanyTaxRateInfo companyTaxRateInfoResult = companyTaxRateInfoResults.get(i);

            if (companyTaxRateInfoResult.isEmpPaid && companyTaxRateInfoResult.lawRate != null) {
                companyTaxRateInfoResult.lawRate = companyTaxRateInfoResult.lawRate * -1;
            }

            SpcfCalendar expirationDate = null;
            if (companyTaxRateInfoResults.size() > i + 1) {
                CompanyTaxRateInfo nextCompanyTaxRateInfo = companyTaxRateInfoResults.get(i + 1);
                if (companyTaxRateInfoResult.isSameCompanyLawAs(nextCompanyTaxRateInfo)) {
                    expirationDate = nextCompanyTaxRateInfo.effectiveDate.copy();
                    expirationDate.addDays(-1);
                }

            }

            writeRecordsToFile(companyTaxRateInfoResult.sourceCompanyId, companyTaxRateInfoResult.sourceTaxCd, companyTaxRateInfoResult.lawRate, companyTaxRateInfoResult.effectiveDate, expirationDate, pPW);
        }

        //Get Company additional filing amounts information
        @SuppressWarnings("unchecked")
        List<Object[]> companyTaxRateAdditionalInfoResults = findCompanyTaxRateAdditionalInfoResults(lastRunDate);
        //Calculate this as - If multiple rates with different effective dates are found - like below
/*
        Rate       Effective Date                   Law                        Calculated expiration date to put in file
        2              1/1/2012                              195                         4/4/2012
        3              5/16/2012                            195                         NULL
        4              4/5/2012                              195                         5/15/2012
        8              1/1/2012                              61                          NULL
*/
        //Initializing to null to clear the values from above loop, as these values used to calculate expiration date
        String sourceCompanyId = null;
        String sourceTaxCd = null;
        Double lawRate;
        SpcfCalendar effectiveDate = null;
        SpcfCalendar expirationDate;

        for (Object[] companyTaxRateAdditionalInfoResult : companyTaxRateAdditionalInfoResults) {

            if(sourceTaxCd != null && effectiveDate != null && sourceCompanyId != null && sourceTaxCd.equals(companyTaxRateAdditionalInfoResult[1]) && sourceCompanyId.equals(companyTaxRateAdditionalInfoResult[0])) {
                //If multiple rates are present for the same law Id, calculate the expiration date based on next effective date
                expirationDate = effectiveDate.copy();
                expirationDate.addDays(-1);
            } else {
                // Default all records are added with empty expiration date for additional rates
                //clear expiration date from previous record
                expirationDate = null;
            }

            sourceCompanyId = (String) companyTaxRateAdditionalInfoResult[0];
            lawRate = (Double) companyTaxRateAdditionalInfoResult[2];
            sourceTaxCd = (String) companyTaxRateAdditionalInfoResult[1];
            effectiveDate = (SpcfCalendar) companyTaxRateAdditionalInfoResult[3];

            writeRecordsToFile(sourceCompanyId, sourceTaxCd, lawRate, effectiveDate, expirationDate, pPW);
        }
    }

    private void writeRecordsToFile(String pSourceCompanyId, String pSourceTaxCd, Double pRate, SpcfCalendar pEffectiveDate, SpcfCalendar pExpirationDate, PrintWriter pPrintWriter) throws Throwable {
        pPrintWriter.print(DOUBLE_QUOTE);
        //ATF Extract type Id
        pPrintWriter.print(ATF_EXTRACT_TYPE_ID + DELIMITER);
        //Source System Id
        writeFormatted(pPrintWriter, pSourceCompanyId);

        //Source Law code
        writeFormatted(pPrintWriter, pSourceTaxCd);

        //Law rate
        writeFormatted(pPrintWriter, pRate == null ? "" : new BigDecimal(pRate).setScale(6, RoundingMode.HALF_EVEN).toString());

        //Rate effective date
        writeFormatted(pPrintWriter, pEffectiveDate == null ? "" : pEffectiveDate.format("yyyyMMdd"));

        //Rate expiration date, send date only expired otherwise empty string
        String expirationDate = "";
        if (pExpirationDate != null) {
            expirationDate = pExpirationDate.format("yyyyMMdd");
        }
        writeFormatted(pPrintWriter, expirationDate, true);

        //Increment the record count for each record written in the extract file for the trailer record
        mRecordCount++;
    }

    /**
     * Returns the Company law tax rate information
     * @param startDate StartDate
     * @return  List
     */
    private static List<CompanyTaxRateInfo> findCompanyTaxRateInfoResults(SpcfCalendar startDate) {

        //Using HQL, since it is straight forward for selecting data from different tables with more conditions.
        StringBuilder builder = new StringBuilder();
        // Query for CompanyTaxRateInfo.
        builder.append("select new com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyTaxRateExtractProcess$CompanyTaxRateInfo(comp.SourceCompanyId, ssla.SourceLawCode, complawrate.Rate, complawrate.EffectiveDate, qbinfo.IsEmployeePaid)\n" +
                               "        from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo, \n" +
                               "        com.intuit.sbd.payroll.psp.domain.SourceSystemLawAssoc ssla\n" +
                               "        join serviceInfo.Company comp \n" +
                               "        join comp.CompanyAgencySet compagency\n" +
                               "        join compagency.CompanyLawSet complaw\n" +
                               "        join complaw.QbdtPayrollItemInfoSet qbinfo\n" +
                               "        join complaw.CompanyLawRateSet complawrate\n" +
                               " where\n" +
                               "        ssla.Law = complaw.Law\n" +
                               "        and ssla.SourceSystem.SourceSystemCd = comp.SourceSystemCd \n" +
                               "        and complawrate.InvalidDate is null \n" +
                               "        and complaw.AdditionalCompanyLaw is null \n" +
                               "        and qbinfo.IsDeleted=false \n" +
                               "        and complawrate.EffectiveDate < :tomorrowsDate \n");

        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append(" and complaw in (select clr.CompanyLaw from com.intuit.sbd.payroll.psp.domain.CompanyLawRate clr where clr.ModifiedDate >= :startDate) ");
        }

        appendDGCheckCondition(builder, "comp", Boolean.FALSE);

        // Sort by PSId to provide consistency for automated testing.
        builder.append(" order by comp.SourceCompanyId, ssla.SourceLawCode, complawrate.EffectiveDate \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        
        SpcfCalendar date = PSPDate.getPSPTime();
        CalendarUtils.clearTime(date);
        date.addDays(1);
        query.setParameter("tomorrowsDate", date);

        //noinspection unchecked
        return query.list();
    }

    public static class CompanyTaxRateInfo {
        public String sourceCompanyId;
        public String sourceTaxCd;
        public Double lawRate;
        public SpcfCalendar effectiveDate;
        public boolean isEmpPaid;

        public CompanyTaxRateInfo(String pSourceCompanyId, String pSourceTaxCd, Double pLawRate, SpcfCalendar pEffectiveDate, boolean pEmpPaid) {
            sourceCompanyId = pSourceCompanyId;
            sourceTaxCd = pSourceTaxCd;
            lawRate = pLawRate;
            effectiveDate = pEffectiveDate;
            isEmpPaid = pEmpPaid;
        }

        public boolean isSameCompanyLawAs(CompanyTaxRateInfo other) {
            return sourceCompanyId.equals(other.sourceCompanyId)
                    && sourceTaxCd.equals(other.sourceTaxCd);
        }
    }

    /**
     * Returns the Company law tax rate additional information
     * @param startDate StartDate
     * @return  List
     */
    private static List findCompanyTaxRateAdditionalInfoResults(SpcfCalendar startDate) {

        StringBuilder builder = new StringBuilder();
        // Query for CompanyTaxRateAdditionalInfo
        builder.append("select comp.SourceCompanyId, afa.ATFLawId, cfa.Amount, cfa.EffectiveDate\n" +
                               "        from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo\n" +
                               "        join serviceInfo.Company comp\n" +
                               "        join comp.CompanyAgencySet compagency\n" +
                               "        join compagency.CompanyAgencyPaymentTemplateSet capt\n" +
                               "        join capt.CompanyFilingAmountSet cfa\n" +
                               "        join capt.PaymentTemplate.AdditionalFilingAmountSet afa\n" +
                               " where " +
                               "        cfa.InvalidDate is null and cfa.Name=afa.Name \n" );
        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and capt in (select icfa.CompanyAgencyPaymentTemplate from com.intuit.sbd.payroll.psp.domain.CompanyFilingAmount icfa where icfa.ModifiedDate >= :startDate)");
        }

        appendDGCheckCondition(builder, "comp", Boolean.FALSE);

        // Sort by PSId to provide consistency for automated testing.
        builder.append(" order by comp.SourceCompanyId, afa.ATFLawId, cfa.EffectiveDate desc \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.list();
    }

}
