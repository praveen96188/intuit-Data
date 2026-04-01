/*
 * $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/EEFinancialTx.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.eefinancialtxquery.EEFinancialTxQuery;
import intuit.osp.pse.dd.wsapi.xsd.eefinancialtxqueryrs.EEFinancialTxQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.EEFinancialTxRet;
import intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.OperationType;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author kevseev
 */
public class EEFinancialTx extends WS {

    private static SpcfLogger logger = Application.getLogger(EEFinancialTx.class);

    public static final String SERVICE_NAME = "EEFinancialTx";


    /**
     * This method executes logic for query request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"169", "194", "168", "166"};
        WSServerContext wsServerContext = new WSServerContext(EEFinancialTx.SERVICE_NAME, "query");

        EEFinancialTxQueryRs eeFinancialTxQueryRs = null;
        String sourceEmployeeId = null;
        String sourcePayrollRunId = null;
        String sourceEmployeeBankAccountId = null;

        try {
            PayrollServices.beginUnitOfWork();

            EEFinancialTxQuery eeFinancialTxQuery =
                    (EEFinancialTxQuery) wsServerContext.translateInputElement(requestDocument);

            eeFinancialTxQueryRs = (EEFinancialTxQueryRs) wsServerContext.getOutputDTO();

            ProcessResult validationResult = new ProcessResult();


            String sourceSystemCode = eeFinancialTxQuery.getSourceSystemCd();
            String sourceCompanyId = eeFinancialTxQuery.getCompanyID();

            //Validate the Company Exist
            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                    sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));

            Employee employee = null;
            EmployeeBankAccount employeeBankAccount = null;

            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                        sourceCompanyId, sourceSystemCode, sourceCompanyId);
            } else {
                //Validate the payroll run Exist
                sourcePayrollRunId = eeFinancialTxQuery.getDDTxBatchID();

                if (sourcePayrollRunId != null) {

                    com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                            findPayrollRun(company, sourcePayrollRunId);

                    if (payrollRun == null) {
                        validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, sourcePayrollRunId,
                                sourcePayrollRunId, sourceSystemCode, sourceCompanyId);
                    }
                }

                //Validate the Employee exist
                sourceEmployeeId = eeFinancialTxQuery.getEmployeeID();

                if (sourceEmployeeId != null) {

                    employee = Employee.findEmployee(company,
                            sourceEmployeeId);
                    if (employee == null) {
                        validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee, sourceEmployeeId,
                                sourceSystemCode, sourceCompanyId, sourceEmployeeId);
                    } else {
                        //Validate the Employee Bank Account Exist
                        sourceEmployeeBankAccountId = eeFinancialTxQuery.getEEBankAccountID();

                        if (sourceEmployeeBankAccountId != null) {
                            employeeBankAccount = EmployeeBankAccount
                                    .findEmployeeBankAccount(employee,
                                            sourceEmployeeBankAccountId);
                            if (employeeBankAccount == null) {
                                validationResult.getMessages().EmployeeBankAccountNotFound(EntityName.EmployeeBankAccount,
                                        sourceEmployeeBankAccountId,
                                        sourceEmployeeBankAccountId,
                                        sourceEmployeeId);
                            }
                        }
                    }
                }
            }

            if (validationResult.isSuccess()) {
                String transactionTypeCode = eeFinancialTxQuery.getTxTypeCd();
                SpcfCalendar settlementDateFrom = null;
                SpcfCalendar settlementDateTo = null;

                if (eeFinancialTxQuery.getTxDateFrom() != null) {
                    settlementDateFrom = CalendarUtils.convertToSpcfCalendar(eeFinancialTxQuery.getTxDateFrom());
                }

                if (eeFinancialTxQuery.getTxDateTo() != null) {
                    settlementDateTo = CalendarUtils.convertToSpcfCalendar(eeFinancialTxQuery.getTxDateTo());
                }

                DomainEntitySet<FinancialTransaction> financialTransactions =
                        FinancialTransaction.findEmployeeFinancialTransactions(company, sourcePayrollRunId, employee,
                                employeeBankAccount,
                                DDCodeToPSP.getTransactionTypeCode(transactionTypeCode),
                                settlementDateFrom, settlementDateTo);

                populateFinancialTransactionsList(eeFinancialTxQueryRs.getEEFinancialTxRet(), financialTransactions);

            }

            eeFinancialTxQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));

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
     * @param pEEFinancialTransactions
     * @param pFinancialTransactions
     * @throws Exception
     */
    private static void populateFinancialTransactionsList(List<EEFinancialTxRet> pEEFinancialTransactions,
                                                          DomainEntitySet<FinancialTransaction> pFinancialTransactions)
            throws Exception {
        FinancialTransaction[] txnArray = pFinancialTransactions.toArray(new FinancialTransaction[0]);
        Arrays.sort(txnArray, new FinancialTransactionComparator());
        for (FinancialTransaction financialTranasction : txnArray) {
            pEEFinancialTransactions.add(buildEEFinancialTxRet(financialTranasction));
        }
    }

