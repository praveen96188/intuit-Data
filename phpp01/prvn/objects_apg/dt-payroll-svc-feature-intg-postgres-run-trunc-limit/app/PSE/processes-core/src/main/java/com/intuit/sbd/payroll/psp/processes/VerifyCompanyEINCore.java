package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created with IntelliJ IDEA.
 * User: Tiger Shao
 * Date: 6/19/12
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class VerifyCompanyEINCore extends Process implements IProcess {
    /**
     * Core process for  verifying a company's EIN in order to allow a
     * connection to PSP.
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mEIN;
    private String mSubscriptionNum;
    private Company mCompany;
    private boolean mPassedVerification = false;

    public VerifyCompanyEINCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                String pEIN, String pSubscriptionNum) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEIN = pEIN;
        mSubscriptionNum = pSubscriptionNum;
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

        // check EIN
        String ein = mCompany.getFedTaxId();
        if (mEIN == null || ein == null) {
            validationResult.getMessages().EinNotRecognized(EntityName.CompanyBankAccount, mSourceCompanyId);
            return validationResult;
        }

        String filteredDBEIN = ein.replaceAll("-", "");
        String filteredEIN = mEIN.replaceAll("-", "");
        if (filteredEIN.length() < 3 || filteredDBEIN.length() < 3 || !filteredEIN.substring(2).equals(filteredDBEIN.substring(2))) {
            validationResult.getMessages().EinNotRecognized(EntityName.CompanyBankAccount, mSourceCompanyId);
            return validationResult;
        }

        // check SubscriptionNum
        EntitlementUnit primaryEntitlementUnit = mCompany.getActivePrimaryEntitlementUnit();
        if (primaryEntitlementUnit != null) {
            String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
            if (subscriptionNumber != null && (mSubscriptionNum == null || !mSubscriptionNum.equals(subscriptionNumber))) {
                validationResult.getMessages().SubscriptionNumberNotRecognized(EntityName.CompanyBankAccount, mSourceCompanyId);
                return validationResult;
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        processResult.setResult(mCompany);
        return processResult;
    }
}

