package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.billing.CompanyDTO;
import com.intuit.sbd.payroll.psp.processes.billing.CreateBillingUsage;
import com.intuit.sbd.payroll.psp.processes.billing.EmployeeDTO;
import com.intuit.sbd.payroll.psp.processes.billing.PaycheckDTO;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/19/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class SyncPSPToEMSBS {
    private static SpcfLogger logger = Application.getLogger(SyncPSPToEMSBS.class);

    public void sync() {
        try {
            StopWatch sw = new StopWatch().start();

            // get last token and the events to process
            HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> payrollRunsToProcess = new HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>>();
            long newToken = findNextSetPayrollRunsToProcess(payrollRunsToProcess);

            // process
            if (payrollRunsToProcess.size() > 0) {
                multithreadProcessing(payrollRunsToProcess, true);
                updateSyncToken(newToken);
            }
            sw.stop();
            logger.info("completed processing - end token: " + newToken + "     events: " + payrollRunsToProcess.values().size() + "     duration: " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.error("failed to sync PSP to EMSBS", t);
        }
    }

    protected long findNextSetPayrollRunsToProcess(HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> pPayrollRunsToProcess) throws Exception {
        long oldToken = -1;
        long newToken = -1;
        pPayrollRunsToProcess.clear();

        try {
            Application.beginUnitOfWork();

            oldToken = SystemParameter.findLongValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN);
            logger.info("PSP_TO_EMSBS_SYNC_TOKEN: " + oldToken);

            int maxPayrollRunsProcessedPerBatch = SystemParameter.findIntValue(SystemParameter.Code.MAX_PAYRUNS_PER_BILLING_BATCH);
            logger.info("MAX_PAYRUNS_PER_BILLING_BATCH: " + maxPayrollRunsProcessedPerBatch);


            List<Object[]> companyAndPayrollRunIds = Application.executeNamedQuery("findPayrollRunsForBillingProcess", new String[]{"lastToken"}, new Object[]{oldToken}, -1, maxPayrollRunsProcessedPerBatch);

            pPayrollRunsToProcess.putAll(groupPayrollRunIdsByCompany(companyAndPayrollRunIds));

            if (!companyAndPayrollRunIds.isEmpty()) {
                Object[] lastRow = companyAndPayrollRunIds.get(companyAndPayrollRunIds.size() - 1);
                newToken = Long.parseLong(lastRow[2].toString());
            }

            logger.info("fetched " + companyAndPayrollRunIds.size() + " payroll runs. The new token is " + newToken);

            return newToken;
        } catch (Throwable t) {
            logger.fatal("Exception in SyncPSPToEMSBS.findNextSetPayrollRunsToProcess(). Started from old token " + oldToken, t);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> groupPayrollRunIdsByCompany(List<Object[]> pIds) {
        HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> companyAndPayRunIds = new HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>>();
        for (Object[] row : pIds) {
            SpcfUniqueId companyId = new SpcfUniqueIdImpl(row[0].toString());
            SpcfUniqueId payrunId = new SpcfUniqueIdImpl(row[1].toString());

            ArrayList<SpcfUniqueId> payrollRunIds = companyAndPayRunIds.get(companyId);
            if (payrollRunIds == null) {
                payrollRunIds = new ArrayList<SpcfUniqueId>();
                companyAndPayRunIds.put(companyId, payrollRunIds);
            }
            payrollRunIds.add(payrunId);
        }
        return companyAndPayRunIds;
    }

    protected void updateSyncToken(Long newToken) throws Exception {
        try {
            Application.beginUnitOfWork();
            ProcessResult pr = PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, newToken.toString());
            if (!pr.isSuccess()) {
                logger.error("failed to write new token (" + newToken + ") to PSP_SYSTEM_PARAMETER under key: " + SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN);
            }
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("failed to write new token (" + newToken + ") to PSP_SYSTEM_PARAMETER under key: " + SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public void syncFailed() {
        try {
            StopWatch sw = new StopWatch().start();

            // get events to process
            HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> payrollRunsToProcess = findFailedPayrollRunsToProcess();

            // process
            if (payrollRunsToProcess.size() > 0) {
                HashSet<SpcfUniqueId> successfulPayrollRuns = multithreadProcessing(payrollRunsToProcess, false);
                updateStatusInFailedTable(successfulPayrollRuns, payrollRunsToProcess);
            }
            sw.stop();
            logger.info("tried to sync payrollruns from the failed table,  completed " + payrollRunsToProcess.keySet().size() + " ones in " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.error("failed to sync PSP to EMSBS", t);
        }
    }

    protected HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> findFailedPayrollRunsToProcess() throws Exception {
        try {
            Application.beginUnitOfWork();

            int maxPayrollRunsProcessedPerBatch = SystemParameter.findIntValue(SystemParameter.Code.MAX_PAYRUNS_PER_BILLING_BATCH);
            logger.info("MAX_PAYRUNS_PER_BILLING_BATCH: " + maxPayrollRunsProcessedPerBatch);

            List<Object[]> companyAndPayrollRunIds = Application.executeNamedQuery("findFailedPayrollRunsForBillingProcess", new String[]{"aStatusToken"}, new Object[]{SyncStatus.Pending}, 0, maxPayrollRunsProcessedPerBatch);

            HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> failedPayrollRunsToProcess = groupPayrollRunIdsByCompany(companyAndPayrollRunIds);

            logger.info("fetched " + companyAndPayrollRunIds.size());

            return failedPayrollRunsToProcess;
        } catch (Throwable t) {
            logger.fatal("Exception in SyncPSPToEMSBS.findFailedPayrollRunsToProcess()", t);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected void updateStatusInFailedTable(HashSet<SpcfUniqueId> pSuccessfulPayrollRunIds, HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> pPayrollRunsToProcess) {
        HashSet<SpcfUniqueId> stillFailedPayrollRunIds = new HashSet<SpcfUniqueId>();
        for (ArrayList<SpcfUniqueId> e : pPayrollRunsToProcess.values()) {
            stillFailedPayrollRunIds.addAll(e);
        }
        stillFailedPayrollRunIds.removeAll(pSuccessfulPayrollRunIds);

        try {
            Application.beginUnitOfWork();
            FailedPayrollRun.updateStatusCode(pSuccessfulPayrollRunIds, SyncStatus.Complete);
            FailedPayrollRun.updateStatusCode(stillFailedPayrollRunIds, SyncStatus.Error);
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("failed to update status in the failed payroll run table", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected HashSet<SpcfUniqueId> multithreadProcessing(HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> pCompanyAndPayRunIds, boolean needSaveFailuresToDB) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        HashSet<SpcfUniqueId> results = new HashSet<SpcfUniqueId>();
	    try {
	         threadPool = Executors.newFixedThreadPool(threadCount);
	        CompletionService<HashSet<SpcfUniqueId>> completionService = new ExecutorCompletionService<HashSet<SpcfUniqueId>>(threadPool);
	
	        for (SpcfUniqueId companyId : pCompanyAndPayRunIds.keySet()) {
	            completionService.submit(new CoreProcessor(pCompanyAndPayRunIds.get(companyId), needSaveFailuresToDB));
	        }
	
	        
	        for (int i = 0; i < pCompanyAndPayRunIds.size(); i++) {
	            try {
	                Future<HashSet<SpcfUniqueId>> f = completionService.take();
	                results.addAll(f.get());
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	        }
		} finally {
			if (threadPool != null) {
				ThreadingUtils.shutdownAndAwaitTermination(threadPool);
			}

		}

        return results;
    }

    protected class CoreProcessor implements Callable<HashSet<SpcfUniqueId>> {
        private ArrayList<SpcfUniqueId> mPayrollRunIds;
        private boolean mNeedSaveFailuresToDB;
        private PSPRequestContextManager pspRequestContextManager;

        CoreProcessor(ArrayList<SpcfUniqueId> pPayrollRunIds, boolean pNeedSaveFailuresToDB) {
            mPayrollRunIds = pPayrollRunIds;
            mNeedSaveFailuresToDB = pNeedSaveFailuresToDB;
            pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        }

        private boolean processSinglePayrollRun(SpcfUniqueId pPayrollRunId) {
            try {
                Application.beginUnitOfWork();
                PayrollRun payrollRun = Application.findById(PayrollRun.class, pPayrollRunId);
                Company company = payrollRun.getCompany();
                pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "PSPToEMSBSDataSyncProcessor");

                DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, Paycheck.PayrollRun().Id().equalTo(pPayrollRunId));

                for (Paycheck paycheck : paychecks) {

                    if(!company.getSourceSystemCd().equals(SourceSystemCode.QBDT)) {
                        continue;
                    }

                    EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
                    if (primaryEntitlementUnit == null) {
                        throw new RuntimeException("No primary entitlement found for company " + company.getSourceSystemCompanyId());
                    }

                    if(!primaryEntitlementUnit.getEntitlement().getEntitlementCode().getIsUsageBilling()) {
                        continue;
                    }

                    // construct DTO's
                    CompanyDTO companyDTO = new CompanyDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), primaryEntitlementUnit.getEntitlement().getEntitlementOfferingCode(), primaryEntitlementUnit.getEntitlement().getLicenseNumber(), primaryEntitlementUnit.getEntitlement().getBillingDayOfMonth(), 1);
                    Employee employee = paycheck.getSourceEmployee();
                    if (employee == null) {
                        employee = paycheck.getDDEmployee();
                    }
                    QbdtPaycheckInfo qbdtPaycheckInfo = paycheck.getQbdtPaycheckInfo();
                    if (qbdtPaycheckInfo == null) {
                        throw new RuntimeException("No qbdtPaycheckInfo found for paycheck " + paycheck.getSourcePaycheckId());
                    }

                    String employeeListId;
                    QbdtEmployeeInfo qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();
                    if (qbdtEmployeeInfo == null) {
                        employeeListId = employee.getSourceEmployeeId();
                    } else {
                        employeeListId = qbdtEmployeeInfo.getListId();
                    }

                    EmployeeDTO employeeDTO = new EmployeeDTO(employee.getFullName(), employeeListId, "1");

                    ReasonForFreeChargeCode reasonForFreeCharge = ReasonForFreeChargeCode.None;
                    String symphonyOnBoardVer = "";
                    if (company.getQuickbooksInfo() != null && company.getQuickbooksInfo().getSymphonyOnBoardVersion() != null) {
                        symphonyOnBoardVer = company.getQuickbooksInfo().getSymphonyOnBoardVersion();
                    }
                    if (symphonyOnBoardVer.startsWith(OFXAPPVERObject.VENTI_MAJOR)) {
                        reasonForFreeCharge = ReasonForFreeChargeCode.Upgrade;
                    }
                    // Retail customers do not have a trial period. Do not check for the subscription date for them
                    if (!primaryEntitlementUnit.getEntitlement().getRetail()
                            && primaryEntitlementUnit.getEntitlement().getSubscriptionStartDate() != null
                            && payrollRun.getPaycheckDate().before(primaryEntitlementUnit.getEntitlement().getSubscriptionStartDate())) {
                        if (reasonForFreeCharge == ReasonForFreeChargeCode.None) {
                            reasonForFreeCharge = ReasonForFreeChargeCode.Trial;
                        } else {
                            reasonForFreeCharge = ReasonForFreeChargeCode.TrialUpgrade;
                        }
                    }

                    boolean paycheckCreatedDateLessThanBillingStartDate = paycheck.isPaycheckCreatedDateLessThanBillingStartDate();


                    PaycheckDTO paycheckDTO = new PaycheckDTO(qbdtPaycheckInfo.getListId(), payrollRun.getPaycheckDate(),
                                                              qbdtPaycheckInfo.getCheckNumber(), "", paycheck.getStatus() == PaycheckStatusCode.Active, reasonForFreeCharge, paycheckCreatedDateLessThanBillingStartDate);

                    // start sync
                    CreateBillingUsage createBillingUsageProcess = new CreateBillingUsage(companyDTO, employeeDTO, paycheckDTO);
                    ProcessResult processResult = createBillingUsageProcess.execute();
                    if (!processResult.isSuccess()) {
                        throw new RuntimeException("CreateBillingUsage process failed on paycheck " + paycheck.getId() + "\n" + processResult.toString());
                    }
                }

                Application.commitUnitOfWork();
                return true;
            } catch (Throwable t) {
                logger.info("Could not sync PSP to EMSBS for payroll ID: " + pPayrollRunId, t);
                return false;
            } finally {
                Application.rollbackUnitOfWork();
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        private void saveFailuresToDB(SpcfUniqueId pPayrollRunId) throws Exception {
            try {
                Application.beginUnitOfWork();
                FailedPayrollRun.createFailures(pPayrollRunId);
                Application.commitUnitOfWork();
            } catch (Throwable t) {
                // when this happens, the rest of the tasks will be cancelled and the whole batch will be considered failed
                throw new Exception("failed to save usage failures to DB" + t.getMessage());
            } finally {
                Application.rollbackUnitOfWork();
            }
        }

        // return all the processed payrollruns, including those moved to the failed table
        // these payrollruns may need a status update in the original source table such as PayrollRun and FailedPayrollRun
        public HashSet<SpcfUniqueId> call() throws Exception {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.PSPToEMSBSDataSyncBatchJob));

            HashSet<SpcfUniqueId> results = new HashSet<SpcfUniqueId>();
            for (SpcfUniqueId payrollRunId : mPayrollRunIds) {
                boolean resultSuccess = processSinglePayrollRun(payrollRunId);

                if (resultSuccess) {
                    results.add(payrollRunId);
                } else if (mNeedSaveFailuresToDB) {
                    saveFailuresToDB(payrollRunId);
                    results.add(payrollRunId);
                }

            }
            return results;
        }
    }
}
