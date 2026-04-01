package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.DeletedRecord;
import com.intuit.sbd.payroll.psp.domain.LedgerBalance;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 2/1/12
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecalculateCompanyLedgerBalances extends Process implements IProcess {
    private String mSourceCompanyId;
    private SourceSystemCode mSourceSystemCd;
    private Company mCompany;
    private final static SpcfLogger logger = Application.getLogger(RecalculateCompanyLedgerBalances.class);

    public RecalculateCompanyLedgerBalances(SourceSystemCode pSourceSystemCd, String pSourceCompanyId) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCd;
    }

    @Override
    public ProcessResult validate() {

        // validate company parameters
        ProcessResult validationResult = Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult result = new ProcessResult();
        try {

            DomainEntitySet<LedgerBalance> ledgerBalances = Application.find(LedgerBalance.class, LedgerBalance.Company().equalTo(mCompany));
            DeletedRecord deletedRecord;
            for (LedgerBalance ledgerBalance : ledgerBalances) {
                deletedRecord = new DeletedRecord();
                deletedRecord.setRecordIdentifier(ledgerBalance.getId().toString());
                deletedRecord.setTableName("PSP_LEDGER_BALANCE");
                Application.save(deletedRecord);
                Application.delete(ledgerBalance);
            }

            logger.info("Flushing Hibernate cache.");
            Application.getHibernateSession().flush();
            logger.info("Calling storedProcedure="+StoredProcedures.PRC_UPD_COMPANY_LEDGER_BALANCE.getStoredProcedureName() +
                    " companyId="+mCompany.getId().toString());
            Application.executeSqlProcedure(StoredProcedures.PRC_UPD_COMPANY_LEDGER_BALANCE, false, Pair.of(String.class, mCompany.getId().toString()));

            logger.info("Completed PRC_UPD_COMPANY_LEDGER_BALANCE stored procedure.");

        } catch (Throwable t) {
            throw new RuntimeException("Exception in RecalculateCompanyLedgerBalances ", t);
        }

        return result;
    }
}
