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
 * ACH code values
 */
public interface TransactionCodes
{
	// Transaction Codes
	
	/** Demand Credit Records (for checking, NOW, and share draft accounts) */
	public static final String TRANSACTION_DCR_RESERVED = "20";
	public static final String TRANSACTION_DCR_RETURN = "21";
	public static final String TRANSACTION_DCR_DEPOSIT = "22";
	public static final String TRANSACTION_DCR_PRENOTIFICATION = "23";
	public static final String TRANSACTION_DCR_ZERODOLLAR = "24";

	/** Demand Debit Records (for checking, NOW, and share draft accounts) */
	public static final String TRANSACTION_DDR_RESERVED = "25";
	public static final String TRANSACTION_DDR_RETURN = "26";
	public static final String TRANSACTION_DDR_PAYMENT = "27";
	public static final String TRANSACTION_DDR_PRENOTIFICATION = "28";
	public static final String TRANSACTION_DDR_ZERODOLLAR = "29";

	/** Savings Account Credit Records */
	public static final String TRANSACTION_SCR_RESERVED = "30";
	public static final String TRANSACTION_SCR_RETURN = "31";
	public static final String TRANSACTION_SCR_DEPOSIT = "32";
	public static final String TRANSACTION_SCR_PRENOTIFICATION = "33";
	public static final String TRANSACTION_SCR_ZERODOLLAR = "34";

	/** Account Debit Records */
	public static final String TRANSACTION_SDR_RESERVED = "35";
	public static final String TRANSACTION_SDR_RETURN = "36";
	public static final String TRANSACTION_SDR_PAYMENT = "37";
	public static final String TRANSACTION_SDR_PRENOTIFICATION = "38";
	public static final String TRANSACTION_SDR_ZERODOLLAR = "39";
}

