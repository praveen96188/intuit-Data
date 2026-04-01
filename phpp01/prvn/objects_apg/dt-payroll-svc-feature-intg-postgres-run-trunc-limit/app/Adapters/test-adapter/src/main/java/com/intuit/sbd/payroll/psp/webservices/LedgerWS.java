package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.LedgerAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.LedgerEntryWSDTO;
import com.intuit.sbd.payroll.psp.webservices.wsdto.LedgerWSDTO;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 2, 2009
 * Time: 3:16:55 PM
 */
@WebService()
public class LedgerWS {
    @WebMethod
    public Collection<LedgerWSDTO> getLedgerAccount(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                    @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                    @WebParam(name = "sourcePayrollRunId") String sourcePayrollRunId,
                                                    @WebParam(name = "ledgerAccountCd") String ledgerAccountCd,
                                                    @WebParam(name = "includeDetails") boolean includeDetails) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            Collection<LedgerWSDTO> ledgerWSDTOs = new ArrayList<LedgerWSDTO>();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = null;
            if (sourcePayrollRunId != null) {
                payrollRun = TransactionsWS.findPayrollRunBySourceId(company, sourcePayrollRunId);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourcePayrollRunId");
                }
            }

            if (ledgerAccountCd != null) {
                LedgerAccountCode ledgerAccountCode =
                        DDCodeToPSP.getLedgerAccountCode(ledgerAccountCd);
                LedgerAccount ledgerAccount =
                        PayrollServices.entityFinder.findById(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class, ledgerAccountCode);
                ledgerWSDTOs.add(buildLedgerAccountWSDTO(ledgerAccount, payrollRun, company, includeDetails));
            } else {
                DomainEntitySet<LedgerAccount> ledgerAccounts = PayrollServices.entityFinder.findObjects(LedgerAccount.class);

                for (LedgerAccount ledgerAccount : ledgerAccounts)
                {
                    ledgerWSDTOs.add(buildLedgerAccountWSDTO(ledgerAccount, payrollRun, company, includeDetails));
                }
            }
            Collections.sort((List<LedgerWSDTO>)ledgerWSDTOs, new LedgerWSDTOComparator());

            return ledgerWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    private LedgerWSDTO buildLedgerAccountWSDTO(LedgerAccount pLedgerAccount,
                                              PayrollRun pPayrollRun,
                                              Company pCompany,
                                              boolean includeEntries) {

       LedgerWSDTO ledgerWSDTO = new LedgerWSDTO();
       ledgerWSDTO.ledgerAccountCode = pLedgerAccount.getLedgerAccountCd().toString();
       ledgerWSDTO.name = pLedgerAccount.getName();

        SpcfDecimal balanceAmount;
        Collection<ActionEvent> actionEventList = new ArrayList<ActionEvent>();
        if (pPayrollRun != null) {
            balanceAmount =
                    LedgerAccount.getLedgerAccountBalanceByPayroll(pLedgerAccount.getLedgerAccountCd(), pPayrollRun.getSourcePayRunId(), pCompany);
            actionEventList.addAll(pPayrollRun.getValidActions(pLedgerAccount));
            ledgerWSDTO.isCredit = balanceAmount.getSign() >= 0; //todo this should be wrong but the get balance by payroll is wrong so two wrongs make a right
        } else {
            balanceAmount =
                    LedgerAccount.getLedgerAccountBalance(pCompany, pLedgerAccount.getLedgerAccountCd());
            ledgerWSDTO.isCredit = CreditDebitCode.Credit == pLedgerAccount.getLedgerBalanceAmountTypeIndicator((SpcfMoney)balanceAmount);
        }
        ledgerWSDTO.balance = SpcfUtils.convertToBigDecimal((SpcfMoney)balanceAmount.abs());


        Collection<String> allowableActions = new ArrayList<String>();
        for (ActionEvent actionEvent : actionEventList) {
            allowableActions.add(actionEvent.getCode().toString());
        }
        Collections.sort((List<String>)allowableActions);
        ledgerWSDTO.allowableActions = allowableActions;

        if(includeEntries){
            Collection<LedgerEntryWSDTO> ledgerEntryWSDTOs = new ArrayList<LedgerEntryWSDTO>();
            List<Object[]> txnList =
                    FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(
                            pCompany,
                            (pPayrollRun != null) ? pPayrollRun.getSourcePayRunId() : null,
                            pLedgerAccount.getLedgerAccountCd());

            for (Object[] finTxnComboObject : txnList) {
                FinancialTransaction financialTransaction = (FinancialTransaction) finTxnComboObject[0];
                Boolean isCredit = finTxnComboObject[1] != null && "C".equals(finTxnComboObject[1]);
                FinancialTransactionState transactionState = (FinancialTransactionState)finTxnComboObject[2];
                ledgerEntryWSDTOs.add(buildLedgerEntryWSDTO(financialTransaction, isCredit, transactionState.getTransactionState()));
            }
            Collections.sort((List<LedgerEntryWSDTO>)ledgerEntryWSDTOs, new LedgerEntryWSDTOComparator());
            ledgerWSDTO.ledgerEntries = ledgerEntryWSDTOs;
        }

        return ledgerWSDTO;
    }

    private LedgerEntryWSDTO buildLedgerEntryWSDTO(
            FinancialTransaction pFinancialTransaction,
            boolean isCredit, TransactionState transactionState) {
        LedgerEntryWSDTO ledgerEntryWSDTO = new LedgerEntryWSDTO();
        ledgerEntryWSDTO.amount = SpcfUtils.convertToBigDecimal((SpcfMoney)pFinancialTransaction.getFinancialTransactionAmount().abs());
        ledgerEntryWSDTO.financialTransactionId = pFinancialTransaction.getId().toString();

        if(pFinancialTransaction.getPaycheckSplit() != null) {
            ledgerEntryWSDTO.sourceDdTransactionId = pFinancialTransaction.getPaycheckSplit().getSourceDdTxnId();
        }


        SpcfCalendar spcfCreatedDate = pFinancialTransaction.getCreatedDate().toLocal();
        Calendar createdDate = Calendar.getInstance();
        createdDate.setTimeInMillis(0);
        createdDate.set(spcfCreatedDate.getYear(), spcfCreatedDate.getMonth(), spcfCreatedDate.getDay(), spcfCreatedDate.getHour(), spcfCreatedDate.getMinute(), 0);
        ledgerEntryWSDTO.createdDate = createdDate.getTime();

        ledgerEntryWSDTO.settlementDate = new Date(pFinancialTransaction.getSettlementDate().toLocal().getTimeInMilliseconds());
        ledgerEntryWSDTO.settlementType = pFinancialTransaction.getSettlementTypeCd().toString();
        ledgerEntryWSDTO.transactionState = transactionState.getTransactionStateCd().toString();
        ledgerEntryWSDTO.transactionType = pFinancialTransaction.getTransactionType().getTransactionTypeCd().toString();
        ledgerEntryWSDTO.transactionCategory = pFinancialTransaction.getTransactionType().getTransactionCategory().toString();
        ledgerEntryWSDTO.isCredit = isCredit;

        return ledgerEntryWSDTO;
    }

     private class LedgerWSDTOComparator implements Comparator<LedgerWSDTO> {
        public int compare(LedgerWSDTO a, LedgerWSDTO b) {
            return a.ledgerAccountCode.compareTo(b.ledgerAccountCode);
        }
    }

    private class LedgerEntryWSDTOComparator implements Comparator<LedgerEntryWSDTO> {
        public int compare(LedgerEntryWSDTO a, LedgerEntryWSDTO b) {
            return key(a).compareTo(key(b));
        }
        private String key(LedgerEntryWSDTO dto) {
            return dto.transactionCategory + dto.transactionType + dto.amount;
        }
    }
}
