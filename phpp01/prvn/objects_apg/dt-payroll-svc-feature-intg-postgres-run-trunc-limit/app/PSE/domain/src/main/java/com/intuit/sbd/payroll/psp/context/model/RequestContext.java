package com.intuit.sbd.payroll.psp.context.model;

import lombok.*;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RequestContext {
    private CompanyInfo companyInfo;
    private RequestType requestType;
    private String requestOperation;
    @Builder.Default
    private AtomicInteger refCount = new AtomicInteger(1);
    public RequestContext(RequestContext requestContextOld)
    {

        if(requestContextOld.getCompanyInfo() != null) {

            this.companyInfo = CompanyInfo.builder()
                    .psid(requestContextOld.getCompanyInfo().getPsid())
                    .realmId(requestContextOld.getCompanyInfo().getRealmId())
                    .companySequence(requestContextOld.getCompanyInfo().getCompanySequence())
                    .build();
        }
        this.requestType=requestContextOld.getRequestType();
        this.requestOperation=requestContextOld.getRequestOperation();
        this.refCount = new AtomicInteger(1);
    }
}


