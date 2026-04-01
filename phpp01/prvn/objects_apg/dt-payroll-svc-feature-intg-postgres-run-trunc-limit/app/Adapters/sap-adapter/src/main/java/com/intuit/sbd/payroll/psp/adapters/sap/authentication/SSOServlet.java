package com.intuit.sbd.payroll.psp.adapters.sap.authentication;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * User: dweinberg
 * Date: Jun 1, 2010
 * Time: 5:01:58 PM
 */
public class SSOServlet extends HttpServlet {

    private static final Pattern corpIdPattern = Pattern.compile(".*intuitCorpID=(\\w+).*",Pattern.CASE_INSENSITIVE);

    private static SpcfLogger logger = PayrollServices.getLogger(SSOServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        AuthUser user = null;
        try {
            PayrollServices.beginUnitOfWork();
            String corpId = getSSOLoggedInUserId(request);

            // Added logs to identify whether Oracle NetPoint code is getting used
            logger.info(String.format("Successfully identified the user[%s] from ObSSOCookie", StringUtils.defaultIfEmpty(corpId, "Not Found")));

            if (corpId != null) {
                user = AuthUser.findUser(corpId);
            }
        } catch (Throwable t) {
            logger.error("Error determining logged in user", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        if (user == null) {
            request.setAttribute("corpId", "notFound");
            request.setAttribute("authToken", "none");
        } else {
            try {
                PayrollServices.beginUnitOfWork();

                ProcessResult<String> newAuthTokenPR = PayrollServices.userManager.updateUserAuthorizationToken(user.getCorpId());
                if (!newAuthTokenPR.isSuccess()) {
                    logger.warn("Could not update user auth token: " + newAuthTokenPR.getMessages().toString());
                    PayrollServices.rollbackUnitOfWork();
                } else {
                    PayrollServices.commitUnitOfWork();
                }

                PayrollServices.beginUnitOfWork();
                ProcessResult updateLRCTimestampPR = PayrollServices.userManager.updateUserLastRemoteCallTimestamp(user.getCorpId());
                if (!updateLRCTimestampPR.isSuccess()) {
                    logger.error("Could not update last remote call timestamp: " + updateLRCTimestampPR.getMessages().toString());
                    PayrollServices.rollbackUnitOfWork();
                } else {
                    PayrollServices.commitUnitOfWork();
                }

                if (!newAuthTokenPR.isSuccess() || !updateLRCTimestampPR.isSuccess()) {
                    request.setAttribute("corpId", "notFound");
                    request.setAttribute("authToken", "none");
                } else {
                    request.setAttribute("corpId", user.getCorpId());
                    request.setAttribute("authToken", newAuthTokenPR.getResult());
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/SAP.jsp");
        view.forward(request, response);
    }

    /**
     * Reads SSO cookie and determines identity of logged in user.
     * @param request httpRequest
     * @return if user not logged in, null; otherwise, logged-in user's corpid
     */
    private String getSSOLoggedInUserId(HttpServletRequest request) {
        try {
            Cookie ssoCookie=null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("ObSSOCookie")) {
                        ssoCookie = cookie;
                        break;
                    }
                }                
            }

            if (ssoCookie == null) {
                logger.info("No SSO cookie found");
                return null;
            }

            logger.warn("SSO coockie found, Still returning null user");
            return null;
        } catch (Throwable t) {
            logger.error("Unexpected error verifying SSO logged-in user", t);
            return null;
        }
    }
}
