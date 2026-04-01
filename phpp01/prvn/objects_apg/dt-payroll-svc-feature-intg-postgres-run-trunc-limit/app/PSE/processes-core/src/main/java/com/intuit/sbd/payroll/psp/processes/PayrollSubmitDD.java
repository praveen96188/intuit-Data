/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/PayrollSubmitDD.java#11 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
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
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * DD process for submitting transactions
 *
 * @author Dawn Martens
 */
public class PayrollSubmitDD extends Process implements IProcess {
    private static final String SERVICE_IDENTIFIER = "DD";

    private Company company;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private PayrollRunDTO payrollRunDTO;
    private DDCompanyServiceInfo ddCompanyServiceInfo;
    private PayrollRun payrollRun;

    public void setPayrollRun(PayrollRun pPayrollRun) {
        payrollRun = pPayrollRun;
    }

    public PayrollSubmitDD(PayrollRunDTO pPayrollRunDTO, SourceSystemCode pSourceSystem, String pSourceCompanyId) {
        sourceSystemCd = pSourceSystem;
        sourceCompanyId = pSourceCompanyId;
        payrollRunDTO = pPayrollRunDTO;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        SpcfDecimal accumulateAmount = SpcfMoney.ZERO;
        int ddCount = 0;
        int payCardChecks = 0;

        // never create financial transactions for dd in a balance file
        if(!payrollRunDTO.getBalanceFilePayroll() && payrollRun != null && !payrollRun.isHistoricalPayroll()) {

            // Iterate through paychecks and paycheck splits and create financial transactions
            // and ledger entries
            for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                boolean chargeForDD = paycheck.getPaycheckSplitCollection().size() > 0;
                boolean isPayCardCheck = paycheck.getPaycheckSplitCollection().size() > 0;

                // Iterate through Paycheck Splits and create corresponding financial transactions and ledger entries
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {

                    // Create financial transactions and ledger entries
                    if (paycheckSplit.getPaycheckSplitAmount().isGreaterThan(SpcfMoney.ZERO)) {
                        if (chargeForDD && !paycheckSplit.getEmployeeBankAccount().getBankAccount().isPayCardAccount()) {
                            ++ddCount;
                            chargeForDD = false;
                            isPayCardCheck = false;
                        }

                        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(paycheckSplit);

                        paycheckSplit.setFinancialTransaction(financialTransaction);

                        // Accumulate amounts to create Employer Debit Transaction
                        accumulateAmount = accumulateAmount.add(financialTransaction.getFinancialTransactionAmount());
                    }
                }

                if(isPayCardCheck) {
                    payCardChecks++;
                }
            }

            // Add Employer Debit Financial Transaction and corresponding ledger entries

            CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

            SpcfMoney payrollAmount = new SpcfMoney(accumulateAmount);
            CompanyService service = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
            FinancialTransaction erDebitFT = FinancialTransaction.createERDebitTransaction(payrollRun,
                                                                                           companyBankAccount,
                                                                                           TransactionTypeCode.EmployerDdDebit,
                                                                                           payrollAmount,
                                                                                           SettlementType.ACH,
                                                                                           null,
                                                                                           service);

            if (payCardChecks > 0) {
                CompanyEvent.createPayrollReceivedPayCardEvent(company, payrollRun.getSourcePayRunId(), payrollRun.getId(), payCardChecks);
            }

            //we'll sometimes need to create billing details with quantity of 0
            CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);
            OfferingCode offeringCode = companyOffering.getOffering().getOfferingCode();
            SpcfCalendar feeSettlementDate = erDebitFT.getSettlementDate().toLocal();

