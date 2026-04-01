package com.intuit.sbd.payroll.psp.gateways.amo;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 15, 2010
 * Time: 8:37:45 AM
 */
public class CCInfo {
    public CCInfo(BigInteger pCcExpMM, BigInteger pCcExpYYYY, String pCcNum, String pCcType) {
        ccExpMM = pCcExpMM;
        ccExpYYYY = pCcExpYYYY;
        ccNum = pCcNum;
        ccType = pCcType;
    }

    public BigInteger ccExpMM;
    public BigInteger ccExpYYYY;
    public String ccNum;
    public String ccType;
}
