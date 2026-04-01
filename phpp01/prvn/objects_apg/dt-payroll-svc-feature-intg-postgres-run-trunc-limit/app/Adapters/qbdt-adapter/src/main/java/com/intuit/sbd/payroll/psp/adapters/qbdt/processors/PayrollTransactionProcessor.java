package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.translators.PayrollTransactionTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.AbstractPayrollTransaction;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollTransaction;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollTransactionResponse;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 11, 2010
 * Time: 1:48:55 PM
 */
public class PayrollTransactionProcessor {
    private Company mCompany;
    private AssistedConnectionInformation mConnectionInformation;
    private Map<SpcfCalendar, LiabilityAdjustmentSubmission> mAdjustmentSubmissionDTOMap;
    private SpcfMoney mPennyCutoff;
    private CredentialType mCredentialType;

    public PayrollTransactionProcessor(Company pCompany, AssistedConnectionInformation pAssistedConnectionInformation, CredentialType pCredentialType) {
        mCompany = pCompany;
        mConnectionInformation = pAssistedConnectionInformation;
        mAdjustmentSubmissionDTOMap = new TreeMap<SpcfCalendar, LiabilityAdjustmentSubmission>();
        mCredentialType = pCredentialType;
        mPennyCutoff = new SpcfMoney(SystemParameter.findStringValue(SystemParameter.Code.QBDT_PENNY_CUTOFF, "0.03"));
    }

