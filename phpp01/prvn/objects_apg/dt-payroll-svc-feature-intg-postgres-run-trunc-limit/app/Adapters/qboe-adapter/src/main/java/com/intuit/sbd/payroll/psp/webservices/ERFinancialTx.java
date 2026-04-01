package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxcancel.ERFinancialTxCancel;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxcancelrs.ERFinancialTxCancelRs;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxquery.ERFinancialTxQuery;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxqueryrs.ERFinancialTxQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxrefund.ERFinancialTxRefund;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxrefundrs.ERFinancialTxRefundRs;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxret.ERFinancialTxRet;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtxret.OperationType;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author kevseev
 */
public class ERFinancialTx extends WS {

    private static SpcfLogger logger = Application.getLogger(ERFinancialTx.class);
    public static final String SERVICE_NAME = "ERFinancialTx";


    /**
     * This method executes logic for cancel request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element cancel(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "125", "169", "177", "1010", "264", "1051"};

        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(ERFinancialTx.SERVICE_NAME, ERFinancialTx.Operations.CANCEL);
            ERFinancialTxCancel erFinancialTxCancel =
                    (ERFinancialTxCancel) wsServerContext.translateInputElement(requestDocument);

            ERFinancialTxCancelRs erFinancialTxCancelRs = (ERFinancialTxCancelRs) wsServerContext.getOutputDTO();

            ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();
            if (erFinancialTxCancel != null) {
                processResult = PayrollServices.financialTransactionManager.cancelTransaction(
                        SourceSystemCode.valueOf(erFinancialTxCancel.getSourceSystemCd()),
                        erFinancialTxCancel.getCompanyID(), erFinancialTxCancel.getPSETransactionID());

            }

            if (! processResult.isSuccess()) {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        erFinancialTxCancel.getCompanyID(),
                        SourceSystemCode.valueOf(erFinancialTxCancel.getSourceSystemCd()));

                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
            }
            erFinancialTxCancelRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        } catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(exception.getMessage(), exception);
            throw new WSException(DDCommon.pse_Error, exception);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }

    /**
     * This method executes logic for refund request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element refund(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "125", "169", "177", "264", "265", "1051", "286", "121", "186", "170", "1010", "267", "269", "271", "266"};

        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(ERFinancialTx.SERVICE_NAME, ERFinancialTx.Operations.REFUND);
            ERFinancialTxRefundRs erFinancialTxRefundRs = null;
            ERRefundDTO erRefundDTO = new ERRefundDTO();

            ERFinancialTxRefund erFinancialTxRefund = (ERFinancialTxRefund) wsServerContext.translateInputElement(requestDocument);
            ProcessResult processResult = new ProcessResult();
            erFinancialTxRefundRs = (ERFinancialTxRefundRs) wsServerContext.getOutputDTO();
            if (erFinancialTxRefund != null) {
                build_ERRefundDTO(erRefundDTO, erFinancialTxRefund);
                processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                        SourceSystemCode.valueOf(erFinancialTxRefund.getSourceSystemCd()),
                        erFinancialTxRefund.getCompanyID(), erRefundDTO);
                if (! processResult.isSuccess()) {
                    com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                            erFinancialTxRefund.getCompanyID(),
                            SourceSystemCode.valueOf(erFinancialTxRefund.getSourceSystemCd()));
                    DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
                }
            }
            erFinancialTxRefundRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }

    private void build_ERRefundDTO(ERRefundDTO pERRefundDTO, ERFinancialTxRefund pERFinancialTxRefund) {
        SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(pERFinancialTxRefund.getTxSettlementTypeCd());
        SpcfMoney financialTxAmt = null;

        if (pERFinancialTxRefund.getFinancialTxAmt() != null) {
            financialTxAmt = new SpcfMoney(pERFinancialTxRefund.getFinancialTxAmt().toString());
        }

        SpcfCalendar txDate = null;
        if (pERFinancialTxRefund.getTxDate() != null) {
            txDate = CalendarUtils.convertToSpcfCalendar(pERFinancialTxRefund.getTxDate());
            pERRefundDTO.setTxDate(new DateDTO(txDate));
        }

        pERRefundDTO.setSettlementType(settlementType);
        pERRefundDTO.setFinancialTxId(pERFinancialTxRefund.getPSETransactionID());
        pERRefundDTO.setFinancialTxAmt(financialTxAmt);
    }

    /**
     * This method executes logic for query request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element query(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"169", "194", "170"};
        WSServerContext wsServerContext = new WSServerContext(ERFinancialTx.SERVICE_NAME,
                ERFinancialTx.Operations.QUERY);

        ERFinancialTxQueryRs erFinancialTxQueryRs = null;
        String sourcePayrollRunId = null;
        ProcessResult validationResult = new ProcessResult();
        try {
            PayrollServices.beginUnitOfWork();

            ERFinancialTxQuery erFinancialTxQuery =
                    (ERFinancialTxQuery) wsServerContext.translateInputElement(requestDocument);

            erFinancialTxQueryRs = (ERFinancialTxQueryRs) wsServerContext.getOutputDTO();

            if (erFinancialTxQuery != null) {
                String sourceSystemCd = erFinancialTxQuery.getSourceSystemCd();
                String sourceCompanyId = erFinancialTxQuery.getCompanyID();

                // Validate the company exists
                com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                        sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

                com.intuit.sbd.payroll.psp.domain.CompanyBankAccount companyBankAccount = null;

                if (company == null) {
                    validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                            sourceSystemCd, sourceCompanyId);
                } else {
                    // Validate the PayrollRun Exists
                    sourcePayrollRunId = erFinancialTxQuery.getDDTxBatchID();

                    if (sourcePayrollRunId != null) {
                        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                                findPayrollRun(company, sourcePayrollRunId);

                        if (payrollRun == null) {
                            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun,
                                    sourcePayrollRunId, sourcePayrollRunId, sourceSystemCd, sourceCompanyId);
                        }
                    }
                    //Validate the CompanyBank Account Exists
                    String sourceCompanyBankAccountId = erFinancialTxQuery.getCompanyBankAccountID();

                    if (sourceCompanyBankAccountId != null) {
                        companyBankAccount =
                                CompanyBankAccount.findCompanyBankAccount(company,
                                        sourceCompanyBankAccountId);

                        if (companyBankAccount == null) {
                            validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                                    sourceCompanyBankAccountId, sourceCompanyBankAccountId, sourceSystemCd,
                                    sourceCompanyId);
                        }
                    }
                }

                if (validationResult.isSuccess()) {
                    String transactionTypeCode = erFinancialTxQuery.getTxTypeCd();

                    SpcfCalendar settlementDateFrom = null;
                    SpcfCalendar settlementDateTo = null;

                    if (erFinancialTxQuery.getTxDateFrom() != null) {
                        settlementDateFrom = CalendarUtils.convertToSpcfCalendar(erFinancialTxQuery.getTxDateFrom());
                    }

                    if (erFinancialTxQuery.getTxDateTo() != null) {
                        settlementDateTo = CalendarUtils.convertToSpcfCalendar(erFinancialTxQuery.getTxDateTo());
                    }

                    DomainEntitySet<FinancialTransaction> financialTranasctions =
                            FinancialTransaction.findFinancialTransactions(company,
                                    sourcePayrollRunId, null, null, companyBankAccount,
                                    DDCodeToPSP.getTransactionTypeCode(transactionTypeCode),
                                    settlementDateFrom, settlementDateTo, null);

                    populateFinancialTransactionsList(erFinancialTxQueryRs.getERFinancialTxRet(), financialTranasctions);
                }
            }

            erFinancialTxQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes));

            PayrollServices.commitUnitOfWork();
            return wsServerContext.translateOutputDTO();
        } catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e);
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Method to populate the financial transaction list from the SpcfList
     *
     * @param pERFinancialTransactions
     * @param pFinancialTransactions
     * @throws Exception
     */
    private static void populateFinancialTransactionsList(List<ERFinancialTxRet> pERFinancialTransactions,
                                                          DomainEntitySet<FinancialTransaction> pFinancialTransactions)
            throws Exception {

        FinancialTransaction[] txnArray = pFinancialTransactions.toArray(new FinancialTransaction[0]);
        Arrays.sort(txnArray, new FinancialTransactionComparator());
        for (FinancialTransaction financialTranasction : txnArray) {
            if (TransactionType.isEmployerTransactionType(financialTranasction.getTransactionType().
                    getTransactionTypeCd())) {
                pERFinancialTransactions.add(buildERFinancialTxRet(financialTranasction));
            }
        }
    }

