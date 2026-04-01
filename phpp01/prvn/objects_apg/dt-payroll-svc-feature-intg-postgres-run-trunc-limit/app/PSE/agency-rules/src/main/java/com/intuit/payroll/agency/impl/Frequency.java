//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IFormFilingFrequency;

import java.util.ArrayList;
/// <summary>
/// Implementation of the Frequency interface.

/// </summary>
public class Frequency implements IPaymentFrequency, IFormFilingFrequency {
    private String m_frequencyID = "";
    private boolean m_isObsolete = false;
    private boolean m_isValid = true;
    private String m_name = "";
    private String m_description = "";
    private String m_templateID = "";
    private boolean m_disallowYearCrossing = false;
    private boolean m_disallowQuarterCrossing = false;
    private boolean m_disallowMonthCrossing = false;
    private boolean m_addHolidayAllowanceToDueDate = false;
    private boolean m_zeroPaymentRequired = false;
    //private ArrayList m_upperLimits;

    /// <summary>
    /// Payment frequency id. It should be unique to the PaymentTemplate that owns this Frequency, not necessarily globally unique.
    /// </summary>

    public String getPaymentFrequencyID() {
        return m_frequencyID;
    }

    public String getFormFrequencyID() {
        return m_frequencyID;
    }

    public void setFrequencyID(String that) {
        m_frequencyID = that;
    }

    /// <summary>
    /// True if this Frequency is obsolete.
    /// </summary>
    public boolean getIsObsolete() {
        return m_isObsolete;
    }

    public void setIsObsolete(boolean that) {
        m_isObsolete = that;
    }

    public void setIsObsolete(String that) {
        m_isObsolete = PaymentTemplate.valueAsBoolean(that);
    }

    /// <summary>
    /// Read-only property that indicates if this object is valid or not.
    /// If IsValid is true, it means that this object contains valid data.
    /// If IsValid is false, it means that this object was not initialized.
    /// </summary>
    public boolean getIsValid() {
        return m_isValid;
    }

    public void setIsValid(boolean that) {
        m_isValid = that;
    }

    /// <summary>
    /// Short text blurb for the user, e.g. "Monthly".
    /// </summary>
    public String getName() {
        return m_name;
    }

    public void setName(String that) {
        m_name = that;
    }

    public String getShortDescription() {
        return m_name;
    }

    public void getShortDescription(String that) {
        m_name = that;
    }

    /// <summary>
    /// Long description for the user, e.g. "Payments for each month are due on the 10th of the following month.".
    /// </summary>
    public String getDescription() {
        return m_description;
    }

    public void setDescription(String that) {
        m_description = that;
    }

    public String getLongDescription() {
        return m_description;
    }

    public void setLongDescription(String that) {
        m_description = that;
    }


    /// <summary>
    /// ID of the payment template that owns this payment frequency.
    /// </summary>
    public String getPaymentTemplateID() {
        return m_templateID;
    }

    public void setTemplateID(String that) {
        m_templateID = that;
    }

    /// <summary>
    /// Indicates if you can span a Quarter boundary.
    /// </summary>
    public boolean getDisallowMonthCrossing() {
        return m_disallowMonthCrossing;
    }

    public void setDisallowMonthCrossing(boolean that) {
        m_disallowMonthCrossing = that;
    }

    /// <summary>
    /// Indicates if you can span a Quarter boundary.
    /// </summary>
    public boolean getDisallowQuarterCrossing() {
        return m_disallowQuarterCrossing;
    }

    public void setDisallowQuarterCrossing(boolean that) {
        m_disallowQuarterCrossing = that;
    }

    /// <summary>
    /// Indicates if you can span a Year boundary.
    /// </summary>
    public boolean getDisallowYearCrossing() {
        return m_disallowYearCrossing;
    }

    public void setDisallowYearCrossing(boolean that) {
        m_disallowYearCrossing = that;
    }

    /// <summary>
    /// Indicates if Holidays affect the DueDate
    /// </summary>
    public boolean getAddHolidayAllowanceToDueDate() {
        return m_addHolidayAllowanceToDueDate;
    }


    public void setAddHolidayAllowanceToDueDate(boolean that) {
        m_addHolidayAllowanceToDueDate = that;
    }

//    public ArrayList<UpperLimit> getUpperLimits() {
//        return m_upperLimits;
//    }


    public boolean isZeroPaymentRequired() {
        return m_zeroPaymentRequired;
    }

    public void setZeroPaymentRequired(boolean m_zeroPaymentRequired) {
        this.m_zeroPaymentRequired = m_zeroPaymentRequired;
    }
}
