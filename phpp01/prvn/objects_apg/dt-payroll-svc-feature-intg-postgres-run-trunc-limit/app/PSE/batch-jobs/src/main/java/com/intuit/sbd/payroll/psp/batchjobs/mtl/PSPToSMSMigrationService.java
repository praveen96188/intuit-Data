package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class PSPToSMSMigrationService {

    private PSPRequestContextManager pspRequestContextManager;

    @Autowired
    public PSPToSMSMigrationService(PSPRequestContextManager pspRequestContextManager) {
        this.pspRequestContextManager = pspRequestContextManager;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<SMSMigrationStatus> asyncMigrateCompany(String psId, boolean debugEnabled, boolean riskLmtMigrationEnabled) throws Exception {
        return CompletableFuture.completedFuture(migratePSPToSMS(psId, getTranscationID(), debugEnabled, riskLmtMigrationEnabled));
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<SMSMigrationStatus> asyncRevertSMSMigratedCompany(String psId) throws Exception {
        return CompletableFuture.completedFuture(revertSMSMigratedCompany(psId, getTranscationID()));
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<SMSMigrationStatus> asyncEnableSMSRevertedCompany(String psId) throws Exception {
        return CompletableFuture.completedFuture(enableSMSMigratedFlags(psId, getTranscationID()));
    }

    /**
     * Revert SMS migrated companies by disabling SMS flags and set SMS status as migration reverted.
     *
     * @param psId
     */
    private SMSMigrationStatus revertSMSMigratedCompany(String psId, String tid) {
        String logPrefix = "job=PSPtoSMSMigration, Action=revertSMSMigratedCompany, Status={}, psid={}, tid={}{}";
        try {
            MDC.put(IntuitCommonHeaders.INTUIT_HEADER_TID, tid);
            log.info(logPrefix, "Start", psId, tid, StringUtils.EMPTY);
            Application.beginUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, BatchJobType.PSPToSMSMigrationProcessor.toString());
            pspRequestContextManager.setRequestContextCompanyFromPSID(psId);
            ProcessResult<SMSMigrationStatus> pr = PayrollServices.companyManager.revertSMSMigratedCompany(psId, tid);
            Application.commitUnitOfWork();
            log.info(logPrefix, pr.getResult(), psId, tid, StringUtils.EMPTY);
            return pr.getResult();
        } catch (Exception e) {
            log.error(logPrefix, "Error", psId, tid, ", errType=ExceptionOccured, errMsg=" + e.getMessage(), e);
            return SMSMigrationStatus.MigrationError;
        } finally {
            Application.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
            MDC.clear();
        }
    }

    /**
     * Enable SMS flags again if batch job is run for reverted SMS migrated companies.
     *
     * @param psId
     */
    private SMSMigrationStatus enableSMSMigratedFlags(String psId, String tid) {
        String logPrefix = "job=PSPtoSMSMigration, Action=enableSMSMigratedFlags, Status={}, psid={}, tid={}{}";
        try {
            MDC.put(IntuitCommonHeaders.INTUIT_HEADER_TID, tid);
            log.info(logPrefix, "Start", psId, tid, StringUtils.EMPTY);
            Application.beginUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, BatchJobType.PSPToSMSMigrationProcessor.toString());
            pspRequestContextManager.setRequestContextCompanyFromPSID(psId);
            ProcessResult<SMSMigrationStatus> pr = PayrollServices.companyManager.enableSMSMigratedFlags(psId, tid);
            Application.commitUnitOfWork();
            log.info(logPrefix, pr.getResult(), psId, tid, StringUtils.EMPTY);
            return pr.getResult();
        } catch (Exception e) {
            log.error(logPrefix, "Error", psId, tid, ", errType=ExceptionOccured, errMsg=" + e.getMessage(), e);
            return SMSMigrationStatus.MigrationError;
        } finally {
            Application.rollbackUnitOfWork();
            MDC.clear();
            pspRequestContextManager.clearRequestContext();
        }
    }

    /**
     * Migrates companies from PSP to SMS by calling SMS's migrate end point
     *
     * @param psId
     * @param tid
     * @return SMSMigrationStatus
     */
    private SMSMigrationStatus migratePSPToSMS(String psId, String tid, boolean debugEnabled, boolean riskLmtMigrationEnabled) {
        String logPrefix = "job=PSPtoSMSMigration, Action=migratePSPToSMS, Status={}, psid={}, tid={}, debugEnabled={}, riskLmtMigrationEnabled={}{}";

        try {
            MDC.put(IntuitCommonHeaders.INTUIT_HEADER_TID, tid);
            log.info(logPrefix, "Start", psId, tid, debugEnabled, riskLmtMigrationEnabled, StringUtils.EMPTY);
            Application.beginUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.OLAP, BatchJobType.PSPToSMSMigrationProcessor.toString());
            pspRequestContextManager.setRequestContextCompanyFromPSID(psId);
            ProcessResult<SMSMigrationStatus> pr = PayrollServices.companyManager.migratePSPToSMS(psId, tid, debugEnabled, riskLmtMigrationEnabled);

            if (!pr.isSuccess()) {
                pr.setResult(SMSMigrationStatus.MigrationError);
                log.error(logPrefix, pr.getResult(), psId, tid, debugEnabled, riskLmtMigrationEnabled,  ", errType=ValidationFailed, errMsg=" + pr.getMessages());
            } else {
                log.info(logPrefix, pr.getResult(), psId, tid, debugEnabled, riskLmtMigrationEnabled, StringUtils.EMPTY);
            }
            Application.commitUnitOfWork();
            return pr.getResult();

        } catch (Exception e) {
            log.error(logPrefix, "Error", psId, tid, debugEnabled, riskLmtMigrationEnabled, ", errType=" + e.getClass().getSimpleName() + ", errMsg=" + e.getMessage(), e);
            return SMSMigrationStatus.MigrationError;
        } finally {
            Application.rollbackUnitOfWork();
            MDC.clear();
            pspRequestContextManager.clearRequestContext();
        }
    }

    private String getTranscationID() {
        return UUID.randomUUID().toString();
    }
}
