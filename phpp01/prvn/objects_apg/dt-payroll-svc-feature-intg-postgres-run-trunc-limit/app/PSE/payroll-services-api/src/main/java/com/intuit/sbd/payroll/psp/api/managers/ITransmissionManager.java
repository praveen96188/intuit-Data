package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Wiktor Kozlik
 */
public interface ITransmissionManager {
    /**
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param transmissionId
     * @param pTransmissionDTO
     * @return
     * @deprecated
     */
    public ProcessResult<SourceSystemTransmission> initializeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO);

    public ProcessResult<SourceSystemTransmission> initializeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO, SpcfCalendar pInitializeDateTime);

    /**
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param transmissionId
     * @param pTransmissionDTO
     * @return
     * @deprecated
     */
    public ProcessResult<SourceSystemTransmission> finalizeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO);

    public ProcessResult<SourceSystemTransmission> finalizeTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO, SpcfCalendar pFinalizeDateTime);

    public ProcessResult<SourceSystemTransmission> beginTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO);
    public ProcessResult<SourceSystemTransmission> endTransmission(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String transmissionId, SourceSystemTransmissionDTO pTransmissionDTO);

    public ProcessResult<PayrollRun> handleBALFOverPaymentTax(final String pTransmissionId, SpcfCalendar pQuarterStartDate);
}
