// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AgencyRulesTableRow.java

package com.intuit.sbd.payroll.psp.agencyrules;


public class AgencyRulesTableRow
{

    public AgencyRulesTableRow(String pColumnValue)
    {
        columnValue = pColumnValue;
    }

    public String getColumnValue()
    {
        return columnValue;
    }

    public void setColumnValue(String columnValue)
    {
        this.columnValue = columnValue;
    }

    private String columnValue;
}
