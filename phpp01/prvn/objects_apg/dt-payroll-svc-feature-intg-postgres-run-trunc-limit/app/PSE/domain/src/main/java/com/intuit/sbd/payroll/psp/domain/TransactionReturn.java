package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.time.StopWatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Hand-written business logic
 */
public class TransactionReturn extends BaseTransactionReturn {

    static final SpcfLogger logger = SpcfLogManager.getLogger(TransactionReturn.class);


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<TransactionReturn> findTransactionReturnsExcludedStatus(Company pSourceCompany, TransactionReturnStatusCode pExcludedStatus) {
        String[] paramNames = new String[2];
        paramNames[0] = "sourceCompany";
        paramNames[1] = "excludedStatus";

        Object[] paramValues = new Object[2];
        paramValues[0] = pSourceCompany;
        paramValues[1] = pExcludedStatus;

        DomainEntitySet<TransactionReturn> retList =
                Application.findByNamedQuery("findTxnRetByCompExclStatus", paramNames, paramValues);

        return retList;
    }

    public static DomainEntitySet<TransactionReturn> findTransactionReturnsExcludedStatus(Company pSourceCompany,
                                                                            BankAccount pBankAccount, TransactionReturnStatusCode pExcludedStatus) {
        String[] paramNames = new String[3];
        paramNames[0] = "sourceCompany";
        paramNames[1] = "bankAccount";
        paramNames[2] = "excludedStatus";

        Object[] paramValues = new Object[3];
        paramValues[0] = pSourceCompany;
        paramValues[1] = pBankAccount;
        paramValues[2] = pExcludedStatus;

        DomainEntitySet<TransactionReturn> retList =
                Application.findByNamedQuery("findTxnRetByCompanyBankAccountExclStatus", paramNames, paramValues);

        return retList;
    }

    public static DomainEntitySet<TransactionReturn> findTransactionReturns(String pSourcePayrollRunId, Company pCompany) {
        String[] paramNames = new String[2];
        paramNames[0] = "sourcePayrollRunId";
        paramNames[1] = "company";

        Object[] paramValues = new Object[2];
        paramValues[0] = pSourcePayrollRunId;
        paramValues[1] = pCompany;

        DomainEntitySet<TransactionReturn> transactionReturnList =
                Application.findByNamedQuery("findTxnRetByPayrollRunId", paramNames, paramValues);


        return transactionReturnList;
    }

    public static TransactionReturn findFirstUnresolvedTransactionReturn(FinancialTransaction pFinTx) {
        return findFirstTransactionReturnExcludedStatus(pFinTx, TransactionReturnStatusCode.Resolved);
    }

    public static TransactionReturn findFirstResolvedTransactionReturn(FinancialTransaction pFinTx) {
        return findFirstTransactionReturnExcludedStatus(pFinTx, TransactionReturnStatusCode.Created);
    }

    private static TransactionReturn findFirstTransactionReturnExcludedStatus(FinancialTransaction pFinTx, TransactionReturnStatusCode pReturnStatCode) {

        DomainEntitySet<TransactionReturn> txReturns = findTransactionReturnsByExcludedStatus(pFinTx, pReturnStatCode);

        if (txReturns.size() > 0) {
            return txReturns.get(0);
        } else {
            return null;
        }
    }

    public static DomainEntitySet<TransactionReturn> findTransactionReturns(FinancialTransaction pFinancialTransaction) {
        return Application.find(TransactionReturn.class, TransactionReturn.MoneyMovementTransaction().equalTo(pFinancialTransaction.getMoneyMovementTransaction()));
    }

    public static DomainEntitySet<TransactionReturn> findTransactionReturns(Company pSourceCompany) {
        return Application.find(TransactionReturn.class, TransactionReturn.Company().equalTo(pSourceCompany));
    }

    public static int findTransactionReturnCount(Company pSourceCompany) {
        return Application.executeScalarAggQuery(TransactionReturn.class, new Query<TransactionReturn>()
                .Select(TransactionReturn.Id().Count())
                .Where(TransactionReturn.Company().equalTo(pSourceCompany)))
                          .intValue();
    }

