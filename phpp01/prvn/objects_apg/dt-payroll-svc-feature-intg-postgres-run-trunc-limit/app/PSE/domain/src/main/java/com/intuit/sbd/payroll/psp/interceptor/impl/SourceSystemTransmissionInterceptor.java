package com.intuit.sbd.payroll.psp.interceptor.impl;

import com.intuit.sbd.payroll.psp.interceptor.PreparedStatementInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SourceSystemTransmissionInterceptor extends PreparedStatementInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceSystemTransmissionInterceptor.class);

    @Override
    public boolean precheck(String sql) {
        return super.precheck(sql) && FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_PARTITIONED_SST_UPDATE, false);
    }

    @Override
    public String manipulatePreparedStatement(String sql) {
        String oldSql = sql;
        String date;
        SpcfCalendar createdDate = DomainEntityChangeManager.getDomainEntityChangeModelContext().getDomainEntity().getCreatedDate();
        if(ObjectUtils.isEmpty(createdDate)) {
            return sql;
        }
        createdDate.addDays(-1);
        date = SpcfCalendar.toDateLiteral(createdDate);
        String dateClause = String.format(InterceptorConstant.DATE_LITERAL, date);
        String whereClause = InterceptorConstant.WHERE + dateClause;
        sql = StringUtils.replaceIgnoreCase(sql, InterceptorConstant.WHERE, whereClause);
        boolean logSSTSqlUpdateQuery = FeatureFlags.get().booleanValue(FeatureFlags.Key.LOG_SST_UPDATE_SQL, false);
        if(logSSTSqlUpdateQuery) {
            LOGGER.info("SST Query Update. Old Query={}, New Query={}", oldSql, sql);
        }
        return sql;
    }

    @Override
    public String getType() {
        return InterceptorConstant.SST_CLASS_NAME;
    }

    @Override
    public String getTableName() {
        return InterceptorConstant.SST_TABLE_NAME;
    }

    @Override
    public String getPartitionKey() {
        return InterceptorConstant.CREATED_DATE_SQL;
    }

}
