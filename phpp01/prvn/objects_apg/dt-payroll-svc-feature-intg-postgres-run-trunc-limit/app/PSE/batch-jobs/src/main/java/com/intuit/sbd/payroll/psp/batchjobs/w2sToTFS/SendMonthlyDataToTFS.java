package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;

import com.intuit.ems.tfs.messages.v1.FilingTypeType;
import com.intuit.ems.tfs.messages.v1.PayrollFormInfo;
import com.intuit.ems.tfs.messages.v1.SubmitFilingRequest;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.hibernate.FlushMode;

import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static com.intuit.ems.payroll.psp.gateways.tfs.util.HTTPHelper.executePost;


/**
 * User: mvillani
 * Date: 8/27/12
 * Time: 10:40 AM
 */
public class SendMonthlyDataToTFS {
    private static SpcfLogger logger = Application.getLogger(SendMonthlyDataToTFS.class);


    private static String mCompanyId = null;
    private static int mYear = 0;
    private static int mMonth = 0;
    private static boolean mForceError = false;

    private static final String YEAR_COMMAND = "-year";
    private static final String MONTH_COMMAND = "-month";
    private static final String SINGLE_COMPANYID_COMMAND = "-companyId";
    private static final String FORCE_ERROR = "-forceError";

    protected static final String LAW_97 = "97";


    public static void main(String args[]) {
        try {
            process(args);
        } catch (Throwable t) {
            logger.error("failed to process monthly data transfer to TFS", t);
        }
    }

