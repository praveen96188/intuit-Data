/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/common/ProcessesToDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes.common;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * @author Dawn Martens
 * @author Wiktor Kozlik
 */
public class ProcessesToDTO {

    public static PayrollFrequency getDomainPayrollFrequency(PayrollFrequencyDTO pPayrollFrequencyDTO) {
        if (null == pPayrollFrequencyDTO) {
            return null;
        }
        String payrollFrequencyCode = null;
        switch (pPayrollFrequencyDTO) {
            case Daily:
                payrollFrequencyCode = PayrollFrequency.Codes.DAILY_MISC;
                break;
            case Weekly:
                payrollFrequencyCode = PayrollFrequency.Codes.WEEKLY;
                break;
            case BiWeekly:
                payrollFrequencyCode = PayrollFrequency.Codes.BI_WEEKLY;
                break;
            case SemiMonthly:
                payrollFrequencyCode = PayrollFrequency.Codes.SEMI_MONTHLY;
                break;
            case Monthly:
                payrollFrequencyCode = PayrollFrequency.Codes.MONTHLY;
                break;
            case Quarterly:
                payrollFrequencyCode = PayrollFrequency.Codes.QUARTERLY;
                break;
            case SemiAnnual:
                payrollFrequencyCode = PayrollFrequency.Codes.SEMI_ANNUALLY;
                break;
            case Annual:
                payrollFrequencyCode = PayrollFrequency.Codes.ANNUAL;
                break;
        }
        return Application.findById(PayrollFrequency.class, payrollFrequencyCode);
    }

    public static ContactRole getDomainContactRole(ContactRole pContactRole) {
        if (null == pContactRole) {
            return null;
        }
        switch (pContactRole) {
            case Other:
                return ContactRole.Other;
            case PrimaryPrincipal:
                return ContactRole.PrimaryPrincipal;
            case SecondaryPrincipal:
                return ContactRole.SecondaryPrincipal;
            case PayrollAdmin:
                return ContactRole.PayrollAdmin;
            default:
                return null;
        }
    }

    public static Service getDomainService(ServiceCode pServiceCode) {
        Service retService = null;
        if (pServiceCode != null) {
            retService = Application.findById(Service.class, pServiceCode);
        }
        return retService;
    }

    public static TransactionState getDomainTransactionState(TransactionStateDTO pTransactionStateDTO) {
        if (null == pTransactionStateDTO) {
            return null;
        }
        TransactionStateCode transactionStateCode = null;
        switch (pTransactionStateDTO) {
            case Pending:
                transactionStateCode = TransactionStateCode.Created;
                break;
            case Executed:
                transactionStateCode = TransactionStateCode.Executed;
                break;
            case Canceled:
                transactionStateCode = TransactionStateCode.Cancelled;
                break;
            case Returned:
                transactionStateCode = TransactionStateCode.Returned;
                break;
            case Completed:
                transactionStateCode = TransactionStateCode.Completed;
                break;
            case Voided:
                transactionStateCode = TransactionStateCode.Voided;
                break;
        }
        return PayrollServices.entityFinder.findById(TransactionState.class, transactionStateCode);
    }

    /**
     * Function to convert the DTO SettlementType into Domain SettlementType
     *
     * @param pSettlementType DTO Settlement type
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
