package com.intuit.sbd.payroll.psp.context.helper;

import com.intuit.sbd.payroll.psp.context.model.RequestContext;
public interface IRequestContextHelper {

    void resetRequestContext();
    void removeRequestContext();
    void initRequestContext();
    void setCurrentRequestContext(RequestContext requestContext);
    void incrementCurrentRequestContextRefCount();
    RequestContext getCurrentRequestContext();
    RequestContext clearCurrentRequestContext();
}
