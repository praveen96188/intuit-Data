package com.intuit.sbd.payroll.psp.hibernate.multitenancy;

public class TenantContext {

   private static final ThreadLocal<String> tenantIdThreadLocal = new ThreadLocal<>();

    public static String getTenantId() {
        return tenantIdThreadLocal.get();
    }

    public static void setTenantId(String tenantId) {
        tenantIdThreadLocal.set(tenantId);
    }

    public static void clearTenantId() {
        tenantIdThreadLocal.remove();
    }

}
