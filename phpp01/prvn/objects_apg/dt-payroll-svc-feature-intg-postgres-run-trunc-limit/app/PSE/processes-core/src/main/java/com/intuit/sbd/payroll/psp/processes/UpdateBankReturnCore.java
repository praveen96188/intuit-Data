package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jan 28, 2008
 * Time: 9:09:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateBankReturnCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPSETransactionId;
    private Company mCompany;
    private TransactionReturnStatusCode mBankReturnStatus;
    private String mCompanyNote;
    private FinancialTransaction mFinancialTransaction;

    public UpdateBankReturnCore(SourceSystemCode pSourceSystemCode,
                                String pSourceCompanyId,
                                String pPSETransactionId,
                                TransactionReturnStatusCode pBankReturnStatus,
                                String pCompanyNote) {

        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.mPSETransactionId = pPSETransactionId;
        this.mBankReturnStatus = pBankReturnStatus;
        this.mCompanyNote = pCompanyNote;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check financial transaction id is null
        if (mPSETransactionId == null || mPSETransactionId.length() == 0) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        try {
            SpcfUniqueId.createInstance(mPSETransactionId);
        }
        catch (SpcfIllegalArgumentException ex) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify the existence of the Financial Tx for the company
        mFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(mPSETransactionId));
        if (mFinancialTransaction == null) {
            validationResult.getMessages()
                    .FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                            mPSETransactionId, mPSETransactionId, mSourceSystemCd.toString(), mSourceCompanyId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        TransactionReturn transactionReturn = null;
        if (mBankReturnStatus == TransactionReturnStatusCode.Resolved) {
            transactionReturn = TransactionReturn.findFirstUnresolvedTransactionReturn(mFinancialTransaction);
        } else {
            transactionReturn = TransactionReturn.findFirstResolvedTransactionReturn(mFinancialTransaction);
        }

        if (transactionReturn != null) {
            transactionReturn.updateTransactionReturnStatus(mBankReturnStatus);

            // Inactivate NOC and PayrollSubmittedWithPendingNOC events
            if (transactionReturn.getBankReturnCd().toUpperCase().startsWith("C")){
                EmployeeBankAccount eeBankAccount = mFinancialTransaction.getEmployeeBankAccount();
                if (eeBankAccount != null) {
                    String eeBAId = eeBankAccount.getId().toString();
                    DomainEntitySet<CompanyEventDetail> localNocEventDetails =
                            CompanyEvent.findCompanyEventDetails(
                            mCompany, EventTypeCode.NOC,
                            EventDetailTypeCode.EmployeeBankAccountId, eeBAId);
                    for (CompanyEventDetail eventDetail : localNocEventDetails) {
                        eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                    }

                    localNocEventDetails = CompanyEvent.findCompanyEventDetails(
                            mCompany, EventTypeCode.PayrollSubmittedWithPendingNOC,
                            EventDetailTypeCode.EmployeeBankAccountId, eeBAId);
                    for (CompanyEventDetail eventDetail : localNocEventDetails) {
                        eventDetail.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
                    }
                }
            }

            String noteMessage = "Bank Return status changed to " + mBankReturnStatus + ".  ";
            noteMessage += "PSE Transaction Id: " + mFinancialTransaction.getId() + ".  ";
            if (mCompanyNote != null) {
                noteMessage += mCompanyNote;
            }

            //Since database can accommodate only 4000 characters, getting first 4000 chars from the string.
            if(noteMessage.length() > 4000){
                noteMessage = noteMessage.substring(0,4000);
            }

            PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

            // Add note
            CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.ManualNoteEvent);
            CompanyNote companyNote = new CompanyNote();
            companyNote.setInsertUserId(principal.getId());
            companyNote.setNotes(noteMessage);
            companyNote.setCompany(mCompany);
            companyEvent.setNoteLastUpdatedDate(PSPDate.getPSPTime());
            companyNote.setCompanyEvent(companyEvent);
            companyNote = Application.save(companyNote);
            mCompany.addCompanyNote(companyNote);
            Application.save(mCompany);
        }
        return processResult;
    }


}
