package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.PINUtils;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Apr 3, 2008
 * Time: 4:08:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateCompanyPINCore extends Process implements IProcess {

    /**
     * Core process for creating a company's PIN in order to allow a
     * connection to PSP.
     *
     * @author Marcela Villani
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mPIN;
    private String mGeneratedPIN = null;
    private String mEncryptedPIN = null;
    private Company mCompany;


    public CreateCompanyPINCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
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

        // If PIN != null , Validate PIN Format, otherwise generate a random PIN

        if (mPIN != null) {
            if (!PINUtils.validatePINFormat(mPIN)) {
                validationResult.getMessages().InvalidPINFormat(EntityName.Company, mPIN);
                return validationResult;
            }
        } else {
            mGeneratedPIN = PINUtils.generateRandomPIN();
            mPIN = mGeneratedPIN;

        }
        return validationResult;
    }

    public ProcessResult<HashMap<String, String>> process() {
        ProcessResult<HashMap<String, String>> processResult = new ProcessResult();
        HashMap<String, String> pinInfo = new HashMap<String, String>();

        ///////////////// PSRV001090 - Removing 15 minute lockout and PIN reset count
        // Change number of failed attempts to zero
        mCompany.setNumberOfFailedAuthentications(0);

        // Set AccountLockedUntil = null
        mCompany.setAccountLockedUntil(null);
        ///////////////// PSRV001090        

        // Create PINCreated event
        CompanyEvent.createPINCreatedEvent(mCompany);

        // Create KeyPairGenerated  event
        CompanyEvent.createCompanyEvent(mCompany, EventTypeCode.KeyPairGenerated);

        //Set each service status to the next valid one
        for (CompanyService companyService : mCompany.getCompanyServiceCollection()) {
                ServiceSubStatusCode nextServiceSubStatusCd = companyService.getNextValidServiceStatus(ServiceSubStatusCode.PendingPinCreation);
                companyService.updateCompanyServiceStatus(nextServiceSubStatusCd);
        }


        // Remove all existing PINs and replace with the new one
        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyPIN> companyPINCollection = mCompany.getCompanyPINCollection();
        while (companyPINCollection.size() > 0) {
            CompanyPIN companyPIN = companyPINCollection.get(0);
            mCompany.removeCompanyPIN(companyPIN);
            Application.delete(companyPIN);
        }

        // Add the generated Random PIN to the process result so it can be displayed once
        pinInfo.put("PIN", mPIN);
        // Generate private/public Key Pair
        String[] keyPair = PINUtils.generateKeyPair();
        pinInfo.put("PrivateKey", keyPair[0]);
        processResult.setResult(pinInfo);

        CompanyPIN companyPIN = CompanyPIN.createCompanyPIN(mCompany, mPIN);
        companyPIN = Application.save(companyPIN);

        mCompany.addCompanyPIN(companyPIN);
        mCompany.setPrivateKey(keyPair[0]);
        mCompany.setPublicKey(keyPair[1]);
        mCompany = Application.save(mCompany);

        return processResult;

    }
}
