package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 *
 * User: mvillani
 * Date: Oct 8, 2007
 * Time: 11:53:20 AM

 */
public class EmployeeBankAccountDataLoader {

    public static Company LoadEmployeeBankAccounts(int pNumberOfEmployees, int pNumberofBankAccounts, String pEmployeeStatus, String pBankAccountStatus) {
        DataLoader dataLoader = new DataLoader();

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);

        // Create Employees and  Bank Accounts

        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        GenerateData.generateEmployees(company, pNumberOfEmployees);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), pNumberofBankAccounts, pBankAccountStatus);
        return company;

    }

    public static Company LoadInactiveCompany() {
        DataLoader dataLoader = new DataLoader();
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);

        // Create one Employee
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        GenerateData.generateEmployees(company, 1);

        // Deactivate company
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);

        assertSuccess("Cancel Service ", cancelServiceProcessResult);
        return company;
    }
}