package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Jun 1, 2009
 * Time: 3:31:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollSubmitHelper {
    private static final PayrollSubmitHelper singleton = new PayrollSubmitHelper();
	private static final SpcfLogger logger = Application.getLogger(PayrollSubmitHelper.class);

    public static PayrollSubmitHelper getInstance() {
        return singleton;
    }

    private PayrollSubmitHelper() {
    }

    public ProcessResult<PayrollRun> createPayrollRun(Company company, PayrollRunDTO payrollRunDTO, SpcfCalendar paycheckSettlementDate, SpcfCalendar pPayrollRunDate) {
        ProcessResult<PayrollRun> processResult = new ProcessResult<PayrollRun>();
        PayrollRun payrollRun = new PayrollRun();

        // Associate Company and Payroll Run
        payrollRun.setCompany(company);
        payrollRun.setFundingModel(company.getFundingModel().getFundingModelCd());

        // Set PayrollRun date
        payrollRun.setPayrollRunDate(pPayrollRunDate);

        //Set paycheck date as passed in from the DTO
        SpcfCalendar paycheckDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());
        payrollRun.setPaycheckDate(paycheckDate);

        // Set usage billing flag if needed
        SpcfCalendar usageBillingEffectiveDate = company.getUsageBillingEffectiveDate();
        if (usageBillingEffectiveDate != null && !usageBillingEffectiveDate.after(payrollRun.getPaycheckDate())) {
            payrollRun.setUsageBillingToken(PayrollRun.fetchNextUsageBillingToken());
        }

        // Added Null Check for active primary entitlement unit as test case donot map and
        // EU when they are creating company causing as null assertion here
        if(company.getActivePrimaryEntitlementUnit() != null && company.getActivePrimaryEntitlementUnit().getEntitlement().getEntitlementCode().isDiamondAssisted()) {
            payrollRun.setAssistedUsageBillingToken(PayrollRun.fetchAssistedNextUsageBillingToken());
        }

        // Set the next valid PaycheckSettlementDate based on the paycheck date
        payrollRun.setPaycheckSettlementDate(paycheckSettlementDate);

        // Set PayrollRunStatus
        payrollRun.setPayrollRunStatus(PayrollStatus.Pending);
        
        // Set Service Bank Accounts
        if (payrollRunDTO.getCompanyBankAccounts() != null) {
            for (ServiceBankAccountDTO serviceBankAccountDTO : payrollRunDTO.getCompanyBankAccounts()) {
                CompanyServiceBankAccount serviceBankAccount = new CompanyServiceBankAccount();

                // Set  company bank account
                CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(
                        company, serviceBankAccountDTO.getCompanyBankAccount().getCompanyBankAccountID());
                serviceBankAccount.setCompanyBankAccount(companyBankAccount);

                // Set company service
                CompanyService companyService = CompanyService
                        .findCompanyService(company, ServiceCode.valueOf(serviceBankAccountDTO.getServiceCode().toString()));
                serviceBankAccount.setCompanyService(companyService);

                // Associate Payroll Run and Service Bank Account
                serviceBankAccount.setPayrollRun(payrollRun);
                payrollRun.addCompanyServiceBankAccount(serviceBankAccount);
            }
        }

        // Set Source PayrollRun Id
        payrollRun.setSourcePayRunId(payrollRunDTO.getPayrollTXBatchId());
        payrollRun = Application.save(payrollRun);

        // Create Paychecks
        String voidedPaycheckMessage = null;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            Paycheck paycheck = createPaycheck(company, payrollRun, paycheckDTO);

            // cache the paycheck id
            Application.getSessionCache().addPrimaryKey(paycheck.getNaturalKey(), paycheck.getId());

            boolean hasDetail = false;
            // Create Paycheck Splits
            if (paycheckDTO.getDdTransactions() != null) {
                for (DDTransactionDTO ddTransaction : paycheckDTO.getDdTransactions()) {
                	String pSessionId=paycheckDTO.getSessionID();
					ddTransaction.getEmployeeBankAccount().setSessionId(pSessionId);;
                    processResult.merge(createPayCheckSplit(company, ddTransaction, paycheck));
                }
            }

            // deactivate old accounts for employees only updated with paychecks
            if (!paycheck.getSourceEmployee().canBeRecoveredByQB() && paycheck.getPaycheckSplitCollection().size() > 0) {
                DomainEntitySet<EmployeeBankAccount> activeBankAccounts = paycheck.getSourceEmployee().getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
                boolean newAccountCreated = false;
                for (Iterator<EmployeeBankAccount> iterator = activeBankAccounts.iterator(); iterator.hasNext(); ) {
                    EmployeeBankAccount activeEmployeeBankAccount = iterator.next();
                    for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                        if (paycheckSplit.getEmployeeBankAccount().equals(activeEmployeeBankAccount)) {
                            iterator.remove();
                            if (paycheckSplit.getEmployeeBankAccount().isCreatedInCurrentSession()) {
                                newAccountCreated = true;
                            }
                            break;
                        }
                    }
                }

                // new account created accounts for existing active accounts that need to be deactivated
                EmployeeBankAccount.deactivateActiveEmployeeBankAccounts(paycheck.getDDEmployee(), activeBankAccounts, newAccountCreated);
            }

            // Create Tax Liabilities
            Collection<LiabilityTransactionDTO> liabilityTransactions = paycheckDTO.getLiabilityTransactions();
            if (liabilityTransactions != null) {
                for (LiabilityTransactionDTO liabilityTransaction : liabilityTransactions) {
                    hasDetail = true;
                    createTaxLiability(company, liabilityTransaction, paycheck);
                }
            }

            // Create Compensations
            Collection<CompensationTransactionDTO> compensationTransactions = paycheckDTO.getCompensationTransactions();
            if (compensationTransactions != null) {
                for (CompensationTransactionDTO compensationTransaction : compensationTransactions) {
                    hasDetail = true;
                    createCompensation(compensationTransaction, company, paycheck);
                }
            }

            // Create Deductions
            Collection<DeductionTransactionDTO> deductionTransactions = paycheckDTO.getDeductionTransactions();
            if (deductionTransactions != null) {
                for (DeductionTransactionDTO deductionTransaction : deductionTransactions) {
                    hasDetail = true;
                    createDeduction(deductionTransaction, company, paycheck);
                }
            }

            // Create EmployerContributionTransaction
            Collection<EmployerContributionTransactionDTO> employerContributionTransactions = paycheckDTO.getEmployerContributionTransactions();
            if (employerContributionTransactions != null) {
                for (EmployerContributionTransactionDTO employerContributionTransaction : employerContributionTransactions) {
                    hasDetail = true;
                    createEmployerContribution(employerContributionTransaction, company, paycheck);
                }
            }

            if (paycheck.getQbdtPaycheckInfo() != null) {
                paycheck.getQbdtPaycheckInfo().setIsAssisted(payrollRunDTO.getIsAssisted());
            }

            if (!hasDetail) {
                if (paycheck.getQbdtPaycheckInfo() != null) {
                    paycheck.getQbdtPaycheckInfo().setIsAssisted(false);
                }

                // if this is a "new" void without detail email the paycheck information to the tax dept
                if (payrollRunDTO.getIsAssisted() && paycheck.isVoidedOrRecalled() && company.isCompanyOnService(ServiceCode.Tax)) {
                    if (voidedPaycheckMessage == null) {
                        voidedPaycheckMessage = String.format("A voided paycheck without details was received for Company: %s \r\n", company.getSourceSystemCompanyId());
                        voidedPaycheckMessage += "This most likely indicates a void for a paycheck not recorded in PSP. (dated before 2011) \n";
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    voidedPaycheckMessage += String.format("\nPaycheck Date: %s \n", sdf.format(CalendarUtils.convertToDate(paycheck.getPayrollRun().getPaycheckDate().toLocal())));
                    voidedPaycheckMessage += String.format("Paycheck Id: %s \n", paycheck.getSourcePaycheckId());
                    voidedPaycheckMessage += String.format("Employee: %s \n", paycheck.getSourceEmployee().getFullName());
                }
            }
        }

        if (voidedPaycheckMessage != null) {
            sendBackDateEmail(voidedPaycheckMessage);
        }

        processResult.setResult(payrollRun);
        return processResult;
    }

    private Paycheck createPaycheck(Company company, PayrollRun payrollRun, PaycheckDTO pPaycheckDTO) {
        Paycheck paycheck = new Paycheck();
        paycheck.setCompany(company);
        if (pPaycheckDTO.isVoid()) {
            paycheck.setStatus(PaycheckStatusCode.Inactive);
        } else {
            paycheck.setStatus(PaycheckStatusCode.Active);
        }

        // Associate Paycheck and Employee
        Employee employee = Employee.findEmployee(company, pPaycheckDTO.getEmployeeId());
        paycheck.setSourceEmployee(employee);

        if (pPaycheckDTO.getDdTransactions() != null &&
                pPaycheckDTO.getDdTransactions().size() > 0) {
            paycheck.setDDEmployee(employee);
        }

        // Associate Paycheck and Payroll Run
        paycheck.setPayrollRun(payrollRun);
        payrollRun.addPaycheck(paycheck);

        //  PaycheckId
        paycheck.setSourcePaycheckId(pPaycheckDTO.getPaycheckId());
        paycheck.setPayPeriodBeginDate(DateDTO.convertToSpcfCalendar(pPaycheckDTO.getPayPeriodBeginDate()));
        paycheck.setPayPeriodEndDate(DateDTO.convertToSpcfCalendar(pPaycheckDTO.getPayPeriodEndDate()));
        paycheck.setIsYTDAdjustment(pPaycheckDTO.isIsYTDAdjustment());

        paycheck.setNetAmount(pPaycheckDTO.getPaycheckNetAmount());
        paycheck.setGrossAmount(pPaycheckDTO.getPaycheckGrossAmount());
        paycheck.setYTDGrossAmount(pPaycheckDTO.getPaycheckYTDGrossAmount());
        paycheck.setYTDNetAmount(pPaycheckDTO.getPaycheckYTDNetAmount());
        paycheck.setSessionId(pPaycheckDTO.getSessionID());
        /*  Added for checksum dd service source approval date
            we saved paycheck before setting approval date time so that paycheck created date property is added
            alternate way is to use psp.getdate but there will be difference of some millisecond in created date and approval date in that case
            in case of paycheck create flow source approval date should be same as paycheck created date
        */
        paycheck.setApprovalDateTimeEnd(PSPDate.getPSPTime());

        paycheck = Application.save(paycheck);

        NaturalKey naturalKey = new NaturalKey(Paycheck.class, company.getId(), paycheck.getSourcePaycheckId());
        Application.getSessionCache().addPrimaryKey(naturalKey, paycheck.getId());

        if (pPaycheckDTO.getQBDTPaycheckInfoDTO() != null) {
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = pPaycheckDTO.getQBDTPaycheckInfoDTO();
            QbdtPaycheckInfo qbdtPaycheckInfo = new QbdtPaycheckInfo();
            if (qbdtPaycheckInfoDTO.getToken() != null && qbdtPaycheckInfoDTO.getToken() == Company.EXCLUDE_TOKEN) {
                qbdtPaycheckInfo.setToken(qbdtPaycheckInfoDTO.getToken());
            }
            qbdtPaycheckInfo.setCompany(company);
            qbdtPaycheckInfo.setListId(qbdtPaycheckInfoDTO.getListId());
            qbdtPaycheckInfo.setAccountName(qbdtPaycheckInfoDTO.getAccountName());
            qbdtPaycheckInfo.setCheckNumber(qbdtPaycheckInfoDTO.getCheckNumber());
            qbdtPaycheckInfo.setCleared(qbdtPaycheckInfoDTO.getCleared());
            qbdtPaycheckInfo.setMemo(qbdtPaycheckInfoDTO.getMemo());
            if (qbdtPaycheckInfoDTO.isOnService() != null) {
                qbdtPaycheckInfo.setOnService(qbdtPaycheckInfoDTO.isOnService());
            } else {
                qbdtPaycheckInfo.setOnService(false);
            }
            qbdtPaycheckInfo.setProrate(qbdtPaycheckInfoDTO.isProrate());
            qbdtPaycheckInfo.setTrackingClass(qbdtPaycheckInfoDTO.getTrackingClass());
            qbdtPaycheckInfo.setSickHoursAccrued(qbdtPaycheckInfoDTO.getSickHoursAccrued());
            qbdtPaycheckInfo.setVacationHoursAccrued(qbdtPaycheckInfoDTO.getVacationHoursAccrued());
            qbdtPaycheckInfo.setPaycheck(paycheck);
            Application.save(qbdtPaycheckInfo);
            paycheck.setQbdtPaycheckInfo(qbdtPaycheckInfo);

            if (qbdtPaycheckInfo.getListId() != null) {
                naturalKey = new NaturalKey(Paycheck.class, company.getId(), qbdtPaycheckInfo.getListId());
                Application.getSessionCache().addPrimaryKey(naturalKey, paycheck.getId());
            }
        }

        return paycheck;
    }

    private ProcessResult<PaycheckSplit> createPayCheckSplit(Company company, DDTransactionDTO pDDTransaction, Paycheck pPaycheck) {
        ProcessResult<PaycheckSplit> processResult = new ProcessResult<PaycheckSplit>();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        String sourceCompanyId = company.getSourceCompanyId();
        PaycheckSplit paycheckSplit = new PaycheckSplit();
        paycheckSplit.setCompany(company);

        EmployeeBankAccount employeeBankAccount;

        //  Find Employee Bank Account or create it if it doesn't exist
        employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(pPaycheck.getDDEmployee(), pDDTransaction.getEmployeeBankAccount().getEmployeeBankAccountId());
        if (employeeBankAccount == null) {
            AddEmployeeBankAccountCore addEmployeeBankAccount = new AddEmployeeBankAccountCore(sourceSystemCd,
                    sourceCompanyId, pPaycheck.getDDEmployee().getSourceEmployeeId(),
                    pDDTransaction.getEmployeeBankAccount());
            processResult.merge(addEmployeeBankAccount.validate());
            if (!processResult.isSuccess()) {
                return processResult;
            }
            processResult.merge(addEmployeeBankAccount.process());
            employeeBankAccount = addEmployeeBankAccount.getEmployeeBankAccount();
            if (pPaycheck.getDDEmployee().canBeRecoveredByQB()) {
                // do not make the new account active, it was not added via employee modification
                employeeBankAccount.expireEmployeeBankAccount();
                Application.save(employeeBankAccount);
            }
        } else {  // Employee Bank Account exists and is active - if anything changed, it will be updated
            if (employeeBankAccount.getStatusCd() == BankAccountStatus.Active && pDDTransaction.getEmployeeBankAccount().getBankAccount() != null) {
                UpdateEmployeeBankAccountCore updateEmployeeBankAccount = new UpdateEmployeeBankAccountCore(sourceSystemCd,
                        sourceCompanyId, pPaycheck.getDDEmployee().getSourceEmployeeId(),
                        pDDTransaction.getEmployeeBankAccount());
                processResult.merge(updateEmployeeBankAccount.validate());
                if (!processResult.isSuccess()) {
                    return processResult;
                }
                processResult.merge(updateEmployeeBankAccount.execute());
                employeeBankAccount = updateEmployeeBankAccount.getEmployeeBankAccount();
            }
        }
        paycheckSplit.setEmployeeBankAccount(employeeBankAccount);
        if (pDDTransaction.getPayStubOrder() != null) {
            paycheckSplit.setPayStubOrder(pDDTransaction.getPayStubOrder());
        }

        //  Set Amount
        paycheckSplit.setPaycheckSplitAmount(SpcfUtils.convertToSpcfMoney(pDDTransaction.getDDTransactionAmount()));

        // Set Source DD Id
        paycheckSplit.setSourceDdTxnId(pDDTransaction.getDDTransactionId());

// Associate PaycheckSplit and Paycheck
        paycheckSplit.setPaycheck(pPaycheck);
        pPaycheck.addPaycheckSplit(paycheckSplit);
        processResult.setResult(paycheckSplit);
        return processResult;

    }

    public static Tax createTaxLiability(Company pCompany, LiabilityTransactionDTO pLiabilityTransaction, Paycheck pPaycheck) {
        Tax taxLiability = new Tax();
        taxLiability.setCompany(pCompany);
        // Associate Law
        Law law = Application.<Law>findById(Law.class, pLiabilityTransaction.getLawId());
        if (law == null) {
            law = Application.<Law>findById(Law.class, "0");    // "0" is PSP for Other, i.e. unexpected value
            //TODO:log error
        }
        taxLiability.setLaw(law);

        if (pLiabilityTransaction.getPayrollItemId() != null) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(pCompany, pLiabilityTransaction.getPayrollItemId());
            taxLiability.setCompanyLaw(companyLaw);
        }


        // Set Amounts
        if (pLiabilityTransaction.getPayStubOrder() != null) {
            taxLiability.setPayStubOrder(pLiabilityTransaction.getPayStubOrder());
        }
        taxLiability.setTaxLiabilityAmount(SpcfUtils.convertToSpcfMoney(pLiabilityTransaction.getLiabilityAmount()));
        taxLiability.setTotalWagesAmount(SpcfUtils.convertToSpcfMoney(pLiabilityTransaction.getLiabilityTotalWages()));
        taxLiability.setTaxableWagesAmount(SpcfUtils.convertToSpcfMoney(pLiabilityTransaction.getLiabilityTaxableWages()));
        if (pLiabilityTransaction.getLiabilityAmountYTD() != null) {
            taxLiability.setTaxLiabilityYTDAmount(SpcfUtils.convertToSpcfMoney(pLiabilityTransaction.getLiabilityAmountYTD()));
        }
        if (pLiabilityTransaction.getLiabilityTipsTaxableWages() != null) {
            taxLiability.setTipsTaxableWageAmount(SpcfUtils.convertToSpcfMoney(pLiabilityTransaction.getLiabilityTipsTaxableWages()));
        }
        taxLiability.setPaycheck(pPaycheck);
        pPaycheck.addTax(taxLiability);

        taxLiability = Application.save(taxLiability);
        return taxLiability;
    }

    public static Compensation createCompensation(CompensationTransactionDTO pCompensationTransaction, Company pCompany, Paycheck pPaycheck) {
        Compensation domainCompensation = new Compensation();
        domainCompensation.setCompany(pCompany);
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, pCompensationTransaction.getSourcePayrollItemId());
        if (pCompensationTransaction.getHoursWorked() != null) {
            domainCompensation.setHoursWorked(Double.parseDouble(pCompensationTransaction.getHoursWorked().toString()));
        }
        domainCompensation.setCompanyPayrollItem(companyPayrollItem);
        domainCompensation.setCompensationAmount(pCompensationTransaction.getCompensationAmount());
        if (pCompensationTransaction.getCompensationYTDAmount() != null) {
            domainCompensation.setCompensationYTDAmount(pCompensationTransaction.getCompensationYTDAmount());
        }
        if (pCompensationTransaction.getPayStubOrder() != null) {
            domainCompensation.setPayStubOrder(pCompensationTransaction.getPayStubOrder());
        }

        domainCompensation.setPaycheck(pPaycheck);
        pPaycheck.addCompensation(domainCompensation);
        domainCompensation = Application.save(domainCompensation);

        if (pCompensationTransaction.getQBDTPaylineInfoDTO() != null) {
            QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pCompensationTransaction.getQBDTPaylineInfoDTO();
            QbdtPaylineInfo qbdtPaylineInfo = new QbdtPaylineInfo();
            qbdtPaylineInfo.setCompany(pCompany);
            qbdtPaylineInfoDTO.copyToDomain(qbdtPaylineInfo);
            qbdtPaylineInfo.setCompensation(domainCompensation); // setting parent key before saving to avoid an extra update on child - optimization
            Application.save(qbdtPaylineInfo);
            domainCompensation.setQbdtPaylineInfo(qbdtPaylineInfo);
        }

        return domainCompensation;
    }

    public static Deduction createDeduction(DeductionTransactionDTO pDeductionTransaction, Company pCompany, Paycheck pPaycheck) {
        Deduction domainDeduction = new Deduction();
        domainDeduction.setCompany(pCompany);
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, pDeductionTransaction.getSourcePayrollItemId());
        domainDeduction.setCompanyPayrollItem(companyPayrollItem);
        domainDeduction.setDeductionAmount(SpcfUtils.convertToSpcfMoney(pDeductionTransaction.getDeductionAmount()));
        if (pDeductionTransaction.getDeductionYTDAmount() != null) {
            domainDeduction.setDeductionYTDAmount(SpcfUtils.convertToSpcfMoney(pDeductionTransaction.getDeductionYTDAmount()));
        }
        if (pDeductionTransaction.getPayStubOrder() != null) {
            domainDeduction.setPayStubOrder(pDeductionTransaction.getPayStubOrder());
        }
        domainDeduction.setPaycheck(pPaycheck);
        pPaycheck.addDeduction(domainDeduction);
        domainDeduction = Application.save(domainDeduction);
        if (pDeductionTransaction.getQBDTPaylineInfoDTO() != null) {
            QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pDeductionTransaction.getQBDTPaylineInfoDTO();
            QbdtPaylineInfo qbdtPaylineInfo = new QbdtPaylineInfo();
            qbdtPaylineInfo.setCompany(pCompany);
            qbdtPaylineInfoDTO.copyToDomain(qbdtPaylineInfo);
            domainDeduction.setQbdtPaylineInfo(qbdtPaylineInfo);
            qbdtPaylineInfo.setDeduction(domainDeduction); // // setting parent key before saving to avoid an extra update on child - optimization
            Application.save(qbdtPaylineInfo);
        }

        return domainDeduction;
    }

    public static EmployerContribution createEmployerContribution(EmployerContributionTransactionDTO pEmployerContributionTransaction, Company pCompany, Paycheck pPaycheck) {
        EmployerContribution employerContribution = new EmployerContribution();
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, pEmployerContributionTransaction.getSourcePayrollItemId());
        employerContribution.setContributionAmount(SpcfUtils.convertToSpcfMoney(pEmployerContributionTransaction.getContributionAmount()));
        employerContribution.setCompanyPayrollItem(companyPayrollItem);
        employerContribution.setCompany(pCompany);
        if (pEmployerContributionTransaction.getContributionYTDAmount() != null) {
            employerContribution.setContributionYTDAmount(SpcfUtils.convertToSpcfMoney(pEmployerContributionTransaction.getContributionYTDAmount()));
        }
        if (pEmployerContributionTransaction.getTaxableWagesAmount() != null) {
            employerContribution.setTaxableWagesAmount(SpcfUtils.convertToSpcfMoney(pEmployerContributionTransaction.getTaxableWagesAmount()));
        }
        if (pEmployerContributionTransaction.getTotalWagesAmount() != null) {
            employerContribution.setTotalWagesAmount(SpcfUtils.convertToSpcfMoney(pEmployerContributionTransaction.getTotalWagesAmount()));
        }
        if (pEmployerContributionTransaction.getPayStubOrder() != null) {
            employerContribution.setPayStubOrder(pEmployerContributionTransaction.getPayStubOrder());
        }
        // Associate Employer Contribution and Paycheck
        employerContribution.setPaycheck(pPaycheck);
        pPaycheck.addEmployerContribution(employerContribution);
        employerContribution = Application.save(employerContribution);

        if (pEmployerContributionTransaction.getQBDTPaylineInfoDTO() != null) {
            QBDTPaylineInfoDTO qbdtPaylineInfoDTO = pEmployerContributionTransaction.getQBDTPaylineInfoDTO();
            QbdtPaylineInfo qbdtPaylineInfo = new QbdtPaylineInfo();
            qbdtPaylineInfo.setCompany(pCompany);
            qbdtPaylineInfoDTO.copyToDomain(qbdtPaylineInfo);
            qbdtPaylineInfo.setEmployerContribution(employerContribution);
            Application.save(qbdtPaylineInfo);
            employerContribution.setQbdtPaylineInfo(qbdtPaylineInfo);
        }

        return employerContribution;
    }

    public static ProcessResult validateLineItems(Company pCompany, PaycheckDTO pPaycheckDTO) {
        ProcessResult validationResult = new ProcessResult();
        Collection<CompensationTransactionDTO> compensationItems = pPaycheckDTO.getCompensationTransactions();
        if (compensationItems != null && compensationItems.size() > 0) {
            for (CompensationTransactionDTO currentCompensation : compensationItems) {
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, currentCompensation.getSourcePayrollItemId());
                PayrollItem givenPayrollItem = companyPayrollItem.getPayrollItem();
                validationResult.merge(CompanyPayrollItem.validatePayrollItem(givenPayrollItem, currentCompensation.getCompensationAmount(), pPaycheckDTO.getPaycheckId()));
            }
        }

        Collection<DeductionTransactionDTO> deductionItems = pPaycheckDTO.getDeductionTransactions();
        if (deductionItems != null && deductionItems.size() > 0) {
            for (DeductionTransactionDTO currentDeduction : deductionItems) {
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, currentDeduction.getSourcePayrollItemId());
                PayrollItem givenPayrollItem = companyPayrollItem.getPayrollItem();
                validationResult.merge(CompanyPayrollItem.validatePayrollItem(givenPayrollItem, SpcfUtils.convertToSpcfMoney(currentDeduction.getDeductionAmount()), pPaycheckDTO.getPaycheckId()));
            }
        }

        Collection<EmployerContributionTransactionDTO> employerContributionItems = pPaycheckDTO.getEmployerContributionTransactions();
        if (employerContributionItems != null && employerContributionItems.size() > 0) {
            for (EmployerContributionTransactionDTO currentEmployerContribution : employerContributionItems) {
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, currentEmployerContribution.getSourcePayrollItemId());
                PayrollItem givenPayrollItem = companyPayrollItem.getPayrollItem();
                validationResult.merge(CompanyPayrollItem.validatePayrollItem(givenPayrollItem, SpcfUtils.convertToSpcfMoney(currentEmployerContribution.getContributionAmount()), pPaycheckDTO.getPaycheckId()));
            }
        }
        return validationResult;
    }

    public static ProcessResult validatePaycheckDTOFor401k(Company pCompany, PaycheckDTO pPaycheckDTO) {
        ProcessResult processResult = new ProcessResult();
        if (pPaycheckDTO.getPayPeriodBeginDate() == null) {
            processResult.getMessages().PaycheckHasInvalidPayPeriodStartDate(EntityName.Paycheck, pPaycheckDTO.getPaycheckId());
        }

        if (pPaycheckDTO.getPayPeriodEndDate() == null) {
            processResult.getMessages().PaycheckHasInvalidPayPeriodEndDate(EntityName.Paycheck, pPaycheckDTO.getPaycheckId());
        }

        if (has401KDeferrals(pCompany, pPaycheckDTO) && getCompensationTotal(pPaycheckDTO).compareTo(SpcfMoney.ZERO) <= 0) {
            processResult.getMessages().PaychecksWith401KDefferalsMustHavePositiveCompensation(EntityName.PayCheck, pPaycheckDTO.getPaycheckId());
        }

        return processResult;
    }

    private static boolean has401KDeferrals(Company pCompany, PaycheckDTO pPaycheckDTO) {
        List<PayrollItemCode> deferralCodes = new ArrayList<PayrollItemCode>();
        Collections.addAll(deferralCodes, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kLoanPayment);
        for (DeductionTransactionDTO deductionTransactionDTO : pPaycheckDTO.getDeductionTransactions()) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, deductionTransactionDTO.getSourcePayrollItemId());
            if (deferralCodes.contains(companyPayrollItem.getPayrollItem().getPayrollItemCode())) {
                return true;
            }
        }

        deferralCodes.clear();
        Collections.addAll(deferralCodes, PayrollItemCode.Tp401kProfitSharing, PayrollItemCode.Tp401kSafeHarbor);
        for (EmployerContributionTransactionDTO employerContributionTransactionDTO : pPaycheckDTO.getEmployerContributionTransactions()) {
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, employerContributionTransactionDTO.getSourcePayrollItemId());
            if (deferralCodes.contains(companyPayrollItem.getPayrollItem().getPayrollItemCode())) {
                return true;
            }
        }

        return false;
    }

    private static SpcfMoney getCompensationTotal(PaycheckDTO pPaycheckDTO) {
        SpcfDecimal total = SpcfDecimal.createInstance(0);
        for (CompensationTransactionDTO compensationTransactionDTO : pPaycheckDTO.getCompensationTransactions()) {
            total = total.add(compensationTransactionDTO.getCompensationAmount());
        }
        return new SpcfMoney(total);
    }

    private LiabilityAdjustment createPayrollTax(PayrollRun payrollRun, LiabilityAdjustmentDTO pLiabilityAdjustmentDTO) {
        LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
        liabilityAdjustment.setCompany(payrollRun.getCompany());

        // Associate PayrollTax and Payroll Run
        liabilityAdjustment.setPayrollRun(payrollRun);
        payrollRun.addLiabilityAdjustment(liabilityAdjustment);

        liabilityAdjustment.setAmount(pLiabilityAdjustmentDTO.getAmount());
        liabilityAdjustment.setEffectiveDate(DateDTO.convertToSpcfCalendar(pLiabilityAdjustmentDTO.getEffectiveDate()));
        liabilityAdjustment.setTaxableWages(pLiabilityAdjustmentDTO.getTaxableWages());
        liabilityAdjustment.setTotalWages(pLiabilityAdjustmentDTO.getTotalWages());
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = pLiabilityAdjustmentDTO.getQBDTTransactionInfoDTO();
        if (qbdtTransactionInfoDTO != null) {
            QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
            qbdtTransactionInfo.setCompany(payrollRun.getCompany());
            qbdtTransactionInfoDTO.copyQBDTTransactionInfoFromDTO(qbdtTransactionInfo);
            Application.save(qbdtTransactionInfo);
            liabilityAdjustment.setQbdtTransactionInfo(qbdtTransactionInfo);
            qbdtTransactionInfo.setLiabilityAdjustment(liabilityAdjustment);
        }
        Law law = Application.findById(com.intuit.sbd.payroll.psp.domain.Law.class, pLiabilityAdjustmentDTO.getLawId());
        liabilityAdjustment.setLaw(law);
        return liabilityAdjustment;
    }


    public boolean anyTaxTransactionsInPayroll(PayrollRunDTO pPayrollRunDTO) {
        Collection<PaycheckDTO> paychecks = pPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            Collection<LiabilityTransactionDTO> taxTxnsForPaycheck = currPaycheck.getLiabilityTransactions();
            if (taxTxnsForPaycheck != null && taxTxnsForPaycheck.size() > 0) {
                return true;
            }
        }
        for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : pPayrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {
            if (companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs() != null && companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean anyDDTransactionsInPaycheck(Paycheck pPaycheck) {
        if (pPaycheck == null) {
            return false;
        }
        Collection<PaycheckSplit> splits = pPaycheck.getPaycheckSplits();
        if (splits != null && splits.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean anyTaxTransactionsInPaycheck(Paycheck pPaycheck) {
        if (pPaycheck == null) {
            return false;
        }
        Collection<Tax> taxes = pPaycheck.getTaxCollection();
        if (taxes != null && taxes.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean anyLineItemsInPaycheck(Paycheck pPaycheck) {
        if (pPaycheck == null) {
            return false;
        }
        boolean hasDeductions = !pPaycheck.getDeductionCollection().isEmpty();
        boolean hasCompensations = !pPaycheck.getCompensationCollection().isEmpty();
        boolean hasEmployerContributionTransactions = !pPaycheck.getEmployerContributionCollection().isEmpty();
        if (hasDeductions || hasCompensations || hasEmployerContributionTransactions) {
            return true;
        }
        return false;
    }

    public ProcessResult validatePayrollRunDTO(Company company, PayrollRunDTO payrollRunDTO) {
        ProcessResult validationResult = new ProcessResult();
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            Collection<LiabilityTransactionDTO> liabilityTransactions = paycheck.getLiabilityTransactions();

            /*if (liabilityTransactions == null || liabilityTransactions.size() == 0) {
                validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.LiabilityTransaction,
                        "PayrollSubmitTax",
                        "LiabilityTransactions");

                return validationResult;
            }*/

            for (LiabilityTransactionDTO liabilityTransaction : liabilityTransactions) {
                if (liabilityTransaction.getPayrollItemId() != null) {
                    CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, liabilityTransaction.getPayrollItemId());
                    if (companyLaw == null) {
                        validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, liabilityTransaction.getPayrollItemId(),
                                "PayrollItemId");
                    }
                } else {
                    Law law = Application.findById(com.intuit.sbd.payroll.psp.domain.Law.class, liabilityTransaction.getLawId());
                    if (law == null) {
                        validationResult.getMessages().InvalidValue(EntityName.LiabilityTransaction, liabilityTransaction.getLawId(),
                                "LawId");
                    } else {
                        Agency agency = law.getPaymentTemplate().getAgency();
                        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(
                                company.getSourceSystemCd(), company.getSourceCompanyId(), agency.getAgencyId());

                        if (companyAgency == null) {
                            validationResult.getMessages().CompanyAgencyNotFound(
                                    EntityName.CompanyAgency, company.getSourceSystemCd().toString(), company.getSourceSystemCd().toString(), company.getSourceCompanyId(), agency.getAgencyId());

                            return validationResult;
                        }
                    }
                }
            }
        }
        return validationResult;
    }

    public Map<String, SpcfDecimal> getLiabilities(PayrollRunDTO payrollRunDTO) {
        Map<String, SpcfDecimal> lawAmountMap = new HashMap<String, SpcfDecimal>();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            Collection<LiabilityTransactionDTO> liabilityTransactions = paycheckDTO.getLiabilityTransactions();
            // Create Liabilities
            if (liabilityTransactions != null) {
                for (LiabilityTransactionDTO liabilityTransaction : liabilityTransactions) {

                    Law law = Application.findById(com.intuit.sbd.payroll.psp.domain.Law.class, liabilityTransaction.getLawId());
                    SpcfDecimal lawAmount = lawAmountMap.get(law.getLawId());

                    if (lawAmount == null) {
                        lawAmount = SpcfUtils.convertToSpcfMoney(liabilityTransaction.getLiabilityAmount());
                    } else {
                        lawAmount = lawAmount.add(SpcfUtils.convertToSpcfMoney(liabilityTransaction.getLiabilityAmount()));
                    }

                    lawAmountMap.put(law.getLawId(), lawAmount);
                }
            }

        }

        if (payrollRunDTO.getCompanyAdjustmentSubmissionDTOs() != null && payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().size() > 0) {
            for (CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO : payrollRunDTO.getCompanyAdjustmentSubmissionDTOs()) {
                // todo update for assisted submissions
                for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
                    Law law = Application.findById(com.intuit.sbd.payroll.psp.domain.Law.class, liabilityAdjustmentDTO.getLawId());

                    SpcfDecimal lawAmount = liabilityAdjustmentDTO.getAmount();

                    if (lawAmount != null) {
                        if (lawAmountMap.get(law.getLawId()) != null) {
                            lawAmount = lawAmount.add(lawAmountMap.get(law.getLawId()));
                        }
                        lawAmountMap.put(law.getLawId(), lawAmount);
                    }
                }
            }
        }
        return lawAmountMap;
    }

    public void createPaycheckList(PayrollRunDTO payrollRunDTO, ProcessResult validationResult, Company company, String transmissionId, boolean bShouldCreatePaycheckEvents) {
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        String sourceCompanyId = company.getSourceCompanyId();
        Map<String, ProcessResult> paycheckIdList = new HashMap<String, ProcessResult>();

        boolean allowDuplicatePaycheckIdsIfStatusIsCancelled = SourcePayrollParameter.findSourcePayrollParameter(sourceSystemCd,
                SourcePayrollParameterCode.AllowDuplicatePaycheckIdsIfStatusIsCancelled).getParameterValue().equals("1");

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            // Check if Payroll Entities' IDs are Unique
            String sourcePaycheckId = paycheckDTO.getPaycheckId();

            // Check if paycheck Ids are duplicated in the request
            if (paycheckIdList.containsKey(sourcePaycheckId)) {
                paycheckIdList.get(sourcePaycheckId).getMessages().DuplicatePaycheckId(EntityName.Paycheck, sourcePaycheckId,
                        sourcePaycheckId, sourceSystemCd.toString(), sourceCompanyId);
            } else {
                paycheckIdList.put(sourcePaycheckId, new ProcessResult());
            }
        }


        DomainEntitySet<Paycheck> paychecks = Paycheck.findPaychecks(company, paycheckIdList.keySet());
        for (Paycheck paycheck : paychecks) {
            if (!allowDuplicatePaycheckIdsIfStatusIsCancelled || paycheck.getStatus() == PaycheckStatusCode.Active) {
                paycheckIdList.get(paycheck.getSourcePaycheckId()).getMessages().PayCheckAlreadyExists(EntityName.Paycheck, paycheck.getSourcePaycheckId(),
                        paycheck.getSourcePaycheckId(), sourceSystemCd.toString(), sourceCompanyId);
            }
        }

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            Employee employee = Employee.findEmployee(company, paycheckDTO.getEmployeeId());
            if (employee == null) {
                paycheckIdList.get(paycheckDTO.getPaycheckId()).getMessages().EmployeeDoesNotExist(EntityName.Employee, paycheckDTO.getEmployeeId(),
                        sourceSystemCd.toString(), sourceCompanyId, paycheckDTO.getEmployeeId());
            } else {
                if (company.getSourceSystemCd() != SourceSystemCode.QBDT && employee.getStatusCd() != EmployeeStatus.Active) {
                    paycheckIdList.get(paycheckDTO.getPaycheckId()).getMessages().EmployeeNotActive(EntityName.Employee, paycheckDTO.getEmployeeId(),
                            sourceSystemCd.toString(), sourceCompanyId, paycheckDTO.getEmployeeId());
                }
            }

            if (bShouldCreatePaycheckEvents) {
                CompanyEvent.updateInvalidPaycheckInformationEvents(company, paycheckDTO.getEmployeeId(), paycheckDTO.getPaycheckId(), transmissionId, paycheckIdList.get(paycheckDTO.getPaycheckId()));
            }

            for (ProcessResult processResult : paycheckIdList.values()) {
                validationResult.merge(processResult);
            }
        }
    }

    public void createPayrollRejectEvents(final Company company, final PayrollRunDTO payrollRunDTO, final ProcessResult validationResult) {
        if (validationResult.isSuccess() || validationResult.getErrorMessages().size() == 0)
            return;

        final ProcessResult finalValidationResult = validationResult;
        PayrollServices.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                Company localCompany = Application.findById(Company.class, company.getId());

                StringBuilder result = new StringBuilder();
                final String newLine = System.getProperty("line.separator");
                result.append("Process Result");
                result.append(newLine);
                result.append(" Success: ");
                result.append(finalValidationResult.isSuccess());
                result.append(newLine);
                result.append(" Messages: ");
                result.append(newLine);
                for (Message currMessage : finalValidationResult.getErrorMessages()) {
                    result.append(currMessage + " ");
                }

                CompanyEvent.createPayrollRejectEvent(localCompany,
                        payrollRunDTO.getPayrollTXBatchId(),
                        null,
                        result.toString(),
                        null,
                        null);

                return new ProcessResult();
            }
        });
    }


    public Map<Law, SpcfDecimal> getLiabilityBalances(PayrollRun payroll) {
        return LiabilityBalances.getLiabilityBalances(payroll, false, false);
    }

    /**
     * Checks to see if the payroll has just one check and that that check is for a terminated employee
     *
     * @param pPayroll Payroll DTO
     * @return true if this is a termination check; false otherwise
     */
    public boolean isTerminationCheckPayroll(Company pCompany, PayrollRunDTO pPayroll) {
        boolean isTerminatedCheckPayroll = false;
        if (pPayroll.getPaychecks().size() == 1) {
            PaycheckDTO paycheck = pPayroll.getPaychecks().iterator().next();
            String sourceEmployeeId = paycheck.getEmployeeId();
            Employee givenEmployee = Employee.findEmployee(pCompany, sourceEmployeeId);
            if (givenEmployee.isTerminated()) {
                isTerminatedCheckPayroll = true;
            }
        }
        return isTerminatedCheckPayroll;
    }

    public static boolean shouldProcess401kForPayroll(Company pCompany, PayrollRunDTO pPayrollRunDTO) {
        if (pPayrollRunDTO == null || pCompany == null || pPayrollRunDTO.getTargetPayrollTXDate() == null) {
            return false;
        }

        boolean isCompanyActiveOn401k = pCompany.isCompanyOnService(ServiceCode.ThirdParty401k);
        boolean wasCompanyOnService = CompanyService.wasCompanyOnServiceForDate(pCompany, ServiceCode.ThirdParty401k, DateDTO.convertToSpcfCalendar(pPayrollRunDTO.getTargetPayrollTXDate()));
        boolean payrollContainsDD = anyDDTransactionsInPayroll(pPayrollRunDTO);

        return isCompanyActiveOn401k && wasCompanyOnService && !payrollContainsDD;
    }

    public static boolean shouldProcessWorkersCompForPayroll(Company company, PayrollRunDTO payrollRunDto) {
        if (payrollRunDto == null || company == null || payrollRunDto.getTargetPayrollTXDate() == null) {
            return false;
        }

        boolean isCompanyActiveOnWorkersComp = company.isCompanyOnService(ServiceCode.WorkersComp);
        boolean wasCompanyOnService = CompanyService.wasCompanyOnServiceForDate(company, ServiceCode.WorkersComp, DateDTO.convertToSpcfCalendar(payrollRunDto.getTargetPayrollTXDate()));
        return isCompanyActiveOnWorkersComp && wasCompanyOnService;
    }

    public static boolean anyDDTransactionsInPayroll(PayrollRunDTO pPayrollRunDTO) {
        Collection<PaycheckDTO> paychecks = pPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            List<DDTransactionDTO> ddTxnsForPaycheck = currPaycheck.getDdTransactions();
            if (ddTxnsForPaycheck != null && ddTxnsForPaycheck.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static ProcessResult validatePayrollItemsExistForLineItems(Company pCompany, PaycheckDTO pPaycheckDTO) {
        ProcessResult validationResult = new ProcessResult();
        Collection<CompensationTransactionDTO> compensationItems = pPaycheckDTO.getCompensationTransactions();
        if (compensationItems != null && compensationItems.size() > 0) {
            for (CompensationTransactionDTO currentCompensation : compensationItems) {
                String sourcePayrollItemId = currentCompensation.getSourcePayrollItemId();
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, sourcePayrollItemId);
                if (companyPayrollItem == null) {
                    validationResult.getMessages().PayrollItemDoesNotExist(EntityName.PayrollItem,
                            sourcePayrollItemId, pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), sourcePayrollItemId);
                }
            }
        }

        Collection<DeductionTransactionDTO> deductionItems = pPaycheckDTO.getDeductionTransactions();
        if (deductionItems != null && deductionItems.size() > 0) {
            for (DeductionTransactionDTO currentDeduction : deductionItems) {
                String sourcePayrollItemId = currentDeduction.getSourcePayrollItemId();
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, sourcePayrollItemId);
                if (companyPayrollItem == null) {
                    validationResult.getMessages().PayrollItemDoesNotExist(EntityName.PayrollItem,
                            sourcePayrollItemId, pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), sourcePayrollItemId);
                }
            }
        }

        Collection<EmployerContributionTransactionDTO> employerContributionItems = pPaycheckDTO.getEmployerContributionTransactions();
        if (employerContributionItems != null && employerContributionItems.size() > 0) {
            for (EmployerContributionTransactionDTO currentEmployerContribution : employerContributionItems) {
                String sourcePayrollItemId = currentEmployerContribution.getSourcePayrollItemId();
                CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(pCompany, sourcePayrollItemId);
                if (companyPayrollItem == null) {
                    validationResult.getMessages().PayrollItemDoesNotExist(EntityName.PayrollItem,
                            sourcePayrollItemId, pCompany.getSourceSystemCd().toString(), pCompany.getSourceCompanyId(), sourcePayrollItemId);
                }
            }
        }

        return validationResult;
    }

    private static void sendBackDateEmail(String pEmailMessage) {
        try {
            String toEmailAddress = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_backdate_received_notify");
            if (toEmailAddress != null && !toEmailAddress.contains("no_reply")) {
                MailSender.sendEmail(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_mail_server"),
                        toEmailAddress, // to
                        "psp_payroll_submit@intuit.com", // from
                        "Back Date Received - " + (Application.isProdEnvironment() ? "PROD" : "Non-PROD"),
                        pEmailMessage);
            } else {
                Application.getLogger(PayrollSubmitHelper.class).warn("Failed to send email message for backdated payroll. " + pEmailMessage);
            }
        } catch (Exception e) {
            Application.getLogger(PayrollSubmitHelper.class).error("Failed to send email message for backdated payroll. " + pEmailMessage, e);
        }
    }
}
