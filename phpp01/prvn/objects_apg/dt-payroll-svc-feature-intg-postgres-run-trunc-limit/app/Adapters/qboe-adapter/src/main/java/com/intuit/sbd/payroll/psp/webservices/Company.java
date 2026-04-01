package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.companyquery.CompanyQuery;
import intuit.osp.pse.dd.wsapi.xsd.companyqueryrs.CompanyQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.companyret.CompanyRet;
import intuit.osp.pse.dd.wsapi.xsd.companysync.CompanySync;
import intuit.osp.pse.dd.wsapi.xsd.companysystemeventret.CompanySystemEventRet;
import intuit.osp.pse.dd.wsapi.xsd.companysystemeventsync.CompanySystemEventSync;
import intuit.osp.pse.dd.wsapi.xsd.companysystemeventsyncrs.CompanySystemEventSyncRs;
import intuit.osp.pse.dd.wsapi.xsd.systemeventdata.SystemEventData;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import java.util.*;


/**
 *
 * User: jjones1
 * Date: Aug 11, 2006
 * Time: 1:11:50 PM

 */
public class Company extends WS {
    
    private static SpcfLogger logger = Application.getLogger(Company.class);

    CompanySystemEventSyncRs companySystemEventSyncRs = null;
    intuit.osp.common.wsf.server.WSServerContext context = null;
    private static final intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory systemEventDataFactory =
            new intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory();

