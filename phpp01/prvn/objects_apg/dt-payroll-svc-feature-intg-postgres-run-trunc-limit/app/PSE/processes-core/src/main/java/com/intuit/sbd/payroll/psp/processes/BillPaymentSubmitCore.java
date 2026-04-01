/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/BillPaymentSubmitCore.java#4 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;

/**
 * @author Marcela Villani
 */
public class BillPaymentSubmitCore extends Process implements IProcess {

    private static final SpcfLogger logger = PayrollServices.getLogger(BillPaymentSubmitCore.class);

    private Map<DateDTO, ArrayList<BillPaymentDTO>> billPayments;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private Company company;
    private ArrayList<PayrollRunDTO> payrollRunDTOs = new ArrayList<PayrollRunDTO>();
    private ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>();
    private CheckBPLimits checkBPLimits = null;

    public BillPaymentSubmitCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Collection<BillPaymentDTO> pBillPayments) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;

        // Group BillPayments by deposit date
        billPayments = new TreeMap<DateDTO, ArrayList<BillPaymentDTO>>();
        for (BillPaymentDTO billPaymentDTO : pBillPayments) {
            if (!billPayments.containsKey(billPaymentDTO.getDepositDate())) {
                billPayments.put(billPaymentDTO.getDepositDate(), new ArrayList<BillPaymentDTO>());
            }
            billPayments.get(billPaymentDTO.getDepositDate()).add(billPaymentDTO);
        }
    }

    public Collection<PayrollRun> getPayrollRuns() {
        return payrollRuns;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        // Check if Company is Active on Bill Payment Service
        if (!sourceSystemCd.equals(SourceSystemCode.IOP) &&
                !company.isCompanyOnService(ServiceCode.BillPayment)) {
            validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, sourceCompanyId,
                                                                        sourceSystemCd.toString(), sourceCompanyId, ServiceCode.BillPayment.toString());
        }

        // NOC Verification
        // Check if there is any OPEN Payee NOC returns or regular returns (R* Code) for the company
        HashMap<Payee, DomainEntitySet<TransactionReturn>> payeeBANOCMap = null;
        DomainEntitySet<TransactionReturn> nocReturns = TransactionReturn.findTxnRetsForReturnType(company,
                                                                                                   TransactionReturn.ReturnTypeCodes.NOTICE_OF_CHANGE,
                                                                                                   TransactionReturnStatusCode.Open);
        DomainEntitySet<TransactionReturn> txReturns = TransactionReturn.findTxnRetsForReturnType(company,
                                                                                                  TransactionReturn.ReturnTypeCodes.RETURN,
                                                                                                  TransactionReturnStatusCode.Open);
        nocReturns.addAll(txReturns);

        if (nocReturns.size() > 0) {
            // Build a list with the payee bank accounts that have NOC returns
            payeeBANOCMap = new HashMap<Payee, DomainEntitySet<TransactionReturn>>();
            for (TransactionReturn nocReturn : nocReturns) {
                DomainEntitySet<TransactionReturn> nocReturnsForBA;
                FinancialTransaction payeeNOCTransaction = TransactionReturn.findFirstFinancialTransaction(nocReturn);
                PayeeBankAccount payeeBA = payeeNOCTransaction.getPayeeBankAccount();
                if (payeeBA != null) {
                    nocReturnsForBA = payeeBANOCMap.get(payeeBA.getPayee());
                    if (nocReturnsForBA == null) {
                        nocReturnsForBA = new DomainEntitySet<TransactionReturn>();
                        payeeBANOCMap.put(payeeBA.getPayee(), nocReturnsForBA);
                    }
                    nocReturnsForBA.add(nocReturn);
                }
            }
        }

        // Validate DTO
        // Validate Deposit Dates  - verify if not too far in the future according to SourcePayrollParameter MaxWarehouseTransactionDays
        // Create a map of all different deposit dates to create the payrollrun entity later

        int numberOfDays = SourcePayrollParameter.findIntValue(sourceSystemCd, SourcePayrollParameterCode.MaxWarehouseTransactionDays);
        HashMap<String, String> billPaymentList = new HashMap<String, String>();
        HashMap<String, String> transactionList = new HashMap<String, String>();

        for (DateDTO dateDTO : billPayments.keySet()) {
            for (BillPaymentDTO billPaymentDTO : billPayments.get(dateDTO)) {
                validationResult.merge(billPaymentDTO.validateBillPaymentDTO());
                if (!validationResult.isSuccess()) {
                    return validationResult;
                }

                SpcfCalendar depositDate = DateDTO.convertToSpcfCalendar(billPaymentDTO.getDepositDate());
                if (CalendarUtils.getDifferenceInDays(depositDate, PSPDate.getPSPTime()) > numberOfDays) {
                    validationResult.getMessages().DepositDateTooFarInTheFuture(EntityName.BillPayment, billPaymentDTO.getBillPaymentId(), Integer.toString(numberOfDays));
                }

                // Check if Payroll Entities' IDs are Unique
                String sourcePaymentId = billPaymentDTO.getBillPaymentId();
                BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourcePaymentId);
                if (billPayment != null) {
                    validationResult.getMessages().BillPaymentAlreadyExists(EntityName.BillPayment,
                                                                            sourcePaymentId,
                                                                            sourcePaymentId,
                                                                            sourceSystemCd.toString(),
                                                                            sourceCompanyId);
                }

                if (billPaymentList.containsKey(sourcePaymentId)) {
                    validationResult.getMessages().DuplicatePaymentId(EntityName.BillPayment,
                                                                      sourcePaymentId,
                                                                      sourcePaymentId,
                                                                      sourceSystemCd.toString(),
                                                                      sourceCompanyId);
                } else {
                    billPaymentList.put(sourcePaymentId, sourcePaymentId);
                }

                // Check if any Split ID Already Exists
                for (BillPaymentSplitDTO split : billPaymentDTO.getPaymentTransactions()) {
                    String splitId = split.getBillPaymentSplitId();

                    // Check if the transaction id is duplicated in the request
                    if (transactionList.containsKey(splitId)) {
                        validationResult.getMessages().DuplicatePaymentSplitId(EntityName.BillPayment,
                                                                               sourcePaymentId,
                                                                               splitId,
                                                                               sourceSystemCd.toString(),
                                                                               sourceCompanyId);
                    } else {
                        transactionList.put(splitId, splitId);
                    }
                }
            }
        }

        HashMap<SpcfCalendar, SpcfMoney> depositDateAmountsList = new HashMap<SpcfCalendar, SpcfMoney>();

        // Create one PayrollRunDTO for a group of bill payments with the same deposit date.
        // A separate ERDDDebit needs to be created for each bill payment submitted
        CompanyService companyService = CompanyService.findCompanyService(company, ServiceCode.BillPayment);
        for (DateDTO dateDTO : billPayments.keySet()) {
            SpcfCalendar depositDate = DateDTO.convertToSpcfCalendar(dateDTO);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

            // Generate an Unique Id for the PayrollRun
            final String payrollRunId = SpcfUniqueId.generateRandomUniqueIdString();
            payrollRunDTO.setPayrollTXBatchId(payrollRunId);

            // Set the Transaction Date
            payrollRunDTO.setTargetPayrollTXDate(dateDTO);

            // Set the Company Bank Account
            if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
                if (companyBankAccount == null) {
                    validationResult.getMessages().CompanyActiveBankAccountNotFound(EntityName.CompanyBankAccount, sourceCompanyId, sourceSystemCd.toString(), sourceCompanyId, "");
                } else {
                    BankAccountDTO bankAccountDTO = new BankAccountDTO();
                    BankAccount bankAccount = companyBankAccount.getBankAccount();
                    bankAccountDTO.setRoutingNumber(bankAccount.getRoutingNumber());
                    bankAccountDTO.setAccountNumber(bankAccount.getAccountNumber());
                    bankAccountDTO.setAccountType(bankAccount.getAccountTypeCd());
                    bankAccountDTO.setBankName(bankAccount.getBankName());
                    CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
                    companyBankAccountDTO.setBankAccountDTO(bankAccountDTO);
                    companyBankAccountDTO.setCompanyBankAccountID(companyBankAccount.getSourceBankAccountId());
                    companyBankAccountDTO.setSourceBankAccountName(companyBankAccount.getSourceBankAccountName());
                    ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
                    serviceBankAccountDTO.setServiceCode(ServiceCode.BillPayment);
                    serviceBankAccountDTO.setCompanyBankAccount(companyBankAccountDTO);
                    ArrayList<ServiceBankAccountDTO> serviceBankAccountDTOs = new ArrayList<ServiceBankAccountDTO>(1);
                    serviceBankAccountDTOs.add(serviceBankAccountDTO);
                    payrollRunDTO.setCompanyBankAccounts(serviceBankAccountDTOs);
                    payrollRunDTOs.add(payrollRunDTO);
                }
            } else {
                payrollRunDTOs.add(payrollRunDTO);
            }

            for (BillPaymentDTO billPaymentDTO : billPayments.get(dateDTO)) {
                //Build a Payee List with the amounts paid to each in order to check limits
                String payeeId = billPaymentDTO.getPayeeDTO().getSourcePayeeId();
                Payee payee = Payee.findPayee(company, payeeId);

                if (payee == null) {
                    validationResult.getMessages().PayeeDoesNotExist(EntityName.Payee, payeeId, sourceSystemCd.toString(), sourceCompanyId, payeeId);
                }

                if (!depositDateAmountsList.containsKey(depositDate)) {
                    depositDateAmountsList.put(depositDate, billPaymentDTO.getAmount());
                } else {
                    SpcfMoney newAmount = depositDateAmountsList.get(depositDate);
                    newAmount = new SpcfMoney(newAmount.add(billPaymentDTO.getAmount()));
                    depositDateAmountsList.put(depositDate, newAmount);
                }

                // Payee payee = Payee.findPayee(company, billPaymentDTO.getPayeeDTO().getSourcePayeeId());
                for (BillPaymentSplitDTO split : billPaymentDTO.getPaymentTransactions()) {
                    // Check if payeeBankAccount has a NOC associated with it and the account information has not changed for this
                    // payroll submission
                    if (payeeBANOCMap != null && payeeBANOCMap.containsKey(payee)) {
                        // Find the most recent NOC Events and Return Events (R*) associated with the employee bank account
                        DomainEntitySet<TransactionReturn> nocReturnsForBA = payeeBANOCMap.get(payee);
                        for (Iterator<TransactionReturn> iterator = nocReturnsForBA.iterator(); iterator.hasNext(); ) {
                            TransactionReturn transactionReturn = iterator.next();
                            FinancialTransaction financialTransaction = TransactionReturn.findFirstFinancialTransaction(transactionReturn);
                            final PayeeBankAccount payeeBankAccount = financialTransaction.getPayeeBankAccount();
                            DomainEntitySet<CompanyEventDetail> nocEventDetails = CompanyEvent.findCompanyEventDetails(company,
                                                                                                                       EventTypeCode.NOC,
                                                                                                                       EventDetailTypeCode.PayeeBankAccountId,
                                                                                                                       payeeBankAccount.getId().toString());
                            DomainEntitySet<CompanyEventDetail> returnEventDetails = CompanyEvent.findCompanyEventDetails(company,
                                                                                                                          EventTypeCode.DDReject,
                                                                                                                          EventDetailTypeCode.PayeeBankAccountId,
                                                                                                                          payeeBankAccount.getId().toString());
                            nocEventDetails.addAll(returnEventDetails);

                            if (nocEventDetails.size() > 0) {
                                CompanyEvent nocEvent = nocEventDetails.get(0).getCompanyEvent();

                                if (isC04(nocEventDetails)) {
                                    final String eeSourceBAId = payeeBankAccount.getSourceBankAccountId();
                                    final DomainEntitySet<TransactionReturn> finalTxnList = payeeBANOCMap.get(payee);
                                    final SpcfUniqueId eeId = payeeBankAccount.getId();
                                    final String eeSourceId = payeeBankAccount.getPayee().getSourcePayeeId();

                                    PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                                        public ProcessResult transaction() {
                                            // Resolve NOC Returns
                                            for (TransactionReturn transactionReturn : finalTxnList) {
                                                TransactionReturn localTxnReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
                                                localTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
                                            }
                                            // Inactivate NOC events
                                            Company localCompany = Application.findById(Company.class, company.getId());

                                            DomainEntitySet<PayeeBankAccount> eeBankAccounts = PayeeBankAccount.findPayeeBankAccounts(localCompany, eeSourceId, eeSourceBAId);
                                            for (PayeeBankAccount payeeBA : eeBankAccounts) {
                                                String payeeBAId = payeeBA.getId().toString();
                                                DomainEntitySet<CompanyEventDetail> localNocEventDetails = CompanyEvent.findCompanyEventDetails(
                                                        localCompany, EventTypeCode.NOC, EventDetailTypeCode.PayeeBankAccountId, payeeBAId);
                                                for (CompanyEventDetail eventDetail : localNocEventDetails) {
                                                    eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                                                }
                                                // Inactivate PayrollSubmittedWithPendingNOC events
                                                localNocEventDetails = CompanyEvent.findCompanyEventDetails(localCompany,
                                                                                                            EventTypeCode.PayrollSubmittedWithPendingNOC,
                                                                                                            EventDetailTypeCode.PayeeBankAccountId,
                                                                                                            payeeBAId);
                                                for (CompanyEventDetail eventDetail : localNocEventDetails) {
                                                    eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                                                }
                                            }
                                            return new ProcessResult();
                                        }
                                    });

                                    // Special scenario for C04 - if the only active NOC associated with the ee bank account is a C04, meaning a name change, the NOC will
                                    // be automatically resolved and the company will be not put on hold
                                    //  Create PayrollSubmitWithNOCPendingEvent
                                    PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                                        public ProcessResult transaction() {
                                            Company localCompany = Application.findById(Company.class, company.getId());
                                            CompanyEvent.createPayrollSubmittedWithPendingNOC(localCompany, payrollRunId, eeId, payeeBankAccount.getSourceBankAccountId());
                                            return new ProcessResult();
                                        }
                                    });
                                    // Issue warning
                                    validationResult.getMessages().PaymentSubmittedWithPendingNOC(EntityName.PayrollRun, sourceCompanyId, "1", payeeBankAccount.getPayee().getName());
                                    iterator.remove();
                                } else if (bankAccountHasNewInformation(split.getPayeeBankAccount(), nocEvent)) {

                                    //Update the ACHBankAccountType from the pending NOC
                                    DomainEntitySet<CompanyEventDetail> companyEventDetails = nocEvent.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.NewAchAccountType);
                                    if (companyEventDetails.isNotEmpty()) {
                                        ACHBankAccountType achBankAccountType = ACHBankAccountType.valueOf(companyEventDetails.getFirst().getValue());
                                        split.getPayeeBankAccount().getBankAccount().setAchAccountType(achBankAccountType);
                                    }

                                    resolveReturnEvents(transactionReturn, nocEvent);
                                    iterator.remove();

                                // If this bank account matches the one from the NOC or DD Reject event, consider it an error.
                                } else if (!bankAccountInfoChanged(split.getPayeeBankAccount(), nocEvent)) {

                                    // DD Reject (R* Code)
                                    if (nocEvent.getEventTypeCd().equals(EventTypeCode.DDReject)) {
                                        validationResult.getMessages().UnresolvedEECreditReturnExists(EntityName.PayrollRun, sourceCompanyId, payeeBankAccount.getPayee().getName());

                                        final SpcfUniqueId eeId = payeeBankAccount.getId();

                                        //  Create createPayrollSubmittedWithEmployeeWithPendingReturn
                                        PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                                            public ProcessResult transaction() {
                                                Company localCompany = Application.findById(Company.class, company.getId());
                                                CompanyEvent.createPayrollSubmittedWithEmployeeWithPendingReturn(localCompany, payrollRunId, eeId);
                                                return new ProcessResult();
                                            }
                                        });
                                    // NOC (C* Code)
                                    } else {
                                        validationResult.getMessages().UnresolvedNOCExists(EntityName.PayrollRun, sourceCompanyId, payeeBankAccount.getPayee().getName());

                                        final SpcfUniqueId eeId = payeeBankAccount.getId();

                                        //  Create PayrollSubmitWithNOCPendingEvent
                                        PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                                            public ProcessResult transaction() {
                                                Company localCompany = Application.findById(Company.class, company.getId());
                                                CompanyEvent.createPayrollSubmittedWithPendingNOC(localCompany, payrollRunId, eeId, payeeBankAccount.getSourceBankAccountId());
                                                return new ProcessResult();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }

                // If there are still returns in the map for this employee mark them as resolved
                if (payeeBANOCMap != null && payeeBANOCMap.containsKey(payee)) {
                    for (TransactionReturn transactionReturn : payeeBANOCMap.get(payee).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Open))) {
                        FinancialTransaction payeeNOCTransaction = TransactionReturn.findFirstFinancialTransaction(transactionReturn);
                        PayeeBankAccount payeeBA = payeeNOCTransaction.getPayeeBankAccount();
                        DomainEntitySet<CompanyEventDetail> nocEventDetails = CompanyEvent.findCompanyEventDetails(company,
                                                                                                                   EventTypeCode.NOC,
                                                                                                                   EventDetailTypeCode.PayeeBankAccountId,
                                                                                                                   payeeBA.getId().toString());
                        DomainEntitySet<CompanyEventDetail> returnEventDetails = CompanyEvent.findCompanyEventDetails(company,
                                                                                                                      EventTypeCode.DDReject,
                                                                                                                      EventDetailTypeCode.PayeeBankAccountId,
                                                                                                                      payeeBA.getId().toString());
                        nocEventDetails.addAll(returnEventDetails);
                        if (nocEventDetails.size() > 0) {
                            CompanyEvent nocEvent = nocEventDetails.get(0).getCompanyEvent();
                            if(nocEvent.getStatusCd() == CompanyEventStatus.Active) {
                                // must have completely different information, resolve the NOC
                                resolveReturnEvents(transactionReturn, nocEvent);
                            }
                        }
                    }
                    payeeBANOCMap.remove(payee);
                }
            } // Bill Payments Loop

            if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                checkBPLimits = new CheckBPLimits(billPayments.get(dateDTO), sourceSystemCd, sourceCompanyId, ServiceCode.BillPayment);
                validationResult.merge(checkBPLimits.validate());
            }
        }
        // Check if Submitting a payroll is allowed based on the current Service Status
        if (!company.isAllowedCapability(SystemCapabilityCode.SubmitPayment)){
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.SubmitPayment.toString());
        }

        for (final PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            SpcfCalendar depositDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());
            SpcfCalendar depositSettlementDate = depositDate;

            if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                boolean allowBackdatedPayrolls = SourcePayrollParameter.findSourcePayrollParameter(sourceSystemCd,
                                                                                                   SourcePayrollParameterCode.AllowBackdatedPayrolls).getParameterValue().equals("1");

                // Get the next valid DepositSettlementDate based on the deposit date
                if (!allowBackdatedPayrolls) {
                    if (CalendarUtils.isWeekendOrHoliday(depositDate)) {
                        validationResult.getMessages().DepositDateOnHolidayOrWeekend(EntityName.BillPayment, sourceCompanyId, depositDate.format("yyyy-MM-dd"));
                        return validationResult;
                    }
                }

                SpcfCalendar currentDateWithoutTime = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
                if (depositSettlementDate.before(currentDateWithoutTime)) {
                    if (!allowBackdatedPayrolls) {
                        validationResult.getMessages().InvalidDepositDate(EntityName.BillPayment, sourceCompanyId,
                                                                          company.getOffloadGroup().getCutoffTime(), Integer.toString(companyService.getEffectiveFundingModel().getNumberOfFundingDays()),
                                                                          depositDate.format("yyyy-MM-dd"));
                        return validationResult;
                    }
                }

                if (allowBackdatedPayrolls) {
                    depositSettlementDate = company.getNextValidPaycheckDepositDate(depositSettlementDate);
                }
            }

            // Set the settlement Date
            payrollRunDTO.setSettlementDate(new DateDTO(depositSettlementDate));
        }

        // Create Payroll Reject Events for the all the validation errors
        if (!validationResult.isSuccess()){
            PayrollSubmitHelper.getInstance().createPayrollRejectEvents(company, new PayrollRunDTO(), validationResult);
        }

        return validationResult;
    }

    public ProcessResult process() {
        // Create Payroll Runs
        ProcessResult processResult = new ProcessResult();
        for (DateDTO dateDTO : billPayments.keySet()) {
            PayrollRunDTO payrollRunDTO = getPayrollRunDTo(dateDTO);

            PayrollRun payrollRun = new PayrollRun();

            // Associate Company and Payroll Run
            payrollRun.setCompany(company);
            payrollRun.setFundingModel(company.getFundingModel().getFundingModelCd());

            // Set PayrollRun date
            payrollRun.setPayrollRunDate(PSPDate.getPSPTime());

            //Set paycheck date as passed in from the DTO
            SpcfCalendar paycheckDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());
            payrollRun.setPaycheckDate(paycheckDate);

            // Set the next valid depositSettlementDate based on the deposit date
            payrollRun.setPaycheckSettlementDate(DateDTO.convertToSpcfCalendar(payrollRunDTO.getSettlementDate()));

            // Set status to "Pending"
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

            // Set Source PayrollRun Id and Payroll Run Type
            payrollRun.setSourcePayRunId(SpcfUniqueId.generateRandomUniqueIdString());
            payrollRun.setPayrollRunType(PayrollType.BillPayment);
            payrollRun = Application.save(payrollRun);

            boolean createEvents = false;
            int numberOfBillPayments = 0;
            SpcfCalendar settlementDate = null;
            SpcfDecimal payrollDirectDepositAmount = SpcfMoney.ZERO;
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);

            // Create BillPayments
            for (BillPaymentDTO billPaymentDTO : billPayments.get(dateDTO)) {
                SpcfDecimal billPaymentNetAmount = new SpcfMoney("0.0");

                // Find the Bill Payments with the same date as the payroll run
                if (billPaymentDTO.getDepositDate().equals(payrollRunDTO.getTargetPayrollTXDate())) {
                    BillPayment billPayment = createBillPayment(payrollRun, billPaymentDTO);
                    billPayment = Application.save(billPayment);

                    billPaymentNetAmount = billPaymentNetAmount.add(billPaymentDTO.getAmount());

                    // Add Employer Debit Financial Transaction and corresponding ledger entries
                    SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentNetAmount);

                    FinancialTransaction erDebitFT = null;

                    if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                        // Only create financial transactions and ledger entries for PSP so money moves
                        CompanyService service = CompanyService.findCompanyService(company, ServiceCode.BillPayment);
                        erDebitFT = FinancialTransaction.createERDebitTransaction(payrollRun, companyBankAccount,
                                                                                  TransactionTypeCode.EmployerDdDebit, billPaymentAmount, SettlementType.ACH, null, service);

                        settlementDate = erDebitFT.getSettlementDate().toLocal();
                    }

                    // Create Bill Payment  Splits
                    for (BillPaymentSplitDTO billPaymentSplitDTO : billPaymentDTO.getPaymentTransactions()) {
                        BillPaymentSplit billPaymentSplit = createBillPaymentSplit(billPayment, billPaymentSplitDTO, processResult);

                        if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                            // Only create financial transactions and ledger entries for PSP so money moves
                            FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(billPaymentSplit);
                            financialTransaction.setRelatableTransaction(erDebitFT);
                            Application.save(financialTransaction);

                            billPaymentSplit.setFinancialTransaction(financialTransaction);
                        }

                        Application.save(billPaymentSplit);
                    }
                    numberOfBillPayments++;
                    payrollDirectDepositAmount = payrollDirectDepositAmount.add(billPaymentNetAmount);
                }

                // Save the PayrollRun
                payrollRun = Application.save(payrollRun);
                createEvents = true;
            }

            if (createEvents) {
                // Create Bill Payment Received Event
                CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.BillPaymentReceived);
            }
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FirstPayrollReceived, CompanyEventStatus.Active, null, null, true);
            if(CompanyEvent.hasNoCompanyEventsWithServiceCodeDetails(companyEvents) && !CompanyEvent.isServiceCodeEventDetailPresent(companyEvents, ServiceCode.BillPayment.toString())){
                CompanyEvent.createPayrollRunEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), EventTypeCode.FirstPayrollReceived, ServiceCode.BillPayment);
            }

            payrollRun.setPayrollDirectDepositAmount(new SpcfMoney(payrollDirectDepositAmount));

            if (!sourceSystemCd.equals(SourceSystemCode.IOP)) {
                // See which BP Offering the company has.
                CompanyOffering bpOffering = company.getOffering(ServiceCode.BillPayment);
                if (bpOffering != null) {
                    // add a PerPayment charge for these Bill Payments
                    BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerPayment, numberOfBillPayments,
                                                      settlementDate, bpOffering.getOffering().getOfferingCode());
                }
            }

            if (sourceSystemCd.equals(SourceSystemCode.IOP)) {
                // All payrolls coming in from IOP are already done
                payrollRun.setPayrollRunStatus(PayrollStatus.Complete, false);
            }

            // Save the PayrollRun
            payrollRun = Application.save(payrollRun);

            payrollRuns.add(payrollRun);

            if (checkBPLimits != null) {
                processResult.merge(checkBPLimits.process());
            }

        }

        return processResult;
    }

    private BillPayment createBillPayment(PayrollRun pPayrollRun, BillPaymentDTO pBillPaymentDTO) {
        BillPayment billPayment = new BillPayment();


        Payee payee = Payee.findPayee(company, pBillPaymentDTO.getPayeeDTO().getSourcePayeeId());

        // Associate BillPayment and Payee
        billPayment.setPayee(payee);

        // Associate BillPayment and Payroll Run
        billPayment.setPayrollRun(pPayrollRun);
        pPayrollRun.addBillPayment(billPayment);


        billPayment.setAmount(pBillPaymentDTO.getAmount());
        billPayment.setSourceId(pBillPaymentDTO.getBillPaymentId());

        billPayment.setMemo(pBillPaymentDTO.getMemo());
        billPayment.setTransactionType(pBillPaymentDTO.getTransactionType());
        billPayment.setSessionId(pBillPaymentDTO.getSessionId());
        return billPayment;
    }

    private PayrollRunDTO getPayrollRunDTo(DateDTO pDepositDate) {
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            if (pDepositDate.equals(payrollRunDTO.getTargetPayrollTXDate())) {
                return payrollRunDTO;
            }
        }
        return null;
    }

    private BillPaymentSplit createBillPaymentSplit(BillPayment pBillPayment, BillPaymentSplitDTO pBillPaymentSplitDTO, ProcessResult pProcessResult) {
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        String sourceCompanyId = company.getSourceCompanyId();
        BillPaymentSplit billPaymentSplit = new BillPaymentSplit();

        //  Add or Update Payee Bank Account
        AddOrUpdatePayeeBankAccountCore addPayeeBankAccount = new AddOrUpdatePayeeBankAccountCore(sourceSystemCd,
                                                                                                  sourceCompanyId, pBillPayment.getPayee().getSourcePayeeId(),
                                                                                                  pBillPaymentSplitDTO.getPayeeBankAccount());
        pProcessResult.merge(addPayeeBankAccount.execute());
        PayeeBankAccount payeeBankAccount = addPayeeBankAccount.getPayeeBankAccount();


        billPaymentSplit.setPayeeBankAccount(payeeBankAccount);

        //  Set Amount
        billPaymentSplit.setAmount(SpcfUtils.convertToSpcfMoney(pBillPaymentSplitDTO.getAmount()));

        // Set Source  Id
        if (pBillPaymentSplitDTO.getBillPaymentSplitId() != null) {
            billPaymentSplit.setSourceId(pBillPaymentSplitDTO.getBillPaymentSplitId());
        } else {
            billPaymentSplit.setSourceId(SpcfUniqueId.generateRandomUniqueIdString());
        }

        billPaymentSplit.setReferenceNumber(pBillPaymentSplitDTO.getReferenceNumber());

        // Associate BillPaymentSplit and BillPayment
        billPaymentSplit.setBillPayment(pBillPayment);
        pBillPayment.addBillPaymentSplit(billPaymentSplit);

        return billPaymentSplit;
    }

    private boolean bankAccountInfoChanged(PayeeBankAccountDTO pPayeeBankAccountDTO, CompanyEvent pNOCEvent) {

        for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case OldAccountNumber:
                    if (!eventDetail.getValue().equals(pPayeeBankAccountDTO.getBankAccount().getAccountNumber()))
                        return true;
                    break;

                case OldRoutingNumber:
                    if (!eventDetail.getValue().equals(pPayeeBankAccountDTO.getBankAccount().getRoutingNumber()))
                        return true;
                    break;

                case OldAccountType:
                    if (!eventDetail.getValue().equals(pPayeeBankAccountDTO.getBankAccount().getAccountType().toString()))
                        return true;
                    break;
            }
        }

        return false;
    }

    private boolean bankAccountHasNewInformation(PayeeBankAccountDTO pPayeeBankAccountDTO, CompanyEvent pNOCEvent) {
        String newAccount = null;
        String newRouting = null;
        String newAccountType = null;

        // For NOCs, every provided field must be updated to the new value for the account to have been "updated".
        for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case NewAccountNumber:
                    newAccount = eventDetail.getValue();
                    break;
                case NewRoutingNumber:
                    newRouting = eventDetail.getValue();
                    break;
                case NewAccountType:
                    newAccountType = eventDetail.getValue();
                    break;
            }
        }

        BankAccountDTO bankAccountDTO = pPayeeBankAccountDTO.getBankAccount();

        return bankAccountDTO.getAccountNumber().equals(newAccount) &&
                bankAccountDTO.getRoutingNumber().equals(newRouting) &&
                bankAccountDTO.getAccountType().toString().equals(newAccountType);
    }

    private boolean isC04(DomainEntitySet<CompanyEventDetail> pNOCDetails) {
        if (pNOCDetails.size() == 1) {
            CompanyEvent nocEvent = pNOCDetails.get(0).getCompanyEvent();
            String nocReturnValue = nocEvent.getCompanyEventDetailValue(EventDetailTypeCode.ACHEventCd);
            return (nocReturnValue.equals("C04"));
        }
        return false;
    }

    private boolean isDDReject(DomainEntitySet<CompanyEventDetail> eventDetails) {
        if (eventDetails.size() == 1) {
            CompanyEvent rejectEvent = eventDetails.get(0).getCompanyEvent();
            return(EventTypeCode.DDReject.equals(rejectEvent.getEventTypeCd()));
        }
        return false;
    }

    private void resolveReturnEvents(TransactionReturn transactionReturn, CompanyEvent pNocEvent) {

        // Resolve Transaction Return
        transactionReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
        Application.save(transactionReturn);

        // Inactivate Return events
        for (CompanyEventDetail eventDetail : pNocEvent.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.PayeeBankAccountId)) {
            eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
            Application.save(eventDetail);

            // Inactivate PayrollSubmittedWithPendingNOC events
            for (CompanyEventDetail payrollSubmittedDetails : CompanyEvent.findCompanyEventDetails(pNocEvent.getCompany(), EventTypeCode.PayrollSubmittedWithPendingNOC, EventDetailTypeCode.PayeeBankAccountId, eventDetail.getValue())) {
                payrollSubmittedDetails.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                Application.save(eventDetail);
            }

            // Inactivate PayrollSubmittedWithEmployeeWithPendingReturn events
            for (CompanyEventDetail payrollSubmittedDetails : CompanyEvent.findCompanyEventDetails(pNocEvent.getCompany(), EventTypeCode.PayrollSubmittedWithEmployeeWithPendingReturn, EventDetailTypeCode.PayeeBankAccountId, eventDetail.getValue())) {
                payrollSubmittedDetails.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                Application.save(eventDetail);
            }
        }
    }
}
