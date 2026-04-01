package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessageBuilder;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by charithah418 on 6/1/15.
 */
public abstract class JPMCReportBase {

    protected static final String OUTPUT_DIRECTORY;
    protected SpcfLogger logger;


    static {
        OUTPUT_DIRECTORY = BatchUtils.getConfigString("psp_batch_ftp_send_dir");
     }

    /**
     * This method will retrieve the list of Unique companies which has
     * active(any sub-status other than cancelled and terminated) DD service and
     * has Company Events with Events in pEventTypeCodes, and the events being
     * logged within the fromDate and toDate. The retrieved companies are used
     * to construct JPMCEventMessage and return the list of JPMCEventMessage
     *
     * @param pEventTypeCodes
     * @param fromDate
     * @param toDate
     * @return
     */
    protected List<JPMCEventMessage> getJPMCReportData(List<EventTypeCode> pEventTypeCodes, boolean ddActive, SpcfCalendar fromDate, SpcfCalendar toDate,boolean shouldExcludeMtlCompaniesFromAML) {
        List<JPMCEventMessage> jpmcEventMessages = new ArrayList<JPMCEventMessage>();
        DomainEntitySet<CompanyEvent> companyEvents;
        if (ddActive) {
            companyEvents = CompanyEvent.findCompanyEventsByEventType(pEventTypeCodes, fromDate, toDate);
        } else {
            List<ServiceCode> serviceCodes = new ArrayList<ServiceCode>();
            serviceCodes.add(ServiceCode.DirectDeposit);
            companyEvents = CompanyEvent.findCompanyEventsByEventTypeAndServiceCode(pEventTypeCodes, serviceCodes, fromDate, toDate);
        }
        List<String> companies = new ArrayList<String>();
        for (CompanyEvent companyEvent : companyEvents) {
            Company company = companyEvent.getCompany();
            if(isRiskAssementDoneBySMS(company,companyEvent)){
                logger.info("Ignoring the event as this belongs to SMS company "+company.getSourceCompanyId());
                continue;
            }
            if (shouldExcludeMtlCompaniesFromAML && isEventCreatedByMTLUpdate(companyEvent)  ) {
                logger.info("Ignoring the event as this was triggered by MTL data collection " + company.getSourceCompanyId());
                continue;
            }
            if (!companies.contains(company.getSourceCompanyId())
                    && ((ddActive && company.isCompanyOnService(ServiceCode.DirectDeposit))
                    || (!ddActive && !company.isCompanyOnService(ServiceCode.DirectDeposit)))) {
                companies.add(company.getSourceCompanyId());
                JPMCEventMessageBuilder jpmcEventMessageBuilder = getEventMessage(company);
                jpmcEventMessages.add(jpmcEventMessageBuilder.build());
            }
        }
        //Set record status based on event type   
        return jpmcEventMessages;
    }

    private boolean isEventCreatedByMTLUpdate( CompanyEvent companyEvent) {
        if("PayrollAPI".equalsIgnoreCase(companyEvent.getCreatorId())){
            logger.info("Excluding the SMS company in the PSP Risk file  Event Type Cd ="+companyEvent.getEventTypeCd().name()+" CreatorID ="+companyEvent.getCreatorId());
            return true;
        }
        return false;
    }

    protected JPMCEventMessageBuilder getEventMessage(Company company) {
        Address legalAddress = company.getLegalAddress();
        Contact primaryPrincipal = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
        String sicCode = "";

        if (company.getCompanyAdditionalInfo() != null && company.getCompanyAdditionalInfo().getIndustryType() != null) {
            sicCode = company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode();
        }
        JPMCEventMessageBuilder jpmcEventMessageBuilder = JPMCEventMessageBuilder.JPMCEventMessage();

        if (primaryPrincipal != null) {
            Calendar dateOfBirth = null;
            if (primaryPrincipal.getDateOfBirth() != null) {
                dateOfBirth = CalendarUtils.convertToCalendar(primaryPrincipal.getDateOfBirth());
            }
            jpmcEventMessageBuilder.withFirstName(primaryPrincipal.getFirstName())
                    .withLastName(primaryPrincipal.getLastName())
                    .withMiddleName(primaryPrincipal.getMiddleName())
                    .withSsn(primaryPrincipal.getSocialSecurityNumberPlainText())
                    .withEmail(primaryPrincipal.getEmail())
                    .withPhoneNumber(primaryPrincipal.getPhone())
                    .withDateOfBirth(dateOfBirth);
        }

        if (legalAddress != null) {
            jpmcEventMessageBuilder.withAddressLine1(legalAddress.getAddressLine1())
                    .withAddressLine2(legalAddress.getAddressLine2())
                    .withCity(legalAddress.getCity())
                    .withState(legalAddress.getState())
                    .withCountry(legalAddress.getCountry())
                    .withSourceCompanyId(company.getSourceCompanyId())
                    .withZipCode(legalAddress.getZipCode())
                    .withDba(company.getDbaName())
                    .withFedTaxId(company.getFedTaxId())
                    .withRealmId(company.getIAMRealmId())
                    .withLegalName(company.getLegalName())
                    .withIndustrySicCode(sicCode);
        }
        return jpmcEventMessageBuilder;
    }

