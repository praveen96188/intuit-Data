package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBookTransferTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: ihannur
 * Date: 2/16/12
 * Time: 10:08 AM
 */
public class AccountingAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(AccountingAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    @FlexMethod
    @Operation(operationIds = OperationId.CreateBookTransfer)
    public List<String> getIntuitAccountsDescription() throws Throwable {
        List<String> intuitAccounts = new ArrayList<String>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<IntuitBankAccount> intuitBankAccounts = IntuitBankAccount.findIntuitBankAccounts();
            for (IntuitBankAccount intuitBankAccount : intuitBankAccounts) {
                intuitAccounts.add(intuitBankAccount.getDescription());
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Intuit accounts", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return intuitAccounts;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateBookTransfer)
    public void createBookTransfer(String pFromAccount, String pToAccount, double pAmount) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            SpcfMoney amount = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(pAmount);
            ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addBookTransferTransaction(pFromAccount, pToAccount, amount);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error creating Book Transfer transaction", processResult);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating Book Transfer transaction", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateBookTransfer)
    public void cancelBookTransfer(String pFinTxnId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.getBookTransferCompany();
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
            ProcessResult processResult = PayrollServices.financialTransactionManager.cancelTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), pFinTxnId);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error cancelling Book Transfer transaction.", processResult);
            }
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error cancelling Book Transfer transaction.", "Transaction", pFinTxnId, ex);
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateBookTransfer)
    public SAPSearchResults<SAPBookTransferTransaction> findBookTransferTransactions(Date fromDate, Date toDate, String pAccount, int pFirstIndex, int pMaxResults, String pSortColumn, Boolean pSortDescending) throws Throwable {
        SAPSearchResults<SAPBookTransferTransaction> searchResults = new SAPSearchResults<SAPBookTransferTransaction>();
        ArrayList<SAPBookTransferTransaction> sapBookTransferTransactions = new ArrayList<SAPBookTransferTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            BankAccount searchBankAccount = null;
            if (!StringUtils.isEmpty(pAccount)) {
                IntuitBankAccount intuitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(pAccount);
                if (intuitBankAccount != null) {
                    searchBankAccount = intuitBankAccount.getBankAccount();
                }
            }

            Company company = Company.getBookTransferCompany();
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);

            Criterion<FinancialTransaction> criterion = FinancialTransaction.Company().equalTo(company)
                    .And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.GlobalBookTransfer)));
            if (fromDate != null) {
                criterion = criterion.And(FinancialTransaction.SettlementDate().greaterOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(fromDate)));
            }

            if (toDate != null) {
                criterion = criterion.And(FinancialTransaction.SettlementDate().lessOrEqualThan(SAPTranslator.getSpcfCalendarFromDate(toDate)));
            }

            if (searchBankAccount != null) {
                criterion = criterion.And(FinancialTransaction.CreditBankAccount().equalTo(searchBankAccount).Or(FinancialTransaction.DebitBankAccount().equalTo(searchBankAccount)));
            }

            Expression<FinancialTransaction> query = new Query<FinancialTransaction>().Where(criterion);

            if(pSortColumn == null || pSortColumn.equals("settlementDate")) {
                if(pSortDescending) {
                    query = ((Query)query).OrderBy(FinancialTransaction.SettlementDate().Descending());
                } else {
                    query = ((Query)query).OrderBy(FinancialTransaction.SettlementDate());
                }
            } else if (pSortColumn.equals("amount")) {
                if(pSortDescending) {
                    query = ((Query)query).OrderBy(FinancialTransaction.FinancialTransactionAmount().Descending());
                } else {
                    query = ((Query)query).OrderBy(FinancialTransaction.FinancialTransactionAmount());
                }
            } else if (pSortColumn.equals("status")) {
                if(pSortDescending) {
                    query = ((Query)query).OrderBy(FinancialTransaction.CurrentTransactionState().Descending());
                } else {
                    query = ((Query)query).OrderBy(FinancialTransaction.CurrentTransactionState());
                }
            }

            searchResults.setTotalRecords(Application.find(FinancialTransaction.class, query).size());

            if (searchResults.getTotalRecords() > 0) {
                query = ((Query) query).LimitResults(pFirstIndex, pMaxResults);
                DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, query);
                for (FinancialTransaction financialTransaction : financialTransactions) {
                    sapBookTransferTransactions.add(AccountingTranslator.getSAPBookTransferTransaction(financialTransaction));
                }
            }
            searchResults.setReturnsList(sapBookTransferTransactions);

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Book Transfer transactions", t);
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }

        return searchResults;
    }

}
