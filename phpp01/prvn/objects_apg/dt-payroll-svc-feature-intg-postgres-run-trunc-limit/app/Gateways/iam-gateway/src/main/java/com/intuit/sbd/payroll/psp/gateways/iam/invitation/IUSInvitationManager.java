package com.intuit.sbd.payroll.psp.gateways.iam.invitation;

import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.gateways.iam.IUSInvitationClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationModel;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationResponse;
import com.launchdarkly.shaded.com.google.common.base.Strings;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class IUSInvitationManager implements InvitationClient {

    @Autowired
    private IUSInvitationClientWrapper iusInvitationClientWrapper;

    @Autowired
    private IUSInvitationService iusInvitationService;

    private static final Logger logger = LoggerFactory.getLogger(IUSInvitationManager.class);

    private String workforceSignUpUrl;
    private String workforceSignInUrl;

    private static final String WORKFORCE_SIGN_UP_URL_CONF = "workforce_invite_sign_up_url";
    private static final String WORKFORCE_SIGN_IN_URL_CONF = "workforce_invite_sign_in_url";
    private static final String QUERY_PARAM_COMPANY_RECORD_ID = "companyRecordId";
    private static final String QUERY_PARAM_COMPANY_NAME = "coName";
    private static final String QUERY_PARAM_EMPLOYEE_RECORD_ID = "employeeRecordId";
    private static final String QUERY_PARAM_TYPE = "type";
    private static final String QUERY_PARAM_TYPE_DT = "DT";

    public IUSInvitationManager() {
        this.workforceSignUpUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, WORKFORCE_SIGN_UP_URL_CONF);
        this.workforceSignInUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, WORKFORCE_SIGN_IN_URL_CONF);
    }

    @Override
    public InvitationResponse sendWorkforceInvitationRequest(InvitationModel invitationModel, IAMTicket iamTicket) {
        //creating Workforce Invitation Request
        CreateInvitationsRequest createInvitationsRequest = createWorkforceInvitationRequest(invitationModel);
        //isResend is false - new persona will be added
        //Else existing persona will be used for resending invitation
        Invitations invitationResponse = iusInvitationClientWrapper.sendInvite(iamTicket, createInvitationsRequest, invitationModel.isResend());

        //Processing Response. It will contain personaId, invitationId
        if (invitationResponse != null) {
            Invitation receiverInvite = invitationResponse.getInvitation().get(0);
            String receiverPersonaId = receiverInvite.getReceiver().getPersonaId();
            String invitationId = receiverInvite.getId();
            if(!Strings.isNullOrEmpty(receiverPersonaId) && !Strings.isNullOrEmpty(invitationId)) {
                InvitationResponse inviteResponse = new InvitationResponse();
                inviteResponse.setProfileId(receiverPersonaId);
                inviteResponse.setInvitationId(invitationId);
                return inviteResponse;
            }
        }
        return null;
    }

    private CreateInvitationsRequest createWorkforceInvitationRequest(InvitationModel invitationModel) {
        CreateInvitationsRequest createInvitationsRequest = new CreateInvitationsRequest();
        //Preparing SignUp/SignIn Link
        createInvitationsRequest.setSignUpUrl(generateInvitationLink(invitationModel.getCompanyModel().getSourceCompanyId(),
                invitationModel.getEmployeeModel().getEmployeeId(), invitationModel.getCompanyModel().getCoName(), false));
        createInvitationsRequest.setSignInUrl(generateInvitationLink(invitationModel.getCompanyModel().getSourceCompanyId(),
                invitationModel.getEmployeeModel().getEmployeeId(), invitationModel.getCompanyModel().getCoName(), true));
        //if sendEmail is set to true then employee will be notified via email
        createInvitationsRequest.setSendEmail(true);
        //Preparing Invitation
        List<Invitation> invitations = createInvitationsRequest.getInvitation();
        Invitation invite = iusInvitationService.prepareInvitation(invitationModel);
        invitations.add(invite);

        return createInvitationsRequest;
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
            logger.error("URL preparation CompanyId={} EmployeeId={} Error={}", sourceCompanyId, employeeId, e);
            throw new IllegalArgumentException(String.format("URL preparation Exception CompanyId=%s EmployeeId=%s", sourceCompanyId, employeeId));
        }
        return uriBuilder.build().toString();
    }
}
