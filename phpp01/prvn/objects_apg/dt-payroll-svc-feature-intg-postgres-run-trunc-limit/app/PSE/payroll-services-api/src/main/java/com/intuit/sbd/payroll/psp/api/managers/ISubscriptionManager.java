package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.Company;

import java.util.HashMap;

/**
 * This is the PSP service API that deals with pricing
 */
public interface ISubscriptionManager {
    /**
     * Verifies that a PIN is valid and locks the account after a number of failed attempts.
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pPIN
     * @return
     */
    ProcessResult<Company> verifyCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN);

    /**
     * Verifies that an EIN is valid
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pEIN
     * @return
     */
    ProcessResult<Company> verifyCompanyEIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pEIN, String pSubscriptionNum);

    /**
     * Creates a new PIN for a company
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pPIN
     * @return
     */
    ProcessResult<HashMap<String,String>> createCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN);

    /**
     * Updates a PIN for a Company
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pPIN
     * @return
     */
    ProcessResult<HashMap<String,String>> updateCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN);

    /**
     * Resets a PIN for a Company
     *
     * @param pSourceSystemCd
     * @param pSourceCompanyId
     * @param pEncryptedData
     * @param pUserId
     * @return
     */
    ProcessResult<HashMap<String,String>> resetCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pEncryptedData, String pUserId);


    ProcessResult unlockPINOnce(SourceSystemCode pSourceSystemCd, String pSourceCompanyId);

    /**
     * Answers if an EIN is in PSP
     *
     * @param pSourceSystemCode
     * @param pEIN
     * @return
     */
    boolean isEINInPsp(SourceSystemCode pSourceSystemCode, String pEIN);

    /**
     * Answers if a new company, for the passed service, should be added to PSP
     *
     * @param pSourceSystemCode
     * @param pServiceCode
     * @return
     */
    boolean shouldAddCompanyToPsp(SourceSystemCode pSourceSystemCode, ServiceCode pServiceCode);
}
