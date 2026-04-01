package com.intuit.sbd.payroll.psp.agency.eftps;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 4, 2011
 * Time: 11:46:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReturnSegInfo {
    private String errorCode;
    private String returnSegId;
    private String returnSegErrorCode;

    public ReturnSegInfo() {
    }

    public ReturnSegInfo(String errorCode, String returnSegId, String returnSegErrorCode) {
        this.errorCode = errorCode;
        this.returnSegId = returnSegId;
        this.returnSegErrorCode = returnSegErrorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getReturnSegId() {
        return returnSegId;
    }

    public String getReturnSegErrorCode() {
        return returnSegErrorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setReturnSegId(String returnSegId) {
        this.returnSegId = returnSegId;
    }

    public void setReturnSegErrorCode(String returnSegErrorCode) {
        this.returnSegErrorCode = returnSegErrorCode;
    }
}
