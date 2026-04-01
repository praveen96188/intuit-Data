package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Getter
@Setter
public class SoxUserAdditionalPropertiesModel {
    @JsonProperty("access")
    private String access;
    @JsonProperty("status")
    private String status;
    @JsonProperty("profile")
    private String profile;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
