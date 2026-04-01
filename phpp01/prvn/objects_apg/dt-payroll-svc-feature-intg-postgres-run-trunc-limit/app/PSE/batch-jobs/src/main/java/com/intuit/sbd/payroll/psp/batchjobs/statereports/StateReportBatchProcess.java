package com.intuit.sbd.payroll.psp.batchjobs.statereports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.statereports.states.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;

/**
 * Batch process to create state coupon files 
 */
public class StateReportBatchProcess {
    private static final SpcfLogger logger;
    
    /** The list of state coupons classes to use */
    private static final StateReportBase[] COUPONS_CLASSES = {new AL_WH(), new IA_WH(), new MA_WH(), new NM_WH(), new KY_WH()};

    /** The list of report frequencies to run */
    private ArrayList<PaymentTemplateFrequency> reportFrequenciesToRun = new ArrayList<PaymentTemplateFrequency>();

    /** The end date to run the reports for */
    private SpcfCalendar endDateForReports;

    /** Should the automatic scheduling be ignored and force the report to run */ 
    private boolean forceRun = false;

    /** Should AllCoupons be run */
    private boolean runAllCoupons = false;
    
    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(StateReportBatchProcess.class);
    }

    /**
     * Constructor
     * @param reportsList A space separated string with end date and any reports to run
     */
    public StateReportBatchProcess(String reportsList) {
        initialize(reportsList);
    }

    /**
     * Initializes the reports list with all frequencies from all classes
     */
    private void initializeWithDefaults() {
        Application.beginUnitOfWork();
        for (StateReportBase couponsClass : COUPONS_CLASSES) {
            for (String reportName : couponsClass.getReportNamesList()) {
                PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(reportName);

                for (PaymentTemplateFrequency paymentTemplateFrequency : paymentTemplate.getPaymentTemplateFrequencyCollection()) {
                    reportFrequenciesToRun.add(paymentTemplateFrequency);
                }
            }
        }
        Application.rollbackUnitOfWork();

        runAllCoupons = true;
    }

    /**
     * Takes a space separated string and extracts end date and any reports
     * @param reportsList A space separated string
     */
    protected void initialize(String reportsList) {
        String[] args = reportsList.split(" ");

        if (reportsList.length() != 0 && args.length > 0) {
            for (String arg : args) {
                StateReportBase foundCouponsBase;

                String reportName = arg;
                String frequency = null;

                if (arg.contains(",")) {
                    // Split report name from frequency
                    String[] splitUp = arg.split(",");
                    reportName = splitUp[0];
                    frequency = splitUp[1];
                }

                // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                    SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                    endDateForReports = SpcfCalendar.createInstance(clDate.getYear(), clDate.getMonth(), clDate.getDay());
                    endDateForReports.addDays(1);
                    endDateForReports.addMilliseconds(-1);
                } else if (arg.equalsIgnoreCase(AllCoupons.REPORT_NAME)) {
                    // ALL-COUPONS should only be run once
                    runAllCoupons = true;
                } else if ((foundCouponsBase = findCouponClassForReportName(reportName)) != null) {
                    Application.beginUnitOfWork();

                    PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(reportName);

                    for (PaymentTemplateFrequency paymentTemplateFrequency : paymentTemplate.getPaymentTemplateFrequencyCollection()) {
                        if (frequency == null ) {
                            // No frequency specified, add all
                            reportFrequenciesToRun.add(paymentTemplateFrequency);
                        } else if (frequency.equals(paymentTemplateFrequency.getPaymentFrequencyId().toString())) {
                            // Frequency specified, add if matches
                            reportFrequenciesToRun.add(paymentTemplateFrequency);
                        }
                    }
                    Application.rollbackUnitOfWork();
                } else {
                    throw new RuntimeException("Could not figure out what to do with argument \"" + arg + "\"");
                }

                // Arguments specified, force reports to run
                forceRun = true;
            }
        } else {
            // No arguments specified
            endDateForReports = PSPDate.getPSPTime();
            initializeWithDefaults();
            logger.info("Running all reports using date " + endDateForReports.format("yyyy/MM/dd"));
        }
    }

    /**
     * Finds the StateCoupon class that handles a report
     * @param reportName The name of the report
     * @return The StateCoupon class that handles a report
     */
    private static StateReportBase findCouponClassForReportName(String reportName) {
        StateReportBase foundStateCouponsBase = null;

        for (StateReportBase couponsClass : COUPONS_CLASSES) {
            if (couponsClass.handlesReport(reportName)) {
                foundStateCouponsBase = couponsClass;
            }
        }

        return foundStateCouponsBase;
    }

    /**
     * Goes through all report frequencies and runs them if scheduled
     */
    public void createFiles() {
//        if (BatchUtils.isWeekendOrHoliday(endDateForReports)) {
//            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
//            return;
//        }
        
        Application.beginUnitOfWork(FlushMode.MANUAL);
        for (PaymentTemplateFrequency reportFrequency : reportFrequenciesToRun) {
            StateReportBase stateCouponsBase = findCouponClassForReportName(reportFrequency.getPaymentTemplate().getPaymentTemplateCd());

            try {
                if (forceRun) {
                    // Reports/Time specified, ignore scheduling
                    logger.info("Forcing run of " + stateCouponsBase.getClass().getSimpleName() + " for frequency " +
                            reportFrequency.getPaymentFrequencyId().toString() + " for time " +
                            endDateForReports.format("yyyy/MM/dd"));
                    stateCouponsBase.process(reportFrequency, endDateForReports);
                } else if (stateCouponsBase.isScheduled(reportFrequency, endDateForReports)) {
                    // No report/time specified, check to see if each frequency is scheduled
                    stateCouponsBase.process(reportFrequency, endDateForReports);
                }
            } catch (Exception e) {
                logger.error("Could not create recon file for " + stateCouponsBase.toString(), e);
            }
        }

        // Now run AllCoupons if it is set to run
        if (runAllCoupons) {
            try {
                AllCoupons allCoupons = new AllCoupons();

                if (forceRun) {
                    // Reports/Time specified, ignore scheduling
                    logger.info("Forcing Run of AllCoupons for time " + endDateForReports.format("yyyy/MM/dd"));
                    allCoupons.process(null, endDateForReports);
                } else if (allCoupons.isScheduled(null, endDateForReports)) {
                    logger.info("Running of AllCoupons for time " + endDateForReports.format("yyyy/MM/dd"));
                    allCoupons.process(null, endDateForReports);
                }
            } catch (Exception e) {
                logger.error("Could not create AllCoupons file", e);
            }
        }

        Application.commitUnitOfWork();
    }
}
