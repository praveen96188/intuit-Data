package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.sql.Timestamp;

@Getter
@Setter
public class SoxUserHeaderModel {
    @JsonProperty("data_manager_name")
    private String dataManagerName;
    @JsonProperty("access_type")
    private String accessType;
    @JsonProperty("batch_id")
    private String batchId;
    @JsonProperty("total_no_of_records")
    private String totalNoOfRecords;
    @JsonProperty("data_query")
    private String dataQuery;
    @JsonProperty("created_date")
    private Timestamp createdDate;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this,"dataManagerName", "totalNoOfRecords", "createdDate");
    }
}
