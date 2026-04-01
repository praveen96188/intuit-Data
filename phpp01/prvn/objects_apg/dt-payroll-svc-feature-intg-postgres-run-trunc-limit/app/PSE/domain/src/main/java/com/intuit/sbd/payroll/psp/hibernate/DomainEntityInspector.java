package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.filter.filtervalidator.FilterValidatorExecutor;
import com.intuit.sbd.payroll.psp.interceptor.PreparedStatementInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.factory.PreparedStatementInterceptorFactory;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.Objects;

@Slf4j
public class DomainEntityInspector implements StatementInspector {
    private PreparedStatementInterceptorFactory preparedStatementInterceptorFactory;
    private FilterValidatorExecutor filterValidatorExecutor;

    public DomainEntityInspector() {
        preparedStatementInterceptorFactory = PayrollApplicationBeanFactory.getBean(PreparedStatementInterceptorFactory.class);
        filterValidatorExecutor = PayrollApplicationBeanFactory.getBean(FilterValidatorExecutor.class);
    }

    @Override
    public String inspect(String sql) {
        try {
            PreparedStatementInterceptor interceptor = preparedStatementInterceptorFactory.getInterceptor(sql);
            if (!Objects.isNull(interceptor)) {
                sql = interceptor.process(sql);
            }
            validateFilter(sql);
        } catch (Exception e) {
            log.error("Exception occured while getting Interceptor", e);
        } finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
            return sql;
        }
    }

    private void validateFilter(String sql) {
        try {
            filterValidatorExecutor.parseSQL(sql);
        } catch (Exception e) {
            log.error("Exception occurred while validating filter", e);
        }
    }
}
