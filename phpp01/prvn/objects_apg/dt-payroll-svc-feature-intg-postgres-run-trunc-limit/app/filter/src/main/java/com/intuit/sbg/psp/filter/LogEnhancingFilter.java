package com.intuit.sbg.psp.filter;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * LogEnhancingFilter class adds request_uid[Transaction ID] in all existing log statements
 * which helps in debugging particular request using its request_uid
 * LogEnhancingFilter class adds request_uid to MDC
 * if its present in REQUEST HEADER, else new request_uid is generated using SpcfUniqueId
 */
public class LogEnhancingFilter implements Filter {

    private static final String INTUIT_TID = "intuit_tid";
    private final Logger logger = LoggerFactory.getLogger(LogEnhancingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing needed in init method
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        addRequestUIDToMDC(servletRequest);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            removeRequestUIDFromMDC();
        }
    }

    private void addRequestUIDToMDC(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String intuitRequestUID = request.getHeader(INTUIT_TID);
        if (StringUtils.isEmpty(intuitRequestUID)) {
            intuitRequestUID = SpcfUniqueId.generateRandomUniqueIdString();
        }
        MDC.put(INTUIT_TID, "intuit_tid=" + intuitRequestUID);
    }

    private void removeRequestUIDFromMDC() {
        MDC.remove(INTUIT_TID);
    }

    @Override
    public void destroy() {
        // Nothing needed in destroy method
    }

}
