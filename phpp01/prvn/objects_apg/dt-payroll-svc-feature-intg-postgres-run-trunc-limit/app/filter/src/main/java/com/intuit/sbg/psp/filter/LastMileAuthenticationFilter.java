package com.intuit.sbg.psp.filter;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbg.psp.filter.exception.LastMileAuthException;
import com.intuit.sbg.psp.filter.helper.LastMileAuthHelper;
import com.intuit.sbg.psp.filter.service.LastMileAuthenticationService;
import com.intuit.sbg.psp.filter.service.constants.Constants;
import org.apache.commons.collections4.SetValuedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author rn5 
 * Any requests to the application should have authentication tokens
 * and requests should come only through gateways.
 *
 */
public class LastMileAuthenticationFilter implements Filter {

	private LastMileAuthenticationService lastMileAuthenticationService;
	private LastMileAuthHelper lastMileAuthHelper;
	private SetValuedMap<String,String> ignorePathMap;
	private final Logger logger = LoggerFactory.getLogger(LastMileAuthenticationFilter.class);

	public LastMileAuthenticationFilter() {
		logger.info("Initializing Last Mile Authentication Filter.");
		try {
			//Get configuration parameters
			String keystorePath = ConfigurationManager.getSettingValue(ConfigurationModule.Common, Constants.KEYSTORE_PATH);
			String keystorePassword = ConfigurationManager.getSettingValue(ConfigurationModule.Common, Constants.KEYSTORE_PASSWORD);
			String targetAudience = ConfigurationManager.getSettingValue(ConfigurationModule.Common, Constants.TARGET_AUDIENCE);
			String ignorePaths = ConfigurationManager.getSettingValue(ConfigurationModule.Common, Constants.IGNORE_PATH);
			
			//Initialize last mile auth.
			lastMileAuthHelper = new LastMileAuthHelper();
			Set<String> targetAudienceSet = lastMileAuthHelper.getUrlPattternSet(targetAudience);
			ignorePathMap = lastMileAuthHelper.getIgnoreUrlPattternMap(ignorePaths);
			lastMileAuthenticationService = new LastMileAuthenticationService(keystorePath, keystorePassword,
					targetAudienceSet);
			
			logger.info("Initialized Last Mile Authentication Filter.");
		} catch (Exception e) {
			throw new LastMileAuthException("Failed to configure LMA filter. Please check LMA Configuration parameters.", e);
		}
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    boolean isLMAEnabled = Boolean.parseBoolean(ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_LMA_ENABLED"));

    String requestURI = httpServletRequest.getRequestURI();

    // Ignore Health check URLs
    String requestUrl = httpServletRequest.getRequestURL().toString();
    if (lastMileAuthHelper.isFilterSkipRequired(requestUrl, requestURI, ignorePathMap.get(Constants.COMMON_HTTP_METHOD))
            || lastMileAuthHelper.isFilterSkipRequired(requestUrl, requestURI, ignorePathMap.get(httpServletRequest.getMethod()))) {
      filterChain.doFilter(request, response);
      return;
    }

    // Intuit client context header is missing.
    String intuitClientContext = httpServletRequest.getHeader(Constants.INTUIT_CLIENT_CONTEXT);
    if (intuitClientContext == null || intuitClientContext.trim().isEmpty()) {
      logger.error(
          "LMA Auth Failure. LMA is enabled but LMA Header intuit_clientcontext is missing. URL={}, LMA Enabled = {}",
          requestUrl, isLMAEnabled);
      if (isLMAEnabled) {
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      } else {
        filterChain.doFilter(request, response);
        return;
      }
    }

    // Do LMA Authentication
    try {
      lastMileAuthenticationService.authenticate(intuitClientContext);
    } catch (LastMileAuthException e) {
      logger.error("LMA Auth Failure. URL={}, LMA Enabled = {}", requestUrl, isLMAEnabled, e);
      if (isLMAEnabled) {
        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      } else {
        filterChain.doFilter(request, response);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void destroy() {

	}

}
