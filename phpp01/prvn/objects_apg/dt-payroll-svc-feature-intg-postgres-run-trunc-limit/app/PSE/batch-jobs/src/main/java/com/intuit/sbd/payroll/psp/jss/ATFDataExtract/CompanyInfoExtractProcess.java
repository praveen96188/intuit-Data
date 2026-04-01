package com.intuit.sbd.payroll.psp.jss.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractFileType;
import com.intuit.sbd.payroll.psp.domain.ATFDataExtractRunType;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.io.PrintWriter;


/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 22, 2009
 */
@ScheduledJob(name="ATFCompanyInfoExtract", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class CompanyInfoExtractProcess extends BaseATFExtractFileProcess {

    private static final String ATF_EXTRACT_TYPE_ID ="CO_INFO";

    public CompanyInfoExtractProcess(String[] pArguments) {
        super(pArguments);
        init();
    }

    public CompanyInfoExtractProcess(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        init();
    }
    
    private void init(){
    	 mProcessCode =  COMPARE_PROCESS_CODE;
         mATFExtractTypeID = ATF_EXTRACT_TYPE_ID;
         mExtractFileType = ATFDataExtractFileType.CompanyInfo;
    }

    public void execute() {
        super.execute();
    }

    /**
     * This method writes the actual company info details.
     * @param pPW PrintWriter
     *
     */
    @Override
    protected void writeData(PrintWriter pPW) throws Throwable {

        SpcfCalendar lastRunDate = null;
        ScrollableResults queryResults;
        Company company;
        Address address;
        ServiceSubStatusCode serviceStatusCd;
        Integer lastTaxQuarter;
        Boolean finalAnnualReturnsFlag;
        Boolean fileAnnualReturnsFlag;
        SpcfCalendar serviceStartDate;
        SpcfCalendar lastPayrollDate;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
        }

        queryResults = findCompanyInfoResults(lastRunDate);

        try {
            int i = 0;

            while (queryResults.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Company Info details..");
                }

                Object[] values = (Object[]) queryResults.get(0);
                company = (Company) values[0];

                address = company.getLegalAddress();
                serviceStatusCd = (ServiceSubStatusCode) values[1];
                lastTaxQuarter = (Integer) values[2];
                finalAnnualReturnsFlag = (Boolean) values[3];
                fileAnnualReturnsFlag = (Boolean) values[4];
                serviceStartDate = (SpcfCalendar) values[5];
                lastPayrollDate = (SpcfCalendar) values[6];

                pPW.print(DOUBLE_QUOTE);
                pPW.print(ATF_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, company.getSourceCompanyId());

                // FEIN (Fed Tax ID)
                writeFormatted(pPW, company.getFedTaxId());

                // Company Name - Use Legal Name
                writeFormatted(pPW, company.getLegalName());

                // Legal Name
                writeFormatted(pPW, company.getLegalName());

                // Legal Address Line 1
                writeFormatted(pPW, address.getAddressLine1());

                // Legal Address Line 2
                writeFormatted(pPW, address.getAddressLine2());

                // Legal CITY
                writeFormatted(pPW, address.getCity());

                // Legal State
                writeFormatted(pPW, address.getState());

                // Zip code (with +4 if available)
                String zipCode = "";
                if(address.getZipCode() != null) {
                    zipCode += address.getZipCode();
                }

                if(address.getZipCodeExtension() != null) {
                    zipCode += address.getZipCodeExtension();
                }

                writeFormatted(pPW, zipCode);

                // Phone Number
                writeFormatted(pPW, company.getPhone());

                // Fax Number  -- Not sent by PSP according to design
                writeFormatted(pPW, null);

                // Email Address (Blank per spec.)
                writeFormatted(pPW, null);

                // Seasonal Employer (Blank per spec.)
                writeFormatted(pPW, null);

                // Company Status
                String companyStatus = "INACTIVE";
                if (company.isCompanyOnHold() && serviceStatusCd.notIn(ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated)) {
                    companyStatus = "ON_HOLD";
                } else if (serviceStatusCd == ServiceSubStatusCode.ActiveCurrent) {
                    companyStatus = "ACTIVE";
                }

                writeFormatted(pPW, companyStatus);

                // First Filings Quarter
                String formattedYearQuarter = "" + serviceStartDate.getYear() + CalendarUtils.getQuarterAsInt(serviceStartDate);
                writeFormatted(pPW, formattedYearQuarter);

                // Last filings Quarter - YYYYQ
                String filingQuarter = null;
                // A zero is essentially a null value on the AS/400.
                if (lastTaxQuarter != null && !lastTaxQuarter.equals(TaxCompanyServiceInfo.LAST_TAX_QUARTER_NULL) && !lastTaxQuarter.equals(TaxCompanyServiceInfo.LAST_TAX_QUARTER_DO_NOT_FILE)) {
                    filingQuarter = lastTaxQuarter.toString();
                }
                writeFormatted(pPW, filingQuarter);

                // GenerateAnnualForm - File Annual Returns flag
                writeFormatted(pPW, fileAnnualReturnsFlag ? "Y" : "");

                // Final Annual Returns Flag
                writeFormatted(pPW, finalAnnualReturnsFlag ? "Y" : "");

                // Final payroll run date
                String finalPayrollDate = null;
                if (lastPayrollDate != null) {
                    finalPayrollDate = lastPayrollDate.format("yyyyMMdd");
                }
                writeFormatted(pPW, finalPayrollDate, true);

                // Increment the record count for each record written in the extract file for the trailer record.
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
    }

    @Override
    protected void writeData(PgpWriter pPW) throws Throwable {

        SpcfCalendar lastRunDate = null;
        ScrollableResults queryResults;
        Company company;
        Address address;
        ServiceSubStatusCode serviceStatusCd;
        Integer lastTaxQuarter;
        Boolean finalAnnualReturnsFlag;
        Boolean fileAnnualReturnsFlag;
        SpcfCalendar serviceStartDate;
        SpcfCalendar lastPayrollDate;

        // For updated data, use the last successful run date as the start date.
        if (mExtractBatch.getRunType().equals(ATFDataExtractRunType.UpdatedData)) {
            lastRunDate = super.getLastSuccessfulExtractBatchStartdate(false).toLocal();
        }

        queryResults = findCompanyInfoResults(lastRunDate);

        try {
            int i = 0;

            while (queryResults.next()) {
                i++;
                if (i % 1000 == 0) {
                    getLogger().info("Completed processing for " + i + " Company Info details..");
                }

                Object[] values = (Object[]) queryResults.get(0);
                company = (Company) values[0];

                address = company.getLegalAddress();
                serviceStatusCd = (ServiceSubStatusCode) values[1];
                lastTaxQuarter = (Integer) values[2];
                finalAnnualReturnsFlag = (Boolean) values[3];
                fileAnnualReturnsFlag = (Boolean) values[4];
                serviceStartDate = (SpcfCalendar) values[5];
                lastPayrollDate = (SpcfCalendar) values[6];

                pPW.write(DOUBLE_QUOTE);
                pPW.write(ATF_EXTRACT_TYPE_ID + DELIMITER);

                // Source Company ID
                writeFormatted(pPW, company.getSourceCompanyId());

                // FEIN (Fed Tax ID)
                writeFormatted(pPW, company.getFedTaxId());

                // Company Name - Use Legal Name
                writeFormatted(pPW, company.getLegalName());

                // Legal Name
                writeFormatted(pPW, company.getLegalName());

                // Legal Address Line 1
                writeFormatted(pPW, address.getAddressLine1());

                // Legal Address Line 2
                writeFormatted(pPW, address.getAddressLine2());

                // Legal CITY
                writeFormatted(pPW, address.getCity());

                // Legal State
                writeFormatted(pPW, address.getState());

                // Zip code (with +4 if available)
                String zipCode = "";
                if(address.getZipCode() != null) {
                    zipCode += address.getZipCode();
                }

                if(address.getZipCodeExtension() != null) {
                    zipCode += address.getZipCodeExtension();
                }

                writeFormatted(pPW, zipCode);

                // Phone Number
                writeFormatted(pPW, company.getPhone());

                // Fax Number  -- Not sent by PSP according to design
                writeFormatted(pPW, null);

                // Email Address (Blank per spec.)
                writeFormatted(pPW, null);

                // Seasonal Employer (Blank per spec.)
                writeFormatted(pPW, null);

                // Company Status
                String companyStatus = "INACTIVE";
                if (company.isCompanyOnHold() && serviceStatusCd.notIn(ServiceSubStatusCode.Cancelled, ServiceSubStatusCode.Terminated)) {
                    companyStatus = "ON_HOLD";
                } else if (serviceStatusCd == ServiceSubStatusCode.ActiveCurrent) {
                    companyStatus = "ACTIVE";
                }

                writeFormatted(pPW, companyStatus);

                // First Filings Quarter
                String formattedYearQuarter = "" + serviceStartDate.getYear() + CalendarUtils.getQuarterAsInt(serviceStartDate);
                writeFormatted(pPW, formattedYearQuarter);

                // Last filings Quarter - YYYYQ
                String filingQuarter = null;
                // A zero is essentially a null value on the AS/400.
                if (lastTaxQuarter != null && !lastTaxQuarter.equals(TaxCompanyServiceInfo.LAST_TAX_QUARTER_NULL) && !lastTaxQuarter.equals(TaxCompanyServiceInfo.LAST_TAX_QUARTER_DO_NOT_FILE)) {
                    filingQuarter = lastTaxQuarter.toString();
                }
                writeFormatted(pPW, filingQuarter);

                // GenerateAnnualForm - File Annual Returns flag
                writeFormatted(pPW, fileAnnualReturnsFlag ? "Y" : "");

                // Final Annual Returns Flag
                writeFormatted(pPW, finalAnnualReturnsFlag ? "Y" : "");

                // Final payroll run date
                String finalPayrollDate = null;
                if (lastPayrollDate != null) {
                    finalPayrollDate = lastPayrollDate.format("yyyyMMdd");
                }
                writeFormatted(pPW, finalPayrollDate, true);

                // Increment the record count for each record written in the extract file for the trailer record.
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
    }


    private static ScrollableResults findCompanyInfoResults(SpcfCalendar startDate) {

        StringBuilder builder = new StringBuilder();

        // Query for Company Info.
        builder.append(" select co, serviceInfo.StatusCd, serviceInfo.LastQuarterToFile, \n")
                .append("   serviceInfo.FinalAnnualReturns, serviceInfo.FileAnnualReturns, serviceInfo.ServiceStartDate, serviceInfo.LastPayrollDate \n")
                .append(" from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo serviceInfo \n")
                .append("    join serviceInfo.Company co \n")
                .append("    join fetch co.LegalAddress address \n")
                .append("    left join fetch co.OnHoldReasonSet reason \n")
                .append(" where \n")
                .append("    serviceInfo.ServiceStartDate is not null and serviceInfo.StatusCd in ('ActiveCurrent', 'Cancelled','Terminated') \n");


        // Only check modified dates during incremental updates.
        if (startDate != null) {
            builder.append("    and (co.ModifiedDate >= :startDate or address.ModifiedDate >= :startDate \n")
                    .append("       or serviceInfo.ModifiedDate >= :startDate or reason.ModifiedDate >= :startDate) \n");
        }

        appendDGCheckCondition(builder,"co", Boolean.FALSE);

        // Sort by PSId to provide consistency for automated testing.
        builder.append(" order by co.SourceCompanyId \n");

        org.hibernate.Query query = Application.createHibernateQuery(builder.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }

        return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
    }


}
