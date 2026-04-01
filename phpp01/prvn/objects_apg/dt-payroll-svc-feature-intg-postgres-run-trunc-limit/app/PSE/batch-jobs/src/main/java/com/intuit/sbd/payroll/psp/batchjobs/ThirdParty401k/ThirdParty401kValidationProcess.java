package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
    @author Jeff Jones
 */
public class ThirdParty401kValidationProcess {
    private static final SpcfLogger logger;

    private static final int PRE_VALIDATION_WINDOW  =  1;   //Days prior to 401k offload
    private static final int POST_VALIDATION_WINDOW = -2;   //Days after to 401k offload

    static {
        logger = Application.getLogger(ThirdParty401kValidationProcess.class);
    }

    public void validate401kData() throws Exception {
        logger.info("401k Data validation started.");

        try {
            Application.beginUnitOfWork();

            SpcfCalendar checkDate;
            SpcfCalendar offloadDate;
            DomainEntitySet<ThirdParty401kPaycheckPendingState> paychecksOnEmployeeHold;

            //Pre offload validation
            SpcfCalendar nextOffload = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(nextOffload, PRE_VALIDATION_WINDOW);
            CalendarUtils.clearTime(nextOffload);
            SpcfCalendar initiationDateStart = nextOffload.copy();
            SpcfCalendar initiationDateEnd = initiationDateStart.copy();
            initiationDateEnd.addDays(1);
            initiationDateEnd.addMilliseconds(-1);

            paychecksOnEmployeeHold = ThirdParty401kPaycheckPendingState.findOnHoldPaycheckByInitiationDate(initiationDateStart, initiationDateEnd);
            create401kEmailEvents(paychecksOnEmployeeHold, true);

            //Post offload validation
            SpcfCalendar twoBusinessDaysAgo = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(twoBusinessDaysAgo, POST_VALIDATION_WINDOW);
            CalendarUtils.clearTime(twoBusinessDaysAgo);
            initiationDateStart = twoBusinessDaysAgo;
            initiationDateEnd = initiationDateStart.copy();
            initiationDateEnd.addDays(1);
            initiationDateEnd.addMilliseconds(-1);

            paychecksOnEmployeeHold = ThirdParty401kPaycheckPendingState.findOnHoldPaycheckByInitiationDate(initiationDateStart, initiationDateEnd);
            create401kEmailEvents(paychecksOnEmployeeHold, false);

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        logger.info("401k Data validation completed.");
    }

    private void create401kEmailEvents(DomainEntitySet<ThirdParty401kPaycheckPendingState> pPaychecksOnEmployeeHold, boolean pPreOffload) {
        if (pPaychecksOnEmployeeHold.size() == 0) {
            return;
        }

        // All paychecks should have the same initiation date
        SpcfCalendar offloadDate = pPaychecksOnEmployeeHold.get(0).getInitiationDate().toLocal();
        Map<String, Map<String, ArrayList<String>>> companyMap =
                  new HashMap<String, Map<String, ArrayList<String>>>();


        for (ThirdParty401kPaycheckPendingState paycheckPendingState : pPaychecksOnEmployeeHold) {
            Paycheck paycheck = paycheckPendingState.getThirdParty401kPaycheck().getPaycheck();
            if (!PaycheckStatusCode.Active.equals(paycheck.getStatus())) {
                continue;
            }

            Employee employee = paycheck.getSourceEmployee();
            DomainEntitySet<CompanyEvent> invalidDataEvents =
                    CompanyEvent.findInvalidEmployeeInformationEvent(paycheck.getCompany(), employee.getSourceEmployeeId(),
                        EventTypeCode.InvalidEmployeeInformation);

            if (!invalidDataEvents.isEmpty()) {
                Map<String, ArrayList<String>> employeeMap;
                if (companyMap.containsKey(paycheck.getCompany().getId().toString())) {
                    employeeMap = companyMap.get(paycheck.getCompany().getId().toString());
                } else {
                   employeeMap = new HashMap<String, ArrayList<String>>();
                   companyMap.put(paycheck.getCompany().getId().toString(), employeeMap);
                }

                if (employeeMap.containsKey(employee.getId().toString())) {
                    ArrayList<String> invalidDataEventList = employeeMap.get(employee.getId().toString());
                    for (CompanyEvent invalidDataEvent : invalidDataEvents) {
                        if (!invalidDataEventList.contains(invalidDataEvent.getId().toString())) {
                            invalidDataEventList.add(invalidDataEvent.getId().toString());
                        }
                    }
                } else {
                    ArrayList<String> invalidDataEventList = new ArrayList<String>();
                    employeeMap.put(employee.getId().toString(), invalidDataEventList);
                    for (CompanyEvent invalidDataEvent : invalidDataEvents) {
                        invalidDataEventList.add(invalidDataEvent.getId().toString());
                    }
                }
            }
        }

        for (Map.Entry<String, Map<String, ArrayList<String>>> entry : companyMap.entrySet()) {
            Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(entry.getKey()));
            Map<String, ArrayList<String>> employeeMap = entry.getValue();

            ArrayList<String> eventList = new ArrayList<String>();
            for (ArrayList<String> invalidDataEventList : employeeMap.values()) {
                eventList.addAll(invalidDataEventList);
            }

            if (pPreOffload) {
                CompanyEvent.createPreOffload401kValidationEvent(company, offloadDate, eventList);
            } else {
                CompanyEvent.createPostOffload401kValidationEvent(company, offloadDate, eventList);
            }
        }
    }
}
