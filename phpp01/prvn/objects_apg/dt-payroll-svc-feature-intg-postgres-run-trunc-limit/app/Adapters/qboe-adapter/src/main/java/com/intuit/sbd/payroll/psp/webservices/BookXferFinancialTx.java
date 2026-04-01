/*
 * $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/BookXferFinancialTx.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
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
import intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxquery.BookXferFinancialTxQuery;
import intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxqueryrs.BookXferFinancialTxQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxret.BookXferFinancialTxRet;
import intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxret.OperationType;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author kevseev
 */
public class BookXferFinancialTx extends WS {

    private static SpcfLogger logger = Application.getLogger(BookXferFinancialTx.class);

    public static final String SERVICE_NAME = "BookXferFinancialTx";

    /**
     * This method executes logic for query request.
     *
     * @param requestDocument Input XML document
     * @return
     * @throws intuit.osp.common.wsf.base.WSException
     *
     */
    public Element query(Element requestDocument) throws WSException {
        WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, "query");

        String[] expectedErrorCodes = {"137", "138", "169", "194"};

        BookXferFinancialTxQueryRs bookXferFinancialTxQueryRs;
        String sourcePayrollRunId = null;

        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult validationResult = new ProcessResult();

            BookXferFinancialTxQuery bookXferFinancialTxQuery =
                    (BookXferFinancialTxQuery) wsServerContext.translateInputElement(requestDocument);

            bookXferFinancialTxQueryRs = (BookXferFinancialTxQueryRs) wsServerContext.getOutputDTO();

            String sourceSystemCode = bookXferFinancialTxQuery.getSourceSystemCd();
            String sourceCompanyId = bookXferFinancialTxQuery.getCompanyID();

            // Check if Company parameters are valid
            validationResult.merge(Validator.validCompanyParameters(SourceSystemCode.valueOf(sourceSystemCode), sourceCompanyId));

