package com.intuit.sbd.payroll.psp.adapters.sap.util;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class SapAppUtil {
    private static SpcfLogger logger = PayrollServices.getLogger(SapAppUtil.class);

    public static boolean isSapAppSSOURL(HttpServletRequest request) {
        if(request.getRequestURL().toString().contains("sapapp")) {
            return true;
        }
        return false;
    }

    public static String getSSOLoginUrl(boolean isSAPApp) {
        String configParameterName = isSAPApp ? "sapsso_app_login_url": "sapsso_login_url";
        return ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, configParameterName);

    }

    public static String getRequestDispatchURL(boolean isSAPApp) {
        return isSAPApp ? "/WEB-INF/SAPApp.jsp": "/WEB-INF/SAP.jsp";
    }

    public static String getCustomURLScheme(HttpServletRequest request) {

        StringBuilder customUrl = new StringBuilder();
        customUrl.append("sapapp://ssologin?");
        customUrl.append("corpId="+request.getAttribute("corpId"));
        customUrl.append("&authToken="+request.getAttribute("authToken"));
        customUrl.append("&serverName="+request.getServerName());
        customUrl.append("&authId="+request.getAttribute("authId"));
        customUrl.append("&ticket="+request.getAttribute("ticket"));
        customUrl.append("&realmId="+request.getAttribute("realmId"));
        customUrl.append("&emailAddress="+request.getAttribute("emailAddress"));

        if(StringUtils.isNotEmpty(request.getQueryString())){
            String serviceAccountId=request.getParameter("serviceAccountId");
            String licenseNumber=request.getParameter("licenseNumber");
            String itemNumber=request.getParameter("itemNumber");
            String eoc=request.getParameter("eoc");

            if(StringUtils.isNotEmpty(serviceAccountId) && StringUtils.isNotEmpty(licenseNumber)
                    && StringUtils.isNotEmpty(itemNumber)  && StringUtils.isNotEmpty(eoc) ){
                logger.info("The incoming request from Siebel/SFDC to SAP App = "+request.getQueryString());
                customUrl.append("&serviceAccountId="+serviceAccountId);
                customUrl.append("&licenseNumber="+licenseNumber);
                customUrl.append("&itemNumber="+itemNumber);
                customUrl.append("&eoc="+eoc);
            }
        }

        return customUrl.toString();
    }
}
