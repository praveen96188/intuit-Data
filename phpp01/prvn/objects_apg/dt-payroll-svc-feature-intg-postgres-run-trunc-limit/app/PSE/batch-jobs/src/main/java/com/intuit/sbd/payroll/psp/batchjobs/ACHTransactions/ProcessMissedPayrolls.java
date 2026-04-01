package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * User: rkrishna, kpaul
 * Date: Jun 2, 2008
 * Time: 9:24:47 AM
 */
public class ProcessMissedPayrolls {
    private static SpcfLogger logger = Application.getLogger(ProcessMissedPayrolls.class);
    private enum PayrollAction { NODATA, NOACTION, SENDEMAIL, CANCEL }

    private static final String NEWLINE = "\n";
    private static final String OFFSET = "   ";

    private PSPRequestContextManager pspRequestContextManager;

    public ProcessMissedPayrolls() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * Method method to invoke the Missed Payroll Batch process
     *
     * @param args String
     */
    public static void main(String args[]) {
        try {
            if (args.length != 1) {
                throw new RuntimeException("Wrong number of parameters. Usage: ProcessMissedPayrolls <yyyyMMdd>");
            }

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.MissedPayrollsBatchJob));

            try {
                PayrollServices.beginUnitOfWork();

                new ProcessMissedPayrolls().process(args[0]);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable t) {
            logger.fatal("Exception in Missed Payroll Processor ", t);
            t.printStackTrace();
            System.exit(1);
        }
    } 

    /**
     * Function to set to convert the given processing date from String to SpcfCalendar and call the methods to process
     * the Missed payrolls for QBOE & QBDT systems.
     *
     * @param pProcessingDate String
     * @return message
     */
    public String process(String pProcessingDate) {
        SpcfCalendar currentDate = PSPDate.getPSPTime(); // returns as local time
        SpcfCalendar processingDate = currentDate.copy();

        // if no date provided, default to PSPDate
        if (pProcessingDate == null) {
            pProcessingDate = processingDate.format(BatchUtils.DATE_FORMAT);
        }

        // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
        if (!pProcessingDate.matches(BatchUtils.VALIDYYYYMMDD)) {
            throw new RuntimeException("Invalid processing date specified: " + pProcessingDate +
                                       " (required date format is: " + BatchUtils.DATE_FORMAT + ")");
        } else {
            SpcfCalendar pDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, pProcessingDate);
            processingDate.setValues(pDate.getYear(), pDate.getMonth(), pDate.getDay());
        }

        CalendarUtils.clearTime(currentDate);
        CalendarUtils.clearTime(processingDate);

        if (processingDate.after(currentDate)) {
            throw new RuntimeException("Invalid processing date specified: " + pProcessingDate +
                                       " (must be <= " + currentDate.format(BatchUtils.DATE_FORMAT) + ")");
        }

        StopWatch timer = StopWatch.startTimer();
        logger.info("Beginning Process Missed Payrolls batch job for date: " + processingDate.format(BatchUtils.DATE_FORMAT));

        //Function call to process the Missed Payrolls for QBOE Source System
        processMissedPayrollsForQBOE(processingDate);

        //Function call to process the Missed Payrolls for QBDT Source System
        Map<PayrollRun, PayrollAction> payrollRunPayrollActionMap = processMissedPayrollsForQBDT(processingDate);
        String message = generateMessage(payrollRunPayrollActionMap);

        logger.info("Completed Process Missed Payrolls batch job. Elapsed time: " + timer.stop().getElapsedTimeString());

        return message;
    }

    private String generateMessage(Map<PayrollRun, PayrollAction> pPayrollRunPayrollActionMap) {
        DomainEntitySet<PayrollRun> payrollRuns = new DomainEntitySet<PayrollRun>(pPayrollRunPayrollActionMap.keySet())
                .sort(PayrollRun.Company().LegalName(),
                      PayrollRun.Company().Id(),
                      PayrollRun.SourcePayRunId());

        StringBuilder notificationMessage = new StringBuilder();

        int compCtr = 0;
        Company priorCompany = null;

        for (PayrollRun payrollRun : payrollRuns) {
            Company currCompany = payrollRun.getCompany();

            if (currCompany != null && currCompany != priorCompany) {
                if (priorCompany != null) {
                    notificationMessage.append(NEWLINE);
                }
                notificationMessage.append("Company ").append(++compCtr);
                notificationMessage.append(ProcessMissedACHTransactions.generateCompanyInfo(currCompany));

            }
            notificationMessage.append(ProcessMissedACHTransactions.generatePayrollInfo(payrollRun))
                               .append(NEWLINE).append(OFFSET).append(OFFSET)
                               .append("Action: ").append(pPayrollRunPayrollActionMap.get(payrollRun).toString());

            priorCompany = currCompany;
        }

        return notificationMessage.toString();

    }



    /**
     * Function to process the Missed payrolss for QBOE SourceSystem for a given processing date
     *
     * @param pProcessingDate SpcfCalendar
     */
    private void processMissedPayrollsForQBOE(SpcfCalendar pProcessingDate) {
        logger.info("Processing Missed Payrolls for QBOE");

        // PSRV001598
        DomainEntitySet<PayrollRun> payrollRuns =
                PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDate(pProcessingDate,
                                                                           SourceSystemCode.QBOE,
                                                                           PayrollStatus.Pending,
                                                                           PayrollStatus.OffloadedDebit);

        for (PayrollRun payrollRun : payrollRuns) {
            PayrollAction action = PayrollAction.NOACTION;

            /**
             *
             * Cancel the Payroll && Cancel any �Pending� Financial Transactions associated with the
             * payroll of type ERDDDB or EEDDCR if The employer debit (ERDDDB) OR
             * *all* employee credit (EEDDCR) transactions for the payroll run match the following criteria
             *
             * The associated FT�s TransactionType = ERDDDB or EEDDCR
             * The associated FT�s OnHold status is TRUE
             * The associated Financial Transaction�s CurrentTransactionState is Pending/Created.
             * The associated Financial Transaction�s SettlementTypeCd is ACH.
             * Money Movement Transaction�s InitiationDate matches <= PROCESSING_DATE
             * (this will be true if PayrollRun appears in result set from findPayrollsByStatusOnOrBeforeMMTInitiationDate)
             *
             */

            // Get EmployerDdDebit Transactions for the payroll run
            DomainEntitySet<FinancialTransaction> erFinTxnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit}, null);

            // For loop to iterate thru EmployerDdDebit transactions and check whether any one of the EmployerDdDebit
            // transactions is meeting the criteria or not.
            for (FinancialTransaction finTxn : erFinTxnList) {
                // Set the cancel payroll flag to true if one of the employer debit transactions meets the criteria
                // and break the loop
                if (finTxn.getOnHold() && finTxn.getSettlementTypeCd().equals(SettlementType.ACH) &&
                    finTxn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerDdDebit) &&
                    (finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created) ||
                     finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled))) {
                    action = PayrollAction.CANCEL;
                    break;
                }
            }

            // If None of the EmployerDdDebit transactions meeting the criteria, then check whether all the
            // EmployeeDdCredit transactions meeting the criteria or not for the cancelling the payroll
            if (action != PayrollAction.CANCEL) {
                int cancelTxnCount = 0;

                // Get EmployeeDdCredit Transactions for the payroll run
                DomainEntitySet<FinancialTransaction> eeFinTxnList = payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, null);

                for (FinancialTransaction finTxn : eeFinTxnList) {
                    // If condition to check whether all the EmployeeDdCredit transactions are meeting the criteria or not.
                    // If atleast one EmployeeDdCredit transactions doesn't meet the criteria set the cancelPayroll = false
                    // and break the loop for not cancelling the payroll.
                    if (finTxn.getOnHold() && finTxn.getSettlementTypeCd().equals(SettlementType.ACH) &&
                        finTxn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployeeDdCredit) &&
                        (finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created) ||
                         finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled))) {
                        ++cancelTxnCount;
                    } else {
                        break;
                    }
                }

                // if txns exist and the number of txns to cancel = all of the txns, cancel the payroll
                if ((cancelTxnCount > 0) && (cancelTxnCount == eeFinTxnList.size())) {
                    action = PayrollAction.CANCEL;
                }
            }

            if (action == PayrollAction.CANCEL) {
                logger.info("Cancelling payroll run for company: " + payrollRun.getCompany().getSourceCompanyId() +
                            " (payroll run id: " + payrollRun.getId().toString() + ")");

                //Cancel the Payroll Run
                payrollRun.setPayrollRunStatus(PayrollStatus.Canceled);
                payrollRun = Application.save(payrollRun);

                //Cancell Financial Transaction & Create Transaction Resposne
                payrollRun.cancelPayrollFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit,
                                                                                        TransactionTypeCode.EmployeeDdCredit});

                //Create Payroll Cancelled System Event
                CompanyEvent.createPayrollCancelledEvent(payrollRun,
                                                         CancellationReasonCode.CompanyOnHold,
                                                         CancellationScopeCode.EntirePayroll);
            }
        }
    }   

    /**
     * Function to process the Missed payrolls for QBDT Source System for a given processing date
     *
     * @param pProcessingDate SpcfCalendar
     * @return map of payrolls and the actions applied against them
     */
    private Map<PayrollRun, PayrollAction> processMissedPayrollsForQBDT(SpcfCalendar pProcessingDate) {
        logger.info("Processing Missed Payrolls for QBDT");

        Map<PayrollRun, PayrollAction> payrollRunActionMap = new HashMap<PayrollRun, PayrollAction>();

        // PSRV001598
        // Check QBDT companies with payrolls still in a Pending state (could be 2-day or 5-day)
        // Note: We can't just check the current funding model for the company since it may have
        //       changed since this payroll was submitted.  We'll determine the funding model in
        //       play when this payroll was submitted on the fly.

        DomainEntitySet<PayrollRun> payrollRuns;

        if(((String) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES_METHODS)).contains("processMissedPayrollsForQBDT")) {
            payrollRuns =
                    PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDateEagerLoad(pProcessingDate,
                            SourceSystemCode.QBDT,
                            PayrollStatus.Pending);
        } else {
            payrollRuns =
                    PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDate(pProcessingDate,
                            SourceSystemCode.QBDT,
                            PayrollStatus.Pending);
        }

        for (PayrollRun payrollRun : payrollRuns) {
            try {
                pspRequestContextManager.setRequestContextCompany(payrollRun.getCompany());
                PayrollAction action;

                // Check EmployerDdDebit transaction for the payroll run
                action = checkPayrollStatus(payrollRun, pProcessingDate, TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerFeeDebit);

                switch (action) {
                    case NODATA:
                        //
                        // Prefunded payrolls will have its EmployerDdDebit txn in a Completed state
                        // which will cause the above checkPayrollStatus() call to return NODATA.
                        //

                        // If this is a not prefunded payroll, we want to cancel the payroll
                        // otherwise, we want to fall-thru to do the EmployeeDdCredit check...
                        if (!isPrefundedPayroll(payrollRun) && !isCreditPayroll(payrollRun)) {
                            // If the EmployerDdDebit cannot be found, log an error--will cancel payroll manually if needed
                            action = PayrollAction.NOACTION;

                            logger.error("No EmployerDdDebit/EmployerTaxDebit transaction exists on Pending payroll for company: " +
                                    payrollRun.getCompany().getSourceCompanyId() +
                                    " (payroll run id: " + payrollRun.getId().toString() + "). Skipping payroll.");
                            break;
                        }

                        // fall-thru intentional

                    case SENDEMAIL: // If the EmployerDdDebit trips, check EmployeeDdCredit
                    case CANCEL:
                        //only retest EE if it is not a tax payroll run
                        if (payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit).size() == 0 && !payrollRun.getCompany().isCompanyOnActiveService(ServiceCode.Tax)) {
                            // Check EmployeeDdCredit transactions for the payroll run
                            action = checkPayrollStatus(payrollRun, pProcessingDate, TransactionTypeCode.EmployeeDdCredit);
                            if (action == PayrollAction.NODATA) {
                                action = checkPayrollStatus(payrollRun, pProcessingDate, TransactionTypeCode.EmployerFeeDebit);
                            }
                        }
                        break;

                    case NOACTION: // if the EmployerDdDebit says no action, we're done.
                        break;
                }

                switch (action) {
                    case CANCEL:
                        cancelPayroll(payrollRun);
                        payrollRunActionMap.put(payrollRun, action);
                        break;

                    case SENDEMAIL:
                        advancePayrollDates(payrollRun);
                        if (payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit).size() == 0 && payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerDdDebit).size() > 0) {
                            //If tax, will not cancel, so do not need cancel warning
                            //if no DD, then it's a fee, so just advance
                            processQBDTAlertEmail(payrollRun, pProcessingDate);
                        }
                        break;
                }
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        // Check QBDT companies with payrolls in an OffloadedDebit state (5-day only)

        if(((String) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES_METHODS)).contains("processMissedPayrollsForQBDT")) {
            payrollRuns = PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDateEagerLoad(pProcessingDate,
                    SourceSystemCode.QBDT,
                    PayrollStatus.OffloadedDebit);
        } else {
            payrollRuns = PayrollRun.findPayrollsByStatusOnOrBeforeMMTInitiationDate(pProcessingDate,
                    SourceSystemCode.QBDT,
                    PayrollStatus.OffloadedDebit);
        }

        for (PayrollRun payrollRun : payrollRuns) {
            try {
                pspRequestContextManager.setRequestContextCompany(payrollRun.getCompany());
                PayrollAction action;

                // Get EmployeeDdCredit Transactions for the payroll run
                action = checkPayrollStatus(payrollRun, pProcessingDate, TransactionTypeCode.EmployeeDdCredit);

                switch (action) {
                    case CANCEL:
                        cancelPayroll(payrollRun);
                        payrollRunActionMap.put(payrollRun, action);
                        break;

                    case SENDEMAIL:
                        advancePayrollDates(payrollRun);
                        processQBDTAlertEmail(payrollRun, pProcessingDate);
                        break;
                }
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        return payrollRunActionMap;
    }

    private boolean isPrefundedPayroll(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> erDebitSet = pPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[] {TransactionStateCode.Completed});

        return (!erDebitSet.isEmpty() && (erDebitSet.get(0).getOriginalTransaction() != null));
    }

    private boolean isCreditPayroll(PayrollRun pPayrollRun) {

        DomainEntitySet<FinancialTransaction> erCreditSet = pPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerTaxCredit},
                new TransactionStateCode[]{TransactionStateCode.Created,
                        TransactionStateCode.Executed,
                        TransactionStateCode.Completed});

        return !erCreditSet.isEmpty();
    }

    /*
     * The EmployerDdDebit transaction's original initiation date will act as the reference date for each payroll
     *
     */
    private SpcfCalendar getPayrollReferenceDate(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> erDebitSet = pPayrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.EmployerTaxCredit},
                new TransactionStateCode[] {TransactionStateCode.Created,
                                            TransactionStateCode.Executed,
                                            TransactionStateCode.Completed});

        SpcfCalendar referenceDate = null;

        // If we found a Created, Executed or Completed EmployerDdDebit txn, use its OriginalInitiationDate as the reference date.
        if (!erDebitSet.isEmpty()) {
            FinancialTransaction ft = erDebitSet.get(0);
            MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

            // The EmployerDdDebit txn might be in a Completed state if the ACHWAITPERIOD has expired
            // or if this is a prefunded payroll. If it is Completed and has an associated MMT, then
            // the ACHWAITPERIOD has expired, otherwise we need to check if it is a prefunded payroll.

            // If the associated mmt is present, use its OriginalInitiationDate as the reference date.
            if (mmt != null) {
                referenceDate = mmt.getOriginalInitiationDate().toLocal();
            } else if (TransactionStateCode.Completed.equals(ft.getCurrentTransactionState().getTransactionStateCd())) {
                // If the mmt is not present on the EmployerDdDebit txn yet the txn is Completed, check to see if
                // this might be a pre-funded payroll.  If so, use the OriginalSettlementDate of the
                // OriginalTransaction to derive the reference date.
                // (subtract one business day from the OriginalSettlementDate to derive the OriginalInitiationDate)

                if (ft.getOriginalTransaction() != null) {
                    referenceDate = ft.getOriginalTransaction().getOriginalSettlementDate().toLocal();
                    CalendarUtils.addBusinessDays(referenceDate, -1);
                }
            }
        }

        return referenceDate;
    }

    /*
     * Determine whether the given payroll run meets the criteria for cancellation.
     *
     * For companies where the EmployerDdDebit and EmployeeDdCredit transactions have different initiation
     * dates, we need to normalize the processing date so the business rules can still be applied.
     * This will come into play for companies on a non 2-day funding model.
     */
    private PayrollAction checkPayrollStatus(PayrollRun pPayrollRun,
                                             SpcfCalendar pProcessingDate,
                                             TransactionTypeCode... pTransactionTypeCodes) {
        // Default to a state signifying that no txns of the given type(s) could be found.
        PayrollAction action = PayrollAction.NODATA;
        SpcfCalendar normalizedDate = pProcessingDate.copy();

        // This date is used as a common baseline for all date arithmetic within this payroll
        // (we use it as an anchor for the payroll when FT initiation dates are different within the payroll)
        SpcfCalendar payrollReferenceDate = getPayrollReferenceDate(pPayrollRun);

        // If we can't determine a payroll reference date, log an error
        // (the business will likely want to take care of this payroll manually)
        if (payrollReferenceDate == null) {
            logger.error(String.format("Unable to determine payroll reference date for payroll run id: %s",
                                       pPayrollRun.getId().toString()));

            return PayrollAction.NOACTION;
        }

        // get the FT's of the requested type(s)
        DomainEntitySet<FinancialTransaction> ftList = pPayrollRun.getFinancialTransactions(pTransactionTypeCodes, null);

        boolean prContainsEmployerTaxDebit = false;
        for (FinancialTransaction finTxn : ftList) {
            if (finTxn.getMoneyMovementTransaction() != null) {
                SpcfCalendar origInitDate = finTxn.getMoneyMovementTransaction().getOriginalInitiationDate().toLocal();

                // if required, normalize the processing date for this FT
                if (!origInitDate.equals(payrollReferenceDate)) {
                    // presumably for 5-day funding, finTxn init date will be > erDebit init date
                    int offset = CalendarUtils.getDifferenceInDays(origInitDate, payrollReferenceDate);

                    normalizedDate = pProcessingDate.copy();
                    normalizedDate.addDays(offset);
                }

                // Get the businessdays difference between Processing Date & OriginalInitiationDate
                // (diffInDays is zero based)
                int diffInDays = CalendarUtils.businessDaysFromDateToDate(origInitDate, normalizedDate);

                if (finTxn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerTaxDebit)) {
                    prContainsEmployerTaxDebit = true;
                }

                /**
                 * Cancel the Payroll, Cancel any �Pending� Financial Transactions associated with the
                 * payroll of type ERDDDB or EEDDCR & send an alert email if The employer debit (ERDDDB) AND
                 * *all* employee credit (EEDDCR) transactions for the payroll run match the following criteria:
                 *
                 * Money Movement Transaction�s OriginalInitiationDate is ten days old or older:
                 * Example:  PROCESSING_DATE � OriginalInitiationDate >= 10 business days
                 * The associated FT�s TransactionType = ERDDDB or EEDDCR
                 * The associated FT�s OnHold status is TRUE
                 * The associated Financial Transaction�s CurrentTransactionState is Created or Cancelled.
                 * The associated Financial Transaction�s SettlementTypeCd is ACH.
                 */

                if (finTxn.getOnHold() && finTxn.getSettlementTypeCd().equals(SettlementType.ACH) &&
                    (finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created) ||
                     finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled))) {
                    //For ERTaxDebit, always advance and never cancel.
                    // Also, don't cancel Assisted payrolls unless it is a BillPayment.
                    if ((diffInDays >= 0 && diffInDays < 9) || prContainsEmployerTaxDebit ||
                            (pPayrollRun.getCompany().isCompanyOnActiveService(ServiceCode.Tax) && !pPayrollRun.getPayrollRunType().equals(PayrollType.BillPayment))) {
                        action = PayrollAction.SENDEMAIL;
                    } else if (diffInDays >= 9) {
                        action = PayrollAction.CANCEL;
                    } else {
                        action = PayrollAction.NOACTION;
                        break;
                    }
                } else {
                    // take no action (i.e. do not cancel this payroll or attempt to send an email)
                    action = PayrollAction.NOACTION;
                    break;
                }
            }
        }

        return action;
    }

    /*
     * Cancel the given payroll and all applicable transactions associated with the payroll run
     *
     */
    private void cancelPayroll(PayrollRun pPayrollRun) {
        logger.info("Cancelling payroll run for company: " + pPayrollRun.getCompany().getSourceCompanyId() +
                    " (payroll run id: " + pPayrollRun.getId().toString() + ")");

        // Determine which financial transaction types need to be cancelled
        // (want FT types for all ACH txns in a CREATED state)
        List<TransactionTypeCode> txnTypes = new Vector<TransactionTypeCode>();
        for (FinancialTransaction ft : pPayrollRun.getFinancialTransactionCollection()) {
            if (ft.getSettlementTypeCd().equals(SettlementType.ACH) &&
                ft.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                TransactionTypeCode code = ft.getTransactionType().getTransactionTypeCd();

                if (!txnTypes.contains(code)) {
                    txnTypes.add(code);
                }
            }
        }

        // Cancel all Financial Transactions of the appropriate type (if any)
        if (!txnTypes.isEmpty()) {
            pPayrollRun.cancelPayrollFinancialTransactions(txnTypes.toArray(new TransactionTypeCode[txnTypes.size()]));
        }

        // Cancel the Payroll Run
        pPayrollRun.setPayrollRunStatus(PayrollStatus.Canceled);

        Application.save(pPayrollRun);

        // Create Payroll Cancelled System Event
        CompanyEvent.createPayrollCancelledEvent(pPayrollRun,
                                                 CancellationReasonCode.CompanyOnHold,
                                                 CancellationScopeCode.EntirePayroll);
    }

    /*
     * Advance the MMT initiation date(s) as well as the FT settlement date(s) by one business day
     *
     */
    private void advancePayrollDates(PayrollRun pPayrollRun) {
        List<SpcfUniqueId> mmtIdList = new Vector<SpcfUniqueId>();

        logger.info("MPP advancing initiation/settlement dates on payroll for on-hold company: " +
                    pPayrollRun.getCompany().getSourceCompanyId() +
                    " (payroll run id: " + pPayrollRun.getId().toString() + ")");

        // go through the ft's/mmt's for this payroll run and bump the settlement dates and init dates
        // bump Impounds first

        DomainEntitySet<FinancialTransaction> impoundFinancialTransactions;

        if(((String) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES_METHODS)).contains("advancePayrollDates")) {
            impoundFinancialTransactions = getFinancialTransactionsForAdvancePayrollDates(pPayrollRun, true);
        } else {
            impoundFinancialTransactions = pPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound));
        }


        for (FinancialTransaction finTxn : impoundFinancialTransactions) {
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            if (mmTxn != null && !mmtIdList.contains(mmTxn.getId()) &&
                    mmTxn.getMoneyMovementPaymentMethod().equals(PaymentMethod.ACHDirectDeposit) &&
                    finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                mmtIdList.add(mmTxn.getId());
                mmTxn.advanceInitiationDate();
            }
        }

        DomainEntitySet<FinancialTransaction> nonImpoundFinancialTransactions;

        if(((String) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES_METHODS)).contains("advancePayrollDates")) {
            nonImpoundFinancialTransactions = getFinancialTransactionsForAdvancePayrollDates(pPayrollRun, false);
        } else {
            nonImpoundFinancialTransactions = pPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().AssociationType().notEqualTo(TransactionAssociationType.Impound));
        }

        for (FinancialTransaction finTxn : nonImpoundFinancialTransactions) {
            MoneyMovementTransaction mmTxn = finTxn.getMoneyMovementTransaction();
            if (mmTxn != null && !mmtIdList.contains(mmTxn.getId()) &&
                    mmTxn.getMoneyMovementPaymentMethod().equals(PaymentMethod.ACHDirectDeposit) &&
                    finTxn.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Created)) {
                mmtIdList.add(mmTxn.getId());
                mmTxn.advanceInitiationDate();
            }
        }
    }

    private DomainEntitySet<FinancialTransaction> getFinancialTransactionsForAdvancePayrollDates(PayrollRun pPayrollRun, boolean impoundStatus) {
        Criterion<FinancialTransaction> where =
                FinancialTransaction.PayrollRun().equalTo(pPayrollRun).And(FinancialTransaction.Company().equalTo(pPayrollRun.getCompany()));

        if(impoundStatus) {
            where = where.And(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound));
        } else {
            where = where.And(FinancialTransaction.TransactionType().AssociationType().notEqualTo(TransactionAssociationType.Impound));
        }


        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(where)
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(pPayrollRun.getCompany()))
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction().FinancialTransactionSet().Filter().Company().equalTo(pPayrollRun.getCompany()))
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction().FinancialTransactionSet().Filter().QbdtTransactionInfo().Company().equalTo(pPayrollRun.getCompany()))
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction().EntryDetailRecordSet().Filter().Company().equalTo(pPayrollRun.getCompany()))
                        .EagerLoad(FinancialTransaction.MoneyMovementTransaction().QbdtTransactionInfo().Company().equalTo(pPayrollRun.getCompany()));

        return Application.find(FinancialTransaction.class, query);
    }

    /*
     * Determine whether a payroll cancel pending email needs to be sent
     *
     */
    private void processQBDTAlertEmail(PayrollRun pPayrollRun, SpcfCalendar pProcessingDate) {
        // diffInDays is zero based
        int diffInDays = CalendarUtils.businessDaysFromDateToDate(getPayrollReferenceDate(pPayrollRun), pProcessingDate);

        if (diffInDays == 4) {
            logger.info("Sending (5 day) payroll cancel pending email notification for company: " +
                        pPayrollRun.getCompany().getSourceCompanyId() + " (payroll run id: " +
                        pPayrollRun.getId().toString() + ")");

            SpcfCalendar cal = pProcessingDate.copy();
            CalendarUtils.addBusinessDays(cal, 5);
            CompanyEvent.createPayrollCancelPendingEvent(pPayrollRun, cal);
        } else if (diffInDays == 8) {
            logger.info("Sending (1 day) payroll cancel pending email notification for company: " +
                        pPayrollRun.getCompany().getSourceCompanyId() + " (payroll run id: " +
                        pPayrollRun.getId().toString() + ")");

            SpcfCalendar cal = pProcessingDate.copy();
            CalendarUtils.addBusinessDays(cal, 1);
            CompanyEvent.createPayrollCancelPendingEvent(pPayrollRun, cal);
        }
    }
}
