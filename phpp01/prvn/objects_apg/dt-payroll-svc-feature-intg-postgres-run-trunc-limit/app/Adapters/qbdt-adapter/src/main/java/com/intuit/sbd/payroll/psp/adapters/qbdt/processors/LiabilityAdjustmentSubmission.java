package com.intuit.sbd.payroll.psp.adapters.qbdt.processors;

import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 1, 2011
 * Time: 11:29:34 AM
 */
public class LiabilityAdjustmentSubmission {
    private SpcfMoney mPennyCutoff;
    private String mPayrollRunId = null;
    private List<PayrollTransactionProcessor.AdjustmentHolder> mAdjustmentHolders;

    public LiabilityAdjustmentSubmission(SpcfMoney pPennyCutoff) {
        mPennyCutoff = pPennyCutoff;
    }

    public String getPayrollRunId() {
        return mPayrollRunId;
    }

    public void setPayrollRunId(String pPayrollRunId) {
        mPayrollRunId = pPayrollRunId;
    }

    public List<PayrollTransactionProcessor.AdjustmentHolder> getAdjustmentHolders() {
        if (mAdjustmentHolders == null) {
            mAdjustmentHolders = new ArrayList<PayrollTransactionProcessor.AdjustmentHolder>();
        }
        return mAdjustmentHolders;
    }

    public void addAdjustmentHolder(PayrollTransactionProcessor.AdjustmentHolder pAdjustmentHolder) {
        getAdjustmentHolders().add(pAdjustmentHolder);
    }

    public boolean rollInToPayroll(CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO) {
        boolean hasNonPennyAdjustments = false;
        boolean hasWageAdjustments = false;
        for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : pCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
            // always roll cobra adjustments in a payroll
            if(liabilityAdjustmentDTO.getLawId() != null && liabilityAdjustmentDTO.getLawId().equals(Law.COBRA)) {
                return true;
            }

            if(!hasNonPennyAdjustments &&
                    liabilityAdjustmentDTO.getAmount() != null && liabilityAdjustmentDTO.getAmount().abs().compareTo(mPennyCutoff) > 0) {
                hasNonPennyAdjustments = true;
            }

            if(!hasWageAdjustments &&
                    ((liabilityAdjustmentDTO.getTaxableWages() != null && !liabilityAdjustmentDTO.getTaxableWages().isZero()) ||
                            (liabilityAdjustmentDTO.getTotalWages() != null && !liabilityAdjustmentDTO.getTotalWages().isZero()))) {
                hasWageAdjustments = true;
            }
        }

        // roll into payroll if the adjustment submission has only penny adjustments and no wage adjustments
        return !hasNonPennyAdjustments && !hasWageAdjustments;
    }
}
