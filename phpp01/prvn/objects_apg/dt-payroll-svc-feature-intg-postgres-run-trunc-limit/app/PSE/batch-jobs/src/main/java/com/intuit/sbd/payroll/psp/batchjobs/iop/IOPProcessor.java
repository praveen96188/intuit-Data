package com.intuit.sbd.payroll.psp.batchjobs.iop;

import com.intuit.onlinepayroll.webservices.v1.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.iop.utils.IJaxBManager;
import com.intuit.sbd.payroll.psp.batchjobs.iop.utils.JaxBFactory;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.iop.exceptions.ServiceUnavailableException;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Jeff Jones
 */
public class IOPProcessor {

    private Map<String, Map<String, EmployeeBankAccountDTO>> employeeBankAccountList;
    private Map<String, Map<String, PayeeBankAccountDTO>> payeeBankAccountList;

    private static SpcfLogger logger;
    private static IJaxBManager jaxBManagerPayroll;
    private static IJaxBManager jaxBManagerPayment;

    private static final String NEW_LINE = "\n";
    public static final String DD_ACCOUNT_1 = "DirectDepositAccount1";
    public static final String DD_ACCOUNT_2 = "DirectDepositAccount2";
    public static final String PAYCHECK_ALREADY_EXISTS_CODE = "184";
    public static final String PAYMENT_ALREADY_EXISTS_CODE = "611";

