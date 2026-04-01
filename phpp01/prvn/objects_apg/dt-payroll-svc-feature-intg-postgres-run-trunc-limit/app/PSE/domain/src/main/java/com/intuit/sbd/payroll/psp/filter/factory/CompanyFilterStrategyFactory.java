package com.intuit.sbd.payroll.psp.filter.factory;

import com.intuit.sbd.payroll.psp.filter.*;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CompanyFilterStrategyFactory {

    private Map<FilterStrategyType, CompanyFilterStrategy> filterStrategyMap;

    @Autowired
    public CompanyFilterStrategyFactory(List<CompanyFilterStrategy> filterStrategyList) {
        filterStrategyMap = filterStrategyList.stream().collect(Collectors.toMap(CompanyFilterStrategy::getType, Function.identity()));
    }

    public CompanyFilterStrategy getCompanyFilterStrategy(FilterStrategyType filterStrategyType) {
        return filterStrategyMap.get(filterStrategyType);
    }
}
