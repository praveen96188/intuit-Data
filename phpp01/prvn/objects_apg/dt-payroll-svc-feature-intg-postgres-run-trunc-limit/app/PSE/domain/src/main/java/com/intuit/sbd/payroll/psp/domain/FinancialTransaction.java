package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.util.AlertTransactionObserver;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.ScrollableResults;

import java.math.BigDecimal;
import java.util.*;

/**
 * Hand-written business logic
 */
public class FinancialTransaction extends BaseFinancialTransaction implements IUpdatable {
    public static final String ATO_CACHE_KEY = "Cache:AgencyTaxOverAppliedFTs";
    public static final String QBDT_PROCESS_OBSERVER = "QBDTProcessObserver";
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final static SpcfLogger LOGGER = Application.getLogger(FinancialTransaction.class);
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static ScrollableResults findFTsWithMMTsForIntuitBankAccount(IntuitBankAccount pIntuitBankAccount, SpcfCalendar pInitiationDate) {

        Expression<FinancialTransaction> query = new Query<FinancialTransaction>()
                .Where(FinancialTransaction.CreditBankAccount().equalTo(pIntuitBankAccount.getBankAccount())
                                           .And(FinancialTransaction.MoneyMovementTransaction().InitiationDate().equalTo(pInitiationDate))
                                           .And(FinancialTransaction.SettlementDate().greaterOrEqualThan(pInitiationDate))
                                           .And(FinancialTransaction.MoneyMovementTransaction().MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit))
                                           .And(FinancialTransaction.MoneyMovementTransaction().Status().equalTo(PaymentStatus.Executed)))
                .EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.Company()))
                .EagerLoad(FinancialTransaction.Company())
                .ReadOnly(true);
        return Application.findScrollable(FinancialTransaction.class, query);

    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(PayrollRun pPayrollRun,
                                                                                  TransactionTypeCode[] pTxnTypeCode,
                                                                                  TransactionStateCode[] pTxnStateCode) {
        DomainEntitySet<TransactionType> typeSet =
                Application.find(TransactionType.class,
                        new Query<TransactionType>().Where(TransactionType.TransactionTypeCd().in(pTxnTypeCode)));

        DomainEntitySet<TransactionState> stateSet =
                Application.find(TransactionState.class,
                        new Query<TransactionState>().Where(TransactionState.TransactionStateCd().in(pTxnStateCode)));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.TransactionType().in(typeSet.toArray(new TransactionType[typeSet.size()]))
                                .And(FinancialTransaction.CurrentTransactionState().in(stateSet.toArray(new TransactionState[stateSet.size()])))))
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        return Application.find(FinancialTransaction.class, query);
    }

    public static long findFinancialTransactionCountFromLedgerOperations(Company pCompany) {

        DomainEntitySet<TransactionType> typeSet =
                Application.find(TransactionType.class,
                        new Query<TransactionType>().Where(TransactionType.TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit)));

        DomainEntitySet<TransactionState> stateSet =
                Application.find(TransactionState.class,
                        new Query<TransactionState>().Where(TransactionState.TransactionStateCd().equalTo(TransactionStateCode.Created)));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Select(FinancialTransaction.Id().Count())
                        .Where(FinancialTransaction.Company().equalTo(pCompany)
                                .And(FinancialTransaction.CreatorId().equalTo("LedgerOperationsBatchJob"))
                                .And(FinancialTransaction.TransactionType().in(typeSet.toArray(new TransactionType[typeSet.size()])))
                                .And(FinancialTransaction.CurrentTransactionState().in(stateSet.toArray(new TransactionState[stateSet.size()]))));

        return Application.executeScalarAggQuery(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  PayrollRun pPayrollRun,
                                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                                  OfferingServiceChargeType pChargeType) {
        DomainEntitySet<FinancialTransaction> financialTransactions = null;
        OfferingServiceCharge osc = OfferingServiceChargeGroup.findFirstOfferingServiceCharge(pCompany, pChargeType);

        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.Company().equalTo(pCompany)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                                .And(FinancialTransaction.Sku().equalTo(osc.getSKU())))))
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        if (financialTransactions != null) {
            DomainEntitySet<FinancialTransaction> tempFinancialTxns = Application.find(FinancialTransaction.class, query);
            financialTransactions.addAll(tempFinancialTxns);
        } else {
            financialTransactions = Application.find(FinancialTransaction.class, query);
        }

        return financialTransactions;
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  PayrollRun pPayrollRun,
                                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                                  OfferingServiceChargeType pChargeType,
                                                                                  TransactionStateCode pTransactionStateCode) {
        DomainEntitySet<FinancialTransaction> financialTransactions = null;
        OfferingServiceCharge osc = OfferingServiceChargeGroup.findFirstOfferingServiceCharge(pCompany, pChargeType);

        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.Company().equalTo(pCompany)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                                .And(FinancialTransaction.Sku().equalTo(osc.getSKU())
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(pTransactionStateCode))))))
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        if (financialTransactions != null) {
            DomainEntitySet<FinancialTransaction> tempFinancialTxns = Application.find(FinancialTransaction.class, query);
            financialTransactions.addAll(tempFinancialTxns);
        } else {
            financialTransactions = Application.find(FinancialTransaction.class, query);
        }

        return financialTransactions;
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                                  TransactionStateCode pTransactionStateCode) {
        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);
        TransactionState transactionState = Application.findById(TransactionState.class, pTransactionStateCode);

        Expression<FinancialTransaction> query =
        new Query<FinancialTransaction>()
                .Where(FinancialTransaction.Company().equalTo(pCompany)
                        .And(FinancialTransaction.TransactionType().equalTo(transactionType))
                        .And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState)));

        return Application.find(FinancialTransaction.class, query);
    }


    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionsByAssociationType(Company pCompany,
                                                                                                   PayrollRun pPayrollRun,
                                                                                                   TransactionAssociationType pTransactionAssociationType) {
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.Company().equalTo(pCompany)
                                .And(FinancialTransaction.TransactionType().AssociationType().equalTo(pTransactionAssociationType))
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))))
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(
            SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionTypeCode pTxnTypeCode, TransactionStateCode pTxnStateCode) {
        String[] paramNames = new String[4];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "txnType";
        paramNames[3] = "txnState";

        Object[] paramValues = new Object[4];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pCompanyId;
        paramValues[2] = pTxnTypeCode;
        paramValues[3] = pTxnStateCode;

        return Application.findByNamedQuery("findFinTxnByTxnTypeAndTxnState", paramNames, paramValues);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(
            SourceSystemCode pSourceSystemCd, String pCompanyId, TransactionTypeCode pTxnTypeCode,
            TransactionStateCode pTxnStateCode, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        String[] paramNames = new String[6];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "txnType";
        paramNames[3] = "txnState";
        paramNames[4] = "fromDate";
        paramNames[5] = "toDate";

        Object[] paramValues = new Object[6];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pCompanyId;
        paramValues[2] = pTxnTypeCode;
        paramValues[3] = pTxnStateCode;
        paramValues[4] = pFromDate;
        paramValues[5] = pToDate;

        return Application.findByNamedQuery("findFinTxnByTxnTypeAndTxnStateAndDt", paramNames, paramValues);
    }

    public static DomainEntitySet<FinancialTransaction> findFraudCheckFinancialTxns(
            Company pCompany, SpcfCalendar pCheckDate) {
        String[] paramNames = new String[5];
        paramNames[0] = "company";
        paramNames[1] = "depositDate";
        paramNames[2] = "bankAccountType";
        paramNames[3] = "txType";
        paramNames[4] = "txState";

        Object[] paramValues = new Object[5];
        paramValues[0] = pCompany;
        paramValues[1] = pCheckDate;
        paramValues[2] = BankAccountOwnerType.Employee;
        paramValues[3] = TransactionTypeCode.EmployeeDdCredit;
        paramValues[4] = TransactionStateCode.Cancelled;

        return Application.findByNamedQuery("findFraudCheckFinancialTxns", paramNames, paramValues);
    }


    public static DomainEntitySet<FinancialTransaction> findPaycheckSplitFinancialTransactions(
            PayrollRun pPayrollRun, TransactionStateCode pTxnStateCode) {

        Criterion<FinancialTransaction> where = FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                .And(FinancialTransaction.PaycheckSplit().isNotNull());
        if (pTxnStateCode != null) {
            TransactionState transactionState = Application.findById(TransactionState.class, pTxnStateCode);
            where = where.And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState));
        }
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        return Application.find(FinancialTransaction.class, query);
    }


    public static DomainEntitySet<FinancialTransaction> findBillPaymentSplitFinancialTransactions(
            PayrollRun pPayrollRun, TransactionStateCode pTxnStateCode) {

        Criterion<FinancialTransaction> where = FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                .And(FinancialTransaction.BillPaymentSplit().isNotNull());
        if (pTxnStateCode != null) {
            TransactionState transactionState = Application.findById(TransactionState.class, pTxnStateCode);
            where = where.And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState));
        }
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());

        return Application.find(FinancialTransaction.class, query);
    }

    /**
     * Returns the Pending Redebit transaction associated with the original financial transaction.
     *
     * @param pOriginalTransacctionId
     * @return
     */

    public static FinancialTransaction getPendingRedebitTransaction(String pOriginalTransacctionId) {
        FinancialTransaction originalTransaction = Application.findById(
                FinancialTransaction.class,
                SpcfUniqueId.createInstance(
                        pOriginalTransacctionId));

        TransactionState transactionState = Application.findById(
                TransactionState.class,
                TransactionStateCode.Created);

        DomainEntitySet<TransactionType> redebitAssociationTypes =
                TransactionType.findTransactionTypeByAssociationType(TransactionAssociationType.Redebit);
        TransactionType[] transactionTypes = new TransactionType[redebitAssociationTypes.size()];
        int index = 0;
        for (TransactionType transactionType : redebitAssociationTypes) {
            transactionTypes[index++] = transactionType;
        }

        DomainEntitySet<FinancialTransaction> pendingRedebits =
                Application.find(FinancialTransaction.class,
                        OriginalTransaction().equalTo(originalTransaction)
                                .And(CurrentTransactionState().equalTo(transactionState))
                                .And(TransactionType().in(transactionTypes)));

        if (pendingRedebits != null && pendingRedebits.size() > 0) {
            return pendingRedebits.get(0);
        }

        return null;
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(
            Company pCompany,
            FinancialTransaction pOriginalTransaction,
            TransactionTypeCode pTxnTypeCode) {

        TransactionType transactionType = Application.findById(TransactionType.class, pTxnTypeCode);

        return Application.find(FinancialTransaction.class,
                FinancialTransaction.Company().equalTo(pCompany)
                        .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                        .And(FinancialTransaction.OriginalTransaction().equalTo(pOriginalTransaction))));
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionsExcludedType(
            SourceSystemCode pSourceSystemCd, String pCompanyId, BankAccount pBankAccount, TransactionTypeCode pTxnTypeCode,
            TransactionStateCode pTxnStateCode) {
        String[] paramNames = new String[5];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "bankAccount";
        paramNames[3] = "txnType";
        paramNames[4] = "txnState";


        Object[] paramValues = new Object[5];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pCompanyId;
        paramValues[2] = pBankAccount;
        paramValues[3] = pTxnTypeCode;
        paramValues[4] = pTxnStateCode;

        return Application.findByNamedQuery("findFinTxnByCompanyBankAcctExclTxnTypeState", paramNames, paramValues);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(
            SourceSystemCode pSourceSystemCd, String pCompanyId, BankAccount pBankAccount, TransactionTypeCode pTxnTypeCode, SpcfCalendar pProcessingDate, TransactionStateCode pTxnStateCode) {
        String[] paramNames = new String[6];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "sourceCompanyId";
        paramNames[2] = "bankAccount";
        paramNames[3] = "txnType";
        paramNames[4] = "processingDate";
        paramNames[5] = "txnState";

        Object[] paramValues = new Object[6];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pCompanyId;
        paramValues[2] = pBankAccount;
        paramValues[3] = pTxnTypeCode;
        paramValues[4] = pProcessingDate;
        paramValues[5] = pTxnStateCode;

        return Application.findByNamedQuery("findFinTxnByCompanyBankAcctDateTxnTypeState", paramNames, paramValues);
    }

    /**
     * Obtains list of financial transactions for a given company excluding the transactions with the specified
     * transaction type codes and having the specified transaction state codes
     *
     * @param pCompany               PayrollRun object
     * @param pTransactionTypeCodes  String array of Transaction Type codes (optional, pass null if not used)
     * @param pTransactionStateCodes String array of TransactionState Codes (optional, pass null if not used)
     * @return List of company financial transactions
     */

    public static DomainEntitySet<FinancialTransaction> findCompanyFinancialTransactionsExcludingType(
            Company pCompany, TransactionTypeCode[] pTransactionTypeCodes, TransactionStateCode[] pTransactionStateCodes) {

        Criterion<FinancialTransaction> ftCriteria = Company().equalTo(pCompany);

        if (pTransactionTypeCodes != null && pTransactionTypeCodes.length > 0) {
            TransactionType[] transactionTypes = new TransactionType[pTransactionTypeCodes.length];
            for (int index = 0; index < pTransactionTypeCodes.length; index++) {
                transactionTypes[index] = TransactionType.findTransactionType(pTransactionTypeCodes[index]);
            }

            ftCriteria = ftCriteria.And(TransactionType().in(transactionTypes).Not());
        }

        if (pTransactionStateCodes != null && pTransactionStateCodes.length > 0) {
            TransactionState[] transactionStates = new TransactionState[pTransactionStateCodes.length];
            for (int index = 0; index < pTransactionStateCodes.length; index++) {
                transactionStates[index] = Application.findById(TransactionState.class, pTransactionStateCodes[index]);
            }

            ftCriteria = ftCriteria.And(CurrentTransactionState().in(transactionStates));
        }

        Expression query =
                new Query<FinancialTransaction>()
                        .Where(ftCriteria)
                        .OrderBy(CreatedDate());


        return Application.find(FinancialTransaction.class, query);
    }

    /**
     * Obtains list of financial transactions for a given company excluding the transactions with the specified
     * transaction type codes, having the specified transaction state codes and having the specified BankAccountOwner
     * type on the debit or credit side
     *
     * @param pCompany              PayrollRun object
     * @param pTransactionTypeCode  String array of Transaction Type codes (optional, pass null if not used)
     * @param pTransactionStateCode String array of TransactionState Codes (optional, pass null if not used)
     * @return List of company financial transactions
     */

    public static DomainEntitySet<FinancialTransaction> findPendingFinancialTransactions(
            Company pCompany,
            TransactionTypeCode pTransactionTypeCode,
            TransactionStateCode pTransactionStateCode,
            BankAccountOwnerType pBankAccountOwnerType) {

        DomainEntitySet<FinancialTransaction> finTxnCollection = null;

        String[] paramNames = new String[5];
        int i = 0;
        paramNames[i++] = "company";
        paramNames[i++] = "txnType";
        paramNames[i++] = "txnState";
        paramNames[i++] = "bankAccountOwnerType";
        paramNames[i++] = "processingDate";

        Object[] paramValues = new Object[5];
        i = 0;
        paramValues[i++] = pCompany;
        paramValues[i++] = pTransactionTypeCode;
        paramValues[i++] = pTransactionStateCode;
        paramValues[i++] = pBankAccountOwnerType;

        SpcfCalendar processingDate = PSPDate.getPSPTime();


        boolean beforeCutoff =
                pCompany.getOffloadGroup().isBeforeActualCutoffTime(processingDate);
        CalendarUtils.clearTime(processingDate);
        paramValues[i++] = processingDate;
        if (beforeCutoff) {
            finTxnCollection =
                    Application.findByNamedQuery("getPendingFinancialTransactionsBeforeCutoff", paramNames, paramValues);

        } else {
            finTxnCollection =
                    Application.findByNamedQuery("findOffloadedFinTxnBySourceSystemAfterCutoff", paramNames, paramValues);
        }

        return finTxnCollection;
    }

    public static DomainEntitySet<FinancialTransaction> findEmployeeFinancialTransactions(Company pCompany,
                                                                                          String pSourcePayrollRunId,
                                                                                          Employee pEmployee,
                                                                                          EmployeeBankAccount pEmployeeBankAccount,
                                                                                          TransactionTypeCode pTransactionTypeCode,
                                                                                          SpcfCalendar pSettlementDateFrom,
                                                                                          SpcfCalendar pSettlementDateTo) {

        if (pTransactionTypeCode != null) {

            String[] paramNames = new String[7];
            paramNames[0] = "company";
            paramNames[1] = "sourcePayrollRunId";
            paramNames[2] = "employee";
            paramNames[3] = "employeeBnkAct";
            paramNames[4] = "txnTypeCd";
            paramNames[5] = "fromDate";
            paramNames[6] = "toDate";

            Object[] paramValues = new Object[7];

            paramValues[0] = pCompany;

            if (pSourcePayrollRunId != null) {
                paramValues[1] = pSourcePayrollRunId;
            } else {
                paramValues[1] = null;
            }

            if (pEmployee != null) {
                paramValues[2] = pEmployee;
            } else {
                paramValues[2] = null;
            }

            if (pEmployeeBankAccount != null) {
                paramValues[3] = pEmployeeBankAccount.getBankAccount();
            } else {
                paramValues[3] = null;
            }

            paramValues[4] = pTransactionTypeCode;

            if (pSettlementDateFrom != null) {
                paramValues[5] = pSettlementDateFrom;
            } else {
                paramValues[5] = null;
            }

            if (pSettlementDateTo != null) {
                paramValues[6] = pSettlementDateTo;
            } else {
                paramValues[6] = null;
            }
            return Application.findByNamedQuery("findEmpFinTxnByTxnTypeCode", paramNames, paramValues);
        } else {

            String[] paramNames = new String[7];
            paramNames[0] = "company";
            paramNames[1] = "sourcePayrollRunId";
            paramNames[2] = "employee";
            paramNames[3] = "employeeBnkAct";
            paramNames[4] = "fromDate";
            paramNames[5] = "toDate";
            paramNames[6] = "transactionCategory";

            Object[] paramValues = new Object[7];

            paramValues[0] = pCompany;

            if (pSourcePayrollRunId != null) {
                paramValues[1] = pSourcePayrollRunId;
            } else {
                paramValues[1] = null;
            }

            if (pEmployee != null) {
                paramValues[2] = pEmployee;
            } else {
                paramValues[2] = null;
            }

            if (pEmployeeBankAccount != null) {
                paramValues[3] = pEmployeeBankAccount.getBankAccount();
            } else {
                paramValues[3] = null;
            }

            if (pSettlementDateFrom != null) {
                paramValues[4] = pSettlementDateFrom;
            } else {
                paramValues[4] = null;
            }

            if (pSettlementDateTo != null) {
                paramValues[5] = pSettlementDateTo;
            } else {
                paramValues[5] = null;
            }

            paramValues[6] = TransactionCategory.Employee;

            return Application.findByNamedQuery("findEmpFinTxn", paramNames, paramValues);
        }
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  String pSourcePayrollRunId,
                                                                                  Employee pEmployee,
                                                                                  EmployeeBankAccount pEmployeeBankAccount,
                                                                                  CompanyBankAccount pCompanyBankAccount,
                                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                                  SpcfCalendar pSettlementDateFrom,
                                                                                  SpcfCalendar pSettlementDateTo,
                                                                                  TransactionStateCode pTransactionStateCd) {

        if ((pCompanyBankAccount != null) || pEmployee == null) {
            String[] paramNames = new String[8];
            paramNames[0] = "company";
            paramNames[1] = "sourcePayrollRunId";
            paramNames[2] = "employeeBnkAct";
            paramNames[3] = "txnTypeCd";
            paramNames[4] = "fromDate";
            paramNames[5] = "toDate";
            paramNames[6] = "companyBnkAct";
            paramNames[7] = "txnStateCd";

            Object[] paramValues = new Object[8];

            paramValues[0] = pCompany;

            if (pSourcePayrollRunId != null) {
                paramValues[1] = pSourcePayrollRunId;
            } else {
                paramValues[1] = null;
            }

            if (pEmployeeBankAccount != null) {
                paramValues[2] = pEmployeeBankAccount.getBankAccount();
            } else {
                paramValues[2] = null;
            }

            if (pTransactionTypeCode != null) {
                paramValues[3] = pTransactionTypeCode;
            } else {
                paramValues[3] = null;
            }

            if (pSettlementDateFrom != null) {
                paramValues[4] = pSettlementDateFrom;
            } else {
                paramValues[4] = null;
            }

            if (pSettlementDateTo != null) {
                paramValues[5] = pSettlementDateTo;
            } else {
                paramValues[5] = null;
            }

            if (pCompanyBankAccount != null) {
                paramValues[6] = pCompanyBankAccount.getBankAccount();
            } else {
                paramValues[6] = null;
            }

            if (pTransactionStateCd != null) {
                paramValues[7] = pTransactionStateCd;
            } else {
                paramValues[7] = null;
            }

            return Application.findByNamedQuery("findFinTxnByCompanyBankAccount", paramNames, paramValues);
        } else {
            String[] paramNames = new String[8];
            paramNames[0] = "company";
            paramNames[1] = "sourcePayrollRunId";
            paramNames[2] = "employee";
            paramNames[3] = "employeeBnkAct";
            paramNames[4] = "txnTypeCd";
            paramNames[5] = "fromDate";
            paramNames[6] = "toDate";
            paramNames[7] = "txnStateCd";

            Object[] paramValues = new Object[8];

            paramValues[0] = pCompany;

            if (pSourcePayrollRunId != null) {
                paramValues[1] = pSourcePayrollRunId;
            } else {
                paramValues[1] = null;
            }

            if (pEmployee != null) {
                paramValues[2] = pEmployee;
            } else {
                paramValues[2] = null;
            }

            if (pEmployeeBankAccount != null) {
                paramValues[3] = pEmployeeBankAccount.getBankAccount();
            } else {
                paramValues[3] = null;
            }

            if (pTransactionTypeCode != null) {
                paramValues[4] = pTransactionTypeCode;
            } else {
                paramValues[4] = null;
            }

            if (pSettlementDateFrom != null) {
                paramValues[5] = pSettlementDateFrom;
            } else {
                paramValues[5] = null;
            }

            if (pSettlementDateTo != null) {
                paramValues[6] = pSettlementDateTo;
            } else {
                paramValues[6] = null;
            }

            if (pTransactionStateCd != null) {
                paramValues[7] = pTransactionStateCd;
            } else {
                paramValues[7] = null;
            }

            return Application.findByNamedQuery("findFinTxn", paramNames, paramValues);
        }
    }


    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  CompanyBankAccount pCompanyBankAccount,
                                                                                  TransactionStateCode pTransactionStateCd) {
        TransactionState transactionState = Application.findById(TransactionState.class, pTransactionStateCd);

        // Need to use partitioning key in FinancialTransaction query to avoid iterating through all partitions
        SpcfCalendar cbaCreatedDateMinus2Days = pCompanyBankAccount.getCreatedDate();
        cbaCreatedDateMinus2Days.addDays(-2);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(pCompany)
                                                .And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState)
                                                .And(FinancialTransaction.SettlementDate().greaterOrEqualThan(cbaCreatedDateMinus2Days))
                                                .And(FinancialTransaction.CreditBankAccount().equalTo(pCompanyBankAccount.getBankAccount())
                                                     .Or(FinancialTransaction.DebitBankAccount().equalTo(pCompanyBankAccount.getBankAccount()))));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.SettlementDate());

        return Application.find(FinancialTransaction.class,  query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  CompanyBankAccount pCompanyBankAccount,
                                                                                  TransactionTypeCode pTransactionTypeCode) {
        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);

        // Need to use partitioning key in FinancialTransaction query to avoid iterating through all partitions
        SpcfCalendar cbaCreatedDateMinus2Days = pCompanyBankAccount.getCreatedDate();
        cbaCreatedDateMinus2Days.addDays(-2);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(pCompany)
                                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                                                .And(FinancialTransaction.SettlementDate().greaterOrEqualThan(cbaCreatedDateMinus2Days))
                                                .And(FinancialTransaction.CreditBankAccount().equalTo(pCompanyBankAccount.getBankAccount())
                                                     .Or(FinancialTransaction.DebitBankAccount().equalTo(pCompanyBankAccount.getBankAccount()))));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.SettlementDate());

        return Application.find(FinancialTransaction.class,  query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionsByLedgerAccountCode(
            Company pCompany,
            String pSourcePayrollRunId,
            LedgerAccountCode pLedgerAccountCode) {

        DomainEntitySet<FinancialTransaction> finTxnCollection = null;

        if (pSourcePayrollRunId != null) {
            String[] paramNames = new String[3];
            int i = 0;
            paramNames[i++] = "company";
            paramNames[i++] = "sourcePayrollRunId";
            paramNames[i] = "ledgerAccountCode";

            Object[] paramValues = new Object[3];
            i = 0;
            paramValues[i++] = pCompany;
            paramValues[i++] = pSourcePayrollRunId;
            paramValues[i] = pLedgerAccountCode;

            finTxnCollection =
                    Application.findByNamedQuery("findFinancialTransactionsByPayrollRunAndLedgerAccountCd",
                            paramNames, paramValues);
        } else {
            String[] paramNames = new String[2];
            int i = 0;
            paramNames[i++] = "company";
            paramNames[i] = "ledgerAccountCode";

            Object[] paramValues = new Object[2];
            i = 0;
            paramValues[i++] = pCompany;
            paramValues[i] = pLedgerAccountCode;

            finTxnCollection =
                    Application.findByNamedQuery("findFinancialTransactionsByLedgerAccountCd",
                            paramNames, paramValues);
        }

        return finTxnCollection;
    }

    public static ScrollableResults findOffloadedACHFinancialTransactionsInDateRange(
            SourceSystemCode pSourceSystemCd,
            SpcfCalendar pLowerBound,
            SpcfCalendar pUpperBound,
            int pMaxResults) {
        DomainEntitySet<FinancialTransaction> finTxnCollection = null;

        String[] paramNames = new String[5];
        int i = 0;
        paramNames[i++] = "sourceSystemCd";
        paramNames[i++] = "txnState";
        paramNames[i++] = "settlementType";
        paramNames[i++] = "lowerDateBound";
        paramNames[i++] = "upperDateBound";

        TransactionState txnState =
                (TransactionState) Application.findById(TransactionState.class,
                        TransactionStateCode.Executed);
        Object[] paramValues = new Object[5];
        i = 0;
        paramValues[i++] = pSourceSystemCd;
        paramValues[i++] = txnState;
        paramValues[i++] = SettlementType.ACH;
        paramValues[i++] = pLowerBound;
        paramValues[i++] = pUpperBound;

        Integer firstResult = (pMaxResults == -1) ? -1 : 0;
        return Application.scrollableResultsByNamedQuery("findOffloadedFinTxnBySourceSystemAndDateRange", paramNames, paramValues, firstResult, pMaxResults);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                                  String pSourcePayrollRunId,
                                                                                  TransactionCategory pTransactionCategory,
                                                                                  SpcfCalendar pSettlementDateFrom,
                                                                                  SpcfCalendar pSettlementDateTo, String pRequiredFk) {

        Criterion<FinancialTransaction> where = FinancialTransaction.PayrollRun().Company().equalTo(pCompany);

        if (pSourcePayrollRunId != null) {
            where = where.And(FinancialTransaction.PayrollRun().SourcePayRunId().equalTo(pSourcePayrollRunId));
        }

        if (pTransactionCategory != null) {
            where = where.And(FinancialTransaction.TransactionType().TransactionCategory().equalTo(pTransactionCategory));
        }

        if (pSettlementDateFrom != null) {
            where = where.And(FinancialTransaction.SettlementDate().greaterOrEqualThan(pSettlementDateFrom));
        }

        if (pSettlementDateTo != null) {
            where = where.And(FinancialTransaction.SettlementDate().lessOrEqualThan(pSettlementDateTo));
        }

        if (pRequiredFk != null) {
            Criterion<FinancialTransaction> criterion = FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.EmployeeDdCredit);
            // required fk only applys to EmployeeDdCredits
            if (pRequiredFk.equals(FinancialTransaction.PaycheckSplit().getPropertyName())) {
                where = where.And(criterion.Or(FinancialTransaction.PaycheckSplit().isNotNull().And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeDdCredit))));
            } else if (pRequiredFk.equals(FinancialTransaction.BillPaymentSplit().getPropertyName())) {
                where = where.And(criterion.Or(FinancialTransaction.BillPaymentSplit().isNotNull().And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeDdCredit))));
            }
        }

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.SettlementDate());

        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinTxnNotReissuedForPayroll(PayrollRun pPayrollRun, Collection<TransactionTypeCode> pTransactionTypeCollection, Collection<TransactionStateCode> pTransactionStateCollection) {
        String[] paramNames = new String[4];
        int i = 0;
        paramNames[i++] = "payrollRun";
        paramNames[i++] = "company";
        paramNames[i++] = "transactionTypeList";
        paramNames[i] = "transactionStateList";

        Object[] paramValues = new Object[4];
        i = 0;
        paramValues[i++] = pPayrollRun;
        paramValues[i++] = pPayrollRun.getCompany();
        paramValues[i++] = pTransactionTypeCollection;
        paramValues[i] = pTransactionStateCollection;

        return Application.findByNamedQuery("findFinTxnNotReissuedForPayroll", paramNames, paramValues);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionsForPayrollByTypeAndState(
            PayrollRun pPayrollRun, Collection<TransactionTypeCode> pTransactionTypeCollection, Collection<TransactionStateCode> pTransactionStateCollection) {

        return pPayrollRun.getFinancialTransactions(pTransactionTypeCollection.toArray(new TransactionTypeCode[pTransactionTypeCollection.size()]), pTransactionStateCollection.toArray(new TransactionStateCode[pTransactionStateCollection.size()]));
    }

    public static DomainEntitySet<FinancialTransaction> findNonRedebitFeeFinancialTransactions(Company pCompany,
                                                                                               String pSourcePayrollRunId) {
        String[] paramNames = new String[4];
        int i = 0;
        paramNames[i++] = "company";
        paramNames[i++] = "sourcePayrollRunId";
        paramNames[i++] = "excludeTransactionAssociation";
        paramNames[i++] = "feeIndicator";

        Object[] paramValues = new Object[4];
        i = 0;
        paramValues[i++] = pCompany;
        paramValues[i++] = pSourcePayrollRunId;
        paramValues[i++] = TransactionAssociationType.Redebit;
        paramValues[i++] = true;

        return Application.findByNamedQuery("findFeeFinTxnExcludingAssociation", paramNames, paramValues);
    }

    public static DomainEntitySet<FinancialTransaction> findFinTxnForPayrollByTypeAndExclTxnState(
            PayrollRun pPayrollrun, TransactionTypeCode pTxnTypeCode, TransactionStateCode pTxnStateCode) {
        String[] paramNames = new String[3];
        paramNames[0] = "payrollRun";
        paramNames[1] = "txnType";
        paramNames[2] = "txnState";

        Object[] paramValues = new Object[3];
        paramValues[0] = pPayrollrun;
        paramValues[1] = pTxnTypeCode;
        paramValues[2] = pTxnStateCode;

        return Application.findByNamedQuery("findFinTxnForPayrollByTypeAndExclTxnState", paramNames, paramValues);
    }

    public static List<Object[]> findFinancialTransactionsWithCreditDebitCode(Company pCompany,
                                                                              String pSourcePayrollRunId,
                                                                              LedgerAccountCode pLedgerAccountCode) {
        String[] paramNames;
        Object[] paramValues;
        if (null == pSourcePayrollRunId) {
            paramNames = new String[2];
            int i = 0;
            paramNames[i++] = "company";
            paramNames[i++] = "ledgerAccountCode";

            paramValues = new Object[2];
            i = 0;
            paramValues[i++] = pCompany;
            paramValues[i++] = pLedgerAccountCode;
            return Application.executeNamedQuery("findFinTxnsWithCreditDebitIndicator", paramNames, paramValues);
        } else {
            paramNames = new String[3];
            int i = 0;
            paramNames[i++] = "company";
            paramNames[i++] = "sourcePayrollRunId";
            paramNames[i++] = "ledgerAccountCode";

            paramValues = new Object[3];
            i = 0;
            paramValues[i++] = pCompany;
            paramValues[i++] = pSourcePayrollRunId;
            paramValues[i++] = pLedgerAccountCode;
            return Application.executeNamedQuery("findFinTxnsWithCreditDebitIndicatorByPayrollRun", paramNames, paramValues);
        }
    }

    public static ScrollableResults findFeeTransactionsForOffloadedBatch(OffloadBatch pOffloadBatch, int pMaxResults) {
        String[] paramNames = new String[1];
        paramNames[0] = "pOffloadBatch";

        Object[] paramValues = new Object[1];
        paramValues[0] = pOffloadBatch;

        Integer firstResult = (pMaxResults == -1) ? -1 : 0;
        return Application.scrollableResultsByNamedQuery("findFeeTxnsForOffloadBatch", paramNames, paramValues, firstResult, pMaxResults);
    }


    public static DomainEntitySet<FinancialTransaction> findVoidImmediateRefundTransactions(CompanyAdjustmentSubmission pCompanyVoid) {
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.CompanyAdjustmentSubmission().equalTo(pCompanyVoid)
                                .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCredit))
                                .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH))
                                .And(FinancialTransaction.Law().isNull()));

        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findVoidApplyForwardTransactions(CompanyAdjustmentSubmission pCompanyVoid) {
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.CompanyAdjustmentSubmission().equalTo(pCompanyVoid)
                                .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCredit))
                                .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ApplyForward))
                                .And(FinancialTransaction.Law().isNotNull()));

        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findTakeOnReturnTransactions(CompanyAdjustmentSubmission pCompanyVoid) {
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.CompanyAdjustmentSubmission().equalTo(pCompanyVoid)
                                .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyRefundTOR))
                                .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.TakenOnReturn))
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)));

        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransaction(Company company, TransactionTypeCode transactionTypeCode) {
        return findAllFinancialTransaction(company, transactionTypeCode)
                .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled));
    }


    public static DomainEntitySet<FinancialTransaction> findAllFinancialTransaction(Company company, TransactionTypeCode transactionTypeCode, TransactionStateCode... pTransactionStateCode) {
        Criterion<FinancialTransaction> ftCriteria = FinancialTransaction.Company().equalTo(company)
                                .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(transactionTypeCode));
         if (pTransactionStateCode != null && pTransactionStateCode.length > 0) {
           ftCriteria = ftCriteria.And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(pTransactionStateCode));
         }

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(ftCriteria)
                        .OrderBy(FinancialTransaction.SettlementDate().Descending());
        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findAllFinancialTransactions(Company company, TransactionTypeCode... transactionTypeCodes) {
        Criterion<FinancialTransaction> ftCriteria = FinancialTransaction.Company().equalTo(company)
                                                                         .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(transactionTypeCodes));
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(ftCriteria)
                        .OrderBy(FinancialTransaction.SettlementDate().Descending());
        return Application.find(FinancialTransaction.class, query);
    }

    public static DomainEntitySet<FinancialTransaction> findNonCancelledFinancialTransactions(Company company, TransactionTypeCode... transactionTypeCodes) {
        Criterion<FinancialTransaction> ftCriteria = FinancialTransaction.Company().equalTo(company)
                                                                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().in(transactionTypeCodes))
                                                                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(ftCriteria)
                        .OrderBy(FinancialTransaction.SettlementDate().Descending());
        return Application.find(FinancialTransaction.class, query);
    }


    /**
     * Obtains list of financial transactions for a given service excluding the transactions with the specified
     * transaction type codes and having the specified transaction state codes
     *
     *
     * @param pService               Service object
     * @param pTransactionTypeCodes  String array of Transaction Type codes (optional, pass null if not used)
     * @param pTransactionStateCodes String array of TransactionState Codes (optional, pass null if not used)
     * @return List of company financial transactions
     */
    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionsByServiceAndExcludingType(Company pCompany,
                                                                                                           Service pService, TransactionTypeCode[] pTransactionTypeCodes, TransactionStateCode[] pTransactionStateCodes) {

        DomainEntitySet<TransactionType> transactionTypes = pService.getTransactionTypeCollection();
        ArrayList<TransactionType> transactionTypeList = new ArrayList<TransactionType>(transactionTypes);

        if (pTransactionTypeCodes != null && pTransactionTypeCodes.length > 0) {
            for (TransactionTypeCode pTransactionTypeCode : pTransactionTypeCodes) {
                TransactionType transactionType = TransactionType.findTransactionType(pTransactionTypeCode);
                if (transactionTypeList.contains(transactionType)) {
                    transactionTypeList.remove(transactionType);
                }
            }
        }

        if (transactionTypeList.isEmpty()) {
            return new DomainEntitySet<FinancialTransaction>();
        }

        int index = 0;
        TransactionType[] transactionTypesArray = new TransactionType[transactionTypeList.size()];
        for (TransactionType transactionType : transactionTypeList) {
            transactionTypesArray[index++] = transactionType;
        }


        Criterion<FinancialTransaction> ftCriteriaPayrollRunIsNull = TransactionType().in(transactionTypesArray);
        ftCriteriaPayrollRunIsNull = ftCriteriaPayrollRunIsNull.And(Company().equalTo(pCompany));
        Criterion<FinancialTransaction> ftCriteriaPayrollRunIsNotNull = TransactionType().in(transactionTypesArray);
        ftCriteriaPayrollRunIsNotNull = ftCriteriaPayrollRunIsNotNull.And(Company().equalTo(pCompany));

        //Define the type of Payroll Run for the service
        PayrollType payrollType = null;
        switch (pService.getServiceCd()) {
            case BillPayment:
                payrollType = PayrollType.BillPayment;
                break;
            case DirectDeposit:
                payrollType = PayrollType.Regular;
                break;
            default:
                payrollType = null;
                break;
        }

        if (payrollType != null) {

            ftCriteriaPayrollRunIsNull = ftCriteriaPayrollRunIsNull.And(PayrollRun().isNull());
            ftCriteriaPayrollRunIsNotNull = ftCriteriaPayrollRunIsNotNull.And(PayrollRun().isNotNull().And(PayrollRun().PayrollRunType().equalTo(payrollType)));
            //ftCriteriaPayrollRunIsNull = ftCriteriaPayrollRunIsNull.And(PayrollRun().isNull().Or(PayrollRun().isNotNull()));
        }

        if (pTransactionStateCodes != null && pTransactionStateCodes.length > 0) {
            TransactionState[] transactionStates = new TransactionState[pTransactionStateCodes.length];
            for (int txnStateIndex = 0; txnStateIndex < pTransactionStateCodes.length; txnStateIndex++) {
                transactionStates[txnStateIndex] = Application.findById(TransactionState.class, pTransactionStateCodes[txnStateIndex]);
            }

            ftCriteriaPayrollRunIsNull = ftCriteriaPayrollRunIsNull.And(CurrentTransactionState().in(transactionStates));
            ftCriteriaPayrollRunIsNotNull = ftCriteriaPayrollRunIsNotNull.And(CurrentTransactionState().in(transactionStates));
        }

        Expression queryPayrollRunIsNull =
                new Query<FinancialTransaction>()
                        .Where(ftCriteriaPayrollRunIsNull)
                        .OrderBy(CreatedDate());

        Expression queryPayrollRunIsNotNull =
                new Query<FinancialTransaction>()
                        .Where(ftCriteriaPayrollRunIsNotNull)
                        .OrderBy(CreatedDate());

        DomainEntitySet<FinancialTransaction> result = Application.find(FinancialTransaction.class, queryPayrollRunIsNull);
        DomainEntitySet<FinancialTransaction> resultPayrollRunNotNull = Application.find(FinancialTransaction.class, queryPayrollRunIsNotNull);
        result.addAll(resultPayrollRunNotNull);

        return result;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static FinancialTransaction createBookTransferTransaction(Company pCompany,
                                                                     PayrollRun pPayrollRun,
                                                                     TransactionTypeCode txnTypeCode,
                                                                     SpcfMoney txnAmount) {
        TransactionType transactionType = TransactionType.findTransactionType(txnTypeCode);

        IntuitBankAccount creditIntuitBankAccount =
                IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount =
                IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit);

        return createFinancialTransaction(pCompany,
                pPayrollRun,
                null,
                creditIntuitBankAccount.getBankAccount(),
                debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit,
                BankAccountOwnerType.Intuit,
                txnTypeCode,
                txnAmount,
                SettlementType.ACH,
                getSettlementDate(txnTypeCode, pCompany.getOffloadGroup()));
    }

    public static FinancialTransaction createFinancialTransaction(PaycheckSplit paycheckSplit) {

        FinancialTransaction financialTransaction = new FinancialTransaction();

        Company company = paycheckSplit.getPaycheck().getPayrollRun().getCompany();

        // Associate Financial Transaction and Company
        financialTransaction.setCompany(company);

        // Associate Financial Transaction and Payroll Run
        PayrollRun payrollRun = paycheckSplit.getPaycheck().getPayrollRun();
        financialTransaction.setPayrollRun(payrollRun);

        //Associate Financial Transaction and Paycheck Split
        financialTransaction.setPaycheckSplit(paycheckSplit);

        // The Employee Bank Account will be the Credit  Bank Account of the financial transaction
        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Employee);
        financialTransaction.setCreditBankAccount(paycheckSplit.getEmployeeBankAccount().getBankAccount());

        TransactionType transactionType = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdCredit);
        financialTransaction.setTransactionType(transactionType);

        //  Get Intuit Bank Account, which will be the debit bank account of the financial transaction
        //  Transaction type EMPLOYEE_DD_CREDIT, Credit Indicator = "D"
        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setDebitBankAccount(IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit).getBankAccount());

        // Settlement type is ACH
        financialTransaction.setSettlementTypeCd(SettlementType.ACH);

        // Settlement Date is Paycheck Settlement Date
        // or next business day if paycheck date is on a weekend or holiday
        // or next possible deposit date if paycheck date is in the past - backdated payroll
        SpcfCalendar settlementDate = payrollRun.getPaycheckSettlementDate().toLocal();
        financialTransaction.setSettlementDate(settlementDate);
        financialTransaction.setOriginalSettlementDate(settlementDate);

        // Amount
        financialTransaction.setFinancialTransactionAmount(paycheckSplit.getPaycheckSplitAmount());

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Created);

        financialTransaction.setCurrentTransactionState(currentTransactionState);
        financialTransaction = Application.save(financialTransaction);
        FinancialTransactionState financialTransactionState = financialTransaction.addTransactionState(currentTransactionState);


        financialTransaction.addOnHoldReasons(company);

        financialTransaction = Application.save(financialTransaction);

        payrollRun.addFinancialTransaction(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();
        return financialTransaction;
    }

    public static FinancialTransaction createFinancialTransaction(BillPaymentSplit pBillPaymentSplit) {

        FinancialTransaction financialTransaction = new FinancialTransaction();

        Company company = pBillPaymentSplit.getBillPayment().getPayrollRun().getCompany();

        // Associate Financial Transaction and Company
        financialTransaction.setCompany(company);

        // Associate Financial Transaction and Payroll Run
        PayrollRun payrollRun = pBillPaymentSplit.getBillPayment().getPayrollRun();
        financialTransaction.setPayrollRun(payrollRun);

        //Associate Financial Transaction and Bill Payment Split
        financialTransaction.setBillPaymentSplit(pBillPaymentSplit);

        // The Payee Bank Account will be the Credit  Bank Account of the financial transaction
        //Todo Change Owner Type to Payee
        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Employee);
        financialTransaction.setCreditBankAccount(pBillPaymentSplit.getPayeeBankAccount().getBankAccount());

        TransactionType transactionType = Application.findById(TransactionType.class, TransactionTypeCode.EmployeeDdCredit);
        financialTransaction.setTransactionType(transactionType);

        //  Get Intuit Bank Account, which will be the debit bank account of the financial transaction
        //  Transaction type EMPLOYEE_DD_CREDIT, Credit Indicator = "D"
        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setDebitBankAccount(IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit).getBankAccount());

        // Settlement type is ACH
        financialTransaction.setSettlementTypeCd(SettlementType.ACH);

        // Settlement Date is Bill Payment Settlement Date
        // or next business day if Bill Payment date is on a weekend or holiday
        // or next possible deposit date if Bill Payment date is in the past - backdated payroll
        SpcfCalendar settlementDate = payrollRun.getPaycheckSettlementDate().toLocal();
        financialTransaction.setSettlementDate(settlementDate);
        financialTransaction.setOriginalSettlementDate(settlementDate);

        // Amount
        financialTransaction.setFinancialTransactionAmount(pBillPaymentSplit.getAmount());

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Created);

        financialTransaction = Application.save(financialTransaction);
        FinancialTransactionState financialTransactionState = financialTransaction.addTransactionState(currentTransactionState);

        financialTransaction.addOnHoldReasons(company);

        financialTransaction = Application.save(financialTransaction);

        payrollRun.addFinancialTransaction(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();
        return financialTransaction;
    }

    /**
     * Creates a new FinancialTransaction along with a Transaction State Object and the
     * corresponding Ledger Entries
     *
     * @param pCompany
     * @param pPayrollRun
     * @param pPaycheckSplit
     * @param pCreditBankAccount
     * @param pDebitBankAccount
     * @param pCreditBankAccountOwnerType
     * @param pDebitBankAccountOwnerType
     * @param pTransactionTypeCode
     * @param pFinancialTransactionAmount
     * @param pSettlementType
     * @param pSettlementDate
     * @return
     */
    public static FinancialTransaction createFinancialTransaction(Company pCompany,
                                                                  PayrollRun pPayrollRun,
                                                                  PaycheckSplit pPaycheckSplit,
                                                                  BankAccount pCreditBankAccount,
                                                                  BankAccount pDebitBankAccount,
                                                                  BankAccountOwnerType pCreditBankAccountOwnerType,
                                                                  BankAccountOwnerType pDebitBankAccountOwnerType,
                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                  SpcfMoney pFinancialTransactionAmount,
                                                                  SettlementType pSettlementType,
                                                                  SpcfCalendar pSettlementDate) {

        return createFinancialTransaction(pCompany, pPayrollRun, pPaycheckSplit, pCreditBankAccount, pDebitBankAccount,
                pCreditBankAccountOwnerType, pDebitBankAccountOwnerType, pTransactionTypeCode,
                pFinancialTransactionAmount, pSettlementType, pSettlementDate, null, null, 0);
    }

    public static FinancialTransaction createFinancialTransaction(Company pCompany,
                                                                  PayrollRun pPayrollRun,
                                                                  PaycheckSplit pPaycheckSplit,
                                                                  BankAccount pCreditBankAccount,
                                                                  BankAccount pDebitBankAccount,
                                                                  BankAccountOwnerType pCreditBankAccountOwnerType,
                                                                  BankAccountOwnerType pDebitBankAccountOwnerType,
                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                  SpcfMoney pFinancialTransactionAmount,
                                                                  SettlementType pSettlementType,
                                                                  SpcfCalendar pSettlementDate,
                                                                  Law pLaw) {

        FinancialTransaction financialTransaction = createFinancialTransaction(pCompany, pPayrollRun, pPaycheckSplit, pCreditBankAccount, pDebitBankAccount,
                pCreditBankAccountOwnerType, pDebitBankAccountOwnerType, pTransactionTypeCode,
                pFinancialTransactionAmount, pSettlementType, pSettlementDate, null, pLaw);

        financialTransaction = Application.save(financialTransaction);
        if (pPayrollRun != null) {
            pPayrollRun.getFinancialTransactionCollection().add(financialTransaction);
        }

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application
                .findById(TransactionState.class, TransactionStateCode.Created);

        // Call appropriate method depending on the transaction type

        if (financialTransaction.isTaxPaymentTransaction()) {
            FinancialTransactionState financialTransactionState = financialTransaction
                    .addTaxPaymentTransactionState(currentTransactionState);
        } else {
            FinancialTransactionState financialTransactionState = financialTransaction
                    .addTransactionState(currentTransactionState);
        }


        financialTransaction = Application.save(financialTransaction);


        if (pPayrollRun != null) {
            pPayrollRun.addFinancialTransaction(financialTransaction);
        }

        return financialTransaction;

    }

    public static FinancialTransaction createAgencyTaxOverpaymentApplied(Company pCompany,
                                                                         SpcfMoney pFinancialTransactionAmount,
                                                                         SettlementType pSettlementType,
                                                                         SpcfCalendar pSettlementDate,
                                                                         Law pLaw,
                                                                         MoneyMovementTransaction pMMT) {

        FinancialTransaction financialTransaction = createFinancialTransaction(pCompany, null, null, null, null,
                null, null, TransactionTypeCode.AgencyTaxOverpaymentApplied,
                pFinancialTransactionAmount, pSettlementType, pSettlementDate, null, pLaw);

        financialTransaction.setMoneyMovementTransaction(pMMT);
        financialTransaction = Application.save(financialTransaction);

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application
                .findById(TransactionState.class, TransactionStateCode.Created);

        FinancialTransactionState financialTransactionState = financialTransaction
                .addTaxPaymentTransactionState(currentTransactionState);

        pMMT.addAgencyFinancialTransaction(financialTransaction);

        financialTransaction = Application.save(financialTransaction);
        pMMT.getFinancialTransactionCollection().add(financialTransaction);

        if (!Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, ATO_CACHE_KEY)) {
            Application.getSessionCache().addEntityCollection(FinancialTransaction.class, ATO_CACHE_KEY, new DomainEntitySet<FinancialTransaction>());
        }
        Application.getSessionCache().addEntity(FinancialTransaction.class, ATO_CACHE_KEY, financialTransaction);

        return financialTransaction;

    }

    public static FinancialTransaction createAgencyTaxOverpaymentTransaction(PayrollRun pPayrollRun, Law pLaw, BankAccount pDebitBankAccount, SpcfDecimal pAgencyTaxOverpaymentAmount) {
        FinancialTransaction agencyTaxOverpayment =
                FinancialTransaction.createFinancialTransaction(pPayrollRun.getCompany(), pPayrollRun, null, null, pDebitBankAccount,
                        BankAccountOwnerType.TaxAgency, BankAccountOwnerType.Intuit,
                        TransactionTypeCode.AgencyTaxOverpayment,
                        new SpcfMoney(pAgencyTaxOverpaymentAmount),
                        SettlementType.ApplyForward, pPayrollRun.getPaycheckDate(), pLaw);

        agencyTaxOverpayment.updateFinancialTransactionState(TransactionStateCode.Executed);

        return agencyTaxOverpayment;
    }

    public static FinancialTransaction createFinancialTransaction(Company pCompany,
                                                                  PayrollRun pPayrollRun,
                                                                  PaycheckSplit pPaycheckSplit,
                                                                  BankAccount pCreditBankAccount,
                                                                  BankAccount pDebitBankAccount,
                                                                  BankAccountOwnerType pCreditBankAccountOwnerType,
                                                                  BankAccountOwnerType pDebitBankAccountOwnerType,
                                                                  TransactionTypeCode pTransactionTypeCode,
                                                                  SpcfMoney pFinancialTransactionAmount,
                                                                  SettlementType pSettlementType,
                                                                  SpcfCalendar pSettlementDate,
                                                                  String pSKU,
                                                                  FinancialTransaction pOriginalTxn,
                                                                  int pSkuQuantity) {

        FinancialTransaction financialTransaction = createFinancialTransaction(pCompany, pPayrollRun, pPaycheckSplit, pCreditBankAccount, pDebitBankAccount,
                pCreditBankAccountOwnerType, pDebitBankAccountOwnerType, pTransactionTypeCode,
                pFinancialTransactionAmount, pSettlementType, pSettlementDate, pSKU, null);

        // Some transactions will have a SKU Quantity
        financialTransaction.setSkuQuantity(pSkuQuantity);

        // Relate to original (aka "parent") transaction
        if (pOriginalTxn != null) {
            financialTransaction.setOriginalTransaction(pOriginalTxn);
            pOriginalTxn.addAssociatedTransactions(financialTransaction);
        }

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application
                .findById(TransactionState.class, TransactionStateCode.Created);


        financialTransaction = Application.save(financialTransaction);
        if (pPayrollRun != null) {
            pPayrollRun.getFinancialTransactionCollection().add(financialTransaction);
        }

        FinancialTransactionState financialTransactionState = financialTransaction
                .addTransactionState(currentTransactionState);


        financialTransaction = Application.save(financialTransaction);

        if (pOriginalTxn != null) {
            pOriginalTxn.addAssociatedTransactions(financialTransaction);
        }

        if (pPayrollRun != null) {
            pPayrollRun.addFinancialTransaction(financialTransaction);
        }

        return financialTransaction;
    }

    private static FinancialTransaction createFinancialTransaction(Company pCompany,
                                                                   PayrollRun pPayrollRun,
                                                                   PaycheckSplit pPaycheckSplit,
                                                                   BankAccount pCreditBankAccount,
                                                                   BankAccount pDebitBankAccount,
                                                                   BankAccountOwnerType pCreditBankAccountOwnerType,
                                                                   BankAccountOwnerType pDebitBankAccountOwnerType,
                                                                   TransactionTypeCode pTransactionTypeCode,
                                                                   SpcfMoney pFinancialTransactionAmount,
                                                                   SettlementType pSettlementType,
                                                                   SpcfCalendar pSettlementDate,
                                                                   String pSKU,
                                                                   Law pLaw) {


        if (pFinancialTransactionAmount.compareTo(new SpcfMoney("0.00")) < 0) {
            throw new RuntimeException("Financial Transaction Amount must be zero or greater FT Amount:" + pFinancialTransactionAmount.toString() +
                    " PSID:" + pCompany.getSourceCompanyId() + " PayrollRun:" + pPayrollRun.getId());
        }
        FinancialTransaction financialTransaction = new FinancialTransaction();

        financialTransaction.setCompany(pCompany);
        financialTransaction.setPayrollRun(pPayrollRun);
        financialTransaction.setPaycheckSplit(pPaycheckSplit);

        // Debit Account
        financialTransaction.setDebitBankAccountType(pDebitBankAccountOwnerType);
        financialTransaction.setDebitBankAccount(pDebitBankAccount);

        // Transaction Type
        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);
        financialTransaction.setTransactionType(transactionType);

        // Credit Account
        financialTransaction.setCreditBankAccountType(pCreditBankAccountOwnerType);
        financialTransaction.setCreditBankAccount(pCreditBankAccount);

        // Settlement Date
        financialTransaction.setSettlementDate(pSettlementDate);
        financialTransaction.setOriginalSettlementDate(pSettlementDate);

        // Amount
        financialTransaction.setFinancialTransactionAmount(pFinancialTransactionAmount);

        // Some transactions will have a SKU
        financialTransaction.setSku(pSKU);

        // Put transaction on hold if necessary
        financialTransaction.updateOnHold(false);
        financialTransaction.addOnHoldReasons(pCompany);

        if (pLaw != null) {
            financialTransaction.setLaw(pLaw);
        }

        // Settlement type
        financialTransaction.updateSettlementType(pSettlementType);

        financialTransaction.validateCanCreateFinancialTransaction();

        try {
            //Register FT for alert tracking if transaction type is tracked
            if (AlertTransactionObserver.isTransactionTypeTracked(financialTransaction.getTransactionType())) {
                AlertTransactionObserver alertTransactionObserver = AlertTransactionObserver.getRegisteredObserver();
                //Alert observer will check and track this FT if the transaction type is in the tracked transaction types
                alertTransactionObserver.queue(financialTransaction);
            }
        }catch(Exception ex) {
            //Ignore exception if in-case it occurs, we do not want to stop a transaction if we are unable to send out alerts
            //Being extra careful for NPEs
            SpcfLogger logger = Application.getLogger(FinancialTransaction.class);
            if (logger != null) {
                logger.warn("AlertTransactionObserver - Unable to add FT to alert observer FT=" + financialTransaction.getId() + ", psid=" + financialTransaction.getCompany().getSourceCompanyId(), ex);
            }
        }

        return financialTransaction;
    }


    public static FinancialTransaction createERDebitTransaction(PayrollRun payrollRun,
                                                                CompanyBankAccount pCompanyBankAccount,
                                                                TransactionTypeCode pTransactionTypeCode, SpcfMoney pAmount, SettlementType pSettlementType, SpcfCalendar pSettlementDate, CompanyService pCompanyService) {

        FinancialTransaction financialTransaction = new FinancialTransaction();

        financialTransaction.setCompany(payrollRun.getCompany());
        financialTransaction.setPayrollRun(payrollRun);

        // The Company Bank Account will be the Debit Bank Account of the financial transaction
        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Company);
        financialTransaction.setDebitBankAccount(pCompanyBankAccount.getBankAccount());

        // Set Financial Transaction Type and State
        TransactionType transactionType =
                Application.findById(TransactionType.class, pTransactionTypeCode);
        financialTransaction.setTransactionType(transactionType);

        //  Get Intuit Bank Account, which will be the credit bank account of the financial transaction
        //  Transaction type EMPLOYER_DD_DEBIT, Credit Indicator = "C"
        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Intuit);
        // If Direct Debit we don't need a credit account
        if (!financialTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxDirectDebit)) {
            financialTransaction.setCreditBankAccount(IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Credit).getBankAccount());
        } else {
            financialTransaction.setCreditBankAccount(null);
        }


        // Settlement
        financialTransaction.setSettlementTypeCd(pSettlementType);

        // Settlement Date
        if (pSettlementDate != null) {
            financialTransaction.setSettlementDate(pSettlementDate);
            financialTransaction.setOriginalSettlementDate(pSettlementDate);
        } else {
            SpcfCalendar settlementDateCal = findNextAvailableSettlementDate(pCompanyService, payrollRun.getPaycheckSettlementDate());

            financialTransaction.setSettlementDate(settlementDateCal);
            financialTransaction.setOriginalSettlementDate(settlementDateCal);
        }

        // Amount
        financialTransaction.setFinancialTransactionAmount(pAmount);

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState =
                Application.findById(TransactionState.class, TransactionStateCode.Created);
        financialTransaction = Application.save(financialTransaction);
        FinancialTransactionState financialTransactionState =
                financialTransaction.addTransactionState(currentTransactionState);

        financialTransaction.addOnHoldReasons(payrollRun.getCompany());

        financialTransaction = Application.save(financialTransaction);

        payrollRun.addFinancialTransaction(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();

        try {
            //Register FT for alert tracking if transaction type is tracked
            if (AlertTransactionObserver.isTransactionTypeTracked(financialTransaction.getTransactionType())) {
                AlertTransactionObserver alertTransactionObserver = AlertTransactionObserver.getRegisteredObserver();
                //Alert observer will check and track this FT if the transaction type is in the tracked transaction types
                alertTransactionObserver.queue(financialTransaction);
            }
        }catch(Exception ex) {
            //Ignore exception if in-case it occurs, we do not want to stop a transaction if we are unable to send out alerts
            //Being extra careful for NPEs
            SpcfLogger logger = Application.getLogger(FinancialTransaction.class);
            if (logger != null) {
                logger.warn("AlertTransactionObserver - Unable to add FT to alert observer FT=" + financialTransaction.getId() + ", psid=" + financialTransaction.getCompany().getSourceCompanyId(), ex);
            }
        }

        return financialTransaction;

    }

    public static SpcfCalendar findNextAvailableSettlementDate(CompanyService pCompanyService, SpcfCalendar pPaycheckSettlementDate) {
        SpcfCalendar settlementDateCal = calculateSettlementDate(pCompanyService, pPaycheckSettlementDate);

        boolean allowUntimelyPayrolls = SourcePayrollParameter.findSourcePayrollParameter(pCompanyService.getCompany().getSourceSystemCd(),
                                                                                          SourcePayrollParameterCode.AllowOneOffUntimelyPayrolls).getParameterValue().equals("1");
        //This is to handle the case when the paycheck date is in the past due to an untimely payroll
        if (allowUntimelyPayrolls && settlementDateCal.before(getSettlementDate(pCompanyService.getCompany().getOffloadGroup()))) {
            settlementDateCal = getSettlementDate(pCompanyService.getCompany().getOffloadGroup());
        }
        return settlementDateCal;
    }

    public static SpcfCalendar calculateSettlementDate(CompanyService pCompanyService, SpcfCalendar pPaycheckSettlementDate) {
        FundingModel fundingModel = pCompanyService.getEffectiveFundingModel();
        int fundingModelDays = fundingModel.getNumberOfFundingDays();
        SpcfCalendar settlementDateCal = pPaycheckSettlementDate.toLocal().copy();

        CalendarUtils.addBusinessDays(settlementDateCal, -1 * fundingModelDays + 1);
        return settlementDateCal;
    }

    public static FinancialTransaction createHPDETransaction(Company pCompany, PayrollRun pPayrollRun,
                                                             TransactionTypeCode pTransactionType,
                                                             SpcfMoney pAmount, SpcfCalendar pSettlementDate,
                                                             CompanyLaw pCompanyLaw,
                                                             MoneyMovementTransaction pMmt,
                                                             QbdtTransactionInfo pQbdtTransactionInfo) {
        return createHPDETransaction(pCompany,
                pPayrollRun,
                pTransactionType,
                pAmount,
                pSettlementDate,
                pCompanyLaw,
                pCompanyLaw.getLaw(),
                pMmt,
                pQbdtTransactionInfo);
    }

    public static FinancialTransaction createHPDETransaction(Company pCompany, PayrollRun pPayrollRun,
                                                             TransactionTypeCode pTransactionType,
                                                             SpcfMoney pAmount, SpcfCalendar pSettlementDate,
                                                             CompanyLaw pCompanyLaw,
                                                             Law pLaw,
                                                             MoneyMovementTransaction pMmt,
                                                             QbdtTransactionInfo pQbdtTransactionInfo) {
        FinancialTransaction transaction = createFinancialTransaction(pCompany, pPayrollRun,
                null,               // paycheck split,
                null, null,         // credit and debit bank accounts,
                null, null,         // credit and debit bank account types
                pTransactionType,
                pAmount,
                SettlementType.HPDE,
                pSettlementDate,
                pLaw);

        if (pMmt != null) {
            transaction.setMoneyMovementTransaction(pMmt);
            pMmt.getFinancialTransactionCollection().add(transaction);
        }
        if (pQbdtTransactionInfo != null) {
            transaction.setQbdtTransactionInfo(pQbdtTransactionInfo);
            pQbdtTransactionInfo.setFinancialTransaction(transaction);
        }
        transaction.setCompanyLaw(pCompanyLaw);

        Application.save(transaction);

        transaction.addTransactionState(TransactionState.findTransactionState(TransactionStateCode.Executed));
        transaction.addTransactionState(TransactionState.findTransactionState(TransactionStateCode.Completed));

        return transaction;
    }

    public static FinancialTransaction findHPDETransaction(String companyLawSourceId, MoneyMovementTransaction mmt) {
        DomainEntitySet<FinancialTransaction> txns = Application.find(FinancialTransaction.class,
                FinancialTransaction.MoneyMovementTransaction().equalTo(mmt).
                        And(FinancialTransaction.CompanyLaw().SourceId().equalTo(companyLawSourceId)));

        if (txns.size() > 1) {
            StringBuilder financialTransactionList = new StringBuilder();

            for (FinancialTransaction financialTransaction : txns) {
                financialTransactionList.append(financialTransaction.getId()).append(", ");
            }

            throw new RuntimeException("Multiple FinTxn for companyLawSourceId FTs List:" + financialTransactionList);
        } else if (txns.size() == 1) {
            return txns.get(0);
        } else {
            return null;
        }
    }

    public static void createFinancialTransactionsForBillingDetail(BillingDetail pDetail, CompanyBankAccount pCompanyBankAccount,
                                                                   SettlementType pSettlementType, SpcfCalendar pSettlementDate,
                                                                   BigDecimal pTotalAmountOverride, boolean pAllowZeroDollarFeeTransaction) {
        SpcfDecimal feeAmount = null;
        if (pTotalAmountOverride == null) {
            // feeAmount (adjusted for tier) = basePrice + (quantity * unitPrice) - discount
            feeAmount = pDetail.getPretaxAmount();

            // itemTotal = feeAmount + taxAmount
            SpcfDecimal itemTotal = feeAmount.add(pDetail.getTaxAmount());

            pDetail.setItemTotal(new SpcfMoney(itemTotal));
        } else {
            // set the total amount to what the caller gave us
            pDetail.setItemTotal(SpcfUtils.convertToSpcfMoney(pTotalAmountOverride));
            feeAmount = pDetail.getItemTotal();
        }
        
        PayrollRun payrollRun = pDetail.getPayrollRun();

        // if we didn't get a settlement date, compute it based on the DD offload group
        if (pSettlementDate == null) {
            Company company = payrollRun.getCompany();
            pSettlementDate = getSettlementDate(company.getOffloadGroup());
        }

        // if it doesn't already have a fee FT, we'll need to create one
        if (pDetail.getFeeTransaction() == null) {
            boolean createFeeTransaction = pAllowZeroDollarFeeTransaction ? feeAmount.isGreaterThanEqualTo(SpcfMoney.ZERO)
                                                                          : feeAmount.isGreaterThan(SpcfMoney.ZERO);

            if (createFeeTransaction) {
                // get the right Intuit bank account
                IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                        TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit),
                        CreditDebitCode.Credit);

                // create the Fee FT
                FinancialTransaction ftFee = createFinancialTransaction(
                        payrollRun.getCompany(),
                        payrollRun,
                        null,
                        iba.getBankAccount(),
                        (pCompanyBankAccount != null) ? pCompanyBankAccount.getBankAccount() : null,
                        BankAccountOwnerType.Intuit,
                        BankAccountOwnerType.Company,
                        TransactionTypeCode.EmployerFeeDebit,
                        new SpcfMoney(feeAmount),
                        pSettlementType,
                        pSettlementDate,
                        pDetail.getItemSku(),
                        null,
                        pDetail.getQuantity()
                );

                // relate it to the BillingDetail
                pDetail.addFinancialTransaction(ftFee);
                ftFee.setBillingDetail(pDetail);

                //PSP-11809:removing Hold from EmployerFeeDebits
                if(ftFee.getOnHold()) {
                    ftFee.removeOnHoldReasons(ftFee.getCompany(),ftFee.getTransactionType(),ftFee.getSku(),ftFee.getBillingDetail());
                }
            }
        }

        createTaxFinancialTransactionForBillingDetail(pDetail, pCompanyBankAccount, pSettlementType, pSettlementDate, payrollRun);

        // To avoid spcf stale state exception
        pDetail.setPayrollRun(payrollRun);

        payrollRun.addBillingDetail(Application.save(pDetail));
    }

    public static void createTaxFinancialTransactionForBillingDetail(BillingDetail pDetail, CompanyBankAccount pCompanyBankAccount, SettlementType pSettlementType, SpcfCalendar pSettlementDate, PayrollRun pPayrollRun) {
        // if there is any sales tax, create the FT
        if (pDetail.getTaxAmount().isGreaterThan(SpcfMoney.ZERO)) {
            // get the right Intuit bank account
            IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                    TransactionType.findTransactionType(TransactionTypeCode.ServiceSalesAndUseTax),
                    CreditDebitCode.Credit);

            // create the Sales Tax FT
            FinancialTransaction ftTax = createFinancialTransaction(
                    pPayrollRun.getCompany(),
                    pPayrollRun,
                    null,
                    iba.getBankAccount(),
                    (pCompanyBankAccount != null) ? pCompanyBankAccount.getBankAccount() : null,
                    BankAccountOwnerType.Intuit,
                    BankAccountOwnerType.Company,
                    TransactionTypeCode.ServiceSalesAndUseTax,
                    pDetail.getTaxAmount(),
                    pSettlementType,
                    pSettlementDate,
                    pDetail.getItemSku(),
                    null,
                    pDetail.getQuantity());

            // relate it to the BillingDetail
            pDetail.addFinancialTransaction(ftTax);
            ftTax.setBillingDetail(pDetail);

            //PSP-11809: removing hold from ServiceSalesAndUseTax transaction
            if(ftTax.getOnHold()) {
                ftTax.removeOnHoldReasons(ftTax.getCompany(),ftTax.getTransactionType(),ftTax.getSku(),ftTax.getBillingDetail());
            }

        }
    }

    public static FinancialTransaction createApplyForwardTaxCredit(Company pCompany, PayrollRun pPayrollRun,
                                                                   SpcfDecimal pAmount, SpcfCalendar pSettlementDate,
                                                                   Law pLaw, TransactionTypeCode pTransactionTypeCode,
                                                                   SettlementType pSettlementType) {
        FinancialTransaction transaction = createFinancialTransaction(pCompany, pPayrollRun,
                null, null, null, // paycheck split, credit and debit bank accounts
                null, null,       // credit and debit bank account types
                pTransactionTypeCode,
                new SpcfMoney(pAmount),  //total credit amount for the lawId
                pSettlementType,
                pSettlementDate,
                pLaw);

        transaction.setRefundType(RefundType.ApplyForward);
        transaction = Application.save(transaction);
        return transaction;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Other static methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactions(
            SourceSystemCode pSourceSystemCd,
            String pCompanyId,
            BankAccount pBankAccount,
            TransactionTypeCode pTxnTypeCode,
            TransactionStateCode pTxnStateCode) {

        Company company = Company.findCompany(pCompanyId, pSourceSystemCd);
        TransactionType transactionType = Application.findById(TransactionType.class, pTxnTypeCode);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                .And(FinancialTransaction.TransactionType().equalTo(transactionType));
        if (pTxnStateCode != null) {
            TransactionState transactionState = Application.findById(TransactionState.class, pTxnStateCode);
            where = where.And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState));
        }

        where = where.And(FinancialTransaction.CreditBankAccount().equalTo(pBankAccount)
                .Or(FinancialTransaction.DebitBankAccount().equalTo(pBankAccount)));

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .OrderBy(FinancialTransaction.CreatedDate().Descending());


        return Application.find(FinancialTransaction.class, query);
    }

    /**
     * Generates a random Amount for a verification transaction between .01 and .99 inclusive
     *
     * @return Verification amount between .01 and .99
     */
    public static SpcfMoney generateRandomAmount() {
        // Math.random() will generate between 0 and 1 not including 1.
        double number = (Math.random() * 98) + 1;

        return new SpcfMoney(SpcfDecimal.createInstance(number / 100));
    }

    /**
     * Returns the earliest possible settlement date. This does not contain time component.
     *
     * @param txnTypeCode  Transaction Type Code
     * @param offloadGroup Offload Group object
     * @return Settlement Date
     */
    public static SpcfCalendar getSettlementDate(TransactionTypeCode txnTypeCode, OffloadGroup offloadGroup) {
        SpcfCalendar settlementDate = PSPDate.getPSPTime();

        if (offloadGroup == null) {
            // runtime exception
            //eventLogger.eventInfo("Offload Group is not specified ");
            //throw new DDBusinessException(DDBusinessException.ErrorCodes.OFFLOAD_GROUP_CODE_NOT_SPECIFIED, DDBusinessException.Severity.ERROR);
        }
        boolean beforeCutoff = offloadGroup.isBeforeActualCutoffTime();
        // for EE DD Credit transactions, the settlement date
        // is (today + 2 biz days) if beforeCutoff and (today + 3 biz days) if not
        // for any other types of transactions, the settlement date is (today + 1 biz day)
        // if beforeCutoff and (today + 2 biz days if after)
        int addDays = (beforeCutoff && !CalendarUtils.isWeekendOrHoliday(settlementDate)) ? 0 : 1;
        if (txnTypeCode.equals(TransactionTypeCode.EmployeeDdCredit)) {
            CalendarUtils.addBusinessDays(settlementDate, 2 + addDays);
        } else {
            CalendarUtils.addBusinessDays(settlementDate, 1 + addDays);
        }

        CalendarUtils.clearTime(settlementDate);
        return settlementDate;
    }

    /**
     * Returns the earliest possible settlement date. This does not contain time component.
     *
     * @param offloadGroup Offload Group Enum
     * @return Settlement Date
     */
    public static SpcfCalendar getSettlementDate(OffloadGroup offloadGroup) {
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        if (offloadGroup == null) {
            //eventLogger.eventInfo("Offload Group is not specified ");
            //throw new DDBusinessException(DDBusinessException.ErrorCodes.OFFLOAD_GROUP_CODE_NOT_SPECIFIED, DDBusinessException.Severity.ERROR);
        }
        boolean beforeCutoff = offloadGroup.isBeforeActualCutoffTime();
        // the settlement date is (today + 1 biz day) if before cutoff and (today + 2 biz days) if after
        CalendarUtils.addBusinessDays(settlementDate, (beforeCutoff && !CalendarUtils.isWeekendOrHoliday(settlementDate)) ? 1 : 2);
        CalendarUtils.clearTime(settlementDate);
        return settlementDate;
    }

    /**
     * Returns the earliest possible settlement date. This does not contain time component.
     *
     * @param
     * @return Settlement Date
     */
    public static SpcfCalendar getSettlementDate(CompanyService pCompanyService, PayrollRun pPayrollRun) {
        return findNextAvailableSettlementDate(pCompanyService, pPayrollRun.getPaycheckSettlementDate());
    }


    public static void cancelPendingEmployerVerificationDebits(Company pCompany) {
        DomainEntitySet<FinancialTransaction> finTxCollection =
                FinancialTransaction.findAllFinancialTransaction(pCompany, TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);

        for (FinancialTransaction finTx : finTxCollection) {
            MoneyMovementTransaction mmTx = finTx.getMoneyMovementTransaction();
            if (mmTx == null || mmTx.isPendingMMT()) {
                FinancialTransaction financialTx =
                        finTx.updateFinancialTransactionState(TransactionStateCode.Cancelled);
                TransactionResponse.createTransactionResponseForFinancialTx(financialTx);
            }
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public FinancialTransaction() {
        super();
        setRefundType(null);
    }

    /**
     * Validates Financial Transaction Data Entity
     *
     * @return ProcessResult with any errors, otherwise empty collection if all validations pass
     */

    public ProcessResult validateFinancialTransaction() {
        ProcessResult processResult = new ProcessResult();

        if (this == null) {
            return processResult;
        }

        if (getCreditBankAccount() != null) {
            processResult.merge(getCreditBankAccount().validateBankAccount());

        }

        if (getDebitBankAccount() != null) {
            processResult.merge(getDebitBankAccount().validateBankAccount());
        }

        return processResult;

    }

    /**
     * Gets the Name on the Debit Bank Account associated with
     * this financial transaction based on the Account Owner Type
     *
     * @return Name on the debit account
     */
    public String getNameOnDebitBankAccount(
    ) {

        BankAccountOwnerType bankAccountOwnerType = getDebitBankAccountType();

        return getNameOnBankAccount(bankAccountOwnerType, CreditDebitCode.Debit);
    }

    /**
     * Gets the Name on the Credit Bank Account associated with
     * this financial transaction based on the Account Owner Type
     *
     * @return Name on the Bank Account
     */
    public String getNameOnCreditBankAccount() {

        BankAccountOwnerType bankAccountOwnerType = getCreditBankAccountType();

        return getNameOnBankAccount(bankAccountOwnerType, CreditDebitCode.Credit);
    }

    /**
     * Gets the Individual ID on the Debit Bank Account associated with
     * this financial transaction based on the Account Owner Type
     *
     * @return Individual ID
     */
    public String getIndividualIDOnDebitBankAccount(
    ) {

        BankAccountOwnerType bankAccountOwnerType = getDebitBankAccountType();
        BankAccount debitBankAccount = getDebitBankAccount();

        return getIndividualIDOnBankAccount(debitBankAccount, bankAccountOwnerType);
    }

    /**
     * Gets the Individual ID on the Credit Bank Account associated with
     * this financial transaction based on the Account Owner Type
     *
     * @return Individual ID
     */
    public String getIndividualIDOnCreditBankAccount() {

        BankAccountOwnerType bankAccountOwnerType = getCreditBankAccountType();
        BankAccount creditBankAccount = getCreditBankAccount();
        return getIndividualIDOnBankAccount(creditBankAccount, bankAccountOwnerType);
    }

    /**
     * Gets the Employee Bank Account Associated with this Financial Transaction
     *
     * @return Employee bank Account
     */

    public EmployeeBankAccount getEmployeeBankAccount() {
        EmployeeBankAccount employeeBankAccount = null;

        PaycheckSplit paycheckSplit = getPaycheckSplit();

        if (paycheckSplit == null && getTransactionType()
                .getTransactionTypeCd().equals(TransactionTypeCode.EmployeeDdReversalDebit)) {
            // Original transaction is the one that was reversed
            FinancialTransaction originalFinancialTransaction = getOriginalTransaction();
            assert (originalFinancialTransaction != null);
            paycheckSplit = originalFinancialTransaction.getPaycheckSplit();
        }

        if (paycheckSplit != null) {
            employeeBankAccount = paycheckSplit.getEmployeeBankAccount();
        }

        return employeeBankAccount;
    }

    /**
     * Gets the Employee Bank Account Associated with this Financial Transaction
     *
     * @return Employee bank Account
     */

    public PayeeBankAccount getPayeeBankAccount() {
        PayeeBankAccount payeeBankAccount = null;

        BillPaymentSplit billPaymentSplit = getBillPaymentSplit();

        if (billPaymentSplit == null && getTransactionType()
                .getTransactionTypeCd().equals(TransactionTypeCode.EmployeeDdReversalDebit)) {
            // Original transaction is the one that was reversed
            FinancialTransaction originalFinancialTransaction = getOriginalTransaction();
            assert (originalFinancialTransaction != null);
            billPaymentSplit = originalFinancialTransaction.getBillPaymentSplit();
        }

        if (billPaymentSplit != null) {
            payeeBankAccount = billPaymentSplit.getPayeeBankAccount();
        }

        return payeeBankAccount;
    }

    /**
     * Gets the Name on the Bank Account based on the Bank Account Owner Type
     *
     * @param pBankAccountOwnerType Owner type (such as Employee, Company, Intuit or Bank Agency)
     * @return Name on bank account
     */
    private String getNameOnBankAccount(
            BankAccountOwnerType pBankAccountOwnerType, CreditDebitCode pCreditDebit) {
        String name = null;
        switch (pBankAccountOwnerType) {
            case Company:
                name = getCompany().getLegalName();
                break;
            case Employee:
                if (getEmployeeBankAccount() != null) {
                    name = getEmployeeBankAccount().getEmployee().getFullName();
                } else {
                    if (this.getBillPaymentSplit() != null) {
                        name = this.getBillPaymentSplit().getBillPayment().getPayee().getName();
                    } else {
                        name = this.getPayeeBankAccount().getPayee().getName();
                    }
                }

                break;
            case Intuit:
                name = IntuitBankAccount.findIntuitBankAccount(getTransactionType(), pCreditDebit).getDescription();
                break;
            case TaxAgency:
                break;
        }

        return name;
    }

    /**
     * Gets the Individual ID on the Bank Account based on the Bank Account Owner Type
     *
     * @param pBankAccount          Bank Account
     * @param pBankAccountOwnerType Bank Account owner type (employee, company, intuit, or tax agency)
     * @return Individual ID
     */
    private String getIndividualIDOnBankAccount(
            BankAccount pBankAccount,
            BankAccountOwnerType pBankAccountOwnerType) {
        String id = null;
        switch (pBankAccountOwnerType) {
            case Company:
                id = getCompany().getFedTaxId();
                break;
            case Employee:
                if (pBankAccount != null) {
                    id = pBankAccount.getAccountNumber();
                    if (id != null && id.length() > 9) {
                        id = id.substring(0, 9);
                    }
                }
                break;
            case Intuit:
                if (pBankAccount != null) {
                    id = pBankAccount.getAccountNumber();
                }
                break;
            case TaxAgency:
                if (pBankAccount != null) {
                    id = pBankAccount.getAccountNumber();
                }
                break;
        }

        return id;

    }

    public boolean isCancellationAllowed() {
        if (null == getMoneyMovementTransaction()) {
            return true;
        }
        SpcfCalendar initDate = getMoneyMovementTransaction().getInitiationDate().toLocal();

        SpcfCalendar limitCalendar = getCompany().getOffloadGroup().getCalendarForCutoffTime(initDate);

        SpcfCalendar now = PSPDate.getPSPTime();

        if (now.after(limitCalendar)) {
            return false;
        }
        return true;
    }

    public boolean isEmployerTaxTransaction() {
        switch (getTransactionType().getTransactionTypeCd()) {
            case EmployerTaxDebit:
            case EmployerTaxRedebit:
            case EmployerTaxCredit:
                return true;
            default:
                return false;
        }
    }

    public boolean isEmployerDebitTransaction() {
        switch (getTransactionType().getTransactionTypeCd()) {
            case EmployerTaxDebit:
            case EmployerTaxRedebit:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isTaxPaymentTransaction() {
        switch (getTransactionType().getTransactionTypeCd()) {
            case AgencyTaxCredit:
            case AgencyTaxDebit:
            case AgencyTaxOverpayment:
            case AgencyTaxOverpaymentApplied:
            case AgencyDirectCredit:
            case AgencyDirectDebit:
            case AgencyDirectOverpayment:    
            case EmployerTaxDirectDebit:
            case EmployerTaxDirectOverpaymentApplied:    
                return true;

            default:
                return false;
        }
    }

    public boolean isEmployerVerificationDebit() {
        return TransactionTypeCode.EmployerVerificationDebit.equals(getTransactionType().getTransactionTypeCd());
    }

    /**
     * Cancels a FinancialTransaction, adding a new FinancialTransactionState and the
     * corresponding Ledger Entries
     */
    public FinancialTransaction cancelFinancialTransaction() {
       return cancelFinancialTransaction(Boolean.FALSE, null);
    }

    /**
     * Cancels a FinancialTransaction, adding a new FinancialTransactionState and the
     * corresponding Ledger Entries
     */
    public FinancialTransaction cancelFinancialTransaction(boolean setCreatorId, String creatorId) {
        // Set the FinancialTransactionState to CANCELLED
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Cancelled);
        if (isTaxPaymentTransaction()) {
            FinancialTransactionState financialTxnState = addTaxPaymentTransactionState(txnState);
        } else {
            FinancialTransactionState financialTxnState = addTransactionState(txnState);
        }

        if(setCreatorId){
            FinancialTransactionState financialTxnState = getCurrentFinancialTransactionState();
            financialTxnState.setCreatorId(creatorId);
            Application.save(financialTxnState);
        }

        return Application.save(this);
    }

    /**
     * Adds a new state for a financial transaction only if the state doesn't already exist
     *
     * @param pTransactionState Transaction status to add to the history
     * @return Newly created FinancialTransactionState (will need saved)
     */
    public FinancialTransactionState addTransactionState(TransactionState pTransactionState) {
        FinancialTransactionState financialTransactionState = new FinancialTransactionState();
        financialTransactionState.setFinancialTransaction(this);
        financialTransactionState.setCompany(this.getCompany());
        financialTransactionState.setTransactionType(this.getTransactionType());
        financialTransactionState.setTransactionState(pTransactionState);
        financialTransactionState.setTransactionStateEffectiveDate(PSPDate.getTimeZoneIndependentDate(PSPDate.getPSPTime()));
        setCurrentTransactionState(pTransactionState);
        financialTransactionState = Application.save(financialTransactionState);
        mCurrentTransactionStateQueue.push(financialTransactionState);

        //If this financial transaction is a type that requires a MoneyMovement transaction AND
        // the state is created- add a new money movement transaction
        // the state is cancelled- delete any associated money movement transactions
        if (MoneyMovementTransaction.isSettlementTypeForMMTxn(getSettlementTypeCd())) {
            if (pTransactionState.getTransactionStateCd().equals(TransactionStateCode.Created)) {
                MoneyMovementTransaction.addFinancialTransactionToMMT(this);
            }
        }
        if (pTransactionState.getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
            MoneyMovementTransaction.subtractFinancialTransaction(this);
        }

        if (isEmployerDebitTransaction()) {
            if (pTransactionState.getTransactionStateCd().equals(TransactionStateCode.Completed)) {
                DomainEntitySet<FinancialTransaction> financialTransactionsToMove = new DomainEntitySet<FinancialTransaction>();
                // business rule: Intuit does not hold payment for taxes successfully impounded
                // ensure tax liability associated w/completed ERTaxDebit is on a Payment MMT in ReadyToSend or Agent Hold status
                DomainEntitySet<FinancialTransaction> taxPaymentTransactions = getPayrollRun().getTaxPaymentTransactions();
                for (FinancialTransaction taxPaymentTransaction : taxPaymentTransactions) {
                    MoneyMovementTransaction paymentMMT = taxPaymentTransaction.getMoneyMovementTransaction();
                    if (paymentMMT.getTaxPaymentStatus().equals(TaxPaymentStatus.OnHold)) {
                        // if the only hold is a company hold, move tax liabilities associated w/completed tax impounds to ReadyToSend Payment
                        // if the payment MMT has an Agent or Enrollment hold, no split/combine
                        if (paymentMMT.getActiveOnHoldReasons().size() == 1 && paymentMMT.hasActiveOnHoldReason(PaymentOnHoldReason.Company)) {
                            financialTransactionsToMove.add(taxPaymentTransaction);
                        }
                    }
                }
                // If financial transactions need to be removed and added, It has to be in correct order.
                if(financialTransactionsToMove.isNotEmpty()) {
                    MoneyMovementTransaction.removeAndAddAgencyTransactions(financialTransactionsToMove);
                }
            }
        }

        return financialTransactionState;
    }


    public FinancialTransactionState addTaxPaymentTransactionState(TransactionState pTransactionState) {
        FinancialTransactionState financialTransactionState = new FinancialTransactionState();
        financialTransactionState.setFinancialTransaction(this);
        financialTransactionState.setCompany(this.getCompany());
        financialTransactionState.setTransactionType(this.getTransactionType());        
        financialTransactionState.setTransactionState(pTransactionState);
        financialTransactionState.setTransactionStateEffectiveDate(PSPDate.getTimeZoneIndependentDate(PSPDate.getPSPTime()));
        setCurrentTransactionState(pTransactionState);
        financialTransactionState = Application.save(financialTransactionState);
        mCurrentTransactionStateQueue.push(financialTransactionState);

        //If Transaction Type is AgencyTaxCredit and status = Created or Transaction Type is AgencyTaxDebit and status is canceled, add the financial transaction
        //If Transaction Type is AgencyTaxCredit and status = Cancelled or Transaction Type is AgencyTaxDebit and status is created, subtract the financial transaction
        if (isTaxPaymentTransaction()) {
            if (getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxOverpaymentApplied) {
                switch (pTransactionState.getTransactionStateCd()) {
                    case Cancelled:
                    case Voided:
                        MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(this);
                        if (Application.getSessionCache().isEntityCollectionCached(FinancialTransaction.class, ATO_CACHE_KEY)) {
                            Application.getSessionCache().removeEntity(FinancialTransaction.class, ATO_CACHE_KEY, this);
                        }
                        break;
                }

            }
            // overpayment transactions are not added to an mmt
            else if (getTransactionType().getTransactionTypeCd() != TransactionTypeCode.AgencyTaxOverpayment) {
                switch (pTransactionState.getTransactionStateCd()) {
                    case Created:
                        MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(this);
                        break;
                    case Cancelled:
                    case Voided:
                        MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(this);
                        break;
                }
            }
        }

        return financialTransactionState;
    }

    /**
     * Gets the Company Bank Account based on the FinancialTransaction
     *
     * @return CompanyBankAccount
     */
    public CompanyBankAccount getCompanyBankAccount() {
        BankAccount bankAccount = null;
        CompanyBankAccount companyBankAccount = null;

        if (BankAccountOwnerType.Company.equals(getCreditBankAccountType())) {
            bankAccount = getCreditBankAccount();
        } else if (BankAccountOwnerType.Company.equals(getDebitBankAccountType())) {
            bankAccount = getDebitBankAccount();
        }

        if (bankAccount != null) {
            companyBankAccount = CompanyBankAccount.findCompanyBankAccount(getCompany(), bankAccount);
        }

        return companyBankAccount;
    }

    /**
     * This method returns a set of balances representing a kind of "mini-ledger" for a group of related
     * FinancialTransactions.  It is intended to be used for an original "debit" transaction and all related redebits,
     * refund credits, returned refund credits (aka recredits), writeoffs and recoveries.  It include the give FT and
     * all other FTs related to it -- whether directly or indirectly -- by their OriginalTransaction properties.
     * <p/>
     * It calculates five "mini-ledger balances" based on the type (group) and CURRENT STATE of each FT it is fed.
     * Unlike the real ledger, which accounts for the history of each FT, this class is a snapshot, based only on the
     * current state of each FT at the time the summary is made.
     *
     * @return a TransactionSummary containing the summary balances
     */
    public TransactionSummary summarizeRelatedTransactions() {
        TransactionSummary summary = new TransactionSummary();

        // the list of related FTs still needing to be included in the summary
        DomainEntitySet<FinancialTransaction> related = new DomainEntitySet<FinancialTransaction>();

        // put the first FT in the list, then keep going until the list is empty
        for (related.add(this); related.size() > 0; related.remove(0)) {
            // process the head FT
            summary.updateBalances(related.get(0));

            // append FTs related to the head
            related.get(0).getRelatedTransactions(related);
        }

        // all done
        return summary;
    }


    /**
     * Finds all FTs whose OriginalTransaction property refers to the given FT and appends them to the list.
     *
     * @param pRelated
     * @see #summarizeRelatedTransactions()
     */
    public void getRelatedTransactions(DomainEntitySet<FinancialTransaction> pRelated) {
        DomainEntitySet<FinancialTransaction> found = getAssociatedTransactionsCollection();

        // add all of them to the list of related FTs
        for (FinancialTransaction relatedFT : found) {
            pRelated.add(relatedFT);
        }
    }

    /**
     * "Excluded" means the amount should not be counted toward any summary balance.
     *
     * @return true if the FT is neither "open" nor "closed" based on CURRENT transaction state
     * @see #summarizeRelatedTransactions()
     * @see #isOpen()
     * @see #isClosed()
     */
    public boolean isExcluded() {
        return (!this.isOpen() && !this.isClosed());
    }

    /**
     * "Open" means the amount did NOT make it to the right place.
     *
     * @return true if the amount did NOT make it to the right place, based on CURRENT transaction state
     * @see #summarizeRelatedTransactions()
     * @see #isExcluded()
     * @see #isClosed()
     */
    public boolean isOpen() {
        TransactionStateCode ftState = calculateCurrentTransactionState().getTransactionStateCd();
        return (ftState == TransactionStateCode.Returned);
    }

    /**
     * "Closed" means that the amount DID make it to the right place.
     *
     * @return true if the amount did NOT make it to the right place, based on CURRENT transaction state
     * @see #summarizeRelatedTransactions()
     * @see #isExcluded()
     * @see #isOpen()
     */
    public boolean isClosed() {
        TransactionStateCode ftState = calculateCurrentTransactionState().getTransactionStateCd();
        TransactionTypeGroupCode ftGroup = getTransactionType().getTransactionTypeGroupCd();
        if (ftGroup == TransactionTypeGroupCode.Debit || ftGroup == TransactionTypeGroupCode.Redebit || ftGroup == TransactionTypeGroupCode.CustomerRecovery) {
            // conservative approach...
            return ftState.equals(TransactionStateCode.Completed);
        } else {
            return (ftState.equals(TransactionStateCode.Completed) || ftState.equals(TransactionStateCode.Executed));
        }
    }

    public TransactionState calculateCurrentTransactionState() {
        /*
            PSRV002768: Remove ft status update (to executed) in offload process
            To do that, we need to test if the associated mmt is executed and this ft is still created
            That means that the offload has started, commited mmt, and the ft update will soon follow - so we can
            consider this ft as executed
         */
        TransactionState currentState = getCurrentTransactionState();
        if (currentState.getTransactionStateCd() == TransactionStateCode.Created && getSettlementDate() != null && 
                getSettlementDate().subtract(PSPDate.getPSPTime()) < 5 * 86400000 /* 5 days*/ &&
                getMoneyMovementTransaction() != null &&
                !getMoneyMovementTransaction().isPendingMMT()) {
            currentState = TransactionState.findTransactionState(TransactionStateCode.Executed);
        }
        return currentState;
    }

    public boolean isPending() {
        TransactionStateCode ftState = calculateCurrentTransactionState().getTransactionStateCd();
        TransactionTypeGroupCode ftGroup = getTransactionType().getTransactionTypeGroupCd();
        if (ftGroup == TransactionTypeGroupCode.Debit || ftGroup == TransactionTypeGroupCode.Redebit || ftGroup == TransactionTypeGroupCode.CustomerRecovery) {
            // conservative approach...
            return (ftState.equals(TransactionStateCode.Created) || ftState.equals(TransactionStateCode.Executed));
        }
        if (getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxCredit && getSettlementTypeCd() == SettlementType.ACH) {
            // conservative approach...
            return (ftState.equals(TransactionStateCode.Returned));
        } else {
            return false;
        }
    }

    public boolean isCancelled() {
        TransactionStateCode ftState = calculateCurrentTransactionState().getTransactionStateCd();
        return ftState.equals(TransactionStateCode.Cancelled);
    }

    public boolean isVoided() {
        return getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd() == TransactionStateCode.Voided;
    }

    public void addOnHoldReasons(Company pCompany) {
        for (OnHoldReason onHoldReason : pCompany.getCurrentOnHoldReasons()) {
            ServiceSubStatus onHoldStatus = Application.findById(ServiceSubStatus.class, onHoldReason.getOnHoldReasonCd());
            if (TransactionType.isExcludedFromOffload(getTransactionType(), getSku(), onHoldStatus, getBillingDetail())) {
                updateOnHold(true);
                addOnHoldReason(onHoldReason);
            }
        }
    }

    /**
     * PSP-11809
     * Verify if the transaction can be removed from hold after billing association
     * If yes, remove hold and on hold reason
     * @param pCompany
     */
    public void removeOnHoldReasons(Company pCompany,TransactionType pTransactionType,String pSku,BillingDetail pBillingDetail ) {
        for (OnHoldReason onHoldReason : pCompany.getCurrentOnHoldReasons()) {
            ServiceSubStatus onHoldStatus = Application.findById(ServiceSubStatus.class, onHoldReason.getOnHoldReasonCd());
            if (TransactionType.isOffloadableFeeOnBillingAssociation(pTransactionType, pSku, onHoldStatus, pBillingDetail)) {
                updateOnHold(false);
                removeOnHoldReason(onHoldReason);
            }
        }
    }

    public SpcfCalendar getTransactionCompletionDate() {
        Company company = getCompany();

        SpcfCalendar origPlus = getSettlementDate().toLocal();
        int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);
        CalendarUtils.addBusinessDays(origPlus, achWaitPeriodDays);

        return origPlus;
    }


    public Collection<OnHoldReason> getCurrentOnHoldReasons() {
        Collection<OnHoldReason> currentOnHoldReasons = new ArrayList<OnHoldReason>();
        for (OnHoldReason onHoldReason : getOnHoldReasonCollection()) {
            if (null == onHoldReason.getExpirationDate()) {
                currentOnHoldReasons.add(onHoldReason);
            }
        }
        return currentOnHoldReasons;
    }

    /**
     * Uptes a FinancialTransaction along with a Transaction State Object and the
     * corresponding Ledger Entries
     *
     * @param pTransactionStateCd
     * @return
     */
    public FinancialTransaction updateFinancialTransactionState(TransactionStateCode pTransactionStateCd) {
        TransactionState curTxnState = getCurrentTransactionState();

        // do the logic only if the new status code is different. this is to prevent execution of the
        // business logic if the financialtransaction was already updated with a new status.
        if ((curTxnState == null) || !pTransactionStateCd.equals(curTxnState.getTransactionStateCd())) {
            TransactionState newTxnState = Application.findById(TransactionState.class, pTransactionStateCd);

            if (isTaxPaymentTransaction()) {
                addTaxPaymentTransactionState(newTxnState);
            } else {
                addTransactionState(newTxnState);
            }
        }

        return Application.save(this);
    }

    /**
     * Updates a FinancialTransaction along with a Transaction State Object and the
     * corresponding Ledger Entries
     *
     * @param pTransactionStateCd
     * @return
     */

    public FinancialTransaction updateTaxPaymentFinancialTransactionState(TransactionStateCode pTransactionStateCd) {
        TransactionState currentTransactionSate = getCurrentTransactionState();

        // do the logic only if the new status code is different. this is to prevent execution of the
        // business logic if the financialtransaction was already updated with a new status.
        if (currentTransactionSate == null || !pTransactionStateCd.equals(currentTransactionSate.getTransactionStateCd())) {
            TransactionState transactionSate = Application.findById(TransactionState.class, pTransactionStateCd);
            addTaxPaymentTransactionState(transactionSate);
        }

        return Application.save(this);
    }

    public static DomainEntitySet<FinancialTransaction> getFinancialTransactions(PayrollRun pPayrollRun, TransactionTypeCode pTransactionTypeCode,
                                                                                 TransactionStateCode pTransactionStateCode) {
        TransactionType transactionType = Application.findById(TransactionType.class, pTransactionTypeCode);
        TransactionState transactionState = Application.findById(TransactionState.class, pTransactionStateCode);
        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.Company().equalTo(pPayrollRun.getCompany())
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                                .And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState)))))
                        .OrderBy(FinancialTransaction.FinancialTransactionAmount(), FinancialTransaction.Law());

        return Application.find(FinancialTransaction.class, query);
    }

    public SpcfMoney getPrefundingAchTransactionBalance() {
        DomainEntitySet<FinancialTransaction> nonAchTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(OriginalTransaction().Id().equalTo(getId())
                .And(CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed))));
        SpcfDecimal amountDue = getFinancialTransactionAmount();
        for (FinancialTransaction nonAchTransaction : nonAchTransactions) {
            amountDue = amountDue.subtract(nonAchTransaction.getFinancialTransactionAmount());
        }
        return (SpcfMoney) amountDue;
    }


    /**
     * Gets the Company Bank Account based on the FinancialTransaction
     *
     * @return CompanyBankAccount
     */
    public CompanyBankAccount getCompanyBankAccountIncludingExpired() {
        BankAccount bankAccount = null;
        CompanyBankAccount companyBankAccount = null;

        if (getCreditBankAccountType() != null && getCreditBankAccountType().equals(BankAccountOwnerType.Company)) {
            bankAccount = getCreditBankAccount();
        } else if (getDebitBankAccountType() != null && getDebitBankAccountType().equals(BankAccountOwnerType.Company)) {
            bankAccount = getDebitBankAccount();
        }

        if (bankAccount != null) {
            companyBankAccount = CompanyBankAccount.findCompanyBankAccountIncludingExpired(
                    getCompany(), bankAccount);
        }

        return companyBankAccount;
    }

    /**
     * Method to update the status of the transaction return as RSLVD(Resolved)
     */
    public void resolveTransactionReturns() {
        // To avoid re-updating the transaction return status codes for previous returned
        // financial transactions, we only get the unresolved transaction returns
        TransactionReturn txReturn = TransactionReturn.findFirstUnresolvedTransactionReturn(this);
        if (txReturn != null) {
            txReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
        }
    }

    public void unResolveTransactionReturns() {
        // To avoid re-updating the transaction return status codes for previous returned
        // financial transactions, we only get the unresolved transaction returns
        TransactionReturn txReturn = TransactionReturn.findFirstResolvedTransactionReturn(this);
        if (txReturn != null) {
            txReturn.updateTransactionReturnStatus(TransactionReturnStatusCode.Open);
        }
    }

    /**
     * Decides whether a given FT should be excluded from a TransactionResponse.
     *
     * @return true means it should be EXCLUDED... false means it should be INCLUDED
     */
    public boolean transactionIsExcludedFromTransactionResponse() {
        // exclude any transaction related to a PaymentArrangementFee for a QBOE company
        String sku = getSku();
        if (sku != null) {
            if (getCompany().getSourceSystemCd() == SourceSystemCode.QBOE) {
                OfferingServiceChargeType chargeType = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
                if (chargeType == OfferingServiceChargeType.PaymentArrangementFee) {
                    return true; // is excluded
                }
            }
        }

        return false; // not excluded
    }

    /**
     * Introduced as performance optimization
     * previously FT <-> FTS but loading collection of FTS was very slow and impacted PrcoessACHTransactions batch job
     * changed relationship to FT <- FTS and required way to get current FTS even when FlushMode == MANUAL and FT had
     * transitioned states
     */
    private Deque<FinancialTransactionState> mCurrentTransactionStateQueue = new ArrayDeque<FinancialTransactionState>(3);

    /**
     * Method for finding the current financial transaction state for a given transaction
     *
     * @return Current financial transaction state
     */
    public FinancialTransactionState getCurrentFinancialTransactionState() {
        if (mCurrentTransactionStateQueue.size() == 0) {
            // Need to use partitioning key in FinancialTransactionState query to avoid iterating through all partitions
            SpcfCalendar ftCreatedDateMinus2Days = this.getCreatedDate();
            ftCreatedDateMinus2Days.addDays(-2);

            // case: this is first call to getCurrentFinancialTransactionState() and no transaction states have been added
            DomainEntitySet<FinancialTransactionState> results =
                    getFinancialTransactionStates().find(FinancialTransactionState.FinancialTransaction().equalTo(this)
                            .And(FinancialTransactionState.TransactionState().equalTo(getCurrentTransactionState()))
                            .And(FinancialTransactionState.TransactionStateEffectiveDate().greaterOrEqualThan(ftCreatedDateMinus2Days)));
            if (results.size() > 0) {
                mCurrentTransactionStateQueue.push(results.get(0));
            }
        }

        if (mCurrentTransactionStateQueue.size() > 0) {
            return mCurrentTransactionStateQueue.peek();
        } else {
            return null;
        }
    }

    public boolean isSalesTaxTransaction() {
        return (getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTax
                || getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
    }

    public static boolean isSystemGeneratedFeesOnly(DomainEntitySet<FinancialTransaction> finTxns) {

        if (finTxns.size() == 0) {
            return false;
        }

        ArrayList<OfferingServiceChargeType> SERVICE_CHARGE_TYPES = new ArrayList<OfferingServiceChargeType>();
        SERVICE_CHARGE_TYPES.add(OfferingServiceChargeType.W2BaseFee);
        SERVICE_CHARGE_TYPES.add(OfferingServiceChargeType.W2Fee);
        SERVICE_CHARGE_TYPES.add(OfferingServiceChargeType.MonthlyFee);

        // If any of the transactions are not a System Generated Fee, return false.
        // note - the transaction list may contain transactions that are not fees or no fees at all
        Boolean allFeeTransactionsSystemGenerated = null;
        for (FinancialTransaction currTxn : finTxns) {
            OfferingServiceCharge offeringServiceCharge = currTxn.getOfferingServiceCharge();
            if(offeringServiceCharge == null) {
                continue;
            }

            if (SERVICE_CHARGE_TYPES.contains(offeringServiceCharge.getOfferingServiceChargeGroup().getAppliesTo())) {
                allFeeTransactionsSystemGenerated = true;
            } else {
                return false;
            }
        }

        return allFeeTransactionsSystemGenerated != null;
    }

    public boolean isFeeTransaction() {
        return TransactionType.isFeeTransactionType(getTransactionType().getTransactionTypeCd());
    }

    public boolean isReversalClientRequested(Company pCompany) {
        String intuitInitiated = "true";

        DomainEntitySet<CompanyEventDetail> rqList;

        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
            rqList = CompanyEvent.findCompanyEventDetailsEagerLoadCompanyEventAndCompanyEventDetailSet(pCompany,
                    EventTypeCode.ReversalRequested,
                    EventDetailTypeCode.FinancialTransactionId,
                    getId().toString());
        } else {
            rqList = CompanyEvent.findCompanyEventDetails(pCompany,
                            EventTypeCode.ReversalRequested,
                            EventDetailTypeCode.FinancialTransactionId,
                            getId().toString());
        }


        if (!rqList.isEmpty()) {
            // If there are multiple ReversalRequested events for this financial transaction,
            // we're only interested in the latest one.
            CompanyEvent companyEvent = rqList.get(rqList.size() - 1).getCompanyEvent();
            intuitInitiated = companyEvent.getCompanyEventDetailValue(
                    EventDetailTypeCode.IntuitInitiated);
        }

        return !Boolean.parseBoolean(intuitInitiated);
    }

    /**
     * @return
     */
    public SpcfCalendar getRefundTransactionSettlementDate() {
        Company company = getCompany();

        //make the settlement date the later of the 2:
        // original ERFEE* txn settlement date + wait period + 1
        //  OR
        // next possible settlement date
        SpcfCalendar nextPossible = getSettlementDate(company.getOffloadGroup());
        SpcfCalendar origPlus = getTransactionCompletionDate();

        CalendarUtils.addBusinessDays(origPlus, 1);

        return origPlus.after(nextPossible) ? origPlus : nextPossible;
    }

    /**
     * Returns the FinancialTransactionState object for a specific state of a Financial Transaction
     *
     * @param pTransactionState Transaction State object used to get the FinancialTransactionState object
     * @return A FinancialTransactionState FinancialTransactionState to be found
     */
    public FinancialTransactionState getFinancialTransactionStateByTransactionState(
            TransactionState pTransactionState) {
        for (FinancialTransactionState financialTransactionState : getFinancialTransactionStates()) {
            if (financialTransactionState.getTransactionState().equals(pTransactionState)) {
                return financialTransactionState;
            }
        }
        return null;
    }

    /**
     * Obtain a list of financial transaction states from a given transaction
     *
     * @return financial transaction state history for a given transaction
     */
    public DomainEntitySet<FinancialTransactionState> getFinancialTransactionStates() {
        DomainEntitySet<FinancialTransactionState> states = new DomainEntitySet<FinancialTransactionState>();
        if(!this.isCreatedInCurrentSession()) {
            // Need to use partitioning key in FinancialTransactionState query to avoid iterating through all partitions
            SpcfCalendar ftCreatedDateMinus2Days = this.getCreatedDate().copy();
            ftCreatedDateMinus2Days.addDays(-2);

            states =
                    Application.find(FinancialTransactionState.class, FinancialTransactionState.FinancialTransaction().equalTo(this)
                            .And(FinancialTransactionState.TransactionStateEffectiveDate().greaterThan(ftCreatedDateMinus2Days)));
        }

        Iterator iterator = mCurrentTransactionStateQueue.descendingIterator();
        while (iterator.hasNext()) {
            FinancialTransactionState fts = (FinancialTransactionState) iterator.next();
            Criterion<FinancialTransactionState> existsQuery =
                    FinancialTransactionState.TransactionState().TransactionStateCd().equalTo(fts.getTransactionState().getTransactionStateCd());
            if (states.findEntity(existsQuery) == null) {
                states.add(fts);
            }
        }

        return states;
    }


    /**
     * Checks if an action event is valid for a financial transaction
     *
     * @param pActionEvent
     * @return
     */
    public boolean isValidAction(ActionEvent pActionEvent) {
        DomainEntitySet<FinancialTransactionAction> financialTransactionAction =
                Application.find(FinancialTransactionAction.class,
                        FinancialTransactionAction.ActionEvent().equalTo(pActionEvent)
                                .And(FinancialTransactionAction.TransactionType().equalTo(getTransactionType()))
                                .And(FinancialTransactionAction.TransactionState().equalTo(calculateCurrentTransactionState())));

        boolean validAction = (financialTransactionAction.size() > 0);

        // Special test for Void Transaction
        if (pActionEvent.getCode().equals(ActionEventCode.FinancialTransactionVoidTx)) {
            validAction = validAction && (!getSettlementTypeCd().equals(SettlementType.ACH));
        }

        // Special validation for Cancel transaction
        // To not allow cancellation of intuit initiated reversal
        if (pActionEvent.getCode().equals(ActionEventCode.FinancialTransactionCancel) &&
                getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployeeDdReversalDebit &&
                !isReversalClientRequested(getCompany())) {
            validAction = false;
        }

        // Special validation for voiding ErDdDebits when settlement type is !ach and payroll run status is !pending
        if (pActionEvent.getCode().equals(ActionEventCode.FinancialTransactionVoidTx) &&
                getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit &&
                getSettlementTypeCd() != SettlementType.ACH &&
                (getPayrollRun() != null && getPayrollRun().getPayrollRunStatus() != PayrollStatus.Pending)) {
            validAction = false;
        }

        if (pActionEvent.getCode().equals(ActionEventCode.RefundRebillFee)) {
            if (!getCompany().isCompanyOnService(ServiceCode.DirectDeposit)) {
                validAction = false;
            }
        }

        if (pActionEvent.getCode().equals(ActionEventCode.ReissuePayrollTaxPayment)) {
            if (getPayrollRun() == null) {
                return false;
            }

            //make sure there is something to reissue
            if (getPayrollRun()
                    .getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyTaxDebit},
                            new TransactionStateCode[]{TransactionStateCode.Voided})
                    .size() == 0) {
                validAction = false;
            }
            //make sure this one hasn't already been reissued
            if (getPayrollRun()
                    .getFinancialTransactions(TransactionTypeCode.ReissueTaxLiabilityTransfer, TransactionTypeCode.ReissueAgencyTaxDebitOffset)
                    .find(FinancialTransaction.OriginalTransaction().equalTo(this))
                    .size() > 0) {
                validAction = false;
            }
        }
        
        if(validAction && pActionEvent.getCode() == ActionEventCode.RefundDebit) {
            TransactionTypeCode transactionTypeCode = null;
            if(getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerPenaltiesRefundCredit) {
                transactionTypeCode = TransactionTypeCode.EmployerPenaltiesRefundDebit;
            } else if (getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerInterestRefundCredit) {
                transactionTypeCode = TransactionTypeCode.EmployerInterestRefundDebit;
            }

            validAction = FinancialTransaction.findFinancialTransactions(getCompany(), this, transactionTypeCode)
                                    .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)).isEmpty();
        }

        return validAction;
    }

    /**
     * Gets a list of valid action events for this financial transaction
     *
     * @return a collection of valid action events for this financial transaction
     */
    public Collection<ActionEvent> getActionCollection() {

        Collection<ActionEvent> validActions = new ArrayList<ActionEvent>();
        DomainEntitySet<ActionEvent> actionEvents = Application.find(ActionEvent.class, ActionEvent.Type().equalTo(ActionType.FinancialTransaction));
        for (ActionEvent actionEvent : actionEvents) {
            if (isValidAction(actionEvent)) {
                validActions.add(actionEvent);
            }
        }
        return validActions;
    }

    /**
     * Gets the Non Intuit Bank Account associated with the FinancialTransaction
     *
     * @return BankAccount
     */
    public BankAccount getNonIntuitBankAccount() {

        if (getDebitBankAccountType().equals(BankAccountOwnerType.Intuit)) {
            return getCreditBankAccount();
        }

        return getDebitBankAccount();
    }

    /**
     * Gets the Non Intuit Bank Account owner typr associated with the FinancialTransaction
     *
     * @return BankAccountOwnerType
     */
    public BankAccountOwnerType getNonIntuitBankAccountType() {

        if (BankAccountOwnerType.Intuit.equals(getDebitBankAccountType())) {
            return getCreditBankAccountType();
        }

        return getDebitBankAccountType();
    }

    public boolean isReversalClientRequested() {
        return isReversalClientRequested(getCompany());
    }


    /**
     * Given a financial transaction with a particular settlement date, find the initiation date for that transaction
     * For Employee DD Credits, the initiation date is always two days prior to the settlement date
     * For all other transaction types, the initiation date is always one day prior to the settlement date
     * When we add payments, this logic will need to change.
     *
     * @return Initiation date for given financial transaction
     */
    SpcfCalendar getInitiationDate() {
        TransactionTypeCode pTransactionTypeCode = getTransactionType().getTransactionTypeCd();
        SpcfCalendar pSettlementDate = getSettlementDate();
        if(!FundingModel.Codes.ONE_DAY.equals(getCompany().getFundingModel().getFundingModelCd())) {
            return getInitiationDate(pSettlementDate, pTransactionTypeCode);
        }
        SpcfCalendar initDate = pSettlementDate.toLocal().copy();
        CalendarUtils.addBusinessDays(initDate, -1);
        return initDate;
    }

    public static SpcfCalendar getInitiationDate(SpcfCalendar pSettlementDate, TransactionTypeCode pTransactionTypeCode) {
        SpcfCalendar initDate = pSettlementDate.toLocal().copy();
        if (TransactionTypeCode.EmployeeDdCredit == pTransactionTypeCode) {
            CalendarUtils.addBusinessDays(initDate, -2);
        } else {
            CalendarUtils.addBusinessDays(initDate, -1);
        }
        return initDate;
    }

    public static ScrollableResults findBPTransactionsForOffloadedBatch(OffloadBatch pOffloadBatch, int pMaxResults) {
        String[] paramNames = new String[1];
        paramNames[0] = "pOffloadBatch";

        Object[] paramValues = new Object[1];
        paramValues[0] = pOffloadBatch;

        Integer firstResult = (pMaxResults == -1) ? -1 : 0;
        return Application.scrollableResultsByNamedQuery("findBillPaymentFinTxnsForOffloadBatch", paramNames, paramValues, firstResult, pMaxResults);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FinancialTransaction")
                .append("  ID: ").append(getId())
                .append("  Type: ").append(getTransactionType() != null ? getTransactionType().getTransactionTypeCd().name() : "")
                .append("  State: ").append(getCurrentTransactionState() != null ? getCurrentTransactionState().getTransactionStateCd().name() : "")
                .append("  PR: ").append(getPayrollRun() != null ? getPayrollRun().getSourcePayRunId() : "")
                .append("  Amt: ").append(getFinancialTransactionAmount());
        return builder.toString();
    }


    public boolean contributesToPayment() {
        return getLaw() != null
                && !calculateCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)
                && !calculateCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Voided);
    }

    /**
     * Additional validation mostly around cancelled companies
     * 1. only agents can create FTs on cancelled companies
     * 2. only agents with a specific operation can create FTs on cancelled companies
     * 3. customer must have a bank account for ACH transactions (this should be checked before getting here, but
     * this is an additional check in case others were adding null instead)
     * This is especially useful because we used to cancel bank accounts when cancelling companies.
     * precondition: company and bank account information already set
     */
    public void validateCanCreateFinancialTransaction() {
        if (!getCompany().passesAdditionalCancelTermValidation(false, true, true)) {
            throw new RuntimeException("Financial Transaction cannot be added to cancelled/termed company FT:" + getId());
        }
        if (getSettlementTypeCd().equals(SettlementType.ACH)) {
            if (getCreditBankAccount() == null || getCreditBankAccountType() == null) {
                if (getCreditBankAccountType() != BankAccountOwnerType.TaxAgency) { //todo remove this line when WH added
                    throw new RuntimeException("Credit bank account information must be specified for ACH Financial Transactions FT:" + getId());
                }
            } else if (getDebitBankAccount() == null || getDebitBankAccountType() == null) {
                throw new RuntimeException("Debit bank account information must be specified for ACH Financial Transactions FT:" + getId());
            }
        }
    }

    public boolean isEFTPS_ACHPaymentTransaction() {
        switch (getTransactionType().getTransactionTypeCd()) {
            case AgencyTaxCredit:
            case AgencyTaxDebit:
            case AgencyTaxOverpaymentApplied:
                return true;

            default:
                return false;
        }
    }

    public void updateSettlementType(PaymentMethod pPaymentMethod) {
        updateSettlementType(FinancialTransaction.getSettlementType(pPaymentMethod));
    }

    public void updateSettlementType(SettlementType pSettlementType) {
        setSettlementTypeCd(pSettlementType);
        
        if(getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxCredit) {
            // update the associated bank accounts
            BankAccount debitBankAccount = null;
            BankAccount creditBankAccount = null;
            switch (pSettlementType) {
                case CheckType:
                    debitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_CHECK).getBankAccount();
                    break;
                case ACH:
                    debitBankAccount = IntuitBankAccount.findIntuitBankAccount(getTransactionType().getTransactionTypeCd(), CreditDebitCode.Debit).getBankAccount();
                    PaymentTemplateBankAccount pmtTemplateBankAccount = PaymentTemplateBankAccount.findActiveBankAccount(getLaw().getPaymentTemplate());
                    if ( pmtTemplateBankAccount != null ) {
                        creditBankAccount = pmtTemplateBankAccount.getBankAccount();
                    }
                    break;
                case EFTPS:
                case EDI:
                    debitBankAccount = IntuitBankAccount.findIntuitBankAccount(getTransactionType().getTransactionTypeCd(), CreditDebitCode.Debit).getBankAccount();
                    break;
            }

            setDebitBankAccount(debitBankAccount);
            setCreditBankAccount(creditBankAccount);
        }
    }

    public static SettlementType getDefaultTaxSettlementType(Company pCompany, PaymentTemplate pPaymentTemplate) {
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, pPaymentTemplate);
        PaymentMethod paymentMethod = companyAgencyPaymentTemplate.getCurrentPaymentMethod();
        if (paymentMethod == null) {
            return SettlementType.Other;
        }

        return getSettlementType(paymentMethod);
    }

    public static SettlementType getSettlementType(PaymentMethod pPaymentMethod) {
        if (pPaymentMethod == null) {
            return SettlementType.Other;
        }
        switch (pPaymentMethod) {
            case EFTPS:
                return SettlementType.EFTPS;
            case EFTPSDirectDebit:
                return SettlementType.EFTPSDirectDebit;
            case ACHCredit:
            case ACHDebit:
            case ACHDirectDeposit:
                return SettlementType.ACH;
            case CheckPayment:
            case SuperCheck:
                return SettlementType.CheckType;
            case EDI:
                return SettlementType.EDI;
        }
        return SettlementType.Other;
    }

    public PaymentMethod findPaymentMethod() {
        SettlementType settlementType = getSettlementTypeCd();
        if(settlementType == SettlementType.ApplyForward) {
            settlementType = getDefaultTaxSettlementType(getCompany(), getLaw().getPaymentTemplate());
        }
        switch (settlementType) {
            case ACH:
                if (isTaxPaymentTransaction()) {
                    CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate =
                            CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompany(), getLaw().getPaymentTemplate());
                    return companyAgencyPaymentTemplate.getCurrentPaymentMethod();
                } else {
                    return PaymentMethod.ACHDirectDeposit;
                }
            case CheckType:
                if (isTaxPaymentTransaction()) {
                    CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate =
                            CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompany(), getLaw().getPaymentTemplate());
                    return companyAgencyPaymentTemplate.getCurrentPaymentMethod();
                }
                return PaymentMethod.CheckPayment;
            case EFTPS:
                return PaymentMethod.EFTPS;
            case EFTPSDirectDebit:
                return PaymentMethod.EFTPSDirectDebit;
            case EDI:
                return PaymentMethod.EDI;
        }

        return null;
    }

    public boolean isPayrollSkuType() {
        SkuType skuType = null;
        OfferingServiceCharge charge = getOfferingServiceCharge();

        if (charge != null) {
            skuType = charge.getSkuType();
        }

        return SkuType.Payroll.equals(skuType);
    }

    public OfferingServiceCharge getOfferingServiceCharge() {
        OfferingServiceCharge charge = null;
        BillingDetail detail = getBillingDetail();

        if (detail != null) {
            OfferingServiceChargePrice price = detail.getOfferingServiceChargePrice();

            if (price != null) {
                charge = price.getOfferingServiceCharge();
            } else {
                charge = OfferingServiceChargeGroup.findFirstOfferingServiceCharge(getCompany(), detail.getOfferingServiceChargeType());

                if ((charge == null) && (getSku() != null)) {
                    charge = OfferingServiceCharge.findBySKU(getSku());
                }
            }
        }

        return charge;
    }

    public boolean isW2FeeChargeType() {
        OfferingServiceChargeType chargeType = null;
        OfferingServiceCharge charge = getOfferingServiceCharge();

        if (charge != null) {
            OfferingServiceChargeGroup group = charge.getOfferingServiceChargeGroup();
            chargeType = group.getAppliesTo();
        }

        return OfferingServiceChargeType.W2Fee.equals(chargeType) || OfferingServiceChargeType.W2BaseFee.equals(chargeType);
    }

    public void updateOnHold(boolean newOnHold)
    {
        if (newOnHold != getOnHold()) {
            setOnHold(newOnHold);
            if (getMoneyMovementTransaction() != null) {
                getMoneyMovementTransaction().recalculatePaymentStatus();
            }
        }
    }

    @Override
    public void setSettlementDate(SpcfCalendar settlementDate) {
        if (settlementDate != null && !CalendarUtils.isTimeClear(settlementDate)) {
            throw new RuntimeException("SettlementDate being set to wrong time: " + settlementDate.toString() + " FT:" + getId());
        }

        if(isQBDTSpecific() && !ObjectUtils.equals(getSettlementDate(), settlementDate)) {
            onUpdate();
        }

        super.setSettlementDate(settlementDate);
    }

    @Override
    public void setPayrollRun(PayrollRun pPayrollRun) {
        if(pPayrollRun != null && getCompany() != null) {
            PayrollRun.getPayrollsInMemory(getCompany()).add(pPayrollRun);

            updateProcessObservers();
        }
        super.setPayrollRun(pPayrollRun);
    }

    private void updateProcessObservers() {
        if(getCompany() != null && getCompany().getSourceSystemCd() == SourceSystemCode.QBDT) {
            IProcessObserver processObserver = Application.getProcessObserver(QBDT_PROCESS_OBSERVER);
            if(processObserver != null) {
                processObserver.addItem(this);
            }
        }
    }

    // ----- QBDT Token overrides -----
    private boolean isQBDTSpecific() {
        return getQbdtTransactionInfo() != null;
    }

    @Override
    public void setFinancialTransactionAmount(SpcfMoney pFinancialTransactionAmount) {
        if(isQBDTSpecific() && !ObjectUtils.equals(getFinancialTransactionAmount(), pFinancialTransactionAmount)) {
            onUpdate();
        }
        super.setFinancialTransactionAmount(pFinancialTransactionAmount);
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if(isQBDTSpecific() && !ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(isQBDTSpecific() && !ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    @Override
    public void setMoneyMovementTransaction(MoneyMovementTransaction pMoneyMovementTransaction) {
        if(isQBDTSpecific() && !ObjectUtils.equals(getMoneyMovementTransaction(), pMoneyMovementTransaction)) {
            onUpdate();
        }

        updateProcessObservers();

        super.setMoneyMovementTransaction(pMoneyMovementTransaction);
    }

    public void onUpdate() {
        if(getMoneyMovementTransaction() != null) {
            getMoneyMovementTransaction().onUpdate();
        }
    }

    /**
     * Update settlement date for ACH Financial Transactions.
     * @param newSettlementDate
     */
    public void updateACHSettlementDate(SpcfCalendar newSettlementDate) {
        LOGGER.info(String.format("FTSeq=%s,OldSettlementDate=%s,newSettlementDate=%s", this.getId(), this.getSettlementDate(), newSettlementDate));
        this.setSettlementDate(newSettlementDate);
        for (EntryDetailRecord edr : this.getMoneyMovementTransaction().getEntryDetailRecordCollection()) {
            LOGGER.info(String.format("EDRSeq=%s,OldSettlementDate=%s,newSettlementDate=%s", edr.getId(), edr.getSettlementDate(), newSettlementDate));
            edr.setSettlementDate(newSettlementDate);
        }
    }

    public static boolean isRefundType(FinancialTransaction financialTransaction) {
        return (financialTransaction.getRefundType() == RefundType.Refund);
    }
}
