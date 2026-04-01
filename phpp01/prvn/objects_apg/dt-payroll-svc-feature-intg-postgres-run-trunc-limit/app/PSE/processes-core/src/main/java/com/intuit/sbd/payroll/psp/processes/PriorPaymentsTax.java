package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PriorPaymentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxPaymentDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * User: dweinberg
 * Date: Oct 27, 2010
 * Time: 11:37:59 AM
 */
public class PriorPaymentsTax extends Process {

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private List<PriorPaymentSubmissionDTO> priorPaymentSubmissions;
    private Boolean isBalanceFile = false;
    private Company company;

    public PriorPaymentsTax(SourceSystemCode sourceSystemCd, String sourceCompanyId, List<PriorPaymentSubmissionDTO> priorPaymentSubmissions, Boolean isBalanceFile) {
        this.sourceSystemCd = sourceSystemCd;
        this.sourceCompanyId = sourceCompanyId;
        this.priorPaymentSubmissions = priorPaymentSubmissions;
        this.isBalanceFile = isBalanceFile;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        validationResult.merge(Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // validate company
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        for (PriorPaymentSubmissionDTO ppsDTO : priorPaymentSubmissions) {
            for (PriorPaymentDTO ppDTO : ppsDTO.getPayments().values()) {
                validatePriorPayment(validationResult, ppDTO);
            }
        }

        return validationResult;
    }


    private void validatePriorPayment(ProcessResult validationResult, PriorPaymentDTO ppDTO) {
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(ppDTO.getPaymentTemplateCd());
        if (paymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.PaymentTemplate, ppDTO.getSourceId(), ppDTO.getPaymentTemplateCd());
            return;
        }

        if (ppDTO.getTaxes().size() == 0) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollItem, ppDTO.getSourceId(), "Payment must contain at least one tax line");
            return;
        }

        SpcfDecimal total = SpcfMoney.ZERO;
        for (TaxPaymentDTO tpDTO : ppDTO.getTaxes()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, tpDTO.getPayrollItemId());

            if (companyLaw == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.CompanyLaw, tpDTO.getPayrollItemId());
                return;
            }
            if (! tpDTO.getLawId().equals(companyLaw.getLaw().getLawId())) {
                validationResult.getMessages().CompanyLawDoesNotMatchLaw(tpDTO.getPayrollItemId(), tpDTO.getLawId(), companyLaw.getLaw().getLawId());
                return;
            }

            if (!paymentTemplate.equals(companyLaw.getLaw().getPaymentTemplate())) {
                validationResult.getMessages().PriorPaymentTemplateDoesNotMatch(company, ppDTO.getSourceId());
                return;
            }

            if (tpDTO.getDate().compareTo(ppDTO.getPaymentDate()) < 0) {
                //this would cause inconsistencies with the partitioning strategy of MMT/FT
                validationResult.getMessages().GenericError(EntityName.FinancialTransaction, tpDTO.getPayrollItemId(), "Prior Payment line cannot settle before prior payment payment date.");
                return;
            }

            total = total.add(tpDTO.getAmount());
        }

        if (!total.equals(ppDTO.getTotalAmount())) {
            validationResult.getMessages().PriorPaymentAmountsDoNotMatch(company, ppDTO.getSourceId());
        }
    }


    @Override
    public ProcessResult process() {
        ProcessResult pr = new ProcessResult();

        for (PriorPaymentSubmissionDTO ppsDTO : priorPaymentSubmissions) {
            PriorPaymentSubmission pps = PriorPaymentSubmission.findPriorPaymentSubmissionByCompanyAndSourceId(company, ppsDTO.getSourceId());
            if (pps == null) {
                newPaymentSubmission(ppsDTO);
            } else {
                updatePaymentSubmission(ppsDTO);
            }
        }

        return pr;
    }

    private void newPaymentSubmission(PriorPaymentSubmissionDTO ppsDTO) {
        PriorPaymentSubmission pps = PriorPaymentSubmission.createNewPaymentSubmission(company, ppsDTO.getSourceId());
        for (PriorPaymentDTO ppDTO : ppsDTO.getPayments().values()) {
            newPayment(pps, ppDTO, ppsDTO.getQBDTTransactionInfoDTO());
        }
    }

    private void updatePaymentSubmission(PriorPaymentSubmissionDTO ppsDTO) {
        for (PriorPaymentDTO ppDTO : ppsDTO.getPayments().values()) {
            MoneyMovementTransaction mmt = MoneyMovementTransaction.findPriorPayment(company,
                    ppsDTO.getSourceId(),
                    PaymentTemplate.findPaymentTemplate(ppDTO.getPaymentTemplateCd()));
            if (mmt == null) {
                //ignore new payments old submission
            } else {
                updatePayment(mmt, ppDTO, ppsDTO.getQBDTTransactionInfoDTO());
            }
        }

        //ignore persisted payments not present in DTO (do not delete)

    }

    private void newPayment(PriorPaymentSubmission pps, PriorPaymentDTO ppDTO, QBDTTransactionInfoDTO qbdtTransactionInfoDTO) {
        QbdtTransactionInfo qbdtTransactionInfo = createQbdtTransactionInfo(qbdtTransactionInfoDTO);

        MoneyMovementTransaction mmt = MoneyMovementTransaction.createHPDEMoneyMovementTransaction(company,
                pps,
                PaymentTemplate.findPaymentTemplate(ppDTO.getPaymentTemplateCd()),
                ppDTO.getPeriodEndDate().toSpcfCalendar(),
                ppDTO.getPaymentDate().toSpcfCalendar(),
                ppDTO.getTotalAmount(),
                ppDTO.isRefund(),
                qbdtTransactionInfo,
                isBalanceFile);

        if (ppDTO.isIsVoid()) {
            mmt.setManualPaymentStatus(ManualPaymentStatus.Voided);
            mmt.setStatus(PaymentStatus.Canceled);
        }

        for (TaxPaymentDTO tpDTO : ppDTO.getTaxes()) {
            newTax(tpDTO, mmt, ppDTO.isIsVoid(), ppDTO.isRefund());
        }

        // Explicitly call this here since we are creating the MMT first followed by the FTs
        // the calls to this in the MMT setters will not see the FTs.
        MoneyMovementTransaction.recalculateATFPayments(mmt);
    }

    private void updatePayment(MoneyMovementTransaction mmt, PriorPaymentDTO ppDTO, QBDTTransactionInfoDTO qbdtTransactionInfoDTO) {
        //Ignore all updates except to qbdt info
        qbdtTransactionInfoDTO.copyQBDTTransactionInfoFromDTO(mmt.getQbdtTransactionInfo());

        for (TaxPaymentDTO tpDTO : ppDTO.getTaxes()) {
            FinancialTransaction finTxn = FinancialTransaction.findHPDETransaction(tpDTO.getPayrollItemId(), mmt);
            if (finTxn == null) {
                //ignore new taxes on old payment
            } else {
                updateTax(finTxn, tpDTO);
            }
        }
        //ignore all persisted transactions not present in the DTO (do not delete)

    }

    private void newTax(TaxPaymentDTO tpDTO, MoneyMovementTransaction mmt, boolean isVoided, boolean isRefund) {
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, tpDTO.getPayrollItemId());

        QbdtTransactionInfo qbdtTransactionInfo = createQbdtTransactionInfo(tpDTO.getQBDTTransactionInfoDTO());

        FinancialTransaction ft = FinancialTransaction.createHPDETransaction(company, null,
                getTransactionTypeCode(isRefund, tpDTO.getAmount()),
                new SpcfMoney(tpDTO.getAmount().abs()),
                tpDTO.getDate().toSpcfCalendar(),
                companyLaw,
                mmt,
                qbdtTransactionInfo);

        if (isVoided) {
            ft.addTransactionState(TransactionState.findTransactionState(TransactionStateCode.Voided));
        }
    }

    //usually amount is greater than 0, but sometimes they decide to make a negative payment that is *not* a refund (or vice versa).
    //We can't have negative FTs, so we will treat them as refunds, but know that they are not because of the MMT method
    private TransactionTypeCode getTransactionTypeCode(boolean isRefund, SpcfMoney amount) {
        if (isRefund) {
            if(isBalanceFile){
                return amount.isGreaterThan(SpcfMoney.ZERO) ? TransactionTypeCode.AgencyHPDETaxRefund : TransactionTypeCode.AgencyHPDETaxPayment;
            }else {
                return amount.isGreaterThan(SpcfMoney.ZERO) ? TransactionTypeCode.AgencyPostBALFHPDETaxRefund : TransactionTypeCode.AgencyPostBALFHPDETaxPayment;
            }
        } else {
            if (isBalanceFile) {
                return amount.isGreaterThan(SpcfMoney.ZERO) ? TransactionTypeCode.AgencyHPDETaxPayment : TransactionTypeCode.AgencyHPDETaxRefund;
            } else {
                return amount.isGreaterThan(SpcfMoney.ZERO) ? TransactionTypeCode.AgencyPostBALFHPDETaxPayment : TransactionTypeCode.AgencyPostBALFHPDETaxRefund;
            }
        }
    }

    private void updateTax(FinancialTransaction finTxn, TaxPaymentDTO tpDTO) {
        //ignore all updates except to QBDT info
        tpDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(finTxn.getQbdtTransactionInfo());
    }

    private QbdtTransactionInfo createQbdtTransactionInfo(QBDTTransactionInfoDTO dto) {
        QbdtTransactionInfo info = new QbdtTransactionInfo();
        info.setCompany(company);
        dto.copyQBDTTransactionInfoFromDTO(info);
        Application.save(info);
        return info;
    }

}
