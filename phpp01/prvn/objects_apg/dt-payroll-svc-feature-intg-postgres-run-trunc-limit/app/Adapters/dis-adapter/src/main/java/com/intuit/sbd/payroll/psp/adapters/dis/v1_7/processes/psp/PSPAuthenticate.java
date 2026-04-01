package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.AuthenticateRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.AuthenticateResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AuthAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPAuthenticate.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
        logger.debug("Entering PSPAuthenticate.process()");
        try {
            responseDISDTO = new AuthenticateResponseDISDTO();

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
