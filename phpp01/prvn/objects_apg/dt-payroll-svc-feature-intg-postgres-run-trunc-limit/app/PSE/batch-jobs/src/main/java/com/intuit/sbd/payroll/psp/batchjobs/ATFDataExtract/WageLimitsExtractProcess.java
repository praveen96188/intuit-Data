package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 */
public class WageLimitsExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID ="WAGE_LIMIT";

    public WageLimitsExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode =  REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.WageLimitsInfo;
    }

    public void execute() {
        super.execute();
    }

    /**
     * This method writes the actual company info details.
     * @param pPW PrintWriter
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {

        SpcfCalendar now = PSPDate.getPSPTime();
        int year = now.getYear();
        int quarter = ((now.getMonth() - 1) / 3) + 1;

        // Write data for current quarter.
        writeWageLimits(pPW, now.getYear(), quarter, false);

        if (quarter == 1) {
            // Wrap around to 4th quarter of previous year.
            year--;
            quarter = 4;
        } else {
            quarter--;
        }

        // Write out data for all past quarters back to 1st quarter.
        while (quarter > 0) {
            writeWageLimits(pPW, year, quarter, true);
            quarter--;
        }
    }

    private void writeWageLimits(PrintWriter pPW, int year, int quarter, boolean includeQuarter) {

        StringBuilder builder = new StringBuilder();

        // Query for Limit/Law for the given year/quarter.
        builder.append(" select limit, law \n")
               .append(" from com.intuit.sbd.payroll.psp.domain.WageLimit limit \n")
               .append("    join limit.Law law \n")
               .append(" where limit.EffectiveYearQuarter = :yearQuarter \n");

        // Sort by Law to provide consistency for automated testing.
        builder.append(" order by law \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());

        query.setParameter("yearQuarter", String.format("%d%d", year, quarter));
        
        writeRecords(pPW, year, quarter, includeQuarter, query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY));
    }

    private void writeRecords(PrintWriter pPW, int year, int quarter, boolean includeQuarter, ScrollableResults records) {

        while(records.next()) {

            WageLimit wl = (WageLimit)records.get(0);
            Law law = (Law)records.get(1);

            pPW.print(DOUBLE_QUOTE);
            pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);

            // Year
            pPW.print(year);
            pPW.print(DELIMITER);

            // Current data will not have a quarter associated with it.
            if (includeQuarter) {
                pPW.print(quarter);
            }
            pPW.print(DELIMITER);

            pPW.print(SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(SourceSystemCode.QBDT, law) + DELIMITER);

            pPW.println(wl.getAmount() + DOUBLE_QUOTE);

            // Increment the record count for each record written in the extract file for the trailer record.
            mRecordCount++;
        }

    }

}
