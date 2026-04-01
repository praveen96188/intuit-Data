package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 14, 2009
 * Time: 4:26:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableColumn {
    public TableColumn(String pColumnName, String pColumnType, String pIsNullable)
    {
        columnName = pColumnName;
        columnType = pColumnType;
        isNullable = pIsNullable;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public String getColumnType()
    {
        return columnType;
    }

    public void setColumnType(String columnType)
    {
        this.columnType = columnType;
    }

    public String getNullable()
    {
        return isNullable;
    }

    public void setNullable(String nullable)
    {
        isNullable = nullable;
    }

    private String columnName;
    private String columnType;
    private String isNullable;
    private String columnLength;
}
