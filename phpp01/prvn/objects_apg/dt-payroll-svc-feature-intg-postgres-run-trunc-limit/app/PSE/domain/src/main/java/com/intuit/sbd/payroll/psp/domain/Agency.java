package com.intuit.sbd.payroll.psp.domain;

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IJurisdiction;
import com.intuit.payroll.agency.dao.DataStore;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

/**
 * Hand-written business logic
 */
public class Agency extends BaseAgency {
    public static final String IRS = "IRS";
    private static final DataStore mStore = DataStore.getDataStore();
    private transient IAgency mDelegate;
    public static final String FL_AGENT_ID = "FLDOR";
    public static String AgencyIdKeyName="Agency_AID";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Agency() {
        super();
    }

    protected IAgency getDelegate() {
        if (mDelegate != null) {
            return mDelegate;
        }
        //
        mDelegate = mStore.getAgency(super.getAgencyId());
        return mDelegate;
    }

    public IJurisdiction getJurisdiction() {
        return mStore.getJurisdiction(getDelegate().getJurisdictionID());
    }

    public String getDescription() {
        return getDelegate().getDescription();
    }

    public boolean isRAAEnrollmentRequired() {
        return getRAAEnrollmentRequired(); // !"SSA".equals(getAgencyId());
    }

    public boolean isRAFEnrollmentRequired() {
        return getRAFEnrollmentRequired(); // isIRS();
    }

    public boolean isACHEnrollmentRequired() {
        return getACHEnrollmentRequired(); // "CAEDD".equals(getAgencyId());
    }

    public boolean isIRS() {
        return IRS.equals(getAgencyId());
    }

}