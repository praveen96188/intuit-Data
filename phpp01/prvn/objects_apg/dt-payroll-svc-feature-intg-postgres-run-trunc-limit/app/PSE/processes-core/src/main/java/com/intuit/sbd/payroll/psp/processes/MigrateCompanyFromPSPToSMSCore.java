package com.intuit.sbd.payroll.psp.processes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.money.account.model.ProfileMigrationRequest;
import com.intuit.payments.cdm.v2.client.EntitlementInfo;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.enums.AccountStatusEnum;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPStringUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.processes.common.PSPToSMSMigrationHelper;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.sbg.psp.accountservices.AccountServicesProfileMigrationResponseModel;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.EMPTY;


@Slf4j
public class MigrateCompanyFromPSPToSMSCore extends Process implements IProcess {

    private static String MONEY_OUT = "MONEYOUT";
    private static String QBDT_PAYROLL_APP_CHANNEL = "QBDT_Payroll";

    public Company company;
    private String sourceCompanyId;
    private SourceSystemCode sourceSystemCd;
    private String tid;
    private ProfileMigrationRequest profilemigrationrequest;
    private PaymentsAccount paymentsAccount = null;
    private AccountServiceGateway accountServiceGateway;
    private boolean isDebugEnabled;
    private static final List<AccountStatusEnum> ACTIVE_ENTITLEMENT_STATUS;

    private AMSValidationProcess amsValidationProcess;

    static {
        ACTIVE_ENTITLEMENT_STATUS = Arrays.asList(
                AccountStatusEnum.ACTIVE, AccountStatusEnum.ACTIVE_NO_BANK, AccountStatusEnum.PENDING,
                AccountStatusEnum.SUSPENDED, AccountStatusEnum.PRE_CLOSED);
    }
    private final boolean isRealmReset;

    public MigrateCompanyFromPSPToSMSCore(String sourceCompanyId, SourceSystemCode sourceSystemCd, String tid) {
        this(sourceCompanyId, sourceSystemCd, tid, false);
    }

    public MigrateCompanyFromPSPToSMSCore(String sourceCompanyId, SourceSystemCode sourceSystemCd, String tid, boolean debugEnabled) {
        this(sourceCompanyId, sourceSystemCd, tid, false, false);
    }

    public MigrateCompanyFromPSPToSMSCore(String sourceCompanyId, SourceSystemCode sourceSystemCd, String tid, boolean debugEnabled, boolean realmReset) {
        this(Company.findCompany(sourceCompanyId,sourceSystemCd),tid,debugEnabled,realmReset);
        this.sourceCompanyId = sourceCompanyId;
        this.sourceSystemCd = sourceSystemCd;
    }
    public MigrateCompanyFromPSPToSMSCore(Company company, String tid, boolean debugEnabled, boolean realmReset) {
        this.company = company;
        this.tid = tid;
        accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
        this.isDebugEnabled = debugEnabled;
        this.isRealmReset = realmReset;
    }

    /*
        For the below 5 validation errors, the SMSMigration state to be saved to DB is as follows.
        1.) CompanyDoesNotExist - Add new fn() in SMSMigration to be able to update state using just PSID.
        2.) CompanyAlreadyMigrated - Nothing to do here as this is a legitimate scenario and don't require any saving of state.
        3.) CompanyDoesNotExistOnDDService - Saving SMSMigration.MigrationError state to DB in case of absence of DD company service.
        4.) CompanyNotActiveOnDDService - Saving SMSMigration.MigrationError state to DB in case of absence of active DD company service.
        5.) ActivePrimaryEntitlementDoesNotExists - Saving SMSMigration.MigrationError state to DB in case of absence of Active Entitlement.
        6.) ProfileMigrationRequestCreateError - Saving SMSMigration.MigrationError state to DB. This might be caused due to data issue.
        7.) MultiplePSIDStoOneRealm - Saving SMSMigration.MigrationError state to DB in case of multiple PSID's for a single realm.
            Will retry for migration in future once data is fixed.
     */
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();
        String logPrefix = "job=PSPtoSMSMigration, Action=MigrateCompanyFromPSPToSMSCoreValidation, Status={}, psid={}, tid={}, realmReset={}";

