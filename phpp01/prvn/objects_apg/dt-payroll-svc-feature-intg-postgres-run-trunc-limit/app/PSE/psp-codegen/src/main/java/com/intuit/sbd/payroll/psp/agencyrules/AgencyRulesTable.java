// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AgencyRulesTable.java

package com.intuit.sbd.payroll.psp.agencyrules;

import com.intuit.sbd.payroll.psp.common.TableColumn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Referenced classes of package com.intuit.sbd.payroll.psp.agencyrules:
//            AgencyRulesTableColumn, AgencyRulesProcessor

public class AgencyRulesTable
{
    public AgencyRulesTable(AgencyRulesProcessor pAgencyRulesProcessor, String pTableName, String pPrimaryKeyColumn, String pForeignKeyColumn1,String pForeignKeyColumn2)
    {
        agencyRulesProcessor = pAgencyRulesProcessor;
        tableName = pTableName;
        primaryKeyColumn = pPrimaryKeyColumn;
        foreignKeyColumn1 = pForeignKeyColumn1;
        foreignKeyColumn2 = pForeignKeyColumn2;
    }
    
    public AgencyRulesTable(AgencyRulesProcessor pAgencyRulesProcessor, String pTableName, String pPrimaryKeyColumn)
    {
        agencyRulesProcessor = pAgencyRulesProcessor;
        tableName = pTableName;
        primaryKeyColumn = pPrimaryKeyColumn;
    }

    public String getPrimaryKey()
    {
        return primaryKeyColumn;
    }

    public String getPrimaryKeyColumn()
    {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn)
    {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getForeignKey1() {
        return foreignKeyColumn1;
    }

    public String getForeignKeyColumn1() {
        return foreignKeyColumn1;
    }

    public void setForeignKeyColumn1(String foreignKeyColumn1) {
        this.foreignKeyColumn1 = foreignKeyColumn1;
    }

    public String getForeignKey2() {
        return foreignKeyColumn2;
    }

    public String getForeignKeyColumn2() {
        return foreignKeyColumn2;
    }

    public void setForeignKeyColumn2(String foreignKeyColumn2) {
        this.foreignKeyColumn2 = foreignKeyColumn2;
    }


    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public List<TableColumn> getTableColumns()
    {
        if(tableColumns == null)
            tableColumns = new ArrayList();
        return tableColumns;
    }

    public String getColumnSelectList()
    {
        return getColumnList("");
    }

    public String getColumnSelectListWithRTPrefix()
    {
        return getColumnList("rt.");
    }

    public String getColumnSelectListWithTTPrefix()
    {
        return getColumnList("tt.");
    }

    public List<String> getInsertRowValues()
    {
        if(rowValuesList == null)
            rowValuesList = new ArrayList();
        return rowValuesList;
    }

    private String getColumnList(String pPrefix)
    {
        String columnList = "";
        for(Iterator i$ = getTableColumns().iterator(); i$.hasNext();)
        {
            TableColumn column = (TableColumn)i$.next();
            columnList = (new StringBuilder()).append(columnList).append(pPrefix).append(column.getColumnName()).append(", ").toString();
        }

        columnList = columnList.substring(0, columnList.length() - 2);
        return columnList;
    }

    private AgencyRulesProcessor agencyRulesProcessor;
    private String primaryKeyColumn;
    private String tableName;
    private ArrayList<TableColumn> tableColumns;
    private ArrayList<String> rowValuesList = new ArrayList();
    private String foreignKeyColumn1;
    private String foreignKeyColumn2;
}
