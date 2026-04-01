/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */



/**
 * The ACH Field IDs
 */
public interface FieldId
{
	public static final int ADDENDA_RECORD_INDICATOR = 100;
	public static final int ADDENDA_SEQUENCE_NUMBER = 101;
	public static final int ADDENDA_TYPE_CODE = 102;
	public static final int AMOUNT = 103;
	public static final int ADDENDA_INFORMATION = 104;
	
	public static final int BATCH_COUNT = 200;
	public static final int BATCH_NUMBER = 201;
	public static final int BLOCKING_FACTOR = 202;
	public static final int BLOCK_COUNT = 203;
	
	public static final int CHECK_DIGIT = 300;
	public static final int COMPANY_DESCRIPTIVE_DATE = 301;
	public static final int COMPANY_DISCRETIONARY_DATA = 302;
	public static final int COMPANY_ENTRY_DESCRIPTION = 303;
	public static final int COMPANY_IDENTIFICATION = 304;
	public static final int COMPANY_NAME = 305;
	public static final int CHANGE_CODE = 306;
	public static final int CORRECTED_DATA = 307;
	
	public static final int DFI_ACCOUNT_NUMBER = 400;
	public static final int DISCRETIONARY_DATA = 401;
	public static final int DATE_OF_DEATH = 402;
	
	public static final int EFFECTIVE_ENTRY_DATE = 500;
	public static final int ENTRY_ADDENDA_COUNT = 501;
	public static final int ENTRY_DETAIL_SEQUENCE_NUMBER = 502;
	public static final int ENTRY_HASH = 503;
	
	public static final int FILE_CREATION_DATE = 600;
	public static final int FILE_CREATION_TIME = 601;
	public static final int FILE_ID_MODIFIER = 602;
	public static final int FORMAT_CODE = 603;
	
	public static final int IMMEDIATE_DESTINATION = 900;
	public static final int IMMEDIATE_DESTINATION_NAME = 901;
	public static final int IMMEDIATE_ORIGIN = 902;
	public static final int IMMEDIATE_ORIGIN_NAME = 903;
	public static final int INDIVIDUAL_IDENTIFICATION_NUMBER = 904;
	public static final int INDIVIDUAL_NAME = 905;

	public static final int LOGDX_INFORMATION = 1200;
	
	public static final int MESSAGE_AUTHENTICATION_CODE = 1300;
	public static final int MESSAGE_AUTHENTIFICATION_CODE = 1301;
	
	public static final int ORIGINATING_DFI_IDENTIFICATION = 1500;
	public static final int ORIGINATOR_STATUS_CODE = 1501;
	public static final int ORIGINAL_ENTRY_TRACE_NUMBER = 1502;
	public static final int ORIGINAL_RECEIVING_DFI_IDENTIFICATION = 1503;
	
	public static final int PAYMENT_RELATED_INFORMATION = 1600;
	public static final int PRIORITY_CODE = 1601;
	
	public static final int RECEIVING_DFI_IDENTIFICATION = 1800;
	public static final int RECORD_SIZE = 1801;
	public static final int RECORD_TYPE_CODE = 1802;
	public static final int REFERENCE_CODE = 1803;
	public static final int RESERVED = 1804;
	public static final int RETURN_REASON_CODE = 1805;
	public static final int RECORD_SEPARATOR = 1806;

	public static final int SERVICE_CLASS_CODE = 1900;
	public static final int SETTLEMENT_DATE = 1901;
	public static final int STANDARD_ENTRY_CLASS_CODE = 1902;
	
	public static final int TOTAL_CREDIT_ENTRY_DOLLAR_AMOUNT = 2000;
	public static final int TOTAL_CREDIT_ENTRY_DOLLAR_AMOUNT_IN_FILE = 2001;
	public static final int TOTAL_DEBIT_ENTRY_DOLLAR_AMOUNT = 2002;
	public static final int TOTAL_DEBIT_ENTRY_DOLLAR_AMOUNT_IN_FILE = 2003;
	public static final int TRACE_NUMBER = 2004;
	public static final int TRANSACTION_CODE = 2005;
	
}
