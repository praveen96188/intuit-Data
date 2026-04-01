package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 14, 2010
 * Time: 3:37:29 PM
 */
public class CompanyAdjustmentSubmissionDTO {
    private DateDTO mSubmissionDate;
    private SpcfMoney mTotalAmount;
    private boolean mIsVoid;
    private String mSourceId;
    private String mMemo;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;
    private Collection<LiabilityAdjustmentDTO> mLiabilityAdjustmentDTOs;
    private SpcfUniqueId mOriginalSubmissionId;
    private QBDTPayrollTransactionDTO mQBDTPayrollTransactionDTO;

    public DateDTO getSubmissionDate() {
        return mSubmissionDate;
    }

    public void setSubmissionDate(DateDTO pSubmissionDate) {
        mSubmissionDate = pSubmissionDate;
    }

    public SpcfMoney getTotalAmount() {
        return mTotalAmount;
    }

    public void setTotalAmount(SpcfMoney pTotalAmount) {
        mTotalAmount = pTotalAmount;
    }

    public boolean isVoid() {
        return mIsVoid;
    }

    public void setIsVoid(boolean pIsVoid) {
        mIsVoid = pIsVoid;
    }

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String pSourceId) {
        mSourceId = pSourceId;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        this.mMemo = pMemo;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

    public Collection<LiabilityAdjustmentDTO> getLiabilityAdjustmentDTOs() {
        if (mLiabilityAdjustmentDTOs == null) {
            mLiabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        }
        return mLiabilityAdjustmentDTOs;
    }

    public void setLiabilityAdjustmentDTOs(Collection<LiabilityAdjustmentDTO> pLiabilityAdjustmentDTOs) {
        mLiabilityAdjustmentDTOs = pLiabilityAdjustmentDTOs;
    }

    public SpcfUniqueId getOriginalSubmissionId() {
        return mOriginalSubmissionId;
    }

    public void setOriginalSubmissionId(SpcfUniqueId pOriginalSubmissionId) {
        mOriginalSubmissionId = pOriginalSubmissionId;
    }

    public QBDTPayrollTransactionDTO getQBDTPayrollTransactionDTO() {
        return mQBDTPayrollTransactionDTO;
    }

    public void setQBDTPayrollTransactionDTO(QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
        this.mQBDTPayrollTransactionDTO = pQBDTPayrollTransactionDTO;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (mLiabilityAdjustmentDTOs != null && mLiabilityAdjustmentDTOs.size() > 0) {
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mLiabilityAdjustmentDTOs) {
                validationResult.merge(liabilityAdjustmentDTO.validate());
            }
        }
        return validationResult;
    }

    public Map<Law, SpcfDecimal> getLiabilityBalances() {
        Map<Law, SpcfDecimal> netAmountPerLaw = new HashMap<Law, SpcfDecimal>();
        for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : getLiabilityAdjustmentDTOs()) {
            Law law = PayrollServices.entityFinder.findById(Law.class, liabilityAdjustmentDTO.getLawId());
            if (law != null && liabilityAdjustmentDTO.getAmount() != null) {
                if (!netAmountPerLaw.containsKey(law)) {
                    netAmountPerLaw.put(law, SpcfMoney.ZERO);
                }
                netAmountPerLaw.put(law, netAmountPerLaw.get(law).add(liabilityAdjustmentDTO.getAmount()));
            }
        }
        return netAmountPerLaw;
    }
}
