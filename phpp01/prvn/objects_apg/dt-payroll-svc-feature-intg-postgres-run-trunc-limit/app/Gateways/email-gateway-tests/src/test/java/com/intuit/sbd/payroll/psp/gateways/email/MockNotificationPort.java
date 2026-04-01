package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.ias.common.xsd.ErrorDataType;
import com.intuit.ias.common.xsd.HeaderDataType;
import com.intuit.ias.notification.pub.wsdl.Fault;
import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.ias.notification.pub.xsd.NotificationDataType;
import com.intuit.ias.notification.pub.xsd.SendRequest;
import com.intuit.ias.notification.pub.xsd.SendResponse;
import com.intuit.ias.notification.pub.xsd.SuccessDataType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 8/21/12
 * Time: 12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockNotificationPort implements NotificationPort {
    private ErrorDataType mErrToReturn = null;
    private String mErrRecipientId = "";
    private int mErrOnIteration = 0;
    private int mCurIteration = 0;

    public MockNotificationPort() {}

    public MockNotificationPort(ErrorDataType pErrToReturn, int pErrOnIteration) {
        mErrToReturn = pErrToReturn;
        mErrOnIteration = pErrOnIteration;
    }

    public MockNotificationPort(ErrorDataType pErrToReturn, String pErrRecipientId) {
        mErrToReturn = pErrToReturn;

        if (pErrRecipientId != null) {
            mErrRecipientId = pErrRecipientId;
        }
    }

    public SendResponse send(@WebParam(name = "Header",
                                       targetNamespace = "http://www.intuit.com/ias/common/xsd",
                                       header = true,
                                       mode = WebParam.Mode.INOUT,
                                       partName = "header") Holder<HeaderDataType> header,
                             @WebParam(name = "SendRequest",
                                       targetNamespace = "http://www.intuit.com/ias/notification/pub/xsd",
                                       partName = "body") SendRequest body) throws Fault {
        ++mCurIteration;
        return buildResponse(body);
    }

    private SendResponse buildResponse(SendRequest pRequestBody) throws Fault {
        if (mErrToReturn != null) {
            switch (NtfErrorCategory.valueOf(mErrToReturn.getCategory())) {
                case INPUT:
                case SYSTEM:
                case TRANSITORY:
                    if (mCurIteration == mErrOnIteration) {
                        throw new Fault(String.format("Send: %s", mErrToReturn.getDescription()), mErrToReturn);
                    }
                    break;
            }
        }

        return buildSuccessResponse(pRequestBody);
    }

    private SendResponse buildSuccessResponse(SendRequest pRequestBody) {
        SendResponse response = new SendResponse();
        NtfErrorCategory errCategory = NtfErrorCategory.NONE;

        if (mErrToReturn != null) {
            errCategory = NtfErrorCategory.valueOf(mErrToReturn.getCategory());
        }

        for (NotificationDataType.Destinations.Destination dest : pRequestBody.getNotification().getDestinations().getDestination()) {
            SendResponse.Result result = new SendResponse.Result();

            if ((errCategory == NtfErrorCategory.BUSINESS_LOGIC) && mErrRecipientId.equals(dest.getRecipientId())) {
                result.setError(mErrToReturn);
            } else {
                result.setSuccess(new SuccessDataType());
            }

            result.setRecipientId(dest.getRecipientId());

            response.getResult().add(result);
        }

        return response;
    }
}
