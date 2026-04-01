package com.intuit.ems.payroll.psp.model;

/**
 * @author rn5
 */
public class QueryModel {

    private boolean rollback;
    private String query;
    private String[] paramNames;
    private Object[] paramValues;

    public QueryModel(String query, String[] paramNames, Object[] paramValues, boolean rollback) {
        this.query = query;
        this.paramNames = paramNames;
        this.paramValues = paramValues;
        this.rollback = rollback;
    }

    public String getQuery() {
        return query;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public Object[] getParamValues() {
        return paramValues;
    }

    public boolean isRollback() {
        return rollback;
    }

}
