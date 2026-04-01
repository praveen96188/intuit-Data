package com.intuit.sbd.payroll.psp.context.threading;

import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;

import java.util.Objects;

public class ChildThreadRequestContextHelper {

    private RequestContext requestContext;
    private PSPRequestContextManager pspRequestContextManager;

    public ChildThreadRequestContextHelper(){
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void loadThreadLocals() {
        requestContext = pspRequestContextManager.getRequestContext();
    }

    public void setThreadLocals() {
        if(Objects.nonNull(requestContext)) {
            pspRequestContextManager.setRequestContext(requestContext);
        }
    }

    public void clearThreadLocals() {
        pspRequestContextManager.clearRequestContext();
    }
}
