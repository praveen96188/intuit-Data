package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.security.PublicKey;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr 3, 2008
 * Time: 4:08:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResetCompanyPINCore extends Process implements IProcess {

    /**
     * Core process for resetting a company's PIN in order to allow a
     * connection to PSP.
     *
     * @author Marcela Villani
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mGeneratedPIN = null;
    private String mEncryptedPIN = null;
    private Company mCompany;
    private String mEncryptedSignedData;
    private String mUserId;
    private String mUniqueIdentifier;
    private int mLockAccountDuration;
    private boolean mPassedVerification;
    private boolean mLockAccount;
    private String mFailureReason = "";


    public ResetCompanyPINCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                               String pEncryptedSignedData, String pUserId) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEncryptedSignedData = pEncryptedSignedData;
        mUserId = pUserId;

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

        // Check if creating/updating PIN is allowed based on status
        if (!mCompany.isAllowedCapability(SystemCapabilityCode.UpdatePIN)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    mCompany.getSourceSystemCd().toString(),
                    mCompany.getSourceCompanyId(), SystemCapabilityCode.UpdatePIN.toString());
            return validationResult;
        }

        //  Retrieve Source Payroll Parameters - SourcePayrollParameterCode.LockAccountDuration  and
        // SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts

        mLockAccountDuration = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.LockAccountDuration).getParameterValue());

        int maxNumberOfAttempts = Integer.parseInt(SourcePayrollParameter.findSourcePayrollParameter(mSourceSystemCd,
                SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts).getParameterValue());
        mFailureReason = verifySignature();
    

        if (((mFailureReason.equals("")) &&
                (mCompany.getAccountLockedUntil() == null || PSPDate.getPSPTime().after(mCompany.getAccountLockedUntil())))) {
            mPassedVerification = true;
        } else {
            mPassedVerification = false;
            if (mCompany.getNumberOfFailedAuthentications() + 1 >= maxNumberOfAttempts) {
                mLockAccount = true;
                validationResult.getMessages().AccountLocked(EntityName.Company, mSourceCompanyId, Integer.toString(mLockAccountDuration));
            } else {
                mLockAccount = false;
                validationResult.getMessages().AuthenticationFailed(EntityName.Company, mSourceCompanyId,
                        mSourceSystemCd.toString(), mSourceCompanyId);
            }
            validationResult.merge(process());
        }
        return validationResult;
    }

    public ProcessResult<HashMap<String, String>> process() {
        ProcessResult<HashMap<String, String>> processResult = new ProcessResult();
        HashMap<String, String> pinInfo = new HashMap<String, String>();


        if (mPassedVerification) {
            // Change number of failed attempts to zero
            mCompany.setNumberOfFailedAuthentications(0);

            // Set AccountLockedUntil = null
            mCompany.setAccountLockedUntil(null);

            // Create PINReset event
            CompanyEvent.createPINResetEvent(mCompany, mUniqueIdentifier, mUserId);

            // Create PINUpdated event
            CompanyEvent.createPINUpdatedEvent(mCompany);

            // Add the generated Random PIN to the process result so it can be displayed once

            mGeneratedPIN = PINUtils.generateRandomPIN();
            pinInfo.put("PIN", mGeneratedPIN);
            pinInfo.put("PrivateKey", mCompany.getPrivateKey());
            processResult.setResult(pinInfo);

            // Remove all existing PINs and replace with the new one
            com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyPIN> companyPINCollection = mCompany.getCompanyPINCollection();
            while (companyPINCollection.size() > 0) {
                CompanyPIN companyPIN = companyPINCollection.get(0);
                mCompany.removeCompanyPIN(companyPIN);
                Application.delete(companyPIN);
            }

            mCompany.setAccountLockedUntil(null);

            // Add new generated PIN to the company
            CompanyPIN companyPIN = CompanyPIN.createCompanyPIN(mCompany, mGeneratedPIN);
            companyPIN = Application.save(companyPIN);

            mCompany.addCompanyPIN(companyPIN);

        } else {
            // Create Authentication Failed event
            CompanyEvent.createAuthenticationFailedEvent(mCompany, mFailureReason);

            // Add 1 to number of failed attempts
            mCompany.setNumberOfFailedAuthentications(mCompany.getNumberOfFailedAuthentications() + 1);
            
            if (mLockAccount) {
                SpcfCalendar pspTime = PSPDate.getPSPTime();
                pspTime.addMinutes(mLockAccountDuration);
                // Create account locked event
                CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.AccountLocked);
                mCompany.setAccountLockedUntil(pspTime);
            }
        }


        mCompany = Application.save(mCompany);

        return processResult;

    }

    private String verifySignature() {

        String failureReason = "";
        try {
            // Get the company's Public Key
            PublicKey publicKey = PINUtils.getPublicKeyFromString(mCompany.getPublicKey());
            if (publicKey == null)  {
                failureReason = "No public key found.";
                return failureReason;
            }

            //Decrypt Data
            String decValue = PINUtils.getDecryptedValue(mEncryptedSignedData, publicKey);

            //Compare
            String[] message = decValue.split(":");
            if (message.length < 4) {
                failureReason = "Missing data;";
            }
            if (!message[0].equals(mCompany.getFedTaxId())) {
                failureReason = failureReason + "Incorrect EIN;";
            }
            if (!message[1].equals(mCompany.getSourceCompanyId())) {
                failureReason = failureReason + "Incorrect CompanyId;";
            }

            EntitlementUnit eu = mCompany.getActivePrimaryEntitlementUnit();
            if (!message[2].equals(eu.getEntitlement().getSubscriptionNumber())) {
                failureReason = failureReason + "Incorrect SubscriptionNumber;";
            }

            // Find a PIN Reset Event with the same Unique Identifier
            mUniqueIdentifier = message[3];
            if (CompanyEvent.findCompanyEventDetails(mCompany, EventTypeCode.PINReset, EventDetailTypeCode.UniqueIdentifier, mUniqueIdentifier).size() > 0) {
                failureReason = failureReason + "PIN Reset identifier is not unique;";
            }
        }
        catch (Exception e) {
           failureReason = "Could not decrypt message.";
        }
        
        return failureReason;
    }
}
