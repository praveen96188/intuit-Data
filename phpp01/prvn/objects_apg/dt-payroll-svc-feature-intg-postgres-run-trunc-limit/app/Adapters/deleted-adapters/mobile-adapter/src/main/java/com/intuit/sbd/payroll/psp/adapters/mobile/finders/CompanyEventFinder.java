package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 @author Jeff Jones
 */

public class CompanyEventFinder {

    private static EventTypeCode[] returnableEvents;

    static {
        returnableEvents = new EventTypeCode[4];
        returnableEvents[0] = EventTypeCode.NOC;
        returnableEvents[1] = EventTypeCode.DDDebitReturn;
        returnableEvents[2] = EventTypeCode.DDReject;
        returnableEvents[3] = EventTypeCode.NSF;
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(Company pCompany, int pDays) {

        SpcfCalendar xDaysAgo = PSPDate.getPSPTime();
        xDaysAgo.addDays(pDays * -1);

        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                       .Where(CompanyEvent.Company().equalTo(pCompany)
                              .And(CompanyEvent.EventTypeCd().in(returnableEvents)
                              .And(CompanyEvent.CreatedDate().greaterOrEqualThan(xDaysAgo))))
                       .OrderBy(CompanyEvent.CreatedDate().Descending());
        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class, query);

        if (companyEvents.isEmpty()) {
            query = new Query<CompanyEvent>()
                           .Where(CompanyEvent.Company().equalTo(pCompany)
                                  .And(CompanyEvent.EventTypeCd().in(returnableEvents)))
                           .OrderBy(CompanyEvent.StatusEffectiveDate().Descending())
                           .LimitResults(0,1);
            companyEvents = Application.find(CompanyEvent.class, query);
        }

        return companyEvents;
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEvents(Company pCompany, int pStart, int pSize) {
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                       .Where(CompanyEvent.Company().equalTo(pCompany)
                              .And(CompanyEvent.EventTypeCd().in(returnableEvents)))
                       .OrderBy(CompanyEvent.CreatedDate().Descending())
                       .LimitResults(pStart, pSize);
        return Application.find(CompanyEvent.class, query);
    }

    public static CompanyEvent findCompanyEvent(String pId) {
        return Application.findById(CompanyEvent.class, SpcfUniqueId.createInstance(pId));
    }

    public static DomainEntitySet<CompanyEvent> findCompanyEventsForPayeeOrEmployee(Company pCompany) {
        SpcfCalendar xDaysAgo = PSPDate.getPSPTime();
        xDaysAgo.addDays(-15);

        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                       .Where(CompanyEvent.Company().equalTo(pCompany)
                              .And(CompanyEvent.EventTypeCd().in(EventTypeCode.NOC, EventTypeCode.ACHReturn, EventTypeCode.InvalidEmployeeInformation)
                              .And(CompanyEvent.CreatedDate().greaterOrEqualThan(xDaysAgo))))
                       .OrderBy(CompanyEvent.CreatedDate().Descending());

        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class, query);

        for (CompanyEvent companyEvent : companyEvents) {

        }
        return null;
    }

    public static Collection<CompanyEvent> findCompanyEvents(Company pCompany,
            CompanyEventStatus pStatus, EventDetailTypeCode pEventDetailTypeCode,
            String pSpcfUniqueIds[]) {

        Map<String, CompanyEvent> companyEventMap = new HashMap<String, CompanyEvent>();

        Criterion<CompanyEventDetail> where = CompanyEventDetail.Company().equalTo(pCompany)
                        .And(CompanyEventDetail.EventDetailTypeCd().equalTo(pEventDetailTypeCode))
                        .And(CompanyEventDetail.Value().in(pSpcfUniqueIds))
                        .And(CompanyEventDetail.CompanyEvent().Company().equalTo(pCompany))
                        .And(CompanyEventDetail.CompanyEvent().EventTypeCd().in(returnableEvents))
                        .And(CompanyEventDetail.CompanyEvent().StatusCd().equalTo(pStatus));

        Expression<CompanyEventDetail> query = new Query<CompanyEventDetail>()
                .Where(where)
                .OrderBy(CompanyEventDetail.CompanyEvent().CreatedDate().Descending());

        DomainEntitySet<CompanyEventDetail> companyEventDetailSet = Application.find(CompanyEventDetail.class, query);
        for (CompanyEventDetail companyEventDetail : companyEventDetailSet) {
            CompanyEvent companyEvent = companyEventDetail.getCompanyEvent();
            if (!companyEventMap.containsKey(companyEventDetail.getId().toString())) {
                companyEventMap.put(companyEvent.getId().toString(), companyEvent);
            }
        }

        return companyEventMap.values();
    }
}
