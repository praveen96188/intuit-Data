package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Mar 25, 2008
 * Time: 4:59:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class VerifyCompanyPINCore extends Process implements IProcess {
    /**
     * Core process for  verifying a company's PIN in order to allow a
     * connection to PSP.
     * If the incorrect PIN is sent 3 times the account gets locked for the next 15 minutes
     * If the company tries again after 15 minutes and the PIN is correct the account gets
     * unlocked automatically
     *
     * @author Marcela Villani
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPIN;
    private Company mCompany;
    private boolean mPassedVerification = false;
    private boolean mLockAccount = false;
    private int mLockAccountDuration;


    public VerifyCompanyPINCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                String pPIN) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mPIN = pPIN;

    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if PIN is not null
        if (mPIN == null) {
            validationResult.getMessages().InvalidArgument(EntityName.Company, mSourceCompanyId, "PIN");
            return validationResult;
        }

        // Check if Company exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        //  Retrieve Source Payroll Parameters - SourcePayrollParameterCode.LockAccountDuration  and
        // SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts

        mLockAccountDuration = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.LockAccountDuration).getParameterValue());

        int maxNumberOfFailedLoginAttempts = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts).getParameterValue());

        // Iterate over Company PINs.
        Iterator<CompanyPIN> pinIterator = mCompany.getCompanyPINCollection().iterator();
        CompanyPIN companyPIN = null;
        String encryptedPIN = null;
        while ( pinIterator.hasNext() ) {
            companyPIN = pinIterator.next();

            // Determine hash type and encrypt using the hash type.
            HashType hashType = companyPIN.getHashType();
            encryptedPIN = PINUtils.encrypt(mPIN, hashType);

            if (encryptedPIN.equals(companyPIN.getPINValue())) {
                // Found a match.
                break;
            }

            companyPIN = null;
        }

        // If we found a match, but it is not using the latest hash type algorithm, migrate to the latest.
        if (companyPIN != null && !PINUtils.CURRENT_HASH_TYPE.equals(companyPIN.getHashType())) {
            encryptedPIN = PINUtils.encrypt(mPIN, PINUtils.CURRENT_HASH_TYPE);
            companyPIN.setPINValue(encryptedPIN);
            companyPIN.setHashType(PINUtils.CURRENT_HASH_TYPE);
        }

        if (companyPIN != null && ((encryptedPIN.equals(companyPIN.getPINValue())) &&
                (mCompany.getAccountLockedUntil() == null || PSPDate.getPSPTime().after(mCompany.getAccountLockedUntil())))) {
            mPassedVerification = true;
        } else {
            mPassedVerification = false;
            if (mCompany.getNumberOfFailedLoginAttempts() + 1 >= maxNumberOfFailedLoginAttempts) {
                mLockAccount = true;
                validationResult.getMessages().AccountLocked(EntityName.Company, mSourceCompanyId, Integer.toString(mLockAccountDuration));
            } else {
                mLockAccount = false;
                validationResult.getMessages().PinNotRecognized(EntityName.Company, mSourceCompanyId, Integer.toString(mLockAccountDuration), Integer.toString(maxNumberOfFailedLoginAttempts));
            }
            validationResult.merge(process());
        }
        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (mPassedVerification) {
            // Change number of failed attempts to zero
            mCompany.setNumberOfFailedLoginAttempts(0);

            // Set AccountLockedUntil = null
            mCompany.setAccountLockedUntil(null);

            // If any of the keys for the company is null, regenerate the key pair

            if (mCompany.getPrivateKey() == null || mCompany.getPublicKey() == null) {
                String[] keyPair = PINUtils.generateKeyPair();
                mCompany.setPrivateKey(keyPair[0]);
                mCompany.setPublicKey(keyPair[1]);

                // Create KeyPairGenerated  event
                CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.KeyPairGenerated);
            }

        } else {
            // Create Incorrect PIN event
            CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.IncorrectPIN);
            // Add 1 to number of failed attempts
            mCompany.setNumberOfFailedLoginAttempts(mCompany.getNumberOfFailedLoginAttempts() + 1);
            if (mLockAccount) {
                SpcfCalendar pspTime = PSPDate.getPSPTime();
                pspTime.addMinutes(mLockAccountDuration);
                // Create account locked event
                CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.AccountLocked);
                mCompany.setAccountLockedUntil(pspTime);
            }
        }

        mCompany = Application.save(mCompany);

        processResult.setResult(mCompany);
        return processResult;
    }
}
