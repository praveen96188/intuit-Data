package com.intuit.ems.payroll.psp.csv.model;

import lombok.Getter;

import java.util.List;

@Getter
public class FileRecord {

    private List<String> header;
    private List<List<String>> data;

    public FileRecord(List<String> header, List<List<String>> data) {
        this.header = header;
        this.data = data;
    }
}
