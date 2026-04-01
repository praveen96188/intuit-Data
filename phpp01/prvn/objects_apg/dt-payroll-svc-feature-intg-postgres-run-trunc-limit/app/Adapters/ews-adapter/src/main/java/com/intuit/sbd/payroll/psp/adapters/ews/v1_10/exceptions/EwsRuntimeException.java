package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessage;


/**
 * @author Jeff Jones
 */
public class EwsRuntimeException extends RuntimeException {

    private EwsMessage ewsMessage;

    /**
     *
     * @param pEwsMessage
     */
    public EwsRuntimeException(EwsMessage pEwsMessage) {
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
