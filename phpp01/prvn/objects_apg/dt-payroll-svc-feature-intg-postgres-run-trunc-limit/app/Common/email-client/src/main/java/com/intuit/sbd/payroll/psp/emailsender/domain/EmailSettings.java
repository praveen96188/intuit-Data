package com.intuit.sbd.payroll.psp.emailsender.domain;

import com.intuit.sbd.payroll.psp.emailsender.filter.RequestFilter;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vishalb849
 */
@Getter
@Component
public class EmailSettings {

    private String url;
    private int postRetryCount;
    private int postRetryIntervalExponential;
    private List<RequestFilter> requestFilters;
    private String sendgridApi;
    private String sendGridUploadAttachmentApi;
    private String exactTargetApi;

    public EmailSettings(String url, int postRetryCount, int postRetryIntervalExponential, String sendgridApi, String
            sendgridWithAttachmentApi, String exactTargetApi) {
        this.url = url;
        this.postRetryCount = postRetryCount;
        this.postRetryIntervalExponential = postRetryIntervalExponential;
        this.requestFilters = new ArrayList<>();
        this.sendgridApi = sendgridApi;
        this.sendGridUploadAttachmentApi = sendgridWithAttachmentApi;
        this.exactTargetApi = exactTargetApi;
    }

    public void addRequestFilter(RequestFilter requestFilter) {
        requestFilters.add(requestFilter);
    }

}
