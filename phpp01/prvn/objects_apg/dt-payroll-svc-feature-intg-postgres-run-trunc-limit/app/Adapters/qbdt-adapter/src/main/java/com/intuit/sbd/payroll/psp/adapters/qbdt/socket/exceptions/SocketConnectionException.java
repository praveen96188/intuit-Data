package com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 29, 2008
 * Time: 10:08:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocketConnectionException extends Exception {

    /**
     * 
     * @param pMessage
     * @param pCause
     */
    public SocketConnectionException(String pMessage, Throwable pCause ) {
        super(pMessage, pCause);
    }

}
