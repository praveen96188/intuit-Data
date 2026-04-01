package com.intuit.sbd.payroll.psp.gateways.wc.manager;

import com.intuit.bp.wc.common.schema.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.WorkersCompServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCMessageDefinitions;
import com.intuit.sbd.payroll.psp.gateways.wc.util.DomainObjToWCObjConverter;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;

import java.util.*;

import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompPropEnum.PUSH_PAYROLL_COMPANIES_BATCH_SIZE;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompPropEnum.PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE;

/**
 * Author: Sriram Nutakki
 * Date created: 11/8/12
 */
public class WorkersCompGatewayManager {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompGatewayManager.class);

    public WorkersCompSubscriptions saveSubscriptionChanges(WorkersCompSubscriptions subscriptions) {
        logger.info("Saving workers comp subscription changes...");
        if (subscriptions != null && subscriptions.getWorkersCompSubscription() != null) {
            WorkersCompSubscriptions updatedEntries = new WorkersCompSubscriptions();
            MAIN: for (WorkersCompSubscription subscription : subscriptions.getWorkersCompSubscription()) {
                logger.info("Processing workers comp subscription for company with psid: " + subscription.getPSID());
                if (subscription.getPSID() != null && subscription.getStartDate() != null) {
                    boolean reactivate = false;
                    ProcessResult<CompanyService> processResult = null;
                    try {
                        Company company = Company.findCompany(subscription.getPSID(), SourceSystemCode.QBDT);
                        if (company == null) {
                            logger.warn("Cannot find company with PSID: " + subscription.getPSID() +
                                                 ".  Workers comp subscription cannot be processed for this company.");
                            continue MAIN;
                        }
                        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);

                        //handle edge case when workers comp is not enabled and we get a deactivation request
                        if (workersCompService == null && !subscription.isActive()){
                            logger.info("Workers comp service is not present for this company. Cannot deactivate.");
                            updatedEntries.getWorkersCompSubscription().add(subscription);
                            continue MAIN;
                        }


                        if (workersCompService != null && workersCompService.getStatusCd() != null) {
                            if (subscription.isActive()) {
                                if (ServiceSubStatusCode.ActiveCurrent == workersCompService.getStatusCd()) {
                                    logger.info("Workers comp service is already active. Not activating again.");
                                    updatedEntries.getWorkersCompSubscription().add(subscription);
                                    continue MAIN;
                                } else if (ServiceSubStatusCode.Cancelled == workersCompService.getStatusCd()) {
                                    reactivate = true;
                                }
                            } else {
                                if (ServiceSubStatusCode.Cancelled == workersCompService.getStatusCd()) {
                                    logger.info("Workers comp service is not active. Not deactivating again.");
                                    updatedEntries.getWorkersCompSubscription().add(subscription);
                                    continue MAIN;
                                }
                            }
                        }

                        PayrollServices.beginUnitOfWork();
                        if (subscription.isActive()) { // Activate
                            logger.debug("Processing activate subscription for psid:" + subscription.getPSID() +
                                                 " start date:" + subscription.getStartDate());
                            WorkersCompServiceInfoDTO serviceInfoDTO = new WorkersCompServiceInfoDTO();
                            serviceInfoDTO.setServiceStartDate(
                                    SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(subscription.getStartDate()));
                            if (reactivate) {
                                logger.info("Reactivating WorkersComp service... psid:" + subscription.getPSID());
                                ProcessResult<CompanyService> updateResult = PayrollServices.companyManager.updateService(
                                        SourceSystemCode.QBDT,
                                        subscription.getPSID(),
                                        serviceInfoDTO);
                                processResult = PayrollServices.companyManager.reactivateService(
                                        SourceSystemCode.QBDT,
                                        subscription.getPSID(),
                                        ServiceCode.WorkersComp);
                                processResult.merge(updateResult);
                            }
                            else {
                                logger.info("Adding WorkersComp service... psid:" + subscription.getPSID());
                                processResult = PayrollServices.companyManager.addService(
                                        SourceSystemCode.QBDT,
                                        subscription.getPSID(),
                                        serviceInfoDTO);
                            }
                        }
                        else { // Deactivate
                            logger.debug("Deactivating WorkersComp subscription... psid:" + subscription.getPSID());
                            processResult = PayrollServices.companyManager.deactivateService(
                                    SourceSystemCode.QBDT,
                                    subscription.getPSID(),
                                    ServiceCode.WorkersComp);
                        }
                        if (processResult != null && processResult.isSuccess()) {
                            PayrollServices.commitUnitOfWork();
                            updatedEntries.getWorkersCompSubscription().add(subscription);
                            logger.debug("Successfully activated/deactivated WorkersComp service. psid:" +
                                                 subscription.getPSID());
                        } else {
                            logger.info("Unable to activate/deactivate WorkersComp service. psid:" +
                                                subscription.getPSID() +
                                                "Failed with process result: " + processResult);
                            PayrollServices.rollbackUnitOfWork();
                        }
                    } catch (Exception ex) {
                        PayrollServices.rollbackUnitOfWork();
                        logger.error("Workers comp subscription activate/deactivate failed.", ex);
                    }
                }
            }
            return updatedEntries;
        } else {
            if(subscriptions == null) {
                logger.debug("Unable to save subscription changes, subscriptions is null");
            } else if(subscriptions.getWorkersCompSubscription() == null) {
                logger.debug("Unable to save subscription changes, workers comp subscription is null");
            }
        }
        return null;
    }

    public List<Set<SpcfUniqueId>> getCompaniesWithPendingPaychecks() {

        List<Set<SpcfUniqueId>> batches = new ArrayList<Set<SpcfUniqueId>>();

        // Find pending paycheck count by company
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        ArrayList<Object[]> results= null;
        if(isWCForSplitLimitCustomers()) {
            results =
                    Application.executeNamedQuery("findDistinctSplitLimitCompaniesWithWCPendingPaychecks", null, null);
            logger.info("getCompaniesWithPendingPaychecks() for splitLimit customers" );
        }
        if(isWCForAllCustomers()) {
            results =
                    Application.executeNamedQuery("findDistinctCompaniesWithWCPendingPaychecks", null, null);
            logger.info("getCompaniesWithPendingPaychecks() for all customers");
        }
        PayrollServices.rollbackUnitOfWork();
        Map<SpcfUniqueId, Number> pendingPaycheckCountByCompany = new HashMap<SpcfUniqueId, Number>();
        if (results != null) {
            for (Object[] result : results) {
                pendingPaycheckCountByCompany.put((SpcfUniqueId) result[0], (Number) result[1]);
            }
        }

        if (pendingPaycheckCountByCompany != null && pendingPaycheckCountByCompany.size() > 0) {
            batches = WCUtil.split(
                    pendingPaycheckCountByCompany,
                    PUSH_PAYROLL_COMPANIES_BATCH_SIZE.getValueAsInt(),
                    PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE.getValueAsInt());
        }

        return batches;
    }

    public static boolean isWCForAllCustomers()
    {
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_ALL_CUSTOMER, true);
    }

    public static boolean isWCForSplitLimitCustomers(){
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.WC_SPLITLIMIT_CUSTOMER, false);
    }

    public PayrollDTO getPendingPaychecks(Set<SpcfUniqueId> companyIds) {

        try {

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Map<Company, Set<Employee>> companyEmployees = new HashMap<Company, Set<Employee>>();
            Map<Employee, Set<WorkersCompPaycheckPendingState>> employeePendingPaychecks =
                    new HashMap<Employee, Set<WorkersCompPaycheckPendingState>>();

            List<WorkersCompPaycheckPendingState> pendingPaychecks =
                    Application.executeNamedQuery("findWCPendingPaychecksByCompanies",
                                                new String[]{"companyIds"},
                                                new Object[]{companyIds},
                                                0,
                                                PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE.getValueAsInt());

            for (WorkersCompPaycheckPendingState pendingCheck : pendingPaychecks) {
                WorkersCompPaycheck wcPaycheck = pendingCheck.getWorkersCompPaycheck();
                Paycheck paycheck = wcPaycheck.getPaycheck();
                Employee employee = paycheck.getSourceEmployee();
                Company company = paycheck.getCompany();

                Set<Employee> employees = companyEmployees.get(company);
                if (employees == null) {
                    employees = new HashSet<Employee>();
                    companyEmployees.put(company, employees);
                }
                employees.add(employee);

                Set<WorkersCompPaycheckPendingState> pendingChecks = employeePendingPaychecks.get(employee);
                if (pendingChecks == null) {
                    pendingChecks = new HashSet<WorkersCompPaycheckPendingState>();
                    employeePendingPaychecks.put(employee, pendingChecks);
                }
                pendingChecks.add(pendingCheck);
            }

            Payroll payroll = new Payroll();
            PayrollDTO dto = new PayrollDTO(payroll);

            for (Company company : companyEmployees.keySet()) {
                Business business = DomainObjToWCObjConverter.convert(company);
                Set<Employee> employees = companyEmployees.get(company);
                for (Employee employee : employees) {
                    com.intuit.bp.wc.common.schema.Employee wcEmployee = DomainObjToWCObjConverter.convert(employee);
                    Set<WorkersCompPaycheckPendingState> pendingChecks = employeePendingPaychecks.get(employee);
                    for (WorkersCompPaycheckPendingState pendingCheck : pendingChecks) {
                        dto.addIncludedPaycheck(company.getSourceCompanyId(), pendingCheck.getWorkersCompPaycheck());
                        Paycheck paycheck = pendingCheck.getWorkersCompPaycheck().getPaycheck();
                        com.intuit.sbd.payroll.psp.domain.Paystub paystub = loadPaystub(paycheck);

                        com.intuit.bp.wc.common.schema.Paycheck wcPaycheck = null;
                        if (paystub != null) {
                            wcPaycheck = DomainObjToWCObjConverter.convert(company, paystub);
                        }
                        else {
                            wcPaycheck = DomainObjToWCObjConverter.convert(paycheck);
                        }

                        switch(pendingCheck.getStateCd()) {
                            case PendingNew: wcPaycheck.setCheckStatus(PaycheckStatusType.NEW); break;
                            case PendingDelete: wcPaycheck.setCheckStatus(PaycheckStatusType.DELETE); break;
                            case PendingEdit: wcPaycheck.setCheckStatus(PaycheckStatusType.UPDATE); break;
                            default: throw new RuntimeException(
                                    "Invalid code for pending check status: " + pendingCheck.getStateCd());
                        }

                        // Set version
                        Long version = pendingCheck.getWorkersCompPaycheck().getPaycheckVersion();
                        wcPaycheck.setPaycheckVersion(version.intValue());

                        if (wcEmployee.getPaychecks() == null) {
                            wcEmployee.setPaychecks(new ArrayOfPaycheck());
                        }
                        wcEmployee.getPaychecks().getItem().add(wcPaycheck);

                        logger.info("WC Gateway :: Company=" + company.getSourceCompanyId() +
                                ", Employee=" + wcEmployee.getEmployeeID() + ", PayCheck_ID=" +
                                wcPaycheck.getPaycheckId() + ", Status=" + wcPaycheck.getCheckStatus());
                    }
                    if (business.getEmployees() == null) {
                        business.setEmployees(new ArrayOfEmployee());
                    }
                    business.getEmployees().getItem().add(wcEmployee);
                }
                if (payroll.getBusinesses() == null) {
                    payroll.setBusinesses(new ArrayOfBusiness());
                }
                payroll.getBusinesses().getItem().add(business);
            }

            return dto;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void markAsSent(List<WorkersCompPaycheck> paychecks) {
        if (paychecks != null && paychecks.size() > 0) {
            PayrollServices.beginUnitOfWork();
            for (WorkersCompPaycheck paycheck : paychecks) {
                Application.getHibernateSession().lock(paycheck, LockMode.NONE); //This will not hit the DB again
                paycheck.markAsSent();
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    public long getToken() {
        // get WC sync token
        long token;
        try {
            token = SystemParameter.findLongValue(SystemParameter.Code.WC_SYNC_TOKEN);
        } catch (Exception e) {
            token = createTokenSystemParameter(PSPDate.getPSPTime().toLocal());
        }

        return token;
    }

    public List<WCChangeEventDTO> getCompanyChangeEvents(long startTime, long endTime) {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        try {
            // get EINChanged and PSIDChanged events
            SpcfCalendar startTimeCal = SpcfCalendar.createInstance(startTime).toLocal();
            SpcfCalendar endTimeCal = SpcfCalendar.createInstance(endTime).toLocal();
            logger.info("getCompanyChangeEvents(): fetching company events from db between " + startTimeCal.toString() + " and " + endTimeCal.toString());
            Criterion<CompanyEvent> criteria = CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EINChanged).Or(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.NewPSIDCreatedForExistingCustomer))
                                               .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active))
                                               .And(CompanyEvent.EventTimeStamp().between(startTimeCal, endTimeCal));
            DomainEntitySet<CompanyEvent> companyChangeEvents = PayrollServices.entityFinder.find(CompanyEvent.class, criteria);
            if (companyChangeEvents != null) {
                logger.info("getCompanyChangeEvents(): fetched company events from db: " + companyChangeEvents.size());
            }
            return convertToDTOList(companyChangeEvents);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void updateToken(long endTime) {
        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.WC_SYNC_TOKEN, String.valueOf(endTime));
        PayrollServices.commitUnitOfWork();
    }


    private Paystub loadPaystub(Paycheck paycheck) {

        Paystub paystub = null;

        Expression<Paystub> query =
                new Query<Paystub>()
                        .Where(Paystub.Paycheck().equalTo(paycheck));

        ((Query<Paystub>)query).EagerLoad(
                Paystub.Paycheck(),
                Paystub.PstubPayItemSet()
        );

        List<Paystub> paystubs = Application.executeQuery(Paystub.class, query);
        if (paystubs != null && paystubs.size() > 0) {
            paystub = paystubs.get(0);
        }

        return paystub;
    }

    private String getCompanyPreviousFEIN(Company company) {
        String previousFEIN = null;
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(
                                CompanyEvent.Company().equalTo(company)
                                            .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EINChanged)));
        ((Query<CompanyEvent>)query).EagerLoad(CompanyEvent.CompanyEventDetailSet());
        List<CompanyEvent> events = Application.executeQuery(CompanyEvent.class, query);
        if (events != null && events.size() > 0) {
            // Sort in reverse chronological order
            Collections.sort(events, new Comparator<CompanyEvent>() {
                public int compare(CompanyEvent o1, CompanyEvent o2) {
                    return o2.getCreatedDate().compareTo(o1.getCreatedDate());
                }
            });

            // Get old FEIN
            HashMap<EventDetailTypeCode, String> eventDetails = events.get(0).getEventDetailInfo();
            previousFEIN = eventDetails.get(EventDetailTypeCode.OldStringValue);
        }
        return previousFEIN;
    }

    private void logPaycheckPaystubForDebug(Company company, Employee employee, Paystub paystub, Paycheck paycheck,
                                            PaycheckStatusType pcStatus, int paycheckVersion) {
        if (!Application.isProdEnvironment() && paystub != null) {
            Payroll payrollLog = new Payroll();
            Business businessLog = new Business();
            com.intuit.bp.wc.common.schema.Employee wcEmployeeLog = new com.intuit.bp.wc.common.schema.Employee();

            payrollLog.setBusinesses(new ArrayOfBusiness());
            businessLog.setEmployees(new ArrayOfEmployee());
            wcEmployeeLog.setPaychecks(new ArrayOfPaycheck());
            payrollLog.getBusinesses().getItem().add(businessLog);
            businessLog.getEmployees().getItem().add(wcEmployeeLog);
            wcEmployeeLog.getPaychecks().getItem().add(DomainObjToWCObjConverter.convert(company, paystub));
            wcEmployeeLog.getPaychecks().getItem().get(0).setCheckStatus(pcStatus);
            wcEmployeeLog.getPaychecks().getItem().get(0).setPaycheckVersion(paycheckVersion);
            String paystubXmlLog = WCUtil.toXML(payrollLog);

            wcEmployeeLog.getPaychecks().getItem().remove(0);
            wcEmployeeLog.getPaychecks().getItem().add(DomainObjToWCObjConverter.convert(paycheck));
            wcEmployeeLog.getPaychecks().getItem().get(0).setCheckStatus(pcStatus);
            wcEmployeeLog.getPaychecks().getItem().get(0).setPaycheckVersion(paycheckVersion);
            String paycheckXmlLog = WCUtil.toXML(payrollLog);

            StringBuffer buffer = new StringBuffer();
            buffer.append("\n##START: WC - Paycheck LOG --" +
                                  " Company Name: " + company.getLegalName() +
                                  ", Employee Name " + employee.getFirstName() + " " + employee.getLastName() +
                                  ", Paycheck Id " + paycheck.getSourcePaycheckId() + "\n");
            buffer.append("Paystub XML: " + paystubXmlLog + "\n");
            buffer.append("Paycheck XML: " + paycheckXmlLog + "\n");
            buffer.append("## END: W C - Paycheck LOG #####\n");
            logger.info(buffer.toString());
        }
    }

    private static Long createTokenSystemParameter(SpcfCalendar pSpcfCalendar) {
        PayrollServices.beginUnitOfWork();
        Long token = pSpcfCalendar.getTimeInMilliseconds();

        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setSystemParameterCd(SystemParameter.Code.WC_SYNC_TOKEN.toString());
        systemParameter.setSystemParameterDescription("Token used to sync data from PSP to WC");
        systemParameter.setSystemParameterOrg("PSP");
        systemParameter.setSystemParameterValue(token.toString());
        Application.save(systemParameter);
        PayrollServices.commitUnitOfWork();

        return token;
    }

    private List<WCChangeEventDTO> convertToDTOList(DomainEntitySet<CompanyEvent> events) {
        if (events == null)
            return null;
        List<WCChangeEventDTO> list = new ArrayList<WCChangeEventDTO>();
        for (int i = 0; i < events.size(); i++) {
            list.add(convertToDTO(events.get(i)));
        }
        return list;
    }

    private WCChangeEventDTO convertToDTO(CompanyEvent event) {
        WCChangeEventDTO eventDTO = new WCChangeEventDTO();
        eventDTO.setId(event.getId().toString());
        eventDTO.setObject(WCMessageDefinitions.ObjectType.COMPANY);
        eventDTO.setEventDateTime(event.getEventTimeStamp().toString());
        eventDTO.setOldValue(event.getCompanyEventDetailValue(EventDetailTypeCode.OldStringValue));
        eventDTO.setNewValue(event.getCompanyEventDetailValue(EventDetailTypeCode.NewStringValue));
        if (event.getEventTypeCd().equals(EventTypeCode.EINChanged)){
            eventDTO.setAttribute(WCMessageDefinitions.Attribute.EIN);
        } else if (event.getEventTypeCd().equals(EventTypeCode.NewPSIDCreatedForExistingCustomer)){
            eventDTO.setAttribute(WCMessageDefinitions.Attribute.PSID);
        }
        return eventDTO;
    }
}
