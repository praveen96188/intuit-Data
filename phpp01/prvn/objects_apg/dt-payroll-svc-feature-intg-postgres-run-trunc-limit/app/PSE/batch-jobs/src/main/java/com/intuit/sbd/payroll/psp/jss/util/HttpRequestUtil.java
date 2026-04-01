package com.intuit.sbd.payroll.psp.jss.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class HttpRequestUtil {

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIPAddress(HttpServletRequest httpServletRequest) {
        for(String header : HEADERS_TO_TRY) {
            String ip = httpServletRequest.getHeader(header);
            if(!Objects.isNull(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return httpServletRequest.getRemoteAddr();
    }
}
