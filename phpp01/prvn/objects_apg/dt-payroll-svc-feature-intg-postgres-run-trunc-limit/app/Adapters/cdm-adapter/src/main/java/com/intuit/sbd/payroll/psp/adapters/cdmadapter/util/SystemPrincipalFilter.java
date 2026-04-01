package com.intuit.sbd.payroll.psp.adapters.cdmadapter.util;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import javax.servlet.*;
import java.io.IOException;

public class SystemPrincipalFilter implements Filter {
    @Override
    public void init(FilterConfig pFilterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest pServletRequest, ServletResponse pServletResponse, FilterChain pFilterChain) throws IOException, ServletException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.CdmAdapter);
        pFilterChain.doFilter(pServletRequest, pServletResponse);
    }

    @Override
    public void destroy() {
    }
}
