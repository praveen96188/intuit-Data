package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * User: mvillani
 * Date: Sep 26, 2007
 * Time: 10:51:36 AM
 */
public class PayrollSubmitDataLoader {

    private SourceSystemCode srcSystemCodeForNewCompany = SourceSystemCode.QBOE;
    private String companyId = "123272727";
    private String fein = "222222223";


    public String getFein() {
        return fein;
    }

    public void setFein(String pFein) {
        fein = pFein;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pCompanyId) {
        companyId = pCompanyId;
    }

    public SourceSystemCode getSrcSystemCodeForNewCompany() {
        return srcSystemCodeForNewCompany;
    }

    public void setSrcSystemCodeForNewCompany(SourceSystemCode pSrcSystemCodeForNewCompany) {
        srcSystemCodeForNewCompany = pSrcSystemCodeForNewCompany;
    }

    public PayrollRunDTO loadDataForPayrollSubmit() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany(fein, companyId);

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

//        dataLoader.persistBillPaymentCompanyService(company);

        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        return payrollRunDTO;
    }

    public Company loadDataForBillPaymentSubmit() {
        DataLoader dataLoader = new DataLoader();
        setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany(fein, companyId);

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        Service service = Application.findById(Service.class, ServiceCode.DirectDeposit);
        CompanyService companyService = CompanyService.findCompanyService(company, service.getServiceCd());
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        dataLoader.persistBillPaymentCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Payees

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generatePayees(company, 5);

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);

        return company;
    }

    public Company loadDataForBillPaymentSubmit_SetupCompany() {
        DataLoader dataLoader = new DataLoader();
        setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany(fein, companyId);

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);



        return company;
    }


    public Company loadDataForBillPaymentSubmit_ContactRole(ContactRole pContactRole) {
        DataLoader dataLoader = new DataLoader();
        setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany(fein, companyId);

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        Service service = Application.findById(Service.class, ServiceCode.DirectDeposit);
        CompanyService companyService = CompanyService.findCompanyService(company, service.getServiceCd());
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        if (pContactRole != null) {
            Contact contact = company.getContactByRoleCode(pContactRole);
            contact.setEmail(null);
        } else {
            for (Contact contact : company.getContactCollection()) {
                if (contact.getContactRoleCd().equals(ContactRole.PrimaryPrincipal) || (contact.getContactRoleCd().equals(ContactRole.SecondaryPrincipal))) {
                    contact.setEmail(null);
                }
            }
        }
        dataLoader.persistBillPaymentCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Payees

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generatePayees(company, 5);

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);

        return company;
    }

    public Company loadDataForBillPaymentSubmit_AlternateOffering() {
        DataLoader dataLoader = new DataLoader();
        setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany(fein, companyId);

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        Service service = Application.findById(Service.class, ServiceCode.DirectDeposit);
        CompanyService companyService = CompanyService.findCompanyService(company, service.getServiceCd());
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        dataLoader.persistBillPaymentCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Payees

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generatePayees(company, 5);

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);

        return company;
    }

    public Collection<PayrollRunDTO> loadMultiplePayrollsWithMultiplePaycheckSplitsForCompany123272727(int pNumEmployees) {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create First Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Employees and Employee Bank Accounts
        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, pNumEmployees);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 2, "Active");

        // generate payroll run dto
        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompany(company, companyBankAccount);

        return payrollRuns;
    }

    public PayrollRunDTO loadDataForPayrollSubmitInactiveEmployees() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();

        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 1);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);

        return createPayrollRunDTO(company, companyBankAccount, "BatchId01");

    }

    public Collection<PayrollRunDTO> loadMultiplePayrollsForCompany123272727() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create First Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());
        // Create Company Service - Direct Deposit
//        dataLoader.persistTestCompanyService(company);

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
//        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompany(company, companyBankAccount);

        return payrollRuns;

    }

    public Collection<PayrollRunDTO> loadMultipleBankAccountsForCompany123272727() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create First Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());
        // Create Company Service - Direct Deposit
