package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.inputFileModel.LegacyToSymphonyInputFileModel;

import java.util.List;

public interface InputFileDeserializer {
    <T> List<T>  deserialize(String filePath);
}
