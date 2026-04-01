package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class SqlStatementInspectorDataManipulationQueryCompanyStrategy extends SqlStatementInspectorCompanyStrategy {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY;
    }

    @Override
    protected SpcfUniqueId getCompanySequence() {
        DomainEntityChangeModel domainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        if(Objects.isNull(domainEntityChangeModel) || Objects.isNull(domainEntityChangeModel.getDomainEntity()) || !(domainEntityChangeModel.getDomainEntity() instanceof TenantInfo )){
            return null;
        }

        TenantInfo tenantInfo = (TenantInfo) domainEntityChangeModel.getDomainEntity();

        return tenantInfo.getTenantId();
    }

    @Override
    protected boolean isFilterRequired(){
        DomainEntityChangeModel domainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        if(Objects.isNull(domainEntityChangeModel) || Objects.isNull(domainEntityChangeModel.getDomainEntity()) || !(domainEntityChangeModel.getDomainEntity() instanceof TenantInfo )){
            return false;
        }

        RequestContext requestContext = getPSPRequestContextManager().getRequestContext();
        if(Objects.isNull(requestContext)) {
            return false;
        }
        return isFilterAllowed(requestContext.getRequestType(), requestContext.getRequestOperation());
    }
}
