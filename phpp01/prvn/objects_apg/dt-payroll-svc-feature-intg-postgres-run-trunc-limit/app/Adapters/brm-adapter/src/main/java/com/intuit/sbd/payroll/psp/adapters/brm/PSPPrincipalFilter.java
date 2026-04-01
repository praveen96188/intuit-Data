package com.intuit.sbd.payroll.psp.adapters.brm;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 *
 */
public class PSPPrincipalFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.BRMAdapter);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
    }
}
