package com.intuit.sbd.payroll.psp.transactionobserver;

import com.intuit.sbd.payroll.psp.filter.CompanyFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.util.TransactionObserver;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyTransactionObserver extends TransactionObserver {

    private CompanyFilterStrategyFactory companyFilterStrategyFactory;
    private CompanyFilterStrategy<Void, Void> companyFilterStrategy;

    public CompanyTransactionObserver() {
        companyFilterStrategyFactory = PayrollApplicationBeanFactory.getBean(CompanyFilterStrategyFactory.class);
        companyFilterStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.SESSION_FILTER);
    }

    @Override
    public void afterTransactionBegin() {
        boolean hibernateFilterEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_COMPANY_FILTER);
        if(!hibernateFilterEnabled)
            return;

        companyFilterStrategy.applyFilter(null);
    }
}