package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.hibernate.*;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 28, 2009
 * Time: 3:40:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeTotalsExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "EE_TOT";

    public EmployeeTotalsExtractProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        mProcessCode = REFRESH_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.EmployeeTotalsInfo;
    }

    public void execute() {
        super.execute();
    }

    /**
     * This method writes the actual company info details.
     *
     * @param pPW PrintWriter
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {
        ScrollableResults employeeLawQtrTotals;
        SpcfCalendar lastRunDate=null;
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.QuarterlyData)) {
            int extractQtr = this.mExtractBatch.getQuarter();
            int extractYear = this.mExtractBatch.getYear();
            employeeLawQtrTotals = getEmployeeLawQtrTotals(extractYear, extractQtr);
        } else {
            //If extracting for UpdatedData, will extract a maximum of 6 prior quarters of data from cut off date
            SpcfCalendar toDate = PSPDate.getPSPTime(); //gets the current date and sets the cutoff to 5:00 PM for that date
            SpcfCalendar upperBoundDate;

            if (CutOffDateCalc.getInstance().isOnOrAfterCutOffDate(PSPDate.getPSPTime())) {
                upperBoundDate = CalendarUtils.getLastDayOfQuarter(toDate);
            } else {
                upperBoundDate = getPriorQtrEndDate(PSPDate.getPSPTime());
            }

            //We will compare the last successful ATF extract date to the modified date on each ee qtr totals record to 
            // determine if that ee qtr total has been updated since the last time an extract was run
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(true).toLocal();

            SpcfCalendar lowerBoundDate = upperBoundDate;
            // select x, y, z from ee_tot_qtr_liability where modified_date>= lastRunDate and quarter <= endDate.getQuarter and year <= endDate.year and quarter >= toDate - 6 quarters
            // i.e. Fetch all the rows from EmployeeLawQtrTotals with modified_date >= lastRunDate for all all previous 6 quarters
            for (int i = 0; i < 5; i++) {
                lowerBoundDate = CalendarUtils.getLastDayOfPreviousQuarter(lowerBoundDate);
            }
            employeeLawQtrTotals = getEmployeeLawQtrTotals(lastRunDate, lowerBoundDate, upperBoundDate);

        }
        writeEmployeeLawQuarterlyInfo(pPW, employeeLawQtrTotals);
    }

    /**
     * This method write a record into the extract file for EE_TOT
     *
     * @param pPW                   PrintWriter
     * @param pEmployeeLawQtrTotals EmployeeLawQtrTotals
     */
    private void writeEmployeeLawQuarterlyInfo(PrintWriter pPW, ScrollableResults pEmployeeLawQtrTotals) throws Throwable {

        try {
            while (pEmployeeLawQtrTotals.next()) {
                pPW.print(DOUBLE_QUOTE);
                //RECORD ID
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);

                //Source Company ID
                writeFormatted(pPW, String.valueOf(pEmployeeLawQtrTotals.get(0)));

                //Employee ID
                writeFormatted(pPW, String.valueOf(pEmployeeLawQtrTotals.get(1)));

                //qtr
                writeFormatted(pPW, String.valueOf(pEmployeeLawQtrTotals.get(2)));

                //Year
                writeFormatted(pPW, String.valueOf(pEmployeeLawQtrTotals.get(3)));

                //src tax code -- Law ID
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(4) != null ? String.valueOf(pEmployeeLawQtrTotals.get(4)) : "");

                //Total Wages
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(5) != null ? new SpcfMoney(pEmployeeLawQtrTotals.get(5).toString()).toString() : SpcfMoney.ZERO.toString());

                //Taxable Wages
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(6) != null ? new SpcfMoney(pEmployeeLawQtrTotals.get(6).toString()).toString() : SpcfMoney.ZERO.toString());

                //Total Tips Taxable Wages
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(7) != null ? new SpcfMoney(pEmployeeLawQtrTotals.get(7).toString()).toString() : SpcfMoney.ZERO.toString());

                //Tax Amount
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(8) != null ? new SpcfMoney(pEmployeeLawQtrTotals.get(8).toString()).toString() : SpcfMoney.ZERO.toString());

                //Hours Worked
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(9) != null ? new BigDecimal(pEmployeeLawQtrTotals.get(9).toString()).setScale(2, RoundingMode.HALF_UP).toString() : SpcfMoney.ZERO.toString());

                //Weeks Worked
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(10) != null ? String.valueOf(pEmployeeLawQtrTotals.get(10)) : SpcfMoney.ZERO.toString());

                //Month one indicator
                writeFormatted(pPW,(pEmployeeLawQtrTotals.get(11) != null &&  new Short(String.valueOf(pEmployeeLawQtrTotals.get(11))).toString().equals("1")) ? "Y" : "N");

                //Month two indicator
                writeFormatted(pPW,(pEmployeeLawQtrTotals.get(12) != null &&  new Short(String.valueOf(pEmployeeLawQtrTotals.get(12))).toString().equals("1")) ? "Y" : "N");

                //Month three indicator
                writeFormatted(pPW,(pEmployeeLawQtrTotals.get(13) != null &&  new Short(String.valueOf(pEmployeeLawQtrTotals.get(13))).toString().equals("1")) ? "Y" : "N");

                //Hourly rate
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(14) != null ? new SpcfMoney(pEmployeeLawQtrTotals.get(14).toString()).toString() : SpcfMoney.ZERO.toString());

                //SourceItemId
                writeFormatted(pPW, pEmployeeLawQtrTotals.get(15) != null?(String) pEmployeeLawQtrTotals.get(15): "",true);

                mRecordCount++;// Increment for the record count in the trailer record

                if (mRecordCount % 100000 == 0) {
                    logger.info("EE Totals written :" + mRecordCount + " Records.");
                }
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pEmployeeLawQtrTotals.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            pEmployeeLawQtrTotals.close();
        }
        logger.info(String.format("EE Totals finished writing %d records to EE Totals extract file.", mRecordCount));
    }

    private ScrollableResults getEmployeeLawQtrTotals(SpcfCalendar pFromUpdateDate, SpcfCalendar pLowerBoundDate, SpcfCalendar pUpperBoundDate) {
        int lowerBoundQuarter = CalendarUtils.getQuarterAsInt(pLowerBoundDate);
        int lowerBoundYear = pLowerBoundDate.getYear();

        int upperBoundQuarter = CalendarUtils.getQuarterAsInt(pUpperBoundDate);
        int upperBoundYear = pUpperBoundDate.getYear();

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("  (select company.SOURCE_COMPANY_ID as srcCompanyId, employee.SOURCE_EMPLOYEE_ID as srcEmpId, elqt.QUARTER as qtr, elqt.YEAR as yr, \n")
                .append("      srcSystemLawAssoc.SOURCE_LAW_CODE as srcLawCd, elqt.TOTAL_WAGES as totalWages, elqt.TAXABLE_WAGES as taxableWages,  \n")
                .append("      elqt.TIPS_TAXABLE_WAGES_AMOUNT as tipsTaxableWageAmount, elqt.TAX_AMOUNT as taxAmount, CEIL(elqt.HOURS_WORKED) as hrsWorked, \n")
                .append("      elqt.WEEKS_WORKED as weeksWorked, elqt.MONTH_ONE_WORKED_INDICATOR as m1WI, elqt.MONTH_TWO_WORKED_INDICATOR as m2WI, \n")
                .append("      elqt.MONTH_THREE_WORKED_INDICATOR as m3WI, CEIL(elqt.HOURLY_RATE) as hourlyRate, '' as srcPayrollItemId \n")
                .append(" from PSP_EMPLOYEE_LAW_QTR_TOTALS elqt,\n")
                .append("      PSP_COMPANY company,PSP_EMPLOYEE employee \n")
                .append(" inner join PSP_INDIVIDUAL ind on employee.EMPLOYEE_SEQ=ind.INDIVIDUAL_SEQ, \n")
                .append("      PSP_SOURCE_SYSTEM_LAW_ASSOC srcSystemLawAssoc,PSP_COMPANY_LAW companylaw \n")
                .append(" where elqt.COMPANY_FK=company.COMPANY_SEQ and elqt.EMPLOYEE_FK=employee.EMPLOYEE_SEQ  \n" )
                .append("       and elqt.COMPANY_LAW_FK=companylaw.COMPANY_LAW_SEQ \n")
                .append("       and (elqt.YEAR >:lowerBoundYear or (elqt.YEAR =:lowerBoundYear and elqt.QUARTER >=:lowerBoundQuarter))  \n")
                .append("       and (elqt.YEAR <:upperBoundYear or (elqt.YEAR =:upperBoundYear and elqt.QUARTER <=:upperBoundQuarter))  \n")
                .append("       and srcSystemLawAssoc.LAW_FK=elqt.LAW_FK \n")
                .append("       and srcSystemLawAssoc.SOURCE_SYSTEM_FK='QBDT' and companylaw.IS_ARCHIVED=0 \n");

        if (pFromUpdateDate != null) {
            queryBuilder.append("       and elqt.MODIFIED_DATE >=:fromUpdatedDate   \n");
        }

        appendDGCheckCondition(queryBuilder, "company", Boolean.TRUE);

        queryBuilder.append(" UNION \n");

        queryBuilder.append("select company.SOURCE_COMPANY_ID as srcCompanyId, srcEmpId.SOURCE_EMPLOYEE_ID as srcEmpId, qet.QUARTER as qtr, qet.YEAR as yr,'' as srcLawCd, \n")
                .append("       sum(qet.TOTAL_WAGES) as totalWages, \n")
                .append("       sum(qet.TAXABLE_WAGES) as taxableWages, \n")
                .append("       sum(qet.TIPS_TAXABLE_WAGES_AMOUNT) as tipsTaxableWageAmount, \n")
                .append("       sum(qet.AMOUNT) as taxAmount, \n")
                .append("       0.0,0,0,0,0,0.0, srcPayrollItemId.SOURCE_PAYROLL_ITEM_ID as srcPayrollItemId  \n")
                .append("from   PSP_EE_PAYROLLITEM_QTRTOTALS qet ")
                .append("inner join PSP_EMPLOYEE srcEmpId on qet.EMPLOYEE_FK=srcEmpId.EMPLOYEE_SEQ \n")
                .append("inner join PSP_INDIVIDUAL ind on srcEmpId.EMPLOYEE_SEQ=ind.INDIVIDUAL_SEQ \n")
                .append("inner join PSP_COMPANY_PAYROLL_ITEM srcPayrollItemId on qet.COMPANY_PAYROLL_ITEM_FK=srcPayrollItemId.COMPANY_PAYROLL_ITEM_SEQ \n")
                .append("inner join PSP_COMPANY company on srcPayrollItemId.COMPANY_FK=company.COMPANY_SEQ \n")
                .append("where (qet.YEAR>:lowerBoundYear or( qet.YEAR =:lowerBoundYear and qet.QUARTER >=:lowerBoundQuarter)) \n")
                .append("       and (qet.YEAR <:upperBoundYear or (qet.YEAR = :upperBoundYear and qet.QUARTER <=:upperBoundQuarter)) \n");

        if (pFromUpdateDate != null) {
            queryBuilder.append("       and qet.MODIFIED_DATE >=:fromUpdatedDate \n");
        }

        appendDGCheckCondition(queryBuilder, "company", Boolean.TRUE);

        queryBuilder.append(" Group by company.SOURCE_COMPANY_ID, srcEmpId.SOURCE_EMPLOYEE_ID, srcPayrollItemId.SOURCE_PAYROLL_ITEM_ID, qet.QUARTER,qet.YEAR) \n");
        queryBuilder.append(" order by srcCompanyId, srcEmpId, srcLawCd  \n");

        Query query = Application.getHibernateSession().createSQLQuery(queryBuilder.toString());
        query.setParameter("lowerBoundQuarter", lowerBoundQuarter);
        query.setParameter("lowerBoundYear", lowerBoundYear);

        query.setParameter("upperBoundQuarter", upperBoundQuarter);
        query.setParameter("upperBoundYear", upperBoundYear);

        if (pFromUpdateDate != null) {
            Date date = CalendarUtils.convertLocalTimestamp(pFromUpdateDate.getTimeInMilliseconds());
            Timestamp fromUpdateDateTimestamp = new Timestamp(date.getTime());

            query.setParameter("fromUpdatedDate", fromUpdateDateTimestamp);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);

    }

    private ScrollableResults getEmployeeLawQtrTotals(int pYear, int pQuarter) {

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("(select company.SOURCE_COMPANY_ID as srcCompanyId, emp.SOURCE_EMPLOYEE_ID as srcEmpId, eetotals.QUARTER qtr,\n ")
                .append(" eetotals.YEAR yr, srcSystemLawAssoc.SOURCE_LAW_CODE srcLawCd,eetotals.TOTAL_WAGES totalWages, eetotals.TAXABLE_WAGES taxableWages, \n")
                .append(" eetotals.TIPS_TAXABLE_WAGES_AMOUNT tipsTaxableWageAmount, eetotals.TAX_AMOUNT taxAmount, CEIL(eetotals.HOURS_WORKED) hrsWorked,\n")
                .append(" eetotals.WEEKS_WORKED weeksWorked, eetotals.MONTH_ONE_WORKED_INDICATOR m1WI, eetotals.MONTH_TWO_WORKED_INDICATOR m2WI, \n")
                .append(" eetotals.MONTH_THREE_WORKED_INDICATOR m3WI, CEIL(eetotals.HOURLY_RATE) hourlyRate, ").append("''"  +" srcPayrollItemId \n")
                .append("from PSP_EMPLOYEE_LAW_QTR_TOTALS eetotals, \n")
                .append("       PSP_COMPANY company, PSP_EMPLOYEE emp \n")
                .append("inner join PSP_INDIVIDUAL ind on emp.EMPLOYEE_SEQ=ind.INDIVIDUAL_SEQ, \n")
                .append("       PSP_SOURCE_SYSTEM_LAW_ASSOC srcSystemLawAssoc, PSP_COMPANY_LAW companylaw  \n")
                .append(" where eetotals.COMPANY_FK=company.COMPANY_SEQ \n")
                .append("   and eetotals.EMPLOYEE_FK=emp.EMPLOYEE_SEQ  \n")
                .append("   and eetotals.COMPANY_LAW_FK=companylaw.COMPANY_LAW_SEQ \n")
                .append("   and eetotals.YEAR=:Year and eetotals.QUARTER=:Quarter \n")
                .append("   and srcSystemLawAssoc.LAW_FK=eetotals.LAW_FK  \n")
                .append("   and srcSystemLawAssoc.SOURCE_SYSTEM_FK='QBDT' \n")
                .append("   and companylaw.IS_ARCHIVED=0 \n");

        appendDGCheckCondition(queryBuilder, "company", Boolean.TRUE);

        queryBuilder.append(" UNION \n")

                .append(" select company.SOURCE_COMPANY_ID as srcCompanyId, emp.SOURCE_EMPLOYEE_ID srcEmpId, qet.QUARTER qtr, qet.YEAR yr, ").append("''" +" srcLawCd, \n")
                .append(" sum(qet.TOTAL_WAGES), \n")
                .append(" sum(qet.TAXABLE_WAGES),\n ")
                .append(" sum(qet.TIPS_TAXABLE_WAGES_AMOUNT), \n")
                .append(" sum(qet.AMOUNT), \n")
                .append("0.0,0,0,0,0,0.0, " ).append(" compPayrollItem.SOURCE_PAYROLL_ITEM_ID as srcPayrollItemId \n")
                .append("from   PSP_EE_PAYROLLITEM_QTRTOTALS qet \n")
                .append("inner join PSP_EMPLOYEE emp on qet.EMPLOYEE_FK=emp.EMPLOYEE_SEQ \n")
                .append("inner join PSP_INDIVIDUAL ind on emp.EMPLOYEE_SEQ=ind.INDIVIDUAL_SEQ \n")
                .append("inner join PSP_COMPANY_PAYROLL_ITEM compPayrollItem on qet.COMPANY_PAYROLL_ITEM_FK=compPayrollItem.COMPANY_PAYROLL_ITEM_SEQ  \n")
                .append("inner join PSP_COMPANY company on compPayrollItem.COMPANY_FK=company.COMPANY_SEQ \n")
                .append("where qet.YEAR=:Year and qet.QUARTER=:Quarter \n");

        appendDGCheckCondition(queryBuilder, "company", Boolean.TRUE);

        queryBuilder.append("group by company.SOURCE_COMPANY_ID , emp.SOURCE_EMPLOYEE_ID , compPayrollItem.SOURCE_PAYROLL_ITEM_ID,qet.QUARTER,qet.YEAR)  \n")
                .append("order by srcCompanyId, srcEmpId, srcLawCd, qtr  \n");
     
        SQLQuery sqlQuery = Application.getHibernateSession().createSQLQuery(queryBuilder.toString());
        sqlQuery.setParameter("Year",pYear);
        sqlQuery.setParameter("Quarter",pQuarter);

        return sqlQuery.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

}

