/*********************************************************************************
 * Copyright Statement: CONFIDENTIAL - Copyright 2004 Intuit Inc.
 * This material contains certain trade secrets and confidential and proprietary
 * information of Intuit Inc. Use, reproduction, disclosure and distribution by any
 * means are prohibited, except pursuant to a written license from Intuit Inc. Use of
 * copyright notice is precautionary and does not imply publication or disclosure.
 *********************************************************************************/
package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSGlobalParameter;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.companynoteadd.CompanyNoteAdd;
import intuit.osp.pse.dd.wsapi.xsd.companynoteaddrs.CompanyNoteAddRs;
import intuit.osp.pse.dd.wsapi.xsd.companynotequery.CompanyNoteQuery;
import intuit.osp.pse.dd.wsapi.xsd.companynotequeryrs.CompanyNoteQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.companynoteret.CompanyNoteRet;
import intuit.osp.pse.dd.wsapi.xsd.companynoteret.ObjectFactory;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import java.util.Calendar;


/**
 * <p/>
 * File: $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/CompanyNote.java#1 $
 * <p/>
 * @author wnichols
 */
public class CompanyNote extends WS {

    private static SpcfLogger logger = Application.getLogger(CompanyNote.class);

	/**
	 * Entry point for "query" web service.
	 * @param request
	 * @return
	 * @throws WSException
	 */
    public Element query(Element request) throws WSException {
        try {
            intuit.osp.common.wsf.server.WSServerContext context = new WSServerContext("CompanyNote", "query");
            CompanyNoteQuery queryRequest = (CompanyNoteQuery)context.translateInputElement(request);
            CompanyNoteQueryRs queryResponse = (CompanyNoteQueryRs)context.getOutputDTO();

            doQuery(queryRequest, queryResponse);

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        } catch (WSValidationException e) {
            logger.error(e.getMessage(),e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(),e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void doQuery(CompanyNoteQuery pRequest, CompanyNoteQueryRs pResponse) throws JAXBException, Exception {
        ProcessResult result = new ProcessResult();
        String[] expectedErrorCodes = {"169"};

        PayrollServices.beginUnitOfWork();
        try {
			String srcSystemCd = pRequest.getSourceSystemCd();
			String srcCompanyId = pRequest.getCompanyID();

            // make sure the company exists, so we can give a specific reason for not finding any notes
            com.intuit.sbd.payroll.psp.domain.Company domainCompany =
                    com.intuit.sbd.payroll.psp.domain.Company.findCompany(srcCompanyId, SourceSystemCode.valueOf(srcSystemCd));
            if (domainCompany == null) // if no such Company
            {
                result.getMessages().CompanyDoesNotExist(EntityName.Company, srcCompanyId, srcSystemCd, srcCompanyId);
                logger.warn(result.getMessages().get(result.getMessages().size()-1).getMessage());
            }
            else // Company exists
            {
                Calendar dateFrom = pRequest.getDateFrom();
                Calendar dateTo = pRequest.getDateTo();
                if (dateTo != null) {
                    // ensure the time portion is present for the correct comparison in sql
                    dateTo.set(Calendar.HOUR_OF_DAY, 23);
                    dateTo.set(Calendar.MINUTE, 59);
                    dateTo.set(Calendar.SECOND, 59);

                    // if both dates are given, make sure they're ordered correctly
                    if (dateFrom != null && dateFrom.after(dateTo)) {
                        Calendar temp = dateFrom;
                        dateFrom = dateTo;
                        dateTo = temp;
                    }
                }

                SpcfCalendar spcfFromDate = (dateFrom == null ? null : CalendarUtils.convertToSpcfCalendar(dateFrom));
                SpcfCalendar spcfToDate = (dateTo == null ? null : CalendarUtils.convertToSpcfCalendar(dateTo));

                ObjectFactory factory = new ObjectFactory(); // for CompanyNoteRet objects

                // add notes matching the date criteria to the output collection
                for (com.intuit.sbd.payroll.psp.domain.CompanyNote domainNote : domainCompany.getCompanyNoteCollection()) {
                    SpcfCalendar noteDate = SpcfCalendar.createInstance(
                            domainNote.getCreatedDate().getTimeInMilliseconds(), SpcfTimeZone.getLocalTimeZone());

                    // if this note matches the date criteria (when provided), add it to the return set
                    if ((spcfFromDate == null || spcfFromDate.compareTo(noteDate) <= 0) &&
                            (spcfToDate == null || spcfToDate.compareTo(noteDate) >= 0)) {
                        CompanyNoteRet xmlNote = factory.createCompanyNoteRet();
                        noteToXML(domainNote, xmlNote);
                        pResponse.getCompanyNoteRet().add(xmlNote);
                    }
                }

                logger.info("search by SourceSystemCd=" + srcSystemCd +
                        " and SourceCompanyId=" + srcCompanyId +
                        " returns " + pResponse.getCompanyNoteRet().size() + " results");
            }

            pResponse.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));

        } finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    /**
	 * Entry point for "add" web service.
	 * @param request
	 * @return
	 * @throws WSException
	 */
	public Element add(Element request) throws WSException {
        String[] expectedErrorCodes = {"137","138","125","169"};
        try {
            intuit.osp.common.wsf.server.WSServerContext context = new WSServerContext("CompanyNote", "add");
            CompanyNoteAdd addRequest = (CompanyNoteAdd)context.translateInputElement(request);
            CompanyNoteAddRs addResponse = (CompanyNoteAddRs)context.getOutputDTO();

            String userId = WSGlobalParameter.getGlobalWSParameter(WSGlobalParameter.WS_GLOBAL_PARAM_DB_USER);
            doAdd(addRequest, userId, addResponse, expectedErrorCodes);

            /*
            TODO:v2 Make the CompanyNoteRet optional in the XSD, so real error msgs will make it back to the client.
            Certain kinds of errors (e.g. no such company) will prevent us from creating a new CompanyNote.
            We'd like to return a specific reason for the failure to the client.  However, the XSD requires that a
            CompanyNoteRet object be present in the return, and that certain of its members be non-null.  Since we
            didn't add a CompanyNote, we can't assign any meaningful non-null values.  The WSF will throw an exception
            when we call context.translateOutputDTO(). That exception will get back to the client, "masking" the real
            reason for the failure.

            The old implementation did the same thing.
            */

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        } catch (WSValidationException e) {
            logger.error(e.getMessage(),e.getCause());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(),e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    void doAdd(
            CompanyNoteAdd pRequest,
            String pUserId,
            CompanyNoteAddRs pResponse,
            String[] pExpectedErrorCodes) throws JAXBException, Exception {
        String srcSystemCd = pRequest.getSourceSystemCd();
        String srcCompanyId = pRequest.getCompanyID();
        String notes = pRequest.getNotes();

        PayrollServices.beginUnitOfWork();
        try {
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyNote> result =
                    PayrollServices.companyManager.addCompanyNote(SourceSystemCode.valueOf(srcSystemCd), srcCompanyId,
                            null, pUserId, notes, false);

            if (result.isSuccess())
            {
                // pack up the new note for return
                noteToXML(result.getResult(), pResponse.getCompanyNoteRet());

                logger.info("added note to Company "+srcSystemCd+":"+srcCompanyId);
            }
            else
            {
                // no new note to return

                for (Message msg : result.getMessages()) {
                    logger.warn("adding, "+msg.getMessage());
                }
            }

            pResponse.setResponseStatus(DDCommon.build_ResponseStatus(result, pExpectedErrorCodes));

        } finally {
            PayrollServices.commitUnitOfWork();
        }
    }

    private static void noteToXML(com.intuit.sbd.payroll.psp.domain.CompanyNote pDomainNote, CompanyNoteRet pXmlNote) {
        pXmlNote.setCompanyID( pDomainNote.getCompany().getSourceCompanyId() );
        pXmlNote.setCompanyNoteDateTime( CalendarUtils.convertToCalendar(pDomainNote.getCreatedDate().toLocal()) );
        pXmlNote.setNotes( pDomainNote.getNotes() );
        pXmlNote.setSourceSystemCd( pDomainNote.getCompany().getSourceSystemCd().toString() );
        pXmlNote.setUserID( pDomainNote.getInsertUserId() );
    }
}
