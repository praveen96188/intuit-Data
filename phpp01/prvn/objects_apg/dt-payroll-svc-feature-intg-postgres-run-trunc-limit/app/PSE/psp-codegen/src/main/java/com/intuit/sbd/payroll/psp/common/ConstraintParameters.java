package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 29, 2009
 * Time: 4:18:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintParameters {
        private String tableName;
        private String foreignKeyColumnName;
        private String referenceTableName;
        private String foreignKeyConstraintName;

    public ConstraintParameters(String pTableName) {
        tableName = pTableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getForeignKeyColumnName() {
        return foreignKeyColumnName;
    }

    public void setForeignKeyColumnName(String foreignKeyColumnName) {
        this.foreignKeyColumnName = foreignKeyColumnName;
    }

    public String getReferenceTableName() {
        return referenceTableName;
    }

    public void setReferenceTableName(String referenceTableName) {
        this.referenceTableName = referenceTableName;
    }

    public String getForeignKeyConstraintName() {
        return foreignKeyConstraintName;
    }

    public void setForeignKeyConstraintName(String foreignKeyConstraintName) {
        this.foreignKeyConstraintName = foreignKeyConstraintName;
    }
}
