package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 14, 2010
 * Time: 1:05:00 PM
 */
public class ResponsePayrollTransaction extends AbstractPayrollTransaction {
    private IPAYROLLTX mIPAYROLLTX;
    private boolean mSystemModified = false;

    public ResponsePayrollTransaction(IPAYROLLTX pIPAYROLLTX) {
        mIPAYROLLTX = pIPAYROLLTX;
        String transactionType = mIPAYROLLTX.getIPAYROLLTXTYPE();
        if(transactionType == null) {
            throw new RuntimeException("Unknown transaction type for payroll transaction " + mIPAYROLLTX.getIPAYROLLTXID());
        }

        switch (QBOFX.OFXPayrollTransactionTransactionType.valueOf(transactionType.trim())) {
            case DDRETURN:
                mTransactionType = TransactionType.DirectDepositReturn;
                break;
            case FUNDSTRANSFER:
                mTransactionType = TransactionType.FundsTransfer;
                break;
            case LIABADJ:
                if(mIPAYROLLTX.getIEMPID() != null) {
                    mTransactionType = TransactionType.EmployeeLiabilityAdjustment;
                } else {
                    mTransactionType = TransactionType.CompanyLiabilityAdjustment;
                }
                break;
            case LIABCHK:
                mTransactionType = TransactionType.LiabilityCheck;
                break;
            case PRIORPMT:
                mTransactionType = TransactionType.PriorPayment;
                break;
            case REFUND:
                mTransactionType = TransactionType.Refund;
                break;
        }
    }

    @Override
    public boolean processInPSP() {
        return getPeriodEndDate().getYear() >= 2011;
    }

    @Override
    public String getSourceId() {
        return mIPAYROLLTX.getIPAYROLLTXID();
    }

    @Override
    public Date getTransactionDate() {
        return QBOFX.mapOFXStringToDate(mIPAYROLLTX.getIDTTX());
    }

    @Override
    public TransactionType getTransactionType() {
        return mTransactionType;
    }

    @Override
    public String getEmployeeId() {
        return mIPAYROLLTX.getIEMPID();
    }

    @Override
    public String getEmployeeName() {
        if(mTransactionType == TransactionType.DirectDepositReturn) {
            return QBOFX.truncateOFXString(mIPAYROLLTX.getIEMPNAME(), 255);
        }
        return null;
    }

    @Override
    public String getAgencyName() {
        return QBOFX.truncateOFXString(mIPAYROLLTX.getINAME(), 128);
    }

    @Override
    public String getReferenceNumber() {
        return QBOFX.truncateOFXString(mIPAYROLLTX.getIREFNUM(), 11);
    }

    @Override
    public String getAccountName() {
        return QBOFX.truncateOFXString(mIPAYROLLTX.getIACCTNAME(), 128);
    }

    @Override
    public SpcfMoney getTotalAmount() {
        if(mTransactionType == TransactionType.Refund ||
                mTransactionType == TransactionType.DirectDepositReturn ||
                mTransactionType == TransactionType.LiabilityCheck ||
                mTransactionType == TransactionType.FundsTransfer) {
            return QBOFX.mapOFXStringToMoney(mIPAYROLLTX.getIAMT());
        } else {
            if(QBOFX.mapOFXStringToMoney(mIPAYROLLTX.getIAMT()) != null) {
                return new SpcfMoney(QBOFX.mapOFXStringToMoney(mIPAYROLLTX.getIAMT()).negate());
            }
        }
        return null;
    }

    @Override
    public String getMemo() {
        return QBOFX.truncateOFXString(mIPAYROLLTX.getIMEMO(), 4000);
    }

    @Override
    public SpcfCalendar getPeriodEndDate() {
        Date periodEndDate = QBOFX.mapOFXStringToDate(mIPAYROLLTX.getIDTPAYPDEND());
        if(periodEndDate != null) {
            return SpcfCalendar.createInstance(periodEndDate.getTime());
        }
        return null;
    }

    @Override
    public boolean getIsVoided() {
        return QBOFX.mapOFXStringToBoolean(mIPAYROLLTX.getIVOID());
    }

    @Override
    public boolean getIsOnService() {
        return QBOFX.mapOFXStringToBoolean(mIPAYROLLTX.getIONSERVICE());
    }

    @Override
    public String getCleared() {
        return QBOFX.truncateOFXString(mIPAYROLLTX.getICLEARED(), 1);
    }

    @Override
    public boolean getSystemModified() {
        return mSystemModified;
    }

    public void setSystemModified(boolean pSystemModified) {
        mSystemModified = pSystemModified;
    }

    // compare amounts on liability adjustments
    @Override
    public boolean equals(PayrollTransactionResponse pPayrollTransactionResponse) {
        if(getTransactionLines().size() != pPayrollTransactionResponse.getTransactionLines().size()) {
            return false;
        }

        for (TransactionLine transactionLine : getTransactionLines()) {
            for (PayrollTransactionResponse.TransactionLine responseLine : pPayrollTransactionResponse.getTransactionLines()) {
                if(transactionLine.getPayrollItemId().equals(responseLine.getPayrollItemId())) {
                    if(SpcfUtils.compareSpcfDecimalTo(responseLine.getAmount(), transactionLine.getAmount()) != 0 ||
                            SpcfUtils.compareSpcfDecimalTo(responseLine.getTaxableWages(), transactionLine.getTaxableWages()) != 0 ||
                            SpcfUtils.compareSpcfDecimalTo(responseLine.getTotalWages(), transactionLine.getTotalWages()) != 0) {
                        return false;
                    }
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public List<TransactionLine> getTransactionLines() {
        if(mTransactionLines == null) {
            mTransactionLines = new ArrayList<TransactionLine>();
            for (ITXLINE itxline : mIPAYROLLTX.getITXLINE()) {
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                if(mTransactionType == TransactionType.Refund && amount != null) {
                    amount = new SpcfMoney(amount.negate());
                }
                mTransactionLines.add(new TransactionLine(amount,
                                                          itxline.getIPITEMID(),
                                                          QBOFX.truncateOFXString(itxline.getIACCTNAME(), 128),
                                                          QBOFX.truncateOFXString(itxline.getIMEMO(), 4000),
                                                          QBOFX.mapOFXStringToBoolean(itxline.getIISDD()),
                                                          QBOFX.mapOFXStringToMoney(itxline.getIWB()),
                                                          QBOFX.mapOFXStringToMoney(itxline.getITAXABLEWAGE()),
                                                          QBOFX.truncateOFXString(itxline.getICLASS(), 128)));
            }
        }

        return mTransactionLines;
    }

}