    public ProcessResult addNonAdjustmentPayrollTransactions(List<IPAYROLLTX> pPayrollTransactions) {
        ProcessResult processResult = new ProcessResult();

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        if(pPayrollTransactions.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        List<String> payrollTransactionIds = new ArrayList<String>();
        List<PriorPaymentHolder> priorPaymentHolders = new ArrayList<PriorPaymentHolder>();
        for (IPAYROLLTX iPayrollTransaction : pPayrollTransactions) {
            PayrollTransaction payrollTransaction = new PayrollTransaction(iPayrollTransaction);

            // make sure we update the next ids on the company so that when we create new liability checks we don't reuse one of these ids
            mCompany.usedPayrollTransactionId(payrollTransaction.getSourceId());

            if(!payrollTransactionIds.contains(payrollTransaction.getSourceId())) {
                payrollTransactionIds.add(payrollTransaction.getSourceId());
                if(!payrollTransaction.processInPSP() && Long.parseLong(payrollTransaction.getSourceId()) < Long.parseLong(mCompany.getNextPayrollTransactionId())) {
                    continue;
                }
                switch (payrollTransaction.getTransactionType()) {
                    case LiabilityCheck:
                        if(mConnectionInformation.isBalanceFile()){
                            PriorPaymentHolder priorPaymentHolder = createPriorPaymentHolder(payrollTransaction);
                            PriorPaymentSubmissionDTO priorPaymentSubmissionDTO = priorPaymentHolder.getPriorPaymentSubmissionDTO();
                            if(priorPaymentSubmissionDTO != null && priorPaymentSubmissionDTO.getPayments().size() > 0) {
                                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO;
                                if(priorPaymentHolder.getQBDTPayrollTransactionDTO() == null) {
                                    qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                                    PayrollTransactionTranslator.populateQBDTPayrollTransactionDTO(payrollTransaction, qbdtPayrollTransactionDTO);
                                    qbdtPayrollTransactionDTO.setRelatedPriorPaymentSourceId(payrollTransaction.getSourceId());
                                    priorPaymentHolder.setQBDTPayrollTransactionDTO(qbdtPayrollTransactionDTO);
                                }
                                priorPaymentHolders.add(priorPaymentHolder);
                            } else {
                                processResult.merge(addOrUpdateLiabilityCheck(payrollTransaction, null));
                                if(!processResult.isSuccess()) {
                                    return processResult;
                                }
                            }
                        } else {
                            processResult.merge(addOrUpdateLiabilityCheck(payrollTransaction, null));
                            if(!processResult.isSuccess()) {
                                return processResult;
                            }
                        }
                        break;
                    case PriorPayment:
                    case Refund:
                        PriorPaymentHolder priorPaymentHolder = createPriorPaymentHolder(payrollTransaction);
                        if(priorPaymentHolder != null) {
                            priorPaymentHolders.add(priorPaymentHolder);
                        }
                        break;
                    case DirectDepositReturn:
                    case FundsTransfer:
                        processResult.merge(addOrUpdatePayrollTransaction(payrollTransaction, null));
                        if(!processResult.isSuccess()) {
                            return processResult;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if(priorPaymentHolders.size() > 0) {
            List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = new ArrayList<PriorPaymentSubmissionDTO>();
            for (PriorPaymentHolder priorPaymentHolder : priorPaymentHolders) {
                if(priorPaymentHolder.getPriorPaymentSubmissionDTO() != null) {
                    priorPaymentSubmissionDTOs.add(priorPaymentHolder.getPriorPaymentSubmissionDTO());
                }
            }
            processResult.merge(PayrollServices.paymentManager.submitPriorPaymentsTax(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), priorPaymentSubmissionDTOs, mConnectionInformation.isBalanceFile()));

            for (PriorPaymentHolder priorPaymentHolder : priorPaymentHolders) {
                if(priorPaymentHolder.getQBDTPayrollTransactionDTO() != null) {
                    processResult.merge(PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), priorPaymentHolder.getQBDTPayrollTransactionDTO()));
                }
            }

            if(!processResult.isSuccess()) {
                return processResult;
            }
        }

        return processResult;
    }

    private PriorPaymentHolder createPriorPaymentHolder(PayrollTransaction payrollTransaction) {
        PriorPaymentHolder priorPaymentHolder = new PriorPaymentHolder();
        PriorPaymentSubmissionDTO priorPaymentSubmissionDTO = new PriorPaymentSubmissionDTO();
        priorPaymentHolder.setPriorPaymentSubmissionDTO(priorPaymentSubmissionDTO);
        PayrollTransactionTranslator.populatePriorPaymentSubmissionDTO(payrollTransaction, priorPaymentSubmissionDTO);

        QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = null;
        for (PayrollTransaction.TransactionLine transactionLine : payrollTransaction.getTransactionLines()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, transactionLine.getPayrollItemId());
            if (companyLaw == null) {
                if(qbdtPayrollTransactionDTO == null) {
                    qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    PayrollTransactionTranslator.populateQBDTPayrollTransactionDTO(payrollTransaction, qbdtPayrollTransactionDTO);
                    qbdtPayrollTransactionDTO.setRelatedPriorPaymentSourceId(payrollTransaction.getSourceId());
                    priorPaymentHolder.setQBDTPayrollTransactionDTO(qbdtPayrollTransactionDTO);
                }
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(PayrollTransactionTranslator.buildQBDTPayrollTransactionLineDTO(transactionLine));
            } else {
                TaxPaymentDTO taxPaymentDTO = PayrollTransactionTranslator.buildTaxPaymentDTOs(transactionLine, new DateDTO(payrollTransaction.getTransactionDate()));
                taxPaymentDTO.setLawId(companyLaw.getLaw().getLawId());
                String paymentTemplateCd = companyLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd();

                if (! priorPaymentSubmissionDTO.getPayments().containsKey(paymentTemplateCd)) {
                    PriorPaymentDTO priorPaymentDTO = new PriorPaymentDTO();
                    PayrollTransactionTranslator.populatePriorPaymentDTO(payrollTransaction, priorPaymentDTO, paymentTemplateCd);
                    priorPaymentSubmissionDTO.getPayments().put(paymentTemplateCd, priorPaymentDTO);
                }

                PriorPaymentDTO priorPaymentDTO = priorPaymentSubmissionDTO.getPayments().get(paymentTemplateCd);
                priorPaymentDTO.setTotalAmount(new SpcfMoney(priorPaymentDTO.getTotalAmount().add(taxPaymentDTO.getAmount())));
                priorPaymentDTO.getTaxes().add(taxPaymentDTO);

                //ensure partitioning strategy not invalidated -- initiation date must always be before all settlement dates
                if (priorPaymentDTO.getPaymentDate().compareTo(taxPaymentDTO.getDate()) < 0) {
                    priorPaymentDTO.setPaymentDate(taxPaymentDTO.getDate());
                }
            }
        }

        if(priorPaymentSubmissionDTO.getPayments().size() == 0 && qbdtPayrollTransactionDTO == null) {
            return null;
        } else {
            return priorPaymentHolder;
        }
    }

    public Map<SpcfCalendar, LiabilityAdjustmentSubmission> getNewLiabilityAdjustments(List<IPAYROLLTX> pPayrollTransactions) {
        if (mCredentialType != CredentialType.Pin) {
            return mAdjustmentSubmissionDTOMap;
        }

        if(pPayrollTransactions.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        List<String> payrollTransactionIds = new ArrayList<String>();
        for (IPAYROLLTX iPayrollTransaction : pPayrollTransactions) {
            PayrollTransaction payrollTransaction = new PayrollTransaction(iPayrollTransaction);
            if(!payrollTransactionIds.contains(payrollTransaction.getSourceId())) {
                payrollTransactionIds.add(payrollTransaction.getSourceId());

                if(!payrollTransaction.processInPSP() && Long.parseLong(payrollTransaction.getSourceId()) < Long.parseLong(mCompany.getNextPayrollTransactionId())) {
                    continue;
                }
                if (payrollTransaction.getTransactionType() == PayrollTransaction.TransactionType.CompanyLiabilityAdjustment ||
                        payrollTransaction.getTransactionType() == PayrollTransaction.TransactionType.EmployeeLiabilityAdjustment) {
                    addCompanyAdjustmentSubmissionToMap(payrollTransaction);
                }
            }
        }

        return mAdjustmentSubmissionDTOMap;
    }

    private void addCompanyAdjustmentSubmissionToMap(PayrollTransaction pPayrollTransaction) {
        mapAdjustmentLaws(pPayrollTransaction);
        // todo can QB send an adjustment with no transaction lines?
        if(pPayrollTransaction.getTransactionLines().size() > 0) {
            AdjustmentHolder adjustmentHolder = PayrollTransactionTranslator.buildAdjustmentHolder(pPayrollTransaction);
            SpcfCalendar effectiveDate = pPayrollTransaction.getPeriodEndDate();
            addCompanyAdjustmentSubmissionToMap(adjustmentHolder, effectiveDate);
        }
    }

    private void addCompanyAdjustmentSubmissionToMap(AdjustmentHolder pAdjustmentHolder, SpcfCalendar pEffectiveDate) {
        LiabilityAdjustmentSubmission liabilityAdjustmentSubmission = mAdjustmentSubmissionDTOMap.get(pEffectiveDate);
            if(liabilityAdjustmentSubmission == null) {
                liabilityAdjustmentSubmission = new LiabilityAdjustmentSubmission(mPennyCutoff);
                mAdjustmentSubmissionDTOMap.put(pEffectiveDate, liabilityAdjustmentSubmission);
            }
            liabilityAdjustmentSubmission.addAdjustmentHolder(pAdjustmentHolder);
    }
                                                                                                                           
    public ProcessResult<List<PayrollRun>> updatePayrollTransactions(List<IPAYROLLTX> pPayrollTransactions) {
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        processResult.setResult(new ArrayList<PayrollRun>());

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        if(pPayrollTransactions.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        List<PriorPaymentHolder> priorPaymentHolders = new ArrayList<PriorPaymentHolder>();
        Set<String> voidAdjustmentIds = new HashSet<String>();
        Set<String> recallAdjustmentIds = new HashSet<String>();

        for (IPAYROLLTX iPayrollTransaction : pPayrollTransactions) {
            PayrollTransaction payrollTransaction = new PayrollTransaction(iPayrollTransaction);
            if(!payrollTransaction.processInPSP() && Long.parseLong(payrollTransaction.getSourceId()) < Long.parseLong(mCompany.getNextPayrollTransactionId())) {
                continue;
            }
            switch (payrollTransaction.getTransactionType()) {
                case CompanyLiabilityAdjustment:
                case EmployeeLiabilityAdjustment:
                    CompanyAdjustmentSubmission companyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(mCompany, payrollTransaction.getSourceId());
                    if(companyAdjustmentSubmission != null) {
                        mapAdjustmentLaws(payrollTransaction);

                        PayrollTransactionResponse adjustmentPayrollTransaction = new PayrollTransactionResponse(companyAdjustmentSubmission);
                        if(!companyAdjustmentSubmission.isVoid()) {
                            if(payrollTransaction.getIsVoided()) {
                                if(companyAdjustmentSubmission.hasTaxImpoundOffloaded()) {
                                    voidAdjustmentIds.add(companyAdjustmentSubmission.getSourceId());
                                } else {
                                    recallAdjustmentIds.add(companyAdjustmentSubmission.getSourceId());
                                }
                            } else if(!payrollTransaction.equals(adjustmentPayrollTransaction)){
                                createOffsettingLiabilityAdjustment(payrollTransaction, adjustmentPayrollTransaction);
                            }
                        }

                        AdjustmentHolder adjustmentHolder = PayrollTransactionTranslator.buildAdjustmentHolder(payrollTransaction);

                        processResult.merge(PayrollServices.payrollManager.updateQBLiabilityAdjustmentInfo(mCompany.getSourceSystemCd(),
                                                                                                           mCompany.getSourceCompanyId(),
                                                                                                           adjustmentHolder.getCompanyAdjustmentSubmissionDTO()));

                        if(adjustmentHolder.getQBDTPayrollTransactionDTO() != null) {
                            processResult.merge(PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(),
                                                                                                                 mCompany.getSourceCompanyId(),
                                                                                                                 adjustmentHolder.getQBDTPayrollTransactionDTO()));
                        }
                        if(!processResult.isSuccess()) {
                            return processResult;
                        }
                    } else {
                        addCompanyAdjustmentSubmissionToMap(payrollTransaction);
                    }
                    break;
                case LiabilityCheck:
                    LiabilityCheck liabilityCheck;
                    if(mConnectionInformation.isBalanceFile()) {
                        liabilityCheck = null;
                    } else {
                        liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(mCompany, payrollTransaction.getSourceId());
                    }

                    if(liabilityCheck == null) {
                        // see if we treated the liability check as a prior payment in the balance file
                        PriorPaymentSubmission priorPaymentSubmission = PriorPaymentSubmission.findPriorPaymentSubmissionByCompanyAndSourceId(mCompany, payrollTransaction.getSourceId());
                        if(priorPaymentSubmission != null) {
                            PriorPaymentHolder priorPaymentHolder = createPriorPaymentHolder(payrollTransaction);
                            if(priorPaymentHolder != null) {
                                priorPaymentHolders.add(priorPaymentHolder);
                            }
                            break;
                        }
                    }
                    processResult.merge(addOrUpdateLiabilityCheck(payrollTransaction, liabilityCheck));
                    if(!processResult.isSuccess()) {
                        return processResult;
                    }
                    break;
                case PriorPayment:
                case Refund:
                    PriorPaymentHolder priorPaymentHolder = createPriorPaymentHolder(payrollTransaction);
                    if(priorPaymentHolder != null) {
                        priorPaymentHolders.add(priorPaymentHolder);
                    }
                    break;
                default:
                    break;
            }
        }

        if(priorPaymentHolders.size() > 0) {
            List<PriorPaymentSubmissionDTO> priorPaymentSubmissionDTOs = new ArrayList<PriorPaymentSubmissionDTO>();
            for (PriorPaymentHolder priorPaymentHolder : priorPaymentHolders) {
                if(priorPaymentHolder.getPriorPaymentSubmissionDTO() != null) {
                    priorPaymentSubmissionDTOs.add(priorPaymentHolder.getPriorPaymentSubmissionDTO());
                }
            }
            processResult.merge(PayrollServices.paymentManager.submitPriorPaymentsTax(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), priorPaymentSubmissionDTOs));

            for (PriorPaymentHolder priorPaymentHolder : priorPaymentHolders) {
                if(priorPaymentHolder.getQBDTPayrollTransactionDTO() != null) {
                    processResult.merge(PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), priorPaymentHolder.getQBDTPayrollTransactionDTO()));
                }
            }

            if(!processResult.isSuccess()) {
                return processResult;
            }
        }

