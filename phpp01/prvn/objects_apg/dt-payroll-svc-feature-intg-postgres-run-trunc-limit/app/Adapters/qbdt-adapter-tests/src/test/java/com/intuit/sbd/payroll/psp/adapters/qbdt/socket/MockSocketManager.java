package com.intuit.sbd.payroll.psp.adapters.qbdt.socket;

import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 25, 2011
 * Time: 5:13:52 AM
 */
public class MockSocketManager implements ISocketManager {
    private static int requestCounter = 0;

    public static int getRequestCount() {
        return requestCounter;
    }

    public static void reset() {
        requestCounter = 0;
    }

    public String processRequest(String pRequest) throws SocketException, RequestException, SocketClosedException {
        requestCounter++;
        return QBDTTestHelper.SUCCESSFUL_OFX_RESPONSE;
    }

    public void close() throws SocketClosingException {
    }

    public void open(String pHost, int pPort, int pSocketTimeout) throws SocketConnectionException {
        reset();
    }

    public void open(String pHost, int pPort, int pConnectionTimeout, int pSocketTimeout) throws SocketConnectionException {
        reset();
    }
}
