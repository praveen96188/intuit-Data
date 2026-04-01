package com.intuit.sbd.payroll.psp.gateways.iam;


import com.intuit.identity.graphql.sdk.client.exceptions.IdentityGraphQLException;
import com.intuit.identity.idlm.exception.IDLMException;
import com.intuit.platform.integration.ius.common.types.Persona;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.gateways.iam.identity.IdentityServiceClientFactory;
import com.intuit.sbd.payroll.psp.workflows.processflag.ProcessFlagWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.processflag.employee.EmployeeProcessFlagWorkflows;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class IUSRealmClientWrapper {

    private IdentityServiceClientFactory identityServiceClientFactory;

    @Autowired
    IUSRealmClientWrapper(IdentityServiceClientFactory identityServiceClientFactory) {
        this.identityServiceClientFactory = identityServiceClientFactory;
    }

    @Async("cdmAdapterThreadPoolExecutor")
    public void initiatePersonaFixIfPersonaCheckNotDone(Company company, String userAuthId, String consumerRealmId) {
        try{
            Application.beginUnitOfWork();
            Employee employee = company.getEmployees().find(Employee.ConsumerRealmId().equalTo(consumerRealmId)).getFirst();
            //Employee present in PSP_VMP_EMPLOYEE_INFO and not present in PSP_EMPLOYEE
            if(Objects.isNull(employee)) {
                log.info("CDMAdapterAPI=addPersonaToUserEmployeeNotFound with PayrollRequestContext having company-realmId={} userId={} consumerRealmId={}", company.getIAMRealmId(), userAuthId, consumerRealmId);
                return;
            }
            log.info("CDMAdapterAPI=addPersonaToUserStarted with PayrollRequestContext having company-realmId={} userId={} employeeId={}", company.getIAMRealmId(), userAuthId, employee.getId());

            if(employee.getProcessFlagWorkflowPackager().getWorkflowState(EmployeeProcessFlagWorkflows.PERSONA_CHECK).equals(ProcessFlagWorkflowState.DONE)) {
                log.info("CDMAdapterAPI=addPersonaToUserSkipped with PayrollRequestContext having company-realmId={} userId={} employeeId={}", company.getIAMRealmId(), userAuthId, employee.getId());
                return;
            }
            createPersonaInRealmForEmployee(company.getIAMRealmId(), userAuthId, employee);
            Application.commitUnitOfWork();
        } catch (Exception e) {
            log.error("CDMAdapterAPI=initiatePersonaFixIfPersonaCheckNotDoneToUserException with PayrollRequestContext having userId={} consumerRealmId={} companyPSID={}", userAuthId, consumerRealmId, company.getSourceCompanyId(), e);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public void createPersonaInRealmForEmployee(String realmId, String userAuthId, Employee employee) {
        String employeeId = employee.getId().toString();
        try{
            String consumerRealmId = employee.getConsumerRealmId();
            //get auth-id from consumer realm id stored in PSP-DB
            List<Persona> adminPersonas = IUSClientWrapper.findAdminPersonaForConsumerRealmId(consumerRealmId);
            String authId = adminPersonas.get(0).getUserId();

            //authorisation of user using
            // UserAuthId in AuthHeaders should match with UserAuthId in ConsumerRealmId in IUS
            if(authId.equals(userAuthId)) {
                storePersonaId(realmId, userAuthId, employee);
            } else {
                log.info("CDMAdapterAPI=createPersonaInRealmForEmployeeNotAllowed with PayrollRequestContext having company-realmId={} userId={} employeeId={}", realmId, userAuthId, employeeId);
            }
        } catch (Exception e) {
            log.error("CDMAdapterAPI=createPersonaInRealmForEmployeeException with PayrollRequestContext having company-realmId={} userId={} employeeId={}", realmId, userAuthId, employeeId, e);
        }
    }

    private void storePersonaId(String realmId, String userAuthId, Employee employee) {
        String personaId = getPersonaInRealm(userAuthId, realmId);
        if(StringUtil.isNullOrEmpty(personaId)) {
            personaId = addUserToRealm(userAuthId, realmId);
            if(StringUtil.isNullOrEmpty(personaId)) {
                log.info("CDMAdapterAPI=addPersonaToUserFailed with PayrollRequestContext having companyRealmId={} userId={} employeeId={}", realmId, userAuthId, employee.getId().toString());
                return;
            }
            log.info("CDMAdapterAPI=addPersonaToUserPerformed with PayrollRequestContext having companyRealmId={} userId={} employeeId={} personaId={}", realmId, userAuthId, employee.getId().toString(), personaId);
            employee.setPersonaId(personaId);
            logAddPersonaToUserSuccess(realmId, userAuthId, employee.getId().toString(), personaId);
        } else {
            if(!StringUtils.equals(employee.getPersonaId(), personaId)) {
                employee.setPersonaId(personaId);
                logAddPersonaToUserSuccess(realmId, userAuthId, employee.getId().toString(), personaId);
            } else {
                log.info("CDMAdapterAPI=addPersonaToUserNotRequired with PayrollRequestContext having companyRealmId={} userId={} employeeId={} personaId={}", realmId, userAuthId, employee.getId().toString(), personaId);
            }
        }
        employee.setProcessFlagWorkflowState(EmployeeProcessFlagWorkflows.PERSONA_CHECK, ProcessFlagWorkflowState.DONE);
    }

    private String getPersonaInRealm(String userAuthId, String realmId) {
        try {
            return identityServiceClientFactory.getIdentityServiceClient().getPersonaInRealmIfExists(userAuthId, realmId);
        } catch (IdentityGraphQLException | IDLMException ex) {
            log.error("Error checking user={} in realm={},exception={}", userAuthId, realmId, ex);
            return null;
        }
    }

    private String addUserToRealm(String userAuthId, String realmId) {
        try {
            return identityServiceClientFactory.getIdentityServiceClient().addUserToRealm(userAuthId, realmId);
        } catch (IdentityGraphQLException | IDLMException ex) {
            log.error("Error adding user={} to realm={}, exception={}", userAuthId, realmId, ex);
            return null;
        }
    }

    private void logAddPersonaToUserSuccess(String realmId, String userAuthId, String employeeId, String personaId) {
        log.info("CDMAdapterAPI=addPersonaToUserSuccess with PayrollRequestContext having companyRealmId={} userId={} employeeId={} personaId={}", realmId, userAuthId, employeeId, personaId);
    }
}
