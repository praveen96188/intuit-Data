package com.intuit.sbd.payroll.psp.jss.processors.workerscomp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SubsTypeCode;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestUtils {
    public static Company createCompanyEmployeesAndComplexPayroll(int noOfEmployees, boolean voidPaychecks) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        List<Employee> employees = DataLoadServices.addEEs(company, noOfEmployees, true, true);

        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        if (companyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = DataLoadServices.createCompanyBankAccount(companyBankAccount);
            companyBankAccounts.add(DataLoadServices.createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
            payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }

        SpcfCalendar payrollDate = PSPDate.getPSPTime();
        payrollDate.addDays(2);
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(payrollDate));
        payrollRunDTO.setPayrollTXBatchId("Batch_DD_401k");

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : employees) {
            employee = Employee.findEmployee(company, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                ddTransactions.add(DataLoadServices.createDDTransactionDTO(DataLoadServices.createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i+1)));
            }



            // Create Paycheck
            PaycheckDTO paycheckDTO = new PaycheckDTO();
            paycheckDTO.setDdTransactions((List<DDTransactionDTO>)ddTransactions);
            paycheckDTO.setEmployeeId(employee.getSourceEmployeeId());
            paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
            for (DDTransactionDTO currDDTxn : ddTransactions) {
                SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
            }
            paycheckDTO.setPaycheckNetAmount(totalPaycheckNetAmount);

            ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
            ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
            ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();

            for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
                switch(companyPayrollItem.getPayrollItem().getPayrollItemCode()) {
                    case Compensation:
                        compensationTransactions.add(DataLoadServices.createCompensationTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                    case Tp401kEmployeeDeferral:
                        deductionTransactions.add(DataLoadServices.createDeductionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                    case Tp401kEmployerMatch:
                        employerContributionTransactions.add(DataLoadServices.createEmployerContributionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                }
            }

            paycheckDTO.setCompensationTransactions(compensationTransactions);
            paycheckDTO.setDeductionTransactions(deductionTransactions);
            paycheckDTO.setEmployerContributionTransactions(employerContributionTransactions);

            paycheckDTO.setEmployeeId(employee.getSourceEmployeeId());
            SpcfDecimal compensationAmount = SpcfDecimal.createInstance(0.00);
            for (CompensationTransactionDTO compensationTransaction : compensationTransactions) {
                compensationAmount = compensationAmount.add(compensationTransaction.getCompensationAmount());
            }
            paycheckDTO.setPaycheckGrossAmount(new SpcfMoney(compensationAmount));

            BigDecimal deductionAmount = new BigDecimal(0.00);
            for (DeductionTransactionDTO deductionTransaction : deductionTransactions) {
                deductionAmount = deductionAmount.add(deductionTransaction.getDeductionAmount());
            }

            paycheckDTO.setPaycheckNetAmount(new SpcfMoney(compensationAmount.subtract(SpcfUtils.convertToSpcfMoney(deductionAmount))));

            SpcfCalendar periodBeginDate = PSPDate.getPSPTime();
            periodBeginDate.addDays(-7);
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(periodBeginDate));

            SpcfCalendar periodEndDate = PSPDate.getPSPTime();
            periodEndDate.addDays(-2);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(periodEndDate));
            paychecks.add(paycheckDTO);
        }

        payrollRunDTO.setPaychecks(paychecks);


        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("addFirstPayroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();


        if (voidPaychecks) {
            // void the 401k paycheck
            PayrollServices.beginUnitOfWork();
            VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
            voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
            ProcessResult voidPaychecksResult = PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
            PSP_PRAssert.assertSuccess("void 401k payroll", voidPaychecksResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
            for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                if (!paycheck.isVoided())
                    throw new RuntimeException("Paycheck not voided");
                if (!(PaycheckStatusCode.Inactive == paycheck.getStatus()))
                    throw new RuntimeException("Paycheck not inactive");
            }
            PayrollServices.rollbackUnitOfWork();


            // cancel the dd transaction
            PayrollServices.beginUnitOfWork();
            TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
            transactionCancelEEDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
            ProcessResult cancelPaycheckResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO);
            PSP_PRAssert.assertSuccess("void 401k payroll", cancelPaycheckResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
            for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplits()) {
                    if (!(TransactionStateCode.Cancelled == paycheckSplit.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd()))
                        throw new RuntimeException("fin txn not cancelled");
                }
            }
            PayrollServices.rollbackUnitOfWork();
        }


        return company;
    }


    public static void createWcCompanyEntryForNextCompany(Company company) {
        PayrollServices.beginUnitOfWork();
        WcCompany wcCompany = new WcCompany();

        wcCompany.setIntegrationId(company.getSourceCompanyId().substring(2));
        wcCompany.setCompany(company);
        wcCompany.setSubsTypeCd(SubsTypeCode.Next);

        Application.save(wcCompany);

        PayrollServices.commitUnitOfWork();

    }

    public static void createWcCompanyEntryForSplitCompany(Company company) {
        PayrollServices.beginUnitOfWork();
        WcCompany wcCompany = new WcCompany();

        wcCompany.setIntegrationId(company.getSourceCompanyId().substring(2));
        wcCompany.setCompany(company);
        wcCompany.setSubsTypeCd(SubsTypeCode.SplitLimit);

        Application.save(wcCompany);

        PayrollServices.commitUnitOfWork();

    }
}
