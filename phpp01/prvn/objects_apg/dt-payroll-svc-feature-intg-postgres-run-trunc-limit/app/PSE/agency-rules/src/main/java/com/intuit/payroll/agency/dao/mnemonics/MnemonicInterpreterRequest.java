//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------


package com.intuit.payroll.agency.dao.mnemonics;

//import com.intuit.spc.foundations.portability.SpcfSystem;
//import com.intuit.spc.foundations.portability.SpcfFactory;
//import com.intuit.spc.foundations.portability.SpcfStringBuilder;

import com.intuit.payroll.agency.util.RulesCalendar;

/// <summary>
/// Used by MnemonicInterpreter - provides input values.
/// </summary>
public class MnemonicInterpreterRequest
{
    private RulesCalendar m_accrualDate;
    /// <summary>
    /// The date that the accrual occurred on.
    /// </summary>
    public RulesCalendar getAccrualDate ()
    {
        return m_accrualDate;
    }

    private String m_startMnemonicInput;
    /// <summary>
    /// The mnemonic representation of the accrual
    /// period start date.
    /// </summary>
    public String getStartMnemonicInput()
    {
        return m_startMnemonicInput;
    }

    private String m_endMnemonicInput;
    /// <summary>
    /// The mnemonic representation of the accrual period
    /// end date.
    /// </summary>
    public String getEndMnemonicInput()
    {
        return m_endMnemonicInput;
    }

    private String m_dueMnemonicInput;
    /// <summary>
    /// The mnemonic representation of the due date.
    /// </summary>
    public String getDueMnemonicInput()
    {
        return m_dueMnemonicInput;
    }

    /// <summary>
    /// create a new mnemonic interpretation request using
    /// the mnemonics gleaned from the rules source and the
    /// accrual date to get the period for.
    /// </summary>
    /// <remarks>
    /// Most mnemonics only encode day/month combinations, so
    /// the accrual date value gives the interpreter context
    /// for the year.
    /// </remarks>
    /// <param name="startMnemonicInput">String representation of the period start</param>
    /// <param name="endMnemonicInput">String representation of the period end</param>
    /// <param name="dueMnemonicInput">String representation of the due date</param>
    /// <param name="accrualDate">The date that the accrual in question occured on</param>
    public MnemonicInterpreterRequest(String startMnemonicInput, String endMnemonicInput, String dueMnemonicInput, RulesCalendar accrualDate)
    {
        m_startMnemonicInput = startMnemonicInput;
        m_endMnemonicInput = endMnemonicInput;
        m_dueMnemonicInput = dueMnemonicInput;
        m_accrualDate = accrualDate;
    }

    /// <summary>
    /// Override of Object.toString() to facilitate output
    /// of the MnemonicInterpreterRequest to a String value.
    /// </summary>
    /// <returns>String dump of the object.</returns>
    public String toString()
    {
//        SpcfStringBuilder builder = SpcfFactory.getInstance().createStringBuilder();
        StringBuilder builder = new StringBuilder();
        builder.append("Start Mnemonic: ");
        builder.append(m_startMnemonicInput);
//        builder.append(SpcfSystem.getNewLine());
        builder.append(System.getProperty("line.separator"));

        builder.append("End Mnemonic: ");
        builder.append(m_endMnemonicInput);
//        builder.append(SpcfSystem.getNewLine());
        builder.append(System.getProperty("line.separator"));
        builder.append("Due Mnemonic: ");
        builder.append(m_dueMnemonicInput);
//        builder.append(SpcfSystem.getNewLine());
        builder.append(System.getProperty("line.separator"));
        builder.append("Accrual Date: ");
        builder.append(m_accrualDate.toString());

        return builder.toString();
    }
}
