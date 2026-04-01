package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: jpatel
 * Date: May 27, 2009
 */
@ScheduledJob(name = "ATFEmployeeInfoExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EmployeeInfoExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID = "EE_INFO";
    private static final String ATF_EE_STDTL_EXTRACT_TYPE_ID = "EE_STDTL";
    private static final String ATF_EE_MISC_EXTRACT_TYPE_ID = "EE_MISC";

    public EmployeeInfoExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public EmployeeInfoExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	mProcessCode = COMPARE_PROCESS_CODE;
        mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
        mExtractFileType = ATFDataExtractFileType.EmployeeInfo;
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

        SpcfCalendar lastRunDate = null;
        SpcfCalendar currentRunStartTime = null;
        ScrollableResults queryResults;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
            currentRunStartTime = mExtractBatch.getStartDate();
        }

        queryResults = findEmployeeInfoResults(lastRunDate, currentRunStartTime);
        try {
            int i = 0;
            while (queryResults.next()) {

                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee Info details");
                }

                pPW.print(DOUBLE_QUOTE);
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, (String) queryResults.get(0));

                // Employee ID
                writeFormatted(pPW, (String) queryResults.get(1));

                // Fed Tax ID/SSN
                String ssn = (String) queryResults.get(2);
                if(StringUtils.isNotEmpty(ssn))
                    ssn = EncryptionUtils.deterministicDecrypt(Employee.TaxIdKeyName, ssn);
                writeFormatted(pPW, ssn);

                // First Name
                writeFormatted(pPW, (String) queryResults.get(3));

                // Middle Initial
                writeFormatted(pPW, (String) queryResults.get(4));

                // Last Name
                writeFormatted(pPW, (String) queryResults.get(5));

                // Address Line 1
                writeFormatted(pPW, (String) queryResults.get(6));

                // Address Line 2
                writeFormatted(pPW, (String) queryResults.get(7));

                // City
                writeFormatted(pPW, (String) queryResults.get(8));

                // State
                writeFormatted(pPW, (String) queryResults.get(9));

                // Zip
                String zip = (String) queryResults.get(10);
                String zipExtension = (String) queryResults.get(11);
                String zipCode = "";

                if (zip != null) {
                    zipCode += zip;
                }

                if(zipExtension != null) {
                    zipCode += zipExtension;
                }
                writeFormatted(pPW, zipCode);

                // Gender
                if (Gender.Female.toString().equals(queryResults.get(12))) {
                    writeFormatted(pPW, "F");
                } else {
                    writeFormatted(pPW, "M");
                }

                //Hire Date
                String formattedHireDate = "";
                Timestamp hireDate = (Timestamp) queryResults.get(13);
                //Rehire date is used if rehired
                if (queryResults.get(14) != null) {
                    hireDate = (Timestamp) queryResults.get(14);
                }
                if (hireDate != null) {
                    formattedHireDate = StringFormatter.formatDate(SpcfCalendar.createInstance(hireDate.getTime()), "yyyyMMdd");
                }
                writeFormatted(pPW, formattedHireDate);

                // Termination Date
                String formattedTermDate = "";
                Timestamp termDate = (Timestamp) queryResults.get(15);
                if (termDate != null) {
                    formattedTermDate = StringFormatter.formatDate(SpcfCalendar.createInstance(termDate.getTime()), "yyyyMMdd");
                }
                writeFormatted(pPW, formattedTermDate);

                // Number of Exemptions
                int exemptions = 0;
                if (queryResults.get(22) != null) {
                        exemptions = ((BigDecimal) queryResults.get(22)).intValue();
                }
                writeFormatted(pPW, String.valueOf(exemptions));

                // State Lived
                writeFormatted(pPW, (String) queryResults.get(16));

                // State Worked
                writeFormatted(pPW, (String) queryResults.get(17));

                // Phone Number
                writeFormatted(pPW, (String) queryResults.get(18));

                // Is Statutory Y/N
                    writeFormatted(pPW, booleanToYesNo((BigDecimal) queryResults.get(19)));

                // Has Pension Plan  Y/N
                    writeFormatted(pPW, booleanToYesNo((BigDecimal) queryResults.get(20)));

                // Inactive - TRUE/FALSE
                String status = "TRUE"; // Inactive
                if (EmployeeStatus.Active.toString().equals(queryResults.get(21)) ) {
                    status = "FALSE";  // Active
                }


                writeFormatted(pPW, status);

                String formattedIsSeasonal = "";
                String isSeasonal = (String) queryResults.get(23);

                if (isSeasonal != null || StringUtils.isNotEmpty(isSeasonal)) {
                    formattedIsSeasonal = (String) queryResults.get(23);
                }

                writeFormatted(pPW, formattedIsSeasonal);

                //QB Unique Id
                writeFormatted(pPW, (String) queryResults.get(24),true);

                // Increment the record count for each record written in the extract file for the trailer record
                mRecordCount++;
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }

        writeEmployeeStateDetailInformation(lastRunDate, pPW, currentRunStartTime);
        writeEmployeeTaxDetailInformation(lastRunDate, pPW);
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {

        SpcfCalendar lastRunDate = null;
        SpcfCalendar currentRunStartTime = null;
        ScrollableResults queryResults;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
            currentRunStartTime = mExtractBatch.getStartDate();
        }

        queryResults = findEmployeeInfoResults(lastRunDate, currentRunStartTime);
        try {
            int i = 0;
            while (queryResults.next()) {

                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee Info details");
                }

                pPW.write(DOUBLE_QUOTE);
                pPW.write(ATF_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, (String) queryResults.get(0));

                // Employee ID
                writeFormatted(pPW, (String) queryResults.get(1));

                // Fed Tax ID/SSN
                String ssn = (String) queryResults.get(2);
                if(StringUtils.isNotEmpty(ssn))
                    ssn = EncryptionUtils.deterministicDecrypt(Employee.TaxIdKeyName, ssn);
                writeFormatted(pPW, ssn);

                // First Name
                writeFormatted(pPW, (String) queryResults.get(3));

                // Middle Initial
                writeFormatted(pPW, (String) queryResults.get(4));

                // Last Name
                writeFormatted(pPW, (String) queryResults.get(5));

                // Address Line 1
                writeFormatted(pPW, (String) queryResults.get(6));

                // Address Line 2
                writeFormatted(pPW, (String) queryResults.get(7));

                // City
                writeFormatted(pPW, (String) queryResults.get(8));

                // State
                writeFormatted(pPW, (String) queryResults.get(9));

                // Zip
                String zip = (String) queryResults.get(10);
                String zipExtension = (String) queryResults.get(11);
                String zipCode = "";

                if (zip != null) {
                    zipCode += zip;
                }

                if(zipExtension != null) {
                    zipCode += zipExtension;
                }
                writeFormatted(pPW, zipCode);

                // Gender
                if (Gender.Female.toString().equals(queryResults.get(12))) {
                    writeFormatted(pPW, "F");
                } else {
                    writeFormatted(pPW, "M");
                }

                //Hire Date
                String formattedHireDate = "";
                Timestamp hireDate = (Timestamp) queryResults.get(13);
                //Rehire date is used if rehired
                if (queryResults.get(14) != null) {
                    hireDate = (Timestamp) queryResults.get(14);
                }
                if (hireDate != null) {
                    formattedHireDate = StringFormatter.formatDate(SpcfCalendar.createInstance(hireDate.getTime()), "yyyyMMdd");
                }
                writeFormatted(pPW, formattedHireDate);

                // Termination Date
                String formattedTermDate = "";
                Timestamp termDate = (Timestamp) queryResults.get(15);
                if (termDate != null) {
                    formattedTermDate = StringFormatter.formatDate(SpcfCalendar.createInstance(termDate.getTime()), "yyyyMMdd");
                }
                writeFormatted(pPW, formattedTermDate);

                // Number of Exemptions
                int exemptions = 0;
                if (queryResults.get(22) != null) {
                        exemptions = ((BigDecimal) queryResults.get(22)).intValue();
                }
                writeFormatted(pPW, String.valueOf(exemptions));

                // State Lived
                writeFormatted(pPW, (String) queryResults.get(16));

                // State Worked
                writeFormatted(pPW, (String) queryResults.get(17));

                // Phone Number
                writeFormatted(pPW, (String) queryResults.get(18));

                // Is Statutory Y/N
                    writeFormatted(pPW, booleanToYesNo((BigDecimal) queryResults.get(19)));

                // Has Pension Plan  Y/N
                    writeFormatted(pPW, booleanToYesNo((BigDecimal) queryResults.get(20)));

                // Inactive - TRUE/FALSE
                String status = "TRUE"; // Inactive
                if (EmployeeStatus.Active.toString().equals(queryResults.get(21)) ) {
                    status = "FALSE";  // Active
                }

                writeFormatted(pPW, status);

                String formattedIsSeasonal = "";
                String isSeasonal = (String) queryResults.get(23);

                if (isSeasonal != null || StringUtils.isNotEmpty(isSeasonal)) {
                    formattedIsSeasonal = (String) queryResults.get(23);
                }

                writeFormatted(pPW, formattedIsSeasonal);

                //QB Unique Id
                writeFormatted(pPW, (String) queryResults.get(24),true);

                // Increment the record count for each record written in the extract file for the trailer record
                mRecordCount++;
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
            }
        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }

        writeEmployeeStateDetailInformation(lastRunDate, pPW, currentRunStartTime);
        writeEmployeeTaxDetailInformation(lastRunDate, pPW);
    }

    private static String booleanToYesNo(BigDecimal pValue) {
        if (pValue == null || pValue.intValue() == 0) {
            return "N";
        } else {
            return "Y";
        }
    }

    /**
     * @param startDate Only get records modified since this date
     * @return ScrollableResults
     */
    private static ScrollableResults findEmployeeInfoResults(SpcfCalendar startDate, SpcfCalendar currentRunStartTime) {

        //HQL is not working properly, Employee.EmployeeTaxSet is not loading always all the data, so updated to use SQL query.
        StringBuilder builder = new StringBuilder();
        builder.append("    select PC.SOURCE_COMPANY_ID, PE.SOURCE_EMPLOYEE_ID, PE.TAX_ID_ENC, PI.FIRST_NAME,\n" +
                "        PI.MIDDLE_NAME, PI.LAST_NAME, PA.ADDRESS_LINE1, PA.ADDRESS_LINE2, \n" +
                "        PA.CITY, PA.STATE, PA.ZIP_CODE, PA.ZIP_CODE_EXTENSION, \n" +
                "        PI.GENDER_CD, PE.HIRE_DATE, PE.RE_HIRE_DATE, PE.TERMINATION_DATE,\n" +
                "        PE.LIVE_STATE, PE.WORK_STATE, PI.PHONE, cast(PE.IS_STATUTORY as numeric(1,0)), cast(PE.HAS_RETIREMENT_PLAN as numeric(1,0)), PE.STATUS_CD,       \n" +
                "        cast(PE.fed_allowances as numeric(10,0)) , qei.EMPLOYEE_SEASONAL, qei.LIST_ID      \n" +
                "    from psp_employee pe\n" +
                "        join psp_company pc on PE.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "        join psp_company_service pcs on PCS.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "        join psp_tax_company_service_info ptcsinfo on PTCSINFO.TAX_COMPANY_SERVICE_INFO_SEQ = PCS.COMPANY_SERVICE_SEQ\n" +
                "        join psp_individual pi on PI.INDIVIDUAL_SEQ = PE.EMPLOYEE_SEQ\n" +
                "        join psp_address pa on PA.ADDRESS_SEQ = PI.MAILING_ADDRESS_FK\n" +
                "        join psp_qbdt_employee_info qei on pe.employee_seq = qei.employee_fk\n" +
                "    where \n" +
                "        qei.is_assisted=1  \n" +
                "        and ((PCS.STATUS_CD = 'ActiveCurrent' and PCS.SERVICE_START_DATE is not null)\n" +
                "                or (PCS.STATUS_CD in ('Cancelled','Terminated') and PTCSINFO.LAST_QUARTER_TO_FILE <> 0))\n");

        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and ((PI.MODIFIED_DATE >= :startDate and PI.MODIFIED_DATE < :currentRunStartTime)  or (PA.MODIFIED_DATE >= :startDate and PA.MODIFIED_DATE < :currentRunStartTime) \n"+
                    "    or PCS.STATUS_EFFECTIVE_DATE >= :startDate )" );
        }

        appendDGCheckCondition(builder, "PC", Boolean.TRUE);

        // Sort by Source ID, employee source ID to provide consistency for automated testing.
        builder.append(" order by PC.SOURCE_COMPANY_ID, PE.SOURCE_EMPLOYEE_ID \n");

        org.hibernate.Query query = Application.getHibernateSession().createSQLQuery(builder.toString());
        if (startDate != null) {
            Date date = CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds());
            Timestamp statTimeStamp = new Timestamp(date.getTime());

            query.setParameter("startDate", statTimeStamp);
        }
        if (currentRunStartTime != null) {
            Date date = CalendarUtils.convertLocalTimestamp(currentRunStartTime.getTimeInMilliseconds());
            Timestamp currentRunTimeStamp = new Timestamp(date.getTime());

            query.setParameter("currentRunStartTime", currentRunTimeStamp);
        }


        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private void writeEmployeeStateDetailInformation(SpcfCalendar pLastRunDate, PgpWriter pPW,  SpcfCalendar currentRunStartTime) throws Throwable {

        ScrollableResults queryResults;

        List<String> companyIds = new ArrayList<String>();

        queryResults = findEmployeeWagePlanDetails(pLastRunDate, companyIds,currentRunStartTime);

        try {
            int i = 0;
            String previousCompEmpId = null;
            List<Object[]> empWagePlans = new ArrayList<Object[]>();
            while (queryResults.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee State Wage Plan details..");
                }

                Object[] values = queryResults.get();

                String currentCompEmpId = "Comp:" + values[0] + "Emp:" + values[1];

                //There is a issue here -- Todo
                if (previousCompEmpId == null || previousCompEmpId.equals(currentCompEmpId)) {
                    previousCompEmpId = currentCompEmpId;
                    empWagePlans.add(values);
                } else {
                    previousCompEmpId = currentCompEmpId;
                    writeSingleEmployeeDetails(pPW, empWagePlans);
                    empWagePlans.clear();
                    empWagePlans.add(values);
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
            }

            writeSingleEmployeeDetails(pPW, empWagePlans);

            // Write Employee wage plan records with companies that have all employee wage plan records deleted

            for (String companyId : companyIds) {
                writeEmptySingleEmployeeDetails(pPW, companyId);
            }

        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }
    }

    private void writeEmployeeStateDetailInformation(SpcfCalendar pLastRunDate, PrintWriter pPW,  SpcfCalendar currentRunStartTime) throws Throwable {

        ScrollableResults queryResults;

        List<String> companyIds = new ArrayList<String>();

        queryResults = findEmployeeWagePlanDetails(pLastRunDate, companyIds,currentRunStartTime);

        try {
            int i = 0;
            String previousCompEmpId = null;
            List<Object[]> empWagePlans = new ArrayList<Object[]>();
            while (queryResults.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee State Wage Plan details..");
                }

                Object[] values = queryResults.get();

                String currentCompEmpId = "Comp:" + values[0] + "Emp:" + values[1];

                //There is a issue here -- Todo
                if (previousCompEmpId == null || previousCompEmpId.equals(currentCompEmpId)) {
                    previousCompEmpId = currentCompEmpId;
                    empWagePlans.add(values);
                } else {
                    previousCompEmpId = currentCompEmpId;
                    writeSingleEmployeeDetails(pPW, empWagePlans);
                    empWagePlans.clear();
                    empWagePlans.add(values);
                }

                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
            }

            writeSingleEmployeeDetails(pPW, empWagePlans);

            // Write Employee wage plan records with companies that have all employee wage plan records deleted

            for (String companyId : companyIds) {
                writeEmptySingleEmployeeDetails(pPW, companyId);
            }

        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }
    }

    /**
     * @param startDate Only get records modified since this date
     * @return ScrollableResults
     */
    private static ScrollableResults findEmployeeWagePlanDetails(SpcfCalendar startDate, List<String> pCompanyIds,  SpcfCalendar currentRunStartTime) {

        //HQL is not working properly, Employee.EmployeeWagePlanSet is not loading always all the data, using SQL query.

        // Get the list of companies without any valid employee_wage_plan records, i.e. (no records with invalid_date is null for company)
        StringBuilder builder = new StringBuilder();
        builder.append("select distinct pc.source_company_id\n" +
                "   from psp_employee pe\n" +
                "       join psp_employee_wage_plan pwp on pe.employee_seq = pwp.employee_fk\n" +
                "       join psp_company pc on PE.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "       join psp_company_service pcs on PCS.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "       join psp_tax_company_service_info ptcsinfo on PTCSINFO.TAX_COMPANY_SERVICE_INFO_SEQ = PCS.COMPANY_SERVICE_SEQ       \n" +
                "       join psp_qbdt_employee_info qei on pe.employee_seq = qei.employee_fk\n" +
                "  where \n" +
                "       qei.is_assisted=1  \n" +
                "       and ((PCS.STATUS_CD = 'ActiveCurrent' and PCS.SERVICE_START_DATE is not null)\n" +
                "               or (PCS.STATUS_CD in ('Cancelled','Terminated') and PTCSINFO.LAST_QUARTER_TO_FILE <> 0)) \n" +
                "       and not exists (select '1' from psp_employee_wage_plan ipwp \n" +
                "                          join psp_employee ipe on ipe.employee_seq = ipwp.employee_fk\n" +
                "                          where ipe.company_fk = pc.company_seq\n" +
                "                            and ipwp.invalid_date is null) ");

        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and pwp.MODIFIED_DATE >= :startDate and pwp.MODIFIED_DATE < :currentRunStartTime \n");
        }

        appendDGCheckCondition(builder, "PC", Boolean.TRUE);

        // Sort by Source ID
        builder.append(" order by  PC.SOURCE_COMPANY_ID ");

        Query query = Application.getHibernateSession().createSQLQuery(builder.toString());

        if (startDate != null) {
            Date date = CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds());
            Timestamp statTimeStamp = new Timestamp(date.getTime());

            query.setParameter("startDate", statTimeStamp);
        }
        if (currentRunStartTime != null) {
            Date date = CalendarUtils.convertLocalTimestamp(currentRunStartTime.getTimeInMilliseconds());
            Timestamp currentRunTimeStamp = new Timestamp(date.getTime());

            query.setParameter("currentRunStartTime", currentRunTimeStamp);
        }
        //noinspection unchecked
        List<String> companyIds = query.list();
        if(companyIds != null) {
            pCompanyIds.addAll(companyIds);
        }

        StringBuilder wagePlanQueryBuilder = new StringBuilder();

        wagePlanQueryBuilder.append("select opc.source_company_id, ope.source_employee_id, opwp.wage_plan_domain, opwp.state, opwp.name, opwp.wage_plan_value, opwp.description, qei.list_id \n" +
                "from psp_employee ope\n" +
                "       join psp_employee_wage_plan opwp on ope.employee_seq = opwp.employee_fk\n" +
                "       join psp_qbdt_employee_info qei on qei.employee_fk = ope.employee_seq\n" +
                "       join psp_company opc on ope.COMPANY_FK = opc.COMPANY_SEQ\n" +
                "where \n" +
                "    opwp.invalid_date is null\n" +
                "  and opc.company_seq in (       \n" +
                "          select distinct pc.company_seq\n" +
                "             from psp_employee pe\n" +
                "                 join psp_employee_wage_plan pwp on pe.employee_seq = pwp.employee_fk\n" +
                "                 join psp_company pc on PE.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "                 join psp_company_service pcs on PCS.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "                 join psp_tax_company_service_info ptcsinfo on PTCSINFO.TAX_COMPANY_SERVICE_INFO_SEQ = PCS.COMPANY_SERVICE_SEQ\n" +
                "                 join psp_qbdt_employee_info qei on pe.employee_seq = qei.employee_fk\n" +
                "            where \n" +
                "                 qei.is_assisted=1  \n" +
                "                 and ((PCS.STATUS_CD = 'ActiveCurrent' and PCS.SERVICE_START_DATE is not null)\n" +
                "                         or (PCS.STATUS_CD in ('Cancelled','Terminated') and PTCSINFO.LAST_QUARTER_TO_FILE <> 0)) \n" +
                "                 and pwp.invalid_date is null ");

        // Only check modified dates during incremental updates.
        if (startDate != null) {
            wagePlanQueryBuilder.append("    and pwp.MODIFIED_DATE >= :startDate \n");
        }

        appendDGCheckCondition(builder, "pc", Boolean.TRUE);

        //Complete inner query
        wagePlanQueryBuilder.append(" ) ");

        // Sort by Source ID, employee source ID to provide consistency for automated testing.
        wagePlanQueryBuilder.append(" order by opc.SOURCE_COMPANY_ID, ope.SOURCE_EMPLOYEE_ID, opwp.wage_plan_value ");

        Query wagePlanQuery = Application.getHibernateSession().createSQLQuery(wagePlanQueryBuilder.toString());

        if (startDate != null) {
            Date date = CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds());
            Timestamp statTimeStamp = new Timestamp(date.getTime());

            wagePlanQuery.setParameter("startDate", statTimeStamp);
        }

        return wagePlanQuery.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }

    private void writeSingleEmployeeDetails(PgpWriter pPW, List<Object[]> pValuesList) throws Throwable {

        int employeeWagePlanCount = pValuesList.size();
        for (Object[] values : pValuesList) {

            // Add the state detail to the EE_INFO file for each employee
            pPW.write(DOUBLE_QUOTE);
            pPW.write(ATF_EE_STDTL_EXTRACT_TYPE_ID + DELIMITER);

            //Source Company ID
            writeFormatted(pPW, (String) values[0]);

            //Employee ID
            writeFormatted(pPW, (String) values[1]);

            //Domain
            writeFormatted(pPW, (String) values[2]);

            //State
            writeFormatted(pPW, (String) values[3]);

            //Name
            writeFormatted(pPW, (String) values[4]);

            //Wage plan Value
            writeFormatted(pPW, (String) values[5]);

            //Description
            writeFormatted(pPW, (String) values[6]);

            //QB Unique Id
            writeFormatted(pPW, (String) values[7]);

            //Count
            writeFormatted(pPW, String.valueOf(employeeWagePlanCount), true);

            // increment the record count for each record written in the extract file for the trailer record
            mRecordCount++;
        }
    }

    private void writeSingleEmployeeDetails(PrintWriter pPW, List<Object[]> pValuesList) throws Throwable {

        int employeeWagePlanCount = pValuesList.size();
        for (Object[] values : pValuesList) {

            // Add the state detail to the EE_INFO file for each employee
            pPW.print(DOUBLE_QUOTE);
            pPW.print(ATF_EE_STDTL_EXTRACT_TYPE_ID + DELIMITER);

            //Source Company ID
            writeFormatted(pPW, (String) values[0]);

            //Employee ID
            writeFormatted(pPW, (String) values[1]);

            //Domain
            writeFormatted(pPW, (String) values[2]);

            //State
            writeFormatted(pPW, (String) values[3]);

            //Name
            writeFormatted(pPW, (String) values[4]);

            //Wage plan Value
            writeFormatted(pPW, (String) values[5]);

            //Description
            writeFormatted(pPW, (String) values[6]);

            //QB Unique Id
            writeFormatted(pPW, (String) values[7]);

            //Count
            writeFormatted(pPW, String.valueOf(employeeWagePlanCount), true);

            // increment the record count for each record written in the extract file for the trailer record
            mRecordCount++;
        }
    }

    private void writeEmptySingleEmployeeDetails(PrintWriter pPW, String companyId) throws Throwable {

        // Add the state detail to the EE_INFO file for each employee
        pPW.print(DOUBLE_QUOTE);
        pPW.print(ATF_EE_STDTL_EXTRACT_TYPE_ID + DELIMITER);

        //Source Company ID
        writeFormatted(pPW, companyId);

        //Employee ID
        writeFormatted(pPW, "");

        //Domain
        writeFormatted(pPW, "");

        //State
        writeFormatted(pPW, "");

        //Name
        writeFormatted(pPW, "");

        //Wage plan Value
        writeFormatted(pPW, "");

        //Description
        writeFormatted(pPW, "");

        //Qb Unique Id
        writeFormatted(pPW, "");

        //Count
        writeFormatted(pPW, "0", true);

        // increment the record count for each record written in the extract file for the trailer record
        mRecordCount++;

    }

    private void writeEmptySingleEmployeeDetails(PgpWriter pPW, String companyId) throws Throwable {

        // Add the state detail to the EE_INFO file for each employee
        pPW.write(DOUBLE_QUOTE);
        pPW.write(ATF_EE_STDTL_EXTRACT_TYPE_ID + DELIMITER);

        //Source Company ID
        writeFormatted(pPW, companyId);

        //Employee ID
        writeFormatted(pPW, "");

        //Domain
        writeFormatted(pPW, "");

        //State
        writeFormatted(pPW, "");

        //Name
        writeFormatted(pPW, "");

        //Wage plan Value
        writeFormatted(pPW, "");

        //Description
        writeFormatted(pPW, "");

        //QB Unique Id
        writeFormatted(pPW, "");

        //Count
        writeFormatted(pPW, "0", true);

        // increment the record count for each record written in the extract file for the trailer record
        mRecordCount++;

    }
    private static ScrollableResults findEmployeeTaxMiscResults(SpcfCalendar startDate) {
        //HQL is not working properly, Employee.EmployeeTaxSet is not loading always all the data, so updated to use SQL query.
        StringBuilder builder = new StringBuilder();
        builder.append("    SELECT PC.SOURCE_COMPANY_ID  AS PSID,\n" +
                "       PE.SOURCE_EMPLOYEE_ID AS EMPLOYEE_ID, \n" +
                "        PCL.LAW_FK AS LAW_ID, \n" +
                "        MD.VALUE AS VALUE,   \n" +
                "        QEI.LIST_ID AS QB_UNIQUE_ID    \n" +
                "     FROM PSP_TAX_TABLE_MISC_DATA MD \n" +
                "         JOIN PSP_EMPLOYEE_TAX PET ON MD.EMPLOYEE_TAX_FK = PET.EMPLOYEE_TAX_SEQ \n" +
                "        JOIN PSP_COMPANY_LAW PCL ON PET.COMPANY_LAW_FK = PCL.COMPANY_LAW_SEQ \n" +
                "        JOIN PSP_EMPLOYEE PE ON PET.EMPLOYEE_FK = PE.EMPLOYEE_SEQ \n" +
                "        JOIN PSP_QBDT_EMPLOYEE_INFO QEI ON QEI.EMPLOYEE_FK = PE.EMPLOYEE_SEQ \n" +
                "        JOIN PSP_COMPANY PC ON PE.COMPANY_FK = PC.COMPANY_SEQ\n" +
                "         JOIN PSP_COMPANY_SERVICE PCS ON PC.COMPANY_SEQ = PCS.COMPANY_FK \n" +
                "       JOIN PSP_TAX_COMPANY_SERVICE_INFO PTCSI ON PCS.COMPANY_SERVICE_SEQ = PTCSI.TAX_COMPANY_SERVICE_INFO_SEQ \n" +
                "    WHERE MD.VALUE IS NOT NULL \n" +
                "        AND ((PCS.STATUS_CD = 'ActiveCurrent' AND PCS.SERVICE_START_DATE IS NOT NULL) \n" +
                "                 OR (PCS.STATUS_CD IN ('Cancelled', 'Terminated') AND PTCSI.LAST_QUARTER_TO_FILE <> 0))\n");

        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("      AND MD.MODIFIED_DATE >= :startDate  \n" );
        }

        appendDGCheckCondition(builder, "PC", Boolean.TRUE);

        // Sort by Source ID, employee source ID to provide consistency for automated testing.
        builder.append(" order by PC.SOURCE_COMPANY_ID, PE.SOURCE_EMPLOYEE_ID \n");
        org.hibernate.Query query = Application.getHibernateSession().createSQLQuery(builder.toString());
        if (startDate != null) {
            Date date = CalendarUtils.convertLocalTimestamp(startDate.getTimeInMilliseconds());
            Timestamp statTimeStamp = new Timestamp(date.getTime());

            query.setParameter("startDate", statTimeStamp);
        }
        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }
    private void writeEmployeeTaxDetailInformation(SpcfCalendar pLastRunDate, PgpWriter pPW) throws Throwable {

        ScrollableResults queryResults;

        queryResults = findEmployeeTaxMiscResults(pLastRunDate);

        try {
            int i = 0;
            while (queryResults.next()) {
                getLogger().info("started processing for Employee Tax details..");
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee Tax details..");
                }
                pPW.write(DOUBLE_QUOTE);
                pPW.write(ATF_EE_MISC_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, (String) queryResults.get(0));

                // Employee ID
                writeFormatted(pPW, (String) queryResults.get(1));

                // LAW  ID
                writeFormatted(pPW, (String) queryResults.get(2));

                // LAW VALUE
                writeFormatted(pPW, (String) queryResults.get(3));

                // QB Unique Id
                writeFormatted(pPW, (String) queryResults.get(4), true);

                // Increment the record count for each record written in the extract file for the trailer record
                mRecordCount++;
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
                // To allow for unlimited size result sets, we need to keep the cache clean.
            }

        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }
    }
    private void writeEmployeeTaxDetailInformation(SpcfCalendar pLastRunDate, PrintWriter pPW) throws Throwable {

        ScrollableResults queryResults;

        queryResults = findEmployeeTaxMiscResults(pLastRunDate);

        try {
            int i = 0;
            while (queryResults.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Employee Tax details..");
                }
                pPW.print(DOUBLE_QUOTE);
                pPW.print(ATF_EE_MISC_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, (String) queryResults.get(0));

                // Employee ID
                writeFormatted(pPW, (String) queryResults.get(1));

                // LAW  ID
                writeFormatted(pPW, (String) queryResults.get(2));

                // LAW VALUE
                writeFormatted(pPW, (String) queryResults.get(3));

                // QB Unique Id
                writeFormatted(pPW, (String) queryResults.get(4), true);

                // Increment the record count for each record written in the extract file for the trailer record
                mRecordCount++;
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(queryResults.get());
                // To allow for unlimited size result sets, we need to keep the cache clean.
            }

        } catch (GenericJDBCException ex) {
            // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
            if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (queryResults != null) {
                queryResults.close();
            }
        }
    }

}
