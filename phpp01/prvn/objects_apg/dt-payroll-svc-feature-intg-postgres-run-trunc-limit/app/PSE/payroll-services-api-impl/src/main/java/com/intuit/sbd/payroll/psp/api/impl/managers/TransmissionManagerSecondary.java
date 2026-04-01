package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.api.managers.ITransmissionManagerSecondary;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.domainsecondary.processes.TransactionThreadSecondary;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * @author Wiktor Kozlik
 */
public class TransmissionManagerSecondary implements ITransmissionManagerSecondary {

    private static SpcfLogger logger = SpcfLogManager.getLogger(TransmissionManagerSecondary.class);

    /**
     * Initializes transmission - this API needs to be called outside a unit of work
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pTransmissionId
     * @param pTransmissionDTO
     * @return
     * @deprecated
     */
    public ProcessResult<SourceSystemTransmission> initializeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pTransmissionDTO) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            ProcessResult<SourceSystemTransmission> result =
                    new InitializeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO).execute();
            PayrollServices.commitUnitOfWorkWithSecondary();
            return result;
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    public ProcessResult<SourceSystemTransmission> initializeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pTransmissionDTO, SpcfCalendar pInitializeDateTime) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            ProcessResult<SourceSystemTransmission> result =
                    new InitializeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO, pInitializeDateTime).execute();
            PayrollServices.commitUnitOfWorkWithSecondary();
            return result;
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    public ProcessResult<SourceSystemTransmission> beginTransmission(final SourceSystemCode pSourceSystemCd, final String pSourceCompanyId, final String pTransmissionId, final SourceSystemTransmissionDTO pTransmissionDTO) {
        TransactionThreadSecondary<ProcessResult> thread = new TransactionThreadSecondary() {
            public ProcessResult<SourceSystemTransmission> transaction() {
                return new InitializeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO).execute();
            }
        };
        PayrollServices.executeTransactionThread(thread);
        return thread.getProcessResult();
    }

    /**
     * Finalizes transmission - this API needs to be called outside a unit of work
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pTransmissionId
     * @param pTransmissionDTO
     * @return
     * @deprecated
     */
    public ProcessResult<SourceSystemTransmission> finalizeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pTransmissionDTO) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            ProcessResult<SourceSystemTransmission> result =
                    new FinalizeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO).execute();
            PayrollServices.commitUnitOfWorkWithSecondary();
            return result;
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    public ProcessResult<SourceSystemTransmission> finalizeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pTransmissionDTO, SpcfCalendar pFinalizeDateTime) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            ProcessResult<SourceSystemTransmission> result =
                    new FinalizeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO, pFinalizeDateTime).execute();
            PayrollServices.commitUnitOfWorkWithSecondary();
            return result;
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    public ProcessResult<SourceSystemTransmission> endTransmission(final SourceSystemCode pSourceSystemCd, final String pSourceCompanyId, final String pTransmissionId, final SourceSystemTransmissionDTO pTransmissionDTO) {
        TransactionThreadSecondary<ProcessResult> thread = new TransactionThreadSecondary() {
            public ProcessResult<SourceSystemTransmission> transaction() {
                return new FinalizeSourceSystemTransmissionSecondaryCore(pSourceSystemCd, pSourceCompanyId, pTransmissionId, pTransmissionDTO).execute();
            }
        };
        PayrollServices.executeTransactionThread(thread);
        return thread.getProcessResult();
    }


    public ProcessResult<PayrollRun> handleBALFOverPaymentTax(final String pTransmissionId, SpcfCalendar pQuarterStartDate) {
        HandleBALFOverPaymentTax handleBALFOverPaymentTax = new HandleBALFOverPaymentTax(pTransmissionId, pQuarterStartDate);
        return handleBALFOverPaymentTax.execute();
    }
}