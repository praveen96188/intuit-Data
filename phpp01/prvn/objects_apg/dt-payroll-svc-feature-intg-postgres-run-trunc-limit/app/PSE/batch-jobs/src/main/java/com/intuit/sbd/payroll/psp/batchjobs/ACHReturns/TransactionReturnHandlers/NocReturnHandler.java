package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jun 6, 2008
 * Time: 12:21:59 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class NocReturnHandler extends TransactionReturnHandler {
    // intentionally use ReturnFileParser as logger class
    private static final SpcfLogger logger = Application.getLogger(NocReturnHandler.class);

    /**
     * Decides whether ALL corrected fields relevant to this instance have valid corrected values.
     *
     * @param pTxnReturn
     * @return true when ALL corrected fields have valid values, or false otherwise
     */
    protected abstract boolean fieldsAreValid(TransactionReturn pTxnReturn);

    /**
     * Updates BankAccount fields with corrected values from the TransactionReturn.
     *
     * @param pTxnReturn   the TransactionReturn with the corrected field value(s)
     * @param pBankAccount the BankAccount to be updated
     */
    protected abstract void updateFields(TransactionReturn pTxnReturn, BankAccount pBankAccount);

    protected void updateAchAccountType(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        ACHBankAccountType achBankAccountType = NoticeOfChangeUtils.getCorrectedBankAccountTypeCode(pTxnReturn);

        if(pBankAccount.getEmployeeBankAccount() == null || !pBankAccount.getEmployeeBankAccount().getEmployee().canBeRecoveredByQB()) {
            pBankAccount.updateACHBankAccountTypeCd(achBankAccountType);
        } else {
            EmployeeBankAccountDTO employeeBankAccountDTO = PayrollServices.dtoFactory.create(pBankAccount.getEmployeeBankAccount());
            employeeBankAccountDTO.getBankAccount().setAchAccountType(achBankAccountType);
            updateEmployeeBankAccount(employeeBankAccountDTO, pBankAccount);
        }
    }

    protected void updateEmployeeBankAccount(EmployeeBankAccountDTO pEmployeeBankAccountDTO, BankAccount pBankAccount) {
        if(pBankAccount.getEmployeeBankAccount() != null) {
            BankAccountDTO bankAccountDTO = pEmployeeBankAccountDTO.getBankAccount();
            pBankAccount.setRoutingNumber(bankAccountDTO.getRoutingNumber());
            pBankAccount.setAccountNumber(bankAccountDTO.getAccountNumber());
            pBankAccount.setAccountTypeCd(bankAccountDTO.getAccountType());
            pBankAccount.setACHAccountTypeCd(bankAccountDTO.getAchAccountType());
            Application.save(pBankAccount);

            pBankAccount.getEmployeeBankAccount().setSourceBankAccountId(pEmployeeBankAccountDTO.getEmployeeBankAccountId());
            Application.save(pBankAccount.getEmployeeBankAccount());
        }
    }

    /**
     * The NOC-specific implementation of TransactionReturnHandler.execute().  When appropriate, this method resolves the
     * TransactionReturn, updates fields on the bank account with corrected values from the TransactionReturn, and
     * creates necessary system events and company notes.
     * <p/>
     * It delegates to the derived classes through the fieldsAreValid() and updateFields() methods.
     *
     * @param pTxnReturn
     * @return
     */
    public TransactionReturn execute(TransactionReturn pTxnReturn) {
        pTxnReturn = Application.findById(TransactionReturn.class, pTxnReturn.getId());
        pTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);

        // call derived class to judge whether updated field value(s) are valid
        boolean bFieldsAreValid = fieldsAreValid(pTxnReturn);
        boolean resolveNOC = isResolvable(pTxnReturn);
        boolean loanOrLedgerUpdate = isLoanOrLedgerTransactionCode(pTxnReturn);
        boolean c05NOC = isC05(pTxnReturn);

        // if all updates are valid, we resolve this transaction return
        // if this is an Employee NOC - based on the SourcePayrollParam ResolveEmployeeNOC the status of the
        // transactionReturn has to be updated to Open
        if (bFieldsAreValid) {
            if (resolveNOC) {
                resolveACHReturn(pTxnReturn);
            } else {
                //If the Employee bank account information is not changed then resolve the NOC Return and
                //create a 'NOCWithOutChanges' event
                if (!NoticeOfChangeUtils.hasBankAccountInfoChanged(pTxnReturn)) {
                    logger.info("resolving NOCWithOutChanges");
                    resolveACHReturn(pTxnReturn);

                    CompanyEvent.createCompanyEvent(pTxnReturn.getCompany(), EventTypeCode.NOCWithOutChanges);
                    return pTxnReturn;
                } else if (loanOrLedgerUpdate && c05NOC) {
                    resolveACHReturn(pTxnReturn);
                } else {
                    pTxnReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);
                }
            }
        }

        // see whether the non-Intuit bank account associated with this return should be updated
        BankAccount bankAccount = getBankAccountToUpdate(pTxnReturn);

        // if it should be updated...
        if (bankAccount != null) {
            // create a Notice Of Change system event (before updating any bank account fields)
            CompanyEvent companyEvent = NoticeOfChangeUtils.createNoticeOfChangeSystemEventRule(pTxnReturn, bankAccount);

            // if all updates are valid...
            if (bFieldsAreValid) {
                // call the derived class to make those updates
                if (resolveNOC) {
                    updateFields(pTxnReturn, bankAccount);
                } else if (loanOrLedgerUpdate) {
                    updateAchAccountType(pTxnReturn, bankAccount);
                }

                //Inactivate the event because there is nothing the customer needs to update.
                boolean isNocEvent = companyEvent != null && EventTypeCode.NOC.equals(companyEvent.getEventTypeCd());
                if (loanOrLedgerUpdate && c05NOC && isNocEvent) {
                    companyEvent.setStatusCd(CompanyEventStatus.Inactive);
                }
            } else {
                // not valid... so create an Invalid Corrected Field company note
                ProcessResult prUnhandled = NoticeOfChangeUtils.createInvalidCorrectedFieldCompanyNoteRule(pTxnReturn, bankAccount);
                if (!prUnhandled.isSuccess()) {
                    SpcfLogger logger = Application.getLogger(NocReturnHandler.class);
                    logger.error("Unhandled ProcessResult failure from NoticeOfChangeUtils.createInvalidCorrectedFieldCompanyNoteRule(): " + prUnhandled.toString());
                }
            }
        }
        // else the non-Intuit bank account should not be updated

        return pTxnReturn;
    }

    protected boolean isResolvable(TransactionReturn pTxnReturn) {
        //
        // If return is an Employer related NOC (Payroll) then allow auto-resolve
        // If return is an Employee related NOC for an Assisted company (Payroll) then allow auto-resolve
        // If return is an Payee NOC for Bill Payment or Employee related NOC for a DD company and the ResolveEmployeeNOC
        //      source payroll param is 'true' (default to true if param is missing) then allow auto-resolve
        //
        boolean resolveNOC = true; // assume we want to auto-resolve

        if (isBillPaymentPayeeReturn(pTxnReturn) || isNonAssistedEmployeeReturn(pTxnReturn)) {
            SourcePayrollParameter resolveEmployeeNOC = SourcePayrollParameter.findSourcePayrollParameter(pTxnReturn.getCompany().getSourceSystemCd(),
                                                                                                          SourcePayrollParameterCode.ResolveEmployeeNOC);

            resolveNOC = (resolveEmployeeNOC == null) || Boolean.parseBoolean(resolveEmployeeNOC.getParameterValue());
        }

        return resolveNOC;
    }

    protected boolean isLoanOrLedgerTransactionCode(TransactionReturn pTxnReturn) {
        boolean response = false;
        String correctedTransactionTypeCode = NoticeOfChangeUtils.getCorrectedTransactionTypeCode(pTxnReturn);
        AchTransactionCode achTransactionCode = AchTransactionCode.findAchTransactionCode(correctedTransactionTypeCode);
        if (achTransactionCode != null && achTransactionCode.getAchAccountTypeCd().in(ACHBankAccountType.Ledger, ACHBankAccountType.Loan)) {
            response = true;
        }
        return response;
    }

    protected boolean isC05(TransactionReturn pTxnReturn) {
        return "C05".equals(pTxnReturn.getBankReturnCd());
    }

    /**
     * This method encapsulates rules about whether the non-Intuit bank account associated with the returnd ACH
     * transaction should be updated with corrected values from the TransactionReturn.
     *
     * @param pTxnReturn
     * @return a BankAccount that should be updated, or null if no updates should happen
     */
    protected BankAccount getBankAccountToUpdate(TransactionReturn pTxnReturn) {
        // we look at the non-Intuit bank account on the first FT from the list of FTs returned with this ACH transaction
        FinancialTransaction firstFT = getFirstFinancialTransaction(pTxnReturn);
        if (firstFT != null && !(BankAccountOwnerType.TaxAgency.equals(firstFT.getCreditBankAccountType()) &&
                BankAccountOwnerType.TaxAgency.equals(firstFT.getDebitBankAccountType()))) {

            BankAccount bankAccount = firstFT.getNonIntuitBankAccount();


            // we want to know whether it is a Company (vs. Employee) account, and whether it is in the Inactive state
            boolean isInactiveCBA = false;

            // see if that account is a Company bank account, and if its status is Inactive
            if (!isEmployeeBankAccount(pTxnReturn)) {
                CompanyBankAccount companyBankAccount = firstFT.getCompanyBankAccountIncludingExpired();
                if (companyBankAccount != null) {
                    if (companyBankAccount.getStatusCd() == BankAccountStatus.Inactive) {
                        bankAccount = companyBankAccount.getBankAccount();
                        isInactiveCBA = true;
                    }
                } else {
                    //Get the EntryDetail Records from the MMT
                    DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                            pTxnReturn.getMoneyMovementTransaction().getEntryDetailRecordCollection();

                    String traceNumber = null;
                    for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
                        if (entryDetailRecord.getTraceNumber() != null) {
                            traceNumber = entryDetailRecord.getTraceNumber();
                            break;
                        }
                    }
                    throw new RuntimeException("Could not find the Company Bank Account for Financial Transaction: "
                            + firstFT.getId() + " and the Trace Number: " + traceNumber);
                }
            }

            if (isInactiveCBA) {
                return null;
            } else {
                // not a Company account, or not Inactive
                return bankAccount;
            }
        }
        return null;
    }

    protected Boolean isNonAssistedEmployeeReturn(TransactionReturn pTxnReturn) {
        return !pTxnReturn.getCompany().isCompanyOnService(ServiceCode.Tax) && isEmployeeBankAccount(pTxnReturn);
    }

    protected Boolean isEmployeeBankAccount(TransactionReturn pTxnReturn) {
        FinancialTransaction firstFT = getFirstFinancialTransaction(pTxnReturn);
        return (firstFT != null) && BankAccountOwnerType.Employee.equals(firstFT.getNonIntuitBankAccountType());
    }

    protected Boolean isBillPaymentPayeeReturn(TransactionReturn pTxnReturn) {
        FinancialTransaction firstFT = getFirstFinancialTransaction(pTxnReturn);
        // It looks like Bill Payment FTs actually have a bank account type of Employee even though Payee is a defined type.
        return (firstFT != null) && firstFT.getBillPaymentSplit() != null &&
                firstFT.getNonIntuitBankAccountType().in(BankAccountOwnerType.Payee, BankAccountOwnerType.Employee);
    }

    protected Boolean isPayeeBankAccount(TransactionReturn pTxnReturn) {
        FinancialTransaction firstFT = getFirstFinancialTransaction(pTxnReturn);
        return (firstFT != null) && BankAccountOwnerType.Payee.equals(firstFT.getNonIntuitBankAccountType());
    }

    protected FinancialTransaction getFirstFinancialTransaction(TransactionReturn pTxnReturn) {
        DomainEntitySet<FinancialTransaction> returnedFTs =
                TransactionReturn.findFinancialTransactionExcludingTransactionTypeCodes(pTxnReturn,
                                                                                        TransactionTypeCode.EmployerTaxCreditApplied,
                                                                                        TransactionTypeCode.EmployerTaxOverpaymentApplied);
        return returnedFTs.isEmpty() ? null : returnedFTs.get(0);
    }
}
