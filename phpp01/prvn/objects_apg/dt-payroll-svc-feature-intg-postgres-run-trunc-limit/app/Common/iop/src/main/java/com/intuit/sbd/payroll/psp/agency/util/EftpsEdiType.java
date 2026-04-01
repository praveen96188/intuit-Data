package com.intuit.sbd.payroll.psp.agency.util;

import com.paycycle.eftpsBp.EftpsBpConstants;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Nov 16, 2010
 * Time: 10:54:11 PM
 * To change this template use File | Settings | File Templates.
 */
public enum EftpsEdiType {
    EDI151(151, EftpsBpConstants.EDI_GS_FUNC_CODE_151), // Payment Acknowledgement file (sent to us by TFA)
    EDI813(813, EftpsBpConstants.EDI_GS_FUNC_CODE_813), // Payment file (sent by us to TFA)
    EDI821(821, EftpsBpConstants.EDI_GS_FUNC_CODE_821), // Payment Forecast file (sent by us to TFA)
    EDI824(824, EftpsBpConstants.EDI_GS_FUNC_CODE_824), // Enrollment Accept/Reject file (sent to us by TFA)
    EDI826(826, EftpsBpConstants.EDI_GS_FUNC_CODE_826), // Same-Day Payment Confirmation file (sent to us by TFA)
    EDI827(827, EftpsBpConstants.EDI_GS_FUNC_CODE_827), // Payment Return file (sent to us by TFA)
    EDI838(838, EftpsBpConstants.EDI_GS_FUNC_CODE_838), // Enrollment file (sent by us to TFA)
    EDI997(997, EftpsBpConstants.EDI_GS_FUNC_CODE_997); // General purpose acknowledgement file (we both send and receive these)

    private int mEdiType;
    private String mFuncIdCode;

    EftpsEdiType(int pEdiType, String pFuncIdCode) {
        mEdiType = pEdiType;
        mFuncIdCode = pFuncIdCode;
    }

    public int value() {
        return mEdiType;
    }

    public String funcIdCode() {
        return mFuncIdCode;
    }

    @Override
    public String toString() {
        return Integer.toString(value());
    }

    public static EftpsEdiType getValueByEdiType(int pValue) {
        for (EftpsEdiType type : EftpsEdiType.values()) {
            if (type.value() == pValue) {
                return type;
            }
        }

        throw new RuntimeException(String.format("Invalid EDI type specified (%d not supported)", pValue));
    }
}
