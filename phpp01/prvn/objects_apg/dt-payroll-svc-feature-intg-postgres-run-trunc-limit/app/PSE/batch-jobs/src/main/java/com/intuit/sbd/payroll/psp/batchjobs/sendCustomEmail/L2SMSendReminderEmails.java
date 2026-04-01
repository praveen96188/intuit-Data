package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.inputFileModel.LegacyToSymphonyInputFileModel;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * @author vdammur1
 */
@Slf4j
public class L2SMSendReminderEmails implements ICustomEmailWorkFlowProcessor {

    private static final String STANDARD = "Standard";
    private static final String NONSTANDARD = "NonStandard";

    private static final String BASIC = "Basic";
    private static final String ENHANCED = "Enhanced";


    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private static final String PER_MONTH = "per month";
    private static final String PER_YEAR = "per year";



    @Override
    public void process(List inputList, String fileName) {
        log.info("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=process, Status=Start, Msg=ExecutingInputFile, FileName={}", fileName);
        try {
            PayrollServices.beginUnitOfWork();
            for(int i = 0; i < inputList.size(); i++) {
                LegacyToSymphonyInputFileModel input = (LegacyToSymphonyInputFileModel) inputList.get(i);
                checkForNullValues(input);
                createLegacySubscriptionMigrationEventFromInput(input, fileName, i);
            }
            PayrollServices.commitUnitOfWork();
            log.info("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=process, Status=Complete, Msg=ExecutingInputFile, FileName={}", fileName);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void createLegacySubscriptionMigrationEventFromInput(LegacyToSymphonyInputFileModel inputRow, String fileName, int idx) {
        try {
            String licenseNumber = inputRow.getLicenseNumber();
            String eoc = inputRow.getEntitlementOfferingCode();
            Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);
            Objects.requireNonNull(entitlement, "job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventFromInput, Status=Error, Msg=EntitlementIsNull, fileName=" + fileName + ", rowIndex=" + idx);
            DomainEntitySet<Company> companies = Company.searchCompaniesByLicenseNumberAndEoc(licenseNumber, eoc); // Search by license and EOC
            if(companies.size() == 0) {
                log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventFromInput, Status=Error, Msg=NoCompaniesFound, fileName={}, rowIndex={}", fileName, idx);
            }
            for(Company company: companies) {
                if(Objects.nonNull(company.getActivePrimaryEntitlementUnit())) {
                    log.info("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventFromInput, Msg=ActivePrimaryEntitlementUnitFound, fileName={}, EntitlementSeq={}", fileName, entitlement.getId());
                    createLegacySubscriptionMigrationEventForCompany(company, inputRow, entitlement.getId().toString(), fileName);
                    break;
                } else {
                    log.warn("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventFromInput, Status=Warn, Msg=ActivePrimaryEntitlementUnitNotFound, fileName={}, EntitlementSeq={}", fileName, entitlement.getId());
                }
            }
        } catch (Exception e) {
            log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventFromInput, Status=Error, Msg=ErrorProcessingInputFileRow(s), fileName={}, rowIndex={}", fileName, idx, e);
        }
    }

    public void createLegacySubscriptionMigrationEventForCompany(Company company, LegacyToSymphonyInputFileModel input, String entitlementId, String fileName) {
        try {
            String subscriptionType = getSubscriptionTypeStandardOrNonStandard(input.getEdition());
            String billingFrequency = getBillingFrequencyType(input.getBillingFrequencyType());

            String baseRate = input.getBaseRate();
            String emailAddress = input.getEmail();
            String licenseNumber = input.getLicenseNumber();
            String eoc = input.getEntitlementOfferingCode();

            long daysTillRenewal = Long.parseLong(input.getDaysTillRenewal() == null ? "0" : input.getDaysTillRenewal());
            daysTillRenewal = getDaysTillRenewalFromNextChargeDate(input.getNextChargeDate());
            String daysTillRenewalString = Long.toString(daysTillRenewal);
            if(daysTillRenewal == 7 || daysTillRenewal == 15 || daysTillRenewal == 30) {
                log.info("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventForCompany, fileName={}, EntitlementSeq={}, nextChargeDate={}, daysTillRenewal={}", fileName, entitlementId, input.getNextChargeDate(), daysTillRenewal);
                try {
                    CompanyEvent.createLegacySubscriptionMigrationEvent(company, subscriptionType, billingFrequency, daysTillRenewalString, baseRate, emailAddress, licenseNumber, eoc);
                } catch (Exception e) {
                    log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventForCompany, Status=Error, fileName={}, EntitlementSeq={}", fileName, entitlementId, e);
                }
            } else {
                log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventForCompany, Status=Error, Msg=InvalidDaysTillRenewalValue, fileName={}, EntitlementSeq={}, nextChargeDate={}, daysTillRenewal={}", fileName, entitlementId, input.getNextChargeDate(), daysTillRenewal);
            }
        } catch (Exception e) {
            log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=createLegacySubscriptionMigrationEventForCompany, Status=Error, fileName={}, EntitlementSeq={}", fileName, entitlementId, e);
        }

    }

    private void checkForNullValues(LegacyToSymphonyInputFileModel input) {
        String subscriptionType = getSubscriptionTypeStandardOrNonStandard(input.getEdition());
        String billingFrequency = getBillingFrequencyType(input.getBillingFrequencyType());
        String baseRate = input.getBaseRate();
        String emailAddress = input.getEmail();
        String licenseNumber = input.getLicenseNumber();
        String eoc = input.getEntitlementOfferingCode();

        Objects.requireNonNull(subscriptionType, "subscriptionType cannot be null");
        Objects.requireNonNull(billingFrequency, "billingFrequency cannot be null");
        Objects.requireNonNull(baseRate, "baseRate cannot be null");
        Objects.requireNonNull(emailAddress, "emailAddress cannot be null");
        Objects.requireNonNull(licenseNumber, "licenseNumber cannot be null");
        Objects.requireNonNull(eoc, "eoc cannot be null");
    }

    private String getSubscriptionTypeStandardOrNonStandard(String subscriptionType) {
        if(Objects.nonNull(subscriptionType)) {
            if(subscriptionType.equalsIgnoreCase(BASIC) || subscriptionType.equalsIgnoreCase(ENHANCED)) {
                return NONSTANDARD;
            } else if(subscriptionType.equalsIgnoreCase(STANDARD)) {
                return STANDARD;
            } else {
                log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=getSubscriptionTypeStandardOrNonStandard, Status=Error, Msg=InvalidSubscriptionType, Value={}", subscriptionType);
                throw new RuntimeException();
            }
        } else {
            log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=getSubscriptionTypeStandardOrNonStandard, Status=Error, Msg=SubscriptionTypeNull");
            throw new RuntimeException();
        }
    }

    private String getBillingFrequencyType(String billingFrequency) {
        if(Objects.nonNull(billingFrequency)) {
            if(billingFrequency.toLowerCase().contains(MONTH)) {
                return PER_MONTH;
            } else if(billingFrequency.toLowerCase().contains(YEAR))  {
                return PER_YEAR;
            } else {
                log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=getBillingFrequencyType, Status=Error, Msg=InvalidBillingFrequencyType, Value={}", billingFrequency);
                throw new RuntimeException();
            }
        } else {
            log.error("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=getBillingFrequencyType, Status=Error, Msg=BillingFrequencyTypeNull");
            throw new RuntimeException();
        }
    }

    private long getDaysTillRenewalFromNextChargeDate(String nextChargeDateString) {
        SpcfCalendar nextChargeDate = SpcfCalendar.parse("MM/dd/yyyy", nextChargeDateString);
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        int daysDifference = CalendarUtils.getDifferenceInDays(nextChargeDate, pspDate);
        log.info("job=SendCustomEmailsProcessor, Action=L2SMSendReminderEmails, Method=getDaysTillRenewalFromNextChargeDate, nextChargeDate={}, daysDifference={}", nextChargeDateString, daysDifference);
        return daysDifference;
    }
}
