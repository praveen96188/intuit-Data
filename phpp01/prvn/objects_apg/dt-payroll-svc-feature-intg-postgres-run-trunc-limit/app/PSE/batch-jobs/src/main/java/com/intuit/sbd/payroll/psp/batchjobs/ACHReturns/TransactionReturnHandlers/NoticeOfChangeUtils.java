package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 26, 2008
 * Time: 2:33:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoticeOfChangeUtils {

    private static final HashMap offsetMap = new HashMap();
    private static SpcfLogger logger = Application.getLogger(NoticeOfChangeUtils.class);
    private static final int ACCOUNT_NUMBER = 0;
    private static final int ROUTING_NUMBER = 1;
    private static final int TRANSACTION_TYPE_CODE = 2;
    private static final int ACCOUNT_NAME = 3;

    static {
        offsetMap.put("C01", new int[][]{{0, 17}, null, null, null});
        offsetMap.put("C02", new int[][]{null, {0, 9}, null, null});
        offsetMap.put("C03", new int[][]{{12, 29}, {0, 9}, null, null});
        offsetMap.put("C04", new int[][]{null, null, null, {0, 22}});
        offsetMap.put("C05", new int[][]{null, null, {0, 2}, null});
        offsetMap.put("C06", new int[][]{{0, 17}, null, {20, 22}, null});
        offsetMap.put("C07", new int[][]{{9, 26}, {0, 9}, {26, 28}, null});
    }

    /**
     * Function to get the corrected value from the corrected datafield for a returned bank code
     *
     * @param pBankChangeCode     String
     * @param pCorrectedDataField String
     * @param pField              int
     * @return String
     */
    private String getCorrectedValue(String pBankChangeCode, String pCorrectedDataField, int pField) {
        int[][] offsets = (int[][]) offsetMap.get(pBankChangeCode);

        try {
            if (pCorrectedDataField != null && offsets[pField] != null) {
                int offsetStart = offsets[pField][0];
                int offsetEnd = offsets[pField][1];

                if (pCorrectedDataField.length() >= offsetEnd) {
                    return pCorrectedDataField.substring(offsetStart, offsetEnd);
                }
            }
        } catch (Exception ex) {
            String message = ex + "\n data buffer: '" + pCorrectedDataField +
                    "', notice of change type: '" + pBankChangeCode + "'.";
            logger.error(message, ex);
        }

        return null;
    }



    /**
     * Function to get the corrected Bank Account Number
     *
     * @param pTxnReturn TransactionReturn
     * @return String
     */
    public static String getCorrectedBankAccountNumber(TransactionReturn pTxnReturn) {
        NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();
        String accountNumberStr = nocUtils.getCorrectedValue(pTxnReturn.getBankReturnCd(),
                pTxnReturn.getBankReturnDescription(), ACCOUNT_NUMBER);
        return accountNumberStr == null ? null : accountNumberStr.trim();
    }

    /**
     * Function to get the corrected Bank Routing Number
     *
     * @param pTxnReturn TransactionReturn
     * @return String
     */
    public static String getCorrectedBankRoutingNumber(TransactionReturn pTxnReturn) {
        NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();

        String routingNumberStr = nocUtils.getCorrectedValue(pTxnReturn.getBankReturnCd(),
                pTxnReturn.getBankReturnDescription(), ROUTING_NUMBER);

        return routingNumberStr == null ? null : routingNumberStr.trim();
    }

    /**
     * Function to get the corrected Transaction Type Code
     *
     * @param pTxnReturn TransactionReturn
     * @return String
     */
    public static String getCorrectedTransactionTypeCode(TransactionReturn pTxnReturn) {
        NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();
        return nocUtils.getCorrectedValue(pTxnReturn.getBankReturnCd(), pTxnReturn.getBankReturnDescription(),
                TRANSACTION_TYPE_CODE);
    }

    /**
     * Function to validate the Transaction Type Code
     *
     * @param pTxnTypeCd String
     * @return true if the transactiontype code is not null and it's 21,22,26,27,31,32,36,37,41,42,46,47,51,52, or 56
     */
    public static boolean isValidTransactionTypeCode(String pTxnTypeCd) {
        return pTxnTypeCd != null &&
                AchTransactionCode.findAchTransactionCode(pTxnTypeCd) != null;
    }

    /**
     * Function to get the corrected Bank Account Type Code
     *
     * @param pTxnReturn TransactionReturn
     * @return 'Checking' or 'Savings'
     */
    public static ACHBankAccountType getCorrectedBankAccountTypeCode(TransactionReturn pTxnReturn) {
        String txnTypeCode = getCorrectedTransactionTypeCode(pTxnReturn);

        AchTransactionCode txCode = AchTransactionCode.findAchTransactionCode(txnTypeCode);
        if (txCode != null) {
            return txCode.getAchAccountTypeCd();
        }
        return null;
    }

    public static BankAccountType getBankAccountType(ACHBankAccountType pACHBankAccountType) {
        BankAccountType bankAccountType = null;
        try {
            bankAccountType = BankAccountType.valueOf(pACHBankAccountType.toString());
        } catch (Exception e) {
            //Do nothing and return null value
        }
        return bankAccountType;
    }

    /**
     * Function to check which field is effected for notice of change
     *
     * @param pTxnReturn TransactionReturn
     * @param pFieldId   int (0 - Account Number, 1 - Routing Number, 2- Transaction Type Code)
     * @return boolean
     */
    private boolean isAffectedByNoticeOfChange(TransactionReturn pTxnReturn, int pFieldId) {
        return ((int[][]) offsetMap.get(pTxnReturn.getBankReturnCd()))[pFieldId] != null;
    }

    private static boolean isCheckingOrSavings(ACHBankAccountType pACHBankAccountType) {
        if (pACHBankAccountType == null) {
            return false;
        }

        return ACHBankAccountType.Checking.toString().equals(pACHBankAccountType.toString()) ||
               ACHBankAccountType.Savings.toString().equals(pACHBankAccountType.toString());
    }

    private static boolean isLoanOrLedger(ACHBankAccountType pACHBankAccountType) {
        if (pACHBankAccountType == null) {
            return false;
        }

        return ACHBankAccountType.Loan.toString().equals(pACHBankAccountType.toString()) ||
               ACHBankAccountType.Ledger.toString().equals(pACHBankAccountType.toString());
    }

    /**
     * Function to check whether the bank account associated with the financial transaction is company bank account
     * or not.
     *
     * @param pFinancialTransaction FinancialTransaction
     * @return boolean
     */
    private static boolean isCompanyExternalBankAccount(FinancialTransaction pFinancialTransaction) {
        BankAccountOwnerType bankAccountOwnnerType = pFinancialTransaction.getDebitBankAccountType();
        if (bankAccountOwnnerType.equals(BankAccountOwnerType.Intuit)) {
            bankAccountOwnnerType = pFinancialTransaction.getCreditBankAccountType();
        }
        return bankAccountOwnnerType.equals(BankAccountOwnerType.Company);
    }

    /**
     * Function to get the source account id from the Company/Employee Bank Account
     *
     * @param pFinancialTransaction FinancialTransaction
     * @return String
     */
    private static String getSourceAccountId(FinancialTransaction pFinancialTransaction) {
        if (isCompanyExternalBankAccount(pFinancialTransaction)) {
            CompanyBankAccount companyBankAccount = pFinancialTransaction.getCompanyBankAccount();
            return companyBankAccount.getSourceBankAccountId();
        } else {
            if(pFinancialTransaction.getEmployeeBankAccount() != null) {                
                return pFinancialTransaction.getEmployeeBankAccount().getSourceBankAccountId();
            }
            else if(pFinancialTransaction.getPayeeBankAccount() != null){
                return pFinancialTransaction.getPayeeBankAccount().getSourceBankAccountId();
            }
            else {
                return null;
            }
        }
    }

    /**
     * Function to create a Notice of Change System Event. This method is called by all the Notice Of Change Return
     * Events.
     *
     * @param pTxnReturn   TransactionReturn
     * @param pBankAccount BankAccount
     */
    public static CompanyEvent createNoticeOfChangeSystemEventRule(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        CompanyEvent companyEvent = null;

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransactionExcludingTransactionTypeCodes(pTxnReturn,
                                                                                                                                   TransactionTypeCode.EmployerTaxCreditApplied,
                                                                                                                                   TransactionTypeCode.EmployerTaxOverpaymentApplied);

        if (!finTxnList.isEmpty()) {
            FinancialTransaction finTxn = finTxnList.get(0);

            Company company = finTxn.getCompany();

            NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();

            boolean includeData = nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NUMBER)
                    || nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ROUTING_NUMBER)
                    || nocUtils.isAffectedByNoticeOfChange(pTxnReturn, TRANSACTION_TYPE_CODE);

            CompanyBankAccount companyBankAccount = finTxn.getCompanyBankAccount();
            EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();
            PayeeBankAccount payeeBankAccount = finTxn.getPayeeBankAccount();

            if (includeData) {
                String oldAccountNumber = pBankAccount.getAccountNumber();
                String newAccountNumber = oldAccountNumber;
                String oldRoutingNumber = pBankAccount.getRoutingNumber();
                String newRoutingNumber = oldRoutingNumber;
                ACHBankAccountType oldAchAccountType = pBankAccount.getACHAccountTypeCd();
                ACHBankAccountType newAchAccountType = oldAchAccountType;
                BankAccountType oldAccountType = pBankAccount.getAccountTypeCd();
                BankAccountType newAccountType = oldAccountType;

                //if-condtion to check whether the Bank Account Number is effected by notice of changer or not.
                //If effected get the corrected bank account number.
                if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NUMBER)) {
                    newAccountNumber = getCorrectedBankAccountNumber(pTxnReturn);
                    if (newAccountNumber == null || newAccountNumber.length() == 0) {
                        newAccountNumber = "INVALID";
                    } else if (!BankAccountDTO.isValidBankAccountNumber(newAccountNumber)) {
                        newAccountNumber = "INVALID";
                    }
                }

                //if-condtion to check whether the Bank Account Routing Number is effected by notice of changer or not.
                //If effected get the corrected bank account routing number.
                if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ROUTING_NUMBER)) {
                    newRoutingNumber = getCorrectedBankRoutingNumber(pTxnReturn);
                    if (newRoutingNumber == null || newRoutingNumber.length() == 0) {
                        newRoutingNumber = "INVALID";
                    } else if (!BankAccountDTO.isValidRoutingNumber(newRoutingNumber)) {
                        newRoutingNumber = "INVALID";
                    }
                }

                //if-condtion to check whether the Transaction Type code is effected by notice of changer or not.
                //If effected get the corrected bank account type code.
                if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, TRANSACTION_TYPE_CODE)) {
                    newAchAccountType = getCorrectedBankAccountTypeCode(pTxnReturn);
                    if (isCheckingOrSavings(newAchAccountType)) {
                        newAccountType = NoticeOfChangeUtils.getBankAccountType(newAchAccountType);
                    }
                }


                if (companyBankAccount != null && ACHBankAccountType.Loan.equals(newAchAccountType)) {
                    companyEvent = CompanyEvent.createERLoanNoticeOfChangeEvent(company, pTxnReturn.getBankReturnCd(), companyBankAccount,
                                                                 isCompanyExternalBankAccount(finTxn), oldAccountNumber,
                                                                 newAccountNumber, oldRoutingNumber, newRoutingNumber, oldAccountType, newAccountType,
                                                                 oldAchAccountType, newAchAccountType, finTxn);
                    company.addOnHoldReason(ServiceSubStatusCode.RiskAssessment);
                } else {
                    companyEvent = CompanyEvent.createNoticeOfChangeEvent(company, pTxnReturn.getBankReturnCd(), companyBankAccount,
                                                           employeeBankAccount, payeeBankAccount, isCompanyExternalBankAccount(finTxn), oldAccountNumber,
                                                           newAccountNumber, oldRoutingNumber, newRoutingNumber, oldAccountType, newAccountType,
                                                           oldAchAccountType, newAchAccountType, finTxn);
                }
            } else if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NAME)) { // C04: we just update the event details
                String oldAccountNumber = pBankAccount.getAccountNumber();
                String newAccountNumber = oldAccountNumber;
                String oldRoutingNumber = pBankAccount.getRoutingNumber();
                String newRoutingNumber = oldRoutingNumber;
                ACHBankAccountType oldAchAccountType = pBankAccount.getACHAccountTypeCd();
                ACHBankAccountType newAchAccountType = oldAchAccountType;
                BankAccountType oldAccountType = pBankAccount.getAccountTypeCd();
                BankAccountType newAccountType = oldAccountType;

                companyEvent = CompanyEvent.createNoticeOfChangeEvent(company, pTxnReturn.getBankReturnCd(), companyBankAccount,
                                                       employeeBankAccount, payeeBankAccount, isCompanyExternalBankAccount(finTxn), oldAccountNumber,
                                                       newAccountNumber, oldRoutingNumber, newRoutingNumber, oldAccountType, newAccountType, oldAchAccountType, newAchAccountType, finTxn);
            } else {
                companyEvent = CompanyEvent.createNoticeOfChangeEvent(company, pTxnReturn.getBankReturnCd(), companyBankAccount,
                                                       employeeBankAccount, payeeBankAccount, isCompanyExternalBankAccount(finTxn), finTxn, true);
            }
        }
        return companyEvent;
    }

    /**
     * Function to create a Invalid Corrected Field Company Note.
     * This method is called by all the Notice Of Change Return Events.
     *
     * @param pTxnReturn   TransactionReturn
     * @param pBankAccount BankAccount
     */
    public static ProcessResult createInvalidCorrectedFieldCompanyNoteRule(TransactionReturn pTxnReturn, BankAccount pBankAccount) {
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(pTxnReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        Company company = finTxn.getCompany();

        boolean isCompanyBankAccount = isCompanyExternalBankAccount(finTxn);
        StringBuffer buffer = new StringBuffer();
        String entity;
        NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();

        String sourceAccountId = getSourceAccountId(finTxn);

        if (isCompanyBankAccount) {
            entity = "company";
        } else {
            entity = "employee";
        }

        buffer.append("An erroneous NOC with change code '").append(pTxnReturn.getBankReturnCd()).append("' was received for ").
                append(entity).append(" bank account id '").append(sourceAccountId);

        if (!isCompanyBankAccount) {
            if(finTxn.getEmployeeBankAccount() != null) {
                EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();
                Employee employee = employeeBankAccount.getEmployee();
                buffer.append("', ").append(entity).append(" id '").append(employee.getSourceEmployeeId());
            }
            else if(finTxn.getPayeeBankAccount() != null) {
                PayeeBankAccount payeeBankAccount = finTxn.getPayeeBankAccount();
                Payee payee = payeeBankAccount.getPayee();
                buffer.append("', ").append(entity).append(" id '").append(payee.getSourcePayeeId());
            }
        }

        buffer.append("'.  ");

        //find out all fields for which invalid data was returned and append them to the buffer

        if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NUMBER)) {
            String accountNumber = getCorrectedBankAccountNumber(pTxnReturn);
            if (!BankAccountDTO.isValidBankAccountNumber(accountNumber)) {
                if (accountNumber == null || accountNumber.length() == 0) {
                    accountNumber = "blank";
                } else {
                    accountNumber = "'" + accountNumber + "'";
                }
                buffer.append("The provided bank account number is invalid. ");
                /*
                 * bank account supressed as per security requirements
                 * buffer.append("The current bank account number is '").append(pBankAccount.getAccountNumber()).
                 *       append("', the corrected value provided for this field in the NOC was ").
                 *       append(accountNumber).append(", which is invalid. ");
                 */
            }
        }

        if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ROUTING_NUMBER)) {
            String routingNumber = getCorrectedBankRoutingNumber(pTxnReturn);
            if (!BankAccountDTO.isValidRoutingNumber(routingNumber)) {
                if (routingNumber == null || routingNumber.length() == 0) {
                    routingNumber = "blank";
                } else {
                    routingNumber = "'" + routingNumber + "'";
                }
                buffer.append("The current bank account routing number is '").append(pBankAccount.getRoutingNumber()).
                        append("', the corrected value provided for this field in the NOC was ").
                        append(routingNumber).append(", which is invalid. ");
            }
        }

        if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, TRANSACTION_TYPE_CODE)) {
            ACHBankAccountType accountTypeCd = getCorrectedBankAccountTypeCode(pTxnReturn);
            if (!BankAccount.isValidBankAccountTypeCode(accountTypeCd)) {
                buffer.append("The current bank account type code is '").append(pBankAccount.getAccountTypeCd()).
                        append("', the corrected value provided for this field in the NOC was ").
                        append(accountTypeCd).append(", which is invalid. ");

            }
        }

        buffer.append("The NOC was left unresolved and no action was taken against the affected ").append(entity).
                append(" bank account by the system.");

        logger.info("Company Note: " + buffer);
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

        //Statement to call the AddCompanyNoteCore process to add the CompanyNote
        ProcessResult result = PayrollServices.companyManager.addCompanyNote(company.getSourceSystemCd(),
                company.getSourceCompanyId(), null,
                principal.getId(), buffer.toString(), false);

        return result;
    }

    /**
     * Function to check whether the Employee or Payee Bank Account information is changed or not. If the account information
     * has changed returns true, other wise returns false.
     *
     * @param pTxnReturn TransactionReturn
     * @return boolean
     */
    public static boolean hasBankAccountInfoChanged(TransactionReturn pTxnReturn) {
        boolean hasInfoChanged = false;
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransactionExcludingTransactionTypeCodes(pTxnReturn,
                TransactionTypeCode.EmployerTaxCreditApplied, TransactionTypeCode.EmployerTaxOverpaymentApplied);

        if (finTxnList.isEmpty()) {
            return false;
        }

        FinancialTransaction finTxn = finTxnList.get(0);

        NoticeOfChangeUtils nocUtils = new NoticeOfChangeUtils();

        boolean includeData = nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NUMBER)
                || nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ROUTING_NUMBER)
                || nocUtils.isAffectedByNoticeOfChange(pTxnReturn, TRANSACTION_TYPE_CODE);

        BankAccount bankAccount;
        if (finTxn.getEmployeeBankAccount() != null) {
            EmployeeBankAccount employeeBankAccount = finTxn.getEmployeeBankAccount();
            bankAccount = employeeBankAccount.getBankAccount();
        } else {
            PayeeBankAccount payeeBankAccount = finTxn.getPayeeBankAccount();
            bankAccount = payeeBankAccount.getBankAccount();
        }
        if (includeData) {
            String oldAccountNumber = bankAccount.getAccountNumber();
            String newAccountNumber = oldAccountNumber;
            String oldRoutingNumber = bankAccount.getRoutingNumber();
            String newRoutingNumber = oldRoutingNumber;
            BankAccountType oldAccountType = bankAccount.getAccountTypeCd();
            BankAccountType newAccountType = oldAccountType;
            ACHBankAccountType oldACHAccountType = bankAccount.getACHAccountTypeCd();
            ACHBankAccountType newACHAccountType = oldACHAccountType;

            //if-condtion to check whether the Bank Account Number is effected by notice of changer or not.
            //If effected get the corrected bank account number.
            if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ACCOUNT_NUMBER)) {
                newAccountNumber = getCorrectedBankAccountNumber(pTxnReturn);
                if (newAccountNumber == null || newAccountNumber.length() == 0) {
                    newAccountNumber = "INVALID";
                } else if (!BankAccountDTO.isValidBankAccountNumber(newAccountNumber)) {
                    newAccountNumber = "INVALID";
                }
            }

            //if-condtion to check whether the Bank Account Routing Number is effected by notice of changer or not.
            //If effected get the corrected bank account routing number.
            if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, ROUTING_NUMBER)) {
                newRoutingNumber = getCorrectedBankRoutingNumber(pTxnReturn);
                if (newRoutingNumber == null || newRoutingNumber.length() == 0) {
                    newRoutingNumber = "INVALID";
                } else if (!BankAccountDTO.isValidRoutingNumber(newRoutingNumber)) {
                    newRoutingNumber = "INVALID";
                }
            }

            //if-condtion to check whether the Transaction Type code is effected by notice of changer or not.
            //If effected get the corrected bank account type code.
            if (nocUtils.isAffectedByNoticeOfChange(pTxnReturn, TRANSACTION_TYPE_CODE)) {
                newACHAccountType = getCorrectedBankAccountTypeCode(pTxnReturn);
                if (!NoticeOfChangeUtils.isLoanOrLedger(newACHAccountType)) {
                    newAccountType = NoticeOfChangeUtils.getBankAccountType(newACHAccountType);
                }
            }

            hasInfoChanged = !(newAccountNumber.equals(oldAccountNumber) &&
                    newRoutingNumber.equals(oldRoutingNumber) &&
                    (newAccountType != null && newAccountType.equals(oldAccountType)) &&
                    (newACHAccountType != null && newACHAccountType.equals(oldACHAccountType)));
        }

        return hasInfoChanged;
    }
}
