
package com.intuit.payroll.agency.dao.mnemonics;

/// <summary>
/// DueDatePolicy encapsulates policies as to how to calculate a due date from a period end.
/// </summary>
/// <remarks>
/// This typically comes from mnemonic encoding in the rules data.
/// </remarks>
public class DueDatePolicy
{
    private boolean m_adjustRelativeToPeriodEnd;
    private int m_daysAdjustment;
    private boolean m_businessDaysAdjustment;

    public DueDatePolicy(boolean adjustRelativeToPeriodEnd, int daysAdjustment, boolean businessDaysAdjustment)
    {
        m_adjustRelativeToPeriodEnd = adjustRelativeToPeriodEnd;
        m_businessDaysAdjustment = businessDaysAdjustment;
        m_daysAdjustment = daysAdjustment;
    }

    /// <summary>
    /// True if the due date adjustment is relative to the end date.  Otherwise, look for
    /// a hard date (a day or an end of month/year type deal).
    /// </summary>
    public boolean getAdjustRelativeToPeriodEnd()
    {
        return m_adjustRelativeToPeriodEnd;
    }

    /// <summary>
    /// True if the due date adjustment is relative to the end date.  Otherwise, look for
    /// a hard date (a day or an end of month/year type deal).
    /// </summary>
    public void setAdjustRelativeToPeriodEnd(boolean that)
    {
        m_adjustRelativeToPeriodEnd = that;
    }

    /// <summary>
    /// If we have to adjust relative to the end of the period, use this value.
    /// Use in coordination with the IsBusinessDaysAdjustment.
    /// </summary>
    public int getDaysAdjustment()
    {
        return m_daysAdjustment;
    }

    /// <summary>
    /// If we have to adjust relative to the end of the period, use this value.
    /// Use in coordination with the IsBusinessDaysAdjustment.
    /// </summary>
    public void setDaysAdjustment(int that)
    {
        m_daysAdjustment = that;
    }

    /// <summary>
    /// True if the DaysAdjustment should observe business days.
    /// </summary>
    public boolean getIsBusinessDaysAdjustment()
    {
        return m_businessDaysAdjustment;
    }

    /// <summary>
    /// True if the DaysAdjustment should observe business days.
    /// </summary>
    public void setIsBusinessDaysAdjustment(boolean that)
    {
        m_businessDaysAdjustment = that;
    }
}
