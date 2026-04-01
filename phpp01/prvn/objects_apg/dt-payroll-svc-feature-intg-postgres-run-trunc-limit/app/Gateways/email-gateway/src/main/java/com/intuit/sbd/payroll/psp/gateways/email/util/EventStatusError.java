package com.intuit.sbd.payroll.psp.gateways.email.util;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 21, 2008
 * Time: 3:09:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventStatusError {
    private EventStatusErrorType mErrorType;
    private String mErrorDetail = null;

    public EventStatusError(String pErrorDetail, EventStatusErrorType pErrorType) {
        mErrorDetail = pErrorDetail;
        mErrorType = pErrorType;
    }

    public EventStatusErrorType getErrorType() {
        return mErrorType;
    }

    public String getErrorDetail() {
        return mErrorDetail;
    }
}
