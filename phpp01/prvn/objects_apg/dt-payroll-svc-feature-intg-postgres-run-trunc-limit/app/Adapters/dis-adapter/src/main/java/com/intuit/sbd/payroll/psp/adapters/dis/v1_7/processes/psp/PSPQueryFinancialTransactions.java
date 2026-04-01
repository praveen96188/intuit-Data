package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.FinancialTransactionDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryFinancialTransactionsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryFinancialTransactionsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryFinancialTransactions.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPQueryFinancialTransactions extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryFinancialTransactions.class);
    }

    private QueryFinancialTransactionsRequestDISDTO requestDISDTO;
    private QueryFinancialTransactionsResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryFinancialTransactionsDISDTO
     *
     */
    public PSPQueryFinancialTransactions(QueryFinancialTransactionsRequestDISDTO pQueryFinancialTransactionsDISDTO) {
        requestDISDTO = pQueryFinancialTransactionsDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryFinancialTransactions.process()");
        responseDISDTO = new QueryFinancialTransactionsResponseDISDTO();

        List<String> transactionIds = requestDISDTO.getTransactionIds();
        // if nether passed then fail

        List<FinancialTransactionDISDTO> financialTransactions = doWork(transactionIds, null);
        responseDISDTO.setFinancialTransactionDISDTOs(financialTransactions);
        logger.debug("Leaving PSPQueryFinancialTransactions.process()");
        return responseDISDTO;
    }

    private List<FinancialTransactionDISDTO> doWork(List<String> pTransactionIds, String sourceCompanyId ) throws Throwable {

        List<FinancialTransactionDISDTO> financialTransactionDISDTOs = new ArrayList<FinancialTransactionDISDTO>();
        try {
            if (pTransactionIds == null) {
                return financialTransactionDISDTOs;
            }
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            for (String transactionId : pTransactionIds) {
                if (transactionId != null) {
                    try {
                        SAPPayrollTransaction sapPayrollTransaction = payrollRunAdapter.findPayrollTransactionById(transactionId, sourceCompanyId);
                        String refundTransactionId = getRefundTransactionId(sapPayrollTransaction);
                        FinancialTransactionDISDTO financialTransactionDISDTO = new FinancialTransactionDISDTO(sapPayrollTransaction, refundTransactionId);
                        financialTransactionDISDTOs.add(financialTransactionDISDTO);
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                        throw new DISException(DISMessages.objectNotFound("SAPPayrollTransaction", transactionId), t);
                    }
                }
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return financialTransactionDISDTOs;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

    public String getRefundTransactionId(SAPPayrollTransaction sapPayrollTransaction) {
        PayrollServices.beginUnitOfWork();
        try {
            String refundTransactionId = null;
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(sapPayrollTransaction.getId()));
            DomainEntitySet<FinancialTransaction> associatedTxCollection = financialTransaction.getAssociatedTransactionsCollection();
            for (FinancialTransaction finTx : associatedTxCollection) {
                if (finTx.getTransactionType().getAssociationType().equals(TransactionAssociationType.Refund)) {
                    return finTx.getId().getStandardFormatString();
                }
            }
            return refundTransactionId;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