    public static int getTransactionReturnCollection(SourceSystemCode pSourceSystemCd,
                                              String pSourceCompanyId, String fein,
                                              Collection pFinancialTransactionIdCollection,
                                              Collection pReturnStatusCodeCollection,
                                              SpcfCalendar pReturnDateFrom,
                                              SpcfCalendar pReturnDateTo, int pFirstResult,
                                              int pMaxResults,
                                              DomainEntitySet<TransactionReturn> pTransactionReturnList) {
        int totalRecordCount = 0;
        DomainEntitySet<TransactionReturn> transactionReturnList;
        TransactionReturnStatusCode[] returnStatusCds = null;

        if (pReturnStatusCodeCollection != null && pReturnStatusCodeCollection.size() > 0) {
            returnStatusCds = new TransactionReturnStatusCode[pReturnStatusCodeCollection.size()];
            Iterator iterator = pReturnStatusCodeCollection.iterator();

            int index = 0;
            while (iterator.hasNext()) {
                TransactionReturnStatusCode itrValue = (TransactionReturnStatusCode) iterator.next();
                returnStatusCds[index++] = itrValue;
            }
        }

        if (pFinancialTransactionIdCollection != null && pFinancialTransactionIdCollection.size() > 0) {

            Iterator iterator = pFinancialTransactionIdCollection.iterator();
            int index = 0;

            //while loop to get the FinancialTransaction Domain Object based on the Unique id and construct the array
            //with Financialtransaction Objects
            List<MoneyMovementTransaction> moneyMovementTxnObjs = new ArrayList<MoneyMovementTransaction>();
            FinancialTransaction finTx = null;
            while (iterator.hasNext()) {
                finTx = Application.findById(FinancialTransaction.class, (SpcfUniqueId) iterator.next());
                if (finTx != null) {
                    moneyMovementTxnObjs.add(finTx.getMoneyMovementTransaction());
                }
            }

            MoneyMovementTransaction[] mmTxArray = new MoneyMovementTransaction[moneyMovementTxnObjs.size()];
            for (int i = 0; i < moneyMovementTxnObjs.size(); i++) {
                mmTxArray[i] = moneyMovementTxnObjs.get(i);
            }

            if (mmTxArray.length > 0) {
                Criterion<TransactionReturn> where = TransactionReturn.MoneyMovementTransaction().in(mmTxArray);

                if (returnStatusCds != null && returnStatusCds.length > 0) {
                    where = where.And(TransactionReturn.ReturnStatusCd().in(returnStatusCds));
                }

                Expression<TransactionReturn> query =
                        new Query<TransactionReturn>()
                               .Where(where)
                               .OrderBy(TransactionReturn.CreatedDate());

                transactionReturnList = Application.find(TransactionReturn.class, query);

                //If-else condition to filter the retrieved transactionreturn list based on the first result and max results
                //for pagination
                if (pMaxResults > 0) {
                    pTransactionReturnList.addAll(filterTransactionReturnList(pFirstResult, pMaxResults,
                            transactionReturnList));
                } else {
                    pTransactionReturnList.addAll(transactionReturnList);
                }

                totalRecordCount = transactionReturnList.size();
            }
        } else {

            String[] paramNames = new String[9];

            paramNames[0] = "sourceSystemCd";
            paramNames[1] = "sourceCompanyId";
            paramNames[2] = "feinEncList";
            paramNames[3] = "returnStatusCd1";
            paramNames[4] = "returnStatusCd2";
            paramNames[5] = "returnStatusCd3";
            paramNames[6] = "returnStatusCd4";
            paramNames[7] = "fromDate";
            paramNames[8] = "toDate";

            Object[] paramValues = new Object[9];

            int index = 0;

            if (pSourceSystemCd != null) {
                paramValues[index++] = pSourceSystemCd;
            } else {
                paramValues[index++] = null;
            }

            if (pSourceCompanyId != null) {
                paramValues[index++] = pSourceCompanyId;
            } else {
                paramValues[index++] = null;
            }

            List<String> feinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, fein);
            paramValues[index++] = feinEncList;


            if (returnStatusCds != null) {
                for (int i = 0; i < 4; i++) {
                    if (i < returnStatusCds.length) {
                        paramValues[index++] = returnStatusCds[i];
                    } else {
                        paramValues[index++] = null;
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    paramValues[index++] = null;
                }
            }

            if (pReturnDateFrom != null) {
                paramValues[index++] = pReturnDateFrom;
            } else {
                paramValues[index++] = null;
            }

            if (pReturnDateTo != null) {
                paramValues[index] = pReturnDateTo;
            } else {
                paramValues[index] = null;
            }

            String namedQueryName = "findTxnReturnCollectionENC";

            if (pMaxResults > 0) {
                transactionReturnList = Application.findByNamedQuery(namedQueryName, paramNames, paramValues,
                        pFirstResult - 1, pMaxResults);
            } else {
                transactionReturnList = Application.findByNamedQuery(namedQueryName, paramNames, paramValues);
            }

            ArrayList returnList = Application.executeNamedQuery("findTxnReturnCollectionTotalCountENC", paramNames, paramValues);

            if (!returnList.isEmpty()) {
                Long totalRecordCountLong = (Long) returnList.iterator().next();
                totalRecordCount = totalRecordCountLong.intValue();
            }
            System.out.println("Record Count " + returnList.iterator().next());

            pTransactionReturnList.addAll(transactionReturnList);
        }
        return totalRecordCount;
    }

