package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;

/**
 * User: mvillani
 * Date: Nov 14, 2007
 * Time: 4:09:00 PM
 */
public class UpdateVoidedAfterOffloadCore extends Process implements IProcess {

    /**
     * Core process for updating a paycheck's VoidedAfterOffload Indicator
     *
     * @author Marcela Villani
     */


    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mSourcePaycheckId;
    private Boolean mVoidedAfterOffload;
    private Company mCompany;
    private CompanyAdjustmentSubmission mCompanyVoid;
    private Paycheck mPaycheck;
    private String mTransmissionId;

    public UpdateVoidedAfterOffloadCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        String pSourcePaycheckId, Boolean pVoidedAfterOffload, String pTransmissionId) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mSourcePaycheckId = pSourcePaycheckId;
        mVoidedAfterOffload = pVoidedAfterOffload;
        mTransmissionId = pTransmissionId;
    }

    public ProcessResult<Paycheck> process() {
        ProcessResult processResult = new ProcessResult();

        // Associate payroll with transmission
        if (mTransmissionId != null) {
            TransmissionPayrollRun transmissionPayrollRun = new TransmissionPayrollRun();
            transmissionPayrollRun.setPayrollRun(mPaycheck.getPayrollRun());
            SourceSystemTransmission transmissionSecondary = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mTransmissionId);
            transmissionPayrollRun.setSourceSystemTransmissionId(transmissionSecondary.getId().toString());
            transmissionPayrollRun.setPayrollProcess(PayrollProcessCode.UpdateTransactionVoidFlag);
            transmissionPayrollRun = Application.save(transmissionPayrollRun);
            transmissionSecondary.addTransmissionPayrollRun(transmissionPayrollRun);
            mPaycheck.getPayrollRun().addTransmissionPayrollRun(transmissionPayrollRun);

        }

        if (mVoidedAfterOffload) {
            mCompanyVoid = new CompanyAdjustmentSubmission();
            mCompanyVoid.setCompany(mCompany);
            mCompanyVoid.setSubmissionDate(PSPDate.getPSPTime());
            mPaycheck.setCompanyAdjustmentSubmission(mCompanyVoid);

            //added for checksum dd service source approval date changing approval date time in case of voided after offload
            mPaycheck.setApprovalDateTimeEnd(PSPDate.getPSPTime());

            mCompanyVoid = Application.save(mCompanyVoid);

            if (mCompany.getSourceSystemCd() == SourceSystemCode.QBDT) {
                if (mPaycheck.getQbdtPaycheckInfo() != null &&
                        mPaycheck.getPaycheckSplitCollection() != null &&
                        !mPaycheck.getPaycheckSplitCollection().isEmpty()) {
                    String memo = mPaycheck.getQbdtPaycheckInfo().getMemo();
                    if (memo == null) {
                        memo = Paycheck.VOID_FUNDS_NOT_RECOVERED;
                    } else {
                        memo += " " + Paycheck.VOID_FUNDS_NOT_RECOVERED;
                    }
                    mPaycheck.getQbdtPaycheckInfo().setMemo(memo);
                }
            }
        }

        mPaycheck = Application.save(mPaycheck);

        processResult.setResult(mPaycheck);
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if Paycheck Exists
        mPaycheck = Paycheck.findPaycheck(mCompany, mSourcePaycheckId);
        if (mPaycheck == null) {
            validationResult.getMessages().PaycheckDoesNotExist(EntityName.PayCheck, mSourcePaycheckId, mSourceSystemCd.toString(), mSourceCompanyId, mSourcePaycheckId);
        }
        return validationResult;
    }
}