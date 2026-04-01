package com.intuit.payroll.agency.impl;
/// <summary>
/// SettlementDatePolicy encapsulates the variables that affect Settlement dates.
/// </summary>
/// <remarks>
/// SubmitMethod has a member of this type which is populated by the DAO.  The
/// SettlementDateCalculator uses this to apply the policy.
/// </remarks>
/// <see cref="SettlementDateCalculator"/>
public class SettlementDatePolicy
{
	private int m_settlementMinOffset;
	private char m_settlementMinOffsetUnit;
	private int m_settlementMaxOffset;
	private char m_settlementMaxOffsetUnit;
	private boolean m_settlementThisQuarterOrFirstMonthOfNextQuarter = false;
	private String m_settlementThisMonth = "false";
	
	/// <summary>
	/// Default Constructor.
	/// </summary>
	public SettlementDatePolicy()
	{
	}

	/// <summary>
	/// The minimum number to advance for the settlement date.
	/// </summary>
	public int getSettlementMinOffset()
	{
		 return m_settlementMinOffset; 
	}
	public void setSettlementMinOffset(int that)
	{
		m_settlementMinOffset = that;
	}

	/// <summary>
	/// The unit for advancing  "D" for day.  "M" for month.
	/// </summary>
	public char getSettlementMinOffsetUnit()
	{
		 return m_settlementMinOffsetUnit; 
	}
	public void setSettlementMinOffsetUnit(char that)
	{
		m_settlementMinOffsetUnit = that;
	}
	
	/// <summary>
	/// The maximum number to advance for the settlement date.
	/// </summary>
	public int getSettlementMaxOffset()
	{
		 return m_settlementMaxOffset; 
	}
	public void setSettlementMaxOffset(int that)
	{
		m_settlementMaxOffset = that;
	}
	
	/// <summary>
	/// The unit for advancing  "D" for day.  "M" for month.
	/// </summary>
	public char getSettlementMaxOffsetUnit()
	{
		 return m_settlementMaxOffsetUnit; 
	}
	public void setSettlementMaxOffsetUnit(char that)
	{
		m_settlementMaxOffsetUnit = that;
	}

	/// <summary>
	/// The settlement date must be in the current quarter or the first
	/// month of the next quarter.  Supports the WA UI requirements.
	/// </summary>
	public boolean getSettlementThisQuarterOrFirstMonthOfNextQuarter()
	{
		 return m_settlementThisQuarterOrFirstMonthOfNextQuarter; 
	}
	public void setSettlementThisQuarterOrFirstMonthOfNextQuarter(boolean that)
	{
		m_settlementThisQuarterOrFirstMonthOfNextQuarter = that;
	}

	/// <summary>
	/// The settlement date must be in the current month
	/// </summary>
	public String getSettlementThisMonth()
	{
		 return m_settlementThisMonth; 
	}
	public void setSettlementThisMonth(String that)
	{
		m_settlementThisMonth = that;
	}
}
