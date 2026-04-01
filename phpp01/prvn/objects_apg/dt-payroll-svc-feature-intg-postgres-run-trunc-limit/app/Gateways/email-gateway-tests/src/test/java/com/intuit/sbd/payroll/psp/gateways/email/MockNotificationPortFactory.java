package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.INotificationPortFactory;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 8/21/12
 * Time: 12:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockNotificationPortFactory implements INotificationPortFactory {
    private ErrorDataType mErrToReturn = null;
    private String mErrRecipientId = null;
    private int mErrOnIteration = 0;

    public void clearErrorStatus() {
        mErrToReturn = null;
        mErrRecipientId = null;
        mErrOnIteration = 0;
    }

    public void forceInputError(int pErrOnIteration) {
        mErrOnIteration = pErrOnIteration;

        //
        // Set up the desired error we want to see from the mock service
        // Note: The following error is what we normally see for "Rejected by filter" error from NTF (for entire request)
        //
        mErrToReturn = new ErrorDataType();
        mErrToReturn.setCategory("INPUT");
        mErrToReturn.setCode("PROXY-SP-10502");
        mErrToReturn.setDescription("Rejected by filter");
        mErrToReturn.setSource("MockNotificationPort"); // normally we'd see something like: datapower/qypprdsrvenap02
    }

    public void forceSystemError(int pErrOnIteration) {
        mErrOnIteration = pErrOnIteration;

        //
        // Set up the desired error we want to see from the mock service
        // Note: The following error is what we normally see for "JMS Failure" error from NTF (for entire request)
        //
        mErrToReturn = new ErrorDataType();
        mErrToReturn.setCategory("SYSTEM");
        mErrToReturn.setCode("PROXY-SP-10501");
        mErrToReturn.setDescription("Failed to queue message for retry. JMS Failure");
        mErrToReturn.setSource("MockNotificationPort"); // normally we'd see something like: datapower/qypprdsrvenap01
    }

    public void forceTransitoryError(int pErrOnIteration) {
        mErrOnIteration = pErrOnIteration;

        //
        // Set up the desired error we want to see from the mock service
        // Note: We've never seen this error type before, so not sure how to mock...
        //
        mErrToReturn = new ErrorDataType();
        mErrToReturn.setCategory("TRANSITORY");
        mErrToReturn.setCode("");
        mErrToReturn.setDescription("");
        mErrToReturn.setSource("MockNotificationPort");
    }

    public void forceBusinessLogicError(String pErrRecipientId) {
        mErrRecipientId = pErrRecipientId;

        //
        // Set up the desired error we want to see from the mock service
        // Note: The following error is other than "List Detective" error from NTF (for individual email recipients)
        //
        mErrToReturn = new ErrorDataType();
        mErrToReturn.setCategory("BUSINESS_LOGIC");
        mErrToReturn.setCode("SEND-NTS-31010");
        mErrToReturn.setDescription("Unable to deliver to notification address. Unknown reason");
        mErrToReturn.setSource("MockNotificationPort"); // normally we'd see something like: datapower/qypprdsrvenap02
    }

    public void forceListDetectiveError(String pErrRecipientId) {
        mErrRecipientId = pErrRecipientId;

        //
        // Set up the desired error we want to see from the mock service
        // Note: The following error is what we normally see for "List Detective" error from NTF (for individual email recipients)
        //
        mErrToReturn = new ErrorDataType();
        mErrToReturn.setCategory("BUSINESS_LOGIC");
        mErrToReturn.setCode("SEND-NTS-31010");
        mErrToReturn.setDescription("Unable to deliver to notification address. Unknown reason (Error Code: 24 - Subscriber was excluded by List Detective.)");
        mErrToReturn.setSource("MockNotificationPort"); // normally we'd see something like: datapower/qypprdsrvenap02
    }

    public NotificationPort getNotificationPort() {
        if (mErrToReturn != null) {
            switch (NtfErrorCategory.valueOf(mErrToReturn.getCategory())) {
                case INPUT:
                case SYSTEM:
                case TRANSITORY:
                    return new MockNotificationPort(mErrToReturn, mErrOnIteration);

                case BUSINESS_LOGIC:
                    return new MockNotificationPort(mErrToReturn, mErrRecipientId);
            }
        }

        return new MockNotificationPort();
    }
}
