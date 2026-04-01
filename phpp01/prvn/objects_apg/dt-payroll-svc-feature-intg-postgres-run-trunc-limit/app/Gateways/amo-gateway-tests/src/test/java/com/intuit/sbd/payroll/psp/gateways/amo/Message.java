package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetChangeReasonType;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 15, 2010
 * Time: 8:27:17 AM
 */
public class Message {
    public SpcfCalendar transactionDate;
    public Collection<Entitlement> entitlements = new ArrayList<Entitlement>();
    public AssetChangeReasonType eventReason;
    public boolean includePayrollAsset = true;
    public boolean includeUsageAsset = false;
    public boolean isIncludeTrialAsset = false;
}
