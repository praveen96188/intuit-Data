package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.EmailTransactionObserver;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageDefinition;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.SortableProperty;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;



/**
 * Hand-written business logic
 */
public class CompanyEvent extends BaseCompanyEvent {
    protected static String tokEmail;

    private static final String C05_NOC = "C05";
    private static final String SMS_MIGRATED_MSG = "Successfully migrated company to SMS.";
    private static final String SMS_MIGRATE_REVERTED_MSG = "Successfully reverted SMS Migration for company.";
    private static final String STANDARD = "Standard";
    private static final String NONSTANDARD = "NonStandard";
    private static SpcfLogger logger = SpcfLogManager.getLogger(CompanyEvent.class);
    static {
        tokEmail = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "tokEmailAddress");
    }

    protected static EventTypeCode[] includeEventsForAssisted = {
            EventTypeCode.AssistedPayrollConfirmation,
            EventTypeCode.AssistedFailedEnrollment, EventTypeCode.ChangeRedebitToWireExpected,
            EventTypeCode.CompanyBankAccountChange, EventTypeCode.DDReject,
            EventTypeCode.DDDebitReturn, EventTypeCode.FeeCreated,
            EventTypeCode.FeeRebilled, EventTypeCode.FeeRefunded,
            EventTypeCode.LastChanceNotify, EventTypeCode.ManualRedebitCreated,
            EventTypeCode.NonAchPaymentReceived, EventTypeCode.NonPrintChecks,
            EventTypeCode.NSF, EventTypeCode.NOC,
            EventTypeCode.PINUpdated, EventTypeCode.ReversalRequested,
            EventTypeCode.ReversalReturn, EventTypeCode.ReversalOK,
            EventTypeCode.ServiceKeyUpdated, EventTypeCode.ServiceStatusChange,
            EventTypeCode.SUIEoqDebitCreated, EventTypeCode.SUIEoqCreditCreated,
            EventTypeCode.SUIImmediateCreditCreated, EventTypeCode.SUIImmediateDebitCreated,
            EventTypeCode.CBAVerifyReturn, EventTypeCode.CompanyContactEmailChanged,
            EventTypeCode.PayrollCancelled, EventTypeCode.CreditReduction,
            EventTypeCode.PreOffload401kValidationAlert, EventTypeCode.PostOffload401kValidationAlert,
            EventTypeCode.TOKNotifiedOfCompanyFraud, EventTypeCode.DeletedPaycheckAlreadyOffloadedToTOK,
            EventTypeCode.VoidedPaycheckAlreadyOffloadedToTOK, EventTypeCode.VmpSignUpEmployeeEmail,
            EventTypeCode.VmpSignUpEmployerEmail, EventTypeCode.PaystubCreated, EventTypeCode.MonthlyFeeCreated,
            EventTypeCode.InvalidVendorEmail, EventTypeCode.ERLoanNOC,
            EventTypeCode.SUICreditsApplied, EventTypeCode.EmployeeBankAccountChange,
            EventTypeCode.PayeeBankAccountChange, EventTypeCode.PendingPaymentRefunded};

    // In addition to the ones above, DD4VA customers should receive the following.
    protected static EventTypeCode[] includeEventsForDD4VA = {
            EventTypeCode.CustomerSignedUp, EventTypeCode.BillPaymentReceived,
            EventTypeCode.WireExpected, EventTypeCode.BillPaymentOffloaded,
            EventTypeCode.PayrollCancelPending };

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyEvent() {
        super();
    }

    @Override
    public String toString() {
        return super.getEventTypeCd().name();
    }

    public boolean isActive() {
        return CompanyEventStatus.Active.equals(getStatusCd());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany) {
        return Application.find(CompanyEvent.class,
                                CompanyEvent.Company().equalTo(pCompany));
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, EventTypeCode pEventTypeCd) {

        return Application.find(CompanyEvent.class,
                                CompanyEvent.Company().equalTo(pCompany).And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCd)));
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsEagerLoad(
            Company pCompany, EventTypeCode pEventTypeCd) {

        Query<CompanyEvent> query = (Query)new Query<CompanyEvent>()
                .Where(CompanyEvent.Company().equalTo(pCompany)
                        .And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCd)))
                .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(CompanyEvent.Company()));

        return Application.find(CompanyEvent.class, query
                );
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, EventTypeCode pEventTypeCd,
            CompanyEventStatus pStatus, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        return findCompanyEvents(pCompany, pEventTypeCd, pStatus, pFromDate, pToDate, false);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, EventTypeCode pEventTypeCd,
            CompanyEventStatus pStatus, SpcfCalendar pFromDate, SpcfCalendar pToDate, boolean pEagerLoadDetails) {
        Criterion<CompanyEvent> where;

        if (pCompany != null) {
            where = CompanyEvent.Company().equalTo(pCompany);
        } else {
            throw new RuntimeException("Company cannot be null when searching for company events.");
        }

        if (pEventTypeCd != null) {
            where = where.And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCd));
        }

        if (pStatus != null) {
            where = where.And(CompanyEvent.StatusCd().equalTo(pStatus));
        }

        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .lessOrEqualThan(pToDate));
        }

        Expression<CompanyEvent> query;
        if (pEagerLoadDetails) {
            query = new Query<CompanyEvent>()
                    .Where(where)
                    .OrderBy(CompanyEvent.EventTimeStamp(),
                             CompanyEvent.EventTypeCd(), CompanyEvent.CreatedDate())
                    .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(CompanyEvent.Company()));
        } else {
            query = new Query<CompanyEvent>()
                    .Where(where)
                    .OrderBy(CompanyEvent.EventTimeStamp(),
                             CompanyEvent.EventTypeCd(), CompanyEvent.CreatedDate());
        }

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, EventTypeCode pEventTypeCd,
            CompanyEventStatus pStatus, Boolean pDescending) {
        Criterion<CompanyEvent> where = CompanyEvent.Company().equalTo(pCompany);

        if (pEventTypeCd != null) {
            where = where.And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCd));
        }

        if (pStatus != null) {
            where = where.And(CompanyEvent.StatusCd().equalTo(pStatus));
        }

        Expression<CompanyEvent> query;

        if (pDescending) {
            query = new Query<CompanyEvent>().Where(where)
                                             .OrderBy(CompanyEvent.CreatedDate()
                                                                  .Descending());
        } else {
            query = new Query<CompanyEvent>().Where(where)
                                             .OrderBy(CompanyEvent.CreatedDate());
        }

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsEagerLoadCompanyEventDetail(
            Company pCompany, EventTypeCode pEventTypeCd,
            CompanyEventStatus pStatus, Boolean pDescending) {
        Criterion<CompanyEvent> where = CompanyEvent.Company().equalTo(pCompany);

        if (pEventTypeCd != null) {
            where = where.And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCd));
        }

        if (pStatus != null) {
            where = where.And(CompanyEvent.StatusCd().equalTo(pStatus));
        }

        SortableProperty<DomainEntity, SpcfCalendar> sortableProperty;

        if (pDescending) {
            sortableProperty = CompanyEvent.CreatedDate().Descending();
        } else {
            sortableProperty = CompanyEvent.CreatedDate();
        }

        Expression<CompanyEvent> query = new Query<CompanyEvent>().Where(where)
            .OrderBy(sortableProperty)
            .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(pCompany));

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsByTypes(
            Company pCompany, EventTypeCode[] pEventTypeCds, String pCreatorId,
            SpcfCalendar pFromDate, SpcfCalendar pToDate, int max) {
        Criterion<CompanyEvent> where;

        if (pCompany != null) {
            where = CompanyEvent.Company().equalTo(pCompany);
        } else {
            throw new RuntimeException("Company cannot be null when searching for company events.");
        }

        if (pEventTypeCds != null) {
            where = where.And(CompanyEvent.EventTypeCd().in(pEventTypeCds));
        }

        if (pCreatorId != null) {
            where = where.And(CompanyEvent.CreatorId().equalTo(pCreatorId));
        }

        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .lessOrEqualThan(pToDate));
        }

        Expression<CompanyEvent> query = new Query<CompanyEvent>()
                .Where(where)
                .OrderBy(CompanyEvent.EventTimeStamp().Descending(),
                         CompanyEvent.EventTypeCd(), CompanyEvent.Id())
                .LimitResults(0, max);

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findCompanySystemEvents(
            Company pCompany, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pFromDate;

        if (pToDate != null) {
            paramValues[2] = pToDate;
        } else {
            paramValues[2] = PSPDate.getPSPTime();
        }

        return Application.findByNamedQuery("findCompanySystemEvents",
                                            paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyEvent> findCompanySystemEvents(
            Company pCompany) {
        String[] paramNames = new String[1];
        paramNames[0] = "company";

        Object[] paramValues = new Object[1];
        paramValues[0] = pCompany;

        return Application.findByNamedQuery("findSystemEventsByCompany",
                                            paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            SourceSystemCode pSourceSystemCd, SpcfCalendar pFromDate,
            SpcfCalendar pToDate) {
        String[] paramNames = new String[3];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "fromDate";
        paramNames[2] = "toDate";

        Object[] paramValues = new Object[3];
        paramValues[0] = pSourceSystemCd;
        paramValues[1] = pFromDate;

        if (pToDate != null) {
            paramValues[2] = pToDate;
        } else {
            paramValues[2] = CalendarUtils.getPSPDateFromDB();
        }

        return Application.findByNamedQuery("findSourceSystemEvents",
                                            paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, Long pFromToken) {
        String[] paramNames = new String[2];
        paramNames[0] = "company";
        paramNames[1] = "fromToken";

        Object[] paramValues = new Object[2];
        paramValues[0] = pCompany;
        paramValues[1] = pFromToken;

        return Application.findByNamedQuery("findCompanyEventsByToken",
                                            paramNames, paramValues);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventWithDetailsEagerLoaded(
            Company pCompany, EventTypeCode pEventTypeCode,
            EventDetailTypeCode pEventDetailTypeCode, String pEventDetailValue) {


        return Application.find(CompanyEvent.class, new Query<CompanyEvent>()
                .Where(CompanyEvent.Company().equalTo(pCompany)
                                   .And(CompanyEvent.EventTypeCd().equalTo(pEventTypeCode))
                                   .And(CompanyEvent.CompanyEventDetailSet()
                                                    .Exists(CompanyEventDetail.Company().equalTo(pCompany)
                                                                              .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                                                                              .And(CompanyEventDetail.Value().equalTo(pEventDetailValue)))))
                .OrderBy(CompanyEvent.EventTimeStamp(), CompanyEvent.CreatedDate())
                .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(pCompany)));
    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetails(
            Company pCompany, EventTypeCode pEventTypeCode,
            EventDetailTypeCode pEventDetailTypeCode, String pEventDetailValue) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                                         .And(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                                         .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                                         .And(CompanyEventDetail.Value().equalTo(pEventDetailValue))
                                         .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(pEventTypeCode)))
                .OrderBy(CompanyEventDetail.CompanyEvent().EventTimeStamp(), CompanyEventDetail.CompanyEvent().CreatedDate()));

    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetailsEagerLoadCompanyEventAndCompanyEventDetailSet(
            Company pCompany, EventTypeCode pEventTypeCode,
            EventDetailTypeCode pEventDetailTypeCode, String pEventDetailValue) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                        .And(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                        .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                        .And(CompanyEventDetail.Value().equalTo(pEventDetailValue))
                        .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(pEventTypeCode)))
                        .OrderBy(CompanyEventDetail.CompanyEvent().EventTimeStamp(), CompanyEventDetail.CompanyEvent().CreatedDate())
                        .EagerLoad(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                        .EagerLoad(CompanyEventDetail.CompanyEvent().CompanyEventDetailSet().Filter().Company().equalTo(pCompany)));

    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetailsEagerLoadCompanyEventAndCompanyEventEmailSet(
            Company pCompany, EventTypeCode pEventTypeCode,
            EventDetailTypeCode pEventDetailTypeCode, String pEventDetailValue) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                        .And(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                        .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                        .And(CompanyEventDetail.Value().equalTo(pEventDetailValue))
                        .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(pEventTypeCode)))
                        .OrderBy(CompanyEventDetail.CompanyEvent().EventTimeStamp(), CompanyEventDetail.CompanyEvent().CreatedDate())
                        .EagerLoad(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                        .EagerLoad(CompanyEventDetail.CompanyEvent().CompanyEventEmailSet().Filter().Company().equalTo(pCompany)));

    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetails(
            Company pCompany, EventTypeCode pEventTypeCode, EventDetailTypeCode pEventDetailTypeCode) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                                         .And(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                                         .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                                         .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(pEventTypeCode)))
                .OrderBy(CompanyEventDetail.CompanyEvent().EventTimeStamp(), CompanyEventDetail.CompanyEvent().CreatedDate()));

    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(
            Company pCompany, EventDetailTypeCode pEventDetailTypeCode,
            String pEventDetailValue) {

        return Application.find(CompanyEvent.class, new Query<CompanyEvent>()
                .Where(CompanyEvent.Company().equalTo(pCompany)
                                   .And(CompanyEvent.CompanyEventDetailSet()
                                                    .Exists(CompanyEventDetail.Company().equalTo(pCompany)
                                                                              .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                                                                              .And(CompanyEventDetail.Value().equalTo(pEventDetailValue)))))
                .OrderBy(CompanyEvent.EventTimeStamp(), CompanyEvent.CreatedDate()));
    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetailForEventDetailValue(
            Company pCompany,
            EventDetailTypeCode pResultEventDetailTypeCode,
            EventDetailTypeCode pSearchByEventDetailTypeCode, String pSearchByEventDetailValue) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                                         .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pResultEventDetailTypeCode))
                                         .And(CompanyEventDetail.Company().equalTo(pCompany))
                                         .And(CompanyEventDetail.CompanyEvent().CompanyEventDetailSet()
                                                                .Exists(CompanyEventDetail.Company().equalTo(pCompany)
                                                                                          .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pSearchByEventDetailTypeCode))
                                                                                          .And(CompanyEventDetail.Value().equalTo(pSearchByEventDetailValue)))))
                .OrderBy(CompanyEventDetail.CreatedDate()));
    }

    public static DomainEntitySet<CompanyEventDetail> findCompanyEventDetailForEventDetailValue(
            Company pCompany,
            EventTypeCode pEventTypeCode,
            EventDetailTypeCode pResultEventDetailTypeCode,
            EventDetailTypeCode pSearchByEventDetailTypeCode, String pSearchByEventDetailValue) {

        return Application.find(CompanyEventDetail.class, new Query<CompanyEventDetail>()
                .Where(CompanyEventDetail.Company().equalTo(pCompany)
                                         .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pResultEventDetailTypeCode))
                                         .And(CompanyEventDetail.Company().equalTo(pCompany))
                                         .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(pEventTypeCode))
                                         .And(CompanyEventDetail.CompanyEvent().CompanyEventDetailSet()
                                                                .Exists(CompanyEventDetail.Company().equalTo(pCompany)
                                                                                          .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pSearchByEventDetailTypeCode))
                                                                                          .And(CompanyEventDetail.Value().equalTo(pSearchByEventDetailValue)))))
                .OrderBy(CompanyEventDetail.CreatedDate()));
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsByEventTypeAndServiceCode(List<EventTypeCode> pEventTypeCodes, List<ServiceCode> pServiceCodes, SpcfCalendar pFromDate, SpcfCalendar pToDate){
        Criterion<CompanyEvent> where = CompanyEvent.EventTypeCd().in(pEventTypeCodes);
        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .lessOrEqualThan(pToDate));
        }

        if(pServiceCodes != null){
            DomainEntitySet<Service> services = Application.find(Service.class, new Query<Service>()
                    .Where(Service.ServiceCd().in(pServiceCodes)));

            List<String> serviceName = new ArrayList<String>();
            for(Service service: services){
                serviceName.add(service.getName());
            }

            where.And(CompanyEvent.CompanyEventDetailSet()
                                  .Exists(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ServiceCode)
                                                            .And(CompanyEventDetail.Value().in(serviceName))));
            
        }

        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .OrderBy(CompanyEvent.EventTimeStamp(),
                         CompanyEvent.EventTypeCd(), CompanyEvent.CreatedDate());

        return Application.find(CompanyEvent.class, query);
    }
    
     public static DomainEntitySet<CompanyEvent> findCompanyEventsByEventTypeAndDDStatus(List<EventTypeCode> pEventTypeCodes, List<ServiceCode> pServiceCodes, SpcfCalendar pFromDate, SpcfCalendar pToDate){
        Criterion<CompanyEvent> where = CompanyEvent.EventTypeCd().in(pEventTypeCodes);
        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .lessOrEqualThan(pToDate));
        }
        if(pServiceCodes != null){
            DomainEntitySet<Service> services = Application.find(Service.class, new Query<Service>()
                    .Where(Service.ServiceCd().in(pServiceCodes)));

            List<String> serviceName = new ArrayList<String>();
            for(Service service: services){
                serviceName.add(service.getName());
            }
            
            List<String> serviceStatus = new ArrayList<String>();
            serviceStatus.add("Terminated");
            serviceStatus.add("Cancelled");


            where = where.And(CompanyEvent.CompanyEventDetailSet()
                                  .Exists(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ServiceCode)
                                                            .And(CompanyEventDetail.Value().in(serviceName))));
            where = where.And(CompanyEvent.CompanyEventDetailSet()
                                  .Exists((CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewServiceStatus).And(CompanyEventDetail.Value().in(serviceStatus))).Or
                                          ((CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldServiceStatus).And(CompanyEventDetail.Value().in(serviceStatus))))));
        }
        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .OrderBy(CompanyEvent.EventTimeStamp().Descending());
        return Application.find(CompanyEvent.class, query);
    }
    

    public static DomainEntitySet<CompanyEvent> findCompanyEventsByEventType(List<EventTypeCode> pEventTypeCd, SpcfCalendar pFromDate, SpcfCalendar pToDate){
        Criterion<CompanyEvent> where = CompanyEvent.EventTypeCd().in(pEventTypeCd);

        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                                          .lessOrEqualThan(pToDate));
        }
        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .OrderBy(CompanyEvent.EventTimeStamp(),
                         CompanyEvent.EventTypeCd(), CompanyEvent.CreatedDate());

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsByEventTypeAndStatus(List<EventTypeCode> pEventTypeCd, CompanyEventStatus status, SpcfCalendar pFromDate, SpcfCalendar pToDate){
        Criterion<CompanyEvent> where = CompanyEvent.EventTypeCd().in(pEventTypeCd);

        if (status == null) {
            status= CompanyEventStatus.Active;
        }

        if (pFromDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                    .greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(CompanyEvent.EventTimeStamp()
                    .lessOrEqualThan(pToDate));
        }

        where = where.And(CompanyEvent.StatusCd().equalTo(status));
        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .OrderBy(CompanyEvent.EventTimeStamp(),
                        CompanyEvent.EventTypeCd(), CompanyEvent.CreatedDate())
                .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(CompanyEvent.Company()));

        return Application.find(CompanyEvent.class, query);
    }

    public static int getEventCountByType(Company pCompany,
                                          EventTypeCode pEventTypeCd) {
        return findCompanyEvents(pCompany, pEventTypeCd,
                                 CompanyEventStatus.Active, null, null).size();
    }

    public static int getCompanyStrikeCount(Company pCompany,
                                            SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        return findCompanyEvents(pCompany, EventTypeCode.Strike,
                                 CompanyEventStatus.Active, pFromDate, pToDate).size();
    }



    /*
    * this version only gets the events themselves (really used only for testing)
    */
    public static DomainEntitySet<CompanyEvent> findActiveCompanyFraudEvents(String pEinCid,
                                                                             FraudEventCategory pFraudEventCategory, SpcfCalendar pFromDate,
                                                                             SpcfCalendar pToDate, SpcfMoney pPayrollNetAmount, Collection<EventTypeCode> eventTypeCodes) {

        DomainEntitySet<CompanyEvent> companyEvents = new DomainEntitySet<CompanyEvent>();
        for (FraudEvent fraudEvent : FraudEvent.findActiveCompanyFraudEventsQuery(pEinCid, null, pFraudEventCategory,
                                                                                  pFromDate, pToDate, pPayrollNetAmount,
                                                                                  eventTypeCodes, false)) {
            companyEvents.add(fraudEvent.getCompanyEvent());
        }
        return companyEvents;
    }




    public static DomainEntitySet<CompanyEvent> findRecentCompanyEvents(
            Company pCompany, int max) {
        Expression<CompanyEvent> query = new Query<CompanyEvent>().Where(CompanyEvent.Company()
                                                                                     .equalTo(pCompany))
                                                                  .OrderBy(CompanyEvent.EventTimeStamp()
                                                                                       .Descending())
                                                                  .LimitResults(0,
                                                                                max);

        return Application.find(CompanyEvent.class, query);
    }

    public static DomainEntitySet<CompanyEvent> findInvalidEmployeeInformationEvent(Company pCompany,
                                                                                    String pSourceEmployeeId,
                                                                                    EventTypeCode pEventTypeCode) {
        String[] paramNames = new String[7];
        paramNames[0] = "company";
        paramNames[1] = "eventTypeCode";
        paramNames[2] = "companyEventStatus";
        paramNames[3] = "eventDetailTypeCode1";
        paramNames[4] = "value1";
        paramNames[5] = "eventDetailTypeCode2";
        paramNames[6] = "value2";

        Object[] paramValues = new Object[7];
        paramValues[0] = pCompany;
        paramValues[1] = EventTypeCode.InvalidEmployeeInformation;
        paramValues[2] = CompanyEventStatus.Active;
        paramValues[3] = EventDetailTypeCode.MessageLevel;
        paramValues[4] = MessageInfo.MessageLevel.WARNING.name();
        paramValues[5] = EventDetailTypeCode.SourceEmployeeId;
        paramValues[6] = pSourceEmployeeId;

        return Application.findByNamedQuery("findInvalidEmployeeInformationEvent", paramNames, paramValues);
    }

    public static CompanyEvent findUnreversedThresholdExceededEventForPayroll(Company pCompany, PaymentTemplate pPaymentTemplate, PayrollRun pPayrollRun) {
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, pPaymentTemplate.getPaymentTemplateCd());
        for (CompanyEvent companyEvent : companyEvents) {

            String thresholdStart = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodStartDate);
            String thresholdEnd = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodEndDate);

            if(thresholdStart != null && thresholdEnd != null) {
                SpcfCalendar thresholdStartDate = SpcfCalendar.parse("d", thresholdStart);
                SpcfCalendar thresholdEndDate = SpcfCalendar.parse("d", thresholdEnd);
                SpcfCalendar paycheckDate = pPayrollRun.getPaycheckDate().copy();
                CalendarUtils.clearTime(paycheckDate);
                if(paycheckDate.between(thresholdStartDate, thresholdEndDate) && companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdReversed) == null) {
                    return companyEvent;
                }
            }
        }
        return null;
    }

    public static CompanyEvent findUnreversedThresholdExceededEventWithStartAndEndDates(Company pCompany, PaymentTemplate pPaymentTemplate, String pStartDate, String pEndDate) {
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, pPaymentTemplate.getPaymentTemplateCd());
        for (CompanyEvent companyEvent : companyEvents) {

            String thresholdPeriodStartDate = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodStartDate);
            String thresholdPeriodEndDate = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdPeriodEndDate);
            if( thresholdPeriodStartDate != null && thresholdPeriodEndDate != null && thresholdPeriodStartDate.equals(pStartDate)
                    && thresholdPeriodEndDate.equals(pEndDate)
                    && companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdReversed) == null) { // Find unreversed event
                return companyEvent;
            }
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Generic method to add a company event. It Creates the base event object and
     * sets the base properties
     *
     */
    public static CompanyEvent createCompanyEvent(Company pCompany,
                                                  EventTypeCode pEventTypeCode) {
        CompanyEvent event = new CompanyEvent();

        // set base event properties
        event.setEventTypeCd(pEventTypeCode);
        event.setCompany(pCompany);
        event.setEventTimeStamp(PSPDate.getPSPTime());
        event.setStatusCd(CompanyEventStatus.Active);
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        // If this is an event that we need to update a token ,
        // update Company Current Token and set it as the event token
        if (isInterestingEvent(pCompany.getSourceSystemCd(), pEventTypeCode)) {
            if(pCompany.getSourceSystemCd() != SourceSystemCode.QBDT) {
                pCompany.setCurrentToken(pCompany.getCurrentToken() + 1);
                pCompany = Application.save(pCompany);
            }
            event.setCompany(pCompany);
            event.setEventToken(pCompany.getCurrentToken());
        }

        Application.save(event);

        if (isInterestingEvent(SourceSystemCode.AS400, pEventTypeCode) && pCompany.hasService(ServiceCode.Tax)) {
            EventAs400Sync as400Sync = new EventAs400Sync();
            as400Sync.setCompany(pCompany);
            as400Sync.setCompanyEvent(event);
            as400Sync.setStatusCd(SyncStatus.Pending);
            as400Sync.setRetryCount(0);
            Application.save(as400Sync);
        }

        return event;
    }

    public static CompanyEvent createRequestProcessingFlagChangedEvent(Company pCompany, boolean pNewValue, boolean pOldValue) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.RequestProcessingFlagChanged);
        event.addCompanyEventDetail(EventDetailTypeCode.GenericEventDetail, "Old Value: " + pOldValue + " New Value: " + pNewValue);
        return Application.save(event);
    }

    public static CompanyEvent createAllowTransmissionChangedEvent(Company pCompany, boolean pNewValue, boolean pOldValue) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.AllowTransmissionsFlagChanged);
        event.addCompanyEventDetail(EventDetailTypeCode.GenericEventDetail, "Old Value: " + pOldValue + " New Value: " + pNewValue);
        return Application.save(event);
    }

    public static CompanyEvent createBalanceFileReceivedEvent(Company pCompany, CompanyService pCompanyService) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.BalanceFileReceived);

        event.addCompanyEventDetail(EventDetailTypeCode.CompanyServiceId, pCompanyService.getId().toString());

        return Application.save(event);
    }

    public static CompanyEvent createZeroPayrollReceivedEvent(Company pCompany, String pSourceTransmissionId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ZeroPayrollReceived);

        event.addCompanyEventDetail(EventDetailTypeCode.TransmissionId, pSourceTransmissionId);

        return Application.save(event);
    }

    public static CompanyEvent createACHEnrollmentStatusChangedEvent(Company pCompany, ACHEnrollment pACHEnrollment, ACHEnrollmentStatus pOldEnrPACHEnrollmentStatus) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ACHEnrollmentStatusChanged);

        event.addCompanyEventDetail(EventDetailTypeCode.ACHEnrollmentId, pACHEnrollment.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pACHEnrollment.getStatus().toString());
        if(pOldEnrPACHEnrollmentStatus != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, pOldEnrPACHEnrollmentStatus.toString());
        }

        return Application.save(event);
    }


    public static CompanyEvent createFraudSignUpEvent(Company pCompany, EventTypeCode pEventTypeCode, String pDetails) {
        CompanyEvent event = CompanyEvent.createCompanyEvent(pCompany, pEventTypeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pDetails);
        event.addCompanyEventDetail(EventDetailTypeCode.FraudEventCategory, FraudEventCategory.SignUp.toString());
        Application.save(event);

        FraudEvent fraudEvent = new FraudEvent(pCompany, event);
        Application.save(fraudEvent);

        return event;
    }

    public static CompanyEvent createSubscriptionEndDateChangedEvent(Company pCompany, SpcfCalendar pOldValue, SpcfCalendar pNewValue) {
        CompanyEvent event = CompanyEvent.createCompanyEvent(pCompany, EventTypeCode.SubscriptionEndDateChanged);

        String oldValue = pOldValue == null ? "" : pOldValue.toString();
        String newValue = pNewValue == null ? "" : pNewValue.toString();

        event.addCompanyEventDetail(EventDetailTypeCode.OldDate, oldValue);
        event.addCompanyEventDetail(EventDetailTypeCode.NewDate, newValue);
        Application.save(event);

        return event;
    }

    public static CompanyEvent createFraudPayrollEvent(Company pCompany, EventTypeCode pEventTypeCode, PayrollRun pPayrollRun, String pDetails) {
        return createFraudPayrollEvent(pCompany, pEventTypeCode, pPayrollRun, pDetails, null, null);
    }

    public static CompanyEvent createFraudPayrollEvent(Company pCompany, EventTypeCode pEventTypeCode, PayrollRun pPayrollRun, String pDetails, Employee pEmployee) {
        return createFraudPayrollEvent(pCompany, pEventTypeCode, pPayrollRun, pDetails, pEmployee, null);
    }

    public static CompanyEvent createFraudPayrollEvent(Company pCompany, EventTypeCode pEventTypeCode, PayrollRun pPayrollRun, String pDetails, Payee pPayee) {
        return createFraudPayrollEvent(pCompany, pEventTypeCode, pPayrollRun, pDetails, null, pPayee);
    }

    private static CompanyEvent createFraudPayrollEvent(Company pCompany, EventTypeCode pEventTypeCode, PayrollRun pPayrollRun, String pDetails, Employee pEmployee, Payee pPayee) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRun.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pDetails);
        event.addCompanyEventDetail(EventDetailTypeCode.FraudEventCategory, FraudEventCategory.Payroll.toString());

        if (pEmployee != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, pEmployee.getId().toString());
        }
        if (pPayee != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.PayeeId, pPayee.getId().toString());
        }

        Application.save(event);
        Application.save(new FraudEvent(pCompany, event, pPayrollRun, pEmployee));

        return event;
    }

    public static CompanyEvent createQuickBooksInfoChangedEvent(Company pCompany, ArrayList<String> pDetails, ArrayList<String> pOldValues, ArrayList<String> pNewValues) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.QuickBooksInfoChanged);

        // set event details
        int i = 0;
        while (i < pDetails.size()) {
            event.addCompanyEventDetail(EventDetailTypeCode.Details, pDetails.get(i));
            event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, pOldValues.get(i));
            event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pNewValues.get(i));
            i++;
        }

        return Application.save(event);
    }

    public static CompanyEvent createQuickBooksFileIDChangedEvent(Company pCompany, String pOldValue, String pNewValue) {
        return createCompanyInfoChangeEvent(pCompany, pOldValue, pNewValue, EventTypeCode.FileIdChanged);
    }

    public static CompanyEvent createCBAStatusChangeEvent(
            CompanyBankAccount pCompanyBankAccount,
            BankAccountStatus pOldBankAccountStatus,
            BankAccountStatus pNewBankAccountStatus, SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompanyBankAccount.getCompany(),
                                                EventTypeCode.CompanyBankAccountStatusChange);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.OldBAStatus,
                                    EnumUtils.getReadableName(pOldBankAccountStatus));
        event.addCompanyEventDetail(EventDetailTypeCode.NewBAStatus,
                                    EnumUtils.getReadableName(pNewBankAccountStatus));
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId,
                                    pCompanyBankAccount.getId().toString());

        return Application.save(event);
    }

    public static CompanyEvent createServiceStatusChangeEvent(
            final Company pCompany,
            final Collection<ServiceSubStatusCode> pOldOnHoldReasonCodes,
            final Collection<ServiceSubStatusCode> pNewOnHoldReasonCodes,
            final SpcfCalendar pEffectiveDate) {


        //If the thread local is set and it is false then we no need to create event
        if(!Objects.isNull(ThreadLocalManager.isHoldEventCreationRequired()) && !ThreadLocalManager.isHoldEventCreationRequired())
        {
            return null;
        }
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ServiceStatusChange);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        //if we're adding/removing an on hold reason, we want want to make the status depend on a service that it applies to.
        //Otherwise can use the default "company" status which will be active if any are active.
        ServiceSubStatusCode serviceSubStatusCode = null;
        Set<ServiceSubStatusCode> onHoldReasonCodes = new HashSet<ServiceSubStatusCode>();
        onHoldReasonCodes.addAll(pOldOnHoldReasonCodes);
        onHoldReasonCodes.addAll(pNewOnHoldReasonCodes);
        if (!onHoldReasonCodes.isEmpty()) {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCodes.iterator().next());
            Set<Service> serviceSet = serviceSubStatus.getServiceSet();
            DomainEntitySet<CompanyService> companyServices = pCompany.getCompanyServiceCollection().find(CompanyService.Service().in(serviceSet.toArray(new Service[serviceSet.size()])));
            if (companyServices.isNotEmpty()) {
                serviceSubStatusCode = companyServices.getFirst().getStatusCd();
            }
        } else {
            serviceSubStatusCode = pCompany.getCompanyStatus();
        }

        //this is really weird--we're just setting them both to the same thing all the time, but apparently
        //the UI is handling it
        if (serviceSubStatusCode != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldServiceStatus,
                                        EnumUtils.getReadableName(serviceSubStatusCode));
            event.addCompanyEventDetail(EventDetailTypeCode.NewServiceStatus,
                                        EnumUtils.getReadableName(serviceSubStatusCode));
        }

        for (ServiceSubStatusCode oldOnHoldReasonCd : pOldOnHoldReasonCodes) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldOnHoldReason,
                                        EnumUtils.getReadableName(oldOnHoldReasonCd));
            //If AMLHold is removed or added , email should be trigerred
            if (ServiceSubStatusCode.AMLHold.equals(oldOnHoldReasonCd) && isMailNeeded(pCompany, EventTypeCode.ServiceStatusChange)) {
                queueEmail(event, pCompany.getSourceSystemCd());
            }
        }

        for (ServiceSubStatusCode newOnHoldReasonCd : pNewOnHoldReasonCodes) {
            event.addCompanyEventDetail(EventDetailTypeCode.NewOnHoldReason,
                                        EnumUtils.getReadableName(newOnHoldReasonCd));
            //If AMLHold is removed or added , email should be trigerred
            if (ServiceSubStatusCode.AMLHold.equals(newOnHoldReasonCd) && isMailNeeded(pCompany, EventTypeCode.ServiceStatusChange)) {
                queueEmail(event, pCompany.getSourceSystemCd());
            }
        }
        return Application.save(event);
    }

    public static CompanyEvent createOfferRemovedEvent(Company pCompany,
                                                       String pOffer) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.OfferRemoved);

        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, pOffer);

        return Application.save(event);
    }

    public static CompanyEvent createOfferClaimedEvent(Company pCompany,
                                                       String pOffer) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.OfferClaimed);

        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pOffer);

        return Application.save(event);
    }

    static public CompanyEvent createEftpsEnrollmentStatusChangeEvent(EftpsEnrollment pEnrollment,
                                                                      EftpsEnrollmentDetail pEftpsEnrollmentDetail,
                                                                      EftpsEnrollmentStatus pOldStatus) {
        Company company = pEnrollment.getCompanyAgency().getCompany();
        CompanyEvent event = createCompanyEvent(company, EventTypeCode.EnrollmentStatusChanged);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, pEnrollment.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.EnrollmentType, "EftpsEnrollment");
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, "IRS");
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pEnrollment.getStatusCd().toString());

        String oldStatus = (pOldStatus == null) ? "<none>" : pOldStatus.toString();
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, oldStatus);

        if (EftpsEnrollmentStatus.Rejected.equals(pEnrollment.getStatusCd())) {
            // create reject email event
            CompanyEvent enrolmentRejectEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.AssistedFailedEnrollment);
            enrolmentRejectEvent.addCompanyEventDetail(EventDetailTypeCode.Timestamp,
                                                       PSPDate.getPSPTime().toString());

            if (pEftpsEnrollmentDetail != null) {
                String rejectReason = String.format("%s (%s)", pEftpsEnrollmentDetail.getRejectReason(), pEftpsEnrollmentDetail.getRejectCd());
                event.addCompanyEventDetail(EventDetailTypeCode.Details, rejectReason);
                enrolmentRejectEvent.addCompanyEventDetail(EventDetailTypeCode.Description,
                                                           rejectReason);
            }

        }

        if (pEnrollment.getSecondary() && pEftpsEnrollmentDetail != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.NoteText, "Manual Enrollment: " + pEftpsEnrollmentDetail.getFedTaxId() + "/" + pEftpsEnrollmentDetail.getLegalName() + "/" + pEftpsEnrollmentDetail.getLegalZip());
        }

        return Application.save(event);
    }

    static public CompanyEvent createRAFEnrollmentStatusChangeEvent(RAFEnrollment pEnrollment,
                                                                    RAFEnrollmentStatus pOldStatus) {
        Company company = pEnrollment.getCompanyAgency().getCompany();
        CompanyEvent event = createCompanyEvent(company, EventTypeCode.EnrollmentStatusChanged);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, pEnrollment.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.EnrollmentType, "RAFEnrollment");
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, "IRS");
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pEnrollment.getStatus().toString());

        String oldStatus = (pOldStatus == null) ? "<none>" : pOldStatus.toString();
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, oldStatus);

        if (RAFEnrollmentStatus.Rejected.equals(pEnrollment.getStatus()) && pEnrollment.getStatusReason() != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.Details, pEnrollment.getStatusReason());
        }

        return Application.save(event);
    }

    static public CompanyEvent createTaxPaymentStatusChangeEvent(EftpsPaymentDetail pPaymentDetail,
                                                                 TaxPaymentStatus pOldStatus) {
        MoneyMovementTransaction mmt = pPaymentDetail.getMoneyMovementTransaction();
        CompanyEvent event = createCompanyEvent(mmt.getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String detail;

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, pPaymentDetail.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.MoneyMovementTransactionId, mmt.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, "IRS");
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyTIN, pPaymentDetail.getFedTaxId());
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentMethod, mmt.getMoneyMovementPaymentMethodString());
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pPaymentDetail.getPaymentDetails());
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pPaymentDetail.getStatusCd().toString());

        detail = (pOldStatus == null) ? "<none>" : pOldStatus.toString();
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPaymentInitiationDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentInitiationDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPaymentDueDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentDueDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPeriodEndDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentPeriodEndDate, detail);

        switch (pPaymentDetail.getStatusCd()) {
            case AcknowledgedByAgency:
                event.addCompanyEventDetail(EventDetailTypeCode.PaymentAcknowledgeNumber, pPaymentDetail.getAgencyPaymentId());
                event.addCompanyEventDetail(EventDetailTypeCode.PaymentEFTNumber, pPaymentDetail.getEftTransactionId());
                break;

            case RejectedByAgency:
                detail = String.format("%s (%s)", pPaymentDetail.getReason(), pPaymentDetail.getRejectCd());
                event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription, detail);
                break;

            case ReturnedTaxPaid:
            case ReturnedTaxNotPaid:
                detail = String.format("%s (%s)", pPaymentDetail.getReason(), pPaymentDetail.getReturnCd().toString());
                event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription, detail);
                break;
        }

        return Application.save(event);
    }

    static public CompanyEvent createTaxPaymentStatusChangeEvent(EdiPaymentDetail pPaymentDetail,
                                                                 TaxPaymentStatus pOldStatus) {
        MoneyMovementTransaction mmt = pPaymentDetail.getMoneyMovementTransaction();
        CompanyEvent event = createCompanyEvent(mmt.getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String detail;

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, pPaymentDetail.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.MoneyMovementTransactionId, mmt.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, mmt.getPaymentTemplate().getPaymentTemplateCd());
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyTIN, pPaymentDetail.getFedTaxId());
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentMethod, mmt.getMoneyMovementPaymentMethodString());
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pPaymentDetail.getPaymentDetails());
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pPaymentDetail.getStatusCd().toString());

        detail = (pOldStatus == null) ? "<none>" : pOldStatus.toString();
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPaymentInitiationDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentInitiationDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPaymentDueDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentDueDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pPaymentDetail.getPeriodEndDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentPeriodEndDate, detail);

        switch (pPaymentDetail.getStatusCd()) {
            case AcknowledgedByAgency:
            case ReturnedTaxPaid:
            case ReturnedTaxNotPaid:
                detail = String.format("%s (%s)", pPaymentDetail.getErrorMessage(), pPaymentDetail.getErrorCd());
                event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription, detail);
                break;
        }

        return Application.save(event);
    }

    static public CompanyEvent createTaxPaymentStatusChangeEvent(MoneyMovementTransaction pMoneyMovementTransaction,
                                                                 TaxPaymentStatus pOldStatus, String pDetails) {
        CompanyEvent event = createCompanyEvent(pMoneyMovementTransaction.getCompany(), EventTypeCode.TaxPaymentStatusChanged);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String detail;

        event.addCompanyEventDetail(EventDetailTypeCode.MoneyMovementTransactionId, pMoneyMovementTransaction.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, pMoneyMovementTransaction.getAgencyTaxpayerId());
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentMethod, pMoneyMovementTransaction.getMoneyMovementPaymentMethodString());
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pMoneyMovementTransaction.getTaxPaymentStatus().toString());

        detail = (pOldStatus == null) ? "<none>" : pOldStatus.toString();
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pMoneyMovementTransaction.getInitiationDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentInitiationDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pMoneyMovementTransaction.getDueDate()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentDueDate, detail);

        detail = sdf.format(CalendarUtils.convertToDate(pMoneyMovementTransaction.getPaymentPeriodEnd()));
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentPeriodEndDate, detail);

        event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription, pDetails);

        String detailMessage = String.format("The tax payment status for %s due on %s via %s has changed to %s",
                                             pMoneyMovementTransaction.getPaymentTemplate().getPaymentTemplateAbbrev(),
                                             pMoneyMovementTransaction.getDueDate().format("MM/dd/yyyy"),
                                             pMoneyMovementTransaction.getMoneyMovementPaymentMethod(),
                                             pMoneyMovementTransaction.getTaxPaymentStatus());
        event.addGenericEventDetail("PaymentRejected", detailMessage);

        return Application.save(event);
    }

    static public CompanyEvent createTaxRateChangeEvent(CompanyLawRate pCompanyLawRate, Double oldRate, Double newRate) {
        CompanyAgency ca = pCompanyLawRate.getCompanyLaw().getCompanyAgency();

        String detailMessage = String.format("The company's rate for %s/%s (%d Q%d) has changed from %s to %s.",
                                             ca.getAgency().getAgencyId(),
                                             pCompanyLawRate.getCompanyLaw().getLaw().getLawAbbrev(),
                                             pCompanyLawRate.getEffectiveDate().getYear(),
                                             CalendarUtils.getQuarterAsInt(pCompanyLawRate.getEffectiveDate()),
                                             formatPercentage(oldRate),
                                             formatPercentage(newRate));

        CompanyEvent event = CompanyEvent.createCompanyEventAndDetail(ca.getCompany(), EventTypeCode.CompanyLawUpdated, EventDetailTypeCode.Details, detailMessage);
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyAgency, ca.getAgency().getAgencyId());
        event.addCompanyEventDetail(EventDetailTypeCode.Law, pCompanyLawRate.getCompanyLaw().getLaw().getLawId());
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,  formatPercentage(oldRate));

        return Application.save(event);
    }

    private static String formatPercentage(Double value) {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(5);
        format.setMinimumIntegerDigits(1);      // Include leading zero.
        format.setRoundingMode(RoundingMode.UP);
        return format.format(value.doubleValue());
    }

    static public CompanyEvent createThresholdExceededEvent(PayrollRun pPayrollRun,
                                                            PaymentTemplate pPaymentTemplate, SpcfCalendar pPaymentPeriodStartDate) {
        // Threshold Period state date is the pPaymentPeriodStartDate (payment period start date)
        String startDate = pPaymentPeriodStartDate.format("d");

        // Threshold End date is the paycheck date when we hit the threshold
        String endDate = pPayrollRun.getPaycheckDate().format("d");

        //Create new threshold exceeded event only if event is not found already with same dates.
        if(findUnreversedThresholdExceededEventWithStartAndEndDates(pPayrollRun.getCompany(), pPaymentTemplate, startDate, endDate) == null) {

            CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(), EventTypeCode.ThresholdExceeded);

            event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRun.getId().toString());
            event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, pPaymentTemplate.getPaymentTemplateCd());
            event.addCompanyEventDetail(EventDetailTypeCode.ThresholdPeriodStartDate, startDate);
            event.addCompanyEventDetail(EventDetailTypeCode.ThresholdPeriodEndDate, endDate);
            Application.save(event);
            return event;

        } else {
            return null; // Do not create new event if already existing, return null
        }
    }

    public static CompanyEvent createManualRedebitCreatedEvent(
            Company pCompany, FinancialTransaction pFinancialTransaction) {
        CompanyEvent event = createManualRedebitCreatedEventNoFinTxns(pCompany);

        addCompanyServiceIdEventDetail(event, pFinancialTransaction);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinancialTransaction.getId().toString());

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createManualRedebitCreatedEvent(
            Company pCompany,
            DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        CompanyEvent event = createManualRedebitCreatedEventNoFinTxns(pCompany);

        // set event details
        for (FinancialTransaction currTxn : pFinancialTransactions) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        currTxn.getId().toString());
        }

        if(pFinancialTransactions.size() > 0) {
            addCompanyServiceIdEventDetail(event, pFinancialTransactions.getFirst());
        } else {
            addCompanyServiceIdEventDetail(event);
        }

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    private static CompanyEvent createManualRedebitCreatedEventNoFinTxns(
            Company pCompany) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ManualRedebitCreated);

        return Application.save(event);
    }

    public static CompanyEvent createPINUpdatedEvent(Company pCompany) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.PINUpdated);

        addCompanyServiceIdEventDetail(event);
        setCaseIdInCompanyEvent(pCompany.getSourceSystemCompanyId(), event);

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createPINResetEvent(Company pCompany,
                                                   String pUniqueIdentifier, String pUserId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.PINReset);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier,
                                    pUniqueIdentifier);
        event.addCompanyEventDetail(EventDetailTypeCode.UserId, pUserId);

        return Application.save(event);
    }

    public static CompanyEvent createLiabilityAdjustmentCreatedEvent(Company pCompany,
                                                                     String pUniqueIdentifier, String pPayrollRunId, String pNoteText, String pDetails) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.LiabilityAdjustmentCreated);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier,
                                    pUniqueIdentifier);
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNoteText);
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pDetails);

        return Application.save(event);
    }
    public static CompanyEvent createManualLedgerEntryEvent(Company pCompany, String pUniqueIdentifier, String pNoteText) {
        return createManualLedgerEntryEvent(pCompany, pUniqueIdentifier, null, pNoteText, null);
    }

    public static CompanyEvent createManualLedgerEntryEvent(Company pCompany, String pUniqueIdentifier,
                                                            String pPayrollRunId, String pNoteText, String pDetails) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ManualLedgerEntry);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier,
                                    pUniqueIdentifier);
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNoteText);
        event.addCompanyEventDetail(EventDetailTypeCode.Details, pDetails);

        return Application.save(event);
    }

    public static CompanyEvent createPendingPaymentRefundedEvent(Company pCompany, String pUniqueIdentifier,
                                                            String pPayrollRunId, String pNoteText) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.PendingPaymentRefunded);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier,
                pUniqueIdentifier);
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNoteText);

        queueEmail(event, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createCustomerTaxPaymentCreatedEvent(Company pCompany,
                                                                    String pUniqueIdentifier, String pNoteText) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.LiabilityAdjustmentCreated);

        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier,
                                    pUniqueIdentifier);

        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNoteText);

        return Application.save(event);
    }

    public static CompanyEvent createManualDataSyncEvent(Company company, String noteText) {
        CompanyEvent event = createCompanyEvent(company, EventTypeCode.ManualDataSync);

        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, noteText);
        setCaseIdInCompanyEvent(company.getSourceSystemCompanyId(),event);
        return Application.save(event);
    }

    public static CompanyEvent createOfferingUpdatedEvent(Company pCompany,
                                                          String pOldOffering, String pNewOffering) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.OfferingUpdated);

        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,
                                    pOldOffering);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue,
                                    pNewOffering);

        return Application.save(event);
    }

    public static CompanyEvent createPINCreatedEvent(Company pCompany) {
        int numPINCreatedEvents = CompanyEvent.getEventCountByType(pCompany,
                                                                   EventTypeCode.PINCreated);
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.PINCreated);

        addCompanyServiceIdEventDetail(event);
        setCaseIdInCompanyEvent(pCompany.getSourceSystemCompanyId(), event);

        // Only send an email if the company is QBDT, they have the DD service and it's their first PIN created event

        CompanyService ddService = CompanyService.findCompanyService(pCompany, ServiceCode.DirectDeposit);
        if (numPINCreatedEvents == 0 && ddService != null && ddService.getStatusCd() != ServiceSubStatusCode.PendingBankVerification) {
            // check for email eligibility
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    private static void setCaseIdInCompanyEvent(String sourceSystemCompanyId, CompanyEvent event) {
        String caseId=com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager.getValue();
        if(caseId!=null){
            event.addCompanyEventDetail(EventDetailTypeCode.CaseId,caseId);
            logger.info("CaseId="+caseId+" EventTypeCode="+event.getEventTypeCd()+" CompanyPSID="+sourceSystemCompanyId+" CreatorId"+event.getCreatorId());
        }
    }

    public static CompanyEvent createAuthenticationFailedEvent(
            Company pCompany, String pFailureReason) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.AuthenticationFailed);

        event.addCompanyEventDetail(EventDetailTypeCode.FailureReason,
                                    pFailureReason);

        return Application.save(event);
    }

    public static CompanyEvent createLastChanceNotifyEvent(
            PayrollRun pPayrollRun, SpcfCalendar pWireExpectedDate) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(),
                                                EventTypeCode.LastChanceNotify);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRun.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.WireExpectedDate,
                                    pWireExpectedDate.format("yyyyMMdd"));

        addCompanyServiceIdEventDetail(event, pPayrollRun);

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createChangeRedebitToWireExpectedEvent(
            PayrollRun pPayrollRun, CollectionStageCode pCollectionStage,
            SpcfCalendar pWireExpectedDate) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(),
                                                EventTypeCode.ChangeRedebitToWireExpected);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRun.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.CollectionStage,
                                    EnumUtils.getReadableName(pCollectionStage));
        event.addCompanyEventDetail(EventDetailTypeCode.WireExpectedDate,
                                    pWireExpectedDate.format("yyyyMMdd"));

        addCompanyServiceIdEventDetail(event, pPayrollRun);

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createPayrollCancelPendingEvent(
            PayrollRun pPayrollRun, SpcfCalendar pCancellationDate) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(),
                                                EventTypeCode.PayrollCancelPending);

        addCompanyServiceIdEventDetail(event, pPayrollRun);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRun.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.CancellationDateTime,
                                    pCancellationDate.format("yyyyMMdd"));

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static List<CompanyEvent> createBillPaymentOffloadedEvent(PayrollRun pPayrollRun, BillPaymentSplit pBillPaymentSplit) {
        List<CompanyEvent> createBillPaymentOffloadedEvents = new ArrayList<CompanyEvent>();

        // In order to send this email we need to make sure the payee email is not null
        String payeeEmail = pBillPaymentSplit.getBillPayment().getPayee().getEmail();
        if (payeeEmail != null && !payeeEmail.isEmpty()) {
            String[] emailList = payeeEmail.split("[;,]");
            List<String> invalidEmailList = new ArrayList<String>();
            Payee payee = pBillPaymentSplit.getBillPayment().getPayee();
            for (String email : emailList) {
                CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(),
                                                        EventTypeCode.BillPaymentOffloaded);

                // set event details
                event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                            pPayrollRun.getId().toString());
                event.addCompanyEventDetail(EventDetailTypeCode.BillPaymentId, pBillPaymentSplit.getId().toString());
                email = email.trim();
                // PSP-2500 check if the vendor email is valid
                if (!payee.getHasInvalidEmail() && Validator.isValidEmail(email)) {
                    event.addCompanyEventDetail(EventDetailTypeCode.RecipientEmailAddress, email);
                    queueEmail(event, SourceSystemCode.QBDT);
                    Application.save(event);
                    createBillPaymentOffloadedEvents.add(event);
                } else {
                    invalidEmailList.add(email);
                }
            }
            if (!invalidEmailList.isEmpty()) {
                CompanyEvent event = createInvalidVendorEmailEvent(pPayrollRun, invalidEmailList);
                createBillPaymentOffloadedEvents.add(event);
                // if all the emails of this vendor are invalid
                if(invalidEmailList.size() == emailList.length) {
                    payee.setHasInvalidEmail(true);
                }
            }

        }
        return createBillPaymentOffloadedEvents;
    }

    public static CompanyEvent createNonAchPaymentReceivedEvent(
            DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        Company company = pFinancialTransactions.get(0).getCompany();
        CompanyEvent event = createCompanyEvent(company,
                                                EventTypeCode.NonAchPaymentReceived);

        // set event details
        for (FinancialTransaction pFinancialTransaction : pFinancialTransactions) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        pFinancialTransaction.getId().toString());
        }

        if(pFinancialTransactions.size() > 0) {
            addCompanyServiceIdEventDetail(event, pFinancialTransactions.getFirst());
        } else {
            addCompanyServiceIdEventDetail(event);
        }

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createFeeRebilledEvent(Company pCompany,
                                                      BillingDetail pRefundedBillingDetail,
                                                      BillingDetail pRebillBillingDetail,
                                                      FinancialTransaction pFinancialTransaction,
                                                      SpcfDecimal pRefundAmount) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.FeeRebilled);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.RefundedFeeBillingDetailId,
                                    pRefundedBillingDetail.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.FeeBillingDetailId,
                                    pRebillBillingDetail.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RefundAmount,
                                    pRefundAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.FeeAmount,
                                    pRebillBillingDetail.getItemTotal().toString());

        if (pFinancialTransaction != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        pFinancialTransaction.getId().toString());
        }

        addCompanyServiceIdEventDetail(event);

        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createFeeRefundedEvent(
            BillingDetail pRefundedBillingDetail,
            FinancialTransaction pFinancialTransaction, boolean pSupressEmail) {
        CompanyEvent event = createCompanyEvent(pFinancialTransaction.getCompany(),
                                                EventTypeCode.FeeRefunded);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.RefundedFeeBillingDetailId,
                                    pRefundedBillingDetail.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinancialTransaction.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.FeeType, pRefundedBillingDetail.getOfferingServiceChargeType().toString());

        addCompanyServiceIdEventDetail(event);

        // check for email eligibility
        if (!pSupressEmail) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createFeeRefundedEventForCourtesyRefund(FinancialTransaction pFinancialTransaction, String pNote) {

        CompanyEvent event = createCompanyEvent(pFinancialTransaction.getCompany(),
                                                EventTypeCode.FeeRefunded);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinancialTransaction.getId().toString());

        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNote);

        addCompanyServiceIdEventDetail(event);

        queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createAdditionalFilingAmountEvent(Company pCompany, PaymentTemplate pPaymentTemplate, String pName,
                                                                 Double oldAmount, Double newAmount, SpcfCalendar effectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.AdditionalFilingAmount);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, pPaymentTemplate.getPaymentTemplateCd());
        String detail = "Additional Filing Amount for " + pPaymentTemplate.getPaymentTemplateCd() + "/" + pName +
                " changed from " + (oldAmount == null ? "<none>" : oldAmount.toString()) + " to " + newAmount.toString() +
                " effective " + sdf.format(CalendarUtils.convertToDate(effectiveDate)) + ".";
        event.addCompanyEventDetail(EventDetailTypeCode.Details, detail);

        return Application.save(event);
    }

    public static CompanyEvent createContactEmailChangedEvent(
            Company pCompany, Contact pContact, String pOldValue, String pNewValue) {
        CompanyEvent event = createCompanyInfoChangeEvent(pCompany, pOldValue,
                                                          pNewValue, EventTypeCode.CompanyContactEmailChanged);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.ContactId,
                                    pContact.getId().toString());

        // PSRV002829: Only want email for Tax and DD clients
        if (pCompany.isCompanyOnService(ServiceCode.Tax) || pCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
            // check for email eligibility
            // (only want email for the following roles)
            if ((pContact.getContactRoleCd() == ContactRole.PayrollAdmin) ||
                    (pContact.getContactRoleCd() == ContactRole.PrimaryPrincipal)) {

                queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createCBAVerifiedEvent(
            CompanyBankAccount pCompanyBankAccount) {
        int numBankAccountVerifyCreatedEvents = CompanyEvent.getEventCountByType(pCompanyBankAccount.getCompany(),
                                                                                 EventTypeCode.BankAccountVerified);

        CompanyEvent event = createCompanyEvent(pCompanyBankAccount.getCompany(),
                                                EventTypeCode.BankAccountVerified);

        addCompanyServiceIdEventDetail(event);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId,
                                    pCompanyBankAccount.getId().toString());

        // check for email eligibility
        if (numBankAccountVerifyCreatedEvents == 0 && pCompanyBankAccount.getCompany().getCompanyPINCollection().size() > 0) {
            if (isMailNeeded(pCompanyBankAccount.getCompany(), EventTypeCode.BankAccountVerified)) {
                queueEmail(event, SourceSystemCode.QBDT);
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createChangeEmployeeCompanyEvent(Company company, EventTypeCode eventTypeCode, String employeeId) {
        CompanyEvent event = createCompanyEvent(company, eventTypeCode);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employeeId);

        return Application.save(event);
    }

    public static CompanyEvent createChangePayeeCompanyEvent(Company company, EventTypeCode eventTypeCode, String payeeId) {
        CompanyEvent event = createCompanyEvent(company, eventTypeCode);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayeeId, payeeId);

        return Application.save(event);
    }

    
    public static CompanyEvent createCBAChangeEvent(CompanyBankAccount pOldCompanyBankAccount,
            CompanyBankAccount pNewCompanyBankAccount, boolean pSupressEmail) {
        CompanyEvent event = createCompanyEvent(pNewCompanyBankAccount.getCompany(),
                                                EventTypeCode.CompanyBankAccountChange);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId,
                                    pNewCompanyBankAccount.getId().toString());
        String oldID = pOldCompanyBankAccount != null ? pOldCompanyBankAccount.getId().toString() : null;
        event.addCompanyEventDetail(EventDetailTypeCode.OldCompanyBankAccountId, oldID);


        addCompanyServiceIdEventDetail(event);

		boolean emailEnabled = SystemParameter
				.findBooleanValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, false);
		// check for email eligibility
        if (emailEnabled || !pSupressEmail) {
            if (isMailNeeded(pNewCompanyBankAccount.getCompany(), EventTypeCode.CompanyBankAccountChange)) {
                queueEmail(event, SourceSystemCode.QBDT);
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createEBAChangeEvent(EmployeeBankAccount pOldEmployeeBankAccount,
                                                    EmployeeBankAccount pNewEmployeeBankAccount) {
        CompanyEvent event = createCompanyEvent(pNewEmployeeBankAccount.getEmployee().getCompany(),
                                                EventTypeCode.EmployeeBankAccountChange);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.NewEmployeeBankAccountId,
                                    pNewEmployeeBankAccount.getId().toString());
        String oldID = pOldEmployeeBankAccount != null ? pOldEmployeeBankAccount.getId().toString() : null;
        event.addCompanyEventDetail(EventDetailTypeCode.OldEmployeeBankAccountId, oldID);

        addCompanyServiceIdEventDetail(event);

        //check for Employee status, if Inactive, do not trigger mail
        boolean isActiveEmployee = EmployeeStatus.Active.equals(pNewEmployeeBankAccount.getEmployee().getStatusCd());

        // check for email eligibility
        // if the EE's bank account number has changed, send an email
		boolean emailEnabled = SystemParameter.findBooleanValue(SystemParameter.Code.ENABLE_EBA_ADD_EMAIL_NOTIFICATION,
				false);
        if (isActiveEmployee && (emailEnabled || (pOldEmployeeBankAccount!=null && !pOldEmployeeBankAccount.getBankAccount().getAccountNumber().equals(
                pNewEmployeeBankAccount.getBankAccount().getAccountNumber())))) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        } else if(!isActiveEmployee) {
            logger.info(String.format("Employee Bank Account Change mail skipped due to inactive employee=%s", pNewEmployeeBankAccount.getEmployee().getId().toString()));
        }

        return Application.save(event);
    }

    public static CompanyEvent createVBDStatusChangeEvent(Company company, CompanyBankAccount companyBankAccount, String oldVBDStatus, String newVBDStatus) {

        CompanyEvent event = createCompanyEvent(company, EventTypeCode.CompanyBankAccountVBDStatusChange);

        event.addCompanyEventDetail(EventDetailTypeCode.OldBAStatus, oldVBDStatus);
        event.addCompanyEventDetail(EventDetailTypeCode.NewBAStatus, newVBDStatus);
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId, companyBankAccount.getId().toString());
        logger.info("VBD Status Change event created");
        return Application.save(event);
    }

    public static CompanyEvent createPBAChangeEvent(String oldAccNum, String oldRoutingNum, PayeeBankAccount pNewPayeeBankAccount) {
        CompanyEvent event = createCompanyEvent(pNewPayeeBankAccount.getPayee().getCompany(),
                                                EventTypeCode.PayeeBankAccountChange);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.NewPayeeBankAccountNumber, pNewPayeeBankAccount.getBankAccount().getAccountNumber());
        event.addCompanyEventDetail(EventDetailTypeCode.NewPayeeBankRoutingNumber, pNewPayeeBankAccount.getBankAccount().getRoutingNumber());

        event.addCompanyEventDetail(EventDetailTypeCode.OldPayeeBankAccountNumber, oldAccNum);
        event.addCompanyEventDetail(EventDetailTypeCode.OldPayeeBankRoutingNumber, oldRoutingNum);

        addCompanyServiceIdEventDetail(event);

        // check for email eligibility
        // if the Payee's bank account number has changed, send an email
        boolean emailEnabled = SystemParameter.findBooleanValue(SystemParameter.Code.ENABLE_PBA_ADD_EMAIL_NOTIFICATION,
                false);
        if (emailEnabled || (oldAccNum != null && !oldAccNum.equals(pNewPayeeBankAccount.getBankAccount().getAccountNumber()))) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createCustomerSignedUpEvent(CompanyService pCompanyService) {
        CompanyEvent event = createCompanyEvent(pCompanyService.getCompany(), EventTypeCode.CustomerSignedUp);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyServiceId, pCompanyService.getId().toString());

        // check for email eligibility
        // (only want dd or bill payment service)
        ServiceCode serviceCode = pCompanyService.getService().getServiceCd();
        CompanyService taxService = pCompanyService.getCompany().getService(ServiceCode.Tax);
        // Send e-mail if adding DD and not a current Tax customer.
        if (ServiceCode.DirectDeposit.equals(serviceCode) && (taxService == null || taxService.isCancelTerm())) {
            queueEmail(event, SourceSystemCode.QBDT);
            // Send e-mail if adding BP service.
        } else if (ServiceCode.BillPayment.equals(serviceCode)) {
            setCaseIdInCompanyEvent(pCompanyService.getCompany().getSourceSystemCompanyId(),event);
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createCompanyInfoChangeEvent(Company pCompany,
                                                            String pOldValue, String pNewValue, EventTypeCode pEventTypeCode) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,
                pOldValue);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue,
                pNewValue);
        setCaseIdInCompanyEvent(pCompany.getSourceSystemCompanyId(),event);

        return Application.save(event);
    }

    public static CompanyEvent createCompanyContactAddedEvent(Company pCompany, String pNewValue) {

        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.CompanyContactAdded);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue,
                                    pNewValue);

        setCaseIdInCompanyEvent(pCompany.getSourceSystemCompanyId(), event);
        return Application.save(event);
    }
    public static CompanyEvent createCompanyContactDeletedEvent(Company pCompany, String pOldValue ) {

        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.CompanyContactDeleted);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,
                                    pOldValue);

        return Application.save(event);
    }

    public static CompanyEvent createPayrollRunEvent(Company pCompany,
                                                     String pSourcePayrollRunId, SpcfUniqueId pPayrollRunId,
                                                     EventTypeCode pEventTypeCode) {
        return createPayrollRunEvent(pCompany, pSourcePayrollRunId, pPayrollRunId, pEventTypeCode, null);

    }

    public static CompanyEvent createPayrollRunEvent(Company pCompany,
                                                     String pSourcePayrollRunId, SpcfUniqueId pPayrollRunId,
                                                     EventTypeCode pEventTypeCode, ServiceCode pServiceCode) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        if (pSourcePayrollRunId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                        pSourcePayrollRunId);
        }

        if (pPayrollRunId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                        pPayrollRunId.toString());
        }

        if(pServiceCode != null){
            event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode, pServiceCode.toString());
        }
        // check for email eligibility
        // (only want email for the following event types)
        if (pEventTypeCode == EventTypeCode.PayrollReceived || pEventTypeCode == EventTypeCode.BillPaymentReceived || pEventTypeCode == EventTypeCode.AssistedPayrollConfirmation) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);

    }

    public static CompanyEvent createPayrollReceivedPayCardEvent(Company pCompany,
                                                                 String pSourcePayrollRunId, SpcfUniqueId pPayrollRunId,
                                                                 int numberOfPayCardPayOnlyPaychecks) {
        CompanyEvent ce = createPayrollRunEvent(pCompany, pSourcePayrollRunId, pPayrollRunId, EventTypeCode.PayrollReceivedPayCard);
        ce.addCompanyEventDetail(EventDetailTypeCode.PaycheckAmount, String.valueOf(numberOfPayCardPayOnlyPaychecks));
        return ce;
    }

    public static CompanyEvent createServiceKeyUpdatedEvent(Company pCompany,
                                                            String pOldServiceKey,
                                                            String pNewServiceKey,
                                                            Entitlement pEntitlement) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ServiceKeyUpdated);

        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, pOldServiceKey);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, pNewServiceKey);
        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, pEntitlement.getId().toString());

        // check for email eligibility
        if (!pEntitlement.getEntitlementCode().isAssisted() &&
                !pEntitlement.getEntitlementCode().isEOorER() &&
                /** Block Service Key Emails for new Symphony DIY units, as there is another email going out with this info */
                !(pEntitlement.getEntitlementCode().getIsUsageBilling() && pOldServiceKey.equals(""))) {
            // PSRV002771 - 10.1 Null pointer on email when svc key has yet to be generated due to timing
            // We need the new service key to be valid to create an email
            if ((pNewServiceKey != null) && (pNewServiceKey.length() > 0)) {
                queueEmail(event, SourceSystemCode.QBDT);
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createPaycheckEvent(Company pCompany,
                                                   String pSourcePaycheckId, SpcfUniqueId pPaycheckId,
                                                   EventTypeCode pEventTypeCode) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        if (pPaycheckId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.PaycheckId,
                                        pPaycheckId.toString());
        }

        return Application.save(event);
    }

    public static CompanyEvent createBillPaymentEvent(Company pCompany,
                                                      String pSourceId, SpcfUniqueId pBillPaymentId,
                                                      EventTypeCode pEventTypeCode) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        if (pBillPaymentId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.BillPaymentId,
                                        pBillPaymentId.toString());
        }


        return Application.save(event);
        
    }

    public static CompanyEvent createBackdatedPayrollEvent(
            PayrollRun pPayrollRun, int pNumberofDays) {
        CompanyEvent event = createPayrollRunEvent(pPayrollRun.getCompany(),
                                                   pPayrollRun.getSourcePayRunId(), pPayrollRun.getId(),
                                                   EventTypeCode.BackdatedPayrollReceived);

        return Application.save(event);
    }

    public static CompanyEvent createPayrollRejectEvent(Company pCompany,
                                                        String pSourcePayrollRunId, SpcfUniqueId pPayrollRunId,
                                                        String pRejectReason, String pRejectDescription,
                                                        CompanyEvent pLinkedEvent) {
        CompanyEvent event = createPayrollRunEvent(pCompany,
                                                   pSourcePayrollRunId, pPayrollRunId,
                                                   EventTypeCode.PayrollRejected);

        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRejectedReason,
                                    pRejectReason);

        if (pRejectDescription != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription,
                                        pRejectDescription);
        }

        if (pLinkedEvent != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.CompanyEventId,
                                        pLinkedEvent.getId().toString());
        }

        return Application.save(event);
    }

    public static void postSourceSystemTransmissionInvalidEvents(final Company pCompany,
                                                                 final String pTransmissionId,
                                                                 final ProcessResult pProcessResult) {

        if (pCompany == null || pTransmissionId == null || pProcessResult == null || pProcessResult.getMessages().isEmpty())
            return;

        Application.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                CompanyEvent.createInvalidSourceSystemTransmissionEvents(pCompany, pTransmissionId, pProcessResult);
                return new ProcessResult();
            }
        });

    }

    public static List<CompanyEvent> createInvalidSourceSystemTransmissionEvents(Company pCompany,
                                                                                 String pTransmissionId,
                                                                                 ProcessResult pProcessResult) {

        List<CompanyEvent> events = new ArrayList<CompanyEvent>();

        if (pCompany == null || pTransmissionId == null || pProcessResult == null || pProcessResult.getMessages().isEmpty())
            return events;

        for (Message message : pProcessResult.getMessages()) {
            CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.InvalidSourceSystemTransmissionInformation);
            event.setSourceId(pTransmissionId);
            event.addCompanyEventDetail(EventDetailTypeCode.SourceSystemTransmissionInvalidReason, message.getMessage());
            event.addCompanyEventDetail(EventDetailTypeCode.TransmissionId, pTransmissionId);
            event.addCompanyEventDetail(EventDetailTypeCode.MessageLevel, message.getLevel().name());
            event.addCompanyEventDetail(EventDetailTypeCode.ErrorCode, message.getMessageCode());
            events.add(event);
        }
        return events;
    }

    public static List<CompanyEvent> createInvalidEmployeeInformationEvents(final Company company, final String employeeId, final String employeeFullName, final String transmissionId, final ProcessResult validationResult) {
        List<CompanyEvent> companyEvents = new ArrayList<CompanyEvent>();

        if (validationResult.getMessages().isEmpty())
            return companyEvents;

        if (company == null || employeeId == null)
            return companyEvents;

        for (Message message : validationResult.getMessages()) {
            CompanyEvent event = CompanyEvent.createCompanyEvent(company, EventTypeCode.InvalidEmployeeInformation);
            event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode, ServiceCode.ThirdParty401k.name());
            event.addCompanyEventDetail(EventDetailTypeCode.SourceEmployeeId, employeeId);
            event.addCompanyEventDetail(EventDetailTypeCode.MessageLevel, message.getLevel().name());
            event.addCompanyEventDetail(EventDetailTypeCode.ErrorCode, message.getMessageCode());
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeName, employeeFullName);
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeInvalidReason, message.getMessage(), message.getLevel().name());
            if (transmissionId != null) {
                event.addCompanyEventDetail(EventDetailTypeCode.TransmissionId, transmissionId);
            }
            Application.save(event);
            companyEvents.add(event);
        }

        return companyEvents;
    }

    public static List<CompanyEvent> createInvalidPaycheckInformationEvents(final Company company,
                                                                            final String employeeId,
                                                                            final Paycheck paycheck,
                                                                            final String transmissionId,
                                                                            final ProcessResult validationResult) {
        final String sourcePaycheckId = paycheck == null ? null : paycheck.getSourcePaycheckId();
        final SpcfCalendar checkDate = paycheck == null ? null : paycheck.getPayrollRun().getPaycheckDate();
        final String checkAmount = paycheck == null ? null : paycheck.getNetAmount().toString();

        return CompanyEvent.createInvalidPaycheckInformationEvents(company, employeeId, sourcePaycheckId, checkDate, checkAmount, transmissionId, validationResult);
    }

    public static List<CompanyEvent> createInvalidPaycheckInformationEvents(final Company company,
                                                                            final String employeeId,
                                                                            final String paycheckId,
                                                                            final SpcfCalendar checkDate,
                                                                            final String amount,
                                                                            final String transmissionId,
                                                                            final ProcessResult validationResult) {
        List<CompanyEvent> companyEvents = new ArrayList<CompanyEvent>();

        if (validationResult.getMessages().isEmpty())
            return companyEvents;

        if (company == null || employeeId == null)
            return companyEvents;

        final Employee employee = company.getCloudEmployees().findEntity(Employee.SourceEmployeeId().equalTo(employeeId));
        String employeeFullName = employee != null ? employee.getFullName() : null;
        return createInvalidPaycheckInformationEvents(company, employeeId, employeeFullName, paycheckId, checkDate, amount, transmissionId, validationResult);
    }

    /**
     * Please be sure to pass the equivalent of Employee.getFullName().  Not SourceEmployeeId, not Employee.getFirstMiddleLast().
     */
    public static List<CompanyEvent> createInvalidPaycheckInformationEvents(final Company company,
                                                                            final String employeeId,
                                                                            final String pEmployeeFullName,
                                                                            final String paycheckId,
                                                                            final SpcfCalendar checkDate,
                                                                            final String amount,
                                                                            final String transmissionId,
                                                                            final ProcessResult validationResult) {
        List<CompanyEvent> companyEvents = new ArrayList<CompanyEvent>();

        if (validationResult.getMessages().isEmpty())
            return companyEvents;

        if (company == null || employeeId == null)
            return companyEvents;

        final String cDate = checkDate == null ? null : checkDate.format("MM/dd/yy");

        for (Message message : validationResult.getMessages()) {
            CompanyEvent event = CompanyEvent.createCompanyEvent(company, EventTypeCode.InvalidPaycheckInformation);
            event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode, ServiceCode.ThirdParty401k.name());
            event.addCompanyEventDetail(EventDetailTypeCode.SourceEmployeeId, employeeId);
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeName, pEmployeeFullName == null ? "Unknown" : pEmployeeFullName);
            event.addCompanyEventDetail(EventDetailTypeCode.SourcePaycheckId, paycheckId);
            event.addCompanyEventDetail(EventDetailTypeCode.PaycheckDate, cDate == null ? "Unknown" : cDate);
            event.addCompanyEventDetail(EventDetailTypeCode.PaycheckAmount, amount == null ? "Unknown" : amount);
            event.addCompanyEventDetail(EventDetailTypeCode.MessageLevel, message.getLevel().name());
            event.addCompanyEventDetail(EventDetailTypeCode.PaycheckInvalidReason, message.getMessage(), message.getLevel().name());
            if (transmissionId != null) {
                event.addCompanyEventDetail(EventDetailTypeCode.TransmissionId, transmissionId);
            }
            Application.save(event);
            companyEvents.add(event);
        }

        return companyEvents;
    }

    public static void deactivateInvalidEmployeeInformationEvents(Company company, String employeeId) {
        if (company == null || employeeId == null)
            return;

        Criterion<CompanyEventDetail> employeeEventCriterion =
                CompanyEventDetail.CompanyEvent().Company().equalTo(company)
                                  .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.InvalidEmployeeInformation))
                                  .And(CompanyEventDetail.CompanyEvent().StatusCd().equalTo(CompanyEventStatus.Active))
                                  .And(CompanyEventDetail.Company().equalTo(company))
                                  .And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.SourceEmployeeId))
                                  .And(CompanyEventDetail.Value().equalTo(employeeId));

        Expression<CompanyEventDetail> activeEmployeeEvents = new Query<CompanyEventDetail>().Where(employeeEventCriterion);
        DomainEntitySet<CompanyEventDetail> employeeEvents = Application.find(CompanyEventDetail.class, activeEmployeeEvents);
        for (CompanyEventDetail employeeEvent : employeeEvents) {
            employeeEvent.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
        }
    }

    //todo:DM fix this later by adding a whole new event

    public static boolean doesMissedCutoffEventExistForPaycheck(Company company, String employeeId, String paycheckId, Object... args) {
        if (company == null || employeeId == null)
            return false;

        MessageDefinition definition = MessageDefinition.getMessageDefinition(10069);
        MessageFormat format = new MessageFormat(definition.getMessageFormat());
        String messageToFind = format.format(args);

        Criterion<CompanyEventDetail> paycheckEventCriterion =
                CompanyEventDetail.CompanyEvent().Company().equalTo(company)
                                  .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.InvalidPaycheckInformation))
                                  .And(CompanyEventDetail.Company().equalTo(company))
                                  .And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.SourcePaycheckId))
                                  .And(CompanyEventDetail.Value().equalTo(paycheckId));

        Expression<CompanyEventDetail> activePaycheckEvents = new Query<CompanyEventDetail>().Where(paycheckEventCriterion);
        DomainEntitySet<CompanyEventDetail> employeeEvents = Application.find(CompanyEventDetail.class, activePaycheckEvents);
        for (CompanyEventDetail currDetail : employeeEvents) {
            Criterion<CompanyEventDetail> missedCutoffCriterion =
                    CompanyEventDetail.CompanyEvent().Company().equalTo(company)
                                      .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.InvalidPaycheckInformation))
                                      .And(CompanyEventDetail.Company().equalTo(company))
                                      .And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.PaycheckInvalidReason))
                                      .And(CompanyEventDetail.CompanyEvent().equalTo(currDetail.getCompanyEvent()))
                                      .And(CompanyEventDetail.Value().equalTo(messageToFind));
            Expression<CompanyEventDetail> missedCutoffEvents = new Query<CompanyEventDetail>().Where(missedCutoffCriterion);
            DomainEntitySet<CompanyEventDetail> events = Application.find(CompanyEventDetail.class, missedCutoffEvents);
            if (events.size() > 0) {
                return true;
            }
        }

        return false;
    }


    public static void deactivateInvalidPaycheckInformationEvents(Company company, String employeeId, String paycheckId) {
        if (company == null || employeeId == null)
            return;

        Criterion<CompanyEventDetail> paycheckEventCriterion =
                CompanyEventDetail.CompanyEvent().Company().equalTo(company)
                                  .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.InvalidPaycheckInformation))
                                  .And(CompanyEventDetail.CompanyEvent().StatusCd().equalTo(CompanyEventStatus.Active))
                                  .And(CompanyEventDetail.Company().equalTo(company))
                                  .And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.SourcePaycheckId))
                                  .And(CompanyEventDetail.Value().equalTo(paycheckId));

        Expression<CompanyEventDetail> activePaycheckEvents = new Query<CompanyEventDetail>().Where(paycheckEventCriterion);
        DomainEntitySet<CompanyEventDetail> employeeEvents = Application.find(CompanyEventDetail.class, activePaycheckEvents);
        for (CompanyEventDetail employeeEvent : employeeEvents) {
            employeeEvent.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
        }

    }

    public static void writeInvalidEmployeeInformationEvents(final Company company, final Map<String, ProcessResult> validationResults, final String transmissionId) {

        Application.executeTransactionThread(new TransactionThread<ProcessResult>() {
            public ProcessResult transaction() {
                // expire existing events
                for (String employeeKey : validationResults.keySet()) {
                    String[] keyParts = employeeKey.split(":");
                    String sourceId = keyParts[0];
                    String fullName = keyParts[1];

                    // expire existing events
                    CompanyEvent.deactivateInvalidEmployeeInformationEvents(company, sourceId);

                    ProcessResult validationResult = validationResults.get(employeeKey);
                    if (!validationResult.getMessages().isEmpty()) {
                        CompanyEvent.createInvalidEmployeeInformationEvents(company, sourceId, fullName, transmissionId, validationResult);
                    }
                }

                return new ProcessResult();
            }
        });
    }

    public static void updateInvalidPaycheckInformationEvents(final Company company, final String employeeId, final String paycheckId, final String transmissionId, final ProcessResult validationResult) {
        if (company == null || employeeId == null || !company.isCompanyOnService(ServiceCode.ThirdParty401k))
            return;

        // poor man's check to determine if this is a new paycheck or an existing paychecks
        // heavy assumption that existing paycheck will have been cached
        // new paychecks do not have events against them that need removing

        SpcfUniqueId paycheckSpcfId = Application.getSessionCache().getPrimaryKey(new NaturalKey(Paycheck.class, company.getId(), paycheckId));
        final Paycheck paycheck = paycheckSpcfId == null ? null : Application.findById(Paycheck.class, paycheckSpcfId);

        boolean markedAsNew = Application.getSessionCache().getNonHibernateObject(company.getSourceCompanyId() + ":" + paycheckId) != null;

        final boolean isNewPaycheck = (paycheck != null && paycheck.isNew()) || markedAsNew;

        final boolean hasValidationMessages = !validationResult.getMessages().isEmpty();

        // don't spawn the thread unless necessary
        if (isNewPaycheck && !hasValidationMessages)
            return;

        Application.executeTransactionThread(new TransactionThread<ProcessResult>() {
            public ProcessResult transaction() {
                if (!isNewPaycheck) {
                    // expire existing events
                    CompanyEvent.deactivateInvalidPaycheckInformationEvents(company, employeeId, paycheckId);
                }

                // add new events if necessary
                if (hasValidationMessages) {
                    SpcfCalendar checkDate = null;
                    String netAmount = null;
                    if (paycheck != null) {
                        checkDate = paycheck.getPayrollRun().getPaycheckDate();
                        netAmount = paycheck.getNetAmount().toString();
                    }

                    CompanyEvent.createInvalidPaycheckInformationEvents(company, employeeId, paycheckId, checkDate, netAmount, transmissionId, validationResult);
                }

                return new ProcessResult();
            }
        });
    }

    public static CompanyEvent createCompanyEventAndDetail(Company pCompany, EventTypeCode pEventTypeCode, EventDetailTypeCode pEventTypeDetailCode, String pDetailValue) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        event.addCompanyEventDetail(pEventTypeDetailCode, pDetailValue);

        return Application.save(event);
    }

    public static CompanyEvent createLimitViolationEvent(Company pCompany,
                                                         EventLimitCode pLimitCode, String pSourcePayrollRunId,
                                                         String pBankAccountNumber, String pBankAccountRoutingNumber,
                                                         Employee pEmployee, SpcfMoney pLimitAmount, SpcfMoney pViolationAmount,
                                                         SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.LimitViolation);
        event.setStatusEffectiveDate(pEffectiveDate);

        event.addCompanyEventDetail(EventDetailTypeCode.LimitType,
                                    EnumUtils.getReadableName(pLimitCode));
        event.addCompanyEventDetail(EventDetailTypeCode.LimitAmount,
                                    String.valueOf(pLimitAmount));
        event.addCompanyEventDetail(EventDetailTypeCode.ViolationAmount,
                                    String.valueOf(pViolationAmount));
        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pSourcePayrollRunId);

        if (pBankAccountNumber != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.BankAccountNumber,
                                        pBankAccountNumber);
        }

        if (pBankAccountRoutingNumber != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.BankAccountRoutingNumber,
                                        pBankAccountRoutingNumber);
        }

        if (pEmployee != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId,
                                        pEmployee.getId().toString());
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeName,
                                        pEmployee.getFirstName() + " " + pEmployee.getLastName());
        }

        return Application.save(event);
    }
    
    public static CompanyEvent createDDLimitViolationEvent(Company pCompany,EventLimitCode pLimitCode, String pSourcePayrollRunId,
			SpcfCalendar pEffectiveDate) {

		CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.LimitViolation);
		event.setStatusEffectiveDate(pEffectiveDate);
		
		event.addCompanyEventDetail(EventDetailTypeCode.LimitType, EnumUtils.getReadableName(pLimitCode));
		event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId, pSourcePayrollRunId);
		return Application.save(event);	
	}

    public static CompanyEvent createBPLimitViolationEvent(Company pCompany,
                                                           EventLimitCode pLimitCode, String pSourcePayrollRunId,
                                                           String pBankAccountNumber, String pBankAccountRoutingNumber,
                                                           Payee pPayee, SpcfMoney pLimitAmount, SpcfMoney pViolationAmount,
                                                           SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.LimitViolation);
        event.setStatusEffectiveDate(pEffectiveDate);

        event.addCompanyEventDetail(EventDetailTypeCode.LimitType,
                                    EnumUtils.getReadableName(pLimitCode));
        event.addCompanyEventDetail(EventDetailTypeCode.LimitAmount,
                                    String.valueOf(pLimitAmount));
        event.addCompanyEventDetail(EventDetailTypeCode.ViolationAmount,
                                    String.valueOf(pViolationAmount));
        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pSourcePayrollRunId);

        event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode, ServiceCode.BillPayment.toString());
        if (pBankAccountNumber != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.BankAccountNumber,
                                        pBankAccountNumber);
        }

        if (pBankAccountRoutingNumber != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.BankAccountRoutingNumber,
                                        pBankAccountRoutingNumber);
        }

        if (pPayee != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.PayeeId,
                                        pPayee.getId().toString());
            event.addCompanyEventDetail(EventDetailTypeCode.PayeeName,
                                        pPayee.getName());
        }

        return Application.save(event);
    }

    public static CompanyEvent createLimitIncreaseEvent(Company pCompany,
                                                        EventLimitCode pLimitCode, String pPayrollRunId, Employee pEmployee,
                                                        SpcfMoney pNewLimit, SpcfMoney pOldLimit, SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.DDIncreasePayrollLimit);
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.LimitType,
                                    EnumUtils.getReadableName(pLimitCode));
        event.addCompanyEventDetail(EventDetailTypeCode.NewLimitAmount,
                                    String.valueOf(pNewLimit));
        event.addCompanyEventDetail(EventDetailTypeCode.OldLimitAmount,
                                    String.valueOf(pOldLimit));
        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pPayrollRunId);

        if (pLimitCode.equals(EventLimitCode.Employee)) {
            event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId,
                                        pEmployee.getId().toString());
        }

        return Application.save(event);
    }

    public static CompanyEvent createBPLimitIncreaseEvent(Company pCompany,
                                                          EventLimitCode pLimitCode, String pPayrollRunId, Payee pPayee,
                                                          SpcfMoney pNewLimit, SpcfMoney pOldLimit, SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.BPIncreasePayrollLimit);
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.LimitType,
                                    EnumUtils.getReadableName(pLimitCode));
        event.addCompanyEventDetail(EventDetailTypeCode.NewLimitAmount,
                                    String.valueOf(pNewLimit));
        event.addCompanyEventDetail(EventDetailTypeCode.OldLimitAmount,
                                    String.valueOf(pOldLimit));
        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pPayrollRunId);

        if (pLimitCode.equals(EventLimitCode.Payee)) {
            event.addCompanyEventDetail(EventDetailTypeCode.PayeeId,
                                        pPayee.getId().toString());
        }

        return Application.save(event);
    }

    public static CompanyEvent createStrikeEvent(Company pCompany,
                                                 StrikeReason pStrikeReason, String pStrikeReasonDescription,
                                                 SpcfCalendar pEffectiveDate, FinancialTransaction pFinancialTransaction) {
        DomainEntitySet<FinancialTransaction> finTxns = new DomainEntitySet<FinancialTransaction>();
        finTxns.add(pFinancialTransaction);

        return createStrikeEventWithFinTxnAssoc(pCompany, pStrikeReason,
                                                pStrikeReasonDescription, pEffectiveDate, finTxns);
    }

    public static CompanyEvent createStrikeEvent(Company pCompany,
                                                 StrikeReason pStrikeReason, String pStrikeReasonDescription,
                                                 SpcfCalendar pEffectiveDate,
                                                 DomainEntitySet<FinancialTransaction> pFinTxns) {
        return createStrikeEventWithFinTxnAssoc(pCompany, pStrikeReason,
                                                pStrikeReasonDescription, pEffectiveDate, pFinTxns);
    }

    public static CompanyEvent createStrikeEventWithFinTxnAssoc(
            Company pCompany, StrikeReason pStrikeReason,
            String pStrikeReasonDescription, SpcfCalendar pEffectiveDate,
            DomainEntitySet<FinancialTransaction> pFinTxns) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.Strike);

        // set base event properties
        event.setEventTimeStamp(pEffectiveDate);

        event.addCompanyEventDetail(EventDetailTypeCode.StrikeReason,
                                    EnumUtils.getReadableName(pStrikeReason));

        // set event details
        if (pStrikeReason.equals(StrikeReason.Manual)) {
            event.addCompanyEventDetail(EventDetailTypeCode.ManualStrikeReasonDescription,
                                        pStrikeReasonDescription);
        } else {
            for (FinancialTransaction currTxn : pFinTxns) {
                event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                            currTxn.getId().toString());
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createReversalRequestedEvent(Company pCompany,
                                                            FinancialTransaction pFinTxn, boolean pIntuitInitiated,
                                                            SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ReversalRequested);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.IntuitInitiated,
                                    Boolean.toString(pIntuitInitiated));

        addCompanyServiceIdEventDetail(event, pFinTxn);

        // check for email eligibility
        // (only want email for non-Intuit initiated reversals with settlement type of ACH)
        if (!pIntuitInitiated &&
                (pFinTxn.getSettlementTypeCd() == SettlementType.ACH)) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createReversalOKEvent(Company pCompany,
                                                     FinancialTransaction pFinTxn, SpcfCalendar pEffectiveDate,
                                                     RefundStatusType pRefundStatus,
                                                     RefundStatusReasonType pRefundStatusReasonType) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ReversalOK);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.RefundStatus,
                                    EnumUtils.getReadableName(pRefundStatus));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        if (null != pRefundStatusReasonType) {
            event.addCompanyEventDetail(EventDetailTypeCode.RefundStatusReason,
                                        EnumUtils.getReadableName(pRefundStatusReasonType));
        }

        addCompanyServiceIdEventDetail(event);

        boolean bIsClientRequested = pFinTxn.isReversalClientRequested(pCompany);

        // check for email eligibility...only send if the original reversal was client-requested

        if (bIsClientRequested && (pRefundStatus == RefundStatusType.Issued)) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createNSFReturnEvent(Company pCompany,
                                                    DomainEntitySet<FinancialTransaction> pFinTxns, String pAchReturnCode,
                                                    NSFSubTypeType pNsfSubType) {
        CompanyEvent event = CompanyEvent.createNSFReturnEventNoFinTxnAssoc(pCompany,
                                                                            pAchReturnCode, pNsfSubType);

        for (FinancialTransaction currTxn : pFinTxns) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        currTxn.getId().toString());
        }

        if(pFinTxns.size() > 0) {
            addCompanyServiceIdEventDetail(event, pFinTxns.getFirst());
        } else {
            addCompanyServiceIdEventDetail(event);
        }

        // check for email eligibility
        // (only want email for the following nsf sub-types, based on source system)
        switch (pNsfSubType) {
            case NSFAutoRedebit:
                queueEmail(event, SourceSystemCode.QBDT);
                break;

            case SecondNSF:
                queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
                break;
        }

        return Application.save(event);
    }

    public static CompanyEvent createDDRejectEvent(Company pCompany,
                                                   FinancialTransaction pFinTxn, String pAchReturnCode,
                                                   RefundStatusType pRefundStatus,
                                                   RefundStatusReasonType pRefundStatusReason, SpcfCalendar pEffectiveDate,
                                                   boolean isBillPayment) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.DDReject);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.DDReject));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RefundStatus,
                                    EnumUtils.getReadableName(pRefundStatus));

        // If this was an employee (or Vendor) credit, save the current/old account information.
        if(BankAccountOwnerType.Employee.equals(pFinTxn.getCreditBankAccountType())) {
            BankAccount ba;
            if (isBillPayment) {
                ba = pFinTxn.getPayeeBankAccount().getBankAccount();
                event.addCompanyEventDetail(EventDetailTypeCode.PayeeBankAccountId,
                                            pFinTxn.getPayeeBankAccount().getId().toString());
            } else {
                ba = pFinTxn.getEmployeeBankAccount().getBankAccount();
                event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId,
                                            pFinTxn.getEmployeeBankAccount().getId().toString());
            }
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountNumber,
                                        ba.getAccountNumber());
            event.addCompanyEventDetail(EventDetailTypeCode.OldRoutingNumber,
                                        ba.getRoutingNumber());
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountType,
                                        EnumUtils.getReadableName(ba.getAccountTypeCd()));
        }

        addCompanyServiceIdEventDetail(event, pFinTxn);

        if (null != pRefundStatusReason) {
            event.addCompanyEventDetail(EventDetailTypeCode.RefundStatusReason,
                                        EnumUtils.getReadableName(pRefundStatusReason));
        }
        // If it is a Vendor Payment Reject set the token to zero, we do not want to sync it back
        if (pFinTxn.getPayrollRun() != null && pFinTxn.getPayrollRun().getPayrollRunType().equals(PayrollType.BillPayment)) {
            event.setEventToken(0L);
        }
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createWireExpectedEvent(Company pCompany,
                                                       PayrollRun pPayrollRun, CollectionStageCode pCollectionStage,
                                                       SpcfCalendar pWireExpectedDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.WireExpected);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRun.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.CollectionStage,
                                    EnumUtils.getReadableName(pCollectionStage));
        event.addCompanyEventDetail(EventDetailTypeCode.WireExpectedDate,
                                    pWireExpectedDate.format("yyyyMMdd"));

        return Application.save(event);
    }

    public static CompanyEvent createACHReturnStatusChangeEvent(
            Company pCompany, String pPayrollRunId,
            PayrollStatus pOldPayrollStatus, PayrollStatus pNewPayrollStatus) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ACHReturnStatusChanged);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.OldPayrollStatus,
                                    EnumUtils.getReadableName(pOldPayrollStatus));
        event.addCompanyEventDetail(EventDetailTypeCode.NewPayrollStatus,
                                    EnumUtils.getReadableName(pNewPayrollStatus));

        return Application.save(event);
    }

    public static CompanyEvent createRedebitAmountUpdatedEvent(
            Company pCompany, String pPayrollRunId, String pOldAmount,
            String pNewAmount) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.RedebitAmountUpdated);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.OldAmount, pOldAmount);
        event.addCompanyEventDetail(EventDetailTypeCode.NewAmount, pNewAmount);

        return Application.save(event);
    }

    public static CompanyEvent createRedebitDateUpdatedEvent(Company pCompany,
                                                             String pPayrollRunId, String pOldDate, String pNewDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.RedebitDateUpdated);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.OldDate, pOldDate);
        event.addCompanyEventDetail(EventDetailTypeCode.NewDate, pNewDate);

        return Application.save(event);
    }

    public static CompanyEvent createPrefundingReceivedEvent(Company pCompany,
                                                             String pPayrollRunId,
                                                             Collection<FinancialTransaction> pFinancialTransactions) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.PrefundingReceived);
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRunId);

        for (FinancialTransaction financialTransaction : pFinancialTransactions) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        financialTransaction.getId().toString());
        }

        return Application.save(event);
    }

    private static CompanyEvent createNSFReturnEventNoFinTxnAssoc(
            Company pCompany, String pAchReturnCode, NSFSubTypeType pNsfSubType) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.NSF);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                     pAchReturnCode).toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.NSF));
        event.addCompanyEventDetail(EventDetailTypeCode.NSFSubType,
                                    EnumUtils.getReadableName(pNsfSubType));

        return Application.save(event);
    }

    public static CompanyEvent createERPayableRefundCreatedEvent(Company pCompany, FinancialTransaction finTxn) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ERPayableRefundCreated);
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId, finTxn.getId().toString());
        return event;
    }

    public static CompanyEvent createERRefundReturnEvent(Company pCompany,
                                                         DomainEntitySet<FinancialTransaction> pFinTxn, String pAchReturnCode) {
        CompanyEvent event = createERRefundReturnEventNoFinTxnAssoc(pCompany,
                                                                    pAchReturnCode);

        for (FinancialTransaction currTxn : pFinTxn) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        currTxn.getId().toString());
        }

        return Application.save(event);
    }

    public static CompanyEvent createERRefundReturnEvent(Company pCompany,
                                                         FinancialTransaction pFinTxn, String pAchReturnCode) {
        CompanyEvent event = createERRefundReturnEventNoFinTxnAssoc(pCompany,
                                                                    pAchReturnCode);

        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        return Application.save(event);
    }

    private static CompanyEvent createERRefundReturnEventNoFinTxnAssoc(
            Company pCompany, String pAchReturnCode) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ERRefundReturn);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                     pAchReturnCode).toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.ERRefundReturn));

        return Application.save(event);
    }

    public static CompanyEvent createFeeDebitReturnEvent(Company pCompany,
                                                         FinancialTransaction pFinTxn, String pAchReturnCode) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.FeeReturn);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                     pAchReturnCode).toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.FeeReturn));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        return Application.save(event);
    }

    public static CompanyEvent createSalesTaxDebitReturnEvent(Company pCompany,
                                                              FinancialTransaction pFinTxn, String pAchReturnCode) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.SalesTaxReturn);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                     pAchReturnCode).toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.SalesTaxReturn));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        return Application.save(event);
    }

    public static CompanyEvent createFeeCreatedEvent(Company pCompany,
                                                     DomainEntitySet<FinancialTransaction> pFinancialTransactions) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.FeeCreated);

        SettlementType settlementType = null;
        // set event details
        for (FinancialTransaction financialTransaction : pFinancialTransactions) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId, financialTransaction.getId().toString());
            settlementType = financialTransaction.getSettlementTypeCd();
        }

        addCompanyServiceIdEventDetail(event);

        // only want email for ACH settlement types, the given service charge types and only if the user is an Agent or Annual Billing Batch job
        PspPrincipal principal = Application.getCurrentPrincipal();

        if ((principal.isAgent() || SystemPrincipal.AnnualBillingBatchJob.equals(principal.getSystemPrincipal())) &&
                (SettlementType.ACH.equals(settlementType))) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createFeeOffloadedEvent(Company pCompany,
                                                       SpcfUniqueId pFinancialTransactionId) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.FeeOffloaded);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinancialTransactionId.toString());

        return Application.save(event);
    }

    public static CompanyEvent createReversalReturnEvent(Company pCompany,
                                                         FinancialTransaction pFinTxn, String pAchReturnCode) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.ReversalReturn);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                     pAchReturnCode).toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.ReversalReturn));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        addCompanyServiceIdEventDetail(event);

        boolean bIsClientRequested = pFinTxn.isReversalClientRequested(pCompany);

        // check for email eligibility...only send if the original reversal was client-requested

        if (bIsClientRequested) {
            queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    private static void addCompanyServiceIdEventDetail(CompanyEvent pEvent, FinancialTransaction pFinancialTransaction) {
        addCompanyServiceIdEventDetail(pEvent, pFinancialTransaction.getPayrollRun());
    }

    private static void addCompanyServiceIdEventDetail(CompanyEvent pEvent, PayrollRun pPayrollRun) {
        if(pPayrollRun != null && pPayrollRun.getPayrollRunType() == PayrollType.BillPayment) {
            addCompanyServiceIdEventDetail(pEvent, ServiceCode.BillPayment);
        } else {
            addCompanyServiceIdEventDetail(pEvent);
        }
    }

    private static void addCompanyServiceIdEventDetail(CompanyEvent pEvent) {
        Company company = pEvent.getCompany();
        if(company.isCompanyOnService(ServiceCode.Tax)) {
            addCompanyServiceIdEventDetail(pEvent, ServiceCode.Tax);
        } else if(company.isCompanyOnService(ServiceCode.DirectDeposit)) {
            addCompanyServiceIdEventDetail(pEvent, ServiceCode.DirectDeposit);
        }
    }

    private static void addCompanyServiceIdEventDetail(CompanyEvent pEvent, ServiceCode pServiceCode) {
        Company company = pEvent.getCompany();
        pEvent.addCompanyEventDetail(EventDetailTypeCode.CompanyServiceId, company.getCompanyService(pServiceCode).getId().toString());
    }

    public static CompanyEvent createDDDebitReturnEvent(Company pCompany,
                                                        FinancialTransaction pFinTxn, String pAchReturnCode,
                                                        PayrollStatus pOldPayrollStatus, PayrollStatus pNewPayrollStatus,
                                                        SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createDDDebitReturnEventNoFinTxnAssoc(pCompany,
                                                                   pAchReturnCode, pOldPayrollStatus, pNewPayrollStatus,
                                                                   pEffectiveDate);

        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        addCompanyServiceIdEventDetail(event, pFinTxn);

        // check for email eligibility
        // (only want email for the following new payroll statuses)
        ACHReturnReason reasonCode = EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                      pAchReturnCode);

        switch (pNewPayrollStatus) {
            case ReturnedTwice:
                // (only want email for the following source systems)
                queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
                break;

            case DebitReturned:
                // (only want email for the following source systems)
                // (only want email for non-nsf return types, excluding the following old payroll statuses)
                if ((reasonCode != ACHReturnReason.R01) &&
                        (reasonCode != ACHReturnReason.R09) &&
                        (pOldPayrollStatus != PayrollStatus.OffloadedDebit) &&
                        (pOldPayrollStatus != PayrollStatus.RedebitOffloaded) &&
                        (pOldPayrollStatus != PayrollStatus.AutoRedebitOffloaded)) {
                    queueEmail(event, SourceSystemCode.QBDT);
                }
                break;
        }

        return Application.save(event);
    }

    public static CompanyEvent createDDDebitReturnEvent(Company pCompany,
                                                        DomainEntitySet<FinancialTransaction> pFinTxns, String pAchReturnCode,
                                                        PayrollStatus pOldPayrollStatus, PayrollStatus pNewPayrollStatus,
                                                        SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createDDDebitReturnEventNoFinTxnAssoc(pCompany,
                                                                   pAchReturnCode, pOldPayrollStatus, pNewPayrollStatus,
                                                                   pEffectiveDate);

        for (FinancialTransaction currTxn : pFinTxns) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                        currTxn.getId().toString());
        }

        if(pFinTxns.size() > 0) {
            addCompanyServiceIdEventDetail(event, pFinTxns.getFirst());
        } else {
            addCompanyServiceIdEventDetail(event);
        }

        // check for email eligibility
        // (only want email for the following new payroll statuses)
        ACHReturnReason reasonCode = EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                      pAchReturnCode);

        switch (pNewPayrollStatus) {
            case ReturnedTwice:

                // (only want email for the following source systems)
                queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);

                break;

            case DebitReturned:

                // (only want email for the following source systems)
                // (only want email for non-nsf return types, excluding the following old payroll statuses)
                if ((reasonCode != ACHReturnReason.R01) &&
                        (reasonCode != ACHReturnReason.R09) &&
                        (pOldPayrollStatus != PayrollStatus.OffloadedDebit) &&
                        (pOldPayrollStatus != PayrollStatus.RedebitOffloaded) &&
                        (pOldPayrollStatus != PayrollStatus.AutoRedebitOffloaded)) {
                    queueEmail(event, SourceSystemCode.QBDT);
                }

                break;
        }

        return Application.save(event);
    }

    private static CompanyEvent createDDDebitReturnEventNoFinTxnAssoc(
            Company pCompany, String pAchReturnCode,
            PayrollStatus pOldPayrollStatus, PayrollStatus pNewPayrollStatus,
            SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.DDDebitReturn);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        ACHReturnReason reasonCode = EnumUtils.getEnumForReadableName(ACHReturnReason.class,
                                                                      pAchReturnCode);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ACHReturnReasonCode,
                                    reasonCode.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.DDDebitReturn));
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollStatus,
                                    EnumUtils.getReadableName(pNewPayrollStatus));

        return Application.save(event);
    }

    public static CompanyEvent createCBAVerificationReturnEvent(
            Company pCompany, FinancialTransaction pFinTxn, String pAchReturnCode,
            VerificationStatusType pVerificationStatus, SpcfCalendar pEffectiveDate) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.CBAVerifyReturn);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchReturnCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn));
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.VerificationStatus,
                                    EnumUtils.getReadableName(pVerificationStatus));

        addCompanyServiceIdEventDetail(event);

        // check for email eligibility
        // (only want email for the following verification status)
        if ((pVerificationStatus == VerificationStatusType.PendingVerification) ||
                (pVerificationStatus == VerificationStatusType.CBADeactivated)) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createNoticeOfChangeEvent(Company pCompany,
                                                         String pAchChangeCode, CompanyBankAccount pCompanyBankAccount,
                                                         EmployeeBankAccount pEmployeeBankAccount,
                                                         PayeeBankAccount pPayeeBankAccount, boolean isCompanyBankAccount,
                                                         String pOldAccountNumber, String pNewAccountNumber,
                                                         String pOldRoutingNumber, String pNewRoutingNumber,
                                                         BankAccountType pOldAccountTypeCode, BankAccountType pNewAccountTypeCode,
                                                         ACHBankAccountType pOldAchAccountTypeCode, ACHBankAccountType pNewAchAccountTypeCode,
                                                         FinancialTransaction finTxn) {

        boolean sendEmail = true;
        if (C05_NOC.equals(pAchChangeCode) && (pNewAchAccountTypeCode != null && pNewAchAccountTypeCode.in(ACHBankAccountType.Ledger, ACHBankAccountType.Loan))) {
            sendEmail = false;
        }

        CompanyEvent event = createNoticeOfChangeEvent(pCompany,
                                                       pAchChangeCode, pCompanyBankAccount, pEmployeeBankAccount, pPayeeBankAccount,
                                                       isCompanyBankAccount, finTxn, sendEmail);

        if (!pAchChangeCode.equals("C04")) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountNumber,
                                        pOldAccountNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.NewAccountNumber,
                                        pNewAccountNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.OldRoutingNumber,
                                        pOldRoutingNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.NewRoutingNumber,
                                        pNewRoutingNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountType,
                                        EnumUtils.getReadableName(pOldAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.NewAccountType,
                                        EnumUtils.getReadableName(pNewAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.OldAchAccountType,
                                        EnumUtils.getReadableName(pOldAchAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.NewAchAccountType,
                                        EnumUtils.getReadableName(pNewAchAccountTypeCode));
        }

        return Application.save(event);
    }

    public static CompanyEvent createERLoanNoticeOfChangeEvent(Company pCompany,
                                                               String pAchChangeCode, CompanyBankAccount pCompanyBankAccount,
                                                               boolean isCompanyBankAccount,
                                                               String pOldAccountNumber, String pNewAccountNumber,
                                                               String pOldRoutingNumber, String pNewRoutingNumber,
                                                               BankAccountType pOldAccountTypeCode, BankAccountType pNewAccountTypeCode,
                                                               ACHBankAccountType pOldAchAccountTypeCode, ACHBankAccountType pNewAchAccountTypeCode,
                                                               FinancialTransaction finTxn) {
        CompanyEvent event = createERLoanNoticeOfChangeEvent(pCompany, pAchChangeCode, pCompanyBankAccount, finTxn);

        if (!pAchChangeCode.equals("C04")) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountNumber,
                                        pOldAccountNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.NewAccountNumber,
                                        pNewAccountNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.OldRoutingNumber,
                                        pOldRoutingNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.NewRoutingNumber,
                                        pNewRoutingNumber);
            event.addCompanyEventDetail(EventDetailTypeCode.OldAccountType,
                                        EnumUtils.getReadableName(pOldAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.NewAccountType,
                                        EnumUtils.getReadableName(pNewAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.OldAchAccountType,
                                        EnumUtils.getReadableName(pOldAchAccountTypeCode));
            event.addCompanyEventDetail(EventDetailTypeCode.NewAchAccountType,
                                        EnumUtils.getReadableName(pNewAchAccountTypeCode));
        }

        return Application.save(event);
    }

    public static CompanyEvent createNoticeOfChangeEvent(Company pCompany,
                                                         String pAchChangeCode, CompanyBankAccount pCompanyBankAccount,
                                                         EmployeeBankAccount pEmployeeBankAccount, PayeeBankAccount pPayeeBankAccount,
                                                         boolean isCompanyBankAccount, FinancialTransaction finTxn, Boolean pSendEmail) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.NOC);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchChangeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.NOC));

        if (isCompanyBankAccount) {
            event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId,
                                        pCompanyBankAccount.getId().toString());
        } else {
            if (pEmployeeBankAccount != null) {
                event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId,
                                            pEmployeeBankAccount.getId().toString());

                Employee employee = pEmployeeBankAccount.getEmployee();
                event.addCompanyEventDetail(EventDetailTypeCode.EmployeeName,
                                            employee.getFirstName() + " " + employee.getLastName());
            } else {
                event.addCompanyEventDetail(EventDetailTypeCode.PayeeBankAccountId,
                                            pPayeeBankAccount.getId().toString());

                Payee payee = pPayeeBankAccount.getPayee();
                event.addCompanyEventDetail(EventDetailTypeCode.PayeeName,
                                            payee.getName());
            }

        }

        addCompanyServiceIdEventDetail(event, finTxn);

        // check for email eligibility
        if (pSendEmail) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createERLoanNoticeOfChangeEvent(Company pCompany,
                                                               String pAchChangeCode, CompanyBankAccount pCompanyBankAccount,
                                                               FinancialTransaction finTxn) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ERLoanNOC);

        // set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ACHEventCd,
                                    pAchChangeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ReturnType,
                                    EnumUtils.getReadableName(ACHReturnType.NOC));

        event.addCompanyEventDetail(EventDetailTypeCode.CompanyBankAccountId,
                                    pCompanyBankAccount.getId().toString());

        addCompanyServiceIdEventDetail(event, finTxn);

        // check for email eligibility
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createPayrollCancelledEvent(
            PayrollRun pPayrollRun, CancellationReasonCode pCancellationReasonCode,
            CancellationScopeCode pCancellationScopeCode) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(),
                                                EventTypeCode.PayrollCancelled);

        event.addCompanyEventDetail(EventDetailTypeCode.PayrollCancellationReason,
                                    EnumUtils.getReadableName(pCancellationReasonCode));
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollCancellationScope,
                                    EnumUtils.getReadableName(pCancellationScopeCode));
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRun.getId().toString());

        addCompanyServiceIdEventDetail(event, pPayrollRun);

        // check for email eligibility
        // (only want email for the following cancellation reason code)
        if (pCancellationReasonCode == CancellationReasonCode.CompanyOnHold) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createPayrollTaxPaymentVoidedEvent(
            PayrollRun pPayrollRun, SpcfDecimal amount) {

        CompanyEvent event = createPayrollRunEvent(pPayrollRun.getCompany(),
                                                   pPayrollRun.getSourcePayRunId(),
                                                   pPayrollRun.getId(),
                                                   EventTypeCode.PayrollTaxPaymentVoided);

        event.addCompanyEventDetail(EventDetailTypeCode.Amount,
                                    amount.toString());

        return event;
    }

    public static CompanyEvent createPayrollTaxPaymentReissuedEvent(
            PayrollRun pPayrollRun, SpcfDecimal amount) {

        CompanyEvent event = createPayrollRunEvent(pPayrollRun.getCompany(),
                                                   pPayrollRun.getSourcePayRunId(),
                                                   pPayrollRun.getId(),
                                                   EventTypeCode.PayrollTaxPaymentReissued);

        event.addCompanyEventDetail(EventDetailTypeCode.Amount,
                                    amount.toString());

        return event;
    }

    public static CompanyEvent createERPayableAppliedToBalanceDueEvent(
            PayrollRun pPayrollRun, SpcfDecimal amount) {

        CompanyEvent event = createPayrollRunEvent(pPayrollRun.getCompany(),
                                                   pPayrollRun.getSourcePayRunId(),
                                                   pPayrollRun.getId(),
                                                   EventTypeCode.ERPayableAppliedToBalanceDue);

        event.addCompanyEventDetail(EventDetailTypeCode.Amount,
                                    amount.toString());

        return event;
    }

    public static CompanyEvent createBackdatePriorToProcessingStartEvent(PayrollRun pPayrollRun) {
        return createPayrollRunEvent(pPayrollRun.getCompany(), pPayrollRun.getSourcePayRunId(), pPayrollRun.getId(), EventTypeCode.BackdatePriorToProcessingStart);
    }

    public static CompanyEvent createPayrollSubmittedWithPendingNOC(
            Company pCompany, String pSourcePayrollRunId,
            SpcfUniqueId pEmployeeBankAccountId, String pSourceBankAccountId) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.PayrollSubmittedWithPendingNOC);

        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pSourcePayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId,
                                    pEmployeeBankAccountId.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.SourceBankAccountId,
                                    pSourceBankAccountId);

        return Application.save(event);
    }

    public static CompanyEvent createPayrollSubmittedWithEmployeeWithPendingReturn(
            Company pCompany, String pSourcePayrollRunId,
            SpcfUniqueId pEmployeeBankAccountId) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.PayrollSubmittedWithEmployeeWithPendingReturn);

        event.addCompanyEventDetail(EventDetailTypeCode.SourcePayrollRunId,
                                    pSourcePayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeBankAccountId,
                                    pEmployeeBankAccountId.toString());

        return Application.save(event);
    }

    public static CompanyEvent createCoaAccountChangeEvent(Company pCompany,
                                                           EventTypeCode pEventTypeCode, String pOldValue, String pNewValue,
                                                           boolean pByAgent) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);

        event.addCompanyEventDetail(EventDetailTypeCode.OldCoaName, pOldValue);
        event.addCompanyEventDetail(EventDetailTypeCode.NewCoaName, pNewValue);
        event.addCompanyEventDetail(EventDetailTypeCode.CoaNameChangeByAgent,
                                    Boolean.toString(pByAgent));

        return Application.save(event);
    }

    public static CompanyEvent createNSFReturnEvent(Company pCompany,
                                                    FinancialTransaction pFinTxn, String pAchReturnCode,
                                                    NSFSubTypeType pNsfSubType) {
        CompanyEvent event = createNSFReturnEventNoFinTxnAssoc(pCompany,
                                                               pAchReturnCode, pNsfSubType);

        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId,
                                    pFinTxn.getId().toString());

        addCompanyServiceIdEventDetail(event, pFinTxn);

        // check for email eligibility
        // (only want email for the following nsf sub-types, based on source system)

        switch (pNsfSubType) {
            case NSFAutoRedebit:
                queueEmail(event, SourceSystemCode.QBDT);
                break;

            case SecondNSF:
                queueEmail(event, SourceSystemCode.QBOE, SourceSystemCode.QBDT);
                break;
        }

        return Application.save(event);
    }

    public static CompanyEvent createAccountingFinancialLedgerAdjustmentEvent(
            Company pCompany, String pPayrollRunId,
            TransactionTypeCode pTransactionTypeCode, SpcfMoney pTransactionAmount, String pNote) {
        CompanyEvent event = createCompanyEvent(pCompany,
                                                EventTypeCode.AccountingFinancialLedgerAdjustmentCreated);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId,
                                    pPayrollRunId);
        event.addCompanyEventDetail(EventDetailTypeCode.TransactionType, pTransactionTypeCode.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.UserId, Application.getCurrentPrincipal().getId());
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNote);
        event.addCompanyEventDetail(EventDetailTypeCode.Amount, pTransactionAmount.toString());

        return Application.save(event);
    }

    public static CompanyEvent createPSIDMismatchEvent(Company pCompany, String pRequestPSID, String pErrorCode, String pErrorMessage) {

        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.PSIDMismatch);

        // set base event properties
        event.setStatusEffectiveDate(PSPDate.getPSPTime());
        event.setStatusCd(CompanyEventStatus.Inactive);

        //set event details
        event.addCompanyEventDetail(EventDetailTypeCode.ErrorCode, pErrorCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ErrorMessage, pErrorMessage);

        return Application.save(event);
    }

    public static boolean isInterestingEvent(SourceSystemCode pSourceSystemCd,
                                             EventTypeCode pEventTypeCode) {
        SourceSystem sourceSystem = Application.findById(SourceSystem.class,
                                                         pSourceSystemCd);

        for (EventType interestingEventType : sourceSystem.getInterestingEventTypesCollection()) {
            if (interestingEventType.getEventTypeCd().equals(pEventTypeCode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method is used to queue up company events for email creation at the end of the current database transaction
     * (just prior to transaction commit.)
     */


    private static void queueEmail(CompanyEvent pEvent, SourceSystemCode... pValidSourceSystems) {
        Company company = pEvent.getCompany();

        // QBPP018347: suppress CloudV2 emails
        CompanyService cs = EmailUtils.getCompanyService(pEvent);
        if (cs != null && cs.getService().getServiceCd() == ServiceCode.CloudV2) {
            return;
        }

        // Assisted customers do not receive certain events/emails.
        boolean companyIsOnTaxService = company.isCompanyOnService(ServiceCode.Tax);
        if (companyIsOnTaxService && !company.isMigratingToAssisted()) {
            // Assisted customers with BillPayment service (DD4VA) do receive some additional emails that Assisted customers do not.
            boolean companyIsOnBillPaymentService = company.isCompanyOnService(ServiceCode.BillPayment);
            if (pEvent.getEventTypeCd().notIn(includeEventsForAssisted) &&
                    (!companyIsOnBillPaymentService || pEvent.getEventTypeCd().notIn(includeEventsForDD4VA))) {
                return;
            }
            
			boolean emailEnabled = SystemParameter
					.findBooleanValue(SystemParameter.Code.ENABLE_BA_CHANGE_EMAIL_NOTIFICATION, false);
			if (!emailEnabled && (pEvent.getEventTypeCd() == EventTypeCode.EmployeeBankAccountChange)) {
				return;
			}
        }        

        // If no source system is specified don't check the list
        List<SourceSystemCode> validSystems = null;
        if (pValidSourceSystems != null) {
            validSystems = Arrays.asList(pValidSourceSystems);
        }

        SourceSystemCode ssc = company.getSourceSystemCd();
        if (validSystems == null || validSystems.contains(ssc)) {
            pEvent = Application.save(pEvent);
            EmailTransactionObserver eto = Application.getTransactionObserver(EmailTransactionObserver.class.getName());

            if (eto == null) {
                eto = new EmailTransactionObserver();
                Application.registerTransactionObserver(eto);
            }
         //   logger.info("Event Type code : " + pEvent.getEventTypeCd());

            eto.queueEvent(pEvent);
        }
    }

    public static CompanyEvent createServiceStatusChangeEvent(
            CompanyService pCompanyService,
            ServiceSubStatusCode pOldDDServiceSubStatusCd,
            ServiceSubStatusCode pNewDDServiceSubStatusCd,
            SpcfCalendar pEffectiveDate) {
        Collection<ServiceSubStatusCode> currentOnHoldReasonCodes = pCompanyService.getCompany()
                                                                                   .getCurrentOnHoldReasonCodes();

        CompanyEvent event = createCompanyEvent(pCompanyService.getCompany(),
                                                EventTypeCode.ServiceStatusChange);

        // set base event properties
        event.setStatusEffectiveDate(pEffectiveDate);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.OldServiceStatus,
                                    EnumUtils.getReadableName(pOldDDServiceSubStatusCd));
        event.addCompanyEventDetail(EventDetailTypeCode.NewServiceStatus,
                                    EnumUtils.getReadableName(pNewDDServiceSubStatusCd));
        event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode,
                                    EnumUtils.getReadableName(pCompanyService.getService().getServiceCd()));

        for (ServiceSubStatusCode oldOnHoldReasonCd : currentOnHoldReasonCodes) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldOnHoldReason,
                                        EnumUtils.getReadableName(oldOnHoldReasonCd));
        }

        for (ServiceSubStatusCode newOnHoldReasonCd : currentOnHoldReasonCodes) {
            event.addCompanyEventDetail(EventDetailTypeCode.NewOnHoldReason,
                                        EnumUtils.getReadableName(newOnHoldReasonCd));
        }

        addCompanyServiceIdEventDetail(event, pCompanyService.getService().getServiceCd());

        // check for email eligibility
        // (only want email for cancels and only if not cancelling cloud/WorkersComp)
        if (pNewDDServiceSubStatusCd == ServiceSubStatusCode.Cancelled && (pCompanyService.getService().getServiceCd().notIn(ServiceCode.Cloud, ServiceCode.CloudV2, ServiceCode.ViewMyPaycheck, ServiceCode.WorkersComp )) ) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return Application.save(event);
    }

    public static CompanyEvent createDepositFrequencyChangedEvent(Company pCompany, SpcfCalendar pEffectiveDate, String pPaymentTemplateCd, DepositFrequencyCode pPaymentFrequencyId) {
        PspPrincipal principal = Application.getCurrentPrincipal();
        AuthUser foundUser = AuthUser.findUser(principal.getId());

        CompanyEvent event = CompanyEvent.createCompanyEvent(pCompany, EventTypeCode.DepositFrequencyChanged);
        event.addCompanyEventDetail(EventDetailTypeCode.NewEffectiveDate, pEffectiveDate.toString());


        event.addCompanyEventDetail(EventDetailTypeCode.NewDepositFrequency, pPaymentFrequencyId.toString());


        if (foundUser != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.UserId, foundUser.getCorpId());
        }

        event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, pPaymentTemplateCd);

        return Application.save(event);
    }

    public static CompanyEvent createAIDUpdatedEvent(Company company, String name, String agencyId, String oldValue, String newValue) {
        CompanyEvent event = createCompanyEvent(company, EventTypeCode.AIDUpdated);
        event.addCompanyEventDetail(EventDetailTypeCode.AgencyId, agencyId);
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, oldValue);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, newValue);
        event.addCompanyEventDetail(EventDetailTypeCode.UniqueIdentifier, name);

        return event;
    }

    public String getCompanyEventDetailValue(
            EventDetailTypeCode pEventDetailTypeCode) {
        for (CompanyEventDetail eventDetail : getCompanyEventDetailCollection()) {
            if (eventDetail.getEventDetailTypeCd() == pEventDetailTypeCode) {
                return eventDetail.getValue();
            }
        }

        return null;
    }

    /*
     * Returns collection of detail values for the same code.
     *
     * @param pEventDetailTypeCode
     * @return
     */
    public Collection<String> getCompanyEventDetailValues(
            EventDetailTypeCode... pEventDetailTypeCodes) {
        DomainEntitySet<CompanyEventDetail> eventDetails = Application.find(CompanyEventDetail.class,
                                                                            CompanyEventDetail.CompanyEvent().equalTo(this)
                                                                                              .And(CompanyEventDetail.Company().equalTo(this.getCompany()))
                                                                                              .And(CompanyEventDetail.EventDetailTypeCd()
                                                                                                                     .in(pEventDetailTypeCodes)));

        Collection<String> eventDetailValues = new ArrayList<String>();

        for (CompanyEventDetail eventDetail : eventDetails) {
            eventDetailValues.add(eventDetail.getValue());
        }

        return eventDetailValues;
    }

    /*
     * Create a CompanyEventDetail and to attach it to the CompanyEvent.
     *
     * @param pEventDetailTypeCd
     * @param pValue
     * @return
     */
    public CompanyEventDetail addCompanyEventDetail(
            final EventDetailTypeCode pEventDetailTypeCd,
            final String pValue) {
        return addCompanyEventDetail(pEventDetailTypeCd, pValue, null);
    }

    public CompanyEventDetail addCompanyEventDetail(EventDetailTypeCode pEventDetailTypeCd, String pValue, String pEventDetailSubtype) {
        CompanyEventDetail eventDetail = new CompanyEventDetail();

        eventDetail.setEventDetailTypeCd(pEventDetailTypeCd);
        eventDetail.setEventDetailSubtype(pEventDetailSubtype);
        if (pValue != null) {
            eventDetail.setValue(pValue.length() >= 4000 ? pValue.substring(0, 3999) : pValue);
        }
        eventDetail.setCompanyEvent(this);
        eventDetail.setCompany(this.getCompany());

        eventDetail = Application.save(eventDetail);

        addCompanyEventDetail(eventDetail);

        return eventDetail;
    }

    public HashMap<EventDetailTypeCode, String> getEventDetailInfo() {
        HashMap<EventDetailTypeCode, String> map = new HashMap<EventDetailTypeCode, String>();

        for (CompanyEventDetail detail : getCompanyEventDetailCollection()) {
            map.put(detail.getEventDetailTypeCd(), detail.getValue());
        }

        return map;
    }

    public CompanyEventDetail addGenericEventDetail(
            String pEventDetailSubtype, String pValue) {
        CompanyEventDetail detail = new CompanyEventDetail();
        detail.setCompanyEvent(this);
        detail.setEventDetailTypeCd(EventDetailTypeCode.GenericEventDetail);
        detail.setEventDetailSubtype(pEventDetailSubtype);
        detail.setValue(pValue);
        detail.setCompany(this.getCompany());
        detail = Application.save(detail);

        addCompanyEventDetail(detail);

        return detail;
    }

    public DomainEntitySet<CompanyEventDetail> getCompanyEventDetails(
            EventDetailTypeCode pEventDetailTypeCode) {

        return Application.find(CompanyEventDetail.class,
                                CompanyEventDetail.CompanyEvent().equalTo(this)
                                                  .And(CompanyEventDetail.Company().equalTo(this.getCompany()))
                                                  .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode)));
    }

    public DomainEntitySet<CompanyEventDetail> getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode pEventDetailTypeCode) {
        return getCompanyEventDetailCollection().find(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode));
    }

    public static CompanyEvent createVoidedPaychecksAlreadyOffloadedEvent(Company pCompany, List<Paycheck> pAlreadyOffloadedPaychecks) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.VoidedPaycheckAlreadyOffloadedToTOK);

        if (pAlreadyOffloadedPaychecks != null && pAlreadyOffloadedPaychecks.size() > 0) {
            Paycheck firstPaycheck = pAlreadyOffloadedPaychecks.get(0);
            PayrollRun payrollRun = firstPaycheck.getPayrollRun();
            event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, payrollRun.getId().toString());
        }

        event.addCompanyEventDetail(EventDetailTypeCode.OverrideRecipientEmailAddress, tokEmail);

        for (Paycheck currentPaycheck : pAlreadyOffloadedPaychecks) {
            event.addCompanyEventDetail(EventDetailTypeCode.PaycheckId, currentPaycheck.getId().toString());
        }

        queueEmail(event, null);

        return Application.save(event);
    }

    public static CompanyEvent createDeletedPaycheckAlreadyOffloadedEvent(Company pCompany, Paycheck pOffloadedPaycheck) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.DeletedPaycheckAlreadyOffloadedToTOK);

        event.addCompanyEventDetail(EventDetailTypeCode.OverrideRecipientEmailAddress, tokEmail);
        event.addCompanyEventDetail(EventDetailTypeCode.PaycheckId, pOffloadedPaycheck.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pOffloadedPaycheck.getPayrollRun().getId().toString());
        queueEmail(event, null);

        return Application.save(event);
    }

    public static CompanyEvent tokNotifiedOfFraudHoldEvent(Company pCompany, ServiceSubStatusCode pServiceSubStatus) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.TOKNotifiedOfCompanyFraud);

        event.addCompanyEventDetail(EventDetailTypeCode.OverrideRecipientEmailAddress, tokEmail);
        event.addCompanyEventDetail(EventDetailTypeCode.ServiceStatus, pServiceSubStatus.toString());
        queueEmail(event, null);

        return Application.save(event);
    }

    public static CompanyEvent createStateIdModifiedEvent(Company company, String paymentTemplateCd, String oldId, String newId) {
        CompanyEvent event = createCompanyEvent(company,
                                                EventTypeCode.StateIdModified);
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, paymentTemplateCd);
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue, oldId);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, newId);
        return Application.save(event);
    }

    public static void createPreOffload401kValidationEvent(Company pCompany, SpcfCalendar pOffloadDate, ArrayList<String> pEventIds) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.PreOffload401kValidationAlert);

        event.addCompanyEventDetail(EventDetailTypeCode.OffloadDate, pOffloadDate.toString());

        for (String eventIds : pEventIds) {
            event.addCompanyEventDetail(EventDetailTypeCode.CompanyEventId, eventIds);
        }

        queueEmail(event, null);

        Application.save(event);
    }

    public static void createPostOffload401kValidationEvent(Company pCompany, SpcfCalendar pOffloadDate, ArrayList<String> pEventIds) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.PostOffload401kValidationAlert);

        event.addCompanyEventDetail(EventDetailTypeCode.OffloadDate, pOffloadDate.toString());

        for (String eventIds : pEventIds) {
            event.addCompanyEventDetail(EventDetailTypeCode.CompanyEventId, eventIds);
        }

        queueEmail(event, null);

        Application.save(event);
    }

    public static CompanyEvent createSUIAdjustmentEvent(PayrollRun pPayrollRun, EventTypeCode pEventTypeCode) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(), pEventTypeCode);

        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRun.getId().toString());

        queueEmail(event, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createMonthlyFeeCreatedEvent(PayrollRun pPayrollRun) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(), EventTypeCode.MonthlyFeeCreated);

        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRun.getId().toString());

        queueEmail(event, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent createInvalidVendorEmailEvent(PayrollRun pPayrollRun, List<String> invalidEmailList ) {
        CompanyEvent event = createCompanyEvent(pPayrollRun.getCompany(), EventTypeCode.InvalidVendorEmail);

        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pPayrollRun.getId().toString());

        StringBuilder vendorEmailAsString = new StringBuilder();
        for(String email:invalidEmailList) {
            vendorEmailAsString.append(email);
            vendorEmailAsString.append("<br>");
        }
        event.addCompanyEventDetail(EventDetailTypeCode.VendorInvalidEmail, invalidEmailList.toString());

        queueEmail(event, SourceSystemCode.QBDT);

        return Application.save(event);
    }

    public static CompanyEvent findOFXServiceActivatedEvent(CompanyService pCompanyService) {
        Criterion<CompanyEventDetail> criteria =
                CompanyEventDetail.CompanyEvent().Company().equalTo(pCompanyService.getCompany())
                                  .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.OFXServiceActivated))
                                  .And(CompanyEventDetail.CompanyEvent().StatusCd().equalTo(CompanyEventStatus.Active))
                                  .And(CompanyEventDetail.Company().equalTo(pCompanyService.getCompany()))
                                  .And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ServiceCode))
                                  .And(CompanyEventDetail.Value().equalTo(pCompanyService.getService().getServiceCd().name()));
        Expression<CompanyEventDetail> query =
                new Query<CompanyEventDetail>().Where(criteria).OrderBy(CreatedDate().Descending());
        DomainEntitySet<CompanyEventDetail> eventDetail = Application.find(CompanyEventDetail.class, query);

        CompanyEvent ofxServiceActivatedEvent = null;
        if (eventDetail.size() > 0) {
            ofxServiceActivatedEvent = eventDetail.get(0).getCompanyEvent();
        }
        return ofxServiceActivatedEvent;
    }

    public static CompanyEvent createOFXServiceActivatedEvent(CompanyService pCompanyService) {
        if (!pCompanyService.getService().getServiceCd().equals(ServiceCode.DirectDeposit) && !pCompanyService.getService().getServiceCd().equals(ServiceCode.Tax)) {
            throw new IllegalArgumentException("CompanyService being added must be DirectDeposit or Tax: " + pCompanyService.getService().getServiceCd().name());
        }

        CompanyEvent event = createCompanyEvent(pCompanyService.getCompany(), EventTypeCode.OFXServiceActivated);

        // set event details
        Company company = pCompanyService.getCompany();
        event.addCompanyEventDetail(EventDetailTypeCode.SourceCompanyId, company.getSourceCompanyId());
        event.addCompanyEventDetail(EventDetailTypeCode.ServiceCode, pCompanyService.getService().getServiceCd().name());
        event.addCompanyEventDetail(EventDetailTypeCode.OFXToken, Long.toString(company.getCurrentToken()));
        event.addCompanyEventDetail(EventDetailTypeCode.NextEmployeeId, company.getNextEmployeeId());
        event.addCompanyEventDetail(EventDetailTypeCode.NextPayrollTransactionId, company.getNextPayrollTransactionId());
        event.addCompanyEventDetail(EventDetailTypeCode.NextPaycheckId, company.getNextPaycheckId());
        event.addCompanyEventDetail(EventDetailTypeCode.NextPaylineTransactionId, company.getNextPayrollItemId());

        return Application.save(event);
    }

    public static CompanyEvent createEntitlementUnitAddedEvent(EntitlementUnit pEntitlementUnit, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode) {
        CompanyEvent event = createCompanyEvent(pEntitlementUnit.getCompany(), EventTypeCode.EntitlementUnitAdded);
        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId,
                                    pEntitlementUnit.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType,
                pEventEmailTemplateTypeCode.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.OverrideRecipientEmailAddress , pEntitlementUnit.getEntitlement().getContactEmail());
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createEntitlementUnitAddedEventAssisted(EntitlementUnit pEntitlementUnit, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode) {
        CompanyEvent event = createCompanyEvent(pEntitlementUnit.getCompany(), EventTypeCode.EntitlementUnitAdded);
        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId,
                                    pEntitlementUnit.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.ServiceKey,
                                    pEntitlementUnit.getServiceKey());
        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType,
                pEventEmailTemplateTypeCode.toString());
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createWelcomeEmailEvent(EntitlementUnit pEntitlementUnit, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode) {
        CompanyEvent event = createCompanyEvent(pEntitlementUnit.getCompany(), EventTypeCode.WelcomeEmail);

        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId,
                                    pEntitlementUnit.getId().toString());

        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType,
                                    pEventEmailTemplateTypeCode.toString());

        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createUsageBillingSubscriptionEvent(EntitlementUnit pEntitlementUnit, EventTypeCode pEventTypeCode) {
        CompanyEvent event = createCompanyEvent(pEntitlementUnit.getCompany(), pEventTypeCode);
        //set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId,
                                    pEntitlementUnit.getId().toString());
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }
    public static CompanyEvent createEntitlementUnitStatusChangedEvent(Company pCompany,
                                                                       EntitlementUnitStatusCode pOldStatus,
                                                                       EntitlementUnitStatusCode pNewStatus,
                                                                       String pEntitlementUnitId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.EntitlementUnitStatusChanged);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,
                                    pOldStatus != null ? pOldStatus.toString() : "");
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue,
                                    pNewStatus.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId,
                                    pEntitlementUnitId);

        return Application.save(event);
    }

    public static CompanyEvent createEntitlementStateChangedEvent(Company pCompany,
                                                                  EntitlementStateCode pOldStatus,
                                                                  EntitlementStateCode pNewStatus,
                                                                  String pEntitlementId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.EntitlementStateChanged);

        // set event specific properties
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,
                                    pOldStatus != null ? pOldStatus.toString() : "");
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue,
                                    pNewStatus.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementId,
                                    pEntitlementId);

        return Application.save(event);
    }

    public static CompanyEvent createEntitlementCodeChangedEvent(Company pCompany,
                                                                 EntitlementCode pOldEntitlementCode,
                                                                 EntitlementCode pNewEntitlementCode) {
        return createCompanyInfoChangeEvent(pCompany, pOldEntitlementCode.toString(), pNewEntitlementCode.toString(), EventTypeCode.EntitlementCodeChanged);
    }

    public static CompanyEvent createEntitlementErrorEvent(Company pCompany,
                                                           EventTypeCode pEventTypeCode,
                                                           String pErrorMessage,
                                                           Entitlement pEntitlement,
                                                           EntitlementUnit pEntitlementUnit) {
        CompanyEvent event = createCompanyEvent(pCompany, pEventTypeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ErrorMessage, pErrorMessage);
        if(pEntitlement != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.EntitlementId, pEntitlement.getId().toString());
        }
        if(pEntitlementUnit != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.EntitlementUnitId, pEntitlementUnit.getId().toString());
        }
        return Application.save(event);
    }

    public static CompanyEvent createPriceTypeChangedEvent(Company pCompany, String pOldPriceType, String pNewPriceType) {
        return createCompanyInfoChangeEvent(pCompany, pOldPriceType, pNewPriceType, EventTypeCode.PriceTypeChanged);
    }

    public static CompanyEvent createERPenaltiesAndInterestRefundCreatedEvent(Company pCompany, DomainEntitySet<FinancialTransaction> finTxns, SpcfMoney pPenaltiesAmount, SpcfMoney pInterestAmount, SpcfMoney pTotalAmount, String pNote) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ERPenaltiesAndInterestRefundCreated);
        for (FinancialTransaction finTxn : finTxns) {
            event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId, finTxn.getId().toString());
        }
        event.addCompanyEventDetail(EventDetailTypeCode.UserId, Application.getCurrentPrincipal().getId());
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNote);
        event.addCompanyEventDetail(EventDetailTypeCode.PenaltiesRefundAmount, pPenaltiesAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.InterestRefundAmount, pInterestAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.TotalRefundAmount, pTotalAmount.toString());
        return event;
    }

    public static CompanyEvent createERPenaltiesAndInterestRefundDebitCreatedEvent(Company pCompany, FinancialTransaction finTxn, String pNote) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.ERPenaltiesAndInterestRefundDebitCreated);
        event.addCompanyEventDetail(EventDetailTypeCode.FinancialTransactionId, finTxn.getId().toString());

        event.addCompanyEventDetail(EventDetailTypeCode.UserId, Application.getCurrentPrincipal().getId());
        event.addCompanyEventDetail(EventDetailTypeCode.NoteText, pNote);
        event.addCompanyEventDetail(EventDetailTypeCode.RefundDebitAmount, finTxn.getFinancialTransactionAmount().toString());
        return event;
    }

    public static CompanyEvent createCreditReductionEvent(Company pCompany, PayrollRun pCreatedPayrollRun, SpcfMoney pAmount, Law law, SpcfCalendar pPaycheckDate) {
        CompanyEvent event = createPayrollRunEvent(pCompany, null, pCreatedPayrollRun.getId(), EventTypeCode.CreditReduction);
        event.addCompanyEventDetail(EventDetailTypeCode.Amount, pAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.Law, law.getLawTypeCd());
        event.addCompanyEventDetail(EventDetailTypeCode.PaycheckDate, pPaycheckDate.toString());
        boolean impoundCreated = pCreatedPayrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployerTaxDebit)
                                                   .find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO))
                                                   .size() > 0;
        if (impoundCreated) {
            queueEmail(event, SourceSystemCode.QBDT);
        }
        return event;
    }

    //creates AutoEnable VMP Event with detail PSID, PersonaID, SourceName in tables PSP_COMPANY_EVENT and PSP_COMPANY_EVENT_DETAIL
    public static CompanyEvent createAutoEnableVMPEvent(Company pCompany, String authId, String source) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.AutoEnabledVMP);
        event.addCompanyEventDetail(EventDetailTypeCode.SourceCompanyId, pCompany.getSourceCompanyId());
        event.addCompanyEventDetail(EventDetailTypeCode.AuthId, authId);
        event.addCompanyEventDetail(EventDetailTypeCode.Description, source);
        return Application.save(event);
    }

    public static CompanyEvent createEmployeeInvitedEvent(Company pCompany, String employeeId, String invitationSource, String emailTemplate, String invitationId, String personaId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.EmployeeInvited);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employeeId);
        event.addCompanyEventDetail(EventDetailTypeCode.InvitationSource, invitationSource);
        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplate, emailTemplate);
        event.addCompanyEventDetail(EventDetailTypeCode.IUSInvitationId, invitationId);
        event.addCompanyEventDetail(EventDetailTypeCode.PersonaId, personaId);
        return Application.save(event);
    }

    public static CompanyEvent createUnsyncedEmployeeInviteEvent(Company pCompany, String employeeRecNums) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.UnsyncedEmployeeInvite);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employeeRecNums);
        //we are using details as retry count to avoid EA changes
        event.addCompanyEventDetail(EventDetailTypeCode.Details, "0");
        return Application.save(event);
    }

    public static CompanyEvent createEmployeeSignedUpEvent(Company pCompany, String employeeId) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.EmployeeSignedUp);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employeeId);
        return Application.save(event);
    }

    public static CompanyEvent createUpdateConsumerRealmIdEvent(Employee employee, String authId, String oldCFR, String newCFR) {
        CompanyEvent event = createCompanyEvent(employee.getCompany(), EventTypeCode.UpdateConsumerRealmId);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employee.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.AuthId,authId);
        event.addCompanyEventDetail(EventDetailTypeCode.PersonaId,employee.getPersonaId());
        event.addCompanyEventDetail(EventDetailTypeCode.OldStringValue,oldCFR);
        event.addCompanyEventDetail(EventDetailTypeCode.NewStringValue, newCFR);
        return Application.save(event);
    }

    public static CompanyEvent createBankAccountWalletEvent(Company company, EventTypeCode eventTypeCode, String oldWalletId,
                                                            String newWalletId,  EventDetailTypeCode parentIdEvent, String parentId) {
        CompanyEvent event = createCompanyEvent(company, eventTypeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.OldWalletId, oldWalletId);
        if(Objects.nonNull(newWalletId)) {
            event.addCompanyEventDetail(EventDetailTypeCode.NewWalletId, newWalletId);
        }
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyRealmId, company.getIAMRealmId());
        event.addCompanyEventDetail(parentIdEvent, parentId);
        return Application.save(event);
    }

    public static CompanyEvent createCloneWalletOnRealmChangeEvent(Company company, EventTypeCode eventTypeCode, String newWalletId,
                                                                   String oldWalletId, String newRealmId, String oldRealmId,
                                                                   EventDetailTypeCode parentIdEvent, String parentId) {
        CompanyEvent event = createCompanyEvent(company, eventTypeCode);
        event.addCompanyEventDetail(EventDetailTypeCode.OldWalletId, oldWalletId);
        if(Objects.nonNull(newWalletId)) {
            event.addCompanyEventDetail(EventDetailTypeCode.NewWalletId, newWalletId);
        }
        event.addCompanyEventDetail(EventDetailTypeCode.OldCompanyRealmId, oldRealmId);
        event.addCompanyEventDetail(EventDetailTypeCode.CompanyRealmId, newRealmId);
        event.addCompanyEventDetail(parentIdEvent, parentId);
        return Application.save(event);
    }

    public static CompanyEvent createVmpSignUpEmployeeEmailEvent(Employee employee, String recipientEmailAddress) {
        CompanyEvent event = createCompanyEvent(employee.getCompany(), EventTypeCode.VmpSignUpEmployeeEmail);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employee.getId().toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RecipientEmailAddress, recipientEmailAddress);
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createVmpSignUpEmployerEmailEvent(Employee employee) {
        CompanyEvent event = createCompanyEvent(employee.getCompany(), EventTypeCode.VmpSignUpEmployerEmail);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employee.getId().toString());
        CompanyEvent.addCompanyServiceIdEventDetail(event);
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    public static CompanyEvent createPaystubCreatedEvent(Employee employee, Paystub paystub) {
        CompanyEvent event = createCompanyEvent(employee.getCompany(), EventTypeCode.PaystubCreated);
        event.addCompanyEventDetail(EventDetailTypeCode.EmployeeId, employee.getId().toString());

        //PSP-7097: Research on using email parameter PaycheckSettlementDate
        event.addCompanyEventDetail(EventDetailTypeCode.PaycheckDate, EmailUtils.formatDate(paystub.getPaycheckDate().toLocal()).toString());

        //Create calendar and set it to a week ago
        SpcfCalendar oneWeekAgo = PSPDate.getPSPTime();
        oneWeekAgo.addDays(-7);
        //Don't want to send emails for paystubs being sent more than a week ago, or if employee has not signed up for VMP yet (consumer realm is not there)
        if(paystub.getPaycheckDate().after(oneWeekAgo) && employee.getConsumerRealmId() != null) {
            //Unless employee has paystub notifications turned off create email event
            boolean sendEmail = true;
            List<PstubEmployeePreference> employeePreferences = PstubEmployeePreference.getEmployeePreferencesByApp(employee, PstubEmployeePreference.VMP);
            if(employeePreferences != null) {
                for(PstubEmployeePreference employeePreference : employeePreferences) {
                    if(PstubEmployeePreference.PAYSTUB_NOTIFICATION.equalsIgnoreCase(employeePreference.getPreferenceName())) {
                        if(PstubEmployeePreference.OFF.equalsIgnoreCase(employeePreference.getPreferenceValue())) {
                            sendEmail = false;
                            break;
                        }
                    }
                }
            }

            if(sendEmail) {
                queueEmail(event, SourceSystemCode.QBDT);
            }
        }

        return Application.save(event);
    }

    public static CompanyEvent createSendEmailFailedEvent(Company pCompany, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode, String pRecipientEmail, String pErrorCode, String pErrorMessage) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.SendEmailFailed);

        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType, pEventEmailTemplateTypeCode.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RecipientEmailAddress, pRecipientEmail);
        event.addCompanyEventDetail(EventDetailTypeCode.ErrorCode, pErrorCode);
        event.addCompanyEventDetail(EventDetailTypeCode.ErrorMessage, pErrorMessage);
        return Application.save(event);
    }

    public static CompanyEvent createSendEmailSkippedEvent(Company pCompany, EventEmailTemplateTypeCode pEventEmailTemplateTypeCode, String pRecipientEmail) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.SendEmailSkipped);

        event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType, pEventEmailTemplateTypeCode.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RecipientEmailAddress, pRecipientEmail);
        return Application.save(event);
    }

    public static CompanyEvent createSUICreditsAppliedEvent(Company pCompany, Law pLaw, int pYear, int pQuarter, SpcfMoney pCreditAmount, SpcfMoney pAppliedAmount, PayrollRun pCreditPayrollRun) {
        CompanyEvent event = createCompanyEvent(pCompany, EventTypeCode.SUICreditsApplied);

        event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, pLaw.getPaymentTemplate().getPaymentTemplateCd());
        event.addCompanyEventDetail(EventDetailTypeCode.Law, pLaw.getLawId());
        event.addCompanyEventDetail(EventDetailTypeCode.NewDate, String.format("%s Q%s", pYear, pQuarter));
        event.addCompanyEventDetail(EventDetailTypeCode.Amount, pCreditAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.RefundAmount, pAppliedAmount.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.PayrollRunId, pCreditPayrollRun.getId().toString());

        if (pAppliedAmount.isGreaterThan(SpcfMoney.ZERO)) {
            queueEmail(event, SourceSystemCode.QBDT);
        }

        return event;
    }

    public static void createSMSSyncEvent(String realmid, Company company, com.intuit.sbd.payroll.psp.domain.EventTypeCode eventTypeCode, final Optional<String> failureReason) {
        if (company == null) {
            logger.error("No active company found to write this event " + realmid);
            return;
        }
        if (realmid == null) {
            logger.error("realmid is null, can not raise event");
            return;
        }
        if (eventTypeCode == null) {
            logger.error("Event type code can not be null");
            return;
        }
        final CompanyEvent event = CompanyEvent.createCompanyEvent(company, eventTypeCode);
        failureReason.ifPresent((reason) -> event.addCompanyEventDetail(com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode.ReasonDescription, reason));

        Application.save(event);

    }

    public static void createSMSMigratedEvent(com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus smsMigrationStatus, Company company) {
        if (smsMigrationStatus != com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus.MigrationComplete) {
            return;
        }
        addSMSEvent(company, EventTypeCode.PSPToSMSMigration, SMS_MIGRATED_MSG);
    }

    public static void createSMSMigrateRevertedEvent(Company company) {
        addSMSEvent(company, EventTypeCode.PSPToSMSMigrationRevert, SMS_MIGRATE_REVERTED_MSG);
    }

    public static CompanyEvent createLegacySubscriptionMigrationEvent(Company company, String subscriptionType, String billingFrequency, String daysTillRenewal, String basePrice, String emailAddress, String license, String eoc) {
        CompanyEvent event = CompanyEvent.createCompanyEvent(company, EventTypeCode.LegacySubscriptionMigration);
        event.addCompanyEventDetail(EventDetailTypeCode.BillingFrequencyType, billingFrequency);
        event.addCompanyEventDetail(EventDetailTypeCode.BaseRate, basePrice);
        EventDetailTypeCode daysTillRenewalEventDetailTypeCode = EventDetailTypeCode.DaysTillRenewal;
        event.addCompanyEventDetail(daysTillRenewalEventDetailTypeCode, daysTillRenewal);
        event.addCompanyEventDetail(EventDetailTypeCode.OverrideRecipientEmailAddress, emailAddress);
        event.addCompanyEventDetail(EventDetailTypeCode.LicenseNumber, license);
        event.addCompanyEventDetail(EventDetailTypeCode.EntitlementOfferingCode, eoc);
        if(subscriptionType.equalsIgnoreCase(STANDARD)) {
            event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType, EventEmailTemplateTypeCode.LegacyStandardSubsMigration.toString());
        } else if(subscriptionType.equalsIgnoreCase(NONSTANDARD)) {
            event.addCompanyEventDetail(EventDetailTypeCode.EmailTemplateType, EventEmailTemplateTypeCode.LegacyNonStandardSubsMigration.toString());
        } else {
            throw new RuntimeException("Invalid email template specified. subscriptionType=" + subscriptionType);
        }
        String sourceCompanyId = company.getSourceCompanyId();
        event.addCompanyEventDetail(EventDetailTypeCode.SourceCompanyId, sourceCompanyId);
        logger.info("job=SendCustomEmailsProcessor, Method=createLegacySubscriptionMigrationEvent, Msg=CreatingL2SMigrationEvent, PSID=" + sourceCompanyId);
        queueEmail(event, SourceSystemCode.QBDT);
        return Application.save(event);
    }

    private static void addSMSEvent(Company company, EventTypeCode eventTypeCode, String description) {
        Map<EventDetailTypeCode, String> eventDetailsMap = new HashMap<>();
        CompanyEvent companyEvent = createCompanyEvent(company, eventTypeCode);
        eventDetailsMap.put(EventDetailTypeCode.Description, description);
        eventDetailsMap.put(EventDetailTypeCode.CompanySequence, company.getId().getStandardFormatString());
        eventDetailsMap.put(EventDetailTypeCode.CompanyRealmId, company.getIAMRealmId());

        eventDetailsMap.forEach((eventDetailTypeCode, value) ->
                companyEvent.addCompanyEventDetail(eventDetailTypeCode, value));
    }

    /**
     *
     * @return company event detail containing the memo if there is one (i.e. if it is a manual payroll/payment); null, otherwise
     */
    public static CompanyEventDetail getManualAdjustmentNote(Company company, EventDetailTypeCode lookupEventDetail, String lookupEventDetailValue) {
        DomainEntitySet<CompanyEventDetail> prIdDetails = Application.find(CompanyEventDetail.class,
                                                                           CompanyEventDetail.EventDetailTypeCd().equalTo(lookupEventDetail)
                                                                                             .And(CompanyEventDetail.Company().equalTo(company))
                                                                                             .And(CompanyEventDetail.Value().equalTo(lookupEventDetailValue))
                                                                                             .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.ManualLedgerEntry)));
        if (prIdDetails.size() > 0) {
            CompanyEvent ce = prIdDetails.get(0).getCompanyEvent();
            DomainEntitySet<CompanyEventDetail> noteDetails = ce.getCompanyEventDetails(EventDetailTypeCode.NoteText);
            if (noteDetails.size() > 0) {
                CompanyEventDetail noteDetail = noteDetails.get(0);
                if (StringUtils.isNotEmpty(noteDetail.getValue())) {
                    return noteDetail;
                }
            }
        }
        return null;
    }

    public static CompanyEventDetail getFullRefundNote(Company company, EventDetailTypeCode lookupEventDetail, String lookupEventDetailValue) {
        DomainEntitySet<CompanyEventDetail> prIdDetails = Application.find(CompanyEventDetail.class,
                CompanyEventDetail.EventDetailTypeCd().equalTo(lookupEventDetail)
                        .And(CompanyEventDetail.Company().equalTo(company))
                        .And(CompanyEventDetail.Value().equalTo(lookupEventDetailValue))
                        .And(CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.PendingPaymentRefunded)));
        if (prIdDetails.size() > 0) {
            CompanyEvent ce = prIdDetails.get(0).getCompanyEvent();
            DomainEntitySet<CompanyEventDetail> noteDetails = ce.getCompanyEventDetails(EventDetailTypeCode.NoteText);
            if (noteDetails.size() > 0) {
                CompanyEventDetail noteDetail = noteDetails.get(0);
                if (StringUtils.isNotEmpty(noteDetail.getValue())) {
                    return noteDetail;
                }
            }
        }
        return null;
    }

    public static boolean isServiceCodeEventDetailPresent(DomainEntitySet<CompanyEvent> pCompanyEvents, String serviceCdValue) {
        for (CompanyEvent companyEvent : pCompanyEvents) {
            DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetailCollection()
                                                                                  .find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ServiceCode)
                                                                                                          .And(CompanyEventDetail.Value().equalTo(serviceCdValue)));
            if(companyEventDetails.size() > 0){
                return true;
            }
        }
        return false;
    }

    public static boolean hasNoCompanyEventsWithServiceCodeDetails(DomainEntitySet<CompanyEvent> pCompanyEvents){
        for (CompanyEvent companyEvent : pCompanyEvents) {
            if(companyEvent.getCompanyEventDetails(EventDetailTypeCode.ServiceCode).size() == 0){
                return false;
            }
        }
        return true;
    }

    public static boolean hasActiveStrikeEventWithinLastYear(Company company) {
        SpcfCalendar eventTimeStampMax = PSPDate.getPSPTime();
        eventTimeStampMax.addMonths(-12);
        Long count = Application.executeScalarAggQuery(CompanyEvent.class,
                                                       new Query<CompanyEvent>()
                                                               .Select(CompanyEvent.Id().Count())
                                                               .Where(CompanyEvent.Company().equalTo(company)
                                                                                  .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.Strike))
                                                                                  .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active))
                                                                                  .And(CompanyEvent.EventTimeStamp().greaterThan(eventTimeStampMax))));

        return count > 0;
    }

    private static boolean isMailNeeded(Company company, EventTypeCode eventTypeCode) {

        logger.info("Company TRON Flag Value " + company.isMoneyMovementOnboardingEnabled());

        if (company.isMoneyMovementOnboardingEnabled()) {
            logger.info("Email is disabled for the workflow "+eventTypeCode.name() +" for PSID - "+ company.getSourceSystemCompanyId());
            return false;
        }
        return true;
    }
}
