package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: dweinberg
 * Date: 7/19/11
 * Time: 3:24 PM
 */
public class ComplianceToolkit {

    protected static final SpcfLogger logger = Application.getLogger(ComplianceToolkit.class);

    public static void main(String[] args) {
        ExecutorService executor = null;

        try {
            String command;
            String paymentTemplateCd = null;
            String sourceSystemCd = null;
            String sourceCompanyId = null;

            if (args.length == 1) {
                //process all templates across all companies
                command = args[0];
            } else if (args.length == 2) {
                //process specified template across all companies
                command = args[0];
                paymentTemplateCd = args[1];
            } else if (args.length == 3) {
                //process all templates for specified company
                command = args[0];
                sourceSystemCd = args[1];
                sourceCompanyId = args[2];
            } else if (args.length == 4) {
                //process specified template for specified company
                command = args[0];
                paymentTemplateCd = args[1];
                sourceSystemCd = args[2];
                sourceCompanyId = args[3];
            } else {
                throw new Exception("usage");
            }

            Application.initialize();
            ApplicationSecondary.initialize();
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            PaymentTemplate paymentTemplate = null;
            Company company = null;

            if (paymentTemplateCd != null && !paymentTemplateCd.equalsIgnoreCase("null")) {
                paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
                if (paymentTemplate == null) {
                    throw new Exception("Payment template " + paymentTemplateCd + " not found");
                }
            }
            if (sourceCompanyId != null) {
                company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));
                if (company == null) {
                    throw new Exception(String.format("Company %s:%s not found", sourceSystemCd, sourceCompanyId));
                }
            }

            PayrollServices.rollbackUnitOfWork();

            ToolkitCommand toolkitCommand = ToolkitCommand.valueOf(command);

            int processors = Runtime.getRuntime().availableProcessors();
            int threadCount = processors * (2);
            executor = Executors.newFixedThreadPool(threadCount);

