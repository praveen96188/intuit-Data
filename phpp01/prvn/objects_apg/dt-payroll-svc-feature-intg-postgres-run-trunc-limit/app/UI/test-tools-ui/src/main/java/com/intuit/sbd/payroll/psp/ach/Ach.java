/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */


import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManager;

/**
 * The base ACH class.
 */
public abstract class Ach extends RecordManager
{
	public static final int RECORD_LENGTH = 94;
	protected AchEnvelope m_envelope;

	/**
	 * Set the parent enclosing envelope.
	 */
	public void setEnvelope (AchEnvelope e) {
		m_envelope = e;
	}	

	/**
	 * Return the parent enclosing envelope.
	 */
	public AchEnvelope getEnvelope () {
		return m_envelope;
	}	
}
