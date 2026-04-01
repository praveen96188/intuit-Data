package com.intuit.sbd.payroll.psp.processes.iam;

import com.intuit.platform.integration.ius.common.types.Grant;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.QuickbooksInfo;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddOrUpdateGrantProcessor {

    private static SpcfLogger logger = Application.getLogger(AddOrUpdateGrantProcessor.class);

    protected String requestRealmId;
    protected Company company;
    protected RealmManager realmManager;

    protected AddOrUpdateGrantProcessor(Company company) {
        this.company = company;
    }

    public AddOrUpdateGrantProcessor(Company company, String requestRealmId) {
        this.requestRealmId = requestRealmId;
        this.company = company;

        this.realmManager = new RealmManager();
    }

    public ProcessResult execute() {
        ProcessResult processFlowResult = validate();
        if (processFlowResult.isSuccess()) {
            ProcessResult processingResult = process();
            processFlowResult.setResult(processingResult.getResult());
            processFlowResult.merge(processingResult);
        }
        return processFlowResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(company)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        try {
            String realmIdGuidSyncFlag = FeatureFlags.get().stringValue(FeatureFlags.Key.REALMID_GUID_SYNC_FLAG,"DISABLE");

            if ("READ".equals(realmIdGuidSyncFlag)) {
                // logging added, will be removed once we got enough monitoring data
                if (StringUtils.isEmpty(company.getIAMRealmId())) {
                    logger.info("RealmID for the company=" + company.getSourceCompanyId() + " is null or empty");
                }
                if (!StringUtils.equals(company.getIAMRealmId(), requestRealmId)) {
                    logger.info("Mismatch between company and validation request Realm, dbRealmId=" +
                            company.getIAMRealmId() + " and realmId=" + requestRealmId);
                }

                if (!StringUtils.isEmpty(requestRealmId)) {
                    if (!realmManager.realmHasPayrollGrant(requestRealmId)) {
                        logger.info("Payroll Grant was not found on realm=" + requestRealmId);
                    } else {
                        logger.info("Payroll Grant was found on realm=" + requestRealmId);
                    }
                }
            } else if ("WRITE".equals(realmIdGuidSyncFlag)) {
                if(!StringUtils.isEmpty(requestRealmId) && StringUtils.equals(company.getIAMRealmId(),
                        requestRealmId)) {
                    if (!realmManager.realmHasPayrollGrant(requestRealmId)) {
                        logger.info("Adding Payroll Grant for realm="+ requestRealmId);
                        Grant grant = realmManager.addPayrollGrantToRealm(requestRealmId, getGrantAttributeKeyMap(), getEntitlementInfoKeyMap(false));
                        if(Objects.nonNull(grant)) {
                            processResult.setResult(grant);
                            return processResult;
                        }

                        logger.error(String.format("Unable to add or update payroll grant for realmId=%s", company.getIAMRealmId()));
                        processResult.getMessages().IUSGrantGenericError(requestRealmId, "Unable to add or update grant");
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.error("Unable to add or update payroll grant for realmId \n "+e);
            processResult.getMessages().IUSGrantGenericError(requestRealmId, e.getMessage());
        }

        return processResult;
    }

    protected Map<RealmManager.EntitlementInfoKey, String> getEntitlementInfoKeyMap(boolean useActivePrimaryEntitlement) {
        Entitlement entitlement = getEntitlement(useActivePrimaryEntitlement);

        Map<RealmManager.EntitlementInfoKey, String> entitlementInfoMap = new HashMap<>();

        if(Objects.isNull(entitlement)) {
            return entitlementInfoMap;
        }

        entitlementInfoMap.put(RealmManager.EntitlementInfoKey.SOURCE, "Payroll");
        entitlementInfoMap.put(RealmManager.EntitlementInfoKey.SUBSCRIPTION_NUMBER, entitlement.getSubscriptionNumber());
        return entitlementInfoMap;
    }

    protected Map<RealmManager.GrantAttributeKey, String> getGrantAttributeKeyMap() {
        QuickbooksInfo quickbooksInfo = company.getQuickbooksInfo();

        Map<RealmManager.GrantAttributeKey, String> grantAttributesMap = new HashMap<>();

        if(Objects.isNull(quickbooksInfo)) {
            return grantAttributesMap;
        }

        grantAttributesMap.put(RealmManager.GrantAttributeKey.RELEASE_NUMBER, quickbooksInfo.getReleaseNumber());
        grantAttributesMap.put(RealmManager.GrantAttributeKey.FLAVOR, quickbooksInfo.getQuickbooksSku());
        grantAttributesMap.put(RealmManager.GrantAttributeKey.VERSION_NUMBER, quickbooksInfo.getVersionNumber());
        return grantAttributesMap;
    }

    protected Entitlement getEntitlement(boolean useActivePrimaryEntitlement) {
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();

        if(Objects.nonNull(entitlementUnit)) {
            return entitlementUnit.getEntitlement();
        }

        if(useActivePrimaryEntitlement) {
            return null;
        }

        DomainEntitySet<EntitlementUnit> primaryEntitlementUnits = company.getPrimaryEntitlementUnits();

        if(primaryEntitlementUnits.isEmpty()) {
            return null;
        }

        DomainEntitySet<EntitlementUnit> sortedPrimaryEntitlementUnits = primaryEntitlementUnits.sort(EntitlementUnit.CreatedDate());
        // Get the latest Entitlement Unit
        entitlementUnit = sortedPrimaryEntitlementUnits.get(sortedPrimaryEntitlementUnits.size() - 1);
        return entitlementUnit.getEntitlement();
    }
}
