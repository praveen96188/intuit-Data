package com.intuit.sbd.payroll.psp.iam;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class VerifyAppId {

    private List<String> appIdList;

    public VerifyAppId(String appListString){
        Validate.notEmpty(appListString, "DIS adapter supported Apps list can not be empty");
        this.appIdList = Arrays.asList(appListString.split(","));
    }

    public boolean validateAppId(String authorisation) {
        String[] headerComponents = authorisation.split(",");
        for (String s : headerComponents) {
            String[] headerDetails = s.split("=");
            String headerKey = headerDetails[0].replace("\"", "").trim();
            String headerValue = headerDetails[1].replace("\"", "").trim();
            log.info("appId: "+headerValue);
            if (headerKey.contains("intuit_appid")) {
                if (appIdList.contains(headerValue)) {
                    return true;
                }
            }
        }
        return false;
    }
}
