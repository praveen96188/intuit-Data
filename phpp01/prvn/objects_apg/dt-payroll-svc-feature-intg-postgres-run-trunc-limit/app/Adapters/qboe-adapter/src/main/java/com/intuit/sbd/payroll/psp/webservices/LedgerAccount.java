package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.ActionEvent;
import com.intuit.sbd.payroll.psp.domain.LedgerAccountCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.ledgeraccountquery.LedgerAccountQuery;
import intuit.osp.pse.dd.wsapi.xsd.ledgeraccountqueryrs.LedgerAccountQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.ledgeraccountret.LedgerAccountRet;
import intuit.osp.pse.dd.wsapi.xsd.ledgeraccountret.OperationType;
import intuit.osp.pse.dd.wsapi.xsd.responsestatus.ResponseStatus;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author lkollu
 */
public class LedgerAccount extends WS {

    private static SpcfLogger logger = Application.getLogger(LedgerAccount.class);

    public static final String SERVICE_NAME = "LedgerAccount";

    /**
     * This method executes logic for query request.
     * Returns the ledger account credit balance for a company, ledgerAccountCode
     * and payrollRunId.
     * payrollRunId is optional
     *
     * @param requestDocument
     * @return
     * @throws intuit.osp.common.wsf.base.WSException
     *
     */
    public Element query(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169", "194"};
        WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, LedgerAccount.Operations.QUERY);

        LedgerAccountQueryRs ledgerAccountQueryRs = null;
        String sourcePayrollRunId = null;

        try {
            PayrollServices.beginUnitOfWork();
            LedgerAccountQuery ledgerAccountQuery =
                    (LedgerAccountQuery) wsServerContext.translateInputElement(requestDocument);

            ledgerAccountQueryRs = (LedgerAccountQueryRs) wsServerContext.getOutputDTO();
            ProcessResult validationResult = new ProcessResult();
            String sourceSystemCode = ledgerAccountQuery.getSourceSystemCd();
            String sourceCompanyId = ledgerAccountQuery.getCompanyID();
            com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = null;

            // Validate whether the company exists or not
            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));
            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
            } else {
                // Check if PayrollRun exists for the company
                sourcePayrollRunId = ledgerAccountQuery.getDDTxBatchID();

                if (sourcePayrollRunId != null) {
                    payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(company, sourcePayrollRunId);
                    if (payrollRun == null) {
                        validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, sourcePayrollRunId,
                                sourcePayrollRunId, sourceSystemCode, sourceCompanyId);
                    }
                }
            }

            List retList = ledgerAccountQueryRs.getLedgerAccountRet();
            if (validationResult.isSuccess()) {

                if (ledgerAccountQuery.getLedgerAccountCd() != null) {
                    LedgerAccountCode ledgerAccountCode =
                        DDCodeToPSP.getLedgerAccountCode(ledgerAccountQuery.getLedgerAccountCd());
                    com.intuit.sbd.payroll.psp.domain.LedgerAccount ledgerAccount =
                            PayrollServices.entityFinder.findById(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class, ledgerAccountCode);
                    retList.add(this.buildLedgerAccountRet(ledgerAccount, sourceSystemCode, sourceCompanyId, sourcePayrollRunId, company));
                } else {
                    DomainEntitySet<com.intuit.sbd.payroll.psp.domain.LedgerAccount> collection = PayrollServices.entityFinder.findObjects(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class);
                    collection = collection.sort(com.intuit.sbd.payroll.psp.domain.LedgerAccount.LedgerAccountCd());

                    for (com.intuit.sbd.payroll.psp.domain.LedgerAccount ledgerAccount : collection)
                    {   
                        retList.add(this.buildLedgerAccountRet(ledgerAccount, sourceSystemCode, sourceCompanyId, sourcePayrollRunId, company));
                    }
                }
            } else {
                retList = null;
            }
            PayrollServices.commitUnitOfWork();
            ResponseStatus responseStatus = DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes);
            ledgerAccountQueryRs.setResponseStatus(responseStatus);
            returnDoc = wsServerContext.translateOutputDTO();

        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.info(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            logger.info(exception.getMessage(), exception);
            throw new WSException(DDCommon.pse_Error, exception);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }

    /**
     * This method builds LedgerAccountRet object based on supplied LedgerAccount object.
     *
     * @param pLedgerAccount
     * @param pSourceSystemCode
     * @param pSourceCompanyId
     * @param pSourcePayrollRunId
     * @return
     * @throws Exception
     */
    private LedgerAccountRet buildLedgerAccountRet(com.intuit.sbd.payroll.psp.domain.LedgerAccount pLedgerAccount,
                                                   String pSourceSystemCode,
                                                   String pSourceCompanyId,
                                                   String pSourcePayrollRunId,
                                                   com.intuit.sbd.payroll.psp.domain.Company pCompany) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.ledgeraccountret.ObjectFactory objectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.ledgeraccountret.ObjectFactory();

        LedgerAccountRet ledgerAccountRet = objectFactory.createLedgerAccountRet();

        ledgerAccountRet.setSourceSystemCd(pSourceSystemCode);
        ledgerAccountRet.setCompanyID(pSourceCompanyId);

        ledgerAccountRet.setDDTxBatchID(pSourcePayrollRunId);
        String ledgerAccountCode = DDCodeToPSP.getQBOELedgerAccountCode(pLedgerAccount.getLedgerAccountCd());
        ledgerAccountRet.setLedgerAccountCd(ledgerAccountCode);
        ledgerAccountRet.setLedgerAccountDesc(pLedgerAccount.getName());

        SpcfDecimal balanceAmount;


        if (pSourcePayrollRunId != null) {
            balanceAmount =
                    com.intuit.sbd.payroll.psp.domain.LedgerAccount.getLedgerAccountBalanceByPayroll(pLedgerAccount.getLedgerAccountCd(),
                                                                    pSourcePayrollRunId, pCompany);
        } else {
            balanceAmount =
                    com.intuit.sbd.payroll.psp.domain.LedgerAccount.getLedgerAccountBalance(pCompany, pLedgerAccount.getLedgerAccountCd());
        }


        BigDecimal amount = new BigDecimal(balanceAmount.toString());
        ledgerAccountRet.setLedgerBalance(amount.abs());
        ledgerAccountRet.setCredit(amount.signum() >= 0);

        Collection actionCollection = null;

        if (pSourcePayrollRunId != null) {
            com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(pCompany, pSourcePayrollRunId);
            actionCollection = payrollRun.getValidActions(pLedgerAccount);
        }

        if (actionCollection != null && actionCollection.size() > 0) {
            Collection allowedOperationCollection = ledgerAccountRet.getAllowedOperation();

            OperationType allowedOperation = null;
            for (Iterator iterator = actionCollection.iterator(); iterator.hasNext();) {
                ActionEvent actionEvent = (ActionEvent) iterator.next();
                allowedOperation = objectFactory.createOperationType();
                allowedOperation.setOperationCd(actionEvent.getCode().toString());
                allowedOperation.setOperationDesc(actionEvent.getDescription());
                allowedOperationCollection.add(allowedOperation);
            }
        }

        return ledgerAccountRet;
    }

    /**
     * Interface to store names of operations.
     */
    public interface Operations {

        public static final String QUERY = "query";
    }
}
