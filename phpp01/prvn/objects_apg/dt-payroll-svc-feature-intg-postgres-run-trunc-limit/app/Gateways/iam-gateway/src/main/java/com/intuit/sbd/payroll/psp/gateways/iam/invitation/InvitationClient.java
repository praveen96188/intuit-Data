package com.intuit.sbd.payroll.psp.gateways.iam.invitation;

import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationModel;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationResponse;

public interface InvitationClient {
    InvitationResponse sendWorkforceInvitationRequest(InvitationModel invitationModel, IAMTicket iamTicket) throws IdentityGraphQLException, IDLMException;
}
