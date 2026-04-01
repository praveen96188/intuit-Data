package com.intuit.ems.payroll.psp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections4.ListUtils;

import com.intuit.ems.payroll.psp.factory.TaxTransactionQueryBuilder;
import com.intuit.ems.payroll.psp.model.QueryModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

public class TaxTransactionsProcessor {

    private static final String NULL = "Null";
    private static final SpcfLogger LOGGER = Application.getLogger(TaxTransactionsProcessor.class);
    private int count = 0;
    private static final int BATCH_SIZE = 50;


    public static void main(String[] args) {
        //Sample Argument --> args = new String[]{"fromDate=09052012230000", "toDate=11052012090000", "rollback=true"};
        QueryModel queryModel = new TaxTransactionQueryBuilder().getQueryForTaxProcessor(args);
        new TaxTransactionsProcessor().processTaxTransactions(queryModel);
    }

    private void processTaxTransactions(QueryModel queryModel) {
        LOGGER.info("Started Tax transaction processing.Rollback=" + queryModel.isRollback());
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TaxTransactionProcessor));
        Map<SpcfCalendar, List<SpcfUniqueId>> initiationDateMap = null;

        try {
            Application.beginUnitOfWork();

            List<Object> mmtIdSet = Application.executeNamedQuery(queryModel.getQuery(), queryModel.getParamNames(), queryModel.getParamValues());

            initiationDateMap = groupMoneyMovementTransactions(mmtIdSet);

            Application.rollbackUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

        process(initiationDateMap, queryModel.isRollback());
        LOGGER.info("Processed all Tax transactions.Rollback=" + queryModel.isRollback());
    }

    private Map<SpcfCalendar, List<SpcfUniqueId>> groupMoneyMovementTransactions(List<Object> moneyMovementTransactionSet) {
        Map<SpcfCalendar, List<SpcfUniqueId>> initiationDateMMTIdMap = new TreeMap<SpcfCalendar, List<SpcfUniqueId>>();
        LOGGER.info("Total tax transactions to be migrated=" + moneyMovementTransactionSet.size());
        for (Object row : moneyMovementTransactionSet) {
            SpcfCalendar initiationDate = (SpcfCalendar) ((Object[]) row)[0];
            SpcfUniqueId mmtId = (SpcfUniqueId) ((Object[]) row)[1];

            List<SpcfUniqueId> moneyMovementIdList = initiationDateMMTIdMap.get(initiationDate);
            if (moneyMovementIdList == null) {
                moneyMovementIdList = new ArrayList<SpcfUniqueId>();
                initiationDateMMTIdMap.put(initiationDate, moneyMovementIdList);
            }
            moneyMovementIdList.add(mmtId);
        }
        return initiationDateMMTIdMap;
    }

    private void process(Map<SpcfCalendar, List<SpcfUniqueId>> initiationDateMap, boolean rollback) {
        for (Entry<SpcfCalendar, List<SpcfUniqueId>> entry : initiationDateMap.entrySet()) {
            SpcfCalendar initiationDate = entry.getKey();
            List<SpcfUniqueId> mmtIdAll = entry.getValue();
            count = 0;

            LOGGER.info("Processing batch. Initiation date=" + initiationDate + ". Total Records=" + mmtIdAll.size());
            List<List<SpcfUniqueId>> partitionedList = ListUtils.partition(mmtIdAll, BATCH_SIZE);
            for (List<SpcfUniqueId> moneyMovementTransactionIdList : partitionedList) {
                try {
                    Application.beginUnitOfWork();
                    processBatch(initiationDate, moneyMovementTransactionIdList, rollback);
                    Application.commitUnitOfWork();
                } finally {
                    Application.rollbackUnitOfWork();
                }
            }
            LOGGER.info("Processed batch. Initiation date : " + initiationDate + ". Total Records : " + count);
        }
    }

    private void processBatch(SpcfCalendar initiationDate, List<SpcfUniqueId> moneyMovementTransactionIdList, boolean rollback) {

        for (SpcfUniqueId moneyMovementTransactionId : moneyMovementTransactionIdList) {
            //Update MMT
            MoneyMovementTransaction moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, moneyMovementTransactionId);
            if (!updateMMTRequired(moneyMovementTransaction, rollback))
                continue;

            String oldOffloadBatch = moneyMovementTransaction.getOffloadBatch() == null ? NULL : moneyMovementTransaction.getOffloadBatch().getId().toString();
            LOGGER.info("Old MMT Record. MMT Id=" + moneyMovementTransaction.getId() + ", Initiation Date=" + moneyMovementTransaction.getInitiationDate() + ", Offload Batch=" + oldOffloadBatch);

            SpcfCalendar newInitiationDate = moneyMovementTransaction.getInitiationDate().copy();
            int offset = rollback == true ? -1 : 1;
            CalendarUtils.addBusinessDays(newInitiationDate, offset);
            moneyMovementTransaction.setInitiationDate(newInitiationDate); //This recalculates offload batch & nacha file.

            //Update EDR
            updateEDRRecord(moneyMovementTransaction, newInitiationDate);
            String newOffloadBatch = moneyMovementTransaction.getOffloadBatch() == null ? NULL : moneyMovementTransaction.getOffloadBatch().getId().toString();
            LOGGER.info("New MMT Record. MMT Id=" + moneyMovementTransaction.getId() + ", Initiation Date=" + moneyMovementTransaction.getInitiationDate() + ", Offload Batch=" + newOffloadBatch);
            LOGGER.info("Processing Date=" + initiationDate + ". Last Processed MMT Record=" + ++count);
        }
    }

    private boolean updateMMTRequired(MoneyMovementTransaction moneyMovementTransaction, boolean rollback) {
        SpcfCalendar initiationDate = moneyMovementTransaction.getInitiationDate().copy();
        SpcfCalendar settlementDate = moneyMovementTransaction.getFirstFinancialTransaction().getSettlementDate().copy();

        CalendarUtils.clearTime(initiationDate);
        CalendarUtils.clearTime(settlementDate);

        int offset = rollback ? 1 : 2;
        SpcfCalendar expectedSettlementDate = initiationDate.copy();
        CalendarUtils.addBusinessDays(expectedSettlementDate, offset);

        boolean updateMMTRequired = settlementDate.getDay() == expectedSettlementDate.getDay()
                && settlementDate.getMonth() == expectedSettlementDate.getMonth()
                && settlementDate.getYear() == expectedSettlementDate.getYear();
        if (!updateMMTRequired) {
            LOGGER.error("MMT update did not happen. MMT Id=" + moneyMovementTransaction.getId());
        }
        return updateMMTRequired;
    }

    private void updateEDRRecord(MoneyMovementTransaction moneyMovementTransaction, SpcfCalendar newInitiationDate) {
        for (EntryDetailRecord entryDetailRecord : moneyMovementTransaction.getEntryDetailRecordCollection()) {
            LOGGER.info("Old EDR Record. EDR Id=" + entryDetailRecord.getId() + ", Initiation Date=" + entryDetailRecord.getInitiationDate());
            entryDetailRecord.setInitiationDate(newInitiationDate);
            String newNachaFile = entryDetailRecord.getNACHAFile() == null ? NULL : entryDetailRecord.getNACHAFile().getId().toString();
            LOGGER.info("New EDR Record. EDR Id=" + entryDetailRecord.getId() + ", Initiation Date=" + entryDetailRecord.getInitiationDate() + ", NachaFileId=" + newNachaFile);
        }
    }

}
