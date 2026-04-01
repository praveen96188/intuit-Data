package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;

import java.util.Collection;

/**
 * Hand-written business logic
 */
public class TransactionResponse extends BaseTransactionResponse {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DomainEntitySet<TransactionResponse> findTransactionResponses(Company pCompany,
                                                                         Long token) {
        // If the token wasn't passed in, then do a full sync of all tokens
        // (done by using zero)
        if (token == null) {
            token = 0L;
        }

        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "transactionTokenNumber";


        Object[] paramValues = new Object[2];
        paramValues[0] = pCompany;
        paramValues[1] = token;

        return Application.findByNamedQueryUsingCache(TransactionResponse.class, "findTransactionResponses", paramNames, paramValues);

    }

    public static TransactionResponse findTransactionResponses(Company pCompany,
                                                        String pRequestId) {
        DomainEntitySet<TransactionResponse> txnResponses =
                Application.find(TransactionResponse.class,
                                 TransactionResponse.Company().equalTo(pCompany)
                                 .And(TransactionResponse.SourceRequestId().equalTo(pRequestId)));

        if (txnResponses.size() > 1) {
            throw new RuntimeException("Query for transaction response by company " + pCompany
                    + " and request id " + pRequestId + " did not return 0 or 1 results as expected");
        }

        if (!txnResponses.isEmpty()) {
            return txnResponses.get(0);
        }

        return null;
    }

    public static DomainEntitySet<TransactionResponse> findTransactionResponses(FinancialTransaction pFinancialTx) {
        String[] paramNames = new String[1];
        Object[] paramValues = new Object[1];

        paramNames[0] = "finTx";
        paramValues[0] = pFinancialTx;

        DomainEntitySet<TransactionResponse> retList =
                Application.findByNamedQueryUsingCache(TransactionResponse.class, "findTxnResponsesByFinTxn", paramNames, paramValues);
        return retList;
    }

    public static long getLastTransactionTokenNumber(Company pCompany) {
        String[] paramNames = new String[1];
        paramNames[0] = "company";


        Object[] paramValues = new Object[1];
        paramValues[0] = pCompany;

        Long token = (Long) Application.executeNamedQuery("findLastTransactionResponseToken", paramNames, paramValues).get(0);
        if (token == null) {
            token = 0L;
        }

        return token;
    }

    /**
     * Obtains the next token for use with the creation of a new Transaction Response
     *
     * @return Token number
     */    
    public static Long getNextTxnResponseToken() {
        return Application.nextSequenceValue(SequenceId.SEQ_TXN_TOKEN_NBR, Long.class);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates the TransactionResponse entity, associates the given FTs and persists everything.
     * @param pCompany
     * @param pToken
     * @param pRequestId
     * @param pIncludedFTs
     * @return the new TransactionResponse
     */
    public static TransactionResponse createTransactionResponse(Company pCompany, Long pToken, String pRequestId,
                                                                 Collection<FinancialTransaction> pIncludedFTs) {
        TransactionResponse transactionResponse = new TransactionResponse();
        if (pRequestId != null) {
            transactionResponse.setSourceRequestId(pRequestId);
        }
        transactionResponse.setTransactionTokenNumber(pToken);
        transactionResponse.setCompany(pCompany);
        transactionResponse = Application.save(transactionResponse);

        for (FinancialTransaction ft : pIncludedFTs) {
            FinancialTransactionState ftState = ft.getCurrentFinancialTransactionState();
            ftState.setTransactionResponse(transactionResponse);
            Application.save(ftState);
        }

        return Application.save(transactionResponse);
    }

    /**
     * Creates a transaction response object. Please ensure that the PayrollRun
     * object in question has been stored off or is otherwise in a ready state
     * for use with hibernate
     *
     * @param pPayrollRun Payroll Run for which to create a payroll run
     * @param pToken      Token for the transaction response
     * @param pRequestId  Request ID for the transaction response (can be left null)
     * @return Transaction Response object for insertion to the database
     */
    public static TransactionResponse createTransactionResponseForPayroll(PayrollRun pPayrollRun,
                                                                          Long pToken, String pRequestId) {
        // filter out transactions that should not be included in the transaction response
        DomainEntitySet<FinancialTransaction> includedFTs = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction ft : pPayrollRun.getFinancialTransactionCollection()) {
            if (!ft.transactionIsExcludedFromTransactionResponse()) {
                includedFTs.add(ft);
            }
        }

        // if all were excluded, we don't create anything
        if (includedFTs.isEmpty()) {
            return null;
        }
        else {
            return createTransactionResponse(pPayrollRun.getCompany(), pToken, pRequestId, includedFTs);
        }
    }

