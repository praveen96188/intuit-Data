package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.CreatePenaltiesAndInterestRefundsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.CreatePenaltiesAndInterestRefundsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.RefundEmployerFinancialTransactionResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyNote;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
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
        String corpId = requestDISDTO.getCorpId();

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

            try {
                PSPHelper.validateUserHasPermissionsInSAP(pCorpId, pToken, payrollAdapaterClass, REFUND_OPERATION_METHOD_NAME);
            } catch (Exception e) {
                responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                return;
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
