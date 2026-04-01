package com.intuit.sbd.payroll.psp.spring.config;

import com.intuit.sbd.payroll.psp.cache.spring.MultipleCacheConfig;
import com.intuit.sbd.payroll.psp.context.config.ContextConfig;
import com.intuit.sbd.payroll.psp.entity.EntityConfig;
import com.intuit.sbd.payroll.psp.entity.InterceptorConfig;
import com.intuit.sbd.payroll.psp.filter.config.FilterConfig;
import com.intuit.sbd.payroll.psp.mapper.MapperConfig;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({EntityConfig.class, MapperConfig.class, InterceptorConfig.class, FilterConfig.class, MultipleCacheConfig.class, ContextConfig.class})
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.context"})
@PropertySource(value = "classpath:resources/domain.yml", factory = YamlPropertySourceFactory.class)
public class DomainConfig {
}
