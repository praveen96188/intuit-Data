package com.intuit.sbd.payroll.psp.batchjobs.ledgeroperations;

import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationStatus;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.FlushMode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: dweinberg
 * Date: 11/9/12
 * Time: 2:08 PM
 */
public class ProcessLedgerOperations {
    private static SpcfLogger logger = PayrollServices.getLogger(ProcessLedgerOperations.class);

    private int mInterval;
    private int mMinPoolSize;
    private int mMaxPoolSize;
    private int mMaxWait;

    private PSPRequestContextManager pspRequestContextManager;

    public ProcessLedgerOperations() {
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_MIN_THREAD_POOL_SIZE, 10);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_MAX_THREAD_POOL_SIZE, 40);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void process() {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<LedgerOperationJob> queuedJobs = Application.find(LedgerOperationJob.class, LedgerOperationJob.Status().equalTo(LedgerOperationJobStatus.Queued));
        PayrollServices.rollbackUnitOfWork();

        for (LedgerOperationJob queuedJob : queuedJobs) {
            processJob(queuedJob);
        }
    }

    private void processJob(final LedgerOperationJob job) {
        try {
            Application.beginUnitOfWork();
            Application.refresh(job);
            logger.info("Starting job " + job.getId().toString());
            job.setStatus(LedgerOperationJobStatus.InProgress);
            job.setStartTime(PSPDate.getPSPTime());
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        ExecutorService threadPool = null;
        try {
            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval,
                                                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(threadPool);

            logger.info("Beginning to queue " + job.getId().toString());
            Application.beginUnitOfWork(FlushMode.MANUAL);
            Application.refresh(job);
            for (final LedgerOperation operation : job.getLedgerOperationCollection()) {
                completionService.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return processOperation(operation.getId(), job.getJobType());
                    }
                });
            }
            Application.rollbackUnitOfWork();

            logger.info("Beginning to take " + job.getId().toString());

            for (int i = 0; i < job.getLedgerOperationCollection().size(); i++) {
                //noinspection EmptyCatchBlock
                try { completionService.take(); } catch (InterruptedException e) {}
            }

            logger.info("Saving results " + job.getId().toString());

            Application.beginUnitOfWork();
            Application.refresh(job);
            StringWriter stringWriter = new StringWriter();
            CSVWriter writer = new CSVWriter(stringWriter);

            for (LedgerOperation ledgerOperation : job.getLedgerOperationCollection().sort(LedgerOperation.OriginalIndex())) {
                String updatedValue;
                RateLedgerOperation rateLedgerOperation = null;
                DepositFrequencyLedgerOperation depositFreqLedgerOperation;

                if (ledgerOperation.getLedgerOperationJob().getJobType().in(LedgerOperationJobType.RateUpdate, LedgerOperationJobType.AdditionalFilingAmountUpdate)) {
                    rateLedgerOperation = (RateLedgerOperation) ledgerOperation;
                    updatedValue = Double.toString(rateLedgerOperation.getRate());
                } else if (ledgerOperation.getLedgerOperationJob().getJobType().equals(LedgerOperationJobType.DepositFrequencyUpdate)) {
                    depositFreqLedgerOperation = (DepositFrequencyLedgerOperation) ledgerOperation;
                    updatedValue = depositFreqLedgerOperation.getDepositFrequency().toString();
                } else {
                    updatedValue = ObjectUtils.toString(ledgerOperation.getAmount());
                }

                List<String> fields = new ArrayList<String>(Arrays.asList(
                        ledgerOperation.getSourceSystemCode().toString(),
                        ledgerOperation.getSourceCompanyId(),
                        updatedValue,
                        ledgerOperation.getMemo(),
                        job.getJobType().toString(),
                        rateLedgerOperation != null && rateLedgerOperation.getAdditionalFilingAmountName() != null ? rateLedgerOperation.getAdditionalFilingAmountName() : ledgerOperation.getLaw().getLawTypeCd(),
                        ledgerOperation.getOriginalLegalName(),
                        ledgerOperation.getCheckDate().format("yyyy-MM-dd")));

                if (ledgerOperation.getLedgerOperationJob().getJobType() == LedgerOperationJobType.RateUpdate) {
                    fields.add(rateLedgerOperation.getPushToQuickBooks() ? "Y" : "N");
                }
                if (ledgerOperation.getWageAmount() != null) {
                    fields.add(ledgerOperation.getWageAmount().toString());
                }
                fields.add(ledgerOperation.getMessages());

                writer.writeNext(fields.toArray(new String[fields.size()]));
            }

            try {
                writer.flush();
            } catch (IOException e) {
                logger.error(e);
            }

            job.setProcessedFile(stringWriter.toString());
            job.setStatus(LedgerOperationJobStatus.Complete);
            job.setFinishTime(PSPDate.getPSPTime());
            Application.commitUnitOfWork();

            logger.info("Completed processing " + job.getId().toString());
        } finally {
            Application.rollbackUnitOfWork();
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }
        }
    }

    private boolean processOperation(SpcfUniqueId ledgerOperationId, LedgerOperationJobType type) {
        try {
            LedgerOperation operation = null;
            boolean success = false;
            try {
                Application.initialize();
                ApplicationSecondary.initialize();
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.LedgerOperationsBatchJob));

                PayrollServices.beginUnitOfWork();
                operation = Application.findById(LedgerOperation.class, ledgerOperationId);
                Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
                pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "LedgerOperations");
                operation.setStatus(LedgerOperationStatus.InProgress);
                Application.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Error processing operation " + ledgerOperationId.toString(), t);
                return false;
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            ProcessResult result;
            try {
                switch (type) {
                    case BulkDebit:
                        result = processCreditReduction(operation);
                        break;
                    case TOR:
                        result = processTOR(operation);
                        break;
                    case RateUpdate:
                        result = processRateUpdate((RateLedgerOperation) operation);
                        break;
                    case AdditionalFilingAmountUpdate:
                        result = processAdditionalAmountUpdate((RateLedgerOperation) operation);
                        break;
                    case DepositFrequencyUpdate:
                        result = processDepositFrequencyUpdate((DepositFrequencyLedgerOperation) operation);
                        break;
                    case EOQV:
                        result = processEndOfQuarterVariance(operation);
                        break;
                    default:
                        throw new RuntimeException("Unimplemented operation");
                }
            } catch (Throwable t) {
                result = new ProcessResult();
                result.getMessages().ExceptionOccurred(t);
            }

            try {
                PayrollServices.beginUnitOfWork();
                Application.refresh(operation);
                if (result.isSuccess()) {
                    success = true;
                    operation.setStatus(LedgerOperationStatus.Completed);
                } else {
                    operation.setStatus(LedgerOperationStatus.Error);
                    logger.info("Error processing ledger operation " + ledgerOperationId.toString() + "\n" + result.toString());
                }
                operation.setMessages(getFlattenedMessages(result.getMessages()));
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Unexpected error processing operation " + ledgerOperationId.toString(), t);
                return false;
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }



            return success;
        } finally {
            pspRequestContextManager.clearRequestContextCompany();
        }
    }

    private String getFlattenedMessages(MessageList messageList) {
        if (messageList.isEmpty()) {
            return "Success";
        }
        StringBuilder sb = new StringBuilder();
        for (Message message : messageList) {
            sb.append(message.getLevel().toString())
              .append(": ")
              .append(message.getMessage())
              .append("; ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private ProcessResult processCreditReduction(LedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);
            List<String> paymentTemplatesForBackDateHoldOverridden = getListOfPaymentTemplatesForOverridingBackDateHold();

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            String lawId = operation.getLaw().getLawId();
            CompanyLaw companyLaw=CompanyLaw.findCompanyLaw(company, lawId);
            if (companyLaw == null) {
                return companyLawDoesNotExist(processResult, operation, lawId);
            }


            if (company.isCompanyOnHold()) {
                processResult.getMessages().GenericWarning(EntityName.Company, company.getSourceCompanyId(), "Company is on hold");
            }

            CompanyAdjustmentSubmissionDTO casDTO = new CompanyAdjustmentSubmissionDTO();
            casDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
            casDTO.setLiabilityAdjustmentDTOs(new ArrayList<LiabilityAdjustmentDTO>());
            casDTO.setIsVoid(false);
            casDTO.setMemo(operation.getMemo());

            LiabilityAdjustmentDTO laDTO = new LiabilityAdjustmentDTO();
            laDTO.setAmount(operation.getAmount());
            laDTO.setEffectiveDate(new DateDTO(operation.getCheckDate()));
            laDTO.setLawId(lawId);
            laDTO.setReconcilingAdjustment(false);
            laDTO.setTaxableWages(operation.getWageAmount() != null ? operation.getWageAmount() : SpcfMoney.ZERO);
            laDTO.setTotalWages(SpcfMoney.ZERO);
            casDTO.getLiabilityAdjustmentDTOs().add(laDTO);

            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
            liabilityAdjustmentOptionsDTO.setForceToRecordFTs(true);

            SpcfCalendar settlementDate = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(settlementDate, 4);
            liabilityAdjustmentOptionsDTO.setSettlementDate(new DateDTO(settlementDate));

            if (paymentTemplatesForBackDateHoldOverridden.contains(companyLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd())) {
                Application.getSessionCache().addNonHibernateObject(MoneyMovementTransaction.IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT, Boolean.TRUE);
            }

            ProcessResult<CompanyAdjustmentSubmission> adjustmentSubmissionProcessResult = PayrollServices.payrollManager.addLiabilityAdjustments(company.getSourceSystemCd(),
                                                                                                                               company.getSourceCompanyId(),
                                                                                                                               null,
                                                                                                                               casDTO,
                                                                                                                               new DateDTO(operation.getCheckDate()),
                                                                                                                               liabilityAdjustmentOptionsDTO);
            processResult.merge(adjustmentSubmissionProcessResult);

            if (processResult.isSuccess()) {

                CompanyEvent.createCreditReductionEvent(company, adjustmentSubmissionProcessResult.getResult().getPayrollRun(), operation.getAmount(), operation.getLaw(), operation.getCheckDate());

                PayrollServices.commitUnitOfWork();
            }

        } finally {
            if (Application.getSessionCache().getNonHibernateObject(MoneyMovementTransaction.IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT) != null) {
                Application.getSessionCache().removeNonHibernateObject(MoneyMovementTransaction.IS_OVEERIDE_BACKDATE_HOLD_FOR_BULK_DEBIT);
            }
            PayrollServices.rollbackUnitOfWork();
        }



        return processResult;
    }
    /**
     * Created Bulk EOQV job for doing same thing as Apply to EOQ Variance Account does on Edit Payement Page on SAP
     * PSP-23504
     */
    private ProcessResult processEndOfQuarterVariance(LedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            if (company.isCompanyOnHold()) {
                processResult.getMessages().GenericWarning(EntityName.Company, company.getSourceCompanyId(), "Company is on hold");
            }

            String lawId = operation.getLaw().getLawId();
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, lawId);
            if (companyLaw == null) {
                return companyLawDoesNotExist(processResult, operation, lawId);
            }

            int defaultBulkEoqvAmountLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_BULK_EOQV_AMOUNT, 500);

            if (SpcfUtils.convertToBigDecimal(operation.getAmount()).doubleValue() > defaultBulkEoqvAmountLimit) {
                processResult.getMessages().GenericError(EntityName.Law, lawId, "The payment amount you have entered is higher than the permissible limit. " +
                        "Enter an amount less than $"+defaultBulkEoqvAmountLimit+" to create the entry.");
                return processResult;
            }

            Law law = Application.find(Law.class, Law.LawId().equalTo(lawId)).getFirst();
            PaymentTemplate paymentTemplate = law.getPaymentTemplate();

            if (paymentTemplate.getAgency().isIRS()) {
                processResult.getMessages().GenericError(EntityName.Law, lawId, "IRS laws are not allowed for EOQV");
                return processResult;
            }

            HashMap<Law, SpcfMoney> lawAmounts = new HashMap<Law, SpcfMoney>();
            lawAmounts.put(law, new SpcfMoney(operation.getAmount()));

            SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(operation.getCheckDate());
            SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(operation.getCheckDate());

            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                    MoneyMovementTransaction.findTaxPayments()
                            .setCompany(company)
                            .setPaymentTemplate(paymentTemplate)
                            .setPeriodBeginDate(quarterBeginDate)
                            .setPeriodEndDate(quarterEndDate)
                            .setTaxPaymentStatuses(TaxPaymentStatus.ATFFinalized)
                            .find();

            if (moneyMovementTransactions.isEmpty()) {
                processResult.getMessages().GenericError(EntityName.MoneyMovementTransaction, company.getSourceCompanyId(), "No ATFFinalized MoneyMovementTransaction found");
                return processResult;
            }

            if (moneyMovementTransactions.size() > 1) {
                processResult.getMessages().GenericError(EntityName.MoneyMovementTransaction, company.getSourceCompanyId(), "More than one MoneyMovementTransaction found");
                return processResult;
            }

            MoneyMovementTransaction mmtId = moneyMovementTransactions.get(0);

            processResult.merge(PayrollServices.paymentManager.adjustSUITaxPayment(mmtId, lawAmounts, false, operation.getMemo()));

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return processResult;
    }

    private ProcessResult processRateUpdate(RateLedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            processResult.merge(PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(),
                                                                                    company.getSourceCompanyId(),
                                                                                    operation.getLaw(),
                                                                                    operation.getCheckDate(),
                                                                                    operation.getRate(),
                                                                                    operation.getPushToQuickBooks()));

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return processResult;
    }

    private ProcessResult processAdditionalAmountUpdate(RateLedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            CompanyFilingAmountDTO companyFilingAmountDTO = new CompanyFilingAmountDTO();
            companyFilingAmountDTO.setName(operation.getAdditionalFilingAmountName());
            companyFilingAmountDTO.setEffectiveDate(new DateDTO(operation.getCheckDate()));
            companyFilingAmountDTO.setAmount(operation.getRate());
            processResult.merge(PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(company.getSourceSystemCd(),
                                                                                              company.getSourceCompanyId(),
                                                                                              companyFilingAmountDTO));

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return processResult;
    }

    private ProcessResult processTOR(LedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            ProcessResult<PayrollRun> torProcessResult = PayrollServices.financialTransactionManager.addTORTransactions(
                    operation.getSourceSystemCode(),
                    operation.getSourceCompanyId(),
                    operation.getLaw().getPaymentTemplate().getPaymentTemplateCd(),
                    operation.getCheckDate());
            processResult.merge(torProcessResult);

            PayrollRun createdPayrollRun = torProcessResult.getResult();
            if (createdPayrollRun != null) {
                SpcfDecimal totalAmount = SpcfMoney.ZERO;
                for (FinancialTransaction financialTransaction : createdPayrollRun.getFinancialTransactionCollection()) {
                    totalAmount = totalAmount.add(financialTransaction.getFinancialTransactionAmount());
                }
                operation.setAmount(new SpcfMoney(totalAmount));
            }

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }


        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return processResult;
    }

    private ProcessResult processDepositFrequencyUpdate(DepositFrequencyLedgerOperation operation) {
        ProcessResult processResult = new ProcessResult();

        try {
            PayrollServices.beginUnitOfWork();
            Application.refresh(operation);

            Company company = Company.findCompany(operation.getSourceCompanyId(), operation.getSourceSystemCode());
            if (company == null) {
                return companyDoesNotExist(processResult, operation);
            }

            EffectiveDepositFrequencyDTO effectiveDepositFrequencyDTO = new EffectiveDepositFrequencyDTO();
            effectiveDepositFrequencyDTO.setPaymentTemplateCd(operation.getLaw().getPaymentTemplate().getPaymentTemplateCd());
            effectiveDepositFrequencyDTO.setAgencyId(operation.getLaw().getPaymentTemplate().getAgency().getAgencyId());
            effectiveDepositFrequencyDTO.setEffectiveDate(operation.getCheckDate());
            effectiveDepositFrequencyDTO.setPaymentFrequencyId(operation.getDepositFrequency());
            processResult.merge(PayrollServices.paymentManager.updateDepositFrequency(company.getSourceSystemCd(),
                                                                                      company.getSourceCompanyId(),
                                                                                      effectiveDepositFrequencyDTO));

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return processResult;
    }

    private static ProcessResult companyDoesNotExist( ProcessResult processResult, LedgerOperation operation ) {

        processResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                                                        operation.getSourceCompanyId(),
                                                        operation.getSourceSystemCode().toString(),
                                                        operation.getSourceCompanyId());
        return processResult;
    }

    public static ProcessResult companyLawDoesNotExist(ProcessResult processResult, LedgerOperation operation, String lawId) {
        processResult.getMessages().CompanyLawDoesNotExist(EntityName.CompanyLaw, operation.getSourceSystemCode().toString(), operation.getSourceCompanyId(), lawId);
        return processResult;
    }

    /**
     *
     * @return
     */
    public static List<String> getListOfPaymentTemplatesForOverridingBackDateHold() {
        List<String> paymentTemplatesForBackDateHoldOverridden = new ArrayList<String>();
        try {
            String paymentTemplates = SystemParameter.findStringValue(SystemParameter.Code.OVERRIDE_BACKDATE_HOLD_FOR_BULK_DEBIT);
            if (paymentTemplates != null) {
                paymentTemplatesForBackDateHoldOverridden = Arrays.asList(paymentTemplates.split(","));
            }
        } catch (Exception e) {
            //Nothing to do
        }
        return paymentTemplatesForBackDateHoldOverridden;
    }


}
