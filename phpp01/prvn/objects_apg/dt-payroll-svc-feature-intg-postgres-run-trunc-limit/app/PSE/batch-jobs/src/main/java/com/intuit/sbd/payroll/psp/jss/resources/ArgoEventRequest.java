package com.intuit.sbd.payroll.psp.jss.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ArgoEventRequest {

    private String fileName;

    public ArgoEventRequest() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