            //
            // Create DirectDepositFee transaction
            //
            if (ddCount > 0) {
                BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, ddCount, feeSettlementDate, offeringCode);
            }
        }

        if (ddCompanyServiceInfo.getStatusCd() == ServiceSubStatusCode.PendingFirstPayroll) {
            ServiceSubStatusCode nextServiceSubStatusCd = ddCompanyServiceInfo.getNextValidServiceStatus(ServiceSubStatusCode.PendingFirstPayroll);
            if (nextServiceSubStatusCd == null) {
                nextServiceSubStatusCd = ServiceSubStatusCode.ActiveCurrent;
            }
            ddCompanyServiceInfo.updateCompanyServiceStatus(nextServiceSubStatusCd);
        }

        payrollRun.setPayrollDirectDepositAmount(new SpcfMoney(accumulateAmount));

        Application.save(payrollRun);

        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Check if company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        //Check if any DD transactions are present
        boolean bAnyDDTransactions = anyDDTransactionsInPayroll(payrollRunDTO);
        if (bAnyDDTransactions) {

            //Validate company is on DD
            ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                    .findCompanyService(company, ServiceCode.DirectDeposit);
            if (ddCompanyServiceInfo == null) {
                validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        company.getSourceCompanyId(), company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), SERVICE_IDENTIFIER);
                return validationResult;
            }

            if (!ddCompanyServiceInfo.isAllowedCapability(SystemCapabilityCode.SubmitPayroll)) {
                validationResult.getMessages().ServiceOperationNotAllowed(
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId(), ServiceCode.DirectDeposit.toString(),
                        SystemCapabilityCode.SubmitPayroll.toString());
            }
        }

        // NOC Verification
        // Check if there are any OPEN Employee NOC returns or regular returns (R* Code) for the company
        HashMap<Employee, DomainEntitySet<TransactionReturn>> eeBANOCMap = null;
        DomainEntitySet<TransactionReturn> nocReturns = TransactionReturn.findTxnRetsForReturnType(company,
                                                                                                   TransactionReturn.ReturnTypeCodes.NOTICE_OF_CHANGE,
                                                                                                   TransactionReturnStatusCode.Open);
        DomainEntitySet<TransactionReturn> txReturns = TransactionReturn.findTxnRetsForReturnType(company,
                                                                                                  TransactionReturn.ReturnTypeCodes.RETURN,
                                                                                                  TransactionReturnStatusCode.Open);
        nocReturns.addAll(txReturns);
        if (nocReturns.size() > 0) {
            // Build a list with the employee bank accounts that have NOC returns
            eeBANOCMap = new HashMap<Employee, DomainEntitySet<TransactionReturn>>();
            for (TransactionReturn nocReturn : nocReturns) {
                DomainEntitySet<TransactionReturn> nocReturnsForBA;
                FinancialTransaction eeNOCTransaction = TransactionReturn.findFirstFinancialTransaction(nocReturn);
                EmployeeBankAccount eeBA = eeNOCTransaction.getEmployeeBankAccount();
                if (eeBA != null) {
                    nocReturnsForBA = eeBANOCMap.get(eeBA.getEmployee());
                    if (nocReturnsForBA == null) {
                        nocReturnsForBA = new DomainEntitySet<TransactionReturn>();
                        eeBANOCMap.put(eeBA.getEmployee(), nocReturnsForBA);
                    }
                    nocReturnsForBA.add(nocReturn);
                }
            }
        }

        // Create a Transaction list to check for duplicate dd transactions in the request
        Set<String> transactionIdSet = new HashSet<String>();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            Employee employee = Employee.findEmployee(company, paycheckDTO.getEmployeeId());

            // Check if employee bank accounts exist and are active
            for (DDTransactionDTO ddTransaction : paycheckDTO.getDdTransactions()) {
                final EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(employee, ddTransaction.getEmployeeBankAccount().getEmployeeBankAccountId());
                if (employeeBankAccount == null) {
                    if (ddTransaction.getEmployeeBankAccount().getBankAccount() == null) {
                        validationResult.getMessages().EmployeeBankAccountNotFound(EntityName.EmployeeBankAccount,
                                                                                   ddTransaction.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                                                                   ddTransaction.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                                                                   paycheckDTO.getEmployeeId());
                    }
                } else {
                    if (employeeBankAccount.getStatusCd() != BankAccountStatus.Active && ddTransaction.getEmployeeBankAccount().getBankAccount() == null) {
                            validationResult.getMessages().EmployeeBankAccountNotActive(
                                    EntityName.EmployeeBankAccount,
                                    ddTransaction.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                    ddTransaction.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                    paycheckDTO.getEmployeeId());
                    } else if(ddTransaction.getEmployeeBankAccount().getBankAccount() == null) {
                        // Populate Bank Account Info from employeeBankAccount
                        ddTransaction.setEmployeeBankAccount(PayrollServices.dtoFactory.create(employeeBankAccount));
                    }
                }

                // Check if employee has a NOC associated with it and the account information has not changed for this
                // payroll submission
                if (eeBANOCMap != null && eeBANOCMap.containsKey(employee)) {
                    // Find the most recent NOC Event associated with the employee
                    DomainEntitySet<TransactionReturn> nocReturnsForBA = eeBANOCMap.get(employee);
                    for (Iterator<TransactionReturn> iterator = nocReturnsForBA.iterator(); iterator.hasNext(); ) {
                        TransactionReturn transactionReturn = iterator.next();
                        FinancialTransaction eeNOCTransaction = TransactionReturn.findFirstFinancialTransaction(transactionReturn);
                        EmployeeBankAccount eeBA = eeNOCTransaction.getEmployeeBankAccount();
                        DomainEntitySet<CompanyEventDetail> nocEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.NOC,
                                                                                                                   EventDetailTypeCode.EmployeeBankAccountId, eeBA.getId().toString());
                        DomainEntitySet<CompanyEventDetail> returnEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.DDReject,
                                                                                                                       EventDetailTypeCode.EmployeeBankAccountId, eeBA.getId().toString());
                        nocEventDetails.addAll(returnEventDetails);
                        if (nocEventDetails.size() > 0) {
                            CompanyEvent nocEvent = null;
                            for(CompanyEventDetail companyEventDetail : nocEventDetails.sort(CompanyEventDetail.CompanyEvent().EventTimeStamp().Descending())) {
                                if (companyEventDetail.getCompanyEvent().isActive()) {
                                    nocEvent = companyEventDetail.getCompanyEvent();
                                    break;
                                }
                            }

                            if(nocEvent != null) {
                                // Special scenario for C04 - if the only active NOC associated with the ee bank account is a C04, meaning a name change, the NOC will
                                // be automatically resolved and the company will be not put on hold
                                if (isC04(nocEvent)) {
                                    resolveNOCEvents(transactionReturn, nocEvent);

                                    final EmployeeBankAccount finalEmployeeBankAccount = eeBA;
                                    //  Create PayrollSubmitWithNOCPendingEvent
                                    //noinspection unchecked
                                    PayrollServices.executeTransactionThread(new TransactionThread() {
                                        public ProcessResult transaction() {
                                            Company localCompany = Application.findById(Company.class, company.getId());
                                            CompanyEvent.createPayrollSubmittedWithPendingNOC(localCompany, payrollRunDTO.getPayrollTXBatchId(), finalEmployeeBankAccount.getId(), finalEmployeeBankAccount.getSourceBankAccountId());
                                            return new ProcessResult();
                                        }
                                    });
                                    // Issue warning
                                    validationResult.getMessages().PayrollSubmitedWithPendingNOC(EntityName.PayrollRun, sourceCompanyId, "1", paycheckDTO.getEmployeeId());
                                    iterator.remove();
                                } else if (bankAccountHasNewInformation(ddTransaction.getEmployeeBankAccount(), nocEvent)) {
                                    // Don't resolve Non-NOC returns since we need to check every bank account against the return.
                                    if (! EventTypeCode.DDReject.equals(nocEvent.getEventTypeCd())) {
                                        if (BankAccountStatus.Active.equals(eeBA.getStatusCd())) {
                                            validationResult.merge(PayrollServices.employeeManager.deactivateEmployeeBankAccount(
                                                    employee.getCompany().getSourceSystemCd(),
                                                    employee.getCompany().getSourceCompanyId(),
                                                    employee.getSourceEmployeeId(), PayrollServices.dtoFactory.create(eeBA)));
                                        }

                                        DomainEntitySet<CompanyEventDetail> companyEventDetails = nocEvent.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.NewAchAccountType);
                                        if (companyEventDetails.isNotEmpty()) {
                                            ACHBankAccountType achBankAccountType = ACHBankAccountType.valueOf(companyEventDetails.getFirst().getValue());
                                            ddTransaction.getEmployeeBankAccount().getBankAccount().setAchAccountType(achBankAccountType);
                                        }

                                        resolveNOCEvents(transactionReturn, nocEvent);
                                        iterator.remove();
                                    }
                                } else if (isDDReject(nocEventDetails)) {
                                    validationResult.getMessages().UnresolvedEECreditReturnExists(EntityName.PayrollRun, sourceCompanyId, employee.getFullName());

                                    final EmployeeBankAccount finalEmployeeBankAccount = eeBA;

                                    //  Create PayrollSubmittedWithEmployeeWithPendingReturn
                                    //noinspection unchecked
                                    PayrollServices.executeTransactionThread(new TransactionThread() {
                                        public ProcessResult transaction() {
                                            Company localCompany = Application.findById(Company.class, company.getId());
                                            CompanyEvent.createPayrollSubmittedWithEmployeeWithPendingReturn(localCompany, payrollRunDTO.getPayrollTXBatchId(), finalEmployeeBankAccount.getId());
                                            return new ProcessResult();
                                        }
                                    });
                                    iterator.remove();
                                } else if (isNotC05AndOnlyAccountTypeChanged(ddTransaction.getEmployeeBankAccount(), nocEvent) || bankAccountHasOldInformation(ddTransaction.getEmployeeBankAccount(), nocEvent)) {
                                    validationResult.getMessages().UnresolvedNOCExists(EntityName.PayrollRun, sourceCompanyId, employee.getFullName());

                                    final EmployeeBankAccount finalEmployeeBankAccount = eeBA;

                                    //  Create PayrollSubmitWithNOCPendingEvent
                                    //noinspection unchecked
                                    PayrollServices.executeTransactionThread(new TransactionThread() {
                                        public ProcessResult transaction() {
                                            Company localCompany = Application.findById(Company.class, company.getId());
                                            CompanyEvent.createPayrollSubmittedWithPendingNOC(localCompany, payrollRunDTO.getPayrollTXBatchId(), finalEmployeeBankAccount.getId(), finalEmployeeBankAccount.getSourceBankAccountId());
                                            return new ProcessResult();
                                        }
                                    });
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }

                if(SpcfUtils.convertToSpcfDecimal(ddTransaction.getDDTransactionAmount()).isLessThan(SpcfMoney.ZERO)) {
                    validationResult.getMessages().AmountMustBeGreaterThan(EntityName.DDTransaction, paycheckDTO.getPaycheckId(), SpcfMoney.ZERO.toString(), ddTransaction.getDDTransactionAmount().toString());
                    return validationResult;
                }
            }

            // If there are still returns in the map for this employee mark them as resolved
            if (eeBANOCMap != null && eeBANOCMap.containsKey(employee)) {
                for (TransactionReturn transactionReturn : eeBANOCMap.get(employee).find(TransactionReturn.ReturnStatusCd().equalTo(TransactionReturnStatusCode.Open))) {
                    FinancialTransaction eeNOCTransaction = TransactionReturn.findFirstFinancialTransaction(transactionReturn);
                    EmployeeBankAccount eeBA = eeNOCTransaction.getEmployeeBankAccount();
                    DomainEntitySet<CompanyEventDetail> nocEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.NOC,
                                                                                                               EventDetailTypeCode.EmployeeBankAccountId, eeBA.getId().toString());
                    DomainEntitySet<CompanyEventDetail> returnEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.DDReject,
                                                                                                                  EventDetailTypeCode.EmployeeBankAccountId, eeBA.getId().toString());
                    nocEventDetails.addAll(returnEventDetails);
                    if (nocEventDetails.size() > 0) {
                        CompanyEvent nocEvent = nocEventDetails.get(0).getCompanyEvent();
                        if(nocEvent.getStatusCd() == CompanyEventStatus.Active) {
                            // must have completely different information, resolve the NOC
                            resolveNOCEvents(transactionReturn, nocEvent);
                        }
                    }
                }
                eeBANOCMap.remove(employee);
            }


            // Check if Payroll Entities' IDs are Unique
            String sourcePaycheckId = paycheckDTO.getPaycheckId();

            // Check if any DD Transaction ID Already Exists
            for (DDTransactionDTO ddTransaction : paycheckDTO.getDdTransactions()) {
                String ddTransactionId = ddTransaction.getDDTransactionId();

                // Check if the transaction id is duplicated in the request
                if (transactionIdSet.contains(ddTransactionId)) {
                    validationResult.getMessages().DuplicateDDTransactionId(EntityName.Paycheck,
                            sourcePaycheckId,
                            ddTransactionId,
                            sourceSystemCd.toString(),
                            sourceCompanyId);
                } else {
                    transactionIdSet.add(ddTransactionId);
                }

            }
        }        

        return validationResult;
    }

    private void resolveNOCEvents(TransactionReturn transactionReturn, CompanyEvent pNocEvent) {
        // Resolve NOC/Return Returns
        transactionReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
        Application.save(transactionReturn);
        // Inactivate NOC/Return events
        for (CompanyEventDetail eventDetail : pNocEvent.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeBankAccountId)) {
            eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
            Application.save(eventDetail);

            // Inactivate PayrollSubmittedWithPendingNOC events
            for (CompanyEventDetail payrollSubmittedDetails : CompanyEvent.findCompanyEventDetails(pNocEvent.getCompany(), EventTypeCode.PayrollSubmittedWithPendingNOC, EventDetailTypeCode.EmployeeBankAccountId, eventDetail.getValue())) {
                payrollSubmittedDetails.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                Application.save(eventDetail);
            }
            // Inactivate PayrollSubmittedWithEmployeeWithPendingReturn events
            for (CompanyEventDetail payrollSubmittedDetails : CompanyEvent.findCompanyEventDetails(pNocEvent.getCompany(), EventTypeCode.PayrollSubmittedWithEmployeeWithPendingReturn, EventDetailTypeCode.EmployeeBankAccountId, eventDetail.getValue())) {
                payrollSubmittedDetails.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                Application.save(eventDetail);
            }
       }
    }

    private boolean anyDDTransactionsInPayroll(PayrollRunDTO pPayrollRunDTO) {
        boolean bAnyDDTransactionsInPayroll = false;
        Collection<PaycheckDTO> paychecks = pPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            Collection<DDTransactionDTO> ddTxnsForPaycheck = currPaycheck.getDdTransactions();
            if (ddTxnsForPaycheck != null && ddTxnsForPaycheck.size() > 0) {
                bAnyDDTransactionsInPayroll = true;
            }
        }
        return bAnyDDTransactionsInPayroll;
    }        

    private boolean bankAccountHasNewInformation(EmployeeBankAccountDTO pEmployeeBankAccountDTO, CompanyEvent pNOCEvent) {
        boolean accountUpdated = true;

        // For NOCs, every provided field must be updated to the new value for the account to have been "updated".
        for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case NewAccountNumber:
                    accountUpdated = accountUpdated && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountNumber());
                    break;
                case NewRoutingNumber:
                    accountUpdated = accountUpdated && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getRoutingNumber());
                    break;
                case NewAccountType:
                    accountUpdated = accountUpdated && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());
                    break;
            }
        }

        // For DD Reject (R* Code) events, if any value has changed, we'll consider the account "updated".
        if (pNOCEvent.getEventTypeCd().equals(EventTypeCode.DDReject)) {
            accountUpdated = false;
            for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
                switch (eventDetail.getEventDetailTypeCd()) {
                    case OldAccountNumber:
                        accountUpdated = accountUpdated || !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountNumber());
                        break;
                    case OldRoutingNumber:
                        accountUpdated = accountUpdated || !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getRoutingNumber());
                        break;
                    case OldAccountType:
                        accountUpdated = accountUpdated || !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());
                        break;
                }
            }
        }

        return accountUpdated;
    }

    private boolean bankAccountHasOldInformation(EmployeeBankAccountDTO pEmployeeBankAccountDTO, CompanyEvent pNOCEvent) {
        boolean accountMatches = true;
        for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case OldAccountNumber:
                    accountMatches = accountMatches && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountNumber());
                    break;
                case OldRoutingNumber:
                    accountMatches = accountMatches && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getRoutingNumber());
                    break;
                case OldAccountType:
                    accountMatches = accountMatches && eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());
                    break;
            }
        }

        return accountMatches;
    }

    private boolean isNotC05AndOnlyAccountTypeChanged(EmployeeBankAccountDTO pEmployeeBankAccountDTO, CompanyEvent pNOCEvent) {
        if(isC05(pNOCEvent)) {
            return false;
        }

        boolean accountNumberChanged = false;
        boolean routingNumberChanged = false;
        boolean accountTypeChanged = false;
        for (CompanyEventDetail eventDetail : pNOCEvent.getCompanyEventDetailCollection()) {
            switch (eventDetail.getEventDetailTypeCd()) {
                case OldAccountNumber:
                    accountNumberChanged = !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountNumber());
                    break;
                case OldRoutingNumber:
                    routingNumberChanged = !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getRoutingNumber());
                    break;
                case OldAccountType:
                    accountTypeChanged = !eventDetail.getValue().equals(pEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());
                    break;
            }
        }

        return !accountNumberChanged && !routingNumberChanged && accountTypeChanged;
    }

    private boolean isC04(CompanyEvent pNOCEvent) {
        String nocReturnValue = pNOCEvent.getCompanyEventDetailValue(EventDetailTypeCode.ACHEventCd);
        return nocReturnValue.equals("C04");
    }

    private boolean isC05(CompanyEvent pNOCEvent) {
        String nocReturnValue = pNOCEvent.getCompanyEventDetailValue(EventDetailTypeCode.ACHEventCd);
        return nocReturnValue.equals("C05");
    }

    private boolean isDDReject(DomainEntitySet<CompanyEventDetail> eventDetails) {
        if (eventDetails.size() == 1) {
            CompanyEvent rejectEvent = eventDetails.get(0).getCompanyEvent();
            return(EventTypeCode.DDReject.equals(rejectEvent.getEventTypeCd()));
        }
        return false;
    }

}
