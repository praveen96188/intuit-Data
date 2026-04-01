package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HibernateSessionFilterCompanyStrategy extends AbstractCompanyFilterStrategy<Void, Void> {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.SESSION_FILTER;
    }

    @Override
    public Void applyFilter(Void v) {
        if(!isFilterRequired()) {
            return null;
        }
        SpcfUniqueId companySequence = getCompanySequence();

        Application.getHibernateSession().enableFilter("COMPANY_FILTER")
                .setParameter("companySequence", companySequence);
        return null;
    }

    public void clearFilter() {
        Application.getHibernateSession().disableFilter("COMPANY_FILTER");
    }
}
