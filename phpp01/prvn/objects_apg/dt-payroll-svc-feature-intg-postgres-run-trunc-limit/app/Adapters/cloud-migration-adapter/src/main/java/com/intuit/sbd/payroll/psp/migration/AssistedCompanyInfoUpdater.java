package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * User: ihannur
 * Date: 1/22/13
 * Time: 3:56 PM
 */
public class AssistedCompanyInfoUpdater {

    private static SpcfLogger logger = PayrollServices.getLogger(AssistedCompanyInfoUpdater.class);

    private static boolean debug = false;

    private static ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>();
    private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
    private static ThreadLocal<PreparedStatement> companyInfoPreparedStatement = new ThreadLocal<PreparedStatement>();

    public static void main(String[] args) {

        List<String> sourceCompanyIds = new ArrayList<String>();
        String[] psids = new String[]{};
        String minPsid = null;
        String maxPsid = null;
        String psidListFileName = null;
        final Vector<String> psIdsFailedToUpdate = new Vector<String>();

        int processors = Runtime.getRuntime().availableProcessors();
        int recommended = processors * 2;
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
            } else if (arg.equals("-min")) {
                if (i + 1 < args.length) {
                    minPsid = args[i + 1];
                }
            } else if (arg.equals("-max")) {
                if (i + 1 < args.length) {
                    maxPsid = args[i + 1];
                }
            } else if (arg.equals("-psidfile")) {
                if (i + 1 < args.length) {
                    psidListFileName = args[i + 1];
                    try {
                        InputStreamReader fileReader=null;
                        File file=new File(psidListFileName);
                        if(StreamUtil.isFileIDPSEncrypted(psidListFileName)){
                            Key key = IDPSFileStreamManager.newKeyHandleLatest();
                            fileReader =  new IDPSFileReader( file, key);
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

        System.out.println("Usage with arguments: AssistedCompanyInfoUpdater -debug true -psids [CommaSeparatedPsIds] -threads [NumberOfThreads] -min [minPsid] -max [maxPsid] -psidfile [fileNameWithPsids]");
        System.out.println(String.format("Running with options: -debug %s -psids %s -thread %s -min %s -max %s -psidfile %s", debug, Arrays.asList(psids), threadCount, minPsid, maxPsid, psidListFileName));

        ExecutorService executor;
        try {
            StopWatch sw = new StopWatch().start();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            List<String> params = new ArrayList<String>();
            List<String> paramValues = new ArrayList<String>();

            if (sourceCompanyIds.isEmpty()) {
                logger.info("Gathering PSP companies to update...");
                StringBuilder hql = new StringBuilder("Select comp.SourceCompanyId " +
                                                              "             from com.intuit.sbd.payroll.psp.domain.TaxCompanyServiceInfo as tcs, " +
                                                              "                  com.intuit.sbd.payroll.psp.domain.Company as comp " +
                                                              "             where comp.SourceSystemCd = 'QBDT'" +
                                                              "               and tcs.Company=comp ");
                if (minPsid != null) {
                    hql = hql.append(" and comp.SourceCompanyId >= :minPsid ");
                    params.add("minPsid");
                    paramValues.add(minPsid);
                }
                if (maxPsid != null) {
                    hql = hql.append(" and comp.SourceCompanyId <= :maxPsid ");
                    params.add("maxPsid");
                    paramValues.add(maxPsid);
                }
                hql = hql.append(" order by comp.SourceCompanyId ");
                sourceCompanyIds = Application.executeHQLQuery(hql.toString(), params.toArray(new String[params.size()]), paramValues.toArray(new String[paramValues.size()]));

                if (debug) {
                    logger.info("PSIds to process (" + sourceCompanyIds.size() + ") fetched from DB in " + sw.getElapsedTimeString());
                }
            }
            PayrollServices.rollbackUnitOfWork();

            logger.info(String.format("%d PSIds to process", sourceCompanyIds.size()));
            logger.info("creating thread pool with size: " + threadCount + "\t recommended: " + recommended + " for " + processors + " processors.");

            executor = Executors.newFixedThreadPool(threadCount);

            logger.info("updating company Company Info and filing flags from AS400 for all the selected PSIds");

            CompletionService<ProcessResult<String>> completionService = new ExecutorCompletionService<ProcessResult<String>>(executor);

            for (final String companyId : sourceCompanyIds) {
                completionService.submit(new Callable<ProcessResult<String>>() {
                    public ProcessResult<String> call() throws Exception {
                        return processCompany(companyId, psIdsFailedToUpdate);
                    }
                });
            }

            boolean showUpdated = (System.getProperty("showUpdated") != null);
            try {
                int success = 0;
                int failed = 0;

                int totalCompaniesToUpdate = sourceCompanyIds.size();
                for (int t = 0; t < totalCompaniesToUpdate; t++) {
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
                        logger.info(result.toString());
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

                logger.info(String.format(intSpec, "totalCompaniesToUpdate:", totalCompaniesToUpdate));
                logger.info(String.format(strSpec, "elapsed time:", sw.getElapsedTimeString()));
                logger.info(String.format(strSpec, "psIdsFailedToUpdate count:", psIdsFailedToUpdate.size()));
                logger.info(String.format(strSpec, "psIdsFailedToUpdate:", psIdsFailedToUpdate));
                logger.info(String.format(intSpec, "success:", success));
                logger.info(String.format(intSpec, "failed:", failed));

                ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            }
        } catch (Throwable t) {
            logger.error("Encountered unrecoverable error during processing", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static ProcessResult<String> processCompany(String companyId, Vector<String> pPsIdsFailedToUpdate) {
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

            if (debug) {
                logger.info(company);
            }

            if (result.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }

        } catch (Throwable t) {
            pPsIdsFailedToUpdate.add(companyId);
            result.getMessages().GenericError(EntityName.Company, companyId, "Error processing this company :" + t.toString());
            logger.error(t);
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return result;
    }


    private static SimpleDateFormat getSimpleDateFormat() {
        if (simpleDateFormat.get() == null) {
            simpleDateFormat.set(new SimpleDateFormat("yyyyMMdd"));
        }
        return simpleDateFormat.get();
    }
}
