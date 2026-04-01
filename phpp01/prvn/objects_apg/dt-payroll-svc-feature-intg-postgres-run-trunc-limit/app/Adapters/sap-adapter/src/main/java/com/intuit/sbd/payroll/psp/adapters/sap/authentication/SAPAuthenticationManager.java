package com.intuit.sbd.payroll.psp.adapters.sap.authentication;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.platform.jsk.security.iam.authn.IntuitUserAuthenticationRequest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.manager.PayrollRequestContextManager;
import com.intuit.sbg.payroll.authorization.properties.PayrollSecurityProperties;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.List;
import java.util.Objects;

public class SAPAuthenticationManager {

    private static SpcfLogger logger = PayrollServices.getLogger(SAPAuthenticationManager.class);
    private boolean initialized = false;

    @Autowired
    private PayrollRequestContextManager payrollRequestContextManager;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PayrollSecurityProperties payrollSecurityProperties;
    @Autowired
    private List<String> sapAuthenticationOperations;

    public SAPAuthenticationManager(){
        if(!initialized) {
            PayrollApplicationBeanFactory.injectBeans(this);
            initialized = true;
        }
    }

    public void authenticate(String ticket, String authId, String realmId) {
        try {
            if(StringUtils.isEmpty(ticket) || StringUtils.isEmpty(authId) || StringUtils.isEmpty(realmId)) {
                return;
            }
            logger.info("Started Authentication for the authId="+authId);
            IntuitUserAuthenticationRequest intuitUserAuthenticationRequest
                    = new IntuitUserAuthenticationRequest(authId, ticket).withRealmid(realmId).withTransactionId(getTransactionId());
            Authentication authentication = authenticationManager.authenticate(new PreAuthenticatedAuthenticationToken(intuitUserAuthenticationRequest, ""));
            logger.info("Completed Authentication for the authId="+authId);
            setAuthorizationContext(authentication);
        } catch (Exception e) {
            logger.error("Exception in Authentication",e);
            if(!payrollSecurityProperties.getAuthentication().isIgnoreAuthenticationFailure()){
                throw e;
            }
        }
    }

    private void setAuthorizationContext(Authentication authentication) {
        payrollRequestContextManager.setAuthorizationContext(authentication);
    }

    private String getTransactionId() {
        String tid;
        IntuitContext intuitContext = RequestAttributesUtils.getAttribute(ContextConstants.INTUIT_CONTEXT, IntuitContext.class);
        if (Objects.nonNull(intuitContext) && Objects.nonNull(intuitContext.getTransactionId())) {
            tid = intuitContext.getTransactionId();
        } else if(!Objects.isNull(MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID))){
            tid = (String)MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID);
        } else {
            tid = SpcfUniqueId.generateRandomUniqueIdString();
        }
        return tid;
    }

    public boolean isAuthenticationRequired(String operation) {
        return StringUtils.isNotEmpty(operation) ? this.sapAuthenticationOperations.contains(operation):false;
    }
}
