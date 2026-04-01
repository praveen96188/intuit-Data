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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PPOEmailChangedFix {

    private String fileName;
    private List<String> realmIds;
    private boolean updateAMS;
    private int batchSize;
    private EventTypeCode eventTypeCode;
    private AccountServiceGateway accountServiceGateway;

    private PPOEmailChangedFix(String fileName, EventTypeCode eventTypeCode, boolean updateAMS, int batchSize) {
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
        System.out.println("Job=PPOEmailChangedFix, Action=mainFn, FileName=" + fileName + ", EventTypeCode=" + eventTypeCodeStr + ", updateAMS=" + updateAMS + ", batchSize=" + batchSize);
        PPOEmailChangedFix ppoEmailChangedFix = new PPOEmailChangedFix(fileName, EventTypeCode.valueOf(eventTypeCodeStr), updateAMS, batchSize);
        ppoEmailChangedFix.fixMigratePPOEmailEvents();
        log.info("Main fn ended");
        System.exit(0);
    }

    private List<String> readRealmIdsFromFile(String fileName) throws IOException {
        List<String> realmList = Files.lines(Paths.get(fileName)).collect(Collectors.toList());
        realmList.replaceAll(String::trim);
        return realmList;
    }

    private void fixMigratePPOEmailEvents() {
        String logPrefix = "Job=PPOEmailChangedFix, Action=fixMigratePPOEmailEvents, Status={}, EventTypeCode={}{}";
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
        String logPrefix = "Job=PPOEmailChangedFix, Action=fixBatchMigrateEvents, Status={}, EventTypeCode={}{}";
        log.info(logPrefix, "Start", eventTypeCode, StringUtils.EMPTY);
        String realmIdsLoggerStr = StringUtils.join(realmIds,",");

        long s = System.currentTimeMillis();
        Map<String, CompanyData> companyDataMap = getCompanyDataForRealmIds(realmIds);

        if (MapUtils.isEmpty(companyDataMap)) {
            log.error(logPrefix, "Error", eventTypeCode, ", errType=CompanyDataMapEmpty");
            return;
        }

        companyDataMap.forEach((realmId, data) -> {
            String logPrefix2 = "Job=PPOEmailChangedFix, Action=fixMigrateEventsForRealm, Status={}, realmId={}, EventTypeCode={}{}";
            log.info(logPrefix2, "Start", realmId, eventTypeCode, StringUtils.EMPTY);
            boolean updateStatus = verifyEventAndUpdateAMS(data, realmId);

            if (updateStatus) {
                log.info(logPrefix2, "Complete", realmId, eventTypeCode, StringUtils.EMPTY);
            } else {
                log.error(logPrefix2, "Error", realmId, eventTypeCode, ", errType=UpdateAMSFailed");
            }
        });
        long e = System.currentTimeMillis();


        log.info(logPrefix, "Complete", eventTypeCode, ", time_taken_millis=" + (e-s) + ", realmIds=" + realmIdsLoggerStr);
    }

    private boolean verifyEventAndUpdateAMS(CompanyData companyData, String realmId) {
        // write event change logic
        String logPrefix = "Job=PPOEmailChangedFix, Action=verifyEventAndUpdateAMS, Status={}, realmId={}, EventTypeCode={}{}";
        String oldCompanyEventDetailValue = companyData.getEventOldString();
        String newCompanyEventDetailValue = companyData.getEventNewString();

        log.info(logPrefix, "Start", realmId, eventTypeCode, ", oldValue=" + oldCompanyEventDetailValue + ", newValue=" + newCompanyEventDetailValue);

        if (StringUtils.equalsIgnoreCase(oldCompanyEventDetailValue, newCompanyEventDetailValue)) {
            log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=PPOEmailIgnoreCaseMatch");
            return true;
        }

        if (StringUtils.equals(companyData.getNotificationEmail(), newCompanyEventDetailValue)) {
            log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", SubStatus=CompanyEmailMatchWithEvent");
            if (updateAMS) {
                boolean updatePaymentsAccountWithBusinessEmail = updatePaymentsAccountWithBusinessEmail(oldCompanyEventDetailValue, realmId);
                log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=UpdateAMSComplete, updateStatus=" + updatePaymentsAccountWithBusinessEmail);
                return updatePaymentsAccountWithBusinessEmail;
            } else {
                log.info(logPrefix, "Complete", realmId, eventTypeCode, ", SubStatus=UpdateAMSIgnored");
                return true;
            }
        }
        log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=CompanyEmailMismatchWithEvent");
        return false;
    }

    private boolean updatePaymentsAccountWithBusinessEmail(String newPPOEmail, String realmId) {
        String logPrefix = "Job=PPOEmailChangedFix, Action=updatePaymentsAccountWithBusinessEmail, Status={}, realmId={}, EventTypeCode={}{}";
        try {
            log.info(logPrefix, "Start", realmId, eventTypeCode, StringUtils.EMPTY);

            PaymentsAccount currentPaymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
            String oldBusinessEmail = getBusinessEmailFromPaymentsAccount(currentPaymentsAccount);
            log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", currentAMSBusinessEmail=" + oldBusinessEmail + ", newPPOEmail=" + newPPOEmail);

            PaymentsAccount updatedPaymentsAccount = createPaymentsAccountWithBusinessEmail(newPPOEmail);

            PaymentsAccount latestPaymentsAccount = accountServiceGateway.updatePaymentsAccount(realmId, updatedPaymentsAccount);
            boolean validPaymentsAccount = validatePaymentsAccountUpdate(newPPOEmail, realmId, latestPaymentsAccount);

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

    private boolean validatePaymentsAccountUpdate(String newPPOEmail, String realmId, PaymentsAccount latestPaymentsAccount) {
        String logPrefix = "Job=PPOEmailChangedFix, Action=validatePaymentsAccountUpdate, Status={}, realmId={}, EventTypeCode={}{}";

        if (!StringUtils.equalsIgnoreCase(newPPOEmail, getBusinessEmailFromPaymentsAccount(latestPaymentsAccount))) {
            log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=UpdatePaymentsBusinessEmailFailed");
            return false;
        }

        log.info(logPrefix, "InProgress", realmId, eventTypeCode, ", SubStatus=UpdatePaymentsPatchCallSuccess");
        PaymentsAccount newPaymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
        String newBusinessEmail = getBusinessEmailFromPaymentsAccount(newPaymentsAccount);

        if (!StringUtils.equalsIgnoreCase(newPPOEmail, newBusinessEmail)) {
            log.error(logPrefix, "Error", realmId, eventTypeCode, ", errType=LatestPaymentsBusinessEmailMismatch");
            return false;
        }

        log.info(logPrefix, "Complete", realmId, eventTypeCode, ", updatedAMSBusinessEmail=" + newBusinessEmail);
        return true;
    }

    private String getBusinessEmailFromPaymentsAccount(PaymentsAccount paymentsAccount) {
        if (Objects.isNull(paymentsAccount) || Objects.isNull(paymentsAccount.getBusinessInfo())) {
            return StringUtils.EMPTY;
        }
        return paymentsAccount.getBusinessInfo().getEmail();
    }

    private PaymentsAccount createPaymentsAccountWithBusinessEmail(String newPPOEmail) {
        PaymentsAccount updatedPaymentsAccount = new PaymentsAccount();
        PrimaryBusiness newBusinessInfo = new PrimaryBusiness();
        newBusinessInfo.setEmail(newPPOEmail);
        updatedPaymentsAccount.setBusinessInfo(newBusinessInfo);
        return updatedPaymentsAccount;
    }

    private Map<String, CompanyData> getCompanyDataForRealmIds(List<String> realmIds) {
        String logPrefix = "Job=PPOEmailChangedFix, Action=getCompanyDataForRealmIds, Status={}, EventTypeCode={}{}";
        String realmIdsLoggerStr = StringUtils.join(realmIds,",");
        List<EventDetailTypeCode> eventDetailTypeCodes = new ArrayList<>();
        eventDetailTypeCodes.add(EventDetailTypeCode.OldStringValue);
        eventDetailTypeCodes.add(EventDetailTypeCode.NewStringValue);

        log.info(logPrefix, "Start", eventTypeCode, ", realmIds=" + realmIdsLoggerStr);

        try {
            Application.beginUnitOfWork();
            String companyHql =
                    "Select c.SourceCompanyId, c.IAMRealmId, c.NotificationEmail, ed.Value, ed.EventDetailTypeCd  " +
                            "             from com.intuit.sbd.payroll.psp.domain.Company as c, com.intuit.sbd.payroll.psp.domain.CompanyEvent as e, com.intuit.sbd.payroll.psp.domain.CompanyEventDetail as ed, com.intuit.sbd.payroll.psp.domain.SMSMigration as s" +
                            "             where e.Company = c and s.Company = c" +
                            "             and e.EventTypeCd = 'CompanyContactEmailChanged' and ed.CompanyEvent = e" +
                            "             and s.MigrationStatus = 'MigrationComplete' and e.CreatorId = 'PayrollAPI' " +
                            "             and s.ModifiedDate < e.CreatedDate " +
                            "             and ed.EventDetailTypeCd in (:EVENT_DETAIL_LIST)" +
                            "             and c.IAMRealmId in (:REALM_LIST)";

            String[] paramNames = {"REALM_LIST", "EVENT_DETAIL_LIST"};
            Object[] paramValues = {realmIds, eventDetailTypeCodes};
            List<Object[]> results = Application.executeHQLQuery(companyHql, paramNames, paramValues);

            if (results.size() != 2*realmIds.size()) {
                log.error(logPrefix, "Error", eventTypeCode, ", errType=QueryResultsCountError, resultsCount=" + results.size() + ", realmIds=" + realmIdsLoggerStr);
                return MapUtils.EMPTY_MAP;
            }

            Map<String, CompanyData> companyDataMap = new HashMap<>();
            for (Object[] r : results) {
                String psid = (String) r[0];
                String realmId = (String) r[1];
                String notificationEmail = (String) r[2];
                String eventString = (String) r[3];
                EventDetailTypeCode eventTypeCode = (EventDetailTypeCode) r[4];

                CompanyData companyData = companyDataMap.get(realmId);

                if (ObjectUtils.isEmpty(companyData)) {
                    // not yet entered
                    CompanyData newCompanyData;
                    if (EventDetailTypeCode.OldStringValue.equals(eventTypeCode)) {
                        newCompanyData = new CompanyData(psid, realmId, notificationEmail, eventString, null);
                    } else if (EventDetailTypeCode.NewStringValue.equals(eventTypeCode)) {
                        newCompanyData = new CompanyData(psid, realmId, notificationEmail, null, eventString);
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
        private String notificationEmail;
        private String eventOldString;
        private String eventNewString;

        CompanyData(String psid, String realmId, String notificationEmail, String eventOldString, String eventNewString) {
            this.psid = psid;
            this.realmId = realmId;
            this.notificationEmail = notificationEmail;
            this.eventOldString = eventOldString;
            this.eventNewString = eventNewString;
        }
    }
}
