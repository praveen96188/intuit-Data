package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtPayrollTransactionType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/10/11
 * Time: 8:11 AM
 */
public class QBDTPayrollTransactionDTO {
    private QbdtPayrollTransactionType mTransactionType;
    private SpcfMoney mAmount;
    private SpcfCalendar mPeriodEndDate;
    private boolean mIsVoided;
    private String mSourceId;
    private SpcfCalendar mTransactionDate;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;
    private String mEmployeeSourceId;
    private String mEmployeeName;
    private List<QBDTPayrollTransactionLineDTO> mQBDTPayrollTransactionLineDTOs;

    private String relatedAdjustmentSourceId;
    private String relatedPriorPaymentSourceId;

    public QbdtPayrollTransactionType getTransactionType() {
        return mTransactionType;
    }

    public void setTransactionType(QbdtPayrollTransactionType pTransactionType) {
        mTransactionType = pTransactionType;
    }

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

    public boolean getIsVoided() {
        return mIsVoided;
    }

    public void setIsVoided(boolean pIsVoided) {
        mIsVoided = pIsVoided;
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

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

    public String getEmployeeSourceId() {
        return mEmployeeSourceId;
    }

    public void setEmployeeSourceId(String pEmployeeSourceId) {
        mEmployeeSourceId = pEmployeeSourceId;
    }

    public String getEmployeeName() {
        return mEmployeeName;
    }

    public void setEmployeeName(String pEmployeeName) {
        mEmployeeName = pEmployeeName;
    }

    public List<QBDTPayrollTransactionLineDTO> getQBDTPayrollTransactionLineDTOs() {
        if (mQBDTPayrollTransactionLineDTOs == null) {
            mQBDTPayrollTransactionLineDTOs = new ArrayList<QBDTPayrollTransactionLineDTO>();
        }
        return mQBDTPayrollTransactionLineDTOs;
    }

    public void setQBDTPayrollTransactionLineDTOs(List<QBDTPayrollTransactionLineDTO> pQBDTPayrollTransactionLineDTOs) {
        mQBDTPayrollTransactionLineDTOs = pQBDTPayrollTransactionLineDTOs;
    }

    public String getRelatedAdjustmentSourceId() {
        return relatedAdjustmentSourceId;
    }

    public void setRelatedAdjustmentSourceId(String pRelatedAdjustmentSourceId) {
        relatedAdjustmentSourceId = pRelatedAdjustmentSourceId;
    }

    public String getRelatedPriorPaymentSourceId() {
        return relatedPriorPaymentSourceId;
    }

    public void setRelatedPriorPaymentSourceId(String pRelatedPriorPaymentSourceId) {
        relatedPriorPaymentSourceId = pRelatedPriorPaymentSourceId;
    }
}
