package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedConnectionInformation;
import com.intuit.sbd.payroll.psp.adapters.qbdt.CredentialType;
import com.intuit.sbd.payroll.psp.adapters.qbdt.processors.PayrollTransactionProcessor;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.ResponsePayrollTransaction;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 10/8/12
 * Time: 3:57 PM
 */
public class AddMissingLiabilityAdjustments {
    protected static final SpcfLogger logger = Application.getLogger(AddMissingLiabilityAdjustments.class);

    private static Connection conn;

    private static ThreadLocal<PreparedStatement> payrollTxPreparedStatement = new ThreadLocal<PreparedStatement>();

    private static final String INPUT_FILE_NAME_COMMAND = "-inputFileName";
    private static final String COMMIT_COMMAND = "-commit";

    private static boolean mCommit = false;
    private static String mInputFileName = null;

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if(argParts.length == 2) {
                if(argParts[0].equals(INPUT_FILE_NAME_COMMAND)) {
                    mInputFileName = argParts[1];
                } else if(argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.parseBoolean(argParts[1]);
                } else {
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    //-DtestMigrationIP=172.17.219.22 -DtestMigrationUser=PWSAPP -DtestMigrationPassword=PWSAPP

    public static void main(String[] args) throws Exception {
        logger.info("Begin");
        parseArgs(args);

        if(mInputFileName == null) {
            logger.info("Usage: AddMissingLiabilityAdjustments -inputFileName=file name [-commit=true|false]");
        }

        Map<String, List<String>> inputMap = parseInputFile();
        logger.info("input file loaded");

        ExecutorService executor = null;

        try {

            Application.initialize();
            ApplicationSecondary.initialize();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

            int processors = Runtime.getRuntime().availableProcessors();
            int threadCount = processors * (2);
            executor = Executors.newFixedThreadPool(threadCount);

            CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executor);

            for (final String companyId : inputMap.keySet()) {
                final List<String> adjustmentIdList = inputMap.get(companyId);
                completionService.submit(new Callable<Void>() {
                    public Void call() throws Exception {
                        try {
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
                            company.setNextToken(company.getCurrentToken());
                            PayrollTransactionProcessor payrollTransactionProcessor = new PayrollTransactionProcessor(company, new AssistedConnectionInformation(), CredentialType.Pin);

                            ProcessResult processResult = new ProcessResult();
                            for (String adjustmentId : adjustmentIdList) {
                                PreparedStatement adjustmentStatement = getAdjustmentStatement();
                                adjustmentStatement.setString(1, company.getSourceCompanyId());
                                adjustmentStatement.setString(2, adjustmentId);
                                ResultSet resultSet = adjustmentStatement.executeQuery();

                                IPAYROLLTX ipayrolltx = null;
                                while (resultSet.next()) {
                                    if(ipayrolltx == null) {
                                        ipayrolltx = new IPAYROLLTX();
                                        ipayrolltx.setIACCTNAME(resultSet.getString("TAX_ACCTNAME"));
                                        String amountType = resultSet.getString("TAX_AMT_TYPE");
                                        if("C".equals(amountType)) {
                                            ipayrolltx.setIAMT("$" + resultSet.getDouble("TAX_AMT"));
                                        }
                                        ipayrolltx.setICLEARED(resultSet.getString("TAX_CLEARED"));
                                        ipayrolltx.setIDTPAYPDEND(resultSet.getLong("TAX_DTPAYPDEND") + "");
                                        ipayrolltx.setIDTTX(resultSet.getLong("TAX_DTTX") + "");
                                        Long employeeId = resultSet.getLong("TAX_EMPID");
                                        if(employeeId != null && employeeId > 0) {
                                            ipayrolltx.setIEMPID(employeeId + "");
                                        }
                                        ipayrolltx.setIEMPNAME(resultSet.getString("TAX_EMPNAME"));
                                        ipayrolltx.setIMEMO(resultSet.getString("TAX_MEMO"));
                                        ipayrolltx.setINAME(resultSet.getString("TAX_NAME"));
                                        ipayrolltx.setIONSERVICE(resultSet.getString("TAX_ONSERVICE"));
                                        ipayrolltx.setIPAYROLLTXID(resultSet.getLong("TAX_PAYROLLTXID") + "");
                                        ipayrolltx.setIPAYROLLTXTYPE(resultSet.getString("TAX_PAYROLLTXTYPE"));
                                        ipayrolltx.setIREFNUM(resultSet.getString("TAX_REFNUM"));
                                        ipayrolltx.setIVOID(resultSet.getString("TAX_VOID"));
                                    }
                                    ITXLINE itxline = new ITXLINE();
                                    itxline.setIACCTNAME(resultSet.getString("TAXI_ACCTNAME"));
                                    String amountType = resultSet.getString("TAXI_AMT_TYPE");
                                    if("C".equals(amountType)) {
                                        itxline.setIAMT("$" + resultSet.getDouble("TAXI_AMT"));
                                    }
                                    itxline.setICLASS(resultSet.getString("TAXI_CLASS"));
                                    itxline.setIISDD(resultSet.getString("TAXI_ISDD"));
                                    itxline.setIMEMO(resultSet.getString("TAXI_MEMO"));
                                    itxline.setIPITEMID(resultSet.getString("TAXI_PITEMID"));
                                    itxline.setITAXABLEWAGE(resultSet.getDouble("TAXI_TAXABLEWAGE") + "");
                                    itxline.setIWB(resultSet.getDouble("TAXI_WB") + "");
                                    ipayrolltx.getITXLINE().add(itxline);
                                }

                                if(ipayrolltx == null) {
                                    throw new RuntimeException("Could not find an adjustment with id: " + adjustmentId + " for company: " + companyId);
                                }

                                processResult.merge(payrollTransactionProcessor.updateLiabilityAdjustments(new ResponsePayrollTransaction(ipayrolltx)));
                                if(resultSet != null) {
                                    resultSet.close();
                                }
                            }

                            if(processResult.isSuccess()) {
                                if(mCommit) {
                                    PayrollServices.commitUnitOfWork();
                                }
                            } else {
                                // log error and stop processing
                                logger.error("Error processing company " + companyId + "\n" + processResult.toString());
                                return null;
                            }

                        } catch (Throwable t) {
                            logger.error("Error processing company " + companyId,  t);
                        } finally {
                            PayrollServices.rollbackUnitOfWork();
                        }

                        return null;
                    }
                });
            }

            int total = 0;
            //noinspection UnusedDeclaration
            for (String companyId : inputMap.keySet()) {
                completionService.take();
                total++;
                if (total % 100 == 0) {
                    logger.info("Completed processing " + total + " of " + inputMap.size() + " companies");
                }
            }

            logger.info("Completed processing all companies.");

            // try to close prepared statements (no guarantee that all of the threads will be used)
            for (int i = 0; i < threadCount; i++) {
                completionService.submit(new Callable<Void>() {
                    public Void call() throws Exception {
                        try {
                            PreparedStatement adjustmentStatement = getAdjustmentStatement();
                            if(adjustmentStatement != null) {
                                adjustmentStatement.close();
                            }
                        } catch (Throwable pThrowable) {
                            // ignore
                        }
                        return null;
                    }
                });
            }

            for (int i = 0; i < threadCount; i++) {
                completionService.take();
            }

            logger.info("\n\n\n\n\n\n\n******************************\nCompleted processing " + total + " of " + inputMap.size() + " companies");
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
            if(conn != null) {
                conn.close();
            }
        }

        System.exit(0);
    }

    private static PreparedStatement getAdjustmentStatement() throws Throwable {
        if(payrollTxPreparedStatement.get() == null) {
            payrollTxPreparedStatement.set(conn.prepareStatement(" select * "+
                                                                         " from iqpaytx paytx "+
                                                                         "   join iqpaytxitm paytxline on paytx.userid = paytxline.userid and paytx.tax_payrolltxid = paytxline.taxi_payrolltxid "+
                                                                         " where paytx.userid = ? "+
                                                                         " and paytx.tax_payrolltxid = ? " +
                                                                         " order by paytxline.taxi_recnum "));
        }
        return payrollTxPreparedStatement.get();
    }

    private static Map<String, List<String>> parseInputFile() throws Exception {
        Map<String, List<String>> companyAdjustmentIdMap = new HashMap<String, List<String>>();

        File f = new File(mInputFileName);
        InputStreamReader fileReader=null;
        if(StreamUtil.isFileIDPSEncrypted(mInputFileName)){
            Key key = IDPSFileStreamManager.newKeyHandleLatest();
            fileReader = new  IDPSFileReader( f, key);
        }else{    
        	fileReader = new FileReader(f);
        }
        BufferedReader input =  new BufferedReader(fileReader);

        String line;
        while (( line = input.readLine()) != null){
            String[] data = line.split(",");
            if(data.length == 2) {
                String companyId = data[0];
                String sourceId = data[1];
                if(!companyAdjustmentIdMap.containsKey(companyId)) {
                    companyAdjustmentIdMap.put(companyId, new ArrayList<String>());
                }
                companyAdjustmentIdMap.get(companyId).add(sourceId);
            } else {
                logger.error("Could not process line:" + line);
            }
        }

        logger.info("Found " + companyAdjustmentIdMap.size() + " companies");
        return companyAdjustmentIdMap;
    }
}
