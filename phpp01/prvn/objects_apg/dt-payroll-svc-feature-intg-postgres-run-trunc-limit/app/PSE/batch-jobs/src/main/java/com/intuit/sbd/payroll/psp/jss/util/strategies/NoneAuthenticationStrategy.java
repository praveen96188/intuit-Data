package com.intuit.sbd.payroll.psp.jss.util.strategies;

import javax.servlet.http.HttpServletRequest;

public class NoneAuthenticationStrategy implements WebHookAuthenticationStrategy {


    @Override
    public boolean authenticate(HttpServletRequest httpServletRequest) {
        return true;
    }
}
