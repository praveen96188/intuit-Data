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
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.bankreturnquery.BankReturnQuery;
import intuit.osp.pse.dd.wsapi.xsd.bankreturnqueryrs.BankReturnQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.bankreturnret.BankReturnRet;
import intuit.osp.pse.dd.wsapi.xsd.bankreturnupdate.BankReturnUpdate;
import intuit.osp.pse.dd.wsapi.xsd.bankreturnupdaters.BankReturnUpdateRs;
import intuit.osp.pse.dd.wsapi.xsd.companyinfo.CompanyInfo;
import intuit.osp.pse.dd.wsapi.xsd.employeeinfo.EmployeeInfo;
import org.w3c.dom.Element;

import java.util.*;

/**
 * <p/>
 * File: $Id: //psp/dev/Adapters/QBOE/src/com/intuit/sbd/payroll/psp/webservices/BankReturn.java#1 $
 * <p/>
 * Class: intuit.osp.pse.dd.wsimpl.BankReturn
 * @author rshenderovsky
 */
public class BankReturn extends WS {

    public static final String SERVICE_NAME = "BankReturn";

        private static SpcfLogger logger = Application.getLogger(BankReturn.class);


        private static final intuit.osp.pse.dd.wsapi.xsd.companyinfo.ObjectFactory companyInfoObjectFactory =
            new intuit.osp.pse.dd.wsapi.xsd.companyinfo.ObjectFactory();

    /**
	 * 
	 * @param requestDocument
	 * @return
	 * @throws WSException
	 */
	public Element query(Element requestDocument) throws WSException {
        WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, "query");
        BankReturnQueryRs bankReturnQueryRs;

        String[] expectedErrorCodes = {"169","287","1057"};

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult validationResult = new ProcessResult();

            BankReturnQuery bankReturnQuery = (BankReturnQuery) wsServerContext.translateInputElement(requestDocument);

            bankReturnQueryRs = (BankReturnQueryRs) wsServerContext.getOutputDTO();

            String sourceSystemCode = bankReturnQuery.getSourceSystemCd();
            String sourceCompanyId = bankReturnQuery.getCompanyID();

