package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 16, 2011
 * Time: 10:58:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeePayrollItemsUpdater {

    private static SpcfLogger logger = PayrollServices.getLogger(EmployeePayrollItemsUpdater.class);

    private static boolean debug = false;

    public static void main(String[] args) {

        List<String> sourceCompanyIds = new ArrayList<String>();
        String[] psids = new String[]{};
        String minPsid = null;
        String maxPsid = null;
        String psidListFileName = null;
        final Vector<String> missingEmployeePSIds = new Vector<String>();

        int processors = Runtime.getRuntime().availableProcessors();
        int recommended = processors * 1 * (1 + 1 / 1);
        int threadCount = recommended;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-debug")) {
                debug = true;
            } else if (arg.equals("-psids")) {
                if (i + 1 < args.length) {
                    psids = args[i + 1].split(",");
                    sourceCompanyIds = Arrays.asList(psids);
                }
            } else if (arg.equals("-threads")) {
                if (i + 1 < args.length) {
                    try {
                        threadCount = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("could not read -threads argument, defaulting to " + threadCount);
                    }
                }
            } else if(arg.equals("-min")) {
                if(i + 1 < args.length) {
                    minPsid = args[i+1];
                }
            } else if(arg.equals("-max")) {
                if(i + 1 < args.length) {
                    maxPsid = args[i+1];
                }
            } else if(arg.equals("-psidfile")) {
                if(i + 1 < args.length) {
                    psidListFileName = args[i+1];
                    try {
                        InputStreamReader fileReader=null;
                        File file=new File(psidListFileName);
                        if(StreamUtil.isFileIDPSEncrypted(psidListFileName)){
                            Key key = IDPSFileStreamManager.newKeyHandleLatest();
                            fileReader = new IDPSFileReader( file, key);
                        }else{    
                        	fileReader = new FileReader(file);
                        }
                        
                        BufferedReader input = new BufferedReader(fileReader);
                        try {
                            String line;
                            while ((line = input.readLine()) != null) {
                                sourceCompanyIds.add(line);                                
                            }
                        } finally {
                            input.close();
                        }
                    } catch (Throwable t) {
                        System.err.println("could not read PSIds from file name provided with -psidfile argument");
                        throw new RuntimeException(t);
                    }
                }
            }
        }

        System.out.println("Usage with arguments: EmployeePayrollItemsUpdater -debug true -psids [CommaSeparatedPsIds] -threads [NumberOfThreads] -min [minPsid] -max [maxPsid] -psidfile [fileNameWithPsids]");
        System.out.println(String.format("Running with options: -debug %s -psids %s -thread %s -min %s -max %s -psidfile %s", debug, Arrays.asList(psids), threadCount, minPsid, maxPsid, psidListFileName));

        ExecutorService executor;
        try {
            StopWatch sw = new StopWatch().start();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            if(sourceCompanyIds.isEmpty()) {
                logger.info("gathering PSP companies to update...");
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                StringBuilder hql = new StringBuilder("Select comp.SourceCompanyId " +
                        "             from com.intuit.sbd.payroll.psp.domain.CompanyService as cs, " +
                        "                  com.intuit.sbd.payroll.psp.domain.Company as comp " +
                        "             where cs.Company.SourceSystemCd = 'QBDT'" +
                        "               and cs.Service.ServiceCd = 'Tax'" +
                        "               and cs.StatusCd not in ('Cancelled', 'Terminated')" +
                        "               and cs.Company=comp ");
                if(minPsid == null && maxPsid == null) {
                    hql= hql.append(" order by comp.SourceCompanyId ");
                    org.hibernate.Query query = Application.createHibernateQuery(hql.toString());
                    sourceCompanyIds = (List<String>) query.list();
                } else {
                    if(minPsid != null) {
                        hql= hql.append(" and comp.SourceCompanyId >= :minPsid ");
                    }
                    if(maxPsid != null) {
                        hql= hql.append(" and comp.SourceCompanyId <= :maxPsid ");
                    }
                    hql= hql.append(" order by comp.SourceCompanyId ");
                    org.hibernate.Query query = Application.createHibernateQuery(hql.toString());
                    if(minPsid != null) {
                        query.setParameter("minPsid", minPsid);
                    }
                    if(maxPsid != null) {
                        query.setParameter("maxPsid", maxPsid);
                    }
                    sourceCompanyIds = (List<String>) query.list();
                    if (debug) {
                        logger.info("PSIds to process (" + sourceCompanyIds.size() + ") fetched from DB in " + sw.getElapsedTimeString());
                    }                    
                }
                PayrollServices.rollbackUnitOfWork();
            }

            logger.info(String.format("%d PSIds to process", sourceCompanyIds.size()));
            logger.info("creating thread pool with size: " + threadCount + "\t recommended: " + recommended + " for " + processors + " processors.");
            
            executor = Executors.newFixedThreadPool(threadCount);

            logger.info("updating company employee data in PSP for all the selected PSIds");
            
            CompletionService<ProcessResult<String>> completionService = new ExecutorCompletionService<ProcessResult<String>>(executor);
            
            for (final String companyId : sourceCompanyIds) {
                completionService.submit(new Callable<ProcessResult<String>>() {
                    public ProcessResult<String> call() throws Exception {
                        boolean employeesMissing = false;
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));
                        ProcessResult<String> result = new ProcessResult<String>();
                        result.setResult(companyId);
                        try {
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
                            if (company == null) {
                                result.getMessages().CompanyDoesNotExist(EntityName.Company, companyId, SourceSystemCode.QBDT.name(), companyId);
                                return result;
                            }

                            if (debug) { logger.info(company); }

                            ProcessResult<Employee> empUpdateProcessResult = new ProcessResult<Employee>();

                            if(employeesMissing) {
                                missingEmployeePSIds.add(companyId);
                            }

                            if (empUpdateProcessResult.isSuccess()) {
                                PayrollServices.commitUnitOfWork();
                            } else {
                                result.merge(empUpdateProcessResult);
                            }
                            return result;
                        } catch (Throwable t) {
                            result.getMessages().GenericError(EntityName.Company, companyId, "Error processing this company :"+ t.toString());
                            logger.error(t);
                            return result;
                        } finally {
                            PayrollServices.rollbackUnitOfWork();
                        }
                    }
                });
            }

            boolean showUpdated = (System.getProperty("showUpdated") != null);
            try {
                int success = 0;
                int failed = 0;

                int totalCompaniesToUpdate = sourceCompanyIds.size();
                for (int t = 0, n = totalCompaniesToUpdate; t < n; t++) {
                    Future<ProcessResult<String>> f = completionService.take();
                    ProcessResult<String> result = f.get();
                    String companyId = result.getResult();

                    if (result.isSuccess()) {
                        if (showUpdated) {
                            logger.info("Updated: " + companyId);
                        }
                        success++;
                    } else {
                        logger.info("Error messages for companyId: " + companyId);
                        for (Message message : result.getErrorMessages()) {
                            logger.info(companyId + ": " + message.getMessage());
                        }
                        failed++;
                    }

                    if (t % 1000 == 0) {
                        logger.info("Completed " + t + " in " + sw.getElapsedTimeString());
                    }
                }
                sw.stop();
                logger.info("Completed in " + sw.getElapsedTimeString());

                logger.info("-----------------------[ results summary ]-----------------------");
                String strSpec = "%1$-25s %2$-15s \n";
                String intSpec = "%1$-25s %2$-15d \n";

                logger.info(String.format(intSpec, "totalCompaniesToUpdate", totalCompaniesToUpdate));
                logger.info(String.format(strSpec, "elapsed time", sw.getElapsedTimeString()));
                logger.info(String.format(intSpec, "missingEmplsCompanyCount", missingEmployeePSIds.size()));
                logger.info(String.format(intSpec, "success", success));
                logger.info(String.format(intSpec, "failed", failed));
                logger.info(String.format(strSpec, "missingEmplsCompanyPSIds", missingEmployeePSIds));
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

}
