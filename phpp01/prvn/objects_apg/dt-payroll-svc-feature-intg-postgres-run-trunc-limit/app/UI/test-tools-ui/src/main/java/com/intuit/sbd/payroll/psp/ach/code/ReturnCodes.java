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
 * Return code values
 */
public interface ReturnCodes
{
	// Return Codes
	public static final String RETURN_INSUFFICIENT_FUNDS = "R01";
	public static final String RETURN_ACCOUNT_CLOSED = "R02";
	public static final String RETURN_NO_ACCOUNT = "R03";
	public static final String RETURN_INVALID_ACCOUNT_NUMBER = "R04";
	public static final String RETURN_NOT_AUTHORIZED_SEC_CODE = "R05";
	public static final String RETURN_RETURNED_PER_ODFI_REQUEST = "R06";
	public static final String RETURN_AUTHORIZATION_REVOKED = "R07";
	public static final String RETURN_PAYMENT_STOPPED = "R08";
	public static final String RETURN_UNCOLLECTED_FUNDS = "R09";
	public static final String RETURN_CUST_ADVISES_NOT_AUTHORIZED = "R10";
	public static final String RETURN_CHECK_TRUNCATION_ENTRY_RETURN = "R11";
	public static final String RETURN_BRANCH_SOLD_TO_ANOTHER_DFI = "R12";
	public static final String RETURN_REPRESENTATIVE_PAYEE_DECEASED = "R14";
	public static final String RETURN_BENEFICIARY_DECEASED = "R15";
	public static final String RETURN_ACCOUNT_FROZEN = "R16";
	public static final String RETURN_FILE_RECORD_EDIT_CRITERIA = "R17";
	public static final String RETURN_NON_TRANSACTION_ACCOUNT = "R20";
	public static final String RETURN_INVALID_COMPANY_IDENTIFICATION = "R21";
	public static final String RETURN_INVALID_INDVIDUAL_ID_NUMBER = "R22";
	public static final String RETURN_CREDIT_ENTRY_REFUSED = "R23";
	public static final String RETURN_DUPLICATE_ENTRY = "R24";
	public static final String RETURN_CORP_CUST_ADVISES_NOT_AUTHORIZED = "R29";
	
	// Change Codes
	public static final String CHANGE_ACCOUNT_NUMBER = "C01";
	public static final String CHANGE_ROUTING_NUMBER = "C02";
	public static final String CHANGE_ROUTING_AND_ACCOUNT_NUMBERS = "C03";
	public static final String CHANGE_ACCOUNT_NAME = "C04";
	public static final String CHANGE_TRANSACTION_CODE = "C05";
	public static final String CHANGE_ACCOUNT_NUMBER_AND_TRANS_CODE = "C06";
	public static final String CHANGE_ROUTING_AND_ACCOUNT_NUMBERS_AND_TRANS_CODE = "C07";
	public static final String CHANGE_INDIVIDUAL_ID_NUMBER = "C09";
	
	// Positions of specific data in Corrected Data field
	public static final int CORRECTED_DATA_C03_ROUTING_START = 0;
	public static final int CORRECTED_DATA_C03_ROUTING_END = 8;
	public static final int CORRECTED_DATA_C03_ACCOUNT_START = 12;
	public static final int CORRECTED_DATA_C03_ACCOUNT_END = 28;
	public static final int CORRECTED_DATA_C06_ACCOUNT_START = 0;
	public static final int CORRECTED_DATA_C06_ACCOUNT_END = 16;
	public static final int CORRECTED_DATA_C06_TRANSACTION_START = 20;
	public static final int CORRECTED_DATA_C06_TRANSACTION_END = 21;
	public static final int CORRECTED_DATA_C07_ROUTING_START = 0;
	public static final int CORRECTED_DATA_C07_ROUTING_END = 8;
	public static final int CORRECTED_DATA_C07_ACCOUNT_START = 9;
	public static final int CORRECTED_DATA_C07_ACCOUNT_END = 25;
	public static final int CORRECTED_DATA_C07_TRANSACTION_START = 26;
	public static final int CORRECTED_DATA_C07_TRANSACTION_END = 27;
}
