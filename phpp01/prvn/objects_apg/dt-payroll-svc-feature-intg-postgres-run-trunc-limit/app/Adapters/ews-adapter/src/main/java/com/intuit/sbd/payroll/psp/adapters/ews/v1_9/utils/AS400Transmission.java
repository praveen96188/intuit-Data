package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Jeff Jones
 */
public class AS400Transmission {


    private SpcfCalendar mInitializeDateTime;
    private PSIMessageWSDTO mRequest;
    private SpcfCalendar mFinalizeDateTime;
    private PSIMessageWSDTO mResponse;
    private TransmissionType mTransmissionType;
    private String mTransmissionId;

    public AS400Transmission() {
        mTransmissionId = SpcfUniqueId.createInstance(true).toString();
    }

    public SpcfCalendar getInitializeDateTime() {
        return mInitializeDateTime;
    }

    public void setInitializeDateTime(SpcfCalendar pInitializeDateTime) {
        this.mInitializeDateTime = pInitializeDateTime;
    }

    public PSIMessageWSDTO getRequest() {
        return mRequest;
    }

    public void setRequest(PSIMessageWSDTO pRequest) {
        this.mRequest = pRequest;
    }

    public SpcfCalendar getFinalizeDateTime() {
        return mFinalizeDateTime;
    }

    public void setFinalizeDateTime(SpcfCalendar pFinalizeDateTime) {
        this.mFinalizeDateTime = pFinalizeDateTime;
    }

    public PSIMessageWSDTO getResponse() {
        return mResponse;
    }

    public void setResponse(PSIMessageWSDTO pResponse) {
        this.mResponse = pResponse;
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
