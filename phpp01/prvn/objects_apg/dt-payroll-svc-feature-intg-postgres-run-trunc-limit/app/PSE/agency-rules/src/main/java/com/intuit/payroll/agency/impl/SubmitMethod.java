//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.api.ISubmitMethod;
import com.intuit.payroll.agency.api.IPaymentReason;
import com.intuit.payroll.agency.api.IRulesList;
import com.intuit.payroll.agency.api.ReleaseStatus;
import com.intuit.payroll.agency.api.IFormSubmitMethod;
import com.intuit.payroll.agency.api.DueDateSpecifier;
import com.intuit.payroll.agency.calculator.SettlementDateCalculator;
/// <summary>
/// </summary>
public abstract class SubmitMethod implements ISubmitMethod, IFormSubmitMethod {

    private String m_submitMethodType;
    private boolean m_isObsolete = false;
    private boolean m_isValid = true;
    private String m_description;
    private String m_templateID;
    private RulesCalendar m_TimeDue;
    private int m_sendByOffset;
    private boolean m_isNegativeLineItemAllowed = false;
    private String m_defaultPaymentReasonCode;
    private boolean m_paymentsRequireBankAccountInfo = false;
    private String m_enrollmentGroupID;
    protected String m_settlementDescription;
    private boolean m_todayExplicitlySet = false;
    private RulesCalendar m_today;
    protected SettlementDatePolicy m_settlementDatePolicy = new SettlementDatePolicy();
    private ReleaseStatus m_releaseStatus = ReleaseStatus.Obsolete; // If not overridden, default is Obsolete
    private DueDateSpecifier m_howIsDueDateDefined = null;




    /// <summary>
    /// the "today" used by the settlement date functions.  defaults to
    /// RulesCalendar.Today.  Ordinarilly only test clients set this property explicitly.
    /// </summary>
    public RulesCalendar 	getToday()
    {
        if (m_todayExplicitlySet)
            return m_today;
        else
            return RulesCalendar.createCalendar(); // default to now
        // disastrous results if we cache today's date and the
        // client keeps an access for multiple days.
    }
    public void setToday(RulesCalendar that)
    {
        m_today = that;
        m_todayExplicitlySet = true;
    }

    /// <summary>
    /// Submit method type {enumerated String: PRINT, EFE-EPAY, OSP-WIRE, OSP-ACH}.
    /// </summary>
    public String getSubmitMethodType()
    {
        return m_submitMethodType;
    }
    public void setSubmitMethodType(String that)
    {
        m_submitMethodType = that;
    }

    /// <summary>
    /// True if this PaymentTemplate is obsolete.
    /// </summary>
    public boolean getIsObsolete()
    {
        return m_isObsolete;
    }
    public void setIsObsolete(boolean that)
    {
        m_isObsolete = that;
    }

    public void setIsObsolete(String that)
    {
        m_isObsolete = PaymentTemplate.valueAsBoolean(that);
    }

    /// <summary>
    /// Read-only property that indicates if this object is valid or not.
    /// If IsValid is true, it means that this object contains valid data.
    /// If IsValid is false, it means that this object was not initialized.
    /// </summary>
    public boolean getIsValid()
    {
        return m_isValid;
    }
    public void setIsValid(boolean that)
    {
        m_isValid = that;
    }

    /// <summary>
    /// Submit method description String for UI.
    /// </summary>
    public String getDescription()
    {
        return m_description;
    }
    public void setDescription(String that)
    {
        m_description = that;
    }

    /// <summary>
    /// The id of enrollment group for this submit method.
    /// </summary>
    /// <remarks> Used to lookup enrollment group information elsewhere.</remarks>
    public String getEnrollmentGroupID()
    {
        return m_enrollmentGroupID;
    }
    public void setEnrollmentGroupID(String that)
    {
        m_enrollmentGroupID = that;
    }

    /// <summary>
    /// Payment template id that owns this submit method.
    /// </summary>
    public String getPaymentTemplateID()
    {
        return m_templateID;
    }
    public void setTemplateID(String that)
    {
        m_templateID = that;
    }