    public static void process(String args[]) {
        try {
            StopWatch sw = new StopWatch().start();

            parseArgs(args);
            Application.beginUnitOfWork();
            ArrayList<SpcfUniqueId> companiesToProcess = new ArrayList<SpcfUniqueId>();
            // Check SYSTEM_PARAMETER to verify  if there is a company list to process, otherwise check the
            // command line parameter
            String companyListParameter = SystemParameter.findStringValue(SystemParameter.Code.TFS_MONTHLY_TRANSFER_COMPANY_LIST);
            // String companyListParameter = SystemParameter.findStringValue(SystemParameter.Code.W2_COMPANY_LIST);

            if (companyListParameter != null) {
                ArrayList<String> companyList = new ArrayList<String>(Arrays.asList(companyListParameter.split(",")));
                for (String companyPSID : companyList) {
                    Company company = Company.findCompany(companyPSID, SourceSystemCode.QBDT);
                    if (company != null) {
                        companiesToProcess.add(company.getId());
                    }
                }
            } else {
                if (mCompanyId != null) {
                    Company company = Company.findCompany(mCompanyId, SourceSystemCode.QBDT);
                    if (company == null) {
                        throw new RuntimeException("Invalid company " + mCompanyId);
                    }
                    companiesToProcess.add(company.getId());
                }
            }

            Application.commitUnitOfWork();

            // If date not specified process for previous month

            if (mYear == 0 || mMonth == 0)      {
                SpcfCalendar processingMonth = PSPDate.getPSPTime();
                processingMonth.addMonths(-1);
                mYear = processingMonth.getYear();
                mMonth = processingMonth.getMonth();

            }
            ProcessResult processResult = submitMonthlyDataToTFS(mYear, mMonth, companiesToProcess);

            sw.stop();
            logger.info("completed processing " + "     duration: " + sw.getElapsedTimeString());

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
        }  finally {
            Application.rollbackUnitOfWork();
        }
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (!arg.equals("")) {
                String[] argParts = arg.split(":");
                if (argParts.length == 2) {
                    if (argParts[0].equals(YEAR_COMMAND)) {
                        mYear = Integer.parseInt(argParts[1]);
                    } else if (argParts[0].equals(MONTH_COMMAND)) {
                        mMonth = Integer.parseInt(argParts[1]);
                    } else if (argParts[0].equals(SINGLE_COMPANYID_COMMAND)) {
                        mCompanyId = argParts[1];
                    } else if (argParts[0].equals(FORCE_ERROR)) {
                        mForceError = Boolean.parseBoolean(argParts[1]);
                    } else {
                        throw new RuntimeException("Invalid command: " + argParts[0]);
                    }
                } else {
                    throw new RuntimeException("Invalid argument: " + arg);
                }
            }
        }
    }

    private static ProcessResult submitMonthlyDataToTFS(int pYear, int pMonth, ArrayList<SpcfUniqueId> pCompaniesToProcess) {

        ProcessResult processResult;

        try {
            // process
            Application.beginUnitOfWork(FlushMode.MANUAL);
            processResult = multithreadProcessing(pYear, pMonth, pCompaniesToProcess);
            Application.commitUnitOfWork();
            logger.info("Processed monthly data transfer to TFS.");

        } catch (Throwable t) {
            Application.rollbackUnitOfWork();
            logger.fatal("Exception in monthly data transfer to TFS.", t);
            throw new RuntimeException(t);
        }
        return processResult;
    }


    protected static ProcessResult multithreadProcessing(final int pYear, final int pMonth, ArrayList<SpcfUniqueId> pCompaniesToProcess) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        ProcessResult processResult = new ProcessResult();

        // Get the amounts for all companies
        final SpcfCalendar beginDate = SpcfCalendar.createInstance(pYear, pMonth, 1);
        final SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(beginDate);

        HashMap<SpcfUniqueId, List<Object[]>> companyMonthlyTotals = getEETotalsTaxAmounts(LAW_97, beginDate, endDate);
        if (pCompaniesToProcess.size() == 0) {
            pCompaniesToProcess = addAllLawCompanies(companyMonthlyTotals, LAW_97);// add all company seq for given law
        }
        try {
            threadPool = Executors.newFixedThreadPool(threadCount);
            CompletionService<ProcessResult> completionService = new ExecutorCompletionService<ProcessResult>(threadPool);

            // Execute each company in one thread
            int processedCompanies = 0;
            for (final SpcfUniqueId companyId : pCompaniesToProcess) {
                List<Object[]> companyTotals = companyMonthlyTotals.get(companyId);
                if (companyTotals == null) {
                    companyTotals = new ArrayList<Object[]>();
                }
                final List<Object[]> finalCompanyTotals = companyTotals;
                processedCompanies++;
                completionService.submit(new Callable<ProcessResult>() {
                    public ProcessResult call() {
                        return submitCompany(beginDate, endDate, companyId, finalCompanyTotals);
                    }
                });
            }

            // Wait for the results of each thread execution

            for (int i=0; i < processedCompanies; i++) {
                try {
                    Future<ProcessResult> f = completionService.take();
                    ProcessResult taskProcessResult = f.get();
                    processResult.merge(taskProcessResult);
                } catch (Throwable t) {
                    logger.error("Exception in monthly data transfer to TFS", t);
                }
            }
            logger.info("We have processed " + processedCompanies + " companies including the zero wages.");
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }

        return processResult;
    }

    protected static ProcessResult<HashMap<String, PayrollFormInfo>> submitCompany(SpcfCalendar pBeginDate, SpcfCalendar pEndDate, SpcfUniqueId pCompanyId, List<Object[]> pCompanyTotals) {
        ProcessResult<HashMap<String, PayrollFormInfo>> companyResult = new ProcessResult<HashMap<String, PayrollFormInfo>>();
        StopWatch sw = new StopWatch().start();
        try {
            Application.beginUnitOfWork(FlushMode.MANUAL);
            Company company = Application.findById(Company.class, pCompanyId);

            SubmitFilingRequest submitFilingRequestXML = createSubmitFilingRequest(company, pCompanyTotals, pBeginDate, pEndDate);
            submitFilingRequest(sw, submitFilingRequestXML);

            Application.commitUnitOfWork();
            return companyResult;

        } catch (Throwable t) {
                Application.rollbackUnitOfWork();
            companyResult.getMessages().ExceptionOccurred(t);
            logger.error("Could not submit monthly Data to TFS for company ID: " + pCompanyId, t);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return companyResult;
    }

    private static void submitFilingRequest(StopWatch sw, SubmitFilingRequest submitFilingRequestXML) {

        Client client = Client.create();
        WebResource webResource = client.resource(getAWSURL());
        logger.info("Packaging time:" + sw.getElapsedTimeString());
        sw.reset();
        sw.start();
        testResponse(executePost(webResource, MediaType.APPLICATION_XML, submitFilingRequestXML));
        logger.info("Call time:" + sw.getElapsedTimeString());
        
    }

    private static String getAWSURL() {
        String awshost = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awshost");
        String awspath = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awsfilingpath");
        return awshost+awspath;
    }

    protected static SubmitFilingRequest createSubmitFilingRequest(Company company, List<Object[]> pCompanyTotals, SpcfCalendar pBeginDate, SpcfCalendar pEndDate) {
        SubmitFilingRequest submitFilingRequest = new SubmitFilingRequest();
        // Todo_MV check what to use to initialize this
        submitFilingRequest.setFilingSubmissionID(SpcfUniqueId.createInstance().toString());

        PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
        builder = builder.buildCompany(company,mYear);
        builder = builder.buildDataSpace(FilingTypeType.ModifiableDailyData, BigInteger.valueOf(mYear));

        // processCompanyLawTotals(pCompanyId, builder);
        processEmployees(company, pCompanyTotals, builder, pBeginDate, pEndDate);

        submitFilingRequest.getPayrollFormInfo().add(builder.getPayrollFormInfo());

        return submitFilingRequest;
        
    }

    protected static HashMap<SpcfUniqueId, List<Object[]>> getEETotalsTaxAmounts(String pLawId, SpcfCalendar pFromDate, SpcfCalendar pEndDate) {

        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        Date endDate = CalendarUtils.convertLocalTimestamp(pEndDate.getTimeInMilliseconds());
        Timestamp endTimeStamp = new Timestamp(endDate.getTime());

        List<Object[]> monthlyEETotalsList = Application.executeNamedQuery(Application.getQueryName("calculateEmployeeMonthlyTotals"),
                                                                           new String[]{"fromDate", "toDate", "lawId", "excludeDeletedCompany"},
                                                                           new Object[]{statTimeStamp, endTimeStamp, pLawId, Company.isDGDeleteFeatureEnabled()});

        HashMap<SpcfUniqueId, List<Object[]>> companyMonthlyTotals = new HashMap<SpcfUniqueId, List<Object[]>>();
        for (Object[] monthlyEETotals : monthlyEETotalsList) {
            SpcfUniqueId companyId = (SpcfUniqueId) monthlyEETotals[5];
            if (!companyMonthlyTotals.containsKey(companyId)) {
                companyMonthlyTotals.put(companyId, new ArrayList<Object[]>());
            }
            companyMonthlyTotals.get(companyId).add(monthlyEETotals);
        }
        return companyMonthlyTotals;
    }

    private static void processEmployees(Company pCompany, List<Object[]> pCompanyTotals, PayrollFormInfoBuilder pBuilder, SpcfCalendar pBeginDate, SpcfCalendar pEndDate) {

        Law law = Application.findById(Law.class, LAW_97);

        EmployeeW2Totals erMonthlyTotals = new EmployeeW2Totals();
        erMonthlyTotals.setCompany(pCompany);
        erMonthlyTotals.setYear(pBeginDate.getYear());
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pCompany, law.getLawId());
        erMonthlyTotals.setCompanyLaw(companyLaw);
        erMonthlyTotals.setLaw(law);
        erMonthlyTotals.setAmount(SpcfMoney.ZERO);
        erMonthlyTotals.setTaxableWages(SpcfMoney.ZERO);
        erMonthlyTotals.setTipsTaxableWagesAmount(SpcfMoney.ZERO);
        erMonthlyTotals.setTotalWages(SpcfMoney.ZERO);

        for (Object[] companyTotals : pCompanyTotals) {
            Employee employee = Application.findById(Employee.class, (SpcfUniqueId) companyTotals[4]);
            if (!pBuilder.containsEmployee(employee)) {
                pBuilder.addEmployee(employee);
            }

            PayrollFormInfo.EmployeeInfo employeeInfo = pBuilder.getEmployeeInfo(employee);
            EmployeeW2Totals eeMonthlyTotals = new EmployeeW2Totals();

            eeMonthlyTotals.setCompany(pCompany);
            eeMonthlyTotals.setYear(pBeginDate.getYear());
            eeMonthlyTotals.setCompanyLaw(companyLaw);
            eeMonthlyTotals.setLaw(law);
            eeMonthlyTotals.setAmount((SpcfMoney) companyTotals[3]);
            eeMonthlyTotals.setTaxableWages((SpcfMoney) companyTotals[1]);
            eeMonthlyTotals.setTipsTaxableWagesAmount((SpcfMoney) companyTotals[2]);
            eeMonthlyTotals.setTotalWages((SpcfMoney) companyTotals[0]);

            // Accumulate Company Totals
            erMonthlyTotals.setAmount(new SpcfMoney(erMonthlyTotals.getAmount().add((SpcfMoney) companyTotals[3])));
            erMonthlyTotals.setTaxableWages(new SpcfMoney(erMonthlyTotals.getTaxableWages().add((SpcfMoney) companyTotals[1])));
            erMonthlyTotals.setTipsTaxableWagesAmount(new SpcfMoney(erMonthlyTotals.getTipsTaxableWagesAmount().add((SpcfMoney) companyTotals[2])));
            erMonthlyTotals.setTotalWages(new SpcfMoney(erMonthlyTotals.getTotalWages().add((SpcfMoney) companyTotals[0])));

            pBuilder.addEmployeeTaxItemTotals(employeeInfo, eeMonthlyTotals, pBeginDate, pEndDate, false);
        }

        PayrollFormInfo.CompanyInfo companyInfo = pBuilder.getCompanyInfo();
        // Tax Item Info
        pBuilder.addCompanyTaxItemInfo(companyInfo, erMonthlyTotals, pBeginDate, pEndDate);

        // Tax Item Totals
        pBuilder.addCompanyTaxItemTotals(companyInfo, erMonthlyTotals, pBeginDate, pEndDate, false);

    }

    private static void testResponse(ClientResponse pClientResponse) {
        if (mForceError) {
            throw new RuntimeException("Failed - forcing error for test purposes.");
        } else {
            if (pClientResponse.getStatus() != HttpURLConnection.HTTP_OK) {
                String output = pClientResponse.getEntity(String.class);
                System.out.println("Output from Server .... \n");
                System.out.println(output);
                throw new RuntimeException("Failed : " + pClientResponse.toString());
            }
        }
    }

    protected static void setYear(int pYear) {
        mYear = pYear;
    }

    /**
     * This method will give all distinct company seq for given law id
     * @param pLawId
     * @return
     */
    protected static Set<SpcfUniqueId> getTaxCompanySeqs(String pLawId) {

        List<SpcfUniqueId> companySeqs = Application.executeNamedQuery(Application.getQueryName("findCompanySeqForTaxCompanies"),
                                                                       new String[]{"lawId", "excludeDeletedCompany"},
                                                                       new Object[]{pLawId, Company.isDGDeleteFeatureEnabled()});

        Set<SpcfUniqueId> taxCompanySeqs = new HashSet<SpcfUniqueId>();

        if (companySeqs != null && companySeqs.size() > 0) {
            taxCompanySeqs.addAll(companySeqs);
        }
        return taxCompanySeqs;
    }


    /**
     * create the array for distinct company seq using existing company seq from a given map and new company seq set
     * @param pCompanyMonthlyTotals
     * @param pLaw
     * @return
     */
    protected static ArrayList<SpcfUniqueId> addAllLawCompanies(HashMap<SpcfUniqueId, List<Object[]>> pCompanyMonthlyTotals, String pLaw) {
        logger.info("Adding all companies for sending IL monthly data to TFS including the zero wage companies.");

        //get the all companies
        Set<SpcfUniqueId> taxCompanySeqs = getTaxCompanySeqs(pLaw);
        taxCompanySeqs.addAll(pCompanyMonthlyTotals.keySet());//it will not add any duplicates records and it will also add new records if any
        ArrayList<SpcfUniqueId> pCompaniesToProcess = new ArrayList<SpcfUniqueId>();//define new one and override old values with new values
        pCompaniesToProcess.addAll(taxCompanySeqs);

        logger.info("Number of companies with wages=" + pCompanyMonthlyTotals.size() + " and totalCompanies=" + pCompaniesToProcess.size() );
        return pCompaniesToProcess;

    }

}