    public Element query(Element request) throws WSException {
        String[] expectedErrorCodes = {"125"};
        try {
            WSServerContext context = new WSServerContext("Company", "query");
            CompanyQuery queryRequest = (CompanyQuery) context.translateInputElement(request);
            CompanyQueryRs queryResponse = (CompanyQueryRs) context.getOutputDTO();

            doQuery(queryRequest, queryResponse, expectedErrorCodes);

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        } catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @SuppressWarnings("unchecked")
    private void doQuery(CompanyQuery pRequest, CompanyQueryRs pResponse, String[] pExpectedErrorCodes) throws Exception {
        Application.beginUnitOfWork();
        try {
            ProcessResult result = new ProcessResult();

            // we need a SourceSystem object for the code in the request
            SourceSystemCode sourceSystemCd = null;
            try {
                sourceSystemCd = SourceSystemCode.valueOf(pRequest.getSourceSystemCd());
            } catch (Exception ex) {
            }
            if (sourceSystemCd == null) {

                result.getMessages().InvalidSourceSystemCdSpecified(EntityName.Company, "", pRequest.getSourceSystemCd());
                logger.warn(result.getMessages().get(0).getMessage());

            } else {

                String crit = "";
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyService> companyServices = null;

                if (pRequest.getCompanyID() != null) {

                    crit = " and SourceCompanyId=" + pRequest.getCompanyID();
                    companyServices = CompanyService.findCompanyServicesBySourceCompanyId(
                            sourceSystemCd, ServiceCode.DirectDeposit, pRequest.getCompanyID());

                } else if (pRequest.getFEIN() != null) {

                    crit = " and FedTaxId=" + pRequest.getFEIN();
                    companyServices = CompanyService.findCompanyServicesByFedTaxId(
                            sourceSystemCd, ServiceCode.DirectDeposit, pRequest.getFEIN());

                } else if (pRequest.getLegalName() != null) {

                    crit = " and LegalName=" + pRequest.getLegalName();
                    companyServices = CompanyService.findCompanyServices(
                            sourceSystemCd, ServiceCode.DirectDeposit, pRequest.getLegalName());

                } else {

                    crit = "";
                    companyServices = CompanyService.findCompanyServices(
                            sourceSystemCd, ServiceCode.DirectDeposit);

                }

                int n = 0;
                if (companyServices != null) {
                    for (n = 0; n < 100 && n < companyServices.size(); n++) {
                        CompanyService cs = companyServices.get(n);
                        CompanyRet c = companyToXML(cs);
                        pResponse.getCompanyRet().add(c);
                    }
                }
                logger.info("search by SourceSystemCd=" + sourceSystemCd + crit + " returns " + n + " results");

            }

            pResponse.setResponseStatus(DDCommon.build_ResponseStatus(result, pExpectedErrorCodes));
        } finally {
            Application.commitUnitOfWork();
        }
    }

    private CompanyRet companyToXML(final CompanyService pCompanyService)
            throws JAXBException {
        // get the factory and create the DTO
        intuit.osp.pse.dd.wsapi.xsd.companyret.ObjectFactory factory =
                new intuit.osp.pse.dd.wsapi.xsd.companyret.ObjectFactory();

        CompanyRet xmlCompany = factory.createCompanyRet();

        xmlCompany.setSourceSystemCd(pCompanyService.getCompany().getSourceSystemCd().toString());
        xmlCompany.setCompanyID(pCompanyService.getCompany().getSourceCompanyId());
        xmlCompany.setFEIN(pCompanyService.getCompany().getFedTaxId());
        xmlCompany.setLegalName(pCompanyService.getCompany().getLegalName());
        xmlCompany.setDBA(pCompanyService.getCompany().getDbaName());
        xmlCompany.setNotificationEmail(pCompanyService.getCompany().getNotificationEmail());

        xmlCompany.setLegalAddress(DDCommon.addressToXML(pCompanyService.getCompany().getLegalAddress()));
        xmlCompany.setMailingAddress(DDCommon.addressToXML(pCompanyService.getCompany().getMailingAddress()));

        for (Contact contact : pCompanyService.getCompany().getContactCollection()) {
            xmlCompany.getContact().add(DDCommon.contactToXML(contact));
        }

        String ddCode = DDCodeToPSP.getQBOECompanyStatus(pCompanyService);
        xmlCompany.setCompanyStatusCd(ddCode);

        return xmlCompany;
    }


    public Element systemEventSync(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {};
        Element returnDoc;
        try {
            //Get the incoming parameters
            context = new intuit.osp.common.wsf.server.WSServerContext("Company", "systemEventSync");
            PayrollServices.beginUnitOfWork();

            CompanySystemEventSync companySystemEventSync = (CompanySystemEventSync) context.translateInputElement(requestDoc);

            build_CompanySystemEventSyncRs(companySystemEventSync, expectedErrorCodes);

            PayrollServices.commitUnitOfWork();

            returnDoc = context.translateOutputDTO();

            return returnDoc;
        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void build_CompanySystemEventSyncRs(
            CompanySystemEventSync companySystemEventSync,
            String[] pExpectedErrorCodes) throws Exception {
        String sourceSystemCd;
        String companyID;
        Calendar systemEventTimeStamp;
        HashMap inputParamMap = new HashMap();

        companySystemEventSyncRs = (CompanySystemEventSyncRs) context.getOutputDTO();
        sourceSystemCd = companySystemEventSync.getSourceSystemCd();

        List companySyncList = companySystemEventSync.getCompanySync();
        if (companySyncList != null && companySyncList.size() > 0) {
            for (Iterator iterator1 = companySyncList.iterator(); iterator1.hasNext();) {
                CompanySync companySync = (CompanySync) iterator1.next();

                systemEventTimeStamp = companySync.getSystemEventTimeStamp();
                companyID = companySync.getCompanyID();
                com.intuit.sbd.payroll.psp.domain.Company company =
                        com.intuit.sbd.payroll.psp.domain.Company.findCompany(companySync.getCompanyID(),
                                SourceSystemCode.valueOf(sourceSystemCd));
                inputParamMap.put(company, systemEventTimeStamp.getTime());
            }
        }

        Calendar calendar = CalendarUtils.convertToCalendar(CalendarUtils.getPSPDateFromDB());
        companySystemEventSyncRs.setSystemEventTimeStamp(calendar);

        Collection companyEventCollection = getCompanyEventCollection(inputParamMap,
                CalendarUtils.convertToSpcfCalendar(calendar));
        if (companyEventCollection != null && companyEventCollection.size() > 0) {

            for (Iterator iterator = companyEventCollection.iterator(); iterator.hasNext();) {
                DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyEvent> companyEvents = (DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyEvent>) iterator.next();

                for (com.intuit.sbd.payroll.psp.domain.CompanyEvent companyEvent : companyEvents) {
                    CompanySystemEventRet ret = build_CompanySystemEventRet(companyEvent);
                    // We have to skip unnecessary service status change events (when the status doesn't change)
                    boolean skipEvent = false;
                    if (ret.getSystemEventCd().equals("DDSTATCHG")) {
                        skipEvent = DDCommon.isStatusUnchanged(ret);
                    }
                    if (! skipEvent) {
                        companySystemEventSyncRs.getCompanySystemEventRet().add(ret);
                    }
                }
            }
        }
        companySystemEventSyncRs.setResponseStatus(DDCommon.build_ResponseStatus(new ProcessResult(), pExpectedErrorCodes));
    }

    private Collection getCompanyEventCollection(HashMap inputParamMap, SpcfCalendar toDate) {
        Collection eventCollection = new ArrayList();

        for (Iterator iterator = inputParamMap.keySet().iterator(); iterator.hasNext();) {

            com.intuit.sbd.payroll.psp.domain.Company company =
                    (com.intuit.sbd.payroll.psp.domain.Company) iterator.next();
            SpcfCalendar fromDate = (CalendarUtils.convertToSpcfCalendar((Date) inputParamMap.get(company)));

            DomainEntitySet<com.intuit.sbd.payroll.psp.domain.CompanyEvent> companyEvents =
                    com.intuit.sbd.payroll.psp.domain.CompanyEvent.findCompanySystemEvents(company, fromDate, toDate);

            if (companyEvents.size() > 0) {
                eventCollection.add(companyEvents);
            }
        }
        return eventCollection;
    }

    private CompanySystemEventRet build_CompanySystemEventRet(com.intuit.sbd.payroll.psp.domain.CompanyEvent
            pCompanyEvent) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.companysystemeventret.ObjectFactory companySystemEventRetObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.companysystemeventret.ObjectFactory();

        intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory systemEventDataObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.systemeventdata.ObjectFactory();

        CompanySystemEventRet companySystemEventRet = companySystemEventRetObjectFactory.createCompanySystemEventRet();

        if (pCompanyEvent != null) {
            com.intuit.sbd.payroll.psp.domain.Company company = pCompanyEvent.getCompany();
            companySystemEventRet.setSourceSystemCd(company.getSourceSystemCd().toString());
            companySystemEventRet.setCompanyID(company.getSourceCompanyId());

            companySystemEventRet.setSystemEventCd(
                    DDCodeToPSP.getQBOEEventTypeCode(pCompanyEvent.getEventTypeCd()));
            companySystemEventRet.setSystemEventDescShort("");
            companySystemEventRet.setSystemEventDescLong("");

            companySystemEventRet.setSystemEventDateTime(CalendarUtils.convertToCalendar(
                    pCompanyEvent.getEventTimeStamp()));

            // Get Event Details
            SortedSet<String[]> companyEventDetails = DDCommon.getEventDetails(pCompanyEvent);

            if (companyEventDetails != null) {
                for (Iterator<String[]> iter = companyEventDetails.iterator(); iter.hasNext();) {
                    String[] nameValuePair = iter.next();
                    SystemEventData systemEventData = systemEventDataFactory.createSystemEventData();
                    systemEventData.setName(nameValuePair[0]);
                    systemEventData.setValue(nameValuePair[1]);
                    companySystemEventRet.getSystemEventData().add(systemEventData);
                }
            }
        } else {
            String args[] = {"CompanyEvent", "build_CompanySystemEventRet"};
            throw new NullPointerException(DDCommon.getErrorMessage(DDCommon.npe_Error, args));
        }
        return companySystemEventRet;
    }
}
