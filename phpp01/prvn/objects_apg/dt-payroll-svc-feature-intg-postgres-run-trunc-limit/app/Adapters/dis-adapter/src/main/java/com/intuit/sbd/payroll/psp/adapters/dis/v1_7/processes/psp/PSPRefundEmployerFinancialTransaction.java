package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.RefundEmployerFinancialTransactionRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.RefundEmployerFinancialTransactionResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.SAPAuthHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyNote;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Date;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPRefundEmployerFinancialTransaction.java $
 * $Revision: #1 $
 * $DateTime: 2012/08/30 22:27:03 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPRefundEmployerFinancialTransaction extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPRefundEmployerFinancialTransaction.class);
    }

    public static final int MAX_RESULT_CNT = 1000;
    public static final String REFUND_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter";
    public static final String REFUND_OPERATION_METHOD_NAME = "refundEmployerTransaction";

    public static Class payrollAdapaterClass;
    static {
        try {
            payrollAdapaterClass = Class.forName(REFUND_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPRefundEmployerFinancialTransactions static loader failed.");
        }
    }

    private RefundEmployerFinancialTransactionRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pRefundEmployerFinancialTransactionsDISDTO
     *
     */
    public PSPRefundEmployerFinancialTransaction(RefundEmployerFinancialTransactionRequestDISDTO pRefundEmployerFinancialTransactionsDISDTO) {
        requestDISDTO = pRefundEmployerFinancialTransactionsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPRefundEmployerFinancialTransactions.process()");

        responseDISDTO = new RefundEmployerFinancialTransactionResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        String financialTxId = requestDISDTO.getFinancialTransactionId();
        Double refundAmount = requestDISDTO.getRefundAmount().doubleValue();
        String noteToAttachToRefundEvent = requestDISDTO.getNoteToAttachToRefundEvent();

        SettlementType settlementType = requestDISDTO.getSettlementType();

        String token = requestDISDTO.getToken();
        String corpId = requestDISDTO.getCorpId();

        doWork(sourceSystem,
                sourceCompanyId,
                financialTxId,
                refundAmount,
                settlementType,
                noteToAttachToRefundEvent,
                token,
                corpId);
        logger.debug("Leaving PSPRefundEmployerFinancialTransactions.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemEnum pSourceSystemCd,
                          String pSourceCompanyId,
                          String pFinancialTxId,
                          Double pRefundAmount,
                          SettlementType pSettlementType,
                          String pNoteToAttachToRefundEvent,
                          String pToken,
                          String pCorpId) throws Throwable {
        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, translateSourceSystemCode(pSourceSystemCd));
            if (company == null) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.companyDoesNotExist(pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            PayrollServices.rollbackUnitOfWork();

            try {
                PSPHelper.validateUserHasPermissionsInSAP(pCorpId,pToken,payrollAdapaterClass, REFUND_OPERATION_METHOD_NAME);
            } catch (Exception e) {
                responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                return;
            }

            performRefund(pSourceCompanyId,
                    pFinancialTxId,
                    pRefundAmount,
                    pSettlementType.toString(),
                    pToken,
                    pCorpId
            );

            String refundTransactionId = getRefundTransactionId(pFinancialTxId);

            if (pNoteToAttachToRefundEvent != null) {
                addRefundEventNote(company,pFinancialTxId,refundTransactionId,pNoteToAttachToRefundEvent,pCorpId);
            }

            ((RefundEmployerFinancialTransactionResponseDISDTO)responseDISDTO).setRefundTransactionId(refundTransactionId);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void addRefundEventNote(Company pCompany,String pOriginalFnTxId,String pRefundTxId,String pNoteToAttachToRefundEvent,String pUserId) throws Throwable {

        PayrollServices.beginUnitOfWork();
        FinancialTransaction originalFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pOriginalFnTxId));
        FinancialTransaction refundFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pRefundTxId));
        CompanyEvent companyEvent = getRefundEvent(pCompany,refundFinancialTransaction.getId().toString());
        PayrollServices.rollbackUnitOfWork();

        CompanyAdapter companyAdapter = new CompanyAdapter();
        //pSourceSystemCd, String pCompanyId, String companyEventId, String companyEventTransmissionId, SAPCompanyNote sapCompanyNote
        SAPCompanyNote note = new SAPCompanyNote();
        note.setInsertUserId(pUserId);
        note.setNotes(pNoteToAttachToRefundEvent);
        companyAdapter.addCompanyNote(pCompany.getSourceSystemCd().toString(),pCompany.getSourceCompanyId(), companyEvent.getId().toString(),null,note);
    }

    private CompanyEvent getRefundEvent(Company pCompany,String pRefundTxId) throws Exception {
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(pCompany,EventTypeCode.FeeRefunded);
        for (CompanyEvent companyEvent : companyEvents) {
            DomainEntitySet<CompanyEventDetail> eventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId);
            for (CompanyEventDetail companyEventDetail : eventDetails) {
                if (companyEventDetail.getValue().equals(pRefundTxId)) {
                    return companyEvent;
                }
            }
        }
        return null;
    }

    private String getRefundTransactionId(String pFinancialTxId) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction originalFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pFinancialTxId));
            DomainEntitySet<FinancialTransaction> associatedTxCollection = originalFinancialTransaction.getAssociatedTransactionsCollection();
            FinancialTransaction refundTx = null;
            for (FinancialTransaction finTx : associatedTxCollection) {
                if (finTx.getTransactionType().getAssociationType().equals(TransactionAssociationType.Refund)) {
                    refundTx = finTx;
                    break;
                }
            }
            if (refundTx == null) {
                throw new Exception("Could not find associated refund transaction");
            }
            return refundTx.getId().toString();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

//    private void performRebill(String pSourceCompanyId,
//                               String pFinancialTxId,
//                               Double pFinancialTxAmt,
//                               Date pTxnDate,
//                               String pSettlementType
//    ) {
//        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
//
//        payrollRunAdapter.addRefundRebillTransaction(
//                SourceSystemCode.QBDT.toString(),
//                pSourceCompanyId,
//                pFinancialTxId,
//                pFinancialTxAmt,
//                pTxnDate,
//                pSettlementType);
//    }


    private void performRefund(String pSourceCompanyId,
                               String pFinancialTxId,
                               Double pFinancialTxAmt,
                               String pSettlementType,
                               String pToken,
                               String pCorpId
    ) throws Throwable {
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();

        Date transactionDate = new Date();
        payrollRunAdapter.refundEmployerTransaction(
                SourceSystemCode.QBDT.toString(),
                pSourceCompanyId,
                pFinancialTxId,
                pFinancialTxAmt,
                transactionDate,
                pSettlementType);
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}
