package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: ihannur
 * Date: 8/31/12
 * Time: 3:03 PM
 */
public class StrikeDTO {
    String strikeReason;
    SpcfCalendar strikeEventTimestamp;

    public String getStrikeReason() {
        return strikeReason;
    }

    public void setStrikeReason(String pStrikeReason) {
        strikeReason = pStrikeReason;
    }

    public SpcfCalendar getStrikeEventTimestamp() {
        return strikeEventTimestamp;
    }

    public void setStrikeEventTimestamp(SpcfCalendar pStrikeEventTimestamp) {
        strikeEventTimestamp = pStrikeEventTimestamp;
    }
}
