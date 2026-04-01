package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.FinancialTransactionDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryEmployerFinancialTransactionsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryEmployerFinancialTransactionsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollRun;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.List;

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
public class PSPQueryEmployerFinancialTransactions extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryEmployerFinancialTransactions.class);
    }

    public static final int MAX_RESULT_CNT = 1000;

    private QueryEmployerFinancialTransactionsRequestDISDTO requestDISDTO;
    private QueryEmployerFinancialTransactionsResponseDISDTO responseDISDTO;
    private String reundTransactionId;

    /**
     * Constructor
     *
     * @param pQueryEmployerFinancialTransactionsDISDTO
     *
     */
    public PSPQueryEmployerFinancialTransactions(QueryEmployerFinancialTransactionsRequestDISDTO pQueryEmployerFinancialTransactionsDISDTO) {
        requestDISDTO = pQueryEmployerFinancialTransactionsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryEmployerFinancialTransactions.process()");
        responseDISDTO = new QueryEmployerFinancialTransactionsResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();
        String sourcePayRunId = requestDISDTO.getSourcePayRunId();

        List<FinancialTransactionDISDTO> financialTransactions = doWork(sourceSystem,sourceCompanyId,sourcePayRunId);
        responseDISDTO.setFinancialTransactionDISDTOs(financialTransactions);
        logger.debug("Leaving PSPQueryEmployerFinancialTransactions.process()");
        return responseDISDTO;
    }

    private List<FinancialTransactionDISDTO> doWork(SourceSystemEnum pSourceSystemCd,String pSourceCompanyId,String pSourcePayRunId) throws Throwable {

        List<FinancialTransactionDISDTO> financialTransactionDISDTOs = new ArrayList<FinancialTransactionDISDTO>();
        try {
            PayrollServices.beginUnitOfWork();
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            Company company = Company.findCompany(pSourceCompanyId, translateSourceSystemCode(pSourceSystemCd));
            if (company == null) {
                throw new DISException(DISMessages.companyDoesNotExist(pSourceCompanyId));
            }
            PayrollServices.rollbackUnitOfWork();

            if (!payrollExists(pSourceCompanyId, pSourceSystemCd.toString(), pSourcePayRunId)) {
                throw new DISException(DISMessages.payrollNotFound(pSourcePayRunId,pSourceCompanyId));
            }
            SAPPayrollRun sapPayrollRun = payrollRunAdapter.findPayrollRun(company.getSourceSystemCd().toString(),company.getSourceCompanyId(),pSourcePayRunId);

            ArrayList<SAPPayrollTransaction> sapPayrollTransactions = payrollRunAdapter.findEmployerTransactions(
                    pSourceCompanyId.toString(),
                    pSourceSystemCd.toString(),
                    sapPayrollRun.getSourcePayRunId(),
                    null,
                    null
            );

//            PayrollRun payrollRun = PayrollRun.findPayrollRun(company,sapPayrollRun.getId());
//            DomainEntitySet<FinancialTransaction> refundedTransaction = FinancialTransaction.findFinancialTransactionsByAssociationType(company,payrollRun,TransactionAssociationType.Refund);
//            //@TODO Better way per Raffi meeting.  Get all refund transactions so we don't need to query each transaction.
//            //    Below is code I started to implement this logic, ran out of time.
//
//            Map<String,String> refundedTransactionMap = new TreeMap<String, String>();
//            for (FinancialTransaction refundedFinancialTransaction : refundedTransaction) {
//                FinancialTransaction originalFinancialTransaction = refundedFinancialTransaction.getOriginalTransaction();
//                refundedTransactionMap.put(originalFinancialTransaction.getId().getStandardFormatString(),refundedFinancialTransaction.getId().getStandardFormatString());
//            }
//
//            for (SAPPayrollTransaction sapPayrollTransaction : sapPayrollTransactions) {
//                String refundTransactionId = null;
//                if (refundedTransactionMap.containsKey(sapPayrollTransaction.getId())) {
//                    refundTransactionId = refundedTransactionMap.get(sapPayrollTransaction.getId());
//                }

            for (SAPPayrollTransaction sapPayrollTransaction : sapPayrollTransactions) {
                String refundTransactionId = getRefundTransactionId(sapPayrollTransaction);
                FinancialTransactionDISDTO financialTransactionDISDTO = new FinancialTransactionDISDTO(sapPayrollTransaction,refundTransactionId);
                financialTransactionDISDTOs.add(financialTransactionDISDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return financialTransactionDISDTOs;
    }

    private boolean payrollExists(String pCompanyId, String pSourceSystemCd,String pSourcePayRunId) {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            PayrollRun payrollRunDE = PayrollRun.findPayrollRun(company, pSourcePayRunId);
            return payrollRunDE!=null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

    public String getRefundTransactionId(SAPPayrollTransaction sapPayrollTransaction) {
        PayrollServices.beginUnitOfWork();
        try {
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sapPayrollTransaction.getId()));
            DomainEntitySet<FinancialTransaction> associatedTxCollection = financialTransaction.getAssociatedTransactionsCollection();
            TransactionStateCode childTxStateCode = null;
            for (FinancialTransaction finTx : associatedTxCollection) {
                childTxStateCode = finTx.calculateCurrentTransactionState().getTransactionStateCd();
                if (finTx.getTransactionType().getAssociationType().equals(TransactionAssociationType.Refund)) {
                    return finTx.getId().getStandardFormatString();
                }
            }
            return reundTransactionId;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
