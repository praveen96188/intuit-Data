/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/SettlementTypeDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * @author Wiktor Kozlik
 */
public enum SettlementTypeDTO {

	/**
	 * Ordinal 0
	 */
	ACH,

	/**
	 * Ordinal 1
	 */
	Wire,

	/**
	 * Ordinal 2
	 */
	Cash,

	/**
	 * Ordinal 3
	 */
	CheckType,

	/**
	 * Ordinal 4
	 */
	Other;

    public static SettlementTypeDTO translateSPSSettlementType(String pSPSSettlementTypeCode) {
        if ("ACH".equalsIgnoreCase(pSPSSettlementTypeCode)) {
            return ACH;
        } else if ("WIRE".equalsIgnoreCase(pSPSSettlementTypeCode)) {
            return Wire;
        } else if ("CASH".equalsIgnoreCase(pSPSSettlementTypeCode)) {
            return Cash;
        } else if ("CHECK".equalsIgnoreCase(pSPSSettlementTypeCode)) {
            return CheckType;
        } else if ("OTHER".equalsIgnoreCase(pSPSSettlementTypeCode)) {
            return Other;
        }

        return null;
    }
}
