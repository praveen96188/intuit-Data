package com.intuit.sbd.payroll.psp.common;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 29, 2009
 * Time: 4:17:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableParameters {
    private String tableName;
    private String primaryKey;
    private ArrayList<TableColumn> propertyTemplateParameters = new ArrayList<TableColumn>();

        public TableParameters(String pTableName) {
            tableName = pTableName;
        }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public ArrayList<TableColumn> getPropertyTemplateParameters() {
        return propertyTemplateParameters;
    }

    public void setPropertyTemplateParameters(ArrayList<TableColumn> propertyTemplateParameters) {
        this.propertyTemplateParameters = propertyTemplateParameters;
    }
}