    private static DomainEntitySet<TransactionReturn> filterTransactionReturnList(int pFirstResult,
                                                                    int pMaxResults,
                                                                    DomainEntitySet<TransactionReturn> pTransactionReturnList) {
        DomainEntitySet<TransactionReturn> filteredList = new DomainEntitySet<TransactionReturn>();

        if (pTransactionReturnList.size() <= pMaxResults) {
            pMaxResults = pTransactionReturnList.size();
        }

        Iterator<TransactionReturn> it = pTransactionReturnList.iterator();

        for (int index = 0; index < pMaxResults; index++) {
            TransactionReturn transactionReturn = it.next();
            if (index >= pFirstResult - 1) {
                filteredList.add(transactionReturn);
            }
        }

        return filteredList;
    }


    public static DomainEntitySet<FinancialTransaction> findFinancialTransaction(TransactionReturn pTransactionReturn) {
        return Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(pTransactionReturn.getMoneyMovementTransaction()));
    }

    public static DomainEntitySet<FinancialTransaction> findFinancialTransactionExcludingTransactionTypeCodes(TransactionReturn pTransactionReturn, TransactionTypeCode... pTransactionTypeCodes) {
        return Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(pTransactionReturn.getMoneyMovementTransaction())
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().notIn(pTransactionTypeCodes)));
    }

    public static DomainEntitySet<FinancialTransaction> findNonOverPaymentFinancialTransacttion(TransactionReturn pTransactionReturn) {
        return Application.find(FinancialTransaction.class, FinancialTransaction.MoneyMovementTransaction().equalTo(pTransactionReturn.getMoneyMovementTransaction())
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.EmployerTaxCreditApplied))
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.AgencyTaxOverpaymentApplied))
                .And(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.EmployerTaxOverpaymentApplied)));
    }

    public static FinancialTransaction findFirstFinancialTransaction(TransactionReturn pTransactionReturn) {
        DomainEntitySet<FinancialTransaction> finTxns = findFinancialTransaction(pTransactionReturn);
        if (finTxns.size() > 0) {
            return finTxns.get(0);
        }

        return null;
    }

    public static DomainEntitySet<TransactionReturn> findTxnRetsForReturnType(
            SpcfUniqueId pTransactionReturnBatchId,
            String pReturnTypeCode,
            TransactionReturnStatusCode pReturnStatusCode) {

        String[] paramNames = new String[3];
        paramNames[0] = "txnRetBatchId";
        paramNames[1] = "returnCdWithPercentSigns";
        paramNames[2] = "txnRetStatusCd";

        Object[] paramValues = new Object[3];
        paramValues[0] = pTransactionReturnBatchId;
        paramValues[1] = pReturnTypeCode + "%";
        paramValues[2] = pReturnStatusCode;

        return Application.findByNamedQuery("findTxnReturnByReturnBatchAndReturnType", paramNames, paramValues);
    }

    public static DomainEntitySet<TransactionReturn> findTxnRetsForReturnType(Company pCompany,
                                                                String pReturnTypeCode, TransactionReturnStatusCode pReturnStatus) {
        String[] paramNames = new String[3];
        paramNames[0] = "sourceCompany";
        paramNames[1] = "returnStatus";
        paramNames[2] = "returnCdWithPercentSigns";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pReturnStatus;
        paramValues[2] = pReturnTypeCode + "%";

        return Application.findByNamedQuery("findTxnRetByCompanyAndReturnType", paramNames, paramValues);
    }

    public static DomainEntitySet<TransactionReturn> findUnresolvedTxnReturnsExcludedTxnTypes(Company pCompany,
                                                                                TransactionCategory pTransactionCategory) {
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "excludedStatus";
        paramNames[2] = "txnCategory";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = TransactionReturnStatusCode.Resolved;
        paramValues[2] = pTransactionCategory;

        return Application.findByNamedQuery("findUnresolvedTxnReturnsExcludedTxnTypes", paramNames, paramValues);
    }


    /**
     * Find information about TransactionReturns matching the given criteria, returning one row for each
     * FinancialTransaction related to the matching TransactinReturns.  The results are sorted by TransactionReturn ID,
     * which groups FTs related to the same return.
     *
     * All search criteria are optional.
     *
     * The fields, their positions and their types in the returned rows are as follows:
     * [0] TransactionReturn ID (SpcfUniqueId)
     * [1] PayrollRun status (PayrollStatus)
     * [2] FT transaction type code (TransactionTypeCode)
     * [3] company legal name (String) only when FT is a DD credit
     * [4] employee first name (String) only when FT is a DD credit
     * [5] employee middle name (String) only when FT is a DD credit
     * [6] employee last name (String) only when FT is a DD credit
     * [7] company EIN
     * [8] non-Intuit BA account number (String)
     * [9] non-Intuit BA routing number (String)
     * [10] TransactionReturn return date (SpcfCalendar)
     * [11] PayrollRun paycheck date (SpcfCalendar)
     * [12] ACH amount returned (SpcfMoney)
     * [13] bank return code (String)
     * [14] PayrollRun source payroll run id (String)
     * [15] TransactionReturn.ReturnStatusCd
     * [16] Company.SourceSystemCd
     * [17] Company.SourceCompanyId
     * [18] FinancialTransaction.id (SpcfUniqueId)
     * [19] DebitBankAccountType
     * [20] Payee name
     *
     * @param pReturnStatusCd
     * @param pStartDate
     * @param pEndDate
     * @param pExclude5DayFunding
     * @param pReturnCd
     * @param pEIN
     * @param pAmount
     * @param pOnHoldReasonCd
     * @param pTransCat         One of TransactionCategory.Employer or .Employee, or null if don't care
     * @param pTransGroup       One of TransactionTypeGroupCode.Debit (to match debits and redebits) or .Credit
     *                          (to match credits and recredits), or null if don't care
     * @param pExcludeOnHoldReason  When pOnHoldReason is non-null, this param controls whether to select rows that
     *                              match (include) that reason (when false), or rows that don't match (exclude) that
     *                              reason (when true).
     * @param pOrderBy - order of the resulsts
     * @param pOrderDesc - order descending?
     * @param pFirstResult - the result row to select
     * @param pMaxResults - to total number of resulst to return
     * @return
     */
    public static ArrayList<Object[]> findTransactionReturnsBySAPCriteria(TransactionReturnStatusCode pReturnStatusCd,
                                                                   SpcfCalendar pStartDate, SpcfCalendar pEndDate,
                                                                   boolean pExclude5DayFunding, String pReturnCd,
                                                                   String pEIN, SpcfMoney pAmount,
                                                                   ServiceSubStatusCode pOnHoldReasonCd,
                                                                   TransactionCategory pTransCat,
                                                                   TransactionTypeGroupCode pTransGroup,
                                                                   boolean pExcludeOnHoldReason,
                                                                   String pOrderBy,
                                                                   boolean pOrderDesc,
                                                                   int pFirstResult,
                                                                   int pMaxResults) {

        ArrayList<Object[]> results = new ArrayList<Object[]>();

        List resultList = findTransactionReturnsBySAPCriteriaQuery(pReturnStatusCd,
                pStartDate, pEndDate,
                pExclude5DayFunding, pReturnCd,
                pEIN, pAmount,
                pOnHoldReasonCd,
                pTransCat,
                pTransGroup,
                pExcludeOnHoldReason,
                pOrderBy,
                pOrderDesc,
                pFirstResult,
                pMaxResults);

        for (Iterator iterator = resultList.iterator(); iterator.hasNext();) {
            results.add( (Object[])iterator.next() );
        }

        return results;
    }

    /**
     * Find the number of bank returns for the given parameters
     * @param pReturnStatusCd
     * @param pStartDate
     * @param pEndDate
     * @param pExclude5DayFunding
     * @param pReturnCd
     * @param pEIN
     * @param pAmount
     * @param pOnHoldReasonCd
     * @param pTransCat
     * @param pTransGroup
     * @param pExcludeOnHoldReason
     * @return
     */
    public static int findTransactionReturnsBySAPCriteriaCount(TransactionReturnStatusCode pReturnStatusCd,
                                                        SpcfCalendar pStartDate, SpcfCalendar pEndDate,
                                                        boolean pExclude5DayFunding, String pReturnCd,
                                                        String pEIN, SpcfMoney pAmount,
                                                        ServiceSubStatusCode pOnHoldReasonCd,
                                                        TransactionCategory pTransCat,
                                                        TransactionTypeGroupCode pTransGroup,
                                                        boolean pExcludeOnHoldReason) {

        List resultList = findTransactionReturnsBySAPCriteriaQuery(pReturnStatusCd,
                pStartDate, pEndDate,
                pExclude5DayFunding, pReturnCd,
                pEIN, pAmount,
                pOnHoldReasonCd,
                pTransCat,
                pTransGroup,
                pExcludeOnHoldReason,
                null,
                false,
                -1,
                -1);

        return resultList.size();
    }

    /**
     * Find information about TransactionReturns matching the given criteria, returning one row for each
     * FinancialTransaction related to the matching TransactinReturns.  The results are sorted by TransactionReturn ID,
     * which groups FTs related to the same return.
     *
     * All search criteria are optional.
     *
     * The fields, their positions and their types in the returned rows are as follows:
     * [0] TransactionReturn ID (SpcfUniqueId)
     * [1] PayrollRun status (PayrollStatus)
     * [2] FT transaction type code (TransactionTypeCode)
     * [3] company legal name (String) only when FT is a DD credit
     * [4] employee first name (String) only when FT is a DD credit
     * [5] employee middle name (String) only when FT is a DD credit
     * [6] employee last name (String) only when FT is a DD credit
     * [7] company EIN
     * [8] non-Intuit BA account number (String)
     * [9] non-Intuit BA routing number (String)
     * [10] TransactionReturn return date (SpcfCalendar)
     * [11] PayrollRun paycheck date (SpcfCalendar)
     * [12] ACH amount returned (SpcfMoney)
     * [13] bank return code (String)
     * [14] PayrollRun source payroll run id (String)
     * [15] TransactionReturn.ReturnStatusCd
     * [16] Company.SourceSystemCd
     * [17] Company.SourceCompanyId
     * [18] FinancialTransaction.id (SpcfUniqueId)
     * [19] DebitBankAccountType
     * [20] Payee name
     *
     * @param pReturnStatusCd
     * @param pStartDate
     * @param pEndDate
     * @param pExclude5DayFunding
     * @param pReturnCd
     * @param pEIN
     * @param pAmount
     * @param pOnHoldReasonCd
     * @param pTransCat         One of TransactionCategory.Employer or .Employee, or null if don't care
     * @param pTransGroup       One of TransactionTypeGroupCode.Debit (to match debits and redebits) or .Credit
     *                          (to match credits and recredits), or null if don't care
     * @param pExcludeOnHoldReason  When pOnHoldReason is non-null, this param controls whether to select rows that
     *                              match (include) that reason (when false), or rows that don't match (exclude) that
     *                              reason (when true).
     * @param pOrderBy - order of the resulsts
     * @param pOrderDesc - order the results descending
     * @param pFirstResult - the result row to select
     * @param pMaxResults - to total number of resulst to return
     * @return List - bank return transactions
     */
    private static List findTransactionReturnsBySAPCriteriaQuery(TransactionReturnStatusCode pReturnStatusCd,
                                                          SpcfCalendar pStartDate, SpcfCalendar pEndDate,
                                                          boolean pExclude5DayFunding, String pReturnCd,
                                                          String pEIN, SpcfMoney pAmount,
                                                          ServiceSubStatusCode pOnHoldReasonCd,
                                                          TransactionCategory pTransCat,
                                                          TransactionTypeGroupCode pTransGroup,
                                                          boolean pExcludeOnHoldReason,
                                                          String pOrderBy,
                                                          boolean pOrderDesc,
                                                          int pFirstResult,
                                                          int pMaxResults){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        StringBuffer hqlSelect = new StringBuffer(" select" +
                "   distinct tr.id,pr.PayrollRunStatus,ft.TransactionType.TransactionTypeCd,mmt.Company.LegalName" +
                "   ,ee.FirstName,ee.MiddleName,ee.LastName,mmt.Company.FedTaxIdEnc,ba.AccountNumberEnc,ba.RoutingNumber" +
                "   ,tr.ReturnBatch.ReturnDate,pr.PaycheckDate,ft.FinancialTransactionAmount,tr.BankReturnCd" +
                "   ,pr.SourcePayRunId,tr.ReturnStatusCd,mmt.Company.SourceSystemCd,mmt.Company.SourceCompanyId,ft.Id" +
                "   ,ft.DebitBankAccountType, p.Name");

        StringBuffer hqlFrom = new StringBuffer(" from" +
                "   com.intuit.sbd.payroll.psp.domain.FinancialTransaction ft" +
                "   left join com.intuit.sbd.payroll.psp.domain.PaycheckSplit pchksplt ON ft.PaycheckSplit=pchksplt AND pchksplt.Company=ft.Company" +
                "   left join com.intuit.sbd.payroll.psp.domain.Paycheck pchk ON pchksplt.Paycheck=pchk AND pchksplt.Company=pchk.Company" +
                "   left join pchk.DDEmployee ee" +
                "   left join com.intuit.sbd.payroll.psp.domain.PayrollRun pr ON ft.PayrollRun=pr AND ft.Company=pr.Company" +
                "   left join ft.BillPaymentSplit.BillPayment.Payee p" +
                "   join com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt ON ft.MoneyMovementTransaction=mmt AND ft.Company=mmt.Company" +
                "   ,com.intuit.sbd.payroll.psp.domain.TransactionReturn tr" +
                "   ,com.intuit.sbd.payroll.psp.domain.BankAccount ba");
        StringBuffer hqlWhere = new StringBuffer(" where " +
                "   (tr.MoneyMovementTransaction=mmt)" +
                "   and ((ft.DebitBankAccountType!='Intuit' and ba=ft.DebitBankAccount) or (ft.CreditBankAccountType!='Intuit' and ba=ft.CreditBankAccount))");

        StringBuffer hqlOrderBy = new StringBuffer(" order by");

        if (pReturnStatusCd != null) {
            if (pReturnStatusCd == TransactionReturnStatusCode.Open) {
                hqlWhere.append("   and pr.PayrollRunStatus in ('DebitReturned', 'ReturnedTwice', 'PendingWire', 'ReversalsFinished')");
            }
            else if (pReturnStatusCd == TransactionReturnStatusCode.Resolved) {
                // equivalent (but not index friendly): pr.PayrollRunStatus not in ('DebitReturned', 'ReturnedTwice', 'PendingWire', 'ReversalsFinished'))
                hqlWhere.append("   and pr.PayrollRunStatus in ('Complete','Canceled','DebitReturnedCanceled','NSFCanceled','OffloadedAll','OffloadedDebit','Pending','WrittenOff','PendingReversals','PendingAutoRedebit','AutoRedebitOffloaded','PendingRedebit','RedebitOffloaded','ReversalsOffloaded')");
            }
        }
        // never show returns before they are processed (i.e. the TransactionReturn.ReturnStatusCd = 'Created'
        // would use the above in filters w/Open & Resolved if the existing use of PayrollRunStatus values didn't completely baffle me
        hqlWhere.append("   and tr.ReturnStatusCd != '" + TransactionReturnStatusCode.Created + "'");

        if (pStartDate != null)
            hqlWhere.append("   and tr.ReturnBatch.ReturnDate >= :startDate");

        if (pEndDate != null)
            hqlWhere.append("   and tr.ReturnBatch.ReturnDate <= :endDate");

        if (pExclude5DayFunding) {
            // equivalent (but not index friendly): pr.PayrollRunStatus not in ('NSFCanceled', 'DebitReturnedCanceled')
            hqlWhere.append("   and pr.PayrollRunStatus in ('Complete','Canceled','DebitReturned','OffloeadedAll','OffloadedDebit','Pending','WrittenOff','PendingReversals','PendingAutoRedebit','AutoRedebitOffloaded','PendingRedebit','RedebitOffloaded','PendingWire','ReversalsOffloaded','ReversalsFinished','ReturnedTwice')");
        }

        if (pReturnCd != null)
            hqlWhere.append("   and tr.BankReturnCd like :bankRetCode");

        if (pEIN != null){
            hqlWhere.append("   and (mmt.Company.FedTaxIdEnc in (:einEncList) or mmt.Company.SourceCompanyId=:Cid)");
        }

        if (pAmount != null)
            hqlWhere.append("   and (mmt.MoneyMovementTransactionAmount=:achReturnAmount)");

        if (pOnHoldReasonCd != null) {
            if (pExcludeOnHoldReason) {
                hqlFrom.append("   ,com.intuit.sbd.payroll.psp.domain.Company company");
                hqlWhere.append("   and mmt.Company=company");
                hqlWhere.append("   and (not exists (from company.OnHoldReasonSet as onHoldReasonMembers where onHoldReasonMembers.ExpirationDate is null and onHoldReasonMembers.OnHoldReasonCd=:onHoldReason))");
            }
            else {
                hqlFrom.append("   ,com.intuit.sbd.payroll.psp.domain.OnHoldReason ohr");
                hqlWhere.append("   and (ohr.Company=mmt.Company and ohr.OnHoldReasonCd=:onHoldReason and ohr.ExpirationDate is null)");
            }
        }

        if (pTransCat != null)
            hqlWhere.append("   and (ft.TransactionType.TransactionCategory = :ftCategory)");

        if (pTransGroup != null && pTransGroup == TransactionTypeGroupCode.Debit)
            hqlWhere.append("   and ft.TransactionType.TransactionTypeGroupCd in ('Debit', 'Redebit')");

        if (pTransGroup != null && pTransGroup == TransactionTypeGroupCode.Credit)
            hqlWhere.append("   and ft.TransactionType.TransactionTypeGroupCd in ('Credit', 'Recredit')");

        // order by
        if(pOrderBy == null){
            hqlOrderBy.append(" tr.ReturnBatch.ReturnDate, mmt.Company.SourceCompanyId");
        }
        else if(pOrderBy.equalsIgnoreCase("payrollStatus")) {
            hqlOrderBy.append(" pr.PayrollRunStatus");
        }
        else if(pOrderBy.equalsIgnoreCase("transactionType")) {
            hqlOrderBy.append(" ft.TransactionType.TransactionTypeCd");
        }
        else if(pOrderBy.equalsIgnoreCase("companyName")) {
            hqlOrderBy.append(" mmt.Company.LegalName");
        }
        else if(pOrderBy.equalsIgnoreCase("fein")) {
            hqlOrderBy.append(" mmt.Company.FedTaxIdEnc");
        }
        else if(pOrderBy.equalsIgnoreCase("bankAccountNumber")) {
            hqlOrderBy.append(" ba.AccountNumberEnc");
        }
        else if(pOrderBy.equalsIgnoreCase("returnDateTime")) {
            hqlOrderBy.append(" tr.ReturnBatch.ReturnDate");
        }
        else if(pOrderBy.equalsIgnoreCase("amount")) {
            hqlOrderBy.append(" ft.FinancialTransactionAmount");
        }
        else if(pOrderBy.equalsIgnoreCase("returnCd")) {
            hqlOrderBy.append(" tr.BankReturnCd");
        }

        if(pOrderDesc){
            hqlOrderBy.append(" desc");
        }


        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hqlSelect.toString()
                + hqlFrom.toString()
                + hqlWhere.toString()
                + hqlOrderBy.toString());


        if (pStartDate != null)
            hibernateQuery.setParameter("startDate", pStartDate);

        if (pEndDate != null)
            hibernateQuery.setParameter("endDate", pEndDate);

        if (pReturnCd != null)
            hibernateQuery.setParameter("bankRetCode", pReturnCd + "%");

        if (pEIN != null) {
            List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,pEIN);
            hibernateQuery.setParameterList("einEncList", fedTaxIdEncList);
            hibernateQuery.setParameter("Cid", pEIN);
        }

        if (pAmount != null)
            hibernateQuery.setParameter("achReturnAmount", pAmount);

        if (pOnHoldReasonCd != null) {
            hibernateQuery.setParameter("onHoldReason", pOnHoldReasonCd);
        }

        if (pTransCat != null)
            hibernateQuery.setParameter("ftCategory", pTransCat);

        // set result set
        if(pFirstResult != -1){
            hibernateQuery.setFirstResult(pFirstResult);
        }
        if(pMaxResults != -1){
            hibernateQuery.setMaxResults(pMaxResults);
        }

        List<Object[]> resultSet = hibernateQuery.list();
        for (Object[] row : resultSet) {
            row[7] = EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName, (String) row[7]);
            row[8] = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName, (String) row[8]);
        }
        return resultSet;
    }

    public static DomainEntitySet<TransactionReturnBatch> findTxnReturnBatch(SpcfCalendar pReturnDate) {
        String[] paramNames = new String[1];
        paramNames[0] = "returnDate";

        Calendar returnDate = Calendar.getInstance();
        returnDate.set(Calendar.DAY_OF_MONTH, pReturnDate.getDay());
        returnDate.set(Calendar.YEAR, pReturnDate.getYear());
        //Subtracting one since SPCFCalendar is 1-12 and Java calendar is 0-11
        returnDate.set(Calendar.MONTH, pReturnDate.getMonth() - 1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String formattedReturnDate = simpleDateFormat.format(returnDate.getTime());

        Object[] paramValues = new Object[1];
        paramValues[0] = formattedReturnDate;

        return Application.findByNamedQuery("findTxnReturnBatchByReturnDate", paramNames, paramValues);
    }

    public static DomainEntitySet<TransactionReturn> findTransactionReturnsByReturnCodeAndMMT(MoneyMovementTransaction pMoneyMovementTransaction, String pBankReturnCd) {
        return Application.find(TransactionReturn.class,
                                TransactionReturn.MoneyMovementTransaction().equalTo(pMoneyMovementTransaction)
                                .And(TransactionReturn.BankReturnCd().like(pBankReturnCd+ "%")));
    }

    /**
     * Function to find the transaction returns for the given financial transaction by excluding the transaction
     * return status.
     * @param pFinTx  FinancialTransaction
     * @param pReturnStatCode  TransactionReturnStatusCode
     * @return DomainEntitySet<TransactionReturn>
     */
    public static DomainEntitySet<TransactionReturn> findTransactionReturnsByExcludedStatus(FinancialTransaction pFinTx,
                                                                                     TransactionReturnStatusCode pReturnStatCode) {

        String[] paramNames = new String[2];
        paramNames[0] = "moneyMovementTx";
        paramNames[1] = "excludedStatus";

        Object[] paramValues = new Object[2];
        paramValues[0] = pFinTx.getMoneyMovementTransaction();
        paramValues[1] = pReturnStatCode;

        DomainEntitySet<TransactionReturn> txReturns = Application.findByNamedQuery("findTransactionReturnsExcludedStatus",
                paramNames, paramValues);

        return txReturns;
    }

    public static DomainEntitySet<TransactionReturn> resolveTransactionReturns(Company pCompany, String pPayrollRunId, TransactionTypeCode pTransactionTypeCode) {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        DomainEntitySet<TransactionReturn> returnForPayrollCollection =
                findTransactionReturns(pPayrollRunId, pCompany);

        if (returnForPayrollCollection != null && returnForPayrollCollection.size() > 0) {
            for (TransactionReturn transactionReturn : returnForPayrollCollection) {
                TransactionReturnStatusCode returnStatusCode = transactionReturn.getReturnStatusCd();
                DomainEntitySet<FinancialTransaction> finTxnList = findFinancialTransaction(transactionReturn);
                for (FinancialTransaction financialTransaction : finTxnList) {
                    if (pTransactionTypeCode.equals(
                            financialTransaction.getTransactionType().getTransactionTypeCd())) {
                        if (TransactionReturnStatusCode.Resolved != returnStatusCode) {
                            transactionReturns.add(transactionReturn.updateTransactionReturnStatus(
                                    TransactionReturnStatusCode.Resolved));
                        }
                    }
                }
            }
        }

        return transactionReturns;
    }

    /**
     * Function to find the transaction returns for the given service and excluding the transaction return status
     * @param  pService        Service
     * @param  pExcludedStatus TransactionReturnStatusCode
     * @return DomainEntitySet<TransactionReturn>
     */
    public static DomainEntitySet<TransactionReturn> findTransactionReturnsByServiceAndExcludedStatus(Company pCompany, Service pService,
                                                                                                      TransactionReturnStatusCode pExcludedStatus) {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Query<TransactionReturn> query =
                (Query)new Query<TransactionReturn>()
                        .Where(TransactionReturn.MoneyMovementTransaction().Company().equalTo(pCompany)
                        .And(TransactionReturn.ReturnStatusCd().notEqualTo(pExcludedStatus))
                        .And(TransactionReturn.Company().equalTo(pCompany)));

        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
            query = (Query)query.EagerLoad(TransactionReturn.MoneyMovementTransaction().FinancialTransactionSet().Filter().Company().equalTo(TransactionReturn.MoneyMovementTransaction().Company()))
                    .EagerLoad(TransactionReturn.MoneyMovementTransaction());
        } else{
            query = (Query)query.EagerLoad(TransactionReturn.MoneyMovementTransaction(), TransactionReturn.MoneyMovementTransaction().FinancialTransactionSet());
        }
        DomainEntitySet<TransactionReturn> returns = Application.find(TransactionReturn.class, query);

        for (TransactionReturn aReturn : returns) {
            for (FinancialTransaction financialTransaction : aReturn.getMoneyMovementTransaction().getFinancialTransactionCollection()) {
                if (pService.getTransactionTypeCollection().contains(financialTransaction.getTransactionType())) {
                    transactionReturns.add(aReturn);
                }
            }
        }

        return transactionReturns;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TransactionReturn()
	{
		super();
	}

    public boolean isRejectReturn() {
        String bankReturnCode = getBankReturnCd();

        return (bankReturnCode != null && bankReturnCode.startsWith(ReturnTypeCodes.RETURN));
    }

    /**
     * Method to update the transaction return status
     *
     * @param pTransactionReturnStatusCd
     */
    public TransactionReturn updateTransactionReturnStatus(
            TransactionReturnStatusCode pTransactionReturnStatusCd) {
        setReturnStatusCd(pTransactionReturnStatusCd);
        setReturnStatusEffectiveDate(PSPDate.getPSPTime());
        return Application.save(this);
    }

    public boolean isNSF() {
        return "R01".equals(getBankReturnCd()) || "R09".equals(getBankReturnCd());
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg .append("TransactionReturn: ").append(getId()).append("   ")
            .append("Status: ").append(getReturnStatusCd()).append("   ")
            .append("TraceNumber: ").append(getBankReturnTraceNumber()).append("   ")
            .append("ReturnCd: ").append(getBankReturnCd()).append("   ")
            .append("Description: ").append(getBankReturnDescription());

        return msg.toString();
    }

    public String toExtendedString() {
        StringBuilder msg = new StringBuilder(toString());
        try {
            if (getCompany() != null) {
                msg.append("\n\t").append(getCompany()).append("   ");
            }
        } catch (Throwable ignored) { }

        try {
            if (getMoneyMovementTransaction() != null) {
                msg.append("\n\t").append(getMoneyMovementTransaction()).append("   ");
            }
        } catch (Throwable ignored) { }

        return msg.toString();
    }

    public static class ReturnTypeCodes {
        public static final String RETURN = "R";
        public static final String NOTICE_OF_CHANGE = "C";
    }
}