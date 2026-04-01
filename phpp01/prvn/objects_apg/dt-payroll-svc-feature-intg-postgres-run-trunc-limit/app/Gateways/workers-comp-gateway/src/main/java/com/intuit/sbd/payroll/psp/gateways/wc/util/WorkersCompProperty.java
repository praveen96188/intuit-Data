package com.intuit.sbd.payroll.psp.gateways.wc.util;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;

import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompURIEnum.*;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompServiceEnum.*;

/**
 * Author: Sriram Nutakki
 * Date created: 11/8/12
 */
public class WorkersCompProperty {
    public enum WorkersCompServiceEnum {
        WC_SERVICE("wc.gateway.wcService");

        private String value;
        private final String URI = ".uri";
        private final String AWS = ".aws";
        private final String HOST = ".host";

        private String AWSHost;
        private String AWSUri;

        WorkersCompServiceEnum(String value) {
            this.value = value;
            this.AWSHost = getConfigValue(this.value + AWS + HOST);
            this.AWSUri = value + AWS + URI;
        }

        public String getAWSHost() {
            return AWSHost;
        }

        public String getAWSUri() {
            return AWSUri;
        }
    }

    public enum WorkersCompURIEnum {
        GET_SUBSCRIPTIONS_URI(".getSubscriptions"),
        POST_SUBSCRIPTION_CONFIRMATION_URI(".postSubscriptionConfirmation"),
        POST_PAYROLL_URI(".postPayroll"),
        GET_DISPLAY_DATA_FOR_HELP_DESK_URI(".getDisplayDataForHelpdesk"),
        CHANGE_EVENTS_URI(".changeEvents");

        private String value;

        WorkersCompURIEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum WorkersCompURLEnum {
        WC_SERVICE_GET_SUBSCRIPTIONS_URL(
                GET_SUBSCRIPTIONS_URI
        ),
        WC_SERVICE_POST_SUBSCRIPTIONS_CONFIRMATION_URL(
                POST_SUBSCRIPTION_CONFIRMATION_URI
        ),
        WC_SERVICE_POST_PAYROLL_URL(
                POST_PAYROLL_URI
        ),
        WC_SERVICE_GET_DISPLAY_DATA_FOR_HELPDESK_URL(
                GET_DISPLAY_DATA_FOR_HELP_DESK_URI
        ),
        WC_SERVICE_POST_CHANGE_EVENTS_URL(
                CHANGE_EVENTS_URI
        );

        private String AWSUrl;

        WorkersCompURLEnum(WorkersCompURIEnum propURI) {
            this.AWSUrl = WC_SERVICE.getAWSHost() + getConfigValue(WC_SERVICE.getAWSUri() + propURI.getValue());
        }

        public String getAWSUrl() {
            return AWSUrl;
        }
    }

    public enum WorkersCompPropEnum {
        PUSH_PAYROLL_COMPANIES_BATCH_SIZE(getConfigValue("wc.gateway.push.payroll.companies.batchSize")),
        PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE(getConfigValue("wc.gateway.push.payroll.paychecks.batchSize"));

        private String value;

        WorkersCompPropEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getValueAsInt() {
            return Integer.parseInt(getValue());
        }
    }

    private static String getConfigValue(String key) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.WorkersCompGateway, key);
    }
}
