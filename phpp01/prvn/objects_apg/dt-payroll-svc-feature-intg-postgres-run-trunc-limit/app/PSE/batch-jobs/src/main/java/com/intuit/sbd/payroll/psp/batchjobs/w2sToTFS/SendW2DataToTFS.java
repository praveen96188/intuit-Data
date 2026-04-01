package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;


import com.intuit.ems.tfs.messages.v1.FilingTypeType;
import com.intuit.ems.tfs.messages.v1.PayrollFormInfo;
import com.intuit.ems.tfs.messages.v1.SubmitFilingRequest;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.hibernate.FlushMode;

import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import static com.intuit.ems.payroll.psp.gateways.tfs.util.HTTPHelper.executePost;


/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 8/27/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class SendW2DataToTFS {
    private static SpcfLogger logger = Application.getLogger(SendW2DataToTFS.class);


    private static String[] mJobArguments;
    private static String mCompanyId = null;
    private static int mYear = 0;
    private static TFSSubmissionStatus mStatus = TFSSubmissionStatus.Pending;
    private static boolean mForceError = false;
    private static FilingTypeType mDataspace = null;

    private static final String YEAR_COMMAND = "-year";
    private static final String STATUS_COMMAND = "-status";
    private static final String SINGLE_COMPANYID_COMMAND = "-companyId";
    private static final String FORCE_ERROR = "-forceError";

    public SendW2DataToTFS(String[] pArgs, FilingTypeType pFilingTypeType) {
        mJobArguments = pArgs;
        mDataspace = pFilingTypeType;
    }


    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        try {
            StopWatch sw = new StopWatch().start();

            parseArgs(mJobArguments);
            Application.beginUnitOfWork();
            ArrayList<SpcfUniqueId> companiesToProcess = new ArrayList<SpcfUniqueId>();
            // Check SYSTEM_PARAMETER to verify  if there is a company list to process, otherwise check the
            // command line parameter
            String companyListParameter = SystemParameter.findStringValue(SystemParameter.Code.W2_COMPANY_LIST);

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
                } else {
                    companiesToProcess = findCompaniesToProcess(mYear, mStatus);
                }
            }

            if (mYear == 0) {
                int currentQuarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
                mYear = PSPDate.getPSPTime().getYear();
                if (currentQuarter == 1) {
                    mYear = mYear - 1;
                }
            }

            if (mDataspace == null) {
                throw new RuntimeException("Invalid dataspace " + mDataspace);
            }

            Application.commitUnitOfWork();

            processResult = submitW2sToTFS(mYear, companiesToProcess);

            sw.stop();
            logger.info("completed processing " + "     duration: " + sw.getElapsedTimeString());
        } catch (Throwable t) {
            logger.error("failed to process W2 data transfer to TFS", t);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return processResult;
    }


    private void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split(":");
            
            if (argParts.length == 2) {
                if (argParts[0].equals(YEAR_COMMAND)) {
                    mYear = Integer.parseInt(argParts[1]);
                } else if (argParts[0].equals(STATUS_COMMAND)) {
                    mStatus = TFSSubmissionStatus.valueOf(argParts[1]);
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

    protected ProcessResult submitW2sToTFS(int pYear, ArrayList<SpcfUniqueId> pCompaniesToProcess) throws Exception {
        ProcessResult processResult = new ProcessResult();
        try {
            // process
            if (pCompaniesToProcess.size() > 0) {
                processResult = multithreadProcessing(pYear, pCompaniesToProcess);
            }

            logger.info("Processed W2 data transfer to TFS.");

        } catch (Throwable t) {
            logger.fatal("Exception in W2 data transfer to TFS.", t);
            throw new RuntimeException(t);
        }
        return processResult;
    }


    protected ProcessResult multithreadProcessing(final int pYear, ArrayList<SpcfUniqueId> pCompaniesToProcess) throws Exception {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;
        ProcessResult<HashMap<String, PayrollFormInfo>> processResult = new ProcessResult();
        HashMap<String, PayrollFormInfo> results = new HashMap<String, PayrollFormInfo>();
        try {
            threadPool = Executors.newFixedThreadPool(threadCount);
            CompletionService<ProcessResult<HashMap<String, PayrollFormInfo>>> completionService = new ExecutorCompletionService<ProcessResult<HashMap<String, PayrollFormInfo>>>(threadPool);

            // Execute each company in one thread
            for (final SpcfUniqueId companyId : pCompaniesToProcess) {
                completionService.submit(new Callable<ProcessResult<HashMap<String, PayrollFormInfo>>>() {
                    public ProcessResult call() {
                        return submitCompany(pYear, companyId);
                    }
                });
            }

            // Wait for the results of each thread execution

                for (SpcfUniqueId companyId : pCompaniesToProcess) {
                    try {
                        Future<ProcessResult<HashMap<String, PayrollFormInfo>>> f = completionService.take();
                        HashMap<String, PayrollFormInfo> result = f.get().getResult();
                        if (result != null) {
                            results.putAll(result);
                        }
                    } catch (Throwable t) {
                        logger.error("Exception in W2 data transfer to TFS. Message: " + t.getMessage() + " Stack Trace: " + t.getStackTrace().toString(), t);
                    }

                }
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(threadPool);
        }

        processResult.setResult(results);
        return processResult;
    }

    private ArrayList<SpcfUniqueId> findCompaniesToProcess(int pYear, TFSSubmissionStatus pSubmissionStatus) {
        Criterion<CompanyTFSSubmission> criterion = CompanyTFSSubmission.Year().equalTo(pYear)
                .And(CompanyTFSSubmission.SubmissionStatus().equalTo(pSubmissionStatus));

        if (Company.isDGDeleteFeatureEnabled()) {
            criterion = criterion.And(CompanyTFSSubmission.Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        Expression<CompanyTFSSubmission> query = new com.intuit.sbd.payroll.psp.query.Query<CompanyTFSSubmission>()
                .Select(CompanyTFSSubmission.Company().Id())
                .Where(criterion);

        List result = Application.executeQuery(CompanyTFSSubmission.class, query);

        return (ArrayList<SpcfUniqueId>) result;
    }

    private ProcessResult<HashMap<String, PayrollFormInfo>> submitCompany(int pYear, SpcfUniqueId pCompanyId) {
        ProcessResult<HashMap<String, PayrollFormInfo>> companyResult = new ProcessResult<HashMap<String, PayrollFormInfo>>();
        StopWatch sw = new StopWatch().start();
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.SendW2DataToTFSBatchJob);
            Application.beginUnitOfWork(FlushMode.MANUAL);
            Company company = Application.findById(Company.class, pCompanyId);
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, pYear);
            SubmitFilingRequest submitFilingRequest = createSubmitFilingRequest(company, pYear, companyTFSSubmission.getId().toString());
            WebResource webResource;
            Client client = Client.create();
            webResource = client.resource(getAWSURL());
            logger.info ("Packaging time:" + sw.getElapsedTimeString());
            sw.reset();
            sw.start();
            testResponse(executePost(webResource, MediaType.APPLICATION_XML, submitFilingRequest));
            logger.info ("Call time:" + sw.getElapsedTimeString());

            companyTFSSubmission.setSubmissionStatus(TFSSubmissionStatus.Submitted);
            companyTFSSubmission.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(companyTFSSubmission);

            Application.commitUnitOfWork();
          //  HashMap<String, PayrollFormInfo> results = new HashMap<String, PayrollFormInfo>();
          //  results.put(pCompanyId.toString(), builder.getPayrollFormInfo());

           // companyResult.setResult(results);
            return companyResult;
        } catch (Throwable t) {
            Application.rollbackUnitOfWork();
            Application.beginUnitOfWork();
            Company company = Application.findById(Company.class, pCompanyId);
            CompanyTFSSubmission companyTFSSubmission = CompanyTFSSubmission.findCompanyTFSSubmission(company, pYear);
            companyTFSSubmission.setSubmissionStatus(TFSSubmissionStatus.Error);
            companyTFSSubmission.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(companyTFSSubmission);
            Application.commitUnitOfWork();
            logger.error("Could not submit W2 Data to TFS for company ID: " + pCompanyId, t);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return companyResult;
    }

    private String getAWSURL() {
        String awshost = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awshost");
        String awspath = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awsfilingpath");
        return awshost+awspath;
    }

    protected SubmitFilingRequest createSubmitFilingRequest(Company company, int pYear, String companyTFSSubmissionId){
        SubmitFilingRequest submitFilingRequest = new SubmitFilingRequest();

        SpcfUniqueId pCompanyId = company.getId();
        submitFilingRequest.setFilingSubmissionID(companyTFSSubmissionId);
        PayrollFormInfoBuilder builder = new PayrollFormInfoBuilder();
        builder = builder.buildCompany(company,pYear);
        builder = builder.buildDataSpace(mDataspace, BigInteger.valueOf(pYear));

        processCompanyLawTotals(pCompanyId, pYear, builder);
        processCompanyPayrollItemTotals(pCompanyId, pYear, builder);
        processEmployees(company, pYear, builder);

        submitFilingRequest.getPayrollFormInfo().add(builder.getPayrollFormInfo());
        return submitFilingRequest;
    }

    private void processCompanyLawTotals(SpcfUniqueId pCompanyId, int pYear, PayrollFormInfoBuilder pBuilder) {
        PayrollFormInfo.CompanyInfo companyInfo = pBuilder.getCompanyInfo();
        String select =
                " select eeTotals.Company.Id, eeTotals.CompanyLaw.Id,  " +
                        "sum(eeTotals.Amount) as TotalTaxAmount," +
                        " sum(eeTotals.TaxableWages) as TotalTaxWages, " +
                        " sum(eeTotals.TipsTaxableWagesAmount) as TotalTipsTaxableWages, " +
                        " sum(eeTotals.TotalWages) as TotalTotalWages" +
                        " from  com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals ";
        String where =
                " where eeTotals.Year = :year" +
                        "   and eeTotals.CompanyLaw is not null" +
                        "   and eeTotals.Law is not null ";


        if (pCompanyId != null) {
            where += "   and eeTotals.Company = :company";
        }
        String groupBy = " group by eeTotals.Company.Id, eeTotals.CompanyLaw.Id";

        org.hibernate.Query hibernateQuery;
        try {
            hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("year", pYear);

            if (pCompanyId != null) {
                Company company = Application.findById(Company.class, pCompanyId);
                hibernateQuery.setParameter("company", company);
            }

        } catch (Throwable t) {
            logger.fatal("Exception", t);
            throw new RuntimeException(t);
        }

        try {
            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();

            for (Object[] result : results) {
                CompanyLaw companyLaw = Application.findById(CompanyLaw.class, (SpcfUniqueId) result[1]);
                Law law = companyLaw.getLaw();
                EmployeeW2Totals eeW2Totals = new EmployeeW2Totals();
                Company company = Application.findById(Company.class, pCompanyId);
                eeW2Totals.setCompany(company);
                eeW2Totals.setYear(pYear);
                eeW2Totals.setCompanyLaw(companyLaw);
                eeW2Totals.setLaw(law);
                eeW2Totals.setAmount((SpcfMoney) result[2]);
                eeW2Totals.setTaxableWages((SpcfMoney) result[3]);
                eeW2Totals.setTipsTaxableWagesAmount((SpcfMoney) result[4]);
                eeW2Totals.setTotalWages((SpcfMoney) result[5]);

                String lawId = law.getLawId();
                // Federal Totals
                if (isFederalLaw(lawId)) {
                    pBuilder.addCompanyFederalTotals(companyInfo, eeW2Totals, pYear);
                }

                SpcfCalendar beginDate  = SpcfCalendar.createInstance(pYear, 1,1);
                SpcfCalendar endDate = SpcfCalendar.createInstance(pYear,12,31);

                // Tax Item Info
                pBuilder.addCompanyTaxItemInfo(companyInfo, eeW2Totals, beginDate, endDate);

                // Tax Item Totals
                pBuilder.addCompanyTaxItemTotals(companyInfo, eeW2Totals, beginDate, endDate, true);

                // Payroll Item Info
                pBuilder.addPayrollItemInfo(companyInfo, companyLaw);
            }
        } catch (Throwable t) {
            logger.fatal("Exception", t);
            throw new RuntimeException(t);
        }

    }

    final SpcfDecimal NEGATIVE_ONE = SpcfDecimal.createInstance(-1);
    private void processCompanyPayrollItemTotals(SpcfUniqueId pCompanyId, int pYear, PayrollFormInfoBuilder pBuilder) {
        try {
            PayrollFormInfo.CompanyInfo companyInfo = pBuilder.getCompanyInfo();
            String select =
                    " select eeTotals.Company.Id,  eeTotals.CompanyPayrollItem.Id, cPItemInfo.IsEmployeePaid, sum(eeTotals.Amount) as TotalAmount " +
                            " from  com.intuit.sbd.payroll.psp.domain.EmployeeW2Totals eeTotals, " +
                            "       com.intuit.sbd.payroll.psp.domain.QbdtPayrollItemInfo cPItemInfo  ";
            String where =
                    " where cPItemInfo.CompanyPayrollItem = eeTotals.CompanyPayrollItem and" +
                            " eeTotals.Year = :year" +
                            "   and eeTotals.CompanyPayrollItem is not null";

            if (pCompanyId != null) {
                where += "   and eeTotals.Company = :company";
            }


            String groupBy = " group by eeTotals.Company.Id, eeTotals.CompanyPayrollItem.Id, cPItemInfo.IsEmployeePaid";

            org.hibernate.Query hibernateQuery = Application.createHibernateQuery(select + where + groupBy);

            hibernateQuery.setParameter("year", pYear);

            if (pCompanyId != null) {
                Company company = Application.findById(Company.class, pCompanyId);
                hibernateQuery.setParameter("company", company);
            }

            @SuppressWarnings({"unchecked"})
            List<Object[]> results = (List<Object[]>) hibernateQuery.list();

            for (Object[] result : results) {
                CompanyPayrollItem companyPayrollItem = Application.findById(CompanyPayrollItem.class, result[1]);
                if (companyPayrollItem != null) {

                    SpcfDecimal amount = SpcfMoney.ZERO;
                    if(result[3]!=null)
                    {
                        amount= SpcfDecimal.createInstance(result[3].toString());
                    }

                    if (!(Boolean) result[2] && companyPayrollItem.getPayrollItem().getPayrollItemType().equals(PayrollItemType.Deduction)) {
                        amount = amount.multiply(NEGATIVE_ONE);
                    }
                    pBuilder.addCompanyW2Totals(companyInfo, companyPayrollItem, new SpcfMoney(amount));
                    pBuilder.addPayrollItemInfo(companyInfo, companyPayrollItem);
                }

            }
        } catch (Throwable t) {
            logger.error("Could not process payroll item calculation for company ID: " + pCompanyId, t);
            throw new RuntimeException(t);
        }
    }

    private void processEmployees(Company pCompany, int pYear, PayrollFormInfoBuilder pBuilder) {

        DomainEntitySet<EmployeeW2Totals> eeW2Totals = Application.find(EmployeeW2Totals.class, new Query<EmployeeW2Totals>()
                .Where(EmployeeW2Totals.Company().equalTo(pCompany)
                                       .And(EmployeeW2Totals.Year().equalTo(pYear))).EagerLoad(EmployeeW2Totals.Employee(),EmployeeW2Totals.CompanyPayrollItem().QbdtPayrollItemInfo()));
        for (EmployeeW2Totals eeTotals : eeW2Totals) {
            Employee employee = eeTotals.getEmployee();
            if (!pBuilder.containsEmployee(employee)) {
                pBuilder.addEmployee(employee);
            }
            Law law = eeTotals.getLaw();
            PayrollFormInfo.EmployeeInfo employeeInfo = pBuilder.getEmployeeInfo(employee);
            // Federal Totals
            if (law != null && isFederalLaw(law.getLawId())) {
                pBuilder.addEmployeeFederalTotals(employeeInfo, eeTotals, pYear);
            }

            // W2Totals   - Only Payroll Items
            if (law == null && eeTotals.getCompanyPayrollItem() != null) {
                SpcfDecimal amount = eeTotals.getAmount();
                if(amount == null){
                    amount = SpcfMoney.ZERO;
                }

                if (eeTotals.getCompanyPayrollItem().getPayrollItem().getPayrollItemType().equals(PayrollItemType.Deduction) && !eeTotals.getCompanyPayrollItem().getQbdtPayrollItemInfo().getIsEmployeePaid()) {
                    amount = amount.multiply(NEGATIVE_ONE);
                }
                pBuilder.addEmployeeW2Totals(employeeInfo, eeTotals.getCompanyPayrollItem(), amount);
            }

            // TaxItemTotals  - Only Laws
            if (law != null && eeTotals.getCompanyPayrollItem() == null) {
                SpcfCalendar beginDate  = SpcfCalendar.createInstance(pYear, 1,1);
                SpcfCalendar endDate = SpcfCalendar.createInstance(pYear,12,31);
                pBuilder.addEmployeeTaxItemTotals(employeeInfo, eeTotals, beginDate, endDate, true);
            }
        }

    }

    private void testResponse(ClientResponse pClientResponse) {
        if (mForceError) {
            throw new RuntimeException("Failed - forcing error for test purposes.");
        } else {
            if (pClientResponse.getStatus() != HttpURLConnection.HTTP_OK)  {
                String output = pClientResponse.getEntity(String.class);
                System.out.println("Output from Server .... \n");
                System.out.println(output);
                throw new RuntimeException("Failed : " + pClientResponse.toString());
            }
        }
    }

    private boolean isFederalLaw(String pLawId) {
        Law law = Application.findById(Law.class, pLawId);
        return (law.isFIT() || law.isFICA() || law.isFUTA() || law.isAEIC() || law.isMED());
    }
}
