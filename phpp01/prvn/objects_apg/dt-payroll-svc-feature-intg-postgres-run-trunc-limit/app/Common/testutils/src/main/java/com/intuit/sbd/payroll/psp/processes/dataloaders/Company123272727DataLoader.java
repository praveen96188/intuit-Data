package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * @author ...
 */
public class Company123272727DataLoader {

    public static final String COMPANY_ID = "123272727";

    public PayrollRunDTO loadPayroll() {
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBOE);
        CompanyBankAccount companyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");
        return createPayrollRunDTO(company, companyBankAccount, "P1", null);
    }

    /**
     * This creates a second payroll
     *
     * @return PayrollRunDTO for the payroll submit process
     */
    public PayrollRunDTO loadPayroll2() {
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBOE);
        CompanyBankAccount companyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");
        return createPayrollRunDTO(company, companyBankAccount, "P2", null);
    }

    /**
     * This will load a background payroll, for which the test case and use to generate a background transaction response
     *
     * @return PayrollRunDTO for use in the payroll submit process
     */
    public PayrollRunDTO loadBackgroundPayroll1() {
        Company company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBOE);
        CompanyBankAccount companyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, Calendar.SEPTEMBER, 25);
        return createPayrollRunDTO(company, companyBankAccount, "B1", null);
    }


    public Company setupTestCompany() {
        DataLoader dataLoader = new DataLoader();
        PayrollServicesTest.truncateTables();

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);
        
        dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());



        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(COMPANY_ID, SourceSystemCode.QBOE);
        generateEmployees(company, 2, "Active");
        generateEmployeeBankAccounts(getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");
        return company;
    }

    public PayrollRunDTO createPayrollRunDTO(Company company,
                                             CompanyBankAccount pCompanyBankAccount, String pRequestId, DateDTO pPayrollDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        if (pRequestId == null) {
            pRequestId = "1";
        }

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(pCompanyBankAccount);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO,
                ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        if (pPayrollDate == null) {
            pPayrollDate = new DateDTO();
            pPayrollDate.set(2007, Calendar.OCTOBER, 2);
        }
        payrollRunDTO.setTargetPayrollTXDate(pPayrollDate);

        // Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("PAYROLL" + pRequestId);

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();

        Collection<Employee> employees = getEmployeeCollection(company.getDirectDepositEmployees());
        int paycheckCount = 1;
        for (Employee employee : employees) {
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                ddTransactions
                        .add(createDDTransactionDTO(createEmployeeBankAccountDTO(employeeBankAccounts
                                .get(i)), pRequestId + "-" + (paycheckCount), paycheckCount));
            }

            // Create Paycheck
            paychecks.add(createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(),
                    "PAYCHECK-" + pRequestId + "-" + paycheckCount++));
        }

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public PaycheckDTO createPaycheckDTO(Collection<DDTransactionDTO> pDDTransactions,
                                         String pEmployeeId, String pPaycheckId) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setDdTransactions((List<DDTransactionDTO>)pDDTransactions);
        paycheckDTO.setEmployeeId(pEmployeeId);
        paycheckDTO.setPaycheckId(pPaycheckId);

        SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
        for (DDTransactionDTO currDDTxn : pDDTransactions) {
            SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
            totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
        }
        paycheckDTO.setPaycheckNetAmount(totalPaycheckNetAmount);

        return paycheckDTO;
    }

    public DDTransactionDTO createDDTransactionDTO(EmployeeBankAccountDTO pEmployeeBankAccountDTO, String pSourceId, int pIndex) {
        BigDecimal money = new BigDecimal(100 * (pIndex));
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();
        ddTransactionDTO.setDDTransactionAmount(money);
        //ddTransactionDTO.setDDTransactionId(SpcfUniqueId.createInstance(true).toString());
        ddTransactionDTO.setDDTransactionId("SPLIT-" + pSourceId);
        ddTransactionDTO.setEmployeeBankAccount(pEmployeeBankAccountDTO);
        return ddTransactionDTO;
    }

    public CompanyBankAccountDTO createCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) {

        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setBankAccountDTO(createBankAccountDTO(pCompanyBankAccount
                .getBankAccount()));
        return companyBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTO(
            EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setBankAccount(createBankAccountDTO(pEmployeeBankAccount
                .getBankAccount()));
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount
                .getSourceBankAccountId());
        return employeeBankAccountDTO;
    }

    public BankAccountDTO createBankAccountDTO(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        return bankAccountDTO;
    }

    public ServiceBankAccountDTO createServiceBankAccountDTO(
            CompanyBankAccountDTO pCompanyBankAccountDTO, ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    public Collection<Employee> generateEmployees(Company pCompany, int pNumber, String pStatusCd) {
        Collection<Employee> employees = new ArrayList<Employee>();
        for (int i = 1; i <= pNumber; i++) {
            String sourceEmployeeId = "Emp" + i;
            EmployeeDTO employee = getEmployee(sourceEmployeeId, pStatusCd);

            ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
                    pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employee);
            PayrollServicesTest.assertSuccess("addEmployee", procResult);

            employees.add(procResult.getResult());
        }
        return employees;
    }

    public Collection<EmployeeBankAccount> generateEmployeeBankAccounts(
            Collection<Employee> pEmployees, int pNumberOfAccounts, String pStatusCd) {
        Collection<EmployeeBankAccount> employeeBankAccounts = new ArrayList<EmployeeBankAccount>();
        for (Employee employee : pEmployees) {
            for (int i = 1; i <= pNumberOfAccounts; i++) {
                String sourceEmployeeBankAccountId = employee.getSourceEmployeeId() + "Acct" + i;

                EmployeeBankAccountDTO employeeBankAccountDTO = getEmployeeBankAccount(sourceEmployeeBankAccountId, pStatusCd);
                ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
                assertSuccess("addEmployeeBankAccount", processResult);
                
                employeeBankAccounts.add(processResult.getResult());
            }
        }
        return employeeBankAccounts;
    }

    public EmployeeDTO getEmployee(String pSourceEmployeeId, String pStatusCd) {

        EmployeeDTO employee = new EmployeeDTO();
        employee.setFirstName(pSourceEmployeeId);
        employee.setLastName(pSourceEmployeeId + "LastName");
        employee.setSocialSecurityNumber("111223333");
        employee.setEmployeeId(pSourceEmployeeId);

        return employee;
    }

    public EmployeeBankAccountDTO getEmployeeBankAccount(String pSourceEmployeeBankAccountId,
                                                      String pStatusCd) {

        EmployeeBankAccountDTO employeeBankAccount = new EmployeeBankAccountDTO();
        employeeBankAccount.setEmployeeBankAccountId(pSourceEmployeeBankAccountId);
        employeeBankAccount.setBankAccount(BankAccountDataLoader.generateBankAccountDTO());
        return employeeBankAccount;
    }

    public Collection<Paycheck> generatePaychecks() {
        return new ArrayList<Paycheck>();
    }

    public Collection<PaycheckSplit> generatePaycheckSplits() {
        return new ArrayList<PaycheckSplit>();
    }

    private Collection<Employee> getEmployeeCollection(
            DomainEntitySet<Employee> pEmployees) {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < pEmployees.size(); i++) {
            employees.add(pEmployees.get(i));
        }
        return employees;
    }


    public TransactionResponse createTransactionResponseForPayroll(PayrollRun pPayrollRun,
                                                                   Long pToken, String pRequestId) {
        TransactionResponse transactionResponse = new TransactionResponse();

        transactionResponse.setSourceRequestId(pRequestId);
        transactionResponse.setTransactionTokenNumber(pToken);
        transactionResponse.setCompany(pPayrollRun.getCompany());

        for (FinancialTransaction transaction : pPayrollRun.getFinancialTransactionCollection()) {
            TransactionState currentState = transaction.getCurrentTransactionState();
            FinancialTransactionState financialState = transaction
                    .getFinancialTransactionStateByTransactionState(currentState);

            financialState.setTransactionResponse(transactionResponse);
        }

        return transactionResponse;
    }

    public void savePayroll(PayrollRunDTO pPayrollRunDTO, Long pToken, String pRequestId) {
        //SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        //PSPDate.setPSPTime(testTime);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, COMPANY_ID, pPayrollRunDTO);

        PayrollRun payrollRun = processResult.getResult();

        TransactionResponse transactionResponse = TransactionResponse.createTransactionResponseForPayroll(payrollRun, pToken, pRequestId);
        transactionResponse = PayrollServicesTest.save(transactionResponse);
    }
    
    public ProcessResult<PayrollRun> savePayroll(SourceSystemCode pSourceSystemId,
                                                 String pSourceCompanyId, PayrollRunDTO pPayrollRun) {
        return PayrollServices.payrollManager.submitPayroll(pSourceSystemId, pSourceCompanyId, pPayrollRun);
    }
}
