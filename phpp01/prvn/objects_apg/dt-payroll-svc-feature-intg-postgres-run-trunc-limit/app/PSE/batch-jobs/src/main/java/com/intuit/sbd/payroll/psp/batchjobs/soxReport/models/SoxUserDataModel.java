package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.sql.Timestamp;

@Getter
@Setter
public class SoxUserDataModel {
    @JsonProperty("data_manager_name")
    private String dataManagerName;
    @JsonProperty("batch_id")
    private String batchId;
    @JsonProperty("access_type")
    private String accessType;
    @JsonProperty("message_id")
    private String transactionId;
    @JsonProperty("user_name")
    private String username;
    @JsonProperty("database_name")
    private String databaseName;
    @JsonProperty("created_date")
    private Timestamp createdDate;
    @JsonProperty("additional_properties")
    private SoxUserAdditionalPropertiesModel soxUserAdditionalPropertiesModel;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "dataManagerName", "batchId", "transactionId", "databaseName", "createdDate");
    }
}
