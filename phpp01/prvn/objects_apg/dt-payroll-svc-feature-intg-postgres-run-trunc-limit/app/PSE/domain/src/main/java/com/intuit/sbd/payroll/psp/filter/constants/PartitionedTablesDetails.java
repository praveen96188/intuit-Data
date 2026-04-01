package com.intuit.sbd.payroll.psp.filter.constants;

import com.google.common.collect.ImmutableMap;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.interceptor.constants.InterceptorConstant;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Getter
public class PartitionedTablesDetails {
    private final Map<Class, String> classTableNameMap = ImmutableMap.<Class, String>builder()
            .put(CompanyEvent.class, "PSP_COMPANY_EVENT")
            .put(CompanyEventDetail.class, "PSP_COMPANY_EVENT_DETAIL")
            .put(CompanyEventEmailParam.class, "PSP_COMPANY_EVENT_EMAIL_PARAM")
            .put(Compensation.class, "PSP_COMPENSATION")
            .put(Deduction.class, "PSP_DEDUCTION")
            .put(DisburseAdviceTaxLiab.class, "PSP_DISBURSE_ADVICE_TAX_LIAB")
            .put(EntryDetailRecord.class, "PSP_ENTRY_DETAIL_RECORD")
            .put(FinancialTransactionState.class, "PSP_FINANCIAL_TRANS_STATE")
            .put(FinancialTransaction.class, "PSP_FINANCIAL_TRANSACTION")
            .put(LedgerBalance.class, "PSP_LEDGER_BALANCE")
            .put(MoneyMovementTransaction.class, "PSP_MONEY_MOVEMENT_TRANSACTION")
            .put(Paycheck.class, "PSP_PAYCHECK")
            .put(PaycheckSplit.class, "PSP_PAYCHECK_SPLIT")
            .put(Paystub.class, "PSP_PAYSTUB")
            .put(PropertyAudit.class, "PSP_PROPERTY_AUDIT")
            .put(PstubEmployeeInfo.class, "PSP_PSTUB_EMPLOYEE_INFO")
            .put(PstubPaidTimeoffItem.class, "PSP_PSTUB_PAID_TIMEOFF_ITEM")
            .put(PstubPayItem.class, "PSP_PSTUB_PAY_ITEM")
            .put(QbdtPaycheckInfo.class, "PSP_QBDT_PAYCHECK_INFO")
            .put(QbdtPaylineInfo.class, "PSP_QBDT_PAYLINE_INFO")
            .put(QbdtTransactionInfo.class, "PSP_QBDT_TRANSACTION_INFO")
            .put(Tax.class, "PSP_TAX")
            .build();

    private final Map<Class, String> childClassTableNameMap = ImmutableMap.<Class, String>builder()
            .put(ATFPaymentsToProcess.class, "PSP_ATFPAYMENTS_TO_PROCESS")
            .put(CompanyEventEmail.class, "PSP_COMPANY_EVENT_EMAIL")
            .put(CompanyNote.class, "PSP_COMPANY_NOTE")
            .put(EdiPaymentDetail.class, "PSP_EDI_PAYMENT_DETAIL")
            .put(EftpsPaymentDetail.class, "PSP_EFTPS_PAYMENT_DETAIL")
            .put(EmployerContribution.class, "PSP_EMPLOYER_CONTRIBUTION")
            .put(EventAs400Sync.class, "PSP_EVENT_AS400_SYNC")
            .put(FraudEvent.class, "PSP_FRAUD_EVENT")
            .put(FsetFilingDetail.class, "PSP_FSET_FILING_DETAIL")
            .put(PaymentBatchAssoc.class, "PSP_PAYMENT_BATCH_ASSOC")
            .put(PstubDDItem.class, "PSP_PSTUB_DDITEM")
            .put(PstubMsg.class, "PSP_PSTUB_MSG")
            .put(TaxPaymentOnHoldReason.class, "PSP_TAX_PAYMENT_ON_HOLD_REASON")
            .put(ThirdParty401kBatchPaycheck.class, "PSP_TP401K_BATCH_PAYCHECK")
            .put(ThirdParty401kPaycheck.class, "PSP_TP401K_PAYCHECK")
            .put(TransactionReturn.class, "PSP_TRANSACTION_RETURN")
            .put(VoidedCheck.class, "PSP_VOIDED_CHECK")
            .put(WorkersCompPaycheck.class, "PSP_WC_PAYCHECK")
            .build();

    //Add here for more partition name and tables
    private final Map<String, Map<Class, String>> partitionedClassTableNameMap = ImmutableMap.<String, Map<Class, String>>builder()
            .put(InterceptorConstant.CREATED_DATE_SQL, ImmutableMap.<Class, String>builder()
                    .put(EntityUpdate.class, "PSP_ENTITY_UPDATE")
                    .build())
            .put(InterceptorConstant.LICENSE_NUMBER_SQL, ImmutableMap.<Class, String>builder()
                    .put(EntitlementMessage.class, "PSP_ENTITLEMENT_MESSAGE")
                    .build())
            .build();

    private Map<String, Class> classNameClassMap;
    private Map<String, Map<String, Class>> partitionedClassNameClassMap;
    private Map<String, String> classNameTableNameMap;
    private Map<String, Map<String, String>> partitionedClassNameTableNameMap;
    private List<String> partitionedAndChildClassesSimpleNameList;

    @PostConstruct
    public void init() {
        classNameClassMap = new HashMap<String, Class>();
        partitionedClassNameClassMap = new HashMap<String, Map<String, Class>>();
        classNameTableNameMap = new HashMap<String, String>();
        partitionedClassNameTableNameMap = new HashMap<String, Map<String, String>>();
        partitionedAndChildClassesSimpleNameList = new ArrayList<>();
        for(Map.Entry<Class, String> entry : classTableNameMap.entrySet()) {
            classNameClassMap.put(entry.getKey().getName(), entry.getKey());
            classNameTableNameMap.put(entry.getKey().getName(), entry.getValue());
            partitionedAndChildClassesSimpleNameList.add(entry.getKey().getSimpleName());
        }

        for(Map.Entry<String, Map<Class, String>> entry : partitionedClassTableNameMap.entrySet()) {

            Map<String, Class> innerClassNameClassMap = new HashMap<String, Class>();
            Map<String, String> innerClassNameTableNameMap = new HashMap<String, String>();

            for(Map.Entry<Class, String> innerEntry : entry.getValue().entrySet()) {
                innerClassNameClassMap.put(innerEntry.getKey().getName(), innerEntry.getKey());
                innerClassNameTableNameMap.put(innerEntry.getKey().getName(), innerEntry.getValue());
            }

            partitionedClassNameClassMap.put(entry.getKey(), innerClassNameClassMap);
            partitionedClassNameTableNameMap.put(entry.getKey(), innerClassNameTableNameMap);

        }

        for(Class aClass : childClassTableNameMap.keySet()) {
            partitionedAndChildClassesSimpleNameList.add(aClass.getSimpleName());
        }

    }
}
