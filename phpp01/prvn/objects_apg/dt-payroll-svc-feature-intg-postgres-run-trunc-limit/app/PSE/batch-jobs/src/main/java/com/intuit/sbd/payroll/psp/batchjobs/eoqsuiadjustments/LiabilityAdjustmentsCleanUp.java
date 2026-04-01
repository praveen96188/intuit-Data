package com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static sun.util.calendar.CalendarUtils.mod;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr, 17 2012
 * Time: 11:04:41 AM
 */
public class LiabilityAdjustmentsCleanUp {
    private static SpcfLogger logger = Application.getLogger(LiabilityAdjustmentsCleanUp.class);

    private static boolean mCommit = false;
    private static int mQuarter;

    private static final String COMMIT_COMMAND = "-commit";
    private static final String QUARTER_COMMAND = "-quarter";

    private PSPRequestContextManager pspRequestContextManager;

    public LiabilityAdjustmentsCleanUp() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public static void main(String[] args) {
        parseArgs(args);

        new LiabilityAdjustmentsCleanUp().process(mCommit, mQuarter);
    }


    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.parseBoolean(argParts[1]);
                } else if (argParts[0].equals(QUARTER_COMMAND)) {
                    mQuarter = Integer.parseInt(argParts[1]);
                    try {
                        SpcfCalendar date = CalendarUtils.getFirstDayOfQuarter(mQuarter / 10, mod(mQuarter, 10));
                        if (date == null) {
                            throw new RuntimeException("Invalid quarter: " + arg + " Format must be YYYYQ");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid quarter: " + arg + " Format must be YYYYQ");
                    }
                } else
                    throw new RuntimeException("Invalid command: " + argParts[0]);
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    public void process(final boolean pCommit, int pQuarter) {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.LiabilityAdjustmentsCleanup);

        logger.info("Beginning Liability Adjustments cleanup...");

        StringBuilder report = new StringBuilder();
        report.append("\nCommit is set to ").append(pCommit).append("\n");

        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * (2);
        logger.info("Creating thread pool with " + threadCount + " threads");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            final SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(pQuarter / 10, mod(pQuarter, 10));
            final SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(quarterStartDate);
            final List<Object[]> liabilityAdjustments = findAgentLiabilityAdjustments(quarterStartDate, quarterEndDate);

            String foundAdjustments = "Found " + liabilityAdjustments.size() + " adjustments to be processed. \n";
            logger.info(foundAdjustments);
            report.append(foundAdjustments);

            final Set<SpcfUniqueId> companyIds = new HashSet<SpcfUniqueId>();
            final HashMap<SpcfUniqueId, HashMap<String, SpcfMoney>> companyAdjustmentAmounts = new HashMap<SpcfUniqueId, HashMap<String, SpcfMoney>>();
            final HashMap<SpcfUniqueId, HashMap<String, SpcfCalendar>> companyAdjustmentDates = new HashMap<SpcfUniqueId, HashMap<String, SpcfCalendar>>();
            for (Object[] liabilityAdjustment : liabilityAdjustments) {
                SpcfUniqueId companyId = (SpcfUniqueId) liabilityAdjustment[0];

                if (!companyAdjustmentAmounts.containsKey(companyId)) {
                    companyAdjustmentAmounts.put(companyId, new HashMap<String, SpcfMoney>());
                    companyAdjustmentDates.put(companyId, new HashMap<String, SpcfCalendar>());
                }
                companyAdjustmentAmounts.get(companyId).put((String) liabilityAdjustment[1], (SpcfMoney) liabilityAdjustment[2]);
                companyAdjustmentDates.get(companyId).put((String) liabilityAdjustment[1], (SpcfCalendar) liabilityAdjustment[3]);

            }

            String foundCompanies = "Found " + companyAdjustmentAmounts.size() + " companies with adjustments. \n";
            logger.info(foundCompanies);
            report.append(foundCompanies);

            CompletionService<StringBuilder> completionService = new ExecutorCompletionService<StringBuilder>(executor);

            for (final SpcfUniqueId companyId : companyAdjustmentAmounts.keySet()) {
                completionService.submit(new Callable<StringBuilder>() {
                    public StringBuilder call() {
                        StringBuilder companyReport = new StringBuilder();
                        boolean success = false;
                        try {
                            Application.initialize();
                            ApplicationSecondary.initialize();
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.LiabilityAdjustmentsCleanup));
                            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                            Company company = Application.findById(Company.class, companyId);
                            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, BatchJobType.EoqSUIAdjustments.toString());
                            HashMap<String, SpcfMoney> adjustments = companyAdjustmentAmounts.get(companyId);
                            HashMap<String, SpcfCalendar> adjustmentDates = companyAdjustmentDates.get(companyId);
                            for (String lawId : adjustments.keySet()) {
                                Law law = Application.findById(Law.class, lawId);
                                // Find the total amount of adjustments created by QBDT (Adapter) for this law/quarter
                                SpcfMoney qbdtAdjustmentAmount = findQBDTAdjustmentAmount(company, law, quarterStartDate, quarterEndDate, adjustmentDates.get(lawId));

                                // Create an "offset" liability amount that equals the smaller amount * -1  between qbdt and agent adjustments

                                SpcfMoney offsetAdjustmentAmount = qbdtAdjustmentAmount;
                                if (qbdtAdjustmentAmount.isGreaterThan(SpcfMoney.ZERO) && adjustments.get(lawId).isGreaterThan(SpcfMoney.ZERO)
                                        || qbdtAdjustmentAmount.isLessThan(SpcfMoney.ZERO) && adjustments.get(lawId).isLessThan(SpcfMoney.ZERO)) {
                                    if (qbdtAdjustmentAmount.abs().isGreaterThan(adjustments.get(lawId).abs())) {
                                        offsetAdjustmentAmount = adjustments.get(lawId);
                                    }
                                    offsetAdjustmentAmount = new SpcfMoney(offsetAdjustmentAmount.negate());

                                    if (!offsetAdjustmentAmount.isZero()) {
                                        logger.info("Creating offset adjustment for company: " + company.getSourceCompanyId() + ", Law: " + lawId + " : " + offsetAdjustmentAmount.toString());
                                        companyReport.append("Creating offset adjustment for company: " + company.getSourceCompanyId() + ", Law: " + lawId + " : " + offsetAdjustmentAmount.toString() + "\n");
                                        CompanyAdjustmentSubmissionDTO dto = new CompanyAdjustmentSubmissionDTO();
                                        dto.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
                                        dto.setMemo("Offset Adjustment - Liability Adjustment Cleanup");
                                        dto.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
                                        dto.setIsVoid(false);

                                        LiabilityAdjustmentDTO laDTO = new LiabilityAdjustmentDTO();
                                        laDTO.setAmount(offsetAdjustmentAmount);
                                        laDTO.setEffectiveDate(new DateDTO(quarterEndDate));
                                        laDTO.setLawId(law.getLawId());
                                        laDTO.setReconcilingAdjustment(false);
                                        dto.getLiabilityAdjustmentDTOs().add(laDTO);

                                        dto.setTotalAmount(offsetAdjustmentAmount);
                                        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
                                        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
                                        liabilityAdjustmentOptionsDTO.setDebitCustomer(false);
                                        liabilityAdjustmentOptionsDTO.setCreditCustomer(false);
                                        liabilityAdjustmentOptionsDTO.setUseVarianceAccount(false);
                                        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(false);
                                        liabilityAdjustmentOptionsDTO.setForceToRecordFTs(false);
                                        liabilityAdjustmentOptionsDTO.setSUIAdjustment(true);

                                        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager.addLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), null, dto, new DateDTO(quarterEndDate), liabilityAdjustmentOptionsDTO, null, false);

                                        if (!processResult.isSuccess()) {
                                            throw new RuntimeException("Error creating offset adjustment for company: " + company.getSourceCompanyId() + " Law: " + law.getLawId());
                                        }
                                        PayrollRun payrollRun = processResult.getResult().getPayrollRun();
                                        payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
                                    }
                                }
                            }
                            if (pCommit) {
                                logger.info("Committing updates for " + company.getSourceCompanyId());
                                PayrollServices.commitUnitOfWork();
                            }

                            success = true;
                        } catch (
                                Throwable t
                                )

                        {
                            logger.error("Error processing company " + companyId.toString(), t);
                        } finally

                        {
                            PayrollServices.rollbackUnitOfWork();
                            pspRequestContextManager.clearRequestContext();
                        }

                        if (!success)

                        {
                            return null;
                        }

                        return companyReport;
                    }
                });
            }


            int successes = 0;
            int total = 0;

            for (SpcfUniqueId companyId : companyIds) {
                Future<StringBuilder> f = completionService.take();
                StringBuilder companyReport = f.get();
                total++;
                logger.info("Completed processing " + total + " of " + companyIds.size() + " companies");
                if (companyReport != null) {
                    successes++;
                    report.append(companyReport);
                } else {
                    logger.error("Failed to process adjustments for company " + companyId.toString());
                }
            }

            if (pCommit) {
                PayrollServices.commitUnitOfWork();
            }

            logger.info("Success on " + successes + " companies out of " + companyIds.size());

            logger.info("Finished fixing adjustments");
        } catch (
                Throwable t
                )

        {
            logger.error("An error occurred", t);
        } finally

        {
            PayrollServices.rollbackUnitOfWork();
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }

        logger.info(report.toString());
    }

