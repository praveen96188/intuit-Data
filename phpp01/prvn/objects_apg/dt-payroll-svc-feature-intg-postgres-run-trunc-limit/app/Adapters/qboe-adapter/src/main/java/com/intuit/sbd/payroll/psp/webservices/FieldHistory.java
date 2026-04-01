package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.PropertyAudit;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.fieldhistoryquery.FieldHistoryQuery;
import intuit.osp.pse.dd.wsapi.xsd.fieldhistoryqueryrs.FieldHistoryQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.fieldhistoryret.FieldChangeType;
import intuit.osp.pse.dd.wsapi.xsd.fieldhistoryret.FieldHistoryRet;
import org.w3c.dom.Element;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation for all Payroll Run-related Web Services.
 * @author kevseev
 */
public class FieldHistory extends WS {
    
    private static SpcfLogger logger = Application.getLogger(FieldHistory.class);
    
    public static final String SERVICE_NAME = "FieldHistory";
    
    /**
     * Default constructor.
     */
    public FieldHistory() {
        super();
    }

    /**
     * 
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169", "5001"};
    	WSServerContext wsServerContext = new WSServerContext(FieldHistory.SERVICE_NAME, FieldHistory.Operations.QUERY);

        FieldHistoryQueryRs fieldHistoryQueryRs = null;
        ProcessResult validationResult = new ProcessResult();
        
        try {
            PayrollServices.beginUnitOfWork();

            FieldHistoryQuery fieldHistoryQuery =
	        	(FieldHistoryQuery) wsServerContext.translateInputElement(requestDocument);

			fieldHistoryQueryRs = (FieldHistoryQueryRs) wsServerContext.getOutputDTO();
            if (fieldHistoryQuery != null) {
	        	String sourceSystemCode = fieldHistoryQuery.getSourceSystemCd();
	        	String sourceCompanyId = fieldHistoryQuery.getCompanyID();
	        	String fieldName = fieldHistoryQuery.getFieldName();
	        	Calendar calendarFrom = fieldHistoryQuery.getDateFrom();
                SpcfCalendar fromDate = null;

                if (calendarFrom != null) {
	        		Calendar newCalendarFrom = Calendar.getInstance();
	        		newCalendarFrom.setTime(calendarFrom.getTime());
	        		newCalendarFrom.set(Calendar.HOUR_OF_DAY, 0);
	        		newCalendarFrom.set(Calendar.MINUTE, 0);
	        		newCalendarFrom.set(Calendar.SECOND, 0);
	        		newCalendarFrom.set(Calendar.MILLISECOND, 0);
                    fromDate = CalendarUtils.convertToSpcfCalendar(newCalendarFrom.getTime());
                }
                
                com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));

                if (company == null) {
                    validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
                    fieldHistoryQueryRs.setFieldHistoryRet(null);
                } else {
                    if (fieldName == null || fieldName.trim().length() == 0) {
                        validationResult.getMessages().InvalidValue(EntityName.PropertyAudit, sourceCompanyId, "FieldName");
                    } else {
                        DomainEntitySet<PropertyAudit> propertyAudits = null;
                        String tableName = (String) PropertyAudit.getFieldNameToTableNameMap().get(fieldName);
                        String columnName = (String) PropertyAudit.getFieldNameToColumnNameMap().get(fieldName);
                        if (tableName == null || columnName == null) {
                            validationResult.getMessages().InvalidValue(EntityName.PropertyAudit, sourceCompanyId, "FieldName");
                        } else {
                            propertyAudits = PropertyAudit.findPropertyAudits(company, tableName, columnName, fromDate);
                        }

                        FieldHistoryRet fieldHistoryRet = null;
                        intuit.osp.pse.dd.wsapi.xsd.fieldhistoryret.ObjectFactory objectFactory =
                            new intuit.osp.pse.dd.wsapi.xsd.fieldhistoryret.ObjectFactory();
                        fieldHistoryRet = objectFactory.createFieldHistoryRet();

                        fieldHistoryRet.setSourceSystemCd(sourceSystemCode);
                        fieldHistoryRet.setCompanyID(sourceCompanyId);
                        fieldHistoryRet.setFieldName(fieldName);

                        List fieldChangeList = fieldHistoryRet.getFieldChange();
                        FieldChangeType fieldChange = null;
                        Date insertDate = null;
                        Calendar calendar = null;
                        if (propertyAudits != null && propertyAudits.size() > 0) {

                            PropertyAudit propertyAudit = null;
                            String oldValue = null;
                            String newValue = null;
                            for (Iterator iterator = propertyAudits.iterator(); iterator.hasNext();) {
                                oldValue = "DEFAULT";
                                newValue = "DEFAULT";
                                propertyAudit = (PropertyAudit) iterator.next();

                                fieldChange = objectFactory.createFieldChangeType();
                                if ((insertDate = CalendarUtils.convertToDate(propertyAudit.getAuditDate().toLocal())) != null) {
                                    calendar = Calendar.getInstance();
                                    calendar.setTime(insertDate);
                                    fieldChange.setDate(calendar);
                                }

                                fieldChange.setUserID(propertyAudit.getUserId());

                                if (null != propertyAudit.getNewPropertyValue()) {
                                       newValue = propertyAudit.getNewPropertyValue();
                                }
                                if (null != propertyAudit.getOldPropertyValue()) {
                                       oldValue = propertyAudit.getOldPropertyValue();
                                }

                                fieldChange.setOldValue(oldValue);
                                fieldChange.setNewValue(newValue);

                                fieldChangeList.add(fieldChange);
                            }
                        } 

                        fieldHistoryQueryRs.setFieldHistoryRet(fieldHistoryRet);
                    }
                }
            }

            fieldHistoryQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException wsValidationException) {
        	PayrollServices.rollbackUnitOfWork();
            FieldHistory.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
		} catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            FieldHistory.logger.error(exception.getMessage(), exception);
			throw new WSException(DDCommon.pse_Error, exception);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }

		return returnDoc;

    }
    
    /**
     * Interface to store names of operations.
     */
    public interface Operations {
		
		public static final String QUERY = "query";
    }
}