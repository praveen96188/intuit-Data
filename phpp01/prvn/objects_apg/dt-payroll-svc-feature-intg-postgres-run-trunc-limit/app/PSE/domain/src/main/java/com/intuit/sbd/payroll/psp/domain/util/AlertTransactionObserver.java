package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.TransactionObserver;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankit on 9/21/2015.
 */
public class AlertTransactionObserver extends TransactionObserver {

    public static final SpcfLogger logger = Application.getLogger(AlertTransactionObserver.class);
    public static final List<TransactionTypeCode> TRACKED_TRANSACTION_TYPE_CODE_LIST;
    public static final Long DEFAULT_FINANCIAL_TRANSACTION_ALERT_THRESHOLD = 250000L;
    public static final String ALERT_SUBJECT="PSP Transaction Alert - High Value Transaction Created";

    static{
        TRACKED_TRANSACTION_TYPE_CODE_LIST = new ArrayList<TransactionTypeCode>();
        TRACKED_TRANSACTION_TYPE_CODE_LIST.add(TransactionTypeCode.EmployerTaxDebit);
        TRACKED_TRANSACTION_TYPE_CODE_LIST.add(TransactionTypeCode.EmployerTaxDirectDebit);
    }

    List<FinancialTransaction> financialTransactionQueue = new ArrayList<FinancialTransaction>();

    private AlertTransactionObserver(){
    }

    public static AlertTransactionObserver getRegisteredObserver(){
        AlertTransactionObserver alertTransactionObserver = (AlertTransactionObserver)Application.getTransactionObserver(AlertTransactionObserver.class.getName());
        if(alertTransactionObserver == null){
            alertTransactionObserver = new AlertTransactionObserver();
            Application.registerTransactionObserver(AlertTransactionObserver.class.getName(),alertTransactionObserver);
        }
        return alertTransactionObserver;
    }

    public static boolean isTransactionTypeTracked(TransactionType transactionType){
        if(transactionType!=null && transactionType.getTransactionTypeCd()!=null){
            return TRACKED_TRANSACTION_TYPE_CODE_LIST.contains(transactionType.getTransactionTypeCd());
        }
        return Boolean.FALSE;
    }

    public void queue(FinancialTransaction financialTransaction){
        if(financialTransaction!=null && financialTransaction.getTransactionType()!=null
                && financialTransaction.getTransactionType().getTransactionTypeCd()!=null
                && TRACKED_TRANSACTION_TYPE_CODE_LIST.contains(financialTransaction.getTransactionType().getTransactionTypeCd())
                && !financialTransactionQueue.contains(financialTransaction)) {
            Long thresholdLimit = SystemParameter.findLongValue(SystemParameter.Code.FINANCIAL_TRANSACTION_ALERT_THRESHOLD, DEFAULT_FINANCIAL_TRANSACTION_ALERT_THRESHOLD);
            SpcfDecimal thresholdMoney = SpcfMoney.createInstance(thresholdLimit);
            if(financialTransaction.getFinancialTransactionAmount()!=null && financialTransaction.getFinancialTransactionAmount().isGreaterThanEqualTo(thresholdMoney)){
                financialTransactionQueue.add(financialTransaction);
            }
        }
    }

    private void clearQueue() {
        financialTransactionQueue.clear();
    }

    public void unregistered() {
        clearQueue();
    }

    public void beforeTransactionBegin() {
        clearQueue();
    }

    public void beforeTransactionRollback() {
        clearQueue();
    }

    public void afterTransactionCommit() {
        for(FinancialTransaction financialTransaction: financialTransactionQueue){
            try{
                if(!financialTransaction.isCancelled() && !financialTransaction.isVoided()) {
                    Company company = financialTransaction.getCompany();

                    Long thresholdLimit = SystemParameter.findLongValue(SystemParameter.Code.FINANCIAL_TRANSACTION_ALERT_THRESHOLD, DEFAULT_FINANCIAL_TRANSACTION_ALERT_THRESHOLD);
                    TransactionType transactionType = financialTransaction.getTransactionType();
                    String mailServer = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_mail_server");
                    String fromAddress = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_transaction_alert_email_list");
                    String toAddress = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_transaction_alert_email_list");
                    String settlementDate = financialTransaction.getSettlementDate() != null ? financialTransaction.getSettlementDate().format("yyyyMMdd") : "Null";
                    StringBuilder body = new StringBuilder();
                    logger.info("Sending transaction alert for company=" + company.getSourceCompanyId() + " FT=" + financialTransaction.getId()+" to="+toAddress+" mailserver="+mailServer
                            + " ftAmount=" + financialTransaction.getFinancialTransactionAmount() + " ftTxnTypeCd=" + financialTransaction.getTransactionType().getTransactionTypeCd()
                            + " ftThresholdLimit=" + thresholdLimit + " ftSettlementTypeCd=" + financialTransaction.getSettlementTypeCd() + " ftSettlementDate="
                            + settlementDate + " isAgentInitiated=" + Application.getCurrentPrincipal().isAgent()
                            + " userId=" + Application.getCurrentPrincipal().getId() + " userName=" + Application.getCurrentPrincipal().getName());
                    if (StringUtils.isNotEmpty(toAddress)) {
                        body.append("High value transaction created for company " + company.getLegalName());
                        body.append("\r\n\r\n");
                        body.append("Company Legal Name: " + company.getLegalName());
                        body.append("\r\n");
                        body.append("Company FEIN: " + company.getFedTaxId());
                        body.append("\r\n");
                        body.append("Source Company Id(PSID): " + company.getSourceCompanyId());
                        body.append("\r\n");
                        body.append("Transaction Type: " + transactionType.getTransactionTypeCd());
                        body.append("\r\n");
                        body.append("Transaction Amount: " + financialTransaction.getFinancialTransactionAmount());
                        body.append("\r\n");
                        body.append("Transaction Settlement Date: ").append(settlementDate);
                        body.append("\r\n");
                        body.append("Financial Transaction Seq: " + financialTransaction.getId());
                        body.append("\r\n");
                        body.append("Current Alert Threshold Amount: " + thresholdLimit);
                        body.append("\r\n\r\n");
                        try {
                            Class<?> clazz = Class.forName("com.intuit.sbd.payroll.psp.common.utils.MailSender");
                            Method method = clazz.getDeclaredMethod("sendEmailAsync", String.class, String.class, String.class, String.class, String.class);
                            method.invoke(clazz.newInstance(), mailServer,
                                    toAddress,
                                    fromAddress,
                                    ALERT_SUBJECT,
                                    body.toString());
                            logger.info("Created Mailsender using Reflection. Successfully scheduled transaction alert for company=" + company.getSourceCompanyId() + " FT=" + financialTransaction.getId()+" to="+toAddress);

                        } catch (Exception e){
                            logger.error("Reflection call- Email send failed.", e);
                        }
                    } else {
                        logger.warn("Unable to send transaction alert email for FT=" + financialTransaction.getId()+" to address is empty");
                    }
                }

            }catch(Throwable ex){
                //Suppress any possible exception we get because we do not want alerts to block transaction processing
                logger.info("Received exception while sending alert email, system will ignore and continue processing",ex);
            }
        }
        clearQueue();
    }

}