// find Agent Liability Adjustments

    private static List findAgentLiabilityAdjustments(SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Date startDate = CalendarUtils.convertLocalTimestamp(pFromDate.getTimeInMilliseconds());
        Timestamp statTimeStamp = new Timestamp(startDate.getTime());

        Date endDate = CalendarUtils.convertLocalTimestamp(pToDate.getTimeInMilliseconds());
        Timestamp endTimeStamp = new Timestamp(endDate.getTime());

        org.hibernate.Query queryObject = Application.getHibernateSession().getNamedQuery("findAgentLiabilityAdjustments");
        queryObject.setParameter("fromDate", statTimeStamp);
        queryObject.setParameter("toDate", endTimeStamp);

        return queryObject.list();
    }


    private static SpcfMoney findQBDTAdjustmentAmount(Company pCompany, Law pLaw, SpcfCalendar pFromDate, SpcfCalendar pToDate, SpcfCalendar pCreatedDate) {

        SpcfMoney totalQBDTAmount = SpcfMoney.ZERO;

        Expression<LiabilityAdjustment> agentAdjustmentQuery = new Query<LiabilityAdjustment>()
                .Select(LiabilityAdjustment.Amount().Sum())
                .Where(LiabilityAdjustment.Company().equalTo(pCompany)
                                          .And((LiabilityAdjustment.Law().equalTo(pLaw)
                                                                   .And(LiabilityAdjustment.CreatorId().equalTo("QBDTAdapter"))
                                                                   .And(LiabilityAdjustment.CreatedDate().greaterThan(pCreatedDate))
                                                                   .And(LiabilityAdjustment.EffectiveDate().between(pFromDate, pToDate)))));


        List qbdtAmountList = Application.executeQuery(LiabilityAdjustment.class, agentAdjustmentQuery);
        if (qbdtAmountList.size() > 0) {
            totalQBDTAmount = (SpcfMoney) qbdtAmountList.get(0) != null ? (SpcfMoney) qbdtAmountList.get(0) : SpcfMoney.ZERO;
        }

        return totalQBDTAmount;

    }

}
