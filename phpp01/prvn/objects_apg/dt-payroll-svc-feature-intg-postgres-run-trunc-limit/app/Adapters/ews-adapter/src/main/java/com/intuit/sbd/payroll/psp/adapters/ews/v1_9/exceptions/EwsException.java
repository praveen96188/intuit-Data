package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessage;

/**
 * @author Jeff Jones
 */
public class EwsException extends Exception {

    private EwsMessage ewsMessage;

    /**
     *
     * @param pEwsMessage
     */
    public EwsException(EwsMessage pEwsMessage) {
        super();
        this.ewsMessage = pEwsMessage;
    }

    /**
     *
     * @return
     */
    public int getCode() {
        return ewsMessage.getCode();
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return ewsMessage.getMessage();
    }

    /**
     *
     * @return
     */
    public EwsMessage getEwsMessage() {
        return ewsMessage;
    }

}
