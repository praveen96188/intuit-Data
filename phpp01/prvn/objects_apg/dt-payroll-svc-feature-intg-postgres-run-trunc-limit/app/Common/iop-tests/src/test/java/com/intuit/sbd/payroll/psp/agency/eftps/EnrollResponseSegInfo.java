package com.intuit.sbd.payroll.psp.agency.eftps;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 14, 2010
 * Time: 3:16:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollResponseSegInfo {
    String segmentId;
    List<String> transactionIds = new ArrayList<String>();

    public EnrollResponseSegInfo(String segmentId) {
        this.segmentId = segmentId;
    }

    public EnrollResponseSegInfo(String pSegmentId, List<String> pTransactionIds) {
        this.segmentId = pSegmentId;
        this.transactionIds = pTransactionIds;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public List<String> getTransactionIds() {
        return transactionIds;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public void setTransactionIds(List<String> transactionIds) {
        this.transactionIds = transactionIds;
    }

    public void addTransactioId(String pTransId){
        transactionIds.add(pTransId);
    }
}
