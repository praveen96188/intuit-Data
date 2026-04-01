/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * AddEscalationProcessDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddEscalationProcessDataLoader {
    public static Company sCompany;
    public static PayrollRun sPayrollRun;

    public static void loadPayrollRunForAddEscalationTest()
    {
        Application.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));

        DataLoader loader = new DataLoader();

        sCompany = loader.persistTestActiveCompany();
        // Create Company Service - Direct Deposit
        loader.persistTestCompanyService(sCompany);
        TransactionReverseCoreDataLoader.sCompanyBankAccount = loader.persistCompanyBankAccount(sCompany, loader.getTestCompanyBankAccount());
        sCompany = Application.refresh(sCompany);

        // Create Employees and Employee Bank Accounts
        GenerateData.generateEmployees(sCompany, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(sCompany.getDirectDepositEmployees()), 1, "Active");

        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        sCompany = Company.findCompany(sCompany.getSourceCompanyId(), sCompany.getSourceSystemCd());
        TransactionReverseCoreDataLoader.sCompanyBankAccount = Application.findById(CompanyBankAccount.class, TransactionReverseCoreDataLoader.sCompanyBankAccount.getId());
        PayrollSubmitDataLoader otherLoader = new PayrollSubmitDataLoader();
        PayrollRunDTO dto = otherLoader.createPayrollRunDTO(sCompany, TransactionReverseCoreDataLoader.sCompanyBankAccount, "MyBatch1");
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(sCompany.getSourceSystemCd(), sCompany.getSourceCompanyId(), dto);

        assertSuccess("PayrollRun submitted", result);

        sPayrollRun = result.getResult();

        Application.commitUnitOfWork();
    }

}
