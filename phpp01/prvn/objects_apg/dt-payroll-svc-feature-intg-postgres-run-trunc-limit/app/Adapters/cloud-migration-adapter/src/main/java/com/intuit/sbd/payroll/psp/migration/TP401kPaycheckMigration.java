package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: rnorian
 * Date: 7/12/11
 * Time: 5:12 PM
 */
public class TP401kPaycheckMigration {
    private static SpcfLogger logger = PayrollServices.getLogger(TP401kPaycheckMigration.class);

    private static boolean debug = false;

    public static void main(String[] args) {
        String[] psids = new String[]{};

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
                }
            } else if (arg.equals("-threads")) {
                if (i + 1 < args.length) {
                    try {
                        threadCount = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("could not read -threads argument, defaulting to " + threadCount);
                    }
                }
            }
        }

        ExecutorService executor = null;
        try {
            final StopWatch sw = StopWatch.startTimer();

            Application.beginUnitOfWork(FlushMode.MANUAL);

            // load all sent paychecks
            DomainEntitySet<ThirdParty401kBatchPaycheck> sentPaychecks = Application
                    .find(ThirdParty401kBatchPaycheck.class, new Query<ThirdParty401kBatchPaycheck>()
                            .EagerLoad(ThirdParty401kBatchPaycheck.Paycheck()));
            final HashMap<SpcfUniqueId, ThirdParty401kBatchPaycheck> sentPaychecksMap = new HashMap<SpcfUniqueId, ThirdParty401kBatchPaycheck>(sentPaychecks.size());
            for (ThirdParty401kBatchPaycheck sentPaycheck : sentPaychecks) {
                sentPaychecksMap.put(sentPaycheck.getPaycheck().getId(), sentPaycheck);
            }
            logger.info("loaded all sent paychecks (" + sentPaychecks.size() + ")  " + sw);

            // load all 401k companies
            Criterion<CompanyService> whereCriteria = CompanyService.Service().ServiceCd().equalTo(ServiceCode.ThirdParty401k);
            if (psids.length > 0) {
                whereCriteria = whereCriteria.And(CompanyService.Company().SourceCompanyId().in(psids));
            }
            Expression<CompanyService> k401ServiceQuery = new Query<CompanyService>().Where(whereCriteria).EagerLoad(CompanyService.Company());
            DomainEntitySet<CompanyService> k401Services = Application.find(CompanyService.class, k401ServiceQuery);
            logger.info("loaded all 401k companies (" + k401Services.size() + ")  " + sw);

            Application.rollbackUnitOfWork();


            logger.info("creating thread pool with size: " + threadCount + "\t recommended: " + recommended + " for " + processors + " processors.");
            executor = Executors.newFixedThreadPool(threadCount);

            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executor);
            for (final CompanyService k401Service : k401Services) {
                completionService.submit(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        int paychecksProcess = 0;
                        try {
                            paychecksProcess = migrateCompany(sentPaychecksMap, k401Service);
                        } catch (Throwable t) {
                            logger.info("ERROR MIGRATING: " + k401Service.getCompany(), t);
                        } finally {
                            Application.rollbackUnitOfWork();
                        }
                        return paychecksProcess;
                    }
                });
            }

            int totalCompaniesMigrated = 0, totalPaychecksMigrated = 0, level = 0;
            for (int i401k = 0; i401k < k401Services.size(); i401k++) {
                try {
                    Future<Integer> f = completionService.take();
                    totalCompaniesMigrated++;
                    totalPaychecksMigrated += f.get();

                    if (totalPaychecksMigrated / 1000 > level) {
                        level = totalPaychecksMigrated / 1000;
                        logger.info("total paychecks migrated: " + totalPaychecksMigrated + "  " + sw);
                    }
                    if (totalCompaniesMigrated % 50 == 0) {
                        logger.info("total companies migrated: " + totalCompaniesMigrated + " " + sw);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            logger.info("Finished.   " + sw);
         } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Encountered unrecoverable error during processing");
        } finally {
            if (executor != null) {
                ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
            }
        }
    }

    private static Integer migrateCompany(HashMap<SpcfUniqueId, ThirdParty401kBatchPaycheck> pSentPaychecksMap, CompanyService p401kService) {
        StopWatch sw = StopWatch.startTimer();

        Company company = p401kService.getCompany();
        if (debug) { logger.info(company); }

        Application.beginUnitOfWork(FlushMode.MANUAL);

        String hql = "            Select pc\n" +
                "              From com.intuit.sbd.payroll.psp.domain.Paycheck as pc\n" +
                "                   join fetch pc.PayrollRun as pr\n" +
                "                   join fetch pr.Company\n" +
                "                   left join fetch pc.QbdtPaycheckInfo\n" +
                "                   left join fetch pc.ThirdParty401kPaycheckSet tpc\n" +
                "                   left join fetch tpc.ThirdParty401kPaycheckPendingState\n" +
                "             Where pr.Company.Id = :companyId \n" +
                "               and pr.PaycheckDate >= :serviceStartDate \n" +
                "               and pc.CreatedDate >= :serviceStartDate - 45\n";
        org.hibernate.Query query = Application.createHibernateQuery(hql);
        query.setParameter("companyId", company.getId());
        query.setParameter("serviceStartDate", p401kService.getServiceStartDate());
        List<Paycheck> paychecks = (List<Paycheck>) query.list();
        if (debug) {
            logger.info("loaded all paychecks to process (" + paychecks.size() + ")  " + sw);
        }
        int result = paychecks.size();

        int count = 0;
        for (Paycheck paycheck : paychecks) {
            company = paycheck.getPayrollRun().getCompany();
            if (debug) {
                logger.info("paycheck: " + company.getSourceCompanyId() + ":" + paycheck.getId().toString());
            }
            if (paycheck.getThirdParty401kPaycheck() != null) {
                logger.info("skipping paycheck - third party paycheck already exists (" + paycheck.getId()
                        .toString() + ")");
                continue;
            }

            // skip paychecks submitted after a company was no longer active on tp401k
            boolean is401kActive = company.isCompanyOnService(ServiceCode.ThirdParty401k);
            if (!is401kActive && paycheck.getCreatedDate().after(company.getCompanyService(ServiceCode.ThirdParty401k)
                                                                         .getStatusEffectiveDate())) {
                continue;
            }

            SpcfCalendar offloadDate = ThirdParty401kPaycheck.calculate401kBaseOffloadDate(paycheck.getPayrollRun()
                                                                                                   .getPaycheckDate());

            ThirdParty401kPaycheck tp401kPaycheck = createThirdParty401kPaycheck(paycheck);
            Application.save(tp401kPaycheck);

            // if it was sent, it was sent
            if (pSentPaychecksMap.containsKey(paycheck.getId())) {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.Sent);
                tp401kPaycheck.setInitiationDate(pSentPaychecksMap.get(paycheck.getId()).getCreatedDate());
            }
            // ineligible
            else if (paycheck.getPayrollRun().getPayrollRunDate().after(offloadDate) || paycheck.originallyMissedCutoff()) {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.Ineligible);
            }
            // void/deleted
            else if (paycheck.getStatus() != PaycheckStatusCode.Active) {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.Cancelled);
            }
            // invalid EE data
            else if (hasInvalidEmployee(paycheck)) {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.InvalidEmployeeData);
                addPendingPaycheck(tp401kPaycheck);
            }
            // negative amounts (todo_rhn_401k optimize negative amounts check)
            else if (tp401kPaycheck.getPayrollFilePaycheck().isValidForPayrollFile().size() > 0) {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.InvalidPaycheckData);
            }
            // ready to go
            else {
                tp401kPaycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.Pending);
                addPendingPaycheck(tp401kPaycheck);
            }
            addStateHistory(tp401kPaycheck);

            if (++count % 100 == 0) {
                logger.info(count + " paychecks processed " + sw);
                System.out.flush();
            }
        }
        StopWatch commit = StopWatch.startTimer();
        Application.commitUnitOfWork();

        logger.info("completed company " + company + " migration - paychecks: " + paychecks.size() + "\t" + sw + "   commit (" + commit + ")");
        return result;
    }

    private static boolean hasInvalidEmployee(Paycheck pPaycheck) {
        Expression<CompanyEventDetail> invalidEmployeeDataQuery =
                new Query<CompanyEventDetail>()
                        .Where(CompanyEventDetail.CompanyEvent().Company().equalTo(pPaycheck.getPayrollRun()
                                                                                           .getCompany())
                                       .And(CompanyEventDetail.CompanyEvent().EventTypeCd()
                                                    .equalTo(EventTypeCode.InvalidEmployeeInformation))
                                       .And(CompanyEventDetail.CompanyEvent().StatusCd()
                                                    .equalTo(CompanyEventStatus.Active))
                                       .And(CompanyEventDetail.EventDetailTypeCd()
                                                    .equalTo(EventDetailTypeCode.PaycheckId))
                                       .And(CompanyEventDetail.Value().equalTo(pPaycheck.getId().toString())));
        DomainEntitySet<CompanyEventDetail> invalidEmployeeEventDetail = Application
                .find(CompanyEventDetail.class, invalidEmployeeDataQuery);

        boolean hasInvalidEvent = (invalidEmployeeEventDetail.size() > 0);
        boolean isInvalid = pPaycheck.getSourceEmployee() == null || pPaycheck.getSourceEmployee()
                .isValidForCensusFile().size() > 0;


        if (hasInvalidEvent && !isInvalid) {
            logger.info("ERROR: Paycheck " + pPaycheck.getId().toString() + " - EE: " + pPaycheck
                    .getSourceEmployee() + "  hasInvalidEvent but is not currently invalid");
        }

        return hasInvalidEvent || isInvalid;
    }

    private static void addStateHistory(ThirdParty401kPaycheck pTp401kPaycheck) {
        ThirdParty401kPaycheckState newPaycheckState = new ThirdParty401kPaycheckState();
        newPaycheckState.setStateEffectiveDate(PSPDate.getPSPTime());
        newPaycheckState.setStateCd(pTp401kPaycheck.getCurrentStateCd());
        newPaycheckState.setThirdParty401kPaycheck(pTp401kPaycheck);
        Application.save(newPaycheckState);

    }

    private static void addPendingPaycheck(ThirdParty401kPaycheck pTp401kPaycheck) {
        ThirdParty401kPaycheckPendingState newPendingPaycheck = new ThirdParty401kPaycheckPendingState();
        newPendingPaycheck.setThirdParty401kPaycheck(pTp401kPaycheck);
        newPendingPaycheck.setInitiationDate(pTp401kPaycheck.getInitiationDate());
        newPendingPaycheck.setStateCd(pTp401kPaycheck.getCurrentStateCd());
        Application.save(newPendingPaycheck);
    }

    private static ThirdParty401kPaycheck createThirdParty401kPaycheck(Paycheck pPaycheck) {
        ThirdParty401kPaycheck tp401kPaycheck = new ThirdParty401kPaycheck();
        tp401kPaycheck.setCompany(pPaycheck.getCompany());
        tp401kPaycheck.setPaycheck(pPaycheck);
        tp401kPaycheck.setCreatedDate(pPaycheck.getCreatedDate());
        tp401kPaycheck.setCreatorId(pPaycheck.getCreatorId());
        tp401kPaycheck.setModifiedDate(pPaycheck.getModifiedDate());
        tp401kPaycheck.setModifierId(pPaycheck.getModifierId());
        tp401kPaycheck.setInitiationDate(ThirdParty401kPaycheck.calculate401kBaseOffloadDate(pPaycheck.getPayrollRun()
                                                                                                     .getPaycheckDate()));
        return tp401kPaycheck;
    }
}