//        dataLoader.persistTestCompanyService(company);

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 2, "Active");

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
//        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompany(company, companyBankAccount);

        return payrollRuns;

    }

    public Collection<PayrollRunDTO> loadMultiplePayrollsForCompany123123123() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create First Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompany123123123();
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccount = dataLoader.getTestCompany123123123BankAccount();
        dataLoader.persistCompanyBankAccount(company, companyBankAccount);

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        CompanyBankAccount domainBankAccount = CompanyBankAccount.findCompanyBankAccount(company, companyBankAccount.getCompanyBankAccountID());

        // Create Company Service - Direct Deposit
//            dataLoader.persistTestCompanyService(company);

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany("123123123", srcSystemCodeForNewCompany);
        GenerateData.generate123123123Employees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany("123123123", srcSystemCodeForNewCompany);
//        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompany(company, domainBankAccount);

        return payrollRuns;

    }

    public PayrollRunDTO createPayrollRunDTO(Company company, CompanyBankAccount pCompanyBankAccount, String pBatchId) {
                    return createPayrollRunDTO( company,  pCompanyBankAccount,  pBatchId,null);
    }

    public PayrollRunDTO createPayrollRunDTO(Company company, CompanyBankAccount pCompanyBankAccount, String pBatchId,SpcfCalendar payrollRunDate) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        if (pCompanyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(pCompanyBankAccount);
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
            payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        if(payrollRunDate == null){
            payrollDate.set(2007, Calendar.OCTOBER, 2);
        }   else{
            payrollDate.set(payrollRunDate.getYear(),payrollRunDate.getMonth(),payrollRunDate.getDay());
        }


        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId(pBatchId);

        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();

        Collection<Employee> employees = GenerateData.getEmployeeCollection(company.getDirectDepositEmployees());
        for (Employee employee : employees) {
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccountDTO(employeeBankAccounts.get(i))));
            }

            // Create Paycheck
            paychecks.add(createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString()));
        }

        payrollRunDTO.setPaychecks(paychecks);
        return payrollRunDTO;
    }

    public Collection<PayrollRunDTO> createMultiplePayrollsForSameCompany(Company company, CompanyBankAccount pCompanyBankAccount) {
        Collection<PayrollRunDTO> payrollRuns = new ArrayList<PayrollRunDTO>();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(pCompanyBankAccount);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();

        payrollDate.set(2007, Calendar.OCTOBER, 2);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchId01");

        payrollRunDTO.setPaychecks(generatePayChecks(company));
        payrollRuns.add(payrollRunDTO);

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        payrollRunDTO2.setCompanyBankAccounts(companyBankAccounts);
        DateDTO payrollDate2 = new DateDTO();

        payrollDate2.set(2007, Calendar.OCTOBER, 9);
        payrollRunDTO2.setTargetPayrollTXDate(payrollDate2);

        //  Set Batch Id
        payrollRunDTO2.setPayrollTXBatchId("BatchId02");

        payrollRunDTO2.setPaychecks(generatePayChecks(company));
        payrollRuns.add(payrollRunDTO2);

        return payrollRuns;
    }

    public Collection<PayrollRunDTO> createMultiplePayrollsForSameCompanyForGemsUploadMultiplePayrolls(Company company, CompanyBankAccount pCompanyBankAccount) {
       return createMultiplePayrollsForSameCompanyForGemsUploadMultiplePayrolls( company,  pCompanyBankAccount,null);
    }
    public Collection<PayrollRunDTO> createMultiplePayrollsForSameCompanyForGemsUploadMultiplePayrolls(Company company, CompanyBankAccount pCompanyBankAccount,SpcfCalendar payrollRunDate) {
        Collection<PayrollRunDTO> payrollRuns = new ArrayList<PayrollRunDTO>();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        // Set Service Bank Accounts
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList();
        CompanyBankAccountDTO companyBankAccountDTO = createCompanyBankAccountDTO(pCompanyBankAccount);
        companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);

        // Set Begin/End Payroll Period Dates and Paycheck Date
        DateDTO payrollDate = new DateDTO();
        if (payrollRunDate == null) {
            payrollDate.set(2007, Calendar.OCTOBER, 8);
        } else {
            payrollDate.set(payrollRunDate.getYear(), payrollRunDate.getMonth(), payrollRunDate.getDay());
        }

        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        //  Set Batch Id
        payrollRunDTO.setPayrollTXBatchId("BatchId01");

        payrollRunDTO.setPaychecks(generatePayChecks(company));
        payrollRuns.add(payrollRunDTO);

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        payrollRunDTO2.setCompanyBankAccounts(companyBankAccounts);
        DateDTO payrollDate2 = new DateDTO();

        if (payrollRunDate == null) {
            payrollDate2.set(2007, Calendar.OCTOBER, 9);
        } else {
            payrollDate2.set(payrollRunDate.getYear(), payrollRunDate.getMonth(), payrollRunDate.getDay());
        }
        payrollRunDTO2.setTargetPayrollTXDate(payrollDate2);

        //  Set Batch Id
        payrollRunDTO2.setPayrollTXBatchId("BatchId02");

        payrollRunDTO2.setPaychecks(generatePayChecks(company));
        payrollRuns.add(payrollRunDTO2);

        return payrollRuns;
    }

    private Collection<PaycheckDTO> generatePayChecks(Company company) {
        // Create Paychecks and Paycheck Splits
        Collection<PaycheckDTO> paychecks = new ArrayList();

        Collection<Employee> employees = GenerateData.getEmployeeCollection(company.getDirectDepositEmployees());
        for (Employee employee : employees) {
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                ddTransactions.add(createDDTransactionDTO(createEmployeeBankAccountDTO(employeeBankAccounts.get(i))));
            }

            // Create Paycheck
            paychecks.add(createPaycheckDTO(ddTransactions, employee.getSourceEmployeeId(), SpcfUniqueId.generateRandomUniqueIdString()));
        }

        return paychecks;
    }

    public PaycheckDTO createPaycheckDTO(Collection<DDTransactionDTO> pDDTransactions, String pEmployeeId, String pPaycheckId) {
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

        // Create liability transactions
        // Todo:v2 - set a parameter for the number of generated liability transactions instead of 3 fixed ones
        Collection<LiabilityTransactionDTO> liabilityTransactions = new ArrayList();
        for (int i = 0; i < 3; i++) {
            liabilityTransactions.add(createLiabilityTransactionDTO(paycheckDTO));
        }
        paycheckDTO.setLiabilityTransactions(liabilityTransactions);
        return paycheckDTO;
    }

    public PaycheckDTO createPaycheckDTO_OnlyLiabilityAmounts(String[] pTaxAmounts, String[] pLawIds, String pEmployeeId, String pPaycheckId, Company pCompany) {
        PaycheckDTO paycheckDTO = new PaycheckDTO();
        paycheckDTO.setDdTransactions(new ArrayList());
        paycheckDTO.setEmployeeId(pEmployeeId);
        paycheckDTO.setPaycheckId(pPaycheckId);
        paycheckDTO.setPaycheckNetAmount(new SpcfMoney("200.00"));
        // Create liability transactions
        // Todo:v2 - set a parameter for the number of generated liability transactions instead of 3 fixed ones
        Collection<LiabilityTransactionDTO> liabilityTransactions = new ArrayList();

        int numberOfLiabilities = pTaxAmounts.length;
        for (int i = 0; i < numberOfLiabilities; i++) {
            LiabilityTransactionDTO liabilityTxDTO = createLiabilityTransactionDTO(paycheckDTO);
            liabilityTxDTO.setLiabilityAmount(new BigDecimal(pTaxAmounts[i]));
            liabilityTxDTO.setLawId(pLawIds[i]);
            liabilityTransactions.add(liabilityTxDTO);
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pCompany, pLawIds[i]);
            if(companyLaw != null){
                liabilityTxDTO.setPayrollItemId(companyLaw.getSourceId());                
            }
        }
        paycheckDTO.setLiabilityTransactions(liabilityTransactions);
        return paycheckDTO;
    }

    public PaycheckDTO createPaycheckDTO_CompensationAmounts(PaycheckDTO pPaycheckDTO) {
        // Create compensation transactions
        Collection<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationDTO = createCompensationTransactionDTO(pPaycheckDTO);
        compensationTransactions.add(compensationDTO);
        pPaycheckDTO.setCompensationTransactions(compensationTransactions);
        return pPaycheckDTO;
    }

    public PaycheckDTO createPaycheckDTO_DeductionAmounts(PaycheckDTO pPaycheckDTO) {
        // Create compensation transactions
        Collection<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionDTO = createDeductionTransactionDTO(pPaycheckDTO);
        deductionTransactions.add(deductionDTO);
        pPaycheckDTO.setDeductionTransactions(deductionTransactions);
        return pPaycheckDTO;
    }

    public DDTransactionDTO createDDTransactionDTO(EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        DDTransactionDTO ddTransactionDTO = new DDTransactionDTO();
        ddTransactionDTO.setDDTransactionAmount(generateRandomPaycheckAmount());
        ddTransactionDTO.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
        ddTransactionDTO.setEmployeeBankAccount(pEmployeeBankAccountDTO);
        return ddTransactionDTO;
    }

    public LiabilityTransactionDTO createLiabilityTransactionDTO(PaycheckDTO pPaycheckDTO) {
        BigDecimal paycheckAmount = getPaycheckAmount(pPaycheckDTO);
        return createLiabilityTransactionDTO(paycheckAmount);
    }

    public LiabilityTransactionDTO createLiabilityTransactionDTO(BigDecimal paycheckAmount) {
        LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
        liabilityTransactionDTO.setLiabilityAmount(generateLiabilityAmount(paycheckAmount));
        liabilityTransactionDTO.setLiabilityTaxableWages(paycheckAmount.multiply(new BigDecimal("10")));
        liabilityTransactionDTO.setLiabilityTotalWages(paycheckAmount.multiply(new BigDecimal("10")));
        liabilityTransactionDTO.setLiabilityTipsTaxableWages(paycheckAmount.multiply(new BigDecimal("10")));
        liabilityTransactionDTO.setLawId(getRandomTaxType());

        return liabilityTransactionDTO;
    }

    public CompensationTransactionDTO createCompensationTransactionDTO(PaycheckDTO pPaycheckDTO) {
        String sourcePayrollItemId = pPaycheckDTO.getEmployeeId() + "_CompensationID";
        createOrUpdatePayrollItem(sourcePayrollItemId, PayrollItemCode.Compensation);

        BigDecimal paycheckAmount = getPaycheckAmount(pPaycheckDTO);
        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
        compensationTransactionDTO.setSourcePayrollItemId(sourcePayrollItemId);
        compensationTransactionDTO.setCompensationAmount(generateCompensationAmount(paycheckAmount));
        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance("8"));

        return compensationTransactionDTO;
    }

    public DeductionTransactionDTO createDeductionTransactionDTO(PaycheckDTO pPaycheckDTO) {
        String sourcePayrollItemId = pPaycheckDTO.getEmployeeId() + "_DeductionID";
        createOrUpdatePayrollItem(sourcePayrollItemId, PayrollItemCode.OtherPostTaxDeduction);

        BigDecimal paycheckAmount = getPaycheckAmount(pPaycheckDTO);
        DeductionTransactionDTO deductionDTO = new DeductionTransactionDTO();
        deductionDTO.setSourcePayrollItemId(sourcePayrollItemId);
        deductionDTO.setDeductionAmount(generateDeductionAmount(paycheckAmount));

        return deductionDTO;
    }

    public void createOrUpdatePayrollItem(String pPayrollItemId, PayrollItemCode pPayrollItemCode) {
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(pPayrollItemCode);
        companyPayrollItemDTO.setSourcePayrollItemId(pPayrollItemId);
        companyPayrollItemDTO.setSourcePayrollItemDescription("Description");

        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(srcSystemCodeForNewCompany, companyId, companyPayrollItemDTO);
        assertSuccess(processResult);
    }

    public CompanyBankAccountDTO createCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) {

        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        companyBankAccountDTO.setBankAccountDTO(createBankAccountDTO(pCompanyBankAccount.getBankAccount()));
        return companyBankAccountDTO;
    }

    public EmployeeBankAccountDTO createEmployeeBankAccountDTO(EmployeeBankAccount pEmployeeBankAccount) {
        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setBankAccount(createBankAccountDTO(pEmployeeBankAccount.getBankAccount()));
        employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.getSourceBankAccountId());
        return employeeBankAccountDTO;
    }

    public BankAccountDTO createBankAccountDTO(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        return bankAccountDTO;
    }

    public ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO, ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }


    private BigDecimal generateRandomPaycheckAmount() {
        // KP: we need to make sure we don't exceed the dd limit here (when we gen multiple employees)...
        //double number = ((Math.random() * 99) + 1) * ((Math.random() * 99) + 1);
        double number = ((Math.random() + 1) * 10) * ((Math.random() + 1) * 10);
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.UP);
    }

    private BigDecimal generateLiabilityAmount(BigDecimal pPayCheckAmount) {
        BigDecimal number = BigDecimal.valueOf(((Math.random() * 99) / 1000) + 0.01);
        return pPayCheckAmount.multiply(number).setScale(2, RoundingMode.UP);
    }

    private SpcfMoney generateCompensationAmount(BigDecimal pPayCheckAmount) {
        BigDecimal number = BigDecimal.valueOf(((Math.random() * 99) / 1000) + 0.01);
        return SpcfUtils.convertToSpcfMoney(pPayCheckAmount.multiply(number).setScale(2, RoundingMode.UP));
    }

    private BigDecimal generateDeductionAmount(BigDecimal pPayCheckAmount) {
        BigDecimal number = BigDecimal.valueOf(((Math.random() * 99) / 1000) + 0.01);
        return pPayCheckAmount.multiply(number).setScale(2, RoundingMode.UP);
    }

    private BigDecimal getPaycheckAmount(PaycheckDTO pPaycheckDTO) {
        BigDecimal amount = new BigDecimal(0);
        for (DDTransactionDTO ddTransaction : pPaycheckDTO.getDdTransactions()) {
            amount = amount.add(ddTransaction.getDDTransactionAmount());
        }
        return amount;
    }

    private String getRandomTaxType() {
        String taxTypes[] = {"67", "27", "28", "99", "20", "34"};
        List<String> taxTypesList = Arrays.asList(taxTypes);
        Collections.shuffle(taxTypesList);
        return taxTypesList.get(0);
    }

    public PayrollRunDTO loadDataForPayrollSubmitForCompany123272727WithValidAddress() {
                    return loadDataForPayrollSubmitForCompany123272727WithValidAddress(null);
    }
    public PayrollRunDTO loadDataForPayrollSubmitForCompany123272727WithValidAddress(SpcfCalendar payrollRunDate) {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompanyWithValidAddress();

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01",payrollRunDate);
        return payrollRunDTO;
    }

    public CompanyBankAccount loadCompanyBankAccount(){
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create First Company and CompanyBankAccount
        Company company = dataLoader.persistTestActiveCompanyWithValidAddress();
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        return companyBankAccount;

    }

    public Collection<PayrollRunDTO> loadMultiplePayrollsForCompany123272727WithValidAddres() {
        CompanyBankAccount companyBankAccount = loadCompanyBankAccount();
        Company company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompany(company, companyBankAccount);

        return payrollRuns;

    }

    public Collection<PayrollRunDTO> loadMultiplePayrollsForCompany123272727WithValidAddresForGemsUploadMultiplePayrolls(SpcfCalendar payrollRunDate) {
        CompanyBankAccount companyBankAccount = loadCompanyBankAccount();
        Company company = Company.findCompany(companyId, srcSystemCodeForNewCompany);
        Collection<PayrollRunDTO> payrollRuns = createMultiplePayrollsForSameCompanyForGemsUploadMultiplePayrolls(company, companyBankAccount, payrollRunDate);

        return payrollRuns;

    }

    public void persistCompanyPIN(String pSourceCompanyId) {
        ProcessResult<HashMap<String, String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(srcSystemCodeForNewCompany, pSourceCompanyId, "1234567a");
        PayrollServicesTest.assertSuccess("createPINResult", procResult);
    }

    public PayrollRunDTO loadDataForPayrollSubmit_BankAccountNumberWithWhiteSpaces() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);

        // Create Company and CompanyBankAccount
        Company company = dataLoader.persistTestIntuitCompany2();

        // Create Company Service - Direct Deposit
        dataLoader.persistTestCompanyService(company);

        CompanyBankAccountDTO bankAccountDTO = dataLoader.getTestCompanyBankAccount();
        bankAccountDTO.getBankAccountDTO().setAccountNumber("3883  383 83");
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, bankAccountDTO);

        //Create company PIN
        persistCompanyPIN(company.getSourceCompanyId());

        // Create Employees and Employee Bank Accounts

        company = Company.findCompany("1234562", srcSystemCodeForNewCompany);
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        company = Company.findCompany("1234562", srcSystemCodeForNewCompany);
        PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01");
        return payrollRunDTO;
    }

    public ArrayList<LiabilityAdjustmentDTO> getPayrollTaxes(DateDTO pPayrollDate, String[][] pPayrollTaxes) {
// Satish
        ArrayList<LiabilityAdjustmentDTO> liabilityAdjustments = new ArrayList<LiabilityAdjustmentDTO>();
        String[] amounts = pPayrollTaxes[0];
        String[] lawIds = pPayrollTaxes[1];

        for (int i = 0; i < amounts.length; i++) {
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
            liabilityAdjustmentDTO.setAmount(SpcfUtils.convertToSpcfMoney(new BigDecimal(amounts[i])));
            liabilityAdjustmentDTO.setLawId(lawIds[i]);
            liabilityAdjustmentDTO.setEffectiveDate(pPayrollDate);
            liabilityAdjustments.add(liabilityAdjustmentDTO);
        }
        return (liabilityAdjustments);
    }
    
	public PayrollRunDTO loadDataForPayrollSubmit(Company pCompany, SpcfCalendar pPayrollRunDate) {
		DataLoader dataLoader = new DataLoader();
		dataLoader.setSrcSystemCodeForNewCompany(srcSystemCodeForNewCompany);
		Company company = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());

		CompanyBankAccount companyBankAccount = getCompanyBankAccount(company);
		PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId01", pPayrollRunDate);
		return payrollRunDTO;
	}

	public CompanyAdjustmentSubmissionDTO createCompanyAdjustmentSubmissionDTO(Company pCompany,
			DateDTO pSubmissionDate, SpcfCalendar pPeriodEndDate) {
		DataLoader dataLoader = new DataLoader();
		return dataLoader.createCompanyAdjustmentSubmissionDTO(pCompany, pSubmissionDate, pPeriodEndDate);
	}

	public Company createAssistedCompany(String pPSID) {
		DataLoader dataLoader = new DataLoader();
		return dataLoader.createAssistedCompany(pPSID);
	}

	public PayrollRunDTO loadDataForLiabilityAdjustmentOnlyPayroll(Company company, SpcfCalendar pPayrollRunDate,
			SpcfCalendar pSubmissionDate) {
		CompanyBankAccount companyBankAccount = getCompanyBankAccount(company);
		PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId02", pPayrollRunDate);
		payrollRunDTO.setPaychecks(null);

		DateDTO submissionDate = new DateDTO();
		submissionDate.set(pSubmissionDate.getYear(), pSubmissionDate.getMonth(), pSubmissionDate.getDay());

		CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = createCompanyAdjustmentSubmissionDTO(company,
				submissionDate, pSubmissionDate);
		payrollRunDTO.setCompanyAdjustmentSubmissionDTOs(Arrays.asList(companyAdjustmentSubmissionDTO));

		return payrollRunDTO;
	}

	public PayrollRunDTO loadDataForPaycheckAndLiabilityAdjustmentPayroll(Company company, SpcfCalendar pPayrollRunDate,
			SpcfCalendar pSubmissionDate) {
		CompanyBankAccount companyBankAccount = getCompanyBankAccount(company);
		PayrollRunDTO payrollRunDTO = createPayrollRunDTO(company, companyBankAccount, "BatchId02", pPayrollRunDate);

		DateDTO submissionDate = new DateDTO();
		submissionDate.set(pSubmissionDate.getYear(), pSubmissionDate.getMonth(), pSubmissionDate.getDay());

		CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = createCompanyAdjustmentSubmissionDTO(company,
				submissionDate, pSubmissionDate);
		payrollRunDTO.setCompanyAdjustmentSubmissionDTOs(Arrays.asList(companyAdjustmentSubmissionDTO));

		return payrollRunDTO;
	}

	private CompanyBankAccount getCompanyBankAccount(Company pCompany) {
		CompanyBankAccount companyBankAccount = null;
		DomainEntitySet<CompanyBankAccount> companyBankAccounts = pCompany.getCompanyBankAccountCollection();		
		if (companyBankAccounts != null && companyBankAccounts.size() > 0) {
			companyBankAccount = companyBankAccounts.get(0);
		}
		return companyBankAccount;
	}
}
