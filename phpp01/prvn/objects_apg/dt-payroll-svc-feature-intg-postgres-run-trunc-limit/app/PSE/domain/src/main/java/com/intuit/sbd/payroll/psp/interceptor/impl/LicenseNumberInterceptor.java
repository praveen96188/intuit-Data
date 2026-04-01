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
public class LicenseNumberInterceptor extends PreparedStatementInterceptor {

    private String className;
    private String tableName;
    private CommonFilterStrategy<String, String> licenseNumberFilterStrategy;

    @Autowired
    public LicenseNumberInterceptor(CommonFilterStrategyFactory licenseNumberFilterStrategyFactory) {
        licenseNumberFilterStrategy = licenseNumberFilterStrategyFactory.getFilterStrategy(FilterStrategyType.LICENSE_NUMBER_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY);
    }

    @Override
    protected String manipulatePreparedStatement(String oldSql) {
        return licenseNumberFilterStrategy.applyFilter(oldSql);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setType(String type) {
        this.className = type;
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
        return InterceptorConstant.LICENSE_NUMBER_SQL;
    }

}
