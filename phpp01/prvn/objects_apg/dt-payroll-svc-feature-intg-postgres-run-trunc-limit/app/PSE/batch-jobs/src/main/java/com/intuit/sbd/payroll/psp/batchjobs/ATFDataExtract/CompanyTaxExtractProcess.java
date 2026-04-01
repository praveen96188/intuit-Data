package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.LawStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.PrintWriter;
import java.util.List;

/**
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:56:31 PM
 */
public class CompanyTaxExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "CO_TAX";

    public CompanyTaxExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode = COMPARE_PROCESS_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyTaxInfo;
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
        String sourceCompanyId;
        String sourceTaxCd;
        String agencyId;
        PayrollItemStatus filingStatus;
        LawStatus exemptionStatus;
        String filingStatusValue;
        String exemptionStatusValue;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
        }

        //Get Company-level law information
        List<Object[]> companyTaxInfoResults = findCompanyTaxInfoResults(lastRunDate);
        for (Object[] companyTaxInfoResult : companyTaxInfoResults) {
            sourceCompanyId = (String) companyTaxInfoResult[0];
            sourceTaxCd = (String) companyTaxInfoResult[1];
            agencyId = EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName,(String) companyTaxInfoResult[2]);
            
            filingStatus = (PayrollItemStatus) companyTaxInfoResult[3];
            exemptionStatus = (LawStatus) companyTaxInfoResult[4];

            if (filingStatus == null || filingStatus == PayrollItemStatus.Active) {
                filingStatusValue = "N";
            } else {
                filingStatusValue = "Y";
            }

            if (exemptionStatus == null || exemptionStatus == LawStatus.NonExempt) {
                exemptionStatusValue = "N";
            } else {
                exemptionStatusValue = "Y";
            }

            writeRecordsToFile(sourceCompanyId, sourceTaxCd, agencyId, filingStatusValue, exemptionStatusValue, pPW);
        }

        //Get Company-level additional filing information
        List<Object[]> companyTaxAdditionalFilingInfoResults = findCompanyTaxAdditionalFilingInfoResults(lastRunDate);
        // All records are added with below constant values for 3 fields
        agencyId = "";
        filingStatusValue = "N";
        exemptionStatusValue = "N";

        for (Object[] companyTaxAdditionalFilingInfoResult : companyTaxAdditionalFilingInfoResults) {
            sourceCompanyId = (String) companyTaxAdditionalFilingInfoResult[0];
            sourceTaxCd = (String) companyTaxAdditionalFilingInfoResult[1];

            writeRecordsToFile(sourceCompanyId, sourceTaxCd, agencyId, filingStatusValue, exemptionStatusValue, pPW);

        }
    }

    private void writeRecordsToFile(String pSourceCompanyId, String pSourceTaxCd, String pAgencyId, String pFilingsInactiveFlag, String pFilingsExemptFlag, PrintWriter pPrintWriter) throws Throwable {
        pPrintWriter.print(DOUBLE_QUOTE);
        //ATF Extract type Id
        pPrintWriter.print(ATF_EXTRACT_TYPE_ID + DELIMITER);
        //Source System Id
        writeFormatted(pPrintWriter, pSourceCompanyId);

        //Source Law code
        writeFormatted(pPrintWriter, pSourceTaxCd);

        //Agency tax payer Id
        writeFormatted(pPrintWriter, pAgencyId);

        //Filings Exempt Flag
        writeFormatted(pPrintWriter, pFilingsExemptFlag);

        //Filings Inactive Flag
        writeFormatted(pPrintWriter, pFilingsInactiveFlag, true);

        //Increment the record count for each record written in the extract file for the trailer record
        mRecordCount++;
    }

    /**
     * Returns the Company-level law information
     *
     * @param startDate StartDate
     * @return List
     */
    private static List findCompanyTaxInfoResults(SpcfCalendar startDate) {

        //Using HQL, since it is straight forward for selecting data from different tables with more conditions.
        StringBuilder builder = new StringBuilder();
        // Query for CompanyTaxInfo
        builder.append("select comp.SourceCompanyId, ssla.SourceLawCode, capt.AgencyTaxpayerIdEnc, complaw.FilingStatus, complaw.ExemptionStatus\n" +
                "              from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo,\n" +
                "              com.intuit.sbd.payroll.psp.domain.SourceSystemLawAssoc ssla\n" +
                "              join serviceInfo.Company comp\n" +
                "              join comp.CompanyAgencySet compagency\n" +
                "              join compagency.CompanyLawSet complaw\n" +
                "              join compagency.CompanyAgencyPaymentTemplateSet capt\n" +
                "              join complaw.QbdtPayrollItemInfoSet qbdtpiteminfo\n" +
                "   where\n" +
                "              ssla.Law = complaw.Law\n" +
                "              and ssla.SourceSystem.SourceSystemCd = comp.SourceSystemCd\n" +
                "              and capt.PaymentTemplate = complaw.Law.PaymentTemplate\n" +
                "              and qbdtpiteminfo.IsDeleted=false \n" +
                "              and (serviceInfo.ServiceStartDate is not null and serviceInfo.StatusCd in ('ActiveCurrent', 'Cancelled','Terminated'))" +
                "              and complaw.AdditionalCompanyLaw is null");


        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and (complaw.ModifiedDate >= :startDate or capt.ModifiedDate >= :startDate or serviceInfo.ModifiedDate >= :startDate) ");
        }

        appendDGCheckCondition(builder, "comp", Boolean.FALSE);

        // Sort by PSId to provide consistency for automated testing.
        builder.append(" order by comp.SourceCompanyId, ssla.SourceLawCode \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.list();
    }

    /**
     * Returns the Company-level tax law additional filing information
     *
     * @param startDate StartDate
     * @return List
     */
    private static List findCompanyTaxAdditionalFilingInfoResults(SpcfCalendar startDate) {

        StringBuilder builder = new StringBuilder();
        // Query for CompanyTaxAdditionalFilingInfo
        builder.append("select distinct comp.SourceCompanyId, afa.ATFLawId\n" +
                               "         from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo\n" +
                               "         join serviceInfo.Company comp\n" +
                               "         join comp.CompanyAgencySet compagency\n" +
                               "         join compagency.CompanyAgencyPaymentTemplateSet capt\n" +
                               "         join capt.CompanyFilingAmountSet cfa\n" +
                               "         join capt.PaymentTemplate.AdditionalFilingAmountSet afa \n" +
                               " where " +
                               "         cfa.InvalidDate is null and cfa.Name=afa.Name " +
                               "         and (serviceInfo.ServiceStartDate is not null and serviceInfo.StatusCd in ('ActiveCurrent', 'Cancelled','Terminated'))\n" );
        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and (cfa.ModifiedDate >= :startDate or serviceInfo.ModifiedDate >= :startDate)");
        }

        appendDGCheckCondition(builder, "comp", Boolean.FALSE);

        // Sort by PSId to provide consistency for automated testing.
        builder.append(" order by comp.SourceCompanyId, afa.ATFLawId \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.list();
    }
}
