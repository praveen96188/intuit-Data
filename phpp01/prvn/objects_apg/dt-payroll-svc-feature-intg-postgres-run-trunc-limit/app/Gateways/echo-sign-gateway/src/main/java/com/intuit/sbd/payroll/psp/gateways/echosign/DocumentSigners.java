package com.intuit.sbd.payroll.psp.gateways.echosign;

import java.util.List;

/**
 * User: dweinberg
 * Date: Sep 27, 2010
 * Time: 1:00:02 PM
 */
public class DocumentSigners {
    private String docId;
    private List<String> signersRemaining;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public List<String> getSignersRemaining() {
        return signersRemaining;
    }

    public void setSignersRemaining(List<String> signersRemaining) {
        this.signersRemaining = signersRemaining;
    }
}
