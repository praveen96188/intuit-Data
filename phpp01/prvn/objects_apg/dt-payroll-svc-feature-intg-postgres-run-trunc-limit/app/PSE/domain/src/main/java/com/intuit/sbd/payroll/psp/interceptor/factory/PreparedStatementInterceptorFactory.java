package com.intuit.sbd.payroll.psp.interceptor.factory;

import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.interceptor.PreparedStatementInterceptor;
import com.intuit.sbd.payroll.psp.filter.constants.PartitionedTablesDetails;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import com.intuit.sbd.payroll.psp.interceptor.impl.CompanyInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.impl.DateInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.impl.LicenseNumberInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.impl.SourceSystemTransmissionInterceptor;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.interceptor.model.DomainEntityChangeModel;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PreparedStatementInterceptorFactory {

    private SourceSystemTransmissionInterceptor sourceSystemTransmissionInterceptor;
    private Map<String, PreparedStatementInterceptor> interceptorMap;
    private ApplicationContext applicationContext;
    private PartitionedTablesDetails partitionedTablesDetails;

    @Autowired
    public PreparedStatementInterceptorFactory(SourceSystemTransmissionInterceptor sourceSystemTransmissionInterceptor, ApplicationContext applicationContext,
                                               PartitionedTablesDetails partitionedTablesDetails) {
        this.sourceSystemTransmissionInterceptor = sourceSystemTransmissionInterceptor;
        this.interceptorMap = new HashMap<>();
        this.applicationContext = applicationContext;
        this.partitionedTablesDetails = partitionedTablesDetails;
    }

    @PostConstruct
    public void init() {
        interceptorMap.put(sourceSystemTransmissionInterceptor.getType(), sourceSystemTransmissionInterceptor);
        boolean hibernateFilterEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_HIBERNATE_COMPANY_FILTER, false);
        if(hibernateFilterEnabled) {
            //CompanyInterceptor
            for(Map.Entry<String, String> entry : partitionedTablesDetails.getClassNameTableNameMap().entrySet()) {
                CompanyInterceptor companyInterceptor = applicationContext.getBean(CompanyInterceptor.class);
                companyInterceptor.setType(entry.getKey());
                companyInterceptor.setTableName(entry.getValue());
                interceptorMap.put(entry.getKey(), companyInterceptor);
            }
        }
        for(Map.Entry<String, Map<String, String>> outerEntry : partitionedTablesDetails.getPartitionedClassNameTableNameMap().entrySet()) {
            //DateInterceptor
            boolean hibernateDateFilterEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_HIBERNATE_DATE_FILTER, false);
            if(hibernateDateFilterEnabled && outerEntry.getKey().equalsIgnoreCase(InterceptorConstant.CREATED_DATE_SQL)) {
                for(Map.Entry<String, String> entry : outerEntry.getValue().entrySet()) {
                    DateInterceptor dateInterceptor = applicationContext.getBean(DateInterceptor.class);
                    dateInterceptor.setType(entry.getKey());
                    dateInterceptor.setTableName(entry.getValue());
                    interceptorMap.put(entry.getKey(), dateInterceptor);
                }
            }
            //LicenseNumberInterceptor
            boolean hibernateLicenseNumberFilterEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER, false);
            if(hibernateLicenseNumberFilterEnabled && outerEntry.getKey().equalsIgnoreCase(InterceptorConstant.LICENSE_NUMBER_SQL)) {
                for(Map.Entry<String, String> entry : outerEntry.getValue().entrySet()) {
                    LicenseNumberInterceptor licenseNumberInterceptor = applicationContext.getBean(LicenseNumberInterceptor.class);
                    licenseNumberInterceptor.setType(entry.getKey());
                    licenseNumberInterceptor.setTableName(entry.getValue());
                    interceptorMap.put(entry.getKey(), licenseNumberInterceptor);
                }
            }

        }
    }

    public PreparedStatementInterceptor getInterceptor(String sql) {
        DomainEntityChangeModel domainEntityChangeModel = DomainEntityChangeManager.getDomainEntityChangeModelContext();
        if(domainEntityChangeModel == null) {
            return null;
        }

       PreparedStatementInterceptor interceptor = interceptorMap.get(domainEntityChangeModel.getClazz().getCanonicalName());
        if(interceptor == null) {
            return null;
        }
        //When we are in a query processing, this can internally fire another query.
        //Ex: Application.getPreviousWeek() --> CalendarUtils.addBusinessDays fires bank holiday query.
        return interceptor.precheck(sql)? interceptor : null;
    }

    public Set<String> getPreparedStatementEntities() {
        return interceptorMap.keySet();
    }

}