    protected List<JPMCEventMessage> getOFACReportData(List<EventTypeCode> pEventTypeCodes, int subListIndex, SpcfCalendar fromDate, SpcfCalendar toDate) {
        List<JPMCEventMessage> jpmcEventMessages = new ArrayList<JPMCEventMessage>();
        DomainEntitySet<CompanyEvent> companyEvents, companyDDEvents;
        List<ServiceCode> serviceCodes = new ArrayList<ServiceCode>();
        serviceCodes.add(ServiceCode.DirectDeposit);
        companyEvents = CompanyEvent.findCompanyEventsByEventType(pEventTypeCodes.subList(0, subListIndex), fromDate, toDate);
        companyDDEvents = CompanyEvent.findCompanyEventsByEventTypeAndDDStatus(pEventTypeCodes.subList(subListIndex, pEventTypeCodes.size()), serviceCodes, fromDate, toDate);
        if (companyEvents != null) {
            companyEvents.addAll(companyDDEvents);
            logger.info("OFAC_Report_Total_Events = " + companyEvents.size());
        } else {
            companyEvents = companyDDEvents;
        }
        //sorting again since one list was ascending and other was descending
        companyEvents = companyEvents.sort(CompanyEvent.EventTimeStamp());
        for (CompanyEvent companyEvent : companyEvents) {
            //create jpmsMessage for each and every companyEvent
            Company company = companyEvent.getCompany();

            if(isRiskAssementDoneBySMS(company,companyEvent)){
                logger.info("Ignoring the event as this belongs to SMS company "+company.getSourceCompanyId());
                continue;
            }
            JPMCEventMessageBuilder jpmcEventMessageBuilder = getOFACEventMessage(company, companyEvent, null);
            if (jpmcEventMessageBuilder != null) {
                jpmcEventMessages.add(jpmcEventMessageBuilder.build());
            }
            //In case company has deactivated Direct Deposit service. delete events should be created for principal owners, employees and vendors too
            if (companyEvent.getEventTypeCd().equals(EventTypeCode.ServiceStatusChange)) {
                boolean directDepositActive = true;
                if(companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewServiceStatus).get(0).getValue().equals("Cancelled")||companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewServiceStatus).get(0).getValue().equals("Terminated")){
                    directDepositActive = false;
                }
                //For primaryPrincipal using PrimaryPrincipalNameChanged as intermediatary for Added and deleted events directdepositActive is symbolic for if it is added or deleted
                CompanyEvent primaryPrincipalEvent = new CompanyEvent();
                primaryPrincipalEvent.setEventTypeCd(EventTypeCode.PrimaryPrincipalNameChanged);
                CompanyEventDetail primaryPrincipalEventDetail = new CompanyEventDetail();
                primaryPrincipalEventDetail.setValue(String.valueOf(directDepositActive));
                jpmcEventMessageBuilder = getOFACEventMessage(company, primaryPrincipalEvent, primaryPrincipalEventDetail);
                if (jpmcEventMessageBuilder != null) {
                    jpmcEventMessages.add(jpmcEventMessageBuilder.build());
                }
                for (Employee employee : company.getEmployees()) {
                    CompanyEvent employeeEvent = new CompanyEvent();
                    employeeEvent.setCompany(company);
                    if (directDepositActive) {
                        employeeEvent.setEventTypeCd(EventTypeCode.EmployeeAdded);
                    } else {
                        employeeEvent.setEventTypeCd(EventTypeCode.EmployeeDeleted);
                    }
                    CompanyEventDetail employeeEventDetail = new CompanyEventDetail();
                    employeeEventDetail.setEventDetailTypeCd(EventDetailTypeCode.EmployeeId);
                    employeeEventDetail.setValue(employee.getId().toString());
                    jpmcEventMessageBuilder = getOFACEventMessage(company, employeeEvent, employeeEventDetail);
                    if (jpmcEventMessageBuilder != null) {
                        jpmcEventMessages.add(jpmcEventMessageBuilder.build());
                    }
                }
                for (Payee payee : company.getPayeeCollection()) {
                    CompanyEvent payeeEvent = new CompanyEvent();
                    payeeEvent.setCompany(company);
                    if (directDepositActive) {
                        payeeEvent.setEventTypeCd(EventTypeCode.PayeeAdded);
                    } else {
                        payeeEvent.setEventTypeCd(EventTypeCode.PayeeUpdated);
                    }
                    CompanyEventDetail payeeEventDetail = new CompanyEventDetail();
                    payeeEventDetail.setEventDetailTypeCd(EventDetailTypeCode.PayeeId);
                    payeeEventDetail.setValue(payee.getId().toString());
                    jpmcEventMessageBuilder = getOFACEventMessage(company, payeeEvent, payeeEventDetail);
                    if (jpmcEventMessageBuilder != null) {
                        jpmcEventMessages.add(jpmcEventMessageBuilder.build());
                    }
                }
            }
            //If OFXService is activated and company has principal owner one event should be created for that
            if (companyEvent.getEventTypeCd().equals(EventTypeCode.OFXServiceActivated)) {
                //For primaryPrincipal using PrimaryPrincipalNameChanged as intermediatary for Added and deleted events directdepositActive is symbolic for if it is added or deleted
                CompanyEvent primaryPrincipalEvent = new CompanyEvent();
                primaryPrincipalEvent.setEventTypeCd(EventTypeCode.PrimaryPrincipalNameChanged);
                CompanyEventDetail primaryPrincipalEventDetail = new CompanyEventDetail();
                primaryPrincipalEventDetail.setValue(Boolean.toString(true));
                jpmcEventMessageBuilder = getOFACEventMessage(company, primaryPrincipalEvent, primaryPrincipalEventDetail);
                if (jpmcEventMessageBuilder != null) {
                    jpmcEventMessages.add(jpmcEventMessageBuilder.build());
                }
            }
        }
        return jpmcEventMessages;
    }

    protected JPMCEventMessageBuilder getOFACEventMessage(Company company, CompanyEvent companyEvent, CompanyEventDetail eventDetail) {
        JPMCEventMessageBuilder jpmcEventMessageBuilder = JPMCEventMessageBuilder.JPMCEventMessage();
        String isCompanyDeactivated = null;
        if (companyEvent.getEventTypeCd().equals(EventTypeCode.PrimaryPrincipalNameChanged) || companyEvent.getEventTypeCd().equals(EventTypeCode.PrimaryPrincipalDOBChanged)) {
            Contact primaryPrincipal = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
            Calendar dateOfBirth = null;

            if (primaryPrincipal != null) {
                if (primaryPrincipal.getDateOfBirth() != null) {
                    dateOfBirth = CalendarUtils.convertToCalendar(primaryPrincipal.getDateOfBirth());
                }
                jpmcEventMessageBuilder.withFirstName(primaryPrincipal.getFirstName())
                        .withLastName(primaryPrincipal.getLastName())
                        .withMiddleName(primaryPrincipal.getMiddleName())
                        .withDateOfBirth(dateOfBirth)
                        .withSourceCompanyId(company.getSourceCompanyId())
                        .withUniqueId("PO" + company.getSourceCompanyId());

                if (primaryPrincipal.getMailingAddress() != null) {
                    jpmcEventMessageBuilder.withAddressLine1(primaryPrincipal.getMailingAddress().getAddressLine1())
                            .withAddressLine2(primaryPrincipal.getMailingAddress().getAddressLine2())
                            .withCity(primaryPrincipal.getMailingAddress().getCity())
                            .withState(primaryPrincipal.getMailingAddress().getState())
                            .withCountry(primaryPrincipal.getMailingAddress().getCountry())
                            .withZipCode(primaryPrincipal.getMailingAddress().getZipCode());
                }
            }
        } else if (companyEvent.getEventTypeCd().equals(EventTypeCode.ServiceStatusChange) || companyEvent.getEventTypeCd().equals(EventTypeCode.OFXServiceActivated)) {
            Address legalAddress = company.getLegalAddress();

            if (legalAddress != null) {
                jpmcEventMessageBuilder.withAddressLine1(legalAddress.getAddressLine1())
                        .withAddressLine2(legalAddress.getAddressLine2())
                        .withCity(legalAddress.getCity())
                        .withState(legalAddress.getState())
                        .withCountry(legalAddress.getCountry())
                        .withUniqueId(company.getSourceCompanyId())          //In case of company event uniqueId is PSID
                        .withZipCode(legalAddress.getZipCode())
                        .withLegalName(company.getLegalName());
            }
        }
        switch (companyEvent.getEventTypeCd()) {
            //check for Principal officer data
            case PrimaryPrincipalNameChanged:
            case PrimaryPrincipalDOBChanged:
                if (eventDetail != null && eventDetail.getValue().equals(Boolean.toString(true)))
                    jpmcEventMessageBuilder.withRecordStatus("A");
                else if (eventDetail != null && eventDetail.getValue().equals(Boolean.toString(false)))
                    jpmcEventMessageBuilder.withRecordStatus("D");
                else
                    jpmcEventMessageBuilder.withRecordStatus("U");
                break;
            //check for Company data
            case ServiceStatusChange:
                if(companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewServiceStatus).get(0).getValue().equals("Cancelled")||companyEvent.getCompanyEventDetails(EventDetailTypeCode.NewServiceStatus).get(0).getValue().equals("Terminated")){
                    jpmcEventMessageBuilder.withRecordStatus("D");
                } else {
                    jpmcEventMessageBuilder.withRecordStatus("A");
                }
                break;
            case OFXServiceActivated:
                jpmcEventMessageBuilder.withRecordStatus("A");
                break;
        }
        logger.info("jpmc_report_company_events = " + companyEvent.getEventTypeCd());
        return jpmcEventMessageBuilder;
    }

    protected void writeData(OutputStreamWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

    public abstract void createJPMCReport(SpcfCalendar fromDate, SpcfCalendar toDate) throws Exception;

    /**
     * Currently SMS team is doing risk assesment only at the time of onboarding
     * If there is any subsequent updates the SMS team is not doing Risk assesment
     * @param company
     * @param companyEvent
     * @return
     */
    private boolean isRiskAssementDoneBySMS(Company company,CompanyEvent companyEvent){

      //If the company is not onboarded to  money movement platform SMS then Risk assesment is not done by SMS
       if(!company.isMoneyMovementOnboardingEnabled()){
           return false;
       }

        //At this line company is onboarded to money movement platform SMS

        //If the creator of the event is PayrollAPI then this event is created by sync from SMS platform for which Risk assesment is done by SMS
        if (isEventSyncedFromAccountService(companyEvent))
        {
            return true;
        }

        //IF the EWS event is OFXServiceActivated and underwriting system is SMS then underwriting is performed by SMS no need of PSP underwriting
        //If the EWS event is not OFXServiceActivated then this might be update event where SMS is not doing under writing  and PSP underwriting is required
        if(isEwsEvent(companyEvent)){
            if (isRiskPermoformedBySMS(companyEvent)) {
                return true;
            }
        }

        logger.info("Including the SMS company in the PSP Risk file"+companyEvent.getEventTypeCd().name()+" "+companyEvent.getCreatorId()+" "+company.getSourceCompanyId());
        return false;
    }

    private boolean isEwsEvent(CompanyEvent companyEvent) {
        return companyEvent.getCreatorId().equals("EWSAdapter");
    }

    private boolean isRiskPermoformedBySMS(CompanyEvent companyEvent) {
        String ignoredEWSAdapterEvents = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_IDV_IGNORED_EWS_EVENTS");

        if(ignoredEWSAdapterEvents.contains(companyEvent.getEventTypeCd().name())){
            logger.info("Excluding the SMS company in the PSP Risk file  Event Type Cd ="+companyEvent.getEventTypeCd().name()+" CreatorID ="+companyEvent.getCreatorId());
            return true;
        }
        return false;
    }

    //If the creator of the event is PayrollAPI then this event is created by sync from SMS platform for which Risk assesment is done by SMS
    private boolean isEventSyncedFromAccountService(CompanyEvent companyEvent) {
        String ignoredUsersUpdates = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "ff_IDV_IGNORED_UPDATES_FROM_USERS");
        if(ignoredUsersUpdates.contains(companyEvent.getCreatorId())){
            logger.info("Excluding the SMS company in the PSP Risk file  Event Type Cd ="+companyEvent.getEventTypeCd().name()+" CreatorID ="+companyEvent.getCreatorId());
            return true;
        }
        return false;
    }

}
