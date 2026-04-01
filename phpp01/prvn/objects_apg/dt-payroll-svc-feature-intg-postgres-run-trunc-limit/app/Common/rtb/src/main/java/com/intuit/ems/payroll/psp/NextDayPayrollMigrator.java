package com.intuit.ems.payroll.psp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intuit.ems.payroll.psp.csv.CSVFileParser;
import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.fundingmodel.NextDayFundingMigrationCore;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * @author rn5
 */
public class NextDayPayrollMigrator {

    private static final SpcfLogger LOGGER = Application.getLogger(NextDayPayrollMigrator.class);
    private int skippedPSIDCount = 0;

    //Input : CSV File with no headers. PSID, PayrollRunSeq as comma separated values.
    public static void main(String[] args) throws Exception {
        String filePath = args[0];
        //String filePath = "/Users/rn5/Softwares/test.csv";
        try {
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.NextDayPayrollMigrator));
            new NextDayPayrollMigrator().process(filePath);
        } finally {
            Application.uninitialize();
        }

    }

    private void process(String filePath) throws IOException {
        LOGGER.info("Event=MigrateTwoDayPayroll, Status=Start, filePath=" + filePath);
        Application.initialize();

        CSVFileParser csvFileParser = PayrollApplicationBeanFactory.getBean(CSVFileParser.class);
        FileRecord fileRecord = csvFileParser.parse(filePath);

        Map<String, List<SpcfUniqueId>> psidPayrollRunSeqMap = groupByPSID(fileRecord);

        int successCount = 0;
        for (Entry<String, List<SpcfUniqueId>> entry : psidPayrollRunSeqMap.entrySet()) {
            int migrationStatus = migratePayroll(entry.getKey(), entry.getValue());
            successCount += migrationStatus;
        }

        int totalRecords = psidPayrollRunSeqMap.size() + skippedPSIDCount;
        int failedCount = totalRecords - successCount - skippedPSIDCount;
        LOGGER.info(String.format("Event=MigrateTwoDayPayroll, Status=Done, TotalPSIDCount=%s, SuccessPSIDCount=%s, FailedPSIDCount=%s, SkippedPSIDCount=%s", totalRecords, successCount, failedCount, skippedPSIDCount));
    }

    private Map<String, List<SpcfUniqueId>> groupByPSID(FileRecord fileRecord) {

        Map<String, List<SpcfUniqueId>> psidPayrollRunSeqMap = new HashMap<String, List<SpcfUniqueId>>();

        for (List<String> row : fileRecord.getData()) {
            SpcfUniqueId payrollRunSeq;
            String sourceCompanyId = null, payrollRunId = null;
            try {
                sourceCompanyId = row.get(0);
                payrollRunId = row.get(1);
                payrollRunSeq = SpcfUniqueId.createInstance(payrollRunId);
                if (psidPayrollRunSeqMap.get(sourceCompanyId) == null) {
                    psidPayrollRunSeqMap.put(sourceCompanyId, new ArrayList<SpcfUniqueId>());
                }
                psidPayrollRunSeqMap.get(sourceCompanyId).add(payrollRunSeq);
            } catch (Exception e) {
                LOGGER.error(String.format("Event=MigrateTwoDayPayroll, Status=Failed, Reason=InvalidInput, SourceCompanyId=%s, PayrollRunSeq=%s", sourceCompanyId, payrollRunId), e);
                skippedPSIDCount++;
                continue;
            }

        }
        return psidPayrollRunSeqMap;
    }

    /**
     * @param sourceCompanyId
     * @param payrollRunSeq
     * @return 0 - Failed, 1 - Success
     */
    private int migratePayroll(String sourceCompanyId, List<SpcfUniqueId> payrollRunSeqList) {
        int migrationStatus = 0;
        try {
            Application.beginUnitOfWork();

            LOGGER.info(String.format("Event=MigrateTwoDayPayroll,Status=Start,SourceCompanyId=%s", sourceCompanyId));
            NextDayFundingMigrationCore nextDayFundingMigrationCore = new NextDayFundingMigrationCore(sourceCompanyId, payrollRunSeqList);
            ProcessResult processResult = nextDayFundingMigrationCore.execute();
            if (processResult.isSuccess()) {
                LOGGER.info(String.format("Event=MigrateTwoDayPayroll,Status=Done,SourceCompanyId=%s", sourceCompanyId));
                Application.commitUnitOfWork();
                migrationStatus++;
                return migrationStatus;
            }
            LOGGER.error(String.format("Event=MigrateTwoDayPayroll,Status=Failed,SourceCompanyId=%s, Reason=%s", sourceCompanyId, processResult.getErrorMessages()));
        } catch (Exception e) {
            LOGGER.error(String.format("Event=MigrateTwoDayPayroll,Status=Failed,SourceCompanyId=%s", sourceCompanyId), e);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return migrationStatus;
    }

}
