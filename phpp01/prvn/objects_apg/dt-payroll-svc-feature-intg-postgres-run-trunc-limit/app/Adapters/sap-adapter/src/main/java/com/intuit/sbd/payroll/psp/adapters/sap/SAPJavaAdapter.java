package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.authentication.SAPAuthenticationManager;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.DataServiceExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.FlexContext;
import flex.messaging.MessageException;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: rnorian
 * Date: Jun 16, 2008
 * Time: 9:27:20 PM
 */
public class SAPJavaAdapter extends JavaAdapter {

    private static SpcfLogger logger = PayrollServices.getLogger(SAPJavaAdapter.class);

    private DataServiceExceptionFactory dseFactory = new DataServiceExceptionFactory(logger);

    private static SAPAuthenticationManager sapAuthenticationManager = new SAPAuthenticationManager();

    private static final Pattern PATTERN_QBDT = Pattern.compile("/Company/QBDT:(.*?)/.*");

    private static final Pattern PATTERN_IOP = Pattern.compile("/Company/IOP:(.*?)/.*");

    // THESE CONSTANTS MUST BE SYNCED WITH ApplicationSession.as
    public static final String SAP_SESSION_EXPIRED = "1001";
    public static final String SAP_SESSION_TERMINATED = "1002";
    public static final String SAP_UNAUTHORIZED_REMOTE_CALL = "1003";

    /*
     * The invoke() method gets executed on every remote call from Flex to Java.  Our implementation of this method
     * extends the default method to strip off the corpId and authToken of the current user and verifies that this
     * is a valid and authorized remote service request, before passing the control back to the default super.invoke() method.
     */
    @Override
    public Object invoke(Message message) {
        long startTime= System.currentTimeMillis();
        try{

        String corpId = (String) message.getHeader("corpId");
        String authorizationToken = (String) message.getHeader("authorizationToken");
            Cookie[] cookies = FlexContext.getHttpRequest().getCookies();
        String ivCookie=null;
        String ssoLogoutCookie = null;

        for(Cookie cookie:cookies){
            if(cookie.getName().equals("sso_param_iv")){
                ivCookie =cookie.getValue().toString();
            }else if(cookie.getName().equals("ssoLogoutCookie")){
                ssoLogoutCookie = cookie.getValue().toString();
            }
        }
        if(corpId ==null){
        RemotingMessage remotingMessage = (RemotingMessage)message;
        List params =remotingMessage.getParameters();
        if(params !=null && params.size()>0 && message.getHeader("corpId")==null){
            corpId=(String)params.get(0);
            message.setHeader("corpId",corpId);
        }else{
                corpId = (String) message.getHeader("corpId");
            }
        }

        validateLoginParameters(corpId, authorizationToken);

        AuthUser user = getLoggedInUser(corpId);
        List<String> userRoles = getRoles(corpId);

        try {
            if(((RemotingMessage) message).getOperation().contains("getAutomationJobList") && userRoles.contains("RTBAutomationAdmin")||
                    (!((RemotingMessage) message).getOperation().contains("getAutomationJobList") && !userRoles.contains("RTBAutomationAdmin")))
            validateUserHasAccessToMethod(user, Class.forName(((RemotingDestination) getDestination()).getSource()), ((RemotingMessage) message).getOperation());
        } catch (ClassNotFoundException e) {
            //if can't find class, then will not validate
        }



        // Called for LDAP flow only where the authtoken validation is required
        if(ivCookie !=null && authorizationToken !=null) {
            logger.debug("Setting authToken");
            authorizationToken=user.getAuthorizationToken();
        }

        validateAuthToken(user, authorizationToken);
        validateIAMToken(message);

        //Called when SSO Logout has happened from one of the logged in tabs
        if(ivCookie ==null && ssoLogoutCookie !=null ){
            logger.info("SSO Flow: IV cookie is null and ssoLogoutCookie is not null. ");
            validateSSOLogout(ivCookie,ssoLogoutCookie);
        }

        //NOTE: the principal must be set before any modifications to the DB
        PspPrincipal principal = user.createPrincipal();
        PayrollServices.setCurrentPrincipal(principal);

        validateSessionTimeout(user);
        validateSSOSessionTimeout(user);
        updateLastRemoteCallTimestamp(user);
        synchronizeTimezones(message);
        return trackAndInvokeMessage(message, principal, authorizationToken);
        }finally {
            logger.info("RequestOption="+((RemotingMessage) message).getOperation()+" timeLapsed="+(System.currentTimeMillis() - startTime));
        }
    }

