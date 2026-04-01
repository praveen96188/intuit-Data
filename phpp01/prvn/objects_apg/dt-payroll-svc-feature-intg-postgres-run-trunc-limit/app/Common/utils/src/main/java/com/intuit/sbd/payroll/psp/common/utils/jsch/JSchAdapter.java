package com.intuit.sbd.payroll.psp.common.utils.jsch;

/**
 * Adapter class for JSchListener. Used to avoid providing empty implementation
 * in the concrete classes for other than necessary events
 * 
 * @author kmuthurangam
 *
 */
public class JSchAdapter implements JSchListener {

	@Override
	public void upload(FileBean val) {

	}

	@Override
	public void download(FileBean val) {

	}

    @Override
    public void deleteFile(FileBean val) {

    }

    @Override
    public void connected(String host) {

    }

    @Override
    public void disconnected(String host) {

    }

    @Override
    public void handleEvent(JSchEvent pEvent, Object val) {
        switch (pEvent.getEventType()) {
            case Download:
                download((FileBean) val);
                break;
            case Upload:
                upload((FileBean) val);
                break;
            case DeleteFile:
                deleteFile((FileBean) val);
                break;
            case Connected:
                connected((String) val);
                break;
            case Disconnected:
                disconnected((String) val);
                break;
        }
    }

}