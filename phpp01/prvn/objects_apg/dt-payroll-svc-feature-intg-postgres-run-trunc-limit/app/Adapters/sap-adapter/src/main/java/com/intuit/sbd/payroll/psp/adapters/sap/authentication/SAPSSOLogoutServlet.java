package com.intuit.sbd.payroll.psp.adapters.sap.authentication;


import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;


import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;


public class SAPSSOLogoutServlet extends HttpServlet {

    private static SpcfLogger logger = PayrollServices.getLogger(SAPSSOLogoutServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            eraseCookie(request, response);
            String logoutUrl = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "sapsso_logout_url");
            request.getSession().invalidate();
            response.sendRedirect(logoutUrl);

        }catch (Throwable t) {
            logger.error("Error determining logged in user", t);
        }
    }


    private void eraseCookie(HttpServletRequest req, HttpServletResponse resp) {
        try{

            Cookie[] cookies = req.getCookies();
            if(cookies !=null){
            for (Cookie cookie : cookies) {

                //Set the browser cookies expiry on logout
                Cookie browserCookie = new Cookie(cookie.getName(), "");
                browserCookie.setPath("/SAP");
                if(cookie.getName().equals("JSESSIONID")) {
                    browserCookie.setPath("/SAP/");
                }
                browserCookie.setDomain(req.getServerName());
                browserCookie.setMaxAge(0);
                resp.addCookie(browserCookie);
                logger.debug("Browser cookies: "+ browserCookie.getName()+"-" +browserCookie.getValue()+"-" +browserCookie.getPath()+"-" +browserCookie.getDomain());

                //Adding logoutCookie for remaining tabs
                logger.info("Adding logoutCookie since  logout is called");
                Cookie logoutCookie = new Cookie("ssoLogoutCookie", "true");
                logoutCookie.setPath("/SAP");
                logoutCookie.setDomain(req.getServerName());
                resp.addCookie(logoutCookie);
                logger.debug("Added logoutCookie: "+ logoutCookie.getName()+"-" +logoutCookie.getValue()+"-" +logoutCookie.getPath()+"-" +logoutCookie.getDomain());

            }}
        } catch (Throwable t) {
            logger.error("Error invalidating cookies/session for the user", t);
            PayrollServices.rollbackUnitOfWork();
        }
    }


}