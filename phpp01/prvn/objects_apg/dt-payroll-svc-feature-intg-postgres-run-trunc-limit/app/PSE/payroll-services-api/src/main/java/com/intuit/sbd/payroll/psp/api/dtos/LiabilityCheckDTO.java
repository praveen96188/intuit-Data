package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.BillingDetail;
import com.intuit.sbd.payroll.psp.domain.LiabilityCheckType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 9, 2010
 * Time: 1:38:16 PM
 */
public class LiabilityCheckDTO {
    private SpcfMoney mAmount;
    private SpcfCalendar mPeriodEndDate;
    private boolean mIsVoid;
    private String mSourceId;
    private SpcfCalendar mTransactionDate;
    private LiabilityCheckType mLiabilityCheckType;
    private String mPayrollRunId;
    private boolean mClientUpdate = false;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;
    private List<LiabilityCheckLineDTO> mLiabilityCheckLineDTOs;
    private Long mSystemModifiedToken;
    private DomainEntitySet<BillingDetail> mAssociatedBillingDetails;
    private boolean mIsNewInBalanceFile = false;

    public SpcfMoney getAmount() {
        return mAmount;
    }

    public void setAmount(SpcfMoney pAmount) {
        mAmount = pAmount;
    }

    public SpcfCalendar getPeriodEndDate() {
        return mPeriodEndDate;
    }

    public void setPeriodEndDate(SpcfCalendar pPeriodEndDate) {
        mPeriodEndDate = pPeriodEndDate;
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

    public SpcfCalendar getTransactionDate() {
        return mTransactionDate;
    }

    public void setTransactionDate(SpcfCalendar pTransactionDate) {
        mTransactionDate = pTransactionDate;
    }

    public LiabilityCheckType getLiabilityCheckType() {
        return mLiabilityCheckType;
    }

    public void setLiabilityCheckType(LiabilityCheckType pLiabilityCheckType) {
        mLiabilityCheckType = pLiabilityCheckType;
    }

    public String getSourcePayrollRunId() {
        return mPayrollRunId;
    }

    public void setSourcePayrollRunId(String pPayrollRunId) {
        mPayrollRunId = pPayrollRunId;
    }

    public boolean isClientUpdate() {
        return mClientUpdate;
    }

    public void setClientUpdate(boolean pClientUpdate) {
        mClientUpdate = pClientUpdate;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

    public List<LiabilityCheckLineDTO> getLiabilityCheckLineDTOs() {
        if(mLiabilityCheckLineDTOs == null) {
            mLiabilityCheckLineDTOs = new ArrayList<LiabilityCheckLineDTO>();
        }
        return mLiabilityCheckLineDTOs;
    }

    public void setLiabilityCheckLineDTOs(List<LiabilityCheckLineDTO> pLiabilityCheckLineDTOs) {
        mLiabilityCheckLineDTOs = pLiabilityCheckLineDTOs;
    }

    public Long getSystemModifiedToken() {
        return mSystemModifiedToken;
    }

    public void setSystemModifiedToken(Long pSystemModifiedToken) {
        mSystemModifiedToken = pSystemModifiedToken;
    }

    public boolean isNewInBalanceFile() {
        return mIsNewInBalanceFile;
    }

    public void setIsNewInBalanceFile(boolean pIsNewInBalanceFile) {
        mIsNewInBalanceFile = pIsNewInBalanceFile;
    }

    public DomainEntitySet<BillingDetail> getAssociatedBillingDetails() {
        if (mAssociatedBillingDetails == null) {
            mAssociatedBillingDetails = new DomainEntitySet<BillingDetail>();
        }
        return mAssociatedBillingDetails;
    }

    public void setAssociatedBillingDetails(DomainEntitySet<BillingDetail> pAssociatedBillingDetails) {
        mAssociatedBillingDetails = pAssociatedBillingDetails;
    }

    public boolean hasNonZeroLines() {
        for (LiabilityCheckLineDTO liabilityCheckLineDTO : getLiabilityCheckLineDTOs()) {
            if(liabilityCheckLineDTO.getAmount() != null && !liabilityCheckLineDTO.getAmount().isZero()) {
                return true;
            }
        }

        return false;
    }
}
