package com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 29, 2008
 * Time: 10:17:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocketException extends Exception {

    /**
     * 
     * @param pMessage
     * @param pCause
     */
    public SocketException(String pMessage, Throwable pCause ) {
        super(pMessage, pCause);
    }

}