    /**
     * Creates a transaction response using the auto-generated token generator
     *
     * @param pPayrollRun Payroll Run
     * @param pRequestId  Request Id
     * @return Transaction Response object to be saved
     */
    public static TransactionResponse createTransactionResponseForPayroll(PayrollRun pPayrollRun, String pRequestId) {
        Long token = TransactionResponse.getNextTxnResponseToken();
        return createTransactionResponseForPayroll(pPayrollRun, token, pRequestId);
    }

    /**
     * Takes an incoming collection of transactions and creates a transaction response to associate to them.  Response
     * is associated to their most current states.
     *
     * @param pCompany      Company for the response
     * @param pTransactions Transactions for which to to associate to a newly created transaction response
     * @param pRequestId    Request Id
     * @return Transaction Response object to be saved
     */
    public static TransactionResponse createTransactionResponse(Company pCompany, Collection<FinancialTransaction> pTransactions, String pRequestId) {
        // filter out transactions that should not be included in the transaction response
        DomainEntitySet<FinancialTransaction> includedFTs = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction ft : pTransactions) {
            if (!ft.transactionIsExcludedFromTransactionResponse()) {
                includedFTs.add(ft);
            }
        }

        // if all were excluded, we don't create anything
        if (includedFTs.isEmpty()) {
            return null;
        }
        else {
            return createTransactionResponse(pCompany, getNextTxnResponseToken(), pRequestId, includedFTs);
        }
    }

    public static TransactionResponse createTransactionResponse(Company pCompany, DomainEntitySet<FinancialTransaction> pTransactions, String pRequestId) {
        // filter out transactions that should not be included in the transaction response
        DomainEntitySet<FinancialTransaction> includedFTs = new DomainEntitySet<FinancialTransaction>();
        for (FinancialTransaction ft : pTransactions) {
            if (!ft.transactionIsExcludedFromTransactionResponse()) {
                includedFTs.add(ft);
            }
        }

        // if all were excluded, we don't create anything
        if (includedFTs.isEmpty()) {
            return null;
        }
        else {
            return createTransactionResponse(pCompany, getNextTxnResponseToken(), pRequestId, includedFTs);
        }
    }

    /**
     * Creates a Transaction Response for the specified Financial Transaction and associate it with the
     * current state of the financial transaction.
     * @param pFT
     * @return
     */
    public static TransactionResponse createTransactionResponseForFinancialTx(FinancialTransaction pFT) {
        // if the FT is excluded, we don't create anything
        if (pFT.transactionIsExcludedFromTransactionResponse()) {
            return null;
        } else {
            DomainEntitySet<FinancialTransaction> includedFTs = new DomainEntitySet<FinancialTransaction>();
            includedFTs.add(pFT);
            return createTransactionResponse(pFT.getCompany(), getNextTxnResponseToken(), null, includedFTs);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TransactionResponse()
	{
		super();
	}


    /**
     * Obtains a list of financial transaction states for a given tranasaction resposne
     *
     * @param pTransactionResponse Transaction resposne to obtain the states from
     * @return List of financial transaction states
     */
    public DomainEntitySet<FinancialTransactionState> getFinancialTransactionStates() {
        return Application.find(FinancialTransactionState.class, FinancialTransactionState.TransactionResponse().equalTo(this));
    }
}