/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/common/DDProcessesToDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;

/**
 *
 * User: rkrishna
 * Date: Dec 12, 2007
 * Time: 9:46:59 AM

 */
public class DDProcessesToDTO {
    /**
     * Function to convert the DTO SettlementType into Domain SettlementType
     * @param pSettlementType  DTO Settlement type
     * @return retSettlementType Domain Settlement type
     */
    public static SettlementType getDomainSettlementType(SettlementTypeDTO pSettlementType) {
        SettlementType retSettlementType = null;
        if (pSettlementType != null) {
            if (pSettlementType.equals(SettlementTypeDTO.ACH)) {
                retSettlementType = SettlementType.ACH;
            } else if (pSettlementType.equals(SettlementTypeDTO.Wire)) {
                retSettlementType = SettlementType.Wire;
            } else if (pSettlementType.equals(SettlementTypeDTO.Cash)) {
                retSettlementType = SettlementType.Cash;
            } else if (pSettlementType.equals(SettlementTypeDTO.CheckType)) {
                retSettlementType = SettlementType.CheckType;
            } else if (pSettlementType.equals(SettlementTypeDTO.Other)) {
                retSettlementType = SettlementType.Other;
            }
        }

        return retSettlementType;
    }    
}
