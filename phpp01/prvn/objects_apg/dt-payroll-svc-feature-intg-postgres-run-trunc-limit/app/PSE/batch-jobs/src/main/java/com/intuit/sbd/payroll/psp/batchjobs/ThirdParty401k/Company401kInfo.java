package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.MessageDefinition;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * User: rnorian
 * Date: Feb 23, 2011
 * Time: 4:31:26 PM
 */
public class Company401kInfo {
    private boolean isNew401kCompany = false;
    HashMap<Employee, SpcfCalendar> lastEmployeeSendDate = new HashMap<Employee, SpcfCalendar>();

    public Company401kInfo(Company pCompany) {
        loadCompany401kEventHistory(pCompany);
    }

    private void loadCompany401kEventHistory(Company pCompany) {
        Expression<CompanyEvent> uploadEventCountQuery =
                new Query<CompanyEvent>()
                        .Select(CompanyEvent.EventTimeStamp().Count())
                        .Where( CompanyEvent.Company().equalTo(pCompany)
                                .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.Employee401kDataUploaded))
                                .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active)));

        long uploadEventCount = Application.executeScalarAggQuery(CompanyEvent.class, uploadEventCountQuery).longValue();
        isNew401kCompany = uploadEventCount == 0;
    }

    public boolean isNew401kCompany() {
        return isNew401kCompany;
    }

    public void loadEmployeeHistory(Employee pEmployee) {
        Expression<ThirdParty401kBatchEmployee> query =
                new Query<ThirdParty401kBatchEmployee>()
                        .Where( ThirdParty401kBatchEmployee.Employee().equalTo(pEmployee)
                                .And(ThirdParty401kBatchEmployee.ThirdParty401kBatch().UploadDate().isNotNull()))
                        .OrderBy(ThirdParty401kBatchEmployee.ThirdParty401kBatch().UploadDate().Descending())
                        .LimitResults(0,1);
        DomainEntitySet<ThirdParty401kBatchEmployee> history = Application.find(ThirdParty401kBatchEmployee.class, query);

        SpcfCalendar lastSendDate = null;
        if (!history.isEmpty()) {
            // technically, this is not the last 'send' date but want to get the date when EE was last reviewed
            lastSendDate = history.get(0).getCreatedDate();
        }
        lastEmployeeSendDate.put(pEmployee, lastSendDate);
    }

    public SpcfCalendar getLastSendDate(Employee pEmployee) {
        if (!lastEmployeeSendDate.containsKey(pEmployee)) {
            loadEmployeeHistory(pEmployee);
        }
        return lastEmployeeSendDate.get(pEmployee);
    }
}
