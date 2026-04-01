package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Nov 14, 2012
 * Time: 3:40:40 PM
 */
@SuppressWarnings({"JpaQueryApiInspection", "JpaQlInspection"})
@ScheduledJob(name="W2CountsExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class W2CountsExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "W2_COUNT";
    private static final String ATF_EXTRACT_RECORD_ID = "CO_ADDL_INFO";

    public W2CountsExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public W2CountsExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	mProcessCode = REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.W2CountInfo;
    }

    public void execute() {
        super.execute();
    }

    /**
     * This method writes the actual W2 Count info details.
     *
     * @param pPW PrintWriter
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {
        int extractYear = this.mExtractBatch.getYear();
        ScrollableResults w2Counts = getW2Counts(extractYear);

        try {
            while (w2Counts.next()) {
                if (!w2Counts.get(1).toString().equals("IRS")) {
                    pPW.print(DOUBLE_QUOTE);
                    //RECORD ID
                    pPW.print(ATF_EXTRACT_RECORD_ID + DELIMITER);

                    //Source Company ID
                    writeFormatted(pPW, (String) w2Counts.get(0));

                    //State
                    writeFormatted(pPW, (String) w2Counts.get(1));

                    //Year
                    writeFormatted(pPW, String.valueOf(extractYear));

                    // W2_COUNT
                    writeFormatted(pPW, ATF_EXTRACT_TYPE_ID);

                    //Count
                    writeFormatted(pPW, String.valueOf(w2Counts.get(2)), true);

                    mRecordCount++;// Increment for the record count in the trailer record

                    if (mRecordCount % 100000 == 0) {
                        getLogger().info("W2 Counts written :" + mRecordCount + " Records.");
                    }
                }
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(w2Counts.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            w2Counts.close();
        }
        getLogger().info(String.format("W2 Counts finished writing %d records to W2 Counts extract file.", mRecordCount));
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {
        int extractYear = this.mExtractBatch.getYear();
        ScrollableResults w2Counts = getW2Counts(extractYear);

        try {
            while (w2Counts.next()) {
                if (!w2Counts.get(1).toString().equals("IRS")) {
                    pPW.write(DOUBLE_QUOTE);
                    //RECORD ID
                    pPW.write(ATF_EXTRACT_RECORD_ID + DELIMITER);

                    //Source Company ID
                    writeFormatted(pPW, (String) w2Counts.get(0));

                    //State
                    writeFormatted(pPW, (String) w2Counts.get(1));

                    //Year
                    writeFormatted(pPW, String.valueOf(extractYear));

                    // W2_COUNT
                    writeFormatted(pPW, ATF_EXTRACT_TYPE_ID);

                    //Count
                    writeFormatted(pPW, String.valueOf(w2Counts.get(2)), true);

                    mRecordCount++;// Increment for the record count in the trailer record

                    if (mRecordCount % 100000 == 0) {
                        getLogger().info("W2 Counts written :" + mRecordCount + " Records.");
                    }
                }
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(w2Counts.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            w2Counts.close();
        }
        getLogger().info(String.format("W2 Counts finished writing %d records to W2 Counts extract file.", mRecordCount));
    }


    private ScrollableResults getW2Counts(int pYear) {
        String[] paramNames = {"processingYear","excludeDeletedCompany"};
        Object[] paramValues = {pYear, Company.isDGDeleteFeatureEnabled()};
        return Application.scrollableResultsByNamedQuery(
                Application.getQueryName("calculateW2Counts"), paramNames, paramValues, -1, -1);
    }


}