        //validates company exists
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            log.error(logPrefix, "CompanyDoesNotExist", sourceCompanyId, tid, isRealmReset);
            return validationResult;
        }

        DomainEntitySet<SMSMigration> smsMigrations = SMSMigration.getSmsMigrationBySourceCompanyId(sourceCompanyId);
        if(isRealmReset){
            if(!company.isMoneyMovementOnboardingEnabled()) {
                validationResult.getMessages().PSPMigrateRequestError(company.getIAMRealmId(), "MoneyMovementOnboardingEnabled=false");
                log.error(logPrefix, "MoneyMovementOnboardingEnabled=false", sourceCompanyId, tid, isRealmReset);
                return validationResult;
            }
        } else if ((CollectionUtils.isEmpty(smsMigrations)
                || smsMigrations.getFirst().getMigrationStatus() == SMSMigrationStatus.MigrationComplete)) {
            validationResult.getMessages().PSPMigrateRequestError(company.getIAMRealmId(), "Company already migrated");
            log.error(logPrefix, "CompanyAlreadyMigrated", sourceCompanyId, tid, isRealmReset);
            return validationResult;
        }

        //To exclude companies that have multiple PSID's for a single realm
        Set<Company> companies = Company.findAllCompaniesByRealmId(company.getIAMRealmId());
        if (companies.size() >= 2) {
            validationResult.getMessages().PSPMigrateRequestError(company.getIAMRealmId(), "Multiple PSID's, single realm");
            SMSMigration.setSMSMigrationStatus(sourceCompanyId, company, SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "MultiplePSIDStoOneRealm, realmId=" + company.getIAMRealmId(), company.getSourceCompanyId(), tid);
            return validationResult;
        }

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        if (companyService == null) {
            validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId, ServiceCode.DirectDeposit.toString());
            setSMSMigrationStatus(SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "CompanyDoesNotExistOnDDService", sourceCompanyId, tid,isRealmReset);
            return validationResult;
        }

        if (!(companyService.getStatusCd().equals(ServiceSubStatusCode.ActiveCurrent) || companyService.getStatusCd().equals(ServiceSubStatusCode.PendingFirstPayroll))) {
            validationResult.getMessages().CompanyNotActiveOnService(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId, ServiceCode.DirectDeposit.toString());
            setSMSMigrationStatus(SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "CompanyNotActiveOnDDService", sourceCompanyId, tid,isRealmReset);
            return validationResult;
        }

        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

        if (ObjectUtils.isEmpty(entitlementUnit)) {
            validationResult.getMessages().ActivePrimaryEntitlementDoesNotExists(EntityName.Company, sourceCompanyId, sourceCompanyId);
            SMSMigration.setSMSMigrationStatus(sourceCompanyId, company, SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "ActivePrimaryEntitlementDoesNotExists", sourceCompanyId, tid);
            return validationResult;
        }

        if(company.getOnHoldReasonCollection().find(OnHoldReason.ExpirationDate().isNull()).isNotEmpty()){
            validationResult.getMessages().CompanyOnHoldForService(EntityName.Company, sourceCompanyId, sourceSystemCd.toString(),
                    sourceCompanyId, "Company");
            log.error(logPrefix, "CompanyOnHoldForService", sourceCompanyId, tid);
            return validationResult;
        }


        if (Objects.isNull(company.getComplianceAddress())) {
            validationResult.getMessages().ComplianceAddressNullError(company.getIAMRealmId(), "Compliance address is null");
            SMSMigration.setSMSMigrationStatus(sourceCompanyId, company, SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "ComplianceAddressNull", sourceCompanyId, tid);
            return validationResult;
        }

        profilemigrationrequest = PSPToSMSMigrationHelper.createProfileMigrationRequest(company, sourceCompanyId, tid, isDebugEnabled);

        if (isRealmReset && Objects.isNull(company.getComplianceAddress()) && Objects.nonNull(profilemigrationrequest.getCompany())){
            profilemigrationrequest.getCompany().setAddress(PSPToSMSMigrationHelper.getCompanyAddressViaLegalAddress(company, "Location"));
        }

        if (null == profilemigrationrequest) {
            validationResult.getMessages().PSPMigrateRequestError(company.getIAMRealmId(), "profilemigrationrequest object empty");
            setSMSMigrationStatus(SMSMigrationStatus.MigrationError);
            log.error(logPrefix, "ProfileMigrationRequestCreateError", sourceCompanyId, tid,isRealmReset);
            return validationResult;
        }

        paymentsAccount = getPaymentsAccount(company.getIAMRealmId());

        return validationResult;
    }

    public void setSMSMigrationStatus(SMSMigrationStatus status) {
        if (!isRealmReset) {
            SMSMigration.setSMSMigrationStatus(sourceCompanyId, company, status);
        }
    }

    public ProcessResult process() {
        String logPrefix = "job=PSPtoSMSMigration, Action=MigrateCompanyFromPSPToSMSCoreExecute, Status={}, realmId={}, tid={}, realmReset={}, psid={}{}";

        log.info(logPrefix, "Start", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", isPaymentsAccountAlreadyPresent=" + Objects.nonNull(paymentsAccount));
        ProcessResult<SMSMigrationStatus> pr = new ProcessResult<SMSMigrationStatus>();

        EnableSMSMigrationFlagCore enableSMSMigrationFlagCore = new EnableSMSMigrationFlagCore(company, tid,isRealmReset);
        amsValidationProcess = new AMSValidationProcess(company);

        if (Objects.nonNull(paymentsAccount) && isPaymentsAccountActive(paymentsAccount)) {
            Map<String, EntitlementInfo> entitlementInfos = paymentsAccount.getEntitlementInfos();

            if (MapUtils.isNotEmpty(entitlementInfos)) {

                EntitlementInfo moneyout = entitlementInfos.get(MONEY_OUT);
                if (Objects.nonNull(moneyout)) {

                    String appChannel = moneyout.getApplicationChannel();
                    if (StringUtils.isNotEmpty(appChannel)) {
                        if (appChannel.contains(QBDT_PAYROLL_APP_CHANNEL)) {
                            if (validateAMSUpdate()) {
                                pr.setResult(SMSMigrationStatus.MigrationComplete);
                            } else {
                                pr.setResult(SMSMigrationStatus.MigrationError);
                                log.error(logPrefix, "StaleData", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, EMPTY);
                            }
                        }
                    }
                }
            }

            if (Objects.isNull(pr.getResult())) {
                pr.setResult(SMSMigrationStatus.MigrationOnHold);
            }
            log.info(logPrefix, "PaymentsAccountAlreadyPresent", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", SMSMigrationStatus=" + pr.getResult());
        } else {
            try {
                AccountServicesProfileMigrationResponseModel profileMigrationResponse = accountServiceGateway.migratePSPAccount(profilemigrationrequest, company.getIAMRealmId(), tid);
                if (profileMigrationResponse != null) {
                    if (validateAMSUpdate()) {
                        pr.setResult(SMSMigrationStatus.MigrationComplete);
                    } else {
                        pr.setResult(SMSMigrationStatus.MigrationError);
                        log.error(logPrefix, "validateAMSUpdateFailed", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, EMPTY);
                    }
                }
            } catch (AccountServicesException e) {
                if (e.getHttpServiceResponse().isClientErrors()) {
                    pr.setResult(SMSMigrationStatus.MigrationError);
                } else {
                    pr.setResult(SMSMigrationStatus.DataCollectionComplete);
                }
                log.error(logPrefix, "Error", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", errType=AccountServicesException" + ", errMsg=" + e.getMessage() + ", SMSMigrationStatus=" + pr.getResult(), e);
            } catch (JsonProcessingException e) {
                pr.setResult(SMSMigrationStatus.DataCollectionComplete);
                log.error(logPrefix, "Error", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", errType=JsonProcessingException" + ", errMsg=" + e.getMessage() + ", SMSMigrationStatus=" + pr.getResult(), e);
            } catch(CallNotPermittedException e) {
                pr.setResult(SMSMigrationStatus.DataCollectionComplete);
                log.error(logPrefix, "Error", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", errType=AccountServicesException" + ", errMsg=" + e.getMessage() + ", SMSMigrationStatus=" + pr.getResult(), e);
            }
        }
        if (SMSMigrationStatus.MigrationComplete.equals(pr.getResult())) {
            pr = enableSMSMigrationFlagCore.execute();
        }

        CompanyEvent.createSMSMigratedEvent(pr.getResult(), company);
        setSMSMigrationStatus(pr.getResult());
        log.info(logPrefix, "Complete", company.getIAMRealmId(), tid, isRealmReset, sourceCompanyId, ", SMSMigrationStatus=" + pr.getResult());
        return pr;
    }

    private boolean isPaymentsAccountActive(PaymentsAccount paymentsAccount) {
        String logPrefix = "job=PSPtoSMSMigration, Action=isPaymentsAccountActive, Status={}, realmId={}, tid={}, realmReset={}, psid={}{}";
        String realmId = company.getIAMRealmId();
        log.info(logPrefix, "Start", realmId, tid, isRealmReset, sourceCompanyId, "");
        boolean isActive = false;
        try {
            Map<String, EntitlementInfo> entitlementInfoMap = paymentsAccount.getEntitlementInfos();

            // if Payments Account has at least one Active Entitlement, then it is an active Payments Account
            if (!(isNull(entitlementInfoMap)) && hasActiveEntitlements(entitlementInfoMap)) {
                isActive = true;
            }
            log.info(logPrefix, "Complete", realmId, tid, isRealmReset, sourceCompanyId, ", isPaymentsAccountActive=" + isActive);
        } catch (Exception e) {
            log.error(logPrefix, "Error", realmId, tid, isRealmReset, sourceCompanyId, ", errType=" + e.getClass().getSimpleName() + ", errMsg=" + e.getMessage());
            throw e;
        }
        return isActive;
    }

    /**
     * @param map Entitlements Info map. For example, Payments, Payroll, Loan etc.
     * @return true if at least one of the EntitlementInfo.Status is ACTIVE/ACTIVE_NO_BANK/PENDING/SUSPENDED/PRE_CLOSED
     * false otherwise
     */
    private boolean hasActiveEntitlements(Map<String, EntitlementInfo> map) {
        return map.values().stream().anyMatch(
                v -> ACTIVE_ENTITLEMENT_STATUS.contains(v.getStatus()));
    }


    private PaymentsAccount getPaymentsAccount(String realmId) {
        String logPrefix = "Action=getPaymentsAccount, Status={}, realmId={}{}";

        try {
            paymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
        } catch (AccountServicesException e) {
            // PSP defensively checks existence of AMS account, before creating a new one. Hence marking 404 as info in this case
            if (HttpStatus.SC_NOT_FOUND == e.getHttpServiceResponse().getStatusCode()) {
                log.info(logPrefix, "PaymentsAccountNotFound", realmId, tid);
            } else {
                log.error(logPrefix, "getPaymentsAccountError", realmId, tid, e);
            }
        } catch(HttpClientErrorException excp) {
            log.info(logPrefix, "ClientErrorExceptionPaymentsAccountNotFound", realmId, tid);
        } catch (CallNotPermittedException cnpe) {
            log.error(logPrefix, "getPaymentsAccountCallNotPermittedException", realmId, tid, cnpe);
        }

        return paymentsAccount;
    }

    /**
     * This method compares PSP Account profile with AMS profile
     *
     * @return true if same, false otherwise
     */
    private boolean validateAMSUpdate() {

        boolean validationResult = false;
        String realmId = company.getIAMRealmId();

        String logPrefix = "job=PSPtoSMSMigration, Action=validateAMSUpdate, Status={}, realmId={}, tid={}, realmReset={}, psid={}{}";
        log.info(logPrefix, "Start", realmId, tid, isRealmReset, sourceCompanyId, EMPTY);

        try {
            amsValidationProcess.setCountryCheckEnabled(false);
            amsValidationProcess.setAddressCheckEnabled(false);
            ProcessResult<Boolean> pr = amsValidationProcess.process();
            validationResult = pr.getResult();
            log.info(logPrefix, "Complete", realmId, tid, isRealmReset, sourceCompanyId, ", validationResult=" + validationResult);
        } catch (Exception e) {
            log.error(logPrefix, "Error", realmId, tid, isRealmReset, sourceCompanyId, e.getMessage(), e);
        }
        return validationResult;
    }
}