    private void validateIAMToken(Message message) {
        String ticket = (String) message.getHeader("ticket");
        String authId = (String) message.getHeader("authId");
        String realmId = (String) message.getHeader("realmId");
        String operation = getOperation(message);
        if (sapAuthenticationManager.isAuthenticationRequired(operation)) {
            sapAuthenticationManager.authenticate(ticket, authId, realmId);
        }
    }

    private String getOperation(Message message) {
        String result = null;
        if (message instanceof RemotingMessage) {
            result = ((RemotingMessage) message).getOperation();
        }
        return result;
    }

    private Object trackAndInvokeMessage(Message message, PspPrincipal principal, String authorizationToken) {
        CallTracker callTracker = new CallTracker();
        try {
            callTracker.invocationBegin(message, principal.getName(), authorizationToken);
            setRequestContext(message);
            Object result = super.invoke(message);
            callTracker.invocationComplete(result);
            return result;
        } catch (Throwable t) {
            callTracker.invocationException(t);
            dseFactory.rethrowException(callTracker.serviceName, callTracker.operation, t);
            return null;
        } finally {
            clearRequestContext();
        }
    }

    class CallTracker {

        private boolean trackingEnabled = false;
        private boolean isSecondaryDbEnableForSMC = false;
        private SpcfUniqueId callId;
        private String screenPath;
        private String serviceName;
        private String operation;
        private String parameters;
        private String principal;
        private String authToken;
        private String host = "Unknown";

        private int resultSize;
        private StopWatch stopWatch;
        private String exceptionMessage;

        CallTracker() {
            trackingEnabled = SystemParameter.findBooleanValue(SystemParameter.Code.SAP_CALL_TRACKING, true);
        }

        CallTracker(boolean trackingEnabled){
            this.trackingEnabled = trackingEnabled;
        }

        public void invocationBegin(Message pMessage, String pPrincipal, String pAuthToken) {
            if (trackingEnabled) {
                try {
                    principal = pPrincipal;
                    authToken = pAuthToken;
                    screenPath = (String) pMessage.getHeader("screenPath");
                    try {
                        if (InetAddress.getLocalHost() != null) {
                            host = InetAddress.getLocalHost().getHostName();
                        }
                    } catch (UnknownHostException e) {
                        // ignore
                    }

                    if (pMessage instanceof RemotingMessage) {
                        RemotingMessage remotingMessage = (RemotingMessage) pMessage;
                        serviceName = remotingMessage.getDestination();
                        operation = remotingMessage.getOperation();

                        if (remotingMessage.getParameters().size() > 0) {
                            StringBuilder paramBuilder = new StringBuilder(remotingMessage.getParameters().size() * 40);
                            for (int i = 0; i < remotingMessage.getParameters().size(); i++) {
                                Object o = remotingMessage.getParameters().get(i);
                                if (o == null || o instanceof String || o.getClass().isPrimitive()) {
                                    paramBuilder.append(String.valueOf(o));
                                } else if (o instanceof Collection) {
                                    paramBuilder.append(o.getClass().getSimpleName()).append(":").append(((Collection) o).size());
                                } else if (o.getClass().isArray()) {
                                    paramBuilder.append(o.getClass().getSimpleName()).append(":").append(((Object[]) o).length);
                                } else {
                                    paramBuilder.append(String.valueOf(o));
                                }
                                if (i + 1 < remotingMessage.getParameters().size()) {
                                    paramBuilder.append(",");
                                }
                                if (paramBuilder.length() > 4000) {
                                    break;
                                }
                            }

                            if (paramBuilder.length() > 4000) {
                                paramBuilder.setLength(4000);
                            }
                            parameters = paramBuilder.toString();
                        }

                        recordInvocationBegin();

                        stopWatch = StopWatch.startTimer();
                    } else {
                        trackingEnabled = false;
                    }
                } catch (Throwable t) {
                    logger.info("SAP Call Tracking Failure", t);
                }
            }
        }

