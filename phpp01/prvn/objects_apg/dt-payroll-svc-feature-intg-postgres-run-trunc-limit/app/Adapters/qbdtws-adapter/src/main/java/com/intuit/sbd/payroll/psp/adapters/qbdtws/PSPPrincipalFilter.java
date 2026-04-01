package com.intuit.sbd.payroll.psp.adapters.qbdtws;

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
 * User: rnorian
 * Date: Dec 24, 2009
 * Time: 3:49:33 PM
 */
public class PSPPrincipalFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.QBDTWSAdapter);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
    }
}
