//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IPaymentReason;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.payroll.agency.impl.PaymentReason;
import com.intuit.payroll.agency.impl.SubmitMethod;
import java.util.ArrayList;
/// <summary>
/// </summary>
public class SubmitMethodData extends SubmitMethod {

	private ArrayList<PaymentReason> paymentReasons = new ArrayList<PaymentReason>();
    private IRulesList activePaymentReasonCodes;
	private ArrayList<Integer> negativeOKForTaxIDs = new ArrayList<Integer>();

    public void addPaymentReason(PaymentReason paymentReason)
    {
        paymentReasons.add(paymentReason);
    }

    public void addNegativeOKForTaxID(String xml)
    {
        Integer val = Integer.valueOf(xml);
        negativeOKForTaxIDs.add(val);
    }

    /// <summary>
    /// Interface to get a payment reason object, given a payment reason code
    /// </summary>
    /// <returns>A list of payment reason code that is associated to this submit method.</returns>
    public IRulesList getActivePaymentReasonCodeList()
    {
        if (activePaymentReasonCodes == null) {
            activePaymentReasonCodes = RulesObjectBroker.getInstance().createRulesList(null);
            for (PaymentReason reason : paymentReasons) {
                if (!reason.getIsObsolete()) {
                    activePaymentReasonCodes.add(reason.getPaymentReasonCode());
                }
            }
        }
        return activePaymentReasonCodes;
    }

    /// <summary>
    /// Interface to get a payment reason object, given a payment reason code.
    /// </summary>
    /// <param name="code">String that identify an unique payment reason</param>
    /// <returns>the payment reason object</returns>
    public IPaymentReason getPaymentReason(String code)
    {
        for (PaymentReason reason : paymentReasons) {
            if (reason.getPaymentReasonCode().equals(code)) {
                return reason;
            }
        }
        return null;
    }

    /// <summary>
    /// get a list of tax ids for which it is ok to send negatives.
    /// </summary>
    /// <returns>An IRulesList of integer tax IDs.</returns>
    /// <remarks>The method is virtual so that test implementations
    /// can subclass SubmitMethod and provide their own implementations.</remarks>
    /// <example>
    /// It's ok for AEIC to be negative.
    /// </example>
    public IRulesList getTaxIDToAllowNegativesList()
    {
        return RulesObjectBroker.getInstance().createRulesList(negativeOKForTaxIDs);
    }

    public void addSettlementData(SettlementData settlementData)
    {
        m_settlementDescription = settlementData.uiString;
        m_settlementDatePolicy.setSettlementThisMonth(settlementData.thisMonth);
        m_settlementDatePolicy.setSettlementThisQuarterOrFirstMonthOfNextQuarter(settlementData.thisQuarterOrFirstMonthOfNextQuarter);

        String maxOffsetString = settlementData.maxOffset;
        int startP = maxOffsetString.indexOf('P');
        char maxUnit = maxOffsetString.charAt(maxOffsetString.length()-1);
        m_settlementDatePolicy.setSettlementMaxOffsetUnit(maxUnit);
        if (startP == 0 && (maxUnit == 'D' || maxUnit == 'M'))
        {
            String numStr = maxOffsetString.substring (1, maxOffsetString.length()-1);
            m_settlementDatePolicy.setSettlementMaxOffset(Integer.parseInt(numStr));
        }
        else
        {
            throw new RuntimeException ("bad duration in the settlement date offset xml");
        }

        String minOffsetString = settlementData.minOffset;
        startP = minOffsetString.indexOf('P');
        char minUnit = minOffsetString.charAt(minOffsetString.length()-1);
        m_settlementDatePolicy.setSettlementMinOffsetUnit(minUnit);
        if (startP == 0 && (minUnit == 'D' || minUnit == 'M'))
        {
            String numStr = minOffsetString.substring (1, minOffsetString.length()-1);
            m_settlementDatePolicy.setSettlementMinOffset(Integer.parseInt(numStr));
        }
        else
        {
            throw new RuntimeException ("bad duration in the settlement date offset xml");
        }

    }
}
