package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.FeeTransferDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionReverseDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.XmlUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.common.wsf.server.WSServerContext;
import intuit.osp.pse.dd.wsapi.xsd.baddebtwriteoff.BadDebtWriteOff;
import intuit.osp.pse.dd.wsapi.xsd.baddebtwriteoffrs.BadDebtWriteOffRs;
import intuit.osp.pse.dd.wsapi.xsd.ddredebitadd.DDRedebitAdd;
import intuit.osp.pse.dd.wsapi.xsd.ddredebitaddrs.DDRedebitAddRs;
import intuit.osp.pse.dd.wsapi.xsd.ddrefund.DDRefund;
import intuit.osp.pse.dd.wsapi.xsd.ddrefundrs.DDRefundRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactioncancel.DDTransactionCancel;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactioncancelrs.DDTransactionCancelRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionrecall.DDTransactionRecall;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionrecallrs.DDTransactionRecallRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionreverse.DDTransactionReverse;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionreversers.DDTransactionReverseRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionsubmit.DDTransactionSubmit;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionsubmitrs.DDTransactionSubmitRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionvoidtx.DDTransactionVoidTx;
import intuit.osp.pse.dd.wsapi.xsd.ddtransactionvoidtxrs.DDTransactionVoidTxRs;
import intuit.osp.pse.dd.wsapi.xsd.ddtx.DDTx;
import intuit.osp.pse.dd.wsapi.xsd.ddtxbatchret.DDTxBatchRet;
import intuit.osp.pse.dd.wsapi.xsd.ddtxrecallbatch.DDTxRecallBatch;
import intuit.osp.pse.dd.wsapi.xsd.eecheck.EECheck;
import intuit.osp.pse.dd.wsapi.xsd.eefinancialtx.EEFinancialTx;
import intuit.osp.pse.dd.wsapi.xsd.eereturnrefund.EEReturnRefund;
import intuit.osp.pse.dd.wsapi.xsd.eereturnrefundrs.EEReturnRefundRs;
import intuit.osp.pse.dd.wsapi.xsd.eereturntransfer.EEReturnTransfer;
import intuit.osp.pse.dd.wsapi.xsd.eereturntransferrs.EEReturnTransferRs;
import intuit.osp.pse.dd.wsapi.xsd.erfeeadd.ERFeeAdd;
import intuit.osp.pse.dd.wsapi.xsd.erfeeaddrs.ERFeeAddRs;
import intuit.osp.pse.dd.wsapi.xsd.erfinancialtx.ERFinancialTx;
import intuit.osp.pse.dd.wsapi.xsd.erreturnrefund.ERReturnRefund;
import intuit.osp.pse.dd.wsapi.xsd.erreturnrefundrs.ERReturnRefundRs;
import intuit.osp.pse.dd.wsapi.xsd.escalationadd.EscalationAdd;
import intuit.osp.pse.dd.wsapi.xsd.escalationaddrs.EscalationAddRs;
import intuit.osp.pse.dd.wsapi.xsd.feeredebitadd.FeeRedebitAdd;
import intuit.osp.pse.dd.wsapi.xsd.feeredebitaddrs.FeeRedebitAddRs;
import intuit.osp.pse.dd.wsapi.xsd.feetransfer.FeeTransfer;
import intuit.osp.pse.dd.wsapi.xsd.feetransferrs.FeeTransferRs;
import intuit.osp.pse.dd.wsapi.xsd.intuit5dayreturntransfer.Intuit5DayReturnTransfer;
import intuit.osp.pse.dd.wsapi.xsd.intuit5dayreturntransferrs.Intuit5DayReturnTransferRs;
import intuit.osp.pse.dd.wsapi.xsd.transactionresponseret.TransactionResponseRet;
import intuit.osp.pse.dd.wsapi.xsd.transactionsync.TransactionSync;
import intuit.osp.pse.dd.wsapi.xsd.transactionsyncrs.TransactionSyncRs;
import intuit.osp.pse.dd.wsapi.xsd.txhistoryquery.TxHistoryQuery;
import intuit.osp.pse.dd.wsapi.xsd.txhistoryqueryrs.TxHistoryQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.txhistoryret.TxHistoryRet;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * This is the <code>Employee</code> web service object that allows for
 * adding, updating, deactivating an employee
 *
 * @author Sean Barenz
 */
public class DDTransaction extends WS {
    private static SpcfLogger logger = Application.getLogger(DDTransaction.class);

    public static final String SERVICE_NAME = "DDTransaction";

//    private SpcfLogger mLogger = SpcfLogManager.getLogger(DDTransaction.class);

    /**
     * Interface to store names of operations.
     */
    public interface Operations {
        public static final String BAD_DEBT_RECOVER = "badDebtRecover";
        public static final String BAD_DEBT_WRITE_OFF = "badDebtWriteOff";
        public static final String CANCEL = "cancel";
        public static final String DD_REFUND = "ddRefund";
        public static final String EE_RETURN_REFUND = "eeReturnRefund";
        public static final String EE_RETURN_TRANSFER = "eeReturnTransfer";
        public static final String ER_FEE_ADD = "erFeeAdd";
        public static final String ER_RETURN_REFUND = "erReturnRefund";
        public static final String ESCALATION_ADD = "escalationAdd";
        public static final String FEE_REDEBIT_ADD = "feeRedebitAdd";
        public static final String FEE_TRANSFER = "feeTransfer";
        public static final String INTUIT_5_DAY_RETURN_TRANSFER = "intuit5DayReturnTransfer";
        public static final String RECALL = "recall";
        public static final String REDEBIT_ADD = "redebitAdd";
        public static final String REPAYMENT_RECORD = "repaymentRecord";
        public static final String REVERSE = "reverse";
        public static final String REVERSAL_RECORD = "reversalRecord";
        public static final String SUBMIT = "submit";
        public static final String SYNC = "sync";
        public static final String VOIDTX = "voidTx";
        public static final String TXHISTORYQUERY = "txHistoryQuery";
    }

    public Element submit(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"10012", "169", "1008", "5001", "103", "121", "255", "137", "138", "125", "177", "183", "109", "170",
                "186", "168", "178", "142", "164", "166", "187", "184", "185", "10002", "10001", "1010", "1011", "252", "1043"};

        String sourceSystemTransmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO transmissionDTO =
                new SourceSystemTransmissionDTO(TransmissionType.PayrollSubmission, XmlUtils.xmlToString(requestDoc.getOwnerDocument()));
        transmissionDTO.setRequestToken(0L);
        DDTransactionSubmit ddTransactionSubmit = null;

        try {
            WSServerContext context = new WSServerContext("DDTransaction", "submit");
            ddTransactionSubmit = (DDTransactionSubmit) context.translateInputElement(requestDoc);

            ProcessResult prUnhandled = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE, ddTransactionSubmit.getCompanyID(), sourceSystemTransmissionId, transmissionDTO);
            if (! prUnhandled.isSuccess()) {
                logger.error("submit(): Unhandled ProcessResult failure from TransmissionManager.initializeTransmission(): "+prUnhandled.toString());
            }

            PayrollServices.beginUnitOfWork();

            DDTransactionSubmitRs ddTransactionSubmitRs = null;
            ddTransactionSubmitRs = (DDTransactionSubmitRs) context.getOutputDTO();

            Integer token = ddTransactionSubmit.getToken();
            String requestId = ddTransactionSubmit.getRequestID();

            Company domainCompany = Company.findCompany(ddTransactionSubmit
                    .getCompanyID(), SourceSystemCode.valueOf(ddTransactionSubmit.getSourceSystemCd()));

            ProcessResult<PayrollRun> result = new ProcessResult<PayrollRun>();

