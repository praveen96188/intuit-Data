package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.PayrollFormInfoBuilder;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: smodgil
 * Date: May 7, 2020
 * Time: 3:40:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyPayrollItemExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "CO_PAYROLL_ITEM";

    public CompanyPayrollItemExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode = REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyPayrollItemInfo;
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
        SpcfCalendar lastRunDate =null;
        int minLastQuarterToFile = 0;
        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
            if(lastRunDate != null) {
                minLastQuarterToFile = Integer.parseInt(Integer.toString(lastRunDate.getYear()) + "1");
            }
        } else if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            minLastQuarterToFile = Integer.parseInt(Integer.toString(mExtractBatch.getYear()) + "1");
        }

        logger.info("Beginning CompanyPayrollItemExtractProcess for: " + mExtractBatch.getRunType());
        writeCompanyPayrollItemInfo(pPW, getCompanyPayrollItemExtract(minLastQuarterToFile, lastRunDate));
        logger.info("Completed writing file");
    }

    /**
     * This method write a record into the extract file for CO_PAYROLL_ITEM
     *
     * @param pPW                   PrintWriter
     * @param pCompanyPayrollItems CompanyPayrollItems
     */
    private void writeCompanyPayrollItemInfo(PrintWriter pPW, ScrollableResults pCompanyPayrollItems) throws Throwable {
        String w2Code=null;
        try {
            while (pCompanyPayrollItems.next()) {
                pPW.print(DOUBLE_QUOTE);
                //RECORD ID
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);

                //Source Company ID
                writeFormatted(pPW, pCompanyPayrollItems.get(0)==null?"":(String) pCompanyPayrollItems.get(0));

                //Source Description
                writeFormatted(pPW, pCompanyPayrollItems.get(1)==null?"":(String) pCompanyPayrollItems.get(1));

                //Source Payroll Item Id
                writeFormatted(pPW, pCompanyPayrollItems.get(2)==null?"":(String) pCompanyPayrollItems.get(2));

                //W2 Code
                w2Code=String.valueOf(pCompanyPayrollItems.get(3));
                writeFormatted(pPW, w2Code != null ? w2Code :PayrollFormInfoBuilder.getW2Codes((String) pCompanyPayrollItems.get(4)));

                //Tax Form Line
                writeFormatted(pPW, pCompanyPayrollItems.get(4)==null?"":(String) pCompanyPayrollItems.get(4),true);


                mRecordCount++;// Increment for the record count in the trailer record

                if (mRecordCount % 100000 == 0) {
                    logger.info("Payroll Items written :" + mRecordCount + " Records.");
                }
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pCompanyPayrollItems.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pCompanyPayrollItems.close();
        }
        logger.info(String.format("Company Payroll Item Extract finished writing %d records to Payroll Items extract file.", mRecordCount));
    }


    private ScrollableResults getCompanyPayrollItemExtract(int pMinLastQuarterToFile, SpcfCalendar pLastRunDate ) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" select c.SourceCompanyId, cpi.SourceDescription,cpi.SourcePayrollItemId ,cpi.W2Code, cpi.TaxFormLine \n")
                .append("      from com.intuit.sbd.payroll.psp.domain.CompanyPayrollItem cpi, \n")
                .append("      com.intuit.sbd.payroll.psp.domain.Company c,\n")
                .append("      com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo \n")
                .append(" where cpi.Company = c \n")
                .append("   and c = serviceInfo.Company \n")
                .append("   and serviceInfo.ServiceStartDate is not null \n")
                .append("and (serviceInfo.StatusCd in ('ActiveCurrent','Terminated') or (serviceInfo.StatusCd = 'Cancelled' and serviceInfo.LastQuarterToFile >= :minLastQuarterToFile))\n");

        if (pLastRunDate != null) {
            queryBuilder.append(" and (cpi.ModifiedDate >= :lastRunDate or serviceInfo.ModifiedDate >=:lastRunDate) \n");
        }

        appendDGCheckCondition(queryBuilder, "c", Boolean.FALSE);

        queryBuilder.append(" order by c.SourceCompanyId, cpi.SourcePayrollItemId \n");

        Query query = Application.getHibernateSession().createQuery(queryBuilder.toString());

        if (pLastRunDate != null) {
            query.setParameter("lastRunDate", pLastRunDate);
        }

        query.setParameter("minLastQuarterToFile", pMinLastQuarterToFile);

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

}
