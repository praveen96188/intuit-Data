package com.intuit.sbd.payroll.psp.filter.range;

import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class SqlStatementInspectorDataManipulationQueryDateStrategy extends SqlStatementInspectorDateStrategy {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.DATE_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY;
    }

    @Override
    protected SpcfCalendar getCreatedDate() {
        DomainEntityChangeModel domainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        if(Objects.isNull(domainEntityChangeModel) || Objects.isNull(domainEntityChangeModel.getDomainEntity())){
            return null;
        }

        return domainEntityChangeModel.getDomainEntity().getCreatedDate();
    }
}
