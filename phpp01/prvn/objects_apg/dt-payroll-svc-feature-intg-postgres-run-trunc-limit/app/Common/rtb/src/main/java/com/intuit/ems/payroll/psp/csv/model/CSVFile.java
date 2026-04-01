package com.intuit.ems.payroll.psp.csv.model;

import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import lombok.Getter;

@Getter
public class CSVFile {

    private String fileName;
    private FileRecord fileRecord;

    public CSVFile(String fileName, FileRecord fileRecord) {
        this.fileName = fileName;
        this.fileRecord = fileRecord;
    }
}
