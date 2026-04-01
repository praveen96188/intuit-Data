package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.PrintConstants;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 30, 2011
 * Time: 1:44:52 PM
 */
public class PrintedChecksSelector {
    private static SpcfLogger logger = Application.getLogger(PrintedChecksSelector.class);
    private DecimalFormat mCheckNumberFormatter = new DecimalFormat(PrintConstants.CHECK_NUMBER_FORMAT);

    //Field separator for GA check payments report
    private static final String SEPARATOR = ",";

    private long mCheckNumber;
    final private static String GA_DOL_PAYMENT="GA-DOL4-PAYMENT";
    final private static String NV_NUC_PAYMENT="NV-NUCS4072-PAYMENT";
    private static String SORT_BY_AGENCY_ID_PMT_TEMPLATES []={GA_DOL_PAYMENT,NV_NUC_PAYMENT};
    List sortByAgencyIdPaymentTemplatesList = null;

    private PSPRequestContextManager pspRequestContextManager;
    public PrintedChecksSelector(){
        sortByAgencyIdPaymentTemplatesList = Arrays.asList(SORT_BY_AGENCY_ID_PMT_TEMPLATES);
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }
    public void processCheckBatchSelection(PaymentMethod paymentMethod) {
        try {
            logger.info("processCheckBatchSelection started for payment method " + paymentMethod);
            StopWatch timer = StopWatch.startTimer();

            PayrollServices.beginUnitOfWork();

            long batchSize=0;
            TaxPaymentStatus[] paymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ATFFinalized};
            if (paymentMethod == PaymentMethod.CheckPayment) {
                batchSize = SystemParameter.findLongValue(SystemParameter.Code.PRINTED_CHECKS_BATCH_SIZE, 500);
                paymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.ATFFinalized};
            }

            SpcfCalendar initiationDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(initiationDate);
            int rowsUpdated = MoneyMovementTransaction.markPaymentsInProcessForDate(paymentMethod,
                                                                                    initiationDate,
                                                                                    null,
                                                                                    0,
                                                                                    paymentStatuses);

            // For SuperCheck payments, also look for Zero Dollar Ready To Send payments since these are never finalized by the UI.
            if (paymentMethod == PaymentMethod.SuperCheck) {
                rowsUpdated += MoneyMovementTransaction.markPaymentsInProcessForDate(paymentMethod,
                                                                                        initiationDate,
                                                                                        SpcfMoney.ZERO,
                                                                                        0);
            }

            PayrollServices.commitUnitOfWork();

            logger.info("updated " + rowsUpdated + " payment MoneyMovementTransactions to status InProcess for payment method " + paymentMethod + " in " + timer.getElapsedTimeString());

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            DomainEntitySet<MoneyMovementTransaction> agencyChecksToPrint;

            mCheckNumber = SystemParameter.findLongValue(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER);