    /**
     * Method to build the EEFinancialTxRet DTO from the FinancialTransaction Domain Entity
     *
     * @param pFinancialTransaction
     * @return EEFinancialTxRet
     * @throws Exception
     */
    private static EEFinancialTxRet buildEEFinancialTxRet(FinancialTransaction pFinancialTransaction) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.ObjectFactory financialTxRetFactory =
                new intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.ObjectFactory();

        EEFinancialTxRet eeFinancialTxRet = financialTxRetFactory.createEEFinancialTxRet();

        Company company = pFinancialTransaction.getCompany();

        eeFinancialTxRet.setSourceSystemCd(company.getSourceSystemCd().toString());
        eeFinancialTxRet.setCompanyID(company.getSourceCompanyId());

        PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();

        eeFinancialTxRet.setDDTxBatchID(payrollRun.getSourcePayRunId());

        PaycheckSplit paycheckSplit = pFinancialTransaction.getPaycheckSplit();

        eeFinancialTxRet.setDDTransactionID(paycheckSplit.getSourceDdTxnId());

        EmployeeBankAccount employeeBankAccount = pFinancialTransaction.getEmployeeBankAccount();
        if (employeeBankAccount != null) {
            eeFinancialTxRet.setEEBankAccountID(employeeBankAccount.getSourceBankAccountId());
            eeFinancialTxRet.setBankAccount(DDCommon.build_BankAccount(employeeBankAccount.getBankAccount()));
            eeFinancialTxRet.setEmployeeInfo(DDCommon.build_EmployeeInfo(employeeBankAccount.getEmployee()));
        }

        eeFinancialTxRet.setFinancialTxAmt(SpcfUtils.
                convertToBigDecimal(pFinancialTransaction.getFinancialTransactionAmount()));

        eeFinancialTxRet.setPSETransactionID(pFinancialTransaction.getId().toString());

        eeFinancialTxRet.setTxDate(CalendarUtils.convertToCalendar(pFinancialTransaction.getSettlementDate().toLocal()));

        eeFinancialTxRet.setTxSettlementTypeCd(DDCodeToPSP.getQBOESettlementType(
                pFinancialTransaction.getSettlementTypeCd()));

        TransactionState transactionState = pFinancialTransaction.getCurrentTransactionState();
        eeFinancialTxRet.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
        eeFinancialTxRet.setTxStatusDesc(transactionState.getName());

        TransactionType transactionType = pFinancialTransaction.getTransactionType();
        String sku = pFinancialTransaction.getSku();
        OfferingServiceChargeType offeringServiceCharge = null;
        if (null != sku) {
            offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        }

        String transactionTypeString = DDCodeToPSP.getQBOETransactionTypeCode(pFinancialTransaction.getTransactionType().
                                            getTransactionTypeCd(), offeringServiceCharge);
        eeFinancialTxRet.setTxTypeCd(transactionTypeString);
        eeFinancialTxRet.setTxTypeDesc(transactionType.getName());


        Collection actionCollection =
                pFinancialTransaction.getActionCollection();
        if (actionCollection != null && actionCollection.size() > 0) {
            Collection allowedOperationCollection = eeFinancialTxRet.getAllowedOperation();
            intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.ObjectFactory objectFactory =
                    new intuit.osp.pse.dd.wsapi.xsd.eefinancialtxret.ObjectFactory();

            OperationType allowedOperation = null;
            for (Iterator iterator = actionCollection.iterator(); iterator.hasNext();) {
                ActionEvent actionEvent = (ActionEvent) iterator.next();
                allowedOperation = objectFactory.createOperationType();
                allowedOperation.setOperationCd(actionEvent.getCode().toString());
                allowedOperation.setOperationDesc(actionEvent.getDescription());
                allowedOperationCollection.add(allowedOperation);
            }
        }

        return eeFinancialTxRet;
    }
}
