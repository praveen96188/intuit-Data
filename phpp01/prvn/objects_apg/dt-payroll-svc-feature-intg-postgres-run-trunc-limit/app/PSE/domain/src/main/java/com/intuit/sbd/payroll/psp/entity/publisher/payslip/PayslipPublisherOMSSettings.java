package com.intuit.sbd.payroll.psp.entity.publisher.payslip;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;

public class PayslipPublisherOMSSettings {

    public static String getJmsUrl() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_jms_url");
    }

    public static String getPulsorUrl() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_pulsor_url");
    }

    public static String getUsername() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_username");
    }

    public static String getPassword() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_password");
    }

    public static String getQueueName() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_payslip_queue_name");
    }

    public static String getIdpsPolicyId(){
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_idps_policy_id");
    }

    public static String getIdpsEndPoint(){
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_idps_endpoint");
    }

    public static String getIdpsQualifiedPrivateKeyName(){
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_idps_payslip_qualifiedPrivateKeyName");
    }

    public static String getIdpsSubscriberNameSpace(){
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "oms_idps_payslip_subscriberNameSpace");
    }

    public String getPSPAppId(){
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_appid");
    }
}