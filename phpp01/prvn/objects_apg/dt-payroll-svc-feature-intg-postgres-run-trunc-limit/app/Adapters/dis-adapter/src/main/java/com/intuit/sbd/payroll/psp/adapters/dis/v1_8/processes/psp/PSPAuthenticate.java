package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.AuthenticateRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.AuthenticateResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.handlers.PspUserAuthZHandler;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AuthAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 * <p/>
 * Query company event Process
 */
public class PSPAuthenticate extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPAuthenticate.class);
    }

    private AuthenticateRequestDISDTO requestDISDTO;
    private AuthenticateResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pAuthenticateDISDTO
     *
     */
    public PSPAuthenticate(AuthenticateRequestDISDTO pAuthenticateDISDTO) {
        requestDISDTO = pAuthenticateDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        responseDISDTO = new AuthenticateResponseDISDTO();
        if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
            throw new UnsupportedOperationException("This method is deprecated. Send the authorization header");
        }
        logger.debug("Entering PSPAuthenticate.process()");
        try {
            AuthAdapter authAdapter = new AuthAdapter();
            String username = requestDISDTO.getUsername();
            SAPUser sapUser = authAdapter.login(username,requestDISDTO.getPassword(), false);
            if (sapUser == null) {
                logger.info("Invalid username or password for login id " + username);
                return createErrorResponse(DISMessages.invalidUsernameOrPassword());
            } else {
                responseDISDTO.setToken(sapUser.getAuthorizationToken());
                responseDISDTO.setCorpId(sapUser.getCorpId());
            }
        } finally {
            logger.debug("Leaving PSPAuthenticate.process()");
        }
        return responseDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }
}
