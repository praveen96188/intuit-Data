package com.intuit.sbd.payroll.psp.emailsender.filter;

import com.sun.jersey.api.client.WebResource;

public interface RequestFilter {

    void filter(WebResource.Builder builder);
}
