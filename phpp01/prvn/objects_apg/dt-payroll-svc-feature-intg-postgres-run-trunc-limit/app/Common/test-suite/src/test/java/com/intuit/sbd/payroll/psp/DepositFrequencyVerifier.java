package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateFrequency;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Query;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: rnorian
 * Date: 8/25/11
 * Time: 2:43 PM
 */
public class DepositFrequencyVerifier {

    private static Connection conn;
    private static ThreadLocal<PreparedStatement> federalDepositFrequencyPreparedStatement = new ThreadLocal<PreparedStatement>();
    private static ThreadLocal<PreparedStatement> stateDepositFrequencyPreparedStatement = new ThreadLocal<PreparedStatement>();

    // error queues
    private static List<String> depositFrequencyRuntimeExceptions = Collections.synchronizedList(new ArrayList<String>());
    private static List<String> as400DepositFrequencyNotFound = Collections.synchronizedList(new ArrayList<String>());
    private static List<String> pspDepositFrequencyNotFound = Collections.synchronizedList(new ArrayList<String>());
    private static Map<String, List<String>> depositFrequenciesMismatchMap = new ConcurrentHashMap<String, List<String>>();
    private static Map<String, List<String>> templateSqlMap = new ConcurrentHashMap<String, List<String>>();
    private static DomainEntitySet<PaymentTemplate> paymentTemplates;

    private static synchronized List<String> getMismatchList(String pPaymentTemplateId) {
        List<String> mismatchList = depositFrequenciesMismatchMap.get(pPaymentTemplateId);
        if(mismatchList == null) {
            mismatchList = Collections.synchronizedList(new ArrayList<String>());
            depositFrequenciesMismatchMap.put(pPaymentTemplateId, mismatchList);
        }
        return mismatchList;
    }

    private static synchronized List<String> getSqlList(String pPaymentTemplateId) {
        List<String> mismatchList = templateSqlMap.get(pPaymentTemplateId);
        if(mismatchList == null) {
            mismatchList = Collections.synchronizedList(new ArrayList<String>());
            templateSqlMap.put(pPaymentTemplateId, mismatchList);
        }
        return mismatchList;
    }

    private static List<String> filerTypeRuntimeExceptions = Collections.synchronizedList(new ArrayList<String>());
    private static List<String> filerTypeMismatchPps941 = Collections.synchronizedList(new ArrayList<String>());
    private static List<String> filerTypeMismatchPps944 = Collections.synchronizedList(new ArrayList<String>());

    private static PrintWriter logFile;

    private static final ArrayList<String> FOLLOWS_FED_PAYMENT_TEMPLATES = new ArrayList<String>(Arrays.asList(
            "CA-PITSDI-PAYMENT",
            "MN-MW1-PAYMENT",
            "SC-WH1601-PAYMENT",
            "OR-OTCWH-PAYMENT",
            "AZ-A1-PAYMENT"
    ));

    private static DomainEntitySet<PaymentTemplateFrequency> paymentTemplateFrequencies;

