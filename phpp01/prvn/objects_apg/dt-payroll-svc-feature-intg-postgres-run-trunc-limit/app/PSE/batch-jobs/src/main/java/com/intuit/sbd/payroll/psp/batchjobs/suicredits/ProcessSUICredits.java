package com.intuit.sbd.payroll.psp.batchjobs.suicredits;

import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CustomerTaxPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import java.io.IOException;
import java.io.StringWriter;
import org.hibernate.FlushMode;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: dweinberg
 * Date: 9/24/13
 * Time: 1:58 PM
 */
public class ProcessSUICredits {
    private static SpcfLogger logger = PayrollServices.getLogger(ProcessSUICredits.class);

    private int mInterval;
    private int mMinPoolSize;
    private int mMaxPoolSize;
    private int mMaxWait;
    private StringWriter stringWriter = new StringWriter();
    private CSVWriter writer = new CSVWriter(stringWriter);
    private ConcurrentHashMap suiCreditsRecords=new ConcurrentHashMap<String, String>();
    private PSPRequestContextManager pspRequestContextManager;
    
    public ProcessSUICredits() {
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_MIN_THREAD_POOL_SIZE, 10);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.LEDGER_OPERATIONS_MAX_THREAD_POOL_SIZE, 40);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public void process() {
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<SUICreditsJob> suiCreditsJobs = Application.find(SUICreditsJob.class, SUICreditsJob.Status().equalTo(SUICreditsJobStatus.Created));
        PayrollServices.rollbackUnitOfWork();

        for (SUICreditsJob suiCreditsJob : suiCreditsJobs) {
            processJob(suiCreditsJob);
        }

    }

    private void processJob(final SUICreditsJob job) {
        try {
            Application.beginUnitOfWork();
            Application.refresh(job);
            logger.info("Starting job " + job.getId().toString());
            job.setStatus(SUICreditsJobStatus.InProcess);
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }


        ExecutorService threadPool = null;
        try {
            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval,
                                                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(threadPool);
            Application.beginUnitOfWork(FlushMode.MANUAL, true);
            Application.refresh(job);
            SpcfCalendar effectiveDate = CalendarUtils.getFirstDayOfQuarter(job.getYear(), job.getQuarter());

            Criterion<AdditionalFilingAmount> afaWhere = AdditionalFilingAmount.IsSystemAppliedCredit().equalTo(true);
            if (job.getPaymentTemplate() != null) {
                afaWhere = afaWhere.And(AdditionalFilingAmount.PaymentTemplate().equalTo(job.getPaymentTemplate()));
            }

            Set<String> namesForTemplate = new HashSet<String>();
            for (AdditionalFilingAmount additionalFilingAmount : Application.find(AdditionalFilingAmount.class, afaWhere)) {
                namesForTemplate.add(additionalFilingAmount.getName());
            }

            Criterion<CompanyFilingAmount> cfaWhere = CompanyFilingAmount.EffectiveDate().equalTo(effectiveDate)
                                                                         .And(CompanyFilingAmount.InvalidDate().isNull())
                                                                         .And(CompanyFilingAmount.Amount().greaterThan(0.))
                                                                         .And(CompanyFilingAmount.Name().in(namesForTemplate));

            logger.info("Loading company filing amounts for " + Arrays.toString(namesForTemplate.toArray()));
            DomainEntitySet<CompanyFilingAmount> companyFilingAmounts =
                    Application.find(CompanyFilingAmount.class,
                                     new Query<CompanyFilingAmount>().Where(cfaWhere)
                                                                     .OrderBy(CompanyFilingAmount.CompanyAgencyPaymentTemplate().CompanyAgency().Company().SourceCompanyId(), CompanyFilingAmount.Name())
                                                                     .EagerLoad(CompanyFilingAmount.CompanyAgencyPaymentTemplate().CompanyAgency().Company()));
            logger.info("Found " + companyFilingAmounts.size() + " company filing amounts to process");
            companyFilingAmounts.sort();
            List<String> fields = new ArrayList<String>(Arrays.asList("Quarter", "Payment Type", "Law Type Code", "PSID", "Company Name", "Date Processed", "Credit Amount Applied(ATDs)", "Credit Amount ATOs"));
            writer.writeNext(fields.toArray(new String[fields.size()]));
            int numberGroups = 0;
            List<CompanyFilingAmount> companyFilingAmountsForCompany = new ArrayList<CompanyFilingAmount>();
            Application.refresh(job);
            Company lastCompany = null;
            for (CompanyFilingAmount companyFilingAmount : companyFilingAmounts) {
                Company currentCompany = companyFilingAmount.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany();
                if (lastCompany != null && lastCompany != currentCompany) {
                    ++numberGroups;
                    submitFilingAmountsToProcess(completionService, companyFilingAmountsForCompany, job);
                    companyFilingAmountsForCompany = new ArrayList<CompanyFilingAmount>();
                }
                companyFilingAmountsForCompany.add(companyFilingAmount);
                lastCompany = currentCompany;
            }
            if (!companyFilingAmountsForCompany.isEmpty()) {
                ++numberGroups;
                submitFilingAmountsToProcess(completionService, companyFilingAmountsForCompany, job);
            }
            
            logger.info("Queued " + numberGroups + " companies");
            PayrollServices.rollbackUnitOfWork();

            logger.info("Beginning to take");
            for (int i = 0; i < numberGroups; i++) {
                //noinspection EmptyCatchBlock
                try {
                    completionService.take();
                } catch (InterruptedException e) {}
                if ((i + 1) % 100 == 0) {
                    logger.info("Completed " + i + " companies");
                }
            }
            logger.info("Updating job status");
            Application.beginUnitOfWork();
            Application.refresh(job);
            if(!suiCreditsRecords.isEmpty())
            {
                Iterator keys=suiCreditsRecords.keySet().iterator();
                while(keys.hasNext())
                {
                    List values = (List) suiCreditsRecords.get((String)keys.next());
                    Iterator itr=values.iterator();
                    while(itr.hasNext())
                    {
                        writer.writeNext(((String)(itr.next())).split(";"));
                    }
                }
                try {
                    writer.flush();
                } catch (IOException e) {
                    logger.error(e);
                }
                job.setProcessedFile(stringWriter.toString());
            }
            job.setStatus(SUICreditsJobStatus.Complete);
            writer.close();
            stringWriter.close();
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error("Error processing SUI Credits", t);
            try {
                Application.rollbackUnitOfWork();
                Application.beginUnitOfWork();
                Application.refresh(job);
                job.setStatus(SUICreditsJobStatus.Error);
                Application.commitUnitOfWork();
            } catch (Throwable errorThrowable) {
                logger.error("Additionally, an error was received attempting to set the job status to Error", errorThrowable);
            } finally {
                Application.rollbackUnitOfWork();
            }
        } finally {
            Application.rollbackUnitOfWork();
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
            }
        }

    }

    private void submitFilingAmountsToProcess(CompletionService<Boolean> completionService, final List<CompanyFilingAmount> companyFilingAmounts, final SUICreditsJob currentJob) {
        completionService.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.SUICreditsBatchJob);
                for (CompanyFilingAmount companyFilingAmount : companyFilingAmounts) {
                    try {
                        PayrollServices.beginUnitOfWork();
                        Application.refresh(companyFilingAmount);
                        Company company = companyFilingAmount.getCompanyAgencyPaymentTemplate().getCompanyAgency().getCompany();
                        pspRequestContextManager.setRequestContext(company, RequestType.OLAP, BatchJobType.SUICreditsBatchJob.toString());
                        PaymentTemplate paymentTemplate = companyFilingAmount.getCompanyAgencyPaymentTemplate().getPaymentTemplate();
                        int year = companyFilingAmount.getEffectiveDate().getYear();
                        int quarter = CalendarUtils.getQuarterAsInt(companyFilingAmount.getEffectiveDate());

                        Map<String, BigDecimal> paymentAmounts = getPaymentAmounts(company,
                                                                                   paymentTemplate,
                                                                                   new SpcfMoney(SpcfMoney.createInstance(companyFilingAmount.getAmount())),
                                                                                   year,
                                                                                   quarter);
                        logger.info(company.getSourceSystemCompanyId() + " applying amounts: " + paymentAmounts.toString());

                        CustomerTaxPaymentDTO customerTaxPaymentDTO = new CustomerTaxPaymentDTO();
                        customerTaxPaymentDTO.setApplyPayments(true);
                        customerTaxPaymentDTO.setMemo("Agency Credit Used");
                        customerTaxPaymentDTO.setPaymentAmounts(paymentAmounts);
                        customerTaxPaymentDTO.setPaymentDate(new DateDTO(CalendarUtils.getLastDayOfQuarter(companyFilingAmount.getEffectiveDate())));
                        customerTaxPaymentDTO.setPaymentTemplateId(paymentTemplate.getPaymentTemplateCd());
                        customerTaxPaymentDTO.setYear(year);
                        customerTaxPaymentDTO.setQuarter(quarter);
                        customerTaxPaymentDTO.setImmediateCredit(true);

                        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.addCustomerTaxPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), customerTaxPaymentDTO);
                        if (!processResult.isSuccess()) {
                            throw new Exception("Error recording customer payment: " + processResult.toString());
                        }
                        Application.commitUnitOfWork();
                        Application.beginUnitOfWork();
                        
                                
                        SpcfMoney appliedAmount = SpcfMoney.ZERO;
                        for (FinancialTransaction financialTransaction : processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxDebit)) {
                            appliedAmount = new SpcfMoney(appliedAmount.add(financialTransaction.getFinancialTransactionAmount()));
                        }
                        
                        SpcfMoney overpaidAmount = SpcfMoney.ZERO;
                        for (FinancialTransaction financialTransaction : processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxOverpayment)) {
                            overpaidAmount = new SpcfMoney(overpaidAmount.add(financialTransaction.getFinancialTransactionAmount()));
                        }

                        CompanyEvent.createSUICreditsAppliedEvent(company,
                                                                  paymentTemplate.getPrimarySUILaw(),
                                                                  year,
                                                                  quarter,
                                                                  new SpcfMoney(SpcfMoney.createInstance(companyFilingAmount.getAmount())),
                                                                  appliedAmount,
                                                                  processResult.getResult());

                        String fields = new String(Integer.toString(currentJob.getYear())+" Q"+Integer.toString(currentJob.getQuarter())+";"+paymentTemplate.getPaymentTemplateCd()+";"+companyFilingAmount.getName()+";"+company.getSourceCompanyId()+";"+company.getLegalName()+";"+currentJob.getCreatedDate().format("yyyy-MM-dd")+";"+appliedAmount.toString()+";"+overpaidAmount.toString());
                        List<String> records=new ArrayList<String>();
                        if(suiCreditsRecords.containsKey(company.getSourceCompanyId()))
                        {
                            records=(List)(suiCreditsRecords.get(company.getSourceCompanyId()));
                        }
                        records.add(fields);
                        suiCreditsRecords.put(company.getSourceCompanyId(), records);
                        
                        Application.commitUnitOfWork();

                    } catch (Throwable t) {
                        logger.error("Error processing SUI Credits for CFA " + companyFilingAmount.getId().toString(), t);
                    } finally {
                        Application.rollbackUnitOfWork();
                        pspRequestContextManager.clearRequestContext();
                    }
                }

                return true;

            }
        });
    }

    /*
    Return a map of Law IDs to Amounts s.t. money is first applied to pending payments on the SUI law.
      If after applying to the main SUI law, then apply to pending payments for other laws on the template.
      If there is still any money left, then put the remainder on the SUI law (which will create an ATO for that amount).
     */
    protected static Map<String, BigDecimal> getPaymentAmounts(Company company, PaymentTemplate paymentTemplate, SpcfMoney creditAmount, int year, int quarter) {
        //find pending payments for the template to figure out how much of the amount is applied to which law
        DomainEntitySet<MoneyMovementTransaction> pendingPayments =
                MoneyMovementTransaction.findTaxPayments()
                                        .setCompany(company)
                                        .setPaymentTemplate(paymentTemplate)
                                        .setPending()
                                        .setQuarter(year, quarter)
                                        .find();
        Map<Law, SpcfMoney> pendingTransactionAmounts = new HashMap<Law, SpcfMoney>();
        Law suiLaw = paymentTemplate.getPrimarySUILaw();
        //default this one to $0 in case there are no pending payments
        pendingTransactionAmounts.put(suiLaw, SpcfMoney.ZERO);

        for (MoneyMovementTransaction pendingPayment : pendingPayments) {
            Application.refresh(pendingPayment);
            for (FinancialTransaction financialTransaction : pendingPayment.getFinancialTransactionCollection()) {
                SpcfMoney amount = null;
                if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    amount = financialTransaction.getFinancialTransactionAmount();
                } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    amount = new SpcfMoney(financialTransaction.getFinancialTransactionAmount().multiply(SpcfDecimal.createInstance(-1.)));
                }
                if (amount != null) {
                    if (!pendingTransactionAmounts.containsKey(financialTransaction.getLaw())) {
                        pendingTransactionAmounts.put(financialTransaction.getLaw(), SpcfMoney.ZERO);
                    }
                    pendingTransactionAmounts.put(financialTransaction.getLaw(), new SpcfMoney(pendingTransactionAmounts.get(financialTransaction.getLaw()).add(amount)));
                }
            }
        }

        DomainEntitySet<Law> laws = new DomainEntitySet<Law>(pendingTransactionAmounts.keySet());
        Law mainLaw = laws.findEntity(Law.LawCategoryCode().equalTo(LawCategoryCode.UnemploymentEmployer));
        if (mainLaw == null) {
            mainLaw = laws.getFirst();
        }
        Map<String, BigDecimal> paymentAmounts = new HashMap<String, BigDecimal>();
        SpcfDecimal remainingAmount = creditAmount;
        if (remainingAmount.isGreaterThanEqualTo(pendingTransactionAmounts.get(mainLaw))) {
            SpcfMoney mainLawAmount = pendingTransactionAmounts.get(mainLaw);
            remainingAmount = remainingAmount.subtract(pendingTransactionAmounts.get(mainLaw));

            for (Law otherLaw : laws.find(Law.LawId().notEqualTo(mainLaw.getLawId()))) {
                if (remainingAmount.equals(SpcfMoney.ZERO)) {
                    break;
                }

                if (remainingAmount.isGreaterThan(pendingTransactionAmounts.get(otherLaw))) {
                    paymentAmounts.put(otherLaw.getLawId(), new BigDecimal(pendingTransactionAmounts.get(otherLaw).toString()));
                    remainingAmount = remainingAmount.subtract(pendingTransactionAmounts.get(otherLaw));
                } else {
                    paymentAmounts.put(otherLaw.getLawId(), new BigDecimal(remainingAmount.toString()));
                    remainingAmount = SpcfMoney.ZERO;
                }
            }

            mainLawAmount = new SpcfMoney(mainLawAmount.add(remainingAmount));

            paymentAmounts.put(mainLaw.getLawId(), new BigDecimal(mainLawAmount.toString()));
        } else {
            paymentAmounts.put(mainLaw.getLawId(), new BigDecimal(remainingAmount.toString()));
        }

        return paymentAmounts;
    }

}
