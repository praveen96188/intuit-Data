/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */


import com.intuit.sbd.payroll.psp.ach.code.*;

/**
 * Union of all ACH code values
 */
public class Codes
	implements ServiceClassCodes, StandardEntryClassCodes,
			TransactionCodes, AddendaTypeCodes,
			RecordTypeCodes, OriginatorStatusCodes,
			ReturnCodes
{
	/**
	 * Helper methods
	 */
	public static boolean isDebit (String transCode)
	{
		if (transCode == null)
			return false;
	
		return (transCode.equals (TransactionCodes.TRANSACTION_DDR_PAYMENT)
			|| transCode.equals (TransactionCodes.TRANSACTION_DDR_RETURN)
			|| transCode.equals (TransactionCodes.TRANSACTION_DDR_PRENOTIFICATION)
			|| transCode.equals (TransactionCodes.TRANSACTION_SDR_PAYMENT)
			|| transCode.equals (TransactionCodes.TRANSACTION_SDR_RETURN)
			|| transCode.equals (TransactionCodes.TRANSACTION_SDR_PRENOTIFICATION)
			);
	}
	
	public static boolean isCredit (String transCode)
	{
		if (transCode == null)
			return false;

		return (transCode.equals (TransactionCodes.TRANSACTION_DCR_DEPOSIT)
				|| transCode.equals (TransactionCodes.TRANSACTION_DCR_RETURN)
				|| transCode.equals (TransactionCodes.TRANSACTION_DCR_PRENOTIFICATION)
				|| transCode.equals (TransactionCodes.TRANSACTION_SCR_DEPOSIT)
				|| transCode.equals (TransactionCodes.TRANSACTION_SCR_RETURN)
				|| transCode.equals (TransactionCodes.TRANSACTION_SCR_PRENOTIFICATION)
				);
	}
	
	public static boolean isReturn (String transCode)
	{
		if (transCode == null)
			return false;

		return (transCode.equals (TransactionCodes.TRANSACTION_DCR_RETURN)
				|| transCode.equals (TransactionCodes.TRANSACTION_DDR_RETURN)
				|| transCode.equals (TransactionCodes.TRANSACTION_SCR_RETURN)
				|| transCode.equals (TransactionCodes.TRANSACTION_SDR_RETURN)
				);
	}

	// Transaction Code lists
	public static String getDebitList () {
		return "'" + String.valueOf (TransactionCodes.TRANSACTION_DDR_PAYMENT)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_DDR_PRENOTIFICATION)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_SDR_PAYMENT)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_SDR_PRENOTIFICATION) + "'";
	}	

	public static String getCreditList () {
		return "'" + String.valueOf (TransactionCodes.TRANSACTION_DCR_DEPOSIT)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_DCR_PRENOTIFICATION)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_SCR_DEPOSIT)
				+ "', '" + String.valueOf (TransactionCodes.TRANSACTION_SCR_PRENOTIFICATION) + "'";
	}
}