            if (sourceCompanyId != null) {
                Company company = null;
                if (sourceSystemCode != null) {
                    //Validate Company Exist
                    company = Company.findCompany(sourceCompanyId,
                                        SourceSystemCode.valueOf(sourceSystemCode));
                }

                if (company == null) {
                    validationResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                            sourceCompanyId, sourceSystemCode, sourceCompanyId);
                }
            }

            SpcfCalendar returnDateFrom = null;
            SpcfCalendar returnDateTo = null;
            int startIndexInt = 0;
            int endIndexInt = 0;

            if (bankReturnQuery.getDateFrom() != null) {
                returnDateFrom = CalendarUtils.convertToSpcfCalendar(bankReturnQuery.getDateFrom());
            }

            if (bankReturnQuery.getDateTo() != null) {
                returnDateTo = CalendarUtils.convertToSpcfCalendar(bankReturnQuery.getDateTo());
            }

            if (returnDateFrom != null && returnDateTo != null) {
                if (returnDateFrom.after(returnDateTo)) {
                    logger.info("Transaction Return Date range is invalid");
                    validationResult.getMessages().BankReturnDateRangeInvalid(EntityName.Company,
                            sourceCompanyId);
                }
            }

            if (bankReturnQuery.getStartIndex() != null) {
                startIndexInt = bankReturnQuery.getStartIndex();
            }

            if (bankReturnQuery.getEndIndex() != null) {
                endIndexInt = bankReturnQuery.getEndIndex();
            }

            if (bankReturnQuery.getStartIndex() != null && bankReturnQuery.getEndIndex() != null
                    && (startIndexInt > endIndexInt || startIndexInt == 0 || endIndexInt == 0)) {

                logger.info("Transaction Return record index range is invalid: "
                        + startIndexInt + " to " + endIndexInt);

                validationResult.getMessages().TransactionReturnIndexRangeInvalid(EntityName.Company,
                        sourceCompanyId);
            }

            if (validationResult.isSuccess()) {
                Collection pseTransactionIdCollection = new ArrayList();
                Collection<TransactionReturnStatusCode> returnStatusCodeCollection =
                        new ArrayList<TransactionReturnStatusCode>();
                if (bankReturnQuery.getReturnStatusCd() != null & bankReturnQuery.getReturnStatusCd().size() > 0) {
                    for (int i = 0; i < bankReturnQuery.getPseTransactionID().size(); i++) {
                        pseTransactionIdCollection.add(SpcfUniqueId.createInstance((String)
                                bankReturnQuery.getPseTransactionID().get(i)));
                    }
                }

                int maxResults = 0;

                //If condition to calculate the maxResults based on the start index & endIndex.If both start index &
                // end index are null maxResults will be passed as zero, if start index is null then endindexInt will
                //be passed as maxResults, other wise maxResults will be calcuated by substracting startIndexInt from
                if (bankReturnQuery.getStartIndex() != null && bankReturnQuery.getEndIndex() != null) {
                    maxResults = endIndexInt - startIndexInt + 1;
                }
                else if (bankReturnQuery.getStartIndex() == null && bankReturnQuery.getEndIndex() != null) {
                    maxResults = endIndexInt;
                }
                if (bankReturnQuery.getPseTransactionID() != null & bankReturnQuery.getPseTransactionID().size() > 0) {
                    for (int i = 0; i < bankReturnQuery.getReturnStatusCd().size(); i++) {
                        returnStatusCodeCollection.add(DDCodeToPSP.getBankReturnStatusCode((String)
                                bankReturnQuery.getReturnStatusCd().get(i)));
                    }
                }
                DomainEntitySet<TransactionReturn> transactionReturnList = new DomainEntitySet<TransactionReturn>();

                int totalRecordCount = TransactionReturn.
                        getTransactionReturnCollection(SourceSystemCode.valueOf(sourceSystemCode), sourceCompanyId,
                                bankReturnQuery.getFEIN(), pseTransactionIdCollection,
                                returnStatusCodeCollection, returnDateFrom, returnDateTo, startIndexInt,
                                maxResults, transactionReturnList);

                populateBankReturnRetList(bankReturnQueryRs.getBankReturnRet(), transactionReturnList);

                bankReturnQueryRs.setTotalRecordCount(new Integer(totalRecordCount));
            }

            bankReturnQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));

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

    private static void populateBankReturnRetList(List<BankReturnRet> bankReturnRetList,
                                                  DomainEntitySet<TransactionReturn> pTxnReturnCollection) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.bankreturnret.ObjectFactory bankReturnObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.bankreturnret.ObjectFactory();

        for (TransactionReturn transactionReturn : pTxnReturnCollection) {
            BankReturnRet bankReturnRet = bankReturnObjectFactory.createBankReturnRet();
            bankReturnRetList.add(bankReturnRet);
            copy(bankReturnRet, transactionReturn);
        }
    }

    private static void copy(BankReturnRet pBankReturnRet, TransactionReturn pTransactionReturn) throws Exception {
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.
                findFinancialTransaction(pTransactionReturn);

        FinancialTransaction financialTransaction = finTxnList.get(0);

        Company company = financialTransaction.getCompany();

        pBankReturnRet.setSourceSystemCd(company.getSourceSystemCd().toString());

        CompanyInfo companyInfo = companyInfoObjectFactory.createCompanyInfo();
        companyInfo.setCompanyID(company.getSourceCompanyId());
        companyInfo.setFEIN(company.getFedTaxId());
        companyInfo.setLegalName(company.getLegalName());

        pBankReturnRet.setCompanyInfo(companyInfo);

        pBankReturnRet.setPseReturnID(pTransactionReturn.getId().toString());
        pBankReturnRet.setPseTransactionID(financialTransaction.getId().toString());
        pBankReturnRet.setBankReturnCd(pTransactionReturn.getBankReturnCd());
        pBankReturnRet.setBankReturnDesc(financialTransaction.getTransactionType().getName());
        Calendar returnDate = CalendarUtils.convertToCalendar(pTransactionReturn.getReturnBatch().getReturnDate().toLocal());
        pBankReturnRet.setReturnDate(returnDate);

        pBankReturnRet.setReturnStatusCd(DDCodeToPSP.getQBOEBankReturnStatus(pTransactionReturn.getReturnStatusCd()));
        pBankReturnRet.setReturnStatusDesc(pTransactionReturn.getReturnStatusCd().toString());
        String sku = financialTransaction.getSku();
        OfferingServiceChargeType offeringServiceCharge = null;
        if (null != sku) {
            offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        }
        String transactionType = DDCodeToPSP.getQBOETransactionTypeCode(financialTransaction.getTransactionType().
            getTransactionTypeCd(), offeringServiceCharge);
        pBankReturnRet.setTxTypeCode(transactionType);

        Calendar settlementDate = CalendarUtils.convertToCalendar(financialTransaction.
                getSettlementDate().toLocal());

        pBankReturnRet.setTxDate(settlementDate);
        pBankReturnRet.setFinancialTxAmt(SpcfUtils.convertToBigDecimal(financialTransaction.
                getFinancialTransactionAmount()));

        PayrollRun payrollRun = financialTransaction.getPayrollRun();

        if (payrollRun != null) {
            pBankReturnRet.setPaycheckDepositDate(CalendarUtils.convertToCalendar(payrollRun.getPaycheckDate().toLocal()));
            pBankReturnRet.setDDTxBatchID(payrollRun.getSourcePayRunId());
        }

        // Bank Info
        intuit.osp.pse.dd.wsapi.xsd.bankaccount.ObjectFactory bankAccountObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.bankaccount.ObjectFactory();

        BankAccountOwnerType creditBankAccountType = financialTransaction.
                getCreditBankAccountType();

        intuit.osp.pse.dd.wsapi.xsd.bankaccount.BankAccount bankAccount = bankAccountObjectFactory.createBankAccount();
        com.intuit.sbd.payroll.psp.domain.BankAccount domainBankAccount;

        if (!BankAccountOwnerType.Intuit.equals(creditBankAccountType)) {
            // if intuit is not on a credit side return credit bank account
            domainBankAccount = financialTransaction.getCreditBankAccount();
        } else {
            domainBankAccount = financialTransaction.getDebitBankAccount();
        }

        bankAccount.setAccountNumber(domainBankAccount.getAccountNumber());
        bankAccount.setRoutingNumber(domainBankAccount.getRoutingNumber());
        bankAccount.setAccountType(DDCodeToPSP.getQBOEBankAccountType(domainBankAccount.getAccountTypeCd()));
        bankAccount.setBankName(domainBankAccount.getBankName());

        pBankReturnRet.setBankAccount(bankAccount);

        PaycheckSplit payCheckSplit = financialTransaction.getPaycheckSplit();
        Employee domainEmployeeInfo = null;

        if (payCheckSplit != null) {
            Paycheck payCheck = payCheckSplit.getPaycheck();

            if (payCheck != null) {
                domainEmployeeInfo = payCheck.getDDEmployee();
            }
        }

        if (domainEmployeeInfo != null) {
            intuit.osp.pse.dd.wsapi.xsd.employeeinfo.ObjectFactory employeeInfoObjectFactory =
                    new intuit.osp.pse.dd.wsapi.xsd.employeeinfo.ObjectFactory();

            EmployeeInfo employeeInfo = employeeInfoObjectFactory.createEmployeeInfo();

            employeeInfo.setEmployeeID(domainEmployeeInfo.getSourceEmployeeId());
            employeeInfo.setFirstName(domainEmployeeInfo.getFirstName());
            employeeInfo.setLastName(domainEmployeeInfo.getLastName());
            employeeInfo.setMiddleName(domainEmployeeInfo.getMiddleName());
            employeeInfo.setSocialSecurityNumber(domainEmployeeInfo.getTaxId());

            employeeInfo.setEmployeeStatusCd(DDCodeToPSP.getQBOEEmployeeStatus(domainEmployeeInfo.getStatusCd()));

            pBankReturnRet.setEmployeeInfo(employeeInfo);
        }
    }

    /**
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element update(Element requestDocument) throws WSException {
        Element returnDoc;

        String[] expectedErrorCodes = {"137","138","125","169","177","264"};

        try{
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, Operations.UPDATE);
            BankReturnUpdateRs bankReturnUpdateRs = (BankReturnUpdateRs) wsServerContext.getOutputDTO();
            BankReturnUpdate bankReturnUpdate =
                                   (BankReturnUpdate)wsServerContext.translateInputElement(requestDocument);

            ProcessResult processResult = PayrollServices.financialTransactionManager.updateBankReturnStatus(
                                                      SourceSystemCode.valueOf(bankReturnUpdate.getSourceSystemCd()),
                                                      bankReturnUpdate.getCompanyId(),
                                                      bankReturnUpdate.getPseTransactionId(),
                                                      DDCodeToPSP.getBankReturnStatusCode(bankReturnUpdate.getBankReturnStatus()),
                                                      bankReturnUpdate.getCompanyNote());
            if (! processResult.isSuccess()) {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        bankReturnUpdate.getCompanyId(),
                        SourceSystemCode.valueOf(bankReturnUpdate.getSourceSystemCd()));
                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
            }
            bankReturnUpdateRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            BankReturn.logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            BankReturn.logger.error(exception.getMessage(), exception);
			throw new WSException(DDCommon.pse_Error, exception);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;
    }

    /**
	 * 
	 */
 	public interface Operations {
		static final String QUERY = "query";
        static final String UPDATE = "update";
    }

}
