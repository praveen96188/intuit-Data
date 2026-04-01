package com.intuit.sbg.psp.filter;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PSPRequestContextFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(PSPRequestContextFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing needed in init method
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            setRequestContext(request);
            chain.doFilter(request, response);
        } finally {
            clearRequestContext();
        }
    }

    private void setRequestContext(ServletRequest request){
        try{
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String requestURI = httpServletRequest.getRequestURI();
            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContext(null, RequestType.REST, requestURI);
        } catch(Exception e){
            logger.error("Unable to set request context", e);
        }
    }

    private void clearRequestContext(){
        try{
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
        } catch(Exception e){
            logger.error("Unable to clear request context", e);
        }
    }

    @Override
    public void destroy() {
        // Nothing needed in destroy method
    }
}
