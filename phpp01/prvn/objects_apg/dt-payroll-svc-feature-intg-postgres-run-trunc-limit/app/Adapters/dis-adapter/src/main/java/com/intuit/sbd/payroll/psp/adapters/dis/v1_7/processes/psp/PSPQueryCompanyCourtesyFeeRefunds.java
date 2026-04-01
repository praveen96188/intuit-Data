package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.LedgerTransactionDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyCourtesyFeeRefundsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyCourtesyFeeRefundsResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryCompanyCourtesyFeeRefunds.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 * <p/>
 * Query company event Process
 */
public class PSPQueryCompanyCourtesyFeeRefunds extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyCourtesyFeeRefunds.class);
    }

    public static final int MAX_RESULT_CNT = 1000;

    private QueryCompanyCourtesyFeeRefundsRequestDISDTO request;
    private QueryCompanyCourtesyFeeRefundsResponseDISDTO response;

    /**
     * Constructor
     *
     * @param pQueryCompanyCourtesyFeeRefundsRequestDISDTO
     *
     */
    public PSPQueryCompanyCourtesyFeeRefunds(QueryCompanyCourtesyFeeRefundsRequestDISDTO pQueryCompanyCourtesyFeeRefundsRequestDISDTO) {
        request = pQueryCompanyCourtesyFeeRefundsRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyCourtesyFeeRefunds.process()");
        response = new QueryCompanyCourtesyFeeRefundsResponseDISDTO();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(request.getSourceCompanyId(),translateSourceSystemCode(request.getSourceSystem()));
        if (company == null) {
            throw new DISException(DISMessages.companyDoesNotExist(request.getSourceCompanyId()));
        }
        PayrollServices.rollbackUnitOfWork();

        doWork(request.getSourceSystem(),request.getSourceCompanyId(),request.getFromDate());
        logger.debug("Leaving PSPQueryCompanyCourtesyFeeRefunds.process()");
        return response;
    }

    private void doWork(SourceSystemEnum pSourceSystemEnum,
                        String pSourceCompanyId,
                        Calendar pFromDate) throws Throwable {
        try {
            List<LedgerTransactionDISDTO> financialLedgerTransactionDISDTOs = new ArrayList<LedgerTransactionDISDTO>();
            TaxAdapter taxAdapter = new TaxAdapter();

            List<SAPTransaction> sapTransactions = taxAdapter.findCourtesyRefundTransactions(
                    translateSourceSystemCode(pSourceSystemEnum).toString(),
                    pSourceCompanyId.toString()
            );

            long fromDateMillis = 0;
            if (pFromDate != null) {
                fromDateMillis = pFromDate.getTimeInMillis();
            }
            for (SAPTransaction sapTransaction : sapTransactions) {
                if (fromDateMillis > 0) {
                    if (fromDateMillis > sapTransaction.getSettlementDate().getTime()) {
                        continue;
                    }
                }
                LedgerTransactionDISDTO ledgerTransactionDISDTO = new LedgerTransactionDISDTO(sapTransaction);
                financialLedgerTransactionDISDTOs.add(ledgerTransactionDISDTO);
            }
            response.setLedgerTransactions(financialLedgerTransactionDISDTOs);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public ResponseDISDTO getResponse() {
        return response;
    }

}
