package com.intuit.sbd.payroll.psp.processes.Workforce;

import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.IDLMInvitationManager;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.IUSInvitationManager;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.InvitationFactory;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationModel;
import com.intuit.sbd.payroll.psp.gateways.iam.invitation.model.InvitationResponse;
import com.intuit.sbd.payroll.psp.processes.IProcess;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.launchdarkly.shaded.com.google.common.base.Strings;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EmployeeWorkforceInviteCore extends Process implements IProcess {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeWorkforceInviteCore.class);

    private Employee employee;
    private Company company;
    private IAMTicket iamTicket;
    private boolean isResend;
    private String invitationSource;
    private String emailTemplateName;
    private InvitationFactory invitationFactory;

    public EmployeeWorkforceInviteCore(Employee employee, Company company, IAMTicket iamTicket, boolean isResend, String emailTemplateName, String invitationSource) {
        this.employee = employee;
        this.company = company;
        this.iamTicket = iamTicket;
        this.isResend = isResend;
        this.invitationSource = invitationSource;
        this.emailTemplateName = emailTemplateName;
        this.invitationFactory = PayrollApplicationBeanFactory.getBean(InvitationFactory.class);
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if (Objects.isNull(employee)) {
            validationResult.getMessages()
                    .BadProcessArgument("Employee");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        try {
                logger.info("Invitation=Attempted EmployeeId={} EmailId={} CompanyId={} Template={} InvitationSource={}", employee.getId(),
                    EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, employee.getEmail(), employee.getId().toString()),
                    company.getSourceCompanyId(), emailTemplateName, invitationSource);
            InvitationModel invitationModel = new InvitationModel(employee, company, isResend, emailTemplateName);
            InvitationResponse invitationResponse = invitationFactory.getInvitationClientInstance().sendWorkforceInvitationRequest(invitationModel, iamTicket);

            if(Objects.nonNull(invitationResponse)){
                String invitationId = invitationResponse.getInvitationId();
                String personaId = invitationResponse.getProfileId();

                logger.info("Invitation=Success EmployeeId={} EmailId={} CompanyId={} Template={} InvitationSource={}", employee.getId(),
                        EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, employee.getEmail(), employee.getId().toString()),
                        company.getSourceCompanyId(), emailTemplateName, invitationSource);
                //Creating company event for employee successfully invited
                CompanyEvent.createEmployeeInvitedEvent(company, employee.getId().toString(), invitationSource, emailTemplateName, invitationId, personaId);
                logger.info("Created CompanyEvent CompanyId={} EmployeeId={} PersonaID={} InvitationSource={}", company.getSourceCompanyId(), employee.getId(), personaId, invitationSource);
                //Updating personaId inside PSP_Employee after successful invitation
                updateEmployeePersonaID(employee, invitationResponse.getProfileId());
                logger.info("Updated PersonaId CompanyId={} EmployeeId={} PersonaId={} InvitationSource={}", company.getSourceCompanyId(), employee.getId(), personaId, invitationSource);
            } else {
                processResult.getMessages().BadProcessArgument("PersonaId/InvitationId is null");
            }
        } catch (Exception e) {
            processResult.getMessages().ExceptionOccurred("Exception=" + e);
        }
        return processResult;
    }

    private void updateEmployeePersonaID(Employee employee, String provisionalPersonaId) {

        if(Objects.nonNull(employee) && !Strings.isNullOrEmpty(provisionalPersonaId)){
            employee = Application.findById(Employee.class, employee.getId());
            employee.setPersonaId(provisionalPersonaId);
            employee.setModifiedDate(PSPDate.getPSPTime());
            PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
            if (currentPrincipal != null) {
                employee.setModifierId(currentPrincipal.getId());
            }
            Application.save(employee);
        }
    }
}
