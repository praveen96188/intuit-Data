package com.intuit.sbd.payroll.psp.common.utils.jsch;

/**
 * 
 * JSch does not provide any listener to listen for connect, disconnect, upload
 * or download events. Custom events for JSch can be added to this listener.
 * 
 * Once JSch provide any listener to listen for the above mentioned events, this
 * listener can be deprecated
 * 
 * @author kmuthurangam
 *
 */
public interface JSchListener {

	public void upload(FileBean val);

	public void download(FileBean val);

    public void deleteFile(FileBean val);

    public void connected(String host);

    public void disconnected(String host);

    public void handleEvent(JSchEvent pEvent, Object val);

}