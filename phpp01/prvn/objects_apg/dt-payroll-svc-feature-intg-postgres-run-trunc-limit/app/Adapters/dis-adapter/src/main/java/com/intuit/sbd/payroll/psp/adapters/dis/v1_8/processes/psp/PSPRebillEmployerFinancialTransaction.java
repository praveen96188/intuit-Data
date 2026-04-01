package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.iam.HeaderUtils;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.RebillEmployerFinancialTransactionRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.RebillEmployerFinancialTransactionResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.handlers.PspUserAuthZHandler;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 * <p/>
 * Query company event Process
 */
public class PSPRebillEmployerFinancialTransaction extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPRebillEmployerFinancialTransaction.class);
    }

    public static final int MAX_RESULT_CNT = 1000;
    public static final String REBILL_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter";
    public static final String REBILL_OPERATION_METHOD_NAME = "addRefundRebillTransaction";

    public static Class payrollAdapaterClass;

    static {
        try {
            payrollAdapaterClass = Class.forName(REBILL_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPRebillEmployerFinancialTransactions static loader failed.");
        }
    }

    private RebillEmployerFinancialTransactionRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pRebillEmployerFinancialTransactionsDISDTO
     *
     */
    public PSPRebillEmployerFinancialTransaction(RebillEmployerFinancialTransactionRequestDISDTO pRebillEmployerFinancialTransactionsDISDTO) {
        requestDISDTO = pRebillEmployerFinancialTransactionsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPRebillEmployerFinancialTransactions.process()");

        responseDISDTO = new RebillEmployerFinancialTransactionResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        String financialTxId = requestDISDTO.getFinancialTransactionId();
        Double rebillAmount = requestDISDTO.getRebillAmount().doubleValue();
        String noteToAttachToRebillEvent = requestDISDTO.getNoteToAttachToRebillEvent();

        String token = requestDISDTO.getToken();
        String corpId = null;
        if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
            corpId = RequestAttributesUtils.getAttribute(PspUserAuthZHandler.CORP_ID, String.class);
        }
        else{
            corpId = requestDISDTO.getCorpId();
        }

        doWork(sourceSystem,
               sourceCompanyId,
               financialTxId,
               rebillAmount,
               noteToAttachToRebillEvent,
               token,
               corpId);
        logger.debug("Leaving PSPRebillEmployerFinancialTransactions.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemEnum pSourceSystemCd,
                        String pSourceCompanyId,
                        String pFinancialTxId,
                        Double pRebillAmount,
                        String pNoteToAttachToRebillEvent,
                        String pToken,
                        String pCorpId
    ) throws Throwable {
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

            FinancialTransaction originalFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pFinancialTxId));
            if (originalFinancialTransaction == null || !originalFinancialTransaction.getCompany().getId().equals(company.getId())) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.financialTransactionIdNotFound(pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }
            String payrollRunId = null;
            if (originalFinancialTransaction.getPayrollRun() != null) {
                payrollRunId = originalFinancialTransaction.getPayrollRun().getSourcePayRunId();
            }

            int originalFnTxQuantity = originalFinancialTransaction.getBillingDetail().getQuantity();

            PayrollServices.rollbackUnitOfWork();

            Double perUnitRebillAmount = pRebillAmount / originalFnTxQuantity;
            if(!(HeaderUtils.isOfflineTicket())) {
                try {
                    PSPHelper.validateUserHasPermissionsInSAP(pCorpId, pToken, payrollAdapaterClass, REBILL_OPERATION_METHOD_NAME);
                } catch (Exception e) {
                    responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                    return;
                }
            }

            DomainEntitySet<BillingDetail> billingDetails = performRebill(
                    pSourceCompanyId,
                    pFinancialTxId,
                    perUnitRebillAmount,
                    payrollRunId,
                    pToken,
                    pCorpId
            );

            for (BillingDetail billingDetail : billingDetails) {
                String feeBillingDetailId = billingDetail.getId().toString();
                if (pNoteToAttachToRebillEvent != null) {
                    CompanyEvent feeRebillCompanyEvent = PSPHelper.findCompanyEventByTransactionId(company, feeBillingDetailId, EventDetailTypeCode.FeeBillingDetailId, EventTypeCode.FeeRebilled);
                    PSPHelper.addCompanyNote(company, pCorpId, feeRebillCompanyEvent.getId().toString(), pNoteToAttachToRebillEvent);
                }
                ((RebillEmployerFinancialTransactionResponseDISDTO) responseDISDTO).setFeeBillingDetailId(feeBillingDetailId);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private DomainEntitySet<BillingDetail> performRebill(String pSourceCompanyId,
                                        String pFinancialTxId,
                                        Double pRebillAmount,
                                        String pPayrollRunId,
                                        String pToken,
                                        String pCorpId
    ) throws Throwable {
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();

        ArrayList<ProcessResult> prResults = payrollRunAdapter.addRefundRebillTransaction(
                SourceSystemCode.QBDT.toString(),
                pSourceCompanyId,
                pPayrollRunId, // pPayRunId is only needed for exception handling.
                0, // pFinancialTxAmt is only used for refund, so setting to null.
                null, // pTxnDate is only used for refund, so setting to null.
                true,
                pFinancialTxId,
                pRebillAmount,
                null
        );

        return (DomainEntitySet<BillingDetail>) prResults.get(0).getResult();
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}