            CalendarUtils.clearTime(initiationDate);
            if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
                agencyChecksToPrint = Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>()
                        .Where(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate)
                                .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(paymentMethod))
                                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.InProcess)))
                        .OrderBy(MoneyMovementTransaction.PaymentTemplate())
                        .EagerLoad(MoneyMovementTransaction.QbdtTransactionInfo().Company().equalTo(MoneyMovementTransaction.Company())));
            } else {
                agencyChecksToPrint = Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>()
                        .Where(MoneyMovementTransaction.InitiationDate().equalTo(initiationDate)
                                .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(paymentMethod))
                                .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.InProcess)))
                        .OrderBy(MoneyMovementTransaction.PaymentTemplate()));
            }

            createPrintBatches(agencyChecksToPrint, batchSize, paymentMethod == PaymentMethod.SuperCheck);

            SystemParameter.update(SystemParameter.Code.PRINTED_CHECKS_NEXT_CHECK_NUMBER, Long.toString(mCheckNumber));

            PayrollServices.commitUnitOfWork();
            logger.info("processCheckBatchSelection finished for payment method " + paymentMethod + ". Total checks selected: " + agencyChecksToPrint.size() + " Elapsed time: " + timer.getElapsedTimeString());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    /**
     * Creates AgencyCheckBatches and adds them to accounting files
     * @param pAgencyChecksToPrint checks to be included (or if zero, marked as processed)
     * @param pBatchSize maximum number of checks per batch, or 0 if no maximum
     * @param pIsSuperCheck sets flag on batch, also used to determine whether the check number is unique per payment template (otherwise is unique per each payment)
     */
    private void createPrintBatches(DomainEntitySet<MoneyMovementTransaction> pAgencyChecksToPrint, long pBatchSize, boolean pIsSuperCheck) {
        // update zero dollar payments to complete
        DomainEntitySet<MoneyMovementTransaction> zeroDollarPayments =
                pAgencyChecksToPrint.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO));
        for (MoneyMovementTransaction zeroDollarPayment : zeroDollarPayments) {
            try {
                pspRequestContextManager.setRequestContextCompany(zeroDollarPayment.getCompany());
                zeroDollarPayment.updateTaxPaymentStatus(TaxPaymentStatus.SentToAgency, false, true);
                zeroDollarPayment.updateTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency, true, true);
                Application.save(zeroDollarPayment);

                pAgencyChecksToPrint.remove(zeroDollarPayment);
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }

        }

        //Log error for negative dollar payments and remove them from list to prevent printing
        DomainEntitySet<MoneyMovementTransaction> negativePayments =
                pAgencyChecksToPrint.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().lessThan(SpcfMoney.ZERO));
        for (MoneyMovementTransaction negativePayment : negativePayments) {

            logger.error(String.format("Found negative payment in check printing process. Details - MMT Id: %s, Payment Template: %s, Payment amount: %s",
                                                negativePayment.getId().toString(), negativePayment.getPaymentTemplate().getPaymentTemplateCd(), negativePayment.getMoneyMovementTransactionAmount()));

            pAgencyChecksToPrint.remove(negativePayment);
        }

        if(pAgencyChecksToPrint.isEmpty()) {
            return;
        }

        DomainEntitySet<AccountingReportFile> printedCheckFiles = AccountingReportFile.findByTypeAndStatus(AccountingReportFileType.PositivePay, AccountingReportFileStatus.New, false);
        AccountingReportFile positivePayFile;
        if(!printedCheckFiles.isEmpty()) {
            positivePayFile = printedCheckFiles.get(0);
        } else {
            positivePayFile = new AccountingReportFile();
            positivePayFile.setType(AccountingReportFileType.PositivePay);
            positivePayFile.setStatus(AccountingReportFileStatus.New);
            positivePayFile = Application.save(positivePayFile);
        }

        printedCheckFiles = AccountingReportFile.findByTypeAndStatus(AccountingReportFileType.PrintedCheckReconPlus, AccountingReportFileStatus.New, false);
        AccountingReportFile reconPlusFile;
        if(!printedCheckFiles.isEmpty()) {
            reconPlusFile = printedCheckFiles.get(0);
        } else {
            reconPlusFile = new AccountingReportFile();
            reconPlusFile.setType(AccountingReportFileType.PrintedCheckReconPlus);
            reconPlusFile.setStatus(AccountingReportFileStatus.New);
            reconPlusFile = Application.save(reconPlusFile);
        }

        Map<PaymentTemplateQuarter, DomainEntitySet<MoneyMovementTransaction>> paymentTemplateMap = new TreeMap<PaymentTemplateQuarter, DomainEntitySet<MoneyMovementTransaction>>();
        for (MoneyMovementTransaction moneyMovementTransaction : pAgencyChecksToPrint) {
            PaymentTemplateQuarter paymentTemplateQuarter = new PaymentTemplateQuarter(
                    moneyMovementTransaction.getPaymentTemplate(),
                    moneyMovementTransaction.getPaymentPeriodEnd().getYear(),
                    CalendarUtils.getQuarterAsInt(moneyMovementTransaction.getPaymentPeriodEnd()));
            if(!paymentTemplateMap.containsKey(paymentTemplateQuarter)) {
                paymentTemplateMap.put(paymentTemplateQuarter, new DomainEntitySet<MoneyMovementTransaction>());
            }
            paymentTemplateMap.get(paymentTemplateQuarter).add(moneyMovementTransaction);
        }

        AgencyCheckBatch agencyCheckBatch;
        for (Map.Entry<PaymentTemplateQuarter, DomainEntitySet<MoneyMovementTransaction>> paymentTemplateEntry : paymentTemplateMap.entrySet()){
            PaymentTemplate paymentTemplate = paymentTemplateEntry.getKey().paymentTemplate;
            if(!PaymentTemplatePrintedCheckInfo.paymentTemplateHasPrintedCheckInfo(paymentTemplate)) {
                logger.error("Skipping payments because printed check info does not exist for payment template: " + paymentTemplate.getPaymentTemplateCd());
                continue;
            }

            DomainEntitySet<MoneyMovementTransaction> payments = paymentTemplateEntry.getValue();
            if(!isSortByAgencyIdPaymentTemplate(paymentTemplate.getPaymentTemplateCd())){
                payments = payments.sort(MoneyMovementTransaction.Company().SourceCompanyId());
            }

            agencyCheckBatch = null;

            long count = 0;
            for (MoneyMovementTransaction payment : payments) {
                try {
                    pspRequestContextManager.setRequestContextCompany(payment.getCompany());
                    if((pBatchSize > 0 && count % pBatchSize == 0) || agencyCheckBatch == null) {
                        if(agencyCheckBatch != null) {
                            finalizeAgencyCheckBatch(agencyCheckBatch);
                        }

                        agencyCheckBatch = new AgencyCheckBatch();
                        agencyCheckBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.Pending);
                        agencyCheckBatch.setPaymentTemplate(paymentTemplate);
                        agencyCheckBatch.setPositivePayFile(positivePayFile);
                        agencyCheckBatch.setSuperCheck(pIsSuperCheck);
                        positivePayFile.addPositivePayFileBatches(agencyCheckBatch);
                        agencyCheckBatch.setReconPlusFile(reconPlusFile);
                        reconPlusFile.addReconPlusFileBatches(agencyCheckBatch);
                        agencyCheckBatch = Application.save(agencyCheckBatch);
                    }

                    // add it to the batch
                    PaymentBatchAssoc paymentBatchAssoc = new PaymentBatchAssoc();
                    paymentBatchAssoc.setCompany(payment.getCompany());
                    paymentBatchAssoc.setAgencyCheckBatch(agencyCheckBatch);
                    agencyCheckBatch.addPaymentBatchAssoc(paymentBatchAssoc);
                    paymentBatchAssoc.setMoneyMovementTransaction(payment);
                    Application.save(paymentBatchAssoc);

                    payment.setReferenceNumber(mCheckNumberFormatter.format(mCheckNumber));
                    if (!pIsSuperCheck) {
                        ++mCheckNumber;
                    }
                    Application.save(payment);

                    count++;
                } finally {
                    pspRequestContextManager.clearRequestContextCompany();
                }
            }

            createEmailReportForGASUI(payments);

            if(agencyCheckBatch != null) {
                finalizeAgencyCheckBatch(agencyCheckBatch);
            }

            if (pIsSuperCheck) {
                ++mCheckNumber;
            }

        }
    }

    private boolean isSortByAgencyIdPaymentTemplate(String pPaymentTemplateCd) {
        if(pPaymentTemplateCd == null ){
            return false;
        }
        if(sortByAgencyIdPaymentTemplatesList !=null && sortByAgencyIdPaymentTemplatesList.contains(pPaymentTemplateCd))  {
            return true;
        }
        return false;
    }

    public void finalizeAgencyCheckBatch(AgencyCheckBatch agencyCheckBatch) {
        agencyCheckBatch.setNumberOfChecks(agencyCheckBatch.getPaymentBatchAssocCollection().size());
        Application.save(agencyCheckBatch);
    }

    private class PaymentTemplateQuarter implements Comparable {
        public PaymentTemplate paymentTemplate;
        public int year;
        public int quarter;

        private PaymentTemplateQuarter(PaymentTemplate paymentTemplate, int year, int quarter) {
            this.paymentTemplate = paymentTemplate;
            this.year = year;
            this.quarter = quarter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PaymentTemplateQuarter that = (PaymentTemplateQuarter) o;

            if (quarter != that.quarter) return false;
            if (year != that.year) return false;
            //noinspection RedundantIfStatement
            if (paymentTemplate != null ? !paymentTemplate.equals(that.paymentTemplate) : that.paymentTemplate != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = paymentTemplate != null ? paymentTemplate.hashCode() : 0;
            result = 31 * result + year;
            result = 31 * result + quarter;
            return result;
        }

        public int compareTo(Object o) {
            PaymentTemplateQuarter other = (PaymentTemplateQuarter) o;
            if (!paymentTemplate.equals(other.paymentTemplate)) {
                return paymentTemplate.compareTo(other.paymentTemplate);
            } else {
                if (year != other.year) {
                    return new Integer(year).compareTo(other.year);
                } else {
                    return new Integer(quarter).compareTo(other.quarter);
                }
            }

        }
    }

    private void createEmailReportForGASUI(DomainEntitySet<MoneyMovementTransaction> payments) {
        //Create check payments email report of GA SUI Check Payments
        PaymentTemplate paymentTemplateGASUI = PaymentTemplate.findPaymentTemplate("GA-DOL4-PAYMENT");

        DomainEntitySet<MoneyMovementTransaction> checkPaymentsForGA = payments.find(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplateGASUI));
        if(checkPaymentsForGA.size() > 0) {
            SpcfCalendar initiationDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(initiationDate);
            //Create email attachment content with check payment details
            StringBuilder textFileContent = new StringBuilder();
            textFileContent.append("Check Number").append(SEPARATOR);
            textFileContent.append("PSID").append(SEPARATOR);
            textFileContent.append("FEIN").append(SEPARATOR);
            textFileContent.append("GA SUI AID").append(SEPARATOR);
            textFileContent.append("Legal Name").append(SEPARATOR);
            textFileContent.append("Check Amount").append(SEPARATOR);

            for (MoneyMovementTransaction moneyMovementTransaction : checkPaymentsForGA) {
                textFileContent.append("\n");
                addCell(textFileContent, moneyMovementTransaction.getReferenceNumber());
                addCell(textFileContent, moneyMovementTransaction.getCompany().getSourceCompanyId());
                addCell(textFileContent, moneyMovementTransaction.getCompany().getFedTaxId());
                addCell(textFileContent, moneyMovementTransaction.getAgencyTaxpayerId());
                addCell(textFileContent, moneyMovementTransaction.getCompany().getLegalName());
                addCell(textFileContent, moneyMovementTransaction.getMoneyMovementTransactionAmount().toString());
            }
            BatchUtils.emailCheckPaymentDetails(textFileContent, initiationDate, paymentTemplateGASUI);
        }
    }



    /**
     * Adds a cell to the report
     * @param builder The builder to append to
     * @param strings The strings to append
     */
    private void addCell(StringBuilder builder, String ... strings) {
        builder.append("=\"");

        for (String string : strings) {
            builder.append(string);
        }

        builder.append("\"").append(SEPARATOR);
    }
}
