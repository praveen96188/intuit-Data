package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.util.List;
import java.util.concurrent.*;

/**
 * User: rnorian
 * Date: May 18, 2010
 * Time: 3:19:59 PM
 */
public class CloudServiceUpdater {
    public static void main(String[] args) {

        ExecutorService executor = null;
        try {
            StopWatch sw = new StopWatch().start();

            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.CloudMigration));

            System.out.println("gathering PSP DirectDeposit companies to update...");
            PayrollServices.beginUnitOfWork();
            String[] paramNames = new String[]{"excludeDeletedCompany"};
            Object[] paramValues = new Object[]{!AuthUser.hasSAPAdminAccess()};
            List<String> sourceCompanyIds = Application.executeNamedQuery("findAllQBDTCompanyIdsToAddCloudService", paramNames, paramValues);
            PayrollServices.rollbackUnitOfWork();

            System.out.println("fetched " + sourceCompanyIds.size() + " companies in " + sw.getElapsedTimeString());

            int processors = Runtime.getRuntime().availableProcessors();
            int recommended = processors * 1 * (1 + 1/1);
            int threadCount = recommended;
            if (args.length > 0) {
                threadCount = Integer.parseInt(args[0]);
            }
            System.out.println("creating thread pool with size: " + threadCount + "\t recommended: " + recommended + " for " + processors + " processors.");
            executor = Executors.newFixedThreadPool(threadCount);

            String startWithCompanyId = null;
            int removedFromProcessing = 0;
            if (args.length > 1) {
                startWithCompanyId = args[1];
            }

            final boolean warnOnSkip = (System.getProperty("warnOnSkip") != null);
            System.out.println("updating company data in PSP...");
            CompletionService<ProcessResult<String>> completionService = new ExecutorCompletionService<ProcessResult<String>>(executor);
            for (final String companyId : sourceCompanyIds) {

                if (startWithCompanyId != null && !companyId.equalsIgnoreCase(startWithCompanyId)) {
                    removedFromProcessing++;
                    continue;
                }

                startWithCompanyId = null;

                completionService.submit(new Callable<ProcessResult<String>>() {
                    public ProcessResult<String> call() throws Exception {
                        ProcessResult<String> result = new ProcessResult<String>();
                        result.setResult(companyId);
                        try {

                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.CloudMigration));
                            PayrollServices.beginUnitOfWork();
                            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
                            if (company == null) {
                                result.getMessages().CompanyDoesNotExist(EntityName.Company, companyId, SourceSystemCode.QBDT.name(), companyId);
                                return result;
                            }

                            if (company.hasService(ServiceCode.Cloud)) {
                                if (warnOnSkip) {
                                    result.getMessages().GenericError(EntityName.Company, companyId, "skipped: already on cloud service");
                                }
                                return result;
                            }

                            // don't process assisted/check-distribution companies or canceled/terminated DD companies
                            if (!company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                                result.getMessages().GenericError(EntityName.Company, companyId, "company is not active on DirectDeposit");
                                return result;
                            }

                            ProcessResult companyResult = addCloudService(companyId);
                            if (companyResult.isSuccess()) {
                                PayrollServices.commitUnitOfWork();
                            } else {
                                result.merge(companyResult);
                            }
                            return result;

                        } finally {
                            PayrollServices.rollbackUnitOfWork();
                        }
                    }
                });
            }

            boolean showUpdated = (System.getProperty("showUpdated") != null);
            try {
                int success=0;
                int failed=0;
                int skipped=0;

                int totalCompaniesToUpdate = sourceCompanyIds.size() - removedFromProcessing;
                for (int t = 0, n = totalCompaniesToUpdate; t < n; t++) {
                    Future<ProcessResult<String>> f = completionService.take();
                    ProcessResult<String> result = f.get();
                    String companyId = result.getResult();

                    if (result.isSuccess()) {
                        if (showUpdated) {
                            System.out.println("updated: " + companyId);
                        }
                        success++;
                    } else {
                        System.out.println("error messages for companyId: " + companyId);
                        for (Message message : result.getErrorMessages()) {
                            System.out.println(companyId + ": " + message.getMessage());
                            if (message.getMessage().indexOf("skipped") != -1) {
                                skipped++;
                            } else {
                                failed++;
                            }
                        }
                        System.out.println();
                    }

                    if (t % 1000 == 0) {
                        System.out.println("completed " + t + " in " + sw.getElapsedTimeString());
                    }
                }
                sw.stop();
                System.out.println(" completed in " + sw.getElapsedTimeString());

                System.out.println("-----------------------[ results summary ]-----------------------");
                String strSpec = "%1$-18s %2$-15s \n";
                String intSpec = "%1$-18s %2$-15d \n";

                System.out.printf(intSpec, "totalCompaniesToUpdate", totalCompaniesToUpdate);
                System.out.printf(strSpec, "elapsed time", sw.getElapsedTimeString());
                System.out.printf(intSpec, "success", success);
                System.out.printf(intSpec, "skipped", skipped);
                System.out.printf(intSpec, "failed", failed);
                System.out.println();
                ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ee) {
                ee.printStackTrace(System.err);
                throw ee;
            }
        } catch (Throwable t) {
            System.err.println("Encountered unrecoverable error during processing");
            t.printStackTrace(System.err);
        }
    }

    public static ProcessResult addCloudService(String companyId) {

        ServiceInfoDTO cloudServiceInfoDTO = new ServiceInfoDTO();
        cloudServiceInfoDTO.setServiceCode(ServiceCode.Cloud);

        return PayrollServices.companyManager.addService(SourceSystemCode.QBDT, companyId, cloudServiceInfoDTO);
    }
}