    /**
     * This method builds ERFinancialTxRet object based on supplied FinancialTransaction object.
     *
     * @param pFinancialTransaction
     * @return ERFinancialTxRet
     * @throws Exception
     */
    private static ERFinancialTxRet buildERFinancialTxRet(FinancialTransaction pFinancialTransaction) throws Exception {

        intuit.osp.pse.dd.wsapi.xsd.erfinancialtxret.ObjectFactory objectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.erfinancialtxret.ObjectFactory();

        ERFinancialTxRet erFinancialTxRet = objectFactory.createERFinancialTxRet();

        Company company = pFinancialTransaction.getCompany();

        erFinancialTxRet.setSourceSystemCd(company.getSourceSystemCd().toString());
        erFinancialTxRet.setCompanyID(company.getSourceCompanyId());

        PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();

        if (payrollRun != null) {
            // pFinancialTransaction.getCreditBankAccountType();
            erFinancialTxRet.setDDTxBatchID(payrollRun.getSourcePayRunId());
            CompanyBankAccount companyBankAccount = pFinancialTransaction.getCompanyBankAccount();
            if (companyBankAccount != null) {
                erFinancialTxRet.setCompanyBankAccountID(companyBankAccount.getSourceBankAccountId());
                erFinancialTxRet.setBankAccount(DDCommon.build_BankAccount(companyBankAccount.getBankAccount()));
            }
        }

        erFinancialTxRet.setFinancialTxAmt(SpcfUtils.convertToBigDecimal(
                pFinancialTransaction.getFinancialTransactionAmount()));

        erFinancialTxRet.setPSETransactionID(pFinancialTransaction.getId().toString());

        erFinancialTxRet.setTxDate(CalendarUtils.convertToCalendar(
                pFinancialTransaction.getSettlementDate().toLocal()));

        erFinancialTxRet.setTxSettlementTypeCd(DDCodeToPSP.getQBOESettlementType(
                pFinancialTransaction.getSettlementTypeCd()));

        TransactionState transactionState = pFinancialTransaction.getCurrentTransactionState();
        erFinancialTxRet.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
        erFinancialTxRet.setTxStatusDesc(transactionState.getName());

        TransactionType transactionType = pFinancialTransaction.getTransactionType();
        String sku = pFinancialTransaction.getSku();
        OfferingServiceChargeType offeringServiceCharge = null;
        if (null != sku) {
            offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        }
        String transactionTypeString = DDCodeToPSP.getQBOETransactionTypeCode(pFinancialTransaction.getTransactionType().
            getTransactionTypeCd(), offeringServiceCharge);
        erFinancialTxRet.setTxTypeCd(transactionTypeString);
        erFinancialTxRet.setTxTypeDesc(transactionType.getName());

        Collection actionCollection =
                pFinancialTransaction.getActionCollection();
        List<ActionEvent> actionList = new ArrayList<ActionEvent>(actionCollection);
        Collections.sort(actionList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                ActionEvent actionEvent1 = (ActionEvent)obj1;
                ActionEvent actionEvent2 = (ActionEvent)obj2;
                return actionEvent1.getCode().compareTo(actionEvent2.getCode());
            }
        });
        if (actionList != null && actionList.size() > 0) {
            Collection allowedOperationCollection = erFinancialTxRet.getAllowedOperation();

            OperationType allowedOperation = null;
            for (Iterator iterator = actionList.iterator(); iterator.hasNext();) {
                ActionEvent actionEvent = (ActionEvent) iterator.next();
                allowedOperation = objectFactory.createOperationType();
                allowedOperation.setOperationCd(actionEvent.getCode().toString());
                allowedOperation.setOperationDesc(actionEvent.getDescription());
                allowedOperationCollection.add(allowedOperation);
            }
        }


        return erFinancialTxRet;
    }

    /**
     * Interface to store names of operations.
     */
    public interface Operations {

        public static final String QUERY = "query";
        public static final String CANCEL = "cancel";
        public static final String REFUND = "refund";
    }
}
