package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: rnorian
 * Date: Dec 2, 2010
 * Time: 8:18:12 PM
 */
public class SAPSqlExecutionResult {
    String reason;
    String sqlStatement;
    int expectedRowCount;
    int rowCount;
    String executionTime;
    String errorMessage;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

    public void setSqlStatement(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public int getExpectedRowCount() {
        return expectedRowCount;
    }

    public void setExpectedRowCount(int expectedRowCount) {
        this.expectedRowCount = expectedRowCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
