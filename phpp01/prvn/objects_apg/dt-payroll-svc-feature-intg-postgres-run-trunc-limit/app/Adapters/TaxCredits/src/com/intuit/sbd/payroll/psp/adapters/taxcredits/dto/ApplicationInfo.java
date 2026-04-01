package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

import java.util.List;

/**
 * User: dweinberg
 * Date: Sep 29, 2010
 * Time: 3:27:05 PM
 */
public class ApplicationInfo {
    private List<List<String>> proofDocumentGroups;
    private String password;

    public List<List<String>> getProofDocumentGroups() {
        return proofDocumentGroups;
    }

    public void setProofDocumentGroups(List<List<String>> proofDocumentGroups) {
        this.proofDocumentGroups = proofDocumentGroups;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
