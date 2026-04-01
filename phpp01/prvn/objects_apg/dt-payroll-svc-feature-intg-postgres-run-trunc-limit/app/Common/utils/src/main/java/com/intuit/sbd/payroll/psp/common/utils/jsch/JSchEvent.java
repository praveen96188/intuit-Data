package com.intuit.sbd.payroll.psp.common.utils.jsch;

/**
 * 
 * Model class for JSch events
 * 
 * @author kmuthurangam
 *
 */
public class JSchEvent {

	private JSchEventType mEventType;

	public JSchEvent() {}

	public JSchEvent(JSchEventType pEventType) {
		super();
		this.mEventType = pEventType;
	}

	public JSchEventType getEventType() {
		return mEventType;
	}

	public void setEventType(JSchEventType pEventType) {
		this.mEventType = pEventType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JSchEvent [mEventType=");
		builder.append(mEventType);
		builder.append("]");
		return builder.toString();
	}

	public static enum JSchEventType {
		Upload, Download, DeleteFile, Connected, Disconnected
	}

}
