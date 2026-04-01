package com.intuit.ems.payroll.psp.ams_migration;

import com.google.common.collect.Lists;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PrimaryBusiness;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class DBANameChangedFix {

    private String fileName;
    private List<String> realmIds;
    private boolean updateAMS;
    private int batchSize;
    private EventTypeCode eventTypeCode;
    private AccountServiceGateway accountServiceGateway;

    private DBANameChangedFix(String fileName, EventTypeCode eventTypeCode, boolean updateAMS, int batchSize) {
        this.fileName = fileName;
        this.eventTypeCode = eventTypeCode;
        this.updateAMS = updateAMS;
        this.batchSize = batchSize;
        this.accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
    }

    public static void main(String[] args) {
        String fileName = args[0];
        String eventTypeCodeStr = args[1];
        boolean updateAMS = Boolean.parseBoolean(args[2]);
        int batchSize = Integer.parseInt(args[3]);
        System.out.println("Job=DBANameChangedFix, Action=mainFn, FileName=" + fileName + ", EventTypeCode=" + eventTypeCodeStr + ", updateAMS=" + updateAMS + ", batchSize=" + batchSize);
        DBANameChangedFix dbaNameChangedFix = new DBANameChangedFix(fileName, EventTypeCode.valueOf(eventTypeCodeStr), updateAMS, batchSize);
        dbaNameChangedFix.fixMigrateDBAEvents();
        log.info("Main fn ended");
        System.exit(0);
    }

    private List<String> readRealmIdsFromFile(String fileName) throws IOException {
        List<String> realmList = Files.lines(Paths.get(fileName)).collect(Collectors.toList());
        realmList.replaceAll(String::trim);
        return realmList;
    }

    private void fixMigrateDBAEvents() {
        String logPrefix = "Job=DBANameChangedFix, Action=fixMigrateDBAEvents, Status={}, EventTypeCode={}{}";
        try {
            Application.initialize();
            log.info(logPrefix, "Start", eventTypeCode, StringUtils.EMPTY);
            realmIds = readRealmIdsFromFile(fileName);
            log.info("RealmIdCount={}", realmIds.size());
            long s = System.currentTimeMillis();

            List<List<String>> realmIdsList = Lists.partition(realmIds, batchSize);
            log.info("RealmIdListsCount={}", realmIdsList.size());

            realmIdsList.forEach(realmIdList -> {
                fixBatchMigrateEvents(realmIdList);
            });
            long e = System.currentTimeMillis();

            log.info(logPrefix, "Complete", eventTypeCode, ", time_taken_millis=" + (e-s));
        } catch (IOException e) {
            log.error(logPrefix, "Error", eventTypeCode, ", errType=IOException", e);
        } catch (Exception e) {
            log.error(logPrefix, "Error", eventTypeCode, ", errType=" + e.getMessage(), e);
        } finally {
            Application.uninitialize();
        }
    }

    private void fixBatchMigrateEvents(List<String> realmIds) {
        String logPrefix = "Job=DBANameChangedFix, Action=fixBatchMigrateEvents, Status={}, EventTypeCode={}{}";
        log.info(logPrefix, "Start", eventTypeCode, StringUtils.EMPTY);
        String realmIdsLoggerStr = StringUtils.join(realmIds,",");

        long s = System.currentTimeMillis();
        Map<String,CompanyData> companyDataMap = getCompanyDataForRealmIds(realmIds);

        if (MapUtils.isEmpty(companyDataMap)) {
            log.error(logPrefix, "Error", eventTypeCode, ", errType=CompanyDataMapEmpty");
            return;
        }

        companyDataMap.forEach((realmId, data) -> {
            String logPrefix2 = "Job=DBANameChangedFix, Action=fixMigrateEventsForRealm, Status={}, realmId={}, EventTypeCode={}{}";
            log.info(logPrefix2, "Start", realmId, eventTypeCode, StringUtils.EMPTY);
            boolean updateStatus = verifyEventAndUpdateAMS(realmId, data);

            if (updateStatus) {
                log.info(logPrefix2, "Complete", realmId, eventTypeCode, StringUtils.EMPTY);
            } else {
                log.error(logPrefix2, "Error", realmId, eventTypeCode, ", errType=UpdateAMSFailed");
            }
        });
        long e = System.currentTimeMillis();


        log.info(logPrefix, "Complete", eventTypeCode, ", time_taken_millis=" + (e-s) + ", realmIds=" + realmIdsLoggerStr);
    }

    private boolean verifyEventAndUpdateAMS(String realmId, CompanyData companyData) {
        String logPrefix = "Job=DBANameChangedFix, Action=verifyEventAndUpdateAMS, Status={}, realmId={}, EventTypeCode={}{}";

        String oldCompanyEventDetailValue = companyData.getEventOldString();
        String newCompanyEventDetailValue = companyData.getEventNewString();

        log.info(logPrefix, "Start", realmId, eventTypeCode, ", oldValue=" + oldCompanyEventDetailValue + ", newValue=" + newCompanyEventDetailValue);

        if (StringUtils.equalsIgnoreCase(oldCompanyEventDetailValue, newCompanyEventDetailValue)) {
            log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=CompanyNameIgnoreCaseMatch");
            return true;
        }

        if (StringUtils.equalsIgnoreCase(companyData.getLegalName(), newCompanyEventDetailValue)) {
            log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", SubStatus=LegalNameMatchWithEvent");
            if (updateAMS) {
                boolean updatePaymentsAccountWithBusinessName = updatePaymentsAccountWithBusinessName(oldCompanyEventDetailValue, realmId);
                log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=UpdateAMSComplete, updateStatus=" + updatePaymentsAccountWithBusinessName);
                return updatePaymentsAccountWithBusinessName;
            } else {
                log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=UpdateAMSIgnored");
                return true;
            }
        }
        log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=LegalNameMismatchWithEvent, oldValue=" + oldCompanyEventDetailValue + ", newValue=" + newCompanyEventDetailValue);
        return false;
    }

    private boolean updatePaymentsAccountWithBusinessName(String newDBAName, String realmId) {
        String logPrefix = "Job=DBANameChangedFix, Action=updatePaymentsAccountWithBusinessName, Status={}, realmId={}, EventTypeCode={}{}";
        try {
            log.info(logPrefix, "Start", realmId, eventTypeCode, StringUtils.EMPTY);

            PaymentsAccount currentPaymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
            String oldBusinessName = getBusinessNameFromPaymentsAccount(currentPaymentsAccount);
            log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", currentAMSBusinessName=" + oldBusinessName + ", newDBAName=" + newDBAName);

            PaymentsAccount updatedPaymentsAccount = createPaymentsAccountWithBusinessName(newDBAName);

            PaymentsAccount latestPaymentsAccount = accountServiceGateway.updatePaymentsAccount(realmId, updatedPaymentsAccount);
            boolean validPaymentsAccount = validatePaymentsAccountUpdate(newDBAName, realmId, latestPaymentsAccount);

            if (validPaymentsAccount) {
                log.info(logPrefix, "Complete", realmId, eventTypeCode, StringUtils.EMPTY);
            } else {
                log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=UpdatedPaymentsAccountValidationFailed");
            }

            return validPaymentsAccount;
        } catch (AccountServicesException e) {
            if (e.getHttpServiceResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=PaymentsAccountNotFound, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            } else {
                log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=AccountServicesException, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            }
            return false;
        } catch(HttpClientErrorException excp) {
            log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=ClientErrorExceptionPaymentsAccountNotFound, errMsg=" + excp.getResponseBodyAsString(), excp);
            return false;
        } catch (CallNotPermittedException cnpe) {
            log.error(logPrefix, "Error", realmId, ", errType=CallNotPermittedException, errMsg=" + cnpe.getMessage(), cnpe);
            return false;
        }
    }

    private boolean validatePaymentsAccountUpdate(String newDBAName, String realmId, PaymentsAccount latestPaymentsAccount) {
        String logPrefix = "Job=DBANameChangedFix, Action=validatePaymentsAccountUpdate, Status={}, realmId={}, EventTypeCode={}{}";

        if (!StringUtils.equalsIgnoreCase(newDBAName, getBusinessNameFromPaymentsAccount(latestPaymentsAccount))) {
            log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=UpdatePaymentsBusinessNameFailed");
            return false;
        }

        log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", SubStatus=UpdatePaymentsPatchCallSuccess");
        PaymentsAccount newPaymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
        String newBusinessName = getBusinessNameFromPaymentsAccount(newPaymentsAccount);

        if (!StringUtils.equalsIgnoreCase(newDBAName, newBusinessName)) {
            log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=LatestPaymentsBusinessNameMismatch");
            return false;
        }

        log.info(logPrefix, "Complete", realmId, eventTypeCode, ", updatedAMSBusinessName=" + newBusinessName);
        return true;
    }

    private PaymentsAccount createPaymentsAccountWithBusinessName(String newDBAName) {
        PaymentsAccount updatedPaymentsAccount = new PaymentsAccount();
        PrimaryBusiness newBusinessInfo = new PrimaryBusiness();
        newBusinessInfo.setBusinessName(newDBAName);
        updatedPaymentsAccount.setBusinessInfo(newBusinessInfo);
        return updatedPaymentsAccount;
    }

    private String getBusinessNameFromPaymentsAccount(PaymentsAccount paymentsAccount) {
        if (Objects.isNull(paymentsAccount) || Objects.isNull(paymentsAccount.getBusinessInfo())) {
            return StringUtils.EMPTY;
        }
        return paymentsAccount.getBusinessInfo().getBusinessName();
    }

    private Map<String,CompanyData> getCompanyDataForRealmIds(List<String> realmIds) {
        String logPrefix = "Job=DBANameChangedFix, Action=getCompanyDataForRealmIds, Status={}, EventTypeCode={}{}";
        String realmIdsLoggerStr = StringUtils.join(realmIds,",");
        log.info(logPrefix, "Start", eventTypeCode, ", realmIds=" + realmIdsLoggerStr);

        try {
            Application.beginUnitOfWork();
            String companyHql =
                    "Select c.SourceCompanyId, c.IAMRealmId, c.LegalName, ed.Value, ed.EventDetailTypeCd  " +
                            "             from com.intuit.sbd.payroll.psp.domain.Company as c, com.intuit.sbd.payroll.psp.domain.CompanyEvent as e, com.intuit.sbd.payroll.psp.domain.CompanyEventDetail as ed, com.intuit.sbd.payroll.psp.domain.SMSMigration as s" +
                            "             where e.Company = c and s.Company = c" +
                            "             and e.EventTypeCd = 'DBANameChanged' and ed.CompanyEvent = e" +
                            "             and s.MigrationStatus = 'MigrationComplete' and e.CreatorId = 'PayrollAPI' " +
                            "             and s.ModifiedDate < e.CreatedDate " +
                            "             and c.IAMRealmId in (:REALM_LIST)";

            String[] paramNames = {"REALM_LIST"};
            Object[] paramValues = {realmIds};
            List<Object[]> results = Application.executeHQLQuery(companyHql, paramNames, paramValues);

            if (results.size() != 2*realmIds.size()) {
                log.error(logPrefix, "Error", eventTypeCode, ", errType=QueryResultsCountError, resultsCount=" + results.size() + ", realmIds=" + realmIdsLoggerStr);
                return MapUtils.EMPTY_MAP;
            }

            Map<String, CompanyData> companyDataMap = new HashMap<>();
            for (Object[] r : results) {
                String psid = (String) r[0];
                String realmId = (String) r[1];
                String legalName = (String) r[2];
                String eventString = (String) r[3];
                EventDetailTypeCode eventTypeCode = (EventDetailTypeCode) r[4];

                CompanyData companyData = companyDataMap.get(realmId);

                if (ObjectUtils.isEmpty(companyData)) {
                    // not yet entered
                    CompanyData newCompanyData;
                    if (EventDetailTypeCode.OldStringValue.equals(eventTypeCode)) {
                        newCompanyData = new CompanyData(psid, realmId, legalName, eventString, null);
                    } else if (EventDetailTypeCode.NewStringValue.equals(eventTypeCode)) {
                        newCompanyData = new CompanyData(psid, realmId, legalName, null, eventString);
                    } else {
                        throw new Exception("Wrong type of event string found");
                    }

                    companyDataMap.put(realmId, newCompanyData);
                } else {
                    if (EventDetailTypeCode.OldStringValue.equals(eventTypeCode)) {
                        companyData.setEventOldString(eventString);
                    } else if (EventDetailTypeCode.NewStringValue.equals(eventTypeCode)) {
                        companyData.setEventNewString(eventString);
                    } else {
                        throw new Exception("Wrong type of event string found");
                    }

                    companyDataMap.put(realmId, companyData);
                }
            }

            log.info(logPrefix, "Complete", eventTypeCode, StringUtils.EMPTY);
            return companyDataMap;

        } catch (Exception e) {
            log.error(logPrefix, "Error", eventTypeCode, ", realmIds=" + realmIdsLoggerStr + ", batchSize=" + realmIds.size() + ", errType=" + e.getMessage(), e);
            return MapUtils.EMPTY_MAP;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @Getter
    @Setter
    @ToString
    private static class CompanyData {
        private String psid;
        private String realmId;
        private String legalName;
        private String eventOldString;
        private String eventNewString;

        CompanyData(String psid, String realmId, String legalName, String eventOldString, String eventNewString) {
            this.psid = psid;
            this.realmId = realmId;
            this.legalName = legalName;
            this.eventOldString = eventOldString;
            this.eventNewString = eventNewString;
        }
    }
}
