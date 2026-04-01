package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsRequest;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Jeff Jones
 */
public class PSPTransmission {
    private SpcfCalendar mInitializeDateTime;
    private EwsRequest mRequest;
    private SpcfCalendar mFinalizeDateTime;
    private EwsResponse mResponse;
    private TransmissionType mTransmissionType;
    private String mTransmissionId;

    public PSPTransmission() {
        mTransmissionId = SpcfUniqueId.createInstance(true).toString();
    }

    public SpcfCalendar getInitializeDateTime() {
        return mInitializeDateTime;
    }

    public void setInitializeDateTime(SpcfCalendar pInitializeDateTime) {
        this.mInitializeDateTime = pInitializeDateTime;
    }

    public EwsRequest getRequest() {
        return mRequest;
    }

    public void setRequest(EwsRequest pRequest) throws Exception {
        this.mRequest = pRequest.clone();
    }

    public SpcfCalendar getFinalizeDateTime() {
        return mFinalizeDateTime;
    }

    public void setFinalizeDateTime(SpcfCalendar pFinalizeDateTime) {
        this.mFinalizeDateTime = pFinalizeDateTime;
    }

    public EwsResponse getResponse() {
        return mResponse;
    }

    public void setResponse(EwsResponse pResponse) throws Exception {
        this.mResponse = pResponse.clone();
    }

    public TransmissionType getTransmissionType() {
        return mTransmissionType;
    }

    public void setTransmissionType(TransmissionType pTransmissionType) {
        this.mTransmissionType = pTransmissionType;
    }

    public String getTransmissionId() {
        return mTransmissionId;
    }
}
