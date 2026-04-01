package com.intuit.sbd.payroll.psp.batchjobs.soxReport.models;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SoxUserDataBatchModel {

    private String batchId;
    private List<SoxUserDataModel> userData;

    public SoxUserDataBatchModel(List<SoxUserDataModel> userData, String batchId){
        this.userData = userData;
        this.batchId = batchId;
    }
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
