package com.intuit.sbd.payroll.psp.common.utils;

import org.apache.commons.lang3.StringUtils;

public class ParallelEnvJSSUtils {

    private static final String PARALLEL_ENV_SUFFIX = "PARALLEL_ENV_SUFFIX";

    private static final String DEFAULT_SUFFIX = "default";
    private static final String ALPHA_SUFFIX = "alpha";
    private static final String BETA_SUFFIX = "beta";

    public static String getParallelEnvJSSSuffix() {
        String suffixEnvVariable = System.getenv(PARALLEL_ENV_SUFFIX);

        if (StringUtils.isEmpty(suffixEnvVariable)) {
            return DEFAULT_SUFFIX;
        }

        if (suffixEnvVariable.equals(DEFAULT_SUFFIX) || suffixEnvVariable.equals(ALPHA_SUFFIX) || suffixEnvVariable.equals(BETA_SUFFIX)) {
            return suffixEnvVariable;
        }

        return DEFAULT_SUFFIX;
    }

}
