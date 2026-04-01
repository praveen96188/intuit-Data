package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.tools.ComplianceToolkit;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;



@ScheduledJob(name = "ComplianceToolKit", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class ComplianceToolKitProcessor extends JSSBatchJob {


    private static String methodList = null;

    private static final String METHOD = "-method";
    private static final String PAYMENTTEMPLATECD = "-paymentTemplateCd";
    private static final String SOURCESYSTEMSOURCECOMPANYID = "-SourceSystemSourceCompanyId";
    private List<ToolkitCommand> toolkitCommands = null;
    private PaymentTemplate paymentTemplate = null;
    private Company company = null;
    ExecutorService executor = null;

    private PSPRequestContextManager pspRequestContextManager;

    public ComplianceToolKitProcessor(String[] pArguments) {
        super(pArguments);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public ComplianceToolKitProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    @Override
    protected void validateRuntimeParameters() {

        try {
            Application.initialize();
            ApplicationSecondary.initialize();
            PayrollServices.beginUnitOfWork();

            SpcfCalendar now = PSPDate.getPSPTime();
            String commandLine = getJobInstanceParameters().trim();
            if (commandLine.length() > 0) {
                String[] args = commandLine.split(" ");
                for (String arg : args) {
                    String[] argParts = arg.split(":");
                    if (argParts.length == 2) {
                        if (argParts[0].equals(METHOD)) {
                            methodList = argParts[1];
                            String[] argsList = methodList.split(",");
                            toolkitCommands=new ArrayList<>();
                            for (String argMethod : argsList){
                                ToolkitCommand toolkitCommand=null;
                                try{
                                    toolkitCommand = ToolkitCommand.valueOf(argMethod);
                                }
                                catch(IllegalArgumentException e){
                                    throw new RuntimeException("Method "+argMethod+" not found");
                                }
                                toolkitCommands.add(toolkitCommand);
                            }

                        }
                        else if (argParts[0].equals(PAYMENTTEMPLATECD)){
                            String paymentTemplateCd=argParts[1];

                            if (paymentTemplateCd != null && !paymentTemplateCd.equalsIgnoreCase("null")) {
                                paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
                                if (paymentTemplate == null) {
                                    throw new RuntimeException("Payment template " + paymentTemplateCd + " not found");
                                }
                            }


                        }
                        else if (argParts[0].equals(SOURCESYSTEMSOURCECOMPANYID)){
                            String sourceCompanyIdAndCode[]=argParts[1].split(",");
                            if(sourceCompanyIdAndCode.length>1){
                                String sourceCompanyId=sourceCompanyIdAndCode[0];
                                String sourceSystemCd=sourceCompanyIdAndCode[1];

                                company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

                                if (company == null) {
                                    throw new RuntimeException(String.format("Company %s:%s not found", sourceSystemCd, sourceCompanyId));
                                }



                            }else {
                                throw new RuntimeException("Arguments are not sufficient");
                            }

                        }

                    } else {
                        throw new RuntimeException("Invalid argument: " + arg);
                    }


                }

            }


        }finally {
            PayrollServices.rollbackUnitOfWork();
        }


    }

    @Override
    public void execute() {


        try {

        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * (2);
        executor = Executors.newFixedThreadPool(threadCount);



        if (null != toolkitCommands && !toolkitCommands.isEmpty()) {


            for(ToolkitCommand toolkitCommand:toolkitCommands){
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
            }

        }
    }
        catch (Throwable t){
            getLogger().info(t.getMessage());
        }
        finally {
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }
        }

    private void addCompanyPaymentMethods(PaymentTemplate paymentTemplate, Company company, ExecutorService executor) throws InterruptedException, ExecutionException {

        getLogger().info("adding company payment methods");

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        List captIds = getCompanyAgencyPaymentTemplateIds(paymentTemplate, company);
        PayrollServices.rollbackUnitOfWork();

        getLogger().info("got " + captIds.size() + " company agency payment templates to process");

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
                        getLogger().error("Error adding company payment methods for " + captIdObject.toString(), t);
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
                getLogger().info("Modified " + templatesWithMethodsAdded + " of " + total + " templates");
            }
        }


        getLogger().info("completed adding company payment methods: " + templatesWithMethodsAdded + " of " + captIds.size());
    }

    private   void  recalculateCompanyPaymentMethodsEnabled(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        getLogger().info("recalculating company payment methods");

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        List captIds = getCompanyAgencyPaymentTemplateIds(paymentTemplate, company);
        PayrollServices.rollbackUnitOfWork();

        getLogger().info("got " + captIds.size() + " company agency payment templates to process");

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
                        getLogger().error("Error recalculating company payment methods for " + captIdObject.toString(), t);
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
                getLogger().info("Modified " + templatesWithMethodsRecalculated + " of " + total + " templates");
            }
        }

        getLogger().info("completed recalculating company payment methods: " + templatesWithMethodsRecalculated + " of " + captIds.size());
    }

    private   void updatePaymentMethodOnPendingPayments(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        getLogger().info("recalculating payment method on pending payments");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, null);
        PayrollServices.rollbackUnitOfWork();

        getLogger().info("got " + payments.size() + " pending payments to process");

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
                        pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "ComplianceToolKit");
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);
                        pspRequestContextManager.setRequestContextCompany(pendingPayment.getCompany());

                        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pendingPayment.getCompany(), pendingPayment.getPaymentTemplate());
                        changed = companyAgencyPaymentTemplate.recalculatePaymentMethods(pendingPayment);

                        PayrollServices.commitUnitOfWork();
                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                        pspRequestContextManager.clearRequestContext();
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
                getLogger().info("Recalculated " + recalculatedPayments + " of " + total + " payments");
            }
        }

        getLogger().info("completed recalculating pending payment methods: " + recalculatedPayments + " of " + payments.size());
    }

    private  void recreateEntryDetailRecords(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        getLogger().info("recreating entry detail records");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, PaymentMethod.ACHCredit);
        PayrollServices.rollbackUnitOfWork();

        getLogger().info("got " + payments.size() + " pending ACH Credit payments to process");

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
                        pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "ComplianceToolKit");
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);
                        pspRequestContextManager.setRequestContextCompany(pendingPayment.getCompany());

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
                            getLogger().error("Inconsistent EDRs on MMT " + pendingPayment.getId().toString());
                        }

                        //normal case should only commit if we can tell which data is changing and it is expected
                        //however, if this is set, will recreate and commit regardless, like if there is some other problem we are correcting
                        if (StringUtils.equals(System.getProperty("force.commit"), "true")) {
                            PayrollServices.commitUnitOfWork();
                            changed = true;
                        }

                    } finally {
                        PayrollServices.rollbackUnitOfWork();
                        pspRequestContextManager.clearRequestContext();
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
                getLogger().info("Recreated " + recreatedPayments + " of " + total + " payments");
            }
        }

        getLogger().info("completed recreating EDRs: " + recreatedPayments + " of " + payments.size());
    }

    private  void updateBankAccountsOnPendingPayments(PaymentTemplate paymentTemplate, Company company, Executor executor) throws InterruptedException, ExecutionException {
        getLogger().info("updating bank accounts on pending payments");

        Application.beginUnitOfWork(FlushMode.MANUAL);
        if (company != null) {
            Application.refresh(company);
        }
        List<SpcfUniqueId> payments = getMoneyMovementIds(paymentTemplate, company, null);
        PayrollServices.rollbackUnitOfWork();

        getLogger().info("got " + payments.size() + " pending payments to process");

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
                        pspRequestContextManager.setRequestContext(company, RequestType.OLAP, "ComplianceToolKit");
                        MoneyMovementTransaction pendingPayment = Application.findById(MoneyMovementTransaction.class, pendingPaymentId);
                        pspRequestContextManager.setRequestContextCompany(pendingPayment.getCompany());

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
                        pspRequestContextManager.clearRequestContext();
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
                getLogger().info("Updated " + updatedPayments + " of " + total + " payments");
            }
        }

        getLogger().info("completed updating bank accounts: " + updatedPayments + " of " + payments.size());
    }

    //List<SpcfUniqueId>
    private  List getCompanyAgencyPaymentTemplateIds(PaymentTemplate paymentTemplate, Company company) {
        String hql = "select capt.Id " +
                "from com.intuit.sbd.payroll.psp.domain.CompanyAgencyPaymentTemplate capt ";
        if (paymentTemplate != null || company != null) {
            hql += "where ";
        }

        if(Company.isDGDeleteFeatureEnabled()){
            hql += " capt.CompanyAgency.Company.IsDgDisassociated = 0 and ";
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

        if(Company.isDGDeleteFeatureEnabled()){
            hql.append(" and mmt.Company.IsDgDisassociated = 0 ");
        }

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

    private void usage() {
        getLogger().info("Usage: ComplianceToolkit <Command> [<Payment Template Code>] [<Source System Cd> <Source Company Id>]");
        getLogger().info("Valid commands are " + Arrays.toString(ComplianceToolkit.ToolkitCommand.values()));
    }

}

