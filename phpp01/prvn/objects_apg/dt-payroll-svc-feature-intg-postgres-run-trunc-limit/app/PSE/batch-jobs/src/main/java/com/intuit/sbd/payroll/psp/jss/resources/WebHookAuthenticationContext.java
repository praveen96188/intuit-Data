package com.intuit.sbd.payroll.psp.jss.resources;

import com.intuit.sbd.payroll.psp.jss.util.strategies.WebHookAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;

public class WebHookAuthenticationContext {

    private WebHookAuthenticationStrategy authenticationStrategy;

    public WebHookAuthenticationContext(WebHookAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    public boolean authenticate(HttpServletRequest httpServletRequest) {
        return authenticationStrategy.authenticate(httpServletRequest);
    }
}
