package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISMessage.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * DTO class to hold the code and message to return in the WS <Response> section.
 *
 */
public class DISMessage {

    private int code;
    private String message;

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
