package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

/**
 * @author Jeff Jones
 */
public class EwsMessage {

    private int code;
    private String message;

    public EwsMessage() {
    }

    public EwsMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     *
     * @param pCode
     */
    public void setCode(int pCode) {
        this.code = pCode;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param pMessage
     */
    public void setMessage(String pMessage) {
        this.message = pMessage;
    }

}
