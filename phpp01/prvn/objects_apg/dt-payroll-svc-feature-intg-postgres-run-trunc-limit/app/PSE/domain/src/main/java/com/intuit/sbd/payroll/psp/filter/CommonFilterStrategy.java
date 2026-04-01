package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;

public interface CommonFilterStrategy<U, V> {

    FilterStrategyType getType();

    V applyFilter(U u);

    void clearFilter();

}
