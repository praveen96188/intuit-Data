/*
 * $Id: //psp/dev/Gateways/AS400/src/com/intuit/sbd/payroll/psp/gateways/as400/ISocketManager.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.qbdt.socket;

import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.*;

/**
 * Interface to be used when interacting with Java Sockets
 */
public interface ISocketManager {
    /**
     * Sends text to the host system via the socket and receives the text reply
     *
     * @param pRequest Request text to send to a server
     * @return Response text sent from a server
     * 
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.SocketException
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.RequestException
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.SocketClosedException
     */
    public String processRequest(String pRequest) throws SocketException, RequestException, SocketClosedException;

    /**
     * Closes the socket connection
     *
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.SocketClosingException Thrown if the socket is closed or another problem is experienced while
     * closing the socket
     */
    public void close() throws SocketClosingException;

    /**
     * Opens a socket connection to a server
     *
     * @param pHost IP/HostName of a server
     * @param pPort Port to use on a server
     * @param pSocketTimeout Timeout in Milliseconds before throwing a connection exception when reading from socket
     * @throws com.intuit.sbd.payroll.psp.adapters.qbdt.socket.exceptions.SocketConnectionException Thrown if the connection is already open or if unable to connect
     * @deprecated
     */
    public void open(String pHost, int pPort, int pSocketTimeout) throws SocketConnectionException;

    /**
     * Opens a socket connection to a server
     *
     * @param pHost IP/HostName of a server
     * @param pPort Port to use on a server
     * @param pConnectionTimeout Timeout in Milliseconds before throwing a connection exception when connecting to socket
     * @param pSocketTimeout Timeout in Milliseconds before throwing a connection exception when reading from socket
     * @throws SocketConnectionException Thrown if the connection is already open or if unable to connect
     */
    public void open(String pHost, int pPort, int pConnectionTimeout, int pSocketTimeout) throws SocketConnectionException;
}
