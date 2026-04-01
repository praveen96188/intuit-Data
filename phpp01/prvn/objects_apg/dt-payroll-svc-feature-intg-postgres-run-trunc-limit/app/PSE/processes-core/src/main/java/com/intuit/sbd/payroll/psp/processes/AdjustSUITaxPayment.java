package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Marcela Villani
 * Date: Jan 25, 2012
 * Time: 1:58:32 PM
 */
public class AdjustSUITaxPayment extends Process implements IProcess {

    private MoneyMovementTransaction moneyMovementTransaction;
    private Map<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
    private boolean immediateDebitOrRefund;
    private String note;


    public AdjustSUITaxPayment(MoneyMovementTransaction pMoneyMovementTransaction, Map<Law, SpcfMoney> pLawAmounts, boolean pImmediateDebitOrRefund, String pNote) {

        moneyMovementTransaction = pMoneyMovementTransaction;
        lawAmounts = pLawAmounts;
        immediateDebitOrRefund = pImmediateDebitOrRefund;
        note = pNote;

    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        PaymentTemplate paymentTemplate = moneyMovementTransaction.getPaymentTemplate();
        if (!(paymentTemplate.getCategory().equals(PaymentTemplateCategory.SUI))) {
            validationResult.getMessages().InvalidPaymentTemplateCategory(EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getCategory().toString());
        }

        if (!(moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.ATFFinalized))) {
            validationResult.getMessages().PaymentStatusDoesNotMatch(EntityName.MoneyMovementTransaction, moneyMovementTransaction.getId().toString(), TaxPaymentStatus.ATFFinalized.toString());
        }

        for (Law law : lawAmounts.keySet()) {
            if (!law.getPaymentTemplate().getPaymentTemplateCd().equals(paymentTemplate.getPaymentTemplateCd())) {
                validationResult.getMessages().InvalidArgument(EntityName.Law, "Law does not belong to MMT Payment Template.", law.getLawId());
            }
        }

        if (QbdtUnprocessedRequest.findUnprocessedRequests(moneyMovementTransaction.getCompany(), false, QbdtRequestStatus.Error, QbdtRequestStatus.Queued, QbdtRequestStatus.Processing).size() > 0) {
            validationResult.getMessages().GenericError(EntityName.Company, "", "A customer request is currently processing please try again later.");
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        CompanyAdjustmentSubmissionDTO dto = new CompanyAdjustmentSubmissionDTO();
        dto.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
        dto.setMemo(note);
        dto.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
        dto.setIsVoid(false);

        SpcfMoney total = SpcfMoney.ZERO;
        for (Law law : lawAmounts.keySet()) {
            total = new SpcfMoney(SpcfUtils.add(total, lawAmounts.get(law)));
            LiabilityAdjustmentDTO laDTO = new LiabilityAdjustmentDTO();
            laDTO.setAmount(lawAmounts.get(law));
            laDTO.setEffectiveDate(new DateDTO(moneyMovementTransaction.getPaymentPeriodEnd()));
            laDTO.setLawId(law.getLawId());
            laDTO.setReconcilingAdjustment(false);
            dto.getLiabilityAdjustmentDTOs().add(laDTO);
        }

        //Unfinalize the payment
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>();
        mmts.add(moneyMovementTransaction);
        int quarter = CalendarUtils.getQuarterAsInt(moneyMovementTransaction.getPaymentPeriodBegin());
        processResult.merge(PayrollServices.paymentManager.unfinalizeSUIPayments(mmts, null, moneyMovementTransaction.getPaymentPeriodBegin().getYear(), quarter));
        moneyMovementTransaction.setCachedForSUIAdjustment(true); // Unfinalize process cached this MMT, need to add special indicator to identify correct for adding SUI Adjustment FTs.

        // Apply Adjustments
        dto.setTotalAmount(total);
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(immediateDebitOrRefund);
        liabilityAdjustmentOptionsDTO.setCreditCustomer(immediateDebitOrRefund);
        liabilityAdjustmentOptionsDTO.setUseVarianceAccount(!immediateDebitOrRefund);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
        liabilityAdjustmentOptionsDTO.setForceToRecordFTs(true);
        liabilityAdjustmentOptionsDTO.setSUIAdjustment(true);
        Company company = moneyMovementTransaction.getCompany();
        processResult.merge(PayrollServices.payrollManager.addLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), null, dto, new DateDTO(moneyMovementTransaction.getPaymentPeriodEnd()), liabilityAdjustmentOptionsDTO));

        //Finalize the payment again
        processResult.merge(PayrollServices.paymentManager.finalizeSUIPayments(mmts, null, moneyMovementTransaction.getPaymentPeriodBegin().getYear(), quarter));
        moneyMovementTransaction.setCachedForSUIAdjustment(false);

        // create a liability check for the liability adjustments
        for (PayrollRun payrollRun : PayrollRun.getPayrollsInMemory(company)) {
            if (immediateDebitOrRefund) {
                createEvent(payrollRun);
            }
        }

        return processResult;
    }

    private void createEvent(PayrollRun pPayrollRun) {
        FinancialTransaction employerTransaction = pPayrollRun.getFinancialTransactionCollection()
                                                              .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit, TransactionTypeCode.EmployerTaxDebit)
                                                                                  .And(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)));

        if (employerTransaction != null) {
            EventTypeCode eventTypeCode = null;
            switch (employerTransaction.getTransactionType().getTransactionTypeCd()) {
                case EmployerTaxDebit:
                    eventTypeCode = EventTypeCode.SUIImmediateDebitCreated;
                    break;
                case EmployerTaxCredit:
                    eventTypeCode = EventTypeCode.SUIImmediateCreditCreated;
                    break;
            }
            // Create Event
            CompanyEvent.createSUIAdjustmentEvent(pPayrollRun, eventTypeCode);
        }

    }
}
