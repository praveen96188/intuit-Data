/**
* FieldId.java
* 
* Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
* 
* This software is the confidential and proprietary information of
* PayCycle, Inc. ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with PayCycle.
* 
* PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
* SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
* 
* CopyrightVersion 1.0
*/

package com.paycycle.eft;

/**
 * The Electronic Fund Transfer Field IDs.
 *
// * Here are some of the abbreviations used:
 * TIN     - Taxpayer Identification Number (EIN or SSN)
 */
public interface FieldId
{
	public static final int REFERENCE_NUMBER = 100;
	public static final int TAXPAYER_EIN = 101;
	public static final int TAXPAYER_PIN = 102;
	public static final int TAX_TYPE_CODE = 103;
	public static final int PAYROLL_DATE = 104;
	public static final int PAYMENT_AMT_1 = 105;
	public static final int PAYMENT_AMT_2 = 106;
	public static final int PAYMENT_AMT_TOTAL = 107;
	public static final int SETTLEMENT_DATE = 108;
	public static final int AUTHORITY_CODE = 109;
	public static final int RECORD_SEPARATOR = 110;
	public static final int STATE_ABBR = 111;
	public static final int VERIFICATION_CODE = 112;
	public static final int TRANSMISSION_DATE = 113;
    public static final int SUI_RATE = 114;
	
	public static final int DEFAULT_VALUE_SPACE_1 = 121;
	public static final int DEFAULT_VALUE_SPACE_2 = 122;
	public static final int DEFAULT_VALUE_SPACE_3 = 123;
	public static final int DEFAULT_VALUE_SPACE_4 = 124;
	public static final int DEFAULT_VALUE_SPACE_5 = 125;
	public static final int DEFAULT_VALUE_SPACE_6 = 126;
	public static final int DEFAULT_VALUE_SPACE_7 = 127;
	public static final int DEFAULT_VALUE_SPACE_8 = 128;
	public static final int DEFAULT_VALUE_SPACE_9 = 129;
	public static final int DEFAULT_VALUE_SPACE_10 = 130;
	public static final int DEFAULT_VALUE_SPACE_11 = 131;
    public static final int DEFAULT_VALUE_SPACE_12 = 132;
    public static final int DEFAULT_VALUE_SPACE_13 = 133;
    public static final int DEFAULT_VALUE_SPACE_14 = 134;
    public static final int DEFAULT_VALUE_SPACE_15 = 135;
    public static final int DEFAULT_VALUE_SPACE_16 = 136;
    public static final int DEFAULT_VALUE_SPACE_17 = 137;
    public static final int DEFAULT_VALUE_SPACE_18 = 138;

	public static final int DEFAULT_VALUE_NUM_1 = 141;
	public static final int DEFAULT_VALUE_NUM_2 = 142;
	public static final int DEFAULT_VALUE_NUM_3 = 143;
	public static final int DEFAULT_VALUE_NUM_4 = 144;
	public static final int DEFAULT_VALUE_NUM_5 = 145;
	public static final int DEFAULT_VALUE_NUM_6 = 146;
	
	public static final int STATUS_CODE = 200;
	 
	
	
/*********** More IDs for EFT by ACH Credit *********/
	public static final int SEGMENT_IDENTIFIER = 200;
	public static final int TAX_PERIOD_END_DATE = 201;
	public static final int TAX_AMOUNT = 202;
	public static final int PENALTY_AMOUNT = 203;
	public static final int INTEREST_AMOUNT = 204;
	public static final int TAX_AMOUNT_TYPE = 205;
	public static final int PENALTY_AMOUNT_TYPE = 206;
	public static final int INTEREST_AMOUNT_TYPE = 207;
	public static final int NAME_CONTROL = 208;
	public static final int TERMINATOR = 209;
	public static final int EIN_PREFIX = 210;
	public static final int OTHER_TAX_AMOUNT1 = 211;
	public static final int OTHER_TAX_AMOUNT2 = 212;
	public static final int OTHER_TAX_AMOUNT1_TYPE = 213;
	public static final int OTHER_TAX_AMOUNT2_TYPE = 214;
    public static final int TAX_PERIOD_START_DATE = 215;
	
	public static final int FILING_NAME = 215;
	public static final int TAXPAYER_FEIN = 216;
	public static final int TAXPAYER_EIN2 = 217;
	public static final int RECORD_SEQUENCE = 218;
	public static final int TAXPAYER_CONTACT = 219;
	
