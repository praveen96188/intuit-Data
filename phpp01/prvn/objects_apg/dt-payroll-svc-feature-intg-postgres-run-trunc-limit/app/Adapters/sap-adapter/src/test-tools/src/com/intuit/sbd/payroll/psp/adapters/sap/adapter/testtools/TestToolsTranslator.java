package com.intuit.sbd.payroll.psp.adapters.sap.adapter.testtools;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTOffloadGroup;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTOffloadBatch;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTEntryDetailRecord;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.TTBankReturn;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

import java.util.ArrayList;
import java.util.Date;

public class TestToolsTranslator {
    public ArrayList<TTOffloadGroup> getTTOffloadGroupsFromDomainEntities(DomainEntitySet<OffloadGroup> domainEntities) {
        ArrayList<TTOffloadGroup> ttOffloadGroups = new ArrayList<TTOffloadGroup>();
        for (OffloadGroup domainEntity : domainEntities) {
            TTOffloadGroup offloadGrp = getTTOffloadGroupFromDomainEntity(domainEntity);
            ttOffloadGroups.add(offloadGrp);
        }

        return ttOffloadGroups;
    }

    public TTOffloadGroup getTTOffloadGroupFromDomainEntity(OffloadGroup domainEntity) {
        TTOffloadGroup offloadGrp = new TTOffloadGroup();
        offloadGrp.setGroupCode(domainEntity.getOffloadGroupCd());
        offloadGrp.setGroupName(domainEntity.getName());
        offloadGrp.setGroupDescription(domainEntity.getDescription());
        offloadGrp.setCutoffTime(domainEntity.getCutoffTime());
        return offloadGrp;
    }

    public TTOffloadBatch getTTOffloadBatchFromDomainEntity(OffloadBatch domainEntity) {
        TTOffloadBatch offloadBatch = new TTOffloadBatch();
        offloadBatch.setOffloadDate(SAPTranslator.getDateFromSpcfCalendar(domainEntity.getOffloadDate()));
        offloadBatch.setOffloadGrpCd(domainEntity.getOffloadGroup().getOffloadGroupCd());
        offloadBatch.setStatusCd(domainEntity.getStatusCd().toString());
        offloadBatch.setInsertDate(SAPTranslator.getDateFromSpcfCalendar(domainEntity.getCreatedDate()));
        offloadBatch.setStatusChangeDate(SAPTranslator.getDateFromSpcfCalendar(domainEntity.getStatusEffeciveDate()));
        offloadBatch.setGseq(domainEntity.getId().toString());
        return offloadBatch;
    }

    public ArrayList<TTOffloadBatch> getTTOffloadBatchesFromDomainEntities(DomainEntitySet<OffloadBatch> domainEntities) {
        ArrayList<TTOffloadBatch> ttOffloadBatches = new ArrayList<TTOffloadBatch>();
        for (OffloadBatch domainEntity : domainEntities) {
            TTOffloadBatch offloadBatch = getTTOffloadBatchFromDomainEntity(domainEntity);
            ttOffloadBatches.add(offloadBatch);
        }

        return ttOffloadBatches;
    }

