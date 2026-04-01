package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: tdry
 * Date: March 15, 2012
 * Time: 3:49:45 PM
 */
public class DepositFrequencyExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID ="CO_DEP_FREQ";

    // File format constants.
    private static final String RECORD_TYPE_DEP_FREQ = "CO_DEP_FREQ";
    private static final String RECORD_TYPE_TXACI = "CO_TXACI";
    private static final String RECORD_TYPE_ADDL_INFO = "CO_ADDL_INFO";
    private static final String DEP_FREQ_REGION_SUFFIX = "SIT";
    private static final String ADDL_INFO_CODE = "ADDL_TAXID";
    private static final String TXACI_CODE = "PAYROLLTYP";

    private static final String FILERTYPE_FT_941 = "IRS-941-FILING";
    private static final String FILERTYPE_FT_944 = "IRS-944-FILING";

    private static final String IA_BUSINESS_TAX_NAME = "BEN Number";

    public DepositFrequencyExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode =  REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyDepFreqInfo;
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
     * @param pPW Writer/File to write to.
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {

        SpcfCalendar lastRunDate = null;
        ScrollableResults queryResults;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
        }

        // Federal and State Deposit Frequency.
        queryResults = findDepositFrequencyResults(lastRunDate);
        writeResultsToFile(queryResults, RECORD_TYPE_DEP_FREQ, pPW);

        // Federal Filing Type.
        queryResults = findFederalFilingTypeResults(lastRunDate);
        writeResultsToFile(queryResults, RECORD_TYPE_TXACI, pPW);

        // Business Tax ID.
        queryResults = findBusinessTaxIdResults(lastRunDate);
        writeResultsToFile(queryResults, RECORD_TYPE_ADDL_INFO, pPW);
    }

    private void writeResultsToFile(ScrollableResults pQueryResults, String pRecordType, PrintWriter pPW) {
        String sourceCompanyId;
        String region;
        DepositFrequencyCode freq = null;
        String value;
        SpcfCalendar effectiveDate;

        try {
            while (pQueryResults.next()) {

                sourceCompanyId = (String) pQueryResults.get(0);

                //Exclude NOCALC from the file
                Object object = pQueryResults.get(2);
                if (DepositFrequencyCode.class.isInstance(object)) {
                    freq = (DepositFrequencyCode) object;
                    if (DepositFrequencyCode.NOCALC.equals(freq)) {
                        continue;
                    }
                }

                region = (String) pQueryResults.get(1);
                effectiveDate = (SpcfCalendar) pQueryResults.get(3);

                // Record type.
                pPW.print(DOUBLE_QUOTE + pRecordType + DELIMITER);

                // Source Company Id.
                pPW.print(sourceCompanyId + DELIMITER);

                // The remaining parts of the record are type specific.
                if (RECORD_TYPE_DEP_FREQ.equals(pRecordType)) {
                    // For federal, use "FIT".  Otherwise use the state code plus " SIT".
                    String formattedRegion = region.startsWith("IRS") ? "FIT" : region.substring(0, 2) + " " + DEP_FREQ_REGION_SUFFIX;

                    pPW.print(formattedRegion + DELIMITER);
                    pPW.print(getDepositFrequencyAbbreviation(region.substring(0, 2), freq) + DELIMITER);
                    pPW.print(effectiveDate.format("yyyyMMdd") + DELIMITER);
                    pPW.println(DOUBLE_QUOTE);
                } else if (RECORD_TYPE_TXACI.equals(pRecordType)) {
                    value = (String) pQueryResults.get(2);
                    pPW.print(region + DELIMITER);
                    pPW.print(TXACI_CODE + DELIMITER);
                    pPW.print(getFilerTypeAbbreviation(value) + DELIMITER);
                    pPW.println(effectiveDate.format("yyyyMMdd") + DOUBLE_QUOTE);
                } else if (RECORD_TYPE_ADDL_INFO.equals(pRecordType)) {
                    value = EncryptionUtils.deterministicDecrypt(CompanyPaymentTemplateAgencyId.AgencyTaxPayerIdKeyName, (String) pQueryResults.get(2));
                    pPW.print(region + DELIMITER);
                    pPW.print(ADDL_INFO_CODE + DELIMITER);
                    pPW.print(value + DELIMITER);
                    pPW.println(effectiveDate.format("yyyyMMdd") + DOUBLE_QUOTE);
                }

                mRecordCount++; // Increment for the record count in the trailer record

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pQueryResults.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pQueryResults.close();
        }
    }

    private static ScrollableResults findDepositFrequencyResults(SpcfCalendar startDate) {

        StringBuilder builder = new StringBuilder();
        // Query for deposit frequency records that do not have an invalid date.
        builder.append(" select distinct co.SourceCompanyId, pt.PaymentTemplateCd, ptfreq.PaymentFrequencyId, edfreq.EffectiveDate\n")
                .append(" from com.intuit.sbd.payroll.psp.domain.Company co,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.CompanyAgency ca,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate capt,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.PaymentTemplate pt,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.PaymentTemplateFrequency ptfreq,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.EffectiveDepositFrequency edfreq,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.CompanyLaw companyLaw,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo qbinfo,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.CompanyService cs\n")
                .append(" where ca.Company = co\n")
                .append("    and companyLaw.CompanyAgency = ca\n")
                .append("    and capt.CompanyAgency = ca\n")
                .append("    and capt.PaymentTemplate = pt\n")
                .append("    and ptfreq.PaymentTemplate = pt\n")
                .append("    and edfreq.CompanyAgencyPaymentTemplate = capt\n")
                .append("    and edfreq.PaymentTemplateFrequency = ptfreq\n")
                .append("    and qbinfo.CompanyLaw = companyLaw\n")
                .append("    and cs.Company = co\n")
                .append("    and cs.Service.ServiceCd = 'Tax'\n")
                .append("    and edfreq.InvalidDate is null \n")
                .append("    and qbinfo.IsDeleted=false \n")
                // All withholding items (except NY-MTA305-PAYMENT) and IRS-941-PAYMENT payment template
                .append("    and (pt.Category = 'Withholding' or pt.PaymentTemplateCd = 'IRS-941-PAYMENT')\n")
                .append("     and pt.PaymentTemplateCd != 'MA-PFML-PAYMENT' \n")
                .append("     and pt.PaymentTemplateCd != 'NY-MTA305-PAYMENT' \n");


        // Only check modified date during incremental updates.
        if (startDate != null) {
            builder.append("    and (edfreq.ModifiedDate >= :startDate or companyLaw.ModifiedDate >= :startDate or cs.ModifiedDate >=:startDate)\n");
        }
        appendDGCheckCondition(builder, "co", Boolean.FALSE);

        builder.append(" order by co.SourceCompanyId, pt.PaymentTemplateCd, edfreq.EffectiveDate DESC");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());

        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private static ScrollableResults findFederalFilingTypeResults(SpcfCalendar startDate) {

        StringBuilder builder = new StringBuilder();

        // Query for deposit frequency records that do not have an invalid date.
        builder.append(" select co.SourceCompanyId, 'FD', formTemplate.FormTemplateCd, caft.EffectiveDate\n")
               .append(" from com.intuit.sbd.payroll.psp.domain.CompanyAgencyFormTemplate caft\n")
               .append(" , com.intuit.sbd.payroll.psp.domain.CompanyService as  cs\n")
               .append(" join caft.FormTemplate formTemplate\n")
               .append(" join caft.CompanyAgency ca\n")
               .append(" join ca.Company co\n")
               .append(" where ca.Agency.Name = 'Internal Revenue Service'\n")
               .append("    and formTemplate.FormTemplateCd in ('IRS-941-FILING', 'IRS-944-FILING')\n")
               .append("    and caft.InvalidDate is null\n")
               .append("    and cs.Service.ServiceCd = 'Tax'\n")
               .append("    and cs.StatusCd in ('ActiveCurrent', 'Cancelled','Terminated','ActiveSeasonal')\n")
               .append("    and cs.Company=co ");

        // Only check modified date during incremental updates.
        if (startDate != null) {
            builder.append("    and (caft.ModifiedDate >= :startDate \n")
                   .append("       or (cs.ModifiedDate >= :startDate and cs.StatusCd ='ActiveCurrent')) \n");
        }

        appendDGCheckCondition(builder, "co", Boolean.FALSE);

        //PSP-11806
        builder.append(" order by co.SourceCompanyId");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private static ScrollableResults findBusinessTaxIdResults(SpcfCalendar startDate) {
        StringBuilder builder = new StringBuilder();
        // Query for deposit frequency records that do not have an invalid date.
        builder.append(" select co.SourceCompanyId, 'IA', coptaid.AgencyTaxpayerIdEnc, coptaid.ModifiedDate\n")
                .append(" from com.intuit.sbd.payroll.psp.domain.CompanyPaymentTemplateAgencyId coptaid\n")
                .append(", com.intuit.sbd.payroll.psp.domain.CompanyService as  cs\n")
                .append(" join coptaid.CompanyAgencyPaymentTemplate capt\n")
                .append(" join capt.CompanyAgency ca\n")
                .append(" join ca.Company co\n")
                .append(" where coptaid.Name = '" + IA_BUSINESS_TAX_NAME + "'")
                .append("    and cs.Company=co \n")
                .append("    and cs.Service.ServiceCd = 'Tax'\n");

        // Only check modified date during incremental updates.
        if (startDate != null) {
            builder.append("    and (coptaid.ModifiedDate >= :startDate or cs.ModifiedDate >=:startDate)\n");
        }
        appendDGCheckCondition(builder, "co", Boolean.FALSE);

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private String getDepositFrequencyAbbreviation(String state, DepositFrequencyCode depFreqCode) {

        String depFreqAbbrev = null;

        // State specific values.
        if ("ID".equals(state) && depFreqCode == DepositFrequencyCode.SPLITMONTHLY) {
            depFreqAbbrev = "SM";
        } else if ("KS".equals(state) && depFreqCode == DepositFrequencyCode.QUADMONTHLY) {
            depFreqAbbrev = "QM";
        } else if ("KY".equals(state) && depFreqCode == DepositFrequencyCode.TWICEMONTHLY) {
            depFreqAbbrev = "SM";
        } else if ("MA".equals(state) && depFreqCode == DepositFrequencyCode.QUARTERMONTHLY) {
            depFreqAbbrev = "W";
        } else if ("NY".equals(state) && depFreqCode == DepositFrequencyCode.THREEBANKINGDAY) {
            depFreqAbbrev = "3D";
            // Non-state specific.
        } else if (depFreqCode == DepositFrequencyCode.ANNUAL) {
            // In AS400Gateway.depositFrequencyMap, this could be "A" or "Y".
            depFreqAbbrev = "A";
        } else if (depFreqCode == DepositFrequencyCode.EIGHTHMONTHLY) {
            depFreqAbbrev = "EM";
        } else if (depFreqCode == DepositFrequencyCode.MONTHLY) {
            depFreqAbbrev = "M";
        } else if (depFreqCode == DepositFrequencyCode.QUARTERLY) {
            depFreqAbbrev = "Q";
        } else if (depFreqCode == DepositFrequencyCode.QUARTERMONTHLY) {
            depFreqAbbrev = "QM";
        } else if (depFreqCode == DepositFrequencyCode.SEMIANNUAL) {
            depFreqAbbrev = "SA";
        } else if (depFreqCode == DepositFrequencyCode.SEMIMONTHLY) {
            depFreqAbbrev = "SM";
        } else if (depFreqCode == DepositFrequencyCode.SEMIWEEKLY) {
            depFreqAbbrev = "SW";
        } else if (depFreqCode == DepositFrequencyCode.TWICEMONTHLY) {
            depFreqAbbrev = "TM";
        } else if (depFreqCode == DepositFrequencyCode.WEEKLY) {
            depFreqAbbrev = "W";
        } else if (depFreqCode == DepositFrequencyCode.ACCELERATED) {
            depFreqAbbrev = "3D";
        } else {
            logger.error("Unable to determine a deposit frequency abbreviation for " + state + " and " + depFreqCode + ".");
        }

        return depFreqAbbrev;
    }

    private String getFilerTypeAbbreviation(String payTemplateName) {
        if (FILERTYPE_FT_941.equals(payTemplateName)) {
            return "1";
        } else if (FILERTYPE_FT_944.equals(payTemplateName)) {
            return "4";
        } else {
            logger.error("Unable to determine a filer type abbreviation for " + payTemplateName + ".");
            return null;
        }
    }
}
