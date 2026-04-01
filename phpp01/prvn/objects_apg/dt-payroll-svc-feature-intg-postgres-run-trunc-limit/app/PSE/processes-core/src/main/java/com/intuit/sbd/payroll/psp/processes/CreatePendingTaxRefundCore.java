package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.CustomerTaxPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.RefundType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.jetbrains.annotations.NotNull;

/**
 * User: dmehta2
 * Date: 01/06/23
 * Time: 9:22 AM
 */
public class CreatePendingTaxRefundCore extends AddCustomerTaxPaymentCore {

    private final String paymentId;
    private MoneyMovementTransaction moneyMovementTransaction;

    public CreatePendingTaxRefundCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentId,
                                      CustomerTaxPaymentDTO customerTaxPaymentDTO) {
        super(pSourceSystemCode, pSourceCompanyId, customerTaxPaymentDTO);
        paymentId = pPaymentId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult;

        validationResult = super.validate();
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate mmt
        if (paymentId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.MoneyMovementTransaction,
                    "CreatePendingTaxRefund", "paymentId");
            return validationResult;
        } else {
            moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(paymentId));
            if (!getCompany().equals(moneyMovementTransaction.getCompany())) {
                validationResult.getMessages().TransactionDoesNotExist(EntityName.MoneyMovementTransaction, paymentId,
                        paymentId, getCompany().getSourceSystemCd().toString(), getCompany().getSourceCompanyId());
            }
        }

        //Check if collected Tax Amount and refund mmt amount is equal
        if (!moneyMovementTransaction.getMoneyMovementTransactionAmount().equals(moneyMovementTransaction.getTaxPaymentAmountCollected())) {
            validationResult.getMessages().TaxAmountNotCollected(EntityName.MoneyMovementTransaction, paymentId, moneyMovementTransaction);
            return validationResult;
        }
        return validationResult;
    }

    @NotNull
    protected DateDTO getPaymentDate() {

        //Setting offload batch null to make sure the mmt is not going to offload
        moneyMovementTransaction.setOffloadBatch(null);
        Application.save(moneyMovementTransaction);

        return new DateDTO(moneyMovementTransaction.getPaymentPeriodEnd());
    }

    protected MoneyMovementTransaction createHPDETransactions(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {

        MoneyMovementTransaction hpdeMMT = super.createHPDETransactions(pCompanyAdjustmentSubmission);
        for (FinancialTransaction financialTransaction : hpdeMMT.getFinancialTransactionCollection()) {
            financialTransaction.setRefundType(RefundType.Refund);
            Application.save(financialTransaction);
        }
        return hpdeMMT;
    }

    protected boolean validateTransactionTypeCode(AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore) {
        PayrollRun newPayrollRun = addLiabilityAdjustmentsCore.getPayrollRun();
        DomainEntitySet<FinancialTransaction> agencyTaxOverpayment = newPayrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxOverpayment);
        return (agencyTaxOverpayment.size() > 0) ? false : true;
    }

    protected void createEvent(Company pCompany, String pUniqueIdentifier, String pPayrollRunId, String pNoteText) {
        CompanyEvent.createPendingPaymentRefundedEvent(getCompany(), pUniqueIdentifier, pPayrollRunId, pNoteText);
    }

}