    public ArrayList<TTEntryDetailRecord> getTTEntryDetailRecordsFromDomainEntities(DomainEntitySet<EntryDetailRecord> entryDetailRecords) {
        ArrayList<TTEntryDetailRecord> ttEntryDetailRecords = new ArrayList<TTEntryDetailRecord>();
        for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
            TTEntryDetailRecord ttEntryDetailRecord = getTTEntryDetailRecordFromDomainEntity(entryDetailRecord);
            ttEntryDetailRecords.add(ttEntryDetailRecord);
        }
        return ttEntryDetailRecords;
    }

    public static final String[] TRANSACTION_CODE_CHECKING_CREDITS = new String[]{"21", "22"};
    public static final String[] TRANSACTION_CODE_CHECKING_DEBITS = new String[]{"26", "27"};
    public static final String[] TRANSACTION_CODE_SAVINGS_CREDITS = new String[]{"31", "32"};
    public static final String[] TRANSACTION_CODE_SAVINGS_DEBITS = new String[]{"36", "37"};

    private TTEntryDetailRecord getTTEntryDetailRecordFromDomainEntity(EntryDetailRecord entryDetailRecord) {
        TTEntryDetailRecord ttEntryDetailRecord = new TTEntryDetailRecord();

        if (entryDetailRecord.getRecordData() != null) {
            String recordData = entryDetailRecord.getRecordData();
            String transactionCode = recordData.substring(1, 3);
            if (transactionCode.equals(TRANSACTION_CODE_CHECKING_CREDITS[0]) ||
                    transactionCode.equals(TRANSACTION_CODE_CHECKING_CREDITS[1]) ||
                    transactionCode.equals(TRANSACTION_CODE_CHECKING_DEBITS[0]) ||
                    transactionCode.equals(TRANSACTION_CODE_CHECKING_DEBITS[1])) {
                ttEntryDetailRecord.setBankAccountType("C");
            }
            else if (transactionCode.equals(TRANSACTION_CODE_SAVINGS_CREDITS[0]) ||
                    transactionCode.equals(TRANSACTION_CODE_SAVINGS_CREDITS[1]) ||
                    transactionCode.equals(TRANSACTION_CODE_SAVINGS_DEBITS[0]) ||
                    transactionCode.equals(TRANSACTION_CODE_SAVINGS_DEBITS[1])) {
                ttEntryDetailRecord.setBankAccountType("S");
            }
            ttEntryDetailRecord.setRoutingNumber(recordData.substring(3, 12).trim());
            ttEntryDetailRecord.setAccountNumber(recordData.substring(12, 29).trim());
            ttEntryDetailRecord.setIndividualName(recordData.substring(54, 76).trim());
        }
        else {
            BankAccount intuitBankAccount = entryDetailRecord.getIntuitBankAccount().getBankAccount();
            ttEntryDetailRecord.setRoutingNumber(intuitBankAccount.getRoutingNumber());
            ttEntryDetailRecord.setAccountNumber(intuitBankAccount.getAccountNumber());
            if (intuitBankAccount.getAccountTypeCd().equals(BankAccountType.Checking)) {
                ttEntryDetailRecord.setBankAccountType("C");
            }
            else if (intuitBankAccount.getAccountTypeCd().equals(BankAccountType.Savings)) {
                ttEntryDetailRecord.setBankAccountType("S");
            }

            ttEntryDetailRecord.setIndividualName(entryDetailRecord.getIntuitBankAccount().getDescription());
        }

        MoneyMovementTransaction mmTxn = entryDetailRecord.getMoneyMovementTransaction();

        ttEntryDetailRecord.setMmTransactionId(mmTxn.getId().toString());
        ttEntryDetailRecord.setAmount(SAPTranslator.getDoubleFromSpcfMoney(entryDetailRecord.getAmount()));
        if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) {
            ttEntryDetailRecord.setCreditDebitIndicator("C");
        }
        else {
            ttEntryDetailRecord.setCreditDebitIndicator("D");
        }
        ttEntryDetailRecord.setTraceNumber(entryDetailRecord.getTraceNumber());

        ttEntryDetailRecord.setSettlementDate(new Date(mmTxn.getDueDate().toLocal().getTimeInMilliseconds()));
        ttEntryDetailRecord.setCompanyId(entryDetailRecord.getCompany().getSourceCompanyId());
        ttEntryDetailRecord.setCompanyLegalName(entryDetailRecord.getCompany().getLegalName());

        DomainEntitySet<TransactionReturn> txReturns = Application.find(TransactionReturn.class, TransactionReturn.MoneyMovementTransaction().equalTo(mmTxn));

        ArrayList<TTBankReturn> bankReturns = getTTBankReturnsFromDomainEntities(ttEntryDetailRecord, txReturns);

        if (bankReturns != null && bankReturns.size() > 0) {
            ttEntryDetailRecord.setBankReturnsExists(true);
            ttEntryDetailRecord.setBankReturns(bankReturns);
        } else {
            ttEntryDetailRecord.setBankReturnsExists(false);
        }

        return ttEntryDetailRecord;
    }

    private ArrayList<TTBankReturn> getTTBankReturnsFromDomainEntities(TTEntryDetailRecord ttEntryDetailRecord, DomainEntitySet<TransactionReturn> txReturns) {
        ArrayList<TTBankReturn> ttBankReturns = new ArrayList<TTBankReturn>();
        for (TransactionReturn transactionReturn : txReturns) {
            TTBankReturn ttBankReturn = getTTBankReturnFromDomainEntity(ttEntryDetailRecord, transactionReturn);
            ttBankReturns.add(ttBankReturn);
        }
        return ttBankReturns;
    }

    private TTBankReturn getTTBankReturnFromDomainEntity(TTEntryDetailRecord ttEntryDetailRecord, TransactionReturn transactionReturn) {
        TTBankReturn ttBankReturn = new TTBankReturn();
        ttBankReturn.setBankReturnCd(transactionReturn.getBankReturnCd());
        ttBankReturn.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(transactionReturn.getCreatedDate()));
        return ttBankReturn;
    }


}
