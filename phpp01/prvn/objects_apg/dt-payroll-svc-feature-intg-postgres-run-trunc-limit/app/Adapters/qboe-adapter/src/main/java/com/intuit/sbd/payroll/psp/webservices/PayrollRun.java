package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.ActionEvent;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.eereturntransfer.EEReturnTransfer;
import intuit.osp.pse.dd.wsapi.xsd.eereturntransferrs.EEReturnTransferRs;
import intuit.osp.pse.dd.wsapi.xsd.payrollrunquery.PayrollRunQuery;
import intuit.osp.pse.dd.wsapi.xsd.payrollrunqueryrs.PayrollRunQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.payrollrunret.OperationType;
import intuit.osp.pse.dd.wsapi.xsd.payrollrunret.PayrollRunRet;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Implementation for all Payroll Run-related Web Services.
 *
 * @author kevseev
 */
public class PayrollRun extends WS {

    private static SpcfLogger logger = Application.getLogger(PayrollRun.class);

    public static final String SERVICE_NAME = "PayrollRun";

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {

        String[] expectedErrorCodes = {"137", "138", "272"};
        WSServerContext wsServerContext = new WSServerContext(PayrollRun.SERVICE_NAME, PayrollRun.Operations.QUERY);

        PayrollRunQueryRs payrollRunQueryRs = null;

        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult validationResult = new ProcessResult();
            PayrollRunQuery payrollRunQuery =
                    (PayrollRunQuery) wsServerContext.translateInputElement(requestDocument);

            payrollRunQueryRs = (PayrollRunQueryRs) wsServerContext.getOutputDTO();
            if (payrollRunQuery != null) {
                String sourceSystemCode = payrollRunQuery.getSourceSystemCd();
                String sourceCompanyId = payrollRunQuery.getCompanyID();

                // Validate Company Parameters
                validationResult.merge(Validator.validCompanyParameters(SourceSystemCode.valueOf(sourceSystemCode), sourceCompanyId));
                if (validationResult.isSuccess()) {
                    com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(
                            sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));

                    if (company != null) {
                        Calendar txCalendarFrom = payrollRunQuery.getTxDateFrom();
                        Calendar txCalendarTo = payrollRunQuery.getTxDateTo();
                        SpcfCalendar fromDate = (txCalendarFrom != null) ? CalendarUtils.convertToSpcfCalendar(txCalendarFrom) : null;
                        SpcfCalendar toDate = null;
                        if (txCalendarTo != null) {
                            Calendar newTxCalendarTo = Calendar.getInstance();
                            newTxCalendarTo.setTime(txCalendarTo.getTime());
                            newTxCalendarTo.set(Calendar.HOUR_OF_DAY, 23);
                            newTxCalendarTo.set(Calendar.MINUTE, 59);
                            newTxCalendarTo.set(Calendar.SECOND, 59);
                            newTxCalendarTo.set(Calendar.MILLISECOND, 999);
                            toDate = CalendarUtils.convertToSpcfCalendar(txCalendarTo);
                        }

                        // Validate Date Range
                        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
                            validationResult.getMessages().InvalidPaycheckDateRange(EntityName.Company, sourceCompanyId);
                        }

                        DomainEntitySet<com.intuit.sbd.payroll.psp.domain.PayrollRun> payrollRuns =
                                com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRuns(company, fromDate, toDate);

                        if (payrollRuns != null && payrollRuns.size() > 0) {
                            List retList = payrollRunQueryRs.getPayrollRunRet();

                            for (com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun : payrollRuns) {
                                retList.add(this.buildPayrollRunRet(payrollRun));
                            }
                        }
                    }
                }
            }
            payrollRunQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw new WSException(DDCommon.pse_Error, e);
        }

        finally {
            PayrollServices.commitUnitOfWork();
        }

        return wsServerContext.translateOutputDTO();

    }

    /**
     * @param pPayrollRun
     * @return
     * @throws Exception
     */
    private PayrollRunRet buildPayrollRunRet(com.intuit.sbd.payroll.psp.domain.PayrollRun pPayrollRun) throws Exception {
        DDCommon ddCommon = new DDCommon();

        intuit.osp.pse.dd.wsapi.xsd.payrollrunret.ObjectFactory objectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.payrollrunret.ObjectFactory();

        PayrollRunRet payrollRunRet = objectFactory.createPayrollRunRet();

        com.intuit.sbd.payroll.psp.domain.Company company = pPayrollRun.getCompany();
        payrollRunRet.setSourceSystemCd(company.getSourceSystemCd().toString());
        payrollRunRet.setCompanyID(company.getSourceCompanyId());

        payrollRunRet.setDDTXBatchID(pPayrollRun.getSourcePayRunId());

        com.intuit.sbd.payroll.psp.domain.CompanyBankAccount companyBankAccount = pPayrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        payrollRunRet.setCompanyBankAccountID(companyBankAccount.getSourceBankAccountId());

        payrollRunRet.setPaycheckDepositDate(CalendarUtils.convertToCalendar(pPayrollRun.getPaycheckDate().toLocal()));

        payrollRunRet.setPayrollRunDate(CalendarUtils.convertToCalendar(pPayrollRun.getPayrollRunDate().toLocal()));

        payrollRunRet.setPayrollNetAmount(SpcfUtils.convertToBigDecimal(pPayrollRun.getPayrollDirectDepositAmount()));

        payrollRunRet.setBankAccount(ddCommon.build_BankAccount(companyBankAccount.getBankAccount()));

        PayrollStatus payrollStatus = pPayrollRun.getPayrollRunStatus();
        if (payrollStatus!=null) {
            payrollRunRet.setPayrollStatusCd(payrollStatus.toString());
            payrollRunRet.setPayrollStatusDesc(payrollStatus.toString());
        }
        
        Collection actionCollection =
                pPayrollRun.getValidPayrollRunActions(ActionEvent.getAllPayrollActionEvents());
        List<ActionEvent> actionList = new ArrayList<ActionEvent>(actionCollection);
        Collections.sort(actionList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                ActionEvent actionEvent1 = (ActionEvent)obj1;
                ActionEvent actionEvent2 = (ActionEvent)obj2;
                return actionEvent1.getCode().compareTo(actionEvent2.getCode());
            }
        });
        if (actionList != null && actionList.size() > 0) {
            Collection allowedOperationCollection = payrollRunRet.getAllowedOperation();

            OperationType allowedOperation = null;
            for (Iterator iterator = actionList.iterator(); iterator.hasNext();) {
                ActionEvent actionEvent = (ActionEvent) iterator.next();
                allowedOperation = objectFactory.createOperationType();
                allowedOperation.setOperationCd(actionEvent.getCode().toString());
                allowedOperation.setOperationDesc(actionEvent.getDescription());
                allowedOperationCollection.add(allowedOperation);
            }
        }
        return payrollRunRet;
    }

    public Element bquery(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "1055", "282", "280", "290"};
        intuit.osp.common.wsf.server.WSServerContext wsServerContext = new WSServerContext(PayrollRun.SERVICE_NAME, PayrollRun.Operations.QUERY);
        EEReturnTransferRs eeReturnTransferRs;
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();

            EEReturnTransfer eeReturnTransfer =
                    (EEReturnTransfer) wsServerContext.translateInputElement(requestDocument);

            eeReturnTransferRs = (EEReturnTransferRs) wsServerContext.getOutputDTO();

            if (eeReturnTransfer != null) {
                processResult = PayrollServices.financialTransactionManager.addEmployeeReturnTransferTransaction(
                        SourceSystemCode.valueOf(eeReturnTransfer.getSourceSystemCd()), eeReturnTransfer.getCompanyID(),
                        eeReturnTransfer.getDDTxBatchID());
            }

            PayrollServices.commitUnitOfWork();

            eeReturnTransferRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        } catch (
                WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            // logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            //  logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Interface to store names of operations.
     */
    public interface Operations {

        public static final String QUERY = "query";
    }
}
