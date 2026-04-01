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

/**
 * For Updated Data - Send All payment records for a company for the filing period (Quarter) if even one record is unserted or modified
 * For Quarterly Data - Send all payment records for ALL companies irresepective of whether a new record was created or updated.
 *
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:49:45 PM
 * To change this template use File | Settings | File Templates.
 */
@ScheduledJob(name = "ATFCompanyPaymentExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class CompanyPaymentsExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID ="CO_PAY";

    public CompanyPaymentsExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public CompanyPaymentsExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	mProcessCode =  REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.CompanyPaymentsInfo;
    }

    public void execute() {
        getLogger().info("Starting " + getClass().getSimpleName() + " process job");
        StopWatch timer = StopWatch.startTimer();
        super.execute();
        getLogger().info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    /**
     * This method writes the actual company info details.
     * @param pPW
     *
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            int extractQtr = this.mExtractBatch.getQuarter();
            int extractYear = this.mExtractBatch.getYear();
            getLogger().info("Beginning CompanyPaymentsExtractProcess for Quarterly data.");

            SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(extractYear, extractQtr);
            SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(extractYear, extractQtr);

            getLogger().info("Finding all payments between "+ firstDayOfQtr+ " and "+ lastDayOfQtr);
            ScrollableResults payments = findPaymentsBetweenDates(null, firstDayOfQtr, lastDayOfQtr);
            getLogger().info("Payments query completed");
            writePaymentRecords(payments, pPW);
            getLogger().info("Completed writing payments to file for ALL");
        } else {
            getLogger().info("Beginning CompanyPaymentsExtractProcess for Updated data.");
            // updated data  - During current qtr auditing get current and all prior qtr changes
            // During filing month, get only previous qtr and all prior qtr changes
            writeUpdatedPaymentData(pPW);
            getLogger().info("Completed writing payments to file for UPDATE");
        }
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            int extractQtr = this.mExtractBatch.getQuarter();
            int extractYear = this.mExtractBatch.getYear();
            getLogger().info("Beginning CompanyPaymentsExtractProcess for Quarterly data.");

            SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(extractYear, extractQtr);
            SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(extractYear, extractQtr);

            getLogger().info("Finding all payments between "+ firstDayOfQtr+ " and "+ lastDayOfQtr);
            ScrollableResults payments = findPaymentsBetweenDates(null, firstDayOfQtr, lastDayOfQtr);
            getLogger().info("Payments query completed");
            writePaymentRecords(payments, pPW);
            getLogger().info("Completed writing payments to file for ALL");
        } else {
            getLogger().info("Beginning CompanyPaymentsExtractProcess for Updated data.");
            // updated data  - During current qtr auditing get current and all prior qtr changes
            // During filing month, get only previous qtr and all prior qtr changes
            writeUpdatedPaymentData(pPW);
            getLogger().info("Completed writing payments to file for UPDATE");
        }
    }

    private void writeUpdatedPaymentData(PgpWriter pPW) throws IOException {
        SpcfCalendar lastRunDate = super.getLastSuccessfulExtractBatchStartdate(true).toLocal();   //gets the max date that the extract was last run and sets the cutoff to to 5:00 PM for that date
        SpcfCalendar endDate = getEndDate();
        ScrollableResults mmts = findPaymentsBetweenDates(lastRunDate, null, endDate);
        writePaymentRecords(mmts, pPW);
    }

    private void writeUpdatedPaymentData(PrintWriter pPW) {
        SpcfCalendar lastRunDate = super.getLastSuccessfulExtractBatchStartdate(true).toLocal();   //gets the max date that the extract was last run and sets the cutoff to to 5:00 PM for that date
        SpcfCalendar endDate = getEndDate();
        ScrollableResults mmts = findPaymentsBetweenDates(lastRunDate, null, endDate);
        writePaymentRecords(mmts, pPW);
    }

    @Override
    public SpcfCalendar getEndDate() {
        return CalendarUtils.getFirstDayOfNextQuarter(PSPDate.getPSPTime());
    }

    private void writePaymentRecords(ScrollableResults pPayments, PrintWriter pPW) {
        int i=0;
        String psid = null;
        SourceSystemCode sourceSystemCode = null;
        SpcfCalendar payPeriodEndDate = null;
        SpcfCalendar paymentDate = null;

        try {
            while (pPayments.next()) {

                Law law = (Law) pPayments.get(0);
                SpcfMoney amount = (SpcfMoney) pPayments.get(1);
                psid = (String) pPayments.get(2);
                sourceSystemCode = (SourceSystemCode) pPayments.get(3);
                payPeriodEndDate = (SpcfCalendar) pPayments.get(4);
                paymentDate = (SpcfCalendar) pPayments.get(5);
                String mmt_id = pPayments.get(6).toString();

                int quarter =  CalendarUtils.getQuarterAsInt(payPeriodEndDate);
                int year = payPeriodEndDate.getYear();
                String paymentDateAsString = StringFormatter.formatDate(paymentDate.toLocal(), "yyyyMMdd" );

                pPW.print(DOUBLE_QUOTE);
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);
                //Source Company ID
                pPW.print(psid + DELIMITER);
                //qtr
                pPW.print(quarter + DELIMITER);
                //Year
                pPW.print(year + DELIMITER);

                //src tax code
                String currLawId = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(sourceSystemCode,law);
                pPW.print(currLawId + DELIMITER);

                // Payment Date -- Initiation date in the MMT
                pPW.print(paymentDateAsString + DELIMITER);

                //Payment Amount for Law ID
                SpcfMoney negatedPaymentAmountForATF = new SpcfMoney(amount.negate());
                pPW.print(negatedPaymentAmountForATF.toString() + DELIMITER);

                //MMT id tobe included for TFS
                pPW.print(mmt_id);

                pPW.println(DOUBLE_QUOTE);
                mRecordCount++;// Increment for the record count in the trailer record

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pPayments.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pPayments.close();
        }
    }

    private void writePaymentRecords(ScrollableResults pPayments, PgpWriter pPW) throws IOException {
        int i=0;
        String psid = null;
        SourceSystemCode sourceSystemCode = null;
        SpcfCalendar payPeriodEndDate = null;
        SpcfCalendar paymentDate = null;

        try {
            while (pPayments.next()) {

                Law law = (Law) pPayments.get(0);
                SpcfMoney amount = (SpcfMoney) pPayments.get(1);
                psid = (String) pPayments.get(2);
                sourceSystemCode = (SourceSystemCode) pPayments.get(3);
                payPeriodEndDate = (SpcfCalendar) pPayments.get(4);
                paymentDate = (SpcfCalendar) pPayments.get(5);
                String mmt_id = pPayments.get(6).toString();

                int quarter =  CalendarUtils.getQuarterAsInt(payPeriodEndDate);
                int year = payPeriodEndDate.getYear();
                String paymentDateAsString = StringFormatter.formatDate(paymentDate.toLocal(), "yyyyMMdd" );

                pPW.write(DOUBLE_QUOTE);
                pPW.write(ATF_EXTRACT_TYPE_ID + DELIMITER);
                //Source Company ID
                pPW.write(psid + DELIMITER);
                //qtr
                pPW.write(quarter + DELIMITER);
                //Year
                pPW.write(year + DELIMITER);

                //src tax code
                String currLawId = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(sourceSystemCode,law);
                pPW.write(currLawId + DELIMITER);

                // Payment Date -- Initiation date in the MMT
                pPW.write(paymentDateAsString + DELIMITER);

                //Payment Amount for Law ID
                SpcfMoney negatedPaymentAmountForATF = new SpcfMoney(amount.negate());
                pPW.write(negatedPaymentAmountForATF.toString() + DELIMITER);

                //MMT id tobe included for TFS
                pPW.write(mmt_id);

                pPW.write(DOUBLE_QUOTE);
                pPW.write(System.lineSeparator());
                mRecordCount++;// Increment for the record count in the trailer record

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pPayments.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pPayments.close();
        }
    }

    public static ScrollableResults findPaymentsBetweenDates(SpcfCalendar lastRunDate, SpcfCalendar startDate, SpcfCalendar endDate) {
        StringBuilder builder = new StringBuilder();
        builder .append(" Select ptp.Law  \n")
                .append(", ptp.Amount \n")
                .append(", ptp.Company.SourceCompanyId, ptp.Company.SourceSystemCd \n")
                .append(", ptp.QuarterEndDate, ptp.PaymentDate, ptp.MoneyMovementTransaction.Id \n")
                .append(" from com.intuit.sbd.payroll.psp.domain.ATFPaymentsToProcess as ptp \n")
                .append(" where ptp.QuarterEndDate >= :startDate ")
                .append(" and ptp.QuarterEndDate <= :endDate \n");

        // If we are given a last run date, only retrieve data for those companies that have had a change since that date.
        if (lastRunDate!=null) {
            // Using "exists" here because the equivalent "in" causes a join to Company within the sub-query which causes the
            // whole query to perform much worse.  TLD.
            builder.append(" and exists (Select 1 from com.intuit.sbd.payroll.psp.domain.ATFPaymentsToProcess as updatedCos where updatedCos.Company = ptp.Company and updatedCos.ModifiedDate >= :lastRunDate) \n");
        }

        appendDGCheckCondition(builder, "ptp.Company", Boolean.FALSE);

        // Consistent ordering allows automated testing to work.
        builder.append(" order by ptp.Company.SourceCompanyId, ptp.PaymentDate, ptp.Law.PaymentTemplate, ptp.MoneyMovementTransaction.Id, ptp.Law");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        query.setParameter("endDate", endDate);

        if(startDate != null) {
            query.setParameter("startDate", startDate);
        } else {
            int xQuarters = SystemParameter.findIntValue(SystemParameter.Code.PAST_ATF_PAYMENTS_INTERVAL, 6);
            SpcfCalendar defaultStartDate = CalendarUtils.getFirstDayOfPrevXQuarter(endDate, xQuarters);
            query.setParameter("startDate", defaultStartDate);
        }

        if (lastRunDate!=null) {
            query.setParameter("lastRunDate", lastRunDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }
}
