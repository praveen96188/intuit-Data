package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.ScrollableResults;
import org.hibernate.exception.GenericJDBCException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Mar 25, 2008
 * Time: 3:33:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessACHTransactions {
    private static final SpcfLogger logger = Application.getLogger(ProcessACHTransactions.class);

    private SpcfCalendar processingDate;
    private int interval;
    private int minPoolSize;
    private int maxPoolSize;
    private int maxWait;
    private PSPRequestContextManager pspRequestContextManager;

    public ProcessACHTransactions() {
        interval = SystemParameter.findIntValue(SystemParameter.Code.ACH_TRANSACTION_THREAD_POOL_INTERVAL, 60);
        maxWait = SystemParameter.findIntValue(SystemParameter.Code.ACH_TRANSACTION_THREAD_POOL_MAX_WAIT, 5 * 60);
        minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ACH_TRANSACTION_MIN_THREAD_POOL_SIZE, 10);
        maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ACH_TRANSACTION_MAX_THREAD_POOL_SIZE, 40);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * main method to be invoked by the scheduler
     *
     * @param args
     */
    public static void main(String args[]) {
        try {
            if ((args == null) || (args.length != 1)) {
                throw new RuntimeException("Wrong number of parameters. Usage: ProcessACHTransactions <yyyyMMdd>");
            }

            if (!args[0].matches(BatchUtils.VALIDYYYYMMDD)) {
                throw new RuntimeException("Invalid processing date format " + args[0] + ". Correct format: yyyyMMdd");
            }

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AchTransactionsBatchJob));

            new ProcessACHTransactions().process(args[0]);
        } catch (Throwable t) {
            t.printStackTrace();
            logger.fatal(t.getMessage(), t);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }

    /**
     * Process offloaded ACH Transactions which are not returned with in the ACH Wait period.
     *
     * @param pProcessingDate
     * @return
     */
    public void process(String pProcessingDate) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }

        process(SpcfCalendar.parse(BatchUtils.DATE_FORMAT, pProcessingDate));
    }

    /**
     * Process offloaded ACH Transactions which are not returned with in the ACH Wait period.
     *
     * @param pProcessingDate
     * @return
     */
    public void process(SpcfCalendar pProcessingDate) {
        if (pProcessingDate == null) {
            throw new RuntimeException("Invalid processing date (null).");
        }

        processingDate = SpcfCalendar.createInstance(pProcessingDate.getYear(), pProcessingDate.getMonth(),
                pProcessingDate.getDay(), SpcfTimeZone.getLocalTimeZone());

        // Do processing seperately for QBOE and QBDT transactions
        // this will be useful when the ACHWaiting period is different for QBOE and QBDT
        processOffloadedTransactions(SourceSystemCode.QBOE);
        processOffloadedTransactions(SourceSystemCode.QBDT);
        processOffloadedTransactions(SourceSystemCode.PSP); //Global book transfers
    }

    private void processOffloadedTransactions(SourceSystemCode pSourceSystemCd) {
        logger.info("Processing ACH transactions for source system " + pSourceSystemCd.toString());

        // Activate bank accounts. TODO: this does not seem to belong here (testing?)
        activateBankAccounts(pSourceSystemCd);

        Boolean manageTransaction = !Application.hasActiveTransaction();

        //
        // Parameters for processing
        //
        if (manageTransaction) PayrollServices.beginUnitOfWork();

        int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);
        SpcfCalendar offloadDateMinusAchWaitPeriodDays = getoffloadDateMinusAchWaitPeriodDays(achWaitPeriodDays);
        SpcfCalendar offloadDateMinusAchWaitPeriodDaysMinus45Days = getOffloadDateMinusAchWaitPeriodDaysMinus45Days(offloadDateMinusAchWaitPeriodDays);

        if (manageTransaction) Application.commitUnitOfWork();

        //
        // Process ACH and EFTPS financial transactions in bulk (no java business logic needed).
        //
        processOffloadedAchAndEftpsTransactionsInBulk(pSourceSystemCd, manageTransaction, offloadDateMinusAchWaitPeriodDays);

        //
        // Process ACH financial transactions that need special business logic
        //
        processOffloadedAchTransactions(pSourceSystemCd, manageTransaction, -1, offloadDateMinusAchWaitPeriodDays, offloadDateMinusAchWaitPeriodDaysMinus45Days);

        //
        // Process 100k payrolls (these are not ACH transactions so were not processed in processOffloadedAchFinancialTransactions)
        //
        processOffloaded100kTransactions(manageTransaction, offloadDateMinusAchWaitPeriodDays, offloadDateMinusAchWaitPeriodDaysMinus45Days);

        //
        // Process reversal offloaded payrolls (these are not ACH transactions so were not processed in processOffloadedAchFinancialTransactions)
        //
        processOffloadedReversalTransactions(pSourceSystemCd, manageTransaction, achWaitPeriodDays);
    }

    private SpcfCalendar getOffloadDateMinusAchWaitPeriodDaysMinus45Days(SpcfCalendar offloadDateMinusAchWaitPeriodDays) {
        SpcfCalendar offloadDateMinusAchWaitPeriodDaysMinus45Days = offloadDateMinusAchWaitPeriodDays.copy();
        offloadDateMinusAchWaitPeriodDaysMinus45Days.addDays(-45);
        return offloadDateMinusAchWaitPeriodDaysMinus45Days;
    }

    private SpcfCalendar getoffloadDateMinusAchWaitPeriodDays(int achWaitPeriodDays) {
        SpcfCalendar offloadDateMinusAchWaitPeriodDays = processingDate.copy();
        CalendarUtils.addBusinessDays(offloadDateMinusAchWaitPeriodDays, -achWaitPeriodDays);
        return offloadDateMinusAchWaitPeriodDays;
    }

    private void processOffloadedReversalTransactions(SourceSystemCode pSourceSystemCd, Boolean manageTransaction, int achWaitPeriodDays) {
        if (manageTransaction) PayrollServices.beginUnitOfWork();
        logger.info("Retrieving reversal transactions to process...");

        SpcfCalendar offloadDateMinusAchWaitPeriodDaysMinus45Days;
        SpcfCalendar settlementDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(settlementDate, -achWaitPeriodDays);
        CalendarUtils.clearTime(settlementDate);

        // PSRV001109: Set look-back time of 45 days for eligible txn window
        offloadDateMinusAchWaitPeriodDaysMinus45Days = settlementDate.copy();
        offloadDateMinusAchWaitPeriodDaysMinus45Days.addDays(-45);

        DomainEntitySet<PayrollRun> payrollRunsInReversalsOffloaded =
                PayrollRun.findReversalsOffloadedPayrollRunsForDateRange(pSourceSystemCd, offloadDateMinusAchWaitPeriodDaysMinus45Days, settlementDate);

        for (PayrollRun currPayrollRun : payrollRunsInReversalsOffloaded) {
            try {
                pspRequestContextManager.setRequestContextCompany(currPayrollRun.getCompany());
                currPayrollRun.updatePayrollRunStatus(PayrollStatus.ReversalsFinished);
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        if (manageTransaction) PayrollServices.commitUnitOfWork();
        logger.info("Done processing reversal transactions.");
    }

    private void processOffloaded100kTransactions(Boolean manageTransaction, SpcfCalendar offloadDateMinusAchWaitPeriodDays, SpcfCalendar offloadDateMinusAchWaitPeriodDaysMinus45Days) {
        if (manageTransaction) PayrollServices.beginUnitOfWork();
        logger.info("Retrieving 100k debit transactions to process...");

        //TODO: Check Query Performance
        Query<FinancialTransaction> directDebitQuery = new Query<FinancialTransaction>();
        directDebitQuery.Where(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Executed, TransactionStateCode.Completed))
                .And(FinancialTransaction.SettlementDate().between(offloadDateMinusAchWaitPeriodDaysMinus45Days, offloadDateMinusAchWaitPeriodDays)));
        directDebitQuery.EagerLoad(FinancialTransaction.PayrollRun());

        DomainEntitySet<FinancialTransaction> erDirectDebits = Application.find(FinancialTransaction.class, directDebitQuery);

        // if there are no other impounds and the payroll status is not complete set the payroll status to complete
        for (FinancialTransaction erDirectDebit : erDirectDebits) {
            try {
                pspRequestContextManager.setRequestContextCompany(erDirectDebit.getCompany());
                PayrollRun payrollRun = erDirectDebit.getPayrollRun();
                if (payrollRun.getPayrollRunStatus() != PayrollStatus.Complete) {

                    Query<FinancialTransaction> financialTransactionQuery = new Query<FinancialTransaction>();
                    financialTransactionQuery.Where(FinancialTransaction.PayrollRun().equalTo(payrollRun)
                            .And(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.EmployerTaxDirectDebit))
                            .And(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound))
                            .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled,
                                    TransactionStateCode.Voided, TransactionStateCode.Completed)));

                    DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, financialTransactionQuery);
                    if (financialTransactions.size() == 0) {
                        payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);

                        //Special case- adding to ATFPayrollsToProcess here since the updatePayrollStatus method only puts payrolls in this table when they
                        //are moving from Pending to Complete.  In this case, the payroll is moving from Offloaded to Complete, but it hasn't been
                        //inserted into this table yet. In order to avoid scanning this table to see if the payroll already exists and was already processed,
                        //we'll just add it again
                        ATFPayrollsToProcess newPayrollToProcess = new ATFPayrollsToProcess();
                        newPayrollToProcess.setPayrollRun(payrollRun);
                        Application.save(newPayrollToProcess);
                    }
                }
            } finally {
             pspRequestContextManager.clearRequestContextCompany();
            }
        }

        if (manageTransaction) Application.commitUnitOfWork();
        logger.info("Done processing 100k debit transactions.");
    }

    private void processOffloadedAchTransactions(SourceSystemCode pSourceSystemCd, Boolean manageTransaction, int batchSize, SpcfCalendar offloadDateMinusAchWaitPeriodDays, SpcfCalendar offloadDateMinusAchWaitPeriodDaysMinus45Days) {
        logger.info("Processing ACH transactions...");

        ExecutorService threadPool = null;
        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            if (manageTransaction) Application.beginUnitOfWork();

            // Keep track of time to report duration of each iteration
            StopWatch totalElapsedTime = StopWatch.startTimer();

            // fetch the offloaded ACH transactions and queue them for processing
            ScrollableResults offloadedTxs =
                    FinancialTransaction.findOffloadedACHFinancialTransactionsInDateRange(
                            pSourceSystemCd,
                            offloadDateMinusAchWaitPeriodDaysMinus45Days,
                            offloadDateMinusAchWaitPeriodDays,
                            batchSize);

            int numberOfQueuedTransactions = 0;
            int numberOfQueuedCompanies = 0;
            SpcfUniqueId lastCompany = SpcfUniqueId.getEmptyUniqueId();
            SpcfUniqueId thisCompany = null;
            List<SpcfUniqueId> transactionsForCompany = new ArrayList<SpcfUniqueId>();
            try {
                while (offloadedTxs.next()) {
                    SpcfUniqueId ftId = (SpcfUniqueId) offloadedTxs.get(0);
                    thisCompany = (SpcfUniqueId) offloadedTxs.get(1);
                    if (lastCompany.compareTo(thisCompany) != 0) {
                        processOffloadedTransactions(completionService, transactionsForCompany, lastCompany);
                        transactionsForCompany.clear();

                        lastCompany = thisCompany;
                        numberOfQueuedCompanies++;
                    }

                    transactionsForCompany.add(ftId);
                    numberOfQueuedTransactions++;
                }

                // Process last group
                processOffloadedTransactions(completionService, transactionsForCompany, thisCompany);
            }
            catch (GenericJDBCException ex) {
                // workaround until Hibernate has the following patch http://opensource.atlassian.com/projects/hibernate/browse/HHH-1804
                if (!ex.getMessage().equals("could not perform sequential read of results (forward)")) {
                    throw new RuntimeException(ex);
                }
            } finally {
                offloadedTxs.close();
            }

            logger.info("Finished queueing transactions to process: " + numberOfQueuedTransactions);

            //
            // Wait and get the results of each thread execution
            //
            int numberOfSuccessfullyProcessedTransactions = 0;
            try {
                for (int t = 0; t < numberOfQueuedCompanies; t++) {
                    Future<Integer> f = completionService.take();
                    numberOfSuccessfullyProcessedTransactions += f.get();

                    if (numberOfSuccessfullyProcessedTransactions % 1000 == 0) {
                        logger.info("working -- completed processing " + (numberOfSuccessfullyProcessedTransactions + 1) + " ACH financial transactions (" + numberOfSuccessfullyProcessedTransactions + "DD payrolls) for fraud in " + totalElapsedTime.getElapsedTimeString());
                    }
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException e) {
                ThreadingUtils.launderThrowable(e.getCause());
            }

            if (manageTransaction) Application.commitUnitOfWork();

            totalElapsedTime.stop();

            String message = String.format("Done processing ACH transactions: processed %,d transactions in %s",
                    numberOfSuccessfullyProcessedTransactions,
                    totalElapsedTime.getElapsedTimeString());
            logger.info(message);
        }
        finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
            }
            if (manageTransaction) Application.rollbackUnitOfWork();
        }
    }

    private void processOffloadedTransactions(CompletionService<Integer> completionService, List<SpcfUniqueId> transactionsForCompany, SpcfUniqueId companyId) {
        if (transactionsForCompany.size() > 0) {
            final List<SpcfUniqueId> finalTransactionsForCompany = new ArrayList<SpcfUniqueId>(transactionsForCompany);
            completionService.submit(new Callable<Integer>() {
                public Integer call() {
                    return processOffloadedAchTransaction(finalTransactionsForCompany, companyId);
                }
            });
        }
    }

    /*
    This is called only by test tools to process transactions that aren't really at the 5 day window.
    Needed for the case where transactions need to be completed without changing the PSP date, but
    changing the transactions' dates cause other issues.
     */
    protected Integer processOffloadedAchTransaction(List<SpcfUniqueId> offloadedFinancialTransactionIds, SpcfUniqueId companyId) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AchTransactionsBatchJob));
            Application.beginUnitOfWork();
            Company company = Application.findById(Company.class, companyId);
            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "NightlyBatchJobs");

            for (SpcfUniqueId offloadedFinancialTransactionId : offloadedFinancialTransactionIds) {
                FinancialTransaction offloadedTx = Application.findById(FinancialTransaction.class, offloadedFinancialTransactionId);
                offloadedTx.updateFinancialTransactionState(TransactionStateCode.Completed);

                // do additional processing for specific txn types
                // update payroll status based on the transaction type offloaded
                TransactionTypeCode transactionTypeCode = offloadedTx.getTransactionType().getTransactionTypeCd();

                //Only update for fee debits when there are no impounds on the payroll
                if (TransactionType.isImpoundTransactionType(transactionTypeCode) ||
                        (transactionTypeCode == TransactionTypeCode.EmployerFeeDebit && offloadedTx.getPayrollRun().getFinancialTransactions(ServiceStatus.getImpoundTypes()).size() == 0)) {
                    SpcfDecimal erReturnReceivableBalance =
                            LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                                    offloadedTx.getPayrollRun().getSourcePayRunId(),
                                    offloadedTx.getPayrollRun().getCompany());

                    //If payroll isn't already completed, need to ensure there aren't any pending Direct Debit for 100K payrolls prior to completing it
                    boolean isPayrollAlreadyCompleted = offloadedTx.getPayrollRun().getPayrollRunStatus() == PayrollStatus.Complete;
                    if (!isPayrollAlreadyCompleted && erReturnReceivableBalance.compareTo(SpcfMoney.ZERO) >= 0 && !payrollHasPendingDirectDebit(offloadedTx.getPayrollRun())) {
                        offloadedTx.getPayrollRun().updatePayrollRunStatus(PayrollStatus.Complete);
                    }

                    // check whether company qualifies for 2 Day Funding
                    boolean qualifiesFor2DayFunding = verifyCompanyQualifiesForTwoDayFunding(offloadedTx.getCompany());

                    // If qualifies update funding model to 2 Day
                    if (qualifiesFor2DayFunding) {
                        FundingModel newFundingModel =
                                PayrollServices.entityFinder.findById(FundingModel.class,
                                        FundingModel.Codes.TWO_DAY);

                        ProcessResult<Company> processResult =
                                PayrollServices.companyManager.updateCompanyFundingModel(
                                        offloadedTx.getCompany().getSourceSystemCd(),
                                        offloadedTx.getCompany().getSourceCompanyId(),
                                        newFundingModel);

                        if (!processResult.isSuccess()) {
                            throw new RuntimeException(processResult.getMessages().get(0).toString());
                        }
                    }
                } else if (TransactionType.isRedebitTransactionType(transactionTypeCode)) {
                    PayrollRun payrollRun = offloadedTx.getPayrollRun();

                    SpcfDecimal erReturnReceivableBalance =
                            LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                                    payrollRun.getSourcePayRunId(),
                                    payrollRun.getCompany());

                    if (erReturnReceivableBalance.compareTo(SpcfDecimal.createInstance("0.00")) >= 0) {
                        payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
                    } else {
                        payrollRun.updatePayrollRunStatus(PayrollStatus.DebitReturned);
                    }

                    boolean isInDebtToIntuit = offloadedTx.getCompany().isCompanyInDebtToIntuit();

                    // If company not in debt to Intuit and company DD state is On Hold, expire ACH Return On Holds
                    if (!isInDebtToIntuit) {
                        expireAchRejectOnHoldReason(offloadedTx.getCompany());
                        //offloadedTx.getCompany().applyRefundsFromVoidedPayrolls();
                    }
                } else if (TransactionTypeCode.EmployeeDdReversalDebit.equals(transactionTypeCode)) {
                    // If company DD status is Active or Suspended and the ER DD DB company bank account is active
                    // create a new ER Financial Transaction for the refund
                    PayrollRun payrollRun = offloadedTx.getPayrollRun();
                    ServiceCode serviceCode;
                    if (payrollRun.getPayrollRunType() == PayrollType.BillPayment) {
                        serviceCode = ServiceCode.BillPayment;
                    } else {
                        serviceCode = ServiceCode.DirectDeposit;
                    }

                    CompanyService service = CompanyService.findCompanyService(offloadedTx.getCompany(), serviceCode);

                    //ServiceStatusCode serviceStatusCd = CompanyServiceBE.getServiceStatus(service.getStatusCd());

                    CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(serviceCode);

                    if (TransactionType.isOffloadable(TransactionTypeCode.EmployerDdReversalRefundCredit, null, offloadedTx.getCompany(), offloadedTx.getBillingDetail()) &&
                            (BankAccountStatus.Active.equals(cba.getStatusCd()))) {
                        // create ER refund transaction
                        ERRefundDTO erRefundDTO = new ERRefundDTO();

                        erRefundDTO.setSettlementType(SettlementTypeDTO.ACH);
                        erRefundDTO.setFinancialTxId(offloadedTx.getId().toString());
                        erRefundDTO.setFinancialTxAmt(offloadedTx.getFinancialTransactionAmount());
                        erRefundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));

                        ProcessResult<FinancialTransaction> processResult =
                                PayrollServices.financialTransactionManager.refundEmployerTransaction(
                                        offloadedTx.getCompany().getSourceSystemCd(),
                                        offloadedTx.getCompany().getSourceCompanyId(),
                                        erRefundDTO);

                        if (!processResult.isSuccess()) {
                            throw new RuntimeException(processResult.getMessages().get(0).toString());
                        }

                        // Also create Reversal OK System Event (Refund issued)
                        CompanyEvent.createReversalOKEvent(offloadedTx.getCompany(),
                                offloadedTx,
                                PSPDate.getPSPTime(),
                                RefundStatusType.Issued,
                                null);
                    } else {
                        // create Reversal OK System Event (Refund not issued)
                        RefundStatusReasonType reasonType;

                        if (ServiceSubStatusCode.Terminated.equals(service.getStatusCd())) {
                            reasonType = RefundStatusReasonType.CompanyTerminated;
                        } else if (ServiceSubStatusCode.PendingTermination.equals(service.getStatusCd())) {
                            reasonType = RefundStatusReasonType.CompanyPendingTermination;
                        } else if (BankAccountStatus.Inactive.equals(cba.getStatusCd())) {
                            reasonType = RefundStatusReasonType.BankAccountInactive;
                        } else {
                            reasonType = RefundStatusReasonType.CompanyOnHold;
                        }

                        CompanyEvent.createReversalOKEvent(offloadedTx.getCompany(),
                                offloadedTx,
                                PSPDate.getPSPTime(),
                                RefundStatusType.NotIssued,
                                reasonType);
                    }
                } else if (TransactionTypeCode.IntuitEmployeeReturnTransfer.equals(transactionTypeCode)) {
                    PayrollRun payrollRun = offloadedTx.getPayrollRun();

                    if (payrollRun.getPayrollRunStatus() == PayrollStatus.ReversalsFinished) {
                        SpcfDecimal erReturnReceivableBalance =
                                LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                                        payrollRun.getSourcePayRunId(),
                                        payrollRun.getCompany());

                        if (erReturnReceivableBalance.compareTo(SpcfDecimal.createInstance("0.00")) >= 0) {
                            payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
                        }
                    }
                } else if (offloadedTx.isEmployerVerificationDebit()) {
                    refundEmployerVerificationDebit(offloadedTx);
                } else if (TransactionTypeCode.EmployerTaxCredit.equals(transactionTypeCode)) {
                    PayrollRun payrollRun = offloadedTx.getPayrollRun();
                    if (payrollRun != null) {
                        payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
                    }
                }
            }
            Application.commitUnitOfWork();
            return offloadedFinancialTransactionIds.size();
        }
        catch (Throwable t) {
            StringBuilder errorMessage = new StringBuilder("Error processing financial transactions : ");
            for (SpcfUniqueId ftId : offloadedFinancialTransactionIds) {
                errorMessage.append(ftId.toString() + ", ");
            }
            errorMessage.append(" :" + t.getMessage());
            logger.error(errorMessage, t);
            return 0;
        }
        finally {
            Application.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
    }

    private void processOffloadedAchAndEftpsTransactionsInBulk(SourceSystemCode pSourceSystemCd, Boolean manageTransaction, SpcfCalendar offloadDateMinusAchWaitPeriodDays) {
        if (manageTransaction) PayrollServices.beginUnitOfWork();

        logger.info("Calling storedProcedure=" + StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR.getStoredProcedureName() +
                " pSourceSystemCd=" + pSourceSystemCd + " currentPrincipal=" + Application.getCurrentPrincipal().getId() + " offloadDateMinusAchWaitPeriodDays=" + offloadDateMinusAchWaitPeriodDays.getTimeInMilliseconds());
        //TODO: Check Query Performance
        Integer numberOfAffectedRows =
                Application.executeSqlProcedureWithOutParameter(StoredProcedures.PRC_ACHTRANSACTIONPROCESSOR,
                                                                false,
                                                                java.sql.Types.INTEGER,
                                                                Pair.of(String.class, pSourceSystemCd.toString()),
                                                                Pair.of(String.class, Application.getCurrentPrincipal().getId()),
                                                                Pair.of(Timestamp.class, new Timestamp(offloadDateMinusAchWaitPeriodDays.getTimeInMilliseconds())),
                                                                Pair.of(Timestamp.class, new Timestamp(PSPDate.getPSPTime().getTimeInMilliseconds())));

        logger.info(numberOfAffectedRows + " rows processed through prc_achtransactionprocessor");

        if (manageTransaction) Application.commitUnitOfWork();
    }

    private void expireAchRejectOnHoldReason(Company pCompany) {
        for (ServiceSubStatusCode onHoldReasonCd : pCompany.getCurrentOnHoldReasonCodes()) {
            if (onHoldReasonCd == ServiceSubStatusCode.AchRejectOther ||
                    onHoldReasonCd == ServiceSubStatusCode.AchRejectR1R9) {
                ProcessResult pr = PayrollServices.companyManager.removeOnHoldReason(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), onHoldReasonCd);
                if (!pr.isSuccess()) {
                    logger.error("Failed to remove Hold reason " + onHoldReasonCd.toString() + "from Company ID:" + pCompany.getSourceCompanyId() + " source system code:" + pCompany.getSourceSystemCd());
                }
            }
        }
    }

    private boolean verifyCompanyQualifiesForTwoDayFunding(Company pCompany) {
        // if currently not on 5 Day funding, doesn't qualify
        if (!FundingModel.Codes.FIVE_DAY.equals(pCompany.getFundingModel().getFundingModelCd())) {
            return false;
        }

        // if status is not ACTIVE, doesn't qualify
        if (!pCompany.isAllowedCapability(SystemCapabilityCode.UpgradeFundingModel)) {
            return false;
        }

        DomainEntitySet<PayrollRun> completedPayrolls =
                PayrollRun.findPayrollRunsByState(pCompany, PayrollStatus.Complete);

        // if number of completed payrolls is not 2 and payroll net amount is less than MinimumNonSuspectPayrollAmount
        // doesn't qualify
        //
        if (completedPayrolls.size() != 2) {
            return false;
        }

        SpcfDecimal minimumNonSuspectPayrollAmount =
                SpcfDecimal.createInstance(LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.MinimumNonSuspectPayrollAmount).getValue());

        for (PayrollRun payrollRun : completedPayrolls) {
            if (payrollRun.getPayrollDirectDepositAmount().compareTo(minimumNonSuspectPayrollAmount) == -1) {
                return false;
            }
        }

        // verify company has no active strikes from last one year
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);

        if (CompanyEvent.getCompanyStrikeCount(pCompany, fromDate, null) > 0) {
            return false;
        }

        // Verify company has ACH Returns excluding the returns on the EE Txns
        DomainEntitySet<TransactionReturn> erReturns =
                TransactionReturn.findUnresolvedTxnReturnsExcludedTxnTypes(
                        pCompany, TransactionCategory.Employee);

        return erReturns.isEmpty();
    }

    /**
     * To activate bank accounts which are in PendingVerification status.
     *
     * @param pSourceSystemCd
     */
    private void activateBankAccounts(SourceSystemCode pSourceSystemCd) {

        // Check if we need to do automatic bank verification or not.
        SourcePayrollParameter sourceParameter =
                SourcePayrollParameter.findSourcePayrollParameter(pSourceSystemCd,
                        SourcePayrollParameterCode.AutomaticCompanyBankAccountVerification);
        if (sourceParameter == null || !sourceParameter.getParameterValue().equals("1"))
            return;

        SpcfCalendar successfulOffloadDate = processingDate.copy();

        int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);
        CalendarUtils.addBusinessDays(successfulOffloadDate, -achWaitPeriodDays);

        // Get all company bank accounts which are in PendingVerification status.
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = CompanyBankAccount.findCompanyBankAccounts(pSourceSystemCd, BankAccountStatus.PendingVerification);

        // For each of the above company Bank Accounts get EmployerVerificationDebit FTs which are in Executed state.
        if (companyBankAccounts != null) {
            for (CompanyBankAccount compBankAccount : companyBankAccounts) {
                DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinancialTransactions(
                        compBankAccount.getCompany().getSourceSystemCd(), compBankAccount.getCompany().getSourceCompanyId(), compBankAccount.getBankAccount(),
                        TransactionTypeCode.EmployerVerificationDebit, successfulOffloadDate, TransactionStateCode.Executed); // There should be two if there are any.

                if (financialTransactions != null && financialTransactions.size() == 2) {
                    PayrollServices.companyManager.verifyCompanyBankAccount(compBankAccount.getCompany().getSourceSystemCd(), compBankAccount.getCompany().getSourceCompanyId(), compBankAccount.getSourceBankAccountId(), financialTransactions.get(0).getFinancialTransactionAmount(), financialTransactions.get(1).getFinancialTransactionAmount(), false);
                }
            }
        }
    }

    private void refundEmployerVerificationDebit(FinancialTransaction pFinancialTransaction) {
        if (!pFinancialTransaction.isEmployerVerificationDebit()) {
            return;
        }

        FinancialTransaction financialTransaction = new FinancialTransaction();

        Company company = pFinancialTransaction.getCompany();

        financialTransaction.setCompany(company);

        financialTransaction.setSku(pFinancialTransaction.getSku());
        financialTransaction.setSkuQuantity(1);

        TransactionType transactionType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerVerificationCredit);
        financialTransaction.setTransactionType(transactionType);

        financialTransaction.setDebitBankAccountType(BankAccountOwnerType.Intuit);
        financialTransaction.setDebitBankAccount(IntuitBankAccount.findIntuitBankAccount(transactionType, CreditDebitCode.Debit).getBankAccount());

        financialTransaction.setCreditBankAccountType(BankAccountOwnerType.Company);
        financialTransaction.setCreditBankAccount(pFinancialTransaction.getDebitBankAccount());

        // Settlement type is ACH
        financialTransaction.setSettlementTypeCd(SettlementType.ACH);

        // Settlement Date
        OffloadGroup offloadGroup = company.getOffloadGroup();
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerVerificationCredit, offloadGroup);
        financialTransaction.setSettlementDate(settlementDate);
        financialTransaction.setOriginalSettlementDate(settlementDate);

        // Amount is a random value between $0.01 and $0.99
        if (pFinancialTransaction.getFinancialTransactionAmount().isGreaterThan(SpcfDecimal.createInstance(1.00))) {
            logger.error(String.format("Employer verification debit for %s in the amount of %s is greater than 1.00; automatic credit was not created.", company.getSourceCompanyId(), pFinancialTransaction.getFinancialTransactionAmount().toString()));
            return;
        }
        financialTransaction.setFinancialTransactionAmount(pFinancialTransaction.getFinancialTransactionAmount());

        // Add the FinancialTransactionState object for the current State
        TransactionState currentTransactionState = Application.findById(TransactionState.class, TransactionStateCode.Created);
        financialTransaction = Application.save(financialTransaction);
        financialTransaction.addTransactionState(currentTransactionState);

        financialTransaction = Application.save(financialTransaction);

        financialTransaction.validateCanCreateFinancialTransaction();
    }


    private boolean payrollHasPendingDirectDebit(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> directDebitTransactions = pPayrollRun.getFinancialTransactionCollection()
                                                                                   .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                                                                                                             .And(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound))
                                                                                                             .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Returned)));


        return (directDebitTransactions != null && directDebitTransactions.size()>0);
    }
}
