/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

/**
    @author Jeff Jones
 */
public enum ActionEnum {

	/**
	 * Ordinal 0
	 */
    CREATE_ACCOUNT,

    /**
	 * Ordinal 1
	 */
    UPDATE_ACCOUNT,

    /**
	 * Ordinal 2
	 */
    MIGRATE_ACCOUNT,

    /**
	 * Ordinal 3
	 */
    ACTIVATE_FEATURE,

    /**
	 * Ordinal 4
	 */
    QUERY_ACCT,

    /**
	 * Ordinal 5
	 */
    VALID_BANK,

    /**
	 * Ordinal 6
	 */
    CREATE_PIN,

    /**
	 * Ordinal 7
	 */
    UPDATE_BANK,

    /**
	 * Ordinal 8
	 */
    UPDATE_PIN,

    /**
	 * Ordinal 9
	 */
    AUTHENTICATE_PIN,

    /**
	 * Ordinal 10
	 */
    QUERY_OFFER,

    /**
	 * Ordinal 11
	 */
    GET_MONTHLY_FEE,

    /**
	 * Ordinal 12
	 */
    DEBUG_SET_FEATURE_STATUS,

    /**
	 * Ordinal 13
	 */
    PSP_MIGRATE_ACCOUNT,

    /**
	 * Ordinal 14
	 */
    RESET_PIN
}
