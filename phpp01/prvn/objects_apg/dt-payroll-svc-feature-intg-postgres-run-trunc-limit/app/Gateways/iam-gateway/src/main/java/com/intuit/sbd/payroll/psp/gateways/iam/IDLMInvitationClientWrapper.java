package com.intuit.sbd.payroll.psp.gateways.iam;

import com.intuit.identity.experiences.graphql.client.IdentityInviteUserProjectionRoot;
import com.intuit.identity.experiences.graphql.client.IdentityReinviteUserProjectionRoot;
import com.intuit.identity.experiences.graphql.model.Identity_InviteUserInput;
import com.intuit.identity.experiences.graphql.model.Identity_InviteUserPayload;
import com.intuit.identity.experiences.graphql.model.Identity_ReinviteUserInput;
import com.intuit.identity.experiences.graphql.model.Identity_ReinviteUserPayload;
import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.client.IDLMMutation;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.gateways.iam.auth.OfflineAuth;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IDLMInvitationClientWrapper {

    @Autowired
    public IDLMMutation idlmMutation;

    public Identity_InviteUserPayload sendInvite(@NonNull Identity_InviteUserInput identityInviteUserInput, IAMTicket iamTicket) throws IDLMException, IdentityGraphQLException {
        try {
            IdentityInviteUserProjectionRoot identityInviteUserProjectionRoot = new IdentityInviteUserProjectionRoot()
                    .invitationId()
                    .invitee()
                    .profileId()
                    .root();
            OfflineAuth offlineAuth = new OfflineAuth(iamTicket);

            return idlmMutation.identityInviteUser(identityInviteUserInput, identityInviteUserProjectionRoot, offlineAuth);
        } catch (IDLMException | IdentityGraphQLException ex) {
            log.error("Error sending invite to user={},exception={}", identityInviteUserInput.getInvitee().getEmail(), ex);
            throw ex;
        }
    }

    public Identity_ReinviteUserPayload resendInvite(@NonNull Identity_ReinviteUserInput identityReinviteUserInput, IAMTicket iamTicket) throws IDLMException, IdentityGraphQLException {
        try {
            IdentityReinviteUserProjectionRoot identityReinviteUserProjectionRoot = new IdentityReinviteUserProjectionRoot()
                    .invitationId()
                    .invitee()
                    .profileId()
                    .root();
            OfflineAuth offlineAuth = new OfflineAuth(iamTicket);
            return idlmMutation.identityReinviteUser(identityReinviteUserInput, identityReinviteUserProjectionRoot, offlineAuth);
        } catch (IDLMException | IdentityGraphQLException ex) {
            log.error("Error resending invite to user={},exception={}", identityReinviteUserInput.getInvitee().getEmail(), ex);
            throw ex;
        }
    }
}