    /// <summary>
    /// Submit time due.
    /// </summary>
    public RulesCalendar getTimeDue()
    {
        return m_TimeDue;
    }
    public void setTimeDue(RulesCalendar that)
    {
        m_TimeDue = that;
    }

    /// <summary>
    /// Number of days to bump the agency due date in order to get the agency send by date.
    /// </summary>
    /// <remarks>
    /// If missing, default to 0 (agency send by date is same as agency due date).
    /// For example, EFTPS should be "-1", meaning that the agency send by date is the day before the agency due date.
    /// Note that this is independent of the padding required by EFE.
    /// This field is for an actual agency requirement, on the agency servers.
    /// </remarks>
    public int getSendByOffset()
    {
        return m_sendByOffset;
    }
    public void setSendByOffset(int that)
    {
        m_sendByOffset = that;
    }

    /// <summary>
    /// Allow negative line items?
    /// </summary>
    public boolean getIsNegativeLineItemAllowed()
    {
        return m_isNegativeLineItemAllowed;
    }
    public void setIsNegativeLineItemAllowed(boolean that)
    {
        m_isNegativeLineItemAllowed = that;
    }
    public void setIsNegativeLineItemAllowed(String xml)
    {
        m_isNegativeLineItemAllowed = PaymentTemplate.valueAsBoolean(xml);
    }

    /// <summary>
    /// Default payment reason code for this submit method.
    /// </summary>
    public String getDefaultPaymentReasonCode()
    {
        return m_defaultPaymentReasonCode;
    }
    public void setDefaultPaymentReasonCode(String that)
    {
        m_defaultPaymentReasonCode = that;
    }

    /// <summary>
    /// does each payment require bank account number and routing number?
    /// false means agency already know via enrollment.
    /// </summary>
    public boolean getPaymentsRequireBankAccountInfo()
    {
        return m_paymentsRequireBankAccountInfo;
    }
    public void setPaymentsRequireBankAccountInfo(boolean that)
    {
        m_paymentsRequireBankAccountInfo = that;
    }
    public void setPaymentsRequireBankAccountInfo(String that)
    {
        m_paymentsRequireBankAccountInfo= PaymentTemplate.valueAsBoolean(that);
    }


    /// <summary>
    /// text description of the settlement rule for the user
    /// </summary>
    public String getSettlementDescription ()
    {
        return fixUpSettlementDateDescription(m_settlementDescription);
    }

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	public String getSettlementDescription(RulesCalendar today)
    {
        return fixUpSettlementDateDescription(m_settlementDescription, today);
    }


    public void setSettlementDescription (String that)
    {
        m_settlementDescription = that;
    }




    /// <summary>
    /// Interface to get a payment reason object, given a payment reason code
    /// </summary>
    /// <returns>A list of payment reason code that is associated to this submit method.</returns>
    public abstract IRulesList getActivePaymentReasonCodeList();
//    {
//		return DAOFactory.getDAOFactory().getPaymentReasonDAO().getActivePaymentReasonCodeList(m_paymentTemplateID, m_submitMethodType);
//    }

    /// <summary>
    /// Interface to get a payment reason object, given a payment reason code.
    /// </summary>
    /// <param name="code">String that identify an unique payment reason</param>
    /// <returns>the payment reason object</returns>
    public abstract IPaymentReason getPaymentReason(String code);
//    {
//        return DAOFactory.getDAOFactory().getPaymentReasonDAO().getPaymentReason(m_paymentTemplateID, m_submitMethodType, code);
//    }

    /// <summary>
    /// get a list of tax ids for which it is ok to send negatives.
    /// </summary>
    /// <returns>An IRulesList of integer tax IDs.</returns>
    /// <remarks>The method is virtual so that test implementations
    /// can subclass SubmitMethod and provide their own implementations.</remarks>
    /// <example>
    /// It's ok for AEIC to be negative.
    /// </example>
    public abstract IRulesList getTaxIDToAllowNegativesList();
//    {
//        return DAOFactory.getDAOFactory().getSubmitMethodDAO().getTaxIDToAllowNegativesList(m_paymentTemplateID, m_submitMethodType);
//    }

