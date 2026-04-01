package com.intuit.sbd.payroll.psp.emailsender.exception;

import com.google.gson.Gson;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Custom exception for OINP Failures.
 *
 * @author nramesh1
 */
public class OINPServicesException extends EmailServiceException {

    private HttpServiceResponse httpServiceResponse;
    @Autowired
    private Gson jsonConverter;

    public OINPServicesException(String message, Throwable cause)
    {
        super(message, cause);
    }
    public OINPServicesException(String message, HttpServiceResponse httpServiceResponse) {
        super(message);
        this.httpServiceResponse = httpServiceResponse;
        jsonConverter = new Gson();
    }

    public HttpServiceResponse getHttpServiceResponse() {
        return httpServiceResponse;
    }

    public String getErrorMessage(){
        StringBuilder result = new StringBuilder();
        if (Objects.isNull(this.httpServiceResponse))
            return result.toString();

        switch (this.httpServiceResponse.getStatusCode()) {
            case 403:
                result.append(httpServiceResponse.getMessage());
                break;
            default:
                setErrorMessageDefault(result);
        }
        return result.toString();
    }

    protected void setErrorMessageDefault(StringBuilder result) {
        result.append(HttpStatus.resolve(getHttpServiceResponse().getStatusCode()).name());
    }

}

