package com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 26, 2010
 * Time: 1:55:51 PM
 */
public class HttpRequest {
    private String url;
    private Map<String, String> parameters;

    public String getUrl() {
        return url;
    }

    public void setUrl(String pUrl) {
        url = pUrl;
    }

    public Map<String, String> getParameters() {
        if(parameters == null){
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    public String sendGetRequest() throws Exception {
        String result = null;
        if (getUrl().length() > 0 && getUrl().startsWith("http"))
        {
            StringBuffer requestURL = new StringBuffer(getUrl());
            if (!getParameters().isEmpty())
            {
                requestURL.append("?");
                for (String name : parameters.keySet()) {
                    requestURL.append(name).append("=");
                    requestURL.append(URLEncoder.encode(parameters.get(name), "UTF-8"));
                    requestURL.append("&");
                }
                // remove the last &
                requestURL.deleteCharAt(requestURL.length()-1);
            }
            URL url = new URL(requestURL.toString());

            String proxyAddress = ConfigurationManager.getSettingValue
                (ConfigurationModule.TaxCreditsAdapter, "proxyAddress");
            String proxyPort = ConfigurationManager.getSettingValue
                (ConfigurationModule.TaxCreditsAdapter, "proxyPort");
            URLConnection conn;
            if (proxyAddress == null || proxyPort == null || proxyAddress.length() == 0 || proxyPort.length() == 0) {
                conn = url.openConnection();
            } else {
                conn = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, Integer.parseInt(proxyPort))));
            }


            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null)
            {
                sb.append(line);
            }
            rd.close();
            result = sb.toString();
        }
        return result;
    }
}
