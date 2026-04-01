package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Hand-written business logic
 */
public class ATFDataExtractBatch extends BaseATFDataExtractBatch {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ATFDataExtractBatch()
	{
		super();
	}

    public DomainEntitySet<ATFDataExtractFile> getATFDataExtractFilesToFTP() {
        DomainEntitySet<ATFDataExtractFile> dataExtractFiles = new DomainEntitySet<ATFDataExtractFile>();

        for (ATFDataExtractFile atfDataExtractFile : getATFDataExtractFileCollection()) {
            switch(atfDataExtractFile.getFileType()) {
                case CompanyInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_COMPANY_INFO_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case CompanyTaxInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_COMPANY_TAX_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case CompanyTaxRateInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_COMPANY_TAX_RATE_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case CompanyDepFreqInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_DEPOSIT_FREQUENCY_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case EmployeeInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_EMPLOYEE_INFO_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case EmployeeTotalsInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case CompanyPayrollItemInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_COMPANY_INFO_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case WageLimitsInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_WAGE_LIMITS_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                case W2CountInfo:
                    if(SystemParameter.findBooleanValue(SystemParameter.Code.FTP_ATF_W2_COUNT_INFO_EXTRACT_FILE, false)){
                        dataExtractFiles.add(atfDataExtractFile);
                    }
                    break;
                //Below 2 types are tested and in production
                case CompanyLiabilitiesInfo:
                case CompanyPaymentsInfo:
                    dataExtractFiles.add(atfDataExtractFile);
            }
        }
        return dataExtractFiles;
    }
}