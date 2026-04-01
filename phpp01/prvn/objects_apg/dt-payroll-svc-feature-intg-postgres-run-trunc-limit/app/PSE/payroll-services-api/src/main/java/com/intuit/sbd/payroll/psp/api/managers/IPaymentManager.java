package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentSubmissionDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.List;
import java.util.Map;

public interface IPaymentManager {

    ProcessResult changePaymentMethod(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                      SpcfUniqueId pPaymentId, PaymentMethod pNewPaymentMethod);
    ProcessResult updatePaymentAgentEnabledCore(SourceSystemCode sourceSystemCd, String sourceCompanyId, String paymentTemplateCd, PaymentMethod paymentMethod, boolean agentEnabled);
    ProcessResult updateDepositFrequency(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO);
    ProcessResult invalidateDepositFrequency(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO);
    ProcessResult submitPriorPaymentsTax(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, List<PriorPaymentSubmissionDTO> pPriorPaymentSubmissions);
    ProcessResult submitPriorPaymentsTax(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, List<PriorPaymentSubmissionDTO> pPriorPaymentSubmissions, Boolean pIsBalanceFile);
    ProcessResult addTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason);
    ProcessResult expireTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason);
    ProcessResult updateInitiationDate(String mmtId, SpcfCalendar newInitDate);
    ProcessResult initiateTaxRepayment(String mmtId, SpcfCalendar newInitDate, boolean recreate);
    ProcessResult rejectPayment(String mmtId, String pReason);
    ProcessResult finalizeSUIPayments(List<MoneyMovementTransaction> pMoneyMovementTransactions, PaymentTemplate pPaymentTemplate, int pYear, int pQuarter);
    ProcessResult unfinalizeSUIPayments(List<MoneyMovementTransaction> pMoneyMovementTransactions, PaymentTemplate pPaymentTemplate, int pYear, int pQuarter);
    ProcessResult splitSUIPayments(List<FinancialTransaction> pFinancialTransactions, String pNote);
    ProcessResult combineSUIPayments(List<FinancialTransaction> pFinancialTransactions, String pNote);
    ProcessResult adjustSUITaxPayment(MoneyMovementTransaction pMoneyMovementTransaction, Map<Law, SpcfMoney> pLawAmounts, boolean pImmediateDebitOrRefund, String pNote);
}
