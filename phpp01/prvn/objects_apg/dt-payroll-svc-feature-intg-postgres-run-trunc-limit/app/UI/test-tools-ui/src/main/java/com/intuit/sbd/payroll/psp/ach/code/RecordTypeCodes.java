/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.code;

/**
 *
 * @author shivanandad069
 */

/**
 * Union of all ACH code values
 */
public interface RecordTypeCodes
{
	// Record Type Codes
	public static final String RECORD_FILE_HEADER = "1";
	public static final String RECORD_BATCH_HEADER = "5";
	public static final String RECORD_ENTRY_DETAIL = "6";
	public static final String RECORD_ADDENDA = "7";
	public static final String RECORD_BATCH_CONTROL = "8";
	public static final String RECORD_FILE_CONTROL = "9";
	
}