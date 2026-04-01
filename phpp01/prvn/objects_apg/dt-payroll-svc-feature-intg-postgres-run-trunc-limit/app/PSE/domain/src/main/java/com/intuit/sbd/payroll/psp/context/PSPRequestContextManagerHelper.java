package com.intuit.sbd.payroll.psp.context;

import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;

import java.util.Objects;

public class PSPRequestContextManagerHelper {
    private static PSPRequestContextManager pspRequestContextManager;

    public static PSPRequestContextManager getPSPRequestContextManager() {
        if(pspRequestContextManager == null) {
            pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        }
        return pspRequestContextManager;
    }
    public static boolean isRequestContextCompanySet(){
        return Objects.nonNull(pspRequestContextManager.getRequestContext()) && Objects.nonNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
    }
}
