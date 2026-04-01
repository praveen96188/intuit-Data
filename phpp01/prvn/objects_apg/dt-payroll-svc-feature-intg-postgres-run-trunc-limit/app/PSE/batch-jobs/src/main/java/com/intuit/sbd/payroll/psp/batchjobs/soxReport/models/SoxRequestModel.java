package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;

@Getter
@Setter
public class SoxRequestModel {
    @JsonProperty("batch_header")
    private SoxUserHeaderModel header;
    @JsonProperty("batch_data")
    private List<SoxUserDataBatchModel> userDataBatches;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
