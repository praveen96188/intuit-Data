package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.filter.helper.HibernateCriteriaPartitionKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HibernateCriteriaCompanyStrategy extends AbstractCompanyFilterStrategy<Criteria, Criteria> {
    private HibernateCriteriaPartitionKeyHelper hibernateCriteriaPartitionKeyHelper;

    @Autowired
    public HibernateCriteriaCompanyStrategy(HibernateCriteriaPartitionKeyHelper hibernateCriteriaPartitionKeyHelper) {
        this.hibernateCriteriaPartitionKeyHelper = hibernateCriteriaPartitionKeyHelper;
    }

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.HIBERNATE_CRITERIA;
    }

    @Override
    public Criteria applyFilter(Criteria criteria) {
        if(!isFilterRequired()) {
            return criteria;
        }
        return hibernateCriteriaPartitionKeyHelper.addRestrictionsToHibernateCriteria(criteria, getCompanySequence());
    }

    @Override
    public void clearFilter() {
        return;
    }
}
