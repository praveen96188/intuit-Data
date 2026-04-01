package com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.CommonValidations;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.ErrorMessageList;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.TransmissionLogging;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.BillPaymentMessageDescriptions;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessage;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 2, 2009
 * Time: 3:59:13 PM
 */
@WebService()
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public class BillPaymentWebServices {
    private static final SpcfLogger logger = PayrollServices.getLogger(BillPaymentWebServices.class);

    @WebMethod()
    @WebResult(name = "SubmitPaymentResponse")
    public SubmitPaymentResponse SendPaymentsToPayees(
            @WebParam(name = "SubmitPaymentRequest") SubmitPaymentRequest pSubmitPaymentRequest) {
        SubmitPaymentResponse submitPaymentResponse = new SubmitPaymentResponse();
        String transmissionId = UUID.randomUUID().toString();
        String companyId = null;
        SourceSystemCode sourceSystemCd = null;
        String transmissionDescription = BillPaymentMessageDescriptions.getSubmitFailure();
        ArrayList<FeeTransaction> duplicateSubmissionDetail = new ArrayList<FeeTransaction>();

        try {
            // check input parameters
            if (pSubmitPaymentRequest == null) {
                submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.invalidArgument("SubmitPaymentRequest"));
                return submitPaymentResponse;
            }

            // validate QB version
            PayrollServices.beginUnitOfWork();
            CommonValidations.isQBVersionActive(pSubmitPaymentRequest.getCompany().getClientApplicationVersion(), submitPaymentResponse);
            PayrollServices.rollbackUnitOfWork();
            if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                return submitPaymentResponse;
            }

            // validate the company & PIN
            PayrollServices.beginUnitOfWork();
            Company company = CommonValidations.validateCompanyPin(pSubmitPaymentRequest, submitPaymentResponse);
            PayrollServices.commitUnitOfWork();
            if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                return submitPaymentResponse;
            }

            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                companyId = company.getSourceCompanyId();
                sourceSystemCd = company.getSourceSystemCd();
                logger.info("Request for PSID=" + companyId + " being handled.");
                BillPaymentTransmissionLogger.recordTransmissionRequest(TransmissionType.WSBillPaySendPaymentsToPayees, pSubmitPaymentRequest, transmissionId, companyId);

                if (pSubmitPaymentRequest.getCompany().getClientApplicationName() == null || pSubmitPaymentRequest.getCompany().getClientApplicationName().length() == 0) {
                    submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.fieldDataNotValid("ClientApplicationName", "Company"));
                }
                if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                    return submitPaymentResponse;
                }

                // update the company legal address
                if (pSubmitPaymentRequest.getCompany().getLegalAddress() != null) {
                    try {
                        PayrollServices.beginUnitOfWork();

                        company = Company.findCompany(companyId, sourceSystemCd);
                        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
                        AddressDTO legalAddress = BillPaymentTranslator.buildAddressDTOFromAddressWSDTO(pSubmitPaymentRequest.getCompany().getLegalAddress());
                        if (!legalAddress.equals(companyDTO.getLegalAddress())) {
                            companyDTO.setLegalAddress(legalAddress);
                            ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompany(sourceSystemCd,
                                    companyId, companyDTO);
                            if (processResult.isSuccess()) {
                                PayrollServices.commitUnitOfWork();
                            } else {
                                ErrorMessageList.mergeResults(processResult, submitPaymentResponse.getProcessingMessagesList());
                                if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                                    mapError1101(submitPaymentResponse, SystemCapabilityCode.SubmitPayment.toString());
                                    return submitPaymentResponse;
                                }
                            }
                        }
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                }

                Map<String, PayeeDTO> payeeDTOMap = new HashMap<String, PayeeDTO>();
                ArrayList<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>(pSubmitPaymentRequest.getPaymentTransactions().size());
                try {
                    PayrollServices.beginUnitOfWork();
                    for (PaymentTransaction paymentTransaction : pSubmitPaymentRequest.getPaymentTransactions()) {
                        // keep a map of the payees
                        PayeeDTO payeeDTO;
                        String sourcePayeeId = paymentTransaction.getPayee().getPayeeSourceId();
                        if (!payeeDTOMap.containsKey(sourcePayeeId)) {
                            payeeDTO = BillPaymentTranslator.buildPayeeDTOFromPayeeWSDTO(paymentTransaction.getPayee());
                            payeeDTOMap.put(sourcePayeeId, payeeDTO);
                        } else {
                            payeeDTO = payeeDTOMap.get(sourcePayeeId);
                        }

                        if (!payeeDTO.getIs1099()) {
                            submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.Non1099PayeeNotSupported());
                        }

                        if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                            return submitPaymentResponse;
                        }

                        ArrayList<BillPaymentSplitDTO> billPaymentSplitDTOs = new ArrayList<BillPaymentSplitDTO>();
                        SpcfDecimal netAmount = SpcfDecimal.createInstance(0.00);
                        String sessionId = paymentTransaction.getSessionId();
                        for (QBBillPaymentSplit billPaymentSplit : paymentTransaction.getBillPaymentSplits()) {
                            BillPaymentSplitDTO billPaymentSplitDTO = new BillPaymentSplitDTO();
                            billPaymentSplitDTO.setAmount(billPaymentSplit.getAmount());
                            if (billPaymentSplit.getSourceBillPaymentSplitId() == null) {
                                billPaymentSplitDTO.setBillPaymentSplitId(UUID.randomUUID().toString());
                            } else {
                                billPaymentSplitDTO.setBillPaymentSplitId(billPaymentSplit.getSourceBillPaymentSplitId());
                            }

                            //find the payees bank account if it exists
                            String sourceBankAccountId = billPaymentSplit.getBankAccount().getSourceBankAccountId();
                            PayeeBankAccount payeeBankAccount = PayeeBankAccount.findActivePayeeBankAccount(company,
                                    sourcePayeeId,
                                    sourceBankAccountId);
                            if (payeeBankAccount == null) {
                                payeeBankAccount = PayeeBankAccount.findActivePayeeBankAccount(company,
                                        sourcePayeeId,
                                        billPaymentSplit.getBankAccount().getAccountNumber(),
                                        billPaymentSplit.getBankAccount().getRoutingNumber(),
                                        BankAccountType.valueOf(billPaymentSplit.getBankAccount().getAccountType().value()));
                            }

                            billPaymentSplitDTO.setPayeeBankAccount(BillPaymentTranslator.buildPayeeBankAccountDTOFromBillPaymentSplitWSDTO(payeeBankAccount, billPaymentSplit.getBankAccount(), sessionId));
                            billPaymentSplitDTO.setReferenceNumber(billPaymentSplit.getReferenceNumber());
                            billPaymentSplitDTOs.add(billPaymentSplitDTO);

                            // add up the net total
                            netAmount = netAmount.add(SpcfUtils.convertToSpcfMoney(billPaymentSplit.getAmount()));
                        }

                        // validate deposit date is with in SpcfCalendar range
                        DateDTO depositDate = null;
                        try {
                            depositDate = new DateDTO(SpcfCalendar.createInstance(paymentTransaction.getDepositDate().getTime()));
                        } catch (SpcfIllegalArgumentException e) {
                            submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.invalidDate(paymentTransaction.getDepositDate().toString(), "DepositDate"));
                        }

                        if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                            return submitPaymentResponse;
                        }

                        billPaymentDTOs.add(BillPaymentTranslator.buildBillPaymentDTO(paymentTransaction,
                                payeeDTO,
                                billPaymentSplitDTOs,
                                depositDate,
                                new SpcfMoney(netAmount)));
                    }
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

                // list of unsubmitted billpayments
                ArrayList<BillPaymentDTO> unsubmittedBillPaymentDTOs = new ArrayList<BillPaymentDTO>();
                try {
                    PayrollServices.beginUnitOfWork();

                    Company tempCompany = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

                    BillPayment billPayment;
                    for (BillPaymentDTO billPaymentDTO : billPaymentDTOs) {
                        billPayment = BillPayment.findBillPaymentBySourceId(tempCompany, billPaymentDTO.getBillPaymentId());
                        if (billPayment != null) {
                            if (compareBillPaymentToBillPaymentDTO(billPayment, billPaymentDTO)) {
                                for (BillingDetail billingDetail : billPayment.getPayrollRun().getBillingDetailCollection()) {
                                    duplicateSubmissionDetail.add(BillPaymentTranslator.buildFeeTransaction(billingDetail, null, null));
                                }
                            } else {
                                submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.paymentAlreadySubmitted(billPaymentDTO.getBillPaymentId()));
                            }
                        } else {
                            unsubmittedBillPaymentDTOs.add(billPaymentDTO);
                        }
                    }

                    if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                        return submitPaymentResponse;
                    }

                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

                // commit all of the payee adds or updates
                ProcessResult<Payee> payeeResult;
                try {
                    PayrollServices.beginUnitOfWork();

                    for (PayeeDTO payeeDTO : payeeDTOMap.values()) {
                        payeeResult =
                                PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);

                        ErrorMessageList.mergeResults(payeeResult, submitPaymentResponse.getProcessingMessagesList());
                    }

                    // if there were any errors adding or updating the payees do not commit and return the errors
                    if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                        mapError1101(submitPaymentResponse, "AddOrUpdatePayee");
                        return submitPaymentResponse;
                    }

                    PayrollServices.commitUnitOfWork();

                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

                try {
                    PayrollServices.beginUnitOfWork();
                    ProcessResult<Collection<PayrollRun>> submitResult =
                            PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), unsubmittedBillPaymentDTOs);

                    ErrorMessageList.mergeResults(submitResult, submitPaymentResponse.getProcessingMessagesList());
                    if (submitPaymentResponse.getProcessingMessagesList().size() != 0) {
                        mapError1101(submitPaymentResponse, SystemCapabilityCode.SubmitPayment.toString());
                        return submitPaymentResponse;
                    }

                    transmissionDescription = BillPaymentMessageDescriptions.getPaymentAdded(unsubmittedBillPaymentDTOs.size());

                    Collection<PayrollRun> payrollRuns = submitResult.getResult();
                    for (PayrollRun payrollRun : payrollRuns) {
                        DomainEntitySet<BillingDetail> billingDetails = payrollRun.getBillingDetailCollection();
                        for (BillingDetail billingDetail : billingDetails) {
                            submitPaymentResponse.getFeeTransactions().add(BillPaymentTranslator.buildFeeTransaction(billingDetail, null, null));
                        }
                    }

                    // add any bill payments that were already submitted
                    submitPaymentResponse.getFeeTransactions().addAll(duplicateSubmissionDetail);

                    PayrollServices.commitUnitOfWork();

                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }

        } catch (Exception e) {
            logger.error("The bill payment ws encountered an unexpected error. PSID: " + pSubmitPaymentRequest.getPSID(), e);
            submitPaymentResponse.setFeeTransactions(new ArrayList<FeeTransaction>());
            submitPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.unexpectedError());
            transmissionDescription = BillPaymentMessageDescriptions.getErrorDescriptor() + e.getMessage();
        } finally {
            if (companyId != null) {
                BillPaymentTransmissionLogger.recordTransmissionResponse(TransmissionType.WSBillPaySendPaymentsToPayees, transmissionId, companyId, transmissionDescription, submitPaymentResponse);
                // if the transmission wasn't successful, create TransmissionError event
                List<QBProcessingMessage> messageList = submitPaymentResponse.getProcessingMessagesList();
                if (submitPaymentResponse.getProcessingMessagesList().size() > 0) {
                    for (QBProcessingMessage message : messageList) {
                        createTransmissionErrorEvent(sourceSystemCd, companyId, transmissionId, message.getMessage());
                    }
                }
                logger.info("Request for PSID=" + companyId + " is done.");
            }
        }

        Collections.sort(submitPaymentResponse.getFeeTransactions(), new FeeTransactionComparator());
        return submitPaymentResponse;
    }

    @WebMethod()
    @WebResult(name = "VoidPaymentResponse")
    public VoidPaymentResponse VoidPayments(@WebParam(name = "VoidPaymentRequest") VoidPaymentRequest pVoidPaymentRequest) {
        VoidPaymentResponse voidPaymentResponse = new VoidPaymentResponse();
        String transmissionId = UUID.randomUUID().toString();
        String companyId = null;
        SourceSystemCode sourceSystemCd = null;
        String transmissionDescription = BillPaymentMessageDescriptions.getVoidFailure();

        // used to send back the offloaded payment transactions
        ArrayList<PayrollRun> executedPayrolls = new ArrayList<PayrollRun>();
        // list of payments in the request that have already been offloaded
        ArrayList<String> paymentsNotToVoid = new ArrayList<String>();
        // map of payment transactions in the request to payrolls used to map the transactions in the response
        Map<PayrollRun, ArrayList<String>> payrollsTransactionIdsMap = new HashMap<PayrollRun, ArrayList<String>>();

        try {
            // check input parameters
            if (pVoidPaymentRequest == null) {
                voidPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.invalidArgument("SubmitPaymentRequest"));
                return voidPaymentResponse;
            }

            // validate QB version
            PayrollServices.beginUnitOfWork();
            OFXAPPVERObject ofxappverObject = CommonValidations.isQBVersionActive(pVoidPaymentRequest.getCompany().getClientApplicationVersion(), voidPaymentResponse);
            boolean isV20R8OrGreater = ofxappverObject.getIntQBVersion() > 20 || (ofxappverObject.getIntQBVersion() == 20 && ofxappverObject.getIntRNumber() >= 8);
            PayrollServices.rollbackUnitOfWork();
            if (voidPaymentResponse.getProcessingMessagesList().size() != 0) {
                return voidPaymentResponse;
            }

            // validate the company & PIN
            PayrollServices.beginUnitOfWork();
            Company company = CommonValidations.validateCompanyPin(pVoidPaymentRequest, voidPaymentResponse);
            PayrollServices.commitUnitOfWork();
            if (voidPaymentResponse.getProcessingMessagesList().size() != 0) {
                return voidPaymentResponse;
            }

            PayrollServices.beginUnitOfWork();
            companyId = company.getSourceCompanyId();
            sourceSystemCd = company.getSourceSystemCd();
            logger.info("Request for PSID=" + companyId + " being handled.");
            company = Company.findCompany(companyId, sourceSystemCd);
            BillPaymentTransmissionLogger.recordTransmissionRequest(TransmissionType.WSBillPayVoidPayments, pVoidPaymentRequest, transmissionId, companyId);

            for (String paymentId : pVoidPaymentRequest.getPaymentGUIDs()) {
                BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, paymentId);
                if (billPayment == null) {
                    voidPaymentResponse.getProcessingMessagesList().add(
                            ErrorMessageList.billPaymentDoesNotExist(paymentId,
                                    sourceSystemCd.toString(), companyId));
                } else {
                    // add the transaction to the map
                    if (!payrollsTransactionIdsMap.containsKey(billPayment.getPayrollRun())) {
                        payrollsTransactionIdsMap.put(billPayment.getPayrollRun(), new ArrayList<String>());
                    }
                    payrollsTransactionIdsMap.get(billPayment.getPayrollRun()).add(paymentId);

                    for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                        // check for offloaded transactions
                        if (billPaymentSplit.getFinancialTransaction() != null) {

                            MoneyMovementTransaction moneyMovementTransaction = billPaymentSplit.getFinancialTransaction().getMoneyMovementTransaction();
                            TransactionStateCode transactionStateCode = billPaymentSplit.getFinancialTransaction().getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd();

                            if ((moneyMovementTransaction != null && !PSPDate.getPSPTime().before(company.getOffloadGroup().getCalendarForCutoffTime(
                                    billPaymentSplit.getFinancialTransaction().getMoneyMovementTransaction().getInitiationDate().toLocal()))) ||
                                    (transactionStateCode == TransactionStateCode.Executed ||
                                            transactionStateCode == TransactionStateCode.Completed ||
                                            transactionStateCode == TransactionStateCode.Returned) || (transactionStateCode == TransactionStateCode.Cancelled)) {

                                paymentsNotToVoid.add(paymentId);
                                if (!executedPayrolls.contains(billPayment.getPayrollRun())) {
                                    executedPayrolls.add(billPayment.getPayrollRun());
                                }
                                // we don't need to check all of the splits
                                break;

                            }
                        }
                    }
                }
            }
            if (voidPaymentResponse.getProcessingMessagesList().size() != 0) {
                return voidPaymentResponse;
            }

            // remove the payments that cannot be voided
            for (String paymentId : paymentsNotToVoid) {
                pVoidPaymentRequest.getPaymentGUIDs().remove(paymentId);
            }

            if (pVoidPaymentRequest.getPaymentGUIDs().size() > 0) {
                ProcessResult<Collection<PayrollRun>> voidResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(sourceSystemCd,
                        companyId, pVoidPaymentRequest.getPaymentGUIDs(),pVoidPaymentRequest.getSessionId());

                ErrorMessageList.mergeResults(voidResult, voidPaymentResponse.getProcessingMessagesList());
                if (voidPaymentResponse.getProcessingMessagesList().size() != 0) {
                    return voidPaymentResponse;
                }

                transmissionDescription = BillPaymentMessageDescriptions.getPaymentVoided(pVoidPaymentRequest.getPaymentGUIDs().size());

                Collection<PayrollRun> payrollRuns = voidResult.getResult();
                for (PayrollRun payrollRun : payrollRuns) {
                    DomainEntitySet<BillingDetail> billingDetails = payrollRun.getBillingDetailCollection();
                    for (BillingDetail billingDetail : billingDetails) {
                        if (!isV20R8OrGreater) {
                            voidPaymentResponse.getFeeTransactions().add(BillPaymentTranslator.buildFeeTransaction(billingDetail, null, null));
                        } else {
                            voidPaymentResponse.getFeeTransactions().add(BillPaymentTranslator.buildFeeTransaction(billingDetail, false, null));
                        }
                    }
                }
            }

            // return the fee transactions for the payments that have already been offloaded
            if (!isV20R8OrGreater) {
                // this is temporary
                FeeTransaction feeTransaction;
                for (String paymentId : paymentsNotToVoid) {
                    feeTransaction = new FeeTransaction();
                    feeTransaction.setFeeAmount(new BigDecimal(0.00));

                    //If the bill payment was cancelled, we set the fee type to PerPaycheck; if it was already offloaded, we set the status to FundsNotRecovered
                    BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, paymentId);
                    if (billPayment!=null && billPayment.getStatus() == BillPaymentStatusCode.Inactive) {
                        feeTransaction.setFeeType(FeeTypeEnum.PerPaycheck);
                    } else {
                        feeTransaction.setFeeType(FeeTypeEnum.FundsNotRecovered);
                    }
                    feeTransaction.setNumberOfTransactions(0);
                    feeTransaction.setSettlementDate(null);
                    feeTransaction.setTaxAmount(new BigDecimal(0.00));
                    feeTransaction.setTransactionId(paymentId);
                    feeTransaction.setHasOffloaded(null);
                    feeTransaction.setAssociatedTransactionIds(null);
                    voidPaymentResponse.getFeeTransactions().add(feeTransaction);
                }
            } else {
                for (PayrollRun payrollRun : executedPayrolls) {
                    DomainEntitySet<BillingDetail> billingDetails = payrollRun.getBillingDetailCollection();
                    for (BillingDetail billingDetail : billingDetails) {
                        //If the fee associated with the billing detail was cancelled, we set the isOffloaded flag to false; otherwise, the txn was offloaded, so we keep the flag's value as true
                        Boolean isOffloaded = true;
                        FinancialTransaction fee = billingDetail.getFeeTransaction();

                        boolean billingDetailNotAssociatedWithFees = billingDetail.getFinancialTransactionCollection().size()==0;
                        boolean bilingDetailAssosciatedFeeIsCancelled = fee!=null && fee.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd() == TransactionStateCode.Cancelled;

                        if (billingDetailNotAssociatedWithFees || bilingDetailAssosciatedFeeIsCancelled) {
                            isOffloaded=false;
                        } 
                        
                        voidPaymentResponse.getFeeTransactions().add(BillPaymentTranslator.buildFeeTransaction(billingDetail, isOffloaded, payrollsTransactionIdsMap.get(payrollRun)));
                    }
                }
            }

            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {
            logger.error("The bill payment ws encountered an unexpected error", e);
            voidPaymentResponse.setFeeTransactions(new ArrayList<FeeTransaction>());
            voidPaymentResponse.getProcessingMessagesList().add(ErrorMessageList.unexpectedError());
            transmissionDescription = BillPaymentMessageDescriptions.getErrorDescriptor() + e.getMessage();
        } finally {
            PayrollServices.rollbackUnitOfWork();

            if (companyId != null) {
                BillPaymentTransmissionLogger.recordTransmissionResponse(TransmissionType.WSBillPayVoidPayments, transmissionId, companyId, transmissionDescription, voidPaymentResponse);
                // if the transmission wasn't successful, create TransmissionError event
                List<QBProcessingMessage> messageList = voidPaymentResponse.getProcessingMessagesList();
                if (voidPaymentResponse.getProcessingMessagesList().size() > 0) {
                    for (QBProcessingMessage message : messageList) {
                        createTransmissionErrorEvent(sourceSystemCd, companyId, transmissionId, message.getMessage());
                    }
                }
                logger.info("Request for PSID=" + companyId + " is done.");
            }
        }

        Collections.sort(voidPaymentResponse.getFeeTransactions(), new FeeTransactionComparator());
        return voidPaymentResponse;
    }

    @WebMethod()
    public QueryBillPaymentStatusResponse QueryPaymentStatus(@WebParam(name = "QueryBillPaymentStatusRequest") QueryBillPaymentStatusRequest pQueryBillPaymentStatusRequest) {
        QueryBillPaymentStatusResponse queryTransactionsStatusResponse = new QueryBillPaymentStatusResponse();
        String transmissionId = UUID.randomUUID().toString();
        String companyId = null;
        String transmissionDescription = BillPaymentMessageDescriptions.getQueryFailure();

        try {
            // validate the company & PIN
            PayrollServices.beginUnitOfWork();
            Company company = CommonValidations.validateCompanyPin(pQueryBillPaymentStatusRequest, queryTransactionsStatusResponse);
            PayrollServices.commitUnitOfWork();
            if (queryTransactionsStatusResponse.getProcessingMessagesList().size() != 0) {
                return queryTransactionsStatusResponse;
            }

            PayrollServices.beginUnitOfWork();
            companyId = company.getSourceCompanyId();
            logger.info("Request for PSID=" + companyId + " being handled.");
            BillPaymentTransmissionLogger.recordTransmissionRequest(TransmissionType.WSBillPayQueryPaymentStatus, pQueryBillPaymentStatusRequest, transmissionId, companyId);

            for (String billPaymentId : pQueryBillPaymentStatusRequest.getBillPaymentIds()) {
                BillPayment billPayment = null;
                try {
                    billPayment = BillPayment.findBillPaymentBySourceId(company, billPaymentId);
                } catch (RuntimeException e) {
                    logger.error("Unexpected error processing QBDTWS QueryPaymentStatus psid:" + company.getSourceCompanyId() + " and bill payment:" + billPaymentId + " - the bill payment exists more than once in the system.", e);
                    queryTransactionsStatusResponse.getProcessingMessagesList().add(ErrorMessageList.unexpectedError("bill payment", billPaymentId, "the bill payment exists more than once in the system."));
                }

                BillPaymentStatus billPaymentStatus = new BillPaymentStatus();
                billPaymentStatus.setSourcePaymentId(billPaymentId);
                for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                    BillPaymentSplitStatus billPaymentSplitStatus = new BillPaymentSplitStatus();
                    billPaymentSplitStatus.setSourcePaymentSplitId(billPaymentSplit.getSourceId());
                    FinancialTransaction financialTransaction = billPaymentSplit.getFinancialTransaction();
                    billPaymentSplitStatus.setTransactionState(TransactionStateEnum.valueOf(financialTransaction.getCurrentTransactionState().getTransactionStateCd().toString()));
                    DomainEntitySet<TransactionReturn> transactionReturns = TransactionReturn.findTransactionReturns(financialTransaction);
                    if (transactionReturns.size() > 0) {
                        for (TransactionReturn transactionReturn : transactionReturns) {
                            BillPaymentReturn billPaymentReturn = new BillPaymentReturn();
                            billPaymentReturn.setACHReturnReason(ACHReturnReasonEnum.valueOf(transactionReturn.getBankReturnCd()));
                            billPaymentReturn.setReturnDescription(ReturnReasonDesc.findReturnDescription(transactionReturn.getBankReturnCd()));
                            billPaymentReturn.setReturnStatus(ReturnStatusEnum.valueOf(transactionReturn.getReturnStatusCd().toString()));
                            billPaymentSplitStatus.getBillPaymentReturns().add(billPaymentReturn);
                        }
                    }
                    billPaymentStatus.getBillPaymentSplitStatuses().add(billPaymentSplitStatus);
                }
                queryTransactionsStatusResponse.getPaymentStatuses().add(billPaymentStatus);
            }

            transmissionDescription = BillPaymentMessageDescriptions.getQuery();

        } catch (Exception e) {
            logger.error("The bill payment ws encountered an unexpected error", e);
            queryTransactionsStatusResponse.getProcessingMessagesList().add(ErrorMessageList.unexpectedError());
            transmissionDescription = BillPaymentMessageDescriptions.getErrorDescriptor() + e.getMessage();
        } finally {
            PayrollServices.rollbackUnitOfWork();

            if (companyId != null) {
                BillPaymentTransmissionLogger.recordTransmissionResponse(TransmissionType.WSBillPayQueryPaymentStatus, transmissionId, companyId, transmissionDescription, queryTransactionsStatusResponse);
                logger.info("Request for PSID=" + companyId + " is done.");
            }
        }

        Collections.sort(queryTransactionsStatusResponse.getPaymentStatuses(), new BillPaymentStatusComparator());
        return queryTransactionsStatusResponse;
    }

    // error 1101 from the core covers any company changes, but does not mean anything to the client

    private void mapError1101(QBProcessingMessages pQBProcessingMessages, String companyInfoReplacement) {
        for (QBProcessingMessage qbProcessingMessage : pQBProcessingMessages.getProcessingMessagesList()) {
            if (qbProcessingMessage.getCode() == 1101) {
                qbProcessingMessage.setMessage(qbProcessingMessage.getMessage().replaceAll(SystemCapabilityCode.ChangeCompanyInfo.toString(), companyInfoReplacement));
            }
        }
    }

    private boolean compareBillPaymentToBillPaymentDTO(BillPayment pBillPayment, BillPaymentDTO pBillPaymentDTO) {
        if (!pBillPayment.getAmount().equals(pBillPaymentDTO.getAmount())) {
            return false;
        }
        boolean splitFound;
        for (BillPaymentSplit billPaymentSplit : pBillPayment.getBillPaymentSplitCollection()) {
            splitFound = false;
            for (BillPaymentSplitDTO billPaymentSplitDTO : pBillPaymentDTO.getPaymentTransactions()) {
                if (billPaymentSplit.getAmount().equals(SpcfUtils.convertToSpcfMoney(billPaymentSplitDTO.getAmount())) &&
                        billPaymentSplit.getPayeeBankAccount().getBankAccount().getAccountNumber().equals(billPaymentSplitDTO.getPayeeBankAccount().getBankAccount().getAccountNumber()) &&
                        billPaymentSplit.getPayeeBankAccount().getBankAccount().getRoutingNumber().equals(billPaymentSplitDTO.getPayeeBankAccount().getBankAccount().getRoutingNumber())) {
                    splitFound = true;
                    break;
                }
            }

            if (!splitFound) {
                return false;
            }
        }
        return true;
    }

    private void createTransmissionErrorEvent(final SourceSystemCode pSourceSystemCd, final String pCompanyId, final String pTransmissionId, final String pTransmissionDescription) {

        CompanyEventDTO tempCompanyEventDTO = new CompanyEventDTO();
        Collection<CompanyEventDetailDTO> eventDTOCollection = new ArrayList<CompanyEventDetailDTO>();
        tempCompanyEventDTO.setEventDetails(eventDTOCollection);
        tempCompanyEventDTO.setEventTypeCode(EventTypeCode.TransmissionError);
        CompanyEventDetailDTO coEventDetailErrMsgDTO = new CompanyEventDetailDTO();
        coEventDetailErrMsgDTO.setEventDetailTypeCode(EventDetailTypeCode.ErrorMessage);
        coEventDetailErrMsgDTO.setEventDetailValue(pTransmissionDescription);
        eventDTOCollection.add(coEventDetailErrMsgDTO);

        CompanyEventDetailDTO coEventDetailTranmissionIdDTO = new CompanyEventDetailDTO();
        coEventDetailTranmissionIdDTO.setEventDetailTypeCode(EventDetailTypeCode.TransmissionId);
        coEventDetailTranmissionIdDTO.setEventDetailValue(pTransmissionId);
        eventDTOCollection.add(coEventDetailTranmissionIdDTO);

        CompanyEventDetailDTO coEventDetailTransmissionIdDTO = new CompanyEventDetailDTO();
        coEventDetailTransmissionIdDTO.setEventDetailTypeCode(EventDetailTypeCode.ReasonDescription);
        coEventDetailTransmissionIdDTO.setEventDetailValue(pTransmissionDescription);
        eventDTOCollection.add(coEventDetailTransmissionIdDTO);


        final CompanyEventDTO companyEventDTO = tempCompanyEventDTO;

        TransactionThread<ProcessResult> thread = new TransactionThread<ProcessResult>() {
            public ProcessResult<CompanyEvent> transaction() {
                return PayrollServices.companyManager.addCompanyEvent(pSourceSystemCd, pCompanyId, companyEventDTO);
            }
        };

        PayrollServices.executeTransactionThread(thread);

        if (!thread.getProcessResult().isSuccess()) {
            logger.warn("Error for company " + pCompanyId + ". Could not add transmission error event with transmissionFailureMessage: " + pTransmissionDescription);
        }

    }
}

