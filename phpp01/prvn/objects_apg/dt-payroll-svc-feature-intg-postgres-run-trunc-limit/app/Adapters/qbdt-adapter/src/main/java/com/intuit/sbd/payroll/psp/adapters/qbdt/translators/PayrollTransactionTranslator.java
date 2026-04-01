package com.intuit.sbd.payroll.psp.adapters.qbdt.translators;

import com.intuit.sbd.payroll.psp.adapters.qbdt.processors.PayrollTransactionProcessor;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.AbstractPayrollTransaction;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollTransactionResponse;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.QbdtPayrollTransactionType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 8:25:35 AM
 */
public class PayrollTransactionTranslator {

    public static void populateLiabilityCheckDTO(AbstractPayrollTransaction pPayrollTransaction, LiabilityCheckDTO pLiabilityCheckDTO) {
        pLiabilityCheckDTO.setPeriodEndDate(pPayrollTransaction.getPeriodEndDate());
        pLiabilityCheckDTO.setTransactionDate(SpcfCalendar.createInstance(pPayrollTransaction.getTransactionDate().getTime()));
        pLiabilityCheckDTO.setIsVoid(pPayrollTransaction.getIsVoided());
        pLiabilityCheckDTO.setSourceId(pPayrollTransaction.getSourceId());
        pLiabilityCheckDTO.setAmount(pPayrollTransaction.getTotalAmount());

        pLiabilityCheckDTO.setLiabilityCheckLineDTOs(buildLiabilityCheckLineDTOs(pPayrollTransaction.getTransactionLines()));

        if(pLiabilityCheckDTO.getQBDTTransactionInfoDTO() == null) {
            pLiabilityCheckDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        }
        populateQBDTTransactionInfoDTO(pPayrollTransaction, pLiabilityCheckDTO.getQBDTTransactionInfoDTO());
    }

    public static List<LiabilityCheckLineDTO> buildLiabilityCheckLineDTOs(List<AbstractPayrollTransaction.TransactionLine> pTransactionLines) {
        List<LiabilityCheckLineDTO> liabilityCheckLineDTOs = new ArrayList<LiabilityCheckLineDTO>();
        for (AbstractPayrollTransaction.TransactionLine transactionLine : pTransactionLines) {
            LiabilityCheckLineDTO liabilityCheckLineDTO = new LiabilityCheckLineDTO();
            liabilityCheckLineDTO.setAmount(transactionLine.getAmount());
            liabilityCheckLineDTO.setCompanyPayrollItemId(transactionLine.getPayrollItemId());

            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            populateQBDTTransactionInfoDTO(transactionLine, qbdtTransactionInfoDTO);
            liabilityCheckLineDTO.setQBDTTransactionInfo(qbdtTransactionInfoDTO);

            liabilityCheckLineDTOs.add(liabilityCheckLineDTO);
        }
        return liabilityCheckLineDTOs;
    }

    public static PayrollTransactionProcessor.AdjustmentHolder buildAdjustmentHolder(AbstractPayrollTransaction pPayrollTransaction) {
        PayrollTransactionProcessor.AdjustmentHolder adjustmentHolder = new PayrollTransactionProcessor.AdjustmentHolder();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        adjustmentHolder.setCompanyAdjustmentSubmissionDTO(companyAdjustmentSubmissionDTO);
        companyAdjustmentSubmissionDTO.setIsVoid(pPayrollTransaction.getIsVoided());
        companyAdjustmentSubmissionDTO.setSourceId(pPayrollTransaction.getSourceId());
        companyAdjustmentSubmissionDTO.setTotalAmount(pPayrollTransaction.getTotalAmount());
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(pPayrollTransaction.getTransactionDate()));

        DateDTO periodEndDate = new DateDTO(pPayrollTransaction.getPeriodEndDate());
        for (AbstractPayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
            if(transactionLine.getLawId() != null) {
                companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(buildLiabilityAdjustmentDTO(transactionLine,
                                                                                                             periodEndDate,
                                                                                                             pPayrollTransaction.getEmployeeId()));
            } else {
                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = adjustmentHolder.getQBDTPayrollTransactionDTO();
                if(qbdtPayrollTransactionDTO == null) {
                    qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setRelatedAdjustmentSourceId(companyAdjustmentSubmissionDTO.getSourceId());
                    populateQBDTPayrollTransactionDTO(pPayrollTransaction, qbdtPayrollTransactionDTO);
                    adjustmentHolder.setQBDTPayrollTransactionDTO(qbdtPayrollTransactionDTO);
                }
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(buildQBDTPayrollTransactionLineDTO(transactionLine));
                companyAdjustmentSubmissionDTO.setQBDTPayrollTransactionDTO(qbdtPayrollTransactionDTO);
            }
        }

