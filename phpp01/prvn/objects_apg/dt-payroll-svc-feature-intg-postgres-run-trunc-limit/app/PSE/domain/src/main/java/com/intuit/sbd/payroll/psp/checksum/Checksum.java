package com.intuit.sbd.payroll.psp.checksum;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class Checksum {
    
    long partioningSize;
    
    long oldPartition;
    
    long partition;
 
    SpcfCalendar initiationDateTimeStart;
    
    SpcfCalendar initiationDateTimeEnd;
    
    SpcfCalendar ddDebitDateStart;
    
    SpcfCalendar ddDebitDateEnd;
    
    SpcfCalendar ddSettlementDateStart;
    
    SpcfCalendar ddSettlementDateEnd;
    
    SpcfCalendar checkDateStart;
    
    SpcfCalendar checkDateEnd;
    
    SpcfCalendar approvalDateTimeEnd;

    public long getPartition() {
        return partition;
    }
    public void setPartition(long partition) {
        this.partition = partition;
    }
    public long getPartioningSize() {
        return partioningSize;
    }
    public void setPartioningSize(long partioningSize) {
        this.partioningSize = partioningSize;
    }
    public long getOldPartition() {
        return oldPartition;
    }
    public void setOldPartition(long oldPartition) {
        this.oldPartition = oldPartition;
    }
    public SpcfCalendar getInitiationDateTimeStart() {
        return initiationDateTimeStart;
    }
    public void setInitiationDateTimeStart(SpcfCalendar initiationDateTimeStart) {
        this.initiationDateTimeStart = initiationDateTimeStart;
    }
    
    public SpcfCalendar getInitiationDateTimeEnd() {
        return initiationDateTimeEnd;
    }
    public void setInitiationDateTimeEnd(SpcfCalendar initiationDateTimeEnd) {
        this.initiationDateTimeEnd = initiationDateTimeEnd;
    }
    
    public SpcfCalendar getDdDebitDateStart() {
        return ddDebitDateStart;
    }
    public void setDdDebitDateStart(SpcfCalendar ddDebitDateStart) {
        this.ddDebitDateStart = ddDebitDateStart;
    }
    public SpcfCalendar getDdDebitDateEnd() {
        return ddDebitDateEnd;
    }
    
    public void setDdDebitDateEnd(SpcfCalendar ddDebitDateEnd) {
        this.ddDebitDateEnd = ddDebitDateEnd;
    }
    public SpcfCalendar getDdSettlementDateStart() {
        return ddSettlementDateStart;
    }
    public void setDdSettlementDateStart(SpcfCalendar ddSettlementDateStart) {
        this.ddSettlementDateStart = ddSettlementDateStart;
    }
    public SpcfCalendar getDdSettlementDateEnd() {
        return ddSettlementDateEnd;
    }
    public void setDdSettlementDateEnd(SpcfCalendar ddSettlementDateEnd) {
        this.ddSettlementDateEnd = ddSettlementDateEnd;
    }
    public SpcfCalendar getCheckDateStart() {
        return checkDateStart;
    }
    public void setCheckDateStart(SpcfCalendar checkDateStart) {
        this.checkDateStart = checkDateStart;
    }
    public SpcfCalendar getCheckDateEnd() {
        return checkDateEnd;
    }
    public void setCheckDateEnd(SpcfCalendar checkDateEnd) {
        this.checkDateEnd = checkDateEnd;
    }
    
    public SpcfCalendar getApprovalDateTimeEnd() {
        return approvalDateTimeEnd;
    }
    public void setApprovalDateTimeEnd(SpcfCalendar approvalDateTimeEnd) {
        this.approvalDateTimeEnd = approvalDateTimeEnd;
    }
}