package com.intuit.sbd.payroll.psp.jss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.sbg.shared.batchjob.BatchJobConfig;
import com.intuit.sbg.shared.batchjob.BatchJobConfigFactory;

import com.intuit.sbg.shared.batchjob.iam.IAMClientWrapper;
import com.intuit.sbg.shared.batchjob.iam.IAMClientWrapperV2;
import com.intuit.sbg.shared.batchjob.jss.client.DefaultRestCommandClient;
import com.intuit.sbg.shared.batchjob.jss.client.IntuitRequestHeaders;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.slf4j.MDC;

/**
 * <pre>
 * Extending the default Rest client to
 *  - Add application specific authorization header
 *  - Add logger
 * 	- Override custom default hystrix command properties
 * </pre>
 * 
 * @author kmuthurangam
 *
 */
public class JSSRestCommandClient extends DefaultRestCommandClient {
	private SpcfLogger logger = SpcfLogManager.getLogger(JSSRestCommandClient.class);
	private static Map<String, String> contentTypeParameters = new HashMap<String, String>();
	private static BatchJobConfig batchJobConfig;

	static {
		contentTypeParameters.put("charset", "utf-8");
		try {
			batchJobConfig = BatchJobConfigFactory.createInstance();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	protected List<Class<? extends ClientFilter>> getLoggingFilters() {
		List<Class<? extends ClientFilter>> filters = new ArrayList<Class<? extends ClientFilter>>();
		// Removing the jersey logging
		//filters.add(com.sun.jersey.api.client.filter.LoggingFilter.class);
		return filters;
	}

	@Override
	public HystrixCommandProperties.Setter getDefaultHystrixCommandProperties() {
		return HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(40000)
				.withCircuitBreakerEnabled(true).withFallbackIsolationSemaphoreMaxConcurrentRequests(5)
				.withExecutionIsolationSemaphoreMaxConcurrentRequests(5);
	}
	@Override
	public MediaType getContentType() {
		return new MediaType("application", "json", contentTypeParameters);
	}

	protected String getApplicationId() {
		return batchJobConfig.getApplicationId();
	}

	protected String getApplicationSecret() {
		return batchJobConfig.getApplicationSecret();
	}
}
