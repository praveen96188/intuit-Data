package com.intuit.sbd.payroll.psp.jss;

import com.intuit.ems.shared.restclient.RestCommandClient;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.ParallelEnvJSSUtils;
import com.intuit.sbg.shared.batchjob.BatchJobConfig;
import com.intuit.qbdt.identity.authN.common.OfflineJobIds;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketAuthNClient;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;

import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.BatchJobConfig;
import com.intuit.sbg.shared.batchjob.jss.client.DefaultRestCommandClient;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration class for all JSS Batch Jobs
 * 
 * 
 * All the IAM related methods are implemented just to satisfy the interface contract, these needs to be overridden only for
 * Private Auth+ authentication mechanism
 * 
 * @author kmuthurangam
 *
 */
@Slf4j
public class JSSBatchJobConfig implements BatchJobConfig {

	@Override
	public String getJSSBaseUrl() {
		return BatchUtils.getConfigString("psp_jss_base_url");
	}

	@Override
	public String getBatchJobPackages() {
		return "com.intuit.sbd.payroll.psp.jss";
	}

	@Override
	public Integer getShutdownWaitSeconds() {
		return getIntegerValue("psp_jss_shutdown_wait_seconds", "10");
	}

	@Override
	public String getPortalEntryName() {
		return BatchUtils.getConfigString("psp_jss_portal_entry_name");
	}

	@Override
	public String getApplicationId() {
		return BatchUtils.getConfigString("psp_jss_app_id");
	}

	@Override
	public String getApplicationSecret() {
		return BatchUtils.getConfigString("psp_jss_app_secret");
	}

	@Override
	public String getGroupName() {
		if (Application.isParallelEnv()) {
			String jssGroupName = BatchUtils.getConfigString("psp_jss_group_name") + "_" + ParallelEnvJSSUtils.getParallelEnvJSSSuffix().toUpperCase();
			log.info("Parallel Env JSS Group Name getParallelEnvSuffix={} jssGroupName={}", ParallelEnvJSSUtils.getParallelEnvJSSSuffix(), jssGroupName);
			return jssGroupName;
		}
		return BatchUtils.getConfigString("psp_jss_group_name");
	}

	@Override
	public String getBaseApplicationUrl() {
		if (Application.isParallelEnv()) {
			String jssBaseApplicationUrl = BatchUtils.getConfigString("psp_jss_base_application_url") + ParallelEnvJSSUtils.getParallelEnvJSSSuffix().toLowerCase();
			log.info("Parallel Env JSS Base Application URL getParallelEnvSuffix={} jssBaseApplicationUrl={}", ParallelEnvJSSUtils.getParallelEnvJSSSuffix(), jssBaseApplicationUrl);
			return jssBaseApplicationUrl;
		}
		return BatchUtils.getConfigString("psp_jss_base_application_url");
	}

	@Override
	public String getFailureNotificationMailingList() {
		return BatchUtils.getConfigString("psp_jss_notification_failure_mailing_list");
	}

	@Override
	public Class<? extends RestCommandClient> getJssRestCommandClient() {
		return JSSRestCommandClient.class;
	}

	@Override
	public Class<? extends RestCommandClient> getAppRestCommandClient() {
		return JSSRestCommandClient.class;
	}


	@Override
	public Integer getRetryDelayMilliseconds() {
		return getIntegerValue("psp_jss_retry_delay_millisecs", "1000");
	}

	@Override
	public Integer getInternalRunRetryDelayMilliseconds() {
		return getIntegerValue("psp_jss_retry_internal_delay_milliseconds", "1000");
	}

	@Override
	public Integer getMaxInternalRunRetries() {
		return getIntegerValue("psp_jss_retry_internal_maxcount", "1");
	}

	@Override
	public String getEnvironment() {
		return BatchUtils.getConfigString("psp_offline_env");
	}

	@Override
	public String getIamAssetId() {
		return BatchUtils.getConfigString("psp_jss_assetid");
	}

	@Override
	public Integer getIamMaxRetries() {
		return 2;
	}

	@Override
	public Long getIamRetryDelay() {
		return 3000l;
	}

	@Override
	public String getIamEndpoint() {
		return BatchUtils.getConfigString("psp_jss_offlineticket_external_endpoint");
	}

	@Override
	public String getIamPassword() {
		return BatchUtils.getConfigString("psp_jss_password");
	}

	@Override
	public String getIamUser() {
		return BatchUtils.getConfigString("psp_jss_username");
	}

	private Integer getIntegerValue(String pSettingName, String pDefaultValue) {
		String settingName = BatchUtils.getConfigString(pSettingName, pDefaultValue);
		return Integer.parseInt(settingName);
	}

	@Override
	public Boolean useLegacyScheduling() {
		return true;
	}

	@Override
	public Boolean useProxy() {
		String proxy = getProxyHost();
		if (StringUtils.isBlank(proxy)) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public Integer getProxyPort() {
		String port = BatchUtils.getConfigString("psp_jss_proxy_port");
		return Integer.parseInt(port);
	}

	@Override
	public HystrixConcurrencyStrategy getHystrixConcurrencyStrategy() {
		return null;
	}

	@Override
	public Set<Integer> getRetryResponseCodes() {
		return Arrays.stream(BatchUtils.getConfigString("psp_jss_retry_response_code").split(",")).map(Integer::new).collect(Collectors.toSet());
	}

	@Override
	public Set<Integer> getRetryResponseCodesForPostCalls() {
		return Arrays.stream(BatchUtils.getConfigString("psp_jss_retry_response_code_post_calls").split(",")).map(Integer::new).collect(Collectors.toSet());
	}

	@Override
	public String getOfflineJobId() {
		return BatchUtils.getConfigString("psp_offline_job_id");

	}

	@Override
	public boolean isIdentity2Enabled() {
		if (FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_ID2_ENABLED_FOR_JSS, true)) {
			return true;
		}
		return false;
}

	@Override
    public String getProxyHost() {

		return BatchUtils.getConfigString("psp_jss_proxy_url");
    }
}
