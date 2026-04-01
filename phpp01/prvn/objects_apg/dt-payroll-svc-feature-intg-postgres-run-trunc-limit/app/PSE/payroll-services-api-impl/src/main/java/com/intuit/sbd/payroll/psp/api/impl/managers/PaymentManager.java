/*
 * $Id: //psp/dev/PSE/PayrollServicesAPIImpl/src/com/intuit/sbd/payroll/psp/api/impl/managers/PaymentManager.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.managers.IPaymentManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.List;
import java.util.Map;

/**
 * @author Dawn Martens
 */
class PaymentManager implements IPaymentManager {

    public ProcessResult changePaymentMethod(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                             SpcfUniqueId pPaymentId, PaymentMethod pNewPaymentMethod) {
        return new ChangePaymentMethodTax(pSourceSystemCode, pSourceCompanyId, pPaymentId, pNewPaymentMethod).execute();
    }

    public ProcessResult updatePaymentAgentEnabledCore(SourceSystemCode sourceSystemCd, String sourceCompanyId, String paymentTemplateCd, PaymentMethod paymentMethod, boolean agentEnabled) {
        return new UpdatePaymentAgentEnabledCore(sourceSystemCd, sourceCompanyId, paymentTemplateCd, paymentMethod, agentEnabled).execute();
    }

    public ProcessResult updateDepositFrequency(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO) {
        UpdateDepositFrequencyCore process = new UpdateDepositFrequencyCore(pSourceSystemCd, pSourceCompanyId,
                pEffectiveDepositFrequencyDTO, false);
        return process.execute();
    }

    public ProcessResult invalidateDepositFrequency(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                                    EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO) {
        UpdateDepositFrequencyCore process = new UpdateDepositFrequencyCore(pSourceSystemCd, pSourceCompanyId,
                pEffectiveDepositFrequencyDTO, true);
        return process.execute();
    }

    public ProcessResult submitPriorPaymentsTax(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, List<PriorPaymentSubmissionDTO> pPriorPaymentSubmissions) {
        return submitPriorPaymentsTax(pSourceSystemCode, pSourceCompanyId, pPriorPaymentSubmissions, false);
    }

    public ProcessResult submitPriorPaymentsTax(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, List<PriorPaymentSubmissionDTO> pPriorPaymentSubmissions, Boolean pIsBalanceFile) {
        return new PriorPaymentsTax(pSourceSystemCode, pSourceCompanyId, pPriorPaymentSubmissions, pIsBalanceFile).execute();
    }

    public ProcessResult addTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason) {
        return new AddTaxPaymentOnHoldReason(pTaxPaymentMMT, pPaymentOnHoldReason).execute();
    }

    public ProcessResult expireTaxPaymentOnHoldReason(MoneyMovementTransaction pTaxPaymentMMT, PaymentOnHoldReason pPaymentOnHoldReason) {
        return new RemoveTaxPaymentOnHoldReason(pTaxPaymentMMT, pPaymentOnHoldReason).execute();
    }

    public ProcessResult updateInitiationDate(String mmtId, SpcfCalendar newInitDate) {
        return new UpdateInitiationDateCore(mmtId, newInitDate).execute();
    }

    public ProcessResult initiateTaxRepayment(String mmtId, SpcfCalendar newInitDate, boolean recreate) {
        return new InitiateTaxRepaymentCore(mmtId, newInitDate, recreate).execute();
    }

    public ProcessResult rejectPayment(String mmtId, String pReason) {
        return new RejectTaxPaymentCore(mmtId, pReason).execute();
    }

    public ProcessResult finalizeSUIPayments(List<MoneyMovementTransaction> pMoneyMovementTransactions, PaymentTemplate pPaymentTemplate, int pYear, int pQuarter) {
        return new FinalizeSUITaxPayment(pMoneyMovementTransactions, pPaymentTemplate, pYear, pQuarter).execute();
    }

    public ProcessResult unfinalizeSUIPayments(List<MoneyMovementTransaction> pMoneyMovementTransactions, PaymentTemplate pPaymentTemplate, int pYear, int pQuarter) {
        return new UnfinalizeSUITaxPayment(pMoneyMovementTransactions, pPaymentTemplate, pYear, pQuarter).execute();
    }
    public ProcessResult splitSUIPayments(List<FinancialTransaction> pFinancialTransactions, String pNote){
        return new SplitSUITaxPayment(pFinancialTransactions, pNote).execute();
    }
    public ProcessResult combineSUIPayments(List<FinancialTransaction> pFinancialTransactions, String pNote){
        return new CombineSUITaxPayment(pFinancialTransactions, pNote).execute();
    }
    public ProcessResult adjustSUITaxPayment(MoneyMovementTransaction pMoneyMovementTransaction, Map<Law, SpcfMoney> pLawAmounts, boolean pImmediateDebitOrRefund, String pNote)  {
        return new AdjustSUITaxPayment(pMoneyMovementTransaction, pLawAmounts, pImmediateDebitOrRefund, pNote).execute();
    }
}