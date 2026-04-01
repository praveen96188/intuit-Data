package com.intuit.sbd.payroll.psp.transactionobserver;

import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.filter.factory.CommonFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.util.TransactionObserver;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateTransactionObserver extends TransactionObserver {

    private CommonFilterStrategy<Void, Void> dateFilterStrategy;

    public DateTransactionObserver() {
        CommonFilterStrategyFactory commonFilterStrategyFactory = PayrollApplicationBeanFactory.getBean(CommonFilterStrategyFactory.class);
        dateFilterStrategy = commonFilterStrategyFactory.getFilterStrategy(FilterStrategyType.DATE_SESSION_FILTER);
    }

    @Override
    public void afterTransactionBegin() {
        boolean hibernateFilterEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_DATE_FILTER);
        if(!hibernateFilterEnabled)
            return;

        dateFilterStrategy.applyFilter(null);
    }
}