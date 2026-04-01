package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;


/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 8, 2010
 * Time: 7:49:18 AM
 */
public class UpdateEntitlementMessageCore extends Process implements IProcess {
    private String mEntitlementMessageId;
    private String mLicenseNumber;
    private EntitlementMessage mEntitlementMessage;
    private String mErrorMessage;
    private EntitlementMessageStatusCode mStatus;

    public UpdateEntitlementMessageCore(String pEntitlementMessageId, String plicenseNumber, EntitlementMessageStatusCode pStatus,  String pErrorMessage) {
        mEntitlementMessageId = pEntitlementMessageId;
        mLicenseNumber = plicenseNumber;
        mStatus = pStatus;
        mErrorMessage = pErrorMessage;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementMessageId == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, "null", "EntitlementMessageId");
            return validationResult;
        }

        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_LICENSE_NUMBER_FILTER)) {
            mEntitlementMessage = EntitlementMessage.findEntitlementMessagesByIdAndLicenseNumber(mEntitlementMessageId, mLicenseNumber);
        } else {
            mEntitlementMessage = Application.findById(EntitlementMessage.class, SpcfUniqueId.createInstance(mEntitlementMessageId));
        }
        if (mEntitlementMessage == null) {
            validationResult.getMessages().EntitlementMessageDoesNotExist(EntityName.EntitlementMessage, mEntitlementMessageId, mEntitlementMessageId);
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult<EntitlementMessage> process() {
        ProcessResult<EntitlementMessage> processResult = new ProcessResult<EntitlementMessage>();

        if(mErrorMessage != null) {
            if(mErrorMessage.length() > 1000) {
                mErrorMessage = mErrorMessage.substring(0, 1000);
            }

            long maxFailureCount = SystemParameter.findLongValue(SystemParameter.Code.AMO_MAX_MESSAGE_FAILURE_COUNT, 5);
            mEntitlementMessage.setFailureCount(mEntitlementMessage.getFailureCount() + 1);
            if(mEntitlementMessage.getFailureCount() >= maxFailureCount) {
                mEntitlementMessage.setStatus(EntitlementMessageStatusCode.Error);
            }
            mEntitlementMessage.setLastFailureMessage(mErrorMessage);
        } else {
            if (mStatus != null) {
                mEntitlementMessage.setStatus(mStatus);
            } else {
                mEntitlementMessage.setStatus(EntitlementMessageStatusCode.Processed);
            }
            mEntitlementMessage.setFailureCount(0);
            mEntitlementMessage.setLastFailureMessage(null);
        }

        mEntitlementMessage = Application.save(mEntitlementMessage);
        processResult.setResult(mEntitlementMessage);

        return processResult;
    }
}