            switch (toolkitCommand) {
                case AddCompanyPaymentMethods:
                    /* scenarios:
                        new payment method added to payment template
                        migration
                    */
                    addCompanyPaymentMethods(paymentTemplate, company, executor);
                    break;
                case RecalculateCompanyPaymentMethodsEnabled:
                    /* scenarios:
                        payment method deleted
                        new requirement
                        changed requirement
                    */
                    recalculateCompanyPaymentMethodsEnabled(paymentTemplate, company, executor);
                    break;
                case UpdatePaymentMethodOnPendingPayments:
                    /* scenarios:
                        no great reason, but perhaps we had to disable a payment method manually
                    */
                    updatePaymentMethodOnPendingPayments(paymentTemplate, company, executor);
                    break;
                case RecreateEntryDetailRecords:
                    /* scenarios:
                        TXP record generation changes format but they have already been generated
                     */
                    recreateEntryDetailRecords(paymentTemplate, company, executor);
                    break;
                case UpdateBankAccountsOnPendingPayments:
                    /* scenarios:
                       Agency bank account changes
                     */
                    updateBankAccountsOnPendingPayments(paymentTemplate, company, executor);
                    break;
            }
        } catch (Throwable t) {
            if (t.getMessage() != null && t.getMessage().equals("usage")) {
                usage();
            } else {
                t.printStackTrace();
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }



    }

    private static void addCompanyPaymentMethods(PaymentTemplate paymentTemplate, Company company, ExecutorService executor) throws InterruptedException, ExecutionException {

        logger.info("adding company payment methods");

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        List captIds = getCompanyAgencyPaymentTemplateIds(paymentTemplate, company);
        PayrollServices.rollbackUnitOfWork();

        logger.info("got " + captIds.size() + " company agency payment templates to process");

        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);

        for (final Object captIdObject : captIds) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean added = false;
                    try {
                        Application.initialize();
                        ApplicationSecondary.initialize();
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        CompanyAgencyPaymentTemplate capt = Application.findById(CompanyAgencyPaymentTemplate.class, (SpcfUniqueId)captIdObject);
                        for (PaymentTemplatePaymentMethod paymentMethod : capt.getPaymentTemplate().getPaymentTemplatePaymentMethods()) {
                            if (capt.getCompanyPaymentTemplatePaymentMethod(paymentMethod.getPaymentMethod()) == null) {
                                CompanyPaymentTemplatePaymentMethod.createNewCompanyPaymentTemplatePaymentMethod(paymentMethod, capt);
                                added = true;
                            }
                        }
                        PayrollServices.commitUnitOfWork();
                    } catch (Throwable t) {
                        logger.error("Error adding company payment methods for " + captIdObject.toString(), t);
                        return false;
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                    return added;
                }
            });
        }

        int templatesWithMethodsAdded=0;
        int total=0;
        //noinspection UnusedDeclaration
        for (Object captId : captIds) {
            Future<Boolean> f = completionService.take();
            total++;
            if (f.get()) {
                templatesWithMethodsAdded++;
            }
            if (total % 1000 == 0) {
                logger.info("Modified " + templatesWithMethodsAdded + " of " + total + " templates");
            }
        }


        logger.info("completed adding company payment methods: " + templatesWithMethodsAdded + " of " + captIds.size());
    }

    public static void  recalculateCompanyPaymentMethodsEnabled(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        logger.info("recalculating company payment methods");

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        List captIds = getCompanyAgencyPaymentTemplateIds(paymentTemplate, company);
        PayrollServices.rollbackUnitOfWork();

        logger.info("got " + captIds.size() + " company agency payment templates to process");

        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);

        for (final Object captIdObject : captIds) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean changed = false;
                    try {
                        Application.initialize();
                        ApplicationSecondary.initialize();
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        CompanyAgencyPaymentTemplate capt = Application.findById(CompanyAgencyPaymentTemplate.class, (SpcfUniqueId)captIdObject);
                        changed = capt.recalculatePaymentMethods();
                        PayrollServices.commitUnitOfWork();
                    } catch (Throwable t) {
                        logger.error("Error recalculating company payment methods for " + captIdObject.toString(), t);
                        return false;
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                    return changed;
                }
            });
        }

        int templatesWithMethodsRecalculated=0;
        int total=0;
        //noinspection UnusedDeclaration
        for (Object captId : captIds) {
            Future<Boolean> f = completionService.take();
            total++;
            if (f.get()) {
                templatesWithMethodsRecalculated++;
            }
            if (total % 1000 == 0) {
                logger.info("Modified " + templatesWithMethodsRecalculated + " of " + total + " templates");
            }
        }

        logger.info("completed recalculating company payment methods: " + templatesWithMethodsRecalculated + " of " + captIds.size());
    }

    public static void updatePaymentMethodOnPendingPayments(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        logger.info("recalculating payment method on pending payments");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, null);
        PayrollServices.rollbackUnitOfWork();

        logger.info("got " + payments.size() + " pending payments to process");

        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);

        for (final SpcfUniqueId pendingPaymentId : payments) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean changed = false;
                    try {
                        Application.initialize();
                        ApplicationSecondary.initialize();
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);

                        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pendingPayment.getCompany(), pendingPayment.getPaymentTemplate());
                        changed = companyAgencyPaymentTemplate.recalculatePaymentMethods(pendingPayment);

                        PayrollServices.commitUnitOfWork();
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                    return changed;
                }
            });
        }

        int recalculatedPayments=0;
        int total=0;
        //noinspection UnusedDeclaration
        for (SpcfUniqueId payment : payments) {
            Future<Boolean> f = completionService.take();
            total++;
            if (f.get()) {
                recalculatedPayments++;
            }
            if (total % 1000 == 0) {
                logger.info("Recalculated " + recalculatedPayments + " of " + total + " payments");
            }
        }

        logger.info("completed recalculating pending payment methods: " + recalculatedPayments + " of " + payments.size());
    }

    private static void recreateEntryDetailRecords(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        logger.info("recreating entry detail records");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, PaymentMethod.ACHCredit);
        PayrollServices.rollbackUnitOfWork();

        logger.info("got " + payments.size() + " pending ACH Credit payments to process");

        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);

        for (final SpcfUniqueId pendingPaymentId : payments) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean changed = false;
                    try {
                        Application.initialize();
                        ApplicationSecondary.initialize();
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);

                        EntryDetailRecord oldCreditEDR = pendingPayment.getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).getFirst();

                        MoneyMovementTransaction.recreateEntryDetailRecords(pendingPayment);

                        EntryDetailRecord newCreditEDR = pendingPayment.getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).getFirst();

                        //only commit if record data actually changed
                        if (oldCreditEDR != null && newCreditEDR != null) {
                            if (!StringUtils.equals(oldCreditEDR.getTxpRecordData(), newCreditEDR.getTxpRecordData()) ||
                                    !StringUtils.equals(oldCreditEDR.getRecordData(), newCreditEDR.getRecordData())) {
                                changed = true;
                                PayrollServices.commitUnitOfWork();
                            }
                        } else {
                            logger.error("Inconsistent EDRs on MMT " + pendingPayment.getId().toString());
                        }

                        //normal case should only commit if we can tell which data is changing and it is expected
                        //however, if this is set, will recreate and commit regardless, like if there is some other problem we are correcting
                        if (StringUtils.equals(System.getProperty("force.commit"), "true")) {
                            PayrollServices.commitUnitOfWork();
                            changed = true;
                        }

                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                    return changed;
                }
            });
        }

        int recreatedPayments=0;
        int total=0;
        //noinspection UnusedDeclaration
        for (SpcfUniqueId payment : payments) {
            Future<Boolean> f = completionService.take();
            total++;
            if (f.get()) {
                recreatedPayments++;
            }
            if (total % 1000 == 0) {
                logger.info("Recreated " + recreatedPayments + " of " + total + " payments");
            }
        }

        logger.info("completed recreating EDRs: " + recreatedPayments + " of " + payments.size());
    }

    private static void updateBankAccountsOnPendingPayments(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        logger.info("updating bank accounts on pending payments");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, null);
        PayrollServices.rollbackUnitOfWork();

        logger.info("got " + payments.size() + " pending payments to process");

        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);

        for (final SpcfUniqueId pendingPaymentId : payments) {
            completionService.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean changed = false;
                    try {
                        Application.initialize();
                        ApplicationSecondary.initialize();
                        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ComplianceToolkit));
                        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);

                        for (FinancialTransaction financialTransaction : pendingPayment.getFinancialTransactionCollection()) {
                            BankAccount oldCreditBankAccount = financialTransaction.getCreditBankAccount();
                            BankAccount oldDebitBankAccount = financialTransaction.getDebitBankAccount();
                            //this is where the bank account is actually set--does not require it to actually change.
                            financialTransaction.updateSettlementType(financialTransaction.getSettlementTypeCd());
                            changed = !ObjectUtils.equals(oldCreditBankAccount, financialTransaction.getCreditBankAccount())|| !ObjectUtils.equals(oldDebitBankAccount, financialTransaction.getDebitBankAccount());
                        }
                        PayrollServices.commitUnitOfWork();
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                    }
                    return changed;
                }
            });
        }

        int updatedPayments=0;
        int total=0;
        //noinspection UnusedDeclaration
        for (SpcfUniqueId payment : payments) {
            Future<Boolean> f = completionService.take();
            total++;
            if (f.get()) {
                updatedPayments++;
            }
            if (total % 1000 == 0) {
                logger.info("Updated " + updatedPayments + " of " + total + " payments");
            }
        }

        logger.info("completed updating bank accounts: " + updatedPayments + " of " + payments.size());
    }

    //List<SpcfUniqueId>
    private static List getCompanyAgencyPaymentTemplateIds(PaymentTemplate paymentTemplate, Company company) {
        String hql = "select capt.Id " +
                "from com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate capt ";
        if (paymentTemplate != null || company != null) {
            hql += "where ";
        }
        if (paymentTemplate != null) {
            hql += "capt.PaymentTemplate = :paymentTemplate ";
        }
        if (company != null) {
            if (paymentTemplate != null) {
                hql += "and ";
            }
            hql += "capt.CompanyAgency.Company = :company";
        }

        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(hql);

        if (paymentTemplate != null) {
            //noinspection JpaQueryApiInspection
            hibernateQuery.setParameter("paymentTemplate", paymentTemplate);
        }
        if (company != null) {
            //noinspection JpaQueryApiInspection
            hibernateQuery.setParameter("company", company);
        }

        return hibernateQuery.list();
    }

    private static List<SpcfUniqueId> getMoneyMovementIds(PaymentTemplate paymentTemplate, Company company, PaymentMethod paymentMethod) {
        HqlBuilder hql = new HqlBuilder(true, "select mmt.Id from com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction mmt");
        hql.append("where mmt.TaxPaymentStatus in (:taxPaymentStatuses)").setParameterList("taxPaymentStatuses", TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.OnHold);

        if (paymentMethod != null) {
            hql.append("and mmt.MoneyMovementPaymentMethod = :paymentMethod");
            hql.setParameter("paymentMethod", paymentMethod);
        }

        if (paymentTemplate == null) {
            hql.append("and mmt.PaymentTemplate is not null");
        } else {
            hql.append("and mmt.PaymentTemplate = :paymentTemplate");
            hql.setParameter("paymentTemplate", paymentTemplate);
        }

        if (company != null) {
            hql.append("and mmt.Company = :company");
            hql.setParameter("company", company);
        }

        return hql.list();
    }

    public static enum ToolkitCommand {
        AddCompanyPaymentMethods,
        RecalculateCompanyPaymentMethodsEnabled,
        UpdatePaymentMethodOnPendingPayments,
        RecreateEntryDetailRecords,
        UpdateBankAccountsOnPendingPayments
    }

    private static void usage() {
        System.out.println("Usage: ComplianceToolkit <Command> [<Payment Template Code>] [<Source System Cd> <Source Company Id>]");
        System.out.println("Valid commands are " + Arrays.toString(ToolkitCommand.values()));
    }

}
