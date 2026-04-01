package com.intuit.sbd.payroll.psp.entity;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.interceptor"})
public class InterceptorConfig {
}
