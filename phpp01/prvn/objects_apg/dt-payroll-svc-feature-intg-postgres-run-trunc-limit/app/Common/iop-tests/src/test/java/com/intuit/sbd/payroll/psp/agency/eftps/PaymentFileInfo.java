package com.intuit.sbd.payroll.psp.agency.eftps;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 28, 2010
 * Time: 11:30:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileInfo {
    String groupId;
    List<PaymentFileSegmentInfo> paymentFileSegmentInfos = new ArrayList<PaymentFileSegmentInfo>();

    public String getGroupId() {
        return groupId;
    }

    public List<PaymentFileSegmentInfo> getPaymentFileSegmentInfos() {
        return paymentFileSegmentInfos;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setPaymentFileSegmentInfos(List<PaymentFileSegmentInfo> paymentFileSegmentInfos) {
        this.paymentFileSegmentInfos = paymentFileSegmentInfos;
    }

    public void addPaymentFileSegmentInfo(PaymentFileSegmentInfo paymentFileSegmentInfo){
        paymentFileSegmentInfos.add(paymentFileSegmentInfo);
    }
}
