package com.intuit.sbd.payroll.psp.adapters.qbdtws.ui;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.MessageDefinition;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: rnorian
 * Date: Mar 17, 2010
 * Time: 1:30:32 PM
 */
public class TransmissionResponseServlet extends HttpServlet {
    private SpcfLogger logger = PayrollServices.getLogger(TransmissionResponseServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.warn("POST called.  Only GET supported.  Someone may be trying to access page external to QB? IP: " + request.getRemoteAddr());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String responseToken = request.getParameter("responseToken");

        if (responseToken == null) {
            logger.warn("transmission response page accessed w/out responseToken parameter (manual entry of URL?)");
            return;
        }

        // verify this is a valid GUID
        try {
            UUID.fromString(responseToken);
        } catch (Exception e) {
            showError(request, response, "Invalid session token.");
            logger.warn("Invalid session token received (" + responseToken + ").  IP: " + request.getRemoteAddr());
            return;
        }

        String companyId = null;
        try {
            // find transmission
            PayrollServices.beginUnitOfWorkWithSecondary();
            TransmissionResponse transmissionResponse = null;
            SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(responseToken);
            if (Objects.isNull(sourceSystemTransmission)) {
                showError(request, response, "Invalid session token.");
                logger.warn("Request for session that does not exist (" + responseToken + ").  IP: " + request.getRemoteAddr());
                return;
            }

            // verify token hasn't 'expired'
            int tokenExpirationMinutes = SystemParameter.findIntValue(SystemParameter.Code.K401_RESPONSE_EXPIRATION_MINUTES, 5);
            SpcfCalendar responseDateTime = sourceSystemTransmission.getModifiedDate().copy();
            responseDateTime.addMinutes(tokenExpirationMinutes);
            if (PSPDate.getPSPTime().after(responseDateTime)) {
                showError(request, response, "Session expired.");
                return;
            }

            companyId = sourceSystemTransmission.getCompany().getSourceCompanyId();
            transmissionResponse = createTransmissionResponse(sourceSystemTransmission.getCompany(), sourceSystemTransmission.getCreatedDate().toLocal(), sourceSystemTransmission.getTransmissionIdentifier());

            PayrollServices.commitUnitOfWorkWithSecondary();

            request.setAttribute("transmissionSummary", transmissionResponse);
            RequestDispatcher view = request.getRequestDispatcher("/payroll/TransmissionSummary.jspx");
            view.forward(request, response);
        } catch (Exception e) {
            String logMsg = String.format("failure trying to display QBDTWS transmissionResponse (transmissionid: %s   companyid: %s",
                                          responseToken, companyId);
            logger.error(logMsg, e);

            String errorMsg = "An unexpected error was encountered while trying to display the transmission response." +
                    "<br/><br/>Wait a few minutes and then resend your payroll transaction request. " +
                    "If you receive this message again, <a href='http://payroll.intuit.com/contact.jhtml'>contact a payroll service representative</a>.";
            showError(request, response, errorMsg);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher("/payroll/Error.jspx");
        request.setAttribute("errorMessage", errorMessage);
        view.forward(request, response);
    }

    public TransmissionResponse createTransmissionResponse(Company company, SpcfCalendar createdDate, String transmissionIdentifier) {

        final String dateFormat = "MM/dd/yy";
        TransmissionResponse transmissionResponse = new TransmissionResponse();
        transmissionResponse.setCompanyName(company.getLegalName());

        Date date = SpcfUtils.convertSpcfCalendarToDate(createdDate);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d 'at' hh:mm:ss z");
        transmissionResponse.setReceivedDate(dateFormatter.format(date));


        // get all the transmission events associated w/last transmission
        Expression<CompanyEvent> transmissionEventsQuery =
                new Query<CompanyEvent>().Where(CompanyEvent.Company().equalTo(company)
                        .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active))
                        .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.InvalidSourceSystemTransmissionInformation))
                        .And(CompanyEvent.SourceId().equalTo(transmissionIdentifier)))
                        .EagerLoad(CompanyEvent.CompanyEventDetailSet());

        DomainEntitySet<CompanyEvent> transmissionEvents = PayrollServices.entityFinder.find(CompanyEvent.class, transmissionEventsQuery);
        for (CompanyEvent transmissionEvent : transmissionEvents) {
            ValidationMessage validationMessage = new ValidationMessage();
            String status = transmissionEvent.getCompanyEventDetailValue(EventDetailTypeCode.MessageLevel);
            String message = transmissionEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourceSystemTransmissionInvalidReason);
            validationMessage.setStatus(status);
            validationMessage.setMessage(message);

            transmissionResponse.getTransmissionMessages().add(validationMessage);
        }

        // get all the non-expired paycheck events associated w/company
        Expression<CompanyEvent> paycheckEventsQuery =
                new Query<CompanyEvent>().Where(CompanyEvent.Company().equalTo(company)
                        .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active))
                        .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.InvalidPaycheckInformation)))
                        .OrderBy(CompanyEvent.EventTimeStamp().Descending())
                        .EagerLoad(CompanyEvent.CompanyEventDetailSet());
        DomainEntitySet<CompanyEvent> paycheckEvents = PayrollServices.entityFinder.find(CompanyEvent.class, paycheckEventsQuery);

        int msgAgeOutDays = SystemParameter.findIntValue(SystemParameter.Code.K401_ERROR_MSG_AGE_OUT_DAYS, 14);
        SpcfCalendar msgWindowStartDate = PSPDate.getPSPTime();
        msgWindowStartDate.addDays(msgAgeOutDays * -1);

        for (CompanyEvent paycheckEvent : paycheckEvents) {
            PaycheckValidationMessage paycheckValidationMessage = new PaycheckValidationMessage();

            String status = paycheckEvent.getCompanyEventDetailValue(EventDetailTypeCode.MessageLevel);
            String message = paycheckEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckInvalidReason);
            String employeeName = paycheckEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeName);
            String checkDate = paycheckEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate);
            String checkAmount = paycheckEvent.getCompanyEventDetailValue(EventDetailTypeCode.PaycheckAmount);

            paycheckValidationMessage.setCheckDate(checkDate == null ? "Unknown" : checkDate);
            paycheckValidationMessage.setNetAmount(checkAmount == null ? "Unknown" : checkAmount);
            paycheckValidationMessage.setEmployeeName(employeeName == null ? "Unknown" : employeeName);
            paycheckValidationMessage.setStatus(status);
            paycheckValidationMessage.setMessage(message);

            // expire events
            //   - expire info messages that do not require any user action after they have been viewed once
            //   - expire messages after an age out period
            if (MessageInfo.MessageLevel.valueOf(status) == MessageInfo.MessageLevel.INFO || paycheckEvent.getCreatedDate().before(msgWindowStartDate) ) {
                paycheckEvent.setStatusCd(CompanyEventStatus.Inactive);
            }

            // expire all 10087 events so they are only viewed once
            String msg = MessageDefinition.getMessageDefinition(10087).getMessageFormat();
            if (msg.equals(message)) {
                paycheckEvent.setStatusCd(CompanyEventStatus.Inactive);
            }

            if (paycheckEvent.getCreatedDate().after(msgWindowStartDate)) {
                transmissionResponse.getPaycheckMessages().add(paycheckValidationMessage);
            }
        }

        // get all the non-expired employee events associated w/company
        // TODO: should restrict returned events to check for 401K service code on event detail?
        Expression<CompanyEvent> employeeEventsQuery =
                new Query<CompanyEvent>().Where(CompanyEvent.Company().equalTo(company)
                        .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active))
                        .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.InvalidEmployeeInformation)))
                        .EagerLoad(CompanyEvent.CompanyEventDetailSet());
        DomainEntitySet<CompanyEvent> employeeEvents = PayrollServices.entityFinder.find(CompanyEvent.class, employeeEventsQuery);

        // collapse to a single validation message per employee per message status
        HashMap<String, EmployeeValidationMessage> employeeValidationMessageMap = new HashMap<String, EmployeeValidationMessage>(employeeEvents.size());
        for (CompanyEvent employeeEvent : employeeEvents) {

            String sourceEmployeeId = employeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.SourceEmployeeId);
            String employeeName = employeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeName);
            String status = employeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.MessageLevel);
            String message = employeeEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeInvalidReason);

            String key = sourceEmployeeId + ":" + status;
            EmployeeValidationMessage employeeValidationMessage = null;
            if (employeeValidationMessageMap.containsKey(key)) {
                employeeValidationMessage = employeeValidationMessageMap.get(key);
            } else {
                employeeValidationMessage = new EmployeeValidationMessage();
                employeeValidationMessage.setEmployeeName(employeeName);
                employeeValidationMessage.setStatus(status);
                employeeValidationMessageMap.put(key, employeeValidationMessage);
            }

            employeeValidationMessage.getMessages().add(message);

            // get affected paychecks (i.e. all unsent paychecks)
            if (validationErrorBlocksSendToTOK(status)) {
                DomainEntitySet<Employee> employees = Application.find(Employee.class,
                        Employee.Company().equalTo(company)
                                .And(Employee.SourceEmployeeId().equalTo(sourceEmployeeId)));
                if (employees.size() > 0) {
                    Employee employee = employees.get(0);
                    DomainEntitySet<Paycheck> queuedPaychecks = Paycheck.findTP401kQueuedPaychecks(employee);
                    for (Paycheck queuedPaycheck : queuedPaychecks) {
                        String msg = String.format("Paycheck from %1$s with net amount: $%2$.2f",
                                queuedPaycheck.getPayrollRun().getPaycheckDate().format(dateFormat),
                                SpcfUtils.convertToBigDecimal(queuedPaycheck.getNetAmount()));
                        employeeValidationMessage.getRelatedPaycheckMessages().add(msg);
                    }
                }
            }
        }

        for (EmployeeValidationMessage employeeValidationMessage : employeeValidationMessageMap.values()) {
            transmissionResponse.getEmployeeMessages().add(employeeValidationMessage);
        }


        // sort by status, employee name
        List<EmployeeValidationMessage> sortedValidationMessages = transmissionResponse.getEmployeeMessages();
        Collections.sort(transmissionResponse.getEmployeeMessages(), new Comparator<EmployeeValidationMessage>() {
            public int compare(EmployeeValidationMessage o1, EmployeeValidationMessage o2) {
                MessageInfo.MessageLevel o1Level = MessageInfo.MessageLevel.valueOf(o1.getStatus());
                MessageInfo.MessageLevel o2Level = MessageInfo.MessageLevel.valueOf(o2.getStatus());

                int nameCompare = o1.getEmployeeName().compareTo(o2.getEmployeeName());
                if (nameCompare != 0)
                    return nameCompare;

                // note: descending order (ERROR, WARNING, INFO)
                return (o1Level.ordinal() - o2Level.ordinal()) * -1;
            }
        });

        return transmissionResponse;
    }

    private boolean validationErrorBlocksSendToTOK(String status) {
        return MessageInfo.MessageLevel.valueOf(status) == MessageInfo.MessageLevel.ERROR
                || MessageInfo.MessageLevel.valueOf(status) == MessageInfo.MessageLevel.WARNING;
    }
}
