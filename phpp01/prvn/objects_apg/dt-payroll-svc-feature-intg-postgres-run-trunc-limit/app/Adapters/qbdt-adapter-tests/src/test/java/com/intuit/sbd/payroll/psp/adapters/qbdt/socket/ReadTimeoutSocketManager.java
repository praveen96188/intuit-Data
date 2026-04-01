package com.intuit.sbd.payroll.psp.adapters.qbdt.socket;

import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 24, 2011
 * Time: 4:47:14 PM
 */
public class ReadTimeoutSocketManager implements ISocketManager {
    public String processRequest(String pRequest) throws SocketException, RequestException, SocketClosedException {
        throw new RuntimeException("blah Read timed out blah");
    }

    public void close() throws SocketClosingException {
    }

    public void open(String pHost, int pPort, int pSocketTimeout) throws SocketConnectionException {
    }

    public void open(String pHost, int pPort, int pConnectionTimeout, int pSocketTimeout) throws SocketConnectionException {       
    }
}