    static {
        logger = PayrollServices.getLogger(IOPProcessor.class);
        try {
            jaxBManagerPayroll = JaxBFactory.getManagerInstance(PayrollCompanyModel.class);
            jaxBManagerPayment = JaxBFactory.getManagerInstance(ContractorPaymentCompanyModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create JAXBManager.", e);
        }
    }

    Integer companyId;
    PayrollCompanyModel payrollCompanyModel;
    ContractorPaymentCompanyModel contractorPaymentCompanyModel;
    SpcfCalendar startCal;
    SpcfCalendar endCal;

    public IOPProcessor(Integer pCompanyId, PayrollCompanyModel pPayrollCompanyModel,
                        ContractorPaymentCompanyModel pContractorPaymentCompanyModel,
                        SpcfCalendar pStartCal, SpcfCalendar pEndCal) {
        this.companyId = pCompanyId;
        this.payrollCompanyModel = pPayrollCompanyModel;
        this.contractorPaymentCompanyModel = pContractorPaymentCompanyModel;
        this.startCal = pStartCal;
        this.endCal = pEndCal;
    }

    public boolean processCompany() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IOPSyncBatchJob));

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        SourceSystemCode sourceSystemCode = SourceSystemCode.IOP;
        String sourceCompanyId = String.valueOf(companyId);

        Company company;
        CompanyModel companyModel = null;

        // Figure out which model to get the company from as they could be null and are separate
        if (payrollCompanyModel != null) {
            companyModel = payrollCompanyModel.getCompany();
        } else if (contractorPaymentCompanyModel != null) {
            companyModel = contractorPaymentCompanyModel.getCompany();
        } else {
            throw new RuntimeException("Both payrollCompanyModel and contractorPaymentCompanyModel are null");
        }

        // Get the company domain object
        try {
            if (companyModel != null) {
                company = persistCompany(sourceSystemCode, sourceCompanyId, companyModel);
            } else {
                company = Company.findCompany(sourceCompanyId, sourceSystemCode);
                if (company == null)
                    return false;
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            logger.warn("Unable to process IOP sync record for " + sourceCompanyId, e);
            sendMail(sourceCompanyId, payrollCompanyModel);
            return false;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        boolean processCompanyPayrollIdReturn = true;
        boolean processCompanyPaymentIdReturn = true;

        // Run the processing
        if (payrollCompanyModel != null) {
            processCompanyPayrollIdReturn = processCompanyPayroll(company);
        }

        if (contractorPaymentCompanyModel != null) {
            processCompanyPaymentIdReturn = processCompanyPayment(company);
        }

        return processCompanyPayrollIdReturn && processCompanyPaymentIdReturn;
    }

    private boolean processCompanyPayroll(Company company) {
        String sourceCompanyId = String.valueOf(companyId);
        try {
            String transmissionId = SpcfUniqueId.generateRandomUniqueIdString();

            logger.info(String.format("Processing company payroll: %s, StateDate: %s, EndDate %s",
                    sourceCompanyId, startCal.toString(), endCal.toString()));

            persistRequestAndResponse(startCal, endCal, company, transmissionId, payrollCompanyModel);

            CompanyModel companyModel = payrollCompanyModel.getCompany();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            CompanyBankAccount companyBankAccount = getOrPersistCompanyBankAccount(company, companyModel);

            employeeBankAccountList = new HashMap<String, Map<String, EmployeeBankAccountDTO>>();
            if (payrollCompanyModel.getEmployees() != null) {
                for (EmployeeModel employeeModel : payrollCompanyModel.getEmployees()) {
                    Employee employee = persistEmployee(company, employeeModel);

                    if (!employeeBankAccountList.containsKey(employee.getSourceEmployeeId())) {
                        employeeBankAccountList.put(employee.getSourceEmployeeId(), new HashMap<String, EmployeeBankAccountDTO>());
                    }

                    persistEmployeeBankAccount(company, employee, getDdAccount1Name(), employeeModel.getDirectDepositAccount1());
                    persistEmployeeBankAccount(company, employee, getDdAccount2Name(), employeeModel.getDirectDepositAccount2());
                }
            }

            //Create a sorted list of paychecks by check date
            if (payrollCompanyModel.getPaychecks() != null) {
                Map<XMLGregorianCalendar, List<PaycheckModel>> paychecksByDate = new HashMap<XMLGregorianCalendar, List<PaycheckModel>>();
                for (PaycheckModel paycheck : payrollCompanyModel.getPaychecks()) {
                    if (paycheck.getNetCheckAmount() != null && paycheck.getNetCheckAmount().doubleValue() > 0) {
                        if (!paychecksByDate.containsKey(paycheck.getCheckDate())) {
                            paychecksByDate.put(paycheck.getCheckDate(), new ArrayList<PaycheckModel>());
                        }
                        paychecksByDate.get(paycheck.getCheckDate()).add(paycheck);
                    } else {
                        if (paycheck.isIsDeleted()) {
                            deletePaycheck(company, paycheck, transmissionId);
                        }
                    }
                }

                for (XMLGregorianCalendar checkDate : paychecksByDate.keySet()) {
                    persistPayrollRun(company, companyBankAccount, checkDate, transmissionId, paychecksByDate.get(checkDate));
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (ServiceUnavailableException e) {
            logger.info(e);
            return false;
        } catch (Exception e) {
            logger.warn("Unable to process IOP sync record for " + sourceCompanyId, e);
            sendMail(sourceCompanyId, payrollCompanyModel);
            return false;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return true;
    }

    private boolean processCompanyPayment(Company company) {
        String sourceCompanyId = String.valueOf(companyId);
        try {
            String transmissionId = SpcfUniqueId.generateRandomUniqueIdString();

            logger.info(String.format("Processing company payment: %s, StateDate: %s, EndDate %s",
                    sourceCompanyId, startCal.toString(), endCal.toString()));

            CompanyModel companyModel = contractorPaymentCompanyModel.getCompany();

            persistRequestAndResponse(startCal, endCal, company, transmissionId, contractorPaymentCompanyModel);

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            getOrPersistCompanyBankAccount(company, companyModel);

            // Process the Payees and BillPayments
            payeeBankAccountList = new HashMap<String, Map<String, PayeeBankAccountDTO>>();
            if (contractorPaymentCompanyModel.getContractors() != null) {
                logger.info(String.format("Processing contractors Found: %d", contractorPaymentCompanyModel.getContractors().size()));

                for (ContractorModel contractorModel : contractorPaymentCompanyModel.getContractors()) {
                    logContractorModel(contractorModel);
                    Payee payee = persistContractor(company, contractorModel);

                    if (!payeeBankAccountList.containsKey(payee.getSourcePayeeId())) {
                        payeeBankAccountList.put(payee.getSourcePayeeId(), new HashMap<String, PayeeBankAccountDTO>());
                    }

                    persistPayeeBankAccount(company, payee, getDdAccount1Name(), contractorModel.getDirectDepositAccount1());
                    persistPayeeBankAccount(company, payee, getDdAccount2Name(), contractorModel.getDirectDepositAccount2());
                }

                logger.info("Finished Processing Contractors");
            }

            //Create a sorted list of payments by check date
            if (contractorPaymentCompanyModel.getContractorPayments() != null) {
                logger.info(String.format("Processing payments Found: %d", contractorPaymentCompanyModel.getContractorPayments().size()));

                Map<XMLGregorianCalendar, List<ContractorPaymentModel>> paymentsByDate = new HashMap<XMLGregorianCalendar, List<ContractorPaymentModel>>();
                for (ContractorPaymentModel payment : contractorPaymentCompanyModel.getContractorPayments()) {
                    if (payment.getGrossAmount() != null && payment.getGrossAmount().doubleValue() > 0) {
                        if (!paymentsByDate.containsKey(payment.getCheckDate())) {
                            paymentsByDate.put(payment.getCheckDate(), new ArrayList<ContractorPaymentModel>());
                        }
                        paymentsByDate.get(payment.getCheckDate()).add(payment);

                        logContractorPaymentModel(payment);
                    } else {
                        logger.info(String.format("Excluding payment id %d because its is null or less than 0", payment.getId()));
                    }
                }

                logger.info(String.format("Persisting %d unique days worth of payments", paymentsByDate.size()));

                for (XMLGregorianCalendar checkDate : paymentsByDate.keySet()) {
                    persistPayment(company, paymentsByDate.get(checkDate));
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (ServiceUnavailableException e) {
            logger.info(e);
            return false;
        } catch (Exception e) {
            logger.warn("Unable to process IOP sync record for " + sourceCompanyId, e);
            sendMail(sourceCompanyId, contractorPaymentCompanyModel);
            return false;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return true;
    }

    /**
     * Gets the DD Account name for the 1st account.  This keeps the source id consistent.
     *
     * @return The DD Account name for the 1st account
     */
    public static String getDdAccount1Name() {
        return DD_ACCOUNT_1;
    }

    /**
     * Gets the DD Account name for the 2nd account.  This keeps the source id consistent.
     *
     * @return The DD Account name for the 2nd account
     */
    public static String getDdAccount2Name() {
        return DD_ACCOUNT_2;
    }

    private void logContractorModel(ContractorModel pContractorModel) {
        long id = pContractorModel.getId();
        String phone = null;
        String email = null;

        if (pContractorModel.getContact() != null) {
            email = pContractorModel.getContact().getEmailAddress();

            if (pContractorModel.getContact().getPhones() != null && pContractorModel.getContact().getPhones().size() > 0) {
                phone = pContractorModel.getContact().getPhones().get(0).getPhoneNumber();
            }
        }

        logger.info(String.format("Contractor Model data - id:%d phone:%s email:%s type:%d businessname:%s taxid:%s firstname:%s " +
                        "lastname:%s SSN is null:%b Contact is null:%b DD Account1 is null:%b DD Account2 is null:%b ",
                pContractorModel.getId(), phone, email, pContractorModel.getType(), pContractorModel.getBusinessName(),
                pContractorModel.getTIN(), pContractorModel.getFirstName(), pContractorModel.getLastName(),
                pContractorModel.getSocialSecurityNumber() == null, pContractorModel.getContact() == null,
                pContractorModel.getDirectDepositAccount1() == null, pContractorModel.getDirectDepositAccount2() == null));
    }

    private void logContractorPaymentModel(ContractorPaymentModel pContractorPaymentModel) {
        logger.info(String.format("Contractor Payment Model data - id:%d contractorid:%d grossamount:%s memo:%s checkdate:%s ",
                pContractorPaymentModel.getId(), pContractorPaymentModel.getContractor().getId(),
                pContractorPaymentModel.getGrossAmount().toPlainString(), pContractorPaymentModel.getMemo(),
                pContractorPaymentModel.getCheckDate()));
    }

    /**
     * Gets the company bank account.  If the company bank account does not exist in the database, a new one is added
     *
     * @param company      The company domain object
     * @param companyModel The company model containing the bank account information
     * @return The CompanyBankAccount as found in the CompanyModel
     * @throws Exception
     */
    private CompanyBankAccount getOrPersistCompanyBankAccount(Company company, CompanyModel companyModel) throws Exception {
        CompanyBankAccount companyBankAccount;
        if (companyModel != null) {
            companyBankAccount = persistCompanyBankAccount(company, companyModel.getDdAccount());
        } else {
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        }

        return companyBankAccount;
    }

    private Company persistCompany(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, CompanyModel pCompanyModel) throws Exception {
        //Add or Update Company
        Company company = Company.findCompany(pSourceCompanyId, pSourceSystemCode);
        if (company == null) {
            //Create company
            CompanyDTO companyDTO = PSPFactory.createCompanyDTO(pCompanyModel);
            ProcessResult<Company> companyPR = PayrollServices.companyManager.addCompany(companyDTO);
            if (!companyPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pSourceSystemCode.toString(), pSourceCompanyId, companyPR.getMessages().get(0).getMessage()));
            }
            company = companyPR.getResult();

            ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
            serviceInfoDTO.setServiceCode(ServiceCode.RiskAssessment);
            FundingModel fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);
            serviceInfoDTO.setFundingModel(fundingModel);

            ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.addService(pSourceSystemCode, pSourceCompanyId, serviceInfoDTO);
            if (!companyServicePR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pSourceSystemCode.toString(), pSourceCompanyId, companyServicePR.getMessages().get(0).getMessage()));
            }
        } else {
            //Update company

            if (company.isCompanyCancelled()) {
                ProcessResult<CompanyService> companyServicePR = PayrollServices.companyManager.reactivateService
                        (pSourceSystemCode, pSourceCompanyId, ServiceCode.RiskAssessment);
                if (!companyServicePR.isSuccess()) {
                    throw new Exception(String.format("%s / %s: %s", pSourceSystemCode.toString(), pSourceCompanyId, companyServicePR.getMessages().get(0).getMessage()));
                }
            }

            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
            PSPFactory.updateCompanyDTO(companyDTO, pCompanyModel);
            ProcessResult<Company> updateCompanyPR = PayrollServices.companyManager.updateCompany
                    (pSourceSystemCode, pSourceCompanyId, companyDTO);
            if (!updateCompanyPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pSourceSystemCode.toString(), pSourceCompanyId, updateCompanyPR.getMessages().get(0).getMessage()));
            }
            company = updateCompanyPR.getResult();
        }

        return company;
    }

    private CompanyBankAccount persistCompanyBankAccount(Company pCompany, BankAccountModel pBankAccountModel) throws Exception {
        //Add or Update CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(pCompany, BankAccountStatus.Active);
        if (companyBankAccount == null || companyBankAccount.getStatusCd().equals(BankAccountStatus.Inactive)) {
            //Create CompanyBankAccount
            CompanyBankAccountDTO companyBankAccountDTO = PSPFactory.createCompanyBankAccountDTO(pBankAccountModel);
            ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.addCompanyBankAccount
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyBankAccountDTO, false, true);
            if (!companyBankAccountPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), companyBankAccountPR.getMessages().get(0).getMessage()));
            }
            companyBankAccount = companyBankAccountPR.getResult();
        } else {
            //Update CompanyBankAccount
            CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
            PSPFactory.updateCompanyBankAccountDTO(companyBankAccountDTO, pBankAccountModel);
            ProcessResult<CompanyBankAccount> companyBankAccountPR = PayrollServices.companyManager.changeCompanyBankAccount
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyBankAccountDTO, false, false, false);
            if (!companyBankAccountPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), companyBankAccountPR.getMessages().get(0).getMessage()));
            }
            companyBankAccount = companyBankAccountPR.getResult();
        }

        return companyBankAccount;
    }

    private Employee persistEmployee(Company pCompany, EmployeeModel pEmployeeModel) throws Exception {
        //Add or Update Employee
        Employee employee = Employee.findEmployee(pCompany, String.valueOf(pEmployeeModel.getId()));
        if (employee == null || employee.getStatusCd().equals(EmployeeStatus.Inactive)) {
            //create Employee
            EmployeeDTO employeeDTO = PSPFactory.createEmployeeDTO(pEmployeeModel);
            ProcessResult<Employee> employeePR = PayrollServices.employeeManager.addEmployee
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employeeDTO);
            if (!employeePR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), employeePR.getMessages().get(0).getMessage()));
            }
            employee = employeePR.getResult();
        } else {
            //Update Employee
            EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
            PSPFactory.updateEmployeeDTO(employeeDTO, pEmployeeModel);
            ProcessResult<Employee> employeePR = PayrollServices.employeeManager.updateEmployee
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employeeDTO);
            if (!employeePR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), employeePR.getMessages().get(0).getMessage()));
            }
            employee = employeePR.getResult();
        }
        return employee;
    }

    /**
     * Adds or updates a payee
     *
     * @param pCompany         The company for the PayeeModel
     * @param pContractorModel The incoming ContractorModel
     * @return The domain payee object representing the PayeeModel
     * @throws Exception On processing error
     */
    private Payee persistContractor(Company pCompany, ContractorModel pContractorModel) throws Exception {
        PayeeDTO payeeDTO = PSPFactory.createPayeeDTO(pContractorModel);
        ProcessResult<Payee> payeePR = PayrollServices.billPaymentManager.addOrUpdatePayee(
                pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payeeDTO);
        if (!payeePR.isSuccess()) {
            throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payeePR.getMessages().get(0).getMessage()));
        }

        return payeePR.getResult();
    }

    private EmployeeBankAccount persistEmployeeBankAccount(Company pCompany, Employee pEmployee, String pSourceEmployeeBankAccountId, BankAccountModel pBankAccountModel) throws Exception {
        EmployeeBankAccount employeeBankAccount = null;

        //Add or Update First EmployeeBankAccount
        EmployeeBankAccountDTO employeeBankAccountDTO = null;
        if (pBankAccountModel != null) {
            employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(pEmployee, pSourceEmployeeBankAccountId);
            if (employeeBankAccount == null || employeeBankAccount.getStatusCd().equals(BankAccountStatus.Inactive)) {
                //Create Employee Bank Account
                employeeBankAccountDTO = PSPFactory.createEmployeeBankAccountDTO
                        (pSourceEmployeeBankAccountId, pBankAccountModel);
                ProcessResult<EmployeeBankAccount> ebaPR = PayrollServices.employeeManager.addEmployeeBankAccount
                        (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee.getSourceEmployeeId(), employeeBankAccountDTO);
                if (!ebaPR.isSuccess()) {
                    throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), ebaPR.getMessages().get(0).getMessage()));
                }
                employeeBankAccount = ebaPR.getResult();
            } else {
                //Update Employee Bank Account
                if (isBankAccountDifferent(employeeBankAccount.getBankAccount(), pBankAccountModel)) {
                    employeeBankAccountDTO = PayrollServices.dtoFactory.create(employeeBankAccount);
                    PSPFactory.updateEmployeeBankAccountDTO(employeeBankAccountDTO, pBankAccountModel);
                    ProcessResult<EmployeeBankAccount> ebaPR = PayrollServices.employeeManager.updateEmployeeBankAccount
                            (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pEmployee.getSourceEmployeeId(), employeeBankAccountDTO);
                    if (!ebaPR.isSuccess()) {
                        throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), ebaPR.getMessages().get(0).getMessage()));
                    }
                    employeeBankAccount = ebaPR.getResult();
                }
            }
            employeeBankAccountDTO = PayrollServices.dtoFactory.create(employeeBankAccount);
            employeeBankAccountList.get(pEmployee.getSourceEmployeeId()).put(pSourceEmployeeBankAccountId, employeeBankAccountDTO);
        }

        return employeeBankAccount;
    }

    /**
     * Add or Update Payee BankAccount
     *
     * @param pCompany                  The payee's company
     * @param pPayee                    The payee
     * @param pSourcePayeeBankAccountId The source id
     * @param pBankAccountModel         The BankAccountModel to persist
     * @return The domain object representing the BankAccount
     * @throws Exception On processing error
     */
    private PayeeBankAccount persistPayeeBankAccount(Company pCompany, Payee pPayee, String pSourcePayeeBankAccountId, BankAccountModel pBankAccountModel) throws Exception {
        PayeeBankAccount payeeBankAccount = null;

        //Add or Update First EmployeeBankAccount
        PayeeBankAccountDTO payeeBankAccountDTO = null;
        if (pBankAccountModel != null) {
            payeeBankAccountDTO = PSPFactory.createPayeeBankAccountDTO
                    (pSourcePayeeBankAccountId, pBankAccountModel);
            ProcessResult<PayeeBankAccount> ebaPR = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pPayee.getSourcePayeeId(), payeeBankAccountDTO);
            if (!ebaPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), ebaPR.getMessages().get(0).getMessage()));
            }
            payeeBankAccount = ebaPR.getResult();

            payeeBankAccountDTO = PayrollServices.dtoFactory.create(payeeBankAccount);
            payeeBankAccountList.get(pPayee.getSourcePayeeId()).put(pSourcePayeeBankAccountId, payeeBankAccountDTO);
        }

        return payeeBankAccount;
    }

    private void persistPayrollRun(Company pCompany,
                                   CompanyBankAccount pCompanyBankAccount,
                                   XMLGregorianCalendar pCheckDate,
                                   String pTransmissionId,
                                   List<PaycheckModel> pPaychecks) throws Exception {
        List<PaycheckDTO> paycheckDTOList = new ArrayList<PaycheckDTO>();
        Set<String> paycheckIdsToAddList = new HashSet<String>();

        for (PaycheckModel paycheckModel : pPaychecks) {
            if (paycheckModel.isIsDeleted()) {
                deletePaycheck(pCompany, paycheckModel, pTransmissionId);
            } else {
                paycheckDTOList.add(PSPFactory.createPaycheckDTO(paycheckModel, employeeBankAccountList));
                paycheckIdsToAddList.add(Long.toString(paycheckModel.getId()));
            }
        }

        if (!paycheckDTOList.isEmpty()) {
            if (pCompanyBankAccount != null) {
                CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(pCompanyBankAccount);
                PayrollRunDTO payrollRunDTO = PSPFactory.createPayrollRunDTO(pCheckDate, null, companyBankAccountDTO, paycheckDTOList);
                ProcessResult<PayrollRun> payrollRunPR = PayrollServices.payrollManager.submitPayroll
                        (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRunDTO, null);
                if (!payrollRunPR.isSuccess()) {
                    if (payrollRunPR.getMessages(PAYCHECK_ALREADY_EXISTS_CODE).size() > 0) {   //184 code is - If paycheck already exist
                        DomainEntitySet<Paycheck> paychecksAlreadyExists = Paycheck.findPaychecks(pCompany, paycheckIdsToAddList);
                        for (Paycheck paycheck : paychecksAlreadyExists) {
                            paycheckIdsToAddList.remove(paycheck.getSourcePaycheckId());
                        }
                        if (paycheckIdsToAddList.size() > 0) {
                            logger.warn("IOP: Unable to process paycheck data for company " + pCompany.getSourceSystemCd().toString() + "/" + companyId + "  as " + payrollRunPR.getMessages().get(0).getMessage());
                            logger.info("IOP: submitting paychecks " + paycheckIdsToAddList + " for company " + companyId + " after removing existing paychecks.");
                            submitPayroll(pCompany, pCompanyBankAccount, pCheckDate, pPaychecks, paycheckIdsToAddList);
                        } else {
                            throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
                        }
                    } else {
                        throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
                    }
                }

            } else {
                throw new Exception(String.format("Company %s / %s does not have an active company bank account.", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId()));
            }
        }
    }

    /**
     * Adds a BillPayment for the company
     *
     * @param pCompany  The company with the payment
     * @param pPayments The list of payments to make
     * @throws Exception On processing error
     */
    private void persistPayment(Company pCompany,
                                List<ContractorPaymentModel> pPayments) throws Exception {
        List<BillPaymentDTO> paymentDTOList = new ArrayList<BillPaymentDTO>();
        Set<String> paymentIdsToAddList = new HashSet<String>();
        for (ContractorPaymentModel contractorPaymentModel : pPayments) {
            if (!contractorPaymentModel.isIsDeleted()) {
                BillPaymentDTO billPaymentDTO = PSPFactory.createPaymentDTO(contractorPaymentModel, contractorPaymentCompanyModel);
                paymentDTOList.add(billPaymentDTO);
                paymentIdsToAddList.add(String.valueOf(contractorPaymentModel.getId()));
            } else {
                logger.info(String.format("Skipping deleted contractor payment with ID: %d, Contractor Id: %s",
                        contractorPaymentModel.getId(), contractorPaymentModel.getContractor().getId()));
            }
        }

        ProcessResult<Collection<PayrollRun>> payrollRunPR = PayrollServices.billPaymentManager.submitBillPayment(
                pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), paymentDTOList);
        if (!payrollRunPR.isSuccess()) {
            if (payrollRunPR.getMessages(PAYMENT_ALREADY_EXISTS_CODE).size() > 0) {   //611 code is - If payment already exist
                DomainEntitySet<BillPayment> billPaymentAlreadyExists = BillPayment.findBillPaymentBySourceIds(pCompany, paymentIdsToAddList);
                for (BillPayment payment : billPaymentAlreadyExists) {
                    paymentIdsToAddList.remove(payment.getSourceId());
                }
                if (paymentIdsToAddList.size() > 0) {
                    logger.warn("IOP: Unable to process payment data for company " + pCompany.getSourceSystemCd().toString() + "/" + companyId + "  as " + payrollRunPR.getMessages().get(0).getMessage());
                    logger.info("IOP: submitting payments " + paymentIdsToAddList + " for company " + companyId + " after removing existing paychecks.");
                    submitPayment(pCompany, pPayments, paymentIdsToAddList);
                } else {
                    throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
                }
            } else {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
            }
        }
    }

    private void deletePaycheck(Company pCompany, PaycheckModel pPaycheckModel, String pTransmissionId) throws Exception {
        Paycheck paycheck = Paycheck.findPaycheck(pCompany, String.valueOf(pPaycheckModel.getId()));
        if (paycheck != null) {
            ProcessResult deletePaycheckPR = PayrollServices.payrollManager.deletePaycheck
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), paycheck.getSourcePaycheckId(), pTransmissionId);
            if (!deletePaycheckPR.isSuccess()) {
                throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), deletePaycheckPR.getMessages().get(0).getMessage()));
            }
        }
    }

    private void persistRequestAndResponse(SpcfCalendar pStartCal,
                                           SpcfCalendar pEndCal,
                                           Company pCompany,
                                           String pTransmissionId,
                                           ContractorPaymentCompanyModel pContractorPaymentCompanyModel) {
        try {
            SourceSystemTransmissionDTO sourceSystemTransmissionDTO = PSPFactory.createBeginningTransmission
                    (pCompany.getSourceCompanyId(), pStartCal, pEndCal);
            ProcessResult<SourceSystemTransmission> btPR = PayrollServices.transmissionManagerSecondary.beginTransmission
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pTransmissionId, sourceSystemTransmissionDTO);
            if (!btPR.isSuccess()) {
                throw new Exception(btPR.getMessages().get(0).getMessage());
            }

            String soapResponse = jaxBManagerPayment.marshall(pContractorPaymentCompanyModel);

            sourceSystemTransmissionDTO.setResponseDocument(soapResponse);
            ProcessResult<SourceSystemTransmission> etPR = PayrollServices.transmissionManagerSecondary.endTransmission
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pTransmissionId, sourceSystemTransmissionDTO);
            if (!etPR.isSuccess()) {
                throw new Exception(btPR.getMessages().get(0).getMessage());
            }
        } catch (Exception e) {
            logger.warn("Unable to persist IOP sync Request/Response for " + pCompany.getSourceCompanyId(), e);
        }
    }

    private void persistRequestAndResponse(SpcfCalendar pStartCal,
                                           SpcfCalendar pEndCal,
                                           Company pCompany,
                                           String pTransmissionId,
                                           PayrollCompanyModel pPayrollCompanyModel) {
        try {
            SourceSystemTransmissionDTO sourceSystemTransmissionDTO = PSPFactory.createBeginningTransmission
                    (pCompany.getSourceCompanyId(), pStartCal, pEndCal);
            ProcessResult<SourceSystemTransmission> btPR = PayrollServices.transmissionManagerSecondary.beginTransmission
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pTransmissionId, sourceSystemTransmissionDTO);
            if (!btPR.isSuccess()) {
                throw new Exception(btPR.getMessages().get(0).getMessage());
            }

            String soapResponse = jaxBManagerPayroll.marshall(pPayrollCompanyModel);

            sourceSystemTransmissionDTO.setResponseDocument(soapResponse);
            ProcessResult<SourceSystemTransmission> etPR = PayrollServices.transmissionManagerSecondary.endTransmission
                    (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pTransmissionId, sourceSystemTransmissionDTO);
            if (!etPR.isSuccess()) {
                throw new Exception(btPR.getMessages().get(0).getMessage());
            }
        } catch (Exception e) {
            logger.warn("Unable to persist IOP sync Request/Response for " + pCompany.getSourceCompanyId(), e);
        }
    }

    private boolean isBankAccountDifferent(BankAccount pBankAccount, BankAccountModel pBankAccountModel) {

        if (pBankAccount == null || pBankAccountModel == null) {
            return !(pBankAccount == null && pBankAccountModel == null);
        }

        String pspAccountNumber = "";
        String pspRoutingNumber = "";
        String pspBankName = "";
        String pspAccountType = "";
        if (pBankAccount.getAccountNumber() != null) {
            pspAccountNumber = pBankAccount.getAccountNumber();
        }
        if (pBankAccount.getRoutingNumber() != null) {
            pspRoutingNumber = pBankAccount.getRoutingNumber();
        }
        if (pBankAccount.getBankName() != null) {
            pspBankName = pBankAccount.getBankName();
        }
        if (pBankAccount.getAccountTypeCd() != null) {
            pspAccountType = pBankAccount.getAccountTypeCd().toString().toUpperCase();
        }

        String iopAccountNumber = "";
        String iopRoutingNumber = "";
        String iopBankName = "";
        String iopAccountType = "";
        if (pBankAccountModel.getAccountNumber() != null) {
            iopAccountNumber = pBankAccountModel.getAccountNumber();
        }
        if (pBankAccountModel.getBankRoutingNumber() != null) {
            iopRoutingNumber = pBankAccountModel.getBankRoutingNumber();
        }
        if (pBankAccountModel.getBankName() != null) {
            iopBankName = pBankAccountModel.getBankName();
        }
        if (pBankAccountModel.getBankAccountType() != null) {
            iopAccountType = pBankAccountModel.getBankAccountType().toString().toUpperCase();
        }

        return !(pspAccountNumber.equals(iopAccountNumber) &&
                pspRoutingNumber.equals(iopRoutingNumber) &&
                pspBankName.equals(iopBankName) &&
                pspAccountType.equals(iopAccountType));
    }

    private void sendMail(String pSourceCompanyId, PayrollCompanyModel pPayrollCompanyModel) {
        try {
            String iopToAddress = BatchUtils.getConfigString("psp_iop_to_address");
            if (iopToAddress == null || iopToAddress.length() == 0) {
                return;
            }

            String companyName = "Unknown";
            Map<XMLGregorianCalendar, BigDecimal> checkDateTotals = new HashMap<XMLGregorianCalendar, BigDecimal>();

            CompanyModel companyModel = pPayrollCompanyModel.getCompany();
            if (companyModel != null) {
                companyName = companyModel.getBusinessName();
            }

            List<PaycheckModel> paycheckList = pPayrollCompanyModel.getPaychecks();
            if (paycheckList != null) {
                //Create a sorted list of paychecks by check date
                Map<XMLGregorianCalendar, List<PaycheckModel>> paychecksByDate = new HashMap<XMLGregorianCalendar, List<PaycheckModel>>();
                for (PaycheckModel paycheck : paycheckList) {
                    if (!paychecksByDate.containsKey(paycheck.getCheckDate())) {
                        paychecksByDate.put(paycheck.getCheckDate(), new ArrayList<PaycheckModel>());
                    }
                    paychecksByDate.get(paycheck.getCheckDate()).add(paycheck);
                }

                for (XMLGregorianCalendar xmlGregorianCalendar : paychecksByDate.keySet()) {
                    BigDecimal total = new BigDecimal(0.00);
                    for (PaycheckModel paycheck : paychecksByDate.get(xmlGregorianCalendar)) {
                        total = total.add(paycheck.getNetCheckAmount());
                    }
                    checkDateTotals.put(xmlGregorianCalendar, total);
                }
            }

            String subject = "Action Required: Failure to process IOP payroll record(s)";

            StringBuilder bodyMsg = new StringBuilder();
            bodyMsg.append("PSP was unable to process the following payroll from IOP.")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append("SourceCompanyId: ")
                    .append(pSourceCompanyId)
                    .append(NEW_LINE)
                    .append("Company Name: ")
                    .append(companyName);

            for (XMLGregorianCalendar xmlGregorianCalendar : checkDateTotals.keySet()) {
                bodyMsg.append(NEW_LINE)
                        .append(NEW_LINE)
                        .append("Check Date: ")
                        .append(xmlGregorianCalendar.toString())
                        .append(NEW_LINE)
                        .append("Payroll Total: ")
                        .append(checkDateTotals.get(xmlGregorianCalendar));
            }

            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    iopToAddress,
                    BatchUtils.getConfigString("psp_iop_from_address"),
                    subject,
                    bodyMsg.toString());
            logger.info("IOP e-mail sent");
        } catch (Exception e) {
            logger.warn("Unable to send IOP e-mail", e);
        }
    }

    private void sendMail(String pSourceCompanyId, ContractorPaymentCompanyModel pContractorPaymentCompanyModel) {
        try {
            String iopToAddress = BatchUtils.getConfigString("psp_iop_to_address");
            if (iopToAddress == null || iopToAddress.length() == 0) {
                return;
            }

            String companyName = "Unknown";
            Map<XMLGregorianCalendar, BigDecimal> checkDateTotals = new HashMap<XMLGregorianCalendar, BigDecimal>();

            CompanyModel companyModel = pContractorPaymentCompanyModel.getCompany();
            if (companyModel != null) {
                companyName = companyModel.getBusinessName();
            }

            List<ContractorPaymentModel> paymentList = pContractorPaymentCompanyModel.getContractorPayments();
            if (paymentList != null) {
                //Create a sorted list of paychecks by check date
                Map<XMLGregorianCalendar, List<ContractorPaymentModel>> paychecksByDate = new HashMap<XMLGregorianCalendar, List<ContractorPaymentModel>>();
                for (ContractorPaymentModel contractorPaymentModel : paymentList) {
                    if (!paychecksByDate.containsKey(contractorPaymentModel.getCheckDate())) {
                        paychecksByDate.put(contractorPaymentModel.getCheckDate(), new ArrayList<ContractorPaymentModel>());
                    }
                    paychecksByDate.get(contractorPaymentModel.getCheckDate()).add(contractorPaymentModel);
                }

                for (XMLGregorianCalendar xmlGregorianCalendar : paychecksByDate.keySet()) {
                    BigDecimal total = new BigDecimal(0.00);
                    for (ContractorPaymentModel contractorPaymentModel : paychecksByDate.get(xmlGregorianCalendar)) {
                        total = total.add(contractorPaymentModel.getGrossAmount());
                    }
                    checkDateTotals.put(xmlGregorianCalendar, total);
                }
            }

            String subject = "Action Required: Failure to process IOP payroll record(s)";

            StringBuilder bodyMsg = new StringBuilder();
            bodyMsg.append("PSP was unable to process the following payroll from IOP.")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append("SourceCompanyId: ")
                    .append(pSourceCompanyId)
                    .append(NEW_LINE)
                    .append("Company Name: ")
                    .append(companyName);

            for (XMLGregorianCalendar xmlGregorianCalendar : checkDateTotals.keySet()) {
                bodyMsg.append(NEW_LINE)
                        .append(NEW_LINE)
                        .append("Check Date: ")
                        .append(xmlGregorianCalendar.toString())
                        .append(NEW_LINE)
                        .append("Payroll Total: ")
                        .append(checkDateTotals.get(xmlGregorianCalendar));
            }

            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    iopToAddress,
                    BatchUtils.getConfigString("psp_iop_from_address"),
                    subject,
                    bodyMsg.toString());
            logger.info("IOP e-mail sent");
        } catch (Exception e) {
            logger.warn("Unable to send IOP e-mail", e);
        }
    }

    /**
     * @param pCompany
     * @param pCompanyBankAccount
     * @param pCheckDate
     * @param pPaychecks
     * @param pPaycheckIdsToAddList - Must contain only paycheck ids to add.
     * @return ProcessResult
     */
    private ProcessResult<PayrollRun> submitPayroll(Company pCompany, CompanyBankAccount pCompanyBankAccount, XMLGregorianCalendar pCheckDate, List<PaycheckModel> pPaychecks, Set<String> pPaycheckIdsToAddList) throws Exception {
        List<PaycheckDTO> paycheckDTOList = new ArrayList<PaycheckDTO>();
        ProcessResult<PayrollRun> payrollRunPR = new ProcessResult<PayrollRun>();
        if (pCompany == null || pPaycheckIdsToAddList == null || pPaychecks == null || pCheckDate == null) {
            return payrollRunPR;
        }
        for (PaycheckModel paycheckModel : pPaychecks) {
            if (!paycheckModel.isIsDeleted() && pPaycheckIdsToAddList.contains(Long.toString(paycheckModel.getId()))) {
                paycheckDTOList.add(PSPFactory.createPaycheckDTO(paycheckModel, employeeBankAccountList));
            }
        }
        if (!paycheckDTOList.isEmpty()) {
            if (pCompanyBankAccount != null) {
                CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(pCompanyBankAccount);
                PayrollRunDTO payrollRunDTO = PSPFactory.createPayrollRunDTO(pCheckDate, null, companyBankAccountDTO, paycheckDTOList);
                payrollRunPR = PayrollServices.payrollManager.submitPayroll
                        (pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRunDTO, null);
                if (!payrollRunPR.isSuccess()) {
                    throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
                }
            }
        }
        return payrollRunPR;
    }

    /**
     * @param pCompany
     * @param pPayments
     * @param pPaymentIdsToAddList - Only payments ids to be added.
     * @return
     * @throws Exception
     */
    private ProcessResult<Collection<PayrollRun>> submitPayment(Company pCompany, List<ContractorPaymentModel> pPayments, Set<String> pPaymentIdsToAddList) throws Exception {
        List<BillPaymentDTO> paymentDTOList = new ArrayList<BillPaymentDTO>();
        ProcessResult<Collection<PayrollRun>> payrollRunPR = new ProcessResult<Collection<PayrollRun>>();
        if (pCompany == null || pPaymentIdsToAddList == null || pPayments == null) {
            return payrollRunPR;
        }
        for (ContractorPaymentModel contractorPaymentModel : pPayments) {
            if (!contractorPaymentModel.isIsDeleted() && pPaymentIdsToAddList.contains(Long.toString(contractorPaymentModel.getId()))) {
                BillPaymentDTO billPaymentDTO = PSPFactory.createPaymentDTO(contractorPaymentModel, contractorPaymentCompanyModel);
                paymentDTOList.add(billPaymentDTO);
            }
        }
        if (!paymentDTOList.isEmpty()) {
            payrollRunPR = PayrollServices.billPaymentManager.submitBillPayment(
                    pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), paymentDTOList);
        }
        if (!payrollRunPR.isSuccess()) {
            throw new Exception(String.format("%s / %s: %s", pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), payrollRunPR.getMessages().get(0).getMessage()));
        }

        return payrollRunPR;
    }
}
