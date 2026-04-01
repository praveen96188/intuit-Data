package com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile;

import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import lombok.Getter;

@Getter
public class PaymentsProfileClientException extends RuntimeException {

    private HttpServiceResponse httpServiceResponse;

    public PaymentsProfileClientException(String message, HttpServiceResponse httpServiceResponse) {
        super(message);
        this.httpServiceResponse = httpServiceResponse;
    }
}
