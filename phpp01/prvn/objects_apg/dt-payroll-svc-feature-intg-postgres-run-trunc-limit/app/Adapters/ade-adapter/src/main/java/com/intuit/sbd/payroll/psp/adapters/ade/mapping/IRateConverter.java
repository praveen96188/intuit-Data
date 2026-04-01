package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

import java.math.BigDecimal;
import java.util.Map;

/**
 * User: TimothyD698
 * Date: 3/29/13
 */
public interface IRateConverter {
    Map<Law, BigDecimal> getRates(String state, BigDecimal baseRate, Map<String, BigDecimal> supplementalRates);
}
