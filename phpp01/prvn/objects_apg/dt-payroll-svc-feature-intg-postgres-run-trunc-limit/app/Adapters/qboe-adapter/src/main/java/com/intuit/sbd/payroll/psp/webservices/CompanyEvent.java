/*********************************************************************************
 * Copyright Statement: CONFIDENTIAL - Copyright 2004 Intuit Inc.
 * This material contains certain trade secrets and confidential and proprietary
 * information of Intuit Inc. Use, reproduction, disclosure and distribution by any
 * means are prohibited, except pursuant to a written license from Intuit Inc. Use of
 * copyright notice is precautionary and does not imply publication or disclosure.
 *********************************************************************************/
package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventType;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.companyeventquery.CompanyEventQuery;
import intuit.osp.pse.dd.wsapi.xsd.companyeventqueryrs.CompanyEventQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.companyeventret.CompanyEventRet;
import intuit.osp.pse.dd.wsapi.xsd.responsestatus.ResponseStatus;
import intuit.osp.pse.dd.wsapi.xsd.systemeventdata.SystemEventData;
import org.w3c.dom.Element;

import java.util.*;


/**
 * <p/>
 * File: $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/CompanyEvent.java#2 $
 * <p/>
 * Class: intuit.osp.pse.dd.wsimpl.CompanyEvent
 *
 * @author mvillani
 */
public class CompanyEvent extends WS {


    public static final String SERVICE_NAME = "CompanyEvent";

    private static SpcfLogger logger = Application.getLogger(CompanyEvent.class);

    private static final intuit.osp.pse.dd.wsapi.xsd.companyeventret.ObjectFactory companyEventRetFactory =
            new intuit.osp.pse.dd.wsapi.xsd.companyeventret.ObjectFactory();
    private static final intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory systemEventDataFactory =
            new intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory();

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"169"};
        WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, "query");
        CompanyEventQueryRs companyEventQueryRs;
        ResponseStatus responseStatus = DDCommon.SUCCESS;
        companyEventQueryRs = (CompanyEventQueryRs) wsServerContext.getOutputDTO();
        try {
            PayrollServices.beginUnitOfWork();
            CompanyEventQuery companyEventQuery = (CompanyEventQuery) wsServerContext.translateInputElement(requestDocument);
            String sourceSystemCd = companyEventQuery.getSourceSystemCd();
            String sourceCompanyId = companyEventQuery.getCompanyID();
            ProcessResult validationResult = new ProcessResult();
            // Validate the company exists
            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCd, sourceCompanyId);
            }

            if (validationResult.isSuccess()) {
                SpcfCalendar fromDate = null;
                SpcfCalendar toDate = null;
                if (companyEventQuery.getDateFrom() != null) {
                    fromDate = CalendarUtils.convertToSpcfCalendar(companyEventQuery.getDateFrom());
                }
                if (companyEventQuery.getDateTo() != null) {
                    toDate = CalendarUtils.convertToSpcfCalendar(companyEventQuery.getDateTo());
                }
                EventTypeCode eventTypeCd = null;
                if (companyEventQuery.getCompanyEventCd() != null) {
                    eventTypeCd = DDCodeToPSP.getEventTypeCode(companyEventQuery.getCompanyEventCd());
                }

                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyEvent> companyEvents = com.intuit.sbd.payroll.psp.domain.CompanyEvent.findCompanyEvents(company, eventTypeCd, null, fromDate, toDate);
                populateCompanyEventRetList(companyEventQueryRs.getCompanyEventRet(), companyEvents);
            }

            companyEventQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));
            PayrollServices.commitUnitOfWork();
            return wsServerContext.translateOutputDTO();
        } catch (WSValidationException e) {
            Application.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            Application.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    private static void populateCompanyEventRetList(List companyEvenRetList, DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyEvent> companyEvents) throws Exception {
        for (Iterator it = companyEvents.iterator(); it.hasNext();) {
            CompanyEventRet companyEventRet = companyEventRetFactory.createCompanyEventRet();
            companyEvenRetList.add(companyEventRet);
            boolean eventToReturn = copy(companyEventRet, (com.intuit.sbd.payroll.psp.domain.CompanyEvent) it.next());

            if (!eventToReturn) {
                companyEvenRetList.remove(companyEventRet);
            } else {
                
                // We have to skip unnecessary service status change events (when the status doesn't change)
                if (companyEventRet.getCompanyEventCd().equals("DDSTATCHG")) {
                    if (DDCommon.isStatusUnchanged(companyEventRet)) {
                        companyEvenRetList.remove(companyEventRet);
                    }
                }
            }
        }
    }

    private static boolean copy(CompanyEventRet copyTo, com.intuit.sbd.payroll.psp.domain.CompanyEvent copyFrom) throws Exception {
        String eventCode = DDCodeToPSP.getQBOEEventTypeCode(copyFrom.getEventTypeCd());
        if (eventCode != null) {
            com.intuit.sbd.payroll.psp.domain.Company company = copyFrom.getCompany();
            copyTo.setCompanyID(company.getSourceCompanyId());
            copyTo.setSourceSystemCd(company.getSourceSystemCd().toString());
            copyTo.setPSECompanyEventID(copyFrom.getId().toString());
            copyTo.setCompanyEventDateTime(CalendarUtils.convertToCalendar(copyFrom.getEventTimeStamp()));
            copyTo.setCompanyEventCd(DDCodeToPSP.getQBOEEventTypeCode(copyFrom.getEventTypeCd()));
            EventType eventType = PayrollServices.entityFinder.findById(EventType.class, copyFrom.getEventTypeCd());
            copyTo.setSystemEvent(eventType.getSourceSystemCollection().contains(SourceSystemCode.QBOE));
            copyTo.setCompanyEventDescShort(eventType.getName());
            copyTo.setCompanyEventDescLong(eventType.getDescription());
            copyTo.setCompanyEventStatus(copyFrom.getStatusCd().toString());

            copyTo.setUserID(copyFrom.getCreatorId());

            if (copyFrom.getStatusCd() == CompanyEventStatus.Inactive) {
                copyTo.setCancelDate(CalendarUtils.convertToCalendar(copyFrom.getStatusEffectiveDate().toLocal()));
            }

            if (CompanyEventStatus.Inactive.equals(copyFrom.getStatusCd())) {
                copyTo.setCancelUserID(copyFrom.getModifierId());
            }

            // Get Event Details
            SortedSet<String[]> companyEventDetails = DDCommon.getEventDetails(copyFrom);

            if (companyEventDetails != null) {
                for (Iterator<String[]> iter = companyEventDetails.iterator(); iter.hasNext();) {
                    String[] nameValuePair = iter.next();
                    SystemEventData systemEventData = systemEventDataFactory.createSystemEventData();
                    systemEventData.setName(nameValuePair[0]);
                    systemEventData.setValue(nameValuePair[1]);
                    copyTo.getCompanyEventData().add(systemEventData);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public interface Operations {
        static final String QUERY = "query";
    }

}
