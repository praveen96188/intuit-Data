package com.intuit.sbd.payroll.psp.filter.range;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HibernateSessionFilterDateStrategy extends AbstractDateFilterStrategy<Void, Void> {

    @Override
    public FilterStrategyType getType() {
        return FilterStrategyType.DATE_SESSION_FILTER;
    }

    @Override
    public Void applyFilter(Void v) {
        if(!isDateFilterRequired()) {
            return null;
        }
        SpcfCalendar createdDate = getCreatedDate().copy();
        createdDate.addDays(-1);

        Application.getHibernateSession().enableFilter("DATE_FILTER")
                .setParameter("createdDate", createdDate);
        return null;
    }

    public void clearFilter() {
        Application.getHibernateSession().disableFilter("DATE_FILTER");
    }
}
