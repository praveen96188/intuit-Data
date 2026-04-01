package com.intuit.sbd.payroll.psp.processes.accountservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.money.account.model.ProfileMigrationRequest;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.common.PSPToSMSMigrationHelper;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.sbg.psp.accountservices.AccountServicesProfileMigrationResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.util.Objects;


@Slf4j
public class RealmResetAccountServiceCore extends Process implements IProcess {

    public Company company;
    private String tid;
    private String oldRealmId;
    private MigrateCompanyFromPSPToSMSCore migrateCompanyFromPSPToSMSCore = null;
    private DeactivateAccountService deactivateAccountService = null;

    public RealmResetAccountServiceCore(Company companyWithNewRealmId, String oldRealmId, String tid) {
        this.company = companyWithNewRealmId;
        this.oldRealmId = oldRealmId;
        this.tid = tid;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        migrateCompanyFromPSPToSMSCore = new MigrateCompanyFromPSPToSMSCore(company,tid,false,true);
        validationResult.merge(migrateCompanyFromPSPToSMSCore.validate());
        return validationResult;
    }

    public ProcessResult process() {
        String logPrefix = "job=RealmReset, Action=RealmResetAccountServiceCore, Status={}, newRealmId={}, oldRealmId tid={}, psid={}{}";
        log.info(logPrefix, "Start", company.getIAMRealmId(),oldRealmId, tid, company.getSourceCompanyId(), "");
        ProcessResult<SMSMigrationStatus> pr = migrateCompanyFromPSPToSMSCore.process();
        log.info(logPrefix, "MigrateCompanyFromPSPToSMSCore", company.getIAMRealmId(),oldRealmId, tid, company.getSourceCompanyId(), ", SMSMigrationStatus=" + pr.getResult());
        if (!pr.isSuccess()){
            return pr;
        }
        if (!SMSMigrationStatus.MigrationComplete.equals(pr.getResult())){
            pr.getMessages().PSPMigrateRequestError(company.getIAMRealmId(), "Failed to migrate SMSMigrationStatus="+pr.getResult());
            return pr;
        }
        log.info(logPrefix, "StartDeactivateAccountServiceCore", company.getIAMRealmId(), tid, company.getSourceCompanyId(), ", SMSMigrationStatus=" + pr.getResult());
        String newRealmId = company.getIAMRealmId();
        try {
            company.setIAMRealmId(oldRealmId);
            deactivateAccountService = new DeactivateAccountService(company);
            pr.merge(deactivateAccountService.execute());
        } finally {
            company.setIAMRealmId(newRealmId);
        }
        log.info(logPrefix, "Complete", company.getIAMRealmId(),oldRealmId, tid, company.getSourceCompanyId(),  ", SMSMigrationStatus=" + pr.getResult());
        return pr;
    }
}
