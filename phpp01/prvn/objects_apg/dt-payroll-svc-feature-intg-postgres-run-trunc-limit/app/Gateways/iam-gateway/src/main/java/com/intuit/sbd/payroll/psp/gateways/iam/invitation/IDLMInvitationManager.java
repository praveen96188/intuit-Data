package com.intuit.sbd.payroll.psp.gateways.iam.invitation;

import com.intuit.identity.experiences.graphql.model.*;
import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.gateways.iam.IDLMInvitationClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationModel;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationResponse;
import com.launchdarkly.shaded.com.google.common.base.Strings;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class IDLMInvitationManager implements InvitationClient {
    private IDLMInvitationClientWrapper idlmInvitationClientWrapper;

    private String workforceSignUpUrl;
    private String workforceSignInUrl;

    private static final String WORKFORCE_SIGN_UP_URL_CONF = "workforce_invite_sign_up_url";
    private static final String WORKFORCE_SIGN_IN_URL_CONF = "workforce_invite_sign_in_url";
    private static final String QUERY_PARAM_COMPANY_RECORD_ID = "companyRecordId";
    private static final String QUERY_PARAM_COMPANY_NAME = "coName";
    private static final String QUERY_PARAM_EMPLOYEE_RECORD_ID = "employeeRecordId";
    private static final String QUERY_PARAM_TYPE = "type";
    private static final String QUERY_PARAM_TYPE_DT = "DT";
    private static final String ROLE_ID = "Intuit.ems.Employee";
    private static final String OINP_TEMPLATE = "oinpTemplate";
    private static final String DEFAULT_OINP_TEMPLATE = "WFOfflineInviteEmailTemplate_Default";

    @Autowired
    IDLMInvitationManager(IDLMInvitationClientWrapper idlmInvitationClientWrapper) {
        this.idlmInvitationClientWrapper = idlmInvitationClientWrapper;
        this.workforceSignUpUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, WORKFORCE_SIGN_UP_URL_CONF);
        this.workforceSignInUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, WORKFORCE_SIGN_IN_URL_CONF);
    }

    @Override
    public InvitationResponse sendWorkforceInvitationRequest(InvitationModel invitationModel, IAMTicket iamTicket) throws IdentityGraphQLException, IDLMException {
        if (invitationModel.isResend()) {
            return sendWorkforceReinviteRequest(invitationModel, iamTicket);
        } else {
            return sendWorkforceInviteRequest(invitationModel, iamTicket);
        }
    }

    private InvitationResponse sendWorkforceInviteRequest(InvitationModel invitationModel, IAMTicket iamTicket) throws IdentityGraphQLException, IDLMException {
        Identity_InviteUserInput identityInviteUserInput = createWorkforceInvitationRequest(invitationModel);
        Identity_InviteUserPayload identityInviteUserPayload = idlmInvitationClientWrapper.sendInvite(identityInviteUserInput, iamTicket);
        if (identityInviteUserPayload != null && identityInviteUserPayload.getInvitee() != null) {
            String profileId = identityInviteUserPayload.getInvitee().getProfileId();
            String invitationId = identityInviteUserPayload.getInvitationId();
            if(!Strings.isNullOrEmpty(profileId) && !Strings.isNullOrEmpty(invitationId)) {
                return getInvitationResponse(profileId, invitationId);
            }
        }
        return  null;
    }

    private InvitationResponse sendWorkforceReinviteRequest(InvitationModel invitationModel, IAMTicket iamTicket) throws IDLMException, IdentityGraphQLException {
        Identity_ReinviteUserInput identityReinviteUserInput = createWorkforceReinvitationRequest(invitationModel);
        Identity_ReinviteUserPayload identityReinviteUserPayload = idlmInvitationClientWrapper.resendInvite(identityReinviteUserInput, iamTicket);
        if (identityReinviteUserPayload != null) {
            String profileId = identityReinviteUserPayload.getInvitee().getProfileId();
            String invitationId = identityReinviteUserPayload.getInvitationId();
            if(!Strings.isNullOrEmpty(profileId) && !Strings.isNullOrEmpty(invitationId)) {
                return getInvitationResponse(profileId, invitationId);
            }
        }
        return  null;
    }


    private InvitationResponse getInvitationResponse(String profileId, String invitationId) {
        InvitationResponse invitationResponse = new InvitationResponse();
        invitationResponse.setInvitationId(invitationId);
        invitationResponse.setProfileId(profileId);
        return invitationResponse;
    }

    private Identity_InviteUserInput createWorkforceInvitationRequest(InvitationModel invitationModel) {
        Identity_InviteUserInput identityInviteUserInput = Identity_InviteUserInput.newBuilder()
                                                            .invitationChannel(getInvitationChannel())
                                                            .invitationType(Identity_InvitationType.INVITE)
                                                            .invitee(getInvitee(invitationModel))
                                                            .roles(getRoles())
                                                            .offeringSetting(getOfferingSetting(invitationModel))
                                                            .notificationTemplateInput(getNotificationTemplateInput(invitationModel))
                                                            .build();
        return identityInviteUserInput;
    }

    private Identity_ReinviteUserInput createWorkforceReinvitationRequest(InvitationModel invitationModel) {
        Identity_ReinviteUserInput identityReinviteUserInput = Identity_ReinviteUserInput.newBuilder()
                                                                .invitationChannel(getInvitationChannel())
                                                                .invitationType(Identity_InvitationType.INVITE)
                                                                .invitee(getInvitee(invitationModel))
                                                                .roles(getRoles())
                                                                .offeringSetting(getOfferingSetting(invitationModel))
                                                                .notificationTemplateInput(getNotificationTemplateInput(invitationModel))
                                                                .build();
        return identityReinviteUserInput;
    }

    private Identity_OfferingSettingInput getOfferingSetting(InvitationModel invitationModel) {
        Identity_OfferingSettingInput identityOfferingSettingInput = new Identity_OfferingSettingInput();
        Identity_InviteUrlsInput identityInviteUrlsInput = new Identity_InviteUrlsInput();
        identityInviteUrlsInput.setSignInUrl(generateInvitationLink(invitationModel.getCompanyModel().getSourceCompanyId(),
                invitationModel.getEmployeeModel().getEmployeeId(), invitationModel.getCompanyModel().getCoName(), true));
        identityInviteUrlsInput.setSignUpUrl(generateInvitationLink(invitationModel.getCompanyModel().getSourceCompanyId(),
                invitationModel.getEmployeeModel().getEmployeeId(), invitationModel.getCompanyModel().getCoName(), false));
        Identity_LegacyUrlsInput identityLegacyUrlsInput = new Identity_LegacyUrlsInput();
        identityOfferingSettingInput.setLegacyUrls(identityLegacyUrlsInput);
        identityOfferingSettingInput.getLegacyUrls().setInviteUrls(identityInviteUrlsInput);
        return identityOfferingSettingInput;
    }

    private List<Identity_InviteeRoleInput> getRoles() {
        List<Identity_InviteeRoleInput> roles = new ArrayList<>();
        Identity_InviteeRoleInput identityInviteeRoleInput = new Identity_InviteeRoleInput();
        identityInviteeRoleInput.setRoleName(ROLE_ID);
        roles.add(identityInviteeRoleInput);
        return roles;
    }

    private Identity_NotificationTemplateInput getNotificationTemplateInput(InvitationModel invitationModel) {
        Identity_NotificationTemplateInput identityNotificationTemplateInput = new Identity_NotificationTemplateInput();
        List<TemplateAttributeInput> templateAttributes = new ArrayList<>();
        TemplateAttributeInput templateAttributeInput = new TemplateAttributeInput();
        templateAttributeInput.setKey(OINP_TEMPLATE);
        templateAttributeInput.setValue(Objects.nonNull(invitationModel.getEmailTemplateName()) ?
                invitationModel.getEmailTemplateName() : DEFAULT_OINP_TEMPLATE);
        templateAttributes.add(templateAttributeInput);
        identityNotificationTemplateInput.setTemplateAttributes(templateAttributes);
        return identityNotificationTemplateInput;
    }

    private List<Identity_InvitationChannel> getInvitationChannel() {
        List<Identity_InvitationChannel> invitationChannel = new ArrayList<>();
        invitationChannel.add(Identity_InvitationChannel.EMAIL);
        return invitationChannel;
    }

    private Identity_InviteeProfileInput getInvitee(InvitationModel invitationModel) {
        Identity_InviteeProfileInput identity_inviteeProfileInput = new Identity_InviteeProfileInput();
        identity_inviteeProfileInput.setEmail(invitationModel.getEmployeeModel().getEmailID());
        identity_inviteeProfileInput.setAccountId(invitationModel.getCompanyModel().getRealmId());
        identity_inviteeProfileInput.setProfileId(invitationModel.isResend() ? invitationModel.getEmployeeModel().getPersonaId() : null);
        identity_inviteeProfileInput.setGivenName(invitationModel.getEmployeeModel().getFirstName());
        identity_inviteeProfileInput.setFamilyName(invitationModel.getEmployeeModel().getLastName());
        return  identity_inviteeProfileInput;
    }

    private String generateInvitationLink(String sourceCompanyId, String employeeId, String coLegalName, boolean isSignIn) {
        String uri = isSignIn ? workforceSignInUrl : workforceSignUpUrl;
        UriBuilder uriBuilder;

        try {
            uriBuilder = UriBuilder.fromUri(uri);
            uriBuilder.queryParam(QUERY_PARAM_COMPANY_RECORD_ID, sourceCompanyId);
            uriBuilder.queryParam(QUERY_PARAM_COMPANY_NAME,  URLEncoder.encode(coLegalName, StandardCharsets.UTF_8.name()));
            uriBuilder.queryParam(QUERY_PARAM_EMPLOYEE_RECORD_ID, employeeId);
            uriBuilder.queryParam(QUERY_PARAM_TYPE, QUERY_PARAM_TYPE_DT);
        } catch (Exception e) {
            log.error("URL preparation CompanyId={},EmployeeId={},Error={}", sourceCompanyId, employeeId, e);
            throw new IllegalArgumentException(String.format("URL preparation Exception CompanyId=%s EmployeeId=%s", sourceCompanyId, employeeId));
        }
        return uriBuilder.build().toString();
    }

}