	public static final int TAX_TYPE = 220;
	public static final int PAYMENT_OPTION = 221;
    public static final int CREATED_DATE = 222;
	
	/*********** IL Enrollment IDs *********/
	public static final int PROV_ID = 230;
	public static final int DBA_NAME = 231;
	public static final int BUS_ADDR1 = 232;
	public static final int BUS_ADDR2 = 233;
	public static final int BUS_CITY = 234;
	public static final int BUS_STATE = 235;
	public static final int BUS_ZIP = 236;
	public static final int CNTACTF = 237;
	public static final int CNTACTL = 238;
	public static final int PHONE = 239;
	public static final int EXT = 240;
	public static final int CNTACTF2 = 241;
	public static final int CNTACTL2 = 242;
	public static final int PHONE2 = 243;
	public static final int EXT2 = 244;
	public static final int FAX = 245;
	public static final int EMAIL = 246;
	public static final int SEQ_NO = 247;
	public static final int CHK_DGT = 248;
	public static final int IBT = 249;
	public static final int POST = 250;
	public static final int FORM = 251;
	public static final int DEB_CRE = 252;
	public static final int MAN_VOL = 253;
	public static final int REG_IND = 254;
	public static final int ENROL_DAT = 255;
	public static final int NDBA_NAM = 256;
	public static final int NBUS_ADD1 = 257;
	public static final int NBUS_ADD2 = 258;
	public static final int NBUS_CITY = 259;
	public static final int NBUS_ST = 260;
	public static final int NBUS_ZIP = 261;
	public static final int NFEIN = 262;
	public static final int NIBT = 263;
	public static final int ACTION_ID = 264;

	/*********** PA specific enrollment ids *********/
	public static final int EIN_TYPE = 2200;
	public static final int ACH_DEBIT_ACCOUNT_NUMBER = 2201;
	public static final int PAYMENT_METHOD = 2202;
	public static final int BANK_ROUTING_NUMBER = 2203;
	public static final int BANK_ACCOUNT_TYPE = 2204;
	public static final int BANK_NAME = 2205;
	public static final int BANK_CITY = 2206;
	public static final int BANK_STATE = 2207;
	public static final int BANK_ZIPCODE = 2208;

    /*********** MA specific enrollment ids *********/
	public static final int DISCRETIONARY_DATA = 3200;

	/*********** MO specific enrollment ids *********/
	public static final int FILING_FREQUENCY = 3600;
	
	/*********** IA specific enrollment ids *********/
	public static final int TAXPAYER_IAWPN = 4100;

	/*********** NV specific enrollment ids *********/
	public static final int DISCRETIONARY_DATA_1 = 4400;
	public static final int DISCRETIONARY_DATA_2 = 4401;
	public static final int DISCRETIONARY_DATA_3 = 4402;
	
	/*********** OK specific eft/efile ids *********/
	public static final int OK_EFILE_INDICATOR = 4500;
	public static final int OK_WAGES_PAID= 4501;
	
	/*********** NE specific eft/efile ids  *********/
	public static final int TAX_YEAR = 4600;
	public static final int TAX_QUARTER = 4601;
	public static final int SUBMITTER_NAME = 4602;	

    /*********** AL specific field ids *********/
    public static final int CONFIRMATION_NUMBER = 4700;

    /*********** HI specific field ids *********/
    public static final int GENERAL_EXCISE_NUMBER = 4800;
    public static final int HAWAII_TAX_ID = 4801;
    public static final int HAWAII_TAX_ID_SUFFIX = 4802;
    public static final int ANNUAL_PERIOD_INDICATOR = 4803;

    /*********** NM specific field ids *********/
    public static final int TAX_PROGRAM = 4900;
    public static final int REPORT_DATE = 4901;

    /*********** PA-UI specific field ids *********/
    public static final int TRANSACTION_ID = 5000;

    /*********** VT-UI specific field ids *********/
    public static final int SERVICE_BUREAU_NUMBER = 5100;
    public static final int TAXPAYER_ID=5101;
    public static final int SECONDARY_TAX_AMOUNT = 5102;

    /*********** GA-DOL-UI specific field ids *********/
    public static final int SERVICE_PROVIDER=5200;
    public static final int FILLER=5201;

	/*********** CO-FAMLI specific field ids *********/
	public static final int PAYOR_FEIN=5300;


}
