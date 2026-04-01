package com.intuit.sbd.payroll.psp.filter.factory;

import com.intuit.sbd.payroll.psp.filter.CommonFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommonFilterStrategyFactory {

    private Map<FilterStrategyType, CommonFilterStrategy> filterStrategyMap;

    @Autowired
    public CommonFilterStrategyFactory(List<CommonFilterStrategy> filterStrategyList) {
        filterStrategyMap = filterStrategyList.stream().collect(Collectors.toMap(CommonFilterStrategy::getType, Function.identity()));
    }

    public CommonFilterStrategy getFilterStrategy(FilterStrategyType filterStrategyType) {
        return filterStrategyMap.get(filterStrategyType);
    }
}
