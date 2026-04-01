package com.intuit.sbd.payroll.psp.gateways.iam.invitation;

import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class IUSInvitationService {

    private static final String INVITE_TYPE = "INVITE";
    private static final String EMPLOYEE_ROLE_NAME = "employee";
    private static final String ROLE_TYPE = "OFFERING";
    private static final String ROLE_ID = "Intuit.ems.Employee";
    private static final String OINP_TEMPLATE = "oinpTemplate";
    private static final String DEFAULT_OINP_TEMPLATE = "WFOfflineInviteEmailTemplate_Default";

    public Invitation prepareInvitation(InvitationModel invitationModel) {
        Invitation invite = new Invitation();
        invite.setType(INVITE_TYPE);
        invite.setRealmId(invitationModel.getCompanyModel().getRealmId());
        //Preparing Receiver
        Persona receiver = prepareReceiver(invitationModel);
        invite.setReceiver(receiver);

        //Set OINP template name as invite attribute so that Template picked up will be from OINP Portal
        NameValuePair nvp = new NameValuePair();
        nvp.setName(OINP_TEMPLATE);
        nvp.setValue(Objects.nonNull(invitationModel.getEmailTemplateName()) ?
                invitationModel.getEmailTemplateName() : DEFAULT_OINP_TEMPLATE);
        invite.getAttributes().add(nvp);

        return invite;
    }

    private Persona prepareReceiver(InvitationModel invitationModel) {
        Persona receiver= new Persona();
        receiver.setRealmId(invitationModel.getCompanyModel().getRealmId());
        //if resend is set to true, set the personaId for employee to be passed in invitation body
        receiver.setPersonaId(invitationModel.isResend() ? invitationModel.getEmployeeModel().getPersonaId() : null);
        receiver.setPersonaName(UUID.randomUUID().toString());
        //Adding employeeName into the list of fullName present in Persona
        FullName employeeName = prepareFullName(invitationModel);
        List<FullName> fullNames = receiver.getFullName();
        fullNames.add(employeeName);

        //Preparing Role
        List<Role> rolesToBeAdded = receiver.getRole();
        Role role = prepareRole(EMPLOYEE_ROLE_NAME, ROLE_ID, ROLE_TYPE);
        rolesToBeAdded.add(role);

        //Adding Email
        Email email = prepareEmail(invitationModel.getEmployeeModel().getEmailID());
        receiver.setEmail(email);

        return receiver;
    }

    private Email prepareEmail(String emailId) {
        Email email = new Email();
        email.setAddress(emailId);
        return email;
    }

    private FullName prepareFullName(InvitationModel invitationModel) {
        FullName fullName = new FullName();
        fullName.setGivenName(invitationModel.getEmployeeModel().getFirstName());
        fullName.setSurName(invitationModel.getEmployeeModel().getLastName());
        return fullName;
    }

    private Role prepareRole(String roleName, String roleId, String roleType) {
        Role role = new Role();
        role.setName(roleName);
        role.setRoleId(roleId);
        role.setRoleType(roleType);
        return role;
    }
}
