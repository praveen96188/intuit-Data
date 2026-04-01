package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.iam.HeaderUtils;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.CreatePenaltiesAndInterestRefundsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.CreatePenaltiesAndInterestRefundsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.handlers.PspUserAuthZHandler;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;

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
public class PSPCreatePenaltiesAndInterestRefunds extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPCreatePenaltiesAndInterestRefunds.class);
    }

    public static final int MAX_RESULT_CNT = 1000;
//    @Operation(operationIds = OperationId.CreateERPenaltiesAndInterestRefunds)
//    public DomainEntitySet<FinancialTransaction> createPenaltiesAndInterestRefunds(String pSourceSystemCd, String pSourceSystemId, double pPenaltiesRefundAmount, double pInterestRefundAmount, String pNote, String pSettlementTypeCd) throws Throwable {

    public static final String REFUND_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter";
    public static final String REFUND_OPERATION_METHOD_NAME = "createPenaltiesAndInterestRefunds";

    public static Class payrollAdapaterClass;

    static {
        try {
            payrollAdapaterClass = Class.forName(REFUND_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPCreatePenaltiesAndInterestRefunds static loader failed.");
        }
    }

    private CreatePenaltiesAndInterestRefundsRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pCreatePenaltiesAndInterestRefundsDISDTO
     *
     */
    public PSPCreatePenaltiesAndInterestRefunds(CreatePenaltiesAndInterestRefundsRequestDISDTO pCreatePenaltiesAndInterestRefundsDISDTO) {
        requestDISDTO = pCreatePenaltiesAndInterestRefundsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPCreatePenaltiesAndInterestRefunds.process()");

        responseDISDTO = new CreatePenaltiesAndInterestRefundsResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        SourceSystemCode sourceSystemCode = translateSourceSystemCode(sourceSystem);
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        BigDecimal penaltiesRefundAmount = null;
        if (requestDISDTO.getPenaltiesRefundAmount() != null) {
            penaltiesRefundAmount = requestDISDTO.getPenaltiesRefundAmount();
        }
        BigDecimal interestRefundAmount = null;
        if (requestDISDTO.getInterestRefundAmount() != null) {
            interestRefundAmount = requestDISDTO.getInterestRefundAmount();
        }
        String noteToAttachToRefund = requestDISDTO.getNoteToAttachToRefund();

        SettlementType settlementType = requestDISDTO.getSettlementType();

        String token = requestDISDTO.getToken();
        String corpId = null;
        if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
            corpId = RequestAttributesUtils.getAttribute(PspUserAuthZHandler.CORP_ID, String.class);
        }
        else{
            corpId = requestDISDTO.getCorpId();
        }

        doWork(sourceSystemCode,
               sourceCompanyId,
               penaltiesRefundAmount,
               interestRefundAmount,
               noteToAttachToRefund,
               settlementType.toString(),
               token,
               corpId);
        logger.debug("Leaving PSPCreatePenaltiesAndInterestRefunds.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemCode pSourceSystemCode,
                        String pSourceCompanyId,
                        BigDecimal pPenaltiesRefundAmount,
                        BigDecimal pInterestRefundAmount,
                        String pNote,
                        String pSettlementTypeCd,
                        String pToken,
                        String pCorpId) throws Throwable {
        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, pSourceSystemCode);
            if (company == null) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.companyDoesNotExist(pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            PayrollServices.rollbackUnitOfWork();
            if(!(HeaderUtils.isOfflineTicket())) {
                try {
                    PSPHelper.validateUserHasPermissionsInSAP(pCorpId, pToken, payrollAdapaterClass, REFUND_OPERATION_METHOD_NAME);
                } catch (Exception e) {
                    responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                    return;
                }
            }
            createRefund(pSourceSystemCode,
                         pSourceCompanyId,
                         pPenaltiesRefundAmount,
                         pInterestRefundAmount,
                         pNote,
                         pSettlementTypeCd,
                         pToken,
                         pCorpId
            );


        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void createRefund(SourceSystemCode pSourceSystemCode,
                              String pSourceSystemId,
                              BigDecimal pPenaltiesRefundAmount,
                              BigDecimal pInterestRefundAmount,
                              String pNote,
                              String pSettlementTypeCd,
                              String pToken,
                              String pCorpId
    ) throws Throwable {
        TaxAdapter taxAdapter = new TaxAdapter();

        double penaltiesRefundAmount = 0;
        double interestRefundAmount = 0;

        if (pPenaltiesRefundAmount != null) {
            penaltiesRefundAmount = pPenaltiesRefundAmount.doubleValue();
        }

        if (pInterestRefundAmount != null) {
            interestRefundAmount = pInterestRefundAmount.doubleValue();
        }

        DomainEntitySet<FinancialTransaction> financialTransactions = taxAdapter.createPenaltiesAndInterestRefunds(
                pSourceSystemCode.toString(),
                pSourceSystemId,
                penaltiesRefundAmount,
                interestRefundAmount,
                pNote,
                pSettlementTypeCd
        );
        for (FinancialTransaction financialTransaction : financialTransactions) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()) {
                case EmployerInterestRefundCredit:
                    ((CreatePenaltiesAndInterestRefundsResponseDISDTO) responseDISDTO).setInterestRefundTransactionId(financialTransaction.getId().toString());
                    break;
                case EmployerPenaltiesRefundCredit:
                    ((CreatePenaltiesAndInterestRefundsResponseDISDTO) responseDISDTO).setPenaltyRefundTransactionId(financialTransaction.getId().toString());
                    break;
            }
        }
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}
