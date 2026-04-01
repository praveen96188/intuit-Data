/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */
public interface RecordId
{
	public static final int FILE_HEADER = 1;
	public static final int FILE_CONTROL = 2;
	public static final int BATCH_HEADER = 3;
	public static final int BATCH_CONTROL = 4;
	public static final int PPD_ENTRY_DETAIL = 5;
	public static final int CCD_ENTRY_DETAIL = 6;
	public static final int ADDENDA = 7; // shared
	public static final int COR_ENTRY_DETAIL = 8;
	public static final int COR_ADDENDA = 9;
	public static final int RETURN_ADDENDA = 10;
}