    /// <summary>
    /// Validate a potential settlement date based on the
    /// rules in the submit method.
    /// </summary>
    /// <param name="date">The date to validate.</param>
    /// <returns>
    /// True if the date is valid, false otherwise.
    /// </returns>
    public boolean validateSettlementDate(RulesCalendar date)
    {
        return SettlementDateCalculator.getInstance().validateSettlementDate(date, getToday(), getPaymentTemplateID(), getSettlementDatePolicy());
    }

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	public boolean validateSettlementDate(RulesCalendar date, RulesCalendar today)
	{
        return SettlementDateCalculator.getInstance().validateSettlementDate(date, today, getPaymentTemplateID(), getSettlementDatePolicy());
	}


    /// <summary>
    /// get the default settlement date based on
    /// the SettlementDefaultOffset.
    /// </summary>
    /// <returns>
    /// A RulesCalendar that is the default settlement
    /// date based on today's date and the number
    /// of default offset days in the rules data.
    /// </returns>
    public RulesCalendar getDefaultSettlementDate()
    {
        return SettlementDateCalculator.getInstance().getDefaultSettlementDate(getToday(), getPaymentTemplateID(), getSettlementDatePolicy());
    }

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	public RulesCalendar getDefaultSettlementDate (RulesCalendar today)
    {
        return SettlementDateCalculator.getInstance().getDefaultSettlementDate(today, getPaymentTemplateID(), getSettlementDatePolicy());
    }

    /// <summary>
    /// The status of the item with regard to release level.
    /// </summary>
    public ReleaseStatus getReleaseStatus()
    {
        return m_releaseStatus;
    }
    public void setReleaseStatus(ReleaseStatus that)
    {
        m_releaseStatus = that;
    }
    public void setReleaseStatus(String that)
    {
        m_releaseStatus = ReleaseStatus.MapReleaseStatus (that);
    }
    public DueDateSpecifier getHowIsDueDateDefined()
    {
        return m_howIsDueDateDefined;
    }
    public void setHowIsDueDateDefined(String that)
    {
        m_howIsDueDateDefined = DueDateSpecifier.MapDueDateSpecifier(that);
    }

    /// <summary>
    /// Sometimes a valid settlement date does not exist because of agency rules
    /// and EFE lag time.  Call this first to get fair warning.
    /// </summary>
    /// <returns>
    /// True if a valid settlement date exists.
    /// </returns>
    public boolean validSettlementDateExists()
    {
        return SettlementDateCalculator.getInstance().validSettlementDateExists(getToday(), getPaymentTemplateID(), getSettlementDatePolicy());
    }

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
    public boolean validSettlementDateExists(RulesCalendar today)
    {
        return SettlementDateCalculator.getInstance().validSettlementDateExists(today, getPaymentTemplateID(), getSettlementDatePolicy());
    }



    /// <summary>
    /// The Policy object that has the rules regarding settlement dates configured.
    /// </summary>
    public SettlementDatePolicy getSettlementDatePolicy()
    {
        return this.m_settlementDatePolicy;
    }
    public void setSettlementDatePolicy(SettlementDatePolicy that)
    {
        m_settlementDatePolicy = that;
    }

    private String fixUpSettlementDateDescription(String description)
    {
        return SettlementDateCalculator.getInstance().fixUpSettlementDescription(description, getToday(), getPaymentTemplateID(), getSettlementDatePolicy());
    }

    private String fixUpSettlementDateDescription(String description, RulesCalendar today)
    {
        return SettlementDateCalculator.getInstance().fixUpSettlementDescription(description, today, getPaymentTemplateID(), getSettlementDatePolicy());
    }



}
/// Interface for the SubmitMethod object. This interface will be exposed through COM.