            if (domainCompany == null) {
                result.getMessages().CompanyDoesNotExist(EntityName.Company, ddTransactionSubmit
                        .getCompanyID(), ddTransactionSubmit
                        .getSourceSystemCd(), ddTransactionSubmit.getCompanyID());
                ddTransactionSubmitRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
            }
            else {
                // Find Transaction Response for RequestId
                TransactionResponse existingTxnRspForReqId = TransactionResponse
                        .findTransactionResponses(domainCompany, requestId);

                // If a transaction response already exists for this request id,
                // create an error; otherwise, process the payroll run
                if (existingTxnRspForReqId != null) {
                    ddTransactionSubmitRs.getTransactionResponseRet().add(
                            buildTransactionResponseRet(existingTxnRspForReqId));

                    // Add a message to the response that this request id already
                    // exists

                    result.getMessages().RequestIdAlreadyExists(EntityName.TransactionResponse,
                                                                existingTxnRspForReqId.getSourceRequestId(),
                                                                existingTxnRspForReqId.getSourceRequestId(),
                                                                domainCompany.getSourceSystemCd().toString(),
                                                                domainCompany.getSourceCompanyId());
                    ddTransactionSubmitRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
                }
                else {
                    // Create PayrollRunDTO
                    PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
                    payrollRunDTO.setTransmissionId(sourceSystemTransmissionId);
                    createPayrollRunDTOFromWSDTO(payrollRunDTO, ddTransactionSubmit);

                    // Call submit payroll process
                    result = PayrollServices.payrollManager.submitPayroll(domainCompany
                            .getSourceSystemCd(), domainCompany.getSourceCompanyId(),
                                                  payrollRunDTO);

                    // If submit payroll processing was successful, create and
                    // save a transaction response for the payroll
                    if (result.isSuccess()) {
                        // Get Transaction Responses for token
                        DomainEntitySet<TransactionResponse> transactionResponses = TransactionResponse
                                .findTransactionResponses(domainCompany, token.longValue());
                        if (transactionResponses.size() > 0) {
                            for (TransactionResponse currTxnResponse : transactionResponses) {
                                ddTransactionSubmitRs.getTransactionResponseRet().add(
                                        buildTransactionResponseRet(currTxnResponse));
                            }
                        }
                        else {
                            ddTransactionSubmitRs.getTransactionResponseRet().clear();
                        }

                        // Create transaction response for payroll
                        TransactionResponse payrollTxnRsp = createTxnResponseFromPayRunForSubmitPayroll(
                                result.getResult(), requestId);
                        ddTransactionSubmitRs.getTransactionResponseRet().add(
                                buildTransactionResponseRet(payrollTxnRsp));
                    }
                    else {
                        DDCommon.replacePSPError(result, "1101", "10012", domainCompany);
                        ddTransactionSubmitRs.getTransactionResponseRet().clear();
                    }
                }
            }

            // PSRV000502:
            // we only want the 10012 to trump other errors if the CompanyService status is Terminated or Cancelled...
            // if the CompanyService status is something else, and if there's any other error code among the errors,
            // then we ignore the 10012 and select another (more specific) error
            Message msg10012 = DDCommon.findMessage(result, "10012");
            if (msg10012 != null) {
                CompanyService cs = CompanyService.findCompanyService(domainCompany, ServiceCode.DirectDeposit);
                if (cs.getStatusCd()!=ServiceSubStatusCode.Terminated && cs.getStatusCd()!=ServiceSubStatusCode.Cancelled) {
                    for (Message msg : result.getMessages()) {
                        if (! msg.getMessageCode().equals("10012")) {
                            result.getMessages().remove(msg10012);
                            break;
                        }
                    }
                }
            }

            ddTransactionSubmitRs.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
            if (returnDoc != null) {
                transmissionDTO.setResponseDocument(XmlUtils.xmlToString(returnDoc.getOwnerDocument()));
            }
            PayrollServices.commitUnitOfWork();
            prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, ddTransactionSubmit.getCompanyID(), sourceSystemTransmissionId, transmissionDTO);
            if (! prUnhandled.isSuccess()) {
                logger.error("submit(): Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): "+prUnhandled.toString());
            }
        }
        catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());

            PayrollServices.rollbackUnitOfWork();

            transmissionDTO.setResponseDocument("Error: " + e.getMessage());
            ProcessResult prUnhandled;
            if(ddTransactionSubmit != null){
                prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, ddTransactionSubmit.getCompanyID(), sourceSystemTransmissionId, transmissionDTO);
            } else {
                prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, null, sourceSystemTransmissionId, transmissionDTO);
            }
            if (! prUnhandled.isSuccess()) {
                logger.error("submit(): Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): "+prUnhandled.toString());
            }

            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());

            PayrollServices.rollbackUnitOfWork();

            transmissionDTO.setResponseDocument("Error: " + e.getMessage());
            ProcessResult prUnhandled;
            if(ddTransactionSubmit != null){
                prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, ddTransactionSubmit.getCompanyID(), sourceSystemTransmissionId, transmissionDTO);
            }else{
                prUnhandled = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE, null, sourceSystemTransmissionId, transmissionDTO);
            }
            if (! prUnhandled.isSuccess()) {
                logger.error("submit(): Unhandled ProcessResult failure from TransmissionManager.finalizeTransmission(): "+prUnhandled.toString());
            }

            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    private TransactionResponse createTxnResponseFromPayRunForSubmitPayroll(PayrollRun pPayrollRun,
                                                                            String pSourceRequestId) {
        TransactionResponse txnResponse = new TransactionResponse();
        txnResponse.setCompany(pPayrollRun.getCompany());
        txnResponse.setSourceRequestId(pSourceRequestId);
        txnResponse.setTransactionTokenNumber(TransactionResponse.getNextTxnResponseToken());

        txnResponse = Application.save(txnResponse);

        for(FinancialTransaction currFinTxn : pPayrollRun.getFinancialTransactionCollection()) {
            if (currFinTxn.getTransactionType().getTransactionTypeCd().equals(
                    TransactionTypeCode.EmployerDdDebit)
                    || currFinTxn.getTransactionType().getTransactionTypeCd().equals(
                    TransactionTypeCode.EmployeeDdCredit)) {
                DomainEntitySet<FinancialTransactionState> financialTransactionStates = currFinTxn
                        .getFinancialTransactionStates();
                for (FinancialTransactionState currFinTxnState : financialTransactionStates) {
                    currFinTxnState.setTransactionResponse(txnResponse);
                    Application.save(currFinTxnState);
                }
            }
        }

        return txnResponse;
    }

    private void createPayrollRunDTOFromWSDTO(PayrollRunDTO pPayrollRunDTO,
                                              DDTransactionSubmit pWSDTO) {
        DateDTO dtoTxnDate = new DateDTO();
        Calendar calendarTxnDate = pWSDTO.getDDTxBatch().getTargetDDTxDate();
        dtoTxnDate.set(calendarTxnDate.get(Calendar.YEAR), calendarTxnDate.get(Calendar.MONTH),
                       calendarTxnDate.get(Calendar.DAY_OF_MONTH));
        pPayrollRunDTO.setTargetPayrollTXDate(dtoTxnDate);
        pPayrollRunDTO.setPayrollTXBatchId(pWSDTO.getDDTxBatch().getDDTxBatchID());

        String ddCompanyBankAccountId = pWSDTO.getDDTxBatch().getCompanyBankAccountID();
        pPayrollRunDTO
                .setCompanyBankAccounts(getServiceBankAccountCollection(ddCompanyBankAccountId));

        Collection<PaycheckDTO> paychecks = getPaycheckCollection(pWSDTO);
        pPayrollRunDTO.setPaychecks(paychecks);
    }

    private Collection<PaycheckDTO> getPaycheckCollection(DDTransactionSubmit pWSDTO) {
        Collection<PaycheckDTO> domainPaychecks = new ArrayList();
        List<EECheck> wsPaychecks = pWSDTO.getDDTxBatch().getEECheck();

        for (EECheck wsPaycheck : wsPaychecks) {
            PaycheckDTO currDomainPaycheck = new PaycheckDTO();
            currDomainPaycheck.setEmployeeId(wsPaycheck.getEmployeeID());
            currDomainPaycheck.setPaycheckId(wsPaycheck.getPaycheckID());

            Collection<DDTransactionDTO> domainPaycheckSplits = new ArrayList();
            List<DDTx> wsPaycheckSplits = wsPaycheck.getDDTx();
            for (DDTx wsPaycheckSplit : wsPaycheckSplits) {
                DDTransactionDTO currDDTxn = new DDTransactionDTO();
                currDDTxn.setDDTransactionAmount(wsPaycheckSplit.getTxAmount());
                currDDTxn.setDDTransactionId(wsPaycheckSplit.getDDTransactionID());

                EmployeeBankAccountDTO currEEBankAccount = new EmployeeBankAccountDTO();
                currEEBankAccount.setEmployeeBankAccountId(wsPaycheckSplit
                        .getEmployeeBankAccountID());
                if (wsPaycheckSplit.getBankAccount() != null) {
                    BankAccountDTO currBankAccount = new BankAccountDTO();
                    currBankAccount.setAccountNumber(wsPaycheckSplit.getBankAccount()
                            .getAccountNumber());
                    currBankAccount.setAccountType(DDCodeToPSP.getBankAccountType(wsPaycheckSplit
                            .getBankAccount().getAccountType()));
                    currBankAccount.setBankName(wsPaycheckSplit.getBankAccount().getBankName());
                    currBankAccount.setRoutingNumber(wsPaycheckSplit.getBankAccount()
                            .getRoutingNumber());
                    currEEBankAccount.setBankAccount(currBankAccount);
                }
                currDDTxn.setEmployeeBankAccount(currEEBankAccount);
                domainPaycheckSplits.add(currDDTxn);
            }
            currDomainPaycheck.setDdTransactions((List<DDTransactionDTO>)domainPaycheckSplits);

            SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
            for (DDTransactionDTO currDDTxn : currDomainPaycheck.getDdTransactions()) {
                SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
            }
            currDomainPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);

            domainPaychecks.add(currDomainPaycheck);
        }

        return domainPaychecks;
    }

    private Collection<ServiceBankAccountDTO> getServiceBankAccountCollection(
            String pDDBankAccountId) {
        CompanyBankAccountDTO ddCompanyBankAccount = new CompanyBankAccountDTO();
        ddCompanyBankAccount.setCompanyBankAccountID(pDDBankAccountId);

        ServiceBankAccountDTO ddServiceBankAccountDTO = new ServiceBankAccountDTO();
        ddServiceBankAccountDTO.setCompanyBankAccount(ddCompanyBankAccount);
        ddServiceBankAccountDTO.setServiceCode(ServiceCode.DirectDeposit);

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        companyBankAccounts.add(ddServiceBankAccountDTO);

        return companyBankAccounts;
    }

    /**
     * Obtain a collection of transation responses if there are any that have
     * tokens greater than the input and returns them to the client. Since there
     * is no inserts or updates, there is no need for declaring a unit of work
     *
     * @param requestDoc Look at TransactionSync.xsd for data structure
     * @return Element Look at TransactionSyncRs.xsd for data structure
     * @throws WSException
     */
    public Element sync(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "169", "10000", "10003"};

        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext context = new WSServerContext("DDTransaction", "sync");
            TransactionSync transactionSync = (TransactionSync) context
                    .translateInputElement(requestDoc);

            // Call the transaction sync process
            DomainEntitySet<TransactionResponse> transactionResponses = null;
            ProcessResult<DomainEntitySet<TransactionResponse>> results = PayrollServices.payrollManager
                    .syncTransactions(SourceSystemCode.valueOf(transactionSync.getSourceSystemCd()),
                                      transactionSync.getCompanyID(), new Long(transactionSync
                            .getToken()));

            /*
                * Performing error code conversion since
                */
            if (results.getMessages().size() > 0) {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        transactionSync.getCompanyID(), SourceSystemCode.valueOf(transactionSync.getSourceSystemCd()));
                DDCommon.replacePSPError(results, "1101", "177", domainCompany);

                Message message = results.getMessages().get(0);
                if (message.getMessageCode().equals("169")) {
                    results = new ProcessResult();
                    results.getMessages().CompanyDoesNotExistSourceSystemCdSourceSystemId(
                            EntityName.Company, transactionSync.getCompanyID(),
                            transactionSync.getSourceSystemCd(), transactionSync.getCompanyID());
                }
            }

            // Handle the results of the transaction sync query
            TransactionSyncRs transactionSyncRs = (TransactionSyncRs) context.getOutputDTO();
            if (results.isSuccess()) {
                transactionResponses = results.getResult();
                processTransactionResponses(transactionSync, transactionSyncRs,
                                            transactionResponses);
            }
            else {
                transactionSyncRs.getTransactionResponseRet().clear();
                transactionSyncRs.setToken(transactionSync.getToken());
            }

            // Build the response status
            transactionSyncRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;
    }

    /**
     * Attempts to recall a payroll submission prior to it being offloaded and
     * returns a collection of transaction responses once this process is
     * complete
     *
     * @param requestDoc Look at TransactionSync.xsd for data structure
     * @return Element Look at TransactionSyncRs.xsd for data structure
     * @throws WSException
     */
    public Element recall(Element requestDoc) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169", "194", "195", "289","177", "1008", "137", "138", "1010", "1101", "1015", "289", "196","1017", "258", "10003", "10004", "308"};

        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext context = new WSServerContext("DDTransaction", "recall");
            DDTransactionRecall transactionRecall = (DDTransactionRecall) context
                    .translateInputElement(requestDoc);
            DDTransactionRecallRs transactionRecallRs = (DDTransactionRecallRs) context.getOutputDTO();
            // Call the transaction sync process
            DomainEntitySet<TransactionResponse> transactionResponses = null;

            // Configure DTO
            TransactionCancelEEDTO cancelDTO = new TransactionCancelEEDTO();
