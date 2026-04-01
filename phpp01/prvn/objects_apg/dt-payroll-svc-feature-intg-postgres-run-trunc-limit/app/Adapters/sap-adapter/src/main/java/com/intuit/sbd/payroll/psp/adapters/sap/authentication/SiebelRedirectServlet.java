package com.intuit.sbd.payroll.psp.adapters.sap.authentication;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * User: dweinberg
 * Date: Jul 26, 2010
 * Time: 10:35:16 AM
 */
public class SiebelRedirectServlet  extends HttpServlet {

    private String targetURL;
    private static SpcfLogger logger = PayrollServices.getLogger(SiebelRedirectServlet.class);
    public void init() {
        targetURL = getServletConfig().getInitParameter("target.url");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer target = new StringBuffer(targetURL);
        target.append("#/EIN-Management//EINs/EINs/");
        logger.info("The incoming request from Siebel/SFDC:  "+request.getRequestURL().toString().concat("?"+request.getQueryString()));
        for (Object paramObj : request.getParameterMap().entrySet()) {
            Map.Entry paramEntry = (Map.Entry) paramObj;
            if(!paramEntry.getKey().equals("IV") && !paramEntry.getKey().equals("ReturnValues")) {
                String paramValue = "";
                target.append(paramEntry.getKey());
                target.append("=");
                paramValue = ((String[]) paramEntry.getValue())[0];
                if (paramValue.endsWith("/") || paramValue.startsWith("/")) {
                    paramValue = paramValue.replaceAll("/", "");
                }
                target.append(paramValue);
                target.append("&");
            }
        }
        target.delete(target.length()-1,target.length());

        target.append("/");

        response.sendRedirect(target.toString());

    }


}
