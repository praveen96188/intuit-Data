package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT_ID = TenantIdentifier.READ_WRITE.name();

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if(StringUtils.isNotEmpty(tenantId)) {
            return tenantId;
        }
        return DEFAULT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
