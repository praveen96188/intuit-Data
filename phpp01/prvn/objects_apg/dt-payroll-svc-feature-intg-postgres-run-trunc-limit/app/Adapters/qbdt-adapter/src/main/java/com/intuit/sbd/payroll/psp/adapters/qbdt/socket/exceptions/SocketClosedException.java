package com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jan 30, 2008
 * Time: 9:56:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class SocketClosedException extends Exception {

    /**
     * 
     * @param pMessage
     */
    public SocketClosedException(String pMessage) {
        super(pMessage);
    }

}
