package com.intuit.sbd.payroll.psp.interceptor;

import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PreparedStatementInterceptor {

    public String process(String sql) {

        try {
            //Precheck
            boolean isSqlChangeRequired = precheck(sql);

            if (isSqlChangeRequired) {
                sql = manipulatePreparedStatement(sql);
            }
            return sql;
        } finally {
            //Remove thread local context.
            postProcess();
        }
    }

    public boolean precheck(String sql) {
        if(!(isUpdateQuery(sql) || isDeleteQuery(sql) || isFindByIdQuery(sql))) {
            return false;
        }
        if(partitionKeyAlreadyPresent(sql, getPartitionKey())) {
            return false;
        }
        return true;
    }

    public boolean isUpdateQuery(String sql){
        String updateQuery = String.format(InterceptorConstant.UPDATE_QUERY_TEMPLATE, getTableName());
        if(StringUtils.containsIgnoreCase(sql, updateQuery)) {
            return true;
        }
        return false;
    }

    public boolean isDeleteQuery(String sql){
        String deleteQuery = String.format(InterceptorConstant.DELETE_QUERY_TEMPLATE, getTableName());
        if(StringUtils.containsIgnoreCase(sql, deleteQuery)) {
            return true;
        }
        return false;
    }

    public boolean partitionKeyAlreadyPresent(String sql, String partitionKey) {
        String existingWhereClause = StringUtils.substringAfter(sql.toLowerCase(), InterceptorConstant.WHERE);
        if(StringUtils.containsIgnoreCase(existingWhereClause, partitionKey)) {
            return true;
        }
        return false;
    }

    private boolean isFindByIdQuery(String sql){
        String seqName = String.format("%s%s", getTableName().replace("PSP_", ""), "_SEQ");
        String regex = String.format(InterceptorConstant.REGEX_TEMPLATE, getTableName(), seqName);

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        return matcher.matches();
    }

    //This method implementation should not fire another database call.
    abstract protected String manipulatePreparedStatement(String sql);

    protected void postProcess() {
        DomainEntityChangeManager.removeDomainEntityChangeModel();
    }

    //Unable to use "Class" as return type because domain-secondary classes are not available here.
    abstract public String getType();

    abstract protected String getTableName();

    abstract public String getPartitionKey();
}
