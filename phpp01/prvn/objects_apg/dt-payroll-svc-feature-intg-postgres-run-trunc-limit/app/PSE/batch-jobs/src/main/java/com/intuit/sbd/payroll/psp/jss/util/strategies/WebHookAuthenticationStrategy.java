package com.intuit.sbd.payroll.psp.jss.util.strategies;

import javax.servlet.http.HttpServletRequest;

public interface WebHookAuthenticationStrategy {

    boolean authenticate(HttpServletRequest httpServletRequest);
}