        if(voidAdjustmentIds.size() > 0) {
            ProcessResult<List<PayrollRun>> voidLiabilityAdjustmentsProcessResult = voidLiabilityAdjustments(voidAdjustmentIds, false);
            if(!voidLiabilityAdjustmentsProcessResult.isSuccess()) {
                processResult.merge(voidLiabilityAdjustmentsProcessResult);
                return processResult;
            }
            processResult.getResult().addAll(voidLiabilityAdjustmentsProcessResult.getResult());
        }

        if(recallAdjustmentIds.size() > 0) {
            ProcessResult<List<PayrollRun>> recallLiabilityAdjustmentsProcessResult = voidLiabilityAdjustments(recallAdjustmentIds, true);
            if(!recallLiabilityAdjustmentsProcessResult.isSuccess()) {
                processResult.merge(recallLiabilityAdjustmentsProcessResult);
                return processResult;
            }
            processResult.getResult().addAll(recallLiabilityAdjustmentsProcessResult.getResult());
        }

        return processResult;
    }

    private ProcessResult<List<PayrollRun>> voidLiabilityAdjustments(Set<String> pVoidAdjustmentIds, boolean pIsRecall) {
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        processResult.setResult(new ArrayList<PayrollRun>());

        ProcessResult<Collection<CompanyAdjustmentSubmission>> voidCompanyAdjustmentSubmissionProcessResult =
                PayrollServices.payrollManager.voidLiabilityAdjustments(mCompany.getSourceSystemCd(),
                                                                        mCompany.getSourceCompanyId(),
                                                                        pVoidAdjustmentIds, pIsRecall);
        if(!voidCompanyAdjustmentSubmissionProcessResult.isSuccess()) {
            processResult.merge(voidCompanyAdjustmentSubmissionProcessResult);
            return processResult;
        }

        for (CompanyAdjustmentSubmission adjustmentSubmission : voidCompanyAdjustmentSubmissionProcessResult.getResult()) {
            DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = adjustmentSubmission.getLiabilityAdjustmentCollection();
            if(liabilityAdjustments.size() > 0 &&
                    liabilityAdjustments.get(0).getPayrollRun() != null) {
                processResult.getResult().add(liabilityAdjustments.get(0).getPayrollRun());
            }
        }

        return processResult;
    }

    private void createOffsettingLiabilityAdjustment(PayrollTransaction pPayrollTransaction, PayrollTransactionResponse pPayrollTransactionResponse) {
        AdjustmentHolder adjustmentHolder = new AdjustmentHolder();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        adjustmentHolder.setCompanyAdjustmentSubmissionDTO(companyAdjustmentSubmissionDTO);
        companyAdjustmentSubmissionDTO.setIsVoid(pPayrollTransactionResponse.isVoid());
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(pPayrollTransaction.getTransactionDate()));
        companyAdjustmentSubmissionDTO.setQBDTTransactionInfoDTO(new QBDTTransactionInfoDTO());
        PayrollTransactionTranslator.populateQBDTTransactionInfoDTO(pPayrollTransaction, companyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO());
        DateDTO effectiveDate = new DateDTO(pPayrollTransaction.getPeriodEndDate());
        String employeeId = pPayrollTransaction.getEmployeeId();

        // create copies of the transactions
        List<PayrollTransactionResponse.TransactionLine> existingAdjustments = new ArrayList<PayrollTransactionResponse.TransactionLine>(pPayrollTransactionResponse.getTransactionLines());
        List<PayrollTransaction.TransactionLine> incomingAdjustments = new ArrayList<PayrollTransaction.TransactionLine>(pPayrollTransaction.getTransactionLines());

        // compare currently saved adjustment amounts with new amounts and create offsetting adjustments for the differences
        for (Iterator<PayrollTransactionResponse.TransactionLine> existingTransactionLineIterator = existingAdjustments.iterator(); existingTransactionLineIterator.hasNext();) {
            PayrollTransactionResponse.TransactionLine existingTransactionLine = existingTransactionLineIterator.next();
            if(existingTransactionLine.getLawId() == null) {
                continue;
            }
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = null;
            for (Iterator<PayrollTransaction.TransactionLine> incomingTransactionLineIterator = incomingAdjustments.iterator(); incomingTransactionLineIterator.hasNext();) {
                PayrollTransaction.TransactionLine incomingTransactionLine = incomingTransactionLineIterator.next();
                if(incomingTransactionLine.getLawId() == null) {
                    continue;
                }
                if(incomingTransactionLine.getPayrollItemId().equals(existingTransactionLine.getPayrollItemId())) {
                    // remove the lines from the collections
                    existingTransactionLineIterator.remove();
                    incomingTransactionLineIterator.remove();

                    if(SpcfUtils.compareSpcfDecimalTo(incomingTransactionLine.getAmount(), existingTransactionLine.getAmount()) != 0){
                        if(liabilityAdjustmentDTO == null) {
                            liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                        }
                        liabilityAdjustmentDTO.setAmount(calculateDifference(incomingTransactionLine.getAmount(), existingTransactionLine.getAmount()));
                    }

                    if(SpcfUtils.compareSpcfDecimalTo(incomingTransactionLine.getTaxableWages(), existingTransactionLine.getTaxableWages()) != 0) {
                        if(liabilityAdjustmentDTO == null) {
                            liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                        }
                        liabilityAdjustmentDTO.setTaxableWages(calculateDifference(incomingTransactionLine.getTaxableWages(), existingTransactionLine.getTaxableWages()));
                    }

                    if(SpcfUtils.compareSpcfDecimalTo(incomingTransactionLine.getTotalWages(), existingTransactionLine.getTotalWages()) != 0) {
                        if(liabilityAdjustmentDTO == null) {
                            liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                        }
                        liabilityAdjustmentDTO.setTotalWages(calculateDifference(incomingTransactionLine.getTotalWages(), existingTransactionLine.getTotalWages()));
                    }

                    if(liabilityAdjustmentDTO != null) {
                        liabilityAdjustmentDTO.setLawId(incomingTransactionLine.getLawId());
                        liabilityAdjustmentDTO.setEffectiveDate(effectiveDate);
                        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                        PayrollTransactionTranslator.populateQBDTTransactionInfoDTO(incomingTransactionLine, qbdtTransactionInfoDTO);
                        liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                        liabilityAdjustmentDTO.setSourceEmployeeId(employeeId);
                        liabilityAdjustmentDTO.setPayrollItemId(incomingTransactionLine.getPayrollItemId());
                        companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
                    }

                    // there can only be one transaction line per payroll item id
                    break;
                }
            }
        }

        // add exact opposite adjustments for any existing adjustments not found in the incoming adjustment
        for (PayrollTransactionResponse.TransactionLine existingAdjustment : existingAdjustments) {
            if(existingAdjustment.getLawId() == null) {
                continue;
            }
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
            if(existingAdjustment.getAmount() != null) {
                liabilityAdjustmentDTO.setAmount(new SpcfMoney(existingAdjustment.getAmount().negate()));
            }
            if(existingAdjustment.getTaxableWages() != null) {
                liabilityAdjustmentDTO.setTaxableWages(new SpcfMoney(existingAdjustment.getTaxableWages().negate()));
            }
            if(existingAdjustment.getTotalWages() != null) {
                liabilityAdjustmentDTO.setTotalWages(new SpcfMoney(existingAdjustment.getTotalWages().negate()));
            }
            liabilityAdjustmentDTO.setLawId(existingAdjustment.getLawId());
            liabilityAdjustmentDTO.setEffectiveDate(effectiveDate);
            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            PayrollTransactionTranslator.populateQBDTTransactionInfoDTO(existingAdjustment, qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setSourceEmployeeId(employeeId);
            liabilityAdjustmentDTO.setPayrollItemId(existingAdjustment.getPayrollItemId());
            companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
        }

        // add any incoming adjustments that did not match existing adjustments
        for (PayrollTransaction.TransactionLine incomingAdjustment : incomingAdjustments) {
            if(incomingAdjustment.getLawId() == null) {
                continue;
            }
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
            if(incomingAdjustment.getAmount() != null) {
                liabilityAdjustmentDTO.setAmount(new SpcfMoney(incomingAdjustment.getAmount()));
            }
            if(incomingAdjustment.getTaxableWages() != null) {
                liabilityAdjustmentDTO.setTaxableWages(new SpcfMoney(incomingAdjustment.getTaxableWages()));
            }
            if(incomingAdjustment.getTotalWages() != null) {
                liabilityAdjustmentDTO.setTotalWages(new SpcfMoney(incomingAdjustment.getTotalWages()));
            }
            liabilityAdjustmentDTO.setLawId(incomingAdjustment.getLawId());
            liabilityAdjustmentDTO.setEffectiveDate(effectiveDate);
            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            PayrollTransactionTranslator.populateQBDTTransactionInfoDTO(incomingAdjustment, qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setSourceEmployeeId(employeeId);
            liabilityAdjustmentDTO.setPayrollItemId(incomingAdjustment.getPayrollItemId());
            companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);
        }

        // this total is not necessarily the sum of the transaction lines. QB sends over the amount that affects accounts.
        companyAdjustmentSubmissionDTO.setTotalAmount(pPayrollTransaction.getTotalAmount());
        companyAdjustmentSubmissionDTO.setOriginalSubmissionId(pPayrollTransactionResponse.getInternalId());

        // filter out positive cobra adjustments and CLA cobra adjustments
        // this can only happen for unlocked customers
        for (Iterator<LiabilityAdjustmentDTO> iterator = companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().iterator(); iterator.hasNext();) {
            LiabilityAdjustmentDTO liabilityAdjustmentDTO = iterator.next();
            if(Law.isCOBRA(liabilityAdjustmentDTO.getLawId()) &&
                    liabilityAdjustmentDTO.getAmount() != null && liabilityAdjustmentDTO.getAmount().compareTo(SpcfMoney.ZERO) > 0) {
                iterator.remove();
            } else if(Law.isCOBRA(liabilityAdjustmentDTO.getLawId()) && liabilityAdjustmentDTO.getSourceEmployeeId() == null) {
                iterator.remove();
            }
        }

        if(companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().size() > 0) {
            addCompanyAdjustmentSubmissionToMap(adjustmentHolder, pPayrollTransaction.getPeriodEndDate());
        }
    }

    public ProcessResult addOrUpdatePayrollTransaction(AbstractPayrollTransaction pPayrollTransaction, QbdtPayrollTransaction pQbdtPayrollTransaction) {
        if (mCredentialType != CredentialType.Pin) {
            return new ProcessResult();
        }

        QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO;
        if(pQbdtPayrollTransaction != null) {
            qbdtPayrollTransactionDTO = PayrollServices.dtoFactory.create(pQbdtPayrollTransaction);
        } else {
            qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
        }

        PayrollTransactionTranslator.populateQBDTPayrollTransactionDTO(pPayrollTransaction, qbdtPayrollTransactionDTO);
        for (AbstractPayrollTransaction.TransactionLine transactionLine : pPayrollTransaction.getTransactionLines()) {
            qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(PayrollTransactionTranslator.buildQBDTPayrollTransactionLineDTO(transactionLine));
        }
        return PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(),
                                                                                mCompany.getSourceCompanyId(),
                                                                                qbdtPayrollTransactionDTO);
    }

    public ProcessResult addOrUpdateLiabilityCheck(AbstractPayrollTransaction pPayrollTransaction, LiabilityCheck pLiabilityCheck) {
        if (mCredentialType != CredentialType.Pin) {
            return new ProcessResult();
        }

        LiabilityCheckDTO liabilityCheckDTO;
        if(pLiabilityCheck != null) {
        liabilityCheckDTO = PayrollServices.dtoFactory.create(pLiabilityCheck);
        } else {
            liabilityCheckDTO = new LiabilityCheckDTO();
            if(mConnectionInformation.isBalanceFile()) {
                liabilityCheckDTO.setIsNewInBalanceFile(true);
            }
        }

        if(pPayrollTransaction.getSystemModified()) {
            liabilityCheckDTO.setSystemModifiedToken(mCompany.getNextToken());
        } else {
            liabilityCheckDTO.setClientUpdate(true);
        }
        PayrollTransactionTranslator.populateLiabilityCheckDTO(pPayrollTransaction, liabilityCheckDTO);
        return PayrollServices.companyManager.addOrUpdateLiabilityCheck(mCompany.getSourceSystemCd(),
                                                                        mCompany.getSourceCompanyId(),
                                                                        liabilityCheckDTO);
    }

    public ProcessResult updateLiabilityAdjustments(AbstractPayrollTransaction pPayrollTransaction) {
        if (mCredentialType != CredentialType.Pin) {
            return new ProcessResult();
        }

        mapAdjustmentLaws(pPayrollTransaction);

        AdjustmentHolder adjustmentHolder = PayrollTransactionTranslator.buildAdjustmentHolder(pPayrollTransaction);

        if(adjustmentHolder.getQBDTPayrollTransactionDTO() != null) {
            return PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(mCompany.getSourceSystemCd(),
                    mCompany.getSourceCompanyId(),
                    adjustmentHolder.getQBDTPayrollTransactionDTO());
        } else {
            return new ProcessResult();
        }
    }

    public ProcessResult<List<PayrollRun>> deletePayrollTransactions(List<String> payrollTransactionIds) {
        ProcessResult<List<PayrollRun>> processResult = new ProcessResult<List<PayrollRun>>();
        processResult.setResult(new ArrayList<PayrollRun>());

        if (mCredentialType != CredentialType.Pin) {
            return processResult;
        }

        if(payrollTransactionIds.size() > 0) {
            mConnectionInformation.setProcessedAddsOrUpdates(true);
        }

        boolean transactionFound;
        for (String payrollTransactionId : payrollTransactionIds) {
            transactionFound = false;

            try {
                Long.parseLong(payrollTransactionId);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Payroll transaction id '" + payrollTransactionId + "' is not a number as expected");
            }


            DomainEntitySet<PriorPaymentSubmission> priorPaymentSubmissions = Application.find(PriorPaymentSubmission.class,
                                                                                                   PriorPaymentSubmission.Company().equalTo(mCompany)
                                                                                                           .And(PriorPaymentSubmission.SourceId().equalTo(payrollTransactionId)));
            for (PriorPaymentSubmission priorPaymentSubmission : priorPaymentSubmissions) {
                for (QbdtTransactionInfo qbdtTransactionInfo : priorPaymentSubmission.getQbdtTransactionInfoCollection()) {
                    qbdtTransactionInfo.setIsDeleted(true);
                    Application.save(qbdtTransactionInfo);
                    transactionFound = true;
                }
                if(priorPaymentSubmission.getQbdtPayrollTransaction() != null &&
                        priorPaymentSubmission.getQbdtPayrollTransaction().getQbdtTransactionInfo() != null) {
                    priorPaymentSubmission.getQbdtPayrollTransaction().getQbdtTransactionInfo().setIsDeleted(true);
                    transactionFound = true;
                }
            }
            if(transactionFound) {
                continue;
            }

            DomainEntitySet<CompanyAdjustmentSubmission> companyAdjustmentSubmissions = Application.find(CompanyAdjustmentSubmission.class,
                                                                                                         CompanyAdjustmentSubmission.Company().equalTo(mCompany)
                                                                                                                 .And(CompanyAdjustmentSubmission.SourceId().equalTo(payrollTransactionId)));
            for (CompanyAdjustmentSubmission companyAdjustmentSubmission : companyAdjustmentSubmissions) {
                if(companyAdjustmentSubmission.hasIRSCreditLaw() && !companyAdjustmentSubmission.isVoid()) {
                    ProcessResult<List<PayrollRun>> voidLiabilityAdjustmentsProcessResult = voidLiabilityAdjustments(new HashSet<String>(Arrays.asList(payrollTransactionId)),
                                                                                                                     !companyAdjustmentSubmission.hasTaxImpoundOffloaded());
                    if(!voidLiabilityAdjustmentsProcessResult.isSuccess()) {
                        processResult.merge(voidLiabilityAdjustmentsProcessResult);
                        return processResult;
                    }
                    processResult.getResult().addAll(voidLiabilityAdjustmentsProcessResult.getResult());
                }
                QbdtTransactionInfo qbdtTransactionInfo = companyAdjustmentSubmission.getQbdtTransactionInfo();
                qbdtTransactionInfo.setIsDeleted(true);
                Application.save(qbdtTransactionInfo);
                if(companyAdjustmentSubmission.getQbdtPayrollTransaction() != null &&
                        companyAdjustmentSubmission.getQbdtPayrollTransaction().getQbdtTransactionInfo() != null) {
                    companyAdjustmentSubmission.getQbdtPayrollTransaction().getQbdtTransactionInfo().setIsDeleted(true);
                }
                transactionFound = true;
            }
            if(transactionFound) {
                continue;
            }

            DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class,
                                                                               LiabilityCheck.Company().equalTo(mCompany)
                                                                               .And(LiabilityCheck.SourceId().equalTo(payrollTransactionId)));
            for (LiabilityCheck liabilityCheck : liabilityChecks) {
                QbdtTransactionInfo qbdtTransactionInfo = liabilityCheck.getQbdtTransactionInfo();
                qbdtTransactionInfo.setIsDeleted(true);
                Application.save(qbdtTransactionInfo);
                transactionFound = true;
            }
            if(transactionFound) {
                continue;
            }

            DomainEntitySet<QbdtPayrollTransaction> qbdtPayrollTransactions =
                    Application.find(QbdtPayrollTransaction.class,
                                     QbdtPayrollTransaction.Company().equalTo(mCompany)
                                             .And(QbdtPayrollTransaction.SourceId().equalTo(payrollTransactionId)));
            for (QbdtPayrollTransaction qbdtPayrollTransaction : qbdtPayrollTransactions) {
                QbdtTransactionInfo qbdtTransactionInfo = qbdtPayrollTransaction.getQbdtTransactionInfo();
                qbdtTransactionInfo.setIsDeleted(true);
                Application.save(qbdtTransactionInfo);
            }
        }

        return processResult;
    }

    private void mapAdjustmentLaws(AbstractPayrollTransaction pPayrollTransaction) {
        for (Iterator<PayrollTransaction.TransactionLine> iterator = pPayrollTransaction.getTransactionLines().iterator(); iterator.hasNext();) {
            PayrollTransaction.TransactionLine transactionLine =  iterator.next();
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, transactionLine.getPayrollItemId());
            if(companyLaw != null) {
                // special logic for positive COBRA
                if(transactionLine.getAmount() != null && companyLaw.getLaw().isCOBRA() &&
                        transactionLine.getAmount().compareTo(SpcfMoney.ZERO) > 0) {
                    String eventMessage = "Positive COBRA amount for '$" + transactionLine.getAmount().toString() + "' was found and removed from " +
                            "a liability adjustment with source id '" + pPayrollTransaction.getSourceId() + "'";
                    CompanyEvent.createCompanyEventAndDetail(mCompany, EventTypeCode.PositiveCobraReceived, EventDetailTypeCode.Details, eventMessage);
                    iterator.remove();
                    continue;
                }

                // special logic for CLA COBRA
                if(companyLaw.getLaw().isCOBRA() && pPayrollTransaction.getEmployeeId() == null) {
                    String eventMessage = "Company liability adjustment for COBRA was found and removed from " +
                            "a liability adjustment with source id '" + pPayrollTransaction.getSourceId() + "'";
                    CompanyEvent.createCompanyEventAndDetail(mCompany, EventTypeCode.PositiveCobraReceived, EventDetailTypeCode.Details, eventMessage);
                    iterator.remove();
                    continue;
                }

                if(transactionLine.getAmount() != null && companyLaw.getQbdtPayrollItemInfo().getIsEmployeePaid()) {
                    // employee taxes are sent over inverted we need them to use the same sign convention as the company paid taxes
                    transactionLine.setAmount(new SpcfMoney(transactionLine.getAmount().negate()));
                }
                transactionLine.setLawId(companyLaw.getLaw().getLawId());
            }
        }
    }

    private void mapPriorPaymentLaw(TaxPaymentDTO pTaxPaymentDTO) {
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, pTaxPaymentDTO.getPayrollItemId());
        if(companyLaw != null) {
            pTaxPaymentDTO.setLawId(companyLaw.getLaw().getLawId());
        }
    }   

    private SpcfMoney calculateDifference(SpcfMoney newAmount, SpcfMoney existingAmount) {
        if(newAmount == null && existingAmount == null) {
            return null;
        } else if(newAmount != null && existingAmount == null) {
            return newAmount;
        } else if(newAmount == null && existingAmount != null) {
            return new SpcfMoney(existingAmount.negate());
        } else {
            return new SpcfMoney(newAmount.subtract(existingAmount));
        }
    }

    public static class PriorPaymentHolder {
        private PriorPaymentSubmissionDTO mPriorPaymentSubmissionDTO;
        private QBDTPayrollTransactionDTO mQBDTPayrollTransactionDTO;

        public PriorPaymentSubmissionDTO getPriorPaymentSubmissionDTO() {
            return mPriorPaymentSubmissionDTO;
        }

        public void setPriorPaymentSubmissionDTO(PriorPaymentSubmissionDTO pPriorPaymentSubmissionDTO) {
            mPriorPaymentSubmissionDTO = pPriorPaymentSubmissionDTO;
        }

        public QBDTPayrollTransactionDTO getQBDTPayrollTransactionDTO() {
            return mQBDTPayrollTransactionDTO;
        }

        public void setQBDTPayrollTransactionDTO(QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
            mQBDTPayrollTransactionDTO = pQBDTPayrollTransactionDTO;
        }
    }

    public static class AdjustmentHolder {
        private CompanyAdjustmentSubmissionDTO mCompanyAdjustmentSubmissionDTO;
        private QBDTPayrollTransactionDTO mQBDTPayrollTransactionDTO;

        public CompanyAdjustmentSubmissionDTO getCompanyAdjustmentSubmissionDTO() {
            return mCompanyAdjustmentSubmissionDTO;
        }

        public void setCompanyAdjustmentSubmissionDTO(CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO) {
            mCompanyAdjustmentSubmissionDTO = pCompanyAdjustmentSubmissionDTO;
        }

        public QBDTPayrollTransactionDTO getQBDTPayrollTransactionDTO() {
            return mQBDTPayrollTransactionDTO;
        }

        public void setQBDTPayrollTransactionDTO(QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
            mQBDTPayrollTransactionDTO = pQBDTPayrollTransactionDTO;
        }
    }
}
