package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Batch job to locate the financial transactions that were not offloaded
 * due to disqualification of offload criteria and to cancel them.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 8, 2008
 * Time: 3:29:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessMissedACHTransactions {
    private static final SpcfLogger logger = Application.getLogger(ProcessMissedACHTransactions.class);
    private static final String NEWLINE = "\n";
    private static final String OFFSET = "   ";
    private static final String NA = "N/A";

    private DomainEntitySet<FinancialTransaction> notificationFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
    private DomainEntitySet<FinancialTransaction> errorFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
    Map<SpcfUniqueId, SpcfCalendar>  finTxInitiationDateMap = new Hashtable<SpcfUniqueId, SpcfCalendar>();

    Map<SpcfUniqueId, TransactionResponse> companyTxResponseMap = new Hashtable<SpcfUniqueId, TransactionResponse>();

    private String errorMessage;
    private String notificationMessage;

    private PSPRequestContextManager pspRequestContextManager;

    public ProcessMissedACHTransactions() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    /**
     * Process offloaded ACH Transactions which are not returned with in the ACH Wait period.
     *
     * @param pProcessingDate
     * @return
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
        logger.info("Beginning Process Missed ACH Transctions batch job for date: " +
                processingDate.format(BatchUtils.DATE_FORMAT));

        String result = processMissedTransactions(processingDate);

        logger.info("Completed Process Missed ACH Transctions batch job. Elapsed time: " +
                timer.stop().getElapsedTimeString());

        return result;
    }

    private String processMissedTransactions(SpcfCalendar pProcessingDate) {
        // select MMTs
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTxs = null;

        if(((String) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES_METHODS)).contains("processMissedTransactions")) {
            moneyMovementTxs =
                    MoneyMovementTransaction.findMissedMMTransactionsCriteria(pProcessingDate);
        } else {
            moneyMovementTxs =
                    MoneyMovementTransaction.findMissedMMTransactions(pProcessingDate);
        }

        for (MoneyMovementTransaction mmTransaction : moneyMovementTxs) {
            try {
                pspRequestContextManager.setRequestContextCompany(mmTransaction.getCompany());
                DomainEntitySet<FinancialTransaction> associatedFinTxs = new DomainEntitySet<FinancialTransaction>();

                boolean hasEmployerTaxTransaction = false;
                for (FinancialTransaction ft : mmTransaction.getFinancialTransactionCollection()) {
                    associatedFinTxs.add(ft);
                    finTxInitiationDateMap.put(ft.getId(), mmTransaction.getInitiationDate().toLocal());
                    if (ft.isEmployerTaxTransaction()) {
                        hasEmployerTaxTransaction = true;
                    }
                }

                // only notify PSP engineering - don't cancel due to pain in re-creating transactions
                if (hasEmployerTaxTransaction) {
                    for (FinancialTransaction currFinancialTxn : associatedFinTxs) {
                        logger.info("notifying support due to missed employer tax financial transaction (id: " + currFinancialTxn.getId().toString() + ")");
                        errorFinancialTransactions.add(currFinancialTxn);
                    }
                    continue;
                }

                for (FinancialTransaction currFinancialTxn : associatedFinTxs) {
                    logger.info("Cancelling financial transaction (id: " + currFinancialTxn.getId().toString() + ")");

                    currFinancialTxn = currFinancialTxn.updateFinancialTransactionState(TransactionStateCode.Cancelled);

                    if (isNotificationFinancialTransaction(currFinancialTxn)) {
                        notificationFinancialTransactions.add(currFinancialTxn);
                    } else {
                        errorFinancialTransactions.add(currFinancialTxn);
                    }

                    // verify this financial txn can be included in a transaction response
                    if (currFinancialTxn.getTransactionType().getIncludeInTransactionResponse()) {
                        // associate all cancelled financial txs in a company to the same tx response
                        TransactionResponse txResponse = null;

                        if (null != currFinancialTxn.getCompany()) {
                            txResponse = companyTxResponseMap.get(currFinancialTxn.getCompany().getId());

                            if (null == txResponse) {
                                txResponse = TransactionResponse.createTransactionResponseForFinancialTx(currFinancialTxn);
                                companyTxResponseMap.put(currFinancialTxn.getCompany().getId(), txResponse);
                            } else {
                                // if txn response exists for a txn in the same payroll associate this txn also to the
                                // same txn response
                                FinancialTransactionState finTxState =
                                        currFinancialTxn.getCurrentFinancialTransactionState();
                                finTxState.setTransactionResponse(txResponse);
                                Application.save(finTxState);
                            }
                        } else {
                            // if not a payroll txn create a new transaction response
                            TransactionResponse.createTransactionResponseForFinancialTx(currFinancialTxn);
                        }
                    }
                }
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        //Notification message is sent to RM so that they can handle any necessary company actions
        notificationMessage= generateNotificationMessage();
        //Error message is sent to PD- this means there is a defect somewhere
        errorMessage = generateErrorMessage();

        return notificationMessage;
    }

    private boolean isNotificationFinancialTransaction(FinancialTransaction pFinTxn) {
        boolean bCompanyIsOnHold = pFinTxn.getCompany().isCompanyOnHold();
        boolean bIsRefundTransactionType = TransactionType.isRefundTransactionType(pFinTxn.getTransactionType().getTransactionTypeCd());
        boolean bIsVerificationDebit = pFinTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerVerificationDebit;

        if (bCompanyIsOnHold && (bIsRefundTransactionType || bIsVerificationDebit)) {
            return true;
        } else {
            return false;
        }
    }

    private String generateErrorMessage() {
        return generateMessage(errorFinancialTransactions);
    }

    private String generateNotificationMessage() {
        return generateMessage(notificationFinancialTransactions);
    }

    private String generateMessage(DomainEntitySet<FinancialTransaction> pFinancialTransactions) {

        // if no cancelled txns, no notification message
        if (pFinancialTransactions.isEmpty()) {
            return null;
        }

        StringBuffer notificationMessage = new StringBuffer();
        String positionStr = NEWLINE + OFFSET;
        int compCtr = 0;

        pFinancialTransactions = pFinancialTransactions.sort(
                FinancialTransaction.Company().LegalName(), 
                FinancialTransaction.Company().Id(), 
                FinancialTransaction.PayrollRun().SourcePayRunId(), 
                FinancialTransaction.FinancialTransactionAmount());

        Company priorCompany = null;
        PayrollRun priorPayrollRun = null;
        boolean bProcessedNonPayrollTransactions = false;
        int payrollCounter = 0;
        int transactionCounter = 0;

        for (FinancialTransaction currTxn : pFinancialTransactions) {
            Company currCompany = currTxn.getCompany();
            if (currCompany != null && currCompany != priorCompany) {
                if (priorCompany != null) {
                    notificationMessage.append(NEWLINE);
                }
                notificationMessage.append("Company ").append(++compCtr);
                notificationMessage.append(generateCompanyInfo(currCompany));
                bProcessedNonPayrollTransactions = false;
            }

            if (currTxn.getPayrollRun() == null && !bProcessedNonPayrollTransactions) {
                transactionCounter = 0;
                notificationMessage.append(positionStr).append("Non-Payroll Transactions ");
                bProcessedNonPayrollTransactions = true;
            } else if (currTxn.getPayrollRun() != priorPayrollRun) {
                transactionCounter = 0;
                notificationMessage.append(positionStr).append("Payroll ").append(++payrollCounter);
                notificationMessage.append(generatePayrollInfo(currTxn.getPayrollRun()));
            }

            notificationMessage.append(positionStr).append(OFFSET).append("Tx ").append(++transactionCounter);
            notificationMessage.append(generateTxInfo(currTxn));
            priorCompany = currCompany;
            priorPayrollRun = currTxn.getPayrollRun();
        }

        return notificationMessage.toString();
    }

    public static String generateCompanyInfo(Company pCompany) {
        StringBuilder companyInfo = new StringBuilder();
        String positionStr = NEWLINE + OFFSET;

        String sourceCompanyId;
        if (null != pCompany.getSourceCompanyId()) {
            sourceCompanyId = pCompany.getSourceCompanyId();
        } else {
            sourceCompanyId = NA;
        }

        String legalName;
        if (null != pCompany.getLegalName()) {
            legalName = pCompany.getLegalName();
        } else {
            legalName = NA;
        }

        CompanyService ddService = CompanyService.findCompanyService(pCompany, ServiceCode.DirectDeposit);

        String ddSserviceStatus;
        if ((null != ddService) && (null != ddService.getStatusCd())) {
            ddSserviceStatus = ddService.getStatusCd().toString();
        } else {
            ddSserviceStatus = NA;
        }

        companyInfo.append(positionStr).append("Source System Code: ").append(pCompany.getSourceSystemCd().toString());
        companyInfo.append(positionStr).append("Source Company ID:  ").append(sourceCompanyId);
        companyInfo.append(positionStr).append("Company Legal Name: ").append(legalName);
        Collection<ServiceSubStatusCode> onHoldReasons = pCompany.getCurrentOnHoldReasonCodes();
        if (onHoldReasons.size() > 0) {
            ddSserviceStatus = pCompany.getOnHoldNotesString();
        }

        companyInfo.append(positionStr).append("DD Service Status:  ").append(ddSserviceStatus);

        return companyInfo.toString();
    }

    private String generateTxInfo(FinancialTransaction pFinancialTransaction) {
        StringBuffer txInfo = new StringBuffer();
        String positionStr = NEWLINE + OFFSET + OFFSET + OFFSET;

        String sourceDdTxnId;
        if (null != pFinancialTransaction.getPaycheckSplit()) {
            sourceDdTxnId = pFinancialTransaction.getPaycheckSplit().getSourceDdTxnId();
        } else {
            sourceDdTxnId = NA;
        }

        String settlementDate;
        if (null != pFinancialTransaction.getSettlementDate()) {
            settlementDate = pFinancialTransaction.getSettlementDate().toLocal().toString();
        } else {
            settlementDate = NA;
        }

        String txnAmount;
        if (null != pFinancialTransaction.getFinancialTransactionAmount()) {
            BigDecimal amt = SpcfUtils.convertToBigDecimal(pFinancialTransaction.getFinancialTransactionAmount());
            txnAmount = String.format("$%,.2f", amt);
        } else {
            txnAmount = NA;
        }

        txInfo.append(positionStr).append("Src DD Txn ID:   ").append(sourceDdTxnId);
        txInfo.append(positionStr).append("Txn Type Code:   ").append(pFinancialTransaction.getTransactionType().getTransactionTypeCd().toString());
        txInfo.append(positionStr).append("Initiation Date: ").append(finTxInitiationDateMap.get(pFinancialTransaction.getId()).toLocal().toString());
        txInfo.append(positionStr).append("Settlement Date: ").append(settlementDate);
        if (pFinancialTransaction.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Cancelled) {
            txInfo.append(positionStr).append("State:           ").append(pFinancialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        txInfo.append(positionStr).append("Txn Amount:      ").append(txnAmount);
        

        return txInfo.toString();
    }

    public static String generatePayrollInfo(PayrollRun pPayrollRun) {
        StringBuilder payrollInfo = new StringBuilder();
        String positionStr = NEWLINE + OFFSET + OFFSET;

        String sourcePayrollRunId;
        if (null != pPayrollRun.getSourcePayRunId()) {
            sourcePayrollRunId = pPayrollRun.getSourcePayRunId();
        } else {
            sourcePayrollRunId = NA;
        }

        String paycheckDate;
        if (null != pPayrollRun.getPaycheckDate()) {
            paycheckDate = pPayrollRun.getPaycheckDate().toLocal().toString();
        } else {
            paycheckDate = NA;
        }

        String netPayrollAmount;
        if (null != pPayrollRun.getPayrollDirectDepositAmount()) {
            BigDecimal amt = SpcfUtils.convertToBigDecimal(pPayrollRun.getPayrollDirectDepositAmount());
            netPayrollAmount = String.format("$%,.2f", amt);
        } else {
            netPayrollAmount = NA;
        }

        payrollInfo.append(positionStr).append("Src Payroll Run ID: ").append(sourcePayrollRunId);
        payrollInfo.append(positionStr).append("Created Date:       ").append(pPayrollRun.getCreatedDate().toLocal().toString());
        payrollInfo.append(positionStr).append("Paycheck Date:      ").append(paycheckDate);
        payrollInfo.append(positionStr).append("Net Payroll Amount: ").append(netPayrollAmount);

        return payrollInfo.toString();
    }
}