class BillPaymentTransmissionLogger {
    private static final SpcfLogger logger = PayrollServices.getLogger(BillPaymentTransmissionLogger.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(
                    Request.class,
                    QBProcessingMessages.class,
                    SubmitPaymentRequest.class,
                    SubmitPaymentResponse.class,
                    VoidPaymentRequest.class,
                    VoidPaymentResponse.class,
                    QueryBillPaymentStatusRequest.class,
                    QueryBillPaymentStatusResponse.class);
        } catch (Exception e) {
            logger.error("Unable to create Bill Payment JAXBContext for marshalling operation responses to transmission XML for recording in PSP", e);
        }
    }

    public static void recordTransmissionRequest(TransmissionType pTransmissionType, Request pRequest, String pTransmissionId, String pSourceCompanyId) throws IOException {
        TransmissionLogging.recordTransmissionRequest(pTransmissionType, pRequest, pTransmissionId, pSourceCompanyId, jaxbContext);
    }

    public static void recordTransmissionResponse(TransmissionType pTransmissionType, String pTransmissionId, String pSourceCompanyId, String pTransmissionDescription, QBProcessingMessages pResponse) {
        TransmissionLogging.recordTransmissionResponse(pTransmissionType, pTransmissionId, pSourceCompanyId, pResponse, pTransmissionDescription, jaxbContext);
    }
}

class FeeTransactionComparator implements java.util.Comparator<FeeTransaction> {
    public int compare(FeeTransaction a, FeeTransaction b) {
        if (a.getSettlementDate() == null && b.getSettlementDate() == null) {
            return 0;
        }

        if (a.getSettlementDate() == null) {
            return 1;
        }

        if (b.getSettlementDate() == null) {
            return -1;
        }
        return a.getSettlementDate().compareTo(b.getSettlementDate());
    }
}

class BillPaymentStatusComparator implements java.util.Comparator<BillPaymentStatus> {
    public int compare(BillPaymentStatus a, BillPaymentStatus b) {
        if (a.getSourcePaymentId() == null && b.getSourcePaymentId() == null) {
            return 0;
        }

        if (a.getSourcePaymentId() == null) {
            return 1;
        }

        if (b.getSourcePaymentId() == null) {
            return -1;
        }
        return a.getSourcePaymentId().compareTo(b.getSourcePaymentId());
    }
}
