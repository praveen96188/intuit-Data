package com.intuit.sbd.payroll.psp.interceptor.impl;

import com.intuit.sbd.payroll.psp.filter.CompanyFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
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
public class CompanyInterceptor extends PreparedStatementInterceptor {

    private String className;
    private String tableName;
    private CompanyFilterStrategyFactory companyFilterStrategyFactory;
    private CompanyFilterStrategy<String, String> companyFilterStrategy;
    private CompanyFilterStrategy<String, String> companyFilterDataManipulationQueryStrategy;

    @Autowired
    public CompanyInterceptor(CompanyFilterStrategyFactory companyFilterStrategyFactory) {
        this.companyFilterStrategyFactory = companyFilterStrategyFactory;
        companyFilterStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.SQL_STATEMENT_INSPECTOR);
        companyFilterDataManipulationQueryStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY);
    }

    @Override
    protected String manipulatePreparedStatement(String oldSql) {
        if(isUpdateQuery(oldSql) || isDeleteQuery(oldSql)){
            return companyFilterDataManipulationQueryStrategy.applyFilter(oldSql);
        }
        return companyFilterStrategy.applyFilter(oldSql);
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
        return InterceptorConstant.COMPANY_FK_SQL;
    }

}
