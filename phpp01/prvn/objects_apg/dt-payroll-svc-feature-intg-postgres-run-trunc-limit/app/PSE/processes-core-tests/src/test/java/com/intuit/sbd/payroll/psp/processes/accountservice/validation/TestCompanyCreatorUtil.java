package com.intuit.sbd.payroll.psp.processes.accountservice.validation;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;

public class TestCompanyCreatorUtil {

    public static String createCompanyWithAdditionalInfo() {
        String sourceCompanyId = null;
        try {
            Company newCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
            sourceCompanyId = newCompany.getSourceCompanyId();



            CompanyAdditionalInfo companyAdditionalInfo = new CompanyAdditionalInfo();
            IndustryType industryType =  IndustryType.findIndustryType("Accounting or Bookkeeping");
            //      industryType.setIndustry("Accounting, Auditing, and Bookkeeping Services");
            industryType.setStandardIndustryCode("8931");


            companyAdditionalInfo.setIndustryType(industryType);

            Application.beginUnitOfWork();
            Company company = Company.findCompany(newCompany.getSourceCompanyId(), SourceSystemCode.QBDT);
            companyAdditionalInfo.setCompany(company);
            Application.save(companyAdditionalInfo);
            company.setCompanyAdditionalInfo(companyAdditionalInfo);

            Application.save(company);
            Application.commitUnitOfWork();

            System.out.println("The industry  type code is "+company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());

        } catch (Exception e) {
            e.printStackTrace();
            Application.rollbackUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        return sourceCompanyId;
    }
}
