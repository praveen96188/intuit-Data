package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqlStatementInspectorCompanyStrategy extends AbstractCompanyFilterStrategy<String, String> {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.SQL_STATEMENT_INSPECTOR;
    }

    @Override
    public String applyFilter(String sql) {
        if(!isFilterRequired()) {
            return sql;
        }
        String companySequence = getCompanySequence().toString();
        String companyIdClause = String.format(InterceptorConstant.COMPANY_FK_LITERAL, companySequence);
        String whereClause = InterceptorConstant.WHERE + companyIdClause;
        sql = StringUtils.replaceIgnoreCase(sql, InterceptorConstant.WHERE, whereClause);
        return sql;
    }

    @Override
    public void clearFilter() {
        return;
    }
}
