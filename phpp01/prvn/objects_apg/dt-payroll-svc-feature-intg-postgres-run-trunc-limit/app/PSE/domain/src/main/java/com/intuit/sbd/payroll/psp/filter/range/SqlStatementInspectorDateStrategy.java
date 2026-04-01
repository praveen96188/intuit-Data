package com.intuit.sbd.payroll.psp.filter.range;

import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqlStatementInspectorDateStrategy extends AbstractDateFilterStrategy<String, String> {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.DATE_SQL_STATEMENT_INSPECTOR;
    }

    @Override
    public String applyFilter(String sql) {
        if(!isDateFilterRequired()) {
            return sql;
        }
        SpcfCalendar createdDate = getCreatedDate().copy();
        createdDate.addDays(-1);

        String createdDateString = SpcfCalendar.toDateLiteral(createdDate);
        String createdDateClause = String.format(InterceptorConstant.DATE_LITERAL, createdDateString);
        String whereClause = InterceptorConstant.WHERE + createdDateClause;
        sql = StringUtils.replaceIgnoreCase(sql, InterceptorConstant.WHERE, whereClause);
        return sql;
    }

    @Override
    public void clearFilter() {
        return;
    }
}