        if(companyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO() == null) {
            companyAdjustmentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        }
        populateQBDTTransactionInfoDTO(pPayrollTransaction, companyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO());
        return adjustmentHolder;
    }

    public static LiabilityAdjustmentDTO buildLiabilityAdjustmentDTO(AbstractPayrollTransaction.TransactionLine pTransactionLine, DateDTO pEffectiveDate, String pEmployeeId) {
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
        liabilityAdjustmentDTO.setAmount(pTransactionLine.getAmount());
        liabilityAdjustmentDTO.setLawId(pTransactionLine.getLawId());
        liabilityAdjustmentDTO.setEffectiveDate(pEffectiveDate);
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        populateQBDTTransactionInfoDTO(pTransactionLine, qbdtTransactionInfoDTO);
        liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        liabilityAdjustmentDTO.setSourceEmployeeId(pEmployeeId);
        liabilityAdjustmentDTO.setTaxableWages(pTransactionLine.getTaxableWages());
        liabilityAdjustmentDTO.setTotalWages(pTransactionLine.getTotalWages());
        liabilityAdjustmentDTO.setPayrollItemId(pTransactionLine.getPayrollItemId());

        return liabilityAdjustmentDTO;
    }

    public static void populateQBDTTransactionInfoDTO(AbstractPayrollTransaction pPayrollTransaction, QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        pQBDTTransactionInfoDTO.setAccountName(pPayrollTransaction.getAccountName());
        pQBDTTransactionInfoDTO.setAgencyName(pPayrollTransaction.getAgencyName());
        pQBDTTransactionInfoDTO.setCleared(pPayrollTransaction.getCleared());
        pQBDTTransactionInfoDTO.setMemo(pPayrollTransaction.getMemo());
        pQBDTTransactionInfoDTO.setOnService(pPayrollTransaction.getIsOnService());
        pQBDTTransactionInfoDTO.setReferenceNumber(pPayrollTransaction.getReferenceNumber());
    }

    public static void populateQBDTTransactionInfoDTO(AbstractPayrollTransaction.TransactionLine pTransactionLine, QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        pQBDTTransactionInfoDTO.setAccountName(pTransactionLine.getAccountName());
        pQBDTTransactionInfoDTO.setMemo(pTransactionLine.getMemo());
        pQBDTTransactionInfoDTO.setTrackingClass(pTransactionLine.getTrackingClass());
        pQBDTTransactionInfoDTO.setIsDirectDeposit(pTransactionLine.isDirectDeposit());        
    }

    public static void populateQBDTTransactionInfoDTO(PayrollTransactionResponse.TransactionLine pTransactionLine, QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        pQBDTTransactionInfoDTO.setAccountName(pTransactionLine.getAccountName());
        pQBDTTransactionInfoDTO.setMemo(pTransactionLine.getMemo());
        pQBDTTransactionInfoDTO.setTrackingClass(pTransactionLine.getTrackingClass());
        pQBDTTransactionInfoDTO.setIsDirectDeposit(pTransactionLine.isDirectDeposit());
    }

    public static void populatePriorPaymentSubmissionDTO(AbstractPayrollTransaction pPayrollTransaction, PriorPaymentSubmissionDTO pPriorPaymentSubmissionDTO) {
        if (pPriorPaymentSubmissionDTO.getQBDTTransactionInfoDTO() == null) {
            pPriorPaymentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        }
        populateQBDTTransactionInfoDTO(pPayrollTransaction, pPriorPaymentSubmissionDTO.getQBDTTransactionInfoDTO());
        pPriorPaymentSubmissionDTO.setSourceId(pPayrollTransaction.getSourceId());
        pPriorPaymentSubmissionDTO.setPayments(new HashMap<String, PriorPaymentDTO>());
    }

    public static void populatePriorPaymentDTO(AbstractPayrollTransaction pPayrollTransaction, PriorPaymentDTO pPriorPaymentDTO, String pPaymentTemplateCd) {
        pPriorPaymentDTO.setIsRefund(pPayrollTransaction.getTransactionType() == AbstractPayrollTransaction.TransactionType.Refund);
        pPriorPaymentDTO.setIsVoid(pPayrollTransaction.getIsVoided());
        pPriorPaymentDTO.setPaymentDate(new DateDTO(pPayrollTransaction.getTransactionDate()));
        pPriorPaymentDTO.setSourceId(pPayrollTransaction.getSourceId());
        pPriorPaymentDTO.setPaymentTemplateCd(pPaymentTemplateCd);

        DateDTO pPeriodEndDate = new DateDTO(pPayrollTransaction.getPeriodEndDate());
        pPriorPaymentDTO.setPeriodEndDate(pPeriodEndDate);

        pPriorPaymentDTO.setTotalAmount(SpcfMoney.ZERO);
        pPriorPaymentDTO.setTaxes(new ArrayList<TaxPaymentDTO>());
    }

    public static TaxPaymentDTO buildTaxPaymentDTOs(AbstractPayrollTransaction.TransactionLine pTransactionLine, DateDTO pSettlementDate) {
        TaxPaymentDTO taxPaymentDTO = new TaxPaymentDTO();
        if(pTransactionLine.getAmount() == null) {
            taxPaymentDTO.setAmount(SpcfMoney.ZERO);
        } else {
            taxPaymentDTO.setAmount(pTransactionLine.getAmount());
        }
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        populateQBDTTransactionInfoDTO(pTransactionLine, qbdtTransactionInfoDTO);
        taxPaymentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        taxPaymentDTO.setDate(pSettlementDate);
        taxPaymentDTO.setPayrollItemId(pTransactionLine.getPayrollItemId());
        return taxPaymentDTO;
    }

    public static void populateQBDTPayrollTransactionDTO(AbstractPayrollTransaction pPayrollTransaction, QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
        if (pQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO() == null) {
            pQBDTPayrollTransactionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        }
        populateQBDTTransactionInfoDTO(pPayrollTransaction, pQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO());
        pQBDTPayrollTransactionDTO.setSourceId(pPayrollTransaction.getSourceId());
        pQBDTPayrollTransactionDTO.setAmount(pPayrollTransaction.getTotalAmount());
        pQBDTPayrollTransactionDTO.setEmployeeSourceId(pPayrollTransaction.getEmployeeId());
        pQBDTPayrollTransactionDTO.setIsVoided(pPayrollTransaction.getIsVoided());
        pQBDTPayrollTransactionDTO.setPeriodEndDate(pPayrollTransaction.getPeriodEndDate());
        pQBDTPayrollTransactionDTO.setTransactionDate(SpcfCalendar.createInstance(pPayrollTransaction.getTransactionDate().getTime()));
        pQBDTPayrollTransactionDTO.setTransactionType(convertTransactionTypeToQBDTTransactionType(pPayrollTransaction.getTransactionType()));
        pQBDTPayrollTransactionDTO.setEmployeeName(pPayrollTransaction.getEmployeeName());
    }

    public static QBDTPayrollTransactionLineDTO buildQBDTPayrollTransactionLineDTO(AbstractPayrollTransaction.TransactionLine pTransactionLine) {
        QBDTPayrollTransactionLineDTO qbdtPayrollTransactionLineDTO = new QBDTPayrollTransactionLineDTO();
        qbdtPayrollTransactionLineDTO.setAmount(pTransactionLine.getAmount());
        qbdtPayrollTransactionLineDTO.setPayrollItemId(pTransactionLine.getPayrollItemId());
        qbdtPayrollTransactionLineDTO.setTaxableWageAmount(pTransactionLine.getTotalWages());
        qbdtPayrollTransactionLineDTO.setWageBaseAmount(pTransactionLine.getTaxableWages());
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        populateQBDTTransactionInfoDTO(pTransactionLine, qbdtTransactionInfoDTO);
        qbdtPayrollTransactionLineDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        return qbdtPayrollTransactionLineDTO;
    }

    public static QbdtPayrollTransactionType convertTransactionTypeToQBDTTransactionType(AbstractPayrollTransaction.TransactionType pTransactionType) {
        if(pTransactionType == null) {
            return null;
        }

        switch (pTransactionType) {
            case DirectDepositReturn:
                return QbdtPayrollTransactionType.DDReturn;
            case FundsTransfer:
                return QbdtPayrollTransactionType.FundsTransfer;
            case CompanyLiabilityAdjustment:
            case EmployeeLiabilityAdjustment:
                return QbdtPayrollTransactionType.LiabilityAdjustment;
            case PriorPayment:
                return QbdtPayrollTransactionType.PriorPayment;
            case Refund:
                return QbdtPayrollTransactionType.Refund;
            case LiabilityCheck:
                return QbdtPayrollTransactionType.LiabilityCheck;
            default:
                return null;
        }
    }
}
