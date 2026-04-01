package com.intuit.sbd.payroll.psp.context;

import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public interface PSPRequestContextManager {
    void setRequestContext(RequestContext requestContext);
    void setRequestContext(Company company, RequestType requestType, String requestOperation);
    void clearRequestContext();
    RequestContext getRequestContext();

    void setRequestContextCompany(Company company);
    void clearRequestContextCompany();

    void setRequestContextCompanyFromPSID(String psid);
    void setRequestContextCompanyFromSeq(String companySeq);

    void setCreatedDate(SpcfCalendar createdDate);

    void clearCreatedDate();

}
