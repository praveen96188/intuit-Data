package com.intuit.sbd.payroll.psp.context.config;

import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerImpl;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerStackImpl;
import com.intuit.sbd.payroll.psp.context.helper.CompanyContextHelper;
import com.intuit.sbd.payroll.psp.context.helper.IRequestContextHelper;
import com.intuit.sbd.payroll.psp.filter.factory.CommonFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbd.payroll.psp.context.aspect.RequestContextAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Aspects;
import org.aspectj.lang.NoAspectBoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.context", "com.intuit.sbd.payroll.psp.filter"})
public class ContextConfig {

    @Bean
    PSPRequestContextManager getPSPRequestContextManager(CompanyFilterStrategyFactory companyFilterStrategyFactory, CommonFilterStrategyFactory commonFilterStrategyFactory, IRequestContextHelper requestContextHelper, CompanyContextHelper companyContextHelper){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(enableStackBasedRequestContext){
            return new PSPRequestContextManagerStackImpl(companyFilterStrategyFactory, commonFilterStrategyFactory, requestContextHelper, companyContextHelper);
        }
        return new PSPRequestContextManagerImpl(companyFilterStrategyFactory, commonFilterStrategyFactory, companyContextHelper);
    }

    @Bean
    public RequestContextAspect requestContextAspect(CompanyContextHelper companyContextHelper, PSPRequestContextManager pspRequestContextManager){
        try {
            RequestContextAspect requestContextAspect = Aspects.aspectOf(RequestContextAspect.class);
            requestContextAspect.setCompanyContextHelper(companyContextHelper);
            requestContextAspect.setPspRequestContextManager(pspRequestContextManager);
            return requestContextAspect;
        } catch (NoAspectBoundException e){
            log.info("Unable to weave aspect", e);
        }
        return null;
    }
}
