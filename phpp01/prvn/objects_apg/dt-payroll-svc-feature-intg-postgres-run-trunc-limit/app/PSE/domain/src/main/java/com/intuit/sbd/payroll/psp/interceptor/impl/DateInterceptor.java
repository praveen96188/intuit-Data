package com.intuit.sbd.payroll.psp.interceptor.impl;

import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.filter.factory.CommonFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.interceptor.PreparedStatementInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DateInterceptor extends PreparedStatementInterceptor {
    private String className;
    private String tableName;

    private CommonFilterStrategy<String, String> dateFilterStrategy;
    private CommonFilterStrategy<String, String> dateFilterDataManipulationQueryStrategy;

    @Autowired
    public DateInterceptor(CommonFilterStrategyFactory commonFilterStrategyFactory) {
        dateFilterStrategy = commonFilterStrategyFactory.getFilterStrategy(FilterStrategyType.DATE_SQL_STATEMENT_INSPECTOR);
        dateFilterDataManipulationQueryStrategy = commonFilterStrategyFactory.getFilterStrategy(FilterStrategyType.DATE_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY);
    }

    @Override
    protected String manipulatePreparedStatement(String oldSql) {
        if(isUpdateQuery(oldSql) || isDeleteQuery(oldSql)){
            return dateFilterDataManipulationQueryStrategy.applyFilter(oldSql);
        }
        return dateFilterStrategy.applyFilter(oldSql);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setType(String type) {
        this.className = className;
    }

    @Override
    protected String getTableName() {
        return tableName;
    }

    @Override
    public String getType() {
        return className;
    }

    @Override
    public String getPartitionKey() {
        return InterceptorConstant.CREATED_DATE_SQL;
    }
}
