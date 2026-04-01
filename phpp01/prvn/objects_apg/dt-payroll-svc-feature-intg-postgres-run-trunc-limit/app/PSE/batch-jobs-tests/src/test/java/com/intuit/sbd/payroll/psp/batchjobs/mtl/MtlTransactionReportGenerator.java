package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionType;
import com.intuit.sbd.payroll.psp.domain.TransactionCategory;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class MtlTransactionReportGenerator {

    private MtlTransactionReportFileUtils mtlTransactionReportFileUtils;

    public MtlTransactionReportGenerator() {
        mtlTransactionReportFileUtils = new MtlTransactionReportFileUtils();
    }

    public void generateMtlTransactionReports(String rawReportName, String enrichedReportName) {
        Map<TransactionCategory, DomainEntitySet<FinancialTransaction>> allFinancialTransactions = getAllFinancialTransactions();

        Map<FinancialTransaction, MtlTransactionRecord> financialTransactionMtlTransactionRecordMap = createMtlTransactionRecordMap(allFinancialTransactions);

        // Get all Raw and Enriched MTL Transaction Records from Financial Transactions
        List<MtlTransactionRecord> rawMtlTransactionRecords = getAllRawMtlTransactionRecords(financialTransactionMtlTransactionRecordMap);
        List<MtlTransactionRecord> enrichedMtlTransactionRecords = getAllEnrichedMtlTransactionRecords(financialTransactionMtlTransactionRecordMap);

        //Generate both Raw and Enriched MTL Transaction Reports
        Path rawReportPath = MtlTransactionReportFileUtils.getAbsolutePath(rawReportName);
        Path enrichedReportPath = MtlTransactionReportFileUtils.getAbsolutePath(enrichedReportName);

        generateMtlTransactionReport(rawReportPath, rawMtlTransactionRecords);
        generateMtlTransactionReport(enrichedReportPath, enrichedMtlTransactionRecords);

        assertTrue("Raw MTL Transaction Report is not generated", Files.exists(rawReportPath));
        assertTrue("Enriched MTL Transaction Report is not generated", Files.exists(enrichedReportPath));
    }

    public Map<FinancialTransaction, MtlTransactionRecord> createMtlTransactionRecordMap(Map<TransactionCategory, DomainEntitySet<FinancialTransaction>> allFinancialTransactions) {
        Map<FinancialTransaction, MtlTransactionRecord> mtlTransactionRecordMap = new HashMap<>();
        for (Map.Entry<TransactionCategory, DomainEntitySet<FinancialTransaction>> entry : allFinancialTransactions.entrySet()) {
            for (FinancialTransaction financialTransaction : entry.getValue()) {
                MtlTransactionRecord transactionRecord = new MtlTransactionRecordBuilder().financialTransaction(financialTransaction).build();
                mtlTransactionRecordMap.put(financialTransaction, transactionRecord);
            }
        }
        return mtlTransactionRecordMap;
    }

    private List<MtlTransactionRecord> getAllRawMtlTransactionRecords(Map<FinancialTransaction, MtlTransactionRecord> financialTransactionMtlTransactionRecordMap) {
        List<MtlTransactionRecord> mtlTransactionRecords = new ArrayList<>();
        for (Map.Entry<FinancialTransaction, MtlTransactionRecord> entry : financialTransactionMtlTransactionRecordMap.entrySet()) {
            mtlTransactionRecords.add(entry.getValue());
        }
        return mtlTransactionRecords;
    }

    private List<MtlTransactionRecord> getAllEnrichedMtlTransactionRecords(Map<FinancialTransaction, MtlTransactionRecord> financialTransactionMtlTransactionRecordMap) {
        List<MtlTransactionRecord> mtlTransactionRecords = new ArrayList<>();
        for (Map.Entry<FinancialTransaction, MtlTransactionRecord> entry : financialTransactionMtlTransactionRecordMap.entrySet()) {
            mtlTransactionRecords.add(createEnrichedMtlTransactionRecord(entry.getValue(), entry.getKey()));
        }
        return mtlTransactionRecords;
    }

    private void generateMtlTransactionReport(Path filePath, List<MtlTransactionRecord> mtlTransactionRecords) {
        mtlTransactionReportFileUtils.createReport(filePath, mtlTransactionRecords);
    }

    private MtlTransactionRecord createEnrichedMtlTransactionRecord(MtlTransactionRecord transactionRecord, FinancialTransaction financialTransaction) {
        Company company = financialTransaction.getCompany();
        MtlTransactionRecord mtlTransactionRecord = transactionRecord;
        mtlTransactionRecord.setCustomerEin(PIIMask.maskText(company.getFedTaxId(), 5));

        BankAccount bankAccount = financialTransaction.getCreditBankAccount();
        if (Objects.isNull(bankAccount)) {
            return mtlTransactionRecord;
        }

        mtlTransactionRecord.setBeneficiaryBank(bankAccount.getBankName());
        mtlTransactionRecord.setBeneficiaryBankAccountNumber(PIIMask.maskText(bankAccount.getAccountNumber()));
        return mtlTransactionRecord;
    }

    private Map<TransactionCategory, DomainEntitySet<FinancialTransaction>> getAllFinancialTransactions() {
        DomainEntitySet<Company> allCompanies = getAllCompanies();
        Map<TransactionCategory, DomainEntitySet<FinancialTransaction>> allFinancialTransactions = new HashMap<>();
        for (Company company : allCompanies) {
            allFinancialTransactions.put(TransactionCategory.Employer, findFinancialTransactions(company, TransactionCategory.Employer));
        }
        return allFinancialTransactions;
    }

    private DomainEntitySet<Company> getAllCompanies() {
        return Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
    }

    private DomainEntitySet<FinancialTransaction> findFinancialTransactions(Company pCompany,
                                                                            TransactionCategory transactionCategory) {
        DomainEntitySet<TransactionType> txnTypeList = TransactionType.findTransactionTypeByTxnCategory(transactionCategory);

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.Company().equalTo(pCompany)
                                .And(FinancialTransaction.TransactionType().in(txnTypeList.toArray(new TransactionType[txnTypeList.size()]))));

        return Application.find(FinancialTransaction.class, query);
    }

}