//            recallDTO.setToken(new Long(transactionRecall.getToken()));
            cancelDTO.setRequestId(transactionRecall.getRequestID());
            //cancelDTO.setServiceCd(ServiceCode.DirectDeposit);
            DDTxRecallBatch ddTxRecallBatch = transactionRecall.getDDTxRecallBatch();
            cancelDTO.setSourcePayrollRunId(ddTxRecallBatch.getDDTxBatchID());
            List<String> paycheckIds = new ArrayList<String>();

            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        transactionRecall.getCompanyID(),
                        SourceSystemCode.valueOf(transactionRecall.getSourceSystemCd()));
            ProcessResult<TransactionResponse> results = new ProcessResult<TransactionResponse>();

            if (domainCompany == null) {
                results.getMessages().CompanyDoesNotExist(EntityName.Company, transactionRecall
                        .getCompanyID(), transactionRecall
                        .getSourceSystemCd(), transactionRecall.getCompanyID());
                transactionRecallRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));
            } else {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, ddTxRecallBatch.getDDTxBatchID());
                if (null == payrollRun) {
                    results.getMessages().PayrollRunDoesNotExist(
                        EntityName.PayrollRun, ddTxRecallBatch.getDDTxBatchID(), ddTxRecallBatch.getDDTxBatchID(),
                        transactionRecall.getSourceSystemCd(),transactionRecall.getCompanyID());
                    transactionRecallRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));
                } else {
                    results.merge(payrollRun.convertSplitIdsToPaycheckIds(ddTxRecallBatch.getDDTransactionID(), paycheckIds));
                    transactionRecallRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));
                }

                transactionResponses = TransactionResponse.findTransactionResponses(domainCompany,
                        new Long(transactionRecall.getToken()));
                // Find Transaction Response for RequestId
                TransactionResponse existingTxnRspForReqId = TransactionResponse
                        .findTransactionResponses(domainCompany, cancelDTO.getRequestId());

                // If a transaction response already exists for this request id,
                // create an error; otherwise, process the payroll run
                if (existingTxnRspForReqId != null) {
                    if (existingTxnRspForReqId != null) {
                        boolean addToCollection = true;
                        for (TransactionResponse response : transactionResponses) {
                            if (existingTxnRspForReqId.getId().equals(response.getId())) {
                                addToCollection = false;
                            }
                        }
                        if (addToCollection) {
                            transactionResponses.add(existingTxnRspForReqId);
                        }
                    }
                    transactionRecallRs.getTransactionResponseRet().add(
                            buildTransactionResponseRet(existingTxnRspForReqId));

                    // Add a message to the response that this request id already
                    // exists

                    results.getMessages().RequestIdAlreadyExists(EntityName.TransactionResponse,
                                                                existingTxnRspForReqId.getSourceRequestId(),
                                                                existingTxnRspForReqId.getSourceRequestId(),
                                                                domainCompany.getSourceSystemCd().toString(),
                                                                domainCompany.getSourceCompanyId());
                    transactionRecallRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));


                }
            }
            
            if(results.getMessages().size() == 0) {
                cancelDTO.setSourcePaycheckIdList(paycheckIds);
                results = PayrollServices.payrollManager.cancelEmployeeTransaction(
                        SourceSystemCode.valueOf(transactionRecall.getSourceSystemCd()),
                        transactionRecall.getCompanyID(), cancelDTO);
            }

            /*
            * Performing error code conversion since
            */
            if (results.getMessages().size() > 0) {


                DDCommon.replacePSPError(results, "1101", "177", domainCompany);

                Message message = results.getMessages().get(0);
                if (message.getMessageCode().equals("169")) {
                    results = new ProcessResult();
                    results.getMessages()
                            .CompanyDoesNotExistSourceSystemCdSourceSystemId(EntityName.Company,
                                                                             transactionRecall.getCompanyID(),
                                                                             transactionRecall.getSourceSystemCd(),
                                                                             transactionRecall.getCompanyID());
                }
                else if (message.getMessageCode().equals("1010")) {
                    results = new ProcessResult();
                    results.getMessages().CompanyDoesNotExistOnService(EntityName.Company,
                                                                       transactionRecall.getCompanyID(),
                                                                       transactionRecall.getSourceSystemCd(),
                                                                       transactionRecall.getCompanyID(), ServiceCode.DirectDeposit.toString());
                }
            }

            // Handle the results of the transaction sync query

            if (results.isSuccess()) {

                TransactionResponse transactionResponse = results.getResult();
                if (transactionResponse != null) {
                    transactionResponses.add(transactionResponse);
                }
                processTransactionResponses(transactionRecall, transactionRecallRs,
                                            transactionResponses);
            }
            else {
                transactionRecallRs.getTransactionResponseRet().clear();
            }

            // Build the response status
            transactionRecallRs.setResponseStatus(DDCommon.build_ResponseStatus(results, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;
    }

    /**
     * Processes one or more transaction responses and adds them to the outgoing
     * DTO
     *
     * @param pTransactionSync      Incoming Transaction Sync DTO
     * @param pTransactionSyncRs    Outgoing Results that'll be sent to the client
     * @param pTransactionResponses Transaction Responses returned from the query
     */
    public void processTransactionResponses(TransactionSync pTransactionSync,
                                            TransactionSyncRs pTransactionSyncRs,
                                            DomainEntitySet<TransactionResponse> pTransactionResponses) throws Exception {

        // Initialize the Transaction Sync Resposne
        pTransactionSyncRs.getTransactionResponseRet().clear();
        pTransactionSyncRs.setToken(pTransactionSync.getToken());

        for (Iterator iter = pTransactionResponses.iterator(); iter.hasNext();) {
            TransactionResponse transactionResponse = (TransactionResponse) iter.next();
            pTransactionSyncRs.setToken(Long.valueOf(transactionResponse.getTransactionTokenNumber()).intValue());
            pTransactionSyncRs.getTransactionResponseRet().add(
                    buildTransactionResponseRet(transactionResponse));
        }
    }

    /**
     * Processes one or more transaction responses and adds them to the outgoing
     * DTO
     *
     * @param pTransactionSync      Incoming Transaction Sync DTO
     * @param pTransactionRecallRs  Outgoing Results that'll be sent to the client
     * @param pTransactionResponses Transaction Responses returned from the query
     */
    public void processTransactionResponses(DDTransactionRecall pTransactionSync,
                                            DDTransactionRecallRs pTransactionRecallRs,
                                            DomainEntitySet<TransactionResponse> pTransactionResponses) throws Exception {

        // Initialize the Transaction Sync Resposne
        pTransactionRecallRs.getTransactionResponseRet().clear();
        //pTransactionRecallRs.setToken(pTransactionSync.getToken());

        for (Iterator iter = pTransactionResponses.iterator(); iter.hasNext();) {
            TransactionResponse transactionResponse = (TransactionResponse) iter.next();
            //pTransactionSyncRs.setToken(transactionResponse.getTransactionTokenNumber().intValue());
            pTransactionRecallRs.getTransactionResponseRet().add(
                    buildTransactionResponseRet(transactionResponse));
        }
    }

    /**
     * Populates the Transaction Response object for an incoming transaction
     * response. This will iterate through each financial transaction state
     * contained in the transaction response and output that into the
     * transaction ret object
     *
     * @param pTransactionResponse Transaction Response to be processed
     * @return TransactionRet object to be added to the outgoing DTO
     * @throws Exception Thrown if there's a JAXB exception or a null pointer
     *                   exception
     */
    private TransactionResponseRet buildTransactionResponseRet(
            TransactionResponse pTransactionResponse) throws Exception {

        // Fully qualifying this since this objects of the same name from other
        // packages are regularly invoked here
        intuit.osp.pse.dd.wsapi.xsd.transactionresponseret.ObjectFactory transactionResponseRetObjectFactory = new intuit.osp.pse.dd.wsapi.xsd.transactionresponseret.ObjectFactory();
        TransactionResponseRet transactionResponseRet = transactionResponseRetObjectFactory
                .createTransactionResponseRet();

        transactionResponseRet
                .setToken(Long.valueOf(pTransactionResponse.getTransactionTokenNumber()).intValue());
        transactionResponseRet.setRequestID(pTransactionResponse.getSourceRequestId());

        // Populate Company Info
        Company company = pTransactionResponse.getCompany();
        transactionResponseRet.setSourceSystem(company.getSourceSystemCd().toString());
        transactionResponseRet.setCompanyID(company.getSourceCompanyId());

        // Populate Transaction Responses
        DomainEntitySet<FinancialTransactionState> financialTransactionStates = pTransactionResponse
                .getFinancialTransactionStates();
        transactionResponseRet.getDDTxBatchRet().add(buildDDTxBatchRet(financialTransactionStates));

        return transactionResponseRet;
    }

    /**
     * Processes each individual transaction provided. This will determine if
     * the transaction is an employee or an employer transaction and then farm
     * that off in the appropriate direction
     *
     * @param pFinancialTransactionStates FinancialTransactionState associated with a particular
     *                                    transaction resposne
     * @return DDTxBatchRet object to be added to the transaction resposne DTO
     * @throws Exception
     */
    private DDTxBatchRet buildDDTxBatchRet(
            DomainEntitySet<FinancialTransactionState> pFinancialTransactionStates)
            throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.ddtxbatchret.ObjectFactory ddTxBatchRetObjectFactory = new intuit.osp.pse.dd.wsapi.xsd.ddtxbatchret.ObjectFactory();
        DDTxBatchRet ddTxBatchRet = ddTxBatchRetObjectFactory.createDDTxBatchRet();
        DomainEntitySet<FinancialTransactionState> finTxStatesByOrder = pFinancialTransactionStates.sort(FinancialTransactionState.FinancialTransaction().CurrentTransactionState().TransactionStateCd(),FinancialTransactionState.FinancialTransaction().PaycheckSplit().SourceDdTxnId());

        for (FinancialTransactionState financialTransactionState:finTxStatesByOrder) {
            FinancialTransaction financialTransaction = financialTransactionState
                    .getFinancialTransaction();
            PayrollRun payroll = financialTransaction.getPayrollRun();

            if (payroll != null) {
                ddTxBatchRet.setDDTxBatchID(payroll.getSourcePayRunId());
            }

            if (TransactionType.isEmployerTransactionType(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                ERFinancialTx erftx = buildERFinancialTx(financialTransactionState);
                if (erftx != null) {
                    ddTxBatchRet.getERFinancialTx().add(erftx);
                }
            }
            else if (TransactionType.isEmployeeTransactionType(financialTransaction
                    .getTransactionType().getTransactionTypeCd())) {
                ddTxBatchRet.getEEFinancialTx().add(
                        buildEEFinancialTx(financialTransactionState));
            }
            else {
                logger.error("Transaction code "
                        + financialTransaction.getTransactionType().getTransactionTypeCd()
                        + " unknown.");
            }
        }
        return ddTxBatchRet;
    }

    private ERFinancialTx buildERFinancialTx(FinancialTransactionState pFinancialTransactionState)
            throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.erfinancialtx.ObjectFactory erFinancialTxObjectFactory = new intuit.osp.pse.dd.wsapi.xsd.erfinancialtx.ObjectFactory();
        ERFinancialTx erFinancialTx = erFinancialTxObjectFactory.createERFinancialTx();

        FinancialTransaction financialTransaction = pFinancialTransactionState
                .getFinancialTransaction();
        TransactionType transactionType = financialTransaction.getTransactionType();
        String sku = financialTransaction.getSku();
        OfferingServiceChargeType offeringServiceCharge = null;
        if (null != sku) {
            offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        }

        String transactionTypeCode = DDCodeToPSP.getQBOETransactionTypeCode(transactionType.getTransactionTypeCd(), offeringServiceCharge);
        if (transactionTypeCode == null) {
            // no mapping from PSP type to QBOE type means QBOE doesn't want a response for this transaction
            return null;
        }
        erFinancialTx.setTxTypeCd(transactionTypeCode);
        erFinancialTx.setTxTypeDesc(DDCodeToPSP.getQBOETransactionTypeName(transactionType, offeringServiceCharge));

        Calendar txnStatusEffDate = Calendar.getInstance();
        txnStatusEffDate.setTime(new Date(pFinancialTransactionState
                .getTransactionStateEffectiveDate().toLocal().getTimeInMilliseconds()));
        erFinancialTx.setTxStatusEffDate(txnStatusEffDate);

        Calendar calendar = Calendar.getInstance();
        calendar
                .setTime(new Date(financialTransaction.getSettlementDate().toLocal().getTimeInMilliseconds()));
        TimeZone zone = calendar.getTimeZone();
        logger.info("ER Txn Time Zone ID: " + zone.getID() + " -- Time Zone Name :" + zone.getDisplayName());
        if(zone.getID().equals("UTC")){
            logger.info("Transaction Date TimeZone : " + zone.getID() + " -- Fin Txn : " + financialTransaction.getId()
                    + " -- Fin Txn Type : " + financialTransaction.getTransactionType().getTransactionTypeCd());
        }
        erFinancialTx.setTxDate(calendar);
        CompanyBankAccount companyBankAccount = financialTransaction.getCompanyBankAccountIncludingExpired();
        if (companyBankAccount!=null) {
            erFinancialTx.setCompanyBankAccountID(companyBankAccount.getSourceBankAccountId());
        }

        TransactionState transactionState = pFinancialTransactionState.getTransactionState();
        erFinancialTx.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
        erFinancialTx.setTxStatusDesc(transactionState.getName());

        // Set the SpcfMoney to BigDecimal conversion for backward compatibility
        BigDecimal amount = new BigDecimal(financialTransaction.getFinancialTransactionAmount()
                .toString());
        amount.setScale(2);
        erFinancialTx.setFinancialTxAmt(amount);

        return erFinancialTx;
    }

    private EEFinancialTx buildEEFinancialTx(FinancialTransactionState pFinancialTransactionState)
            throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.eefinancialtx.ObjectFactory eeFinancialTxObjectFactory = new intuit.osp.pse.dd.wsapi.xsd.eefinancialtx.ObjectFactory();
        EEFinancialTx eeFinancialTx = eeFinancialTxObjectFactory.createEEFinancialTx();

        FinancialTransaction financialTransaction = pFinancialTransactionState
                .getFinancialTransaction();
        TransactionState transactionState = pFinancialTransactionState.getTransactionState();
        eeFinancialTx.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
        eeFinancialTx.setTxStatusDesc(transactionState.getName());

        Calendar calendar = Calendar.getInstance();
        calendar
                .setTime(new Date(financialTransaction.getSettlementDate().toLocal().getTimeInMilliseconds()));
        eeFinancialTx.setDDTxDate(calendar);
        TimeZone zone = calendar.getTimeZone();
        logger.info("EE Txn Time Zone ID: " + zone.getID() + " -- Time Zone Name :" + zone.getDisplayName());
        if(zone.getID().equals("UTC")){
            logger.info("Transaction Date TimeZone : " + zone.getID() + " -- Fin Txn : " + financialTransaction.getId()
                    + " -- Fin Txn Type : " + financialTransaction.getTransactionType().getTransactionTypeCd());
        }
        Calendar txnStatusEffDate = Calendar.getInstance();
        txnStatusEffDate.setTime(new Date(pFinancialTransactionState
                .getTransactionStateEffectiveDate().toLocal().getTimeInMilliseconds()));
        eeFinancialTx.setTxStatusEffDate(txnStatusEffDate);

        BigDecimal amount = new BigDecimal(financialTransaction.getFinancialTransactionAmount()
                .toString());
        amount.setScale(2);
        eeFinancialTx.setDDTxAmt(amount);

        FinancialTransaction pPaycheckSplitTx = financialTransaction;
        if (TransactionTypeCode.EmployeeDdReversalDebit == financialTransaction.getTransactionType().getTransactionTypeCd()) {
            pPaycheckSplitTx = financialTransaction.getOriginalTransaction();
        }
        PaycheckSplit paycheckSplit = pPaycheckSplitTx.getPaycheckSplit();
        eeFinancialTx.setDDTransactionID(paycheckSplit.getSourceDdTxnId());
        Paycheck paycheck = paycheckSplit.getPaycheck();
        eeFinancialTx.setPaycheckID(paycheck.getSourcePaycheckId());

        EmployeeBankAccount employeeBankAccount = financialTransaction
                .getEmployeeBankAccount();
        eeFinancialTx.setEEBankAccountID(employeeBankAccount.getSourceBankAccountId());

        Employee employee = employeeBankAccount.getEmployee();
        eeFinancialTx.setEmployeeID(employee.getSourceEmployeeId());

        return eeFinancialTx;
    }


    /**
     * This method executes logic for cancel request.
     *
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element cancel(Element requestDoc) throws WSException {
        Element responseDoc = null;
        String[] expectedErrorCodes = {"137", "138", "118", "130", "11", "125", "169", "1010", "194", "1048", "195", "289", "1046",
                "1047", "258", "306", "307"};
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = new ProcessResult();

            WSServerContext context = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.CANCEL);
            DDTransactionCancel requestXml = (DDTransactionCancel) context.translateInputElement(requestDoc);
            DDTransactionCancelRs responseXml = (DDTransactionCancelRs) context.getOutputDTO();

            List<String> paycheckIds = new ArrayList<String>();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        requestXml.getCompanyID(),
                        SourceSystemCode.valueOf(requestXml.getSourceSystemCd()));

            if (domainCompany == null) {
                processResult.getMessages().CompanyDoesNotExist(EntityName.Company, requestXml
                        .getCompanyID(), requestXml
                        .getSourceSystemCd(), requestXml.getCompanyID());
                responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            } else {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, requestXml.getDDTxBatchID());
                if (null == payrollRun) {
                    processResult.getMessages().PayrollRunDoesNotExist(
                        EntityName.PayrollRun, requestXml.getDDTxBatchID(), requestXml.getDDTxBatchID(),
                        requestXml.getSourceSystemCd(),requestXml.getCompanyID());
                    responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
                } else {
                    processResult.merge(payrollRun.convertSplitIdsToPaycheckIds(requestXml.getDDTransactionID(), paycheckIds));
                    responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
                }
            }

            try {
                if (processResult.isSuccess()) {
                    TransactionCancelEEDTO cancelDTO = buildTransactionCancelDTO(requestXml);
                    cancelDTO.setSourcePaycheckIdList(paycheckIds);
                    processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(
                            SourceSystemCode.valueOf(requestXml.getSourceSystemCd()),
                            requestXml.getCompanyID(),
                            cancelDTO));
                }
                PayrollServices.commitUnitOfWork();
            }
            catch (Exception e) {
                PayrollServices.rollbackUnitOfWork();
                throw e;
            }

            responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            responseDoc = context.translateOutputDTO();
        }
        catch (WSValidationException e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return responseDoc;
    }

    private TransactionCancelEEDTO buildTransactionCancelDTO(DDTransactionCancel pRequestXml) {
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId(pRequestXml.getDDTxBatchID());
        return dto;
    }

    /**
     * This method executes logic for reverse request.
     *
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element reverse(Element requestDoc) throws WSException {
        Element responseDoc = null;
        String[] expectedErrorCodes = {"137", "138", "118", "11", "269", "139", "130", "125", "169", "1010", "194", "1048", "271",
                "266", "1062", "501", "502", "170", "186", "195", "260", "261", "262", "263"};

        try {
            ProcessResult processResult = new ProcessResult();

            WSServerContext context = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.REVERSE);
            DDTransactionReverse requestXml = (DDTransactionReverse) context.translateInputElement(requestDoc);
            DDTransactionReverseRs responseXml = (DDTransactionReverseRs) context.getOutputDTO();

            PayrollServices.beginUnitOfWork();
            try {
                processResult.merge(PayrollServices.payrollManager.reverseTransaction(
                        SourceSystemCode.valueOf(requestXml.getSourceSystemCd()),
                        requestXml.getCompanyID(),
                        buildTransactionReverseDTO(requestXml)));
                PayrollServices.commitUnitOfWork();
            }
            catch (Exception e) {
                PayrollServices.rollbackUnitOfWork();
                throw e;
            }

            responseXml.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            responseDoc = context.translateOutputDTO();
        }
        catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return responseDoc;
    }

    private TransactionReverseDTO buildTransactionReverseDTO(DDTransactionReverse pRequestXml) {
        TransactionReverseDTO dto = new TransactionReverseDTO();

        dto.setTxSettlementTypeCd(SettlementTypeDTO.translateSPSSettlementType(pRequestXml.getTxSettlementTypeCd()));
        dto.setTxDate(pRequestXml.getTxDate());
        dto.setChargeFee(pRequestXml.isChargeFee());
//        dto.setCompanyBankAccountId(pRequestXml.getCompanyBankAccountID());
        dto.setSourcePayrollRunId(pRequestXml.getDDTxBatchID());
        dto.setDdTransactionIdList(pRequestXml.getDDTransactionID());

        return dto;
    }

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element badDebtWriteOff(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "1055", "282"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "badDebtWriteOff");
        BadDebtWriteOffRs badDebtWriteOffRs;
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();

            BadDebtWriteOff badDebtWriteOff =
                    (BadDebtWriteOff) wsServerContext.translateInputElement(requestDocument);

            badDebtWriteOffRs = (BadDebtWriteOffRs) wsServerContext.getOutputDTO();

            if (badDebtWriteOff != null) {
                processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                        SourceSystemCode.valueOf(badDebtWriteOff.getSourceSystemCd()), badDebtWriteOff.getCompanyID(),
                        badDebtWriteOff.getDDTxBatchID());
            }
            PayrollServices.commitUnitOfWork();

            badDebtWriteOffRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();
        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

//    /**
//     * @param requestDocument
//     * @return
//     * @throws WSException
//     */
//    public Element badDebtRecover(Element requestDocument) throws WSException {
//        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "281", "1055", "267", "279", "271", "266", "280"};
//        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "badDebtRecover");
//        BadDebtRecoverRs badDebtRecoverRs;
//        ProcessResult processResult = new ProcessResult();
//        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
//        try {
//            PayrollServices.beginUnitOfWork();
//            BadDebtRecover badDebtRecover =
//                    (BadDebtRecover) wsServerContext.translateInputElement(requestDocument);
//
//            badDebtRecoverRs = (BadDebtRecoverRs) wsServerContext.getOutputDTO();
//            if (badDebtRecover != null) {
//                build_BadDebtRecoverDTO(badDebtRecoverDTO, badDebtRecover);
//
//                processResult = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(
//                        SourceSystemCode.valueOf(badDebtRecover.getSourceSystemCd()), badDebtRecover.getCompanyID(),
//                        badDebtRecoverDTO);
//            }
//
//            PayrollServices.commitUnitOfWork();
//
//            badDebtRecoverRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
//
//            return wsServerContext.translateOutputDTO();
//
//        }
//        catch (WSValidationException wsValidationException) {
//            PayrollServices.rollbackUnitOfWork();
//            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
//            throw wsValidationException;
//        }
//        catch (Exception ex) {
//            PayrollServices.rollbackUnitOfWork();
//            logger.error(ex.getMessage(), ex.getCause());
//            throw new WSException(DDCommon.pse_Error, ex);
//        }
//    }
//
//    /**
//     * Method to build the BadDebtRecoverDTO from the BadDebtRecover object
//     *
//     * @param pBadDebtRecoverDTO BadDebtRecoverDTO
//     * @param pBadDebtRecover    BadDebtRecover
//     */
//    private void build_BadDebtRecoverDTO(BadDebtRecoverDTO pBadDebtRecoverDTO, BadDebtRecover pBadDebtRecover) {
//        SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(pBadDebtRecover.getTxSettlementTypeCd());
//        SpcfMoney financialTxAmt = SpcfUtils.convertToSpcfMoney(pBadDebtRecover.getFinancialTxAmt());
//
//        SpcfCalendar txDate = null;
//        txDate = CalendarUtils.convertToSpcfCalendar(pBadDebtRecover.getTxDate());
//
//        pBadDebtRecoverDTO.setSettlementType(settlementType);
//        pBadDebtRecoverDTO.setSourcePayrollRunId(pBadDebtRecover.getDDTxBatchID());
//        pBadDebtRecoverDTO.setFinancialTxAmt(financialTxAmt);
//        pBadDebtRecoverDTO.setRecoveryType(BadDebtRecoverDTO.RecoveryType.DirectDeposit);
//        pBadDebtRecoverDTO.setTxDate(new DateDTO(txDate));
//    }

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element eeReturnTransfer(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "1055", "282", "280", "290"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "eeReturnTransfer");
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

        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element feeTransfer(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "1055", "283", "5001", "284"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "feeTransfer");
        FeeTransferRs feeTransferRs;
        ProcessResult processResult = new ProcessResult();
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();

        try {
            PayrollServices.beginUnitOfWork();

            FeeTransfer feeTransfer =
                    (FeeTransfer) wsServerContext.translateInputElement(requestDocument);

            feeTransferRs = (FeeTransferRs) wsServerContext.getOutputDTO();

            if (feeTransfer != null) {
                build_FeeTransferDTO(feeTransferDTO, feeTransfer);

                processResult = PayrollServices.financialTransactionManager.addFeeTransferTransaction(
                        SourceSystemCode.valueOf(feeTransfer.getSourceSystemCd()),
                        feeTransfer.getCompanyID(), feeTransferDTO);
            }

            PayrollServices.commitUnitOfWork();

            feeTransferRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Method to build the FeeTransferDTO from the FeeTransfer object
     *
     * @param pFeeTransferDTO FeeTransferDTO
     * @param pFeeTransfer    FeeTransfer
     */
    private void build_FeeTransferDTO(FeeTransferDTO pFeeTransferDTO, FeeTransfer pFeeTransfer) {
        SpcfMoney financialTxAmt = SpcfUtils.convertToSpcfMoney(pFeeTransfer.getFinancialTxAmt());

        pFeeTransferDTO.setSourcePayrollRunId(pFeeTransfer.getDDTxBatchID());
        pFeeTransferDTO.setFinancialTxAmt(financialTxAmt);
        pFeeTransferDTO.setFeeTypeCode(DDCodeToPSP.getFeeTypeCode(pFeeTransfer.getFeeTypeCd()));
    }

    /**
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element intuit5DayReturnTransfer(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "1055", "282", "280"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "intuit5DayReturnTransfer");
        Intuit5DayReturnTransferRs intuit5DayReturnTransferRs;
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Intuit5DayReturnTransfer intuit5DayReturnTransfer =
                    (Intuit5DayReturnTransfer) wsServerContext.translateInputElement(requestDocument);

            intuit5DayReturnTransferRs = (Intuit5DayReturnTransferRs) wsServerContext.getOutputDTO();

            if (intuit5DayReturnTransfer != null) {
                processResult = PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction(
                        SourceSystemCode.valueOf(intuit5DayReturnTransfer.getSourceSystemCd()),
                        intuit5DayReturnTransfer.getCompanyID(),
                        intuit5DayReturnTransfer.getDDTxBatchID());
            }

            PayrollServices.commitUnitOfWork();

            intuit5DayReturnTransferRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();
        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Webservice for Direct Deposit Refund
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element ddRefund(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "170", "186", "1010", "282", "280", "1055", "283", "271", "266"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "ddRefund");
        DDRefundRs ddRefundRs;
        ProcessResult processResult = new ProcessResult();
        RefundDTO refundDTO = new RefundDTO();

        try {
            PayrollServices.beginUnitOfWork();

            DDRefund ddRefund = (DDRefund) wsServerContext.translateInputElement(requestDocument);

            ddRefundRs = (DDRefundRs) wsServerContext.getOutputDTO();

            if (ddRefund != null) {

                build_DDRefundDTO(refundDTO, ddRefund);

                processResult = PayrollServices.financialTransactionManager.addRefundTransaction(
                        SourceSystemCode.valueOf(ddRefund.getSourceSystemCd()),
                        ddRefund.getCompanyID(), refundDTO);
            }

            PayrollServices.commitUnitOfWork();

            ddRefundRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Method to build the RefundDTO from the DDRefund object
     *
     * @param pRefundDto RefundDTO
     * @param pDDRefund  DDRefund
     */
    private void build_DDRefundDTO(RefundDTO pRefundDto, DDRefund pDDRefund) {

        SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(pDDRefund.getTxSettlementTypeCd());
        SpcfMoney financialTxAmt = new SpcfMoney(pDDRefund.getFinancialTxAmt().toString());

        SpcfCalendar txDate = null;
        txDate = CalendarUtils.convertToSpcfCalendar(pDDRefund.getTxDate());

        pRefundDto.setSettlementType(settlementType);
        pRefundDto.setSourcePayrollRunId(pDDRefund.getDDTxBatchID());
        pRefundDto.setFinancialTxAmt(financialTxAmt);
        pRefundDto.setTxDate(new DateDTO(txDate));
    }

    /**
     * Webservice for Employer Return Refund
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element erReturnRefund(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "170", "186", "1010", "1055", "280", "283", "271", "266"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "erReturnRefund");
        ERReturnRefundRs erReturnRefundRs;
        ProcessResult processResult = new ProcessResult();
        RefundDTO refundDTO = new RefundDTO();

        try {
            PayrollServices.beginUnitOfWork();

            ERReturnRefund erReturnRefund =
                    (ERReturnRefund) wsServerContext.translateInputElement(requestDocument);

            erReturnRefundRs = (ERReturnRefundRs) wsServerContext.getOutputDTO();

            if (erReturnRefund != null) {
                build_ERReturnRefundDTO(refundDTO, erReturnRefund);

                processResult = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(
                        SourceSystemCode.valueOf(erReturnRefund.getSourceSystemCd()),
                        erReturnRefund.getCompanyID(), refundDTO);
            }

            PayrollServices.commitUnitOfWork();

            erReturnRefundRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();

        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Method to build the RefundDTO from the ERReturnRefund object
     *
     * @param pRefundDTO      RefundDTO
     * @param pERReturnRefund ERReturnRefund
     */
    private void build_ERReturnRefundDTO(RefundDTO pRefundDTO, ERReturnRefund pERReturnRefund) {
        SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(pERReturnRefund.getTxSettlementTypeCd());
        SpcfMoney financialTxAmt = new SpcfMoney(pERReturnRefund.getFinancialTxAmt().toString());

        SpcfCalendar txDate = null;
        txDate = CalendarUtils.convertToSpcfCalendar(pERReturnRefund.getTxDate());

        pRefundDTO.setSettlementType(settlementType);
        pRefundDTO.setSourcePayrollRunId(pERReturnRefund.getDDTxBatchID());
        pRefundDTO.setFinancialTxAmt(financialTxAmt);
        pRefundDTO.setTxDate(new DateDTO(txDate));
    }

    /**
     * Webservice for Employee Return Refund
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element eeReturnRefund(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "125", "169", "194", "170", "186", "1010", "280", "290", "1055", "283", "271", "266"};
        WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, "eeReturnRefund");
        EEReturnRefundRs eeReturnRefundRs;
        ProcessResult processResult = new ProcessResult();
        RefundDTO refundDTO = new RefundDTO();

        try {
            PayrollServices.beginUnitOfWork();

            EEReturnRefund eeReturnRefund =
                    (EEReturnRefund) wsServerContext.translateInputElement(requestDocument);

            eeReturnRefundRs = (EEReturnRefundRs) wsServerContext.getOutputDTO();


            if (eeReturnRefund != null) {
                build_EEReturnRefundDTO(refundDTO, eeReturnRefund);

                processResult = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
                        SourceSystemCode.valueOf(eeReturnRefund.getSourceSystemCd()),
                        eeReturnRefund.getCompanyID(), refundDTO);
            }

            PayrollServices.commitUnitOfWork();

            eeReturnRefundRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));

            return wsServerContext.translateOutputDTO();
        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            logger.error(ex.getMessage(), ex.getCause());
            throw new WSException(DDCommon.pse_Error, ex);

        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Method to build the RefundDTO from the EEReturnRefund object
     *
     * @param pRefundDTO      RefundDTO
     * @param pEEReturnRefund EEReturnRefund
     */
    private void build_EEReturnRefundDTO(RefundDTO pRefundDTO, EEReturnRefund pEEReturnRefund) {
        SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(pEEReturnRefund.getTxSettlementTypeCd());
        SpcfMoney financialTxAmt = new SpcfMoney(pEEReturnRefund.getFinancialTxAmt().toString());

        SpcfCalendar txDate = null;
        txDate = CalendarUtils.convertToSpcfCalendar(pEEReturnRefund.getTxDate());

        pRefundDTO.setSettlementType(settlementType);
        pRefundDTO.setSourcePayrollRunId(pEEReturnRefund.getDDTxBatchID());
        pRefundDTO.setFinancialTxAmt(financialTxAmt);
        pRefundDTO.setTxDate(new DateDTO(txDate));
    }

    /**
     * This method executes logic for voidTx request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element voidTx(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "125", "169", "177", "1010", "264", "1051"};
        Element returnDoc = null;
        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.VOIDTX);

            DDTransactionVoidTxRs ddtransactionVoidTxRs;

            DDTransactionVoidTx ddtransactionVoidTx =
                    (DDTransactionVoidTx) wsServerContext.translateInputElement(requestDocument);

            ddtransactionVoidTxRs = (DDTransactionVoidTxRs) wsServerContext.getOutputDTO();
            ProcessResult processResult = new ProcessResult();
            if (ddtransactionVoidTx != null) {
                processResult = PayrollServices.financialTransactionManager.voidTransaction(
                        SourceSystemCode.valueOf(ddtransactionVoidTx.getSourceSystemCd()),
                        ddtransactionVoidTx.getCompanyID(), ddtransactionVoidTx.getPSETransactionID());
                if (!processResult.isSuccess()) {
                    com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                            ddtransactionVoidTx.getCompanyID(),
                            SourceSystemCode.valueOf(ddtransactionVoidTx.getSourceSystemCd()));
                    DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
                }
            }
            ddtransactionVoidTxRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            DDTransaction.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            DDTransaction.logger.error(exception.getMessage(), exception);
            throw new WSException(DDCommon.pse_Error, exception);
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }


    public Element txHistoryQuery(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"169", "5001"};
        Element returnDoc;
        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.TXHISTORYQUERY);

            TxHistoryQueryRs txHistoryQueryRs;

            TxHistoryQuery txHistoryQuery =
                    (TxHistoryQuery) wsServerContext.translateInputElement(requestDocument);
            txHistoryQueryRs = (TxHistoryQueryRs) wsServerContext.getOutputDTO();
            ProcessResult processResult = new ProcessResult();
            String sourceSystemCode = txHistoryQuery.getSourceSystemCd();
            String sourceCompanyId = txHistoryQuery.getCompanyID();
            String pseTransactionID = txHistoryQuery.getPSETransactionID();

            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));
            DomainEntitySet<FinancialTransactionState> financialTxStateCollection = null;

            if (company == null) {
                processResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
            }
            else if (pseTransactionID == null || pseTransactionID.length() <= 0) {
                processResult.getMessages().InvalidValue(EntityName.FinancialTransaction, pseTransactionID, "TransactionId");
            }
            else {
                FinancialTransaction ft = Application.findById(FinancialTransaction.class, pseTransactionID);
                financialTxStateCollection = ft.getFinancialTransactionStates();
                buildTxHistoryQueryRs(txHistoryQueryRs, financialTxStateCollection);
            }

            txHistoryQueryRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            DDTransaction.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
        }
        catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            DDTransaction.logger.error(exception.getMessage(), exception);
            throw new WSException(DDCommon.pse_Error, exception);
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnDoc;
    }

    private void buildTxHistoryQueryRs(TxHistoryQueryRs txHistoryQueryRs, DomainEntitySet<FinancialTransactionState> financialTxStateCollection)
            throws Exception {
        intuit.osp.pse.dd.wsapi.xsd.txhistoryret.ObjectFactory txhistoryretObjectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.txhistoryret.ObjectFactory();

        for (FinancialTransactionState finTxState : financialTxStateCollection) {

            TxHistoryRet txHistoryRet = txhistoryretObjectFactory.createTxHistoryRet();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(finTxState.getTransactionStateEffectiveDate().getTimeInMilliseconds());
            txHistoryRet.setPSETransactionID(finTxState.getFinancialTransaction().getId().toString());
            txHistoryRet.setDate(calendar);
            txHistoryRet.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(finTxState.getTransactionState().getTransactionStateCd()));
            txHistoryRet.setTxStatusDesc(finTxState.getTransactionState().getName());

            String userID = "";
            if (finTxState.getInsertUserId() != null) {
                userID = finTxState.getInsertUserId();
            }
            txHistoryRet.setUserID(userID);

            txHistoryQueryRs.getTxHistoryRet().add(txHistoryRet);
        }

    }

    /**
     * Method to create a re-debit employer transaction, if the debit transaction
     * to receive funds from the employer bank account in the submitted payroll run fails.
     * <p/>
     * Adds a new Employer re debit transaction for the return debit transactions.
     * Resolves the transaction returns associated with the Employer's returned
     * debit transactions. Creates a transaction response and updates
     * the payroll run status from  Returned to Debit pending.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element redebitAdd(Element requestDocument) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "283", "125", "169", "177", "1010", "1062",
                "264", "1051", "285", "504", "10011", "194", "1048", "186", "170"};
        Element returnDoc;
        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext context = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.REDEBIT_ADD);
            DDRedebitAdd ddRedebitAdd =
                    (DDRedebitAdd) context.translateInputElement(requestDocument);
            DDRedebitAddRs ddRedebitAddRs = (DDRedebitAddRs) context.getOutputDTO();
            ProcessResult processResult = new ProcessResult();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = null;
            if (ddRedebitAdd != null) {
                domainCompany = Company.findCompany(
                        ddRedebitAdd.getCompanyID(), SourceSystemCode.valueOf(ddRedebitAdd.getSourceSystemCd()));
                FinancialTransaction originalTxn = null;
                if (null != domainCompany) {
                    PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, ddRedebitAdd.getDDTxBatchID());
                    // Get the employer debit transactions returned for the payroll
                    if (null != payrollRun) {
                        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit ,TransactionTypeCode.EmployerDdRedebit},
                               new TransactionStateCode[]{TransactionStateCode.Returned});
                        for (FinancialTransaction finTxn : financialTxs) {
                            TransactionReturn txnReturn = TransactionReturn.findFirstUnresolvedTransactionReturn(finTxn);
                            if (txnReturn != null) {
                                originalTxn = finTxn;
                                break;
                            }
                        }
                    }
                }
                RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
                if (null != originalTxn) {
                    redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
                    redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
                    redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
                }
                ArrayList<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
                redebitImpoundDTOs.add(redebitDTO);
                processResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                        SourceSystemCode.valueOf(ddRedebitAdd.getSourceSystemCd()),
                        ddRedebitAdd.getCompanyID(), redebitImpoundDTOs);
            }

            if (!processResult.isSuccess()) {


                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
            }

            ddRedebitAddRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = context.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException wsValidationException) {
            DDTransaction.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            PayrollServices.rollbackUnitOfWork();
            throw wsValidationException;
        }
        catch (Exception exception) {
            DDTransaction.logger.error(exception.getMessage(), exception);
            PayrollServices.rollbackUnitOfWork();
            throw new WSException(DDCommon.pse_Error, exception);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }

    /**
     * This method executes logic for cancel request.
     *
     * @param requestDocument
     * @return
     * @throws WSException
     */
    public Element erFeeAdd(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"137", "138", "125", "169", "177", "130", "5001", "194", "1048", "5001", "121", "186", "170", "1010", "267", "269", "271", "266"};
        try {
            PayrollServices.beginUnitOfWork();
            WSServerContext wsServerContext = new WSServerContext(DDTransaction.SERVICE_NAME, DDTransaction.Operations.ER_FEE_ADD);

            ERFeeAdd erFeeAdd =
                    (ERFeeAdd) wsServerContext.translateInputElement(requestDocument);

            ERFeeAddRs erFeeAddRs = (ERFeeAddRs) wsServerContext.getOutputDTO();
            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = new ProcessResult<DomainEntitySet<FinancialTransaction>>();
            if (erFeeAdd != null) {
                // send data using DTO
                SettlementTypeDTO settlementType = DDCodeToPSP.getSettlementTypeDTO(erFeeAdd.getTxSettlementTypeCd());
                ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(SourceSystemCode.valueOf(erFeeAdd.getSourceSystemCd()),
                                                        erFeeAdd.getCompanyID(),
                                                        erFeeAdd.getDDTxBatchID(), settlementType,
                                                        erFeeAdd.getTxDate().getTime(), new SpcfMoney(erFeeAdd.getFinancialTxAmt().toString()),
                                                        DDCodeToPSP.getFeeTypeCode(erFeeAdd.getFeeTypeCd()), null);

                processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
            }

            if (!processResult.isSuccess()) {
                com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                        erFeeAdd.getCompanyID(), SourceSystemCode.valueOf(erFeeAdd.getSourceSystemCd()));

                DDCommon.replacePSPError(processResult, "1101", "177", domainCompany);
            }

            erFeeAddRs.setResponseStatus(DDCommon.build_ResponseStatus(processResult, expectedErrorCodes));
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        }
        catch (WSValidationException wsValidationException) {
            DDTransaction.logger.error(wsValidationException.getMessage(), wsValidationException.getCause());
            PayrollServices.rollbackUnitOfWork();
            throw wsValidationException;
        }
        catch (Exception exception) {
            DDTransaction.logger.error(exception.getMessage(), exception);
            PayrollServices.rollbackUnitOfWork();
            throw new WSException(DDCommon.pse_Error, exception);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnDoc;

    }

    /**
     * This method executes logic for Fee Redebit Add.
     *
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element feeRedebitAdd(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "5001", "169", "10004", "170", "186", "1031", "1051", "285"};
        try {
            WSServerContext context = new WSServerContext("DDTransaction", "feeRedebitAdd");
            FeeRedebitAdd request = (FeeRedebitAdd) context.translateInputElement(requestDoc);
            FeeRedebitAddRs response = (FeeRedebitAddRs) context.getOutputDTO();

            // unpack the request
            String srcSystemCd = request.getSourceSystemCd();
            String companyId = request.getCompanyID();
            String cbaId = request.getCompanyBankAccountID();
            String oldTxnId = request.getPSETransactionID();

            // do the work
            ProcessResult result = null;
            PayrollServices.beginUnitOfWork();
            try {
                FinancialTransaction originalTxn =
                    PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(oldTxnId));
                RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
                if (null != originalTxn) {
                    redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
                    redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
                    redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
                }
                ArrayList<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
                redebitImpoundDTOs.add(redebitDTO);
                result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                        SourceSystemCode.valueOf(srcSystemCd),
                        companyId, redebitImpoundDTOs);
                if (result.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                }
                else {
                    PayrollServices.rollbackUnitOfWork();

                    if (result.getMessages().size() > 0) {
                        logger.info("feeRedebitAdd failed: " + result.getMessages().get(0).getMessage());
                    }
                    else {
                        logger.info("feeRedebitAdd failed with no message");
                    }
                }
            }
            catch (Exception e) {
                PayrollServices.rollbackUnitOfWork();
                logger.warn(e.getMessage(), e);
                throw e;
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            // pack up the response
            response.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        }
        catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * This method executes logic for Escalation Add.
     *
     * @param requestDoc
     * @return
     * @throws WSException
     */
    public Element escalationAdd(Element requestDoc) throws WSException {
        String[] expectedErrorCodes = {"137", "138", "169", "194", "283", "266", "271"};
        try {
            WSServerContext context = new WSServerContext(SERVICE_NAME, Operations.ESCALATION_ADD);
            EscalationAdd request = (EscalationAdd) context.translateInputElement(requestDoc);
            EscalationAddRs response = (EscalationAddRs) context.getOutputDTO();

            // unpack the request
            String srcSystemCd = request.getSourceSystemCd();
            String companyId = request.getCompanyID();
            String payrollRunId = request.getDDTxBatchID();
            BigDecimal amount = request.getFinancialTxAmt();
            Calendar settlementDate = request.getTxDate();
            boolean isEmployee = request.isEmployee();
            SettlementType sType = DDCodeToPSP.getSettlementType(request.getTxSettlementTypeCd());

            // do the work
            ProcessResult result = null;
            PayrollServices.beginUnitOfWork();
            try {
                DateDTO dtoTxnDate = new DateDTO();
                dtoTxnDate.set(settlementDate.get(Calendar.YEAR), settlementDate.get(Calendar.MONTH),
                               settlementDate.get(Calendar.DAY_OF_MONTH));
                result =
                        PayrollServices.financialTransactionManager.addEscalation(
                                SourceSystemCode.valueOf(srcSystemCd), companyId, payrollRunId, isEmployee,
                                sType, amount, dtoTxnDate);
                if (result.isSuccess()) {
                    PayrollServices.commitUnitOfWork();
                }
                else {
                    PayrollServices.rollbackUnitOfWork();

                    if (result.getMessages().size() > 0) {
                        logger.info(Operations.ESCALATION_ADD + " failed: " + result.getMessages().get(0).getMessage());
                    }
                    else {
                        logger.info(Operations.ESCALATION_ADD + " failed with no message");
                    }
                }
            }
            catch (Exception e) {
                PayrollServices.rollbackUnitOfWork();
                logger.warn(e.getMessage(), e);
                throw e;
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            // pack up the response
            response.setResponseStatus(DDCommon.build_ResponseStatus(result, expectedErrorCodes));

            Element responseDoc = context.translateOutputDTO();
            return responseDoc;
        }
        catch (WSValidationException e) {
            logger.error(e.getMessage(), e.getCause());
            throw e;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            throw new WSException(DDCommon.pse_Error, e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
