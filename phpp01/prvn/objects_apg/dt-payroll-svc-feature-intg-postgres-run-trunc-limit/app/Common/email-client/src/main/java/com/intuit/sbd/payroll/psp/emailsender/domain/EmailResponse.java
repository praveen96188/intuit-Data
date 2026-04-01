package com.intuit.sbd.payroll.psp.emailsender.domain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResults;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.sun.jersey.api.client.ClientResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.Map;

/**
 * @author vishalb849
 */
@Getter
@Setter
@Component
public class EmailResponse implements Serializable {
    private String responseBody;
    private String attachmentKey;
    private String traceId;
    private int status;
    private String statusInfo;
    private MultivaluedMap<String, String> headers;
    private ClientResponse clientResponse;
    private ExactTargetResults results;
    //added for OINP via web-services-client TODO: After phase 3, replace headers with httpServiceHeaders
    private HttpServiceResponse httpServiceResponse;
    private Map<String, String> httpServiceResponseHeaders;

    public EmailResponse(ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
        responseBody = clientResponse.getEntity(String.class);
        headers = clientResponse.getHeaders();
        status = clientResponse.getStatus();
    }

    public EmailResponse(HttpServiceResponse httpServiceResponse) {
        this.httpServiceResponse = httpServiceResponse;
        responseBody = httpServiceResponse.getMessage();
        status = httpServiceResponse.getStatusCode();
        httpServiceResponseHeaders = httpServiceResponse.getHeaders();
    }

    public EmailResponse() {
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getAttachmentKey() {
        JsonObject jsonObject = new Gson().fromJson(getResponseBody(), JsonObject.class);
        if(jsonObject.get("s3Key")!=null){
            attachmentKey = jsonObject.get("s3Key").getAsString();
        }
        return attachmentKey;
    }

    public String getTraceId() {
        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
        if(jsonObject.get("traceId")!=null){
            traceId = jsonObject.get("traceId").toString();
            traceId = traceId.replaceAll("^\"|\"$", "");
        }
        return traceId;
    }

    public EmailResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public EmailResponse setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
        return this;
    }

    public ExactTargetResults getResult() {
        return results;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Email Response [status=");
        builder.append(status);
        builder.append(", traceId=");
        builder.append(responseBody);
        builder.append(", statusInfo=");
        builder.append(statusInfo);
        builder.append(", headers=");
        builder.append(headers);
        builder.append("]");
        return builder.toString();
    }
}