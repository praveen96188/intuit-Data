package com.intuit.sbd.payroll.psp.jss.util.strategies;

import com.intuit.sbd.payroll.psp.jss.util.HttpRequestUtil;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class SourceIPBasedAuthenticationStrategy implements WebHookAuthenticationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceIPBasedAuthenticationStrategy.class);
    private static final String allowedIpCIDR = "103.15.250.0/24";

    @Override
    public boolean authenticate(HttpServletRequest httpServletRequest) {

        String sourceIPAddress = HttpRequestUtil.getClientIPAddress(httpServletRequest);
        String[] ips = sourceIPAddress.split(",");
        LOGGER.info("sourceIPAddress = {}", sourceIPAddress);
        LOGGER.info("allowedIpCIDR = {}", allowedIpCIDR);
        SubnetUtils subnetUtils = new SubnetUtils(allowedIpCIDR);
        return Arrays.stream(ips).anyMatch(ip -> subnetUtils.getInfo().isInRange(ip));
    }
}
