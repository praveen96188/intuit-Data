package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.handlers;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.sbd.payroll.psp.iam.HeaderUtils;
import com.intuit.sbd.payroll.psp.iam.VerifyAppId;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.soap.handler.AbstractPayrollSOAPSecurityHandler;
import com.intuit.sbg.psp.idmapperservices.service.IdMapperServiceRestClient;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author rn5
 * Any requests to the application should have authentication tokens
 * and requests should come only through gateways.
 */
public class PspUserAuthZHandler extends AbstractPayrollSOAPSecurityHandler {

    private static final String ACCESS_DENIED = "Access Denied";
    public static final String CORP_ID = "CorpId";
    private static final Logger logger = LoggerFactory.getLogger(PspUserAuthZHandler.class);
    private IdMapperServiceRestClient idMapperServiceRestClient = PayrollApplicationBeanFactory.getBean(IdMapperServiceRestClient.class);
    private VerifyAppId verifyAppId = PayrollApplicationBeanFactory.getBean(VerifyAppId.class);

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if ((Boolean) context.get("javax.xml.ws.handler.message.outbound")) {
            logger.info("PSP User Authorization MESSAGE OUTBOUND completed");
            RequestAttributesUtils.removeAttributes(ContextConstants.USER_AUTHORIZATION_CONTEXT, IntuitCommonHeaders.INTUIT_HEADER_TID);
            return true;
        }
        logger.info("PSP User Authorization of the request started");

        try {
            this.init();
            HttpServletRequest httpServletRequest = (HttpServletRequest) context.get("javax.xml.ws.servlet.request");
            String authorization = httpServletRequest.getHeader("Authorization");
            RequestAttributesUtils.setAttribute(IntuitCommonHeaders.INTUIT_HEADER_TID, httpServletRequest.getHeader("intuit_tid"));

            if (StringUtils.isBlank(authorization)) {
                logger.warn("Code:Authorization " + HttpHeaders.AUTHORIZATION + " = blank" + ACCESS_DENIED);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "DIS Adapter: Authorization Header is empty");
            }

            if(HeaderUtils.isOfflineTicket() && verifyAppId.validateAppId(authorization)){
                return true;
            }

            String authId = HeaderUtils.fetchAuthId(authorization);

            String corpId = idMapperServiceRestClient.fetchCorpId(authId);
            if(StringUtils.isBlank(corpId)){
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "DIS Adapter: CorpId not found with authId:"+authId);
            }
            RequestAttributesUtils.setAttribute(CORP_ID,corpId);
            AuthUser authUser = AuthUser.findUser(corpId);
            if (authUser == null) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "DIS Adapter: Unauthorized authId:"+authId);
            }
        } catch(HttpClientErrorException ex) {
            logger.error("DIS Adapter: ",ex);
            if (isEligibleForPSPAuthUser()) {
                try {
                    context.put(MessageContext.HTTP_RESPONSE_CODE, new Integer(HttpStatus.OK.value()));
                    context.getMessage().getSOAPBody().removeContents();
                    context.getMessage().getSOAPBody().addFault(QName.valueOf("Unauthorised"), ex.getMessage());
                } catch (SOAPException e) {
                    logger.error("Exception while adding the fault", e);
                }
                return false;
            }
        }
        catch(Exception ex) {
            logger.error("DIS Adapter: "+ex.getMessage());
            if (isEligibleForPSPAuthUser()) {
                throw ex;
            }
        }
        logger.info("PSP User Authorization of the request completed");
        return true;
    }

    public boolean isEligibleForPSPAuthUser() {
        if (!FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
            logger.info("ENABLE_PSPUSERAUTH_HANDLER is Off");
            return false;
        }
        logger.info("ENABLE_PSPUSERAUTH_HANDLER is ON");
        return true;
    }
}