    // -DtestMigrationIP=172.17.219.22 -DtestMigrationUser=PWSAPP -DtestMigrationPassword=PWSAPP
    /*
    <entry key="dataAccess.connection.username">pspread</entry>
    <entry key="dataAccess.connection.password">pspread01</entry>
    <entry key="dataAccess.connection.url">jdbc:oracle:thin://@localhost:9902:pspprod1</entry>
     */
    public static void main(String[] args) throws Exception {
        try {

            try {
                PayrollServices.beginUnitOfWork();
                paymentTemplates = Application.findObjects(PaymentTemplate.class);
                paymentTemplateFrequencies = Application.find(PaymentTemplateFrequency.class, new Query<PaymentTemplateFrequency>().EagerLoad(PaymentTemplateFrequency.PaymentTemplate()));
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            // load the file
            List<String> taxCompanyPSIDs = new ArrayList<String>();
            try {
                File f = new File(System.getProperty("user.dir") + File.separatorChar + "PSIDs.txt");
                FileReader fileReader = new FileReader(f);
                BufferedReader input =  new BufferedReader(fileReader);

                String line;
                while (( line = input.readLine()) != null){
                    String psid = line.trim();
                    if(psid != null && psid.length() == 9) {
                        taxCompanyPSIDs.add(line.trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            logln("loaded " + taxCompanyPSIDs.size() + " tax companies");


            // for each company, query AS400 for filer type and deposit frequency
            Executor executor = Executors.newFixedThreadPool(8);
            CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executor);

            StopWatch sw = StopWatch.startTimer();
            for (final String taxCompanyPSID : taxCompanyPSIDs) {
                completionService.submit(new Callable<Void>() {
                    public Void call() throws Exception {
                            verifySyncInfo(taxCompanyPSID);
                            return null;
                        }
                    });
            }

            for (int i = 1; i < taxCompanyPSIDs.size() + 1; i++) {
                try {
                    Future<Void> f = completionService.take();
                    f.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (i % 50 == 0) {
                    logln("processed count: " + i);
                    logln("\telapsed: " + sw);
                    for (String paymentTemplate : depositFrequenciesMismatchMap.keySet()) {
                        logln("\tdep freq mismatches for " + paymentTemplate + ": " + depositFrequenciesMismatchMap.get(paymentTemplate).size());
                    }
                    logln("\truntime freq errors: " + depositFrequencyRuntimeExceptions.size());
                    logln("\tas400DepositFreqNotFound: " + as400DepositFrequencyNotFound.size());
                    logln("\tpspDepositFreqNotFound: " + pspDepositFrequencyNotFound.size());
                    //log("\truntime filer errors: " + filerTypeRuntimeExceptions.size());
                    //log("\tfiler type mismatch (PSP:941): " + filerTypeMismatchPps941.size());
                    //log("\tfiler type mismatch (PSP:940): " + filerTypeMismatchPps944.size());
                    logln();
                }

                if (i % 500 == 0) {
                    printSummaryReport(i, sw);
                }
            }

            printSummaryReport(taxCompanyPSIDs.size(), sw);

            System.exit(0);
        } finally {            
            if(conn != null) {
                conn.close();
            }            
        }
    }

    public static void logln() {
        logln("");
    }

    public static void logln(String pMessage) {
        logFile.println(pMessage);
        logFile.flush();
        System.out.println(pMessage);
    }

    public static void log(String pMessage) {
        logFile.print(pMessage);
        logFile.flush();
        System.out.print(pMessage);
    }

    private static void printSummaryReport(int processed, StopWatch sw) {
        // report
        logln("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        logln("                     SUMMARY REPORT");
        logln("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        logln("processed count: " + processed);
        logln("\telapsed: " + sw);
        for (String paymentTemplateId : depositFrequenciesMismatchMap.keySet()) {
            logln("\tdep freq mismatch " + paymentTemplateId + ": " + depositFrequenciesMismatchMap.get(paymentTemplateId).size());
        }
        logln("\truntime freq errors: " + depositFrequencyRuntimeExceptions.size());
        logln("\tas400DepositFreqNotFound: " + as400DepositFrequencyNotFound.size());
        logln("\tpspDepositFreqNotFound: " + pspDepositFrequencyNotFound.size());
        //logln("\truntime filer errors: " + filerTypeRuntimeExceptions.size());
        //logln("\tfiler type mismatch (PSP:941): " + filerTypeMismatchPps941.size());
        //logln("\tfiler type mismatch (PSP:944): " + filerTypeMismatchPps944.size());
        logln();
        logln();
        for (String paymentTemplateId : depositFrequenciesMismatchMap.keySet()) {
            printSummary("dep freq mismatch " + paymentTemplateId, depositFrequenciesMismatchMap.get(paymentTemplateId));
        }
        for (String paymentTemplateId : templateSqlMap.keySet()) {
            printSummary("sql for dep freq mismatch " + paymentTemplateId, templateSqlMap.get(paymentTemplateId));
        }
        printSummary("runtime freq errors", depositFrequencyRuntimeExceptions);
        printSummary("as400DepositFreqNotFound", as400DepositFrequencyNotFound);
        printSummary("pspDepositFreqNotFound", pspDepositFrequencyNotFound);
        //printSummary("runtime filer errors", filerTypeRuntimeExceptions);
        //printSummary("filer type mismatch (PSP:941)", filerTypeMismatchPps941);
        //printSummary("filer type mismatch (PSP:944)", filerTypeMismatchPps944);
    }

    private static void printSummary(String title, List<String> messages) {
        if (messages.size() == 0)
            return;

        logln("--------------[ " + title + " ]---------------");
        for (String message : messages) {
            logln(message);
        }
        logln();
        logln();
    }

    private static void verifySyncInfo(final String pPSID) {
        try {
            PayrollServices.beginUnitOfWork();
            final Company taxCompany = Company.findCompany(pPSID, SourceSystemCode.QBDT);
            if (taxCompany == null) {
                System.err.println("company not found for psid: " + pPSID);
                return;
            }

            String query = " select distinct pt " +
                " from  com.intuit.sbd.payroll.psp.domain.EffectiveDepositFrequency efd," +
                "       com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate capt, " +
                "       com.intuit.sbd.payroll.psp.domain.CompanyAgency ca, " +
                "       com.intuit.sbd.payroll.psp.domain.PaymentTemplate pt" +
                " where ca.Company = :company" +
                "   and efd.CompanyAgencyPaymentTemplate = capt" +
                "   and capt.CompanyAgency = ca" +
                "   and capt.PaymentTemplate = pt" +
                "   and pt.SupportStartDate is not null" +
                "   and pt.PaymentTemplateCd not in ('IRS-941-PAYMENT', 'IRS-940-PAYMENT')";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(query);
            hibernateQuery.setReadOnly(true);
            hibernateQuery.setParameter("company", taxCompany);

        }
        catch (Throwable e) {
            StringWriter sw = new StringWriter(1024);
            e.printStackTrace(new PrintWriter(sw));
            depositFrequencyRuntimeExceptions.add(sw.toString());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        /*
        if (mFilerTypePS.get() == null) {
            mFilerTypePS.set(conn.prepareStatement(AS400GatewaySQLStatements.COMPANY_AGENCY_FILERTYPE));
        }
        try {
            verifyFedFilerType(pTaxCompany);
        }
        catch (Throwable e) {
            StringWriter sw = new StringWriter(1024);
            e.printStackTrace(new PrintWriter(sw));
            filerTypeRuntimeExceptions.add(sw.toString());
            throw e;
        }
        */
    }
}
