package com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 29, 2008
 * Time: 10:11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestException extends Exception {
    /**
     *
     * @param pMessage
     */
    public RequestException(String pMessage) {
        super(pMessage);
    }

}
