package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class SqlStatementInspectorDataManipulationQueryLicenseNumberStrategy implements CommonFilterStrategy<String, String> {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.LICENSE_NUMBER_SQL_STATEMENT_INSPECTOR_DATA_MANIPULATION_QUERY;
    }

    @Override
    public String applyFilter(String sql) {
        if(!isFilterRequired()) {
            return sql;
        }
        String licenseNumber = getLicenseNumber();
        String licenseNumberClause = String.format(InterceptorConstant.LICENSE_NUMBER_LITERAL, licenseNumber);
        String whereClause = InterceptorConstant.WHERE + licenseNumberClause;
        sql = StringUtils.replaceIgnoreCase(sql, InterceptorConstant.WHERE, whereClause);
        return sql;
    }

    private boolean isFilterRequired(){
        boolean isFilterRequired = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER);
        if(!isFilterRequired) {
            return false;
        }
        if(StringUtils.isEmpty(getLicenseNumber())) {
            return false;
        }
        return true;
    }

    @Override
    public void clearFilter() {
        return;
    }

    protected String getLicenseNumber() {
        DomainEntityChangeModel domainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        if(Objects.isNull(domainEntityChangeModel) || Objects.isNull(domainEntityChangeModel.getDomainEntity())){
            return null;
        }

        if(domainEntityChangeModel.getDomainEntity() instanceof EntitlementMessage) {
            return ((EntitlementMessage) domainEntityChangeModel.getDomainEntity()).getLicenseNumber();
        }

        return null;
    }
}
