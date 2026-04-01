package com.intuit.sbd.payroll.psp.adapters.sap.authentication;


import com.intuit.platform.jsk.security.iam.util.IntuitAuthCookiesHelper;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.util.SapAppUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.generalutils.AESUtility;
import org.apache.commons.lang.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class SAPSSOServlet extends HttpServlet {
    private static final Pattern corpIdPattern = Pattern.compile(".*intuitCorpID=(\\w+).*", Pattern.CASE_INSENSITIVE);

    public static final String SSO_KEY = null;
    public static final String CORP_ID = "intuitcorpid";
    public static final String PARAM_IV = "IV";
    public static final String PARAM_RETURN_VALUES = "ReturnValues";
    public static final String UID = "userid";
    public static final String SERVICE_ACCOUNT_ID="serviceAccountId";
    public static final String LICENSE_NUMBER="licenseNumber";
    public static final String ITEM_NUMBER="itemNumber";
    public static final String EOC ="eoc";
    private static SpcfLogger logger = PayrollServices.getLogger(SAPSSOServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        AuthUser user = null;
        String ssoLoginUrl = null;
        String corpId=null;
        String authKey =(String)request.getSession().getAttribute("authKey");
        boolean isSAPApp = SapAppUtil.isSapAppSSOURL(request);

        try {
            ssoLoginUrl = SapAppUtil.getSSOLoginUrl(isSAPApp);
            Map<String,String> ssoParamReturnValuesMap = getSsoParamReturnValuesMap(request);
            setCookiesForSsoParams(ssoParamReturnValuesMap,request,response);
            corpId = ssoParamReturnValuesMap.get(CORP_ID);

            if (corpId != null && !corpId.equals("")) {
                String appType = isSAPApp?"Desktop":"Browser";
                logger.info("Received request from app=" + appType + " for corpId=" + corpId );
                user = AuthUser.findUser(corpId);
            }else{
                logger.info("CorpId is null");
                response.sendRedirect(ssoLoginUrl);
                eraseCookie(request,response);
                return;
            }

            if (user == null) {

                logger.info("SAPUser value is null");
                request.setAttribute("corpId", "notFound");
                request.setAttribute("authToken", "none");
                request.setAttribute("ssoLoginUrl", ssoLoginUrl);
                RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/Error.jsp");
                view.forward(request, response);
                return;
                } else {
                            logger.info("AuthKey value is null");
                            PayrollServices.beginUnitOfWork();
                            ProcessResult<String> newAuthTokenPR = PayrollServices.userManager.updateUserAuthorizationToken(user.getCorpId());
                            logger.info("SAPSSOServlet: New Authkey value: "+newAuthTokenPR.getResult());
                            if (!newAuthTokenPR.isSuccess()) {
                                logger.warn("Could not update user auth token: " + newAuthTokenPR.getMessages().toString());
                                PayrollServices.rollbackUnitOfWork();
                            } else {
                                logger.info("Fetching authToken from session");
                                request.getSession().setAttribute("authToken", newAuthTokenPR);
                                logger.info("SAPSSOServlet: Authkey value from session : "+newAuthTokenPR.getResult());
                                authKey=newAuthTokenPR.getResult();
                                response.addHeader("authKey", newAuthTokenPR.getResult());
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


                        logger.info("AuthKey is not null");
                        request.getSession().setAttribute("corpId", user.getCorpId());
                        request.getSession().setAttribute("authToken", authKey);

                        response.addHeader("corpId", user.getCorpId());
                        response.addHeader("authToken", authKey);

                        request.setAttribute("corpId", user.getCorpId());
                        request.setAttribute("authToken", authKey);

                        // Set email address attribute. Is null if 'mail' key is not there.
                        request.setAttribute("emailAddress", ssoParamReturnValuesMap.get("mail"));

                        setIAMAttributes(request, ssoParamReturnValuesMap);

                        if(isSAPApp) {
                            request.setAttribute("customURLScheme", SapAppUtil.getCustomURLScheme(request));
                        }

                if(!isSAPApp && request.getQueryString()!=null && request.getQueryString().contains(SERVICE_ACCOUNT_ID) && request.getQueryString().contains(ITEM_NUMBER)
                                && request.getQueryString().contains(LICENSE_NUMBER)&& request.getQueryString().contains(EOC)){
                            RequestDispatcher view = request.getRequestDispatcher("/SAPSSORedirect");
                            view.forward(request, response);
                            return;
                        }

                }

            String requestDispatchURL = SapAppUtil.getRequestDispatchURL(isSAPApp);
            RequestDispatcher view = request.getRequestDispatcher(requestDispatchURL);
            view.forward(request, response);
        }
        catch (Exception e){
            logger.debug("Failure in SSO Servlet" +e.getMessage());
            eraseCookie(request,response);
            response.sendRedirect(ssoLoginUrl);
        }
        finally {

            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Set SSO Params in cookies for if the request is coming from SSO after authentication or page refresh.
     * @param ssoParamReturnValuesMap
     * @param request
     * @param response
     */
    private void setCookiesForSsoParams(Map<String,String> ssoParamReturnValuesMap, HttpServletRequest request, HttpServletResponse response) {
        String sso_param_iv = request.getParameter(PARAM_IV);
        String sso_param_returnValues = request.getParameter(PARAM_RETURN_VALUES);
        if(StringUtils.isNotEmpty(sso_param_iv) && StringUtils.isNotEmpty(sso_param_returnValues)){
            // User is coming just after SSO login.
            // If the user is already login the ssoParamReturnValues will already be there in his cookies.
            eraseCookie(request,response);
            Cookie paranIVcookie = new Cookie(PARAM_IV,sso_param_iv);
            Cookie paramReturnValuescookie = new Cookie(PARAM_RETURN_VALUES,sso_param_returnValues);
            response.addCookie(paranIVcookie);
            response.addCookie(paramReturnValuescookie);
        }
    }

    /**
     * Get sso_param_returnValues from request and convert it into map
     * if user is coming after SSO login or get the data from cookies
     * and create the map.
     * @param request
     * @return
     */
    private Map<String,String> getSsoParamReturnValuesMap(HttpServletRequest request) {
        Map<String, String> result;
        String sso_param_iv = request.getParameter(PARAM_IV);
        String sso_param_returnValues = request.getParameter(PARAM_RETURN_VALUES);
        if(StringUtils.isEmpty(sso_param_iv) || StringUtils.isEmpty(sso_param_returnValues)){
            //User have directly hit the "SAP/authenticate" URL
            //so the sso_param_returnValues will not be there in request
            //Check the data in cookies
            Cookie[] cookies = request.getCookies();
            if(cookies !=null){
                for(Cookie cookie : cookies){
                    switch (cookie.getName()) {
                        case PARAM_IV :
                            sso_param_iv = cookie.getValue();
                            break;
                        case PARAM_RETURN_VALUES :
                            sso_param_returnValues = cookie.getValue();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        result = decryptSsoParamReturnValue(sso_param_iv, sso_param_returnValues);
        return result;
    }

    /**
     * Decrypt the values of the SSO params and set it to map.
     * @param sso_param_iv
     * @param sso_param_returnValues
     */
    private Map<String, String> decryptSsoParamReturnValue(String sso_param_iv, String sso_param_returnValues) {
        Map<String, String> result = new HashMap<>();
        AESUtility aesutil = new AESUtility();
        String sso_key = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "sapsso_decryptKey");
        String decryptedStr = aesutil.decryptCBC(sso_param_returnValues, sso_key, sso_param_iv);
        StringTokenizer st = new StringTokenizer(decryptedStr, "|");

        //set username corpid and role in the session
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            String[] nameVal = str.split("=", 0);
            if(nameVal.length == 2 && StringUtils.isNotEmpty(nameVal[0]) && StringUtils.isNotEmpty(nameVal[1]))
                result.put(nameVal[0],nameVal[1]);
        }
        return result;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        logger.info("Calling Init for SAPSSOServlet");

    }

    @Override
    public void destroy() {

        logger.info("Calling Destroy() for SAPSSOServlet");
    }

    private void eraseCookie(HttpServletRequest req, HttpServletResponse resp) {
        try{
            Cookie[] cookies = req.getCookies();
            logger.info("Erasing existing cookies");
            if(cookies !=null){
                for (Cookie cookie : cookies) {
                    cookie = new Cookie(cookie.getName(), "");

                    if(cookie.getName().equals("JSESSIONID")){
                        cookie.setPath("/SAP/");
                    }else{
                        cookie.setPath("/SAP");
                    }
                    cookie.setDomain(req.getServerName());
                    cookie.setMaxAge(0);
                    resp.addCookie(cookie);
                }
            }
        } catch (Throwable t) {
            logger.error("Error invalidating cookies/session for the user", t);
        }
    }

    private void setIAMAttributes(HttpServletRequest request, Map<String, String> ssoParamReturnValuesMap) {
        IntuitAuthCookiesHelper.AuthCookieNames authCookieNames = getAuthCookieNames();
        request.setAttribute("ticket", nullSafeGet(ssoParamReturnValuesMap, authCookieNames.getTicketCookieName()));
        request.setAttribute("authId", nullSafeGet(ssoParamReturnValuesMap, authCookieNames.getAuthIdCookieName()));
        request.setAttribute("realmId", nullSafeGet(ssoParamReturnValuesMap, authCookieNames.getRealmIdCookieName()));
    }

    private IntuitAuthCookiesHelper.AuthCookieNames getAuthCookieNames() {
        return new IntuitAuthCookiesHelper(Application.isProdEnvironment()).getAuthCookieNames();
    }

    private String nullSafeGet(Map<String, String> ssoParamReturnValuesMap, String key) {
        return StringUtils.defaultIfEmpty(ssoParamReturnValuesMap.get(key), StringUtils.EMPTY);
    }

}