        public void invocationComplete(Object result) {
            if (trackingEnabled) {
                try {
                    stopWatch.stop();

                    resultSize = 0;
                    if (result == null) {
                        resultSize = 0;
                    } else if (result instanceof Collection) {
                        resultSize = ((Collection) result).size();
                    } else if (result.getClass().isArray()) {
                        resultSize = ((Object[]) result).length;
                    } else if (result instanceof SAPSearchResults) {
                        resultSize = ((SAPSearchResults) result).getReturnsList().size();
                    }

                    recordInvocationComplete();
                } catch (Throwable t) {
                    logger.info("SAP Call Tracking Failure", t);
                }
            }
        }

        public void invocationException(Throwable throwable) {
            if (trackingEnabled) {
                try {
                    stopWatch.stop();
                    StringWriter sw = new StringWriter(1024);
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    exceptionMessage = sw.toString();
                    if (exceptionMessage.length() > 4000) {
                        exceptionMessage = exceptionMessage.substring(0, 4000);
                    }
                    recordInvocationComplete();
                } catch (Throwable t) {
                    logger.info("SAP Call Tracking Failure - " + serviceName + ":" + operation, t);
                }
            }
        }

        private void recordInvocationBegin() {
            // record:
            //   class target
            //   method target
            //   parameters
            //   result size
            //   exception message, if any
            if (trackingEnabled) {
                try {
                    ApplicationSecondary.beginUnitOfWork();
                    com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall sapMethodCall = new com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall();
                    sapMethodCall.setElapsedMillis(-1);
                    sapMethodCall.setHost(host);
                    sapMethodCall.setScreenPath(screenPath);
                    sapMethodCall.setServiceName(serviceName);
                    sapMethodCall.setMethodName(operation);
                    sapMethodCall.setParameters(parameters);
                    sapMethodCall.setResultSize(-1);
                    sapMethodCall.setSecurityPrincipal(principal);
                    sapMethodCall.setSessionId(authToken);
                    sapMethodCall = ApplicationSecondary.save(sapMethodCall);

                    callId = sapMethodCall.getId();

                    ApplicationSecondary.commitUnitOfWork();
                } finally {
                    ApplicationSecondary.rollbackUnitOfWork();
                }
            }
        }

        private void recordInvocationComplete() {
            if (trackingEnabled && callId != null) {
                try {
                    ApplicationSecondary.beginUnitOfWork();
                    com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall sapMethodCall = ApplicationSecondary.findById(com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall.class, callId);
                    sapMethodCall.setElapsedMillis(stopWatch.getElapsedMillis());
                    sapMethodCall.setExceptionTrace(exceptionMessage);
                    sapMethodCall.setResultSize(resultSize);

                    ApplicationSecondary.save(sapMethodCall);

                    callId = null;

                    ApplicationSecondary.commitUnitOfWork();
                } finally {
                    ApplicationSecondary.rollbackUnitOfWork();
                }
            }
        }
    }

    private void validateLoginParameters(String corpId, String authorizationToken) throws MessageException {
        try {
            SAPAuthHelper.validateLoginParameters(corpId, authorizationToken);
        } catch (Exception e) {
            throw new MessageException(e.getMessage());
        }
    }

    private AuthUser getLoggedInUser(String corpId) throws MessageException {
        try {
            return SAPAuthHelper.getLoggedInUser(corpId);
        } catch (Exception e) {
            throw new MessageException(e.getMessage());
        }
    }

