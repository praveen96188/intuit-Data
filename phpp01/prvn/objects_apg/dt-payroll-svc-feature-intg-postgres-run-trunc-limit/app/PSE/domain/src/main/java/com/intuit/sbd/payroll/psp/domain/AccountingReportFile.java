package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class AccountingReportFile extends BaseAccountingReportFile {

    /**
     * Default constructor.
     */
    public AccountingReportFile() {
        super();
    }

    public static DomainEntitySet<AccountingReportFile> findByTypeAndStatus(AccountingReportFileType pFileType, AccountingReportFileStatus pPrintedCheckFileStatus, boolean pEagerLoadBatches) {
        Query<AccountingReportFile> query = new Query<AccountingReportFile>();
        query.Where(AccountingReportFile.Status().equalTo(pPrintedCheckFileStatus)
                .And(AccountingReportFile.Type().equalTo(pFileType)));

        if (pEagerLoadBatches) {
            switch (pFileType) {
                case PositivePay:
                    query.EagerLoad(AccountingReportFile.PositivePayFileBatchesSet());
                    break;
                case PrintedCheckReconPlus:
                    query.EagerLoad(AccountingReportFile.ReconPlusFileBatchesSet());
                    break;
            }
        }

        query.OrderBy(AccountingReportFile.CreatedDate());

        return Application.find(AccountingReportFile.class, query);
    }

    @Override
    public String toString() {
        return "AccountingReportFile -   Type: " + getType() + "  Status: " + getStatus() + "  CreatedDate: " + getCreatedDate();
    }
}