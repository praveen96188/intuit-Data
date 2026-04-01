package com.intuit.sbd.payroll.psp.filter.config;

import com.intuit.sbg.psp.filtervalidator.FilterValidatorConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FilterValidatorConfiguration.class})
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.filter"})
public class FilterConfig {
}
