package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CutOffDateCalc;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSCallBackGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:46:46 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name="ATFCompanyLiabilityExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class CompanyLiabilitiesExtractProcess extends BaseATFExtractFileProcess{

    private static final String ATF_EXTRACT_TYPE_ID ="CO_LIA";
    private static HashMap<String, String> LAWS_TO_EXTRACT_TIPS_FOR=new HashMap<String, String>();

    public CompanyLiabilitiesExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public CompanyLiabilitiesExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	mProcessCode =  REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyLiabilitiesInfo;

        LAWS_TO_EXTRACT_TIPS_FOR.put("61", "00G");
        LAWS_TO_EXTRACT_TIPS_FOR.put("63", "00D");
    }

    public void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        super.execute();
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    /**
     * This method writes the actual Liability details into the CO_LIA extract file.
     *
     * @param pPW
     *
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            int extractQtr = this.mExtractBatch.getQuarter();
            int extractYear = this.mExtractBatch.getYear();
            getLogger().info("Beginning CompanyLiabilitiesExtractProcess for Quarterly data.");
            writeLiabilityRecords(getCDLsForQuarterlyData(extractYear, extractQtr), pPW);
            getLogger().info("Completed writing file for Quarterly data.");
        } else {
            // updated data  - During current qtr auditing get current and all prior qtr changes
            // During filing month, get only previous qtr and all prior qtr changes
            getLogger().info("Beginning CompanyLiabilitiesExtractProcess for Updated data.");
            writeLiabilityRecords(getUpdatedLiabilityData(), pPW);
            getLogger().info("Completed writing file for Updated data.");
        }
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            int extractQtr = this.mExtractBatch.getQuarter();
            int extractYear = this.mExtractBatch.getYear();
            getLogger().info("Beginning CompanyLiabilitiesExtractProcess for Quarterly data.");
            writeLiabilityRecords(getCDLsForQuarterlyData(extractYear, extractQtr), pPW);
            getLogger().info("Completed writing file for Quarterly data.");
        } else {
            // updated data  - During current qtr auditing get current and all prior qtr changes
            // During filing month, get only previous qtr and all prior qtr changes
            getLogger().info("Beginning CompanyLiabilitiesExtractProcess for Updated data.");
            writeLiabilityRecords(getUpdatedLiabilityData(), pPW);
            getLogger().info("Completed writing file for Updated data.");
        }
    }

    /**
     * This method return the list of UPDATED liabilities that we need to flow to ATF in the CO_LIA extract file.
     *
     * @return
     */
    private ScrollableResults getUpdatedLiabilityData() {
        SpcfCalendar lastRunDate = super.getLastSuccessfulExtractBatchStartdate(true).toLocal();   //gets the max date that the extract was last run
        SpcfCalendar toDate = PSPDate.getPSPTime(); //gets the current date and sets the cutoff to 5:00 PM for that date
        return findCompanyDailyLiabilitiesBetweenDates(null, getEndDate(), lastRunDate);
    }

    /**
     * This method returns all tax records for a quarter sorted by company
     *
     * @param pExtractYear
     * @param pExtractQtr
     * @return
     */
    private ScrollableResults getCDLsForQuarterlyData(int pExtractYear, int pExtractQtr) {
        // the paycheck date for which the payments were made needs to fall within these 2 dates
        SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(pExtractYear, pExtractQtr);
        SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(pExtractYear, pExtractQtr);

        return findCompanyDailyLiabilitiesBetweenDates(firstDayOfQtr, lastDayOfQtr, null);
    }

    private void writeLiabilityRecords(ScrollableResults pCompanyDailyLiabilities, PgpWriter pPW) throws IOException {
        getLogger().info("Completed query to find company liability records.  Writing file.");
        try {
            int i=0;
            while (pCompanyDailyLiabilities.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for "+i+" total CDLs");
                }

                CompanyDailyLiability currCDL = (CompanyDailyLiability) pCompanyDailyLiabilities.get(0);
                String psid = (String) pCompanyDailyLiabilities.get(1);
                SourceSystemCode sourceSystemCode = (SourceSystemCode) pCompanyDailyLiabilities.get(2);

                SpcfMoney taxableWages = currCDL.getTaxableWages();
                boolean liabilityRequiresBrokenOutTips = false;
                //If we're breaking out tips for this item, we need to send in the taxable wages MINUS the tips taxable wages since we'll be sending the tips taxable wages in a compeltely
                // separate record
                if (LAWS_TO_EXTRACT_TIPS_FOR.containsKey(currCDL.getLaw().getLawId()) && currCDL.getTotalTipsAmount().compareTo(SpcfMoney.ZERO)!=0) {
                    taxableWages = new SpcfMoney(taxableWages.subtract(currCDL.getTotalTipsAmount()));
                    liabilityRequiresBrokenOutTips = true;
                }

                pPW.write(DOUBLE_QUOTE);
                //RECORD ID
                pPW.write(ATF_EXTRACT_TYPE_ID + DELIMITER);
                //Source Company ID
                pPW.write(psid + DELIMITER);

                //qtr
                pPW.write(CalendarUtils.getQuarterAsInt(currCDL.getLiabilityDate()) + DELIMITER);

                //Year
                pPW.write(currCDL.getLiabilityDate().getYear() + DELIMITER);

                //src tax code
                String currLawId = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(sourceSystemCode, currCDL.getLaw());

                pPW.write(currLawId + DELIMITER);
                //Liability Date
                pPW.write(StringFormatter.formatDate(currCDL.getLiabilityDate().toLocal(), "yyyyMMdd" )+ DELIMITER);
                //Entry Date
                pPW.write(DELIMITER);
                //Total Wages and Tips
                pPW.write(currCDL.getTotalWages().toString() + DELIMITER);
                //Taxable Wages and Tips
                pPW.write(taxableWages.toString() + DELIMITER);
                //Total Tips
                pPW.write("0.00"+DELIMITER);
                //Tax Amount
                pPW.write(currCDL.getTaxAmount().toString()+DELIMITER);
                //QTD Tax
                pPW.write(DELIMITER);
                //RECORD NUMBER
                pPW.write(mRecordCount + DELIMITER);
                //YTD Tax
                pPW.write(DELIMITER);
                pPW.write(DOUBLE_QUOTE);
                pPW.write(System.lineSeparator());
                mRecordCount++;// Increment for the record count in the trailer record

                //ATF wants tips in a completely separate record in the extract file with its own fake law; send it if there are any tips associated with the CDL
                if (liabilityRequiresBrokenOutTips) {
                    pPW.write(DOUBLE_QUOTE);
                    //RECORD ID
                    pPW.write(ATF_EXTRACT_TYPE_ID + DELIMITER);
                    //Source Company ID
                    pPW.write(psid + DELIMITER);

                    //qtr
                    pPW.write(CalendarUtils.getQuarterAsInt(currCDL.getLiabilityDate()) + DELIMITER);

                    //Year
                    pPW.write(currCDL.getLiabilityDate().getYear() + DELIMITER);

                    //src tax code
                    String tipsLawId = LAWS_TO_EXTRACT_TIPS_FOR.get(currCDL.getLaw().getLawId());

                    pPW.write(tipsLawId + DELIMITER);
                    //Liability Date
                    pPW.write(StringFormatter.formatDate(currCDL.getLiabilityDate().toLocal(), "yyyyMMdd" )+ DELIMITER);
                    //Entry Date
                    pPW.write(DELIMITER);
                    //Total Wages and Tips
                    pPW.write(currCDL.getTotalWages().toString() + DELIMITER);
                    //Taxable Tips Wages and Tips
                    pPW.write(currCDL.getTotalTipsAmount().toString() + DELIMITER);
                    //Total Tips
                    pPW.write("0.00"+DELIMITER);
                    //Tax Amount- we don't have the actual liability amount, and it's not needed for forms
                    pPW.write("0.00"+DELIMITER);
                    //QTD Tax
                    pPW.write(DELIMITER);
                    //RECORD NUMBER
                    pPW.write(mRecordCount + DELIMITER);
                    //YTD Tax
                    pPW.write(DELIMITER);
                    pPW.write(DOUBLE_QUOTE);
                    pPW.write(System.lineSeparator());
                    mRecordCount++;// Increment for the record count in the trailer record
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pCompanyDailyLiabilities.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pCompanyDailyLiabilities.close();
        }
    }

    private void writeLiabilityRecords(ScrollableResults pCompanyDailyLiabilities, PrintWriter pPW) {
        getLogger().info("Completed query to find company liability records.  Writing file.");
        try {
            int i=0;
            while (pCompanyDailyLiabilities.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for "+i+" total CDLs");
                }

                CompanyDailyLiability currCDL = (CompanyDailyLiability) pCompanyDailyLiabilities.get(0);
                String psid = (String) pCompanyDailyLiabilities.get(1);
                SourceSystemCode sourceSystemCode = (SourceSystemCode) pCompanyDailyLiabilities.get(2);

                SpcfMoney taxableWages = currCDL.getTaxableWages();
                boolean liabilityRequiresBrokenOutTips = false;
                //If we're breaking out tips for this item, we need to send in the taxable wages MINUS the tips taxable wages since we'll be sending the tips taxable wages in a compeltely
                // separate record
                if (LAWS_TO_EXTRACT_TIPS_FOR.containsKey(currCDL.getLaw().getLawId()) && currCDL.getTotalTipsAmount().compareTo(SpcfMoney.ZERO)!=0) {
                    taxableWages = new SpcfMoney(taxableWages.subtract(currCDL.getTotalTipsAmount()));
                    liabilityRequiresBrokenOutTips = true;
                }

                pPW.print(DOUBLE_QUOTE);
                //RECORD ID
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);
                //Source Company ID
                pPW.print(psid + DELIMITER);

                //qtr
                pPW.print(CalendarUtils.getQuarterAsInt(currCDL.getLiabilityDate()) + DELIMITER);

                //Year
                pPW.print(currCDL.getLiabilityDate().getYear() + DELIMITER);

                //src tax code
                String currLawId = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(sourceSystemCode, currCDL.getLaw());

                pPW.print(currLawId + DELIMITER);
                //Liability Date
                pPW.print(StringFormatter.formatDate(currCDL.getLiabilityDate().toLocal(), "yyyyMMdd" )+ DELIMITER);
                //Entry Date
                pPW.print(DELIMITER);
                //Total Wages and Tips
                pPW.print(currCDL.getTotalWages().toString() + DELIMITER);
                //Taxable Wages and Tips
                pPW.print(taxableWages.toString() + DELIMITER);
                //Total Tips
                pPW.print("0.00"+DELIMITER);
                //Tax Amount
                pPW.print(currCDL.getTaxAmount().toString()+DELIMITER);
                //QTD Tax
                pPW.print(DELIMITER);
                //RECORD NUMBER
                pPW.print(mRecordCount + DELIMITER);
                //YTD Tax
                pPW.print(DELIMITER);
                pPW.println(DOUBLE_QUOTE);
                mRecordCount++;// Increment for the record count in the trailer record

                //ATF wants tips in a completely separate record in the extract file with its own fake law; send it if there are any tips associated with the CDL
                if (liabilityRequiresBrokenOutTips) {
                    pPW.print(DOUBLE_QUOTE);
                    //RECORD ID
                    pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);
                    //Source Company ID
                    pPW.print(psid + DELIMITER);

                    //qtr
                    pPW.print(CalendarUtils.getQuarterAsInt(currCDL.getLiabilityDate()) + DELIMITER);

                    //Year
                    pPW.print(currCDL.getLiabilityDate().getYear() + DELIMITER);

                    //src tax code
                    String tipsLawId = LAWS_TO_EXTRACT_TIPS_FOR.get(currCDL.getLaw().getLawId());

                    pPW.print(tipsLawId + DELIMITER);
                    //Liability Date
                    pPW.print(StringFormatter.formatDate(currCDL.getLiabilityDate().toLocal(), "yyyyMMdd" )+ DELIMITER);
                    //Entry Date
                    pPW.print(DELIMITER);
                    //Total Wages and Tips
                    pPW.print(currCDL.getTotalWages().toString() + DELIMITER);
                    //Taxable Tips Wages and Tips
                    pPW.print(currCDL.getTotalTipsAmount().toString() + DELIMITER);
                    //Total Tips
                    pPW.print("0.00"+DELIMITER);
                    //Tax Amount- we don't have the actual liability amount, and it's not needed for forms
                    pPW.print("0.00"+DELIMITER);
                    //QTD Tax
                    pPW.print(DELIMITER);
                    //RECORD NUMBER
                    pPW.print(mRecordCount + DELIMITER);
                    //YTD Tax
                    pPW.print(DELIMITER);
                    pPW.println(DOUBLE_QUOTE);
                    mRecordCount++;// Increment for the record count in the trailer record
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pCompanyDailyLiabilities.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pCompanyDailyLiabilities.close();
        }
    }

    public static ScrollableResults findCompanyDailyLiabilitiesBetweenDates(SpcfCalendar startDate, SpcfCalendar endDate, SpcfCalendar lastRunDate) {
        StringBuilder builder = new StringBuilder();
        builder .append("  Select companyDailyLiability, companyDailyLiability.Company.SourceCompanyId, companyDailyLiability.Company.SourceSystemCd \n")
                .append(" from com.intuit.sbd.payroll.psp.domain.CompanyDailyLiability as companyDailyLiability\n")
                .append(" where companyDailyLiability.LiabilityDate <= :endDate ");

        if (startDate!=null) {
            builder.append(" and companyDailyLiability.LiabilityDate >= :startDate ");
        }

        if (lastRunDate!=null) {
            builder.append(" and companyDailyLiability.ModifiedDate >= :lastRunDate ");
        }

        appendDGCheckCondition(builder, "companyDailyLiability.Company", Boolean.FALSE);

        builder.append(" order by companyDailyLiability.Company, companyDailyLiability.LiabilityDate, companyDailyLiability.Law");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        query.setParameter("endDate", endDate);

        if (startDate!=null) {
            query.setParameter("startDate", startDate);
        }

        if (lastRunDate!=null) {
            query.setParameter("lastRunDate", lastRunDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

}