            //Validate Company Exist
            Company company = Company.findCompany(sourceCompanyId,
                    SourceSystemCode.valueOf(sourceSystemCode));

            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                        sourceCompanyId, sourceSystemCode, sourceCompanyId);
            } else {
                //Validate Payroll run exist
                sourcePayrollRunId = bookXferFinancialTxQuery.getDDTxBatchID();
                if (sourcePayrollRunId != null) {
                    com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                            findPayrollRun(company, sourcePayrollRunId);

                    if (payrollRun == null) {
                        validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, sourcePayrollRunId,
                                sourcePayrollRunId, sourceSystemCode, sourceCompanyId);
                    }
                }
            }

            if (validationResult.isSuccess()) {
                String transactionTypeCode = bookXferFinancialTxQuery.getTxTypeCd();
                SpcfCalendar settlementDateFrom = null;
                SpcfCalendar settlementDateTo = null;

                if (bookXferFinancialTxQuery.getTxDateFrom() != null) {
                    settlementDateFrom = CalendarUtils.convertToSpcfCalendar(bookXferFinancialTxQuery.getTxDateFrom());
                }

                if (bookXferFinancialTxQuery.getTxDateTo() != null) {
                    settlementDateTo = CalendarUtils.convertToSpcfCalendar(bookXferFinancialTxQuery.getTxDateTo());
                }

                DomainEntitySet<FinancialTransaction> financialTranasctions = FinancialTransaction.findFinancialTransactions(
                        company, sourcePayrollRunId, null, null, null, DDCodeToPSP.getTransactionTypeCode(transactionTypeCode),
                        settlementDateFrom, settlementDateTo, null);

                populateFinancialTransactionsList(bookXferFinancialTxQueryRs.getBookXferFinancialTxRet(),
                        financialTranasctions);
            }

            bookXferFinancialTxQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));

            PayrollServices.commitUnitOfWork();

            return wsServerContext.translateOutputDTO();
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

    /**
     * Method to populate the financial transaction list from the SpcfList
     *
     * @param pFinancialTransactionList BookXferFinancialTxRet list
     * @param pFinancialTranasctions    Financialtransactions list
     * @throws Exception exception
     */
    private static void populateFinancialTransactionsList(List<BookXferFinancialTxRet> pFinancialTransactionList,
                                                          DomainEntitySet<FinancialTransaction> pFinancialTranasctions)
            throws Exception {

        for (FinancialTransaction financialTranasction : pFinancialTranasctions) {
            if (TransactionType.isIntuitTransactionType(financialTranasction.getTransactionType().
                    getTransactionTypeCd())) {
                pFinancialTransactionList.add(buildBookXferFinancialTxRet(financialTranasction));
            }
        }
    }

    /**
     * This method builds BookXferFinancialTxRet object based on supplied FinancialTransaction Domain object.
     *
     * @param pFinancialTransaction FinancialTransaction
     * @return bookXferFinancialTxRet BookXferFinancialTxRet
     * @throws Exception exception
     */
    private static BookXferFinancialTxRet buildBookXferFinancialTxRet(FinancialTransaction pFinancialTransaction)
            throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxret.ObjectFactory objectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.bookxferfinancialtxret.ObjectFactory();

        BookXferFinancialTxRet bookXferFinancialTxRet = objectFactory.createBookXferFinancialTxRet();

        Company company = pFinancialTransaction.getCompany();

        bookXferFinancialTxRet.setSourceSystemCd(company.getSourceSystemCd().toString());
        bookXferFinancialTxRet.setCompanyID(company.getSourceCompanyId());

        PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();

        bookXferFinancialTxRet.setDDTxBatchID(payrollRun.getSourcePayRunId());

        bookXferFinancialTxRet.setFinancialTxAmt(SpcfUtils.
                convertToBigDecimal(pFinancialTransaction.getFinancialTransactionAmount()));

        bookXferFinancialTxRet.setPSETransactionID(pFinancialTransaction.getId().toString());
        bookXferFinancialTxRet.setTxDate(CalendarUtils.convertToCalendar(
                pFinancialTransaction.getSettlementDate().toLocal()));

        bookXferFinancialTxRet.setTxSettlementTypeCd(DDCodeToPSP.getQBOESettlementType(
                pFinancialTransaction.getSettlementTypeCd()));

        TransactionState transactionState = pFinancialTransaction.getCurrentTransactionState();
        bookXferFinancialTxRet.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
        bookXferFinancialTxRet.setTxStatusDesc(transactionState.getName());

        TransactionType transactionType = pFinancialTransaction.getTransactionType();
        String sku = pFinancialTransaction.getSku();
        OfferingServiceChargeType offeringServiceCharge = null;
        if (null != sku) {
            offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        }
        String transactionTypeCode = DDCodeToPSP.getQBOETransactionTypeCode(transactionType.getTransactionTypeCd()
                , offeringServiceCharge);
        bookXferFinancialTxRet.setTxTypeCd(transactionTypeCode);
        bookXferFinancialTxRet.setTxTypeDesc(transactionType.getName());

        Collection actionCollection =
                pFinancialTransaction.getActionCollection();
        List<ActionEvent> actionList = new ArrayList<ActionEvent>(actionCollection);
        Collections.sort(actionList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                ActionEvent actionEvent1 = (ActionEvent) obj1;
                ActionEvent actionEvent2 = (ActionEvent) obj2;
                return actionEvent1.getCode().compareTo(actionEvent2.getCode());
            }
        });
        if (actionList != null && actionList.size() > 0) {
            Collection allowedOperationCollection = bookXferFinancialTxRet.getAllowedOperation();

            OperationType allowedOperation = null;
            for (Iterator iterator = actionList.iterator(); iterator.hasNext();) {
                ActionEvent actionEvent = (ActionEvent) iterator.next();
                allowedOperation = objectFactory.createOperationType();
                allowedOperation.setOperationCd(actionEvent.getCode().toString());
                allowedOperation.setOperationDesc(actionEvent.getDescription());
                allowedOperationCollection.add(allowedOperation);
            }
        }

        return bookXferFinancialTxRet;
    }
}
