package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.LawStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.domain.ReimbursableStatus;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 9, 2010
 * Time: 1:33:39 PM
 */
public class CompanyLawDTO {
    private String mSourceId;
    private String mSourceDescription;
    private String mLawId;
    private LawStatus mExemptionStatus;
    private PayrollItemStatus mFilingStatus;
    private ReimbursableStatus mReimbursableStatus;
    private PayrollItemStatus mStatus;
    private String mTaxFormLine;
    private Integer mW2Code;
    private boolean mIsArchived;
    private QBDTPayrollItemInfoDTO mQBDTPayrollItemInfoDTO;
    private List<EffectiveDepositFrequencyDTO> mDepositFrequencies;
    private List<CompanyLawRateDTO> mRateDTOs;
    private boolean mDTOCreatedBySystem = true;

    public Integer getW2Code() {
        return mW2Code;
    }

    public void setW2Code(Integer pW2Code) {
        mW2Code = pW2Code;
    }

    public String getSourceId() {
        return mSourceId;
    }

    public void setSourceId(String pSourceId) {
        mSourceId = pSourceId;
    }

    public String getSourceDescription() {
        return mSourceDescription;
    }

    public void setSourceDescription(String pSourceDescription) {
        mSourceDescription = pSourceDescription;
    }

    public String getLawId() {
        return mLawId;
    }

    public void setLawId(String pLawId) {
        mLawId = pLawId;
    }

    public LawStatus getExemptionStatus() {
        return mExemptionStatus;
    }

    public void setExemptionStatus(LawStatus pExemptionStatus) {
        mExemptionStatus = pExemptionStatus;
    }

    public PayrollItemStatus getStatus() {
        return mStatus;
    }

    public void setStatus(PayrollItemStatus pStatus) {
        mStatus = pStatus;
    }

    public PayrollItemStatus getFilingStatus() {
        return mFilingStatus;
    }

    public void setFilingStatus(PayrollItemStatus filingStatus) {
        this.mFilingStatus = filingStatus;
    }

    public ReimbursableStatus getReimbursableStatus() {
        return mReimbursableStatus;
    }

    public void setReimbursableStatus(ReimbursableStatus reimbursableStatus) {
        this.mReimbursableStatus = reimbursableStatus;
    }

    public String getTaxFormLine() {
        return mTaxFormLine;
    }

    public void setTaxFormLine(String pTaxFormLine) {
        mTaxFormLine = pTaxFormLine;
    }

    public boolean isArchived() {
        return mIsArchived;
    }

    public void setArchived(boolean pArchived) {
        mIsArchived = pArchived;
    }

    public QBDTPayrollItemInfoDTO getQBDTPayrollItemInfoDTO() {
        return mQBDTPayrollItemInfoDTO;
    }

    public void setQBDTPayrollItemInfoDTO(QBDTPayrollItemInfoDTO pQBDTPayrollItemInfoDTO) {
        mQBDTPayrollItemInfoDTO = pQBDTPayrollItemInfoDTO;
    }

    public List<CompanyLawRateDTO> getRateDTOs() {
        if (mRateDTOs == null) {
            mRateDTOs = new ArrayList<CompanyLawRateDTO>();
        }
        return mRateDTOs;
    }

    public void setRateDTOs(List<CompanyLawRateDTO> pRateDTOs) {
        mRateDTOs = pRateDTOs;
    }

    public List<EffectiveDepositFrequencyDTO> getDepositFrequencies() {
        if (mDepositFrequencies == null) {
            mDepositFrequencies = new ArrayList<EffectiveDepositFrequencyDTO>();
        }
        return mDepositFrequencies;
    }

    public void setDepositFrequencies(List<EffectiveDepositFrequencyDTO> pDepositFrequencies) {
        this.mDepositFrequencies = pDepositFrequencies;
    }

    public boolean isDTOCreatedBySystem() {
        return mDTOCreatedBySystem;
    }

    public void setDTOCreatedBySystem(boolean pDTOCreatedBySystem) {
        mDTOCreatedBySystem = pDTOCreatedBySystem;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //  Validate the law id
        if ((mLawId == null) || (mLawId.trim().equals(""))) {
            validationResult.getMessages().InvalidValue(EntityName.Law, mLawId, "LawId");
        } else if (!Validator.isValidLength(mLawId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.Law, mLawId, "LawId");
        }
        if (mQBDTPayrollItemInfoDTO != null) {
            validationResult.merge(mQBDTPayrollItemInfoDTO.validate());
        }

        return validationResult;
    }
}