    private void validateUserHasAccessToMethod(AuthUser user, Class destinationClass, String destinationMethodName) throws MessageException {
        try {
            SAPAuthHelper.validateUserHasAccessToMethod(user, destinationClass, destinationMethodName);
        } catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAPJavaAdapter.SAP_UNAUTHORIZED_REMOTE_CALL);
            throw me;
        }
    }

    private void validateAuthToken(AuthUser user, String authorizationToken) throws MessageException {
        try {
            SAPAuthHelper.validateAuthToken(user, authorizationToken);
        } catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAPJavaAdapter.SAP_SESSION_TERMINATED);
            throw me;
        }
    }

    private void validateSSOLogout(String ivCookie,String ssoLogoutCookie) throws MessageException {
        try {
            String msg = "ValidateSSOLogout: SSO session is terminated";
            if(ivCookie==null && ssoLogoutCookie.equals("true")){
                eraseLogoutCookie();
                throw new Exception(msg);
            }
        } catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAPJavaAdapter.SAP_SESSION_TERMINATED);
            throw me;
        }
    }

    private void synchronizeTimezones(Message message) {
        int serverOffset = Calendar.getInstance().getTimeZone().getOffset(new Date().getTime()) / 1000 / 60 * -1;
        int clientOffset = (Integer) message.getHeader("timezoneOffset");
        if (serverOffset != clientOffset) {
            for (Object obj : ((RemotingMessage) message).getParameters()) {
                if (obj instanceof Date) {
                    Date date = (Date) obj;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.MINUTE, serverOffset - clientOffset);
                    date.setTime(cal.getTimeInMillis());
                }
            }

        }
    }

    private void validateSessionTimeout(AuthUser user) {
        try {
            SAPAuthHelper.validateSessionTimeout(user);
        } catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAP_SESSION_EXPIRED);
            throw me;
        }
    }

    private void validateSSOSessionTimeout(AuthUser user)  {
        try {
            SAPAuthHelper.validateSSOSessionTimeout(user);
        }catch (Exception e) {
            MessageException me = new MessageException(e.getMessage());
            me.setCode(SAP_SESSION_EXPIRED);
            throw me;
        }
    }

    private void updateLastRemoteCallTimestamp(AuthUser user) {
        try {
            SAPAuthHelper.updateLastRemoteCallTimestamp(user);
        } catch (Exception e) {
            throw new MessageException(e.getMessage());
        }
    }

    private void eraseLogoutCookie() {
        try{
            Cookie[] cookies = FlexContext.getHttpRequest().getCookies();
            logger.debug("Erasing existing cookies");
            if(cookies !=null){
                for (Cookie cookie : cookies) {
                    if(cookie.getName().equals("ssoLogoutCookie")){
                        cookie = new Cookie(cookie.getName(), "");
                        cookie.setPath("/SAP");
                        cookie.setDomain(FlexContext.getHttpRequest().getServerName());
                        cookie.setMaxAge(0);
                        FlexContext.getHttpResponse().addCookie(cookie);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Error invalidating cookies/session for the user", t);
        } finally {
        }
    }

    private List<String> getRoles(String corpId) {

        List<String> roles = new ArrayList<>();


        try {

            PayrollServices.beginUnitOfWork();

            AuthUser foundUser = AuthUser.findUser(corpId);

            for (AuthRole authRole : foundUser.getAuthRoleCollection()) {

                roles.add(authRole.getRoleId());

            }

            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {

            PayrollServices.rollbackUnitOfWork();

            logger.error("Error while retrieving the user role information.");

        }


        return roles;

    }

    private void setRequestContext(Message message) {
        String operation = null;
        try {
            if(Objects.isNull(message)){
                return;
            }
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
            logger.info("Event=SetRequestContext Type=SAP Status=Started");

            operation = ((RemotingMessage) message).getOperation();

            PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContext(null, RequestType.SAP, operation);
        } catch (Exception e) {
            logger.error("Event=SetRequestContext Type=SAP Status=Error Operation=" + operation, e);
        }
    }

    private Pair<String, SourceSystemCode> getPSIDandSourceSystemCode(String screenPath) {
        if (StringUtils.isEmpty(screenPath))
            return null;

        String psid = null;
        Matcher matcher = PATTERN_QBDT.matcher(screenPath);
        if (matcher.find()) {
            psid = matcher.group(1);
            return new Pair<>(psid, SourceSystemCode.QBDT);
        }

        matcher = PATTERN_IOP.matcher(screenPath);
        if (matcher.find()) {
            psid = matcher.group(1);
            return new Pair<>(psid, SourceSystemCode.IOP);
        }
        return null;
    }

    private void clearRequestContext() {
        try {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
        } catch (Exception e) {
            logger.error("Event=ClearRequestContext Type=SAP Status=Error", e);
        }
    }

}
